package com.wosloveslife.fantasy.ui.swapablenavigation.navi_holders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.orhanobut.logger.Logger;
import com.wosloveslife.fantasy.R;
import com.wosloveslife.fantasy.manager.SettingConfig;
import com.wosloveslife.fantasy.ui.swapablenavigation.navi_item.NaviSettingItem;
import com.wosloveslife.fantasy.utils.FormatUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NaviSettingItemHolder extends BaseNaviItemHolder<NaviSettingItem> {
    @BindView(R.id.fl_rootView)
    FrameLayout mFlRootView;
    @BindView(R.id.iv_icon)
    ImageView mIvIcon;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.tv_accessory)
    TextView mTvAccessory;

    public int mType;
    public int mGroup;

    public NaviSettingItemHolder(ViewGroup parent) {
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
    public void onBind(final NaviSettingItem navigationItem, final int position) {
        super.onBind(navigationItem, position);
        mType = navigationItem.mType;
        mGroup = navigationItem.mGroup;
        mIvIcon.setImageResource(navigationItem.mIcon);
        mTvTitle.setText(navigationItem.mTitle);

        mFlRootView.setBackgroundResource(R.drawable.ripple_gray_white_bg);

        if (navigationItem.mItem == NaviSettingItem.Item.ITEM_COUNTDOWN_TIMER) {
            mTvAccessory.setTag("countdown");
            updateCountdownTime();
        } else {
            mTvAccessory.setTag(null);
        }
    }

    public void updateCountdownTime() {
        mTvAccessory.removeCallbacks(mCountDownTask);
        if (SettingConfig.getCountdownTime() > 0 && !SettingConfig.isCountdown()) {
            mTvAccessory.setText(FormatUtils.stringForTime(SettingConfig.getCountdownTime() - System.currentTimeMillis()));
            mTvAccessory.postDelayed(mCountDownTask, System.currentTimeMillis() % 1000);
            mTvAccessory.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                    mTvAccessory.removeOnAttachStateChangeListener(this);
                    mTvAccessory.removeCallbacks(mCountDownTask);
                }
            });
        }
        tick();
    }

    private void tick() {
        if (SettingConfig.getCountdownTime() <= 0) {
            mTvAccessory.removeCallbacks(mCountDownTask);
            mTvAccessory.setVisibility(View.GONE);
            return;
        }

        mTvAccessory.setVisibility(View.VISIBLE);
        if (SettingConfig.isCountdown() && SettingConfig.isCloseAfterPlayEnd()) {
            mTvAccessory.setText("播完后停止");
            mTvAccessory.removeCallbacks(mCountDownTask);
        } else {
            long l = SettingConfig.getCountdownTime() - System.currentTimeMillis();
            if (l < 0) {
                mTvAccessory.removeCallbacks(mCountDownTask);
                mTvAccessory.setVisibility(View.GONE);
                SettingConfig.saveCountdownTime(0);
            } else {
                mTvAccessory.setText(FormatUtils.stringForTime(l));
                mTvAccessory.postDelayed(mCountDownTask, 1000);
            }
        }
    }

    private Runnable mCountDownTask = new Runnable() {
        @Override
        public void run() {
            tick();
            Logger.w("time = " + System.currentTimeMillis());
        }
    };
}