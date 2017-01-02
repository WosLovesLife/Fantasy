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
    public static void showShort(Context context, CharSequence message) {
        dispose(context, message, Toast.LENGTH_SHORT);
    }

    /**
     * 短时间显示Toast
     */
    public static void showShort(Context context, int message) {
        showShort(context, context.getResources().getString(message));
    }

    /**
     * 长时间显示Toast
     */
    public static void showLong(Context context, CharSequence message) {
        dispose(context, message, Toast.LENGTH_LONG);
    }

    /**
     * 长时间显示Toast
     */
    public static void showLong(Context context, int message) {
        showLong(context, context.getResources().getString(message));
    }

    /**
     * 自定义显示Toast时间
     */
    public static void show(Context context, CharSequence message, int duration) {
        dispose(context, message, duration);
    }

    /**
     * 自定义显示Toast时间
     */
    public static void show(Context context, int message, int duration) {
        dispose(context, context.getResources().getString(message), duration);
    }
}  