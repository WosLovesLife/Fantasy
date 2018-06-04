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
    @BindView(R.id.switch_changeSheetWithPlayList)
    SwitchCompat mSwitchChangeSheetWithPlayList;
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

//        mSwitchChangeSheetWithPlayList.setChecked(CustomConfiguration.isChangeSheetWithPlayList());
//        mSwitchChangeSheetWithPlayList.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                CustomConfiguration.saveChangeSheetWithPlayList(isChecked);
//            }
//        });

        setMinDurationContent(CustomConfiguration.getMinDuration());
    }

    @OnClick({R.id.tv_filterDuration, R.id.tv_fileFilter, R.id.tv_rescan,
            R.id.fl_setAutoExpand, R.id.fl_changeSheetWithPlayList,
            R.id.iv_setAutoExpand_help, R.id.iv_changeSheetWithPlayList_help})
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
                        .setMessage("重新扫描将丢失之前对于“本地音乐”中的设定，例如歌曲顺序等，但不会影响其它歌单的数据。")
                        .setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MusicManager.Companion.getInstance().scanMusic();
                                Toaster.showShort("返回主页看看吧~");
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
            case R.id.fl_setAutoExpand:
                mSwitchAutoExpand.setChecked(!mSwitchAutoExpand.isChecked());
                break;
            case R.id.iv_setAutoExpand_help:
                new AlertDialog.Builder(getActivity())
                        .setTitle("这是什么？")
                        .setMessage("   首页头部的“Fantasy Panel”是可以下拉展开的哦~" +
                                "\n\n   展开后会显示歌词等额外信息和功能。" +
                                "\n\n   开启后：滑动歌曲列表时会自动展开/收起Fantasy Panel")
                        .setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();
                break;
            case R.id.fl_changeSheetWithPlayList:
                mSwitchChangeSheetWithPlayList.setChecked(!mSwitchChangeSheetWithPlayList.isChecked());
                break;
            case R.id.iv_changeSheetWithPlayList_help:
                new AlertDialog.Builder(getActivity())
                        .setTitle("这是什么？")
                        .setMessage("   大多播放器歌单和播放列表是分离的，你可以切换浏览不同的歌单而不会改变当前的播放列表，只有点击了歌单中的一首歌，播放列表才会被切换。" +
                                "\n\n    由于Fantasy中列表和播放页面是整合在一起的，你可能会忘记自己当前停留在哪个歌单，这时如果播放的歌曲来自于其它歌单，可能会造成一些困扰。" +
                                "\n\n    因此这里额外提供了一个辅助功能，开启后：切换歌单既切换播放列表（不会打断当前的播放）。" +
                                "\n\n    可以试试哦~ ")
                        .setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
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
