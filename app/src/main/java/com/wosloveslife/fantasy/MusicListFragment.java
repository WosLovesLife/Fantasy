package com.wosloveslife.fantasy;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.wosloveslife.fantasy.adapter.MusicListAdapter;
import com.wosloveslife.fantasy.manager.MusicManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import base.BaseFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by zhangh on 2017/1/2.
 */
public class MusicListFragment extends BaseFragment {

    @BindView(R.id.iv_previous_btn)
    ImageView mIvPreviousBtn;
    @BindView(R.id.iv_play_btn)
    ImageView mIvPlayBtn;
    @BindView(R.id.iv_next_btn)
    ImageView mIvNextBtn;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    //=============
    private MusicListAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private Snackbar mSnackbar;

    public static MusicListFragment newInstance() {

        Bundle args = new Bundle();

        MusicListFragment fragment = new MusicListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected View setContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    public void initView() {
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MusicListAdapter();
        mRecyclerView.setAdapter(mAdapter);

        onRefreshChanged(new MusicManager.RefreshEventM(MusicManager.getInstance().isLoading()));
    }

    @Override
    protected void getData() {
        super.getData();

        mAdapter.setData(MusicManager.getInstance().getMusicList());
    }

    @OnClick({R.id.iv_previous_btn, R.id.iv_play_btn, R.id.iv_next_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_previous_btn:
                break;
            case R.id.iv_play_btn:
                break;
            case R.id.iv_next_btn:
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    //==========================================事件================================================
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshChanged(MusicManager.RefreshEventM event) {
        if (event.mRefreshing) {
            if (mSnackbar == null) {
                mSnackbar = Snackbar.make(mRecyclerView, "正在更新歌曲...", Snackbar.LENGTH_INDEFINITE);
            } else {
                mSnackbar.setText("正在更新歌曲...");
                mSnackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
            }

            mSnackbar.show();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGotMusic(MusicManager.OnGotMusicEvent event) {
        if (event == null || event.mBMusicList == null) return;

        mAdapter.setData(event.mBMusicList);

        if (mSnackbar == null) {
            mSnackbar = Snackbar.make(mRecyclerView, "找到了" + event.mBMusicList.size() + "首音乐", Snackbar.LENGTH_INDEFINITE);
        } else {
            mSnackbar.setText("找到了" + event.mBMusicList.size() + "首音乐");
            mSnackbar.setDuration(Snackbar.LENGTH_SHORT);
        }

        mSnackbar.show();
    }
}
