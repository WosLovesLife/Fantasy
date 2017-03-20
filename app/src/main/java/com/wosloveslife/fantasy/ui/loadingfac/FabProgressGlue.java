package com.wosloveslife.fantasy.ui.loadingfac;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.support.design.widget.FloatingActionButton;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;

import com.yesing.blibrary_wos.utils.screenAdaptation.Dp2Px;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by zhangh on 2017/2/26.
 */

public class FabProgressGlue {
    private final int mProgressSize;
    private FloatingActionButton mFacBtn;
    private ProgressBar mProgressBar;

    public FabProgressGlue(FloatingActionButton facBtn, ProgressBar progressBar) {
        mFacBtn = facBtn;
        mProgressBar = progressBar;
        mProgressSize = Dp2Px.toPX(mFacBtn.getContext(), 10);
        syncProgressSize();
    }

    public void show(final boolean loading) {
        if (mFacBtn.isShown()) {
            if (loading) {
                showLoading();
            }
        } else {
            mFacBtn.show(new FloatingActionButton.OnVisibilityChangedListener() {
                @Override
                public void onShown(FloatingActionButton fab) {
                    super.onShown(fab);
                    if (loading) {
                        showLoading();
                    }
                }
            });
        }
    }

    public void hide() {
        hideLoading();
        mFacBtn.hide();
    }

    public void showLoading() {
        mProgressBar.setVisibility(VISIBLE);
    }

    public void hideLoading() {
        mProgressBar.setVisibility(GONE);
    }

    public boolean isLoading() {
        return mProgressBar.getVisibility() == VISIBLE;
    }

    public void smoothTranslationAndScale(float scale, float tranX, TimeInterpolator interpolator, Animator.AnimatorListener animatorListener) {
        mFacBtn.animate().cancel();
        mFacBtn.animate()
                .scaleX(scale)
                .scaleY(scale)
                .setDuration(200)
                .translationX(tranX)
                .setListener(animatorListener)
                .setInterpolator(interpolator);
    }

    private void syncProgressSize() {
        if (mFacBtn.getWidth() <= 0) {
            mFacBtn.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mFacBtn.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    ViewGroup.LayoutParams params = mProgressBar.getLayoutParams();
                    params.width = mFacBtn.getWidth() + mProgressSize;
                    params.height = mFacBtn.getHeight() + mProgressSize;
                    mProgressBar.setLayoutParams(params);
                }
            });
        } else {
            ViewGroup.LayoutParams params = mProgressBar.getLayoutParams();
            params.width = mFacBtn.getWidth() + mProgressSize;
            params.height = mFacBtn.getHeight() + mProgressSize;
            mProgressBar.setLayoutParams(params);
        }
    }
}
