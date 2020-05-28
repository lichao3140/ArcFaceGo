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

import android.Manifest;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.ObservableField;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.business.active.DeviceActiveRepository;
import com.arcsoft.arcfacesingle.business.active.IActive;
import com.arcsoft.arcfacesingle.data.model.ParamCheckActivation;
import com.arcsoft.arcfacesingle.navigator.DeviceActiveNavigator;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.asg.libcommon.util.business.DoubleClickUtils;
import com.arcsoft.asg.libcommon.util.common.ActivityUtils;
import com.arcsoft.asg.libcommon.util.common.KeyboardUtils;
import com.arcsoft.asg.libcommon.util.common.PermissionUtils;
import com.arcsoft.asg.libcommon.util.common.SPUtils;
import com.arcsoft.asg.libcommon.util.common.StringUtils;
import com.arcsoft.asg.libcommon.util.common.ToastUtils;
import com.arcsoft.asg.libcommon.util.common.Utils;
import com.arcsoft.faceengine.ErrorInfo;

import java.io.File;
import java.util.List;

public class DeviceActiveViewModel extends BaseObservable {

    private static final String TAG = DeviceActiveViewModel.class.getSimpleName();
    private static final String[] STORAGE_PERMISSION = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
    };

    private DeviceActiveNavigator navigator;
    private DeviceActiveRepository repository;
    private boolean isReadLocalActiveInfoEnable;
    private boolean fromSplash;

    @Bindable
    public final ObservableField<String> fieldAppId = new ObservableField<>();
    public final ObservableField<String> fieldSdkKey = new ObservableField<>();
    public final ObservableField<String> fieldActiveKey = new ObservableField<>();
    public final ObservableField<Boolean> appIdVisible = new ObservableField<>();
    public final ObservableField<Boolean> sdkKeyVisible = new ObservableField<>();
    public final ObservableField<Boolean> activeKeyVisible = new ObservableField<>();
    public final ObservableField<Boolean> chargeVisible = new ObservableField<>();
    public final ObservableField<Boolean> inputInfoVisible = new ObservableField<>();
    public final ObservableField<Boolean> questionVisible = new ObservableField<>();
    public final ObservableField<Boolean> activeInfo1Visible = new ObservableField<>();
    public final ObservableField<Boolean> activeInfo2Visible = new ObservableField<>();

    public DeviceActiveViewModel() {
    }

    public void init(boolean fromSplash) {
        this.fromSplash = fromSplash;
        repository = new DeviceActiveRepository();
        repository.setFromSplash(fromSplash);
        isReadLocalActiveInfoEnable = true;
        fieldAppId.set(SPUtils.getInstance().getString(Constants.SP_KEY_APP_ID));
        fieldSdkKey.set(SPUtils.getInstance().getString(Constants.SP_KEY_SDK_KEY));
        fieldActiveKey.set(SPUtils.getInstance().getString(Constants.SP_KEY_ACTIVE_KEY));
        chargeVisible.set(true);

        inputInfoVisible.set(true);
        questionVisible.set(true);
        activeInfo1Visible.set(true);
        activeInfo2Visible.set(true);
        if (!TextUtils.isEmpty(fieldAppId.get())) {
            appIdVisible.set(false);
        } else {
            appIdVisible.set(true);
        }
        if (!TextUtils.isEmpty(fieldSdkKey.get())) {
            sdkKeyVisible.set(false);
        } else {
            sdkKeyVisible.set(true);
        }
        activeKeyVisible.set(true);
    }

    public void setNavigator(DeviceActiveNavigator deviceActiveNavigator) {
        this.navigator = deviceActiveNavigator;
    }

    public void onActivityResume() {
        if (repository != null) {
            if (fromSplash) {
                repository.initPermission();
            }
        }
    }

    public void onActivityDestroy() {
        if (repository != null) {
            repository.unInit();
            repository = null;
        }
        navigator = null;
    }

    public void onAppIdEditTextChanged(Editable editable) {
        String appId = editable.toString().trim();
        if (!TextUtils.isEmpty(appId)) {
            appIdVisible.set(false);
        } else {
            appIdVisible.set(true);
        }
    }

    public void onSdkKeyEditTextChanged(Editable editable) {
        String sdkKey = editable.toString().trim();
        if (!TextUtils.isEmpty(sdkKey)) {
            sdkKeyVisible.set(false);
        } else {
            sdkKeyVisible.set(true);
        }
    }

    public void onActiveKeyEditTextChanged(Editable editable) {
        String activeKey = editable.toString().trim();
        if (!TextUtils.isEmpty(activeKey)) {
            activeKeyVisible.set(false);
        } else {
            activeKeyVisible.set(true);
        }
    }

    public void onClick(View v) {
        int resId = v.getId();
        if (DoubleClickUtils.isFastDoubleClick(resId)) {
            return;
        }
        switch (resId) {
            case R.id.btn_read_local_active_info:
                PermissionUtils.permission(STORAGE_PERMISSION)
                        .callback(new PermissionUtils.FullCallback() {
                            @Override
                            public void onGranted(List<String> permissionsGrantedList) {
                                readLocalActiveInfo();
                            }

                            @Override
                            public void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied) {
                                ToastUtils.showShortToast(CommonUtils.getStrFromRes(R.string.you_have_no_access_right));
                            }
                        })
                        .request();
                break;
            case R.id.btn_device_active:
                activeDevice();
                break;
            case R.id.ll_offline_active:
                navigator.openOfflineFile();
                break;
            default:
                break;
        }
    }

    /**
     * 读取本地配置文件
     */
    private void readLocalActiveInfo() {
        try {
            if (repository != null) {
                List<String> activeConfigs = repository.readActivationFileFromSdCard();
                if (activeConfigs == null || activeConfigs.isEmpty()) {
                    if (repository.usbDeviceAvailable()) {
                        repository.readActivationFileFromUsb(new DeviceActiveRepository.ReadUsbFileCallback() {
                            @Override
                            public void getFileSuccess(List<String> fileList) {
                                setActivationInfo(fileList, true);
                            }

                            @Override
                            public void getFileFailed(String msg) {
                                setReadLocalActiveInfoFail(msg);
                            }
                        });
                    } else {
                        setReadLocalActiveInfoFail(CommonUtils.getStrFromRes(R.string.no_active_file_please_check));
                    }
                } else {
                    setActivationInfo(activeConfigs, false);
                }
            }
        } catch (Exception e) {
            setReadLocalActiveInfoFail(Utils.getApp().getString(R.string.config_file_invalid));
        }
    }

    private void setActivationInfo(List<String> activeConfigs, boolean selectUsbFile) {
        if (repository != null) {
            ParamCheckActivation appIdParam = repository.checkActivationAppId(activeConfigs, selectUsbFile);
            if (appIdParam.isSuccess()) {
                ParamCheckActivation sdkKeyParam = repository.checkActivationSdkKey(activeConfigs, selectUsbFile);
                if (sdkKeyParam.isSuccess()) {
                    ParamCheckActivation activeKeyParam = repository.checkActivationActiveKey(activeConfigs, selectUsbFile);
                    if (activeKeyParam.isSuccess()) {
                        fieldAppId.set(appIdParam.getAppId());
                        fieldSdkKey.set(sdkKeyParam.getSdkKey());
                        String activeKeyString = activeKeyParam.getActiveKey().replaceAll("-", "");
                        if (activeKeyString.length() > DeviceActiveRepository.LENGTH_CHAR_MAX) {
                            fieldActiveKey.set(activeKeyString.substring(0, DeviceActiveRepository.LENGTH_CHAR_MAX));
                        } else {
                            fieldActiveKey.set(activeKeyString);
                        }
                        navigator.setEditTextClearFocus();
                        isReadLocalActiveInfoEnable = true;
                        repository.setSelectUsbActiveFileFlag(selectUsbFile);
                        KeyboardUtils.hideSoftInput(ActivityUtils.getTopActivity());
                    } else {
                        setReadLocalActiveInfoFail(activeKeyParam.getMessage());
                    }
                } else {
                    setReadLocalActiveInfoFail(sdkKeyParam.getMessage());
                }
            } else {
                setReadLocalActiveInfoFail(appIdParam.getMessage());
            }
        }
    }

    /**
     * 设置读取配置失败信息
     *
     * @param msg
     */
    private void setReadLocalActiveInfoFail(String msg) {
        if (navigator != null) {
            navigator.showReadActivationFileDialog(msg);
        }
        isReadLocalActiveInfoEnable = false;
    }

    /**
     * 读取配置按钮是否可点击
     *
     * @return
     */
    @Bindable
    public boolean getReadLocalActiveInfoEnable() {
        return isReadLocalActiveInfoEnable;
    }

    /**
     * 设备激活
     */
    private void activeDevice() {
        if (TextUtils.isEmpty(fieldAppId.get())) {
            return;
        }
        if (StringUtils.containSpecialChar(fieldAppId.get(), Constants.STRING_ACTIVE_FILE_COMPILE)) {
            return;
        }
        if (TextUtils.isEmpty(fieldSdkKey.get())) {
            return;
        }
        if (StringUtils.containSpecialChar(fieldSdkKey.get(), Constants.STRING_ACTIVE_FILE_COMPILE)) {
            return;
        }
        if (TextUtils.isEmpty(fieldActiveKey.get())) {
            return;
        }
        if (StringUtils.containSpecialChar(fieldActiveKey.get(), Constants.STRING_ACTIVE_FILE_COMPILE)) {
            return;
        }
        if (repository != null) {
            String stringAppId = "";
            String appIdField = fieldAppId.get();
            if (appIdField != null) {
                stringAppId = appIdField.trim().replaceAll(" ", "");
            }
            String stringSdkKey = "";
            String sdkKeyField = fieldSdkKey.get();
            if (sdkKeyField != null) {
                stringSdkKey = sdkKeyField.trim().replaceAll(" ", "");
            }
            String stringActiveKey = "";
            String stringActiveKeyField = fieldActiveKey.get();
            if (stringActiveKeyField != null) {
                stringActiveKey = stringActiveKeyField.trim().replaceAll(" ", "");
            }
            if (navigator != null) {
                navigator.setBtnActiveEnable(false);
            }
            repository.onlineActive(new IActive.ActiveCallback() {
                @Override
                public void onSuccess() {
                    if (navigator != null) {
                        String msg = Utils.getApp().getResources().getString(R.string.device_active_successful);
                        navigator.showActiveDialog(ErrorInfo.MOK, msg, repository.isSelectUsbActiveFile(), false);
                        navigator.setBtnActiveEnable(true);
                    }
                }

                @Override
                public void onFail(int result, String errorMsg, boolean needSaveActiveFileToUsb) {
                    if (navigator != null) {
                        navigator.showActiveDialog(result, errorMsg, needSaveActiveFileToUsb, false);
                        navigator.setBtnActiveEnable(true);
                    }
                }

                @Override
                public void onSaveActiveFile(boolean success) {
                    if (navigator != null) {
                        navigator.setActiveResultBtnEnable(success);
                    }
                }
            }, stringAppId, stringSdkKey, stringActiveKey);
        }
    }

    public void offlineActive(File file) {
        String path = file.getAbsolutePath();
        repository.offlineActive(new IActive.ActiveCallback() {
            @Override
            public void onSuccess() {
                if (navigator != null) {
                    String msg = Utils.getApp().getResources().getString(R.string.device_active_successful);
                    navigator.showActiveDialog(ErrorInfo.MOK, msg, false, true);
                }
            }

            @Override
            public void onFail(int result, String errorMsg, boolean needSaveActiveFileToUsb) {
                if (navigator != null) {
                    navigator.showActiveDialog(result, errorMsg, false, true);
                }
            }

            @Override
            public void onSaveActiveFile(boolean success) {
            }
        }, path);
    }

    /**
     * 保存激活文件至SDCard
     */
    public void saveOfflineActiveFile() {
        repository.saveOfflineActiveFile(new IActive.ReadActiveFileCallback() {
            @Override
            public void onSuccess(String appId, String sdkKey, String activeKey) {
                fieldAppId.set(appId);
                fieldSdkKey.set(sdkKey);
                fieldActiveKey.set(activeKey);
            }

            @Override
            public void onFail() {
            }
        });
    }
}
