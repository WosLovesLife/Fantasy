package com.yesing.blibrary_wos.utils.viewUtils;

import android.text.InputFilter;
import android.text.Spanned;

public class EditCountByBytesFilter implements InputFilter {
    int MAX_EN;// 最大字符长度

    public EditCountByBytesFilter(int mAX_EN) {
        super();
        MAX_EN = mAX_EN;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        int destCount = getChineseCount(dest.toString());
        int sourceCount = getChineseCount(source.toString());
        if (destCount + sourceCount > MAX_EN) {
            return "";
        } else {
            return source;
        }
    }

    private int getChineseCount(String str) {
        return str.getBytes().length;
    }
}