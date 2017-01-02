package com.yesing.blibrary_wos.refreshlayout;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by YesingBeijing on 2016/9/13.
 */
public class LinearRefreshRecyclerView extends BaseRefreshRecyclerView {

    private GridLayoutManager mLayoutManager;

    public LinearRefreshRecyclerView(Context context) {
        this(context, null);
    }

    public LinearRefreshRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected RecyclerView.LayoutManager initLayoutManager() {
        mLayoutManager = new GridLayoutManager(getContext(), 1);
        return mLayoutManager;
    }

    @Override
    public int getFirstCompletelyVisibleItemPosition() {
        return mLayoutManager.findFirstCompletelyVisibleItemPosition();
    }

    @Override
    public int getFirstVisibleItemPosition() {
        return mLayoutManager.findFirstVisibleItemPosition();
    }

    @Override
    public int getLastCompletelyVisibleItemPosition() {
        return mLayoutManager.findLastCompletelyVisibleItemPosition();
    }

    @Override
    public int getLastVisibleItemPosition() {
        return mLayoutManager.findLastVisibleItemPosition();
    }

    @Override
    public int getSpanCount() {
        return mLayoutManager.getSpanCount();
    }

    public void setSpanCount(int spanCount) {
        mLayoutManager.setSpanCount(spanCount);
    }
}
