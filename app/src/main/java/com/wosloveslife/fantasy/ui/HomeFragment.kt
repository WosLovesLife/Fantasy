package com.wosloveslife.fantasy.ui

import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import base.BaseFragment
import com.wosloveslife.fantasy.R
import com.wosloveslife.fantasy.dao.bean.NavigationItem
import com.wosloveslife.fantasy.ui.funcs.CountdownAnimView
import com.wosloveslife.fantasy.ui.funcs.CountdownPickDialog
import com.wosloveslife.fantasy.ui.swapablenavigation.SwapNavigationAdapter
import com.wosloveslife.fantasy.ui.swapablenavigation.VerticalSwapItemTouchHelperCallBack
import com.wosloveslife.fantasy.v2.player.Controller
import com.wosloveslife.fantasy.v2.player.PlayEventAdapter
import java.util.*

/**
 * Created by zhangh on 2017/1/2.
 */
class HomeFragment : BaseFragment() {
    companion object {
        fun newInstance(): HomeFragment {
            val args = Bundle()

            val fragment = HomeFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private var mDrawerLayout: DrawerLayout? = null
    private var mControlView: ControlView? = null
    private var mCardViewNavigation: CardView? = null
    private var mRvNavigation: RecyclerView? = null
    private var mToolbar: Toolbar? = null

    //=============
    private var mNavigationAdapter: SwapNavigationAdapter? = null
    private var mController: Controller? = null

    //=============
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

        mController!!.addListener(object : PlayEventAdapter() {
            override fun onPause() {
                updateNvCountdown()
            }
        })

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

    private fun generateNavigationItems(): List<NavigationItem> {
        val navigationItems = ArrayList<NavigationItem>()
        val item = NavigationItem(0, R.drawable.ic_phone, "本地音乐")
        navigationItems.add(item)
        val item5 = NavigationItem(3, 0, "工具")
        navigationItems.add(item5)
        val item6 = NavigationItem(0, 1, R.drawable.ic_countdown_tiemr, "定时停止播放")
        navigationItems.add(item6)
        val item7 = NavigationItem(0, 1, R.drawable.ic_setting, "设置")
        navigationItems.add(item7)
        return navigationItems
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

    override fun onBackPressed(): Boolean {
        if (mDrawerLayout!!.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout!!.closeDrawer(Gravity.LEFT)
            return true
        }
        return super.onBackPressed()
    }

    //==========================================事件================================================

}
