package com.zjw.appmethodtime;

import android.content.Context;
import android.util.AttributeSet;

import com.google.android.flexbox.FlexLine;
import com.google.android.flexbox.FlexboxLayout;

import java.util.List;

/**
 * FlexboxLayout设置maxLines之后，如果item超出maxLines，会全部罗列在最后一行，不符合需求；
 *
 * FlexBoxLayoutMaxLines支持设置MaxLines，并截断超出MaxLines的内容；
 */
public class FlexBoxMaxLinesLayout extends FlexboxLayout {
    private int maxLines = NOT_SET;

    @Override
    public void setMaxLine(int maxLine) {
        maxLines = maxLine;
    }

    public int getMaxLines() {
        return maxLines;
    }

    /**
     * see {@link #getMaxLines()}
     */
    @Deprecated
    @Override
    public int getMaxLine() {
        return NOT_SET;
    }

    public FlexBoxMaxLinesLayout(Context context) {
        this(context, null);
    }

    public FlexBoxMaxLinesLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlexBoxMaxLinesLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setMaxLine(super.getMaxLine());
        super.setMaxLine(NOT_SET);
    }

    @Override
    public List<FlexLine> getFlexLinesInternal() {
        List<FlexLine> flexLines = super.getFlexLinesInternal();
        int size = flexLines.size();
        if (maxLines > 0 && size > maxLines) {
            flexLines.subList(maxLines, size).clear();
        }
        return flexLines;
    }
}