//package com.wosloveslife.fantasy.manager;
//
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.content.ServiceConnection;
//import android.os.IBinder;
//
//import com.google.android.exoplayer2.ExoPlayer;
//import com.orhanobut.logger.Logger;
//import com.wosloveslife.fantasy.bean.BMusic;
//import com.wosloveslife.fantasy.interfaces.IPlay;
//import com.wosloveslife.fantasy.services.PlayService;
//
///**
// * Created by zhangh on 2017/2/26.
// */
//
//public class ControlManager implements IPlay {
//    private static ControlManager sControlManager;
//    private PlayService.PlayBinder mPlayBinder;
//    Context mContext;
//    private BMusic mCurrentMusic;
//
//    public static ControlManager getInstance() {
//        if (sControlManager == null) {
//            synchronized (ControlManager.class) {
//                if (sControlManager == null) {
//                    sControlManager = new ControlManager();
//                }
//            }
//        }
//        return sControlManager;
//    }
//
//    public void init(Context context) {
//        mContext = context.getApplicationContext();
//        mContext.bindService(new Intent(mContext, PlayService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
//    }
//
//    public void destroy() {
//        mContext.unbindService(mServiceConnection);
//    }
//
//    private ServiceConnection mServiceConnection = new ServiceConnection() {
//        /** bindService()方法执行后, 绑定成功时回调 */
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            Logger.d("连接到播放服务");
//            mPlayBinder = (PlayService.PlayBinder) service;
//        }
//
//        /** 和服务断开连接后回调(比如unbindService()方法执行后) */
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            Logger.d("和服务断开连接");
//        }
//    };
//
//    //========================================播放控制相关==========================================
//
//    @Override
//    public void play(BMusic music) {
//        if (mPlayBinder == null) return;
//        mPlayBinder.play(music);
//    }
//
//    @Override
//    public void play() {
//        if (mPlayBinder == null) return;
//        mPlayBinder.play();
//    }
//
//    @Override
//    public void pause() {
//        if (mPlayBinder == null) return;
//        mPlayBinder.pause();
//    }
//
//    @Override
//    public void next() {
//        if (mPlayBinder == null) return;
//        mPlayBinder.next();
//    }
//
//    @Override
//    public void previous() {
//        if (mPlayBinder == null) return;
//        mPlayBinder.previous();
//    }
//
//    @Override
//    public void seekTo(long progress) {
//        if (mPlayBinder == null) return;
//        mPlayBinder.seekTo(progress);
//    }
//
//    //========================================播放状态相关==========================================
//
//    @Override
//    public void addListener(ExoPlayer.EventListener listener) {
//        if (mPlayBinder == null) return;
//        mPlayBinder.addListener(listener);
//    }
//
//    @Override
//    public boolean isPlaying() {
//        if (mPlayBinder == null) return false;
//        return mPlayBinder.isPlaying();
//    }
//
//    @Override
//    public int getBufferState() {
//        if (mPlayBinder == null) return 0;
//        return mPlayBinder.getBufferState();
//    }
//
//    public BMusic getCurrentMusic() {
//        if (mPlayBinder == null) return null;
//        return mCurrentMusic;
//    }
//
//    public long getCurrentPosition() {
//        if (mPlayBinder == null) return 0;
//        return mPlayBinder.getCurrentPosition();
//    }
//
//    public long getDuration() {
//        if (mPlayBinder == null) return 0;
//        return mPlayBinder.getDuration();
//    }
//
//    public long getBufferedPosition() {
//        if (mPlayBinder == null) return 0;
//        return mPlayBinder.getBufferedPosition();
//    }
//
//    public int getPlaybackState() {
//        if (mPlayBinder == null) return 0;
//        return mPlayBinder.getPlaybackState();
//    }
//
//    //======================================定时关闭相关============================================
//    public boolean isCountdown() {
//        if (mPlayBinder == null) return false;
//        return mPlayBinder.isCountdown();
//    }
//
//    public void setCountdown(long pickDate, boolean closeAfterPlayComplete) {
//        mPlayBinder.setCountdown(pickDate, closeAfterPlayComplete);
//    }
//
//    public boolean isCloseAfterPlayComplete() {
//        if (mPlayBinder == null) return false;
//        return mPlayBinder.isCloseAfterPlayComplete();
//    }
//}
