package com.yesing.blibrary_wos.text;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

/**
 * Created by YesingBeijing on 2016/12/27.
 */
public class SpannableTextUtils {

    public static SpannableString highLight(Context context, String res, int color) {
        SpannableString spannableString = new SpannableString(res);
        spannableString.setSpan(new ForegroundColorSpan(context.getResources().getColor(color)), 0, res.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return spannableString;
    }
}
