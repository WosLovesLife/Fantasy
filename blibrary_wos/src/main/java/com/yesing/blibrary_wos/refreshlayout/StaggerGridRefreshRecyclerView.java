package com.yesing.blibrary_wos.refreshlayout;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;

import java.util.Arrays;

/**
 * Created by YesingBeijing on 2016/9/13.
 */
public class StaggerGridRefreshRecyclerView extends BaseRefreshRecyclerView {

    private StaggeredGridLayoutManager mLayoutManager;

    public StaggerGridRefreshRecyclerView(Context context) {
        super(context);
    }

    public StaggerGridRefreshRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected RecyclerView.LayoutManager initLayoutManager() {
        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        return mLayoutManager;
    }

    @Override
    public int getFirstCompletelyVisibleItemPosition() {
        int[] itemPositions = mLayoutManager.findFirstCompletelyVisibleItemPositions(null);
        Arrays.sort(itemPositions);
        return itemPositions[0];
    }

    @Override
    public int getFirstVisibleItemPosition() {
        int[] itemPositions = mLayoutManager.findFirstVisibleItemPositions(null);
        Arrays.sort(itemPositions);
        return itemPositions[0];
    }

    @Override
    public int getLastCompletelyVisibleItemPosition() {
        /* 这个数组中的position表示整个元素序列中,处于最靠近底边的元素所在的下标.如果列表有两列, 则数组length=2 */
        int[] itemPositions = mLayoutManager.findLastCompletelyVisibleItemPositions(null);
        Arrays.sort(itemPositions);
        return itemPositions[itemPositions.length - 1];
    }

    @Override
    public int getLastVisibleItemPosition() {
        /* 这个数组中的position表示整个元素序列中,处于最靠近底边的元素所在的下标.如果列表有两列, 则数组length=2 */
        int[] itemPositions = mLayoutManager.findLastVisibleItemPositions(null);
        Arrays.sort(itemPositions);
        return itemPositions[itemPositions.length - 1];
    }

    @Override
    public int getSpanCount() {
        return mLayoutManager.getSpanCount();
    }
}
