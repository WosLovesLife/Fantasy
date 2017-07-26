package com.wosloveslife.fantasy.dao.bean;

import android.support.annotation.DrawableRes;

/**
 * Created by zhangh on 2017/2/6.
 */

public class NavigationItem {
    /** 0:固定的条目,不可改变位置,1:可交互位置的条目,2:分割线,3:subTitle */
    public int type;
    /** 条目所属组,同组之间可以交换位置,不同组不能 */
    public int group;
    @DrawableRes
    public int mIcon;
    public String mTitle;

    public NavigationItem() {
    }

    public NavigationItem(int type, int icon, String title) {
        this.type = type;
        mIcon = icon;
        mTitle = title;
    }

    public NavigationItem(int type, int group, int icon, String title) {
        this.type = type;
        this.group = group;
        mIcon = icon;
        mTitle = title;
    }
}
