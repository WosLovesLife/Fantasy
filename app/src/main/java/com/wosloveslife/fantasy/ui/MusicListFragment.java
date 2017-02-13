package com.wosloveslife.fantasy.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orhanobut.logger.Logger;
import com.wosloveslife.fantasy.R;
import com.wosloveslife.fantasy.adapter.MusicListAdapter;
import com.wosloveslife.fantasy.bean.BMusic;
import com.wosloveslife.fantasy.bean.NavigationItem;
import com.wosloveslife.fantasy.manager.MusicManager;
import com.wosloveslife.fantasy.services.CountdownTimerService;
import com.wosloveslife.fantasy.services.PlayService;
import com.wosloveslife.fantasy.ui.swapablenavigation.SwapNavigationAdapter;
import com.wosloveslife.fantasy.ui.swapablenavigation.VerticalSwapItemTouchHelperCallBack;
import com.wosloveslife.fantasy.utils.DividerDecoration;
import com.yesing.blibrary_wos.baserecyclerviewadapter.adapter.BaseRecyclerViewAdapter;
import com.yesing.blibrary_wos.utils.assist.WLogger;
import com.yesing.blibrary_wos.utils.screenAdaptation.Dp2Px;
import com.yesing.blibrary_wos.utils.systemUtils.SystemServiceUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import base.BaseFragment;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by zhangh on 2017/1/2.
 */
public class MusicListFragment extends BaseFragment {
    private static final int REQ_CODE_TIMER = 0;

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.control_view)
    ControlView mControlView;
    //    @BindView(R.id.navigation_view)
//    NavigationView mNavigationView;
    //    @BindView(R.id.sll_navigation)
//    ScrollLinearLayout mSllNavigation;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.card_view_navigation)
    CardView mCardViewNavigation;
    @BindView(R.id.rv_navigation)
    RecyclerView mRvNavigation;

    private Toolbar mToolbar;

    private Snackbar mSnackbar;

    //=============
    private MusicListAdapter mAdapter;
    private SwapNavigationAdapter mNavigationAdapter;
    private LinearLayoutManager mLayoutManager;
    private PlayService.PlayBinder mPlayBinder;

    //=============
    BMusic mCurrentMusic;

    boolean mIsCountdown;

    public static MusicListFragment newInstance() {

        Bundle args = new Bundle();

        MusicListFragment fragment = new MusicListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    //========================================生命周期-start========================================
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        WLogger.d("onStart() : 页面显示, 时间 = " + System.currentTimeMillis());
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

        initToolbar();

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new DividerDecoration(
                new ColorDrawable(getResources().getColor(R.color.gray_light)),
                (int) Math.max(Dp2Px.toPX(getContext(), 0.5f), 1),
                Dp2Px.toPX(getContext(), 48)));

        mAdapter = new MusicListAdapter();
        mAdapter.setUseOriginData(true);
        mAdapter.setOnItemClickListener(new BaseRecyclerViewAdapter.OnItemClickListener<BMusic>() {
            @Override
            public void onItemClick(final BMusic music, View v, int position) {
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

        /** 监听控制面板中的事件 */
        mControlView.setControlListener(new ControlView.ControlListener() {
            @Override
            public void previous() {
                mPlayBinder.previous();
            }

            @Override
            public void next() {
                mPlayBinder.next();
            }

            @Override
            public void play() {
                mPlayBinder.play();
            }

            /**
             * 如果暂停时发现当前的状态是有关闭倒计时而服务中以及完成了倒计时,则同步导航栏的倒计时状态
             */
            @Override
            public void pause() {
                mPlayBinder.pause();
            }
        });
    }

    private void initToolbar() {
        mToolbar = mControlView.getToolbar();
        final AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(mToolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, mToolbar, R.string.app_name, R.string.app_name);
        toggle.syncState();
        mToolbar.setTitle("本地音乐");

        ViewCompat.setElevation(mCardViewNavigation, 8);
        mRvNavigation.setLayoutManager(new LinearLayoutManager(getContext()));
        mNavigationAdapter = new SwapNavigationAdapter();
        View header = LayoutInflater.from(getActivity()).inflate(R.layout.layout_navigation_header, mRvNavigation, false);
        mNavigationAdapter.addHeaderView(header);
        mNavigationAdapter.setData(generateNavigationItems());
        mNavigationAdapter.setChosenPosition(1);
        onChangeSheet(MusicManager.getInstance().getCurrentSheetOrdinal());
        mNavigationAdapter.setOnItemClickListener(new BaseRecyclerViewAdapter.OnItemClickListener<NavigationItem>() {
            @Override
            public void onItemClick(final NavigationItem navigationItem, View v, final int position) {
                /* // 在抽屉关闭后再进行操作,避免动画卡顿. 官方解释:
                // Avoid performing expensive operations such as layout during animation as it can cause stuttering;
                //try to perform expensive operations during the STATE_IDLE state. */
                switch (navigationItem.mTitle) {
                    case "本地音乐":
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        mNavigationAdapter.setChosenPosition(position);
                        mToolbar.setTitle("本地音乐");
                        if (!TextUtils.equals(MusicManager.getInstance().getCurrentSheetOrdinal(), "0")) {
                            MusicManager.getInstance().changeSheet("0");
                        }
                        break;
                    case "我的收藏":
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        mNavigationAdapter.setChosenPosition(position);
                        mToolbar.setTitle("我的收藏");
                        if (!TextUtils.equals(MusicManager.getInstance().getCurrentSheetOrdinal(), "1")) {
                            MusicManager.getInstance().changeSheet("1");
                        }
                        break;
                    case "最近播放":
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        mNavigationAdapter.setChosenPosition(position);
                        break;
                    case "下载管理":
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        mNavigationAdapter.setChosenPosition(position);
                        break;
                    case "定时停止播放":
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        mDrawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
                            @Override
                            public void onDrawerClosed(View drawerView) {
                                super.onDrawerClosed(drawerView);
                                mDrawerLayout.removeDrawerListener(this);

                                CountdownPickDialog pickDialog = new CountdownPickDialog();
                                pickDialog.setTargetFragment(MusicListFragment.this, 0);
                                pickDialog.show(getChildFragmentManager(), "pick_time");
                            }
                        });
                        break;
                    case "设置":
                        startActivity(SettingActivity.newStartIntent(getActivity()));
                        mDrawerLayout.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mDrawerLayout.closeDrawer(Gravity.LEFT);
                            }
                        }, 200);
                        break;
                }
            }
        });
        mRvNavigation.setAdapter(mNavigationAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new VerticalSwapItemTouchHelperCallBack(mNavigationAdapter));
        itemTouchHelper.attachToRecyclerView(mRvNavigation);
    }

    private void onChangeSheet(String currentSheetOrdinal) {
        switch (currentSheetOrdinal) {
            case "0":
                mNavigationAdapter.setChosenPosition(mNavigationAdapter.getHeadersCount());
                mToolbar.setTitle("本地音乐");
                break;
            case "1":
                mNavigationAdapter.setChosenPosition(mNavigationAdapter.getHeadersCount() + 1);
                mToolbar.setTitle("我的收藏");
                break;
            case "2":
                mNavigationAdapter.setChosenPosition(mNavigationAdapter.getHeadersCount() + 2);
                break;
            case "3":
                mNavigationAdapter.setChosenPosition(mNavigationAdapter.getHeadersCount() + 3);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;
        switch (requestCode) {
            case REQ_CODE_TIMER: // 用户选择了计时关闭后, 如果开启,则将时间和是否播放完后再结束同步给计时器服务和播放服务以及导航栏中更新倒计时进度
                long pickDate = CountdownPickDialog.getPickDate(data);
                boolean closeAfterPlayComplete = CountdownPickDialog.isCloseAfterPlayComplete(data);
                mIsCountdown = pickDate > 0;
                if (mIsCountdown) {
                    Intent intent = CountdownTimerService.createIntent(getActivity(), pickDate);
                    getActivity().startService(intent);
                    mPlayBinder.setCountdown(pickDate, closeAfterPlayComplete);
                } else if (SystemServiceUtils.isServiceRunning(getActivity(), CountdownTimerService.class.getName())) {
                    Intent intent = CountdownTimerService.stopService(getActivity());
                    getActivity().startService(intent);
                    mPlayBinder.setCountdown(pickDate, false);
                }
                updateNvCountdown(pickDate, 0);
                break;
        }
    }

    private List<NavigationItem> generateNavigationItems() {
        List<NavigationItem> navigationItems = new ArrayList<>();
        NavigationItem item = new NavigationItem(0, R.drawable.ic_phone, "本地音乐");
        NavigationItem item2 = new NavigationItem(1, R.drawable.ic_favorite_border, "我的收藏");
        NavigationItem item3 = new NavigationItem(1, R.drawable.ic_clock, "最近播放");
        NavigationItem item4 = new NavigationItem(1, R.drawable.ic_download, "下载管理");
        navigationItems.add(item);
        navigationItems.add(item2);
        navigationItems.add(item3);
        navigationItems.add(item4);
        NavigationItem item5 = new NavigationItem(3, 0, "工具");
        navigationItems.add(item5);
        NavigationItem item6 = new NavigationItem(0, 1, R.drawable.ic_countdown_tiemr, "定时停止播放");
        navigationItems.add(item6);
        NavigationItem item7 = new NavigationItem(0, 1, R.drawable.ic_setting, "设置");
        navigationItems.add(item7);
        return navigationItems;
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

                    if (mIsCountdown && !mPlayBinder.isCountdown()) {
                        mIsCountdown = false;
                        updateNvCountdown(0, 0);
                    }
                }
            });

            mControlView.setPlayer(mPlayBinder.getExoPlayer());
            syncVisual(mPlayBinder.getCurrentMusic());
            mIsCountdown = mPlayBinder.isCountdown();
            updateNvCountdown(0, 0);
        }

        /** 和服务断开连接后回调(比如unbindService()方法执行后) */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logger.d("和服务断开连接");
        }
    };

    //=======================================UI和逻辑的同步=========================================

    private void syncVisual(BMusic music) {
        mCurrentMusic = music;
        mControlView.syncPlayView(mCurrentMusic);
        int index = MusicManager.getInstance().getIndex(music);
        mAdapter.setChosenItem(index, mPlayBinder.isPlaying());
    }

    private void updateNvCountdown(long totalMillis, long millisUntilFinished) {
        if (totalMillis == millisUntilFinished) {
            mNavigationAdapter.updateCountDownTimer(mNavigationAdapter.getHeadersCount() + 5, -1,
                    mPlayBinder != null && mPlayBinder.isCloseAfterPlayComplete());
        } else {
            mNavigationAdapter.updateCountDownTimer(mNavigationAdapter.getHeadersCount() + 5, millisUntilFinished,
                    mPlayBinder != null && mPlayBinder.isCloseAfterPlayComplete());
        }
    }

    //=======================================UI和逻辑的同步-end=====================================

    @Override
    protected void getData() {
        super.getData();

        setData(MusicManager.getInstance().getMusicList());
    }

    @Override
    protected boolean onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.closeDrawer(Gravity.LEFT);
            return true;
        }
        return super.onBackPressed();
    }

    //==========================================事件================================================

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onScannedMusic(MusicManager.OnScannedMusicEvent event) {
        if (event == null) return;

        setData(event.mBMusicList);

        if (mSnackbar == null) {
            mSnackbar = Snackbar.make(mRecyclerView, "找到了" + event.mBMusicList.size() + "首音乐", Snackbar.LENGTH_LONG);
        } else {
            mSnackbar.setText("找到了" + event.mBMusicList.size() + "首音乐");
            mSnackbar.setDuration(Snackbar.LENGTH_LONG);
        }

        mSnackbar.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGotMusic(MusicManager.OnGotMusicEvent event) {
        if (event == null) return;
        setData(event.mBMusicList);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCountDownTimerTick(CountdownTimerService.CountDownEvent event) {
        if (event == null) return;
        updateNvCountdown(event.totalMillis, event.millisUntilFinished);
    }

    /**
     * 显示音乐列表并做状态处理
     *
     * @param musicList 音乐列表
     */
    private void setData(List<BMusic> musicList) {
        if (musicList == null || musicList.size() == 0) {
            /* todo 没有音乐,显示空白页面 */

        }
        mAdapter.setData(musicList);
    }

    /**
     * 处理各种异常状态
     * 没有音乐
     * 无法播放
     */
    private void handleErrorState() {

    }
}
