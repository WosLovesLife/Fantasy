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
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.wosloveslife.fantasy.R;
import com.wosloveslife.fantasy.adapter.SearchResultAdapter;
import com.wosloveslife.fantasy.adapter.SubscriberAdapter;
import com.wosloveslife.fantasy.baidu.BaiduMusic;
import com.wosloveslife.fantasy.baidu.BaiduMusicInfo;
import com.wosloveslife.fantasy.baidu.BaiduSearch;
import com.wosloveslife.fantasy.bean.BMusic;
import com.wosloveslife.fantasy.manager.MusicManager;
import com.wosloveslife.fantasy.utils.DividerDecoration;
import com.wosloveslife.fantasy.utils.NetWorkUtil;
import com.yesing.blibrary_wos.baserecyclerviewadapter.adapter.BaseRecyclerViewAdapter;
import com.yesing.blibrary_wos.utils.assist.Toaster;
import com.yesing.blibrary_wos.utils.assist.WLogger;
import com.yesing.blibrary_wos.utils.screenAdaptation.Dp2Px;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

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
    private Subscription mSubscription;

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
        mMusicListAdapter.setOnItemClickListener(new BaseRecyclerViewAdapter.OnItemClickListener<BMusic>() {
            @Override
            public void onItemClick(final BMusic bMusic, final View v, final int position) {
                if (TextUtils.isEmpty(bMusic.path)) {
                    if (!NetWorkUtil.isNetWorkAvailable(getContext())) {
                        Toaster.showShort(getContext(), "网络貌似不可用哦~");
                        return;
                    }

                    if (mSubscription != null) {
                        return;
                    }

                    mSubscription = MusicManager.getInstance().getMusicInfoByNet(bMusic.songId)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new SubscriberAdapter<BaiduMusicInfo>() {
                                @Override
                                public void onNext(BaiduMusicInfo baiduMusicInfo) {
                                    super.onNext(baiduMusicInfo);
                                    if (baiduMusicInfo != null) {
                                        BaiduMusicInfo.BitrateBean bitrate = baiduMusicInfo.getBitrate();
                                        if (bitrate != null) {
                                            if (baiduMusicInfo.getSonginfo() != null) {
                                                bMusic.album = baiduMusicInfo.getSonginfo().getAlbum_title();
                                            }
                                            bMusic.path = bitrate.getFile_link();
                                            bMusic.size = bitrate.getFile_size();
                                            bMusic.duration = bitrate.getFile_duration();
                                            mMusicListAdapter.notifyItemChanged(position);
                                            if (mOnItemClickListener != null) {
                                                mOnItemClickListener.onItemClick(bMusic, v, position);
                                            }
                                        }
                                    }
                                    cancelNetSearching();
                                }

                                @Override
                                public void onError(Throwable e) {
                                    super.onError(e);
                                    cancelNetSearching();
                                    WLogger.w("onError : 从网络获取歌曲信息失败 ");
                                    Toaster.showShort(getContext(), "获取播放地址失败");
                                }
                            });
                } else {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(bMusic, v, position);
                    }
                }
            }
        });
        mRvResult.setAdapter(mMusicListAdapter);
    }

    private String mBelongTo;

    public void setSheet(String belongTo) {
        mBelongTo = belongTo;
    }

    ViewGroup mParent;

    public void setParent(ViewGroup viewGroup) {
        mParent = viewGroup;
    }

    View mAnchor;

    public void setAnchor(View anchor) {
        mAnchor = anchor;
    }

    BaseRecyclerViewAdapter.OnItemClickListener<BMusic> mOnItemClickListener;

    public void setOnItemChosenListener(BaseRecyclerViewAdapter.OnItemClickListener<BMusic> listener) {
        mOnItemClickListener = listener;
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

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(mSearch);
        cancelNetSearching();
    }

    Runnable mSearch = new Runnable() {
        @Override
        public void run() {
            searchAndShow();
        }
    };

    private void searchAndShow() {
        cancelNetSearching();

        final String query = mSearchView.getQuery().toString().trim();
        if (TextUtils.isEmpty(query)) {
            mMusicListAdapter.setData(null);
            mTvMsg.setText("");
            return;
        }

        List<BMusic> bMusics = MusicManager.getInstance().searchMusic(query, mBelongTo);

        mMusicListAdapter.setKey(query);
        mMusicListAdapter.setData(bMusics);

        if (bMusics == null || bMusics.size() == 0) {
            SpannableString spannableString = SpannableString.valueOf("没有包含 " + query + " 的歌曲\n点击 从网络曲库搜索 获取更多");
            ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(getContext().getResources().getColor(R.color.blue_a700));
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    Toaster.showShort(getContext(), "点击搜索");
                    searchByNet(query);
                }
            };
            int end = 5 + query.length();
            spannableString.setSpan(foregroundColorSpan, 5, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(clickableSpan, end + 8, end + 8 + 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            /** 不调用该方法无法应用点击事件 */
            mTvMsg.setMovementMethod(LinkMovementMethod.getInstance());
            mTvMsg.setText(spannableString);
        } else {
            mTvMsg.setText("");
        }
    }

    private void searchByNet(final String query) {
        if (!NetWorkUtil.isNetWorkAvailable(getContext())) {
            Toaster.showShort(getContext(), "网络貌似不可用哦~");
            return;
        }

        if (mSubscription != null) {
            return;
        }

        Toaster.showShort(getContext(), "正在搜索中...");

        mSubscription = MusicManager.getInstance().searchMusicByNet(query)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SubscriberAdapter<BaiduSearch>() {
                    @Override
                    public void onNext(BaiduSearch baiduSearch) {
                        super.onNext(baiduSearch);
                        if (baiduSearch != null) {
                            List<BaiduMusic> song = baiduSearch.getSong();
                            if (song != null && song.size() > 0) {
                                List<BMusic> bMusics = new ArrayList<>();
                                for (BaiduMusic music : song) {
                                    BMusic bMusic = new BMusic(null, music.getSongname(), music.getArtistname(), null, "", 0, 0, null, null, true);
                                    bMusic.songId = music.getSongid();
                                    bMusics.add(bMusic);
                                }

                                mTvMsg.setText("");

                                mMusicListAdapter.setData(bMusics);
                            } else {
                                SpannableString spannableString = SpannableString.valueOf("未找到包含 " + query + " 的歌曲");
                                ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(getContext().getResources().getColor(R.color.blue_a700));
                                spannableString.setSpan(foregroundColorSpan, 6, 6 + query.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                mTvMsg.setText(spannableString);
                            }
                        }

                        cancelNetSearching();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        cancelNetSearching();
                        WLogger.w("onError : 从网络搜索歌曲失败 ");
                    }
                });
    }

    private void cancelNetSearching() {
        if (mSubscription != null) {
            mSubscription.unsubscribe();
            mSubscription = null;
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
