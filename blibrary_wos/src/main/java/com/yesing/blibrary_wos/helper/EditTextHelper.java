package com.yesing.blibrary_wos.helper;

import android.text.InputFilter;
import android.text.InputType;
import android.widget.EditText;

import com.yesing.blibrary_wos.utils.viewUtils.CashierInputFilter;

/**
 * Created by YesingBeijing on 2016/12/28.
 */
public class EditTextHelper {
    EditText mEditText;

    private EditTextHelper(EditText editText) {
        mEditText = editText;
    }

    public static EditTextHelper newInstance(EditText editText) {
        return new EditTextHelper(editText);
    }

    public void toMoneyMode() {
//        mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(9)});
        mEditText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
        mEditText.setFilters(new InputFilter[]{new CashierInputFilter()});
        mEditText.setSingleLine();
        mEditText.setMaxLines(1);
        mEditText.setLines(1);

//        mEditText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (s.length() < mEditText.getText().length()) return;
//
//                String str = s.toString().trim();
//
//                int selectionEnd = mEditText.getSelectionEnd();
//                if (str.startsWith(".")) {
//                    mEditText.setText("0" + str);
//                    mEditText.setSelection(selectionEnd);
//                    return;
//                }
//
//                try {
//                    Float.parseFloat(str);
//                    int i = str.lastIndexOf(".");
//                    if (i > 0 && i < str.length() - 2) {
//                        CharSequence charSequence = str.subSequence(0, i + 2);
//                        mEditText.setText(charSequence);
//                        mEditText.setSelection(selectionEnd);
//                    }
//                } catch (Throwable e) {
//                    Toaster.showShort(mEditText.getContext(), "包含非法字符");
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//            }
//        });
    }
}
