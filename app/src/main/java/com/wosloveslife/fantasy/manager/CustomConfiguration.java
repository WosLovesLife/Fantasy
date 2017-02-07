package com.wosloveslife.fantasy.manager;

import com.wosloveslife.fantasy.helper.SPHelper;

/**
 * Created by zhangh on 2017/2/7.
 */

public class CustomConfiguration {
    public static final String KEY_CUSTOM_COUNTDOWN_DELAY = "com.wosloveslife.fantasy.ui.CountdownPickDialog.KEY_CUSTOM_COUNTDOWN_DELAY";
    public static final String KEY_CLOSE_AFTER_PLAY_END = "com.wosloveslife.fantasy.ui.CountdownPickDialog.KEY_CLOSE_AFTER_PLAY_END";

    public static int getCustomCountdown() {
        return SPHelper.getInstance().get(KEY_CUSTOM_COUNTDOWN_DELAY, -1);
    }

    /**
     *
     * @param customCountdown 单位: 分钟
     */
    public static void saveCustomCountdown(int customCountdown){
        SPHelper.getInstance().save(KEY_CUSTOM_COUNTDOWN_DELAY, customCountdown);
    }
    public static boolean isCloseAfterPlayEnd() {
        return SPHelper.getInstance().get(KEY_CLOSE_AFTER_PLAY_END, false);
    }

    public static void saveCloseAfterPlayEnd(boolean closeAfterPlayEnd){
        SPHelper.getInstance().save(KEY_CLOSE_AFTER_PLAY_END, closeAfterPlayEnd);
    }
}
