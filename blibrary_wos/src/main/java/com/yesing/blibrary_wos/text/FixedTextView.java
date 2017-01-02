package com.yesing.blibrary_wos.text;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.yesing.blibrary_wos.R;

/**
 * Created by YesingBeijing on 2016/9/19.
 */
public class FixedTextView extends TextView {
    private static final String TAG = "FixedTextView";
    private CharSequence mVariedText;
    private String mFixedText;
    private String mFrontFixedText;
    private String mHindFixedText;
    private boolean mAttach;

    public FixedTextView(Context context) {
        this(context, null);
    }

    public FixedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FixedText);
        init(a);
    }

    public FixedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FixedText, defStyleAttr, 0);
        init(a);
    }

    private void init(final TypedArray a) {
//        mFixedText = a.getString(R.styleable.FixedText_fixedText);
        mFrontFixedText = a.getString(R.styleable.FixedText_fixedFrontText);
        mHindFixedText = a.getString(R.styleable.FixedText_fixedHindText);
        a.recycle();

        mVariedText = getText();
        mFixedText = mFixedText == null ? "" : mFixedText;
        mFrontFixedText = mFrontFixedText == null ? "" : mFrontFixedText;
        mHindFixedText = mHindFixedText == null ? "" : mHindFixedText;

        setFixedText(mFixedText);
        setFrontFixedText(mFrontFixedText);
        setHindFixedText(mHindFixedText);

        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                /* 由于每次setText()的时候都会触发这个回调, 所以要做一个判断, 防止无限递归 */
                mAttach = !mAttach;
                if (mAttach) {
                    mVariedText = s;
                    setText(mFrontFixedText + s + mFixedText + mHindFixedText);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public void setFixedText(String fixedText) {
        String currentText = getText().toString();

        String text;
        if (currentText.contains(mFixedText)) {
            text = currentText.replace(mFixedText, fixedText);
        } else {
            text = currentText + fixedText;
        }
        setText(text);

        mFixedText = fixedText;
    }

    public void setFrontFixedText(String frontFixedText) {
        String currentText = getText().toString();

        String text;
        if (currentText.contains(mFrontFixedText)) {
            text = currentText.replace(mFrontFixedText, frontFixedText);
        } else {
            text = frontFixedText + currentText;
        }
        setText(text);

        mFrontFixedText = frontFixedText;
    }

    public void setHindFixedText(String hindFixedText) {
        String currentText = getText().toString();

        String text;
        if (currentText.contains(mHindFixedText)) {
            text = currentText.replace(mHindFixedText, hindFixedText);
        } else {
            text = currentText + hindFixedText;
        }
        setText(text);

        mHindFixedText = hindFixedText;
    }

    public CharSequence getVariedText() {
        return mVariedText;
    }

    public String getFixedText() {
        return mFixedText;
    }

    public String getFrontFixedText() {
        return mFrontFixedText;
    }

    public String getHindFixedText() {
        return mHindFixedText;
    }
}
