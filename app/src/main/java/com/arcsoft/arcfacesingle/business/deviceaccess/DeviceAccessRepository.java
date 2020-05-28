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

package com.arcsoft.arcfacesingle.business.deviceaccess;

import android.text.TextUtils;
import android.util.Pair;
import android.util.SparseArray;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.business.common.CommonRepository;
import com.arcsoft.arcfacesingle.data.db.dao.SettingConfigInfoDao;
import com.arcsoft.arcfacesingle.data.event.KeyboardVisibleEvent;
import com.arcsoft.arcfacesingle.data.model.DeviceAccessInfo;
import com.arcsoft.arcfacesingle.service.CloudAIotService;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.arcfacesingle.util.scheduler.BaseObserver;
import com.arcsoft.arcfacesingle.util.scheduler.ExceptionHandler;
import com.arcsoft.arcsoftlink.enums.ArcFaceVersionEnum;
import com.arcsoft.arcsoftlink.enums.ArcLinkErrorCodeEnum;
import com.arcsoft.arcsoftlink.http.bean.res.DeviceInfo;
import com.arcsoft.arcsoftlink.http.bean.res.DeviceInfoResponse;
import com.arcsoft.arcsoftlink.mqtt.ArcLinkEngine;
import com.arcsoft.arcsoftlink.mqtt.ArcLinkException;
import com.arcsoft.arcsoftlink.mqtt.InitResult;
import com.arcsoft.asg.libcommon.util.business.rx.RxUtils;
import com.arcsoft.asg.libcommon.util.common.ActivityUtils;
import com.arcsoft.asg.libcommon.util.common.KeyboardUtils;
import com.arcsoft.asg.libcommon.util.common.SPUtils;
import com.arcsoft.asg.libcommon.util.common.Utils;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;

public class DeviceAccessRepository implements IDeviceAccess {

    public static final int CODE_CHECK_DEVICE_INFO_SUCCESS = 0;
    public static final int CODE_CHECK_DEVICE_INFO_FAIL = -1;
    public static final int CODE_DEVICE_ONLINE = 1;
    public static final int TYPE_DEVICE_TAG = 0;
    public static final int TYPE_ACCESS_ID = 1;
    public static final int TYPE_SERVER_URL = 2;
    private static final int LENGTH_DEVICE_TAG_MAX = 30;

    private WeakReference<DeviceAccessListener> listenerWeakReference;
    private SparseArray deviceAccessMsgList;
    private Disposable accessFirstDisposable;
    private Disposable accessSecondDisposable;
    private Disposable updateTagDisposable;

    public DeviceAccessRepository(DeviceAccessListener listener) {
        if (listener != null) {
            listenerWeakReference = new WeakReference<>(listener);
        }
    }

    @Override
    public void init(boolean fromSplash) {
        deviceAccessMsgList = ExceptionHandler.initDeviceAccessMsgList();
        if (fromSplash) {
            CommonRepository.getInstance().initMainLogo();
            CommonRepository.getInstance().initSecondLogo();
        }
    }

    @Override
    public void initUrl(String url) {
        if (!TextUtils.isEmpty(url)) {
            try {
                ArcLinkEngine.getInstance().setUrl(url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void accessDeviceFirst(String accessId, String deviceTag, String url, AccessDeviceFirstCallback callback) {
        Pair<Integer, String> pair = checkAccessId(accessId);
        if (pair.first != CODE_CHECK_DEVICE_INFO_SUCCESS) {
            callback.onFail(TYPE_ACCESS_ID, pair.second);
            return;
        }
        Pair<Integer, String> pair1 = checkDeviceTag(deviceTag);
        if (pair1.first != CODE_CHECK_DEVICE_INFO_SUCCESS) {
            callback.onFail(TYPE_DEVICE_TAG, pair1.second);
            return;
        }
        Pair<Integer, String> pairUrl = checkServerUrl(url);
        if (pairUrl.first != CODE_CHECK_DEVICE_INFO_SUCCESS) {
            callback.onFail(TYPE_SERVER_URL, pairUrl.second);
            return;
        }
        if (disposeDisposable(accessFirstDisposable)) {
            accessFirstDisposable = null;
        }
        accessFirstDisposable = Observable.create((ObservableEmitter<DeviceInfoResponse> emitter) -> {
            ArcLinkEngine.getInstance().setUrl(url);
            DeviceInfoResponse deviceInfo = ArcLinkEngine.getInstance().getDeviceInfo(accessId);
            if (deviceInfo == null) {
                DeviceInfoResponse deviceInfoError = new DeviceInfoResponse();
                deviceInfoError.setData(null);
                emitter.onNext(deviceInfoError);
            } else {
                emitter.onNext(deviceInfo);
            }
        }).compose(RxUtils.ioToMain())
                .subscribeWith(new BaseObserver<DeviceInfoResponse>() {
                    @Override
                    public void onNext(DeviceInfoResponse response) {
                        if (response != null) {
                            int code = response.getCode();
                            if (code == ArcLinkErrorCodeEnum.SUCCESS.getCode()) {
                                DeviceInfo deviceInfo = response.getData();
                                if (deviceInfo != null) {
                                    String currentMac = deviceInfo.getCurrentDeviceMacAddress();
                                    String lastMac = deviceInfo.getLastDeviceMacAddress();
                                    int onLine = deviceInfo.getOnline();
                                    if (onLine == CODE_DEVICE_ONLINE) {
                                        if (!currentMac.equals(lastMac)) {
                                            //该接入码正在被其他设备使用，是否将其覆盖
                                            if (getListener() != null) {
                                                getListener().showCoverDeviceDialog(accessId, deviceTag, url);
                                            }
                                        } else {
                                            //该设备已处于在线状态
                                            if (getListener() != null) {
                                                getListener().accessNewDevice(deviceInfo, accessId, deviceTag, url);
                                            }
                                        }
                                    } else {
                                        if (TextUtils.isEmpty(lastMac)) {
                                            //此AccessId对应的为新接入设备
                                            if (getListener() != null) {
                                                getListener().accessNewDevice(deviceInfo, accessId, deviceTag, url);
                                            }
                                        } else if (currentMac.equals(lastMac)) {
                                            //该设备已离线
                                            if (getListener() != null) {
                                                getListener().accessNewDevice(deviceInfo, accessId, deviceTag, url);
                                            }
                                        } else {
                                            //accessId对应之前的设备已离线，可以绑定到新设备
                                            if (getListener() != null) {
                                                getListener().accessNewDevice(deviceInfo, accessId, deviceTag, url);
                                            }
                                        }
                                    }
                                } else {
                                    if (getListener() != null) {
                                        getListener().deviceAccessFail(ExceptionHandler.ERROR.UNKNOWN,
                                                CommonUtils.getStrFromRes(R.string.device_info_get_fail));
                                    }
                                }
                            } else {
                                String msg = getMsgFromMsgList(code);
                                if (getListener() != null) {
                                    getListener().deviceAccessFail(code, msg);
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(ExceptionHandler.ResponseThrowable throwable) {
                        if (getListener() != null) {
                            getListener().deviceAccessFail(throwable.code, throwable.message);
                        }
                    }
                });
    }

    @Override
    public void coverAlreadyBindDevice(String accessId, String deviceTag, String url) {
        accessDeviceSecond(null, accessId, deviceTag, url);
    }

    @Override
    public Pair<Integer, String> checkAccessId(String accessId) {
        if (TextUtils.isEmpty(accessId)) {
            return new Pair<>(CODE_CHECK_DEVICE_INFO_FAIL, CommonUtils.getStrFromRes(R.string.not_empty));
        }
        return new Pair<>(CODE_CHECK_DEVICE_INFO_SUCCESS, CommonUtils.getStrFromRes(R.string.successful_operation));
    }

    @Override
    public Pair<Integer, String> checkDeviceTag(String tag) {
        if (TextUtils.isEmpty(tag)) {
            return new Pair<>(CODE_CHECK_DEVICE_INFO_FAIL, CommonUtils.getStrFromRes(R.string.not_empty));
        }
        if (tag.length() > LENGTH_DEVICE_TAG_MAX) {
            return new Pair<>(CODE_CHECK_DEVICE_INFO_FAIL, CommonUtils.getStrFromRes(R.string.char_length_is_not_twenty));
        }
        return new Pair<>(CODE_CHECK_DEVICE_INFO_SUCCESS, CommonUtils.getStrFromRes(R.string.successful_operation));
    }

    @Override
    public Pair<Integer, String> checkServerUrl(String serverUrl) {
        if (TextUtils.isEmpty(serverUrl)) {
            return new Pair<>(CODE_CHECK_DEVICE_INFO_FAIL, CommonUtils.getStrFromRes(R.string.not_empty));
        }
        return new Pair<>(CODE_CHECK_DEVICE_INFO_SUCCESS, CommonUtils.getStrFromRes(R.string.successful_operation));
    }

    @Override
    public void accessDeviceSecond(DeviceInfo deviceInfo, String accessId, String deviceTag, String url) {
        if (disposeDisposable(accessSecondDisposable)) {
            accessSecondDisposable = null;
        }
        accessSecondDisposable = Observable.create(new ObservableOnSubscribe<InitResult>() {
            @Override
            public void subscribe(ObservableEmitter<InitResult> emitter) throws Exception {
                ArcLinkEngine.getInstance().setUrl(url);
                InitResult initResult = ArcLinkEngine.getInstance().init(Utils.getApp(), ArcFaceVersionEnum.V_2_2,
                        accessId, deviceTag, null);
                if (initResult == null) {
                    emitter.onError(new Throwable(CommonUtils.getStrFromRes(R.string.unable_to_connect_to_server)));
                } else {
                    emitter.onNext(initResult);
                }
            }
        }).compose(RxUtils.ioToMain())
                .subscribeWith(new BaseObserver<InitResult>() {
                    @Override
                    public void onNext(InitResult initResult) {
                        int code = initResult.getCode();
                        if (code == ArcLinkErrorCodeEnum.SUCCESS.getCode()) {
                            String content = CommonUtils.getStrFromRes(R.string.device_already_access_app,
                                    initResult.getData().getAppName());
                            if (getListener() != null) {
                                getListener().deviceSecondSuccess(content);
                                SettingConfigInfoDao.getInstance().updateDeviceName(deviceTag);
                                CommonRepository.getInstance().getSettingConfigInfo().setDeviceName(deviceTag);
                            }
                            CommonRepository.getInstance().setDeviceAccessStatus(true);
                            SPUtils.getInstance().put(Constants.SP_KEY_ARC_LINK_SERVER_ADDRESS, url);
                            String oldAccessId = SPUtils.getInstance().getString(Constants.SP_KEY_DEVICE_OLD_ACCESS_ID);
                            if (!TextUtils.isEmpty(oldAccessId) && !accessId.equals(oldAccessId)) {
                                SPUtils.getInstance().put(Constants.SP_KEY_ARC_LINK_PERSON_SYNC_KEY, "");
                            }
                            SPUtils.getInstance().put(Constants.SP_KEY_DEVICE_ACCESS_ID, accessId);
                            SPUtils.getInstance().put(Constants.SP_KEY_DEVICE_OLD_ACCESS_ID, accessId);
                            SPUtils.getInstance().put(Constants.SP_KEY_DEVICE_TAG, deviceTag);
                            CloudAIotService.startSyncData();
                            CommonRepository.getInstance().uploadArcLinkMainLogo();
                            CommonRepository.getInstance().uploadArcLinkSecondLogo();

                        } else {
                            String msg = getMsgFromMsgList(code);
                            if (getListener() != null) {
                                getListener().deviceAccessFail(code, msg);
                            }
                        }
                    }

                    @Override
                    public void onError(ExceptionHandler.ResponseThrowable throwable) {
                        if (getListener() != null) {
                            getListener().deviceAccessFail(throwable.code, throwable.message);
                        }
                    }
                });
    }

    @Override
    public void unBindDevice() {
        CommonRepository.getInstance().arcLinkDisconnected();
    }

    @Override
    public boolean getDeviceAccessStatus() {
        return CommonRepository.getInstance().getDeviceAccessStatus();
    }

    private void saveServerUrl(String url) {
        SPUtils.getInstance().put(Constants.SP_KEY_ARC_LINK_SERVER_ADDRESS, url);
    }

    @Override
    public void updateDeviceTag(String accessId, String deviceTag, String url) {
        if (disposeDisposable(updateTagDisposable)) {
            updateTagDisposable = null;
        }
        updateTagDisposable = Observable.create(new ObservableOnSubscribe<InitResult>() {
            @Override
            public void subscribe(ObservableEmitter<InitResult> emitter) throws Exception {
                ArcLinkEngine.getInstance().unInit();
                DeviceAccessInfo accessInfo = CommonRepository.getInstance().getDeviceAccessInfo();
                ArcLinkEngine.getInstance().setUrl(accessInfo.getServerIp());
                InitResult initResult = ArcLinkEngine.getInstance().init(Utils.getApp(), ArcFaceVersionEnum.V_2_2,
                        accessId, deviceTag, null);
                if (initResult == null) {
                    emitter.onError(new Throwable(CommonUtils.getStrFromRes(R.string.unable_to_connect_to_server)));
                } else {
                    emitter.onNext(initResult);
                }
            }
        }).compose(RxUtils.ioToMain())
                .subscribeWith(new BaseObserver<InitResult>() {
                    @Override
                    public void onNext(InitResult initResult) {
                        int code = initResult.getCode();
                        if (code == ArcLinkErrorCodeEnum.SUCCESS.getCode()) {
                            KeyboardUtils.hideSoftInput(ActivityUtils.getTopActivity());
                            CommonRepository.getInstance().setDeviceAccessStatus(true);
                            SPUtils.getInstance().put(Constants.SP_KEY_DEVICE_ACCESS_ID, accessId);
                            SPUtils.getInstance().put(Constants.SP_KEY_DEVICE_OLD_ACCESS_ID, accessId);
                            SPUtils.getInstance().put(Constants.SP_KEY_DEVICE_TAG, deviceTag);
                            SettingConfigInfoDao.getInstance().updateDeviceName(deviceTag);
                            CommonRepository.getInstance().getSettingConfigInfo().setDeviceName(deviceTag);

                            if (getListener() != null) {
                                getListener().updateDeviceTagSuccess(CommonUtils.getStrFromRes(R.string.device_tag_update_success));
                            }
                            CloudAIotService.startSyncData();
                            EventBus.getDefault().post(new KeyboardVisibleEvent(true));
                        } else {
                            String msg = getMsgFromMsgList(code);
                            String content = CommonUtils.getStrFromRes(R.string.device_tag_update_fail, msg);
                            if (getListener() != null) {
                                getListener().deviceAccessFail(code, content);
                            }
                        }
                    }

                    @Override
                    public void onError(ExceptionHandler.ResponseThrowable throwable) {
                        if (getListener() != null) {
                            getListener().deviceAccessFail(throwable.code, throwable.message);
                        }
                    }
                });
    }

    @Override
    public boolean updateServerUrl(String url) {
        try {
            ArcLinkEngine.getInstance().setUrl(url);
            saveServerUrl(url);
            return true;
        } catch (ArcLinkException e) {
            e.printStackTrace();
            if (getListener() != null) {
                getListener().showMsgDialog(ArcLinkErrorCodeEnum.ERROR_ILLEGAL_URL.getCode(),
                        CommonUtils.getStrFromRes(R.string.server_address_is_not_valid));
            }
        }
        return false;
    }

    public DeviceAccessListener getListener() {
        return listenerWeakReference != null ? listenerWeakReference.get() : null;
    }

    @Override
    public void unInit() {
        deviceAccessMsgList = null;
        disposeDisposable(accessFirstDisposable);
        disposeDisposable(accessSecondDisposable);
        disposeDisposable(updateTagDisposable);
        accessFirstDisposable = null;
        accessSecondDisposable = null;
        updateTagDisposable = null;
        listenerWeakReference = null;
    }

    private boolean disposeDisposable(Disposable disposable) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
            return true;
        }
        return false;
    }

    private String getMsgFromMsgList(int code) {
        if (deviceAccessMsgList == null || deviceAccessMsgList.size() == 0) {
            return CommonUtils.getStrFromRes(R.string.face_manager_tip_common_fail);
        }
        String msg = (String) deviceAccessMsgList.get(code);
        if (TextUtils.isEmpty(msg)) {
            return CommonUtils.getStrFromRes(R.string.face_manager_tip_common_fail);
        }
        return msg;
    }
}
