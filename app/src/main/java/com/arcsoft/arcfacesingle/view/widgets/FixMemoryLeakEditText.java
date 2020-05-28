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

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

@SuppressLint("AppCompatCustomView")
public class FixMemoryLeakEditText extends EditText {

    private static Field mParent;
    private WeakReference<TextWatcher> textWatcher;

    static {
        try {
            mParent = View.class.getDeclaredField("mParent");
            mParent.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public FixMemoryLeakEditText(Context context) {
        super(context.getApplicationContext());
    }

    public FixMemoryLeakEditText(Context context, AttributeSet attrs) {
        super(context.getApplicationContext(), attrs);
    }

    public FixMemoryLeakEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context.getApplicationContext(), attrs, defStyleAttr);
    }

    @Override
    protected void onDetachedFromWindow() {
        try {
            if (mParent != null) {
                mParent.set(this, null);
            }
            TextWatcher tw = textWatcher.get();
            if (null != tw) {
                removeTextChangedListener(tw);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDetachedFromWindow();
    }

    @Override
    public void addTextChangedListener(TextWatcher textWatcher) {
        this.textWatcher = new WeakReference<>(textWatcher);
        super.addTextChangedListener(textWatcher);
    }

}
