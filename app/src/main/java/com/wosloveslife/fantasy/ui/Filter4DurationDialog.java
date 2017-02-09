package com.wosloveslife.fantasy.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import com.wosloveslife.fantasy.R;
import com.wosloveslife.fantasy.manager.CustomConfiguration;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by zhangh on 2017/2/9.
 */

public class Filter4DurationDialog extends DialogFragment {

    @BindView(R.id.numberPicker_minute)
    NumberPicker mNumberPickerMinute;
    @BindView(R.id.numberPicker_second)
    NumberPicker mNumberPickerSecond;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_filter_duration, null);
        ButterKnife.bind(this, view);
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(null)
                .setView(view)
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                init();
            }
        });
        return dialog;
    }

    private void init(){
        mNumberPickerMinute.setMaxValue(59);
        mNumberPickerMinute.setMinValue(0);
        mNumberPickerSecond.setMaxValue(59);
        mNumberPickerSecond.setMinValue(0);

        int minDuration = CustomConfiguration.getMinDuration();
        if (minDuration > 0) {
            int minute = minDuration / 60;
            int second = minDuration % 60;
            if (minute > 0) {
                mNumberPickerMinute.setValue(minute);
            } else {
                mNumberPickerSecond.setValue(second);
            }
        }
    }

    @OnClick({R.id.btn_cancel, R.id.btn_submit})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_cancel:
                getDialog().dismiss();
                break;
            case R.id.btn_submit:
                int minute = mNumberPickerMinute.getValue();
                int second = mNumberPickerSecond.getValue();
                int time = minute * 60 + second;
                CustomConfiguration.saveMinDuration(time);
                onChosen(time);
                getDialog().dismiss();
                break;
        }
    }

    private void onChosen(int second) {
        Fragment targetFragment = getTargetFragment();
        if (targetFragment != null) {
            Intent intent = new Intent();
            intent.putExtra("time", second);
            targetFragment.onActivityResult(targetFragment.getTargetRequestCode(), Activity.RESULT_OK, intent);
        }
    }

    public static int getMinDurationSecond(Intent intent) {
        if (intent == null) return -1;
        return intent.getIntExtra("time", -1);
    }
}
