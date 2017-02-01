package com.yesing.blibrary_wos.utils.assist;

import android.util.Log;

/**
 * Created by YesingBeijing on 2016/8/29.
 */
public class WLogger {
    public static final String TAG = "WLogger";

    public static boolean sIsDebug = true;

    public static void v(String message) {
        if (sIsDebug) {
            Log.d(TAG, "WLogger v : " + message);
        }
    }

    public static void d(String message) {
        if (sIsDebug) {
            Log.d(TAG, "WLogger d: " + message);
        }
    }

    public static void i(String message) {
        if (sIsDebug) {
            Log.d(TAG, "WLogger i: " + message);
        }
    }

    public static void w(String message) {
        if (sIsDebug) {
            Log.w(TAG, "WLogger w: " + message);
        }
    }

    public static void e(String message) {
        if (sIsDebug) {
            Log.e(TAG, "WLogger e: " + message);
        }
    }

    public static void e(Throwable e, String message) {
        if (sIsDebug) {
            Log.e(TAG, "WLogger e: " + message, e);
        }
    }
}
