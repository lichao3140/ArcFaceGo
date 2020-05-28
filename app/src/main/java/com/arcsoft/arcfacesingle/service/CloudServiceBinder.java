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

package com.arcsoft.arcfacesingle.service;

import android.os.Binder;
import android.os.Build;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.business.arclink.ArcLinkPersonManager;
import com.arcsoft.arcfacesingle.business.common.CommonRepository;
import com.arcsoft.arcfacesingle.business.common.FaceRecordDataManager;
import com.arcsoft.arcfacesingle.business.selectmode.ISelectMode;
import com.arcsoft.arcfacesingle.data.db.dao.IdentifyRecordDao;
import com.arcsoft.arcfacesingle.data.db.table.TableSettingConfigInfo;
import com.arcsoft.arcfacesingle.data.db.table.TableSignRecord;
import com.arcsoft.arcfacesingle.data.event.CleanDataEvent;
import com.arcsoft.arcfacesingle.data.event.DeviceStorageCheckEvent;
import com.arcsoft.arcfacesingle.data.event.InstallPackageEvent;
import com.arcsoft.arcfacesingle.data.event.arclink.ArcLinkAuthEvent;
import com.arcsoft.arcfacesingle.data.event.arclink.ArcLinkDisconnectedEvent;
import com.arcsoft.arcfacesingle.server.api.ServerConstants;
import com.arcsoft.arcfacesingle.server.pojo.base.ResponseBase;
import com.arcsoft.arcfacesingle.server.pojo.request.RequestSetting;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.arcfacesingle.util.download.ApkDownloadManager;
import com.arcsoft.arcsoftlink.contants.MqttConnectCode;
import com.arcsoft.arcsoftlink.enums.MqttResponseCodeEnum;
import com.arcsoft.arcsoftlink.enums.MqttResponseStateEnum;
import com.arcsoft.arcsoftlink.mqtt.ArcLinkEngine;
import com.arcsoft.arcsoftlink.mqtt.EventCallback;
import com.arcsoft.arcsoftlink.mqtt.EventType;
import com.arcsoft.arcsoftlink.mqtt.api.ConnectStatusChangedCallback;
import com.arcsoft.arcsoftlink.mqtt.api.IMessageListener;
import com.arcsoft.arcsoftlink.mqtt.bean.UpgradeInfo;
import com.arcsoft.asg.libcommon.util.business.rx.RxUtils;
import com.arcsoft.asg.libcommon.util.common.AppUtils;
import com.arcsoft.asg.libcommon.util.common.DeviceUtils;
import com.arcsoft.asg.libcommon.util.common.FileUtils;
import com.arcsoft.asg.libcommon.util.common.Md5Utils;
import com.arcsoft.asg.libcommon.util.common.NetworkUtils;
import com.arcsoft.asg.libcommon.util.common.PermissionUtils;
import com.arcsoft.asg.libcommon.util.common.SPUtils;
import com.arcsoft.asg.libcommon.util.common.TimeUtils;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

public class CloudServiceBinder extends Binder {
    private static final String TAG = CloudServiceBinder.class.getSimpleName();

    private Disposable rebootDisposable;
    private Disposable timerTaskDisposable;
    private boolean initialized;

    public void start() {
        startTask();
    }

    public void stop() {
        stopTask();
    }

    public void startSyncData() {
        syncArcLinkServerData();
    }

    public void startReboot() {
        startRebootTask();
    }

    public void stopReboot() {
        clearRebootDisposable();
    }

    public void connectArcLink() {
        if (!initialized) {
            initArcLink();
        }
    }

    private void startTask() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NetworkUtils.unRegisterNetwork();
            NetworkUtils.registerNetwork(state -> {
                if (state && !initialized) {
                    initArcLink();
                }
            });
        } else {
            initArcLink();
        }
        clearTimerDisposable();
        timerTaskDisposable = Observable.interval(Constants.INIT_SEND_HEART, Constants.DELAY_SEND_HEART, TimeUnit.MILLISECONDS)
                .compose(RxUtils.computingToMain())
                .subscribe(aLong -> {
                    //检测设备剩余存储空间
                    long sdcardSize = FileUtils.getSdcardAvailableSize();
                    if (sdcardSize <= Constants.SDCARD_STORAGE_SIZE_SAFE) {
                        EventBus.getDefault().post(new DeviceStorageCheckEvent(true));
                    }
                    if (sdcardSize <= Constants.SDCARD_STORAGE_SIZE_DELETE) {
                        deleteSignRecordThread();
                    } else {
                        FaceRecordDataManager.getInstance().startUploadTask();
                    }
                });
        //开始定时重启任务
        clearRebootDisposable();
        startRebootTask();
    }

    private void stopTask() {
        FaceRecordDataManager.getInstance().stopUploadTask();
        CommonRepository.getInstance().unInitArcLinkEngine();
        clearTimerDisposable();
        clearRebootDisposable();
    }

    private void initArcLink() {
        initArcLinkEngine();
        ArcLinkEngine.setConnectStatusCallback(new ConnectStatusChangedCallback() {
            @Override
            public void onReconnected() {
                syncArcLinkServerData();
            }

            @Override
            public void onForceDisconnected(String s, String s1) {
                EventBus.getDefault().post(new ArcLinkDisconnectedEvent(true));
            }

            @Override
            public void onReconnectFailed(int i) {
                if (i == MqttConnectCode.BAD_USERNAME_OR_PASSWORD) {
                    EventBus.getDefault().post(new ArcLinkDisconnectedEvent(true));
                } else if (i == MqttConnectCode.NOT_AUTHORIZED) {
                    EventBus.getDefault().post(new ArcLinkAuthEvent(true));
                }
            }
        });
    }

    private void initArcLinkEngine() {
        CommonRepository.getInstance().initArcLinkEngine(new ISelectMode.ArcLinkInitCallback() {
            @Override
            public void initSuccess() {
                initialized = true;
                syncArcLinkServerData();
            }

            @Override
            public void initFail(String msg) {
                initialized = false;
            }
        });
    }

    private void syncArcLinkServerData() {
        syncPersonFaceInfo();
        syncDeviceInfo();
    }

    /**
     * 同步人员信息
     */
    private void syncPersonFaceInfo() {
        ArcLinkEngine.getInstance().subscribe(EventType.personSetSync, new IMessageListener() {
            @Override
            public void onMessageArrived(EventCallback eventCallback, Object o) {
                ArcLinkPersonManager.getInstance().syncPersonInfoFromArcLink(eventCallback);
            }
        }, null);
    }

    /**
     * 同步设备信息
     */
    private void syncDeviceInfo() {
        ArcLinkEngine.getInstance().subscribe(EventType.doorOpen, (eventCallback, o) -> {
            CommonRepository.getInstance().openDoor();
            randomExecuteCallBack(eventCallback, true, "", MqttResponseStateEnum.STATE_SUCCESS);
        }, null);

        ArcLinkEngine.getInstance().subscribe(EventType.reboot, (eventCallback, o) -> {
            boolean success = true;
            if (!DeviceUtils.isRooted()) {
                success = false;
            }
            String msg = success ? "重启指令发送成功，请确认" : CommonUtils.getStrFromRes(R.string.device_reboot_failed_please_check);
            randomExecuteCallBack(eventCallback, success, msg, MqttResponseStateEnum.STATE_SUCCESS);
            CommonRepository.getInstance().rebootDelay(Constants.DEVICE_REBOOT_DELAY);
        }, null);

        ArcLinkEngine.getInstance().subscribe(EventType.reset, (eventCallback, o) -> {
            if (CommonUtils.isCloudAiotAppMode()) {
                CloudAIotService.stopReboot();
            }
            EventBus.getDefault().post(new CleanDataEvent(true));
            randomExecuteCallBack(eventCallback, true, "", MqttResponseStateEnum.STATE_SUCCESS);
        }, null);

        ArcLinkEngine.getInstance().subscribe(EventType.getConfig, (eventCallback, o) -> {
            RequestSetting requestSetting = CommonUtils.getRequestSetting();
            String configInfo = new Gson().toJson(requestSetting);
            eventCallback.setReturnData(configInfo);
            randomExecuteCallBack(eventCallback, true, "", MqttResponseStateEnum.STATE_SUCCESS);
        }, null);

        ArcLinkEngine.getInstance().subscribe(EventType.updateConfig, (IMessageListener<String>) (eventCallback, config) -> {
            try {
                RequestSetting requestSetting = new Gson().fromJson(config, RequestSetting.class);
                if (requestSetting != null) {
                    ResponseBase responseBase = CommonRepository.getInstance().checkDeviceParams(requestSetting, false);
                    randomExecuteCallBack(eventCallback, responseBase.getCode() == ServerConstants.RESPONSE_CODE_SUCCESS,
                            responseBase.getMsg(), MqttResponseStateEnum.STATE_SUCCESS);
                } else {
                    randomExecuteCallBack(eventCallback, false, "设备信息更新失败",
                            MqttResponseStateEnum.STATE_SUCCESS);
                }
            } catch (Exception e) {
                e.printStackTrace();
                randomExecuteCallBack(eventCallback, false, "设备信息更新失败",
                        MqttResponseStateEnum.STATE_SUCCESS);
            }
        }, null);

        IMessageListener<UpgradeInfo> upgradeInfoListener = (eventCallback, data) -> {
            String json = new Gson().toJson(data);
            if (data == null) {
                eventCallback.setReturnData(json);
                randomExecuteCallBack(eventCallback, MqttResponseStateEnum.STATE_FAILED,
                        ServerConstants.MSG_REQUEST_INVALID_AND_RETRY);
                return;
            }

            if (ApkDownloadManager.getInstance().isDownloadApk()) {
                eventCallback.setReturnData(json);
                randomExecuteCallBack(eventCallback, MqttResponseStateEnum.STATE_FAILED,
                        ServerConstants.MSG_PACKAGE_IN_PROCESS);
                return;
            }

            if (FileUtils.getSdcardAvailableSize() <= Constants.SDCARD_STORAGE_SIZE_DELETE) {
                eventCallback.setReturnData(json);
                randomExecuteCallBack(eventCallback, MqttResponseStateEnum.STATE_FAILED,
                        ServerConstants.MSG_RESPONSE_DEVICE_SDCARD_STORAGE_LESS_MIN);
                return;
            }

            if (!ApkDownloadManager.getInstance().checkPkgName(data.getPackageName())) {
                eventCallback.setReturnData(json);
                randomExecuteCallBack(eventCallback, MqttResponseStateEnum.STATE_FAILED,
                        ServerConstants.MSG_PACKAGE_NAME_INVALID);
                return;
            }

            int versionCode = AppUtils.getAppVersionCode();
            if (data.getVersionCode() == versionCode) {
                eventCallback.setReturnData(json);
                randomExecuteCallBack(eventCallback, MqttResponseStateEnum.STATE_FAILED,
                        ServerConstants.MSG_IS_CURRENTLY_THE_LATEST_VERSION);
                return;
            }
            if (data.getVersionCode() < versionCode) {
                eventCallback.setReturnData(json);
                randomExecuteCallBack(eventCallback, MqttResponseStateEnum.STATE_FAILED,
                        ServerConstants.MSG_PACKAGE_VERSION_CODE_INVALID);
                return;
            }

            String fileName = Md5Utils.encode(data.getVersionCode() + data.getPackageName()) + ".apk";
            String filePath = ApkDownloadManager.getInstance().getFilePath(fileName);
            if (FileUtils.isFileExists(filePath)) {
                if (!ApkDownloadManager.getInstance().packageTransfer(filePath)) {
                    //apk校验失败，删除本地apk包，重新下载
                    FileUtils.delete(filePath);
                    downloadApk(filePath, data, eventCallback, json);
                } else {
                    postInstallEvent(filePath, data);
                }
            } else {
                downloadApk(filePath, data, eventCallback, json);
            }
        };
        ArcLinkEngine.getInstance().subscribe(EventType.upgrade, upgradeInfoListener, UpgradeInfo.class);
    }

    private void downloadApk(String filePath, UpgradeInfo upgradeInfo, EventCallback eventCallback, String json) {
        FileUtils.createFileByDeleteOldFile(new File(filePath));
        ApkDownloadManager.getInstance().downloadApk(filePath, upgradeInfo.getFileUrl(),
                new ApkDownloadManager.DownloadListener() {
                    @Override
                    public void complete(String path) {
                        postInstallEvent(path, upgradeInfo);
                    }

                    @Override
                    public void loadFail(String message) {
                        SPUtils.getInstance().put(Constants.SP_KEY_UPGRADE_ID, upgradeInfo.getUpgradeId());
                        SPUtils.getInstance().put(Constants.SP_KEY_OLD_VERSION_CODE, ApkDownloadManager.VALUE_APK_UPGRADE_FAIL);
                        eventCallback.setReturnData(json);
                        randomExecuteCallBack(eventCallback, MqttResponseStateEnum.STATE_FAILED,
                                ServerConstants.MSG_FILE_DOWNLOAD_FAILED);
                    }
                });
    }

    private void postInstallEvent(String filePath, UpgradeInfo upgradeInfo) {
        SPUtils.getInstance().put(Constants.SP_KEY_LOCAL_APK_CRC, filePath);
        SPUtils.getInstance().put(Constants.SP_KEY_OLD_VERSION_CODE, AppUtils.getAppVersionCode());
        SPUtils.getInstance().put(Constants.SP_KEY_UPGRADE_ID, upgradeInfo.getUpgradeId());
        if (!PermissionUtils.isGranted(PermissionUtils.PERMISSION_INSTALL_PACKAGE)) {
            EventBus.getDefault().post(new InstallPackageEvent(filePath, InstallPackageEvent.INSTALL_TYPE_NO_SILENCE));
        } else {
            EventBus.getDefault().post(new InstallPackageEvent(filePath, InstallPackageEvent.INSTALL_TYPE_SILENCE));
        }
    }

    private void randomExecuteCallBack(EventCallback eventCallback, boolean success, String s, MqttResponseStateEnum state) {
        eventCallback.onEventFinished(
                success ? MqttResponseCodeEnum.CODE_SUCCESS : MqttResponseCodeEnum.CODE_UNKNOWN_ERROR,
                state,
                success ? "success" : s);
    }

    private void randomExecuteCallBack(EventCallback eventCallback, MqttResponseStateEnum state, String s) {
        eventCallback.onEventFinished(
                MqttResponseCodeEnum.CODE_SUCCESS,
                state,
                state == MqttResponseStateEnum.STATE_SUCCESS ? "success" : s);
    }

    private void startRebootTask() {
        TableSettingConfigInfo settingConfigInfo = CommonRepository.getInstance().getSettingConfigInfo();
        boolean rebootDevice = settingConfigInfo.isRebootEveryDay();
        if (rebootDevice) {
            startRebootEveryDayTask(settingConfigInfo);
        }
    }

    /**
     * 开启每日重启设备任务
     */
    private void startRebootEveryDayTask(TableSettingConfigInfo settingConfigInfo) {
        try {
            clearRebootDisposable();
            String strHour = settingConfigInfo.getRebootHour();
            String strMin = settingConfigInfo.getRebootMin();
            int hour = Integer.parseInt(strHour);
            int min = Integer.parseInt(strMin);
            long current = System.currentTimeMillis();
            long zero = TimeUtils.getStartTimeOfDay();
            long taskTime = hour * 1000 * 3600 + min * 1000 * 60;
            //今日任务执行时间
            long todayTime = zero + taskTime;
            long sub = todayTime - current;
            if (sub > 0) {
                //今日任务未开始
                rebootDisposable = Observable.timer(sub, TimeUnit.MILLISECONDS)
                        .compose(RxUtils.computingToMain())
                        .subscribe(aLong -> CommonRepository.getInstance().reboot());
            } else {
                //今日任务已结束，继续循环至明天任务开始
                long endTime = TimeUtils.getEndTimeOfDay();
                long subTime = endTime - current;
                rebootDisposable = Observable.timer(subTime, TimeUnit.MILLISECONDS)
                        .subscribe(aLong -> startRebootEveryDayTask(settingConfigInfo));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearRebootDisposable() {
        if (rebootDisposable != null && !rebootDisposable.isDisposed()) {
            rebootDisposable.dispose();
            rebootDisposable = null;
        }
    }

    private void clearTimerDisposable() {
        if (timerTaskDisposable != null && !timerTaskDisposable.isDisposed()) {
            timerTaskDisposable.dispose();
            timerTaskDisposable = null;
        }
    }

    /**
     * 开启线程删除刷脸记录
     */
    private void deleteSignRecordThread() {
        Observable.create((emitter -> {
            List<TableSignRecord> records = IdentifyRecordDao.getInstance().queryRecords(30);
            IdentifyRecordDao.getInstance().deleteListTransactionAsync(records, new IdentifyRecordDao.OnIdentifyListener() {
                @Override
                public void onSuccess() {
                    for (TableSignRecord record : records) {
                        FileUtils.delete(record.imagePath);
                    }
                    emitter.onNext(1);
                    emitter.onComplete();
                }

                @Override
                public void onError() {
                    emitter.onError(new Throwable("fail to delete table sign record"));
                }
            });
        })).compose(RxUtils.ioToMain())
                .subscribe(aLong -> {
                }, throwable -> throwable.printStackTrace());
    }
}
