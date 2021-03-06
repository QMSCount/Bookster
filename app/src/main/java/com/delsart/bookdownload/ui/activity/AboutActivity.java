package com.delsart.bookdownload.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import com.danielstone.materialaboutlibrary.ConvenienceBuilder;
import com.danielstone.materialaboutlibrary.MaterialAboutActivity;
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem;
import com.danielstone.materialaboutlibrary.items.MaterialAboutItemOnClickAction;
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem;
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard;
import com.danielstone.materialaboutlibrary.model.MaterialAboutList;
import com.danielstone.materialaboutlibrary.util.ViewTypeManager;
import com.delsart.bookdownload.MyApplication;
import com.delsart.bookdownload.R;
import com.delsart.bookdownload.Url;
import com.delsart.bookdownload.custom.MyViewTypeManager;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.net.URL;

import moe.feng.alipay.zerosdk.AlipayZeroSdk;

/**
 * Created by Delsart on 2017/7/24.
 * Modify by 狂飙的犀牛 on 2019/04/26
 */

public class AboutActivity extends MaterialAboutActivity {
    public static final String THEME_EXTRA = "";
    public static final int THEME_LIGHT_LIGHTBAR = 0;
    public static final int THEME_LIGHT_DARKBAR = 1;
    public static final int THEME_DARK_LIGHTBAR = 2;
    public static final int THEME_DARK_DARKBAR = 3;
    public static final int THEME_CUSTOM_CARDVIEW = 4;

    protected int colorIcon = R.color.DayColor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false)) {
            setTheme(R.style.DarkTheme_aboutTheme);
            colorIcon = R.color.DarkColor;
        }
        else
            setTheme(R.style.aboutTheme);
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    protected MaterialAboutList getMaterialAboutList(@NonNull final Context c) {
        final Activity activity = this;
        MaterialAboutCard.Builder appCardBuilder = new MaterialAboutCard.Builder();
        // Add items to card
        appCardBuilder.addItem(new MaterialAboutTitleItem.Builder()
                .text(R.string.app_name)
                .desc("© 2020 狂飙的犀牛")
                .icon(R.mipmap.ic_launcher)
                .build());

        try {
            appCardBuilder.addItem(ConvenienceBuilder.createVersionActionItem(c,
                    new IconicsDrawable(c)
                            .icon(CommunityMaterial.Icon.cmd_information_outline)
                            .color(ContextCompat.getColor(c, colorIcon))
                            .sizeDp(18),
                    "版本",
                    false));

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        appCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text("更新日志")
                .icon(new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_history)
                        .color(ContextCompat.getColor(c, colorIcon))
                        .sizeDp(18))
                .setOnClickAction(ConvenienceBuilder.createWebViewDialogOnClickAction(c, "", "https://github.com/tobeforever/Bookster/blob/master/%E6%9B%B4%E6%96%B0%E6%97%A5%E5%BF%97.txt", true, false))
                .build());

        MaterialAboutCard.Builder authorCardBuilder = new MaterialAboutCard.Builder();
        authorCardBuilder.title("作者相关");
//        authorCardBuilder.titleColor(ContextCompat.getColor(c, R.color.colorAccent));

        authorCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text("Delsart")
                .subText("作者 , 中国")
                .icon(new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_account)
                        .color(ContextCompat.getColor(c, colorIcon))
                        .sizeDp(18))
                .setOnClickAction(
                        ConvenienceBuilder.createWebsiteOnClickAction(c, Uri.parse("https://www.coolapk.com/u/473036")))
                .build());

        authorCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text("狂飙的犀牛")
                .subText("维护者 , 中国")
                .icon(new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_account)
                        .color(ContextCompat.getColor(c, colorIcon))
                        .sizeDp(18))
                .setOnClickAction(
                        ConvenienceBuilder.createWebsiteOnClickAction(c, Uri.parse("http://www.coolapk.com/u/1033370")))
                .build());

        authorCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text("Archie")
                .subText("图标设计者 , 中国")
                .icon(new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_account)
                        .color(ContextCompat.getColor(c, colorIcon))
                        .sizeDp(18))
                .setOnClickAction(ConvenienceBuilder.createWebsiteOnClickAction(c, Uri.parse("https://www.coolapk.com/u/801526")))
                .build());

        authorCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text("Fairyex")
                .subText("空视图设计者 , 中国")
                .icon(new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_account)
                        .color(ContextCompat.getColor(c, colorIcon))
                        .sizeDp(18))
                .setOnClickAction(ConvenienceBuilder.createWebsiteOnClickAction(c, Uri.parse("http://www.coolapk.com/u/466253")))
                .build());

        authorCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text("Used-open-sources")
                .icon(new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_android_debug_bridge)
                        .color(ContextCompat.getColor(c, colorIcon))
                        .sizeDp(18))
                .setOnClickAction(new MaterialAboutItemOnClickAction() {
                    @Override
                    public void onClick() {
                        Intent intent = new Intent(c, UsedOpenSource.class);
                        intent.putExtra(AboutActivity.THEME_EXTRA, getIntent().getIntExtra(THEME_EXTRA, THEME_LIGHT_DARKBAR));
                        c.startActivity(intent);
                    }
                })
                .build());

        authorCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text("Fork on GitHub")
                .icon(new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_github_circle)
                        .color(ContextCompat.getColor(c, colorIcon))
                        .sizeDp(18))
                .setOnClickAction(ConvenienceBuilder.createWebsiteOnClickAction(c, Uri.parse("https://github.com/tobeforever/Bookster")))
                .build());

        MaterialAboutCard.Builder convenienceCardBuilder = new MaterialAboutCard.Builder();
        convenienceCardBuilder.title("更多");

        convenienceCardBuilder.addItem(ConvenienceBuilder.createRateActionItem(c,
                new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_star)
                        .color(ContextCompat.getColor(c, colorIcon))
                        .sizeDp(18),
                "为这个应用评分",
                null
        ));

        convenienceCardBuilder.addItem(ConvenienceBuilder.createEmailItem(c,
                new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_email)
                        .color(ContextCompat.getColor(c, colorIcon))
                        .sizeDp(18),
                "发送邮件",
                true,
                "2643363070@qq.com",
                "Question concerning MaterialAboutLibrary"));

        convenienceCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text("捐赠，请维护者 @狂飙的犀牛 喝瓶快乐水（微信）")
                .icon(new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_coffee)
                        .color(ContextCompat.getColor(c, colorIcon))
                        .sizeDp(18))
                .setOnClickAction(new MaterialAboutItemOnClickAction() {
                    @Override
                    public void onClick() {
                        Uri uri = Uri.parse("https://miao.su/images/2019/08/13/weixin0d829.jpg");
                        startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    }
                })
                .build());

        convenienceCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text("捐赠，请作者 @Delsart 喝杯果汁（微信）")
                .icon(new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_coffee)
                        .color(ContextCompat.getColor(c, colorIcon))
                        .sizeDp(18))
                .setOnClickAction(new MaterialAboutItemOnClickAction() {
                    @Override
                    public void onClick() {
                        Uri uri = Uri.parse("http://a3.qpic.cn/psb?/V10dNxbX00vsuB/eKAX6FA4sv5Y3Tnb.lrqpj6OjMWE6QuHyv2Z*h2MBtk!/b/dGoBAAAAAAAA&bo=.AJSA*gCUgMDCSw!&rf=viewer_4");
                        startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    }
                })
                .build());

        MaterialAboutCard.Builder otherCardBuilder = new MaterialAboutCard.Builder();
        otherCardBuilder.title("来源");
        otherCardBuilder.addItem(ConvenienceBuilder.createWebsiteActionItem(c,
                new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_earth)
                        .color(ContextCompat.getColor(c, colorIcon))
                        .sizeDp(18),
                "爱下小说",
                true,
                Uri.parse("https://m.ixdzs.com")));

        otherCardBuilder.addItem(ConvenienceBuilder.createWebsiteActionItem(c,
                new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_earth)
                        .color(ContextCompat.getColor(c, colorIcon))
                        .sizeDp(18),
                "知轩藏书1",
                true,
                Uri.parse("http://www.zxcs.me")));

        otherCardBuilder.addItem(ConvenienceBuilder.createWebsiteActionItem(c,
                new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_earth)
                        .color(ContextCompat.getColor(c, colorIcon))
                        .sizeDp(18),
                "知轩藏书2",
                true,
                Uri.parse("http://www.zxcs.info")));

        otherCardBuilder.addItem(ConvenienceBuilder.createWebsiteActionItem(c,
                new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_earth)
                        .color(ContextCompat.getColor(c, colorIcon))
                        .sizeDp(18),
                "棉花糖小说网",
                true,
                Uri.parse("http://www.mianhuatang520.com/")));

        otherCardBuilder.addItem(ConvenienceBuilder.createWebsiteActionItem(c,
                new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_earth)
                        .color(ContextCompat.getColor(c, colorIcon))
                        .sizeDp(18),
                "精校藏书",
                true,
                Uri.parse("https://jingjiaocangshu.cn/")));

        return new MaterialAboutList(appCardBuilder.build(), authorCardBuilder.build(), convenienceCardBuilder.build(), otherCardBuilder.build());
    }

    @Override
    protected CharSequence getActivityTitle() {
        return "关于";
    }

    @NonNull
    @Override
    protected ViewTypeManager getViewTypeManager() {
        return new MyViewTypeManager();
    }
}