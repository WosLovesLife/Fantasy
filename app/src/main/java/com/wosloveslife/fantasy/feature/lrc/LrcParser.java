package com.wosloveslife.fantasy.feature.lrc;

import android.text.TextUtils;

import com.orhanobut.logger.Logger;
import com.yesing.blibrary_wos.utils.assist.WLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhangh on 2017/2/15.
 */

public class LrcParser {
    private static final Pattern PATTERN_LRC_INTERVAL = Pattern.compile("\\u005b[0-9]+:[0-9]+\\u002e[0-9]+\\u005d");

    public static BLyric parseLrc(String lrcContent) {
        if (TextUtils.isEmpty(lrcContent)) return null;

        List<BLyric.LyricLine> lrcLines;
        if (PATTERN_LRC_INTERVAL.matcher(lrcContent).find()) {
            lrcLines = match(lrcContent);
        } else {
            String[] split = lrcContent.split("\r\n");
            lrcLines = new ArrayList<>();
            for (String s : split) {
                lrcLines.add(new BLyric.LyricLine(-1, s));
            }
        }

        return new BLyric(lrcLines);
    }

    private static List<BLyric.LyricLine> match(String content) {
        List<BLyric.LyricLine> lyricLines = new ArrayList<>();
        int start = 0;
        int end;
        String time = null;
        Matcher matcher = PATTERN_LRC_INTERVAL.matcher(content);
        while (matcher.find()) {
            /* 时间字段开始处为上一次遍历的内容的起始处 */
            end = matcher.start();
            if (end > 0) {
                String lrcLine = content.substring(start, end);
                if (lrcLine.endsWith("\n")) {
                    lrcLine = lrcLine.substring(0, lrcLine.length() - 1);
                }
                WLogger.d("match : LrcLine = " + lrcLine);
                lyricLines.add(new BLyric.LyricLine(lrcTime2Timestamp(time), lrcLine));
            }
            /* 本次的时间结尾处作为下一次查询的本次内容的起始处 */
            start = matcher.end();
            time = matcher.group();
        }
        return lyricLines;
    }

    private static int lrcTime2Timestamp(String time) {
        if (time == null || time.equals("")) return 0;
        int minutes = string2Int(time.substring(1, 3));
        int seconds = string2Int(time.substring(4, 6));
        int milliseconds = string2Int(time.substring(7, 9));
        return minutes * 60 * 1000 + seconds * 1000 + milliseconds * 10;
    }

    private static int string2Int(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Throwable e) {
            Logger.w("时间转换错误");
        }
        return 0;
    }
}
