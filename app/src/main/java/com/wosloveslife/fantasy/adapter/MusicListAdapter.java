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

    int mPlayingIndex = -1;
    boolean mPlaying;

    @Override
    protected BaseRecyclerViewHolder<BMusic> onCreateItemViewHolder(ViewGroup parent) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_music, parent, false));
    }

    /**
     * 获取当前正在播放的歌曲所在的position<br/>
     * 注意: 获取当前position应该只作为对比的参考,不应该依据该position作为真实播放的歌曲的索引
     *
     * @return 当前歌曲的索引
     */
    public int getPlayingIndex() {
        return mPlayingIndex;
    }

    public void setChosenItem(int position, boolean playing) {
        if (position < 0 || position >= getRealItemCount()) return;

        if (mPlayingIndex != position) {
            mPlaying = playing;
            int notice = mPlayingIndex;
            mPlayingIndex = position;
            notifyItemChanged(notice);
            notifyItemChanged(position);
        } else if (!mPlaying) {
            mPlaying = true;
            notifyItemChanged(mPlayingIndex);
        }
    }

    /**
     * 设置播放当前播放的歌曲在条目中的显示<br/>
     * 如果position不等于当前记录的歌曲,则切换显示<br/>
     * 如果等于但是当前歌曲为暂停状态,则切换成播放状态
     *
     * @param position 新的position
     */
    public void setPlayItem(int position) {
        if (position < 0 || position >= getRealItemCount()) return;
        setChosenItem(position, true);
    }

    /**
     * 切换播放条目的显示状态
     *
     * @param play true 显示播放图标,false 显示暂停图标
     */
    public void togglePlay(boolean play) {
        if (mPlayingIndex < 0 || mPlayingIndex >= getRealItemCount()) return;

        if (play != mPlaying) {
            mPlaying = play;
            notifyItemChanged(mPlayingIndex);
        }
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
        private BMusic mMusic;

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
            mMusic = music;
            mPosition = position;

            mTvId.setText(String.valueOf(mPosition + 1));
            mTvTitle.setText(music.title);
            mTvArtist.setText(music.artist);

            if (position != mPlayingIndex) {
                mIvState.setVisibility(View.INVISIBLE);
            } else {
                mIvState.setVisibility(View.VISIBLE);
                if (mPlaying) {
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
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(mMusic, null, mPosition);
                    }
                    break;
            }
        }
    }
}
