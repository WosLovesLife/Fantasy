package com.yesing.blibrary_wos.utils.systemUtils;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.yesing.blibrary_wos.utils.assist.Toaster;

/**
 * Created by YesingBeijing on 2016/9/20.
 */
public class SystemServiceUtils {
    /**
     * 将文字复制到剪切板
     *
     * @param context   用于获取系统服务的上下文
     * @param text      要发送到剪切板的文字
     * @param alertText Toast提示的文字, 如果为null则不提示
     */
    public static void copy2Clipboard(Context context, CharSequence text, String alertText) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("text", text);
        clipboard.setPrimaryClip(clipData);

        if (!TextUtils.isEmpty(alertText)) {
            Toaster.showShort(context, alertText);
        }
    }

    /**
     * 将文字复制到剪切板, 发送后默认会弹出Toast提示 "复制成功~"
     * 如果不想要提示,请调用重载方法{@link SystemServiceUtils#copy2Clipboard(Context, CharSequence, String)}
     *
     * @param context 用于获取系统服务的上下文
     * @param text    要发送到剪切板的文字
     */
    public static void copy2Clipboard(Context context, CharSequence text) {
        copy2Clipboard(context, text, "已复制到剪切板~");
    }

    public static void hideSoftKeyBoard(Activity activity) {
        View currentFocus = activity.getCurrentFocus();
        if (currentFocus != null) {
            ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(currentFocus.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static void hideSoftKeyBoard(Activity activity, View view) {
        if (view == null) {
            hideSoftKeyBoard(activity);
            return;
        }
        ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void showKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }

    public static void showKeyboard(Activity activity) {
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public static void showKeyboardAlways(Activity activity) {
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    public static void toggleKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
