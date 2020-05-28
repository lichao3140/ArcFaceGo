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
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.asg.libcommon.util.common.SizeUtils;

public class HorizontalProgressBar extends ProgressBar {

    private static final int TEXT_SIZE = 10;
    private static final int TEXT_OFFSET = 5;
    private static final int REACHED_PROGRESS_BAR = 2;
    private static final int UNREACHED_PROGRESS_BAR = 2;
    private static final int TEXT_COLOR = 0XFFFC00D1;
    private static final int COLOR_UNREACHED_COLOR = 0xFFd3d6da;

    protected Paint paint = new Paint();
    protected int textColor = TEXT_COLOR;
    protected int textSize = SizeUtils.sp2px(TEXT_SIZE);
    protected int textOffset = SizeUtils.dp2px(TEXT_OFFSET);
    protected int reachedBarHeight = SizeUtils.dp2px(REACHED_PROGRESS_BAR);
    protected int reachedBarColor = TEXT_COLOR;
    protected int unReachedBarColor = COLOR_UNREACHED_COLOR;
    protected int unReachedBarHeight = SizeUtils.dp2px(UNREACHED_PROGRESS_BAR);
    protected int realWidth;

    protected boolean bDrawText = true;

    public HorizontalProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        initView(context, attrs);
    }

    public HorizontalProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        obtainAttributes(attrs);
        paint.setTextSize(textSize);
        paint.setColor(textColor);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), measureHeight(heightMeasureSpec));
        realWidth = getMeasuredWidth() - getPaddingRight() - getPaddingLeft();
    }

    private int measureHeight(int measure) {
        int height;
        int size = MeasureSpec.getSize(measure);
        int mode = MeasureSpec.getMode(measure);
        if (mode == MeasureSpec.EXACTLY) {
            height = size;
        } else {
            float textHeight = (paint.descent() - paint.ascent());
            height = (int) (getPaddingTop() + getPaddingBottom() + Math.max(Math.max(reachedBarHeight,
                    unReachedBarHeight), Math.abs(textHeight)));
            if (mode == MeasureSpec.AT_MOST) {
                height = Math.min(height, size);
            }
        }
        return height;
    }

    private void obtainAttributes(AttributeSet attrs) {
        final TypedArray styleAttributes = getContext().obtainStyledAttributes(attrs, R.styleable.HorizontalProgressBar);
        textColor = styleAttributes.getColor(R.styleable.HorizontalProgressBar_progress_text_color, TEXT_COLOR);
        textSize = (int) styleAttributes.getDimension(R.styleable.HorizontalProgressBar_progress_text_size, textSize);
        reachedBarColor = styleAttributes.getColor(R.styleable.HorizontalProgressBar_progress_reached_color, textColor);
        unReachedBarColor = styleAttributes.getColor(R.styleable.HorizontalProgressBar_progress_unreached_color,
                COLOR_UNREACHED_COLOR);
        reachedBarHeight = (int) styleAttributes.getDimension(R.styleable.HorizontalProgressBar_progress_reached_bar_height,
                reachedBarHeight);
        unReachedBarHeight = (int) styleAttributes.getDimension(R.styleable.HorizontalProgressBar_progress_unreached_bar_height,
                unReachedBarHeight);
        textOffset = (int) styleAttributes.getDimension(R.styleable.HorizontalProgressBar_progress_text_offset, textOffset);

        int textVisible = styleAttributes.getInt(R.styleable.HorizontalProgressBar_progress_text_visibility, View.VISIBLE);
        if (textVisible != View.VISIBLE) {
            bDrawText = false;
        }
        styleAttributes.recycle();
    }

    public void setProgressBar(int progress) {
        setProgress(progress);
        invalidate();
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(getPaddingLeft(), getHeight() >> 1);
        float rate = (getProgress() * 1.0f) / getMax();
        float progressX = (int) (realWidth * rate);
        progressX = Math.min(progressX, realWidth);
        if (progressX > 0) {
            paint.setStrokeWidth(reachedBarHeight);
            paint.setColor(reachedBarColor);
            canvas.drawLine(1, 0, progressX - 1, 0, paint);
        }
        canvas.restore();
    }
}
