package com.wosloveslife.fantasy.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.text.TextUtils;

import com.wosloveslife.fantasy.event.RxBus;

/**
 * Created by zhangh on 2017/2/4.
 */

public class AudioBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        /* 监听耳机的插入/拔出事件,但是这种方式会有大约一秒钟的延迟,体验很差 */
//        switch (intent.getIntExtra("state", -1)) {
//            case 0:
//                EventBus.getDefault().post(new BroadcastEvent());
//                break;
//            case 1:
//                break;
//        }

        /* 这个广播只是针对有线耳机，或者无线耳机的手机断开连接的事件，监听不到有线耳机和蓝牙耳机的接入,但没有延迟 */
        if (TextUtils.equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY, intent.getAction())) {
            RxBus.getDefault().post(new AudioNoisyEvent());
        }
    }

    public static IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        return intentFilter;
    }

    public static class AudioNoisyEvent {

    }
}
