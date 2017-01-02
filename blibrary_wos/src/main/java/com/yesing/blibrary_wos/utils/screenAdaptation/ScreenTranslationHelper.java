package com.yesing.blibrary_wos.utils.screenAdaptation;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YesingBeijing on 2016/12/8.
 */
public class ScreenTranslationHelper {
    Activity mActivity;

    View mRootView;

    /** 额外偏移量 */
    int mAddOffset;

    boolean mEnable;

    private View mDecorView;

    private View mContentView;

    public ScreenTranslationHelper(Activity activity) {
        mActivity = activity;

        mAddOffset = Dp2Px.toPX(mActivity, 12);

        mRootView = ((ViewGroup) mActivity.findViewById(android.R.id.content)).getChildAt(0);
        mDecorView = mActivity.getWindow().getDecorView();
        mDecorView.getViewTreeObserver().addOnGlobalFocusChangeListener(new ViewTreeObserver.OnGlobalFocusChangeListener() {
            @Override
            public void onGlobalFocusChanged(View oldFocus, View newFocus) {
                autoFit();
            }
        });
        mDecorView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                autoFit();
            }
        });
    }

    public void setRootView(View view) {
        mContentView = view;
    }

    private void autoFit() {
        if (!mEnable) return;

        Rect rect = new Rect();
        if (mContentView != null) {
            mContentView.getWindowVisibleDisplayFrame(rect);
        } else {
            mDecorView.getWindowVisibleDisplayFrame(rect);
        }
        int keyboardHeight = getContentViewHeight() - rect.bottom;

        if (keyboardHeight == 0) {
            if (mScreenSizeChangedListeners != null) {
                for (OnScreenSizeChangedListener onScreenSizeChangedListener : mScreenSizeChangedListeners) {
                    onScreenSizeChangedListener.onScreenChanged(false);
                }
            }

            if (mRootView.getTranslationY() != 0) {
                float start = mRootView.getTranslationY();
                int velocity = (int) (Math.abs(start) / 800 * 240);
                ObjectAnimator.ofFloat(mRootView, "translationY", mRootView.getTranslationY(), 0)
                        .setDuration(velocity)
                        .start();
            }
            return;
        }

        if (mScreenSizeChangedListeners != null) {
            for (OnScreenSizeChangedListener onScreenSizeChangedListener : mScreenSizeChangedListeners) {
                onScreenSizeChangedListener.onScreenChanged(true);
            }
        }

        /* 键盘弹出,寻找当前获得焦点的控件(通常是EditText),获取该控件处于屏幕上的y轴位置
         * 通过屏幕高度减去在屏幕上的位置得到该控件距离屏幕顶端的距离
         * 距离顶端的距离-键盘高度-附加高度(即控件应该高于键盘额外的偏移量) = 控件距离键盘顶端的位置
         * 如果这个值是一个负数,说明当前键盘覆盖了该控件, 则需要执行偏移操作,否则说明键盘没有挡住该控件,则结束
         * 获得当前根控件的偏移量,如果和旧的偏移量和新的一样,则说明不需要执行任何操作,则结束
         * 否则根据新偏移量和旧偏移量的差计算出TranslationY操作的持续时间, 最后执行动画 */
        int[] size = new int[2];
        View focus = mDecorView.findFocus();
        if (focus == null) return;
        focus.getLocationOnScreen(size);

        /* 屏幕顶部距离获取焦点的View的距离 */
        int location = getContentViewHeight() - size[1];
        /* 焦点位置减去距离底部的距离等于需要垂直移动的距离 */
        int target = location - keyboardHeight - rect.top - mAddOffset;
        if (target >= 0) return;

        float start = mRootView.getTranslationY();
        /* 最终要偏移的距离 */
        float offset = target - start;
        if (offset == 0) return;

        /* 根据偏移的距离算出动画的持续时间 */
        int duration = (int) (Math.abs(offset) / 800 * 240);

        ObjectAnimator.ofFloat(mRootView, "translationY", start, start + target)
                .setDuration(duration)
                .start();
    }

    //========================================对外开放的方法========================================

    /** 如果为true,则自动调节当前获取焦点的控件不被键盘遮挡, 默认为false */
    public void setEnable(boolean enable) {
        mEnable = enable;
    }

    public boolean isKeyboardShown() {
        return getKeyboardHeight() > 0;
    }

    public int getKeyboardHeight() {
        Rect rect = new Rect();
        if (mContentView != null) {
            mContentView.getWindowVisibleDisplayFrame(rect);
        } else {
            mDecorView.getWindowVisibleDisplayFrame(rect);
        }
        return getContentViewHeight() - rect.bottom;
    }

    //==========================================工具方法============================================

    /** 获取除底边操作栏之外的高度 */
    private int getContentViewHeight() {
        if (mContentView != null) return mContentView.getHeight();
        return mActivity.getWindowManager().getDefaultDisplay().getHeight();
    }

    /** 获取整个屏幕的高度,包括底边的虚拟操作栏, 但是通常我们所有的控件都是显示在底边栏之上的,因此也就不需要加上它的高度 */
    private int getScreenHeight() {
        return mDecorView.getRootView().getHeight();
    }

    //===========================监听事件

    List<OnScreenSizeChangedListener> mScreenSizeChangedListeners;

    public interface OnScreenSizeChangedListener {
        void onScreenChanged(boolean isKeyboardShow);
    }

    public void addOnScreenSizeChangedListener(OnScreenSizeChangedListener listener) {
        if (mScreenSizeChangedListeners == null) {
            mScreenSizeChangedListeners = new ArrayList<>();
        }
        mScreenSizeChangedListeners.add(listener);
    }

    public boolean removeOnScreenSizeChangedListener(OnScreenSizeChangedListener listener) {
        return mScreenSizeChangedListeners != null && listener != null && mScreenSizeChangedListeners.remove(listener);
    }
}
