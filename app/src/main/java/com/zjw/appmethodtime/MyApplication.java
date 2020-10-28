package com.zjw.appmethodtime;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

import com.google.gson.Gson;

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
        Test(new Gson());
        test();
        test1();
    }

    void test(){
        long startTime = System.nanoTime();
        System.nanoTime();
        long endTime = System.nanoTime();
        Log.i("TAG", "test cost is " + ((endTime - startTime)*1.0f / 1000000f));
    }

    //test cost is 0.001042 (说明System.nanoTime()要占0.001042毫秒)
    //test1 cost is 0.013073 (说明log本身要占0.01毫秒)
    //所以我们只需要关注 精度到0.1毫秒
    void test1(){
        long startTime = System.nanoTime();
        Log.i("TAG", "log cost is " + (100-50) / 1000000f);
        long endTime = System.nanoTime();
        Log.i("TAG", "test1 cost is " + (endTime - startTime)*1.0 / 1000000f);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    private void Test(Gson gson){
        Log.e("TAG",""+gson.toString());
    }
}
