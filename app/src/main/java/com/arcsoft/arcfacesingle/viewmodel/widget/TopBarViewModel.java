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

package com.arcsoft.arcfacesingle.viewmodel.widget;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.view.View;

import androidx.databinding.BaseObservable;
import androidx.databinding.ObservableField;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.business.common.CommonRepository;
import com.arcsoft.arcfacesingle.data.event.ClosePageEvent;
import com.arcsoft.arcfacesingle.view.activity.DeviceAccessActivity;
import com.arcsoft.arcfacesingle.view.activity.DeviceActiveActivity;
import com.arcsoft.arcfacesingle.view.activity.PersonListActivity;
import com.arcsoft.arcfacesingle.view.activity.RecognizeActivity;
import com.arcsoft.arcfacesingle.view.activity.SelectModeActivity;
import com.arcsoft.arcfacesingle.view.activity.SettingActivity;
import com.arcsoft.arcfacesingle.view.activity.SettingSelectActivity;
import com.arcsoft.asg.libcommon.util.business.DoubleClickUtils;
import com.arcsoft.asg.libcommon.util.common.ActivityUtils;
import com.arcsoft.asg.libcommon.util.common.SPUtils;

import org.greenrobot.eventbus.EventBus;

public class TopBarViewModel extends BaseObservable {

    public ObservableField<String> fieldStringTitle = new ObservableField<>();
    public ObservableField<Boolean> fieldVisibleClose = new ObservableField<>();
    public ObservableField<Boolean> fieldVisibleTitle = new ObservableField<>();
    public ObservableField<Boolean> fieldVisibleSkip = new ObservableField<>();
    public ObservableField<Boolean> fieldVisibleSetting = new ObservableField<>();
    public OnBackClickListener backClickListener;

    public TopBarViewModel() {
    }

    public void setFieldStringTitle(String title) {
        fieldStringTitle.set(title);
    }

    public void setFieldVisibleTitle(boolean visible) {
        fieldVisibleTitle.set(visible);
    }

    public void setFieldVisibleSkip(boolean visible) {
        fieldVisibleSkip.set(visible);
    }

    public void setFieldVisibleSetting(boolean visible) {
        fieldVisibleSetting.set(visible);
    }

    public void setFieldVisibleClose(boolean visible) {
        fieldVisibleClose.set(visible);
    }

    public void setBackClickListener(OnBackClickListener backClickListener) {
        this.backClickListener = backClickListener;
    }

    public void onClick(View v) {
        int resId = v.getId();
        if (DoubleClickUtils.isFastDoubleClick(resId)) {
            return;
        }
        Activity activity = getActivityByContext(v.getContext());
        if (activity == null) {
            return;
        }
        String strActivityName = activity.getClass().getSimpleName();
        switch (v.getId()) {
            case R.id.rl_top_bar_close:
                if (backClickListener != null) {
                    backClickListener.onClick();
                }

                if (strActivityName.equals(SettingActivity.class.getSimpleName())) {
                    if (!activity.isFinishing()) {
                        EventBus.getDefault().post(new ClosePageEvent());
                        Intent intent = new Intent(activity, RecognizeActivity.class);
                        activity.startActivity(intent);
                    }
                }

                if (ActivityUtils.activityCount() == 1 && strActivityName.equals(DeviceActiveActivity.class.getSimpleName())) {
                    CommonRepository.getInstance().sendExitAppBroadcast();
                    CommonRepository.getInstance().exitApp();
                    return;
                }

                if (strActivityName.equals(PersonListActivity.class.getSimpleName())) {
                    return;
                }

                activity.finish();

                break;
            case R.id.btn_top_bar_skip:
                if (strActivityName.equals(DeviceAccessActivity.class.getSimpleName())) {
                    if (!activity.isFinishing()) {
                        Intent intent = new Intent(activity, RecognizeActivity.class);
                        activity.startActivity(intent);
                    }
                } else {
                    int appMode = SPUtils.getInstance().getInt(Constants.SP_KEY_APP_MODE, Constants.APP_MODE_NONE);
                    if (appMode == Constants.APP_MODE_NONE) {
                        if (!activity.isFinishing()) {
                            Intent intent = new Intent(activity, SelectModeActivity.class);
                            intent.putExtra(Constants.SP_KEY_FROM_SPLASH, true);
                            activity.startActivity(intent);
                        }
                    } else {
                        if (!activity.isFinishing()) {
                            Intent intent = new Intent(activity, RecognizeActivity.class);
                            intent.putExtra(Constants.SP_KEY_APP_MODE, appMode);
                            activity.startActivity(intent);
                        }
                    }
                }
                activity.finish();
                break;
            case R.id.btn_top_bar_setting:
                if (!activity.isFinishing()) {
                    Intent intent = new Intent(activity, SettingSelectActivity.class);
                    activity.startActivity(intent);
                }
                break;
            default:
                break;
        }
    }

    public interface OnBackClickListener {

        /**
         * 点击事件
         */
        void onClick();
    }

    private Activity getActivityByContext(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }
}
