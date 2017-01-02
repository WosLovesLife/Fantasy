package com.yesing.blibrary_wos.utils.viewUtils;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

/**
 * 通用的一些工具方法, 例如设置宽高比例
 * Created by YesingBeijing on 2016/9/20.
 */
public class CommonViewUtils {
    /**
     * 宽度作为1 设置高度的比例
     *
     * @param sizeRatio 例如sizeRatio = 0.5f 则高度 = 宽度 * 0.5f
     */
    public static void setSizeRatio(final View view, final float sizeRatio) {
        int measuredWidth = view.getMeasuredWidth();
        if (measuredWidth > 0) {
            setRatio(view, sizeRatio);
            return;
        }

        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                view.getViewTreeObserver().removeOnPreDrawListener(this);
                setRatio(view, sizeRatio);
                return true;
            }
        });
    }

    private static void setRatio(final View view, float sizeRatio) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = (int) (view.getMeasuredWidth() * sizeRatio);
        view.setLayoutParams(params);
    }
}
