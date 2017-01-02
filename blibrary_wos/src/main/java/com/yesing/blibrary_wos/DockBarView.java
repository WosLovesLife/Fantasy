package com.yesing.blibrary_wos;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.annotation.DrawableRes;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YesingBeijing on 2016/11/21.
 */
public class DockBarView extends LinearLayout {
    public static final float MAX_RATIO = 1.32f;
    public static final float DEF_RATIO = 1.0f;

    private int mIconWidth;
    private int mIconHeight;
    private float mOffsetY;
    private boolean mIsChildInit;
    private int mDuration = 300;

    private TabView mCheckedTab;

    private ViewPager mViewPager;

    private List<TabView> mTabViewList = new ArrayList<>();

    public DockBarView(Context context) {
        this(context, null);
    }

    public DockBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DockBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mOffsetY = toPX(context, 4f);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (!mIsChildInit && mTabViewList.size() > 0) {
            mIconWidth = mTabViewList.get(0).mImageView.getMeasuredWidth();
            mIconHeight = mTabViewList.get(0).mImageView.getMeasuredHeight();

            mIsChildInit = true;

            if (mCheckedTab == null) {
                mCheckedTab = mTabViewList.get(0);
            }
            toDefault();
        }
    }

    public interface CallBack {
        void onClick(TabView tabView);
    }

    /**
     * 添加一个不影响ViewPager的Item
     */
    public void addTab(@DrawableRes int originRes, @DrawableRes int targetRes, String originTitle, String targetTitle, final CallBack callBack) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_anim_dock_bar, this, false);
        ImageView icon = (ImageView) view.findViewById(R.id.iv_icon);
        TextView label = (TextView) view.findViewById(R.id.tv_label);

        final TabView tabView = new TabView(
                view, label, icon,
                getResources().getDrawable(originRes), getResources().getDrawable(targetRes),
                originTitle, targetTitle, mDuration);

        tabView.mRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callBack != null) {
                    if (!tabView.isChecked()) {
                        tabView.setCheckedWithoutAnim(true);
//                        runDockBarAnim(tabView.mRatio, MAX_RATIO, tabView.mImageView, tabView).start();
                    } else {
                        tabView.setCheckedWithoutAnim(false);
//                        runDockBarAnim(tabView.mRatio, DEF_RATIO, tabView.mImageView, tabView).start();
                    }
                    callBack.onClick(tabView);
                }
            }
        });

        addView(view);
        LinearLayout.LayoutParams params = (LayoutParams) view.getLayoutParams();
        params.weight = 1;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.width = 0;
        params.gravity = Gravity.BOTTOM;
        view.setLayoutParams(params);
    }

    public void addTab(@DrawableRes int originRes, @DrawableRes int targetRes, String originTitle, String targetTitle) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_anim_dock_bar, this, false);
        ImageView icon = (ImageView) view.findViewById(R.id.iv_icon);
        TextView label = (TextView) view.findViewById(R.id.tv_label);

        final TabView tabView = new TabView(
                view, label, icon,
                getResources().getDrawable(originRes), getResources().getDrawable(targetRes),
                originTitle, targetTitle, mDuration);

        mTabViewList.add(tabView);

        tabView.mRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle(tabView);
            }
        });

        addView(view);
        LinearLayout.LayoutParams params = (LayoutParams) view.getLayoutParams();
        params.weight = 1;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.width = 0;
        params.gravity = Gravity.BOTTOM;
        view.setLayoutParams(params);
    }

    public TabView getTab(int position) {
        if (position >= 0 && position < mTabViewList.size()) {
            return mTabViewList.get(position);
        }
        return null;
    }

    public void removeTab(int position) {
        if (position >= 0 && position < mTabViewList.size()) {
            TabView tabView = mTabViewList.get(position);
            mTabViewList.remove(position);
            removeView(tabView.mRootView);
        }
    }

    public void setDuration(int duration) {
        mDuration = duration;
        for (TabView tabView : mTabViewList) {
            tabView.mDuration = duration;
        }
    }

    /**
     * 获取当前选中的Tab的索引
     *
     * @return 如果返回-1 说明没有选中的条目(默认选中0)
     */
    public int getCheckedPosition() {
        if (mCheckedTab != null) {
            return mTabViewList.indexOf(mCheckedTab);
        }
        return -1;
    }

    public TabView getCheckedTab() {
        return mCheckedTab;
    }

    public void setCheckedPosition(int position) {
        if (mViewPager == null) {
            throw new IllegalStateException("必须在 bindViewPager() 之后调用");
        }
        if (position >= 0 && position < mTabViewList.size()) {
            toggle(mTabViewList.get(position));
        }
    }

    /**
     * 回归的默认选择状态, 适用于第一次加载完成后显示默认选中的Tab
     */
    private void toDefault() {
        if (mCheckedTab == null) return;
        mCheckedTab.setCheckedWithoutAnim(true);
        ImageView target = mCheckedTab.mImageView;
        ViewGroup.MarginLayoutParams params = (MarginLayoutParams) target.getLayoutParams();
        params.width = (int) (mIconWidth * MAX_RATIO);
        params.height = (int) (mIconHeight * MAX_RATIO);
        target.setLayoutParams(params);
        target.setPadding(target.getPaddingLeft(), target.getPaddingTop(), target.getPaddingRight(), (int) (mOffsetY * (MAX_RATIO - DEF_RATIO)));

        changePage(getCheckedPosition());
    }

    /**
     * 如果当前的Item是选中的状态, 则不作处理,
     * 如果未选中,则切换Item的状态, 并且将之前选中的Item状态复原,并且切换ViewPager页面
     *
     * @param tabView 点击的Item
     */
    private void toggle(TabView tabView) {
        if (!mIsChildInit) {
            mCheckedTab = tabView;
            return;
        }

        if (!tabView.isChecked()) {
            if (mCheckedTab != null) {
                mCheckedTab.setChecked(false);
                runDockBarAnim(mCheckedTab.mRatio, DEF_RATIO, mCheckedTab.mImageView, mCheckedTab).start();
            }
            mCheckedTab = tabView;
            changePage(getCheckedPosition());

            tabView.setChecked(true);
            runDockBarAnim(tabView.mRatio, MAX_RATIO, tabView.mImageView, tabView).start();
        }
    }

    private ValueAnimator runDockBarAnim(float start, float end, final View target, final TabView tabView) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(start, end);
        valueAnimator.setDuration(mDuration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float ratio = (float) animation.getAnimatedValue();
                tabView.mRatio = ratio;

                ViewGroup.MarginLayoutParams params = (MarginLayoutParams) target.getLayoutParams();
                params.width = (int) (mIconWidth * ratio);
                params.height = (int) (mIconHeight * ratio);
                target.setLayoutParams(params);

                /* 在放大的同时向上偏移,原本打算用TranslationY,但是发现会使图片移动出控件外,所以只好通过设置padding的方式设置
                 * 目前有一个小问题, 就是如果target本身有原始的padding值, 则这样就会使原始的padding值受到影响 */
                target.setPadding(target.getPaddingLeft(), target.getPaddingTop(), target.getPaddingRight(), (int) (mOffsetY * (ratio - 1f)));
            }
        });
        valueAnimator.setTarget(target);
        return valueAnimator;
    }


    //=======================================和ViewPager的联动======================================

    public void bindViewPager(ViewPager viewPager) {
        mViewPager = viewPager;

        changePage(getCheckedPosition());
    }

    private void changePage(int position) {
        if (mViewPager == null) return;
        if (position >= 0) {
            mViewPager.setCurrentItem(position, false);
        }
    }

    //=============================================工具=============================================

    /** 色彩平滑过渡的动画 */
    private static TransitionDrawable getTransitionDrawable(Drawable source, Drawable target, int duration) {
        TransitionDrawable transitionDrawable = new TransitionDrawable(new Drawable[]{source, target});
        transitionDrawable.setCrossFadeEnabled(true);
        transitionDrawable.startTransition(duration);
        return transitionDrawable;
    }

    public static float toPX(Context context, float dp) {
        return context.getResources().getDisplayMetrics().density * dp;
    }

    public static class TabView {
        public View mRootView;
        public TextView mTextView;
        public ImageView mImageView;

        public Drawable mOriginDrawable;
        public Drawable mTargetDrawable;
        public String mOriginTitle;
        public String mTargetTitle;

        public int mDuration;
        public boolean mIsChecked;
        public float mRatio = 1;

        public TabView(View rootView, TextView textView, ImageView imageView, Drawable originDrawable, Drawable targetDrawable, String originTitle, String targetTitle, int duration) {
            mRootView = rootView;
            mTextView = textView;
            mImageView = imageView;
            mOriginDrawable = originDrawable;
            mTargetDrawable = targetDrawable;
            mOriginTitle = originTitle;
            mTargetTitle = targetTitle;
            mDuration = duration;

            if (null == mTargetDrawable) {
                mTargetDrawable = mOriginDrawable;
            }

            if (null == mOriginTitle) {
                mOriginTitle = "";
            }

            if (null == mTargetTitle) {
                mTargetTitle = mOriginTitle;
            }

            mImageView.setImageDrawable(mOriginDrawable);
            mTextView.setText(mOriginTitle);
        }

        public void setCheckedWithoutAnim(boolean isChecked) {
            mIsChecked = isChecked;

            if (mIsChecked) {
                mImageView.setImageDrawable(mTargetDrawable);
                mTextView.setText(mTargetTitle);
            } else {
                mImageView.setImageDrawable(mOriginDrawable);
                mTextView.setText(mOriginTitle);
            }
        }

        public void setChecked(boolean isChecked) {
            if (isChecked == mIsChecked) return;

            mIsChecked = isChecked;

            if (mIsChecked) {
                if (mOriginDrawable != null && mTargetDrawable != null) {
                    mImageView.setImageDrawable(getTransitionDrawable(mOriginDrawable, mTargetDrawable, mDuration));
                } else if (mTargetDrawable != null) {
                    mImageView.setImageDrawable(mTargetDrawable);
                }

                mTextView.setText(mTargetTitle);
            } else {
                if (mOriginDrawable != null && mTargetDrawable != null) {
                    mImageView.setImageDrawable(getTransitionDrawable(mTargetDrawable, mOriginDrawable, mDuration));
                } else if (mOriginDrawable != null) {
                    mImageView.setImageDrawable(mOriginDrawable);
                }

                mTextView.setText(mOriginTitle);
            }
        }

        public boolean isChecked() {
            return mIsChecked;
        }
    }
}
