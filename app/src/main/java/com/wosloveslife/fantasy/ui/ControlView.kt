package com.wosloveslife.fantasy.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.support.annotation.UiThread
import android.support.annotation.WorkerThread
import android.support.graphics.drawable.AnimatedVectorDrawableCompat
import android.support.v4.view.MotionEventCompat
import android.support.v4.view.VelocityTrackerCompat
import android.support.v4.view.ViewCompat
import android.support.v4.view.animation.FastOutLinearInInterpolator
import android.support.v4.view.animation.LinearOutSlowInInterpolator
import android.support.v7.graphics.Palette
import android.support.v7.widget.CardView
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.text.format.DateFormat
import android.util.AttributeSet
import android.view.*
import android.view.View.OnTouchListener
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import butterknife.OnClick
import com.makeramen.roundedimageview.RoundedImageView
import com.orhanobut.logger.Logger
import com.wosloveslife.dao.Audio
import com.wosloveslife.fantasy.R
import com.wosloveslife.fantasy.R.id.iv_favor
import com.wosloveslife.fantasy.adapter.SubscriberAdapter
import com.wosloveslife.fantasy.lrc.BLyric
import com.wosloveslife.fantasy.lrc.LrcView
import com.wosloveslife.fantasy.manager.MusicManager
import com.wosloveslife.fantasy.manager.SettingConfig
import com.wosloveslife.fantasy.utils.FormatUtils
import com.wosloveslife.fantasy.utils.NetWorkUtil
import com.wosloveslife.fantasy.v2.player.Controller
import com.wosloveslife.fantasy.v2.player.PlayEvent
import com.wosloveslife.player.PlayerException
import com.yesing.blibrary_wos.utils.assist.Toaster
import com.yesing.blibrary_wos.utils.assist.WLogger
import com.yesing.blibrary_wos.utils.screenAdaptation.Dp2Px
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import stackblur_java.StackBlurManager

/**
 * Created by zhangh on 2017/1/15.
 */

class ControlView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {

    var mFlRoot: FrameLayout? = null
    var mIvBg: ImageView? = null
    /**
     * 这个控件是专门用来覆盖在mIvBg上面用来在切换头部背景时的CircularReveal动画使用,
     * 这样可以在原有背景色基础上有一个新的背景色展开的效果
     * 应该有更好的实现方式,暂时用这个
     */
    var mCardView: CardView? = null
    var mIvAlbum: RoundedImageView? = null
    var mTvTitle: TextView? = null
    var mTvArtist: TextView? = null
    var mTvProgress: TextView? = null
    var mTvDuration: TextView? = null
    var mIvPlayBtn: ImageView? = null
    var mIvFavor: ImageView? = null
    var mIvPlayOrder: ImageView? = null
    var mPbProgress: ProgressBar? = null
    var mTvSeekValue: TextView? = null
    var toolbar: Toolbar? = null
    var mLrcView: LrcView? = null

    private var mCurrentMusic: Audio? = null

    //============
    private var mVelocityTracker: VelocityTracker
    internal var mTouchSlop: Int = 0
    private var mMinimumFlingVelocity: Int = 0
    private var mMaximumFlingVelocity: Int = 0
    private var mScrollPointerId: Int = 0

    //=============
    private var mController: Controller

    //=============Var
    /** 如果手正在拖动SeekBar,就不能让Progress自动跳转  */
    private var mDragging: Boolean = false
    private var m16dp: Int = 0
    private var m48dp: Int = 0

    //======
    /** 封面的最大弧度(为圆形时)  */
    private var mAlbumMaxRadius: Int = 0
    /** 播放进度文字等的最小左边距,同时也是最大向左偏移量  */
    private var mMinLeftMargin: Int = 0
    /** 歌曲名/艺术家的最小Margin值,最大Margin值为最小Margin+mMinLeftMargin  */
    private var mTitleLeftMargin: Int = 0
    private var mAlbumSize: Int = 0
    /** 播放总时长文字的最大向右偏移量  */
    private var mDurationRightMargin: Int = 0
    private var mStatusBarHeight: Int = 0

    //============
    private var mPlayDrawable: Drawable? = null
    private var mPauseDrawable: Drawable? = null

    private var mDefAlbum: Drawable? = null
    private var mDefBlurredAlbum: Drawable? = null
    private var mDefColorMutedBg: Drawable? = null
    private var mDefColorTitle: Drawable? = null
    private var mDefColorBody: Drawable? = null

    private var mAlbum: Drawable? = null
    /** 在当前封面的基础上进行了模糊处理,作为展开时的背景图片  */
    private var mBlurredAlbum: Drawable? = null
    private var mColorMutedBg: Drawable? = null
    private var mColorTitle: Drawable? = null
    private var mColorBody: Drawable? = null

    private var mHeadLastY: Float = 0.toFloat()
    private var mHeadDownY: Float = 0.toFloat()
    private var mHeadClick: Boolean = false

    private var mLrcSeeking: Boolean = false
    private var mLrcProgress: Long = 0

    private val mHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            updateProgress()
        }
    }

    private val isPlaying: Boolean
        get() = mController.getState().isPlaying()

    /**
     * 这个值是为了防止从别的形态到第二阶段的滑动过程中拦截了触摸事件而开启了Seek
     * 也就是说,只有已经是展开了的形态下再次触摸并横向滑动,才可能拦截事件
     */
    private var mIntercept: Boolean = false

    private var mDownX: Float = 0.toFloat()
    private var mLastX: Float = 0.toFloat()
    private var mLastY: Float = 0.toFloat()
    private var mSeeking: Boolean = false

    private val headDrawable: Drawable?
        get() = if (mBlurredAlbum == null) mDefBlurredAlbum else mBlurredAlbum

    private val headColorDrawable: Drawable?
        get() = if (mColorMutedBg == null) mDefColorMutedBg else mColorMutedBg

    init {
        m16dp = Dp2Px.toPX(context, 16)
        m48dp = Dp2Px.toPX(context, 48)

        /* 圆形的角度等于边长的一半,因为布局中写死了48dp,因此这里取24dp,如果有需要,应该在onSizeChanged()方法中监听子控件的边长除2 */
        mAlbumMaxRadius = Dp2Px.toPX(context, 24)
        mMinLeftMargin = Dp2Px.toPX(context, 56)
        mTitleLeftMargin = Dp2Px.toPX(context, 14)
        mAlbumSize = m48dp
        mDurationRightMargin = Dp2Px.toPX(context, 22)
        mStatusBarHeight = resources.getDimension(R.dimen.statusBar_height).toInt()
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
        mMinimumFlingVelocity = ViewConfiguration.get(context).scaledMinimumFlingVelocity
        mMaximumFlingVelocity = ViewConfiguration.get(context).scaledMaximumFlingVelocity

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_portrait_chicken_174)

        mPlayDrawable = resources.getDrawable(R.drawable.ic_play_arrow)
        mPauseDrawable = resources.getDrawable(R.drawable.ic_pause)

        mDefAlbum = BitmapDrawable(bitmap)
        mDefBlurredAlbum = resources.getDrawable(R.drawable.bg_blur)
        mDefColorTitle = ColorDrawable(resources.getColor(R.color.white))
        mDefColorMutedBg = ColorDrawable(resources.getColor(R.color.colorPrimary))
        mDefColorBody = ColorDrawable(resources.getColor(R.color.colorAccent))

        mVelocityTracker = VelocityTracker.obtain()

        mController = Controller.sInstance

        initView();
    }

    private fun initView() {
        val view = LayoutInflater.from(context).inflate(R.layout.view_control, this, false)
        addView(view, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM))

        mFlRoot = findViewById(R.id.fl_root) as FrameLayout?
        mIvBg = findViewById(R.id.iv_bg) as ImageView?
        mCardView = findViewById(R.id.card_view) as CardView?
        mIvAlbum = findViewById(R.id.iv_album) as RoundedImageView?
        mTvTitle = findViewById(R.id.tv_title) as TextView?
        mTvArtist = findViewById(R.id.tv_artist) as TextView?
        mTvProgress = findViewById(R.id.tv_progress) as TextView?
        mTvDuration = findViewById(R.id.tv_duration) as TextView?
        mIvPlayBtn = findViewById(R.id.iv_play_btn) as ImageView?
        mIvFavor = findViewById(R.id.iv_favor) as ImageView?
        mIvPlayOrder = findViewById(R.id.iv_playOrder) as ImageView?
        mPbProgress = findViewById(R.id.pb_progress) as ProgressBar?
        mTvSeekValue = findViewById(R.id.tv_seek_value) as TextView?
        toolbar = findViewById(R.id.toolbar) as Toolbar?
        mLrcView = findViewById(R.id.lrc_view) as LrcView?

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mPbProgress?.progressBackgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.transparent))
        }

        val listener = OnTouchListener { v, event ->
            var consume = false
            val y = event.y
            mVelocityTracker.addMovement(event)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    mScrollPointerId = MotionEventCompat.getPointerId(event, 0)
                    mHeadDownY = y
                    mHeadClick = true
                }
                MotionEvent.ACTION_MOVE -> if (Math.abs(mHeadDownY - y) > mTouchSlop) {
                    val deltaY = Math.round(mHeadLastY - y)
                    consume = true
                    mHeadClick = false
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (mHeadClick) {
                        performClick()
                    } else {
                        mVelocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity.toFloat())
                        val velocityY = VelocityTrackerCompat.getYVelocity(mVelocityTracker, mScrollPointerId)
                        TODO("可以通过速度进行一些动画效果")
                        mVelocityTracker.clear()
                        consume = true
                    }
                }
            }
            mHeadLastY = y
            consume
        }

        mFlRoot?.setOnTouchListener(listener)

        mLrcView?.setOnSeekLrcProgressListener(object : LrcView.OnSeekLrcProgressListener {
            override fun onSeekingProgress(progress: Long) {
                mLrcSeeking = true
                mLrcProgress = progress
            }

            override fun onSeekFinish(progress: Long) {
                mLrcSeeking = false
            }
        })

        syncPlayOrderVisual()

        mController.getState().addListener(object : PlayEvent {
            override fun onPlay(audio: Audio) {
                updateProgress()
            }

            override fun onPause() {
                updateProgress()
            }

            override fun onSeekTo(progress: Long) {
                updateProgress()
            }

            override fun onStop() {
                updateProgress()
            }

            override fun onBuffering(bufferProgress: Long) {

            }

            override fun onError(e: PlayerException) {
                updateProgress()
            }
        })
    }

    //==========================================状态同步-start======================================

    /**
     * 当歌曲切换时通过该功能同步该控件的状态
     *
     * @param music
     */
    @UiThread
    fun syncPlayView(music: Audio?) {
        if (music == null) {
            // TODO: 2017/9/3 如果Music == null 则置为默认样式
            return
        }
        if (isPlaying) {
            mIvPlayBtn!!.setImageDrawable(mPauseDrawable)
        } else {
            mIvPlayBtn!!.setImageDrawable(mPlayDrawable)
        }

        toggleLrcLoop()

        // TODO: 2017/11/19 加入收藏
        //        if (MusicManager.getInstance().isFavored(mCurrentMusic)) {
        //            mIvFavor.setImageResource(R.drawable.ic_favored_white);
        //        } else {
        //            mIvFavor.setImageResource(R.drawable.ic_favor_white);
        //        }

        if (music == mCurrentMusic) return
        mCurrentMusic = music

        val currentAlbum = if (mCurrentMusic != null) mCurrentMusic!!.album else null

        mTvTitle!!.text = if (TextUtils.isEmpty(music.title)) "未知" else music.title
        mTvArtist!!.text = if (TextUtils.isEmpty(music.artist)) "未知" else music.artist
        mTvProgress!!.text = "00:00"
        mTvDuration!!.text = DateFormat.format("mm:ss", music.duration).toString()

        if (mCurrentMusic == null) return

        if (!TextUtils.equals(currentAlbum, music.album)) {
            MusicManager.getInstance().getAlbum(mCurrentMusic!!.id, mAlbumSize)
                    .subscribeOn(Schedulers.io())
                    .subscribe(object : SubscriberAdapter<Bitmap>() {
                        override fun onNext(bitmap: Bitmap) {
                            super.onNext(bitmap)
                            updateAlbum(bitmap)
                        }

                        override fun onError(e: Throwable) {
                            super.onError(e)
                            updateAlbum(null)
                        }
                    })
        }

        updateProgress()

        MusicManager.getInstance()
                .getLrc(mCurrentMusic!!.id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SubscriberAdapter<BLyric>() {
                    override fun onError(e: Throwable) {
                        super.onError(e)
                        updateLrc(null)
                        if (NetWorkUtil.isNetWorkAvailable(context)) {
                            Toaster.showShort("错误 " + e.message)
                        }
                    }

                    override fun onNext(bLyric: BLyric) {
                        super.onNext(bLyric)
                        updateLrc(bLyric)
                    }
                })
    }

    /**
     * 通过Pattern获得封面的色调作为头部控件收起时和Toolbar的背景色<br></br>
     * 对封面作模糊处理作为头部控件展开时的背景图<br></br>
     * 对封面信息做对比,如果是同样的封面(同专辑)就不作处理,避免不必要的开支和可能的延迟<br></br>
     *
     * @param bitmap 如果等于null 则恢复默认的色彩和背景
     */
    @WorkerThread
    private fun updateAlbum(bitmap: Bitmap?) {
        if (bitmap == null && mAlbum === mDefAlbum) return

        if (bitmap == null) {
            mAlbum = mDefAlbum
            mBlurredAlbum = mDefBlurredAlbum
            mColorMutedBg = mDefColorMutedBg
            mColorTitle = mDefColorTitle
            mColorBody = mDefColorBody
        } else {
            mAlbum = BitmapDrawable(bitmap)
            mBlurredAlbum = BitmapDrawable(StackBlurManager(bitmap).process(60))

            val mutedSwatch = Palette.from(bitmap).generate().mutedSwatch
            if (mutedSwatch != null) {
                mColorMutedBg = ColorDrawable(mutedSwatch.rgb)
                mColorTitle = ColorDrawable(mutedSwatch.titleTextColor)
                mColorBody = ColorDrawable(mutedSwatch.bodyTextColor)
            }
        }

        val millis = System.currentTimeMillis()
        Handler(Looper.getMainLooper()).post {
            mIvAlbum!!.setImageDrawable(mAlbum)
            if (ViewCompat.isAttachedToWindow(mIvBg) && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                mIvBg!!.setImageDrawable(mColorMutedBg)
                val animator = ViewAnimationUtils.createCircularReveal(
                        mIvBg,
                        mIvAlbum!!.width / 2 + mIvAlbum!!.left,
                        mIvBg!!.height / 2,
                        0f,
                        mIvBg!!.width.toFloat())
                animator.interpolator = AccelerateDecelerateInterpolator()
                animator.duration = 320
                animator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        mCardView!!.setCardBackgroundColor((mColorMutedBg as ColorDrawable).color)
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        super.onAnimationCancel(animation)
                        mCardView!!.setCardBackgroundColor((mColorBody as ColorDrawable).color)
                    }
                })
                animator.start()
            }
            Logger.d("封面设置完成 时间 = " + (System.currentTimeMillis() - millis))
        }
    }

    @UiThread
    private fun updateProgress() {
        val duration = mController.getState().getDuration()
        val position = mController.getState().getCurrentPosition()

        if (duration >= 0) {
            if (mTvDuration != null) {
                mTvDuration!!.text = FormatUtils.stringForTime(duration)
            }
            mPbProgress!!.max = (duration / 1000).toInt()
        }

        mTvProgress!!.text = FormatUtils.stringForTime(position)

        if (!mDragging) {
            mPbProgress!!.progress = (position / 1000).toInt()
        }

        /* 如果是网络资源(播放地址以http开头)则显示缓存进度 */
        if (mCurrentMusic?.isOnline == true) {
            val bufferedPosition = mController.getState().getBufferedPosition()
            mPbProgress!!.secondaryProgress = (bufferedPosition / 1000).toInt()
        }

        mHandler.removeCallbacksAndMessages(null)

        // Schedule an update if necessary.
        if (isPlaying) {
            mHandler.sendEmptyMessageDelayed(0, position % 1000)
        }
    }

    private fun updateLrc(bLyric: BLyric?) {
        mLrcView!!.setLrc(bLyric)
        toggleLrcLoop()
    }

    private fun toggleLrcLoop() {
        val progress = mController.getState().getCurrentPosition()
        WLogger.d("toggleLrcLoop : progress =  " + progress)
        if (isPlaying) {
            mLrcView!!.setAutoSyncLrc(true, if (progress < 0) 0 else progress)
        } else {
            mLrcView!!.setAutoSyncLrc(false, if (progress < 0) 0 else progress)
        }
    }

    //===========================================状态同步-end=======================================

    @OnClick(R.id.fl_root, R.id.toolbar, R.id.iv_play_btn, iv_favor, R.id.iv_playOrder)
    fun onClick(view: View) {
        when (view.id) {
            R.id.fl_root -> {
                if (mDragging) return
            }
            R.id.toolbar -> return
            R.id.iv_play_btn -> {
                if (isPlaying) {
                    mController.pause()
                } else {
                    mController.play(MusicManager.getInstance().musicConfig.mCurrentMusic!!)
                }
            }
            iv_favor -> if (mCurrentMusic == null) return
            R.id.iv_playOrder -> {
                when (SettingConfig.getPlayOrder()) {
                    SettingConfig.PlayOrder.SUCCESSIVE // 列表循环
                    -> SettingConfig.savePlayOrder(SettingConfig.PlayOrder.RANDOM)
                    SettingConfig.PlayOrder.ONE // 单曲循环
                    -> SettingConfig.savePlayOrder(SettingConfig.PlayOrder.SUCCESSIVE)
                    SettingConfig.PlayOrder.RANDOM // 随机播放
                    -> SettingConfig.savePlayOrder(SettingConfig.PlayOrder.ONE)
                }
                syncPlayOrderVisual()
            }
        }
        // 通过等待歌曲同步来改变收藏状态 // TODO: 2017/11/19
        //                if (MusicManager.getInstance().isFavored(mCurrentMusic)) {
        //                    MusicManager.getInstance().removeFavor(mCurrentMusic.getId());
        //                } else {
        //                    MusicManager.getInstance().addFavor(mCurrentMusic.getId()).toBlocking().first();
        //                }
    }

    private fun syncPlayOrderVisual() {
        when (SettingConfig.getPlayOrder()) {
            SettingConfig.PlayOrder.SUCCESSIVE // 列表循环
            -> {
                val vectorDrawableCompat = AnimatedVectorDrawableCompat.create(context, R.drawable.animated_vector_order_dismiss)
                mIvPlayOrder!!.setImageDrawable(vectorDrawableCompat)
                vectorDrawableCompat?.start()
            }
            SettingConfig.PlayOrder.ONE // 单曲循环
            -> {
                val vectorDrawableCompat1 = AnimatedVectorDrawableCompat.create(context, R.drawable.animated_vector_order_show)
                mIvPlayOrder!!.setImageDrawable(vectorDrawableCompat1)
                vectorDrawableCompat1?.start()
            }
            SettingConfig.PlayOrder.RANDOM // 随机播放
            -> mIvPlayOrder!!.setImageResource(R.drawable.ic_order_random)
        }
    }

    public override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mHandler.removeCallbacksAndMessages(null)
        mVelocityTracker.recycle()
    }

    //==============================================================================================
    //=========================================View联动相关=========================================
    //==============================================================================================

    //========================================触摸事件-start======================================

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> mDragging = false
            else -> mDragging = true
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        var intercept = false
        val x = ev.x
        val y = ev.y
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                mDownX = x
                WLogger.d("onInterceptTouchEvent :  ")
                if (!mIntercept || y < mFlRoot!!.bottom - m16dp || y > mFlRoot!!.bottom + m16dp) {
                    intercept = false
                    mIntercept = false
                } else if (Math.abs(mDownX - x) > mTouchSlop && Math.abs(mLastX - x) > Math.abs(mLastY - y)) {
                    intercept = true
                }
            }
            MotionEvent.ACTION_MOVE -> if (!mIntercept || y < mFlRoot!!.bottom - m16dp || y > mFlRoot!!.bottom + m16dp) {
                intercept = false
                mIntercept = false
            } else if (Math.abs(mDownX - x) > mTouchSlop && Math.abs(mLastX - x) > Math.abs(mLastY - y)) {
                intercept = true
            }
        }
        mLastX = x
        mLastY = y
        return intercept
    }

    private fun seekProgress(x: Float, y: Float, seek: Boolean) {
        if (!mSeeking) {
            mSeeking = true
        }

        val max = mPbProgress!!.max
        val ratio = x / width
        val progress = Math.round(ratio * max)
        mPbProgress!!.progress = progress

        if (mTvSeekValue!!.visibility != View.VISIBLE) {
            mTvSeekValue!!.scaleX = 0f
            mTvSeekValue!!.scaleY = 0f
            mTvSeekValue!!.visibility = View.VISIBLE
            mTvSeekValue!!.animate().cancel()
            mTvSeekValue!!.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .setStartDelay(100)
                    .setInterpolator(LinearOutSlowInInterpolator())
                    .setListener(null)
        }
        var tX = x - mTvSeekValue!!.width / 2
        if (tX < 0) tX = 0f
        if (tX > width - mTvSeekValue!!.width) tX = (width - mTvSeekValue!!.width).toFloat()
        mTvSeekValue!!.translationX = tX
        if (y > mFlRoot!!.bottom + m48dp) {
            mTvSeekValue!!.text = "取消"
        } else {
            mTvSeekValue!!.text = FormatUtils.stringForTime((progress * 1000).toLong())
        }

        if (seek) {
            mController.seekTo((progress * 1000).toLong())
            toggleLrcLoop()
            recoverProgress()
        }
    }

    private fun recoverProgress() {
        mSeeking = false

        if (mTvSeekValue!!.visibility == View.VISIBLE) {
            mTvSeekValue!!.visibility = View.VISIBLE
            mTvSeekValue!!.animate().cancel()
            mTvSeekValue!!.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .setDuration(200)
                    .setInterpolator(FastOutLinearInInterpolator())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationCancel(animation: Animator) {
                            super.onAnimationCancel(animation)
                            recoverFac()
                        }

                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            recoverFac()
                        }
                    })
        }

        val animator = ValueAnimator.ofInt(mPbProgress!!.progress, (mController.getState().getCurrentPosition() / 1000).toInt())
        animator.addUpdateListener { animation ->
            val progress = animation.animatedValue as Int
            mPbProgress!!.progress = progress
        }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationCancel(animation: Animator) {
                super.onAnimationCancel(animation)
                updateProgress()
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                updateProgress()
            }
        })
        animator.duration = 340
        animator.start()
    }

    private fun recoverFac() {
        mTvSeekValue!!.visibility = View.INVISIBLE
    }

    companion object {
        private val KEY_IS_EXPAND = "fantasy.ui.ControlView.KEY_IS_EXPAND"
        private val PROGRESS_MAX = 100f

        private fun getTransitionDrawable(source: Drawable, target: Drawable, duration: Int): TransitionDrawable {
            val transitionDrawable = TransitionDrawable(arrayOf<Drawable>(source, target))
            transitionDrawable.isCrossFadeEnabled = true
            transitionDrawable.startTransition(duration)
            return transitionDrawable
        }
    }
}
