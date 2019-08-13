package com.delsart.bookdownload.service;

import android.os.Handler;
import android.os.Message;

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

public class ZhouDuService extends BaseService {
    private static String TAG = "test";
    private final Handler mHandler;
    private int mPage;
    private String mBaseUrl;
    private CountDownLatch latch;
    private ArrayList<NovelBean> list = new ArrayList<>();

    public ZhouDuService(Handler handler, String keywords) {
        super(handler, keywords);
        this.mHandler = handler;
        mPage = 1;
        mBaseUrl = Url.ZHOUDU + keywords + "&p=";
    }

    String lasts="";
    @Override
    public void get() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    list.clear();
                    Elements select = Jsoup.connect(mBaseUrl + mPage)
                            .timeout(10000)
                            .ignoreContentType(true)
                            .ignoreHttpErrors(true)
                            .userAgent(Url.MOBBILE_AGENT)
                            .get()
                            .select("body > div.w_news > dl > dd > a");
                    latch = new CountDownLatch(select.size());
                    for (int i = 0; i < select.size(); i++) {
                        runInSameTime(select.get(i));
                    }
                    latch.await();
                    if (select.toString().equals(lasts))
                        list.clear();
                    lasts=select.toString();
                    mPage++;
                    Message msg = mHandler.obtainMessage();
                    msg.what = MsgType.SUCCESS;
                    msg.obj = list;
                    mHandler.sendMessage(msg);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Message msg = mHandler.obtainMessage();
                    msg.what = MsgType.ERROR;
                    mHandler.sendMessage(msg);
                }
            }
        }).start();
    }

    private void runInSameTime(final Element element) throws IOException {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Document document = Jsoup.connect(element.attr("abs:href"))
                            .ignoreContentType(true)
                            .ignoreHttpErrors(true)
                            .get();

                    String t = document.select("body > div.news_detail > div.content").text();
                    if (t.length() < 5) {
                        latch.countDown();
                        return;
                    }
                    String name = document.select("body > div.news_detail > div.content > p:nth-child(1)").text();
                    String status = "";
                    String time = "";
                    String info = document.select("body > div.news_detail > div.content > p:nth-child(5)").text();
                    String category = "";
                    String author = document.select("body > div.news_detail > div.content > p:nth-child(2)").text();
                    String words = "";

                    String url = element.attr("abs:href");
                    String pic = document.select("body > div.news_detail > div.tu > img").attr("abs:src");
                    NovelBean no = new NovelBean(name, time, info, category, status, author, words, pic, url);
                    list.add(no);
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
                    Document document = Jsoup.connect(url)
                            .ignoreContentType(true)
                            .ignoreHttpErrors(true)
                            .get();

                    String u1 = document.select("#guanzhu > div.pic > p:nth-child(1) > a").attr("href");
                    String u1n = document.select("#guanzhu > div.pic > p:nth-child(1) > a").text();
                    String u2 = document.select("#guanzhu > div.pic > p:nth-child(2) > a").attr("href");
                    String u2n = document.select("#guanzhu > div.pic > p:nth-child(2) > a").text();
                    String u3 = document.select("#guanzhu > div.pic > p:nth-child(3) > a").attr("href");
                    String u3n = document.select("#guanzhu > div.pic > p:nth-child(3) > a").text();
                    if (!u1n.isEmpty()){
                        urls.add(new DownloadBean(u1n, u1));
                    }
                    if (!u2n.isEmpty()){
                        urls.add(new DownloadBean(u2n, u2));
                    }
                    if (!u3n.isEmpty()){
                        urls.add(new DownloadBean(u3n, u3));
                    }
                } catch (Exception e) {
                    //
                }
                latch.countDown();
            }
        });
        latch.await();
        return urls;
    }
}