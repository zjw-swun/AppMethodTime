package com.zjw.appmethodtime;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ListView;

/**
 * Created by hasee on 2016/12/24.
 */

public class MyListView extends ListView {

    long curTime ;
    private static final String debugTag = MyListView.class.getName();
    private long endTime;

    public MyListView(Context context) {
        super(context);
    }

    public MyListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public void requestLayout() {
        super.requestLayout();
    }

    @Override
    protected int computeVerticalScrollOffset() {
        return super.computeVerticalScrollOffset();
    }
}