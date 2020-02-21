package com.zjw.appmethodtime

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = "TAG"

    @CostTime
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.activity_main)
        initView()
    }

    @CostTime
    private fun initView() {
        mTextView.setOnClickListener(this@MainActivity)
        //android官方 lint插件自带的warring提示
        //自定义MyLogDetector lint error提示
        // Log.e(TAG,"test")

    }

    @CostTime
    override fun onClick(view: View) {
        if (view.id == R.id.mTextView) {
            val fromJson = Gson().fromJson<SearchShopModel>("{\n" +
                   // "    \"name\": \"zjw\",\n" +
                    "    \"age\": 1,\n" +
                    "    \"title\": \"test\"\n" +
                    "}"
                    , SearchShopModel::class.java)
            Log.e(TAG, "fromJson is $fromJson")
            val searchShopModel = SearchShopModel()
            searchShopModel.base = "我是基类Title"
            searchShopModel.shop = "我是子类Title"
            val toJson = Gson().toJson(searchShopModel)

            Log.e(TAG, "toJson is $toJson")

        }
    }
}
