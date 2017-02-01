package com.yesing.blibrary_wos;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yesing.blibrary_wos.utils.assist.WLogger;

/**
 * Created by YesingBeijing on 2016/9/19.
 */
public class AmountPickerView extends LinearLayout {

    private OnAmountChangedListener mOnAmountChangedListener;

    private TextView mDecrease;
    private TextView mIncrease;
    private EditText mEditText;

    /** 当前的值, 默认值是1, 最小是1 */
    private int mAmount = 1;

    public AmountPickerView(Context context) {
        this(context, null);
    }

    public AmountPickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AmountPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mDecrease = new TextView(getContext());
        mEditText = new EditText(getContext());
        mIncrease = new TextView(getContext());

        addView(mDecrease);
        addView(mEditText);
        addView(mIncrease);

        setBg(getBackground());

        init();
    }

    private void setBg(Drawable background) {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            view.setBackgroundDrawable(background);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setSize(mDecrease);
        setSize(mIncrease);
        setSize(mEditText);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void setSize(View view) {
        LinearLayout.LayoutParams params = (LayoutParams) view.getLayoutParams();
        params.width = 0;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        params.weight = 1;
        view.setLayoutParams(params);
    }

    private void init() {
        initPicker(mDecrease);
        mDecrease.setText("-");
        mDecrease.setTextSize(18);
        mDecrease.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setAmount(mAmount - 1);
            }
        });

        initPicker(mIncrease);
        mIncrease.setText("+");
        mIncrease.setTextSize(18);
        mIncrease.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setAmount(mAmount + 1);
            }
        });

        initEditor();
    }

    private void initPicker(TextView textView) {
        textView.setClickable(true);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(18);
    }

    private void initEditor() {
        mEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEditText.setGravity(Gravity.CENTER);
        mEditText.setTextSize(18);
        setAmount(mAmount);

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count < 1) mEditText.setText("1");

                int amount;
                try {
                    amount = Integer.parseInt(s.toString());
                    amount = amount < 1 ? 1 : amount;

                } catch (NumberFormatException e) {
                    WLogger.e(e, "数字转换错误");
                    amount = 1;
                }
                mEditText.setSelection(mEditText.getText().length());

                if (amount == mAmount) return;

                mAmount = amount;

                if (mOnAmountChangedListener != null) {
                    mOnAmountChangedListener.onAmountChanged(amount);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public void setAmount(int amount) {
        amount = amount < 1 ? 1 : amount;

        mEditText.setText(String.valueOf(amount));
        mAmount = amount;
    }

    public int getAmount() {
        int amount;
        try {
            amount = Integer.parseInt(mEditText.getText().toString());
        } catch (NumberFormatException e) {
            WLogger.e(e, "数字转换错误");
            amount = 1;
        }
        return amount;
    }

    public interface OnAmountChangedListener {
        void onAmountChanged(int amount);
    }

    public void setOnAmountChangedListener(OnAmountChangedListener listener) {
        mOnAmountChangedListener = listener;
    }
}
