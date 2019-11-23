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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SanQiuShuWuService extends BaseService {
    private final Handler mHandler;
    private int mPage;
    private String mBaseUrl;
    private CountDownLatch latch;
    private ArrayList<NovelBean> list = new ArrayList<>();

    public SanQiuShuWuService(Handler handler, String keywords) {
        super(handler, keywords);
        this.mHandler = handler;
        mPage = 1;
        mBaseUrl = Url.SANQIU + keywords;
    }

    String lasts="";

    @Override
    public void get() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    list.clear();
                    Elements select = Jsoup.connect(mBaseUrl.replace("0", mPage + ""))
                            .timeout(10000)
                            .ignoreContentType(true)
                            .ignoreHttpErrors(true)
                            .userAgent(Url.MOBBILE_AGENT)
                            .get()
                            .select("#main > article > div > div.kratos-post-inner-new > header > h2 > a");
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
                String url = element.attr("abs:href");
                Document document = null;
                try {
                    document = Jsoup.connect(url)
                            .ignoreContentType(true)
                            .ignoreHttpErrors(true)
                            .userAgent(Url.MOBBILE_AGENT)
                            .get();

                    String name = document.select("#paydown > div.down-detail > p.down-price > span").text();
                    String time = document.select("#paydown > div.down-detail > p.down-ordinary").text();
                    String info = document.select("#main > article > div.kratos-hentry.kratos-post-inner.clearfix > div.kratos-post-content > p").text();
                    String category = document.select("#main > article > div.kratos-hentry.kratos-post-inner.clearfix > footer > div.footer-tag.clearfix > div.pull-left").text();
                    String status = "";
                    String author = document.select(" #main > article > div.kratos-hentry.kratos-post-inner.clearfix > div.kratos-post-content > p:nth-child(5)").text();
                    String words = "";
                    String pic = document.select("#main > article > div.kratos-hentry.kratos-post-inner.clearfix > div.kratos-post-content > p:nth-child(1) > img").attr("abs:src");
                    String durl = document.select("#paydown > div.down-detail > p:nth-child(5) > strong > a").attr("abs:href");
                    NovelBean no = new NovelBean(name, time, info, category, status, author, words, pic, durl);
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
                Document document = null;
                try {
                    document = Jsoup.connect(url)
                            .timeout(10000)
                            .ignoreContentType(true)
                            .ignoreHttpErrors(true)
                            .userAgent(Url.MOBBILE_AGENT)
                            .get();

                    String u1 = "";
                    String u1n = document.select("body > div.wrap > div.content > div.plus_box > div.plus_l > ul > li:nth-child(4)").text();
                    Elements elements = document.select("body > div.wrap > div.content > div:nth-child(4) > div.panel-body > span:nth-child(4) > a");
                    urls.add(new DownloadBean(u1n, u1));
                    for (Element element : elements) {
                        urls.add(new DownloadBean(element.text(), element.attr("abs:href")));
                    }
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
