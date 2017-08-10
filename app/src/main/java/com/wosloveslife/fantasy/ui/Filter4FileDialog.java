package com.wosloveslife.fantasy.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.wosloveslife.fantasy.R;
import com.wosloveslife.fantasy.adapter.FileFilterAdapter;
import com.wosloveslife.fantasy.dao.bean.BFolder;
import com.wosloveslife.fantasy.manager.SettingConfig;
import com.yesing.blibrary_wos.utils.assist.Toaster;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by zhangh on 2017/2/9.
 */

public class Filter4FileDialog extends DialogFragment {

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    private FileFilterAdapter mAdapter;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_choice_file_filtrate, null);
        ButterKnife.bind(this, view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new FileFilterAdapter();
        mRecyclerView.setAdapter(mAdapter);
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(null)
                .setView(view)
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                init();
            }
        });
        return dialog;
    }

    private void init() {
        List<BFolder> filter = SettingConfig.getFolders();
        if (filter == null) {
            Toaster.showShort("请等待播放器初始化完毕");
            getDialog().dismiss();
        } else if (filter.size() == 0) {
            Toaster.showShort("没有任何包含歌曲的文件夹");
            getDialog().dismiss();
        } else {
            mAdapter.setData(filter);
        }
    }

    @OnClick({R.id.btn_cancel, R.id.btn_submit})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_cancel:
                getDialog().dismiss();
                break;
            case R.id.btn_submit:
                SettingConfig.saveFolders(mAdapter.getNormalDataList());
                getDialog().dismiss();
                break;
        }
    }
}
