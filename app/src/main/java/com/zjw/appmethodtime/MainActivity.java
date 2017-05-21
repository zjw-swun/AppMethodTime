package com.zjw.appmethodtime;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

import com.zjw.myapplication.MyLibActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    protected Button mTv;
    protected MyListView mListView;
    private MyAdapter mMyAdapter;
    private ArrayList<String> mArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_main);
        initView();
    }

    @CostTime
    private void initView() {
        mListView = (MyListView) findViewById(R.id.list_view);
        mArrayList = new ArrayList<String>();
        mMyAdapter = new MyAdapter(mArrayList, this);
        for (int i = 0; i < 50; i++) {
            mArrayList.add("" + i);
        }
        mListView.setAdapter(mMyAdapter);
        mListView.setOnItemClickListener(this);
    }

    @CostTime
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, MyLibActivity.class);
        startActivity(intent);
    }
}
