package com.delsart.bookdownload.ui.fragment;


import android.os.Handler;

import com.delsart.bookdownload.service.BaseService;
import com.delsart.bookdownload.service.ZhiXuanService1;

public class ZhiXuanFragment1 extends BaseFragment {

    @Override
    protected BaseService getService(Handler handler, String keywords) {
        return new ZhiXuanService1(handler, keywords);
    }
}
