package com.wosloveslife.fantasy.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.widget.ScrollerCompat;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;

import com.wosloveslife.fantasy.R;
import com.wosloveslife.fantasy.bean.BLyric;
import com.yesing.blibrary_wos.utils.assist.WLogger;
import com.yesing.blibrary_wos.utils.screenAdaptation.Dp2Px;

import java.util.List;


/**
 * Created by zhangh on 2017/2/2.
 */

public class LrcView extends View {
    private TextPaint mPaint;
    private TextPaint mPlayingPaint;
    private TextPaint mChosenPaint;

    private int mTextSize;
    private int mTextSpace;
    private int mWidth;
    private int mHeight;
    private int mMaxScrollRange;

    private ScrollerCompat mScroller;
    private VelocityTracker mVelocityTracker;
    private int mTouchSlop;
    private int mMinimumFlingVelocity;
    private int mMaximumFlingVelocity;
    private int mScrollPointerId;
    private int mChosenLine = -1;
    private int mCurrentLine;
    boolean mTouching;
    boolean mSeeking;
    private int mIgnoreEdge;

    BLyric mBLyric;
    private List<BLyric.LyricLine> mLyricLines;

    public LrcView(Context context) {
        this(context, null);
    }

    public LrcView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LrcView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public LrcView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(Dp2Px.toPX(getContext(), 100), MeasureSpec.EXACTLY);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void init() {
        mPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(getResources().getColor(R.color.gray_text));
        mPlayingPaint = new TextPaint(mPaint);
        mPlayingPaint.setColor(getResources().getColor(R.color.white));
        mChosenPaint = new TextPaint(mPaint);
        mChosenPaint.setColor(getResources().getColor(R.color.gray_light));
        setTextSize(16, 8);

        mIgnoreEdge = Dp2Px.toPX(getContext(), 16);

        mScroller = ScrollerCompat.create(getContext(), new DecelerateInterpolator());
        ViewConfiguration viewConfiguration = ViewConfiguration.get(getContext());
        mTouchSlop = viewConfiguration.getScaledTouchSlop();
        mMinimumFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        mMaximumFlingVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mTouching = false;
        mHandler.removeCallbacksAndMessages(null);
        notifySeekingFinish();
        recycleVelocityTracker();
    }

    private void initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int y = mTextSpace / 2 + mHeight / 2;
        int lineCount = 0;

        canvas.save();
        if (mBLyric == null || mBLyric.mLrc == null) {
            float x = (mWidth - mPaint.measureText("暂无歌词,请期待后续优化")) / 2;
            canvas.drawText("暂无歌词,请期待后续优化", x, y, mPaint);
            canvas.restore();
            return;
        }

        if (!isSupportAutoScroll()) {
            float x = (mWidth - mPaint.measureText("当前歌词不支持自动滚动")) / 2;
            canvas.drawText("当前歌词不支持自动滚动", x, y, mPaint);
            y += mTextSpace;
            ++lineCount;
        }

        for (BLyric.LyricLine lyricLine : mBLyric.mLrc) {
            if (y > getScrollY() && y < getScrollY() + getHeight() + mTextSpace) {
                float x = (mWidth - mPaint.measureText(lyricLine.content)) / 2;
                String content = TextUtils.isEmpty(lyricLine.content) ? "." : lyricLine.content;
                if (lineCount == mCurrentLine) {
                    canvas.drawText(content, x, y, mPlayingPaint);
                } else if (isSeeking() && lineCount == mChosenLine) {
                    canvas.drawText(content, x, y, mChosenPaint);
                } else {
                    canvas.drawText(content, x, y, mPaint);
                }
            }
            y += mTextSpace;
            ++lineCount;
        }
        canvas.restore();
    }

    private void syncLrc(long progress) {
        if (!isLrcEnable()) return;

        /* 记录当前歌词行的时间距离progress的差值,如果下一行的差值小于当前记录的值,则选择下一行为目标行
         * 如果下一行的时间节点大于progress,则终止循环.因为后面的时间值距离progress肯定越来越大 */
        long interval = Long.MAX_VALUE;
        int chosenIndex = 0;
        for (int i = 0; i < mLyricLines.size(); i++) {
            BLyric.LyricLine line = mLyricLines.get(i);
            long next = progress - line.time;
            if (Math.abs(next) < interval) {
                chosenIndex = i;
                interval = next;
            }
            if (line.time > progress) {
                break;
            }
        }

        if (chosenIndex == mCurrentLine) return;

        /* 如果在触摸中,就不改变当前的Scroll,只重绘视图播放行 */
        mCurrentLine = chosenIndex;
        if (!mTouching) {
            mScroller.startScroll(0, getScrollY(), 0, (chosenIndex * mTextSpace) - getScrollY(), 300);
        }
        invalidate();
    }

    public void setAutoSyncLrc(boolean enable, long offsetProgress) {
        if (!isSupportAutoScroll()) return;

        mHandler.removeMessages(0);
        if (offsetProgress > 0) {
            syncLrc(offsetProgress);
        }

        if (!enable || mCurrentLine + 1 >= mLyricLines.size()) return;

        long cTime = 0;
        if (mCurrentLine > 0) {
            cTime = mLyricLines.get(mCurrentLine).time;
        }
        long nTime = mLyricLines.get(mCurrentLine + 1).time;

        long delay = nTime - cTime;
        if (offsetProgress > cTime) {
            delay = nTime - offsetProgress;
        }

        mHandler.sendMessageDelayed(mHandler.obtainMessage(0, mCurrentLine + 1), delay);
    }

    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    /* 如果在触摸中,就不改变当前的Scroll,只重绘视图播放行 */
                    mCurrentLine = (Integer) msg.obj;
                    if (!isSeeking()) {
                        smoothScroll(mCurrentLine * mTextSpace);
                    } else {
                        invalidate();
                    }

                    setAutoSyncLrc(true, 0);
                    break;
                case 1:
                    smoothScroll(mCurrentLine * mTextSpace);
                    notifySeekingFinish();
                    break;
            }
        }
    };

    private void smoothScroll(int targetY) {
        int offset = targetY - getScrollY();
        if (offset != 0) {
            mChosenLine = 0;
            mScroller.startScroll(0, getScrollY(), 0, offset, 240);
            invalidate();
        }
    }

    float mDownY;
    float mLastX;
    float mLastY;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                mTouching = false;
                WLogger.d("dispatchTouchEvent : mTouching = false ");
                break;
            default:
                mTouching = true;
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isLrcEnable()) return false;

        boolean addVelocityTracker = false;
        boolean consume = true;
        float x = event.getX();
        float y = event.getY();

        final MotionEvent vtev = MotionEvent.obtain(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                WLogger.d("onTouchEvent : ACTION_DOWN ");
                mDownY = y;
                mScrollPointerId = MotionEventCompat.getPointerId(event, 0);
                if (y > mHeight - mIgnoreEdge) {
                    consume = false;
                } else {
                    if (!mScroller.isFinished()) {
                        mScroller.abortAnimation();
                    }
                    initOrResetVelocityTracker();
                    mHandler.removeMessages(1);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaY = mLastY - y;
                scrollYBy((int) deltaY);
                notifySeekingProgress();
                break;
            case MotionEvent.ACTION_UP:
                WLogger.d("onTouchEvent : ACTION_UP ");
                addVelocityTracker = true;
                mVelocityTracker.addMovement(vtev);
                mVelocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                float yVelocity = VelocityTrackerCompat.getYVelocity(mVelocityTracker, mScrollPointerId);
                if (Math.abs(yVelocity) > mMinimumFlingVelocity) {
                    fling((int) yVelocity);
                }
            case MotionEvent.ACTION_CANCEL:
                recycleVelocityTracker();
                scrollDef();
                break;
        }

        if (!addVelocityTracker) {
            initVelocityTrackerIfNotExists();
            mVelocityTracker.addMovement(vtev);
        }
        vtev.recycle();

        mLastX = x;
        mLastY = y;
        return consume;
    }

    private void fling(int yVelocity) {
        mScroller.fling(
                0, getScrollY(),
                0, -yVelocity / 2,
                Integer.MIN_VALUE, Integer.MAX_VALUE,
                0, mMaxScrollRange);
        invalidate();
    }

    private void scrollDef() {
        if (!isSupportAutoScroll()) return;
        mHandler.removeMessages(1);
        mHandler.sendEmptyMessageDelayed(1, 2000);
        WLogger.d("scrollDef :  ");
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            scrollYTo(mScroller.getCurrY());
        }
    }

    public void scrollYBy(int y) {
        scrollYTo(getScrollY() + y);
    }

    public void scrollYTo(int targetY) {
        int currentY = getScrollY();
        if (targetY <= 0 && currentY <= 0) return;
        if (targetY >= mMaxScrollRange && currentY >= mMaxScrollRange) return;

        if (targetY < 0) {
            targetY = 0;
        } else if (targetY > mMaxScrollRange) {
            targetY = mMaxScrollRange;
        }
        super.scrollTo(0, targetY);

        if (!isSupportAutoScroll()) return;

        int newChosen = Math.round(targetY * 1f / mTextSpace);
        if (!isSeeking()) {
            mChosenLine = 0;
            invalidate();
        } else if (mChosenLine != newChosen) {
            mChosenLine = newChosen;
            invalidate();

            notifySeekingProgress();
        }
    }

    private boolean isSeeking() {
        return mTouching || mSeeking;
    }

    private boolean isLrcEnable() {
        return mLyricLines != null && mLyricLines.size() > 0;
    }

    private boolean isSupportAutoScroll() {
        return isLrcEnable() && mLyricLines.get(0).time > 0;
    }

    private BLyric.LyricLine getLrcLine(int index) {
        if (!isLrcEnable() || index < 0 || index >= mLyricLines.size()) return null;
        return mLyricLines.get(index);
    }

    private long getLrcTimeLien(int index) {
        BLyric.LyricLine line = getLrcLine(index);
        return line != null ? line.time : 0;
    }

    private void notifySeekingProgress() {
        mSeeking = true;
        if (mOnSeekLrcProgressListener != null && mLyricLines.size() > mChosenLine) {
            long lrcTimeLien = getLrcTimeLien(mChosenLine);
            if (lrcTimeLien > 0) {
                mOnSeekLrcProgressListener.onSeekingProgress(lrcTimeLien);
            }
        }
    }

    private void notifySeekingFinish() {
        mSeeking = false;
        if (mOnSeekLrcProgressListener != null && isLrcEnable()) {
            long lrcTimeLien = getLrcTimeLien(mChosenLine);
            if (lrcTimeLien > 0) {
                mOnSeekLrcProgressListener.onSeekFinish(lrcTimeLien);
            }
        }
    }

    //==============================================================================================
    public void setLrc(BLyric lrc) {
        mBLyric = lrc;
        if (mBLyric != null && mBLyric.mLrc != null) {
            mLyricLines = mBLyric.mLrc;
            mMaxScrollRange = (mLyricLines.size() - 1) * mTextSpace;
        } else {
            mLyricLines = null;
            mMaxScrollRange = 0;
        }
        mChosenLine = 0;
        mCurrentLine = 0;

        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        scrollTo(0, 0);
        syncLrc(0);
        invalidate();
    }

    public void setTextSize(float size, float span) {
        mTextSize = (int) Dp2Px.toPX(getContext(), size);
        mTextSpace = (int) (mTextSize + Dp2Px.toPX(getContext(), span));

        mPaint.setTextSize(mTextSize);
        mPlayingPaint.setTextSize(mTextSize);
        mChosenPaint.setTextSize(mTextSize);
    }

    OnSeekLrcProgressListener mOnSeekLrcProgressListener;

    public void setOnSeekLrcProgressListener(OnSeekLrcProgressListener listener) {
        mOnSeekLrcProgressListener = listener;
    }

    interface OnSeekLrcProgressListener {
        void onSeekingProgress(long progress);

        void onSeekFinish(long progress);
    }
}
