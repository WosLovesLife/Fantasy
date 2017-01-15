package com.wosloveslife.fantasy;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.wosloveslife.fantasy.bean.BMusic;
import com.yesing.blibrary_wos.utils.assist.WLogger;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.wosloveslife.fantasy.utils.FormatUtils.progressBarValue;
import static com.wosloveslife.fantasy.utils.FormatUtils.stringForTime;

/**
 * Created by zhangh on 2017/1/15.
 */

public class ControlView extends FrameLayout {
    @BindView(R.id.iv_album)
    ImageView mIvAlbum;
    /** 歌曲名 */
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    /** 艺术家 */
    @BindView(R.id.tv_artist)
    TextView mTvArtist;
    /** 当前进度 */
    @BindView(R.id.tv_progress)
    TextView mTvProgress;
    /** 总进度 */
    @BindView(R.id.tv_duration)
    TextView mTvDuration;

    /** 播放/暂停 */
    @BindView(R.id.iv_play_btn)
    ImageView mIvPlayBtn;
    /** 上一曲按钮 */
    @BindView(R.id.iv_previous_btn)
    ImageView mIvPreviousBtn;
    /** 下一曲按钮 */
    @BindView(R.id.iv_next_btn)
    ImageView mIvNextBtn;

    /** 进度条(不可拖动) */
    @BindView(R.id.pb_progress)
    ProgressBar mPbProgress;

    //==============
    private BMusic mCurrentMusic;

    //=============
    private SimpleExoPlayer mPlayer;

    //====Var
    /** 如果手正在拖动SeekBar,就不能让Progress自动跳转 */
    boolean mDragging;


    public ControlView(Context context) {
        this(context, null);
    }

    public ControlView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ControlView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_control, this);
        ButterKnife.bind(this, view);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mPbProgress.setProgressBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.transparent)));
        }
    }

    public void setPlayer(SimpleExoPlayer player) {
        mPlayer = player;

        mPlayer.addListener(new ExoPlayer.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {
                WLogger.logD("timeline = " + timeline + "; manifest = " + manifest);
                updateProgress();
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                WLogger.logD("trackGroups = " + trackGroups + "; trackSelections = " + trackSelections);
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
                WLogger.logD("isLoading = " + isLoading);
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                WLogger.logD("playWhenReady = " + playWhenReady + "; playbackState = " + playbackState);
                updateProgress();
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                WLogger.logE("error = " + error);
            }

            @Override
            public void onPositionDiscontinuity() {
                WLogger.logD("onPositionDiscontinuity()");
                updateProgress();
            }
        });
    }

    /**
     * 当歌曲切换时通过该功能同步该控件的状态
     *
     * @param music
     */
    public void syncPlayView(BMusic music) {
        if (music == null) return;

        if (mPlayer == null) {
            throw new IllegalStateException("必须先设置Player");
        }

        if (music.playState == 1) {
            mIvPlayBtn.setImageResource(R.drawable.ic_pause);
        } else {
            mIvPlayBtn.setImageResource(R.drawable.ic_play_arrow);
        }

        if (music.equals(mCurrentMusic)) {
            return;
        }
        mCurrentMusic = music;

        Glide.with(getContext())
                .load(music.album)
                .placeholder(R.color.gray_disable)
                .crossFade()
                .into(mIvAlbum);
        mTvTitle.setText(TextUtils.isEmpty(music.title) ? "未知" : music.title);
        mTvArtist.setText(TextUtils.isEmpty(music.artist) ? "未知" : music.artist);
        mTvProgress.setText("00:00");
        mTvDuration.setText(DateFormat.format("mm:ss", music.duration).toString());
    }

    private void updateProgress() {
        long duration = mPlayer == null ? 0 : mPlayer.getDuration();
        long position = mPlayer == null ? 0 : mPlayer.getCurrentPosition();
        if (mTvDuration != null) {
            mTvDuration.setText(stringForTime(duration));
        }
        if (mTvProgress != null && !mDragging) {
            mTvProgress.setText(stringForTime(position));
        }

        if (!mDragging) {
            mPbProgress.setProgress(progressBarValue(position, duration));
        }

        /* TODO 如果是网络资源,则显示缓存进度 */
        if (false) {
            long bufferedPosition = mPlayer == null ? 0 : mPlayer.getBufferedPosition();
            mPbProgress.setSecondaryProgress(progressBarValue(bufferedPosition, duration));
        }

        // Schedule an update if necessary.
        int playbackState = mPlayer == null ? ExoPlayer.STATE_IDLE : mPlayer.getPlaybackState();
        if (playbackState != ExoPlayer.STATE_IDLE && playbackState != ExoPlayer.STATE_ENDED) {
            long delayMs;
            if (mPlayer.getPlayWhenReady() && playbackState == ExoPlayer.STATE_READY) {
                delayMs = 1000 - (position % 1000);
                if (delayMs < 200) {
                    delayMs += 1000;
                }
            } else {
                delayMs = 1000;
            }
            mPbProgress.postDelayed(updateProgressAction, delayMs);
        }
    }

    /**
     * 计数器回调
     */
    private final Runnable updateProgressAction = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };

    @OnClick({R.id.iv_previous_btn, R.id.iv_play_btn, R.id.iv_next_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_previous_btn:
                if (mControlListener != null) {
                    mControlListener.previous();
                }
                break;
            case R.id.iv_play_btn:
                if (mControlListener != null) {
                    /* 判断是播放还是暂停, 回传 */
                    mControlListener.play();
                }
                break;
            case R.id.iv_next_btn:
                if (mControlListener != null) {
                    mControlListener.next();
                }
                break;
        }
    }

    //==============================================================================================
    ControlListener mControlListener;

    public void setControlListener(ControlListener listener) {
        mControlListener = listener;
    }

    interface ControlListener {
        void previous();

        void next();

        void play();

        void pause();
    }
}
