package com.wosloveslife.fantasy.ui.funcs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.wosloveslife.fantasy.R;
import com.wosloveslife.fantasy.manager.SettingConfig;
import com.wosloveslife.player.PlayService;
import com.yesing.blibrary_wos.utils.screenAdaptation.Dp2Px;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by leonard on 17/6/19.
 */

public class CountdownPickDialog extends FrameLayout {
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

    OnChosenListener mOnChosenListener;
    private View mRootView;

    public interface OnChosenListener {
        void onChosen(Result result);
    }

    public static class Result {
        public long duration;
        public boolean closeAfterPlayComplete;

        public Result(long duration, boolean closeAfterPlayComplete) {
            this.duration = duration;
            this.closeAfterPlayComplete = closeAfterPlayComplete;
        }
    }

    public static CountdownPickDialog newInstance(Context context, OnChosenListener listener) {
        CountdownPickDialog dialog = new CountdownPickDialog(context);
        dialog.setOnChosenListener(listener);
        return dialog;
    }

    public CountdownPickDialog(@NonNull Context context) {
        this(context, null);
    }

    public CountdownPickDialog(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CountdownPickDialog(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        onCreateView();
    }

    public void setOnChosenListener(OnChosenListener listener) {
        mOnChosenListener = listener;
    }

    public void onCreateView() {
        mRootView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_pick_countdown, this, false);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        int margin = Dp2Px.toPX(getContext(), 24);
        params.setMargins(margin, margin * 2, margin, margin * 2);
        addView(mRootView, params);
        ButterKnife.bind(this);

        setBackgroundResource(R.color.gray_bg_translucent);
        setAlpha(0);
        mRootView.setScaleX(0.7f);
        mRootView.setScaleY(0.7f);
        mRootView.setAlpha(0.3f);

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

        mLastCustomTime = SettingConfig.getCustomCountdown();
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

        mCheckBox.setChecked(SettingConfig.isCloseAfterPlayEnd());

        checkSubmitBenEnable();

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
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
                dismiss();
                break;
            case R.id.btn_submit:
                int hour = mNumberPickerHour.getValue();
                int minute = mNumberPickerMinute.getValue();
                int time = hour * 60 + minute;
                SettingConfig.saveCustomCountdown(time);
                onChosen(time);
                break;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        animate().alpha(1).setDuration(160).start();
        mRootView.animate().scaleX(1).scaleY(1).alpha(1).setDuration(160).start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        animate().setListener(null);
        mRootView.animate().setListener(null);
    }

    public void show(ViewGroup parent) {
        parent.addView(this);
    }

    boolean dismissing;

    private void dismiss() {
        if (dismissing) {
            return;
        }
        dismissing = true;
        animate().alpha(0).setDuration(160).start();
        mRootView.animate().setListener(null);
        mRootView.animate().scaleX(0.7f).scaleY(0.7f).alpha(0.3f).setDuration(160).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                detach();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                detach();
            }
        }).start();
    }

    private void detach() {
        ViewParent parent = getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(this);
        } else {
            dismissing = false;
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
        SettingConfig.saveCloseAfterPlayEnd(mCheckBox.isChecked());
        SettingConfig.saveCountdownTime(System.currentTimeMillis() + minute * 60 * 1000L);

        Intent intent = new Intent(getContext(), PlayService.class);
        PendingIntent pendingIntent = PendingIntent.getService(getContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + minute * 60 * 1000L, pendingIntent);

        if (mOnChosenListener != null) {
            mOnChosenListener.onChosen(new Result(minute * 60 * 1000L, mCheckBox.isChecked()));
        }
        dismiss();
    }

    public static long getPickDate(Intent intent) {
        if (intent == null) return -1;
        return intent.getLongExtra("time", -1);
    }

    public static boolean isCloseAfterPlayComplete(Intent intent) {
        return intent != null && intent.getBooleanExtra("closeAfterPlayComplete", false);
    }
}
