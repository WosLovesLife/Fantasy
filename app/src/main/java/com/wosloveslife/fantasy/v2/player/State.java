package com.wosloveslife.fantasy.v2.player;

import com.google.android.exoplayer2.ExoPlayer;
import com.wosloveslife.fantasy.services.PlayService;

import java.util.ArrayList;
import java.util.List;

/**
 * 只负责状态变化的传达和当前状态的获取
 * Created by zhangh on 2017/9/3.
 */

public class State {
    private PlayService.PlayBinder mPlayBinder;
    private List<ExoPlayer.EventListener> mEventListenerBuffers;

    void setPlayBinder(PlayService.PlayBinder playBinder) {
        mPlayBinder = playBinder;
        if (mPlayBinder != null) {
            executeAfterBind();
        }
    }

    private void executeAfterBind() {
        if (mEventListenerBuffers != null) {
            List<ExoPlayer.EventListener> mBuffers = new ArrayList<>();
            mBuffers.addAll(mEventListenerBuffers);
            for (ExoPlayer.EventListener listener : mBuffers) {
                addListener(listener);
            }
            mEventListenerBuffers.removeAll(mBuffers);
        }
    }

    public boolean isPlaying() {
        return mPlayBinder != null && mPlayBinder.isPlaying();
    }

    public long getCurrentPosition() {
        return mPlayBinder != null ? mPlayBinder.getCurrentPosition() : 0;
    }

    public int getBufferState() {
        return mPlayBinder != null ? mPlayBinder.getBufferState() : 0;
    }

    public void addListener(ExoPlayer.EventListener listener) {
        if (mPlayBinder != null) {
            mPlayBinder.addListener(listener);
        } else {
            if (mEventListenerBuffers == null) {
                mEventListenerBuffers = new ArrayList<>();
            }
            mEventListenerBuffers.add(listener);
        }
    }

    public boolean removeListener(ExoPlayer.EventListener listener) {
        boolean remove = false;
        if (mEventListenerBuffers != null) {
            remove = mEventListenerBuffers.remove(listener);
        }
        if (mPlayBinder != null) {
            remove |= mPlayBinder.removeListener(listener);
        }
        return remove;
    }

    public long getDuration() {
        return mPlayBinder != null ? mPlayBinder.getDuration() : 0;
    }

    public long getBufferedPosition() {
        return mPlayBinder != null ? mPlayBinder.getBufferedPosition() : 0;
    }

    public int getPlaybackState() {
        return mPlayBinder != null ? mPlayBinder.getPlaybackState() : 0;
    }
}
