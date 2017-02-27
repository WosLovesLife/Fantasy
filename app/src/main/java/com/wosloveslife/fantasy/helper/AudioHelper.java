package com.wosloveslife.fantasy.helper;

import android.media.AudioManager;

import com.wosloveslife.fantasy.adapter.ExoPlayerEventListenerAdapter;
import com.wosloveslife.fantasy.services.PlayService;
import com.yesing.blibrary_wos.utils.assist.WLogger;

import static android.content.Context.AUDIO_SERVICE;

/**
 * Created by zhangh on 2017/2/13.
 */

public class AudioHelper {
    private final PlayService mPlayService;
    private AudioManager mAudioManager;

    private boolean mSavePlay;

    /**
     * 如果已经获取了焦点再次获取焦点会造成回调失去焦点.因此使用该变量记录当前的焦点状态,
     * 因为焦点适用于整个程序,所以使用静态变量
     */
    private static boolean sHasFocus;

    public AudioHelper(PlayService playService) {
        mPlayService = playService;
        mPlayService.addListener(new ExoPlayerEventListenerAdapter() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                super.onPlayerStateChanged(playWhenReady, playbackState);
                if (playWhenReady) {
                    /* 在这里重置该值, 避免影响后续的播放状态 */
                    mSavePlay = false;
                }
            }
        });
    }

    public void registerAudioFocus() {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) mPlayService.getSystemService(AUDIO_SERVICE);
        }

        if (sHasFocus) return;

        int result = mAudioManager.requestAudioFocus(new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: // 短暂失去焦点
                        mSavePlay = mPlayService.isPlaying();
                    case AudioManager.AUDIOFOCUS_LOSS: // 失去焦点,
                        if (mPlayService.isPlaying()) {
                            mPlayService.pause();
                        }
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT: //
                    case AudioManager.AUDIOFOCUS_REQUEST_GRANTED: // 获得焦点
                        if (mSavePlay) {
                            mPlayService.play();
                        }
                        break;
                }
                sHasFocus = focusChange > 0;
                WLogger.d("requestAudioFocus onAudioFocusChange : [focusChange] = " + focusChange);
            }
        }, 1, AudioManager.AUDIOFOCUS_GAIN);

        sHasFocus = result > 0;
    }

    public void abandonAudioFocus() {
        if (mAudioManager != null) {
            mAudioManager.abandonAudioFocus(null);
        }
    }
}
