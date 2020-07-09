package com.tablayout;

import android.content.Context;
import android.graphics.Outline;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ClipPagerFrameLayout extends FrameLayout implements PageTransformerListener{
    private float mCurrentProgress;
    private float mClipLeft;
    private float mClipRight;
    private Direction mDirection = Direction.LEFT_TO_RIGHT;
    private Rect mRect = new Rect();
    private ClipViewOutlineProvider mClipViewOutlineProvider;


    public ClipPagerFrameLayout(@NonNull Context context) {
        this(context, null);
    }

    public ClipPagerFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public ClipPagerFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mClipViewOutlineProvider = new ClipViewOutlineProvider();
        setOutlineProvider(mClipViewOutlineProvider);
        setClipToOutline(true);
    }


    public float getCurrentProgress() {
        return mCurrentProgress;
    }

    @Override
    public void setCurrentProgress(float currentProgress) {
        mCurrentProgress = currentProgress;
        if (mDirection == Direction.LEFT_TO_RIGHT) {
            mRect.set(0, 0, (int) (getWidth() * mCurrentProgress), getHeight());
        } else {
            mRect.set((int) (getWidth() * (1 - mCurrentProgress)), 0, getWidth(), getHeight());
        }
        //setClipBounds(mRect);
        invalidateOutline();
    }

    @Override
    public void onLeave(int index, float leavePercent, boolean leftToRight) {

    }

    @Override
    public void onEnter(int index, float enterPercent, boolean leftToRight) {

    }

    private class ClipViewOutlineProvider extends ViewOutlineProvider {
        @Override
        public void getOutline(View view, Outline outline) {
            outline.setRect(mRect);
        }
    }


    public Direction getDirection() {
        return mDirection;
    }

    @Override
    public void setDirection(Direction direction) {
        mDirection = direction;
    }

    public float getClipLeft() {
        return mClipLeft;
    }

    public void setClipLeft(float clipLeft) {
        if (clipLeft <= 0) {
            clipLeft = 0;
        }
        mClipLeft = clipLeft;
    }

    public float getClipRight() {
        return mClipRight;
    }

    public void setClipRight(float clipRight) {
        if (clipRight >= getWidth()) {
            clipRight = getWidth();
        }
        mClipRight = clipRight;
    }
}
