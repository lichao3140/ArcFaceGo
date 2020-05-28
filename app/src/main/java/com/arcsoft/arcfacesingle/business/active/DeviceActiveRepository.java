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

package com.arcsoft.arcfacesingle.business.active;

import android.annotation.SuppressLint;
import android.hardware.usb.UsbDevice;
import android.text.TextUtils;
import android.util.SparseArray;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.broadcast.UsbReceiver;
import com.arcsoft.arcfacesingle.business.common.CommonRepository;
import com.arcsoft.arcfacesingle.data.model.ParamCheckActivation;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.arcfacesingle.util.SdcardUtils;
import com.arcsoft.arcfacesingle.util.business.UsbHelper;
import com.arcsoft.arcfacesingle.util.scheduler.BaseObserver;
import com.arcsoft.arcfacesingle.util.scheduler.ExceptionHandler;
import com.arcsoft.asg.libcommon.util.business.rx.RxUtils;
import com.arcsoft.asg.libcommon.util.common.DeviceUtils;
import com.arcsoft.asg.libcommon.util.common.FileUtils;
import com.arcsoft.asg.libcommon.util.common.PermissionUtils;
import com.arcsoft.asg.libcommon.util.common.SPUtils;
import com.arcsoft.asg.libcommon.util.common.StringUtils;
import com.arcsoft.asg.libcommon.util.common.Utils;
import com.arcsoft.faceengine.ActiveFileInfo;
import com.arcsoft.faceengine.ErrorInfo;
import com.arcsoft.faceengine.FaceEngine;
import com.github.mjdev.libaums.fs.UsbFile;
import com.github.mjdev.libaums.fs.UsbFileStreamFactory;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

public class DeviceActiveRepository implements IActive {

    private static final String TAG = DeviceActiveRepository.class.getSimpleName();

    private static final String FILE_USB_ACTIVATED_TXT = "activated_key.txt";
    private static final String FILE_USB_NOT_ACTIVE_TXT = "not_active_key.txt";
    private static final int ACTIVE_CONFIG_LIST_SIZE_THREE = 3;
    private static final int ACTIVE_CONFIG_LIST_SIZE_FOUR = 4;
    private static final int ACTIVE_CONFIG_ITEM_LENGTH = 2;
    private static final String TAG_APP_ID = "APP_ID:";
    private static final String TAG_SDK_KEY = "SDK_KEY:";
    private static final String TAG_ACTIVE_KEY = "ACTIVE_KEY:";
    private static final int[] ENGINE_CODE = {98308, 98309, 98310, 98311, 98313};
    public static final int LENGTH_CHAR_MAX = 16;

    private Disposable activeDisposable;
    private Disposable readUsbDisposable;
    private Disposable multiReadUsbFileDisposable;
    private Disposable offlineActiveDisposable;
    private boolean usbDeviceAvailable;
    private boolean useUsbActivationFile;
    private String stringActiveKeyTemp;
    private SparseArray<String> errMsgMap;

    public DeviceActiveRepository() {
        errMsgMap = CommonUtils.initFaceEngineErrorMsgList();
    }

    @Override
    public void setFromSplash(boolean fromSplash) {
        if (fromSplash) {
            UsbHelper.getInstance().init(Utils.getApp(), usbListener);
        }
    }

    @Override
    public void setSelectUsbActiveFileFlag(boolean selected) {
        useUsbActivationFile = selected;
    }

    @Override
    public boolean isSelectUsbActiveFile() {
        return useUsbActivationFile;
    }

    @Override
    public void initPermission() {
        PermissionUtils.permission(CommonUtils.getNeededPermissions())
                .callback(new PermissionUtils.FullCallback() {
                    @Override
                    public void onGranted(List<String> permissionsGrantedList) {
                        Observable.create(emitter -> SdcardUtils.getInstance().init()).compose(RxUtils.ioToMain()).subscribe();
                        stringActiveKeyTemp = "";
                        getUsbPermissionAsync();
                    }

                    @Override
                    public void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied) {
                        CommonRepository.getInstance().exitApp();
                    }
                })
                .request();
    }

    @Override
    public void onlineActive(ActiveCallback activeCallback, String appId, String sdkKey, String activeKey) {
        disposeActiveThread();
        activeDisposable = Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
            int result = FaceEngine.onlineActive(Utils.getApp(), appId, sdkKey, activeKey);
            emitter.onNext(result);
            emitter.onComplete();
        })
                .compose(RxUtils.ioToMain())
                .subscribeWith(new BaseObserver<Integer>() {
                    @Override
                    public void onNext(Integer result) {
                        if (result == ErrorInfo.MOK) {
                            activeCallback.onSuccess();
                            SPUtils.getInstance().put(Constants.SP_KEY_APP_ID, appId);
                            SPUtils.getInstance().put(Constants.SP_KEY_SDK_KEY, sdkKey);
                            SPUtils.getInstance().put(Constants.SP_KEY_ACTIVE_KEY, activeKey);
                            if (useUsbActivationFile) {
                                saveActivationFile(true, appId, sdkKey, activeKey, result, activeCallback);
                            }
                        } else {
                            boolean need = needSaveActiveKeyToUsb(result);
                            String errorMsg = errMsgMap.get(result);
                            activeCallback.onFail(result, errorMsg, need);
                            if (need) {
                                saveActivationFile(false, null, null, stringActiveKeyTemp, result, activeCallback);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        activeCallback.onFail(-1, CommonUtils.getStrFromRes(R.string.device_active_failed_please_try_again),
                                false);
                    }
                });
    }

    @Override
    public void offlineActive(ActiveCallback activeCallback, String path) {
        disposeOfflineActiveThread();
        offlineActiveDisposable = Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
            int result = FaceEngine.offlineActive(Utils.getApp(), path);
            emitter.onNext(result);
            emitter.onComplete();
        })
                .compose(RxUtils.ioToMain())
                .subscribeWith(new BaseObserver<Integer>() {
                    @Override
                    public void onNext(Integer result) {
                        if (result == ErrorInfo.MOK) {
                            activeCallback.onSuccess();
                        } else {
                            String errorMsg = errMsgMap.get(result);
                            activeCallback.onFail(result, errorMsg, false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        activeCallback.onFail(-1,
                                CommonUtils.getStrFromRes(R.string.device_active_failed_please_try_again),
                                false);
                    }
                });
    }

    @Override
    public void saveOfflineActiveFile(ReadActiveFileCallback callback) {
        Disposable disposable = Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            ActiveFileInfo activeFileInfo = FaceEngine.getActiveFileInfo(Utils.getApp());
            if (activeFileInfo != null) {
                String appId = activeFileInfo.getAppId();
                String sdkKey = activeFileInfo.getSdkKey();
                String activeKey = activeFileInfo.getActiveKey();
                SPUtils.getInstance().put(Constants.SP_KEY_APP_ID, appId);
                SPUtils.getInstance().put(Constants.SP_KEY_SDK_KEY, sdkKey);
                SPUtils.getInstance().put(Constants.SP_KEY_ACTIVE_KEY, activeKey);
                writeActiveFileToSdCard(appId, sdkKey, activeKey);
                emitter.onNext(true);
            } else {
                emitter.onNext(false);
            }
            emitter.onComplete();
        }).compose(RxUtils.ioToMain())
                .subscribeWith(new BaseObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        //写入激活文件至SD卡成功
                    }

                    @Override
                    public void onError(Throwable e) {
                        //写入激活文件至SD卡失败
                    }
                });
    }

    @Override
    public boolean needSaveActiveKeyToUsb(int errorCode) {
        boolean exist = false;
        if (useUsbActivationFile) {
            for (int code : ENGINE_CODE) {
                if (errorCode == code) {
                    exist = true;
                    break;
                }
            }
        }
        return exist;
    }

    private UsbReceiver.UsbListener usbListener = new UsbReceiver.UsbListener() {

        @Override
        public void insertUsb(UsbDevice deviceAdd) {
            getUsbPermissionAsync();
        }

        @Override
        public void removeUsb(UsbDevice deviceRemove) {
            usbDeviceAvailable = false;
        }

        @Override
        public void getReadUsbPermission(UsbDevice usbDevice) {
            getUsbPermissionSync(usbDevice);
        }

        @Override
        public void readFailedUsb(UsbDevice usbDevice) {
            usbDeviceAvailable = false;
        }
    };

    /**
     * 异步读取U盘
     */
    private void getUsbPermissionAsync() {
        Disposable disposable = Observable.create((ObservableOnSubscribe<Boolean>) emitter ->
                UsbHelper.getInstance().readUsbDiskDevList(new UsbHelper.UsbHelperCallback() {

                    @Override
                    public void requestPermission(boolean hasPermission) {
                        emitter.onNext(hasPermission);
                        emitter.onComplete();
                    }

                    @Override
                    public void onError(String message) {
                        emitter.onNext(false);
                        emitter.onComplete();
                    }
                })).compose(RxUtils.ioToMain())
                .subscribeWith(new BaseObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean success) {
                        usbDeviceAvailable = success;
                    }

                    @Override
                    public void onError(Throwable e) {
                        usbDeviceAvailable = false;
                    }
                });
    }

    /**
     * 同步读取U盘
     *
     * @param usbDevice
     */
    private void getUsbPermissionSync(UsbDevice usbDevice) {
        Disposable disposable = Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            boolean permission = UsbHelper.getInstance().setUpDevice(UsbHelper.getInstance().getUsbMass(usbDevice));
            emitter.onNext(permission);
            emitter.onComplete();
        }).compose(RxUtils.ioToMain())
                .subscribeWith(new BaseObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean success) {
                        usbDeviceAvailable = success;
                    }

                    @Override
                    public void onError(Throwable e) {
                        usbDeviceAvailable = false;
                    }
                });
    }

    @Override
    public List<String> readActivationFileFromSdCard() {
        return FileUtils.readFileByLine(SdcardUtils.getInstance().getDeviceActiveTxtFile());
    }

    @Override
    public void readActivationFileFromUsb(ReadUsbFileCallback callback) {
        disposeReadUsbDisposable();
        readUsbDisposable = Observable.create((ObservableOnSubscribe<List<UsbFile>>) emitter -> {
            emitter.onNext(UsbHelper.getInstance().readFilesFromDevice());
            emitter.onComplete();
        }).compose(RxUtils.ioToMain())
                .subscribeWith(new BaseObserver<List<UsbFile>>() {
                    @Override
                    public void onNext(List<UsbFile> usbFiles) {
                        if (usbFiles != null && usbFiles.size() > 0) {
                            boolean exist = false;
                            UsbFile notActiveFile = null;
                            UsbFile activatedFile = null;
                            for (UsbFile usbFile : usbFiles) {
                                String fileName = usbFile.getName();
                                if (FILE_USB_NOT_ACTIVE_TXT.equals(fileName)) {
                                    notActiveFile = usbFile;
                                    exist = true;
                                }
                                if (FILE_USB_ACTIVATED_TXT.equals(fileName)) {
                                    activatedFile = usbFile;
                                }
                            }
                            if (!exist) {
                                callback.getFileFailed(CommonUtils.getStrFromRes(R.string.no_active_file_please_check));
                            } else {
                                getFilesFromUsb(notActiveFile, activatedFile, callback);
                            }
                        } else {
                            callback.getFileFailed(CommonUtils.getStrFromRes(R.string.no_active_file_please_check));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        usbDeviceAvailable = false;
                    }
                });
    }

    /**
     * 从usbFile中读取文件
     *
     * @param notActiveFile
     * @param activatedFile
     * @param callback
     */
    private void getFilesFromUsb(UsbFile notActiveFile, UsbFile activatedFile, ReadUsbFileCallback callback) {
        disposeMultiReadUsbDisposable();
        multiReadUsbFileDisposable = Observable.create((ObservableEmitter<LinkedHashSet<String>> emitter) -> {
            if (notActiveFile != null) {
                emitter.onNext(CommonUtils.getFilesFromIo(notActiveFile));
            } else {
                emitter.onNext(new LinkedHashSet<>());
            }
        }).concatMap((Function<LinkedHashSet<String>, ObservableSource<List<String>>>) notActiveFiles -> {
            List<String> activeConfig = new ArrayList<>(3);
            Set<String> activatedStrings = new LinkedHashSet<>();
            if (activatedFile != null) {
                Set<String> activatedFiles = CommonUtils.getFilesFromIo(activatedFile);
                if (!activatedFiles.isEmpty()) {
                    for (String string : activatedFiles) {
                        String[] strArr = string.split(":");
                        if (strArr.length > 0) {
                            activatedStrings.add(strArr[0]);
                        }
                    }
                }
            }

            Set<String> notActiveStrings = new LinkedHashSet<>();
            if (notActiveFiles != null && !notActiveFiles.isEmpty()) {
                List<String> list = new ArrayList<>(notActiveFiles);
                if (list.size() < ACTIVE_CONFIG_LIST_SIZE_THREE) {
                    throw new ExceptionHandler.CustomThrowable(new Throwable(), ExceptionHandler.ERROR.CUSTOM_BUSINESS,
                            CommonUtils.getStrFromRes(R.string.config_file_not_complete));
                }
                for (int i = 0; i < list.size(); i++) {
                    String str = list.get(i);
                    if (i == 0 && !str.contains(TAG_APP_ID)) {
                        throw new ExceptionHandler.CustomThrowable(new Throwable(), ExceptionHandler.ERROR.CUSTOM_BUSINESS,
                                CommonUtils.getStrFromRes(R.string.config_file_format_error));
                    }
                    if (i == 0 && str.contains(TAG_APP_ID)) {
                        String[] arr = str.split(":");
                        if (arr.length != ACTIVE_CONFIG_ITEM_LENGTH) {
                            throw new ExceptionHandler.CustomThrowable(new Throwable(), ExceptionHandler.ERROR.CUSTOM_BUSINESS,
                                    CommonUtils.getStrFromRes(R.string.config_file_format_error));
                        }
                    }
                    if (i == 1 && !str.contains(TAG_SDK_KEY)) {
                        throw new ExceptionHandler.CustomThrowable(new Throwable(), ExceptionHandler.ERROR.CUSTOM_BUSINESS,
                                CommonUtils.getStrFromRes(R.string.config_file_format_error));
                    }
                    if (i == 1 && str.contains(TAG_SDK_KEY)) {
                        String[] arr = str.split(":");
                        if (arr.length != ACTIVE_CONFIG_ITEM_LENGTH) {
                            throw new ExceptionHandler.CustomThrowable(new Throwable(), ExceptionHandler.ERROR.CUSTOM_BUSINESS,
                                    CommonUtils.getStrFromRes(R.string.config_file_format_error));
                        }
                    }
                    if (i == 2) {
                        if (!str.contains(TAG_ACTIVE_KEY)) {
                            throw new ExceptionHandler.CustomThrowable(new Throwable(), ExceptionHandler.ERROR.CUSTOM_BUSINESS,
                                    CommonUtils.getStrFromRes(R.string.config_file_format_error));
                        } else if (list.size() < ACTIVE_CONFIG_LIST_SIZE_FOUR) {
                            throw new ExceptionHandler.CustomThrowable(new Throwable(), ExceptionHandler.ERROR.CUSTOM_BUSINESS,
                                    CommonUtils.getStrFromRes(R.string.config_file_format_error));
                        }
                    }
                }
                for (String str : notActiveFiles) {
                    if (str.contains(TAG_APP_ID)) {
                        activeConfig.add(0, str);
                    }
                    if (str.contains(TAG_SDK_KEY)) {
                        activeConfig.add(1, str);
                    }
                    if (str.contains(TAG_APP_ID) || str.contains(TAG_SDK_KEY) || str.contains(TAG_ACTIVE_KEY)) {
                        continue;
                    }
                    notActiveStrings.add(str);
                }
            }

            if (notActiveStrings.size() > 0 && activatedStrings.size() > 0) {
                Set<String> diffList = new LinkedHashSet<>();
                diffList.addAll(notActiveStrings);
                diffList.removeAll(activatedStrings);
                if (diffList.size() == 0) {
                    throw new ExceptionHandler.CustomThrowable(new Throwable(), ExceptionHandler.ERROR.CUSTOM_BUSINESS,
                            CommonUtils.getStrFromRes(R.string.u_disk_config_file_empty));
                }
                Iterator<String> iterator = diffList.iterator();
                if (iterator.hasNext()) {
                    String strActiveKey = iterator.next();
                    activeConfig.add(TAG_ACTIVE_KEY + strActiveKey);
                    stringActiveKeyTemp = strActiveKey;
                }
            } else if (notActiveStrings.size() > 0) {
                Iterator<String> iterator = notActiveStrings.iterator();
                if (iterator.hasNext()) {
                    String strActiveKey = iterator.next();
                    activeConfig.add(TAG_ACTIVE_KEY + strActiveKey);
                    stringActiveKeyTemp = strActiveKey;
                }
            } else {
                throw new ExceptionHandler.CustomThrowable(new Throwable(), ExceptionHandler.ERROR.CUSTOM_BUSINESS,
                        CommonUtils.getStrFromRes(R.string.config_file_not_complete));
            }
            return Observable.just(activeConfig);
        }).compose(RxUtils.ioToMain())
                .subscribeWith(new BaseObserver<List<String>>() {
                    @Override
                    public void onNext(List<String> list) {
                        useUsbActivationFile = true;
                        callback.getFileSuccess(list);
                    }

                    @Override
                    public void onError(ExceptionHandler.ResponseThrowable throwable) {
                        callback.getFileFailed(throwable.message);
                    }
                });
    }

    @Override
    public ParamCheckActivation checkActivationAppId(List<String> activeConfigs, boolean useUsb) {
        ParamCheckActivation param = new ParamCheckActivation();
        if (activeConfigs.size() != ACTIVE_CONFIG_LIST_SIZE_THREE) {
            param.setSuccess(false);
            param.setMessage(Utils.getApp().getString(R.string.config_file_not_complete));
            return param;
        }
        String string0 = activeConfigs.get(0);
        String[] strArr0 = string0.split(":");
        if (strArr0.length != ACTIVE_CONFIG_ITEM_LENGTH) {
            param.setSuccess(false);
            param.setMessage(Utils.getApp().getString(R.string.config_file_format_error));
            return param;
        }
        String strAppId = strArr0[1].replaceAll(" ", "").trim();
        if (TextUtils.isEmpty(strAppId)) {
            param.setSuccess(false);
            param.setMessage(Utils.getApp().getString(R.string.app_id_empty));
            return param;
        }
        if (StringUtils.containSpecialChar(strAppId, "")) {
            param.setSuccess(false);
            param.setMessage(Utils.getApp().getString(R.string.app_id_contain_special_char));
            return param;
        }
        param.setSuccess(true);
        param.setAppId(strAppId);
        return param;
    }

    @Override
    public ParamCheckActivation checkActivationSdkKey(List<String> activeConfigs, boolean useUsb) {
        ParamCheckActivation param = new ParamCheckActivation();
        String string1 = activeConfigs.get(1);
        String[] strArr1 = string1.split(":");
        if (strArr1.length != ACTIVE_CONFIG_ITEM_LENGTH) {
            param.setSuccess(false);
            param.setMessage(Utils.getApp().getString(R.string.config_file_format_error));
            return param;
        }
        String strSdkKey = strArr1[1].replaceAll(" ", "").trim();
        if (TextUtils.isEmpty(strSdkKey)) {
            param.setSuccess(false);
            param.setMessage(Utils.getApp().getString(R.string.sdk_key_empty));
            return param;
        }
        if (StringUtils.containSpecialChar(strSdkKey, "")) {
            param.setSuccess(false);
            param.setMessage(Utils.getApp().getString(R.string.sdk_key_contain_special_char));
            return param;
        }
        param.setSuccess(true);
        param.setSdkKey(strSdkKey);
        return param;
    }

    @Override
    public ParamCheckActivation checkActivationActiveKey(List<String> activeConfigs, boolean useUsb) {
        ParamCheckActivation param = new ParamCheckActivation();
        String string2 = activeConfigs.get(2);
        String[] strArr2 = string2.split(":");
        if (strArr2.length != ACTIVE_CONFIG_ITEM_LENGTH) {
            param.setSuccess(false);
            param.setMessage(Utils.getApp().getString(R.string.config_file_format_error));
            return param;
        }
        String strActiveKey = strArr2[1].replaceAll(" ", "").trim();
        if (TextUtils.isEmpty(strActiveKey)) {
            param.setSuccess(false);
            param.setMessage(Utils.getApp().getString(R.string.active_key_empty));
            return param;
        }
        if (StringUtils.containSpecialChar(strActiveKey, "")) {
            param.setSuccess(false);
            param.setMessage(Utils.getApp().getString(R.string.active_key_contain_special_char));
            return param;
        }
        param.setSuccess(true);
        param.setActiveKey(strActiveKey.replaceAll("-", "").trim());
        return param;
    }

    @Override
    public boolean usbDeviceAvailable() {
        return usbDeviceAvailable;
    }

    private void disposeActiveThread() {
        if (activeDisposable != null) {
            if (!activeDisposable.isDisposed()) {
                activeDisposable.dispose();
            }
            activeDisposable = null;
        }
    }

    private void disposeOfflineActiveThread() {
        if (offlineActiveDisposable != null) {
            if (!offlineActiveDisposable.isDisposed()) {
                offlineActiveDisposable.dispose();
            }
            offlineActiveDisposable = null;
        }
    }

    private void disposeReadUsbDisposable() {
        if (readUsbDisposable != null && !readUsbDisposable.isDisposed()) {
            readUsbDisposable.dispose();
            readUsbDisposable = null;
        }
    }

    /**
     * 将激活文件保存至本地，更新USB文件中的已激活文件
     */
    @SuppressLint("MissingPermission")
    private void saveActivationFile(boolean success, String appId, String sdkKey, String activeKey,
                                    int errorCode, ActiveCallback callback) {
        getSaveFileObservable(success, appId, sdkKey, activeKey)
                .concatMap((Function<Boolean, ObservableSource<Boolean>>) aBoolean -> {
                    List<UsbFile> usbFiles = UsbHelper.getInstance().readFilesFromDevice();
                    boolean exist = false;
                    UsbFile txtFile = null;
                    for (UsbFile usbFile : usbFiles) {
                        String fileName = usbFile.getName();
                        if (FILE_USB_ACTIVATED_TXT.equals(fileName)) {
                            txtFile = usbFile;
                            exist = true;
                        }
                    }
                    String content;
                    if (success) {
                        content = activeKey + ":" + DeviceUtils.getSerial();
                    } else {
                        content = activeKey + ":Failed_" + errorCode;
                    }
                    if (exist) {
                        Set<String> lineStrings = CommonUtils.getFilesFromIo(txtFile);
                        StringBuffer sb = new StringBuffer();
                        for (String string : lineStrings) {
                            if (!string.equals(content)) {
                                sb.append(string).append("\r\n");
                            }
                        }
                        sb.append(content);
                        OutputStream os = UsbFileStreamFactory.createBufferedOutputStream(txtFile,
                                UsbHelper.getInstance().getFileSystem());
                        os.write(sb.toString().getBytes());
                        os.close();
                    } else {
                        UsbFile rooFile = UsbHelper.getInstance().getRootFolder();
                        txtFile = rooFile.createFile(FILE_USB_ACTIVATED_TXT);
                        OutputStream os = UsbFileStreamFactory.createBufferedOutputStream(txtFile,
                                UsbHelper.getInstance().getFileSystem());
                        os.write(content.getBytes());
                        os.close();
                    }
                    return Observable.just(true);
                })
                .compose(RxUtils.ioToMain())
                .subscribeWith(new BaseObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (callback != null) {
                            callback.onSaveActiveFile(aBoolean);
                        }
                    }

                    @Override
                    public void onError(ExceptionHandler.ResponseThrowable throwable) {
                        if (callback != null) {
                            callback.onSaveActiveFile(true);
                        }
                    }
                });
    }

    private Observable<Boolean> getSaveFileObservable(boolean success, String appId, String sdkKey, String activeKey) {
        if (success) {
            return Observable.create(emitter -> {
                writeActiveFileToSdCard(appId, sdkKey, activeKey);
                emitter.onNext(true);
            });
        } else {
            return Observable.just(true);
        }
    }

    private void writeActiveFileToSdCard(String appId, String sdkKey, String activeKey) {
        StringBuilder stringBuilder = new StringBuilder(TAG_APP_ID)
                .append(appId)
                .append("\r\n")
                .append(TAG_SDK_KEY)
                .append(sdkKey)
                .append("\r\n")
                .append(TAG_ACTIVE_KEY)
                .append(activeKey);
        String content = stringBuilder.toString();
        String filePath = SdcardUtils.getInstance().getDeviceActiveTxtFile();
        FileUtils.deleteFile(filePath);
        FileUtils.writeContent(content, filePath);
    }

    private void disposeMultiReadUsbDisposable() {
        if (multiReadUsbFileDisposable != null && !multiReadUsbFileDisposable.isDisposed()) {
            multiReadUsbFileDisposable.dispose();
            multiReadUsbFileDisposable = null;
        }
    }

    public void unInit() {
        UsbHelper.getInstance().unInit();
        disposeActiveThread();
        disposeReadUsbDisposable();
        disposeMultiReadUsbDisposable();
        disposeOfflineActiveThread();
        stringActiveKeyTemp = null;
    }

    public interface ReadUsbFileCallback {

        /**
         * 获取文件成功
         * @param fileList 文件数据
         */
        void getFileSuccess(List<String> fileList);

        /**
         * 获取文件失败
         * @param msg 失败信息
         */
        void getFileFailed(String msg);
    }
}
