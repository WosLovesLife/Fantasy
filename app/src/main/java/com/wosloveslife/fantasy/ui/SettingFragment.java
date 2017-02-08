package com.wosloveslife.fantasy.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.wosloveslife.fantasy.R;
import com.wosloveslife.fantasy.manager.CustomConfiguration;

import base.BaseFragment;
import butterknife.BindView;

/**
 * Created by zhangh on 2017/2/8.
 */

public class SettingFragment extends BaseFragment {
    @BindView(R.id.switch_autoExpand)
    SwitchCompat mSwitchAutoExpand;

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
    }
}
