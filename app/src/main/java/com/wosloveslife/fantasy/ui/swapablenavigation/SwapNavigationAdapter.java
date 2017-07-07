package com.wosloveslife.fantasy.ui.swapablenavigation;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wosloveslife.fantasy.ui.swapablenavigation.navi_holders.NaviSettingItemHolder;
import com.wosloveslife.fantasy.ui.swapablenavigation.navi_holders.NaviSheetHolder;
import com.wosloveslife.fantasy.ui.swapablenavigation.navi_holders.SubHolder;
import com.wosloveslife.fantasy.ui.swapablenavigation.navi_item.BaseNaviItem;
import com.yesing.blibrary_wos.baserecyclerviewadapter.adapter.BaseRecyclerViewAdapter;
import com.yesing.blibrary_wos.baserecyclerviewadapter.viewHolder.BaseRecyclerViewHolder;

import java.util.Collections;

/**
 * Created by zhangh on 2017/2/6.
 */

public class SwapNavigationAdapter extends BaseRecyclerViewAdapter<BaseNaviItem> implements ItemTouchHelperAdapter {

    TextView mTvCountDown;
    private int mChosenPosition = 1;

    @Override
    public int getItemViewType(int position) {
        int type = super.getItemViewType(position);
        return type != 0 ? type : mData.get(position - getHeadersCount()).mType;
    }

    @Override
    protected BaseRecyclerViewHolder<BaseNaviItem> onCreateItemViewHolder(ViewGroup parent, int viewType) {
        BaseRecyclerViewHolder holder;
        switch (viewType) {
            case BaseNaviItem.Type.TYPE_SHEET:
                holder = new NaviSheetHolder(parent);
                break;
            case BaseNaviItem.Type.TYPE_SETTING:
                holder = new NaviSettingItemHolder(parent);
                break;
            case BaseNaviItem.Type.TYPE_SUBTITLE:
                holder = new SubHolder(parent);
                break;
            default:
                holder = new NaviSettingItemHolder(parent);
                break;
        }
        return holder;
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition == mChosenPosition) {
            mChosenPosition = toPosition;
        } else if (toPosition == mChosenPosition) {
            mChosenPosition = fromPosition;
        }
        Collections.swap(mData, fromPosition - getHeadersCount(), toPosition - getHeadersCount());
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
    }

    @Override
    public void onBindViewHolder(final BaseRecyclerViewHolder<BaseNaviItem> holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemClick(mData.get(holder.getAdapterPosition() - getHeadersCount()), v, holder.getAdapterPosition());
            }
        });
    }

    public int getChosenPosition() {
        return mChosenPosition;
    }

    public void setChosenPosition(int chosenPosition) {
        if (chosenPosition == mChosenPosition) return;
        int position = mChosenPosition;
        mChosenPosition = chosenPosition;
        notifyItemChanged(position);
        notifyItemChanged(mChosenPosition);
    }
}
