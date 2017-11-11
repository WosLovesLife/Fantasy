package com.wosloveslife.fantasy.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wosloveslife.dao.Audio;
import com.wosloveslife.dao.SheetIds;
import com.wosloveslife.fantasy.R;
import com.wosloveslife.fantasy.adapter.MusicListAdapter;
import com.wosloveslife.fantasy.dao.bean.NavigationItem;
import com.wosloveslife.fantasy.manager.MusicManager;
import com.wosloveslife.fantasy.services.CountdownTimerService;
import com.wosloveslife.fantasy.ui.funcs.CountdownAnimView;
import com.wosloveslife.fantasy.ui.funcs.CountdownPickDialog;
import com.wosloveslife.fantasy.ui.swapablenavigation.SwapNavigationAdapter;
import com.wosloveslife.fantasy.ui.swapablenavigation.VerticalSwapItemTouchHelperCallBack;
import com.wosloveslife.fantasy.utils.DividerDecoration;
import com.wosloveslife.player.ExoPlayerEventListenerAdapter;
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
    private IPlayer mPlayer;

    //=============
    Audio mCurrentMusic;

    boolean mIsCountdown;
    // TODO: 17/6/18 暂时禁用搜索页面
//    private SearchSuggestLayout mSuggestLayout;

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

        mPlayer = Controller.getInstance();
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

    //========================================生命周期-end========================================

    @Override
    protected View setContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    public void initView() {
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new DividerDecoration(
                new ColorDrawable(getResources().getColor(R.color.gray_light)),
                (int) Math.max(Dp2Px.toPX(getContext(), 0.5f), 1),
                Dp2Px.toPX(getContext(), 48)));

        mAdapter = new MusicListAdapter();
        mAdapter.setOnItemClickListener(new BaseRecyclerViewAdapter.OnItemClickListener<Audio>() {
            @Override
            public void onItemClick(Audio music, View v, int position) {
                Audio currentMusic = MusicManager.getInstance().getMusicConfig().mCurrentMusic;
                boolean playing = mPlayer.getState().isPlaying();
                if (currentMusic == null || !currentMusic.equals(music) || !playing) {
                    mPlayer.play(music);
                } else {
                    mPlayer.pause();
                }
                mControlView.syncPlayView(music);
            }
        });
        mRecyclerView.setAdapter(mAdapter);

        initToolbar();

        mPlayer.getState().addListener(new ExoPlayerEventListenerAdapter() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                super.onPlayerStateChanged(playWhenReady, playbackState);
                // TODO: 2017/9/3
                if (playWhenReady) {
                    syncVisual(MusicManager.getInstance().getMusicConfig().mCurrentMusic);
                } else {
                    mControlView.syncPlayView(mCurrentMusic);
                    mAdapter.togglePlay(false);
                    if (mIsCountdown/* && !mPlayBinder.isCountdown() todo Countdown*/ ) {
                        mIsCountdown = false;
                        updateNvCountdown();
                    }
                }
            }
        });

        syncVisual(MusicManager.getInstance().getMusicConfig().mCurrentMusic);
//        mIsCountdown = mPlayBinder.isCountdown(); // TODO: 2017/9/3 Countdown
        updateNvCountdown();
    }

    @Override
    protected void updateData() {
        super.updateData();
        setData(MusicManager.getInstance().getMusicConfig().mMusicList);
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
        onChangeSheet(MusicManager.getInstance().getMusicConfig().mCurrentSheetId);
        mNavigationAdapter.setOnItemClickListener(new BaseRecyclerViewAdapter.OnItemClickListener<NavigationItem>() {
            @Override
            public void onItemClick(final NavigationItem navigationItem, final View v, final int position) {
                /* // 在抽屉关闭后再进行操作,避免动画卡顿. 官方解释:
                // Avoid performing expensive operations such as layout during animation as it can cause stuttering;
                //try to perform expensive operations during the STATE_IDLE state. */
                switch (navigationItem.mTitle) {
                    case "本地音乐":
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        onChangeSheet("0");
                        break;
                    case "我的收藏":
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        onChangeSheet("1");
                        break;
                    case "最近播放":
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        onChangeSheet("2");
                        break;
                    case "下载管理":
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        onChangeSheet("3");
                        break;
                    case "定时停止播放":
                        final ViewGroup viewGroup = (ViewGroup) getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
                        final CountdownPickDialog countdownPickDialog = CountdownPickDialog.newInstance(getActivity(), new com.wosloveslife.fantasy.ui.funcs.CountdownPickDialog.OnChosenListener() {
                            @Override
                            public void onChosen(com.wosloveslife.fantasy.ui.funcs.CountdownPickDialog.Result result) {
                                if (result.duration > 0) {
                                    CountdownAnimView view = new CountdownAnimView(getContext());
                                    view.show(viewGroup, result.duration);
                                }
                                updateNvCountdown();
                            }
                        });
                        countdownPickDialog.show(viewGroup);
                        mDrawerLayout.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mDrawerLayout.closeDrawer(Gravity.START);
                            }
                        }, 200);
                        break;
                    case "设置":
                        // TODO: 17/6/19 跳转SettingPage
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

    // TODO: 17/6/18 切换歌单
    private void onChangeSheet(@Nullable String sheetId) {
        if (TextUtils.isEmpty(sheetId)) {
            sheetId = SheetIds.LOCAL;
        }
        if (TextUtils.equals(MusicManager.getInstance().getMusicConfig().mCurrentSheetId, sheetId)) {
            return;
        }
        switch (sheetId) {
            case "0":
                mNavigationAdapter.setChosenPosition(mNavigationAdapter.getHeadersCount());
                mToolbar.setTitle("本地音乐");
                MusicManager.getInstance().changeSheet("0");
                break;
            case "1":
                mNavigationAdapter.setChosenPosition(mNavigationAdapter.getHeadersCount() + 1);
                mToolbar.setTitle("我的收藏");
                MusicManager.getInstance().changeSheet("1");
                break;
            case "2":
                mNavigationAdapter.setChosenPosition(mNavigationAdapter.getHeadersCount() + 2);
                mToolbar.setTitle("最近播放");
                MusicManager.getInstance().changeSheet("2");
                break;
            case "3":
                mNavigationAdapter.setChosenPosition(mNavigationAdapter.getHeadersCount() + 3);
                break;
        }
        // TODO: 17/6/18 暂时禁用搜索
//        if (mSuggestLayout != null) {
//            mSuggestLayout.setSheet(mCurrentSheetOrdinal);
//        }
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
                    // TODO: 2017/9/3 Countdown
                } else if (SystemServiceUtils.isServiceRunning(getActivity(), CountdownTimerService.class.getName())) {
                    Intent intent = CountdownTimerService.stopService(getActivity());
                    getActivity().startService(intent);
                    // TODO: 2017/9/3 Countdown
                }
                updateNvCountdown();
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
//        navigationItems.add(item4);
        NavigationItem item5 = new NavigationItem(3, 0, "工具");
        navigationItems.add(item5);
        NavigationItem item6 = new NavigationItem(0, 1, R.drawable.ic_countdown_tiemr, "定时停止播放");
        navigationItems.add(item6);
        NavigationItem item7 = new NavigationItem(0, 1, R.drawable.ic_setting, "设置");
        navigationItems.add(item7);
        return navigationItems;
    }

    //=======================================UI和逻辑的同步=========================================

    private void syncVisual(Audio music) {
        mCurrentMusic = music;
        mControlView.syncPlayView(mCurrentMusic);
        int position = mAdapter.getNormalPosition(music);
        mAdapter.setChosenItem(position, mPlayer.getState().isPlaying());
    }

    private void updateNvCountdown() {
        int index = 0;
        for (int i = 0; i < mNavigationAdapter.getRealItemCount(); i++) {
            NavigationItem normalData = mNavigationAdapter.getNormalData(i);
            if (normalData.mTitle.equals("定时停止播放")) {
                index = i;
                break;
            }
        }
        mNavigationAdapter.notifyItemChanged(index + mNavigationAdapter.getHeadersCount());
    }

    //=======================================UI和逻辑的同步-end=====================================

    @Override
    protected void getData() {
        super.getData();

        setData(MusicManager.getInstance().getMusicConfig().mMusicList);
    }

    @Override
    protected boolean onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.closeDrawer(Gravity.LEFT);
            return true;
        }
        return super.onBackPressed();
    }

    @Override
    protected int initMenu() {
        return R.menu.menu_search;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        SearchView searchView = (SearchView) menu.findItem(R.id.item_search).getActionView();
        searchView.setQueryHint("搜索歌单内歌曲/歌手/专辑...");
        // TODO: 17/6/18 暂时禁用搜索
//        mSuggestLayout = new SearchSuggestLayout
//                .Builder(getActivity())
//                .setSheet(mCurrentSheetOrdinal)
//                .setOnItemChosenListener(new BaseRecyclerViewAdapter.OnItemClickListener<Audio>() {
//                    @Override
//                    public void onItemClick(Audio bMusic, View v, int position) {
//                        mPlayBinder.play(bMusic);
//                    }
//                })
//                .setParent(mControlView)
//                .setAnchor(mToolbar)
//                .bindSearchView(searchView)
//                .build();
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
    public void onAddMusic(MusicManager.OnAddMusic event) {
        if (event == null || event.mMusic == null) return;
        Audio music = event.mMusic;
        if (TextUtils.equals(MusicManager.getInstance().getMusicConfig().mCurrentSheetId, event.mSheetId)) {
            mAdapter.addItem(music, 0);
        }
        if (music.equals(mCurrentMusic)) {
            mControlView.syncPlayView(music);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRemoveMusic(MusicManager.OnRemoveMusic event) {
        if (event == null || event.mMusic == null) return;
        Audio music = event.mMusic;
        if (TextUtils.equals(MusicManager.getInstance().getMusicConfig().mCurrentSheetId, event.mBelongTo)) {
            int startPosition = mAdapter.getNormalPosition(music);
            mAdapter.removeItem(music);
            mAdapter.notifyItemRangeChanged(startPosition, mAdapter.getRealItemCount() - startPosition);
        }
        if (music.equals(mCurrentMusic)) {
            mControlView.syncPlayView(music);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMusicChanged(MusicManager.OnMusicChanged event) {
        if (event == null || event.mMusic == null) return;
        Audio music = event.mMusic;
        if (TextUtils.equals(MusicManager.getInstance().getMusicConfig().mCurrentSheetId, event.mSheetId) && TextUtils.equals(MusicManager.getInstance().getMusicConfig().mCurrentSheetId, "2")) {
            int oldPosition = mAdapter.getNormalPosition(event.mMusic);
            if (mRecyclerView.getItemAnimator().isRunning()) {
                mRecyclerView.getItemAnimator().endAnimations();
            }
            mAdapter.removeItemNotNotify(oldPosition);
            mAdapter.addItemNotNofity(music, 0);
            mAdapter.notifyItemRangeChanged(0, oldPosition + 1);
        }
        if (music.equals(mCurrentMusic)) {
            mControlView.syncPlayView(music);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCountDownTimerTick(CountdownTimerService.CountDownEvent event) {
        if (event == null) return;
        updateNvCountdown();
    }

    /**
     * 显示音乐列表并做状态处理
     *
     * @param musicList 音乐列表
     */
    private void setData(List<Audio> musicList) {
        if (musicList == null || musicList.size() == 0) {
            /* todo 没有音乐,显示空白页面 */

        }
        if (mAdapter != null) {
            mAdapter.setData(musicList);
        }
    }

    /**
     * 处理各种异常状态
     * 没有音乐
     * 无法播放
     */
    private void handleErrorState() {

    }
}
