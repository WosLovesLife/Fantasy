package com.wosloveslife.fantasy.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.wosloveslife.fantasy.R;
import com.wosloveslife.fantasy.adapter.SearchResultAdapter;
import com.wosloveslife.fantasy.bean.BMusic;
import com.wosloveslife.fantasy.manager.MusicManager;
import com.wosloveslife.fantasy.utils.DividerDecoration;
import com.yesing.blibrary_wos.baserecyclerviewadapter.adapter.BaseRecyclerViewAdapter;
import com.yesing.blibrary_wos.utils.screenAdaptation.Dp2Px;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by zhangh on 2017/2/20.
 */

public class SearchSuggestLayout extends FrameLayout {
    @BindView(R.id.rv_result)
    RecyclerView mRvResult;
    @BindView(R.id.tv_msg)
    TextView mTvMsg;

    SearchView mSearchView;

    private SearchResultAdapter mMusicListAdapter;

    public SearchSuggestLayout(Context context) {
        this(context, null);
    }

    public SearchSuggestLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchSuggestLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SearchSuggestLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_search_suggest, this, true);
        ButterKnife.bind(this, view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRvResult.setLayoutManager(layoutManager);
        mRvResult.addItemDecoration(new DividerDecoration(
                new ColorDrawable(getResources().getColor(R.color.gray_light)),
                (int) Math.max(Dp2Px.toPX(getContext(), 0.5f), 1),
                Dp2Px.toPX(getContext(), 16)));

        mMusicListAdapter = new SearchResultAdapter();
        mRvResult.setAdapter(mMusicListAdapter);
    }

    private String mBelongTo;

    public void setSheet(String belongTo) {
        mBelongTo = belongTo;
    }

    public void setOnItemChosenListener(BaseRecyclerViewAdapter.OnItemClickListener<BMusic> listener) {
        mMusicListAdapter.setOnItemClickListener(listener);
    }

    public void bindSearchView(SearchView searchView) {
        if (searchView == null) {
            throw new NullPointerException("SearchView不能为null");
        }
        mSearchView = searchView;

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchAndShow();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                removeCallbacks(mSearch);
                postDelayed(mSearch, 100);
                return true;
            }
        });

        mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (mParent == null) return;
                if (hasFocus) {
                    mParent.addView(SearchSuggestLayout.this);
                    if (mAnchor != null) {
                        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) SearchSuggestLayout.this.getLayoutParams();
                        params.topMargin = mAnchor.getBottom();
                        SearchSuggestLayout.this.setLayoutParams(params);
                    }
                } else {
                    mParent.removeView(SearchSuggestLayout.this);
                    mSearchView.setIconified(true);
                }
            }
        });
    }

    ViewGroup mParent;

    public void setParent(ViewGroup viewGroup) {
        mParent = viewGroup;
    }

    View mAnchor;

    public void setAnchor(View anchor) {
        mAnchor = anchor;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(mSearch);
    }

    Runnable mSearch = new Runnable() {
        @Override
        public void run() {
            searchAndShow();
        }
    };

    private void searchAndShow() {
        String query = mSearchView.getQuery().toString().trim();
        if (TextUtils.isEmpty(query)) {
            mMusicListAdapter.setData(null);
            mTvMsg.setText("");
            return;
        }

        List<BMusic> bMusics = MusicManager.getInstance().searchMusic(query, mBelongTo);

        mMusicListAdapter.setKey(query);
        mMusicListAdapter.setData(bMusics);

        if (bMusics == null || bMusics.size() == 0) {
            SpannableString spannableString = SpannableString.valueOf("没有包含 " + query + " 的歌曲");
            ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(getContext().getResources().getColor(R.color.blue_a700));
            spannableString.setSpan(foregroundColorSpan, 5, 5 + query.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            mTvMsg.setText(spannableString);
        } else {
            mTvMsg.setText("");
        }
    }

    public static class Builder {
        private final SearchSuggestLayout mSuggestLayout;

        ViewGroup mParent;
        SearchView mSearchView;
        View mAnchor;

        public Builder(Context context) {
            mSuggestLayout = new SearchSuggestLayout(context);
        }

        public Builder setOnItemChosenListener(BaseRecyclerViewAdapter.OnItemClickListener<BMusic> listener) {
            mSuggestLayout.setOnItemChosenListener(listener);
            return this;
        }

        public Builder setSheet(String belongTo) {
            mSuggestLayout.setSheet(belongTo);
            return this;
        }

        public Builder setParent(ViewGroup viewGroup) {
            mParent = viewGroup;
            return this;
        }

        public Builder bindSearchView(SearchView searchView) {
            mSearchView = searchView;
            return this;
        }

        public Builder setAnchor(View anchor) {
            mAnchor = anchor;
            return this;
        }

        public SearchSuggestLayout build() {
            mSuggestLayout.setParent(mParent);
            mSuggestLayout.setAnchor(mAnchor);
            mSuggestLayout.bindSearchView(mSearchView);
            return mSuggestLayout;
        }
    }
}
