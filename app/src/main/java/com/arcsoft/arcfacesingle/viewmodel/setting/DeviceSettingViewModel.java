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

package com.arcsoft.arcfacesingle.viewmodel.setting;

import android.annotation.SuppressLint;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Switch;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.ObservableField;

import com.arcsoft.arcfacesingle.BR;
import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.business.common.CommonRepository;
import com.arcsoft.arcfacesingle.business.setting.ConfigConstants;
import com.arcsoft.arcfacesingle.data.db.table.TableSettingConfigInfo;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.asg.libcommon.util.common.DeviceUtils;
import com.arcsoft.asg.libcommon.util.common.NetworkUtils;
import com.arcsoft.asg.libnetwork.manage.DefaultRemoteApiManager;

import java.util.List;

public class DeviceSettingViewModel extends BaseObservable {

    private static final String TAG = DeviceSettingViewModel.class.getSimpleName();
    private static final int COUNT_MAC_ADDRESS = 2;

    private TableSettingConfigInfo settingConfigInfo;
    private DeviceSettingListener deviceSettingListener;

    @Bindable
    public final ObservableField<TableSettingConfigInfo> settingInfo = new ObservableField<>();
    public final ObservableField<String> strIp = new ObservableField<>();
    public final ObservableField<String> strMacAddress = new ObservableField<>();
    public final ObservableField<String> strMacAddress2 = new ObservableField<>();
    public final ObservableField<String> strSN = new ObservableField<>();
    public final ObservableField<String> strDelay = new ObservableField<>();
    public final ObservableField<String> strPort = new ObservableField<>();
    public final ObservableField<Boolean> uploadImageEnable = new ObservableField<>();
    public final ObservableField<Boolean> bPortVisible = new ObservableField<>();
    public final ObservableField<Boolean> fieldMacAddress2Visible = new ObservableField<>();
    public final ObservableField<String> fieldDeviceName = new ObservableField<>();
    public final ObservableField<String> fieldServerIp = new ObservableField<>();
    public final ObservableField<Boolean> fieldConnectStatus = new ObservableField<>(false);

    public DeviceSettingViewModel() {
        settingConfigInfo = new TableSettingConfigInfo();
        settingInfo.set(settingConfigInfo);
    }

    private void notifySettingInfo() {
        notifyPropertyChanged(BR.settingInfo);
    }

    public void reloadSettingInfo(TableSettingConfigInfo srcInfo, TableSettingConfigInfo desInfo) {
        desInfo.setDeviceIp(srcInfo.getDeviceIp());
        desInfo.setMacAddress(srcInfo.getMacAddress());
        desInfo.setSerialNumber(srcInfo.getSerialNumber());
        desInfo.setDevicePort(srcInfo.getDevicePort());
        desInfo.setDeviceSleepFollowSys(srcInfo.isDeviceSleepFollowSys());
        desInfo.setScreenBrightFollowSys(srcInfo.isScreenBrightFollowSys());
        desInfo.setScreenDefBrightPercent(srcInfo.getScreenDefBrightPercent());
        desInfo.setIndexScreenDefShow(srcInfo.isIndexScreenDefShow());
        desInfo.setRebootEveryDay(srcInfo.isRebootEveryDay());
        desInfo.setRebootHour(srcInfo.getRebootHour());
        desInfo.setRebootMin(srcInfo.getRebootMin());
        desInfo.setCloseDoorDelay(srcInfo.getCloseDoorDelay());
        desInfo.setUploadRecordImage(srcInfo.getUploadRecordImage());
        desInfo.setDeviceName(srcInfo.getDeviceName());
        desInfo.setServerIp(srcInfo.getServerIp());
    }

    public void getConfig(TableSettingConfigInfo configInfo) {
        settingConfigInfo.setDevicePort(strPort.get());
        settingConfigInfo.setDeviceName(fieldDeviceName.get());
        reloadSettingInfo(settingConfigInfo, configInfo);
    }

    public void onRadioGroupCheckedChanged(RadioGroup group, int checkedId) {
        if (group.getId() == R.id.rg_device_sleep) {
            boolean followSys = checkedId != R.id.rb_device_sleep_not_sleep;
            settingInfo.get().setDeviceSleepFollowSys(followSys);
        } else if (group.getId() == R.id.rg_screen_default_bright_switch) {
            boolean followSys = checkedId != R.id.rb_screen_default_bright_custom;
            settingInfo.get().setScreenBrightFollowSys(followSys);
            if (followSys && TextUtils.isEmpty(settingInfo.get().getScreenDefBrightPercent())) {
                settingInfo.get().setScreenDefBrightPercent(String.valueOf(ConfigConstants.SCREEN_DEFAULT_BRIGHT));
            }
        }

        notifySettingInfo();
    }

    public void onScreenDefaultBrightTextChanged(Editable editable) {
        String strContent = editable.toString().trim();
        if (!TextUtils.isEmpty(strContent)) {
            int percent = Integer.parseInt(strContent);
            if (percent > ConfigConstants.SCREEN_DEFAULT_BRIGHT) {
                percent = ConfigConstants.SCREEN_DEFAULT_BRIGHT;
            }
            settingInfo.get().setScreenDefBrightPercent(String.valueOf(percent));
        } else {
            settingInfo.get().setScreenDefBrightPercent(strContent);
        }
        notifySettingInfo();
    }

    public void onDeviceRebootHourTextChanged(Editable editable) {
        String strContent = editable.toString().trim();
        if (!TextUtils.isEmpty(strContent)) {
            int hour = Integer.parseInt(strContent);
            if (hour > ConfigConstants.DEVICE_REBOOT_HOUR) {
                hour = ConfigConstants.DEVICE_REBOOT_HOUR;
            }
            settingInfo.get().setRebootHour(String.valueOf(hour));
        } else {
            settingInfo.get().setRebootHour(strContent);
        }
        notifySettingInfo();
    }

    public void onDeviceRebootMinTextChanged(Editable editable) {
        String strContent = editable.toString().trim();
        if (!TextUtils.isEmpty(strContent)) {
            int min = Integer.parseInt(strContent);
            if (min > ConfigConstants.DEVICE_REBOOT_MIN) {
                min = ConfigConstants.DEVICE_REBOOT_MIN;
            }
            settingInfo.get().setRebootMin(String.valueOf(min));
        } else {
            settingInfo.get().setRebootMin(strContent);
        }
        notifySettingInfo();
    }

    public void onCloseDoorDelayTextChanged(Editable editable) {
        String strContent = editable.toString().trim();
        if (!TextUtils.isEmpty(strContent)) {
            if (".".equals(strContent)) {
                strContent = "0.";
                editable.insert(0, strContent);
            }
            String[] strArr = strContent.split("\\.");
            if (strArr.length > 1) {
                if (strArr[1].length() > 1) {
                    strContent = strArr[0] + "." + strArr[1].substring(0, 1);
                    strDelay.set(strContent);
                }
            }
            float closeDelay = Float.parseFloat(strContent);
            if (closeDelay > ConfigConstants.TIME_DELAY_MAX) {
                closeDelay = ConfigConstants.TIME_DELAY_MAX;
                strDelay.set(String.valueOf(closeDelay));
            }
            settingInfo.get().setCloseDoorDelay(String.valueOf(closeDelay));
        } else {
            settingInfo.get().setCloseDoorDelay(strContent);
        }
        notifySettingInfo();
    }

    public void onSwitchClicked(View view) {
        boolean value = ((Switch) view).isChecked();
        if (view.getId() == R.id.sh_upload_image_key) {
            Integer iValue = value ? ConfigConstants.DEFAULT_UPLOAD_RECORD_IMAGE : ConfigConstants.DEFAULT_UPLOAD_RECORD_IMAGE - 1;
            settingInfo.get().setUploadRecordImage(iValue);
        } else if (view.getId() == R.id.sh_device_reboot) {
            settingInfo.get().setRebootEveryDay(value);
            if (TextUtils.isEmpty(settingInfo.get().getRebootHour())) {
                settingInfo.get().setRebootHour(ConfigConstants.DEFAULT_DEVICE_REBOOT_HOUR);
            }
            if (TextUtils.isEmpty(settingInfo.get().getRebootMin())) {
                settingInfo.get().setRebootMin(ConfigConstants.DEFAULT_DEVICE_REBOOT_MIN);
            }
        } else if (view.getId() == R.id.sh_screen_default_show) {
            settingInfo.get().setIndexScreenDefShow(value);
        }
        notifySettingInfo();
    }

    public void onDisconnect(View view) {
        if (view.getId() == R.id.ib_disconnect) {
            if (deviceSettingListener != null) {
                deviceSettingListener.showDisconnectDialog(fieldServerIp.get());
            }
        }
    }

    public void disconnect() {
        TableSettingConfigInfo configInfo = CommonRepository.getInstance().getSettingConfigInfo();
        resetServerStatus(configInfo);
        CommonRepository.getInstance().saveSettingConfigSync(configInfo);
        DefaultRemoteApiManager.getInstance().unInit();
        updateConnectStatus(false, null);
    }

    public void updateConnectStatus(boolean connected, String ip) {
        fieldConnectStatus.set(connected);
        fieldServerIp.set(getServerIp(ip));
    }

    private void resetServerStatus(TableSettingConfigInfo tableSettingConfigInfo) {
        tableSettingConfigInfo.setDeviceId(0);
        tableSettingConfigInfo.setSignKey("");
        tableSettingConfigInfo.setServerIp(ConfigConstants.DEFAULT_SERVER_IP);
        tableSettingConfigInfo.setServerPort(ConfigConstants.DEFAULT_SERVER_PORT);
    }

    public void setDeviceSettingListener(DeviceSettingListener listener) {
        deviceSettingListener = listener;
        onVisible();
        strPort.set(settingConfigInfo.getDevicePort());
    }

    public void setIpAndMacAddress() {
        String ipAddress = NetworkUtils.getIPAddress(true);
        strIp.set(ipAddress);
        String macAddress = updateMacAddress();
        settingConfigInfo.setDeviceIp(ipAddress);
        settingConfigInfo.setMacAddress(macAddress);
        CommonRepository.getInstance().setIpAndMacAddress(ipAddress, macAddress);
    }

    @SuppressLint("MissingPermission")
    public void onVisible() {
        reloadSettingInfo(CommonRepository.getInstance().getSettingConfigInfo(), settingConfigInfo);
        String stringIp = NetworkUtils.getIPAddress(true);
        settingConfigInfo.setDeviceIp(stringIp);
        settingConfigInfo.setSerialNumber(DeviceUtils.getSerial());
        uploadImageEnable.set(true);
        strIp.set(stringIp);
        String macAddress = updateMacAddress();
        settingConfigInfo.setMacAddress(macAddress);
        strSN.set(settingConfigInfo.getSerialNumber());
        strDelay.set(settingConfigInfo.getCloseDoorDelay());
        fieldDeviceName.set(settingConfigInfo.getDeviceName());
        fieldServerIp.set(getServerIp(settingConfigInfo.getServerIp()));
        fieldConnectStatus.set(!TextUtils.isEmpty(settingConfigInfo.getServerIp()));
        notifySettingInfo();
    }

    private String getServerIp(String Ip) {
        if (TextUtils.isEmpty(Ip)) {
            return CommonUtils.getStrFromRes(R.string.connect_offline);
        } else {
            return CommonUtils.getStrFromRes(R.string.connect_server) + Ip;
        }
    }

    public void onDestroy() {
        settingConfigInfo = null;
        deviceSettingListener = null;
    }

    public void onResume() {
        String macAddress = updateMacAddress();
        if (TextUtils.isEmpty(macAddress)) {
            if (deviceSettingListener != null) {
                deviceSettingListener.showWifiWarnDialog();
            }
        }
        bPortVisible.set(CommonUtils.isOfflineLanAppMode());
    }

    private String updateMacAddress() {
        List<String> macList = DeviceUtils.getMacAddressList();
        String macAddress;
        int size = macList.size();
        if (size == COUNT_MAC_ADDRESS) {
            macAddress = macList.get(0);
            String macAddress2 = macList.get(1);
            fieldMacAddress2Visible.set(true);
            strMacAddress2.set(macAddress2);
            NetworkUtils.NetworkType networkType = NetworkUtils.getNetworkType();
        } else if (size == COUNT_MAC_ADDRESS - 1) {
            macAddress = macList.get(0);
            fieldMacAddress2Visible.set(false);
        } else {
            macAddress = "";
            fieldMacAddress2Visible.set(false);
        }
        strMacAddress.set(macAddress);
        return macAddress;
    }

    public interface DeviceSettingListener {
        /**
         * 显示wifi开关警告弹框
         */
        void showWifiWarnDialog();

        /**
         * 修改mac地址控件UI
         *
         * @param firstText
         */
        void updateMacAddressUi(boolean firstText);

        void showDisconnectDialog(String content);
    }
}
