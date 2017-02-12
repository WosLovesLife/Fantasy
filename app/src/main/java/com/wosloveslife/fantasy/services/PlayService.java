package com.wosloveslife.fantasy.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
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
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;
import com.wosloveslife.fantasy.R;
import com.wosloveslife.fantasy.adapter.ExoPlayerEventListenerAdapter;
import com.wosloveslife.fantasy.adapter.SubscriberAdapter;
import com.wosloveslife.fantasy.bean.BMusic;
import com.wosloveslife.fantasy.broadcast.AudioBroadcast;
import com.wosloveslife.fantasy.broadcast.BroadcastManager;
import com.wosloveslife.fantasy.helper.FileDataSourceFactory;
import com.wosloveslife.fantasy.helper.SPHelper;
import com.wosloveslife.fantasy.interfaces.IPlay;
import com.wosloveslife.fantasy.manager.MusicManager;
import com.wosloveslife.fantasy.ui.MusicListActivity;
import com.yesing.blibrary_wos.utils.assist.Toaster;
import com.yesing.blibrary_wos.utils.assist.WLogger;
import com.yesing.blibrary_wos.utils.screenAdaptation.Dp2Px;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by zhangh on 2017/1/17.
 */

public class PlayService extends Service {
    //=============常量
    //------循环规则
    /** 列表循环 */
    public static final int PLAY_ORDER_LIST = 0;
    /** 单曲循环 */
    public static final int PLAY_ORDER_CIRCLE = 1;
    /** 列表单次 */
    public static final int PLAY_ORDER_ONCE = 2;

    //=============
    Context mContext;

    //=============ExoPlayer相关
    private SimpleExoPlayer mPlayer;
    private DefaultBandwidthMeter mBandwidthMeter;
    public DataSource.Factory mDataSourceFactory;
    public ExtractorsFactory mExtractorsFactory;

    //============数据
    BMusic mCurrentMusic;

    //============变量
    public int mPlayOrder;
    private boolean mSaveCache;

    //============定时自动关闭服务
    boolean mCountdownUp;
    boolean mStopPlay;
    long mCountdownTargetTimestamp;
    boolean mCloseAfterPlayComplete;

    List<PlayStateListener> mPlayStateListeners = new ArrayList<>();
    private AudioManager mAudioManager;
    private Notification mNotification;
    private RemoteViews mRemoteViews;

    public PlayService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        WLogger.w("onBind(Intent intent)");
        return new PlayBinder();
    }

    public class PlayBinder extends Binder implements IPlay {
        //========================================播放相关-start====================================
        public void play(BMusic music) {
            PlayService.this.play(music);
        }

        public void togglePlayOrPause() {
            PlayService.this.togglePlayOrPause();
        }

        @Override
        public void play() {
            PlayService.this.play();
        }

        @Override
        public void pause() {
            PlayService.this.pause();
        }

        @Override
        public void next() {
            PlayService.this.next();
        }

        @Override
        public void previous() {
            PlayService.this.previous();
        }

        /**
         * 跳转进度
         *
         * @param progress 0~100
         */
        @Override
        public void setProgress(int progress) {
            PlayService.this.setProgress(progress);
        }

        //========================================播放相关-end======================================

        public SimpleExoPlayer getExoPlayer() {
            return PlayService.this.getExoPlayer();
        }

        public BMusic getCurrentMusic() {
            return PlayService.this.getCurrentMusic();
        }

        public boolean isPlaying() {
            return PlayService.this.isPlaying();
        }

        //========================================监听相关-start====================================
        public boolean addPlayStateListener(PlayStateListener listener) {
            return PlayService.this.addPlayStateListener(listener);
        }

        public boolean removePlayStateListener(PlayStateListener listener) {
            return PlayService.this.removePlayStateListener(listener);
        }

        public void setCountdown(long pickDate, boolean closeAfterPlayComplete) {
            mCountdownUp = pickDate > 0;
            mCountdownTargetTimestamp = System.currentTimeMillis() + pickDate;
            mCloseAfterPlayComplete = closeAfterPlayComplete;
        }

        /** 如果没有开启计时器或者计时器未完成,则返回false. */
        public boolean isCountdown() {
            return mCountdownUp;
        }

        public boolean isCloseAfterPlayComplete() {
            return mCloseAfterPlayComplete;
        }
    }

    //==============================================================================================
    //==============================================================================================
    //==============================================================================================

    //========================================播放相关-start====================================

    /**
     * 播放给定的某一首歌曲资源<br/>
     * 将歌曲资源序列化存储本地<br/>
     * 如果资源为null,尝试暂停现有播放<br/>
     *
     * @param music 要播放的歌曲资源
     */
    public void play(BMusic music) {
        mCurrentMusic = music;
        if (mCurrentMusic != null) {
            prepare(music.path);
            play();
            notifyNotification(true);
            SPHelper.getInstance().save("current_music", new Gson().toJson(mCurrentMusic));
        } else {
            pause();
        }
    }

    /**
     * 切换播放/暂停两种状态.
     */
    public void togglePlayOrPause() {
        if (mPlayer.getPlayWhenReady()) {
            pause();
        } else {
            play();
        }
    }

    /**
     * 开始播放
     */
    public void play() {
        mStopPlay = false;
        if (mCurrentMusic != null) {
            mForcePlay = true;
            registerAudioFocus();
            mPlayer.setPlayWhenReady(true);
        } else {
            next();
        }
    }

    /**
     * 暂停播放
     */
    public void pause() {
        if (mCurrentMusic != null) {
            /* 在这里重置该值, 避免影响后续的播放状态 */
            mSavePlay = false;
            mPlayer.setPlayWhenReady(false);
        }
    }

    /**
     * 播放下一首<br/>
     * 如果下一首没有了,则获取第1首歌曲<br/>
     * 如果第一首歌也没有了,则抛出异常
     */
    public void next() {
        BMusic next = MusicManager.getInstance().getNext(mCurrentMusic);
        if (next == null) {
            next = MusicManager.getInstance().getFirst();
        }

        if (next == null) {
            WLogger.e("next(); 获取下一首歌曲失败");
            //todo 传递异常
            return;
        }

        play(next);
    }

    /**
     * 播放上一首<br/>
     * 如果上一首没有了,则获取最后一首歌曲<br/>
     * 如果最后一首歌也没有了,则抛出异常
     */
    public void previous() {
        BMusic previous = MusicManager.getInstance().getPrevious(mCurrentMusic);
        /* 如果下一首没有了,则获取第1首歌曲 */
        if (previous == null) {
            previous = MusicManager.getInstance().getLast();
        }

        /* 如果第一首歌也没有了,则说明发生了异常状况 */
        if (previous == null) {
            WLogger.e("next(); 获取上一首歌曲失败");
        }

        play(previous);
    }

    /**
     * 跳转进度
     *
     * @param progress 0~100
     */
    public void setProgress(int progress) {
        mPlayer.seekTo((long) (progress / 100f * mPlayer.getDuration()));
    }

    //========================================播放相关-end======================================

    public SimpleExoPlayer getExoPlayer() {
        return mPlayer;
    }

    public BMusic getCurrentMusic() {
        return mCurrentMusic;
    }

    public boolean isPlaying() {
        return mPlayer.getPlayWhenReady();
    }

    //========================================监听相关-start====================================
    public boolean addPlayStateListener(PlayStateListener listener) {
        return listener != null && mPlayStateListeners.add(listener);
    }

    public boolean removePlayStateListener(PlayStateListener listener) {
        return listener != null && mPlayStateListeners.remove(listener);
    }

    //==============================================================================================
    //==============================================================================================
    //==============================================================================================

    public interface PlayStateListener {
        void onPlay(BMusic music);

        void onPause();
    }

    /** 只在服务第一次创建时调用 */
    @Override
    public void onCreate() {
        super.onCreate();
        WLogger.w("onCreate()");

        mContext = this;

        initPlayer();

        mPlayer.addListener(new ExoPlayerEventListenerAdapter() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (mCountdownUp
                        && (playbackState == ExoPlayer.STATE_ENDED || !playWhenReady)
                        && System.currentTimeMillis() >= mCountdownTargetTimestamp) {
                    mStopPlay = true;
                    resetPlayService();
                    for (PlayStateListener listener : mPlayStateListeners) {
                        listener.onPause();
                    }
                } else if (!mStopPlay && playbackState == ExoPlayer.STATE_ENDED) {
                    next();
                } else {
                    if (playWhenReady) {
                        /* 这里是为了播放回调先调用而焦点变化回调后调用导致刚点下播放键就立即又被暂停 */
                        new Thread() {
                            @Override
                            public void run() {
                                super.run();
                                try {
                                    sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                mForcePlay = false;
                            }
                        }.start();
                        notifyNotification(false);
                        for (PlayStateListener listener : mPlayStateListeners) {
                            listener.onPlay(mCurrentMusic);
                        }
                    } else {
                        mForcePlay = false;
                        abandonAudioFocus();
                        notifyNotification(false);
                        for (PlayStateListener listener : mPlayStateListeners) {
                            listener.onPause();
                        }
                    }
                }
            }
        });

        String currentMusic = SPHelper.getInstance().get("current_music", "");
        if (!TextUtils.isEmpty(currentMusic)) {
            mCurrentMusic = new Gson().fromJson(currentMusic, BMusic.class);
            prepare(mCurrentMusic.path);
        }

        EventBus.getDefault().register(this);
        BroadcastManager.getInstance().init(this);
    }

    private void notifyNotification(boolean newRes) {
        if (mNotification == null) {
            createNotification();
        }

        if (newRes) {
            if (mCurrentMusic != null) {
                MusicManager.getAlbum(mCurrentMusic.path, Dp2Px.toPX(this, 80))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SubscriberAdapter<Bitmap>() {
                            @Override
                            public void onNext(Bitmap bitmap) {
                                super.onNext(bitmap);
                                mRemoteViews.setImageViewBitmap(R.id.iv_album, bitmap);
                                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                manager.notify(3, mNotification);
                            }
                        });
            }
            mRemoteViews.setImageViewResource(R.id.iv_album, R.drawable.ic_portrait_chicken_174);
        }
        mRemoteViews.setTextViewText(R.id.tv_title, mCurrentMusic != null ? mCurrentMusic.title : "暂无播放歌曲");
        mRemoteViews.setTextViewText(R.id.tv_artist, mCurrentMusic != null ? mCurrentMusic.artist : "");
        mRemoteViews.setImageViewResource(R.id.iv_play_btn, isPlaying() ? R.drawable.ic_pause_black : R.drawable.ic_play_arrow_black);
        mRemoteViews.setImageViewResource(R.id.iv_favor, MusicManager.getInstance().isFavored(mCurrentMusic) ? R.drawable.ic_favored : R.drawable.ic_favorite_border);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(3, mNotification);
    }

    /** 生成通知栏通知对象和RemoteView对象,应该只在成员变量为null时调用,避免重复创建对象 */
    private void createNotification() {
        /////// RemoteView-start //////
        /* 自定义通知栏的样式, 参1是应用包名,因为需要系统来托管此服务, 参2是自定义布局样式 */
        mRemoteViews = new RemoteViews(getPackageName(), R.layout.remote_view);
        mRemoteViews.setImageViewResource(R.id.iv_previous_btn, R.drawable.ic_skip_previous_black);
        mRemoteViews.setImageViewResource(R.id.iv_next_btn, R.drawable.ic_skip_next_black);
        Intent intent = new Intent(this, MusicListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.remote_view_open, pendingIntent);

        mNotification = new NotificationCompat.Builder(this)
                .setTicker("Fantasy已启动")
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_portrait_chicken_174)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(false)
                .setShowWhen(true)
                .build();

        mNotification.contentView = mRemoteViews;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mNotification.bigContentView = mRemoteViews;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mNotification.headsUpContentView = mRemoteViews;
        }
    }

    /**
     * 由于暂时不知道的原因,第二次起播放时或者其他程序播放时都会失去焦点<br/>
     * 但是为了防止用户按下播放键后立即暂停,设置了一个boolean<br/>
     * 变量的值会在歌曲播放100毫秒后被置为false.<br/>
     * 现在的效果是, 歌曲播放100毫秒后如果回调了失去焦点,表示有其他应用抢夺焦点,则暂停<br/>
     * 如果100毫秒内回调,认为是再次播放造成的,则忽略.
     */
    boolean mForcePlay;

    /**
     * 监听声音播放的焦点变化,当有别的应用抢占了焦点时(例如拨打电话).终止播放,并在重新获取焦点后根据之前保存的状态而判断是否继续播放
     */
    private void registerAudioFocus() {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        }
        mAudioManager.requestAudioFocus(new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_LOSS: // 失去焦点,
                        if (!mForcePlay) {
                            pause();
                        }
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: // 短暂失去焦点
                        boolean playing = isPlaying();
                        pause();
                        mSavePlay = playing;
                        break;
                    case AudioManager.AUDIOFOCUS_REQUEST_GRANTED: // 获得焦点
                        if (mSavePlay) {
                            /* 在这里重置该值, 避免影响后续的播放状态 */
                            mSavePlay = false;
                            play();
                        }
                        break;
                }
                WLogger.d("requestAudioFocus onAudioFocusChange : [focusChange] = " + focusChange);
            }
        }, 1, AudioManager.AUDIOFOCUS_GAIN);
    }

    private void abandonAudioFocus() {
        if (mAudioManager != null) {
            mAudioManager.abandonAudioFocus(null);
        }
    }

    boolean mSavePlay;

    /** 每次调用startService()启用该服务时都被调用 */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        WLogger.w("onStartCommand(Intent intent, int flags, int startId); startId = " + startId);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 后台强制结束程序,会执行该回调. 因此可以在该生命周期中执行善后工作
     * 但是注意: 某些意外情况,例如在软件管理中强行结束应用, 不会走任何回调
     */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        WLogger.w("onTaskRemoved(Intent rootIntent)");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        WLogger.w("onDestroy()");
        release();
    }

    private void release() {
        EventBus.getDefault().unregister(this);
        BroadcastManager.getInstance().unregisterAllBroadcasts();
        if (mPlayer != null) {
            resetPlayService();
            mPlayer.release();
        }
        abandonAudioFocus();
    }

    //==================================播放逻辑-start==============================================

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
        mPlayer = ExoPlayerFactory.newSimpleInstance(mContext, trackSelector, loadControl);

        //=========step3准备播放
        // Measures bandwidth during playback. Can be null if not required.
        mBandwidthMeter = new DefaultBandwidthMeter();
        // Produces DataSource instances through which media data is loaded.
        mDataSourceFactory = new DefaultDataSourceFactory(mContext, Util.getUserAgent(mContext, "com.wosloveslife.fantasy"), mBandwidthMeter);
        // Produces Extractor instances for parsing the media data.
        mExtractorsFactory = new DefaultExtractorsFactory();
    }

    private void prepare(String path) {
        if (TextUtils.isEmpty(path)) {
            /* todo 没有播放地址,传递错误 */
            Toaster.showShort(this, "找不到播放地址");
            return;
        }

        Uri uri = Uri.parse(path);
        SimpleCache simpleCache = new SimpleCache(getExternalFilesDir(Environment.DIRECTORY_MUSIC), new NoOpCacheEvictor());
        /* 这个文件大小指的是单个缓存文件的尺寸2MB, 例如一首歌10MB,则需要5个缓存文件
         * 同时它也会影响到缓存的时机.
         * Exo会一次性缓存一个缓存文件大小的数据,然后当播放进度接近缓存的末端时开启新的缓存文件
         * 可以参考{@link FileDataSource#write(byte[] buffer, int offset, int length)}
         * 方法和{@link FileDataSource#openNextOutputStream()}方法
         * 当一个文件写满后会开启一个新的文件继续写入*/
        long cacheFileSize = CacheDataSource.DEFAULT_MAX_CACHE_FILE_SIZE;

        /* 构建带缓存的Source */
        CacheDataSourceFactory cacheDataSourceFactory = new CacheDataSourceFactory(
                simpleCache,
                mDataSourceFactory,
                new FileDataSourceFactory(null),
                new com.wosloveslife.fantasy.helper.CacheDataSinkFactory(simpleCache, cacheFileSize, null),
                1,
                new CacheDataSource.EventListener() {
                    @Override
                    public void onCachedBytesRead(long cacheSizeBytes, long cachedBytesRead) {
                        Logger.d("cacheSizeBytes = " + cacheSizeBytes + "; cachedBytesRead = " + cachedBytesRead);
                    }
                });

        /* 将带缓存的source作为资源传入,构建普通多媒体播放资源 */
        MediaSource videoSource = new ExtractorMediaSource(uri, cacheDataSourceFactory, mExtractorsFactory, new Handler(getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

            }
        }, new ExtractorMediaSource.EventListener() {
            @Override
            public void onLoadError(IOException error) {

            }
        });

        mPlayer.prepare(videoSource);
    }

    private void resetPlayService() {
        mCountdownUp = false;
        mCloseAfterPlayComplete = false;
        pause();
    }

    //==========================================事件处理============================================

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBroadcastEvent(AudioBroadcast.AudioNoisyEvent event) {
        pause();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCountDownTimerTick(CountdownTimerService.CountDownEvent event) {
        if (event == null) return;

        /** 当前启用了定时器并且定时器进度结束并且没有设定播放完当前歌曲后结束或者当前为暂停状态则立即暂停播放并通知客户 */
        if (mCountdownUp && event.totalMillis == event.millisUntilFinished && (!mCloseAfterPlayComplete || !mPlayer.getPlayWhenReady())) {
            //结束播放服务
            resetPlayService();
            for (PlayStateListener listener : mPlayStateListeners) {
                listener.onPause();
            }
        }
    }

    //==================================记录生命周期-忽略===========================================

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        WLogger.w("onConfigurationChanged(Configuration newConfig)");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        WLogger.w("onLowMemory()");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        WLogger.w("onTrimMemory(int level); level = " + level);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        WLogger.w("onUnbind(Intent intent)");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        WLogger.w("onRebind(Intent intent)");
    }

    @Override
    protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(fd, writer, args);
        WLogger.w("dump(FileDescriptor fd, PrintWriter writer, String[] args)");
    }
}
