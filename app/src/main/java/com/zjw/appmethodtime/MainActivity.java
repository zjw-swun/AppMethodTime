package com.zjw.appmethodtime;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.zjw.mylibrary.Bean;
import com.zjw.mylibrary.LibActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.zjw.appmethodtime.R.id.tv;

public class MainActivity extends AppCompatActivity {

    protected Button mTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_main);
        initView();
        EventBus.getDefault().register(this);
    }

    public void onClick(View v) {
        if (v.getId() == tv) {
            sayHello();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMain(EventBus event){
        Toast.makeText(this, "onEventMain!", Toast.LENGTH_SHORT).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMain(Bean event){
        Toast.makeText(this, "onEventMain!", Toast.LENGTH_SHORT).show();
    }

    @CostTime
    public void sayHello() {
        Toast.makeText(this, "Hello!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.setClass(MainActivity.this,LibActivity.class);
        startActivity(intent);
        EventBus.getDefault().post(new Bean());
    }

    private void initView() {
        mTv = (Button) findViewById(R.id.tv);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);

    }
}
