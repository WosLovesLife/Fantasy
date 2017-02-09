package com.wosloveslife.fantasy.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.wosloveslife.fantasy.R;
import com.wosloveslife.fantasy.manager.CustomConfiguration;
import com.wosloveslife.fantasy.manager.MusicManager;
import com.yesing.blibrary_wos.utils.assist.Toaster;

import base.BaseFragment;
import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by zhangh on 2017/2/8.
 */

public class SettingFragment extends BaseFragment {
    private static final int REQ_CODE_MIN_DURATION = 0;

    @BindView(R.id.switch_autoExpand)
    SwitchCompat mSwitchAutoExpand;
    @BindView(R.id.tv_filterDuration)
    TextView mTvFilterDuration;

    public static SettingFragment newInstance() {

        Bundle args = new Bundle();

        SettingFragment fragment = new SettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected View setContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    protected void initView() {
        super.initView();

        mSwitchAutoExpand.setChecked(CustomConfiguration.isPlayControllerAutoExpand());
        mSwitchAutoExpand.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                CustomConfiguration.savePlayControllerAutoExpand(isChecked);
            }
        });

        setMinDurationContent(CustomConfiguration.getMinDuration());
    }

    @OnClick({R.id.tv_filterDuration, R.id.tv_fileFilter, R.id.tv_rescan})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_filterDuration:
                Filter4DurationDialog filter4DurationDialog = new Filter4DurationDialog();
                filter4DurationDialog.setTargetFragment(this, REQ_CODE_MIN_DURATION);
                filter4DurationDialog.show(getChildFragmentManager(), "durationFilter");
                break;
            case R.id.tv_fileFilter:
                Filter4FileDialog dialog = new Filter4FileDialog();
                dialog.show(getChildFragmentManager(), "fileFilter");
                break;
            case R.id.tv_rescan:
                new AlertDialog.Builder(getActivity())
                        .setTitle("确定要重新扫描吗？")
                        .setMessage("目前如果重新扫描, 将丢失掉之前所有关于歌曲的配置信息. 例如\"我的收藏\"中的数据")
                        .setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MusicManager.getInstance().scanMusic();
                                Toaster.showShort(getActivity(), "返回主页看看吧~");
                            }
                        })
                        .setNegativeButton("算了", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;
        switch (requestCode) {
            case REQ_CODE_MIN_DURATION:
                setMinDurationContent(Filter4DurationDialog.getMinDurationSecond(data));
                break;
        }
    }

    private void setMinDurationContent(int second) {
        String text;
        if (second > 0) {
            int minute = second / 60;
            second = second % 60;
            if (minute > 0) {
                text = minute + "分钟" + second + "秒";
            } else {
                text = second + "秒";
            }
            text = "过滤小于 " + text + " 的文件";
        } else {
            text = "不过滤歌曲时长";
        }
        mTvFilterDuration.setText(text);
    }
}
