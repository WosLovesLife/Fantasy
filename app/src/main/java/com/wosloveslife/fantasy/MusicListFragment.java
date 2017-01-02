package com.wosloveslife.fantasy;

import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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
import butterknife.OnClick;

/**
 * Created by zhangh on 2017/1/2.
 */
public class MusicListFragment extends BaseFragment {

    @BindView(R.id.iv_album)
    ImageView mIvAlbum;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.tv_artist)
    TextView mTvArtist;
    @BindView(R.id.tv_progress)
    TextView mTvProgress;
    @BindView(R.id.tv_duration)
    TextView mTvDuration;
    @BindView(R.id.iv_previous_btn)
    ImageView mIvPreviousBtn;
    @BindView(R.id.iv_play_btn)
    ImageView mIvPlayBtn;
    @BindView(R.id.iv_next_btn)
    ImageView mIvNextBtn;
    @BindView(R.id.pb_progress)
    ProgressBar mPbProgress;

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    private Snackbar mSnackbar;

    //=============
    private MusicListAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    //=============
    BMusic mCurrentMusic;

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mPbProgress.setProgressBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.transparent)));
        }

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new MusicListAdapter();
        mAdapter.setOnItemClickListener(new BaseRecyclerViewAdapter.OnItemClickListener<BMusic>() {
            @Override
            public void onItemClick(BMusic music, View v, int position) {
                syncPlayView(music);
            }
        });
        mRecyclerView.setAdapter(mAdapter);

        onRefreshChanged(new MusicManager.RefreshEventM(MusicManager.getInstance().isLoading()));
    }

    private void syncPlayView(BMusic music) {
        if (music == null) return;

        if (music.playState == 1) {
            mIvPlayBtn.setImageResource(R.drawable.ic_pause);
        } else {
            mIvPlayBtn.setImageResource(R.drawable.ic_play_arrow);
        }

        if (music.equals(mCurrentMusic)) {
            return;
        }
        mCurrentMusic = music;

        Glide.with(getActivity())
                .load(music.album)
                .placeholder(R.color.gray_disable)
                .crossFade()
                .into(mIvAlbum);
        mTvTitle.setText(TextUtils.isEmpty(music.title) ? "未知" : music.title);
        mTvArtist.setText(TextUtils.isEmpty(music.artist) ? "未知" : music.artist);
        mTvProgress.setText("00:00");
        mTvDuration.setText(DateFormat.format("mm:ss", music.duration).toString());
    }

    @Override
    protected void getData() {
        super.getData();

        mAdapter.setData(MusicManager.getInstance().getMusicList());
    }

    @OnClick({R.id.iv_previous_btn, R.id.iv_play_btn, R.id.iv_next_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_previous_btn:
                mAdapter.toPrevious();
                break;
            case R.id.iv_play_btn:
                mAdapter.togglePlay();
                break;
            case R.id.iv_next_btn:
                mAdapter.toNext();
                break;
        }

        syncPlayView(mAdapter.getNormalData(mAdapter.getPlayingIndex()));
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
}
