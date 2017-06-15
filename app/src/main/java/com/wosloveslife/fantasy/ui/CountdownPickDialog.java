package com.wosloveslife.fantasy.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.wosloveslife.fantasy.R;
import com.wosloveslife.fantasy.manager.CustomConfiguration;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by zhangh on 2017/2/7.
 */

public class CountdownPickDialog extends DialogFragment {

    @BindView(R.id.ll_pick)
    LinearLayout mLlPick;
    @BindView(R.id.fl_custom)
    FrameLayout mFlCustom;
    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @BindView(R.id.tv_custom)
    TextView mTvCustom;
    @BindView(R.id.checkbox)
    CheckBox mCheckBox;

    @BindView(R.id.numberPicker_hour)
    NumberPicker mNumberPickerHour;
    @BindView(R.id.numberPicker_minute)
    NumberPicker mNumberPickerMinute;
    @BindView(R.id.btn_submit)
    Button mBtnSubmit;
    private int mLastCustomTime;

    public static CountdownPickDialog newInstance(OnChosenListener listener) {

        Bundle args = new Bundle();

        CountdownPickDialog fragment = new CountdownPickDialog();
        fragment.setArguments(args);
        fragment.setOnChosenListener(listener);
        return fragment;
    }

    OnChosenListener mOnChosenListener;

    interface OnChosenListener{
        void onChosen(Result result);
    }

    static class Result{
        public int duration;
        public boolean closeAfterPlayComplete;
    }

    public void setOnChosenListener(OnChosenListener listener){
        mOnChosenListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mNumberPickerHour.setMaxValue(23);
        mNumberPickerHour.setMinValue(0);
        mNumberPickerHour.setValue(0);
        mNumberPickerMinute.setMaxValue(59);
        mNumberPickerMinute.setMinValue(0);
        mNumberPickerMinute.setValue(0);

        mNumberPickerHour.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                checkSubmitBenEnable();
            }
        });
        mNumberPickerMinute.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                checkSubmitBenEnable();
            }
        });

        mLastCustomTime = CustomConfiguration.getCustomCountdown();
        if (mLastCustomTime > 0) {
            int hour = mLastCustomTime / 60;
            int minute = mLastCustomTime % 60;
            String time;
            if (hour > 0) {
                time = hour + "小时" + minute + "分钟";
                mNumberPickerHour.setValue(hour);
            } else {
                time = minute + "分钟";
                mNumberPickerMinute.setValue(minute);
            }
            mTvCustom.setText("自定义 (" + time + "后) ");
        }

        mCheckBox.setChecked(CustomConfiguration.isCloseAfterPlayEnd());

        checkSubmitBenEnable();

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_pick_countdown, null);
        ButterKnife.bind(this, view);
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(null)
                .setView(view)
                .create();
        return dialog;
    }

    @OnClick({R.id.tv_close, R.id.tv_10m, R.id.tv_20m, R.id.tv_30m, R.id.tv_45m, R.id.tv_60m,
            R.id.tv_custom, R.id.checkbox, R.id.fl_accessory, R.id.btn_cancel, R.id.btn_submit})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_close:
                onChosen(-1);
                break;
            case R.id.tv_10m:
                onChosen(10);
                break;
            case R.id.tv_20m:
                onChosen(20);
                break;
            case R.id.tv_30m:
                onChosen(30);
                break;
            case R.id.tv_45m:
                onChosen(45);
                break;
            case R.id.tv_60m:
                onChosen(60);
                break;
            case R.id.tv_custom:
                customTime();
                break;
            case R.id.fl_accessory:
                if (mCheckBox.isChecked()) {
                    mCheckBox.setChecked(false);
                } else {
                    mCheckBox.setChecked(true);
                }
                break;
            case R.id.btn_cancel:
                getDialog().dismiss();
                break;
            case R.id.btn_submit:
                int hour = mNumberPickerHour.getValue();
                int minute = mNumberPickerMinute.getValue();
                int time = hour * 60 + minute;
                CustomConfiguration.saveCustomCountdown(time);
                onChosen(time);
                break;
        }
    }

    private void customTime() {
        mLlPick.setVisibility(View.GONE);
        mFlCustom.setVisibility(View.VISIBLE);
        mTvTitle.setText("自定义停止播放时间");
    }

    private void checkSubmitBenEnable() {
        mBtnSubmit.setEnabled(mNumberPickerHour.getValue() != 0 || mNumberPickerMinute.getValue() != 0);
    }

    private void onChosen(int minute) {
        CustomConfiguration.saveCloseAfterPlayEnd(mCheckBox.isChecked());
        Fragment targetFragment = getTargetFragment();
        if (targetFragment != null) {
            Intent intent = new Intent();
            intent.putExtra("time", minute * 60 * 1000L);
            intent.putExtra("closeAfterPlayComplete", mCheckBox.isChecked());
            targetFragment.onActivityResult(targetFragment.getTargetRequestCode(), Activity.RESULT_OK, intent);
        }
        getDialog().dismiss();
    }

    public static long getPickDate(Intent intent) {
        if (intent == null) return -1;
        return intent.getLongExtra("time", -1);
    }

    public static boolean isCloseAfterPlayComplete(Intent intent) {
        return intent != null && intent.getBooleanExtra("closeAfterPlayComplete", false);
    }
}
