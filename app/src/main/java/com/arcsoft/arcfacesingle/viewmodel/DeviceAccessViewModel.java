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

import android.text.Editable;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.ObservableField;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.business.common.CommonRepository;
import com.arcsoft.arcfacesingle.business.deviceaccess.DeviceAccessListener;
import com.arcsoft.arcfacesingle.business.deviceaccess.DeviceAccessRepository;
import com.arcsoft.arcfacesingle.business.deviceaccess.IDeviceAccess;
import com.arcsoft.arcfacesingle.data.db.table.TableSettingConfigInfo;
import com.arcsoft.arcfacesingle.data.model.DeviceAccessInfo;
import com.arcsoft.arcfacesingle.navigator.DeviceAccessNavigator;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.arcfacesingle.view.widgets.CustomTopBar;
import com.arcsoft.arcsoftlink.http.bean.res.DeviceInfo;
import com.arcsoft.asg.libcommon.util.business.DoubleClickUtils;
import com.arcsoft.asg.libcommon.util.common.ActivityUtils;
import com.arcsoft.asg.libcommon.util.common.KeyboardUtils;
import com.arcsoft.asg.libcommon.util.common.ToastUtils;

public class DeviceAccessViewModel extends BaseObservable {

    private DeviceAccessNavigator navigator;
    private IDeviceAccess deviceAccessRep;
    private DeviceAccessInfo accessInfo;
    private boolean accessSuccess;
    private boolean editDeviceTagFlag;

    @Bindable
    public final ObservableField<Boolean> fieldBindSuccess = new ObservableField<>();
    public final ObservableField<String> fieldAccessId = new ObservableField<>();
    public final ObservableField<String> fieldDeviceTag = new ObservableField<>();
    public final ObservableField<String> fieldServerIp = new ObservableField<>();
    public final ObservableField<Boolean> fieldLlDeviceTagInputVisible = new ObservableField<>();
    public final ObservableField<Boolean> fieldDeviceTagRlVisible = new ObservableField<>();
    public final ObservableField<Boolean> fieldDeviceTagModifyIvVisible = new ObservableField<>();
    public final ObservableField<Boolean> fieldServerIpEditLlVisible = new ObservableField<>();
    public final ObservableField<Boolean> fieldServerIpModifyIvVisible = new ObservableField<>();
    public final ObservableField<Boolean> fieldDeviceTagConfirmIvVisible = new ObservableField<>();
    public final ObservableField<Boolean> fieldAccessIdTvVisible = new ObservableField<>();
    public final ObservableField<Boolean> fieldAccessIdWarnVisible = new ObservableField<>();
    public final ObservableField<Boolean> fieldDeviceTagWarnVisible = new ObservableField<>();
    public final ObservableField<Boolean> fieldServerURLWarnVisible = new ObservableField<>();
    public final ObservableField<String> fieldStringWarn1 = new ObservableField<>();
    public final ObservableField<String> fieldStringWarn2 = new ObservableField<>();
    public final ObservableField<String> fieldStringWarn3 = new ObservableField<>();

    public DeviceAccessViewModel() {
    }

    public void setNavigator(DeviceAccessNavigator navigator) {
        this.navigator = navigator;
    }

    public void init(CustomTopBar customTopBar, boolean fromSplash) {
        deviceAccessRep = new DeviceAccessRepository(listener);
        deviceAccessRep.init(fromSplash);
        accessInfo = CommonRepository.getInstance().getDeviceAccessInfo();
        String serverUrl = accessInfo.getServerIp();
        deviceAccessRep.initUrl(serverUrl);
        configurationBindStatusParam();
        if (fromSplash) {
            customTopBar.setVisibleSkip(true);
        }
        customTopBar.setVisibleClose(true);
        customTopBar.setVisibleTitle(true);
        customTopBar.setStringTitle(CommonUtils.getStrFromRes(R.string.device_access));
        if (navigator != null) {
            navigator.setUiEnable(true, accessSuccess ? CommonUtils.getStrFromRes(R.string.device_unbinding) :
                    CommonUtils.getStrFromRes(R.string.device_access));
        }
    }

    public void unInit() {
        if (deviceAccessRep != null) {
            deviceAccessRep.unInit();
            deviceAccessRep = null;
        }
        navigator = null;
    }

    private void configurationBindStatusParam() {
        accessSuccess = deviceAccessRep.getDeviceAccessStatus();
        setFieldParamFromStatus(accessSuccess);

        if (accessSuccess) {
            fieldServerIpEditLlVisible.set(false);
            fieldAccessId.set(accessInfo.getAccessId());
        } else {
            fieldStringWarn1.set(CommonUtils.getStrFromRes(R.string.not_empty));
            fieldStringWarn2.set(CommonUtils.getStrFromRes(R.string.not_empty));
            fieldServerIpEditLlVisible.set(false);
            fieldServerIpModifyIvVisible.set(true);
            fieldAccessId.set("");
        }
        fieldServerIp.set(accessInfo.getServerIp());
        fieldDeviceTagRlVisible.set(true);
        fieldDeviceTagConfirmIvVisible.set(false);

        TableSettingConfigInfo configInfo = CommonRepository.getInstance().getSettingConfigInfo();
        if (!TextUtils.isEmpty(configInfo.getDeviceName())) {
            fieldDeviceTag.set(configInfo.getDeviceName());
        } else if (!TextUtils.isEmpty(accessInfo.getDeviceTag())) {
            fieldDeviceTag.set(configInfo.getDeviceName());
        } else {
            fieldDeviceTag.set("");
        }
    }

    private void setFieldParamFromStatus(boolean accessSuccess) {
        fieldBindSuccess.set(accessSuccess);
        fieldAccessIdTvVisible.set(accessSuccess);
        fieldAccessIdWarnVisible.set(!accessSuccess);
        fieldLlDeviceTagInputVisible.set(!accessSuccess);
        fieldDeviceTagModifyIvVisible.set(accessSuccess);
        fieldDeviceTagWarnVisible.set(!accessSuccess);
    }

    public void onAccessIdEditTextChanged(Editable editable) {
        String content = editable.toString().trim();
        if (TextUtils.isEmpty(content)) {
            fieldAccessIdWarnVisible.set(true);
            fieldStringWarn1.set(CommonUtils.getStrFromRes(R.string.not_empty));
        } else {
            fieldAccessIdWarnVisible.set(false);
        }
    }

    public void onDeviceTagEditTextChanged(Editable editable) {
        String content = editable.toString().trim();
        if (TextUtils.isEmpty(content)) {
            fieldDeviceTagWarnVisible.set(true);
            fieldStringWarn2.set(CommonUtils.getStrFromRes(R.string.not_empty));
            fieldDeviceTagRlVisible.set(true);
        } else {
            fieldDeviceTagWarnVisible.set(false);
            fieldDeviceTagRlVisible.set(editDeviceTagFlag);
        }
    }

    public void onServerIpEditTextChanged(Editable editable) {
        String url = editable.toString().trim();
        if (TextUtils.isEmpty(url)) {
            fieldServerURLWarnVisible.set(true);
            fieldStringWarn3.set(CommonUtils.getStrFromRes(R.string.not_empty));
        } else {
            fieldServerURLWarnVisible.set(false);
        }
    }

    /**
     * 确定接入设备
     */
    public void confirmAccess(DeviceInfo deviceInfo, String accessId, String deviceTag, String url) {
        if (deviceAccessRep != null) {
            if (navigator != null) {
                navigator.setTopBarEnable(false);
            }
            deviceAccessRep.accessDeviceSecond(deviceInfo, accessId, deviceTag, url);
        }
    }

    /**
     * 设备解绑
     */
    public void unBindDevice() {
        if (deviceAccessRep != null) {
            deviceAccessRep.unBindDevice();
        }
        accessSuccess = false;
        editDeviceTagFlag = false;

        fieldBindSuccess.set(false);
        fieldAccessIdTvVisible.set(false);
        fieldAccessIdWarnVisible.set(true);
        fieldLlDeviceTagInputVisible.set(true);
        fieldDeviceTagModifyIvVisible.set(false);
        fieldDeviceTagWarnVisible.set(false);
        fieldDeviceTagRlVisible.set(true);
        fieldDeviceTagConfirmIvVisible.set(false);
        fieldServerIpEditLlVisible.set(false);
        fieldServerIpModifyIvVisible.set(true);

        fieldAccessId.set("");
        if (navigator != null) {
            navigator.setUiEnable(true, CommonUtils.getStrFromRes(R.string.device_access));
        }
    }

    /**
     * 覆盖原有设备
     */
    public void coverAlreadyBindDevice(String accessId, String deviceTag, String url) {
        if (deviceAccessRep != null) {
            if (navigator != null) {
                navigator.setTopBarEnable(false);
            }
            deviceAccessRep.coverAlreadyBindDevice(accessId, deviceTag, url);
        }
    }

    private DeviceAccessListener listener = new DeviceAccessListener() {

        @Override
        public void accessNewDevice(DeviceInfo deviceInfo, String accessId, String deviceTag, String url) {
            if (navigator != null) {
                navigator.showConfirmAccessDeviceDialog(deviceInfo, accessId, deviceTag, url);
            }
        }

        @Override
        public void deviceAccessFail(int code, String msg) {
            if (navigator != null) {
                navigator.setUiEnable(true);
                navigator.setTopBarEnable(true);
                navigator.showAccessFailDialog(code, msg);
            }
        }

        @Override
        public void showMsgDialog(int code, String msg) {
            if (navigator != null) {
                navigator.showAccessFailDialog(code, msg);
            }
        }

        @Override
        public void deviceAccessFail(String msg) {
            if (navigator != null) {
                navigator.setUiEnable(true);
                navigator.setTopBarEnable(true);
            }
            ToastUtils.showShortToast(msg);
        }

        @Override
        public void deviceSecondSuccess(String message) {
            accessSuccess = true;
            editDeviceTagFlag = false;

            fieldBindSuccess.set(true);
            fieldAccessIdTvVisible.set(true);
            fieldAccessIdWarnVisible.set(false);
            fieldLlDeviceTagInputVisible.set(false);
            fieldDeviceTagModifyIvVisible.set(true);
            fieldDeviceTagWarnVisible.set(false);
            fieldDeviceTagRlVisible.set(true);
            fieldDeviceTagConfirmIvVisible.set(false);
            fieldServerIpEditLlVisible.set(false);
            fieldServerIpModifyIvVisible.set(false);

            if (navigator != null) {
                navigator.setUiEnable(true, CommonUtils.getStrFromRes(R.string.device_unbinding));
                navigator.setTopBarEnable(true);
                navigator.deviceAccessSuccess(message);
            }
        }

        @Override
        public void updateDeviceTagSuccess(String message) {
            fieldDeviceTagModifyIvVisible.set(true);
            fieldLlDeviceTagInputVisible.set(false);
            editDeviceTagFlag = false;
            if (navigator != null) {
                navigator.setUiEnable(true);
                navigator.setTopBarEnable(true);
                navigator.deviceAccessSuccess(message);
            }
        }

        @Override
        public void showCoverDeviceDialog(String accessId, String deviceTag, String url) {
            if (navigator != null) {
                navigator.showCoverDeviceDialog(accessId, deviceTag, url);
            }
        }
    };

    public void onClick(View v) {
        int resId = v.getId();
        if (DoubleClickUtils.isFastDoubleClick(resId)) {
            return;
        }
        switch (resId) {
            case R.id.iv_device_tag_modify:
                fieldLlDeviceTagInputVisible.set(true);
                fieldDeviceTagRlVisible.set(true);
                fieldDeviceTagConfirmIvVisible.set(true);
                fieldDeviceTagModifyIvVisible.set(false);
                fieldDeviceTagWarnVisible.set(false);
                editDeviceTagFlag = true;
                if (navigator != null) {
                    navigator.setUiEnable(true);
                }
                break;
            case R.id.iv_device_tag_modify_confirm:
                if (deviceAccessRep != null) {
                    Pair<Integer, String> pair1 = deviceAccessRep.checkDeviceTag(fieldDeviceTag.get());
                    if (pair1.first != DeviceAccessRepository.CODE_CHECK_DEVICE_INFO_SUCCESS) {
                        fieldDeviceTagWarnVisible.set(true);
                        fieldStringWarn2.set(pair1.second);
                        return;
                    }
                    if (navigator != null) {
                        navigator.setUiEnable(false);
                    }
                    deviceAccessRep.updateDeviceTag(fieldAccessId.get(), fieldDeviceTag.get(), fieldServerIp.get());
                }
                break;
            case R.id.iv_server_ip_modify:
                fieldServerIpEditLlVisible.set(true);
                break;
            case R.id.iv_server_ip_modify_confirm:
                String fieldServerUrl = fieldServerIp.get();
                if (fieldServerUrl == null) {
                    return;
                }
                String serverUrl = fieldServerUrl.trim().replaceAll(" ", "");
                if (TextUtils.isEmpty(serverUrl)) {
                    return;
                }
                if (deviceAccessRep != null) {
                    if (deviceAccessRep.updateServerUrl(serverUrl)) {
                        fieldServerIpEditLlVisible.set(false);
                        fieldServerIpModifyIvVisible.set(true);
                        fieldServerIp.set(serverUrl);
                    }
                    KeyboardUtils.hideSoftInput(ActivityUtils.getTopActivity());
                }
                break;
            case R.id.btn_device_access_unbind:
                if (deviceAccessRep == null) {
                    return;
                }
                if (accessSuccess) {
                    //设备解绑
                    if (navigator != null) {
                        navigator.showConfirmUnbindDialog();
                    }
                } else {
                    //设备接入
                    if (navigator != null) {
                        navigator.setUiEnable(false);
                    }
                    String accessId = fieldAccessId.get();
                    String strAccessId = "";
                    if (accessId != null) {
                        strAccessId = accessId.trim().replace(" ", "").toUpperCase();
                    }
                    String deviceTag = fieldDeviceTag.get();
                    String strDeviceTag = "";
                    if (deviceTag != null) {
                        strDeviceTag = deviceTag.trim().replace(" ", "");
                    }
                    String serverIp = fieldServerIp.get();
                    String strServerIp = "";
                    if (serverIp != null) {
                        strServerIp = serverIp.trim().replace(" ", "");
                    }
                    deviceAccessRep.accessDeviceFirst(strAccessId, strDeviceTag, strServerIp, ((type, msg) -> {
                        if (type == DeviceAccessRepository.TYPE_ACCESS_ID) {
                            fieldAccessIdWarnVisible.set(true);
                            fieldStringWarn1.set(msg);
                        } else if (type == DeviceAccessRepository.TYPE_DEVICE_TAG) {
                            fieldDeviceTagWarnVisible.set(!editDeviceTagFlag);
                            fieldDeviceTagRlVisible.set(true);
                            fieldStringWarn2.set(msg);
                        } else {
                            fieldServerURLWarnVisible.set(true);
                            fieldStringWarn3.set(msg);
                        }
                        if (navigator != null) {
                            navigator.setUiEnable(true);
                        }
                    }));
                }
                break;
            default:
                break;
        }
    }
}
