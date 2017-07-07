package com.wosloveslife.fantasy.ui;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.wosloveslife.dao.Audio;
import com.wosloveslife.fantasy.R;
import com.wosloveslife.fantasy.adapter.ExoPlayerEventListenerAdapter;
import com.wosloveslife.fantasy.adapter.MusicListAdapter;
import com.wosloveslife.fantasy.manager.MusicManager;
import com.wosloveslife.fantasy.services.PlayerController;
import com.wosloveslife.fantasy.utils.DividerDecoration;
import com.yesing.blibrary_wos.baserecyclerviewadapter.adapter.BaseRecyclerViewAdapter;
import com.yesing.blibrary_wos.utils.assist.WLogger;
import com.yesing.blibrary_wos.utils.screenAdaptation.Dp2Px;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * 管理当前歌单列表
 * Created by zhangh on 2017/6/19.
 */

public class MusicListV extends FrameLayout {
    RecyclerView mRecyclerView;
    ControlView mControlView;

    private Snackbar mSnackbar;

    //=============
    private MusicListAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private PlayerController mController;
    private MusicManager mMusicManager;

    public MusicListV(@NonNull Context context) {
        this(context, null);
    }

    public MusicListV(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MusicListV(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        bindToManager();
        setContentView();

        mController.addListener(new ExoPlayerEventListenerAdapter() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                super.onPlayerStateChanged(playWhenReady, playbackState);
                if (playWhenReady) {
                    syncVisual(mMusicManager.getMusicConfig().mCurrentMusic);
                } else {
                    mControlView.syncPlayView(MusicManager.getInstance().getMusicConfig().mCurrentMusic);
                    mAdapter.togglePlay(false);
                }
            }
        });

        syncVisual(mMusicManager.getMusicConfig().mCurrentMusic);

        setData(MusicManager.getInstance().getMusicConfig().mMusicList);
    }

    private void bindToManager() {
        mMusicManager = MusicManager.getInstance();
        mController = PlayerController.getInstance();
    }

    //========================================生命周期-start========================================

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        WLogger.d("onStart() : 页面显示, 时间 = " + System.currentTimeMillis());
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        EventBus.getDefault().unregister(this);
    }

    //========================================生命周期-end========================================

    protected void setContentView() {
        mControlView = new ControlView(getContext());
        mRecyclerView = new RecyclerView(getContext());

        mControlView.addView(mRecyclerView);
        addView(mControlView);

        /* 监听控制面板中的事件 */
        // TODO: 2017/7/2 所有地方都直接通过Controller改变播放状态, 再由Controller通知绑定者
        mControlView.setControlListener(new ControlView.ControlListener() {
            @Override
            public void previous() {
                mController.previous();
            }

            @Override
            public void next() {
                mController.next();
            }

            @Override
            public void play() {
                mController.play(null);
            }

            /**
             * 如果暂停时发现当前的状态是有关闭倒计时而服务中以及完成了倒计时,则同步导航栏的倒计时状态
             */
            @Override
            public void pause() {
                mController.pause();
            }
        });

        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new DividerDecoration(
                new ColorDrawable(getResources().getColor(R.color.gray_light)),
                (int) Math.max(Dp2Px.toPX(getContext(), 0.5f), 1),
                Dp2Px.toPX(getContext(), 48)));

        mAdapter = new MusicListAdapter();
        mAdapter.setOnItemClickListener(new BaseRecyclerViewAdapter.OnItemClickListener<Audio>() {
            @Override
            public void onItemClick(final Audio music, View v, int position) {
                String sheetId = mMusicManager.getMusicConfig().mCurrentSheetId;
                Audio currentMusic = mMusicManager.getMusicConfig().mCurrentMusic;
                if (!TextUtils.equals(mMusicManager.getMusicConfig().mCurrentSheetId, sheetId) || TextUtils.equals(sheetId, "2")) {
                    mMusicManager.changeSheet(sheetId);
                }
                if (currentMusic == null || !currentMusic.equals(music)) {
                    mController.play(music);
                } else {
                    /* 播放,暂停当前曲目 */
                    if (mController.isPlaying()) {
                        mController.pause();
                    } else {
                        mController.play(null);
                    }
                }
                mControlView.syncPlayView(music);
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }

    //=======================================UI和逻辑的同步=========================================

    private void syncVisual(Audio music) {
        mControlView.syncPlayView(music);
        int position = mAdapter.getNormalPosition(music);
        mAdapter.setChosenItem(position, mController.isPlaying());
    }

    //=======================================UI和逻辑的同步-end=====================================

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
        if (TextUtils.equals(mMusicManager.getMusicConfig().mCurrentSheetId, event.mSheetId)) {
            mAdapter.addItem(music, 0);
        }
        if (music.equals(mMusicManager.getMusicConfig().mCurrentMusic)) {
            mControlView.syncPlayView(music);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRemoveMusic(MusicManager.OnRemoveMusic event) {
        if (event == null || event.mMusic == null) return;
        Audio music = event.mMusic;
        if (TextUtils.equals(mMusicManager.getMusicConfig().mCurrentSheetId, event.mBelongTo)) {
            int startPosition = mAdapter.getNormalPosition(music);
            mAdapter.removeItem(music);
            mAdapter.notifyItemRangeChanged(startPosition, mAdapter.getRealItemCount() - startPosition);
        }
        if (music.equals(mMusicManager.getMusicConfig().mCurrentMusic)) {
            mControlView.syncPlayView(music);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMusicChanged(MusicManager.OnMusicChanged event) {
        if (event == null || event.mMusic == null) return;
        Audio music = event.mMusic;
        if (TextUtils.equals(mMusicManager.getMusicConfig().mCurrentSheetId, event.mSheetId) && TextUtils.equals(mMusicManager.getMusicConfig().mCurrentSheetId, "2")) {
            int oldPosition = mAdapter.getNormalPosition(event.mMusic);
            if (mRecyclerView.getItemAnimator().isRunning()) {
                mRecyclerView.getItemAnimator().endAnimations();
            }
            mAdapter.removeItemNotNotify(oldPosition);
            mAdapter.addItemNotNofity(music, 0);
            mAdapter.notifyItemRangeChanged(0, oldPosition + 1);
        }
        if (music.equals(mMusicManager.getMusicConfig().mCurrentMusic)) {
            mControlView.syncPlayView(music);
        }
    }

    //==============================================================================================

    /**
     * 显示音乐列表并做状态处理
     *
     * @param musicList 音乐列表
     */
    private void setData(List<Audio> musicList) {
        if (musicList == null || musicList.size() == 0) {
            /* todo 没有音乐,显示空白页面 */
            handleErrorState(true);
            return;
        } else {
            handleErrorState(false);
        }
        mAdapter.setData(musicList);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 处理各种异常状态
     * 没有音乐
     * 无法播放
     */
    private void handleErrorState(boolean noData) {
        if (noData) {
            TextView textView = new TextView(getContext());
            textView.setTextSize(16);
            textView.setTextColor(getResources().getColor(R.color.black_text_matt));
            textView.setGravity(Gravity.CENTER);
            textView.setText("暂时没有数据");
            textView.setTag("error_page");
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            addView(textView, params);
        } else {
            View errorPage = findViewWithTag("error_page");
            if (errorPage != null) {
                removeView(errorPage);
            }
        }
    }
}
