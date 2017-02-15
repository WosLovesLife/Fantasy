package com.wosloveslife.fantasy.lrc;

import android.text.StaticLayout;

import java.util.List;

/**
 * Created by zhangh on 2017/2/2.
 */

public class BLyric {
    public List<LyricLine> mLrc;

    public BLyric() {
    }

    public BLyric(List<LyricLine> lrc) {
        mLrc = lrc;
    }

    public static class LyricLine {
        public long time;
        public String content;
        public StaticLayout staticLayout;

        public LyricLine(long time, String content) {
            this.time = time;
            this.content = content;
        }

        @Override
        public String toString() {
            return "LyricLine{" +
                    "time=" + time +
                    ", content='" + content + '\'' +
                    "}\n";
        }
    }
}
