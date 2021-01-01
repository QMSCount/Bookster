package com.delsart.bookdownload.ui.fragment;


import android.os.Handler;

import com.delsart.bookdownload.service.BaseService;
import com.delsart.bookdownload.service.ZhiXuanService2;

public class ZhiXuanFragment2 extends BaseFragment {

    @Override
    protected BaseService getService(Handler handler, String keywords) {
        return new ZhiXuanService2(handler, keywords);
    }
}
