package com.yesing.blibrary_wos.utils.assist;

import android.content.Context;
import android.widget.Toast;

/**
 * 通过该工具类创建Toast, 避免造成Toast弹出排队的问题
 * !!!注意: 如果是重要的Toast需要一条条显示, 推荐使用SnackBar显示
 */
public class Toaster {
    private static Toast sToast;

    private Toaster() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    private static Context sContext;

    public static void init(Context context) {
        sContext = context.getApplicationContext();
    }

    public static boolean isShow = true;

    private static void dispose(Context context, CharSequence message, int time) {
        if (isShow) {
            if (sToast != null) {
                sToast.cancel();
                sToast = null;
            }
            sToast = Toast.makeText(context.getApplicationContext(), message, time);
            sToast.show();
        }
    }

    /**
     * 短时间显示Toast
     */
    public static void showShort(CharSequence message) {
        dispose(sContext, message, Toast.LENGTH_SHORT);
    }

    /**
     * 短时间显示Toast
     */
    public static void showShort(int message) {
        showShort(sContext.getResources().getString(message));
    }

    /**
     * 长时间显示Toast
     */
    public static void showLong(CharSequence message) {
        dispose(sContext, message, Toast.LENGTH_LONG);
    }

    /**
     * 长时间显示Toast
     */
    public static void showLong(int message) {
        showLong(sContext.getResources().getString(message));
    }

    /**
     * 自定义显示Toast时间
     */
    public static void show(CharSequence message, int duration) {
        dispose(sContext, message, duration);
    }

    /**
     * 自定义显示Toast时间
     */
    public static void show(int message, int duration) {
        dispose(sContext, sContext.getResources().getString(message), duration);
    }
}  