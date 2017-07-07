package com.wosloveslife.fantasy.ui.swapablenavigation.navi_holders;

import android.view.View;

import com.wosloveslife.fantasy.ui.swapablenavigation.navi_item.BaseNaviItem;
import com.yesing.blibrary_wos.baserecyclerviewadapter.viewHolder.BaseRecyclerViewHolder;

/**
 * Created by zhangh on 2017/6/29.
 */

public class BaseNaviItemHolder<T extends BaseNaviItem> extends BaseRecyclerViewHolder<T> {
    public T mItem;

    public BaseNaviItemHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void onBind(final T t, final int position) {
        mItem = t;
    }
}
