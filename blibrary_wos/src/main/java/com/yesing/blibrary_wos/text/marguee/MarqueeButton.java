package com.yesing.blibrary_wos.text.marguee;

import android.content.Context;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Created by zhangH on 2016/5/27.
 */
public class MarqueeButton extends Button {

    /*public MarqueeButton(Context context) {
        this(context, null);
    }

    public MarqueeButton(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }*/

    public MarqueeButton(Context context) {
        super(context);
    }

    public MarqueeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MarqueeButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        setFocusable(true);
        setFocusableInTouchMode(true);
        setEllipsize(TextUtils.TruncateAt.MARQUEE);
        setMarqueeRepeatLimit(-1);
        setSingleLine(true);
    }

    @Override
    public boolean isFocused() {
        return true;
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        if (focused) {
            super.onFocusChanged(true, direction, previouslyFocusedRect);
        }
    }
}