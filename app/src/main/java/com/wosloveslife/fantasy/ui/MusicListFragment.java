package com.wosloveslife.fantasy.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.orhanobut.logger.Logger;
import com.wosloveslife.fantasy.R;
import com.wosloveslife.fantasy.adapter.MusicListAdapter;
import com.wosloveslife.fantasy.bean.BMusic;
import com.wosloveslife.fantasy.manager.MusicManager;
import com.wosloveslife.fantasy.services.PlayService;
import com.wosloveslife.fantasy.utils.DividerDecoration;
import com.yesing.blibrary_wos.baserecyclerviewadapter.adapter.BaseRecyclerViewAdapter;
import com.yesing.blibrary_wos.utils.screenAdaptation.Dp2Px;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import base.BaseFragment;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by zhangh on 2017/1/2.
 */
public class MusicListFragment extends BaseFragment {
    public static final String LRC = "[00:00.79]一生为你感动\n" +
            "[00:02.48]\n" +
            "[00:04.53]作词：祁隆\n" +
            "[00:06.58]作曲：祁隆\n" +
            "[00:08.63]演唱：祁隆\n" +
            "[00:10.66]\n" +
            "[00:33.41]天天的等 \n" +
            "[00:35.44]夜夜的梦\n" +
            "[00:37.48]幻想着见到你是怎样的激动\n" +
            "[00:40.97]\n" +
            "[00:41.54]你就像是\n" +
            "[00:43.59]心灵的灯\n" +
            "[00:45.62]照亮我人生一段崭新的旅程\n" +
            "[00:48.93]\n" +
            "[00:49.79]我亲爱的\n" +
            "[00:51.74]我的宝贝\n" +
            "[00:53.77]你是我心底那块剔透的水晶\n" +
            "[00:57.05]\n" +
            "[00:57.82]一想起你\n" +
            "[00:59.85]心跳怦怦\n" +
            "[01:01.88]简直无法表达我爱你的心声\n" +
            "[01:05.57]\n" +
            "[01:08.80]我一生为你感动\n" +
            "[01:10.69]一生为你心动\n" +
            "[01:12.77]宝贝我盼了很久\n" +
            "[01:14.81]为你发了疯\n" +
            "[01:16.73]\n" +
            "[01:16.75]我一生为你感动\n" +
            "[01:18.79]一生为你心动\n" +
            "[01:20.88]你就是我的全部\n" +
            "[01:22.88]我终于圆了梦\n" +
            "[01:24.85]\n" +
            "[01:41.58]我亲爱的\n" +
            "[01:43.55]我的宝贝\n" +
            "[01:45.63]你是我心底那块剔透的水晶\n" +
            "[01:48.83]\n" +
            "[01:49.70]一想起你\n" +
            "[01:51.73]心跳怦怦\n" +
            "[01:53.71]简直无法表达我爱你的心声\n" +
            "[01:58.51]\n" +
            "[02:01.42]我一生为你感动\n" +
            "[02:03.64]一生为你心动\n" +
            "[02:05.62]宝贝我盼了很久\n" +
            "[02:07.68]为你发了疯\n" +
            "[02:09.07]\n" +
            "[02:09.57]我一生为你感动\n" +
            "[02:11.67]一生为你心动\n" +
            "[02:13.70]你就是我的全部\n" +
            "[02:15.72]我终于圆了梦\n" +
            "[02:17.49]\n" +
            "[02:34.00]我一生为你感动\n" +
            "[02:36.09]一生为你心动\n" +
            "[02:38.15]宝贝我盼了很久\n" +
            "[02:40.21]为你发了疯\n" +
            "[02:41.77]\n" +
            "[02:42.06]我一生为你感动\n" +
            "[02:44.17]一生为你心动\n" +
            "[02:46.30]你就是我的全部\n" +
            "[02:48.29]我终于圆了梦\n" +
            "[02:50.16]\n" +
            "[02:50.24]我一生为你感动\n" +
            "[02:52.34]一生为你心动\n" +
            "[02:54.45]宝贝我盼了很久\n" +
            "[02:56.51]为你发了疯\n" +
            "[02:58.03]\n" +
            "[02:58.32]我一生为你感动\n" +
            "[03:00.50]一生为你心动\n" +
            "[03:02.51]你就是我的全部\n" +
            "[03:04.56]我终于圆了梦\n" +
            "[03:06.64]\n" +
            "[03:18.87]你就是我的全部\n" +
            "[03:20.88]我终于圆了梦";

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.navigation_view)
    NavigationView mNavigationView;
    @BindView(R.id.control_view)
    ControlView mControlView;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.lrc_view)
    LrcView mLrcView;
    private Toolbar mToolbar;

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
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
                Dp2Px.toPX(getContext(), 1),
                Dp2Px.toPX(getContext(), 48)));

        mAdapter = new MusicListAdapter();
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

                mLrcView.setLrc(MusicManager.generateLrcData(LRC));
            }
        });
        mRecyclerView.setAdapter(mAdapter);

        onRefreshChanged(new MusicManager.RefreshEventM(MusicManager.getInstance().isLoading()));

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

            @Override
            public void pause() {
                mPlayBinder.pause();
            }
        });
    }

    private void initToolbar() {
        mToolbar = mControlView.getToolbar();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(mToolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, mToolbar, R.string.app_name, R.string.app_name);
        toggle.syncState();
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                mToolbar.setTitle(item.getTitle());
                mDrawerLayout.closeDrawer(Gravity.LEFT);
                return true;
            }
        });
        mNavigationView.setCheckedItem(R.id.item_1);
        mToolbar.setTitle(mNavigationView.getMenu().getItem(0).getTitle());
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

        setData(event.mBMusicList);

        if (mSnackbar == null) {
            mSnackbar = Snackbar.make(mRecyclerView, "找到了" + event.mBMusicList.size() + "首音乐", Snackbar.LENGTH_INDEFINITE);
        } else {
            mSnackbar.setText("找到了" + event.mBMusicList.size() + "首音乐");
            mSnackbar.setDuration(Snackbar.LENGTH_SHORT);
        }

        mSnackbar.show();
    }

    /**
     * 显示音乐列表并做状态处理
     *
     * @param musicList 音乐列表
     */
    private void setData(List<BMusic> musicList) {
        if (musicList == null || musicList.size() == 0) {
            /* todo 没有音乐,显示空白页面 */

            return;
        }

        musicList.add(new BMusic(0, "qiuniang", "秋酿", "", "房东的猫",
                "http://yinyueshiting.baidu.com/data2/music/d31fdf4aceb28170563fe8c91b7c568e/275148779/275148779.mp3?xcode=2c52deb5e3da39649e9cdd6067ee2141",
                0, 0, true));

        musicList.add(new BMusic(0, "qiuniang", "龙港秘密", "", "尧十三",
                "http://yinyueshiting.baidu.com/data2/music/256006854/256006854.mp3?xcode=85efd0ea655803e205ffee87c3adb48c",
                0, 0, true));
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
