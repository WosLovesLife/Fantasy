package com.yesing.blibrary_wos.anim;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;

/**
 * Created by YesingBeijing on 2016/11/21.
 */
public class CommonAnim {
    /**
     * 图片渐变
     * @param source 原图
     * @param target 目标图
     * @param duration 持续时间
     * @return 将结果设置给控件即可
     */
    private static TransitionDrawable getTransitionDrawable(Drawable source, Drawable target, int duration) {
        TransitionDrawable transitionDrawable = new TransitionDrawable(
                new Drawable[]{source, target});
        transitionDrawable.setCrossFadeEnabled(true);
        transitionDrawable.startTransition(duration);
        return transitionDrawable;
    }
}
