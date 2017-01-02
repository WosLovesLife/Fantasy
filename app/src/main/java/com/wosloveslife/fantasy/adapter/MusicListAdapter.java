package com.wosloveslife.fantasy.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wosloveslife.fantasy.R;
import com.wosloveslife.fantasy.bean.BMusic;
import com.yesing.blibrary_wos.baserecyclerviewadapter.adapter.BaseRecyclerViewAdapter;
import com.yesing.blibrary_wos.baserecyclerviewadapter.viewHolder.BaseRecyclerViewHolder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by zhangh on 2017/1/2.
 */
public class MusicListAdapter extends BaseRecyclerViewAdapter<BMusic> {

    int mPlayingIndex;

    @Override
    protected BaseRecyclerViewHolder<BMusic> onCreateItemViewHolder(ViewGroup parent) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_music, parent, false));
    }

    class Holder extends BaseRecyclerViewHolder<BMusic> {
        @BindView(R.id.tv_id)
        TextView mTvId;
        @BindView(R.id.tv_title)
        TextView mTvTitle;
        @BindView(R.id.tv_artist)
        TextView mTvArtist;
        @BindView(R.id.iv_state)
        ImageView mIvState;

        //=================
        private int mPosition;

        public Holder(View itemView) {
            super(itemView);

        }

        @Override
        protected void butterKnife(View itemView) {
            super.butterKnife(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void onBind(BMusic music, int position) {
            mPosition = position;
            mTvId.setText(String.valueOf(mPosition + 1));
            mTvTitle.setText(music.title);
            mTvArtist.setText(music.artist);

            if (music.playState == 0) {
                mIvState.setVisibility(View.INVISIBLE);
            } else {
                mIvState.setVisibility(View.VISIBLE);
                if (music.playState == 1) {
                    mIvState.setImageResource(R.drawable.ic_volume_up);
                } else {
                    mIvState.setImageResource(R.drawable.ic_volume_mute);
                }
                mPlayingIndex = position;
            }
        }

        @OnClick({R.id.iv_more_btn, R.id.fl_root_view})
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.iv_more_btn:
                    break;
                case R.id.fl_root_view:
                    if (mPlayingIndex == mPosition) {
                        int playState = mData.get(mPlayingIndex).playState;
                        if (playState == 1) {
                            mData.get(mPlayingIndex).playState = 2;
                        } else {
                            mData.get(mPlayingIndex).playState = 1;
                        }
                        notifyItemChanged(mPlayingIndex);
                        return;
                    }
                    mData.get(mPlayingIndex).playState = 0;
                    mData.get(mPosition).playState = 1;
                    notifyItemChanged(mPlayingIndex);
                    notifyItemChanged(mPosition);
                    break;
            }
        }
    }
}
