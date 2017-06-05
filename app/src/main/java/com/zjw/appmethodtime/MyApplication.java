package com.zjw.appmethodtime;

import android.app.Application;
import android.content.res.Configuration;

/**
 * Created by hasee on 2017/6/5.
 */

public class MyApplication extends Application {

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
