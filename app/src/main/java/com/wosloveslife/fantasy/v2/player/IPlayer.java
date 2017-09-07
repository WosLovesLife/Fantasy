package com.wosloveslife.fantasy.v2.player;

import com.wosloveslife.dao.Audio;

/**
 * Created by zhangh on 2017/9/3.
 */

public interface IPlayer {
    void play(Audio audio);

    void pause();

    void seekTo(long progress);

    State getState();
}
