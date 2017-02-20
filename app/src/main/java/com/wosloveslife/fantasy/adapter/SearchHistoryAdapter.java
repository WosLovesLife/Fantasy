package com.wosloveslife.fantasy.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wosloveslife.fantasy.R;
import com.yesing.blibrary_wos.baserecyclerviewadapter.adapter.BaseRecyclerViewAdapter;
import com.yesing.blibrary_wos.baserecyclerviewadapter.viewHolder.BaseRecyclerViewHolder;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by zhangh on 2017/2/20.
 */

public class SearchHistoryAdapter extends BaseRecyclerViewAdapter<String> {

    private HistoryListener mHistoryListener;

    @Override
    protected BaseRecyclerViewHolder<String> onCreateItemViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_histroy, parent, false));
    }

    class Holder extends BaseRecyclerViewHolder<String> {
        @BindView(R.id.tv_log)
        TextView mTvLog;
        @BindView(R.id.iv_delete)
        ImageView mIvDelete;

        Holder(View itemView) {
            super(itemView);
        }

        @Override
        protected void butterKnife(View itemView) {
            super.butterKnife(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void onBind(final String s, final int position) {
            mTvLog.setText(s);
            mTvLog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mHistoryListener != null) {
                        mHistoryListener.onChosenItem(s, mTvLog, position);
                    }
                }
            });
            mIvDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mHistoryListener != null) {
                        mHistoryListener.onDeleteItem(s, mIvDelete, position);
                    }
                }
            });
        }
    }

    public interface HistoryListener {
        void onChosenItem(String item, View view, int position);

        void onDeleteItem(String item, View view, int position);
    }

    public void setHistoryListener(HistoryListener listener) {
        mHistoryListener = listener;
    }
}
