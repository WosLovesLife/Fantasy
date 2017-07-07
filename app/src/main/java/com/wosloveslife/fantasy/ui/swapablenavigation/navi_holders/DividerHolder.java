package com.wosloveslife.fantasy.ui.swapablenavigation.navi_holders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wosloveslife.fantasy.R;
import com.wosloveslife.fantasy.ui.swapablenavigation.navi_item.BaseNaviItem;

public class DividerHolder extends BaseNaviItemHolder<BaseNaviItem> {
    public DividerHolder(ViewGroup parent) {
        super(generateView(parent));
    }

    private static View generateView(ViewGroup parent) {
        return LayoutInflater.from(parent.getContext()).inflate(R.layout.view_divider, parent, false);
    }

    @Override
    public void onBind(BaseNaviItem baseNaviItem, int position) {
        super.onBind(baseNaviItem, position);
    }
}