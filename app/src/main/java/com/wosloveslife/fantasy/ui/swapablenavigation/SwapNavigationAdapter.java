package com.wosloveslife.fantasy.ui.swapablenavigation;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.wosloveslife.fantasy.R;
import com.wosloveslife.fantasy.bean.NavigationItem;
import com.yesing.blibrary_wos.baserecyclerviewadapter.adapter.BaseRecyclerViewAdapter;
import com.yesing.blibrary_wos.baserecyclerviewadapter.viewHolder.BaseRecyclerViewHolder;

import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by zhangh on 2017/2/6.
 */

public class SwapNavigationAdapter extends BaseRecyclerViewAdapter<NavigationItem> implements ItemTouchHelperAdapter {

    @Override
    public int getItemViewType(int position) {
        int type = super.getItemViewType(position);
        return type != 0 ? type : mData.get(position - getHeadersCount()).type;
    }

    @Override
    protected BaseRecyclerViewHolder<NavigationItem> onCreateItemViewHolder(ViewGroup parent, int viewType) {
        BaseRecyclerViewHolder holder;
        switch (viewType) {
            case 2:
                holder = new DividerHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_divider, parent, false));
                break;
            case 3:
                holder = new SubHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_sub, parent, false));
                break;
            default:
                holder = new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nv_normal, parent, false));
                break;
        }
        return holder;
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mData, fromPosition - getHeadersCount(), toPosition - getHeadersCount());
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
    }

    public class Holder extends BaseRecyclerViewHolder<NavigationItem> {
        @BindView(R.id.fl_rootView)
        FrameLayout mFlRootView;
        @BindView(R.id.iv_icon)
        ImageView mIvIcon;
        @BindView(R.id.tv_title)
        TextView mTvTitle;

        public int mType;
        public int mGroup;

        Holder(View itemView) {
            super(itemView);
        }

        @Override
        protected void butterKnife(View itemView) {
            super.butterKnife(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void onBind(final NavigationItem navigationItem, final int position) {
            mType = navigationItem.type;
            mGroup = navigationItem.group;
            mIvIcon.setImageResource(navigationItem.mIcon);
            mTvTitle.setText(navigationItem.mTitle);
            mFlRootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(navigationItem, v, position);
                    }
                }
            });
        }
    }

    public class DividerHolder extends BaseRecyclerViewHolder<NavigationItem> {
        TextView mTextView;

        DividerHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView;
        }

        @Override
        public void onBind(NavigationItem navigationItem, int position) {
            mTextView.setText(navigationItem.mTitle);
        }
    }

    public class SubHolder extends BaseRecyclerViewHolder<NavigationItem> {
        TextView mTextView;

        SubHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView;
        }

        @Override
        public void onBind(NavigationItem navigationItem, int position) {
            mTextView.setText(navigationItem.mTitle);
        }
    }
}
