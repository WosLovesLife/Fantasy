package com.wosloveslife.fantasy.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;

import com.wosloveslife.fantasy.R;
import com.wosloveslife.fantasy.bean.BLyric;
import com.yesing.blibrary_wos.utils.screenAdaptation.Dp2Px;


/**
 * Created by zhangh on 2017/2/2.
 */

public class LrcView extends View {
    BLyric mBLyric;
    private Paint mPaint;
    private int mTextSize;
    private int mTextSpace;
    private int mWidth;

    public LrcView(Context context) {
        this(context, null);
    }

    public LrcView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LrcView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public LrcView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(1080, 500);
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(getResources().getColor(R.color.colorAccent));

        mTextSize = Dp2Px.toPX(getContext(), 16);
        mTextSpace = mTextSize + Dp2Px.toPX(getContext(), 2);
        mPaint.setTextSize(mTextSize);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mBLyric == null || mBLyric.mLrc == null) return;

        int v = mTextSpace;
        for (BLyric.LyricLine lyricLine : mBLyric.mLrc) {
            float measureText = mPaint.measureText(lyricLine.content);
            canvas.drawText(lyricLine.content, (mWidth - measureText) / 2, v, mPaint);
            v += mTextSpace;
        }
    }

    public void setLrc(BLyric lrc) {
        mBLyric = lrc;
        invalidate();
    }
}
