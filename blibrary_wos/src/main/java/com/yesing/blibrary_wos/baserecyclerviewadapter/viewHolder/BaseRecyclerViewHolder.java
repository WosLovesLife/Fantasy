package com.yesing.blibrary_wos.baserecyclerviewadapter.viewHolder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * 基础ViewHolder,继承该ViewHolder, 实现相关方法
 * Created by WosLovesLife on 2016/7/13.
 */
public abstract class BaseRecyclerViewHolder<T> extends RecyclerView.ViewHolder {
    protected View mView;

    public BaseRecyclerViewHolder(View itemView) {
        super(itemView);

        mView = itemView;

        butterKnife(itemView);

        onCreateView(itemView);
    }

    protected void butterKnife(View itemView){
    }

    public void onCreateView(View view){
    }

    public abstract void onBind(T t, int position);

    public Context getContext(){
        return itemView.getContext();
    }
}