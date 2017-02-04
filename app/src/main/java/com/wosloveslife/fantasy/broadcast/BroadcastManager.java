package com.wosloveslife.fantasy.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangh on 2016/8/7.
 */
public class BroadcastManager {

    public static BroadcastManager sBroadcastManager;
    private Context mContext;

    /* 广播 */
    List<BroadcastReceiver> mBroadcastReceivers;

    private BroadcastManager() {
    }

    public static BroadcastManager getInstance() {
        if (sBroadcastManager == null) {
            synchronized (BroadcastManager.class) {
                if (sBroadcastManager == null) {
                    sBroadcastManager = new BroadcastManager();
                }
            }
        }
        return sBroadcastManager;
    }

    public void init(Context context){
        mContext = context;
        mBroadcastReceivers = new ArrayList<>();
        registerAllBroadcasts();
    }

    /** 注册各类广播事件 */
    public void registerAllBroadcasts() {
        AudioBroadcast audioBroadcast = new AudioBroadcast();
        registerAudioBroadcast(audioBroadcast);
        mBroadcastReceivers.add(audioBroadcast);
    }

    /* 卸载广播 */
    public void unregisterAllBroadcasts() {
        for (BroadcastReceiver broadcastReceiver : mBroadcastReceivers) {
            mContext.unregisterReceiver(broadcastReceiver);
        }
    }

    private void registerAudioBroadcast(AudioBroadcast audioBroadcast) {
        if (audioBroadcast == null) return;
        IntentFilter intentFilter = AudioBroadcast.getIntentFilter();
        mContext.registerReceiver(audioBroadcast, intentFilter);
    }
}
