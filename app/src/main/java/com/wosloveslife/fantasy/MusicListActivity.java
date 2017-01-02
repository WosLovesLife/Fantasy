package com.wosloveslife.fantasy;

import android.support.v4.app.Fragment;

import base.SingleActivity;

public class MusicListActivity extends SingleActivity {

    @Override
    protected Fragment setFragment() {
        return MusicListFragment.newInstance();
    }

    @Override
    protected String setLabel() {
        hideActionBar();
        return "";
    }
}
