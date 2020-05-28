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

package com.arcsoft.arcfacesingle.util.business;

import android.app.Activity;
import android.app.Application;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;

import com.arcsoft.arcfacesingle.data.event.KeyboardVisibleEvent;
import com.arcsoft.asg.libcommon.util.common.SizeUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

public class KeyboardObserverHelper {

    private final static float KEYBOARD_VISIBLE_THRESHOLD_DP = 100F;
    private static volatile KeyboardObserverHelper mInstance;

    public static KeyboardObserverHelper getInstance() {
        if (mInstance == null) {
            synchronized (KeyboardObserverHelper.class) {
                if (mInstance == null) {
                    mInstance = new KeyboardObserverHelper();
                }
            }
        }
        return mInstance;
    }

    private KeyboardObserverHelper() {
    }

    public void init(final Application application) {
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {

            private boolean wasOpened = false;
            private Map<String, ViewTreeObserver.OnGlobalLayoutListener> layoutListenerMap = new HashMap<>();

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                try {
                    final View activityRoot = getActivityRoot(activity);
                    if (activityRoot == null) {
                        return;
                    }
                    ViewTreeObserver.OnGlobalLayoutListener layoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {

                        private final Rect r = new Rect();
                        private final int visibleThreshold = Math.round(SizeUtils.dp2px(activity, KEYBOARD_VISIBLE_THRESHOLD_DP));

                        @Override
                        public void onGlobalLayout() {
                            activityRoot.getWindowVisibleDisplayFrame(r);
                            int heightDiff = activityRoot.getRootView().getHeight() - r.height();
                            boolean isOpen = heightDiff > visibleThreshold;

                            if (isOpen == wasOpened) {
                                // keyboard state has not changed
                                return;
                            }
                            wasOpened = isOpen;
                            EventBus.getDefault().post(new KeyboardVisibleEvent(isOpen));
                        }
                    };

                    activityRoot.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
                    layoutListenerMap.put(activity.getClass().getName(), layoutListener);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                try {
                    ViewTreeObserver.OnGlobalLayoutListener layoutListener = layoutListenerMap.get(activity.getClass().getName());
                    if (layoutListener != null){
                        if (wasOpened){
                            EventBus.getDefault().post(new KeyboardVisibleEvent(!wasOpened));
                        }
                        View activityRoot = getActivityRoot(activity);
                        activityRoot.getViewTreeObserver().removeGlobalOnLayoutListener(layoutListener);
                        layoutListenerMap.remove(activity.getClass().getName());
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Determine if keyboard is visible
     *
     * @param activity Activity
     * @return Whether keyboard is visible or not
     */
    public boolean isKeyboardVisible(Activity activity) {
        Rect r = new Rect();

        View activityRoot = getActivityRoot(activity);
        int visibleThreshold =
                Math.round(SizeUtils.dp2px(activity, KEYBOARD_VISIBLE_THRESHOLD_DP));

        activityRoot.getWindowVisibleDisplayFrame(r);

        int heightDiff = activityRoot.getRootView().getHeight() - r.height();

        return heightDiff > visibleThreshold;
    }

    private View getActivityRoot(Activity activity) {
        return activity.getWindow().getDecorView();
    }
}
