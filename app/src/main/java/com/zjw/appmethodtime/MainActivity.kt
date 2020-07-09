package com.zjw.appmethodtime

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
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
            try {
                Thread.sleep(500)
            } catch (e: Exception) {
            }
        }
    }
}
