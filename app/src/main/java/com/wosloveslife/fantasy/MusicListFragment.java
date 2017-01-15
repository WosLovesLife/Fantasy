package com.wosloveslife.fantasy;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
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

//    @BindView(R.id.iv_album)
//    ImageView mIvAlbum;
//    @BindView(R.id.tv_title)
//    TextView mTvTitle;
//    @BindView(R.id.tv_artist)
//    TextView mTvArtist;
//    @BindView(R.id.tv_progress)
//    TextView mTvProgress;
//    @BindView(R.id.tv_duration)
//    TextView mTvDuration;
//    @BindView(R.id.iv_previous_btn)
//    ImageView mIvPreviousBtn;
//    @BindView(R.id.iv_play_btn)
//    ImageView mIvPlayBtn;
//    @BindView(R.id.iv_next_btn)
//    ImageView mIvNextBtn;
//    @BindView(R.id.pb_progress)
//    ProgressBar mPbProgress;

    @BindView(R.id.control_view)
    ControlView mControlView;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    private Snackbar mSnackbar;

    //=============
    private MusicListAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    //=============
    BMusic mCurrentMusic;

    //=============
    private SimpleExoPlayer mPlayer;
    private DefaultBandwidthMeter mBandwidthMeter;
    private DataSource.Factory mDataSourceFactory;
    private ExtractorsFactory mExtractorsFactory;

    public static MusicListFragment newInstance() {

        Bundle args = new Bundle();

        MusicListFragment fragment = new MusicListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected View setContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    public void initView() {
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new MusicListAdapter();
        mAdapter.setOnItemClickListener(new BaseRecyclerViewAdapter.OnItemClickListener<BMusic>() {
            @Override
            public void onItemClick(BMusic music, View v, int position) {
                if (mCurrentMusic != music) {
                    prepare(music.path);
                    mCurrentMusic = music;
                }else {
                    mPlayer.setPlayWhenReady(!mPlayer.getPlayWhenReady());
                }
                mControlView.syncPlayView(music);
            }
        });
        mRecyclerView.setAdapter(mAdapter);

        onRefreshChanged(new MusicManager.RefreshEventM(MusicManager.getInstance().isLoading()));

        initPlayer();

        /** 监听控制面板中的事件 */
        mControlView.setControlListener(new ControlView.ControlListener() {
            @Override
            public void previous() {
                mAdapter.toPrevious();
            }

            @Override
            public void next() {
                mAdapter.toNext();
            }

            @Override
            public void play() {
                mAdapter.togglePlay();
            }

            @Override
            public void pause() {
                mAdapter.togglePlay();
            }
        });
    }

    @Override
    protected void getData() {
        super.getData();

        mAdapter.setData(MusicManager.getInstance().getMusicList());
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


    private void initPlayer() {
        //==========step1初始操作
        // 1. Create a default TrackSelector
        Handler mainHandler = new Handler();
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        // 2. Create a default LoadControl
        LoadControl loadControl = new DefaultLoadControl();

        // 3. Create the player
        mPlayer = ExoPlayerFactory.newSimpleInstance(getActivity(), trackSelector, loadControl);

        //=========step3准备播放
        // Measures bandwidth during playback. Can be null if not required.
        mBandwidthMeter = new DefaultBandwidthMeter();
        // Produces DataSource instances through which media data is loaded.
        mDataSourceFactory = new DefaultDataSourceFactory(getActivity(), Util.getUserAgent(getActivity(), "yourApplicationName"), mBandwidthMeter);
        // Produces Extractor instances for parsing the media data.
        mExtractorsFactory = new DefaultExtractorsFactory();

        mControlView.setPlayer(mPlayer);
    }

    private void prepare(String path) {
        Uri uri = Uri.parse(path);
        MediaSource videoSource = new ExtractorMediaSource(uri, mDataSourceFactory, mExtractorsFactory, null, null);
        mPlayer.prepare(videoSource);
        mPlayer.setPlayWhenReady(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPlayer.release();
    }
}
