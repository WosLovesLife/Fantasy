package com.wosloveslife.fantasy.ui.swapablenavigation.navi_item;

import com.wosloveslife.dao.Sheet;

/**
 * Created by zhangh on 2017/2/6.
 */

public class NaviSheetItem extends BaseNaviItem {
    public int mIcon;
    public Sheet mSheet;

    public NaviSheetItem(int group, int icon, Sheet sheet) {
        super(Type.TYPE_SHEET, group);
        mIcon = icon;
        mSheet = sheet;
    }
}
