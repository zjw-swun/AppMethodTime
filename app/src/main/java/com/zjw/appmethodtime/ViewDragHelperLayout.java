package com.zjw.appmethodtime;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.customview.widget.ViewDragHelper;


public class ViewDragHelperLayout extends LinearLayout {

    private ViewDragHelper mDragger;

    private View mAutoBackView;

    private Point mAutoBackOriginPos = new Point();

    private int finalTop = -1;

    private OnExpandListenner onExpandListenner;
    private boolean isExpan;
    private boolean needDrag;

    public ViewDragHelperLayout(Context context) {
        super(context);
        init();
    }

    public ViewDragHelperLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mDragger = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {
            // 返回true，则表示可以捕获该view
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                // return true;
                if (!needDrag) {
                    return false;
                }
                return child == mAutoBackView;
            }

            // 水平方向边界进行控制，left , top 分别为即将移动到的位置,如果返回固定的值，表示只能水平或垂直移动
            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                return child.getLeft();
            }

            // 垂直方向边界进行控制
            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                return clamp(top, 0, getMeasuredHeight());
            }

            // 手指释放的时候回调
            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                // mAutoBackView手指释放时可以自动回去
                if (releasedChild == mAutoBackView) {
                    //调用settleCapturedViewAt回到某个位置，其内部调用mScroller.startScroll，因此重写computeScroll
                    float newY = releasedChild.getY();
                    if (newY <= DPIUtil.dip2px(releasedChild.getContext(), 80f)) {
                        finalTop = mAutoBackOriginPos.y;
                        mDragger.settleCapturedViewAt(mAutoBackOriginPos.x, mAutoBackOriginPos.y);
                    } else {
                        finalTop = ViewDragHelperLayout.this.getMeasuredHeight();
                        mDragger.settleCapturedViewAt(0, ViewDragHelperLayout.this.getMeasuredHeight());
                    }
                    invalidate();
                }
            }

            //解决子View如果消耗事件就无法移动
            @Override
            public int getViewHorizontalDragRange(View child) {
                return 0;
            }

            @Override
            public int getViewVerticalDragRange(View child) {
                return getMeasuredHeight();
            }

            @Override
            public void onViewDragStateChanged(int state) {
                if (finalTop != -1 && mDragger.getViewDragState() == ViewDragHelper.STATE_IDLE) {
                    if (finalTop == ViewDragHelperLayout.this.getMeasuredHeight()) {
                        onClose();
                    } else {
                        onExpan();
                    }
                    finalTop = -1;
                }
            }
        });
        mDragger.setEdgeTrackingEnabled(ViewDragHelper.EDGE_TOP);
    }

    // ViewDragHelper中拦截和处理事件时，需要会回调CallBack
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDragger.processTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        if (mDragger.continueSettling(true)) {
            invalidate();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return mDragger.shouldInterceptTouchEvent(event);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mAutoBackOriginPos.x = mAutoBackView.getLeft();
        mAutoBackOriginPos.y = mAutoBackView.getTop();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mAutoBackView = getChildAt(0);
    }


    public OnExpandListenner getOnExpandListenner() {
        return onExpandListenner;
    }

    public void setOnExpandListenner(OnExpandListenner onExpandListenner) {
        this.onExpandListenner = onExpandListenner;
    }

    public void onExpan() {
        if (onExpandListenner != null) {
            isExpan = true;
            onExpandListenner.onExpan();
        }
    }

    public void onClose() {
        if (onExpandListenner != null) {
            isExpan = false;
            onExpandListenner.onClose();
        }
    }

    public boolean isExpan() {
        return isExpan;
    }

    public void setExpan(boolean expan) {
        isExpan = expan;
    }

    public boolean isNeedDrag() {
        return needDrag;
    }

    public void setNeedDrag(boolean needDrag) {
        this.needDrag = needDrag;
    }

    public interface OnExpandListenner {
        void onExpan();

        void onClose();
    }

    public static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        } else if (value > max) {
            return max;
        }
        return value;
    }
}
