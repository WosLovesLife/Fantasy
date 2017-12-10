package com.wosloveslife.fantasy.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.view.ViewCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.*
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.*
import base.BaseFragment
import com.wosloveslife.dao.Audio
import com.wosloveslife.dao.Sheet
import com.wosloveslife.fantasy.R
import com.wosloveslife.fantasy.dao.bean.NavigationItem
import com.wosloveslife.fantasy.manager.MusicManager
import com.wosloveslife.fantasy.services.CountdownTimerService
import com.wosloveslife.fantasy.ui.funcs.CountdownAnimView
import com.wosloveslife.fantasy.ui.funcs.CountdownPickDialog
import com.wosloveslife.fantasy.ui.swapablenavigation.SwapNavigationAdapter
import com.wosloveslife.fantasy.ui.swapablenavigation.VerticalSwapItemTouchHelperCallBack
import com.wosloveslife.fantasy.v2.player.Controller
import com.wosloveslife.fantasy.v2.player.PlayEvent
import com.wosloveslife.player.PlayerException
import com.yesing.blibrary_wos.utils.systemUtils.SystemServiceUtils
import java.util.*

/**
 * Created by zhangh on 2017/1/2.
 */
class HomeFragment : BaseFragment() {

    private var mDrawerLayout: DrawerLayout? = null
    private var mControlView: ControlView? = null
    private var mCardViewNavigation: CardView? = null
    private var mRvNavigation: RecyclerView? = null
    private var mToolbar: Toolbar? = null

    private val mSnackbar: Snackbar? = null

    //=============
    private var mNavigationAdapter: SwapNavigationAdapter? = null
    private var mController: Controller? = null

    //=============
    internal var mCurrentMusic: Audio? = null

    internal var mIsCountdown: Boolean = false

    companion object {
        private val REQ_CODE_TIMER = 0

        fun newInstance(): HomeFragment {

            val args = Bundle()

            val fragment = HomeFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

        mController = Controller.sInstance
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mDrawerLayout = view!!.findViewById(R.id.drawer_layout) as DrawerLayout?
        mControlView = view.findViewById(R.id.control_view) as ControlView?
        mCardViewNavigation = view.findViewById(R.id.card_view_navigation) as CardView?
        mRvNavigation = view.findViewById(R.id.rv_navigation) as RecyclerView?

        initToolbar()

        initListFragment()

        mController!!.getState().addListener(object : PlayEvent {
            override fun onPlay(audio: Audio) {
                syncVisual(MusicManager.getInstance().musicConfig.mCurrentMusic)
            }

            override fun onPause() {
                mControlView!!.syncPlayView(mCurrentMusic)
                if (mIsCountdown/* && !mPlayBinder.isCountdown() todo Countdown*/) {
                    mIsCountdown = false
                    updateNvCountdown()
                }
            }

            override fun onSeekTo(progress: Long) {

            }

            override fun onStop() {

            }

            override fun onBuffering(bufferProgress: Long) {

            }

            override fun onError(e: PlayerException) {

            }
        })

        syncVisual(MusicManager.getInstance().musicConfig.mCurrentMusic)
        updateNvCountdown()
    }

    private fun initToolbar() {
        mToolbar = mControlView!!.toolbar
        val activity = activity as AppCompatActivity
        activity.setSupportActionBar(mToolbar)
        val toggle = ActionBarDrawerToggle(getActivity(), mDrawerLayout, mToolbar, R.string.app_name, R.string.app_name)
        toggle.syncState()
        mToolbar!!.title = "本地音乐"

        ViewCompat.setElevation(mCardViewNavigation, 8f)
        mRvNavigation!!.layoutManager = LinearLayoutManager(context)
        mNavigationAdapter = SwapNavigationAdapter()
        val header = LayoutInflater.from(getActivity()).inflate(R.layout.layout_navigation_header, mRvNavigation, false)
        mNavigationAdapter!!.addHeaderView(header)
        mNavigationAdapter!!.setData(generateNavigationItems())
        mNavigationAdapter!!.setOnItemClickListener { navigationItem, v, position ->
            // 在抽屉关闭后再进行操作,避免动画卡顿. 官方解释:
            // Avoid performing expensive operations such as layout during animation as it can cause stuttering;
            //try to perform expensive operations during the STATE_IDLE state.
            when (navigationItem.mTitle) {
                "定时停止播放" -> {
                    val viewGroup = getActivity().window.decorView.findViewById(android.R.id.content) as ViewGroup
                    val countdownPickDialog = CountdownPickDialog.newInstance(getActivity()) { result ->
                        if (result.duration > 0) {
                            val view = CountdownAnimView(context)
                            view.show(viewGroup, result.duration)
                        }
                        updateNvCountdown()
                    }
                    countdownPickDialog.show(viewGroup)
                    mDrawerLayout!!.postDelayed({ mDrawerLayout!!.closeDrawer(Gravity.START) }, 200)
                }
                "设置" -> {
                    // TODO: 17/6/19 跳转SettingPage
                    startActivity(SettingActivity.newStartIntent(getActivity()))
                    mDrawerLayout!!.postDelayed({ mDrawerLayout!!.closeDrawer(Gravity.LEFT) }, 200)
                }
            }
        }
        mRvNavigation!!.adapter = mNavigationAdapter
        val itemTouchHelper = ItemTouchHelper(VerticalSwapItemTouchHelperCallBack(mNavigationAdapter))
        itemTouchHelper.attachToRecyclerView(mRvNavigation)
    }

    private fun initListFragment() {
        if (childFragmentManager.findFragmentById(R.id.fl_container) != null) {
            return
        }

        val audioListFragment = AudioListFragment.newInstance()
        childFragmentManager.beginTransaction().add(R.id.fl_container, audioListFragment).commit()
    }

    // TODO: 17/6/18 切换歌单
    private fun onChangeSheet(sheet: Sheet) {
        mToolbar!!.title = sheet.title
        //        mNavigationAdapter.setChosenPosition(mNavigationAdapter.getHeadersCount() + 2);
        //        MusicManager.getInstance().changeSheet(sheet);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            REQ_CODE_TIMER // 用户选择了计时关闭后, 如果开启,则将时间和是否播放完后再结束同步给计时器服务和播放服务以及导航栏中更新倒计时进度
            -> {
                val pickDate = CountdownPickDialog.getPickDate(data)
                val closeAfterPlayComplete = CountdownPickDialog.isCloseAfterPlayComplete(data)
                mIsCountdown = pickDate > 0
                if (mIsCountdown) {
                    val intent = CountdownTimerService.createIntent(activity, pickDate)
                    activity.startService(intent)
                    // TODO: 2017/9/3 Countdown
                } else if (SystemServiceUtils.isServiceRunning(activity, CountdownTimerService::class.java.name)) {
                    val intent = CountdownTimerService.stopService(activity)
                    activity.startService(intent)
                    // TODO: 2017/9/3 Countdown
                }
                updateNvCountdown()
            }
        }
    }

    private fun generateNavigationItems(): List<NavigationItem> {
        val navigationItems = ArrayList<NavigationItem>()
        val item = NavigationItem(0, R.drawable.ic_phone, "本地音乐")
        val item2 = NavigationItem(1, R.drawable.ic_favorite_border, "我的收藏")
        val item3 = NavigationItem(1, R.drawable.ic_clock, "最近播放")
        val item4 = NavigationItem(1, R.drawable.ic_download, "下载管理")
        navigationItems.add(item)
        navigationItems.add(item2)
        navigationItems.add(item3)
        //        navigationItems.add(item4);
        val item5 = NavigationItem(3, 0, "工具")
        navigationItems.add(item5)
        val item6 = NavigationItem(0, 1, R.drawable.ic_countdown_tiemr, "定时停止播放")
        navigationItems.add(item6)
        val item7 = NavigationItem(0, 1, R.drawable.ic_setting, "设置")
        navigationItems.add(item7)
        return navigationItems
    }

    //=======================================UI和逻辑的同步=========================================

    private fun syncVisual(music: Audio?) {
        mCurrentMusic = music
        mControlView!!.syncPlayView(mCurrentMusic)
    }

    private fun updateNvCountdown() {
        var index = 0
        for (i in 0 until mNavigationAdapter!!.realItemCount) {
            val normalData = mNavigationAdapter!!.getNormalData(i)
            if (normalData!!.mTitle == "定时停止播放") {
                index = i
                break
            }
        }
        mNavigationAdapter!!.notifyItemChanged(index + mNavigationAdapter!!.headersCount)
    }

    //=======================================UI和逻辑的同步-end=====================================

    override fun onBackPressed(): Boolean {
        if (mDrawerLayout!!.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout!!.closeDrawer(Gravity.LEFT)
            return true
        }
        return super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.menu_search, menu)
        super.onCreateOptionsMenu(menu, inflater)
        val searchView = menu!!.findItem(R.id.item_search).actionView as SearchView
        searchView.queryHint = "搜索歌单内歌曲/歌手/专辑..."
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

    //    public void onScannedMusic(MusicManager.OnScannedMusicEvent event) {
    //        if (event == null) return;
    //
    //        setData(event.mBMusicList);
    //
    //        if (mSnackbar == null) {
    //            mSnackbar = Snackbar.make(mRecyclerView, "找到了" + event.mBMusicList.size() + "首音乐", Snackbar.LENGTH_LONG);
    //        } else {
    //            mSnackbar.setText("找到了" + event.mBMusicList.size() + "首音乐");
    //            mSnackbar.setDuration(Snackbar.LENGTH_LONG);
    //        }
    //
    //        mSnackbar.show();
    //    }
    //
    //    public void onGotMusic(MusicManager.OnGotMusicEvent event) {
    //        if (event == null) return;
    //        setData(event.mBMusicList);
    //    }
    //
    //    public void onAddMusic(MusicManager.OnAddMusic event) {
    //        if (event == null || event.mMusic == null) return;
    //        Audio music = event.mMusic;
    //        if (TextUtils.equals(MusicManager.getInstance().getMusicConfig().mCurrentSheetId, event.mSheetId)) {
    //            mAdapter.addItem(music, 0);
    //        }
    //        if (music.equals(mCurrentMusic)) {
    //            mControlView.syncPlayView(music);
    //        }
    //    }
    //
    //    public void onRemoveMusic(MusicManager.OnRemoveMusic event) {
    //        if (event == null || event.mMusic == null) return;
    //        Audio music = event.mMusic;
    //        if (TextUtils.equals(MusicManager.getInstance().getMusicConfig().mCurrentSheetId, event.mBelongTo)) {
    //            int startPosition = mAdapter.getNormalPosition(music);
    //            mAdapter.removeItem(music);
    //            mAdapter.notifyItemRangeChanged(startPosition, mAdapter.getRealItemCount() - startPosition);
    //        }
    //        if (music.equals(mCurrentMusic)) {
    //            mControlView.syncPlayView(music);
    //        }
    //    }
    //
    //    public void onMusicChanged(MusicManager.OnMusicChanged event) {
    //        if (event == null || event.mMusic == null) return;
    //        Audio music = event.mMusic;
    //        if (TextUtils.equals(MusicManager.getInstance().getMusicConfig().mCurrentSheetId, event.mSheetId) && TextUtils.equals(MusicManager.getInstance().getMusicConfig().mCurrentSheetId, "2")) {
    //            int oldPosition = mAdapter.getNormalPosition(event.mMusic);
    //            if (mRecyclerView.getItemAnimator().isRunning()) {
    //                mRecyclerView.getItemAnimator().endAnimations();
    //            }
    //            mAdapter.removeItemNotNotify(oldPosition);
    //            mAdapter.addItemNotNofity(music, 0);
    //            mAdapter.notifyItemRangeChanged(0, oldPosition + 1);
    //        }
    //        if (music.equals(mCurrentMusic)) {
    //            mControlView.syncPlayView(music);
    //        }
    //    }
    //
    //    public void onCountDownTimerTick(CountdownTimerService.CountDownEvent event) {
    //        if (event == null) return;
    //        updateNvCountdown();
    //    }
}
