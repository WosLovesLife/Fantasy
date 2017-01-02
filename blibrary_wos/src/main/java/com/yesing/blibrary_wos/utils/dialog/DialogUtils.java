package com.yesing.blibrary_wos.utils.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;

import com.mmga.metroloading.MetroLoadingView;
import com.yesing.blibrary_wos.R;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

/**
 * Dialog工具类
 * Created by zhangh on 2016/9/3.
 */
public class DialogUtils {
    /** 返回一个精美的LoadingDialog控制对象,具体见 : {@link DialogLoading} */
    public static DialogLoading createLoadingDialog(Context context) {
        return createLoadingDialog(context, true);
    }

    /**
     * 返回一个精美的LoadingDialog控制对象,具体见 : {@link DialogLoading}
     *
     * @param edgeCancelable 为true时,点击空白地区取消Dialog,为false则不消失.
     */
    public static DialogLoading createLoadingDialog(Context context, boolean edgeCancelable) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_dialog_loading, null);
        MetroLoadingView loading = (MetroLoadingView) view.findViewById(R.id.metro_loading);
        AlertDialog alertDialog = new AlertDialog
                .Builder(context)
                .setTitle(null)
                .setView(view)
                .setCancelable(edgeCancelable)
                .create();
        return new DialogLoading(alertDialog, loading);
    }

    /**
     * 在Dialog.show()之前调用该方法,使dialog点击确定和中立按钮时不触发dismiss
     * 要想让Dialog消失,请手动调用Dialog.dismiss()
     */
    public static void setNotDismissOnClick(Dialog dialog) {
        try {
            Field field = dialog.getClass().getDeclaredField("mAlert");
            field.setAccessible(true);
            //   获得mAlert变量的值
            Object obj = field.get(dialog);
            field = obj.getClass().getDeclaredField("mHandler");
            field.setAccessible(true);
            //   修改mHandler变量的值，使用新的ButtonHandler类
            field.set(obj, new ButtonHandler(dialog));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class ButtonHandler extends Handler {
        private WeakReference<DialogInterface> mDialog;

        public ButtonHandler(DialogInterface dialog) {
            mDialog = new WeakReference<DialogInterface>(dialog);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DialogInterface.BUTTON_POSITIVE:
                case DialogInterface.BUTTON_NEGATIVE:
                case DialogInterface.BUTTON_NEUTRAL:
                    ((DialogInterface.OnClickListener) msg.obj).onClick(mDialog.get(), msg.what);
                    break;
            }
        }
    }
}
