package com.wosloveslife.fantasy.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.support.design.widget.FloatingActionButton;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.makeramen.roundedimageview.RoundedImageView;
import com.mpatric.mp3agic.ID3v2;
import com.orhanobut.logger.Logger;
import com.wosloveslife.dao.Audio;
import com.wosloveslife.fantasy.App;
import com.wosloveslife.fantasy.R;
import com.wosloveslife.fantasy.adapter.ExoPlayerEventListenerAdapter;
import com.wosloveslife.fantasy.adapter.SubscriberAdapter;
import com.wosloveslife.fantasy.helper.SPHelper;
import com.wosloveslife.fantasy.lrc.BLyric;
import com.wosloveslife.fantasy.lrc.LrcView;
import com.wosloveslife.fantasy.manager.CustomConfiguration;
import com.wosloveslife.fantasy.manager.MusicManager;
import com.wosloveslife.fantasy.ui.loadingfac.FabProgressGlue;
import com.wosloveslife.fantasy.utils.FormatUtils;
import com.wosloveslife.fantasy.utils.NetWorkUtil;
import com.yesing.blibrary_wos.utils.assist.Toaster;
import com.yesing.blibrary_wos.utils.assist.WLogger;
import com.yesing.blibrary_wos.utils.screenAdaptation.Dp2Px;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import stackblur_java.StackBlurManager;

import static com.wosloveslife.fantasy.R.id.iv_favor;

/**
 * Created by zhangh on 2017/1/15.
 */

public class ControlView extends FrameLayout implements NestedScrollingParent {
    private static final String KEY_IS_EXPAND = "fantasy.ui.ControlView.KEY_IS_EXPAND";
    private static final float PROGRESS_MAX = 100;

    @BindView(R.id.fl_root)
    FrameLayout mFlRoot;
    @BindView(R.id.iv_bg)
    ImageView mIvBg;
    /**
     * 这个控件是专门用来覆盖在mIvBg上面用来在切换头部背景时的CircularReveal动画使用,
     * 这样可以在原有背景色基础上有一个新的背景色展开的效果
     * 应该有更好的实现方式,暂时用这个
     */
    @BindView(R.id.card_view)
    CardView mCardView;
    @BindView(R.id.iv_album)
    RoundedImageView mIvAlbum;
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
//    /** 上一曲按钮 */
//    @BindView(R.id.iv_previous_btn)
//    ImageView mIvPreviousBtn;
//    /** 下一曲按钮 */
//    @BindView(R.id.iv_next_btn)
//    ImageView mIvNextBtn;
    /** 收藏按钮 */
    @BindView(iv_favor)
    AppCompatImageView mIvFavor;
    @BindView(R.id.iv_playOrder)
    AppCompatImageView mIvPlayOrder;

    /** 进度条(不可拖动) */
    @BindView(R.id.pb_progress)
    ProgressBar mPbProgress;
    /** 二段展开时的播放/暂停按钮 */
    @BindView(R.id.fac_play_btn)
    FloatingActionButton mFacPlayBtn;
    @BindView(R.id.tv_seek_value)
    TextView mTvSeekValue;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.lrc_view)
    LrcView mLrcView;

    //==============
    private Audio mCurrentMusic;
    private boolean mIsOnline;

    //=============
    private SimpleExoPlayer mPlayer;

    //=============Var
    /** 如果手正在拖动SeekBar,就不能让Progress自动跳转 */
    boolean mDragging;
    int mWidth;
    int m16dp;
    int m48dp;

    //=============联动相关
    NestedScrollingParentHelper mParentHelper;
    View mNestedScrollingChild;
    //=======
    /** 展开时的高度 */
    int mHeadMaxHeight;
    /** 收起时的高度 */
    int mHeadMinHeight;
    /** 从收起到展开总共偏移的距离(即 mHeadMaxHeight - mHeadMinHeight ) */
    int mMaxOffsetY;
    /** 用来记录当前头部布局的高度,因为mFlRoot.getHeight()获得到的高度可能正在设置中,会和真实的高度有偏差 */
    int mCurrentHeight;
    //=======
    /** 最后一次设置控件高度时是展开的还是收起的.true=展开中,false=收起中 */
    boolean mExpanding;
    /** 记录当前控件的展开形态 */
    boolean mIsExpanded;
    /** 记录当前控件的显示模式: true普通Toolbar,false歌曲控制 */
    private boolean mIsToolbarShown;
    //======
    /** 用于计算展开/收起的动画 */
    ValueAnimator mAnimator;
    //======
    VelocityTracker mVelocityTracker;
    int mTouchSlop;
    int mMinimumFlingVelocity;
    int mMaximumFlingVelocity;
    int mScrollPointerId;
    //======
    /** 封面的最大弧度(为圆形时) */
    int mAlbumMaxRadius;
    /** 播放进度文字等的最小左边距,同时也是最大向左偏移量 */
    int mMinLeftMargin;
    /** 歌曲名/艺术家的最小Margin值,最大Margin值为最小Margin+mMinLeftMargin */
    private int mTitleLeftMargin;
    int mAlbumSize;
    /** 播放总时长文字的最大向右偏移量 */
    int mDurationRightMargin;
    int mStatusBarHeight;

    //============
    private Drawable mPlayDrawable;
    private Drawable mPauseDrawable;

    private Drawable mDefAlbum;
    private Drawable mDefBlurredAlbum;
    private Drawable mDefColorMutedBg;
    private Drawable mDefColorTitle;
    private Drawable mDefColorBody;

    private Drawable mAlbum;
    /** 在当前封面的基础上进行了模糊处理,作为展开时的背景图片 */
    private Drawable mBlurredAlbum;
    private Drawable mColorMutedBg;
    private Drawable mColorTitle;
    private Drawable mColorBody;

    private ID3v2 mCurrentId3v2;
    private ValueAnimator mTitleAnimator;
    private FabProgressGlue mFabProgressGlue;

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
        m16dp = Dp2Px.toPX(getContext(), 16);
        m48dp = Dp2Px.toPX(getContext(), 48);

        mHeadMinHeight = Dp2Px.toPX(getContext(), 56);
        mHeadMaxHeight = Dp2Px.toPX(getContext(), 200);
        mMaxOffsetY = mHeadMaxHeight - mHeadMinHeight;
        /* 圆形的角度等于边长的一半,因为布局中写死了48dp,因此这里取24dp,如果有需要,应该在onSizeChanged()方法中监听子控件的边长除2 */
        mAlbumMaxRadius = Dp2Px.toPX(getContext(), 24);
        mMinLeftMargin = Dp2Px.toPX(getContext(), 56);
        mTitleLeftMargin = Dp2Px.toPX(getContext(), 14);
        mAlbumSize = m48dp;
        mDurationRightMargin = Dp2Px.toPX(getContext(), 22);
        mStatusBarHeight = (int) getResources().getDimension(R.dimen.statusBar_height);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mMinimumFlingVelocity = ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();
        mMaximumFlingVelocity = ViewConfiguration.get(getContext()).getScaledMaximumFlingVelocity();

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_portrait_chicken_174);

        mPlayDrawable = getResources().getDrawable(R.drawable.ic_play_arrow);
        mPauseDrawable = getResources().getDrawable(R.drawable.ic_pause);

        mDefAlbum = new BitmapDrawable(bitmap);
        mDefBlurredAlbum = getResources().getDrawable(R.drawable.bg_blur);
        mDefColorTitle = new ColorDrawable(getResources().getColor(R.color.white));
        mDefColorMutedBg = new ColorDrawable(getResources().getColor(R.color.colorPrimary));
        mDefColorBody = new ColorDrawable(getResources().getColor(R.color.colorAccent));

        mVelocityTracker = VelocityTracker.obtain();
        mParentHelper = new NestedScrollingParentHelper(this);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        ControlViewState state = new ControlViewState(super.onSaveInstanceState());
        state.mIsExpanded = mIsExpanded;
        state.mExpanding = mExpanding;
        state.mIsToolbarShown = mIsToolbarShown;
        return state;
    }

    class ControlViewState extends BaseSavedState {
        boolean mExpanding;
        boolean mIsExpanded;
        boolean mIsToolbarShown;

        ControlViewState(Parcelable source) {
            super(source);
        }
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof ControlViewState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        ControlViewState cs = (ControlViewState) state;
        super.onRestoreInstanceState(cs.getSuperState());

        /* 恢复操作 */
        syncPlayView(mCurrentMusic);
        toggleToolbarShown(cs.mIsToolbarShown);
        toggleExpand(cs.mIsExpanded);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (childAt instanceof NestedScrollingChild) {
                mNestedScrollingChild = childAt;
                childAt.setPadding(0, mHeadMinHeight + mStatusBarHeight, 0, 0);
            }
        }

        mWidth = w;

        if (mFlRoot != null) {
            boolean isExpand = SPHelper.getInstance().get(KEY_IS_EXPAND, false);
            if (isExpand) {
                toggleExpand(true);
            }
        }
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);

        if (mFlRoot != null) return;
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_control, this, false);
        ButterKnife.bind(this, view);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mPbProgress.setProgressBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.transparent)));
        }

        ProgressBar mPbLoadingBig = (ProgressBar) view.findViewById(R.id.pb_loading_big);
        ViewCompat.setElevation(mPbLoadingBig, mFacPlayBtn.getCompatElevation());
        mFabProgressGlue = new FabProgressGlue(mFacPlayBtn, mPbLoadingBig);
        mFabProgressGlue.hide();

        mFlRoot.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean consume = false;
                float y = event.getY();
                mVelocityTracker.addMovement(event);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mScrollPointerId = MotionEventCompat.getPointerId(event, 0);
                        mHeadDownY = y;
                        mHeadClick = true;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(mHeadDownY - y) > mTouchSlop) {
                            int deltaY = Math.round(mHeadLastY - y);
                            setOffsetBy(deltaY);
                            consume = true;
                            mHeadClick = false;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (mHeadClick) {
                            performClick();
                            break;
                        }
                        mVelocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                        float velocityY = VelocityTrackerCompat.getYVelocity(mVelocityTracker, mScrollPointerId);
                        if (Math.abs(velocityY) < mMinimumFlingVelocity
                                || (velocityY < 0 && mFlRoot.getHeight() <= mHeadMinHeight)
                                || (velocityY > 0 && mFlRoot.getHeight() >= mHeadMaxHeight)) {
                            if (mAnimator == null || !mAnimator.isRunning()) {
                                toggleExpand(mCurrentHeight > mHeadMinHeight + mMaxOffsetY / 2);
                            }
                        } else {
                            toggleExpand(velocityY > 0);
                        }

                        mVelocityTracker.clear();
                        consume = true;
                        break;
                }
                mHeadLastY = y;
                return consume;
            }
        });

        mLrcView.setOnSeekLrcProgressListener(new LrcView.OnSeekLrcProgressListener() {
            @Override
            public void onSeekingProgress(long progress) {
                mLrcSeeking = true;
                mLrcProgress = progress;
                if (mPlayer != null) {
                    if (progress > mPlayer.getCurrentPosition()) {
                        mFacPlayBtn.setImageResource(R.drawable.ic_fast_forward);
                    } else if (progress < mPlayer.getCurrentPosition()) {
                        mFacPlayBtn.setImageResource(R.drawable.ic_fast_rewind);
                    }
                }
            }

            @Override
            public void onSeekFinish(long progress) {
                mLrcSeeking = false;
                toggleFacBtn(isPlaying());
            }
        });

        syncPlayOrderVisual();

        addView(view);
    }

    float mHeadLastY;
    float mHeadDownY;
    boolean mHeadClick;

    boolean mLrcSeeking;
    long mLrcProgress;

    public void setPlayer(SimpleExoPlayer player) {
        mPlayer = player;

        mPlayer.addListener(new ExoPlayerEventListenerAdapter() {

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                updateProgress();
            }

            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {
                updateProgress();
            }

            @Override
            public void onPositionDiscontinuity() {
                updateProgress();
            }
        });
    }

    //==========================================状态同步-start======================================

    /**
     * 当歌曲切换时通过该功能同步该控件的状态
     *
     * @param music
     */
    @UiThread
    public void syncPlayView(Audio music) {
        if (music == null || mPlayer == null) return;

        if (isPlaying()) {
            mIvPlayBtn.setImageDrawable(mPauseDrawable);
        } else {
            mIvPlayBtn.setImageDrawable(mPlayDrawable);
        }
        toggleFacBtn(mPlayer.getPlayWhenReady());

        toggleLrcLoop();

        if (MusicManager.getInstance().isFavored(mCurrentMusic)) {
            mIvFavor.setImageResource(R.drawable.ic_favored_white);
        } else {
            mIvFavor.setImageResource(R.drawable.ic_favor_white);
        }

        if (music.equals(mCurrentMusic)) return;

        String currentAlbum = mCurrentMusic != null ? mCurrentMusic.album : null;
        mCurrentMusic = music;
        mIsOnline = mCurrentMusic.path.startsWith("http");

        mTvTitle.setText(TextUtils.isEmpty(music.title) ? "未知" : music.title);
        mTvArtist.setText(TextUtils.isEmpty(music.artist) ? "未知" : music.artist);
        mTvProgress.setText("00:00");
        mTvDuration.setText(DateFormat.format("mm:ss", music.duration).toString());

        if (mCurrentMusic == null) return;

        if (!TextUtils.equals(currentAlbum, music.album)) {
            MusicManager.getInstance().getAlbum(mCurrentMusic, mAlbumSize)
                    .observeOn(Schedulers.computation())
                    .subscribe(new SubscriberAdapter<Bitmap>() {
                        @Override
                        public void onNext(Bitmap bitmap) {
                            super.onNext(bitmap);
                            updateAlbum(bitmap);
                        }

                        @Override
                        public void onError(Throwable e) {
                            super.onError(e);
                            updateAlbum(null);
                        }
                    });
        }

        updateProgress();

        MusicManager.getInstance()
                .getLrc(mCurrentMusic)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SubscriberAdapter<BLyric>() {
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        updateLrc(null);
                        if (NetWorkUtil.isNetWorkAvailable(getContext())) {
                            Toaster.showShort("错误 " + e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(BLyric bLyric) {
                        super.onNext(bLyric);
                        updateLrc(bLyric);
                    }
                });
    }

    /**
     * 通过Pattern获得封面的色调作为头部控件收起时和Toolbar的背景色<br/>
     * 对封面作模糊处理作为头部控件展开时的背景图<br/>
     * 对封面信息做对比,如果是同样的封面(同专辑)就不作处理,避免不必要的开支和可能的延迟<br/>
     *
     * @param bitmap 如果等于null 则恢复默认的色彩和背景
     */
    @WorkerThread
    private void updateAlbum(@Nullable Bitmap bitmap) {
        if (bitmap == null && mAlbum == mDefAlbum) return;

        if (bitmap == null) {
            mAlbum = mDefAlbum;
            mBlurredAlbum = mDefBlurredAlbum;
            mColorMutedBg = mDefColorMutedBg;
            mColorTitle = mDefColorTitle;
            mColorBody = mDefColorBody;
        } else {
            mAlbum = new BitmapDrawable(bitmap);
            mBlurredAlbum = new BitmapDrawable(new StackBlurManager(bitmap).process(60));

            Palette.Swatch mutedSwatch = Palette.from(bitmap).generate().getMutedSwatch();
            if (mutedSwatch != null) {
                mColorMutedBg = new ColorDrawable(mutedSwatch.getRgb());
                mColorTitle = new ColorDrawable(mutedSwatch.getTitleTextColor());
                mColorBody = new ColorDrawable(mutedSwatch.getBodyTextColor());
            }
        }

        Logger.d("准备设置封面 时间 = " + System.currentTimeMillis());
        App.executeOnMainThread(new SubscriberAdapter<Object>() {
            @Override
            public void onCompleted() {
                Logger.d("准备设置封面2 时间 = " + System.currentTimeMillis());
                mIvAlbum.setImageDrawable(mAlbum);
                if (ViewCompat.isAttachedToWindow(mIvBg) && !mIsExpanded && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    mIvBg.setImageDrawable(mColorMutedBg);
                    Animator animator = ViewAnimationUtils.createCircularReveal(
                            mIvBg,
                            mIvAlbum.getWidth() / 2 + mIvAlbum.getLeft(),
                            mIvBg.getHeight() / 2,
                            0,
                            mIvBg.getWidth());
                    animator.setInterpolator(new AccelerateDecelerateInterpolator());
                    animator.setDuration(320);
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            mCardView.setCardBackgroundColor(((ColorDrawable) mColorMutedBg).getColor());
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            super.onAnimationCancel(animation);
                            mCardView.setCardBackgroundColor(((ColorDrawable) mColorBody).getColor());
                        }
                    });
                    animator.start();
                } else {
                    toggleAlbumBg(mIsExpanded);
                }
                Logger.d("封面设置完成 时间 = " + System.currentTimeMillis());
            }
        });
    }

    @UiThread
    private void updateProgress() {
        long duration = mPlayer == null ? 0 : mPlayer.getDuration();
        long position = mPlayer == null ? 0 : mPlayer.getCurrentPosition();

        if (duration >= 0) {
            if (mTvDuration != null) {
                mTvDuration.setText(FormatUtils.stringForTime(duration));
            }
            mPbProgress.setMax((int) (duration / 1000));
        }

        mTvProgress.setText(FormatUtils.stringForTime(position));

        if (!mDragging) {
            mPbProgress.setProgress((int) (position / 1000));
        }

        /* 如果是网络资源(播放地址以http开头)则显示缓存进度 */
        if (mIsOnline) {
            long bufferedPosition = mPlayer == null ? 0 : mPlayer.getBufferedPosition();
            mPbProgress.setSecondaryProgress((int) (bufferedPosition / 1000));
        }

        mHandler.removeCallbacksAndMessages(null);

        // Schedule an update if necessary.
        int playbackState = mPlayer == null ? ExoPlayer.STATE_IDLE : mPlayer.getPlaybackState();
        if (isPlaying() && playbackState != ExoPlayer.STATE_IDLE && playbackState != ExoPlayer.STATE_ENDED) {
            long delayMs;
            if (playbackState == ExoPlayer.STATE_READY) {
                delayMs = 1000 - (position % 1000);
                if (delayMs < 200) {
                    delayMs += 1000;
                }
            } else {
                delayMs = 1000;
            }
            mHandler.sendEmptyMessageDelayed(0, delayMs);
        }
    }

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            updateProgress();
        }
    };

    private boolean isPlaying() {
        return mPlayer != null && mPlayer.getPlayWhenReady();
    }

    private void toggleFacBtn(boolean play) {
        if (play) {
            mFacPlayBtn.setImageDrawable(mPauseDrawable);
        } else {
            mFacPlayBtn.setImageDrawable(mPlayDrawable);
        }
    }

    private void updateLrc(BLyric bLyric) {
        mLrcView.setLrc(bLyric);
        toggleLrcLoop();
    }

    private void toggleLrcLoop() {
        if (mPlayer == null) return;
        long progress = mPlayer.getCurrentPosition();
        WLogger.d("toggleLrcLoop : progress =  " + progress);
        if (isPlaying()) {
            mLrcView.setAutoSyncLrc(true, progress < 0 ? 0 : progress);
        } else {
            mLrcView.setAutoSyncLrc(false, progress < 0 ? 0 : progress);
        }
    }

    //===========================================状态同步-end=======================================

    @OnClick({R.id.fl_root, R.id.toolbar, R.id.iv_play_btn, R.id.fac_play_btn,
            iv_favor, R.id.iv_playOrder})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fl_root:
                if (mDragging) return;
                toggleToolbarShown(true);
                break;
            case R.id.toolbar:
                toggleToolbarShown(false);
                break;
            case R.id.iv_play_btn:
                if (mIsExpanded) {
                    break;
                }
            case R.id.fac_play_btn:
                if (mPlayer == null || mControlListener == null) break;
                if (mLrcSeeking) {
                    mLrcSeeking = false;
                    mPlayer.seekTo(mLrcProgress);
                    toggleFacBtn(isPlaying());
                    toggleLrcLoop();
                    break;
                }
                /* 判断是播放还是暂停, 回传 */
                if (isPlaying()) {
                    mControlListener.pause();
                } else {
                    mControlListener.play();
                }
                break;
            case iv_favor:
                if (mCurrentMusic == null) return;

                /* 通过等待歌曲同步来改变收藏状态 */
                if (MusicManager.getInstance().isFavored(mCurrentMusic)) {
                    MusicManager.getInstance().removeFavor(mCurrentMusic);
                } else {
                    MusicManager.getInstance().addFavor(mCurrentMusic);
                }
                break;
            case R.id.iv_playOrder:
                switch (CustomConfiguration.getPlayOrder()) {
                    case CustomConfiguration.PLAY_ORDER_SUCCESSIVE: // 列表循环
                        CustomConfiguration.savePlayOrder(CustomConfiguration.PLAY_ORDER_RANDOM);
                        break;
                    case CustomConfiguration.PLAY_ORDER_REPEAT_ONE: // 单曲循环
                        CustomConfiguration.savePlayOrder(CustomConfiguration.PLAY_ORDER_SUCCESSIVE);
                        break;
                    case CustomConfiguration.PLAY_ORDER_RANDOM: // 随机播放
                        CustomConfiguration.savePlayOrder(CustomConfiguration.PLAY_ORDER_REPEAT_ONE);
                        break;
                }
                syncPlayOrderVisual();
                break;
        }
    }

    private void syncPlayOrderVisual() {
        switch (CustomConfiguration.getPlayOrder()) {
            case CustomConfiguration.PLAY_ORDER_SUCCESSIVE: // 列表循环
                AnimatedVectorDrawableCompat vectorDrawableCompat = AnimatedVectorDrawableCompat.create(getContext(), R.drawable.animated_vector_order_dismiss);
                mIvPlayOrder.setImageDrawable(vectorDrawableCompat);
                if (vectorDrawableCompat != null) {
                    vectorDrawableCompat.start();
                }
                break;
            case CustomConfiguration.PLAY_ORDER_REPEAT_ONE: // 单曲循环
                AnimatedVectorDrawableCompat vectorDrawableCompat1 = AnimatedVectorDrawableCompat.create(getContext(), R.drawable.animated_vector_order_show);
                mIvPlayOrder.setImageDrawable(vectorDrawableCompat1);
                if (vectorDrawableCompat1 != null) {
                    vectorDrawableCompat1.start();
                }
                break;
            case CustomConfiguration.PLAY_ORDER_RANDOM: // 随机播放
                mIvPlayOrder.setImageResource(R.drawable.ic_order_random);
                break;
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacksAndMessages(null);
        mVelocityTracker.recycle();
        SPHelper.getInstance().save(KEY_IS_EXPAND, mIsExpanded);
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

    public Toolbar getToolbar() {
        return mToolbar;
    }

    //==============================================================================================
    //=========================================View联动相关=========================================
    //==============================================================================================

    //========================================触摸事件-start======================================

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                mDragging = false;
                break;
            default:
                mDragging = true;
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 这个值是为了防止从别的形态到第二阶段的滑动过程中拦截了触摸事件而开启了Seek
     * 也就是说,只有已经是展开了的形态下再次触摸并横向滑动,才可能拦截事件
     */
    boolean mIntercept;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercept = false;
        float x = ev.getX();
        float y = ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = x;
                if (mIsExpanded && y > mFlRoot.getBottom() - m16dp && y < mFlRoot.getBottom() + m16dp) {
                    mIntercept = true;
                }
                WLogger.d("onInterceptTouchEvent :  ");
            case MotionEvent.ACTION_MOVE:
                if (!mIntercept || y < mFlRoot.getBottom() - m16dp || y > mFlRoot.getBottom() + m16dp) {
                    intercept = false;
                    mIntercept = false;
                } else if (Math.abs(mDownX - x) > mTouchSlop && Math.abs(mLastX - x) > Math.abs(mLastY - y)) {
                    intercept = true;
                }
                break;
        }
        mLastX = x;
        mLastY = y;
        return intercept;
    }

    float mDownX;
    float mLastX;
    float mLastY;
    boolean mSeeking;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!mIsExpanded) return false;

        boolean consume = true;
        float x = ev.getX();
        float y = ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = x;
                if (y < mFlRoot.getBottom() - m16dp || y > mFlRoot.getBottom() + m16dp) {
                    consume = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                seekProgress(x, y, false);
                break;
            case MotionEvent.ACTION_UP:
                if (y < mFlRoot.getBottom() + m48dp) {
                    seekProgress(x, y, true);
                    break;
                }
            case MotionEvent.ACTION_CANCEL:
                recoverProgress();
                break;
        }
        mLastX = x;
        mLastY = y;
        return consume;
    }

    private void seekProgress(float x, float y, boolean seek) {
        if (!mSeeking) {
            mSeeking = true;
            mFabProgressGlue.smoothTranslationAndScale(0.3f, mFacPlayBtn.getTranslationX(), new FastOutLinearInInterpolator(), null);
            mFabProgressGlue.hideLoading();
        }
        mFacPlayBtn.setTranslationX(x - mFacPlayBtn.getLeft() - mFacPlayBtn.getWidth() / 2);

        int max = mPbProgress.getMax();
        float ratio = x / mWidth;
        int progress = Math.round(ratio * max);
        mPbProgress.setProgress(progress);

        if (mTvSeekValue.getVisibility() != VISIBLE) {
            mTvSeekValue.setScaleX(0);
            mTvSeekValue.setScaleY(0);
            mTvSeekValue.setVisibility(VISIBLE);
            mTvSeekValue.animate().cancel();
            mTvSeekValue.animate()
                    .scaleX(1)
                    .scaleY(1)
                    .setDuration(200)
                    .setStartDelay(100)
                    .setInterpolator(new LinearOutSlowInInterpolator())
                    .setListener(null);
        }
        float tX = x - mTvSeekValue.getWidth() / 2;
        if (tX < 0) tX = 0;
        if (tX > mWidth - mTvSeekValue.getWidth()) tX = mWidth - mTvSeekValue.getWidth();
        mTvSeekValue.setTranslationX(tX);
        if (y > mFlRoot.getBottom() + m48dp) {
            mTvSeekValue.setText("取消");
        } else {
            mTvSeekValue.setText(FormatUtils.stringForTime(progress * 1000));
        }

        if (seek) {
            if (mPlayer != null) {
                mPlayer.seekTo(progress * 1000);
                toggleLrcLoop();
            }
            recoverProgress();
        }
    }

    private void recoverProgress() {
        mSeeking = false;

        if (mTvSeekValue.getVisibility() == VISIBLE) {
            mTvSeekValue.setVisibility(VISIBLE);
            mTvSeekValue.animate().cancel();
            mTvSeekValue.animate()
                    .scaleX(0)
                    .scaleY(0)
                    .setDuration(200)
                    .setInterpolator(new FastOutLinearInInterpolator())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationCancel(Animator animation) {
                            super.onAnimationCancel(animation);
                            recoverFac();
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            recoverFac();
                        }
                    });
        }

        ValueAnimator animator = ValueAnimator.ofInt(mPbProgress.getProgress(), mPlayer != null ? (int) mPlayer.getCurrentPosition() / 1000 : 0);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int progress = (Integer) animation.getAnimatedValue();
                mPbProgress.setProgress(progress);
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                updateProgress();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                updateProgress();
            }
        });
        animator.setDuration(340);
        animator.start();
    }

    private void recoverFac() {
        mTvSeekValue.setVisibility(INVISIBLE);
        mFabProgressGlue.smoothTranslationAndScale(1, 0, new LinearOutSlowInInterpolator(), new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
//                mFabProgressGlue.showLoading();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
//                mFabProgressGlue.showLoading();
            }
        });
    }

    //========================================NestScroll-start======================================

    /**
     * 滑动开始的调用startNestedScroll()，Parent 收到onStartNestedScroll()回调，
     * 决定是否需要配合 Child 一起进行处理滑动，
     * 如果需要配合,还会回调{@link ControlView#onNestedScrollAccepted(View, View, int)}。
     */
    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return CustomConfiguration.isPlayControllerAutoExpand();
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        super.onNestedScrollAccepted(child, target, axes);
    }

    /**
     * 每次滑动前，Child 先询问 Parent 是否需要滑动，即dispatchNestedPreScroll()，
     * 这就回调到 Parent 的onNestedPreScroll()，
     * Parent 可以在这个回调中“劫持”掉 Child 的滑动，也就是先于 Child 滑动。
     *
     * @param dx       表示view本次x方向的滚动的总距离长度
     * @param dy       表示view本次y方向的滚动的总距离长度
     * @param consumed 表示父布局消费的距离,consumed[0]表示x方向,consumed[1]表示y方向
     */
    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        super.onNestedPreScroll(target, dx, dy, consumed);
        setOffsetBy(dy);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        if (Math.abs(velocityY) < mMinimumFlingVelocity
                || (velocityY > 0 && mFlRoot.getHeight() <= mHeadMinHeight)
                || (velocityY < 0 && mFlRoot.getHeight() >= mHeadMaxHeight)) {
            return false;
        }

        toggleExpand(velocityY < 0);
        /* 如果想让头部滚动不影响列表滚动,这里应该返回false */
        return false;
    }

    /**
     * 本次滑动结束
     */
    @Override
    public void onStopNestedScroll(View child) {
        super.onStopNestedScroll(child);

        if (mAnimator == null || !mAnimator.isRunning()) {
            toggleExpand(mCurrentHeight > mHeadMinHeight + mMaxOffsetY / 2);
        }
    }

    //========================================NestScroll-end========================================

    private void setOffset(int height) {
        if (height == mFlRoot.getHeight()) return;

        mExpanding = height > mFlRoot.getHeight();

        if (height < mHeadMinHeight) height = mHeadMinHeight;
        if (height > mHeadMaxHeight) height = mHeadMaxHeight;
        mCurrentHeight = height;

        ViewGroup.LayoutParams params = mFlRoot.getLayoutParams();
        params.height = height;
        mFlRoot.setLayoutParams(params);

        ViewGroup.LayoutParams params2 = mIvBg.getLayoutParams();
        params2.height = height + mStatusBarHeight;
        mIvBg.setLayoutParams(params2);

        if (mNestedScrollingChild != null) {
            mNestedScrollingChild.setPadding(0, height + mStatusBarHeight, 0, 0);
        }

        linkViews(getOffsetRadius(height));
    }

    private void setOffsetBy(int dy) {
        if ((dy > 0 && mFlRoot.getHeight() <= mHeadMinHeight) || (dy < 0 && mFlRoot.getHeight() >= mHeadMaxHeight)) {
            return;
        }
        setOffset(mFlRoot.getHeight() - dy);
    }

    private void toggleExpand(boolean expand) {
        int targetY;
        if (expand) {
            targetY = mHeadMaxHeight;
        } else {
            targetY = mHeadMinHeight;
        }

        if (targetY == mCurrentHeight) return;

        if (mAnimator == null) {
            mAnimator = new ValueAnimator();
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (Float) animation.getAnimatedValue();
                    setOffset((int) value);
                }
            });
        } else if (mAnimator.isRunning()) {
            mAnimator.cancel();
        }

        /* 这里的CurrentHeight是上次设置高度是记录的高度值,
        因为上次设置的高度可能还没有被应用,所以这里如果通过mFlRoot.getHeight()来获取高度,可能是不及时的 */
        mAnimator.setFloatValues(mCurrentHeight, targetY);
        float offset = targetY + 0f - mCurrentHeight;
        int duration = Math.min((int) (Math.abs(offset) / mMaxOffsetY * 320), 200);
        mAnimator.setDuration(duration);
        mAnimator.start();
    }

    //========================================其它的联动效果========================================

    private void linkViews(final float offsetRadius) {
        if (mToolbar.getVisibility() == VISIBLE) {
            toggleToolbarShown(false);
        }

        if (mExpanding && !mIsExpanded && offsetRadius > 0.5f) {
            mIsExpanded = true;
        } else if (!mExpanding && mIsExpanded && offsetRadius < 0.5) {
            mIsExpanded = false;
        } else {
            return;
        }

        final int value = mIsExpanded ? 0 : 1;
        if (mIsExpanded) {
            getScaleAlphaAnim(mIvAlbum, value).setListener(new ViewPropertyAnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(View view) {
                    toggleAlbumBg(mIsExpanded);
                    toggleTextOffset(mIsExpanded);
                    toggleControlBtn(mIsExpanded);
                    if (mIsExpanded) {
                        mFabProgressGlue.show(false);
                        toggleFacBtn(isPlaying());
                        mLrcView.setVisibility(VISIBLE);
                    } else {
                        mFabProgressGlue.hide();
                        mLrcView.setVisibility(INVISIBLE);
                    }
                }
            }).start();
        } else {
            toggleAlbumBg(false);
            toggleTextOffset(false);
            toggleControlBtn(false);
            mFabProgressGlue.hide();
            mFacPlayBtn.setTranslationX(0);
            mTvSeekValue.setVisibility(INVISIBLE);
            mLrcView.setVisibility(INVISIBLE);
            getScaleAlphaAnim(mIvAlbum, value)
                    .setListener(null)
                    .start();
        }
    }

    private void toggleTextOffset(boolean expand) {
        updateTitleMargin(expand ? mTitleLeftMargin : mTitleLeftMargin + mMinLeftMargin);
        controlTextOffsetAnim(mTvProgress, expand ? -mMinLeftMargin : 0);
        controlTextOffsetAnim(mTvDuration, expand ? mDurationRightMargin : 0);
    }

    private void updateTitleMargin(int targetMargin) {
        if (mTitleAnimator == null) {
            mTitleAnimator = ValueAnimator.ofInt(mTvArtist.getLeft(), targetMargin);
            mTitleAnimator.setDuration(240);
            mTitleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int margin = (Integer) animation.getAnimatedValue();
                    ViewGroup.MarginLayoutParams params = (MarginLayoutParams) mTvTitle.getLayoutParams();
                    params.leftMargin = margin;
                    mTvTitle.setLayoutParams(params);
                    ViewGroup.MarginLayoutParams params2 = (MarginLayoutParams) mTvArtist.getLayoutParams();
                    params2.leftMargin = margin;
                    mTvArtist.setLayoutParams(params2);
                }
            });
        } else {
            mTitleAnimator.cancel();
        }
        mTitleAnimator.setIntValues(mTvArtist.getLeft(), targetMargin);
        mTitleAnimator.start();
    }

    private void toggleControlBtn(boolean expand) {
        getScaleAlphaAnim(mIvPlayBtn, expand ? 0 : 1).start();
        getScaleAlphaAnim(mIvPlayOrder, expand ? 1 : 0).start();
    }

    private void toggleAlbumBg(boolean expand) {
        if (expand) {   // 展开时背景为专辑模糊图片
            Drawable source;
            if (mIvBg.getDrawable() != null) {
                source = mIvBg.getDrawable();
            } else {
                source = getHeadColorDrawable();
            }
            mIvBg.setImageDrawable(getTransitionDrawable(source, getHeadDrawable(), 380));
        } else {    // 收起时背景为专辑色调纯色
            Drawable source;
            if (mIvBg.getDrawable() != null) {
                source = mIvBg.getDrawable();
            } else {
                source = getHeadDrawable();
            }
            mIvBg.setImageDrawable(getTransitionDrawable(source, getHeadColorDrawable(), 380));
        }
    }

    private Drawable getHeadDrawable() {
        if (mBlurredAlbum == null) {
            return mDefBlurredAlbum;
        } else {
            return mBlurredAlbum;
        }
    }

    private Drawable getHeadColorDrawable() {
        if (mColorMutedBg == null) {
            return mDefColorMutedBg;
        } else {
            return mColorMutedBg;
        }
    }

    /**
     * 切换为普通Toolbar模式或者歌曲信息模式.<bar/>
     * 注意, 如果当前头部处于展开状态,则不会进行任何切换处理.
     *
     * @param isToolbarShown true 将头部切换为Toolbar,显示导航键,列表名,menu等
     */
    private void toggleToolbarShown(boolean isToolbarShown) {
        if (mIsExpanded || mIsToolbarShown == isToolbarShown) return;
        mIsToolbarShown = isToolbarShown;
        if (ViewCompat.isAttachedToWindow(mToolbar) && isToolbarShown && mToolbar.getVisibility() != VISIBLE) {
            mToolbar.setVisibility(VISIBLE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                Animator animator = ViewAnimationUtils.createCircularReveal(
                        mToolbar,
                        mIvAlbum.getWidth() / 2 + mIvAlbum.getLeft(),
                        mToolbar.getHeight() / 2,
                        0,
                        mToolbar.getWidth());
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.setDuration(320);
                animator.start();
            } else {
                ViewCompat.animate(mToolbar)
                        .alpha(1)
                        .setDuration(200)
                        .setListener(null)
                        .start();
            }
        } else if (!isToolbarShown && mToolbar.getVisibility() == VISIBLE) {
            if (ViewCompat.isAttachedToWindow(mToolbar) && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                Animator animator = ViewAnimationUtils.createCircularReveal(
                        mToolbar,
                        mIvAlbum.getWidth() / 2 + mIvAlbum.getLeft(),
                        mToolbar.getHeight() / 2,
                        mToolbar.getWidth(),
                        0);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.setDuration(320);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mToolbar.setVisibility(INVISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        super.onAnimationCancel(animation);
                        mToolbar.setVisibility(INVISIBLE);
                    }
                });
                animator.start();
            } else {
                ViewCompat.animate(mToolbar)
                        .alpha(0)
                        .setDuration(200)
                        .setListener(new ViewPropertyAnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(View view) {
                                super.onAnimationEnd(view);
                                mToolbar.setVisibility(INVISIBLE);
                            }
                        })
                        .start();
            }
        }
    }

    //==============================================================================================
    private float getOffsetRadius(int height) {
        float offsetY = height - mHeadMinHeight;
        return offsetY / mMaxOffsetY;
    }

    private ViewPropertyAnimatorCompat getScaleAlphaAnim(View v, float value) {
        return ViewCompat.animate(v)
                .scaleX(value)
                .scaleY(value)
                .alpha(value)
                .setDuration(240);
    }

    private void controlTextOffsetAnim(View v, int value) {
        ViewCompat.animate(v)
                .translationX(value)
                .setDuration(240)
                .start();
    }

    private static TransitionDrawable getTransitionDrawable(Drawable source, Drawable target, int duration) {
        TransitionDrawable transitionDrawable = new TransitionDrawable(new Drawable[]{source, target});
        transitionDrawable.setCrossFadeEnabled(true);
        transitionDrawable.startTransition(duration);
        return transitionDrawable;
    }
}
