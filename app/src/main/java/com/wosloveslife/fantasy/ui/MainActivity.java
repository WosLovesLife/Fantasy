package com.wosloveslife.fantasy.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import com.wosloveslife.dao.Sheet;
import com.wosloveslife.dao.SheetIds;
import com.wosloveslife.dao.store.SheetStore;
import com.wosloveslife.fantasy.R;
import com.wosloveslife.fantasy.manager.MusicManager;
import com.wosloveslife.fantasy.ui.funcs.CountdownPickDialog;
import com.wosloveslife.fantasy.ui.swapablenavigation.SwapNavigationAdapter;
import com.wosloveslife.fantasy.ui.swapablenavigation.VerticalSwapItemTouchHelperCallBack;
import com.wosloveslife.fantasy.ui.swapablenavigation.navi_holders.NaviSettingItemHolder;
import com.wosloveslife.fantasy.ui.swapablenavigation.navi_item.BaseNaviItem;
import com.wosloveslife.fantasy.ui.swapablenavigation.navi_item.NaviSettingItem;
import com.wosloveslife.fantasy.ui.swapablenavigation.navi_item.NaviSheetItem;
import com.wosloveslife.fantasy.ui.swapablenavigation.navi_item.NaviSubTitleItem;
import com.yesing.blibrary_wos.baserecyclerviewadapter.adapter.BaseRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.musicListV)
    MusicListV mMusicListV;
    @BindView(R.id.rv_navigation)
    RecyclerView mRvNavigation;
    @BindView(R.id.card_view_navigation)
    CardView mCardViewNavigation;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    private Toolbar mToolbar;

    private SwapNavigationAdapter mNavigationAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme_Translucent_Main);
        setContentView(R.layout.activity_base);
        ButterKnife.bind(this);
        initViews();
    }

    public void initViews() {
        mToolbar = mMusicListV.mControlView.getToolbar();
        setSupportActionBar(mToolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.app_name, R.string.app_name);
        toggle.syncState();
        mToolbar.setTitle("本地音乐");

        ViewCompat.setElevation(mCardViewNavigation, 8);
        mRvNavigation.setLayoutManager(new LinearLayoutManager(this));
        mNavigationAdapter = new SwapNavigationAdapter();
        View header = LayoutInflater.from(this).inflate(R.layout.layout_navigation_header, mRvNavigation, false);
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
                            final ViewGroup viewGroup = (ViewGroup) getWindow().getDecorView().findViewById(android.R.id.content);
                            final CountdownPickDialog countdownPickDialog = CountdownPickDialog.newInstance(MainActivity.this, new com.wosloveslife.fantasy.ui.funcs.CountdownPickDialog.OnChosenListener() {
                                @Override
                                public void onChosen(com.wosloveslife.fantasy.ui.funcs.CountdownPickDialog.Result result) {
                                    if (result.duration > 0) {
                                        CountdownAnimView view = new CountdownAnimView(MainActivity.this);
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
                            startActivity(SettingActivity.newStartIntent(MainActivity.this));
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

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(Gravity.START)) {
            mDrawerLayout.closeDrawer(Gravity.START);
            return;
        }
        super.onBackPressed();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

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

        return super.onCreateOptionsMenu(menu);
    }
}
