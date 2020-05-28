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

import android.text.TextUtils;
import android.view.View;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.ObservableField;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.business.common.CommonRepository;
import com.arcsoft.arcfacesingle.business.setting.ConfigConstants;
import com.arcsoft.arcfacesingle.business.setting.ISetting;
import com.arcsoft.arcfacesingle.business.setting.SettingRepository;
import com.arcsoft.arcfacesingle.data.db.table.TableSettingConfigInfo;
import com.arcsoft.arcfacesingle.data.model.ConfigurationInfo;
import com.arcsoft.arcfacesingle.server.api.LocalHttpApiDataUtils;
import com.arcsoft.arcfacesingle.service.CloudAIotService;
import com.arcsoft.arcfacesingle.service.OfflineLanService;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.arcfacesingle.util.SdcardUtils;
import com.arcsoft.asg.libcommon.util.business.DoubleClickUtils;
import com.arcsoft.asg.libcommon.util.business.rx.RxUtils;
import com.arcsoft.asg.libcommon.util.common.AppUtils;
import com.arcsoft.asg.libcommon.util.common.FileUtils;
import com.arcsoft.asg.libcommon.util.common.ScreenUtils;
import com.arcsoft.asg.libcommon.util.common.ToastUtils;
import com.arcsoft.asg.libnetwork.helper.LocalHttpHelper;
import com.google.gson.Gson;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;

public class SettingViewModel extends BaseObservable {

    private NewSettingListener newSettingListener;
    private ISetting settingRepository;
    private String tempDevicePort;

    @Bindable
    public ObservableField<Boolean> addFaceVisible = new ObservableField<>();
    public ObservableField<String> strVersion = new ObservableField<>();

    public SettingViewModel() {
        settingRepository = SettingRepository.getInstance();
        tempDevicePort = CommonRepository.getInstance().getSettingConfigInfo().getDevicePort();
        if (CommonUtils.isOfflineLanAppMode()) {
            addFaceVisible.set(true);
        }
        if (CommonUtils.isCloudAiotAppMode()) {
            addFaceVisible.set(false);
        }
        addFaceVisible.set(true);
        String strVersionName = AppUtils.getAppVersionName();
        if (ScreenUtils.isLandscape()) {
            strVersion.set(CommonUtils.getStrFromRes(R.string.version2, strVersionName));
        }
        if (ScreenUtils.isPortrait()) {
            strVersion.set(CommonUtils.getStrFromRes(R.string.version1, strVersionName));
        }
    }

    public void onClick(View view) {
        int resId = view.getId();
        if (DoubleClickUtils.isFastDoubleClick(resId)) {
            return;
        }
        switch (view.getId()) {
            case R.id.btn_face_register:
                if (newSettingListener != null) {
                    newSettingListener.goFaceManager();
                }
                break;
            case R.id.btn_device_active:
                if (newSettingListener != null) {
                    newSettingListener.goDeviceActive();
                }
                break;
            default:
                break;
        }
    }

    private void loadConfig(TableSettingConfigInfo srcInfo, TableSettingConfigInfo desInfo) {
        desInfo.setCompanyName(srcInfo.getCompanyName());
        desInfo.setMainImagePath(srcInfo.getMainImagePath());
        desInfo.setViceImagePath(srcInfo.getViceImagePath());
        desInfo.setDisplayMode(srcInfo.getDisplayMode());
        desInfo.setVoiceMode(srcInfo.getVoiceMode());
        desInfo.setDisplayModeFail(srcInfo.getDisplayModeFail());
        desInfo.setVoiceModeFail(srcInfo.getVoiceModeFail());
        desInfo.setCustomDisplayModeFormat(srcInfo.getCustomDisplayModeFormat());
        desInfo.setCustomVoiceModeFormat(srcInfo.getCustomVoiceModeFormat());
        desInfo.setCustomFailDisplayModeFormat(srcInfo.getCustomFailDisplayModeFormat());
        desInfo.setCustomFailVoiceModeFormat(srcInfo.getCustomFailVoiceModeFormat());

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

        desInfo.setSimilarThreshold(srcInfo.getSimilarThreshold());
        desInfo.setRecognitionRetryDelay(srcInfo.getRecognitionRetryDelay());
        desInfo.setSuccessRetry(srcInfo.getSuccessRetry());
        desInfo.setSuccessRetryDelay(srcInfo.getSuccessRetryDelay());
        desInfo.setLivenessDetect(srcInfo.isLivenessDetect());
        desInfo.setLiveDetectType(srcInfo.getLiveDetectType());
        desInfo.setIrLivePreview(srcInfo.getIrLivePreview());
        desInfo.setIrLiveThreshold(srcInfo.getIrLiveThreshold());
        desInfo.setRecognizeDistance(srcInfo.getRecognizeDistance());

        desInfo.setFaceQuality(srcInfo.isFaceQuality());
        desInfo.setFaceQualityThreshold(srcInfo.getFaceQualityThreshold());
        desInfo.setDeviceName(srcInfo.getDeviceName());
    }

    public boolean saveSettings(String strDevicePort, TableSettingConfigInfo configInfo) {
        boolean result = false;
        if (TextUtils.isEmpty(strDevicePort)) {
            ToastUtils.showShortToast(R.string.please_input_device_port);
            return false;
        }
        if (strDevicePort.length() != 0) {
            int devicePort = Integer.parseInt(strDevicePort);
            if (devicePort < ConfigConstants.DEVICE_PORT_MIN || devicePort > ConfigConstants.DEVICE_PORT_MAX) {
                ToastUtils.showShortToast(R.string.device_port_range);
                return false;
            }

            if (settingRepository.checkSaveSettingConfigInfo(configInfo)) {
                boolean isPortChanged = tempDevicePort.compareTo(strDevicePort) != 0;
                if (isPortChanged) {
                    if (CommonUtils.isOfflineLanAppMode()) {
                        LocalHttpHelper.getInstance().stopServer();
                    }
                    configInfo.setDevicePort(strDevicePort);
                }
                TableSettingConfigInfo desInfo = CommonRepository.getInstance().getSettingConfigInfo();
                loadConfig(configInfo, desInfo);
                result = settingRepository.saveSettingConfigInfo(desInfo);
                if (result) {
                    Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
                        ConfigurationInfo configurationInfo = new ConfigurationInfo();
                        configurationInfo.loadFromDatabase(desInfo);
                        String strJson = new Gson().toJson(configurationInfo);
                        String path = SdcardUtils.getInstance().getSettingsPath() + File.separator
                                + Constants.USB_FILE_NAME_SETTING;
                        File file = new File(path);
                        FileUtils.createFileByDeleteOldFile(file);
                        FileUtils.write(file, strJson);
                        emitter.onNext(true);
                        emitter.onComplete();
                    }).compose(RxUtils.ioToMain())
                            .subscribe();
                }
                if (isPortChanged) {
                    //生效端口更改
                    if (CommonUtils.isOfflineLanAppMode()) {
                        LocalHttpHelper.getInstance().startServer(LocalHttpApiDataUtils.getLocalApiPort());
                    }
                    tempDevicePort = configInfo.getDevicePort();
                }
                boolean rebootDevice = configInfo.isRebootEveryDay();
                if (rebootDevice) {
                    if (CommonUtils.isOfflineLanAppMode()) {
                        OfflineLanService.stopReboot();
                        OfflineLanService.startReboot();
                    }
                    if (CommonUtils.isCloudAiotAppMode()) {
                        CloudAIotService.stopReboot();
                        CloudAIotService.startReboot();
                    }
                } else {
                    if (CommonUtils.isOfflineLanAppMode()) {
                        OfflineLanService.stopReboot();
                    }
                    if (CommonUtils.isCloudAiotAppMode()) {
                        CloudAIotService.stopReboot();
                    }
                }
            }
        }

        return result;
    }

    public void setNewSettingListener(NewSettingListener listener) {
        newSettingListener = listener;
    }

    public void onResume() {
        if (settingRepository != null) {
            settingRepository.startDelayClosePageTimer(() -> {
                if (newSettingListener != null) {
                    newSettingListener.goRecognize();
                }
            });
        }
    }

    public void onPause() {
        if (settingRepository != null) {
            settingRepository.disposeDelayClosePageTimer();
        }
    }

    public void release() {
        if (settingRepository != null) {
            settingRepository.unInit();
        }
        settingRepository = null;
        newSettingListener = null;
    }

    public interface NewSettingListener {
        /**
         * 返回
         */
        void goRecognize();

        /**
         * 跳转注册
         */
        void goFaceManager();

        /**
         * 跳转激活
         */
        void goDeviceActive();
    }
}
