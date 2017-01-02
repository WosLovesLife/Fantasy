package com.yesing.blibrary_wos.button;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;

import com.yesing.blibrary_wos.text.FixedTextView;

/**
 * Created by YesingBeijing on 2016/9/18.
 */
public class BorderlessButton extends FixedTextView {
    public BorderlessButton(Context context) {
        this(context, null);
    }

    public BorderlessButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BorderlessButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        setClickable(true);
        setGravity(Gravity.CENTER);
    }

    protected int toPX(int dp) {
        return (int) (getContext().getResources().getDisplayMetrics().density * dp);
    }
}
