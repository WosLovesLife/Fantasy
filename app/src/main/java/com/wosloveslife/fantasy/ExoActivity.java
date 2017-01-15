package com.wosloveslife.fantasy;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

/**
 * Created by zhangh on 2017/1/15.
 */

public class ExoActivity extends AppCompatActivity {

    private SimpleExoPlayer mPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exo);

        initPlayer();
    }

    private void initPlayer() {
        //==========step1初始操作
        // 1. Create a default TrackSelector
        Handler mainHandler = new Handler();
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

// 2. Create a default LoadControl
        LoadControl loadControl = new DefaultLoadControl();

// 3. Create the player
        mPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);

        //==========step2绑定View和控制器
        // Bind the player to the view.
        SimpleExoPlayerView simpleExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.simple_exo_player_view);
        simpleExoPlayerView.setPlayer(mPlayer);

        //=========step3准备播放
        // Measures bandwidth during playback. Can be null if not required.
        DefaultBandwidthMeter bandwidthMeter1 = new DefaultBandwidthMeter();
// Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "yourApplicationName"), bandwidthMeter1);
// Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
// This is the MediaSource representing the media to be played.

        Uri uri = Uri.parse("file:///android_asset/20140509.mp4");
        MediaSource videoSource = new ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory, null, null);

        /* 无缝循环播放 */
        LoopingMediaSource loopingSource = new LoopingMediaSource(videoSource);

        /* 无缝播放一个序列 */
        MediaSource secondSource = new ExtractorMediaSource(Uri.parse("file:///android_asset/xiangsheng.mp4"), dataSourceFactory, extractorsFactory, null, null);
        ConcatenatingMediaSource concatenatedSource = new ConcatenatingMediaSource(videoSource, secondSource);

// Prepare the player with the source.
        mPlayer.prepare(concatenatedSource);
        mPlayer.addListener(new ExoPlayer.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            @Override
            public void onPositionDiscontinuity() {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mPlayer.release();
    }
}
