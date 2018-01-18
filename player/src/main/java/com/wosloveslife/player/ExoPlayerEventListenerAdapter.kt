package com.wosloveslife.player

import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray

/**
 * Created by zhangh on 2017/1/19.
 */

open class ExoPlayerEventListenerAdapter : ExoPlayer.EventListener {

    override fun onPlayerError(error: ExoPlaybackException?) {}

    override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {}

    override fun onLoadingChanged(isLoading: Boolean) {}

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {}

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?) {}

    override fun onPositionDiscontinuity() {}
}
