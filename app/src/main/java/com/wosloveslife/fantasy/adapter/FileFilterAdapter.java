package com.wosloveslife.fantasy.adapter;

import android.support.v7.widget.AppCompatCheckBox;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.wosloveslife.dao.Sheet;
import com.wosloveslife.fantasy.R;
import com.wosloveslife.fantasy.utils.FormatUtils;
import com.yesing.blibrary_wos.baserecyclerviewadapter.adapter.BaseRecyclerViewAdapter;
import com.yesing.blibrary_wos.baserecyclerviewadapter.viewHolder.BaseRecyclerViewHolder;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by zhangh on 2017/2/9.
 */

public class FileFilterAdapter extends BaseRecyclerViewAdapter<Sheet> {

    @Override
    protected BaseRecyclerViewHolder<Sheet> onCreateItemViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_filter, parent, false));
    }

    class Holder extends BaseRecyclerViewHolder<Sheet> {
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
        public void onBind(final Sheet sheet, int position) {
            mTvFilePath.setText(FormatUtils.trimEnvironmentPath(sheet.path));
            mCheckbox.setChecked(sheet.state == Sheet.STATE_FILTERED);
            mCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    sheet.state = isChecked ? Sheet.STATE_FILTERED : Sheet.STATE_NORMAL;
                }
            });
        }
    }
}
