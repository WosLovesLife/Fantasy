package com.yesing.blibrary_wos.utils.assist;

import android.util.Log;

/**
 * Created by YesingBeijing on 2016/8/29.
 */
public class WLogger {
    public static final String TAG = "WLogger";

    public static boolean sIsDebug = true;

    public static void logD(String message) {
        if (sIsDebug) {
            Log.d(TAG, "logD: Log输出 " + message);
        }
    }

    public static void logW(String message) {
        if (sIsDebug) {
            Log.w(TAG, "logD: Log输出 " + message);
        }
    }

    public static void logE(String message) {
        if (sIsDebug) {
            Log.e(TAG, "logD: Log输出 " + message);
        }
    }

    public static void logE(String message, Throwable e) {
        if (sIsDebug) {
            Log.e(TAG, "logD: Log输出 " + message, e);
        }
    }
}
