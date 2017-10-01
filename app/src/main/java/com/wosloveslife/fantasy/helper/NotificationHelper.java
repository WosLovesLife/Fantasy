package com.wosloveslife.fantasy.helper;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.WorkerThread;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.wosloveslife.dao.Audio;
import com.wosloveslife.fantasy.R;
import com.wosloveslife.fantasy.adapter.SubscriberAdapter;
import com.wosloveslife.fantasy.manager.MusicManager;
import com.wosloveslife.fantasy.services.PlayService;
import com.wosloveslife.fantasy.ui.MusicListActivity;
import com.yesing.blibrary_wos.utils.screenAdaptation.Dp2Px;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by zhangh on 2017/2/13.
 */

public class NotificationHelper {
    private final Service mService;
    private final int mAlbumSize;
    private Notification mNotification;
    private RemoteViews mRemoteViews;
    private Audio mCurrentMusic;

    private boolean mIsShown;

    public NotificationHelper(Service service) {
        if (service == null) {
            throw new NullPointerException("Service 不能为null");
        }
        mService = service;
        mAlbumSize = Dp2Px.toPX(mService, 80);
    }

    /** 生成通知栏通知对象和RemoteView对象,应该只在成员变量为null时调用,避免重复创建对象 */
    @WorkerThread
    private void createNotification() {
        /////// RemoteView-start //////
        /* 自定义通知栏的样式, 参1是应用包名,因为需要系统来托管此服务, 参2是自定义布局样式 */
        mRemoteViews = new RemoteViews(mService.getPackageName(), R.layout.remote_view);
        mRemoteViews.setImageViewResource(R.id.iv_previous_btn, R.drawable.ic_skip_previous_black);
        mRemoteViews.setImageViewResource(R.id.iv_next_btn, R.drawable.ic_skip_next_black);
        Intent intent = new Intent(mService, MusicListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mService, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.remote_view_open, pendingIntent);

        Intent preIntent = new Intent(mService, PlayService.class);
        preIntent.putExtra("action", 0);
        PendingIntent prePendingIntent = PendingIntent.getService(mService, 0, preIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.iv_previous_btn, prePendingIntent);

        Intent playIntent = new Intent(mService, PlayService.class);
        playIntent.putExtra("action", 1);
        PendingIntent playPendingIntent = PendingIntent.getService(mService, 1, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.iv_play_btn, playPendingIntent);

        Intent nextIntent = new Intent(mService, PlayService.class);
        nextIntent.putExtra("action", 2);
        PendingIntent nextPendingIntent = PendingIntent.getService(mService, 2, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.iv_next_btn, nextPendingIntent);

        Intent favorIntent = new Intent(mService, PlayService.class);
        favorIntent.putExtra("action", 3);
        PendingIntent favorPendingIntent = PendingIntent.getService(mService, 3, favorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.iv_favor, favorPendingIntent);

        Intent lrcIntent = new Intent(mService, PlayService.class);
        lrcIntent.putExtra("action", 4);
        PendingIntent lrcPendingIntent = PendingIntent.getBroadcast(mService, 4, lrcIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.tv_lrc, lrcPendingIntent);

        mNotification = new NotificationCompat.Builder(mService)
                .setTicker(mCurrentMusic != null ? "正在播放: " + mCurrentMusic.getTitle() : "Fantasy已启动")
                .setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
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

    public void update(final boolean isPlaying, final Audio music) {
        Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                boolean needAlbum = mCurrentMusic != music;
                if (mCurrentMusic != null && music != null) {
                    needAlbum = !TextUtils.equals(mCurrentMusic.getAlbum(), music.getAlbum());
                }
                mCurrentMusic = music;
                if (mNotification == null) {
                    createNotification();
                    needAlbum = true;
                }

                if (false) {
                    if (music != null) {
                        MusicManager.getInstance().getAlbum(music.getId(), mAlbumSize)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new SubscriberAdapter<Bitmap>() {
                                    @Override
                                    public void onNext(Bitmap bitmap) {
                                        super.onNext(bitmap);
                                        if (bitmap != null) {
                                            mRemoteViews.setImageViewBitmap(R.id.iv_album, bitmap);
                                        } else {
                                            mRemoteViews.setImageViewResource(R.id.iv_album, R.drawable.ic_portrait_chicken_174);
                                        }
                                        show();
                                    }
                                });
                    } else {
                        mRemoteViews.setImageViewResource(R.id.iv_album, R.drawable.ic_portrait_chicken_174);
                    }
                }
                mRemoteViews.setTextViewText(R.id.tv_title, music != null ? music.getTitle() : "暂无播放歌曲");
                mRemoteViews.setTextViewText(R.id.tv_artist, music != null ? music.getArtist() : "");
                mRemoteViews.setImageViewResource(R.id.iv_play_btn, isPlaying ? R.drawable.ic_pause_black : R.drawable.ic_play_arrow_black);
                mRemoteViews.setImageViewResource(R.id.iv_favor, music != null && MusicManager.getInstance().isFavorite(music.getId()) ? R.drawable.ic_favored : R.drawable.ic_favorite_border_matt);

                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        })
                .observeOn(Schedulers.computation())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new SubscriberAdapter<Object>() {
                    @Override
                    public void onNext(Object o) {
                        super.onNext(o);
                        show();
                    }
                });
    }

    private void show() {
        if (mService != null) {
            mService.startForeground(3, mNotification);
            mIsShown = true;
        }
    }

    public boolean isShown() {
        return mIsShown;
    }
}
