package com.wosloveslife.fantasy.utils;

import android.text.format.DateFormat;

/**
 * Created by zhangh on 2017/1/15.
 */

public class FormatUtils {
    public static int progressBarValue(long progress, long duration) {
        return (int) (((float) progress) / duration * 100);
    }

    public static String stringForTime(long timeMs) {
        return DateFormat.format("mm:ss", timeMs).toString();
    }
}
