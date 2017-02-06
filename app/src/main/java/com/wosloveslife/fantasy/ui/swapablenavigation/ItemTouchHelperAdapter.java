package com.wosloveslife.fantasy.ui.swapablenavigation;

/**
 * Created by zhangh on 2017/2/6.
 */

public interface ItemTouchHelperAdapter {
    void onItemMove(int fromPosition, int toPosition);

    void onItemDismiss(int position);
}
