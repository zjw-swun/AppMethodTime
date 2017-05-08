package com.zjw.appmethodtime;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View v){
        if (v.getId() == R.id.tv){
            sayHello();
        }
    }

    @CostTime
    public void sayHello(){
        Toast.makeText(this, "Hello!", Toast.LENGTH_SHORT).show();
    }
}
