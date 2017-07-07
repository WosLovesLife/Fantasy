package com.wosloveslife.fantasy.ui.swapablenavigation.navi_item;

import android.support.annotation.IntDef;

import static com.wosloveslife.fantasy.ui.swapablenavigation.navi_item.NaviSettingItem.Item.ITEM_COUNTDOWN_TIMER;
import static com.wosloveslife.fantasy.ui.swapablenavigation.navi_item.NaviSettingItem.Item.ITEM_TO_SETTING;

/**
 * Created by zhangh on 2017/6/29.
 */

public class NaviSettingItem extends BaseNaviItem {

    @IntDef({ITEM_COUNTDOWN_TIMER, ITEM_TO_SETTING})
    public @interface Item {
        int ITEM_COUNTDOWN_TIMER = 0;
        int ITEM_TO_SETTING = 1;
    }

    public int mIcon;
    public String mTitle;
    @NaviSettingItem.Item
    public int mItem;

    public NaviSettingItem(int group, int icon, String title, int item) {
        super(Type.TYPE_SETTING, group);
        mIcon = icon;
        mTitle = title;
        mItem = item;
    }
}
