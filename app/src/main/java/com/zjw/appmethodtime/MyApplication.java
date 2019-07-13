package com.zjw.appmethodtime;

import android.app.Application;
import android.content.res.Configuration;
import android.util.Log;

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
        //自定义MyLogDetector lint error提示
        Log.i("TAG", "啊啊啊啊，我被发现了！");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
