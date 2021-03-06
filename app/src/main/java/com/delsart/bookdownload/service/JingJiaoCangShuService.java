package com.delsart.bookdownload.service;

import android.os.Handler;
import android.os.Message;

import com.delsart.bookdownload.MsgType;
import com.delsart.bookdownload.Url;
import com.delsart.bookdownload.bean.DownloadBean;
import com.delsart.bookdownload.bean.NovelBean;

import com.alibaba.fastjson.JSONObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class JingJiaoCangShuService extends BaseService {
    private final Handler mHandler;
    private int mPage;
    private String mBaseUrl;
    private CountDownLatch latch;
    private ArrayList<NovelBean> list = new ArrayList<>();

    public JingJiaoCangShuService(Handler handler, String keywords) {
        super(handler, keywords);
        this.mHandler = handler;
        mPage = 1;
        mBaseUrl = Url.JJCS + "page/00?s=" + keywords;
    }

    String lasts = "";

    @Override
    public void get() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    list.clear();
                    Elements select = Jsoup.connect(mBaseUrl.replace("00", mPage + ""))
                            .timeout(10000)
                            .ignoreContentType(true)
                            .ignoreHttpErrors(true)
                            .userAgent(Url.MOBBILE_AGENT)
                            .get()
                            .select("#main-content > div.container-fluid > div > section > div > div.widget-content > ul > li");
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
                String url = element.select("article > a").attr("abs:href");
                Document document = null;
                try {
                    document = Jsoup.connect(url)
                            .timeout(10000)
                            .ignoreContentType(true)
                            .ignoreHttpErrors(true)
                            .userAgent(Url.MOBBILE_AGENT)
                            .get();

                    String name = document.select("div.entry > p:nth-child(2)").text();
                    String author = document.select("div.entry > p:nth-child(3)").text();
                    String status = document.select("div.entry > p:nth-child(4)").text();
                    String words = document.select("div.entry > p:nth-child(6)").text();
                    String category = document.select("#post-header > p > span.cat > a").text();
                    String time = "";
                    String info = "小说简介：\r\n" + document.select("div.entry > p:nth-child(9)").text();
                    String pic = document.select("div.entry > p:nth-child(1) > img").attr("abs:src");
                    String bookid = document.select("#comment_post_ID").attr("value");
//                    String durl = document.select("#J_DLIPPCont > div > div.dlipp-cont-bd > a").attr("abs:href");
                    NovelBean no = new NovelBean(name, time, info, category, status, author, words, pic, bookid);
                    list.add(no);
                } catch (Exception e) {
                    //
                }
                latch.countDown();
            }
        });
    }

    @Override
    public ArrayList<DownloadBean> getDownloadurls(final String bookid) throws InterruptedException {
        latch = new CountDownLatch(1);
        final ArrayList<DownloadBean> urls = new ArrayList<>();
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Document doc = Jsoup.connect(Url.JJCS + "wp-admin/admin-ajax.php")
                            .timeout(10000)
                            .ignoreContentType(true)
                            .ignoreHttpErrors(true)
                            .userAgent(Url.MOBBILE_AGENT)
                            .data("action", "wb_dlipp_front",
                                    "pid", bookid,
                                    "rid", "local")
                            .post();

                    JSONObject jsonObject = JSONObject.parseObject(doc.select("body").html()).getJSONObject("data");
                    String url = (String)jsonObject.get("url");
                    urls.add(new DownloadBean("解压密码：jjcs", url));

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
