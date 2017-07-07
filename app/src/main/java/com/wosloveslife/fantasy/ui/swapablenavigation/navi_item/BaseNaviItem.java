package com.wosloveslife.fantasy.ui.swapablenavigation.navi_item;

import android.support.annotation.IntDef;

import static com.wosloveslife.fantasy.ui.swapablenavigation.navi_item.BaseNaviItem.Type.TYPE_DIVIDER;
import static com.wosloveslife.fantasy.ui.swapablenavigation.navi_item.BaseNaviItem.Type.TYPE_SETTING;
import static com.wosloveslife.fantasy.ui.swapablenavigation.navi_item.BaseNaviItem.Type.TYPE_SHEET;
import static com.wosloveslife.fantasy.ui.swapablenavigation.navi_item.BaseNaviItem.Type.TYPE_SUBTITLE;

/**
 * Created by zhangh on 2017/2/6.
 */

public abstract class BaseNaviItem {
    /** 0:默认歌单,1:设置项,2:分割线,3:subTitle */
    @IntDef({TYPE_SHEET, TYPE_SETTING, TYPE_DIVIDER, TYPE_SUBTITLE})
    public @interface Type {
        int TYPE_SHEET = 0;
        int TYPE_SETTING = 1;
        int TYPE_DIVIDER = 2;
        int TYPE_SUBTITLE = 3;
    }

    @Type
    public int mType;
    /** 条目所属组,同组之间可以交换位置,不同组不能相互调换位置, <=0 时不可移动 */
    public int mGroup;

    public BaseNaviItem(@Type int type, int group) {
        this.mType = type;
        this.mGroup = group;
    }
}
