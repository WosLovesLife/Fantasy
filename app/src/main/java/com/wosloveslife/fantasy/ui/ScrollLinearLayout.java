package com.wosloveslife.fantasy.ui;

import android.content.Context;
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
import android.widget.LinearLayout;

/**
 * Created by zhangh on 2017/2/5.
 */

public class ScrollLinearLayout extends LinearLayout {

    private int mTouchSlop;
    private int mMinimumFlingVelocity;
    private int mMaximumFlingVelocity;
    private ScrollerCompat mScroller;
    private VelocityTracker mVelocityTracker;
    private int mScrollPointerId;

    public ScrollLinearLayout(Context context) {
        this(context, null);
    }

    public ScrollLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ScrollLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setOrientation(VERTICAL);

        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mMinimumFlingVelocity = ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();
        mMaximumFlingVelocity = ViewConfiguration.get(getContext()).getScaledMaximumFlingVelocity();

        mScroller = ScrollerCompat.create(getContext());
        mVelocityTracker = VelocityTracker.obtain();
    }

    int mMaxScrollRange;
    int mTotalHeight;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (childAt.getVisibility() != VISIBLE) continue;
            mTotalHeight += childAt.getMeasuredHeight();
        }
        mMaxScrollRange = mTotalHeight - h;
        if (mMaxScrollRange < 0) mMaxScrollRange = 0;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
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
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (disallowIntercept) {
            recycleVelocityTracker();
        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    float mDownX;
    float mDownY;
    float mLastX;
    float mLastY;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercept = false;
        float x = ev.getX();
        float y = ev.getY();

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = x;
                mDownY = y;
                intercept = true;
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = mLastX - x;
                float deltaY = mLastY - y;
                if (Math.abs(mDownY - y) > mTouchSlop && Math.abs(deltaY) > Math.abs(deltaX)) {
                    intercept = true;
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
        }

        mLastX = x;
        mLastY = y;
        return intercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean consume = false;
        boolean addVelocityTracker = false;

        MotionEvent vtev = MotionEvent.obtain(ev);

        float x = ev.getX();
        float y = ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = x;
                mDownY = y;
                consume = true;
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                mScrollPointerId = MotionEventCompat.getPointerId(ev, 0);
                initOrResetVelocityTracker();
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = mLastX - x;
                float deltaY = mLastY - y;
                if (Math.abs(mDownY - y) > mTouchSlop && Math.abs(deltaY) > Math.abs(deltaX)) {
                    consume = true;
                    getParent().requestDisallowInterceptTouchEvent(true);
                    scrollBy((int) deltaY);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                addVelocityTracker = true;
                mVelocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                float yVelocity = VelocityTrackerCompat.getYVelocity(mVelocityTracker, mScrollPointerId);
                fling((int) yVelocity);
                recycleVelocityTracker();
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
        mScroller.fling(0, getScrollY(),
                0, -yVelocity,
                0, 0,
                0, mMaxScrollRange);
        invalidate();
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrY());
        }
    }

    private void scrollBy(int deltaY) {
        int scrollY = getScrollY();
        if ((deltaY < 0 && scrollY <= 0) || (deltaY > 0 && scrollY >= mMaxScrollRange)) return;
        scrollTo(deltaY + scrollY);
    }

    private void scrollTo(int targetY) {
        if (targetY > mMaxScrollRange) {
            targetY = mMaxScrollRange;
        } else if (targetY < 0) {
            targetY = 0;
        }
        scrollTo(0, targetY);
    }
}
