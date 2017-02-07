package com.wosloveslife.fantasy.ui.swapablenavigation;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.yesing.blibrary_wos.utils.assist.WLogger;

/**
 * Created by zhangh on 2017/2/6.
 */

public class VerticalSwapItemTouchHelperCallBack extends ItemTouchHelper.Callback {
    private final ItemTouchHelperAdapter mAdapter;

    public VerticalSwapItemTouchHelperCallBack(ItemTouchHelperAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    /**
     * 在条目被点击时回调, 决定该条目是否支持Drag/Swipe
     *
     * @param recyclerView ItemTouchHelper依附的RecyclerView
     * @param viewHolder   被点击的Item
     * @return 支持的操作
     */
    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int flag = 0;
        if (viewHolder instanceof SwapNavigationAdapter.Holder && ((SwapNavigationAdapter.Holder) viewHolder).mType == 1) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            flag = makeMovementFlags(dragFlags, 0);
        }
        return flag;
    }

    /**
     * 如果某一条支持Drag的Item(见getMovementFlags()方法)将要和某一目标条目交互(这里我们用来交换条目的顺序)时调用<br/>
     *
     * @param recyclerView ItemTouchHelper依附的RecyclerView
     * @param viewHolder   当前触摸的条目
     * @param target       将要互动的条目
     * @return 如果我们将要进行一些操作就返回true, 否则返回false. 这里如果目标条目的Holder是SwapHolder说明支持交互顺序
     */
    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        if (viewHolder instanceof SwapNavigationAdapter.Holder && target instanceof SwapNavigationAdapter.Holder
                && ((SwapNavigationAdapter.Holder) target).mType == 1) {
            int group = ((SwapNavigationAdapter.Holder) viewHolder).mGroup;
            int tGroup = ((SwapNavigationAdapter.Holder) target).mGroup;
            if (group == tGroup) {
                mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }
        }
        return false;
    }

    /**
     * onMove()方法执行后如果为true 则在该方法回调条目的变化
     *
     * @param recyclerView
     * @param viewHolder
     * @param fromPos
     * @param target
     * @param toPos
     * @param x
     * @param y
     */
    @Override
    public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                        int fromPos, RecyclerView.ViewHolder target, int toPos, int x, int y) {
        super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
        WLogger.d("onSwiped : [viewHolder, direction] = " + direction);
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
        WLogger.d("onSelectedChanged : [viewHolder, actionState] = " + actionState);
    }
}
