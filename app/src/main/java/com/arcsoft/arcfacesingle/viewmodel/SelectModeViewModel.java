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

package com.arcsoft.arcfacesingle.viewmodel;

import android.view.View;
import android.widget.RadioGroup;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.ObservableField;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.business.common.CommonRepository;
import com.arcsoft.arcfacesingle.business.selectmode.ISelectMode;
import com.arcsoft.arcfacesingle.business.selectmode.SelectModeRepository;
import com.arcsoft.arcfacesingle.navigator.SelectModeNavigator;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.arcfacesingle.view.widgets.CustomTopBar;
import com.arcsoft.asg.libcommon.util.business.DoubleClickUtils;
import com.arcsoft.asg.libcommon.util.common.PermissionUtils;
import com.arcsoft.asg.libcommon.util.common.SPUtils;
import com.arcsoft.asg.libcommon.util.common.ToastUtils;
import com.arcsoft.asg.libcommon.util.common.Utils;

import java.util.List;

public class SelectModeViewModel extends BaseObservable {

    private static final String TAG = SelectModeViewModel.class.getSimpleName();

    private int newAppMode;
    private int oldAppMode;
    private boolean fromSplash;
    private SelectModeRepository repository;
    private SelectModeNavigator navigator;

    @Bindable
    public ObservableField<Integer> appMode = new ObservableField<>();
    public ObservableField<String> strSelectedMode = new ObservableField<>();
    public ObservableField<Boolean> inputServerVisible = new ObservableField<>();

    public SelectModeViewModel() {
        repository = new SelectModeRepository();
    }

    public void setNavigator(SelectModeNavigator navigator) {
        this.navigator = navigator;
    }

    public void onActivityResume(CustomTopBar customTopBar, boolean fromSplash) {
        oldAppMode = SPUtils.getInstance().getInt(Constants.SP_KEY_APP_MODE, Constants.APP_MODE_NONE);
        newAppMode = oldAppMode;
        appMode.set(newAppMode);
        String strMode;
        if (newAppMode == Constants.APP_MODE_OFFLINE_LAN) {
            strMode = Utils.getApp().getResources().getString(R.string.current_app_mode_offline_lan);
            inputServerVisible.set(false);
        } else if (newAppMode == Constants.APP_MODE_CLOUD_AI_OT) {
            strMode = Utils.getApp().getResources().getString(R.string.current_app_mode_ai_cloud);
            inputServerVisible.set(true);
        } else {
            strMode = "";
            inputServerVisible.set(false);
        }
        strSelectedMode.set(strMode);
        if (fromSplash) {
            customTopBar.setVisibleClose(false);
        } else {
            customTopBar.setVisibleClose(true);
        }
        customTopBar.setVisibleTitle(true);
        customTopBar.setStringTitle(strMode);
        initPermissions();
    }

    public void setFromSplash(boolean fromSplash) {
        this.fromSplash = fromSplash;
    }

    private void initPermissions() {
        PermissionUtils.permission(CommonUtils.getNeededPermissions())
                .callback(new PermissionUtils.FullCallback() {
                    @Override
                    public void onGranted(List<String> permissionsGranted) {
                    }

                    @Override
                    public void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied) {
                        CommonRepository.getInstance().exitApp();
                    }
                })
                .request();
    }

    /**
     * 确认切换应用模式
     */
    public void confirmSelectMode() {
        if (oldAppMode == Constants.APP_MODE_NONE && newAppMode != Constants.APP_MODE_NONE) {
            SPUtils.getInstance().put(Constants.SP_KEY_APP_MODE, newAppMode);
            if (navigator != null) {
                if (fromSplash) {
                    if (newAppMode == Constants.APP_MODE_OFFLINE_LAN) {
                        navigator.switch2DeviceInfoPage();
                    }
                    if (newAppMode == Constants.APP_MODE_CLOUD_AI_OT) {
                        navigator.gotoDeviceAccessPage();
                    }
                }
            }
            return;
        }
        if (newAppMode == Constants.APP_MODE_OFFLINE_LAN) {
            if (navigator != null) {
                navigator.setCleanDataDialog();
            }
            boolean accessSuccess = CommonRepository.getInstance().getDeviceAccessStatus();
            if (accessSuccess) {
                CommonRepository.getInstance().unInitArcLinkEngine();
                CommonRepository.getInstance().setDeviceAccessStatus(false);
                SPUtils.getInstance().put(Constants.SP_KEY_DEVICE_ACCESS_ID, "");
                SPUtils.getInstance().put(Constants.SP_KEY_DEVICE_OLD_ACCESS_ID, "");
                SPUtils.getInstance().put(Constants.SP_KEY_DEVICE_TAG, "");
            }
            repository.cleanDataToOfflineLanMode(new ISelectMode.SelectModeCallback() {

                @Override
                public void onCleanDataProgress(int total, int current) {
                    if (navigator != null) {
                        navigator.setCleanDataProgress(total, current);
                    }
                }

                @Override
                public void onCleanDataFail() {
                    if (navigator != null) {
                        navigator.cancelCleanDataDialog();
                    }
                }
            });
        } else if (newAppMode == Constants.APP_MODE_CLOUD_AI_OT) {
            if (navigator != null) {
                navigator.setCleanDataDialog();
            }
            repository.cleanDataToAiCloudMode(new ISelectMode.SelectModeCallback() {

                @Override
                public void onCleanDataProgress(int total, int current) {
                    if (navigator != null) {
                        navigator.setCleanDataProgress(total, current);
                    }
                }

                @Override
                public void onCleanDataFail() {
                    if (navigator != null) {
                        navigator.cancelCleanDataDialog();
                    }
                }
            });
        }
    }

    /**
     * 清除数据完成
     */
    public void cleanDataComplete() {
        SPUtils.getInstance().put(Constants.SP_KEY_APP_MODE, newAppMode);
        if (navigator != null) {
            if (fromSplash) {
                if (newAppMode == Constants.APP_MODE_OFFLINE_LAN) {
                    navigator.switch2DeviceInfoPage();
                } else if (newAppMode == Constants.APP_MODE_CLOUD_AI_OT) {
                    navigator.gotoDeviceAccessPage();
                } else {
                    navigator.gotoRecognitionPage();
                }
            } else {
                if (newAppMode == Constants.APP_MODE_OFFLINE_LAN) {
                    CommonRepository.getInstance().setDeviceAccessStatus(false);
                    SPUtils.getInstance().put(Constants.SP_KEY_DEVICE_ACCESS_ID, "");
                    SPUtils.getInstance().put(Constants.SP_KEY_DEVICE_OLD_ACCESS_ID, "");
                    SPUtils.getInstance().put(Constants.SP_KEY_DEVICE_TAG, "");
                    SPUtils.getInstance().put(Constants.SP_KEY_ARC_LINK_PERSON_SYNC_KEY, "");
                    SPUtils.getInstance().put(Constants.SP_KEY_ARC_LINK_PERSON_SYNC_EXCEPTION, false);
                }
                CommonRepository.getInstance().initService();
                navigator.finishPage();
            }
        }
    }

    public void onClick(View view) {
        int resId = view.getId();
        if (DoubleClickUtils.isFastDoubleClick(resId)) {
            return;
        }
        switch (resId) {
            case R.id.btn_setting_save:
                if (newAppMode == Constants.APP_MODE_NONE) {
                    ToastUtils.showLongToast(CommonUtils.getStrFromRes(R.string.toast_select_app_mode));
                    return;
                }
                if (oldAppMode != Constants.APP_MODE_NONE && newAppMode == oldAppMode) {
                    if (newAppMode == Constants.APP_MODE_CLOUD_AI_OT) {
                        if (fromSplash) {
                            if (navigator != null) {
                                navigator.gotoDeviceAccessPage();
                            }
                        } else {
                            ToastUtils.showShortToast(CommonUtils.getStrFromRes(R.string.current_mode_is_cloud_ai_ot_mode));
                        }
                        return;
                    } else {
                        if (fromSplash) {
                            if (navigator != null) {
                                navigator.switch2DeviceInfoPage();
                            }
                        } else {
                            ToastUtils.showShortToast(CommonUtils.getStrFromRes(R.string.current_mode_is_offline_lan_mode));
                        }
                        return;
                    }
                }
                if (newAppMode == Constants.APP_MODE_CLOUD_AI_OT) {
                    if (navigator != null) {
                        String strWarn = repository.getDialogWarn(newAppMode);
                        String strTitle = repository.getTitle(newAppMode);
                        navigator.showDialog(oldAppMode, newAppMode, strWarn, strTitle);
                    }
                    return;
                }
                if (newAppMode == Constants.APP_MODE_OFFLINE_LAN) {
                    if (navigator != null) {
                        String strWarn = repository.getDialogWarn(newAppMode);
                        String strTitle = repository.getTitle(newAppMode);
                        navigator.showDialog(oldAppMode, newAppMode, strWarn, strTitle);
                    }
                }
                break;
            default:
                break;
        }
    }

    public void onSelectModeCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == R.id.rb_offline_lan_mode) {
            newAppMode = Constants.APP_MODE_OFFLINE_LAN;
            inputServerVisible.set(false);
        } else if (checkedId == R.id.rb_cloud_ai_ot_mode) {
            newAppMode = Constants.APP_MODE_CLOUD_AI_OT;
            inputServerVisible.set(true);
        }
        appMode.set(newAppMode);
    }

    public void release() {
        if (repository != null) {
            repository.release();
        }
    }
}
