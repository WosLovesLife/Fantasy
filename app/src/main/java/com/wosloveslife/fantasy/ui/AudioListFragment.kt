package com.wosloveslife.fantasy.ui

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import base.BaseFragment
import com.wosloveslife.dao.Audio
import com.wosloveslife.fantasy.R
import com.wosloveslife.fantasy.adapter.MusicListAdapter
import com.wosloveslife.fantasy.event.Event
import com.wosloveslife.fantasy.event.RxBus
import com.wosloveslife.fantasy.manager.MusicManager
import com.wosloveslife.fantasy.utils.DividerDecoration
import com.wosloveslife.fantasy.v2.player.Controller
import com.wosloveslife.fantasy.v2.player.PlayEvent
import com.wosloveslife.player.PlayerException
import com.yesing.blibrary_wos.utils.screenAdaptation.Dp2Px
import rx.Subscription
import rx.functions.Action1

/**
 * Created by zhangh on 2017/1/2.
 */
class AudioListFragment : BaseFragment() {
    private var mAdapter: MusicListAdapter? = null
    private var mLayoutManager: LinearLayoutManager? = null
    private var mRecyclerView: RecyclerView? = null

    private var mController: Controller? = null

    private var mSubscription: Subscription? = null

    companion object {
        fun newInstance(): AudioListFragment {
            val args = Bundle()

            val fragment = AudioListFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        retainInstance = true

        mController = Controller.sInstance
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_audio_list, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mRecyclerView = view?.findViewById(R.id.rv) as RecyclerView
        mLayoutManager = LinearLayoutManager(activity)
        mRecyclerView!!.layoutManager = mLayoutManager
        mRecyclerView!!.addItemDecoration(DividerDecoration(
                ColorDrawable(resources.getColor(R.color.gray_light)),
                Math.max(Dp2Px.toPX(context, 0.5f), 1f).toInt(),
                Dp2Px.toPX(context, 48)))

        mAdapter = MusicListAdapter()
        mAdapter?.setOnItemClickListener { music, v, position ->
            val currentMusic = MusicManager.getInstance().musicConfig.mCurrentMusic
            val playing = mController!!.getState().isPlaying()
            if (currentMusic == null || currentMusic != music || !playing) {
                mController!!.play(music)
            } else {
                mController!!.pause()
            }
        }

        mRecyclerView!!.adapter = mAdapter;

        mController!!.getState().addListener(object : PlayEvent {
            override fun onPlay(audio: Audio) {
                syncVisual(MusicManager.getInstance().musicConfig.mCurrentMusic)
            }

            override fun onPause() {
                mAdapter?.togglePlay(false)
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
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setData(MusicManager.getInstance().musicConfig.mMusicList)

        bindEvent()
    }

    override fun onDestroy() {
        super.onDestroy()

        mSubscription?.unsubscribe()
    }

    private fun syncVisual(music: Audio?) {
        val position = mAdapter!!.getNormalPosition(music)
        mAdapter!!.setChosenItem(position, mController!!.getState().isPlaying())
    }

    private fun setData(musicList: List<Audio>) {
        mAdapter?.setData(musicList)

        // TODO: 2017/11/19 没有音乐,显示空白页面
    }

    private fun handleErrorState() {
        // TODO: 错误提示及后续处理
    }

    //==========================================事件================================================

    private fun bindEvent() {
        mSubscription = RxBus.getDefault()
                .toObservable(Event::class.java)
                .subscribe(object : Action1<Event> {
                    override fun call(event: Event?) {
                        handleEvent(event!!)
                    }
                })
    }

    private fun handleEvent(event: Event) {
        if (event.action == Event.SHEET_LOADED) {
            setData(event.data as List<Audio>)
        }
    }
}
