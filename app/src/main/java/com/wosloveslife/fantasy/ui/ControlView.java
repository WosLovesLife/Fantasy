package com.wosloveslife.fantasy.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.makeramen.roundedimageview.RoundedImageView;
import com.orhanobut.logger.Logger;
import com.wosloveslife.fantasy.R;
import com.wosloveslife.fantasy.adapter.ExoPlayerEventListenerAdapter;
import com.wosloveslife.fantasy.bean.BMusic;
import com.wosloveslife.fantasy.utils.FormatUtils;
import com.yesing.blibrary_wos.utils.screenAdaptation.Dp2Px;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by zhangh on 2017/1/15.
 */

public class ControlView extends FrameLayout implements NestedScrollingParent {
    private static final float PROGRESS_MAX = 100;

    @BindView(R.id.fl_root)
    FrameLayout mFlRoot;
    @BindView(R.id.iv_bg)
    ImageView mIvBg;
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
    /** 上一曲按钮 */
    @BindView(R.id.iv_previous_btn)
    ImageView mIvPreviousBtn;
    /** 下一曲按钮 */
    @BindView(R.id.iv_next_btn)
    ImageView mIvNextBtn;

    /** 进度条(不可拖动) */
    @BindView(R.id.pb_progress)
    ProgressBar mPbProgress;
    /** 二段展开时的播放/暂停按钮 */
    @BindView(R.id.fac_play_btn)
    FloatingActionButton mFacPlayBtn;

    //==============
    private BMusic mCurrentMusic;
    private boolean mIsOnline;

    //=============
    private SimpleExoPlayer mPlayer;

    //=============Var
    /** 如果手正在拖动SeekBar,就不能让Progress自动跳转 */
    boolean mDragging;

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
    /** 最后一次设置控件高度时是展开的还是收起的.true=展开中,false=收起中 */
    boolean mExpanding;
    /** 记录当前控件的展开形态 */
    boolean mIsExpanded;
    //======
    /** 用于计算展开/收起的动画 */
    ValueAnimator mAnimator;
    //======
    int mTouchSlop;
    int mMinimumFlingVelocity;
    //======
    /** 封面的最大弧度(为圆形时) */
    int mAlbumMaxRadius;
    /** 歌曲名/艺术家/播放进度文字等的最小左边距,同时也是最大向左偏移量 */
    int mMinLeftMargin;
    /** 播放总时长文字的最大向右偏移量 */
    int mDurationRightMargin;
    int mStatusBarHeight;

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
        mHeadMinHeight = Dp2Px.toPX(getContext(), 56);
        mHeadMaxHeight = Dp2Px.toPX(getContext(), 160);
        mMaxOffsetY = mHeadMaxHeight - mHeadMinHeight;
        /* 圆形的角度等于边长的一半,因为布局中写死了48dp,因此这里取24dp,如果有需要,应该在onSizeChanged()方法中监听子控件的边长除2 */
        mAlbumMaxRadius = Dp2Px.toPX(getContext(), 24);
        mMinLeftMargin = Dp2Px.toPX(getContext(), 56);
        mDurationRightMargin = Dp2Px.toPX(getContext(), 58);
        mStatusBarHeight = (int) getResources().getDimension(R.dimen.statusBar_height);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mMinimumFlingVelocity = ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();

        mParentHelper = new NestedScrollingParentHelper(this);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        ControlViewState state = new ControlViewState(super.onSaveInstanceState());
        state.mIsExpanded = mIsExpanded;
        state.mExpanding = mExpanding;
        return state;
    }

    class ControlViewState extends BaseSavedState {
        boolean mExpanding;
        boolean mIsExpanded;

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
        mExpanding = cs.mExpanding;
        mIsExpanded = cs.mIsExpanded;

        toggleExpand(mIsExpanded);
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

        ViewGroup.LayoutParams params = mIvBg.getLayoutParams();
        params.height = params.height + mStatusBarHeight;
        mIvBg.setLayoutParams(params);
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

        mFacPlayBtn.hide();

        addView(view);
    }

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

        if (mPlayer.getPlayWhenReady()) {
            mIvPlayBtn.setImageResource(R.drawable.ic_pause);
            mFacPlayBtn.setImageResource(R.drawable.ic_pause);
        } else {
            mIvPlayBtn.setImageResource(R.drawable.ic_play_arrow);
            mFacPlayBtn.setImageResource(R.drawable.ic_play_arrow);
        }

        if (music.equals(mCurrentMusic)) {
            return;
        }
        mCurrentMusic = music;

        try {
            Glide.with(getContext())
                    .load(music.album)
                    .placeholder(R.color.colorCement)
                    .crossFade()
                    .into(mIvAlbum);
        } catch (Throwable e) {
            Logger.w("Glide错误,可忽略");
        }
        mTvTitle.setText(TextUtils.isEmpty(music.title) ? "未知" : music.title);
        mTvArtist.setText(TextUtils.isEmpty(music.artist) ? "未知" : music.artist);
        mTvProgress.setText("00:00");
        mTvDuration.setText(DateFormat.format("mm:ss", music.duration).toString());

        mIsOnline = mCurrentMusic.path.startsWith("http");
        updateProgress();
    }

    private void updateProgress() {
        long duration = mPlayer == null ? 0 : mPlayer.getDuration();
        long position = mPlayer == null ? 0 : mPlayer.getCurrentPosition();

        if (duration >= 0) {
            if (mTvDuration != null) {
                mTvDuration.setText(FormatUtils.stringForTime(duration));
            }
            mPbProgress.setMax((int) (duration / 1000));
        }

        if (mTvProgress != null && !mDragging) {
            mTvProgress.setText(FormatUtils.stringForTime(position));
        }

        if (!mDragging) {
            mPbProgress.setProgress((int) (position / 1000));
        }

        /* 如果是网络资源(播放地址以http开头)则显示缓存进度 */
        if (mIsOnline) {
            long bufferedPosition = mPlayer == null ? 0 : mPlayer.getBufferedPosition();
            mPbProgress.setSecondaryProgress((int) (bufferedPosition / 1000));
        }

        removeCallbacks(updateProgressAction);

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
            postDelayed(updateProgressAction, delayMs);
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

    @OnClick({R.id.iv_previous_btn, R.id.iv_play_btn, R.id.iv_next_btn, R.id.fac_play_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_previous_btn:
                if (mControlListener != null) {
                    mControlListener.previous();
                }
                break;
            case R.id.iv_play_btn:
            case R.id.fac_play_btn:
                if (mControlListener != null) {
                    /* 判断是播放还是暂停, 回传 */
                    if (mPlayer.getPlayWhenReady()) {
                        mControlListener.pause();
                    } else {
                        mControlListener.play();
                    }
                }
                break;
            case R.id.iv_next_btn:
                if (mControlListener != null) {
                    mControlListener.next();
                }
                break;
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(updateProgressAction);
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

    //==============================================================================================
    //=========================================View联动相关=========================================
    //==============================================================================================

    //========================================NestScroll-start======================================

    /**
     * 滑动开始的调用startNestedScroll()，Parent 收到onStartNestedScroll()回调，
     * 决定是否需要配合 Child 一起进行处理滑动，
     * 如果需要配合,还会回调{@link ControlView#onNestedScrollAccepted(View, View, int)}。
     */
    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return true;
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
        if (mExpanding && !mIsExpanded && offsetRadius > 0.5f) {
            mIsExpanded = true;
        } else if (!mExpanding && mIsExpanded && offsetRadius < 0.5) {
            mIsExpanded = false;
        } else {
            return;
        }

        final int value = mIsExpanded ? 0 : 1;
        if (mIsExpanded) {
            getScaleAlphaAnim(mIvAlbum, value)
                    .setListener(new ViewPropertyAnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(View view) {
                            toggleAlbumBg(mIsExpanded, offsetRadius);
                            toggleTextOffset(mIsExpanded);
                            toggleControlBtn(mIsExpanded);
                            if (mIsExpanded) {
                                mFacPlayBtn.show();
                            } else {
                                mFacPlayBtn.hide();
                            }
                        }
                    })
                    .start();
        } else {
            toggleAlbumBg(false, offsetRadius);
            toggleTextOffset(mIsExpanded);
            toggleControlBtn(mIsExpanded);
            if (mIsExpanded) {
                mFacPlayBtn.show();
            } else {
                mFacPlayBtn.hide();
            }
            getScaleAlphaAnim(mIvAlbum, value)
                    .setListener(null)
                    .start();
        }
    }

    private void toggleTextOffset(boolean expand) {
        controlTextOffsetAnim(mTvTitle, expand ? -mMinLeftMargin : 0);
        controlTextOffsetAnim(mTvArtist, expand ? -mMinLeftMargin : 0);
        controlTextOffsetAnim(mTvProgress, expand ? -mMinLeftMargin : 0);
        controlTextOffsetAnim(mTvDuration, expand ? mDurationRightMargin : 0);
    }

    private void toggleControlBtn(boolean expand) {
        if (expand) {
            controlBtnAnim(mIvNextBtn, 0);
            controlBtnAnim(mIvPlayBtn, 0);
            controlBtnAnim(mIvPreviousBtn, 0);
        } else {
            controlBtnAnim(mIvNextBtn, 1);
            controlBtnAnim(mIvPlayBtn, 1);
            controlBtnAnim(mIvPreviousBtn, 1);
        }
    }

    private void toggleAlbumBg(boolean expand, float offsetRadius) {
        if (expand) {   // 展开时背景为专辑模糊图片
            final Drawable targetDrawable = getResources().getDrawable(R.drawable.bg_control);
//            /* 圆角扩散模式,但是效果不好,太过于吸引用户眼球 */
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//                Animator animator = ViewAnimationUtils.createCircularReveal(
//                        mFlRoot,
//                        mIvAlbum.getWidth() / 2,
//                        mFlRoot.getHeight() / 2,
//                        0,
//                        mIvBg.getWidth());
//                animator.setInterpolator(new AccelerateDecelerateInterpolator());
//                animator.setDuration(320);
//                animator.addListener(new AnimatorListenerAdapter() {
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                        super.onAnimationEnd(animation);
//                        mIvBg.setImageDrawable(targetDrawable);
//                    }
//                });
//                animator.start();
//                return;
//            }
            Drawable source = mIvBg.getBackground();
            if (source == null) {
                source = new ColorDrawable(getResources().getColor(R.color.colorPrimary));
            }
            mIvBg.setImageDrawable(getTransitionDrawable(source, targetDrawable, 240));
        } else {    // 收起时背景为专辑色调纯色
            Drawable source = mIvBg.getBackground();
            if (source == null) {
                source = getResources().getDrawable(R.drawable.bg_control);
            }
            Drawable targetDrawable = new ColorDrawable(getResources().getColor(R.color.colorPrimary));
            mIvBg.setImageDrawable(getTransitionDrawable(source, targetDrawable, 240));
        }
    }

    //==============================================================================================
    private float getOffsetRadius(int height) {
        float offsetY = height - mHeadMinHeight;
        return offsetY / mMaxOffsetY;
    }

    private void controlBtnAnim(View v, float value) {
        getScaleAlphaAnim(v, value).start();
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
