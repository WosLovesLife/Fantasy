package com.wosloveslife.fantasy.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;

import com.wosloveslife.fantasy.R;

/**
 * Created by zhangh on 2017/2/5.
 */

public class WosNavigationLayout extends ScrollLinearLayout {
    private Drawable mCheckedDrawable;
    private Drawable mRecoverCheckedDrawable;

    int mCheckedItem = -1;


    public WosNavigationLayout(Context context) {
        this(context, null);
    }

    public WosNavigationLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WosNavigationLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        mCheckedDrawable = getResources().getDrawable(R.drawable.shape_rect_checkted);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public WosNavigationLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setCheckedItem(mCheckedItem);
    }

    public int getCheckedItem() {
        return mCheckedItem < 0 ? 0 : mCheckedItem;
    }

    public void setCheckedItem(int checkedItem) {
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (mCheckedItem == i) {
                childAt.setBackgroundDrawable(mRecoverCheckedDrawable);
            }
            if (checkedItem == i) {
                mRecoverCheckedDrawable = childAt.getBackground();
                childAt.setBackgroundDrawable(mCheckedDrawable);
                break;
            }
        }
        mCheckedItem = checkedItem;
    }
}
