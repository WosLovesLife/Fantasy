package com.yesing.blibrary_wos.utils.screenAdaptation;

import android.content.Context;

/**
 * Created by zhangH on 2016/5/21.
 */
public class Dp2Px {
    public static int toPX(Context context, int dp) {
        return (int) (context.getResources().getDisplayMetrics().density * dp);
    }

    public static float toPX(Context context, float dp) {
        return context.getResources().getDisplayMetrics().density * dp;
    }

    public static int toDp(Context context, int dp) {
        return (int) (dp / context.getResources().getDisplayMetrics().density);
    }
}
