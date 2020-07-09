package com.tablayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import androidx.appcompat.widget.AppCompatTextView;

public class ScaleTitleView extends AppCompatTextView implements PageTransformerListener {

    private float mCurrentProgress;
    private Direction mDirection = Direction.LEFT_TO_RIGHT;
    private float mSelectedSize = 22;
    private float mUnSelectedSize = 16;
    private float mTextSize = mUnSelectedSize;
    private Interpolator mInterpolator = new LinearInterpolator();
    private View mIndicator;
    private float mMinScale = 0.75f;


    public ScaleTitleView(Context context) {
        super(context);
    }

    public ScaleTitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScaleTitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setCurrentProgress(float currentProgress) {
        mCurrentProgress = currentProgress;
    }

    @Override
    public void onLeave(int index, float leavePercent, boolean leftToRight) {
        setScaleX(1.0f + (mMinScale - 1.0f) * leavePercent);
        setScaleY(1.0f + (mMinScale - 1.0f) * leavePercent);
        if (mIndicator != null) {
            mIndicator.setAlpha(1 - leavePercent);
        }
    }

    @Override
    public void onEnter(int index, float enterPercent, boolean leftToRight) {
        setScaleX(mMinScale + (1.0f - mMinScale) * enterPercent);
        setScaleY(mMinScale + (1.0f - mMinScale) * enterPercent);
        if (mIndicator != null) {
            mIndicator.setAlpha(enterPercent);
        }
    }

    @Override
    public void setDirection(Direction direction) {
        mDirection = direction;
    }

    public View getIndicator() {
        return mIndicator;
    }

    public void setIndicator(View indicator) {
        mIndicator = indicator;
    }
}
