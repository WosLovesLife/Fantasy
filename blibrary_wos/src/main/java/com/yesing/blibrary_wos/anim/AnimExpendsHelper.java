package com.yesing.blibrary_wos.anim;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

/**
 * Created by zhangh on 2016/10/30.
 */

public class AnimExpendsHelper {
    private View mView;
    private ValueAnimator mMAnimator;

    private boolean mModeMargin;
    private boolean mExtend;
    private int mBasePadding;
    private int mBaseMargin;

    private ViewGroup.MarginLayoutParams mMarginLayoutParams;

    private AnimExpendsHelper(View view) {
        mView = view;

        mMarginLayoutParams = (ViewGroup.MarginLayoutParams) mView.getLayoutParams();
        initAnim();
    }

    private void build() {
        if (mView.getMeasuredHeight() <= 0) {
            if (mView.getVisibility() != View.VISIBLE) {
                mView.setVisibility(View.VISIBLE);
            }

            sync();

            mBaseMargin = mMarginLayoutParams.bottomMargin;
            mBasePadding = mView.getPaddingBottom();
        }
    }

    private void initAnim() {
        //1.调用ofInt(int...values)方法创建ValueAnimator对象
        mMAnimator = ValueAnimator.ofInt(0, 0);
        //2.为目标对象的属性变化设置监听器
        /* 放在对外方法中方法中 */
        mMAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // 3.为目标对象的属性设置计算好的属性值
                int animatorValue = (int) animation.getAnimatedValue();

                if (mModeMargin) {
                    /* 通过Margin的方式 */
                    ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mView.getLayoutParams();
                    marginLayoutParams.bottomMargin = animatorValue;
                    mView.setLayoutParams(marginLayoutParams);
                } else {
                    /* 通过Padding的方式 */
                    mView.setPadding(
                            mView.getPaddingLeft(),
                            mView.getPaddingTop(),
                            mView.getPaddingRight(),
                            animatorValue);
                }
            }
        });
        //4.设置动画的持续时间、是否重复及重复次数等属性
        mMAnimator.setDuration(260);
        //5.为ValueAnimator设置目标对象并开始执行动画
        mMAnimator.setTarget(mView);
    }

    public void changeAnim(int start, int target) {
        mExtend = start < target;

        mMAnimator.setIntValues(start, target);
        mMAnimator.start();
    }

    public void expend() {
        if (!mExtend) {
            if (mModeMargin) {
                changeAnim(mMarginLayoutParams.bottomMargin, mBaseMargin);
            } else {
                changeAnim(mView.getPaddingBottom(), mBasePadding);
            }
        }
    }

    public void close() {
        if (mExtend) {
            if (mModeMargin) {
                changeAnim(mMarginLayoutParams.bottomMargin, -mView.getMeasuredHeight() - mBaseMargin);
            } else {
                changeAnim(mView.getPaddingBottom(), -mView.getMeasuredHeight() - mBasePadding);
            }
        }
    }

    public void shrink() {
        if (mExtend) {
            close();
        } else {
            expend();
        }
    }

    public boolean isExtend() {
        return mExtend;
    }

    public void addUpdateListener(ValueAnimator.AnimatorUpdateListener listener) {
        mMAnimator.addUpdateListener(listener);
    }

    public void addListener(Animator.AnimatorListener listener) {
        mMAnimator.addListener(listener);
    }

    public void sync(){
        mView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mView.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                if (!mExtend) {
                    if (mModeMargin) {
                        /* 通过Margin的方式 */
                        mMarginLayoutParams.bottomMargin = -mBaseMargin - mView.getMeasuredHeight();
                        mView.setLayoutParams(mMarginLayoutParams);
                    } else {
                        /* 通过Padding的方式 */
                        mView.setPadding(
                                mView.getPaddingLeft(),
                                mView.getPaddingTop(),
                                mView.getPaddingRight(),
                                -mBasePadding - mView.getMeasuredHeight());
                    }
                }
            }
        });
    }

    public static class Builder {
        private final AnimExpendsHelper mHelper;

        public Builder(View v) {
            mHelper = new AnimExpendsHelper(v);
        }

        public Builder addUpdateListener(ValueAnimator.AnimatorUpdateListener listener) {
            mHelper.addUpdateListener(listener);
            return this;
        }

        public Builder addListener(Animator.AnimatorListener listener) {
            mHelper.addListener(listener);
            return this;
        }

        public Builder setInitState(int visibility) {
            mHelper.mExtend = visibility == View.VISIBLE;
            return this;
        }

        public Builder setMode(int mode) {
            mHelper.mModeMargin = mode == 0;
            return this;
        }

        public AnimExpendsHelper build() {
            mHelper.build();
            return mHelper;
        }
    }
}