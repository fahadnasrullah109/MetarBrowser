package com.metar.browser;

import android.app.Application;

import com.metar.browser.utils.NetworkHelper;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        new NetworkHelper().setContext(this);
    }
}
