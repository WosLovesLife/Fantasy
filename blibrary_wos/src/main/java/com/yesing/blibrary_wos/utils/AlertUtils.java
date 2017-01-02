package com.yesing.blibrary_wos.utils;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;


/**
 * 一个弹出各种提示的工具类
 * 目前是根据Exception类型, 弹出各种SnackBar
 * Created by YesingBeijing on 2016/9/27.
 */
public class AlertUtils {
    public static String getErrorMsg(Throwable e){
        if (e instanceof ConnectException || e instanceof UnknownHostException) {
            return "网络开小差~,请检查网络连接.";
        } else if (e instanceof TimeoutException || e instanceof SocketTimeoutException) {
            return "连接超时,请检查网络状态";
        } else {
            return "";
        }
    }
}