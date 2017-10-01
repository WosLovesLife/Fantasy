package com.wosloveslife.fantasy.v2.player;

import android.support.annotation.IntDef;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.wosloveslife.fantasy.adapter.ExoPlayerEventListenerAdapter;

import java.util.ArrayList;
import java.util.List;

import static com.wosloveslife.fantasy.v2.player.State.StateCode.STATE_BUFFERING;
import static com.wosloveslife.fantasy.v2.player.State.StateCode.STATE_ENDED;
import static com.wosloveslife.fantasy.v2.player.State.StateCode.STATE_IDLE;
import static com.wosloveslife.fantasy.v2.player.State.StateCode.STATE_READY;

/**
 * 只负责状态变化的传达和当前状态的获取
 * Created by zhangh on 2017/9/3.
 */

public class State {
    @IntDef({STATE_IDLE, STATE_BUFFERING, STATE_READY, STATE_ENDED})
    public @interface StateCode {
        int STATE_IDLE = 1;
        /**
         * The player not able to immediately play from the current position. The cause is
         * {@link Renderer} specific, but this state typically occurs when more data needs to be
         * loaded to be ready to play, or more data needs to be buffered for playback to resume.
         */
        int STATE_BUFFERING = 2;
        /**
         * The player is able to immediately play from the current position. The player will be playing if
         * {@link ExoPlayer#getPlayWhenReady()} returns true, and paused otherwise.
         */
        int STATE_READY = 3;
        /**
         * The player has finished playing the media.
         */
        int STATE_ENDED = 4;
    }

    private List<ExoPlayer.EventListener> mEventListenerBuffers;
    private SimpleExoPlayer mExoPlayer;

    @StateCode
    private int mState;

    void setPlayBinder(SimpleExoPlayer player) {
        mExoPlayer = player;
        if (mExoPlayer == null) {
            return;
        }
            executeAfterBind();

        mExoPlayer.addListener(new ExoPlayerEventListenerAdapter(){
            @Override
            public void onPlayerError(ExoPlaybackException error) {
                super.onPlayerError(error);
                // TODO: 2017/9/24 通知错误
            }
        });
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
        return mExoPlayer != null && mExoPlayer.getPlayWhenReady();
    }

    public long getDuration() {
        return mExoPlayer != null ? mExoPlayer.getDuration() : 0;
    }

    public long getCurrentPosition() {
        return mExoPlayer != null ? mExoPlayer.getCurrentPosition() : 0;
    }

    public long getBufferedPosition() {
        return mExoPlayer != null ? mExoPlayer.getBufferedPosition() : 0;
    }

    public int getPlaybackState() {
        if (mExoPlayer == null) {
            return StateCode.STATE_IDLE;
        }
        return mExoPlayer.getPlaybackState();
    }

    public void badPlay(PlayerException e) {
        // TODO: 2017/9/24 发送通知
    }

    public void setState(int state) {
        mState = state;
    }

    public void addListener(ExoPlayer.EventListener listener) {
        if (mExoPlayer != null) {
            mExoPlayer.addListener(listener);
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
        if (mExoPlayer != null) {
            mExoPlayer.removeListener(listener);
            remove = true;
        }
        return remove;
    }
}
