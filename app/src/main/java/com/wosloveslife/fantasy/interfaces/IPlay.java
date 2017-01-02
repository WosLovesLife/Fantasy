package com.wosloveslife.fantasy.interfaces;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by zhangh on 2017/1/2.
 */
public interface IPlay {
    int PLAY_STATE_IDLE = 0;
    int PLAY_STATE_PLAY = 1;
    int PLAY_STATE_PAUSE = 2;
    int PLAY_STATE_COMPLETE = 3;

    @IntDef({PLAY_STATE_IDLE, PLAY_STATE_PLAY, PLAY_STATE_PAUSE, PLAY_STATE_COMPLETE})
    @Retention(RetentionPolicy.SOURCE)
    @interface PlayState {
    }

    void play();

    void pause();

    void next();

    void previous();

    void setProgress(int progress);
}
