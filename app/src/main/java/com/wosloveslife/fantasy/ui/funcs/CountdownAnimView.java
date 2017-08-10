package com.wosloveslife.fantasy.ui.funcs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.wosloveslife.fantasy.R;
import com.wosloveslife.fantasy.manager.SettingConfig;
import com.wosloveslife.fantasy.utils.FormatUtils;
import com.yesing.blibrary_wos.utils.screenAdaptation.Dp2Px;

/**
 * Created by zhangh on 2017/6/22.
 */

public class CountdownAnimView extends FrameLayout {

    private TextView mTextView;
    private RoundedImageView mImageView;

    public CountdownAnimView(@NonNull Context context) {
        this(context, null);
    }

    public CountdownAnimView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CountdownAnimView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        int size = Dp2Px.toPX(getContext(), 150);
        mImageView = new RoundedImageView(getContext());
        mImageView.setImageResource(R.color.colorAccent);
        mImageView.setCornerRadius(size / 2);
        mImageView.setVisibility(GONE);
        mImageView.setAlpha(0f);
        addView(mImageView, new FrameLayout.LayoutParams(size, size, Gravity.CENTER));

        mTextView = new TextView(getContext());
        mTextView.setTextSize(30);
        mTextView.setTextColor(getResources().getColor(R.color.colorPrimary));
        mTextView.setTypeface(Typeface.DEFAULT_BOLD);
        addView(mTextView, new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
    }

    public void show(ViewGroup parent, long date) {
        parent.addView(this, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.CENTER));


        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeGlobalOnLayoutListener(this);

                tick();
            }
        });
    }

    private void tick() {
        new TickTask(0).run();
        postDelayed(new TickTask(0), 1000);
        postDelayed(new TickTask(0), 2000);
    }

    private class TickTask implements Runnable {
        int mIndex;

        public TickTask(int index) {
            mIndex = index;
        }

        @Override
        public void run() {
            mTextView.setText(FormatUtils.stringForTime(SettingConfig.getCountdownTime() - System.currentTimeMillis()));
            mTextView.setScaleX(1);
            mTextView.setScaleY(1);
            mTextView.setAlpha(1);

            mTextView.animate().scaleX(2f).scaleY(2f).alpha(0).setDuration(280).setStartDelay(720).start();
            if (mIndex == 2) {
                mImageView.animate().alpha(0).scaleX(1.5f).scaleY(1.5f).setDuration(280).setStartDelay(720).start();

                mTextView.animate().setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        super.onAnimationCancel(animation);
                        dismiss();
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        dismiss();
                    }
                });
            }
        }
    }

    private void dismiss() {
        clearAnimation();
        ViewParent parent = getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(CountdownAnimView.this);
        }
    }
}
