package com.zjw.appmethodtime

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = "TAG"

    @CostTime
    override fun onCreate(savedInstanceState: Bundle?) {
        val startTime = System.nanoTime()
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.activity_main)
        initView()
        val endTime = System.nanoTime()
        Log.i("TAG","onCreate cost is "+(endTime - startTime)/1000000f)
    }

    @CostTime
    private fun initView() {
        mTextView.setOnClickListener(this@MainActivity)
        //android官方 lint插件自带的warring提示
        //自定义MyLogDetector lint error提示
        // Log.e(TAG,"test")
        test()
        test1()
    }

    fun test(){
        val startTime = System.nanoTime()
        System.nanoTime()
        val endTime = System.nanoTime()
        Log.i("TAG", "test cost is " + (endTime - startTime) / 1000000f)
    }

    fun test1(){
        val startTime = System.nanoTime()
        Log.i("TAG", "log cost is " + (100-50) / 1000000f)
        val endTime = System.nanoTime()
        Log.i("TAG", "test1 cost is " + (endTime - startTime) / 1000000f)
    }
    @CostTime
    override fun onClick(view: View) {
        if (view.id == R.id.mTextView) {
            try {
                Thread.sleep(500)
            } catch (e: Exception) {
            }
        }
    }
}
