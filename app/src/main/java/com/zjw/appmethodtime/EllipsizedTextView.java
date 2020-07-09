package com.zjw.appmethodtime;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/**
 * 在原textview 文案后添加$mFoldLine，如果遇到末尾放不下就变成 ...$mFoldLine 的组件
 */
@SuppressLint("AppCompatCustomView")
public class EllipsizedTextView extends TextView {
    private static int DEFAULT_TAIL_TEXT_COLOR;
    private static final String DEFAULT_ELLIPSIZE = "...";
    private String mUnFoldText;
    private String mEllipsizeText = DEFAULT_ELLIPSIZE;
    private static final String DEFAULT_UNFOLD_TEXT = "  查看详情>";
    private int mFoldLine;
    private int mTailColor;
    private String mFullText;

    // 绘制，防止重复进行绘制
    private boolean mHasDrawn = false;

    // 行间距倍数
    private float mLineSpacingMultiplier = 1.0f;
    // 行间距额外像素
    private float mLineSpacingExtra = 0.0f;

    public EllipsizedTextView(Context context) {
        this(context, null);
    }

    public EllipsizedTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EllipsizedTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        DEFAULT_TAIL_TEXT_COLOR = ContextCompat.getColor(context, R.color.c_4d97ff);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExpandableTextView);
        mUnFoldText = a.getString(R.styleable.ExpandableTextView_unFoldText);
        if (null == mUnFoldText) {
            mUnFoldText = DEFAULT_UNFOLD_TEXT;
        }
        mFoldLine = a.getInt(R.styleable.ExpandableTextView_foldLine, getMaxLines());
        if (mFoldLine < 1) {
            mFoldLine = 1;
        }
        mTailColor = a.getColor(R.styleable.ExpandableTextView_tailTextColor, DEFAULT_TAIL_TEXT_COLOR);
        a.recycle();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        float availableScreenWidth = getMeasuredWidth() - getCompoundPaddingLeft() - getCompoundPaddingRight();
        float availableTextWidth = availableScreenWidth * getMaxLines();
        CharSequence ellipsizedText = TextUtils.ellipsize(getText(), getPaint(), availableTextWidth, getEllipsize());
        if (getText().toString().equals(ellipsizedText.toString())) {
            //没有省略号的时候  预测量添加 查看详情 是否存在...  存在则加 ...查看详情>  否则就直接加就行
            String preText = getText() + DEFAULT_ELLIPSIZE + mUnFoldText;
            CharSequence preEllipsizeText = TextUtils.ellipsize(preText, getPaint(), availableTextWidth, getEllipsize());
            if (preText.equals(preEllipsizeText.toString())) {
                //直接加查看详情
                mEllipsizeText = "";
            } else {
                //如果之前预加字段导致end ellipseize的则还原字段
                if (mEllipsizeText.equals("")) {
                    mEllipsizeText = DEFAULT_ELLIPSIZE;
                    mHasDrawn = false;
                }
                mEllipsizeText = DEFAULT_ELLIPSIZE;
            }
        } else {
            if (mEllipsizeText.equals("")) {
                mEllipsizeText = DEFAULT_ELLIPSIZE;
                mHasDrawn = false;
            }
        }
        invalidate();
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (TextUtils.isEmpty(mFullText)) {
            mHasDrawn = false;
            mFullText = String.valueOf(text);
        }
        super.setText(text, type);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (!mHasDrawn) {
            resetText();
        }
        super.onDraw(canvas);
        mHasDrawn = true;
    }

    public void setMaxLines(int maxLines) {
        super.setMaxLines(maxLines);
        mFoldLine = maxLines;
    }

    // 点击处理
    private ClickableSpan clickSpan = new ClickableSpan() {
        @Override
        public void onClick(View widget) {
            invalidate();
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setColor(mTailColor);
        }
    };

    private ForegroundColorSpan mForegroundColorSpan;

    /**
     * 设置click span选择背景色
     * @param color
     */
    @Override
    public void setHighlightColor(int color) {
        super.setHighlightColor(color);
    }

    /**
     * 重置文字
     */
    private void resetText() {
        SpannableString spanStr = createFoldSpan(mFullText);
        setText(spanStr);
        setMovementMethod(LinkMovementMethod.getInstance());
    }

    public String getFullText() {
        return mFullText;
    }

    public void setFullText(String fullText) {
        mFullText = fullText;
        mHasDrawn = false;
        setText(fullText);
        invalidate();
    }


    /**
     * 创建收缩状态下的Span
     *
     * @param text
     * @return 收缩状态下的Span
     */
    private SpannableString createFoldSpan(String text) {
        String destStr = "";
        int start = 0;
        int end = 0;
        if (mEllipsizeText.equals("")){
            //不需要裁剪
            destStr = text + mUnFoldText;
            start = text.length();
            end = destStr.length();
        }else {
            //裁剪
            destStr = tailorText(text);
            start = destStr.length() - mUnFoldText.length();
            end = destStr.length();
        }

        SpannableString spanStr = new SpannableString(destStr);
        mForegroundColorSpan = new ForegroundColorSpan(mTailColor);
        spanStr.setSpan(mForegroundColorSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spanStr;
    }

    /**
     * 裁剪文本至固定行数（备用方法）
     *
     * @param text 源文本
     * @return 裁剪后的文本
     */
    private String tailorTextBackUp(String text) {

        String destStr = text + mEllipsizeText + mUnFoldText;
        Layout layout = makeTextLayout(destStr);

        // 如果行数大于固定行数
        if (layout.getLineCount() > getFoldLine()) {
            int index = layout.getLineEnd(getFoldLine() - 1);
            if (text.length() < index) {
                index = text.length();
            }
            // 从最后一位逐渐试错至固定行数（可以考虑用二分法改进）
            if (index <= 1) {
                return mEllipsizeText + mUnFoldText;
            }
            String subText = text.substring(0, index - 1);
            return tailorText(subText);
        } else {
            return destStr;
        }
    }

    /**
     * 裁剪文本至固定行数（二分法）。经试验，在文字长度不是很长时，效率比备用方法高不少；当文字长度过长时，备用方法则优势明显。
     *
     * @param text 源文本
     * @return 裁剪后的文本
     */
    private String tailorText(String text) {
        // return tailorTextBackUp(text);

        int start = 0;
        int end = text.length() - 1;
        int mid = (start + end) / 2;
        int find = finPos(text, mid);
        while (find != 0 && end > start) {
            if (find > 0) {
                end = mid - 1;
            } else if (find < 0) {
                start = mid + 1;
            }
            mid = (start + end) / 2;
            find = finPos(text, mid);
        }

        String ret;
        if (find == 0) {
            ret = text.substring(0, mid) + mEllipsizeText + mUnFoldText;
        } else {
            ret = tailorTextBackUp(text);
        }
        return ret;
    }

    /**
     * 查找一个位置P，到P时为mFoldLine这么多行，加上一个字符‘A’后则刚好为mFoldLine+1这么多行
     *
     * @param text 源文本
     * @param pos  位置
     * @return 查找结果
     */
    private int finPos(String text, int pos) {
        String destStr = text.substring(0, pos) + mEllipsizeText + mUnFoldText;
        Layout layout = makeTextLayout(destStr);
        Layout layoutMore = makeTextLayout(destStr + "A");

        int lineCount = layout.getLineCount();
        int lineCountMore = layoutMore.getLineCount();

        if (lineCount == getFoldLine() && (lineCountMore == getFoldLine() + 1)) {
            // 行数刚好到折叠行数
            return 0;
        } else if (lineCount > getFoldLine()) {
            // 行数比折叠行数多
            return 1;
        } else {
            // 行数比折叠行数少
            return -1;
        }
    }

    /**
     * 获取TextView的Layout，注意这里使用getWidth()得到宽度
     *
     * @param text 源文本
     * @return Layout
     */
    private Layout makeTextLayout(String text) {
        return new StaticLayout(text, getPaint(), getWidth() - getPaddingLeft() - getPaddingRight(), Layout.Alignment
                .ALIGN_NORMAL, mLineSpacingMultiplier, mLineSpacingExtra, true);
    }


    @Override
    public void setLineSpacing(float extra, float multiplier) {
        mLineSpacingExtra = extra;
        mLineSpacingMultiplier = multiplier;
        super.setLineSpacing(extra, multiplier);
    }

    public int getFoldLine() {
        return mFoldLine;
    }
}
