package com.wosloveslife.fantasy.manager;

import android.os.Looper;

import com.wosloveslife.dao.Audio;
import com.wosloveslife.fantasy.adapter.SubscriberAdapter;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by zhangh on 2017/7/1.
 */

public class PlayState {
    private Audio mAudio;
    private long mDuration;
    private long mCurrentPosition;
    private long mBufferedPosition;
    private int mPlayState;
    private int mBufferState;

    private List<OnStateChangeListener> mChangeListenerList = new ArrayList<>();

    private static PlayState sPlayState;

    private PlayState() {
    }

    public static PlayState getInstance() {
        if (sPlayState == null) {
            synchronized (PlayState.class) {
                if (sPlayState == null) {
                    sPlayState = new PlayState();
                }
            }
        }
        return sPlayState;
    }

    public interface OnStateChangeListener {
        void onAudioChanged(Audio audio);

        void onCurrentPositionChanged(long position);

        void onBufferedPosition(long position);

        void onDurationChanged(long duration);

        void onPlayStateChanged(boolean isPlaying, int playState);

        void onBufferStateChanged(int bufferState);
    }

    public void addOnStateChangeListener(OnStateChangeListener listener) {
        if (listener != null) {
            mChangeListenerList.add(listener);
        }
    }

    public boolean removeOnStateChangeListener(OnStateChangeListener listener) {
        return listener != null && mChangeListenerList.remove(listener);
    }

    //==============================================================================================

    public Audio getAudio() {
        return mAudio;
    }

    public void setAudio(final Audio audio) {
        mAudio = audio;
        getMainObs().subscribe(new SubscriberAdapter<OnStateChangeListener>() {
            @Override
            public void onNext(OnStateChangeListener listener) {
                super.onNext(listener);
                listener.onAudioChanged(audio);
            }
        });
    }

    public long getDuration() {
        return mDuration;
    }

    public void setDuration(final long duration) {
        mDuration = duration;
        getMainObs().subscribe(new SubscriberAdapter<OnStateChangeListener>() {
            @Override
            public void onNext(OnStateChangeListener listener) {
                super.onNext(listener);
                listener.onDurationChanged(duration);
            }
        });
    }

    public long getCurrentPosition() {
        return mCurrentPosition;
    }

    public void setCurrentPosition(final long currentPosition) {
        mCurrentPosition = currentPosition;
        getMainObs().subscribe(new SubscriberAdapter<OnStateChangeListener>() {
            @Override
            public void onNext(OnStateChangeListener listener) {
                super.onNext(listener);
                listener.onCurrentPositionChanged(currentPosition);
            }
        });
    }

    public long getBufferedPosition() {
        return mBufferedPosition;
    }

    public void setBufferedPosition(final long bufferedPosition) {
        mBufferedPosition = bufferedPosition;
        getMainObs().subscribe(new SubscriberAdapter<OnStateChangeListener>() {
            @Override
            public void onNext(OnStateChangeListener listener) {
                super.onNext(listener);
                listener.onBufferedPosition(bufferedPosition);
            }
        });
    }

    public int getPlayState() {
        return mPlayState;
    }

    public void setPlayState(final int playState) {
        mPlayState = playState;
        getMainObs().subscribe(new SubscriberAdapter<OnStateChangeListener>() {
            @Override
            public void onNext(OnStateChangeListener listener) {
                super.onNext(listener);
                listener.onPlayStateChanged(playState == 1, playState);
            }
        });
    }

    public int getBufferState() {
        return mBufferState;
    }

    public void setBufferState(final int bufferState) {
        mBufferState = bufferState;
        getMainObs().subscribe(new SubscriberAdapter<OnStateChangeListener>() {
            @Override
            public void onNext(OnStateChangeListener listener) {
                super.onNext(listener);
                listener.onBufferStateChanged(bufferState);
            }
        });
    }

    public boolean isPlaying() {
        return mPlayState == 1;
    }

    //==============================================================================================

    private Observable<OnStateChangeListener> getMainObs() {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            return Observable.from(mChangeListenerList);
        }
        return Observable.from(mChangeListenerList).observeOn(AndroidSchedulers.mainThread());
    }
}
