 /**
 * Copyright 2020 ArcSoft Corporation Limited. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package com.arcsoft.arcfacesingle.view.widgets;

import android.content.Context;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

public class SeparateEditText extends EditText {

    public static final int LENGTH_CHAR_MAX = 19;

    public SeparateEditText(Context context) {
        super(context);
        this.addTextChangedListener(new BankCardNumWatcher());
    }

    public SeparateEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.addTextChangedListener(new BankCardNumWatcher());
    }

    /**
     * 获取真实的text（去掉空格）
     *
     * @return
     */
    public String getTextWithoutSpace() {
        String text = super.getText().toString();
        if (android.text.TextUtils.isEmpty(text)) {
            return "";
        } else {
            return text.replace("-", "");
        }
    }

    /**
     * 银行卡号输入框格式（每4位有个空格）
     *
     */
    class BankCardNumWatcher implements TextWatcher {
        int beforeTextLength = 0;
        int onTextLength = 0;
        boolean isChanged = false;

        int location = 0;
        private char[] tempChar;
        private StringBuffer buffer = new StringBuffer();

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            onTextLength = s.length();
            if (onTextLength > LENGTH_CHAR_MAX) {
                buffer.append(s.toString().substring(0, LENGTH_CHAR_MAX));
                isChanged = true;
                onTextLength = LENGTH_CHAR_MAX;
            } else {
                buffer.append(s.toString());
                boolean flag = (onTextLength == beforeTextLength && beforeTextLength != 16) ||
                        onTextLength <= 3 ||
                        isChanged;
                if (flag) {
                    isChanged = false;
                    return;
                }
                isChanged = true;
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            beforeTextLength = s.length();
            if (buffer.length() > 0) {
                buffer.delete(0, buffer.length());
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (isChanged) {
                location = getSelectionEnd();
                int index = 0;
                while (index < buffer.length()) {
                    if (buffer.charAt(index) == '-') {
                        buffer.deleteCharAt(index);
                    } else {
                        index++;
                    }
                }
                index = 0;
                while (index < buffer.length()) {
                    if ((index == 4 || index == 9 || index == 14)) {
                        buffer.insert(index, '-');
                    }
                    index++;
                }
                tempChar = new char[buffer.length()];
                buffer.getChars(0, buffer.length(), tempChar, 0);
                String str = buffer.toString();
                setText(str);
                Editable etAble = getText();
                Selection.setSelection(etAble, etAble.length());
                isChanged = false;
            }
        }
    }
}