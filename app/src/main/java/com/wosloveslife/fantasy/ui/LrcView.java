package com.wosloveslife.fantasy.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.widget.ScrollerCompat;
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


/**
 * Created by zhangh on 2017/2/2.
 */

public class LrcView extends View {
    BLyric mBLyric;
    private Paint mPaint;
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
    private int mChosenLine;
    private Paint mChosenPaint;

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
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(getResources().getColor(R.color.gray_light));
        mChosenPaint = new Paint(mPaint);
        mChosenPaint.setColor(getResources().getColor(R.color.white));
        setTextSize(16, 8);

        mScroller = ScrollerCompat.create(getContext(), new DecelerateInterpolator());
        mVelocityTracker = VelocityTracker.obtain();
        ViewConfiguration viewConfiguration = ViewConfiguration.get(getContext());
        mTouchSlop = viewConfiguration.getScaledTouchSlop();
        mMinimumFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        mMaximumFlingVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mVelocityTracker.recycle();
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

        if (mBLyric == null || mBLyric.mLrc == null) return;

        int v = mTextSpace / 2 + mHeight / 2;
        int lineCount = 0;
        for (BLyric.LyricLine lyricLine : mBLyric.mLrc) {
            float measureText = mPaint.measureText(lyricLine.content);
            if (lineCount == mChosenLine) {
                canvas.drawText(lyricLine.content, (mWidth - measureText) / 2, v, mChosenPaint);
            } else {
                canvas.drawText(lyricLine.content, (mWidth - measureText) / 2, v, mPaint);
            }
            v += mTextSpace;
            ++lineCount;
        }
    }

    private void postPlay() {

    }

    float mDownY;
    float mLastX;
    float mLastY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
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
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaY = mLastY - y;
                scrollYBy((int) deltaY);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                addVelocityTracker = true;
                mVelocityTracker.addMovement(vtev);
                mVelocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                float yVelocity = VelocityTrackerCompat.getYVelocity(mVelocityTracker, mScrollPointerId);
                if (Math.abs(yVelocity) > mMinimumFlingVelocity) {
                    fling((int) yVelocity);
                }
                mVelocityTracker.clear();
                break;
        }

        if (!addVelocityTracker) {
            mVelocityTracker.addMovement(vtev);
        }

        mLastX = x;
        mLastY = y;
        return consume;
    }

    private void fling(int yVelocity) {
        mScroller.fling(
                getScrollX(), getScrollY(),
                0, -yVelocity,
                Integer.MIN_VALUE, Integer.MAX_VALUE,
                0, mMaxScrollRange);
        invalidate();
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

        int newChosen = Math.round(targetY * 1f / mTextSpace);
        if (mChosenLine != newChosen) {
            mChosenLine = newChosen;
            invalidate();
        }
    }

    //==============================================================================================
    public void setLrc(BLyric lrc) {
        mBLyric = lrc;
        if (mBLyric != null && mBLyric.mLrc != null) {
            mMaxScrollRange = (mBLyric.mLrc.size() - 1) * mTextSpace;
        }
        mChosenLine = 0;

        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        scrollTo(0, 0);
        invalidate();

        postPlay();
    }

    public void setTextSize(float size, float span) {
        mTextSize = (int) Dp2Px.toPX(getContext(), size);
        mTextSpace = (int) (mTextSize + Dp2Px.toPX(getContext(), span));

        mPaint.setTextSize(mTextSize);
        mChosenPaint.setTextSize(mTextSize);
    }
}
