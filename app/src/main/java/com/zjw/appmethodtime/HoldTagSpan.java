package com.zjw.appmethodtime;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.ReplacementSpan;

/**
 * 占位使用的Span
 * 使用案例  textview?.measure(0, 0) //textview为targetTv要嵌入的view 可以是任意view
 *   val truss = Truss()
 *             val span = HoldTagSpan(textview?.measuredWidth!! + textview?.measuredWidth!!,
 *                     1)
 *             truss.pushSpan(span)
 *                     .append("tag")
 *                     .popSpan()
 *                     .append(mData?.questionTitle)
 *             targetTv?.text = truss.build()
 *
 */
public class HoldTagSpan extends ReplacementSpan {
    private int width;
    private int height;
    private int leftMargin;
    private int rightMargin;

    public HoldTagSpan(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public HoldTagSpan(int width, int height, int leftMargin, int rightMargin) {
        this.width = width;
        this.height = height;
        this.leftMargin = leftMargin;
        this.rightMargin = rightMargin;
    }

    public HoldTagSpan setLeftMargin(int leftMargin) {
        this.leftMargin = leftMargin;
        return this;
    }

    public HoldTagSpan setRightMargin(int rightMargin) {
        this.rightMargin = rightMargin;
        return this;
    }


    @Override
    public int getSize(@androidx.annotation.NonNull Paint paint, CharSequence charSequence, int start, int end, @androidx.annotation.Nullable Paint.FontMetricsInt fontMetricsInt) {
        return width + leftMargin + rightMargin;
    }

    @Override
    public void draw(@androidx.annotation.NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @androidx.annotation.NonNull Paint paint) {
    }
}