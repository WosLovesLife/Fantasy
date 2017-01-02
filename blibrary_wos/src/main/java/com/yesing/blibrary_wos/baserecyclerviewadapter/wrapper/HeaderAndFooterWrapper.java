package com.yesing.blibrary_wos.baserecyclerviewadapter.wrapper;

import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * 带Header和Footer的RecyclerViewAdapter
 * Created by YesingBeijing on 2016/9/18.
 */
public class HeaderAndFooterWrapper<T> extends RecyclerView.Adapter<ViewHolder> {
    private static final int BASE_ITEM_TYPE_HEADER = 100000;
    private static final int BASE_ITEM_TYPE_FOOTER = 200000;

    /**
     * SparseArrayCompat类似于Map，只不过在某些情况下比Map的性能要好，
     * 并且只能存储key为int的情况。
     */
    private SparseArrayCompat<View> mHeaderViews = new SparseArrayCompat<>();
    private SparseArrayCompat<View> mFooterViews = new SparseArrayCompat<>();

    private RecyclerView.Adapter mInnerAdapter;

    public HeaderAndFooterWrapper(RecyclerView.Adapter adapter) {
        mInnerAdapter = adapter;
    }

    public boolean isHeaderViewPos(int position) {
        return position < getHeadersCount();
    }

    public boolean isFooterViewPos(int position) {
        return position >= getHeadersCount() + getRealItemCount();
    }

    public void addHeaderView(View v) {
        mHeaderViews.put(mHeaderViews.size() + BASE_ITEM_TYPE_HEADER, v);
    }

    public void addFooterView(View v) {
        mFooterViews.put(mFooterViews.size() + BASE_ITEM_TYPE_FOOTER, v);
    }

    public int getHeadersCount() {
        return mHeaderViews.size();
    }

    public int getFootersCount() {
        return mFooterViews.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mHeaderViews.get(viewType) != null) {
            return new ViewHolder(mHeaderViews.get(viewType)) {
            };
        } else if (mFooterViews.get(viewType) != null) {
            return new ViewHolder(mFooterViews.get(viewType)) {
            };
        }
        return mInnerAdapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public int getItemViewType(int position) {
        if (isHeaderViewPos(position)) {
            return mHeaderViews.keyAt(position);
        } else if (isFooterViewPos(position)) {
            return mFooterViews.keyAt(position - getHeadersCount() - getRealItemCount());
        }
        return mInnerAdapter.getItemViewType(position - getHeadersCount());
    }

    private int getRealItemCount() {
        return mInnerAdapter.getItemCount();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (isHeaderViewPos(position)) return;
        if (isFooterViewPos(position)) return;

        mInnerAdapter.onBindViewHolder(holder, position - getHeadersCount());
    }

    @Override
    public int getItemCount() {
        return getHeadersCount() + getFootersCount() + getRealItemCount();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mInnerAdapter.onAttachedToRecyclerView(recyclerView);

        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            final GridLayoutManager.SpanSizeLookup spanSizeLookup = gridLayoutManager.getSpanSizeLookup();

            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {

                @Override
                public int getSpanSize(int position) {
                    int itemViewType = getItemViewType(position);
                    if (mHeaderViews.get(itemViewType) != null) {
                        return gridLayoutManager.getSpanCount();
                    } else if (mFooterViews.get(itemViewType) != null) {
                        return gridLayoutManager.getSpanCount();
                    }
                    if (spanSizeLookup != null) {
                        return spanSizeLookup.getSpanSize(position);
                    }
                    return 1;
                }
            });
        }
    }

    private static final String TAG = "HeaderAndFooterWrapper";

    @Override
    public void onViewAttachedToWindow(ViewHolder holder) {
        mInnerAdapter.onViewAttachedToWindow(holder);
        int position = holder.getLayoutPosition();
        ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
        if (params != null && params instanceof StaggeredGridLayoutManager.LayoutParams) {
            StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) params;
            if (isHeaderViewPos(position) || isFooterViewPos(position)) {
                p.setFullSpan(true);
            } else {
                int spanIndex = p.getSpanIndex();
                Log.w(TAG, "onViewAttachedToWindow: spanIndex = " + spanIndex);
            }
        }
    }
}
