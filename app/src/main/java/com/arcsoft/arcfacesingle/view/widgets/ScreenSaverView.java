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

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.asg.libcommon.util.business.rx.RxUtils;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

public class ScreenSaverView extends FrameLayout {

    private static final int FRAME_ONE = 1;
    private static final int FRAME_TWO = 2;
    private static final int FRAME_THREE = 3;
    private static final int FRAME_FOUR = 4;
    private static final int FRAME_FIVE = 5;

    private ObjectAnimator outerAnimator;
    private ObjectAnimator innerAnimator;
    private Disposable faceDisposable;

    private ImageView ivFace;

    public ScreenSaverView(Context context) {
        super(context);
        init(context);
    }

    @Override
    public void setVisibility(int visibility) {
        if (visibility == View.VISIBLE) {
            outerAnimator.start();
            innerAnimator.start();
            stopAnimation();
            startAnimation();
        } else {
            stopAllAnimation();
        }
        super.setVisibility(visibility);
    }

    @Override
    public void onDetachedFromWindow() {
        stopAllAnimation();
        super.onDetachedFromWindow();
    }

    public ScreenSaverView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    private void init(Context context) {
        View.inflate(context, R.layout.layout_screen_saver, this);

        ivFace = findViewById(R.id.iv_face);

        ImageView outImageView = findViewById(R.id.iv_outer_circle);
        outerAnimator = ObjectAnimator.ofFloat(outImageView, "rotation", 0, 360);
        outerAnimator.setDuration(4000);
        outerAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        outerAnimator.setInterpolator(new LinearInterpolator());

        ImageView innerImageView = findViewById(R.id.iv_inner_circle);
        innerAnimator = ObjectAnimator.ofFloat(innerImageView, "rotation", 0, -360);
        innerAnimator.setDuration(4000);
        innerAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        innerAnimator.setInterpolator(new LinearInterpolator());
    }

    public void startAnimation() {
        faceDisposable = Observable.interval(0,2000, TimeUnit.MILLISECONDS).compose(RxUtils.computingToMain()).subscribe(aLong -> {
            TransitionDrawable transitionDrawable = null;
            long times = aLong % FRAME_FIVE;
            if (times == 0) {
                transitionDrawable = (TransitionDrawable) getResources().getDrawable(R.drawable.transition_screen_saver_1);
            } else if (times == FRAME_ONE) {
                transitionDrawable = (TransitionDrawable) getResources().getDrawable(R.drawable.transition_screen_saver_2);
            } else if (times == FRAME_TWO) {
                transitionDrawable = (TransitionDrawable) getResources().getDrawable(R.drawable.transition_screen_saver_3);
            } else if (times == FRAME_THREE) {
                transitionDrawable = (TransitionDrawable) getResources().getDrawable(R.drawable.transition_screen_saver_4);
            } else if (times == FRAME_FOUR) {
                transitionDrawable = (TransitionDrawable) getResources().getDrawable(R.drawable.transition_screen_saver_5);
            }
            if (transitionDrawable != null) {
                ivFace.setImageDrawable(transitionDrawable);
                transitionDrawable.startTransition(1990);
            }
        });
    }

    private void stopAnimation() {
        if (faceDisposable != null && !faceDisposable.isDisposed()) {
            faceDisposable.dispose();
        }
        faceDisposable = null;
    }

    public void stopAllAnimation() {
        outerAnimator.end();
        innerAnimator.end();
        stopAnimation();
    }

}
