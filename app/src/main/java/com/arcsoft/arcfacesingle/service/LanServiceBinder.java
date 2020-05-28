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

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Binder;
import android.text.TextUtils;
import android.util.Base64;

import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.broadcast.IntelligentCardEventReceiver;
import com.arcsoft.arcfacesingle.business.common.CommonRepository;
import com.arcsoft.arcfacesingle.business.common.FaceRecordDataManager;
import com.arcsoft.arcfacesingle.business.iccard.IcCardEntranceRepository;
import com.arcsoft.arcfacesingle.data.db.dao.IdentifyRecordDao;
import com.arcsoft.arcfacesingle.data.db.table.TableSettingConfigInfo;
import com.arcsoft.arcfacesingle.data.db.table.TableSignRecord;
import com.arcsoft.arcfacesingle.data.event.DeviceStorageCheckEvent;
import com.arcsoft.arcfacesingle.data.model.IcCardReceive;
import com.arcsoft.arcfacesingle.data.model.http.ReqHeartBeat;
import com.arcsoft.arcfacesingle.server.api.LocalHttpApiDataUtils;
import com.arcsoft.arcfacesingle.server.api.LocalHttpImpl;
import com.arcsoft.arcfacesingle.util.RSAUtils;
import com.arcsoft.arcfacesingle.util.SdcardUtils;
import com.arcsoft.asg.libcommon.util.business.rx.RxUtils;
import com.arcsoft.asg.libcommon.util.common.AppUtils;
import com.arcsoft.asg.libcommon.util.common.DeviceUtils;
import com.arcsoft.asg.libcommon.util.common.FileUtils;
import com.arcsoft.asg.libcommon.util.common.NetworkUtils;
import com.arcsoft.asg.libcommon.util.common.TimeUtils;
import com.arcsoft.asg.libnetwork.bean.RemoteResponseBase;
import com.arcsoft.asg.libnetwork.helper.LocalHttpHelper;
import com.arcsoft.asg.libnetwork.manage.DefaultRemoteApiManager;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

public class LanServiceBinder extends Binder {

    private static final String PRIVATE_KEY = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAMb54UMkmJw7mQ+KOESvlnUFFNr/\n" +
            "YZbuZAa4aGyKdbJfUGekp7nJFy1vDR+f4MLy3XUCkOgna92Ll4Z+ZHtvfymbuixmlFcCs77J5zoF\n" +
            "+klRIDe7dDkzk9sAUHb+AgFPF+ROvilmRdQfK+TqVvLpcFmutPIEfE+la8hl/4CLM4NZAgMBAAEC\n" +
            "gYBoRXqu3Pa8qN7FLgHevMVz/X24ZiyWE8eHXgwbFVQobZqNNdRPn/ntbUzLTdVTVEKJlxapEj+M\n" +
            "kiKN8J5nsT5Jeo8aXdzM+DS+RT3ja8X6eujZF0/H+/dSk4ncWTbXfXMcVAIMlKvajIarG4V+AkQO\n" +
            "eMv6fAt3QT6BLZMmURVogQJBAPKhtpB6WHue36wmuOTp6TwKC55DwflhbIq7Oz7AcYr7+UqjFQ1o\n" +
            "obyOYi0C1acxp5q1kGKrEfSABLzEo2z7Kv0CQQDR8GkZA8pAs/sg4aejloAoYe9JyZsZC/rPxeTX\n" +
            "JQ1g6/XvbPMkQ/LQkUn0IDkb05dTqo9t5KgKFmISoIHZfw6NAkAQ/Fk3jBkdrmWiO6O+AtIdLeba\n" +
            "UYwG1Vcv7yOapQZN4I33N6uFoMmR1bXxyL0EU2mUXxmYjrlXEI9EV8/JFVxRAkEArAb5t7u5UYep\n" +
            "smn1+kBqx6xobeg7pSyB2xtnxzvfWJpBrm1yVzcukuVa2iZFZZ3elHpgiUYixBFPT8AiZtyMSQJA\n" +
            "bKYA402ueQ36lstNdeHFHuFU09HI14mddGcOrQuWCC6pwWLwiTIMWhKG6OX0MEh0Bn+00pjRdXNS\n" +
            "ESOgEg5kQA==\n";

    private CompositeDisposable timerTaskComposite;
    private Disposable beatHeartDisposable;
    private Disposable rebootDisposable;
    /**
     * 是否已连接至管理端
     */
    private boolean connectedServer;

    private byte[] privateKeyByte = Base64.decode(PRIVATE_KEY, Base64.DEFAULT);

    public void start() {
        startTask();
    }

    public void stop() {
        stopTask();
    }

    public void startReboot() {
        startRebootTask();
    }

    public void stopReboot() {
        clearRebootDisposable();
    }

    public void postIcEvent(Intent intent) {
        postIcService(intent);
    }

    private void startTask() {
        NetworkUtils.unRegisterNetwork();
        NetworkUtils.registerNetwork(state -> {
            if (state) {
                LocalHttpHelper.getInstance().setLocalHttpCallback(LocalHttpImpl.getInstance());
                LocalHttpHelper.getInstance().startServer(LocalHttpApiDataUtils.getLocalApiPort());
            } else {
                LocalHttpHelper.getInstance().stopServer();
            }
        });
        connectedServer = DefaultRemoteApiManager.getInstance().isInitSuccess();
        IcCardEntranceRepository.getInstance().init();
        if (timerTaskComposite == null) {
            timerTaskComposite = new CompositeDisposable();
        } else {
            clearTimerTaskComposite(false);
        }
        timerTaskComposite.add(Observable.interval(Constants.INIT_SEND_HEART, Constants.DELAY_SEND_HEART, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Long>() {
                    @Override
                    public void onNext(Long aLong) {
                        sendHeartbeat();
                        checkThumbnailFile();
                        long sdcardSize = FileUtils.getSdcardAvailableSize();
                        if (sdcardSize <= Constants.SDCARD_STORAGE_SIZE_SAFE) {
                            EventBus.getDefault().post(new DeviceStorageCheckEvent(true));
                        }
                        if (sdcardSize <= Constants.SDCARD_STORAGE_SIZE_DELETE) {
                            if (!connectedServer) {
                                deleteSignRecordThread();
                            }
                        } else {
                            FaceRecordDataManager.getInstance().startUploadTask();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                }));
        //开始定时重启任务
        clearRebootDisposable();
        startRebootTask();
    }

    private void stopTask() {
        LocalHttpHelper.getInstance().stopServer();
        FaceRecordDataManager.getInstance().stopUploadTask();
        clearTimerTaskComposite(true);
        disposeHeartBeat();
        clearRebootDisposable();
        IcCardEntranceRepository.getInstance().unInit();
    }

    private void postIcService(Intent intent) {
        byte[] content = intent.getByteArrayExtra(IntelligentCardEventReceiver.INTENT_NAME_CONTENT);
        try {
            byte[] result = RSAUtils.decryptByPrivateKey(content, privateKeyByte);
            IcCardReceive cardReceive = new Gson().fromJson(new String(result), IcCardReceive.class);
            String identity = cardReceive.getId();
            if (!TextUtils.isEmpty(identity)) {
                IcCardEntranceRepository.getInstance().processIcCardEntrance(identity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startRebootTask() {
        TableSettingConfigInfo settingConfigInfo = CommonRepository.getInstance().getSettingConfigInfo();
        boolean rebootDevice = settingConfigInfo.isRebootEveryDay();
        if (rebootDevice) {
            startRebootEveryDayTask(settingConfigInfo);
        }
    }

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
                        .compose(RxUtils.computingToMain())
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

    @SuppressLint("MissingPermission")
    private void sendHeartbeat() {
        disposeHeartBeat();
        if (!DefaultRemoteApiManager.getInstance().isInitSuccess()) {
            connectedServer = false;
            return;
        }
        TableSettingConfigInfo settingConfigInfo = CommonRepository.getInstance().getSettingConfigInfo();
        String currentIp = NetworkUtils.getIPAddress(true);
        String dbIp = settingConfigInfo.getDeviceIp();
        if (TextUtils.isEmpty(dbIp) || !dbIp.equals(currentIp)) {
            settingConfigInfo.setDeviceIp(currentIp);
            CommonRepository.getInstance().saveSettingConfigAsync(settingConfigInfo, null);
        }

        ReqHeartBeat reqHeartBeat = new ReqHeartBeat();
        reqHeartBeat.setEquipmentId(settingConfigInfo.getDeviceId());
        reqHeartBeat.setClientIp(settingConfigInfo.getDeviceIp());
        reqHeartBeat.setClientPort(settingConfigInfo.getDevicePort());
        reqHeartBeat.setSerialNumber(DeviceUtils.getSerial());
        reqHeartBeat.setMacAddress(DeviceUtils.getMacAddress());
        reqHeartBeat.setSignKey(settingConfigInfo.getSignKey());
        reqHeartBeat.setVersionCode(AppUtils.getAppVersionCode());
        reqHeartBeat.setPackageName(AppUtils.getAppPackageName());
        reqHeartBeat.setVersionName(AppUtils.getAppVersionName());

        String jsonData = new Gson().toJson(reqHeartBeat);
        beatHeartDisposable = DefaultRemoteApiManager.getInstance()
                .heartBeat(jsonData)
                .compose(RxUtils.ioToMain())
                .subscribeWith(new DisposableObserver<RemoteResponseBase>() {
                    @Override
                    public void onNext(RemoteResponseBase responseBase) {
                        connectedServer = responseBase.getCode() == Constants.HTTP_REQUEST_CODE_SUCCESS;
                    }

                    @Override
                    public void onError(Throwable e) {
                        connectedServer = false;
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    /**
     * 检测.thumbnail文件夹大小，若大小超过100M，则把文件删除
     */
    private void checkThumbnailFile() {
        String filePath = SdcardUtils.getInstance().getDCIMThumbnailDirPath();
        File parentFile = new File(filePath);
        File[] files = parentFile.listFiles();
        if (files != null && files.length > 0) {
            deleteThumbnailThread(files);
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

    /**
     * 开启线程删除thumbnail文件
     */
    private void deleteThumbnailThread(File[] files) {
        Disposable disposable = Observable.create((emitter -> {
            for (File file : files) {
                if (file.getAbsolutePath().contains(".hwbk")) {
                    continue;
                }
                if (file.isFile()) {
                    FileUtils.delete(file);
                } else if (file.isDirectory()) {
                    FileUtils.deleteDir(file);
                }
            }
            emitter.onNext(1);
            emitter.onComplete();
        })).compose(RxUtils.ioToMain())
                .subscribe(aLong -> {
                });
    }

    private void disposeHeartBeat() {
        if (null != beatHeartDisposable && !beatHeartDisposable.isDisposed()) {
            beatHeartDisposable.dispose();
            beatHeartDisposable = null;
        }
    }

    private void clearTimerTaskComposite(boolean unInit) {
        if (null != timerTaskComposite) {
            timerTaskComposite.clear();
            if (unInit) {
                timerTaskComposite = null;
            }
        }
    }
}
