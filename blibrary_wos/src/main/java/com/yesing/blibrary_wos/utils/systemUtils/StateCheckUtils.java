package com.yesing.blibrary_wos.utils.systemUtils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.text.TextUtils;

import java.util.List;

/**
 * Created by YesingBeijing on 2016/9/5.
 */
public class StateCheckUtils {

    /** 检查Service是否在运行中(无法检测IntentService) */
    public static boolean isServicesRunning(Context con, Class<? extends Service> clazz){
        boolean flag = false;
        ActivityManager am = (ActivityManager) con.getSystemService(Activity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = am.getRunningServices(1000);
        for (ActivityManager.RunningServiceInfo rsi : services) {
            ComponentName componentName = rsi.service;
            if(TextUtils.equals(componentName.getClassName(), clazz.getName())){
                flag = true;
            }
        }
        return flag;
    }
}
