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
import java.util.IllegalFormatCodePointException;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZhiXuanService extends BaseService {
    private static String TAG = "test";
    private final Handler mHandler;
    private int mPage;
    private String mBaseUrl;
    private CountDownLatch latch;
    private ArrayList<NovelBean> list = new ArrayList<>();

    public ZhiXuanService(Handler handler, String keywords) {
        super(handler, keywords);
        this.mHandler = handler;
        mPage = 1;
        mBaseUrl = Url.ZHIXUAN + keywords + "&page=";
    }

    String lasts = "";

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
                            .select("#plist");
//                            .select("div.info");
//                            .select("#pleft > dlid=\\\"plist\\\"");
                    latch = new CountDownLatch(select.size());
                    for (int i = 0; i < select.size(); i++) {
                        runInSameTime(select.get(i));
                    }
                    latch.await();
                    if (select.toString().equals(lasts))
                        list.clear();
                    lasts = select.toString();
                    mPage++;
                    Message msg = mHandler.obtainMessage();
                    msg.what = MsgType.SUCCESS;
                    msg.obj = list;
                    mHandler.sendMessage(msg);
                } catch (Exception e) {
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
                String url = element.select("dt > a").attr("abs:href");
                Document document = null;
                try {
                    document = Jsoup.connect(url)
                            .ignoreContentType(true)
                            .ignoreHttpErrors(true)
                            .userAgent(Url.MOBBILE_AGENT)
                            .get();

                    String t = document.select("#content > div:nth-child(4) > p:nth-child(2)").text();
                    String name = document.select("#content > h1").text().replace("（校对版全本）", "").replace("（整理版下载）", "").replace("（精校版全本）", "").replace("（精校版下载）", "").replace("（精校版下载）", "").replace("《", "").replace("》", "");
                    String time = "";
                    String info = "";
                    Matcher m = Pattern.compile("【内容简介】.+").matcher(t);
                    if (m.find())
                        info = m.group(0).replace("　　", "\n").replace("      ", "\n").replace("【内容简介】： \n", "");
                    String category = document.select("#content > p > a:nth-child(2)").text();
                    String status = "";
                    String author = "";
                    if (name.contains("作者")) {
                        author = name.substring(name.indexOf("作者"), name.length());
                        name = name.replace(author, "");
                    }
                    String words = "";
                    if (t.contains("【内容简介"))
                        words = t.substring(0, t.indexOf("【内容简介")).replace("【", "").replace("】", "");
                    String pic = document.select("#content > div:nth-child(3) > span > a > img").attr("abs:src");
                    String durl = document.select("#content > div:nth-child(4) > div.pagefujian > div.down_2 > a").attr("abs:href");
                    NovelBean no = new NovelBean(name, time, info, category, status, author, words, pic, durl);
                    list.add(no);
                } catch (Exception e) {
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
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                Document document = null;
                try {
                    document = Jsoup.connect(url)
                            .timeout(10000)
                            .ignoreContentType(true)
                            .ignoreHttpErrors(true)
                            .userAgent(Url.MOBBILE_AGENT)
                            .get();
                    Elements elements = document.select("body > div.wrap > div.panel > div > span");
                    for (Element element : elements) {
                        String URL = element.select("a").attr("abs:href");
                        if (URL != null && !URL.equals("")) {
                            urls.add(new DownloadBean(element.text(), element.select("a").attr("abs:href")));
                        }
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
