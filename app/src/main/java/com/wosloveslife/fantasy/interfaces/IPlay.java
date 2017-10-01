package com.wosloveslife.fantasy.interfaces;

import android.support.annotation.IntDef;

import com.wosloveslife.dao.Audio;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.wosloveslife.fantasy.interfaces.IPlay.PlayState.PLAY_STATE_COMPLETE;
import static com.wosloveslife.fantasy.interfaces.IPlay.PlayState.PLAY_STATE_IDLE;
import static com.wosloveslife.fantasy.interfaces.IPlay.PlayState.PLAY_STATE_PAUSE;
import static com.wosloveslife.fantasy.interfaces.IPlay.PlayState.PLAY_STATE_PLAY;

/**
 * Created by zhangh on 2017/1/2.
 */
public interface IPlay {

    @IntDef({PLAY_STATE_IDLE, PLAY_STATE_PLAY, PLAY_STATE_PAUSE, PLAY_STATE_COMPLETE})
    @Retention(RetentionPolicy.SOURCE)
    @interface PlayState {
        int PLAY_STATE_IDLE = 0;
        int PLAY_STATE_PLAY = 1;
        int PLAY_STATE_PAUSE = 2;
        int PLAY_STATE_COMPLETE = 3;
    }

    void play(Audio audio);

    void pause();

    void seekTo(long progress);
}
