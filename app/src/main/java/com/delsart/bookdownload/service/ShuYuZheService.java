package com.delsart.bookdownload.service;

import android.icu.text.LocaleDisplayNames;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.delsart.bookdownload.MsgType;
import com.delsart.bookdownload.Url;
import com.delsart.bookdownload.bean.DownloadBean;
import com.delsart.bookdownload.bean.NovelBean;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.jar.Attributes;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShuYuZheService extends BaseService {
    private static String TAG = "test";
    private final Handler mHandler;
    private int mPage;
    private String mBaseUrl;
    private CountDownLatch latch;
    private ArrayList<NovelBean> list = new ArrayList<>();

    public ShuYuZheService(Handler handler, String keywords) {
        super(handler, keywords);
        this.mHandler = handler;
        mPage = 1;
        mBaseUrl = Url.SHUYUZHE + keywords;
    }

    @Override
    public void get() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    list.clear();
                    //Log.d(TAG, "run: " + mBaseUrl + "/" + mPage);
                    Elements select = Jsoup.connect(mBaseUrl + "/" + mPage)
                            .timeout(5000)
                            .ignoreContentType(true)
                            .ignoreHttpErrors(true)
                            .userAgent(Url.PC_AGENT)
                            .get()
                            .select("body > main > div.row > div.col-md-9 > div > table > tbody > tr");
                    latch = new CountDownLatch(select.size() - 1);
                    for (int i = 1; i < select.size(); i++) {
                        runInSameTime(select.get(i));
                    }

                    Log.d(TAG, "run: " + select.size());
                    latch.await();
                    mPage++;

                    Message msg = mHandler.obtainMessage();
                    msg.what = MsgType.SUCCESS;
                    msg.obj = list;
                    mHandler.sendMessage(msg);

                } catch (IOException e) {
                    e.printStackTrace();
                    Message msg = mHandler.obtainMessage();
                    msg.what = MsgType.ERROR;
                    mHandler.sendMessage(msg);
                }
                catch (Exception e) {
                    //
                }
            }
        }).start();
    }

    private void runInSameTime(final Element element) throws IOException {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Document document = Jsoup.connect(element.select("td:nth-child(1) > a").attr("abs:href"))
                            .timeout(5000)
                            .ignoreContentType(true)
                            .userAgent(Url.PC_AGENT)
                            .get();
                    Elements elements = document.select("body > main > div.row > div > div > div > div.col-md-8 > ul");
                    String name = elements.select("li").get(1).text();
                    String status = elements.select("li").get(5).text();
                    String time = elements.select("li").get(4).text();
                    String info = elements.select("li").get(6).text();
                    String category = elements.select("li").get(3).text();
                    String author = elements.select("li").get(2).text();
                    String words = elements.select("li").get(7).text();
                    String url = elements.select("li").get(8).select("a:nth-child(2)").attr("href");
                    String pic = document.select("body > main > div.row > div > div > div > div.col-md-2 > img").attr("abs:src");
                    list.add(new NovelBean(name, time, info, category, status, author, words, pic, url));
                }
                catch (Exception e) {
                    //
                }
                latch.countDown();
            }
        });
    }

    @Override
    public ArrayList<DownloadBean> getDownloadurls(final String url) throws InterruptedException {
        latch = new CountDownLatch(1);
        final ArrayList<DownloadBean> urls = new ArrayList<>();
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Elements elements = Jsoup.connect(url)
                            .timeout(10000)
                            .ignoreContentType(true)
                            .ignoreHttpErrors(true)
                            .userAgent(Url.MOBBILE_AGENT)
                            .get()
                            .select("body");
                    String u1 = elements.select("a").attr("abs:href");
                    String u1n = elements.select("h3").text().replace("欢迎使用","");
                    urls.add(new DownloadBean(u1n, u1));
                }
                catch (Exception e) {
                    //
                }

                latch.countDown();
            }
        });
        latch.await();
        return urls;
    }

}