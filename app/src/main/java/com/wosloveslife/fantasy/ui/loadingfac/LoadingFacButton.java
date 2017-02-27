package com.wosloveslife.fantasy.ui.loadingfac;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.wosloveslife.fantasy.R;
import com.yesing.blibrary_wos.utils.screenAdaptation.Dp2Px;

/**
 * Created by zhangh on 2017/2/25.
 */
public class LoadingFacButton extends FrameLayout {

    private ProgressBar mProgressBar;
    private int mProgressAddSize;

    public LoadingFacButton(Context context) {
        this(context, null);
    }

    public LoadingFacButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingFacButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LoadingFacButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    FloatingActionButton mFacButton;

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);

        if (child instanceof FloatingActionButton) {
            mProgressAddSize = Dp2Px.toPX(getContext(), 11);

            mFacButton = (FloatingActionButton) child;
            FrameLayout.LayoutParams params = (LayoutParams) mFacButton.getLayoutParams();
            params.gravity = Gravity.CENTER;
            mFacButton.setLayoutParams(params);

            mProgressBar = new ProgressBar(getContext());
            mProgressBar.setVisibility(mVisibility);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mProgressBar.setIndeterminateTintList(ColorStateList.valueOf(getResources().getColor(R.color.red_normal)));
            }
            addView(mProgressBar, 0);
        }
    }

    boolean mInit;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (mInit) return;

        if (mFacButton != null) {
            resize(w, h);
            mInit = true;
        }
    }

    private void resize(int w, int h) {
        ViewGroup.LayoutParams params = mProgressBar.getLayoutParams();
        params.width = w + mProgressAddSize;
        params.height = h + mProgressAddSize;
        mProgressBar.setLayoutParams(params);
    }

    int mVisibility;

    public void showLoading() {
        if (mProgressBar != null) {
            mVisibility = VISIBLE;
            mProgressBar.setVisibility(VISIBLE);
        }
    }

    public void dismissLoading() {
        if (mProgressBar != null) {
            mVisibility = GONE;
            mProgressBar.setVisibility(GONE);
        }
    }
}
