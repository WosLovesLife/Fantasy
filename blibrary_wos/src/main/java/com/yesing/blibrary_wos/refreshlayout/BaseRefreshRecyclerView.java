package com.yesing.blibrary_wos.refreshlayout;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yesing.blibrary_wos.R;
import com.yesing.blibrary_wos.baserecyclerviewadapter.adapter.BaseRecyclerViewAdapter;


/**
 * Created by YesingBeijing on 2016/9/14.
 */
public abstract class BaseRefreshRecyclerView extends SwipeRefreshLayout {

    //===========Views
    private RecyclerView mRecyclerView;
    private View mLoadMoreView;
    private TextView mTvFooterHint;
    private ProgressBar mPbFooterLoading;

    //===========管理器
    private RecyclerView.LayoutManager mLayoutManager;

    //===========适配器
    BaseRecyclerViewAdapter mAdapter;

    //===========监听器
    /** 监听器，下拉刷新或上拉加载时回调对应方法 */
    private OnRefreshListener mOnRefreshListener;
    /** 监听器, 当布局尺寸发生变化时回调相关方法 */
    private OnSizeChangeListener mOnSizeChangeListener;

    //===========变量
    /** 为true时, 滑动到最后一条时加载更多 */
    private boolean mLoadMoreEnable;
    /** 为true时说明数据大于一页, 则可以触发onLoadMore */
    private boolean mPullUp;
    /** 为了避免反复触发加载更多 */
    private boolean mIsLoadingMore;

    public BaseRefreshRecyclerView(Context context) {
        this(context, null);
    }

    public BaseRefreshRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mOnSizeChangeListener != null) {
            mOnSizeChangeListener.onSizeChanged(w, h, oldw, oldh);
        }
    }

    /**
     * 初始化
     */
    private void init() {
        mRecyclerView = new RecyclerView(getContext());
        addView(mRecyclerView);

        initRefreshLayout();
        initRecyclerView();
    }

    /**
     * 初始化刷新控件
     */
    private void initRefreshLayout() {
        setColorSchemeColors(Color.rgb(51, 181, 168), Color.rgb(64, 92, 113));
        setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mOnRefreshListener != null) {
                    mOnRefreshListener.onRefresh();
                }
            }
        });
    }

    /**
     * 初始化列表控件
     */
    private void initRecyclerView() {
        mLayoutManager = initLayoutManager();
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState != RecyclerView.SCROLL_STATE_IDLE) return;
                if (mLayoutManager.getItemCount() < 1) return;

                /* 双重判断, 如果用户是向上的滑动操作, 并且处于最后一条,且开启了loadMore且监听不为null */
                if (!mLoadMoreEnable || !mPullUp) return;

                /* 未在loading中,并且处于最下面,并且监听不为null,则通知加载更多 */
                if (!mIsLoadingMore && isLast() && mOnRefreshListener != null) {
                    showLoadMoreLoading();
                    mOnRefreshListener.onLoadMore();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mPullUp = dy > 0;

            }
        });
    }

    private boolean isLast() {
        return getLastVisibleItemPosition() >= mLayoutManager.getItemCount() - 1;
    }

    private boolean isFooterBottom() {
        View view = mLayoutManager.findViewByPosition(getLastVisibleItemPosition());
        if (view == mAdapter.getFooterView(mAdapter.getFootersCount() - 1)) {
            int[] size = new int[2];
            view.getLocationOnScreen(size);
            int i = size[1];
            if (i >= mRecyclerView.getHeight() - view.getHeight()) {
                return true;
            }
        }
        return false;
    }

    //==============================================================================================
    //==========================================控制方法============================================
    //==============================================================================================

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    /**
     * 设置RecyclerView数据适配器
     *
     * @param adapter 适配器
     */
    public void setAdapter(BaseRecyclerViewAdapter adapter) {
        mAdapter = adapter;
        mRecyclerView.setAdapter(adapter);
    }

    public void setLoadMoreFooterEnable(boolean enable) {
        if (mAdapter == null) {
            throw new IllegalStateException("该方法必须在setAdapter()方法之后调用");
        }

        if (enable) {
            if (mLoadMoreView != null) return;

            mLoadMoreView = LayoutInflater.from(getContext()).inflate(R.layout.refresh_recyclerview_footer_view, this, false);
            mTvFooterHint = (TextView) mLoadMoreView.findViewById(R.id.tv_hint);
            mPbFooterLoading = (ProgressBar) mLoadMoreView.findViewById(R.id.pb_loading);
            mAdapter.addFooterView(mLoadMoreView);
        } else {
            if (mLoadMoreView != null) {
                mAdapter.removeFooter(mLoadMoreView);
            }
        }
    }

    public void showLoadMoreLoading() {
        showLoadMoreLoading(null);
    }

    public void showLoadMoreLoading(@Nullable String msg) {
        if (mAdapter == null) {
            throw new NullPointerException("必须先调用setLoadMoreFooter()");
        }
        mIsLoadingMore = true;

        mTvFooterHint.setText(msg == null ? "正在获取数据..." : msg);
        mTvFooterHint.setClickable(false);
        mTvFooterHint.setVisibility(VISIBLE);
        mPbFooterLoading.setVisibility(VISIBLE);
    }

    public void showRetry(OnClickListener onClickListener) {
        showRetry(null, onClickListener);
    }

    public void showRetry(@Nullable String msg, OnClickListener onClickListener) {
        mIsLoadingMore = true;

        mTvFooterHint.setText(msg == null ? "加载失败,点击重试" : msg);
        mTvFooterHint.setClickable(true);
        mTvFooterHint.setOnClickListener(onClickListener);
        mTvFooterHint.setVisibility(VISIBLE);
        mPbFooterLoading.setVisibility(INVISIBLE);
    }

    public void showEndLine() {
        showEndLine(null);
    }

    public void showEndLine(@Nullable String msg) {
        if (mAdapter == null) {
            throw new NullPointerException("必须先设置Adapter");
        }
        mIsLoadingMore = false;

        if (!isFooterBottom()) return;

        mTvFooterHint.setText(msg == null ? "— 已经到底了 —" : msg);
        mTvFooterHint.setClickable(false);
        mTvFooterHint.setVisibility(VISIBLE);
        mPbFooterLoading.setVisibility(INVISIBLE);
    }

    public void hideEndLine() {
        if (mAdapter == null) {
            throw new NullPointerException("必须先设置Adapter");
        }
        mIsLoadingMore = false;

        mTvFooterHint.setVisibility(INVISIBLE);
        mPbFooterLoading.setVisibility(INVISIBLE);
    }

    public boolean isLoadingMore() {
        return mIsLoadingMore;
    }

    /**
     * 开启刷新
     */
    public void startRefreshing() {
        if (!isRefreshing()) {
            setRefreshing(true);
        }
    }

    /**
     * 停止刷新
     */
    public void refreshingComplete() {
        if (isRefreshing()) {
            setRefreshing(false);
        }
    }

    /**
     * 是否启用下拉刷新
     *
     * @param enable 为true时启用, 默认为true
     */
    public void setRefreshEnable(boolean enable) {
        setEnabled(enable);
    }

    public interface OnRefreshListener {
        void onRefresh();

        void onLoadMore();
    }

    public interface OnSizeChangeListener {
        void onSizeChanged(int w, int h, int oldW, int oldH);
    }

    /**
     * 是否启用加载更多(当滑动到最后一条时触发)
     *
     * @param enable 为true时启用, 默认为false
     */
    public void setLoadMoreEnable(boolean enable) {
        mLoadMoreEnable = enable;
    }

    public void setBothEnable(boolean enable) {
        setRefreshEnable(enable);
        setLoadMoreEnable(enable);
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        mOnRefreshListener = listener;
    }

    public void setOnSizeChangedListener(OnSizeChangeListener listener) {
        mOnSizeChangeListener = listener;
    }

    public void setListPadding(int left, int top, int right, int bottom) {
        mRecyclerView.setPadding(left, top, right, bottom);
    }

    public int getListWidth() {
        return (mRecyclerView.getMeasuredWidth()
                - mRecyclerView.getPaddingLeft()
                - mRecyclerView.getPaddingRight())
                / getSpanCount();
    }

    //=============================需要子类实现的=========================
    protected abstract RecyclerView.LayoutManager initLayoutManager();
    public abstract int getFirstCompletelyVisibleItemPosition();
    public abstract int getFirstVisibleItemPosition();
    public abstract int getLastCompletelyVisibleItemPosition();
    public abstract int getLastVisibleItemPosition();

    public abstract int getSpanCount();
}
