package com.wosloveslife.fantasy.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import base.SingleActivity;

/**
 * Created by zhangh on 2017/2/8.
 */

public class SettingActivity extends SingleActivity {
    public static Intent newStartIntent(Context context) {
        return new Intent(context, SettingActivity.class);
    }

    @Override
    protected Fragment setFragment() {
        return SettingFragment.newInstance();
    }

    @Override
    protected String setLabel() {
        return "设置";
    }
}
