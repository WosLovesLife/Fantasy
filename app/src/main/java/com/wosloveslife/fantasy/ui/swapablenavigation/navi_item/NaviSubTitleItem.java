package com.wosloveslife.fantasy.ui.swapablenavigation.navi_item;

/**
 * Created by zhangh on 2017/2/6.
 */

public class NaviSubTitleItem extends BaseNaviItem {
    public String mTitle;

    public NaviSubTitleItem(String title) {
        super(Type.TYPE_SUBTITLE, -1);
        mTitle = title;
    }
}
