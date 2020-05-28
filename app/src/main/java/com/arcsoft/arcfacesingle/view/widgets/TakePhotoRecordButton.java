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
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.asg.libcommon.util.common.SizeUtils;

public class TakePhotoRecordButton extends AppCompatImageButton {

    private static final int PADDING = 8;
    private final static int DELAY = 1000;
    private long lastTime = 0;

    public TakePhotoRecordButton(Context context) {
        super(context, null, 0);
        init(context);
    }

    public TakePhotoRecordButton(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        init(context);
    }

    private void init(Context context) {
        Drawable captureDrawable = ContextCompat.getDrawable(context, R.drawable.take_photo_button);
        setBackground(ContextCompat.getDrawable(context, R.drawable.circle_frame_background));

        setOnClickListener(view -> {
            if (System.currentTimeMillis() - lastTime >= DELAY) {
                lastTime = System.currentTimeMillis();
            }
        });
        setSoundEffectsEnabled(false);

        setImageDrawable(captureDrawable);
        int buttonPadding = SizeUtils.dp2px(PADDING);
        setPadding(buttonPadding, buttonPadding, buttonPadding, buttonPadding);
    }

}
