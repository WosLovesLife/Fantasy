package com.wosloveslife.fantasy.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhangh on 2016/8/7.
 */
public class BroadcastManager {

    private static BroadcastManager sBroadcastManager;
    private Context mContext;

    /* 广播 */
    private Map<String, BroadcastReceiver> mBroadcastReceivers;

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

    public void init(Context context) {
        mContext = context;
        mBroadcastReceivers = new HashMap<>();
    }

    public void register(BroadcastReceiver receiver, IntentFilter intentFilter) {
        mContext.registerReceiver(receiver, intentFilter);
    }

    public void unregister(BroadcastReceiver receiver) {
        mContext.unregisterReceiver(receiver);
    }

    /* 卸载广播 */
    public void unregisterAllBroadcasts() {
        for (BroadcastReceiver broadcastReceiver : mBroadcastReceivers.values()) {
            mContext.unregisterReceiver(broadcastReceiver);
        }
    }
}
