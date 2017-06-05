package com.zjw.appmethodtime;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    protected TextView mTextView;
    private String TAG = "TAG";

    @CostTime
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_main);
        initView();
    }

    @CostTime
    private void initView() {
        mTextView = (TextView) findViewById(R.id.text_view);
        mTextView.setOnClickListener(MainActivity.this);
    }

    @CostTime
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.text_view) {
            Log.e(TAG, "onClick");
        }
    }
}
