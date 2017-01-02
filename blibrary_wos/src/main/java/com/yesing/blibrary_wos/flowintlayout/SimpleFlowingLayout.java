package com.yesing.blibrary_wos.flowintlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by YesingBeijing on 2016/8/9.
 */
@Deprecated
public class SimpleFlowingLayout extends FrameLayout {

    private int mVb;
    private int mVr;

    public SimpleFlowingLayout(Context context) {
        this(context, null);
    }

    public SimpleFlowingLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleFlowingLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int w = measureSize(widthMeasureSpec, mVr);
        int h = measureSize(heightMeasureSpec, mVb);
        setMeasuredDimension(w, h);
    }

    private int measureSize(int spec, int defaultSize) {
        int result;
        int mode = MeasureSpec.getMode(spec);
        if (mode == MeasureSpec.EXACTLY) {
            result = MeasureSpec.getSize(spec);
        } else {
            result = defaultSize;
            if (mode == MeasureSpec.AT_MOST) {
                result = Math.min(result, MeasureSpec.getSize(spec));
            }
        }
        return result;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int right = r - l;

        /* 记录已经使用的宽度值, 后面的控件按照该值作为横向起始值 */
        int usedW = getPaddingLeft();
        /* 记录上一行已使用高度, 作为当前行的起始值, 当需要换行的时候, 则要加上当前行的最大高度值 */
        int usedH = getPaddingTop();

        /* 表示已知的最大高度, 记录了上一行的高度和当前行的控件中占用高度最大的值.
         当需要换行的时候, 依照该值作为起始值 */
        int maxHeight = 0;

        /* 循环布局每一个子控件 */
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);

            if (view == null || view.getVisibility() == GONE) continue; // 忽略

            LayoutParams params = (LayoutParams) view.getLayoutParams();

            /* 横向起始值 = 已使用的宽度 + 该控件的左边外边距.
             * 结束值 = 起始值 + 控件宽度 */
            int vl = usedW + params.leftMargin;
            mVr = view.getMeasuredWidth() + vl;

            /* 纵向起始值 = 已使用的高度 + 该控件的顶部外边距
             * 结束值 = 起始值 + 控件高度 */
            int vt = usedH + params.topMargin;
            mVb = view.getMeasuredHeight() + vt;

            /* 重点!!!
            当一行中的已使用的宽度再上该控件的宽度会超出父布局的宽度时
            则另起一行, 让当前子控件从新的一行开始布局.
            以下操作:
            首先恢复已使用的宽度为默认值paddingLeft(即父布局占用的大小).然后重新计算出横向的起始值和结束值
            然后变更已使用的高度为最大占用高度值, 然后重新计算起始高度值和结束高度值  */
            if (mVr > right) {
                usedW = getPaddingLeft();
                vl = usedW + params.leftMargin;
                mVr = view.getMeasuredWidth() + vl;

                usedH = maxHeight;
                vt = usedH + params.topMargin;
                mVb = view.getMeasuredHeight() + vt;
            }

            usedW = mVr + params.rightMargin;

            /* 如果某个控件的总高度大于已知的最大高度值, 则取代 */
            int sum = mVb + params.bottomMargin;
            if (sum > maxHeight) maxHeight = sum;

            view.layout(vl, vt, mVr, mVb);
        }
    }
}
