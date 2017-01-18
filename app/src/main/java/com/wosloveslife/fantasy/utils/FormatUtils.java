package com.wosloveslife.fantasy.utils;

import android.text.format.DateFormat;

import java.util.Formatter;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhangh on 2017/1/15.
 */

public class FormatUtils {
    private static final String HAS_HOUR = "hh:mm:ss";
    private static final String MINUTE = "mm:ss";

    public static String stringForTime(long timeMs) {
        /* 用于将当前进度转成(时)分秒的, 这种方式比用TimeUnit+if判断的效率节省大约4~5倍 */
        final StringBuilder formatBuilder = new StringBuilder();
        final Formatter formatter = new Formatter(formatBuilder, Locale.getDefault());
        timeMs = (timeMs + 500) / 1000;
        long seconds = timeMs % 60;
        long minutes = (timeMs / 60) % 60;
        long hours = timeMs / 3600;
        formatBuilder.setLength(0);
        return hours > 0 ? formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
                : formatter.format("%02d:%02d", minutes, seconds).toString();
    }

    /** 不推荐使用 */
    public static CharSequence stringForTime2(long timeMs) {
        TimeUnit timeUnit = TimeUnit.MILLISECONDS;
        timeMs = timeMs + 500;
        long hours = timeUnit.toHours(timeMs);
        return DateFormat.format(hours > 0 ? HAS_HOUR : MINUTE, timeMs);
    }
}
