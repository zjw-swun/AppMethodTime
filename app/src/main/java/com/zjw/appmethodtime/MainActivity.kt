package com.zjw.appmethodtime

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.zjw.mylibrary.LibActivity
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
        Toast.makeText(this,"test",Toast.LENGTH_SHORT)
        //自定义MyLogDetector lint error提示
        Log.e(TAG,"test")
    }

    @CostTime
    override fun onClick(view: View) {
        if (view.id == R.id.mTextView) {
            val intent = Intent(this@MainActivity, LibActivity::class.java)
            startActivity(intent)
        }
    }
}
