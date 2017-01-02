package com.yesing.blibrary_wos.waveview.library;

import android.content.Context;
import android.util.AttributeSet;

/**
 * WaveView 的增强类, 集成了一些常用方法
 * Created by YesingBeijing on 2016/10/26.
 */
public class WaveView2 extends WaveView {

    private WaveHelper mWaveHelper = new WaveHelper(this);

    private boolean mBrimmed;

    public WaveView2(Context context) {
        super(context);
    }

    public WaveView2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WaveView2(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void start(){
        mWaveHelper.start();
    }

    public void cancel(){
        mWaveHelper.cancel();
    }

    public void brim() {
        mWaveHelper.brim();
        mBrimmed = true;
    }

    public void fade(float level) {
        mWaveHelper.fade(level);
        mBrimmed = false;
    }

    public boolean isBrimmed() {
        return mBrimmed;
    }
}
