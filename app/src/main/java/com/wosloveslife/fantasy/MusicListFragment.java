package com.wosloveslife.fantasy;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orhanobut.logger.Logger;
import com.wosloveslife.fantasy.adapter.MusicListAdapter;
import com.wosloveslife.fantasy.bean.BMusic;
import com.wosloveslife.fantasy.manager.MusicManager;
import com.yesing.blibrary_wos.baserecyclerviewadapter.adapter.BaseRecyclerViewAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import base.BaseFragment;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by zhangh on 2017/1/2.
 */
public class MusicListFragment extends BaseFragment {
    @BindView(R.id.control_view)
    ControlView mControlView;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    private Snackbar mSnackbar;

    //=============
    private MusicListAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private PlayService.PlayBinder mPlayBinder;

    //=============
    BMusic mCurrentMusic;

    public static MusicListFragment newInstance() {

        Bundle args = new Bundle();

        MusicListFragment fragment = new MusicListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    //========================================生命周期-start========================================
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unbindService(mServiceConnection);
    }

    //========================================生命周期-end========================================

    @Override
    protected View setContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    public void initView() {
        initServiceBinder();

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new MusicListAdapter();
        mAdapter.setOnItemClickListener(new BaseRecyclerViewAdapter.OnItemClickListener<BMusic>() {
            @Override
            public void onItemClick(BMusic music, View v, int position) {
                if (mCurrentMusic != music) {
                    mCurrentMusic = music;
                    mPlayBinder.play(music);
                } else {
                    /* 播放,暂停当前曲目 */
                    mPlayBinder.togglePlayOrPause();
                }
                mControlView.syncPlayView(music);
            }
        });
        mRecyclerView.setAdapter(mAdapter);

        onRefreshChanged(new MusicManager.RefreshEventM(MusicManager.getInstance().isLoading()));

        /** 监听控制面板中的事件 */
        mControlView.setControlListener(new ControlView.ControlListener() {
            @Override
            public void previous() {
                mPlayBinder.previous();
//                syncLogic();
            }

            @Override
            public void next() {
                mPlayBinder.next();
//                syncLogic();
            }

            @Override
            public void play() {
                mPlayBinder.play();
            }

            @Override
            public void pause() {
                mPlayBinder.pause();
            }
        });
    }

    private void initServiceBinder() {
        getActivity().bindService(new Intent(getActivity(), PlayService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    ServiceConnection mServiceConnection = new ServiceConnection() {
        /** bindService()方法执行后, 绑定成功时回调 */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Logger.d("连接到播放服务");
            mPlayBinder = (PlayService.PlayBinder) service;

            mPlayBinder.addPlayStateListener(new PlayService.PlayStateListener() {

                @Override
                public void onPlay(BMusic music) {
                    syncVisual(music);
                }

                @Override
                public void onPause() {
                    mControlView.syncPlayView(mCurrentMusic);
                    mAdapter.togglePlay(false);
                }
            });

            mControlView.setPlayer(mPlayBinder.getExoPlayer());
            syncVisual(mPlayBinder.getCurrentMusic());
        }

        /** 和服务断开连接后回调(比如unbindService()方法执行后) */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logger.d("和服务断开连接");
        }
    };

    //=======================================UI和逻辑的同步=========================================
    private void syncLogic() {
        syncVisual(mPlayBinder.getCurrentMusic());
    }

    private void syncVisual(BMusic music) {
        mCurrentMusic = music;
        mControlView.syncPlayView(mCurrentMusic);
        int index = MusicManager.getInstance().getIndex(music);
        mAdapter.setChosenItem(index, mPlayBinder.isPlaying());
    }

    //=======================================UI和逻辑的同步-end=====================================

    @Override
    protected void getData() {
        super.getData();

        mAdapter.setData(MusicManager.getInstance().getMusicList());
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
