package com.wosloveslife.fantasy.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by zhangh on 2017/2/7.
 */

public class CountdownTimerService extends Service {
    private long mFuture;
    private long mMillisUntilFinished;
    private CountDownTimer mCountDownTimer;

    public static Intent createIntent(Context context, long future) {
        Intent intent = new Intent(context, CountdownTimerService.class);
        intent.putExtra("future", future);
        return intent;
    }

    public static Intent stopService(Context context) {
        Intent intent = new Intent(context, CountdownTimerService.class);
        intent.putExtra("stopSelf", true);
        return intent;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean stopSelf = intent.getBooleanExtra("stopSelf", false);
        if (stopSelf) {
            if (mCountDownTimer != null) {
                mCountDownTimer.cancel();
            }
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        long future = intent.getLongExtra("future", 0);
        if (future <= 0) {
            EventBus.getDefault().post(new CountDownEvent(mFuture, mMillisUntilFinished));
            return super.onStartCommand(intent, flags, startId);
        }

        mFuture = future;

        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
        mCountDownTimer = new CountDownTimer(mFuture, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                mMillisUntilFinished = millisUntilFinished;
                EventBus.getDefault().post(new CountDownEvent(mFuture, millisUntilFinished));
            }

            @Override
            public void onFinish() {
                EventBus.getDefault().post(new CountDownEvent(mFuture, mFuture));
                stopSelf();
            }
        };
        mCountDownTimer.start();

        return super.onStartCommand(intent, flags, startId);
    }

    public static class CountDownEvent {
        public long totalMillis;
        public long millisUntilFinished;

        CountDownEvent(long totalMillis, long millisUntilFinished) {
            this.totalMillis = totalMillis;
            this.millisUntilFinished = millisUntilFinished;
        }
    }
}
