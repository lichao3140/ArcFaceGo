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

package com.arcsoft.arcfacesingle.business.common;

import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.business.selectmode.ISelectMode;
import com.arcsoft.arcfacesingle.business.setting.ConfigConstants;
import com.arcsoft.arcfacesingle.business.setting.SettingRepDataManager;
import com.arcsoft.arcfacesingle.data.db.dao.PersonDao;
import com.arcsoft.arcfacesingle.data.db.dao.SettingConfigInfoDao;
import com.arcsoft.arcfacesingle.data.db.table.TablePerson;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonFace;
import com.arcsoft.arcfacesingle.data.db.table.TableSettingConfigInfo;
import com.arcsoft.arcfacesingle.data.event.ChangeMainLogoEvent;
import com.arcsoft.arcfacesingle.data.event.ChangeSecondLogoEvent;
import com.arcsoft.arcfacesingle.data.event.ReInitFaceEngineEvent;
import com.arcsoft.arcfacesingle.data.event.ReOpenCameraEvent;
import com.arcsoft.arcfacesingle.data.event.SettingConfigChangedEvent;
import com.arcsoft.arcfacesingle.data.model.ConfigurationInfo;
import com.arcsoft.arcfacesingle.data.model.DeviceAccessInfo;
import com.arcsoft.arcfacesingle.libutil.ImageFileUtils;
import com.arcsoft.arcfacesingle.server.api.LocalHttpApiDataUtils;
import com.arcsoft.arcfacesingle.server.api.LocalHttpImpl;
import com.arcsoft.arcfacesingle.server.api.ServerConstants;
import com.arcsoft.arcfacesingle.server.pojo.base.ResponseBase;
import com.arcsoft.arcfacesingle.server.pojo.request.RequestSetting;
import com.arcsoft.arcfacesingle.service.CloudAIotService;
import com.arcsoft.arcfacesingle.service.OfflineLanService;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.arcfacesingle.util.SdcardUtils;
import com.arcsoft.arcfacesingle.util.download.ApkDownloadManager;
import com.arcsoft.arcfacesingle.util.download.FileDownloadManager;
import com.arcsoft.arcfacesingle.util.scheduler.BaseObserver;
import com.arcsoft.arcfacesingle.util.scheduler.ExceptionHandler;
import com.arcsoft.arcsoftlink.enums.ArcFaceVersionEnum;
import com.arcsoft.arcsoftlink.enums.ArcLinkErrorCodeEnum;
import com.arcsoft.arcsoftlink.http.bean.res.UploadDataResponse;
import com.arcsoft.arcsoftlink.mqtt.ArcLinkEngine;
import com.arcsoft.arcsoftlink.mqtt.InitResult;
import com.arcsoft.arcsoftlink.mqtt.bean.UpgradeResult;
import com.arcsoft.asg.libcommon.util.business.rx.RxUtils;
import com.arcsoft.asg.libcommon.util.common.AppUtils;
import com.arcsoft.asg.libcommon.util.common.DeviceUtils;
import com.arcsoft.asg.libcommon.util.common.FileUtils;
import com.arcsoft.asg.libcommon.util.common.Md5Utils;
import com.arcsoft.asg.libcommon.util.common.NetworkUtils;
import com.arcsoft.asg.libcommon.util.common.SPUtils;
import com.arcsoft.asg.libcommon.util.common.StringUtils;
import com.arcsoft.asg.libcommon.util.common.ToastUtils;
import com.arcsoft.asg.libcommon.util.common.UUIDUtils;
import com.arcsoft.asg.libcommon.util.common.Utils;
import com.arcsoft.asg.libdeviceadapt.bean.DeviceAdaptationInfo;
import com.arcsoft.asg.libdeviceadapt.bean.DeviceCameraInfo;
import com.arcsoft.asg.libnetwork.helper.LocalHttpHelper;
import com.arcsoft.faceengine.Config;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import okhttp3.Call;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class CommonRepository implements ICommon {

    private static final String TAG = CommonRepository.class.getSimpleName();

    /**
     * 灯控状态
     */
    public static final int OPEN_WHITE_LAMP = 0;
    public static final int OPEN_RED_LAMP = 1;
    public static final int OPEN_GREEN_LAMP = 2;
    public static final int CLOSE_ALL_LAMP = 3;
    public static final int LOGO_MAX_WIDTH = 500;

    private TableSettingConfigInfo configInfo;
    private Disposable initArcLinkDisposable;
    private Disposable openDoorDisposable;

    private CommonRepository() {
    }

    private static volatile CommonRepository commonRepository;

    public static CommonRepository getInstance() {
        if (commonRepository == null) {
            synchronized (CommonRepository.class) {
                if (commonRepository == null) {
                    commonRepository = new CommonRepository();
                }
            }
        }
        return commonRepository;
    }

    @Override
    public boolean saveSettingConfigSync(TableSettingConfigInfo configInfo) {
        this.configInfo = configInfo;
        return SettingConfigInfoDao.getInstance().saveSetting(configInfo);
    }

    @Override
    public void saveSettingConfigAsync(TableSettingConfigInfo configInfo, SettingConfigInfoDao.SettingConfigCallback callback) {
        this.configInfo = configInfo;
        SettingConfigInfoDao.getInstance().saveSetting(configInfo, callback);
    }

    @Override
    public void setIpAndMacAddress(String ipAddress, String macAddress) {
        if (configInfo != null) {
            configInfo.setDeviceIp(ipAddress);
            configInfo.setMacAddress(macAddress);
            SettingConfigInfoDao.getInstance().saveSetting(configInfo, null);
        }
    }

    @Override
    public synchronized TableSettingConfigInfo getSettingConfigInfo() {
        if (configInfo == null) {
            TableSettingConfigInfo dbConfig = SettingConfigInfoDao.getInstance().getSetting();
            if (dbConfig == null) {
                configInfo = new TableSettingConfigInfo();
                configInfo.setDeviceIp(NetworkUtils.getIPAddress(true));
                saveSettingConfigAsync(configInfo, null);
            } else {
                //***************V2.0.0版本数据兼容***************//
                boolean update = false;
                if (dbConfig.getSuccessRetryDelay() == null) {
                    dbConfig.setSuccessRetryDelay(ConfigConstants.DEFAULT_RETRY_DELAY);
                    update = true;
                }
                if (dbConfig.getSuccessRetry() == null) {
                    dbConfig.setSuccessRetry(ConfigConstants.DEFAULT_RECOGNITION_SUCCESS_RETRY);
                    update = true;
                }
                if (dbConfig.getUploadRecordImage() == null) {
                    dbConfig.setUploadRecordImage(ConfigConstants.DEFAULT_UPLOAD_RECORD_IMAGE);
                    update = true;
                }
                if (dbConfig.getLiveDetectType() == null) {
                    dbConfig.setLiveDetectType(ConfigConstants.DEFAULT_LIVE_DETECT_RGB);
                    update = true;
                }
                if (dbConfig.getIrLivePreview() == null) {
                    dbConfig.setIrLivePreview(ConfigConstants.DEFAULT_IR_LIVE_PREVIEW_HIDE);
                    update = true;
                }

                //***************V2.0.27(2019.09.06)数据兼容***************//
                if (dbConfig.getIrLiveThreshold() == null) {
                    dbConfig.setIrLiveThreshold(ConfigConstants.DEFAULT_IR_LIVE_THRESHOLD);
                    update = true;
                }
                if (TextUtils.isEmpty(dbConfig.getSuccessRetryDelay())) {
                    dbConfig.setSuccessRetryDelay(ConfigConstants.DEFAULT_RETRY_DELAY);
                    update = true;
                }

                if (dbConfig.getFaceQualityThreshold() == null) {
                    dbConfig.setFaceQualityThreshold(ConfigConstants.DEFAULT_FACE_QUALITY_THRESHOLD);
                    dbConfig.setFaceQuality(true);
                    update = true;
                }

                if (dbConfig.getDeviceName() == null) {
                    String deviceTag = SPUtils.getInstance().getString(Constants.SP_KEY_DEVICE_TAG);
                    if (!TextUtils.isEmpty(deviceTag)) {
                        dbConfig.setDeviceName(deviceTag);
                        update = true;
                    }
                }

                configInfo = dbConfig;
                if (update) {
                    saveSettingConfigAsync(configInfo, null);
                }
            }
        }
        return configInfo;
    }

    /**
     * 初始化主logo，保存
     */
    public void initMainLogo() {
        Disposable disposable = Observable.create((ObservableOnSubscribe<String>) emitter -> {
            String url = getSettingConfigInfo().getMainImagePath();
            if (url == null || (!url.isEmpty() && !FileUtils.isFileExists(url))) {
                url = ConfigConstants.DEFAULT_MAIN_LOGO_FILE_PATH;
                File mainLogoFile = new File(url);
                Bitmap bmpMainLogo = ImageFileUtils.getBitmap(R.mipmap.ic_company_main_logo);
                if (ImageFileUtils.save(bmpMainLogo, mainLogoFile, Bitmap.CompressFormat.PNG)) {
                    getSettingConfigInfo().setMainImagePath(url);
                    saveMainLogoId(createDatabaseId());
                    emitter.onNext(url);
                } else {
                    emitter.onNext("");
                }
            } else {
                emitter.onNext("");
            }
            emitter.onComplete();
        }).compose(RxUtils.ioToMain())
                .subscribeWith(new BaseObserver<String>() {
                    @Override
                    public void onNext(String imagePath) {
                        if (!TextUtils.isEmpty(imagePath)) {
                            EventBus.getDefault().post(new ChangeMainLogoEvent(imagePath));
                        }
                    }
                });
    }

    /**
     * 初始化副logo，保存
     */
    public void initSecondLogo() {
        Disposable disposable = Observable.create((ObservableOnSubscribe<String>) emitter -> {
            String url = getSettingConfigInfo().getViceImagePath();
            if (url == null || (!url.isEmpty() && !FileUtils.isFileExists(url))) {
                url = ConfigConstants.DEFAULT_SECOND_LOGO_FILE_PATH;
                File secondLogoFile = new File(url);
                Bitmap bmpSecondLogo = ImageFileUtils.getBitmap(R.mipmap.ic_company_second_logo);
                if (ImageFileUtils.save(bmpSecondLogo, secondLogoFile, Bitmap.CompressFormat.PNG)) {
                    getSettingConfigInfo().setViceImagePath(url);
                    saveSecondLogoId(createDatabaseId());
                    emitter.onNext(url);
                } else {
                    emitter.onNext("");
                }
            } else {
                emitter.onNext("");
            }
            emitter.onComplete();
        }).compose(RxUtils.ioToMain())
                .subscribeWith(new BaseObserver<String>() {
                    @Override
                    public void onNext(String imagePath) {
                        if (!TextUtils.isEmpty(imagePath)) {
                            EventBus.getDefault().post(new ChangeSecondLogoEvent(imagePath));
                        }
                    }
                });
    }

    @Override
    public void closeLamp(int lightType) {
    }

    @Override
    public void openLamp(int lightType) {
        if (lightType == OPEN_GREEN_LAMP) {
            sendBroadcast(Constants.ACTION_TURN_ON_GREEN_LIGHT);
        } else if (lightType == OPEN_RED_LAMP) {
            if (getSettingConfigInfo().getDisplayModeFail() != ConfigConstants.DISPLAY_MODE_FAILED_NOT_FEEDBACK) {
                sendBroadcast(Constants.ACTION_TURN_ON_RED_LIGHT);
            }
        }
    }

    @Override
    public void openDoor() {
        TableSettingConfigInfo settingConfigInfo = getSettingConfigInfo();
        float closeDoorDelay = Float.parseFloat(settingConfigInfo.getCloseDoorDelay());
        sendBroadcast(Constants.ACTION_OPEN_DOOR);
        long closeDelay = (long) closeDoorDelay * 1000;
        if (openDoorDisposable != null && !openDoorDisposable.isDisposed()) {
            openDoorDisposable.dispose();
            openDoorDisposable = null;
        }
        openDoorDisposable = Observable.timer(closeDelay, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> sendBroadcast(Constants.ACTION_CLOSE_DOOR));
    }

    @Override
    public void reboot() {
        if (DeviceUtils.isRooted()) {
            deviceReboot();
        } else {
            ToastUtils.showShortToast(CommonUtils.getStrFromRes(R.string.device_is_not_root));
        }
    }

    @Override
    public void rebootDelay(long delayTime) {
        if (DeviceUtils.isRooted()) {
            Disposable disposable = Observable.timer(delayTime, TimeUnit.MILLISECONDS)
                    .compose(RxUtils.computingToMain())
                    .subscribe(aLong -> deviceReboot());
        } else {
            ToastUtils.showShortToast(CommonUtils.getStrFromRes(R.string.device_is_not_root));
        }
    }

    private void deviceReboot() {
        boolean flag1 = false;
        boolean flag2 = false;
        try {
            DeviceUtils.reboot();
        } catch (Exception e) {
            e.printStackTrace();
            flag1 = true;
        }
        try {
            DeviceUtils.reboot("");
        } catch (Exception e) {
            e.printStackTrace();
            flag2 = true;
        }
        if (flag1 && flag2) {
            ToastUtils.showShortToast(R.string.device_reboot_failed_please_check);
        }
    }

    @Override
    public ResponseBase checkDeviceParams(RequestSetting requestSetting, boolean offline) {
        TableSettingConfigInfo configInfo = CommonRepository.getInstance().getSettingConfigInfo();
        String msgError = CommonUtils.getStrFromRes(R.string.invalid_param);
        boolean needReUpdateEngineConfig = false;
        boolean needReOpenCamera = false;

        String password = requestSetting.getDevicePassword();
        if (password != null) {
            if (TextUtils.isEmpty(password)) {
                return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_PASSWORD_INVALID, msgError);
            }
            if (password.length() > Constants.MESSAGE_INPUT_STRING_MAX_LENGTH) {
                return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_PASSWORD_INVALID, msgError);
            }
            if (!StringUtils.matcherPassword(password)) {
                return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_PASSWORD_INVALID, msgError);
            }
            configInfo.setDevicePassword(password);
        }

        //**********门禁机设置************//
        String deviceName = requestSetting.getDeviceName();
        if (deviceName != null) {
            if (deviceName.length() <= Constants.MESSAGE_INPUT_STRING_MAX_LENGTH) {
                if (StringUtils.containChar(deviceName)) {
                    return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_DEVICE_NAME_INVALID, msgError);
                } else {
                    configInfo.setDeviceName(deviceName);
                }
            } else {
                return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_DEVICE_NAME_INVALID, msgError);
            }
        }
        Integer rebootEveryDay = requestSetting.getRebootEveryDay();
        if (rebootEveryDay != null) {
            if (rebootEveryDay != ConfigConstants.DEVICE_REBOOT_OPEN &&
                    rebootEveryDay != ConfigConstants.DEVICE_REBOOT_CLOSE) {
                return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_REBOOT_EVERYDAY_INVALID, msgError);
            } else {
                configInfo.setRebootEveryDay(rebootEveryDay == 1);
            }
        }
        String rebootHour = requestSetting.getRebootHour();
        if (rebootHour != null) {
            try {
                int hour = Integer.parseInt(rebootHour);
                if (hour > ConfigConstants.DEVICE_REBOOT_HOUR || hour < ConfigConstants.DEVICE_REBOOT_HOUR_MIN) {
                    return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_REBOOT_EVERYDAY_INVALID, msgError);
                } else {
                    configInfo.setRebootHour(rebootHour);
                }
            } catch (Exception e) {
                return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_REBOOT_EVERYDAY_INVALID, msgError);
            }
        }
        String rebootMin = requestSetting.getRebootMin();
        if (rebootMin != null) {
            try {
                int min = Integer.parseInt(rebootMin);
                if (min > ConfigConstants.DEVICE_REBOOT_MIN || min < ConfigConstants.DEVICE_REBOOT_MIN_MIN) {
                    return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_REBOOT_EVERYDAY_INVALID, msgError);
                } else {
                    configInfo.setRebootMin(rebootMin);
                }
            } catch (Exception e) {
                return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_REBOOT_EVERYDAY_INVALID, msgError);
            }
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
        String strCloseDoorDelay = requestSetting.getOpenDelay();
        if (requestSetting.getOpenDelay() != null) {
            try {
                double closeDoorDelay = Double.parseDouble(strCloseDoorDelay);
                if (CommonUtils.checkIsDoublePointTwo(strCloseDoorDelay) > ServerConstants.FAILED_RETRY_POINT_MAX_LENGTH) {
                    return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_OPEN_DELAY_INVALID, msgError);
                }
                if (closeDoorDelay > ConfigConstants.OUT_PUT_MODE_CUSTOM ||
                        closeDoorDelay < ConfigConstants.OUT_PUT_MODE_CUSTOM_MIN) {
                    return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_OPEN_DELAY_INVALID, msgError);
                } else {
                    configInfo.setCloseDoorDelay(strCloseDoorDelay);
                }
            } catch (Exception e) {
                return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_OPEN_DELAY_INVALID, msgError);
            }
        }
        Integer uploadRecordImage = requestSetting.getUploadRecordImage();
        if (uploadRecordImage != null) {
            if (uploadRecordImage != ConfigConstants.DEFAULT_UPLOAD_RECORD_IMAGE &&
                    uploadRecordImage != ConfigConstants.DEFAULT_UPLOAD_RECORD_IMAGE_CLOSE) {
                return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_UPLOAD_RECORD_IMAGE_INVALID, msgError);
            } else {
                configInfo.setUploadRecordImage(uploadRecordImage);
            }
        }

        //**********识别设置************//
        String strThreshold = requestSetting.getThreshold();
        if (strThreshold != null) {
            try {
                double threshold = Double.parseDouble(strThreshold);
                if (CommonUtils.checkIsDoublePointTwo(strThreshold) > ServerConstants.THRESHOLD_POINT_MAX_LENGTH) {
                    return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_THRESHOLD_INVALID, msgError);
                }
                if (threshold > ConfigConstants.THRESHOLD_MAX || threshold < ConfigConstants.THRESHOLD_MIN) {
                    return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_THRESHOLD_INVALID, msgError);
                } else {
                    if (!strThreshold.equals(configInfo.getSimilarThreshold())) {
                        needReUpdateEngineConfig = true;
                    }
                    configInfo.setSimilarThreshold(strThreshold);
                }
            } catch (Exception e) {
                return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_THRESHOLD_INVALID, msgError);
            }
        }

        String strInterval = requestSetting.getInterval();
        if (strInterval != null) {
            try {
                double interval = Double.parseDouble(strInterval);
                if (CommonUtils.checkIsDoublePointTwo(strInterval) > ServerConstants.FAILED_RETRY_POINT_MAX_LENGTH) {
                    return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_INTERVAL_INVALID, msgError);
                }
                if (interval > ConfigConstants.RETRY_DELAY_MAX ||
                        interval < ConfigConstants.RETRY_DELAY_MIN) {
                    return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_INTERVAL_INVALID, msgError);
                } else {
                    if (!strInterval.equals(configInfo.getRecognitionRetryDelay())) {
                        needReUpdateEngineConfig = true;
                    }
                    configInfo.setRecognitionRetryDelay(strInterval);
                }
            } catch (Exception e) {
                return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_INTERVAL_INVALID, msgError);
            }
        }

        Integer successRetry = requestSetting.getSuccessRetry();
        if (successRetry != null) {
            if (successRetry != ConfigConstants.RECOGNITION_SUCCESS_RETRY_OPEN &&
                    successRetry != ConfigConstants.DEFAULT_RECOGNITION_SUCCESS_RETRY) {
                return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_SUCCESS_RETRY_INVALID, msgError);
            } else {
                if (!successRetry.equals(configInfo.getSuccessRetry())) {
                    needReUpdateEngineConfig = true;
                }
                configInfo.setSuccessRetry(successRetry);
            }
        }
        String stringSuccessRetryDelay = requestSetting.getSuccessRetryDelay();
        if (stringSuccessRetryDelay != null) {
            try {
                double delay = Double.parseDouble(stringSuccessRetryDelay);
                if (CommonUtils.checkIsDoublePointTwo(stringSuccessRetryDelay) > ServerConstants.FAILED_RETRY_POINT_MAX_LENGTH) {
                    return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_SUCCESS_RETRY_INVALID, msgError);
                }
                if (delay > ConfigConstants.RETRY_DELAY_MAX || delay < ConfigConstants.RETRY_DELAY_MIN) {
                    return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_SUCCESS_RETRY_INVALID, msgError);
                } else {
                    if (!stringSuccessRetryDelay.equals(configInfo.getSuccessRetryDelay())) {
                        needReUpdateEngineConfig = true;
                    }
                    configInfo.setSuccessRetryDelay(stringSuccessRetryDelay);
                }
            } catch (Exception e) {
                return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_SUCCESS_RETRY_INVALID, msgError);
            }
        }

        Integer livenessType = requestSetting.getLivenessType();
        if (livenessType != null) {
            if (livenessType != ConfigConstants.DEFAULT_LIVE_DETECT_CLOSE &&
                    livenessType != ConfigConstants.DEFAULT_LIVE_DETECT_RGB &&
                    livenessType != ConfigConstants.DEFAULT_LIVE_DETECT_IR) {
                return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_LIVE_INVALID, msgError);
            } else {
                boolean newLiveOpen = (requestSetting.getLivenessType() != ConfigConstants.DEFAULT_LIVE_DETECT_CLOSE);
                boolean oldLiveOpen = SettingRepDataManager.getInstance().isFaceLiveOpen(configInfo);
                boolean newIrLiveOpen = (requestSetting.getLivenessType() == ConfigConstants.DEFAULT_LIVE_DETECT_IR);
                boolean oldIrLiveOpen = SettingRepDataManager.getInstance().isIrFaceLiveOpen(configInfo);
                if (newLiveOpen != oldLiveOpen) {
                    needReOpenCamera = true;
                    needReUpdateEngineConfig = true;
                }
                if (newLiveOpen == oldLiveOpen && newIrLiveOpen != oldIrLiveOpen) {
                    needReOpenCamera = true;
                    needReUpdateEngineConfig = true;
                }
                configInfo.setLivenessDetect(livenessType != 0);
                configInfo.setLiveDetectType(livenessType);
            }
        }
        Integer irLivePreview = requestSetting.getIrLivePreview();
        if (irLivePreview != null) {
            if (irLivePreview != ConfigConstants.DEFAULT_IR_LIVE_PREVIEW &&
                    irLivePreview != ConfigConstants.DEFAULT_IR_LIVE_PREVIEW_HIDE) {
                return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_LIVE_INVALID, msgError);
            } else {
                configInfo.setIrLivePreview(irLivePreview);
            }
        }
        boolean irLiveOpen;
        boolean showIrPreview = (configInfo.getIrLivePreview() == ConfigConstants.DEFAULT_IR_LIVE_PREVIEW);
        if (requestSetting.getLivenessType() != null) {
            irLiveOpen = (requestSetting.getLivenessType() == ConfigConstants.DEFAULT_LIVE_DETECT_IR);
        } else {
            irLiveOpen = (configInfo.getLiveDetectType() == ConfigConstants.DEFAULT_LIVE_DETECT_IR);
        }
        if (requestSetting.getIrLivePreview() != null) {
            showIrPreview = (requestSetting.getIrLivePreview() == ConfigConstants.DEFAULT_IR_LIVE_PREVIEW);
        }

        Integer signDistance = requestSetting.getSignDistance();
        if (signDistance != null) {
            if (signDistance != ConfigConstants.RECOGNITION_DISTANCE_TYPE1 &&
                    signDistance != ConfigConstants.RECOGNITION_DISTANCE_TYPE2 &&
                    signDistance != ConfigConstants.RECOGNITION_DISTANCE_TYPE3 &&
                    signDistance != ConfigConstants.RECOGNITION_DISTANCE_TYPE4 &&
                    signDistance != ConfigConstants.RECOGNITION_DISTANCE_TYPE5) {
                return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_SIGN_DISTANCE_INVALID, msgError);
            } else {
                if (signDistance != configInfo.getRecognizeDistance()) {
                    needReUpdateEngineConfig = true;
                }
                configInfo.setRecognizeDistance(signDistance);
            }
        }

        //**********个性化设置************//
        String strCompanyName = requestSetting.getCompanyName();
        if (strCompanyName != null) {
            if (strCompanyName.length() <= Constants.MESSAGE_INPUT_STRING_MAX_LENGTH) {
                if (StringUtils.containChar(strCompanyName)) {
                    return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_COMPANY_NAME_INVALID, msgError);
                } else {
                    configInfo.setCompanyName(requestSetting.getCompanyName());
                }
            } else {
                return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_COMPANY_NAME_INVALID, msgError);
            }
        }

        String strDisplayMode = requestSetting.getDisplayMode();
        if (strDisplayMode != null) {
            try {
                int displayMode = Integer.parseInt(strDisplayMode);
                if (displayMode != ConfigConstants.DISPLAY_MODE_SUCCESS_NAME &&
                        displayMode != ConfigConstants.DISPLAY_MODE_HIDE_LAST_CHAR &&
                        displayMode != ConfigConstants.DISPLAY_MODE_SUCCESS_CUSTOM) {
                    return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_DISPLAY_MODE_INVALID, msgError);
                } else {
                    configInfo.setDisplayMode(displayMode);
                }
            } catch (Exception e) {
                return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_DISPLAY_MODE_INVALID, msgError);
            }
        }
        String stringDisplayCustom = requestSetting.getDisplayCustom();
        if (stringDisplayCustom != null) {
            if (stringDisplayCustom.length() <= Constants.MESSAGE_INPUT_STRING_MAX_LENGTH) {
                if (StringUtils.containChar(stringDisplayCustom)) {
                    return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_DISPLAY_MODE_INVALID, msgError);
                } else {
                    configInfo.setCustomDisplayModeFormat(stringDisplayCustom);
                }
            } else {
                return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_DISPLAY_MODE_INVALID, msgError);
            }
        }

        String stringVoiceMode = requestSetting.getVoiceMode();
        if (stringVoiceMode != null) {
            try {
                int voiceMode = Integer.parseInt(stringVoiceMode);
                if (voiceMode != ConfigConstants.SUCCESS_VOICE_MODE_NO_PLAY &&
                        voiceMode != ConfigConstants.SUCCESS_VOICE_MODE_NAME &&
                        voiceMode != ConfigConstants.SUCCESS_VOICE_MODE_PREVIEW_TYPE3 &&
                        voiceMode != ConfigConstants.SUCCESS_VOICE_MODE_PREVIEW_TYPE4 &&
                        voiceMode != ConfigConstants.SUCCESS_VOICE_MODE_PREVIEW_TYPE5 &&
                        voiceMode != ConfigConstants.SUCCESS_VOICE_MODE_PREVIEW_TYPE6 &&
                        voiceMode != ConfigConstants.SUCCESS_VOICE_MODE_CUSTOM) {
                    return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_VOICE_MODE_INVALID, msgError);
                } else {
                    configInfo.setVoiceMode(voiceMode);
                }
            } catch (Exception e) {
                return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_VOICE_MODE_INVALID, msgError);
            }
        }
        String stringVoiceCustom = requestSetting.getVoiceCustom();
        if (stringVoiceCustom != null) {
            if (stringVoiceCustom.length() <= Constants.MESSAGE_INPUT_STRING_MAX_LENGTH) {
                if (StringUtils.containChar(stringVoiceCustom)) {
                    return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_VOICE_MODE_INVALID, msgError);
                } else {
                    configInfo.setCustomVoiceModeFormat(stringVoiceCustom);
                }
            } else {
                return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_VOICE_MODE_INVALID, msgError);
            }
        }

        String stringStrangerMode = requestSetting.getStrangerMode();
        if (stringStrangerMode != null) {
            try {
                int strangerMode = Integer.parseInt(stringStrangerMode);
                if (strangerMode != ConfigConstants.DISPLAY_MODE_FAILED_DEFAULT_MARKUP &&
                        strangerMode != ConfigConstants.DISPLAY_MODE_FAILED_NOT_FEEDBACK &&
                        strangerMode != ConfigConstants.DISPLAY_MODE_FAILED_CUSTOM) {
                    return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_STRANGER_MODE_INVALID, msgError);
                } else {
                    configInfo.setDisplayModeFail(strangerMode);
                }
            } catch (Exception e) {
                return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_STRANGER_MODE_INVALID, msgError);
            }
        }
        String stringStrangerCustom = requestSetting.getStrangerCustom();
        if (stringStrangerCustom != null) {
            if (stringStrangerCustom.length() <= Constants.MESSAGE_INPUT_STRING_MAX_LENGTH) {
                if (StringUtils.containChar(stringStrangerCustom)) {
                    return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_STRANGER_MODE_INVALID, msgError);
                } else {
                    configInfo.setCustomFailDisplayModeFormat(stringStrangerCustom);
                }
            } else {
                return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_STRANGER_MODE_INVALID, msgError);
            }
        }

        String stringStrangerVoiceMode = requestSetting.getStrangerVoiceMode();
        if (stringStrangerVoiceMode != null) {
            try {
                int strangerVoiceMode = Integer.parseInt(stringStrangerVoiceMode);
                if (strangerVoiceMode != ConfigConstants.FAILED_VOICE_MODE_NO_PLAY &&
                        strangerVoiceMode != ConfigConstants.FAILED_VOICE_MODE_WARN &&
                        strangerVoiceMode != ConfigConstants.FAILED_VOICE_MODE_PREVIEW_TYPE3 &&
                        strangerVoiceMode != ConfigConstants.FAILED_VOICE_MODE_PREVIEW_TYPE4 &&
                        strangerVoiceMode != ConfigConstants.FAILED_VOICE_MODE_CUSTOM) {
                    return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_STRANGER_VOICE_MODE_INVALID, msgError);
                } else {
                    configInfo.setVoiceModeFail(strangerVoiceMode);
                }
            } catch (Exception e) {
                return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_STRANGER_VOICE_MODE_INVALID, msgError);
            }
        }
        String strangerVoiceCustom = requestSetting.getStrangerVoiceCustom();
        if (strangerVoiceCustom != null) {
            if (strangerVoiceCustom.length() <= Constants.MESSAGE_INPUT_STRING_MAX_LENGTH) {
                if (StringUtils.containChar(strangerVoiceCustom)) {
                    return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_STRANGER_VOICE_MODE_INVALID, msgError);
                } else {
                    configInfo.setCustomFailVoiceModeFormat(strangerVoiceCustom);
                }
            } else {
                return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_STRANGER_VOICE_MODE_INVALID, msgError);
            }
        }

        Integer faceQuality = requestSetting.getFaceQuality();
        if (faceQuality != null) {
            if (faceQuality != 0 && faceQuality != 1) {
                return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_FACE_QUALITY_INVALID, msgError);
            } else {
                configInfo.setFaceQuality(faceQuality == 1);
            }
        }
        String faceQualityThreshold = requestSetting.getFaceQualityThreshold();
        if (faceQualityThreshold != null) {
            try {
                double threshold = Double.parseDouble(faceQualityThreshold);
                if (CommonUtils.checkIsDoublePointTwo(faceQualityThreshold) > ServerConstants.THRESHOLD_POINT_MAX_LENGTH) {
                    return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_FACE_QUALITY_INVALID, msgError);
                }
                if (threshold > ConfigConstants.THRESHOLD_MAX || threshold < ConfigConstants.THRESHOLD_MIN) {
                    return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_FACE_QUALITY_INVALID, msgError);
                } else {
                    configInfo.setFaceQualityThreshold(faceQualityThreshold);
                }
            } catch (Exception e) {
                return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_FACE_QUALITY_INVALID, msgError);
            }
        }
        //***********云端模式和离线局域网模式差异化部分****************//
        if (!offline) {
            configLogo(requestSetting);
            try {
                Integer signType = requestSetting.getSignType();
                if (signType != null) {
                    if (signType != ConfigConstants.TYPE_SIGN_IN &&
                            signType != ConfigConstants.TYPE_SIGN_OUT &&
                            signType != ConfigConstants.TYPE_SIGN_BOTH) {
                        return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_SIGN_TYPE_INVALID, msgError);
                    } else {
                        configInfo.setSignType(signType);
                    }
                }
            } catch (Exception e) {
                return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_SIGN_TYPE_INVALID, msgError);
            }
        }

        boolean success = saveSettingConfigSync(configInfo);
        if (success) {
            saveSettings(configInfo, offline, requestSetting);
            if (needReUpdateEngineConfig && !needReOpenCamera) {
                EventBus.getDefault().post(new ReInitFaceEngineEvent(true));
                EventBus.getDefault().post(new ReOpenCameraEvent(false, false, irLiveOpen, showIrPreview));
            }
            if (needReUpdateEngineConfig && needReOpenCamera) {
                EventBus.getDefault().post(new ReOpenCameraEvent(true, true, irLiveOpen, showIrPreview));
            }
            if (!needReUpdateEngineConfig) {
                EventBus.getDefault().post(new ReOpenCameraEvent(false, false, irLiveOpen, showIrPreview));
            }
            EventBus.getDefault().post(new SettingConfigChangedEvent());
            return LocalHttpApiDataUtils.getResponseBaseSuccess(ServerConstants.MSG_RESPONSE_REQUEST_SUCCESS);
        } else {
            return LocalHttpApiDataUtils.getResponseBase(ServerConstants.RESPONSE_CODE_FAILED_BASE, ServerConstants.MSG_RESPONSE_FAILED);
        }
    }

    /**
     * 配置logo
     *
     * @param requestSetting
     */
    private void configLogo(RequestSetting requestSetting) {
        String mainUrl = requestSetting.getMainLogoUrl();
        if (null != mainUrl) {
            TableSettingConfigInfo configInfo = getSettingConfigInfo();
            if (Constants.CHAR_DOUBLE_QUOTATION_MARKS.equals(mainUrl)) {
                configInfo.setMainImagePath("");
                saveMainLogoId("");
                SPUtils.getInstance().put(Constants.SP_KEY_CLOUD_MAIN_LOGO_URL, "");
                saveSettingConfigAsync(configInfo, null);
                EventBus.getDefault().post(new ChangeMainLogoEvent(""));
            } else {
                File mainFilePath = new File(ConfigConstants.DEFAULT_MAIN_LOGO_FILE_PATH);
                if (!mainFilePath.exists()) {
                    downloadLogo(mainUrl, true);
                } else {
                    String localMainLogo = SPUtils.getInstance().getString(Constants.SP_KEY_CLOUD_MAIN_LOGO_URL);
                    if (!localMainLogo.equals(mainUrl)) {
                        downloadLogo(mainUrl, true);
                    }
                }
            }
        }
        String secondUrl = requestSetting.getSecondLogoUrl();
        if (null != secondUrl) {
            TableSettingConfigInfo configInfo = getSettingConfigInfo();
            if (Constants.CHAR_DOUBLE_QUOTATION_MARKS.equals(secondUrl)) {
                configInfo.setViceImagePath("");
                saveSecondLogoId("");
                SPUtils.getInstance().put(Constants.SP_KEY_CLOUD_SECOND_LOGO_URL, "");
                saveSettingConfigAsync(configInfo, null);
                EventBus.getDefault().post(new ChangeSecondLogoEvent(""));
            } else {
                File file = new File(ConfigConstants.DEFAULT_SECOND_LOGO_FILE_PATH);
                if (!file.exists()) {
                    downloadLogo(secondUrl, false);
                } else {
                    String localSecondLogo = SPUtils.getInstance().getString(Constants.SP_KEY_CLOUD_SECOND_LOGO_URL);
                    if (!localSecondLogo.equals(secondUrl)) {
                        downloadLogo(secondUrl, false);
                    }
                }
            }
        }
    }

    private void downloadLogo(String url, boolean mainLogo) {
        FileDownloadManager.getInstance().downloadFile(url, new FileDownloadManager.FileDownloadListener() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) {
                ResponseBody body = response.body();
                if (null != body) {
                    saveLogo(url, mainLogo, body);
                }
            }
        });
    }

    private void saveLogo(String url, boolean mainLogo, ResponseBody body) {
        Disposable disposable = Observable.create((ObservableOnSubscribe<String>) emitter -> {
            InputStream inputStream = body.byteStream();
            Bitmap decodeBmp = ImageFileUtils.getBitmap(inputStream);
            if (decodeBmp != null) {
                Bitmap resizeBmp;
                if (decodeBmp.getWidth() > LOGO_MAX_WIDTH || decodeBmp.getHeight() > LOGO_MAX_WIDTH) {
                    resizeBmp = ImageFileUtils.resizeImage(decodeBmp, LOGO_MAX_WIDTH, LOGO_MAX_WIDTH);
                } else {
                    resizeBmp = decodeBmp;
                }
                String localPath;
                String logoId;
                String netSuffix = url.substring(url.lastIndexOf(Constants.CHAR_POINT_MARKS) + 1);
                Bitmap.CompressFormat format = CommonUtils.getCompressFormat(netSuffix);
                TableSettingConfigInfo configInfo = CommonRepository.getInstance().getSettingConfigInfo();
                if (mainLogo) {
                    localPath = ConfigConstants.DEFAULT_MAIN_LOGO_FILE_PATH;
                    if (ImageFileUtils.save(resizeBmp, localPath, format)) {
                        logoId = CommonRepository.getInstance().createDatabaseId();
                        CommonRepository.getInstance().saveMainLogoId(logoId);
                        configInfo.setMainImagePath(localPath);
                        saveSettingConfigAsync(configInfo, null);
                        SPUtils.getInstance().put(Constants.SP_KEY_CLOUD_MAIN_LOGO_URL, url);
                    }
                } else {
                    localPath = ConfigConstants.DEFAULT_SECOND_LOGO_FILE_PATH;
                    if (ImageFileUtils.save(resizeBmp, localPath, format)) {
                        logoId = CommonRepository.getInstance().createDatabaseId();
                        CommonRepository.getInstance().saveSecondLogoId(logoId);
                        configInfo.setViceImagePath(localPath);
                        saveSettingConfigAsync(configInfo, null);
                        SPUtils.getInstance().put(Constants.SP_KEY_CLOUD_SECOND_LOGO_URL, url);
                    }
                }
                if (!resizeBmp.isRecycled()) {
                    resizeBmp.recycle();
                }
                if (!decodeBmp.isRecycled()) {
                    decodeBmp.recycle();
                }
                emitter.onNext(localPath);
            } else {
                emitter.onNext("");
            }
        }).compose(RxUtils.ioToMain())
                .subscribeWith(new BaseObserver<String>() {
                    @Override
                    public void onNext(String path) {
                        if (!TextUtils.isEmpty(path)) {
                            if (mainLogo) {
                                EventBus.getDefault().post(new ChangeMainLogoEvent(path));
                            } else {
                                EventBus.getDefault().post(new ChangeSecondLogoEvent(path));
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                    }
                });
    }

    @Override
    public void uploadArcLinkMainLogo() {
        String url = SPUtils.getInstance().getString(Constants.SP_KEY_CLOUD_MAIN_LOGO_URL);
        File mainFile = new File(ConfigConstants.DEFAULT_MAIN_LOGO_FILE_PATH);
        if (TextUtils.isEmpty(url) && mainFile.exists()) {
            uploadLogo(mainFile, true);
        }
    }

    @Override
    public void uploadArcLinkSecondLogo() {
        String url = SPUtils.getInstance().getString(Constants.SP_KEY_CLOUD_SECOND_LOGO_URL);
        File secondFile = new File(ConfigConstants.DEFAULT_SECOND_LOGO_FILE_PATH);
        if (TextUtils.isEmpty(url) && secondFile.exists()) {
            uploadLogo(secondFile, false);
        }
    }

    private void uploadLogo(File logoFile, boolean main) {
        Disposable disposable = Observable.create((ObservableOnSubscribe<UploadDataResponse>) emitter -> {
            UploadDataResponse response = ArcLinkEngine.getInstance().uploadData(logoFile);
            emitter.onNext(response);
            emitter.onComplete();
        }).compose(RxUtils.ioToMain())
                .subscribeWith(new BaseObserver<UploadDataResponse>() {
                    @Override
                    public void onNext(UploadDataResponse response) {
                        if (response != null) {
                            UploadDataResponse.DataBean dataBean = response.getData();
                            if (dataBean != null) {
                                String logoUrl = dataBean.getHttpUrl();
                                if (main) {
                                    SPUtils.getInstance().put(Constants.SP_KEY_CLOUD_MAIN_LOGO_URL, logoUrl);
                                } else {
                                    SPUtils.getInstance().put(Constants.SP_KEY_CLOUD_SECOND_LOGO_URL, logoUrl);
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(ExceptionHandler.ResponseThrowable throwable) {
                    }
                });
    }

    @Override
    public String createDatabaseId() {
        String guid = UUIDUtils.getUUID32();
        return Md5Utils.encode(guid).toLowerCase();
    }

    /**
     * 向外发送广播
     *
     * @param action
     */
    public void sendBroadcast(String action) {
        Intent intent = new Intent(action);
        String pkgName = SPUtils.getInstance().getString(Constants.SP_KEY_BROADCAST_PACKAGE_NAME);
        if (!TextUtils.isEmpty(pkgName)) {
            intent.setPackage(pkgName);
        }
        Utils.getApp().sendBroadcast(intent, Constants.PERMISSION_DEVICE_REMOTE_SERVICE);
    }

    /**
     * 发送带有Bundle的广播
     *
     * @param action
     * @param bundle
     */
    public void sendBroadcastWithBundle(String action, Bundle bundle) {
        Intent intent = new Intent(action);
        String pkgName = SPUtils.getInstance().getString(Constants.SP_KEY_BROADCAST_PACKAGE_NAME);
        if (!TextUtils.isEmpty(pkgName)) {
            intent.setPackage(pkgName);
        }
        intent.putExtras(bundle);
        Utils.getApp().sendBroadcast(intent, Constants.PERMISSION_DEVICE_REMOTE_SERVICE);
    }

    /**
     * 向外发送广播
     */
    public void sendExitAppBroadcast() {
        Intent intent = new Intent("com.arcsoft.arcfacesingle.ACTION_HAND_EXIT_APP");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.setPackage("com.arcsoft.arcfacesingle.alive");
        }
        Utils.getApp().sendBroadcast(intent, Constants.PERMISSION_DEVICE_REMOTE_SERVICE);
    }

    public Config.DetectPriority getFaceDetectDegree(String stringDegree) {
        Config.DetectPriority detectPriority;
        switch (stringDegree) {
            case Constants.DEGREE_90_STRING:
                detectPriority = Config.DetectPriority.DP_90_ONLY;
                break;
            case Constants.DEGREE_180_STRING:
                detectPriority = Config.DetectPriority.DP_180_ONLY;
                break;
            case Constants.DEGREE_270_STRING:
                detectPriority = Config.DetectPriority.DP_270_ONLY;
                break;
            case Constants.DEGREE_ALL_STRING:
                detectPriority = Config.DetectPriority.DP_ALL;
                break;
            default:
                detectPriority = Config.DetectPriority.DP_0_ONLY;
                break;
        }
        return detectPriority;
    }

    public Config.DetectPriority getFaceDetectDegree(int detectDegree) {
        Config.DetectPriority detectPriority;
        switch (detectDegree) {
            case Constants.DEGREE_90:
                detectPriority = Config.DetectPriority.DP_90_ONLY;
                break;
            case Constants.DEGREE_180:
                detectPriority = Config.DetectPriority.DP_180_ONLY;
                break;
            case Constants.DEGREE_270:
                detectPriority = Config.DetectPriority.DP_270_ONLY;
                break;
            case Constants.DEGREE_ALL:
                detectPriority = Config.DetectPriority.DP_ALL;
                break;
            default:
                detectPriority = Config.DetectPriority.DP_0_ONLY;
                break;
        }
        return detectPriority;
    }

    public int getFaceDetectDegreeInteger(String strFaceDetectDegree) {
        int detectFaceDegree;
        switch (strFaceDetectDegree) {
            case Constants.DEGREE_90_STRING:
                detectFaceDegree = Constants.DEGREE_90;
                break;
            case Constants.DEGREE_180_STRING:
                detectFaceDegree = Constants.DEGREE_180;
                break;
            case Constants.DEGREE_270_STRING:
                detectFaceDegree = Constants.DEGREE_270;
                break;
            default:
                detectFaceDegree = Constants.DEGREE_0;
                break;
        }
        return detectFaceDegree;
    }

    public Config.DetectDistance getFaceEngineDistance(int distance) {
        Config.DetectDistance detectDistance;
        switch (distance) {
            case ConfigConstants.RECOGNITION_DISTANCE_TYPE1:
                detectDistance = Config.DetectDistance.CLOSE;
                break;
            case ConfigConstants.RECOGNITION_DISTANCE_TYPE2:
                detectDistance = Config.DetectDistance.NEAR;
                break;
            case ConfigConstants.RECOGNITION_DISTANCE_TYPE3:
                detectDistance = Config.DetectDistance.REGULAR;
                break;
            case ConfigConstants.RECOGNITION_DISTANCE_TYPE4:
                detectDistance = Config.DetectDistance.FAR;
                break;
            default:
                detectDistance = Config.DetectDistance.DISTANT;
                break;
        }
        return detectDistance;
    }

    public Config.DetectLivenessMode getFaceEngineLiveMode(int mode) {
        Config.DetectLivenessMode liveMode;
        switch (mode) {
            case ConfigConstants.DEFAULT_LIVE_DETECT_RGB:
                liveMode = Config.DetectLivenessMode.RGB;
                break;
            case ConfigConstants.DEFAULT_LIVE_DETECT_IR:
                liveMode = Config.DetectLivenessMode.IR;
                break;
            default:
                liveMode = Config.DetectLivenessMode.CLOSE;
                break;
        }
        return liveMode;
    }

    public DeviceAdaptationInfo getAdaptationInfo() {
        String strAdaptation = SPUtils.getInstance().getString(Constants.SP_KEY_ADAPTATION_INFO);
        Gson gson = new Gson();
        if (TextUtils.isEmpty(strAdaptation)) {
            return createNewAdaptationInfo(gson);
        } else {
            try {
                return gson.fromJson(strAdaptation, DeviceAdaptationInfo.class);
            } catch (Exception e) {
                e.printStackTrace();
                return createNewAdaptationInfo(gson);
            }
        }
    }

    private DeviceAdaptationInfo createNewAdaptationInfo(Gson gson) {
        DeviceCameraInfo cameraInfo = CommonUtils.getDeviceCameraInfo();
        DeviceAdaptationInfo adaptationInfo = new DeviceAdaptationInfo();
        adaptationInfo.setPreviewHeight(cameraInfo.getDefPreviewH());
        adaptationInfo.setPreviewWidth(cameraInfo.getDefPreviewW());
        adaptationInfo.setCameraCount(cameraInfo.getCameraPosList().size());
        adaptationInfo.setMainCameraId(Camera.CameraInfo.CAMERA_FACING_BACK);
        adaptationInfo.setSecondCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT);
        adaptationInfo.setCameraSizeList(cameraInfo.getCameraSizeList());
        adaptationInfo.setCameraPositionList(cameraInfo.getCameraPosList());
        List<String> fdDegreeArray = AdaptationInfoDataManager.getInstance().getFaceDetectDegreeArray();
        if (fdDegreeArray.size() > 0) {
            String faceDetectDegree = fdDegreeArray.get(0);
            adaptationInfo.setFaceDetectDegree(faceDetectDegree);
        }
        adaptationInfo.setMainCameraRotation(0);
        adaptationInfo.setMainCameraChangeWidthHeight(false);
        adaptationInfo.setMainCameraMirror(false);
        adaptationInfo.setRectVerticalMirror(false);
        adaptationInfo.setRectHorizontalMirror(false);
        adaptationInfo.setLeftGlSurfaceViewRotation(0);
        adaptationInfo.setLeftGlSurfaceViewMirror(false);
        adaptationInfo.setRightGlSurfaceViewRotation(0);
        adaptationInfo.setRightGlSurfaceViewMirror(false);
        adaptationInfo.setHorizontalDisplacement(0);
        adaptationInfo.setVerticalDisplacement(0);
        SPUtils.getInstance().put(Constants.SP_KEY_ADAPTATION_INFO, gson.toJson(adaptationInfo));
        return adaptationInfo;
    }

    public String getMainLogoId() {
        return SPUtils.getInstance().getString(Constants.SP_KEY_MAIN_LOGO_ID);
    }

    public void saveMainLogoId(String id) {
        SPUtils.getInstance().put(Constants.SP_KEY_MAIN_LOGO_ID, id);
    }

    public String getSecondLogoId() {
        return SPUtils.getInstance().getString(Constants.SP_KEY_SECOND_LOGO_ID);
    }

    public void saveSecondLogoId(String id) {
        SPUtils.getInstance().put(Constants.SP_KEY_SECOND_LOGO_ID, id);
    }

    public void initArcLinkEngine(ISelectMode.ArcLinkInitCallback callback) {
        boolean accessSuccess = getDeviceAccessStatus();
        if (accessSuccess) {
            if (initArcLinkDisposable != null && !initArcLinkDisposable.isDisposed()) {
                initArcLinkDisposable.dispose();
                initArcLinkDisposable = null;
            }
            initArcLinkDisposable = Observable.create((ObservableEmitter<InitResult> emitter) -> {
                DeviceAccessInfo accessInfo = getDeviceAccessInfo();
                ArcLinkEngine.getInstance().unInit();
                ArcLinkEngine.getInstance().setUrl(accessInfo.getServerIp());
                UpgradeResult upgradeResult = ApkDownloadManager.getInstance().getUpgradeResult();
                InitResult result = ArcLinkEngine.getInstance().init(Utils.getApp(),
                        ArcFaceVersionEnum.V_2_2,
                        accessInfo.getAccessId(),
                        accessInfo.getDeviceTag(),
                        upgradeResult);
                if (result.getCode() == ArcLinkErrorCodeEnum.SUCCESS.getCode() && upgradeResult != null) {
                    SPUtils.getInstance().put(Constants.SP_KEY_OLD_VERSION_CODE, ApkDownloadManager.VALUE_APK_UPGRADE_RESET);
                }
                emitter.onNext(result);
                emitter.onComplete();
            }).compose(RxUtils.ioToMain())
                    .subscribeWith(new DisposableObserver<InitResult>() {
                        @Override
                        public void onNext(InitResult result) {
                            int code = result.getCode();
                            if (code == ArcLinkErrorCodeEnum.SUCCESS.getCode()) {
                                setDeviceAccessStatus(true);
                                if (callback != null) {
                                    callback.initSuccess();
                                }
                            } else {
                                if (callback != null) {
                                    callback.initFail("ArcLink连接失败：" + code);
                                }
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (callback != null) {
                                callback.initFail(e.getMessage());
                            }
                        }

                        @Override
                        public void onComplete() {
                        }
                    });
        } else {
        }
    }

    public void unInitArcLinkEngine() {
        Disposable disposable = Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            ArcLinkEngine.getInstance().unInit();
            emitter.onNext(true);
            emitter.onComplete();
        }).compose(RxUtils.ioToMain())
                .subscribeWith(new BaseObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                    }
                });
    }

    public boolean getDeviceAccessStatus() {
        return SPUtils.getInstance().getBoolean(Constants.SP_KEY_DEVICE_ACCESS_SUCCESS, false);
    }

    public void setDeviceAccessStatus(boolean success) {
        SPUtils.getInstance().put(Constants.SP_KEY_DEVICE_ACCESS_SUCCESS, success);
    }

    public DeviceAccessInfo getDeviceAccessInfo() {
        DeviceAccessInfo deviceAccessInfo = new DeviceAccessInfo();
        String accessId = SPUtils.getInstance().getString(Constants.SP_KEY_DEVICE_ACCESS_ID);
        deviceAccessInfo.setAccessId(accessId);
        String deviceTag = SPUtils.getInstance().getString(Constants.SP_KEY_DEVICE_TAG);
        deviceAccessInfo.setDeviceTag(deviceTag);
        String serverIp = SPUtils.getInstance().getString(Constants.SP_KEY_ARC_LINK_SERVER_ADDRESS,
                Constants.ARC_LINK_SERVER_ADDRESS);
        deviceAccessInfo.setServerIp(serverIp);
        return deviceAccessInfo;
    }

    @Override
    public void exitApp() {
        CommonRepository.getInstance().sendBroadcast(Constants.ACTION_STOP_IDENTIFY);
        if (CommonUtils.isOfflineLanAppMode()) {
            if (CommonUtils.isServiceRunning(Utils.getApp(), OfflineLanService.class.getName())) {
                OfflineLanService.stop();
            }
        }
        if (CommonUtils.isCloudAiotAppMode()) {
            if (CommonUtils.isServiceRunning(Utils.getApp(), CloudAIotService.class.getName())) {
                CloudAIotService.stop();
            }
        }
        NetworkUtils.unRegisterNetwork();
        AppUtils.exitApp();
    }

    private void saveSettings(TableSettingConfigInfo configInfo, boolean offline, RequestSetting requestSetting) {
        ConfigurationInfo configurationInfo = new ConfigurationInfo();
        configurationInfo.loadFromDatabase(configInfo);
        if (!offline) {
            String mainLogoUrl = requestSetting.getMainLogoUrl();
            if (mainLogoUrl != null) {
                if (mainLogoUrl.length() > 0) {
                    configurationInfo.setMainLogoPath(ConfigConstants.DEFAULT_MAIN_LOGO_FILE_PATH);
                } else {
                    configurationInfo.setMainLogoPath("");
                }
            }

            String subLogoUrl = requestSetting.getSecondLogoUrl();
            if (subLogoUrl != null) {
                if (subLogoUrl.length() > 0) {
                    configurationInfo.setSubLogoPath(ConfigConstants.DEFAULT_SECOND_LOGO_FILE_PATH);
                } else {
                    configurationInfo.setSubLogoPath("");
                }
            }
        }
        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
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

    /**
     * 是否允许上传刷脸记录图片
     *
     * @return true 上传图片；false 只上传文字，不上传图片
     */
    public boolean uploadRecordImage(TableSettingConfigInfo configInfo) {
        return configInfo != null && configInfo.getUploadRecordImage() ==
                ConfigConstants.DEFAULT_UPLOAD_RECORD_IMAGE;
    }

    /**
     * ArcLink断线，解绑设备
     */
    public void arcLinkDisconnected() {
        unInitArcLinkEngine();
        setDeviceAccessStatus(false);
        SPUtils.getInstance().put(Constants.SP_KEY_DEVICE_ACCESS_ID, "");
        SPUtils.getInstance().put(Constants.SP_KEY_DEVICE_TAG, "");
    }

    public Config getDefaultFaceEngineConfig() {
        return getFaceEngineConfig(false, ConfigConstants.RECOGNITION_DISTANCE_TYPE1,
                Constants.DEGREE_ALL_STRING, ConfigConstants.DEFAULT_LIVE_DETECT_CLOSE);
    }

    public Config getFaceEngineConfig(String fdDegree) {
        return getFaceEngineConfig(false, ConfigConstants.RECOGNITION_DISTANCE_TYPE1, fdDegree,
                ConfigConstants.DEFAULT_LIVE_DETECT_CLOSE);
    }

    public Config getFaceEngineConfig(boolean videoMode, int distance, String fdDegree, int liveMode) {
        Config config = new Config();
        config.detectMode = videoMode ? Config.DetectMode.VIDEO : Config.DetectMode.IMAGE;
        if (videoMode) {
            config.distance = getFaceEngineDistance(distance);
        }
        config.detectPriority = getFaceDetectDegree(fdDegree);
        config.liveMode = getFaceEngineLiveMode(liveMode);
        config.outputLog = Constants.SWITCH_PRINT_LOG;
        return config;
    }

    public Config getFaceEngineConfig(boolean videoMode, int distance, String fdDegree, int liveMode,
                                      float fcThreshold, float fqThreshold, float irThreshold, boolean mirror,
                                      int rotation, int offsetX, int offsetY, boolean successRetry, float successRetryTime,
                                      float failedRetryTime) {
        Config config = new Config();
        config.detectMode = videoMode ? Config.DetectMode.VIDEO : Config.DetectMode.IMAGE;
        if (videoMode) {
            config.distance = getFaceEngineDistance(distance);
        }
        config.detectPriority = getFaceDetectDegree(fdDegree);
        config.liveMode = getFaceEngineLiveMode(liveMode);
        config.fcThreshold = fcThreshold;
        config.fqThreshold = fqThreshold;
        config.irThreshold = irThreshold;
        config.mirror = mirror;
        config.rotation = rotation;
        config.offsetX = offsetX;
        config.offsetY = offsetY;
        config.successRetry = successRetry;
        config.successInterval = successRetryTime;
        config.failInterval = failedRetryTime;
        config.outputLog = Constants.SWITCH_PRINT_LOG;
        return config;
    }

    public TablePerson createTablePerson(String picName) {
        TablePerson tablePerson = new TablePerson();
        String fileName = picName.substring(0, picName.lastIndexOf("."));
        if (fileName.contains("_")) {
            String[] fileArr = fileName.split("_");
            tablePerson.personName = fileArr[0].replaceAll("'", "");
            tablePerson.personInfoNo = fileArr[1].replaceAll("'", "");
        } else {
            tablePerson.personName = fileName.replaceAll("'", "");
        }
        tablePerson.personSerial = CommonUtils.createPersonSerial();
        tablePerson.addTime = System.currentTimeMillis();
        tablePerson.updateTime = tablePerson.addTime;
        tablePerson.authMorningStartTime = ConfigConstants.DOOR_AUTHORITY_DEFAULT_START_TIME;
        tablePerson.authMorningEndTime = ConfigConstants.DOOR_AUTHORITY_DEFAULT_END_TIME;
        tablePerson.authNoonStartTime = tablePerson.authMorningStartTime;
        tablePerson.authNoonEndTime = tablePerson.authMorningEndTime;
        tablePerson.authNightStartTime = tablePerson.authMorningStartTime;
        tablePerson.authNightEndTime = tablePerson.authMorningEndTime;
        tablePerson.doorAuthorityDetail = CommonUtils.getStrFromRes(R.string.face_manager_allow_pass);
        tablePerson.personInfoType = PersonDao.TYPE_PERSON_INFO_ONLY_FACE;
        tablePerson.icCardNo = "";
        return tablePerson;
    }

    public TablePersonFace createTablePersonFace(@NonNull TablePerson tablePerson, @NonNull byte[] feature) {
        TablePersonFace personFace = new TablePersonFace();
        personFace.faceInfo = tablePerson.personName;
        personFace.personSerial = tablePerson.personSerial;
        personFace.addTime = tablePerson.addTime;
        personFace.updateTime = personFace.addTime;
        String imageName = personFace.personSerial;
        personFace.imagePath = CommonUtils.getPersonFaceLocalPath(imageName);
        personFace.feature = feature;
        personFace.featureVersion = Constants.FACE_FEATURE_VERSION_V30;
        return personFace;
    }

    public void initService() {
        if (CommonUtils.isCloudAiotAppMode()) {
            if (CommonUtils.isServiceRunning(Utils.getApp(), OfflineLanService.class.getName())) {
                LocalHttpHelper.getInstance().stopServer();
                OfflineLanService.stop();
            }
            if (!CommonUtils.isServiceRunning(Utils.getApp(), CloudAIotService.class.getName())) {
                CloudAIotService.start();
            }
        }
        if (CommonUtils.isOfflineLanAppMode()) {
            if (CommonUtils.isServiceRunning(Utils.getApp(), CloudAIotService.class.getName())) {
                CloudAIotService.stop();
            }
            if (!CommonUtils.isServiceRunning(Utils.getApp(), OfflineLanService.class.getName())) {
                LocalHttpHelper.getInstance().setLocalHttpCallback(LocalHttpImpl.getInstance());
                LocalHttpHelper.getInstance().startServer(LocalHttpApiDataUtils.getLocalApiPort());
                OfflineLanService.start();
            }
        }
    }

}
