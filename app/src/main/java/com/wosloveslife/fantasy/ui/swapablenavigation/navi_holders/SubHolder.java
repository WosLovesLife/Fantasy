package com.wosloveslife.fantasy.ui.swapablenavigation.navi_holders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wosloveslife.fantasy.R;
import com.wosloveslife.fantasy.ui.swapablenavigation.navi_item.NaviSubTitleItem;

public class SubHolder extends BaseNaviItemHolder<NaviSubTitleItem> {
    private TextView mTextView;

    public SubHolder(ViewGroup parent) {
        super(generateView(parent));
        mTextView = (TextView) itemView;
    }

    private static View generateView(ViewGroup parent) {
        return LayoutInflater.from(parent.getContext()).inflate(R.layout.view_sub, parent, false);
    }

    @Override
    public void onBind(NaviSubTitleItem navigationItem, int position) {
        super.onBind(navigationItem, position);
        mTextView.setText(navigationItem.mTitle);
    }
}