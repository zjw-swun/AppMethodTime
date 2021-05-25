package com.zjw.appmethodtime;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;


/**
 * 渐变颜色的TextView
 * Created by Dimitrios on 9/26/2017.
 */
public class GradientTextView extends AppCompatTextView {

    private final static String TAG = GradientTextView.class.getSimpleName();

    private int[] mColors;

    private int mAngle = 0;

    private DIRECTION mDIRECTION;

    public enum DIRECTION {
        LEFT(0),
        TOP(90),
        RIGHT(180),
        BOTTOM(270);

        int angle;

        DIRECTION(int angle) {
            this.angle = angle;
        }
    }

    public GradientTextView(Context context) {
        super(context);
        init(context, null);
    }

    public GradientTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public GradientTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //if colors haven't been set, skip this
        if (mColors != null) {
            int[] xyPositions = calculateGradientPositions(w, h);
            Shader shader = new LinearGradient(xyPositions[0], xyPositions[1], xyPositions[2], xyPositions[3], mColors, null, Shader.TileMode.CLAMP);
            getPaint().setShader(shader);
        }

    }

    private int[] calculateGradientPositions(int w, int h) {
        int[] gradientPositions;
        if (mAngle < 0 || mAngle > 360) {
        }
        if (mDIRECTION != null) {
            switch (mDIRECTION) {
                case TOP:
                    return new int[]{0, h, 0, 0};
                case RIGHT:
                    return new int[]{0, 0, w, 0};
                case BOTTOM:
                    return new int[]{0, 0, 0, h};
                case LEFT:
                default:
                    return new int[]{w, 0, 0, 0};
            }
        }
        //should not reach here
        return new int[]{0, 0, 0, 0};
    }

    private void init(Context context, AttributeSet attributeSet) {

        final TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.GradientTextView);

        try {
            int colorArrayResourceId = typedArray.getResourceId(R.styleable.GradientTextView_gt_color_list, 0);
            if (colorArrayResourceId != 0) {
                mColors = getResources().getIntArray(colorArrayResourceId);
            }
            if (typedArray.hasValue(R.styleable.GradientTextView_gt_gradient_direction)) {
                int value = typedArray.getInt(R.styleable.GradientTextView_gt_gradient_direction, 0);
                mDIRECTION = DIRECTION.values()[value];
            }

            if (typedArray.hasValue(R.styleable.GradientTextView_gt_gradient_angle)) {
                mAngle = typedArray.getInt(R.styleable.GradientTextView_gt_gradient_angle, 0);
            }

        } catch (Exception ignored) {
        } finally {
            typedArray.recycle();
        }
    }
}