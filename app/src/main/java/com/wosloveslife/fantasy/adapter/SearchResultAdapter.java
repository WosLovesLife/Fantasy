package com.wosloveslife.fantasy.adapter;

import android.graphics.Bitmap;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wosloveslife.dao.Audio;
import com.wosloveslife.fantasy.R;
import com.wosloveslife.fantasy.manager.MusicManager;
import com.yesing.blibrary_wos.baserecyclerviewadapter.adapter.BaseRecyclerViewAdapter;
import com.yesing.blibrary_wos.baserecyclerviewadapter.viewHolder.BaseRecyclerViewHolder;
import com.yesing.blibrary_wos.utils.screenAdaptation.Dp2Px;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * Created by zhangh on 2017/2/21.
 */

public class SearchResultAdapter extends BaseRecyclerViewAdapter<Audio> {

    private int mAlbumSize;
    private ForegroundColorSpan mForegroundColorSpan;

    public SearchResultAdapter() {

    }

    @Override
    protected BaseRecyclerViewHolder<Audio> onCreateItemViewHolder(ViewGroup parent, int viewType) {
        if (mForegroundColorSpan == null) {
            mAlbumSize = Dp2Px.toPX(parent.getContext(), 40);
            mForegroundColorSpan = new ForegroundColorSpan(parent.getContext().getResources().getColor(R.color.blue_a700));
        }
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false));
    }

    class Holder extends BaseRecyclerViewHolder<Audio> {
        @BindView(R.id.iv_album)
        ImageView mIvAlbum;
        @BindView(R.id.iv_more_btn)
        ImageView mIvMoreBtn;
        @BindView(R.id.tv_title)
        TextView mTvTitle;
        @BindView(R.id.iv_state)
        ImageView mIvState;
        @BindView(R.id.iv_source)
        ImageView mIvSource;
        @BindView(R.id.tv_artist)
        TextView mTvArtist;
        @BindView(R.id.fl_root_view)
        RelativeLayout mFlRootView;

        public Holder(View itemView) {
            super(itemView);
        }

        @Override
        protected void butterKnife(View itemView) {
            super.butterKnife(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void onBind(final Audio bMusic, final int position) {
            mTvTitle.setText(hasKeyWord(bMusic.getTitle()));
            mTvArtist.setText(hasKeyWord(bMusic.getArtist()));

            mIvAlbum.setVisibility(View.INVISIBLE);
            MusicManager.getInstance()
                    .getAlbum(bMusic.getId(), mAlbumSize)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SubscriberAdapter<Bitmap>() {
                        @Override
                        public void onNext(Bitmap bitmap) {
                            super.onNext(bitmap);
                            if (bitmap != null) {
                                mIvAlbum.setVisibility(View.VISIBLE);
                                mIvAlbum.setImageBitmap(bitmap);
                            }
                        }
                    });

            mFlRootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(bMusic, v, position);
                    }
                }
            });

            mIvMoreBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

        private CharSequence hasKeyWord(String word) {
            String temp = word.toUpperCase();
            if (temp.contains(mKey)) {
                int start = temp.indexOf(mKey);
                SpannableString spannableString = new SpannableString(word);
                spannableString.setSpan(mForegroundColorSpan, start, start + mKey.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                return spannableString;
            }
            return word;
        }
    }

    private String mKey;

    public void setKey(String key) {
        if (!TextUtils.isEmpty(key)) {
            mKey = key.toUpperCase();
        }
    }
}
