package com.delsart.bookdownload.ui.fragment;


import android.os.Handler;

import com.delsart.bookdownload.service.BaseService;
import com.delsart.bookdownload.service.SanQiuShuWuService;

public class SanQiuShuWuFragment extends BaseFragment {

    @Override
    protected BaseService getService(Handler handler, String keywords) {
        return new SanQiuShuWuService(handler, keywords);
    }
}
