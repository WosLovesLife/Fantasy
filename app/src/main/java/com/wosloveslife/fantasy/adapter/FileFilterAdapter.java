package com.wosloveslife.fantasy.adapter;

import android.support.v7.widget.AppCompatCheckBox;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.wosloveslife.fantasy.R;
import com.wosloveslife.fantasy.bean.BFolder;
import com.wosloveslife.fantasy.utils.FormatUtils;
import com.yesing.blibrary_wos.baserecyclerviewadapter.adapter.BaseRecyclerViewAdapter;
import com.yesing.blibrary_wos.baserecyclerviewadapter.viewHolder.BaseRecyclerViewHolder;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by zhangh on 2017/2/9.
 */

public class FileFilterAdapter extends BaseRecyclerViewAdapter<BFolder> {

    @Override
    protected BaseRecyclerViewHolder<BFolder> onCreateItemViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_filter, parent, false));
    }

    class Holder extends BaseRecyclerViewHolder<BFolder> {
        @BindView(R.id.tv_file_path)
        TextView mTvFilePath;
        @BindView(R.id.checkbox)
        AppCompatCheckBox mCheckbox;

        public Holder(View itemView) {
            super(itemView);
        }

        @Override
        protected void butterKnife(View itemView) {
            super.butterKnife(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void onBind(final BFolder folder, int position) {
            mTvFilePath.setText(FormatUtils.trimEnvironmentPath(folder.getFilePath()));
            mCheckbox.setChecked(folder.getIsFiltered());
            mCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    folder.setIsFiltered(isChecked);
                }
            });
        }
    }
}
