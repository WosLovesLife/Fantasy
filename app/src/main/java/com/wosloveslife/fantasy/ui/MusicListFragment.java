package com.wosloveslife.fantasy.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
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

import com.orhanobut.logger.Logger;
import com.wosloveslife.dao.Audio;
import com.wosloveslife.dao.Sheet;
import com.wosloveslife.dao.SheetIds;
import com.wosloveslife.dao.store.SheetStore;
import com.wosloveslife.fantasy.R;
import com.wosloveslife.fantasy.adapter.MusicListAdapter;
import com.wosloveslife.fantasy.manager.MusicManager;
import com.wosloveslife.fantasy.services.PlayService;
import com.wosloveslife.fantasy.ui.funcs.CountdownPickDialog;
import com.wosloveslife.fantasy.ui.swapablenavigation.SwapNavigationAdapter;
import com.wosloveslife.fantasy.ui.swapablenavigation.VerticalSwapItemTouchHelperCallBack;
import com.wosloveslife.fantasy.ui.swapablenavigation.navi_holders.NaviSettingItemHolder;
import com.wosloveslife.fantasy.ui.swapablenavigation.navi_item.BaseNaviItem;
import com.wosloveslife.fantasy.ui.swapablenavigation.navi_item.NaviSettingItem;
import com.wosloveslife.fantasy.ui.swapablenavigation.navi_item.NaviSheetItem;
import com.wosloveslife.fantasy.ui.swapablenavigation.navi_item.NaviSubTitleItem;
import com.wosloveslife.fantasy.utils.DividerDecoration;
import com.yesing.blibrary_wos.baserecyclerviewadapter.adapter.BaseRecyclerViewAdapter;
import com.yesing.blibrary_wos.utils.assist.WLogger;
import com.yesing.blibrary_wos.utils.screenAdaptation.Dp2Px;

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
@Deprecated
public class MusicListFragment extends BaseFragment {

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
    private PlayService.PlayBinder mPlayBinder;

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
        WLogger.d("onStart() : 页面显示, 时间 = " + System.currentTimeMillis());
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
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
        mRecyclerView.addItemDecoration(new DividerDecoration(
                new ColorDrawable(getResources().getColor(R.color.gray_light)),
                (int) Math.max(Dp2Px.toPX(getContext(), 0.5f), 1),
                Dp2Px.toPX(getContext(), 48)));

        mAdapter = new MusicListAdapter();
        mAdapter.setOnItemClickListener(new BaseRecyclerViewAdapter.OnItemClickListener<Audio>() {
            @Override
            public void onItemClick(final Audio music, View v, int position) {
                Audio currentMusic = MusicManager.getInstance().getMusicConfig().mCurrentMusic;
                if (currentMusic == null || !currentMusic.equals(music)) {
                    mPlayBinder.play(music);
                } else {
                    /* 播放,暂停当前曲目 */
                    mPlayBinder.togglePlayOrPause();
                }
                mControlView.syncPlayView(music);
            }
        });
        mRecyclerView.setAdapter(mAdapter);

        initToolbar();

        /* 监听控制面板中的事件 */
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
                mPlayBinder.play(null);
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
        mNavigationAdapter.setOnItemClickListener(new BaseRecyclerViewAdapter.OnItemClickListener<BaseNaviItem>() {
            @Override
            public void onItemClick(final BaseNaviItem baseNaviItem, final View v, final int position) {
                /* // 在抽屉关闭后再进行操作,避免动画卡顿. 官方解释:
                // Avoid performing expensive operations such as layout during animation as it can cause stuttering;
                //try to perform expensive operations during the STATE_IDLE state. */
                if (baseNaviItem instanceof NaviSheetItem) {
                    NaviSheetItem item = (NaviSheetItem) baseNaviItem;
                    mDrawerLayout.closeDrawer(Gravity.START);
                    onChangeSheet(item.mSheet);
                } else if (baseNaviItem instanceof NaviSettingItem) {
                    NaviSettingItem item = (NaviSettingItem) baseNaviItem;
                    switch (item.mItem) {
                        case NaviSettingItem.Item.ITEM_COUNTDOWN_TIMER:
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
                        case NaviSettingItem.Item.ITEM_TO_SETTING:
                            startActivity(SettingActivity.newStartIntent(getActivity()));
                            mDrawerLayout.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mDrawerLayout.closeDrawer(Gravity.START);
                                }
                            }, 200);
                            break;
                    }
                }
            }
        });
        mRvNavigation.setAdapter(mNavigationAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new VerticalSwapItemTouchHelperCallBack(mNavigationAdapter));
        itemTouchHelper.attachToRecyclerView(mRvNavigation);
    }

    // TODO: 17/6/18 切换歌单
    private void onChangeSheet(@NonNull Sheet sheet) {
        mNavigationAdapter.setChosenPosition(mNavigationAdapter.getHeadersCount() + 2);
        mToolbar.setTitle(sheet.title);
        MusicManager.getInstance().changeSheet(sheet.id);

        // TODO: 17/6/18 暂时禁用搜索
//        if (mSuggestLayout != null) {
//            mSuggestLayout.setSheet(mCurrentSheetOrdinal);
//        }
    }

    private List<BaseNaviItem> generateNavigationItems() {
        final List<BaseNaviItem> naviSheetItems = new ArrayList<>();
        NaviSheetItem item = new NaviSheetItem(-1, R.drawable.ic_phone, SheetStore.loadById(SheetIds.LOCAL).toBlocking().first().clone());
        NaviSheetItem item2 = new NaviSheetItem(1, R.drawable.ic_favorite_border, SheetStore.loadById(SheetIds.LOCAL).toBlocking().first().clone());
        NaviSheetItem item3 = new NaviSheetItem(1, R.drawable.ic_clock, SheetStore.loadById(SheetIds.LOCAL).toBlocking().first().clone());
//        NaviSheetItem item4 = new NaviSheetItem(1, R.drawable.ic_download, SheetStore.loadById(SheetIds.LOCAL).toBlocking().first().clone());
        naviSheetItems.add(item);
        naviSheetItems.add(item2);
        naviSheetItems.add(item3);
//        navigationItems.add(item4);

        NaviSubTitleItem item5 = new NaviSubTitleItem("工具");
        NaviSettingItem item6 = new NaviSettingItem(2, R.drawable.ic_countdown_tiemr, "定时停止播放", NaviSettingItem.Item.ITEM_COUNTDOWN_TIMER);
        NaviSettingItem item7 = new NaviSettingItem(2, R.drawable.ic_setting, "设置", NaviSettingItem.Item.ITEM_TO_SETTING);
        naviSheetItems.add(item5);
        naviSheetItems.add(item6);
        naviSheetItems.add(item7);
        return naviSheetItems;
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
//            mPlayBinder.addListener(new ExoPlayerEventListenerAdapter() {
//                @Override
//                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
//                    super.onPlayerStateChanged(playWhenReady, playbackState);
//                    if (playWhenReady) {
//                        syncVisual(mPlayBinder.getCurrentMusic());
//                    } else {
//                        mControlView.syncPlayView(MusicManager.getInstance().getMusicConfig().mCurrentMusic);
//                        mAdapter.togglePlay(false);
//                    }
//                }
//            });
//
//            mControlView.setPlayer(mPlayBinder.getExoPlayer());
//            syncVisual(mPlayBinder.getCurrentMusic());
        }

        /** 和服务断开连接后回调(比如unbindService()方法执行后) */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logger.d("和服务断开连接");
        }
    };

    //=======================================UI和逻辑的同步=========================================

    private void syncVisual(Audio music) {
        mControlView.syncPlayView(music);
        int position = mAdapter.getNormalPosition(music);
        mAdapter.setChosenItem(position, mPlayBinder.isPlaying());
    }

    private void updateNvCountdown() {
        int index = 0;
        for (int i = 0; i < mNavigationAdapter.getRealItemCount(); i++) {
            BaseNaviItem normalData = mNavigationAdapter.getNormalData(i);
            if (normalData instanceof NaviSettingItem) {
                if (((NaviSettingItem) normalData).mItem == NaviSettingItem.Item.ITEM_COUNTDOWN_TIMER) {
                    index = i;
                    break;
                }
            }
        }
        RecyclerView.ViewHolder holder = mRvNavigation.findViewHolderForAdapterPosition(index + mNavigationAdapter.getHeadersCount());
        if (holder instanceof NaviSettingItemHolder) {
            ((NaviSettingItemHolder) holder).updateCountdownTime();
        }
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
        if (music.equals(MusicManager.getInstance().getMusicConfig().mCurrentMusic)) {
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
        if (music.equals(MusicManager.getInstance().getMusicConfig().mCurrentMusic)) {
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
        if (music.equals(MusicManager.getInstance().getMusicConfig().mCurrentMusic)) {
            mControlView.syncPlayView(music);
        }
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
