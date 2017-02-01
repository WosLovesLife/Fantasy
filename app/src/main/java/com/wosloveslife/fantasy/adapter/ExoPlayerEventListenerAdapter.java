package com.wosloveslife.fantasy.adapter;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.orhanobut.logger.Logger;
import com.yesing.blibrary_wos.utils.assist.WLogger;

import static com.orhanobut.logger.Logger.v;

/**
 * Created by zhangh on 2017/1/19.
 */

public class ExoPlayerEventListenerAdapter implements ExoPlayer.EventListener {

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        Logger.e(error, "播放错误");
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        WLogger.v("trackGroups = " + trackGroups + "; trackSelections = " + trackSelections);
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        WLogger.v("isLoading = " + isLoading);
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        WLogger.v("playWhenReady = " + playWhenReady + "; playbackState = " + playbackState);
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
        WLogger.d("timeline = " + timeline + "; manifest = " + manifest);
    }

    @Override
    public void onPositionDiscontinuity() {
        Logger.d("onPositionDiscontinuity()");
    }
}
