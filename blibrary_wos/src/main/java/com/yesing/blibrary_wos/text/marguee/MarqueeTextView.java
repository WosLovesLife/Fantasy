package com.yesing.blibrary_wos.text.marguee;

import android.content.Context;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by zhangH on 2016/5/27.
 */
public class MarqueeTextView extends TextView {


    public MarqueeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setEllipsize(TextUtils.TruncateAt.MARQUEE);
        setMarqueeRepeatLimit(-1);
        setSingleLine();
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public MarqueeTextView(Context context) {
        this(context, null);
    }


    @Override
    public boolean isFocused() {
        return true;
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        if (focused)
            super.onFocusChanged(true, direction, previouslyFocusedRect);
    }
}