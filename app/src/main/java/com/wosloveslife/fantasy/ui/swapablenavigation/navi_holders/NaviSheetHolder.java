package com.wosloveslife.fantasy.ui.swapablenavigation.navi_holders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.wosloveslife.fantasy.R;
import com.wosloveslife.fantasy.ui.swapablenavigation.navi_item.NaviSheetItem;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by zhangh on 2017/6/29.
 */

public class NaviSheetHolder extends BaseNaviItemHolder<NaviSheetItem> {
    @BindView(R.id.fl_rootView)
    FrameLayout mFlRootView;
    @BindView(R.id.iv_icon)
    ImageView mIvIcon;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.tv_accessory)
    TextView mTvAccessory;

    public NaviSheetHolder(ViewGroup parent) {
        super(generateView(parent));
    }

    private static View generateView(ViewGroup parent) {
        return LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nv_normal, parent, false);
    }

    @Override
    protected void butterKnife(View itemView) {
        super.butterKnife(itemView);
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void onBind(final NaviSheetItem navigationItem, final int position) {
        super.onBind(navigationItem, position);
        mIvIcon.setImageResource(navigationItem.mIcon);
        mTvTitle.setText(navigationItem.mSheet.title);

        int chosenPosition = 0; // TODO: 2017/6/29 获取
        if (position == chosenPosition) {
            mFlRootView.setBackgroundResource(R.color.gray_press);
        } else {
            mFlRootView.setBackgroundResource(R.drawable.ripple_gray_white_bg);
        }
    }
}
