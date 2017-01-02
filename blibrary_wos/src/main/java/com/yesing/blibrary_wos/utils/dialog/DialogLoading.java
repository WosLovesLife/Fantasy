package com.yesing.blibrary_wos.utils.dialog;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;

import com.mmga.metroloading.MetroLoadingView;

/**
 * 一个LoadingDialog类, 通过简单的方法即可显示或消失,不需要自己做判断
 */
public class DialogLoading {
    private AlertDialog mAlertDialog;
    private MetroLoadingView mMetroLoadingView;

    public DialogLoading(AlertDialog dialog, MetroLoadingView loadingView) {
        mAlertDialog = dialog;
        mAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (mMetroLoadingView.isAnimating()) {
                    mMetroLoadingView.stop();
                }
            }
        });
        mMetroLoadingView = loadingView;
    }

    public void show() {
        if (!mAlertDialog.isShowing()) {
            mAlertDialog.show();
            mMetroLoadingView.start();
        }
    }

    public void dismiss() {
        if (mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
            mMetroLoadingView.stop();
        }
    }

    public void dismissDelayed(int timeDelay) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dismiss();
            }
        }, timeDelay);
    }

    public boolean isShowing() {
        return mAlertDialog.isShowing();
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        mAlertDialog.setOnDismissListener(onDismissListener);
    }
}