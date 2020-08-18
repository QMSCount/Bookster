package com.delsart.bookdownload.service;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.delsart.bookdownload.MsgType;
import com.delsart.bookdownload.Url;
import com.delsart.bookdownload.bean.DownloadBean;
import com.delsart.bookdownload.bean.NovelBean;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MianHuaTangService extends BaseService {
    private final Handler mHandler;
    private int mPage;
    private String mBaseUrl;
    private CountDownLatch latch;
    private ArrayList<NovelBean> list = new ArrayList<>();

    public MianHuaTangService(Handler handler, String keywords) {
        super(handler, keywords);
        this.mHandler = handler;
        mPage = 1;
        try {
            mBaseUrl = Url.MHT + URLEncoder.encode(keywords, "GB2312");
        }
        catch (Exception e){
            //
        }
    }

    @Override
    public void get() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    list.clear();
                    SimpleDateFormat bjSdf = new SimpleDateFormat("yyyy-M-dd_HH:mm:ss:SSS");
                    bjSdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
                    String cuid = "www.mianhuatang520.com_" + bjSdf.format(new Date()) + "_" + (int) (Math.random() * 1000 + 1);

                    Elements select = Jsoup.connect(mBaseUrl)
                            .timeout(10000)
                            .ignoreContentType(true)
                            .ignoreHttpErrors(true)
                            .userAgent(Url.MOBBILE_AGENT)
                            .referrer(mBaseUrl)
                            .cookie("cuid", cuid)
                            .get()
                            .select("#newscontent > div.l > ul > li > span.s2 > a");
                    latch = new CountDownLatch(select.size());
                    for (int i = 0; i < select.size(); i++) {
                        runInSameTime(select.get(i));
                    }

                    latch.await();
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
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Document document = Jsoup.connect(element.attr("abs:href"))
                            .timeout(5000)
                            .ignoreContentType(true)
                            .userAgent(Url.PC_AGENT)
                            .get();
                    Elements elements = document.select("#maininfo");
                    String name = elements.select("#info > h1").text();
                    String status = "";
                    String time = "";
                    String info = elements.select("#intro > p:nth-child(1)").text();
                    String category =  "";
                    String author = elements.select("#info > div:nth-child(2)").text();
                    String words =  "";
                    String urlInfo=elements.select("#info > div:nth-child(3) > script").html();

                    String bookid="";
                    String key="";
                    Pattern bookidPattern = Pattern.compile("'bookid=[0-9]+'");
                    Matcher matcher1 = bookidPattern.matcher(urlInfo);
                    while (matcher1.find()) {
                        bookid=matcher1.group(0).replace("'","");
                        break;
                    }

                    Pattern keyPattern = Pattern.compile("'&txtkey=[\\S]+'");
                    Matcher matcher2 = keyPattern.matcher(urlInfo);
                    while (matcher2.find()) {
                        key=matcher2.group(0).replace("'", "");
                        break;
                    }

                    String url = "http://www.mianhuatang520.com/download.aspx?"+bookid+key;
                    String pic =  "";
                    list.add(new NovelBean(name, time, info, category, status, author, words, pic, url));
                }
                catch (Exception e) {
                    int a=1;

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
                    String u1n = "txt下载";
                    urls.add(new DownloadBean(u1n, url));
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