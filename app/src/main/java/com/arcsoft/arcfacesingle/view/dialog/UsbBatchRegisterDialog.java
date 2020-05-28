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

package com.arcsoft.arcfacesingle.view.dialog;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.app.BusinessErrorCode;
import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.broadcast.UsbReceiver;
import com.arcsoft.arcfacesingle.business.common.CommonRepository;
import com.arcsoft.arcfacesingle.business.personlist.PersonListDataManager;
import com.arcsoft.arcfacesingle.data.db.dao.PersonDao;
import com.arcsoft.arcfacesingle.data.db.dao.PersonFaceDao;
import com.arcsoft.arcfacesingle.data.db.dao.PersonPermissionDao;
import com.arcsoft.arcfacesingle.data.db.table.TablePerson;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonFace;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonPermission;
import com.arcsoft.arcfacesingle.data.event.UsbEnableEvent;
import com.arcsoft.arcfacesingle.data.model.FaceExtractResult;
import com.arcsoft.arcfacesingle.data.model.UsbRegisterFailedInfo;
import com.arcsoft.arcfacesingle.libutil.ImageFileUtils;
import com.arcsoft.arcfacesingle.server.api.LocalHttpApiDataUtils;
import com.arcsoft.arcfacesingle.server.faceengine.FaceEngineManager;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.arcfacesingle.util.business.UsbHelper;
import com.arcsoft.arcfacesingle.util.scheduler.BaseObserver;
import com.arcsoft.arcfacesingle.view.widgets.HorizontalProgressBar;
import com.arcsoft.asg.libcommon.base.BaseDialogFragment;
import com.arcsoft.asg.libcommon.base.BaseViewHolder;
import com.arcsoft.asg.libcommon.util.business.rx.RxUtils;
import com.arcsoft.asg.libcommon.util.common.ActivityUtils;
import com.arcsoft.asg.libcommon.util.common.AppUtils;
import com.arcsoft.asg.libcommon.util.common.DeviceUtils;
import com.arcsoft.asg.libcommon.util.common.FileUtils;
import com.arcsoft.asg.libcommon.util.common.Md5Utils;
import com.arcsoft.asg.libcommon.util.common.ScreenUtils;
import com.arcsoft.asg.libcommon.util.common.Utils;
import com.arcsoft.faceengine.ErrorInfo;
import com.arcsoft.faceengine.FaceInfo;
import com.github.mjdev.libaums.fs.UsbFile;
import com.github.mjdev.libaums.fs.UsbFileStreamFactory;
import com.google.gson.Gson;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import me.jessyan.autosize.AutoSize;

public class UsbBatchRegisterDialog extends BaseDialogFragment {

    private static final String TAG = UsbBatchRegisterDialog.class.getSimpleName();
    private static final int REGISTER_COMPLETE_PROGRESS_NUMBER = 100;
    private static final String USB_REGISTER_FAILED_LOG_DIR = "registerFailedLog";

    private TextView tvRegisterResult;
    private TextView tvRegisterComplete;
    private TextView tvProgressValue;
    private Button btConfirm;
    private HorizontalProgressBar progressBar;

    private int imageFileTotal;
    private int registerSuccessCount;
    private int registerFailureCount;
    private FaceEngineManager faceEngineManager;
    private Subscription subscription;
    private boolean isRegisterReceiver;
    private UsbHelper usbHelper;
    private List<UsbRegisterFailedInfo> failedInfoList;

    @Override
    protected boolean useEventBus() {
        return true;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        failedInfoList = new ArrayList<>();
        usbHelper = new UsbHelper();
        usbHelper.init(getContext(), usbListener);
        isRegisterReceiver = true;
    }

    @Override
    protected int setUpLayoutId() {
        if (null != getActivity()) {
            if (ScreenUtils.isPortrait()) {
                AutoSize.autoConvertDensity(getActivity(), 720, true);
            } else {
                AutoSize.autoConvertDensity(getActivity(), 1280, true);
            }
        }
        return R.layout.dialog_batch_register_progress;
    }

    @Override
    protected void convertView(BaseViewHolder holder, BaseDialogFragment baseDialog) {
        super.convertView(holder, baseDialog);
        tvRegisterResult = holder.getView(R.id.tv_batch_register_progress_result);
        tvRegisterComplete = holder.getView(R.id.tv_batch_register_complete_result);
        tvProgressValue = holder.getView(R.id.tv_progress_value);
        progressBar = holder.getView(R.id.progress_horizontal);
        btConfirm = holder.getView(R.id.btn_batch_register_confirm);
        btConfirm.setEnabled(false);
        btConfirm.setText(AppUtils.getString(R.string.wait));
        readUsbAsync();
    }

    private UsbReceiver.UsbListener usbListener = new UsbReceiver.UsbListener() {
        @Override
        public void insertUsb(UsbDevice deviceAdd) {
        }

        @Override
        public void removeUsb(UsbDevice deviceRemove) {
            cancelSubscription();
            String strResult = CommonUtils.getStrFromRes(R.string.u_disk_removed);
            registerComplete(strResult, false);
        }

        @Override
        public void getReadUsbPermission(UsbDevice usbDevice) {
            readUsbSync(usbDevice);
        }

        @Override
        public void readFailedUsb(UsbDevice usbDevice) {
            String strResult = CommonUtils.getStrFromRes(R.string.u_disk_is_not_available);
            registerComplete(strResult, false);
        }
    };

    /**
     * 异步读取U盘
     */
    private void readUsbAsync() {
        Disposable disposable = Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            if (usbHelper != null) {
                usbHelper.readUsbDiskDevList(new UsbHelper.UsbHelperCallback() {
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
                });
            }
        }).compose(RxUtils.ioToMain())
                .subscribeWith(new BaseObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean success) {
                        if (!success) {
                            String strResult = CommonUtils.getStrFromRes(R.string.u_disk_is_not_available);
                            registerComplete(strResult, false);
                        } else {
                            registerFromUsb();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                    }
                });
    }

    /**
     * 同步读取U盘
     *
     * @param usbDevice
     */
    private void readUsbSync(UsbDevice usbDevice) {
        Disposable disposable = Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            boolean permission = false;
            if (usbHelper != null) {
                permission = usbHelper.setUpDevice(usbHelper.getUsbMass(usbDevice));
            }
            emitter.onNext(permission);
            emitter.onComplete();
        }).compose(RxUtils.ioToMain())
                .subscribeWith(new BaseObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean success) {
                        if (!success) {
                            String strResult = CommonUtils.getStrFromRes(R.string.u_disk_is_not_available);
                            registerComplete(strResult, false);
                        } else {
                            registerFromUsb();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                    }
                });
    }

    /**
     * 注册完成，显示按钮，释放资源
     */
    private void registerComplete(String strResult, boolean success) {
        if (subscription != null) {
            subscription.cancel();
        }
        if (null != ActivityUtils.getTopActivity() && null != getDialog() && getDialog().isShowing()) {
            if (success) {
                tvRegisterComplete.setTextColor(getResources().getColor(R.color.color_success_bg_color));
            } else {
                tvRegisterComplete.setTextColor(getResources().getColor(R.color.color_failed_bg_color));
            }
        }
        btConfirm.setEnabled(true);
        btConfirm.setText(CommonUtils.getStrFromRes(R.string.confirm));
        tvRegisterComplete.setText(strResult);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshAdapter(UsbEnableEvent event) {
        if (null != event) {
            if (!event.enable) {
                String strResult = CommonUtils.getStrFromRes(R.string.u_disk_removed);
                registerComplete(strResult, false);
            }
        }
    }

    public void registerFromUsb() {
        Disposable disposable = Observable.create((ObservableOnSubscribe<List<UsbFile>>) emitter -> {
            List<UsbFile> usbRootFiles = usbHelper.readFilesFromDevice();
            List<UsbFile> usbFiles = new ArrayList<>();
            UsbFile parentFile = null;
            boolean exist = false;
            for (UsbFile usbFile : usbRootFiles) {
                if (PersonRegisterMethodSelectDialog.REGISTER_FACE_INFO_DIR_NAME.equals(usbFile.getName())) {
                    parentFile = usbFile;
                    exist = true;
                    break;
                }
            }
            if (!exist) {
                emitter.onNext(usbFiles);
                emitter.onComplete();
            } else {
                CommonUtils.clearUsbFileList();
                usbFiles.addAll(CommonUtils.getAllImageFiles(parentFile));
                emitter.onNext(usbFiles);
                emitter.onComplete();
            }
        }).compose(RxUtils.ioToMain())
                .subscribeWith(new BaseObserver<List<UsbFile>>() {

                    @Override
                    public void onNext(List<UsbFile> usbFiles) {
                        failedInfoList.clear();
                        if (usbFiles != null && usbFiles.size() > 0) {
                            imageFileTotal = usbFiles.size();
                            faceEngineManager = new FaceEngineManager();
                            faceEngineManager.createFaceEngine();
                            int res = faceEngineManager.initFaceEngine();
                            if (res == ErrorInfo.MOK) {
                                setRegisterResultUi(0, 0);
                                beginRegisterPictures(usbFiles);
                            } else {
                                String strResult = CommonUtils.getStrFromRes(R.string.face_engine_init_fail_error_code, res);
                                registerComplete(strResult, false);
                            }
                        } else {
                            String strResult = CommonUtils.getStrFromRes(R.string.registered_picture_not_detected_please_check);
                            registerComplete(strResult, false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        String strResult = CommonUtils.getStrFromRes(R.string.u_disk_is_not_available);
                        registerComplete(strResult, false);
                    }
                });
    }

    private void beginRegisterPictures(List<UsbFile> files) {
        Flowable.create((FlowableEmitter<Pair<Boolean, UsbFile>> e) -> {
            boolean lastFile = false;
            for (int i = 0; i < files.size(); i++) {
                UsbFile usbFile = files.get(i);
                if (i == (files.size() - 1)) {
                    lastFile = true;
                }
                Pair<Boolean, UsbFile> pair = new Pair<>(lastFile, usbFile);
                e.onNext(pair);
            }
        }, BackpressureStrategy.BUFFER)
                .subscribe(new Subscriber<Pair<Boolean, UsbFile>>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        subscription = s;
                        subscription.request(1);
                    }

                    @Override
                    public void onNext(Pair<Boolean, UsbFile> pair) {
                        if (FileUtils.getSdcardAvailableSize() < Constants.SDCARD_STORAGE_SIZE_DELETE) {
                            String strResult = CommonUtils.getStrFromRes(R.string.device_storage_warn_tip1);
                            registerComplete(strResult, false);
                        } else if (TextUtils.isEmpty(DeviceUtils.getMacAddress())) {
                            String strResult = CommonUtils.getStrFromRes(R.string.device_mac_address_empty);
                            registerComplete(strResult, false);
                        } else {
                            processUsbFile(pair, subscription);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private void processUsbFile(Pair<Boolean, UsbFile> pair, Subscription subscription) {
        Disposable disposable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                faceEngineRegisterImage(pair, emitter);
                emitter.onComplete();
            }
        }).compose(RxUtils.ioToMain())
                .subscribeWith(new DisposableObserver<Integer>() {
                    @Override
                    public void onNext(Integer type) {
                        if (type == 1) {
                            registerSuccessCount++;
                        } else {
                            registerFailureCount++;
                        }
                        setRegisterResultUi(registerSuccessCount, registerFailureCount);
                        subscription.request(1);
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private void setRegisterResultUi(int successCount, int failureCount) {
        double div = 1.0F * (successCount + failureCount) / imageFileTotal;
        int progress = (int) (div * REGISTER_COMPLETE_PROGRESS_NUMBER);
        progressBar.setProgressBar(progress);
        String strProgress = progress + "%";
        tvProgressValue.setText(strProgress);
        String tip = Utils.getApp().getString(R.string.batch_register_count_detail, imageFileTotal,
                successCount, failureCount);
        tvRegisterResult.setText(tip);
        if (progress == REGISTER_COMPLETE_PROGRESS_NUMBER) {
            if (FileUtils.getSdcardAvailableSize() < Constants.SDCARD_STORAGE_SIZE_DELETE) {
                String strResult = CommonUtils.getStrFromRes(R.string.device_storage_warn_tip1);
                registerComplete(strResult, false);
            } else if (TextUtils.isEmpty(DeviceUtils.getMacAddress())) {
                String strResult = CommonUtils.getStrFromRes(R.string.device_mac_address_empty);
                registerComplete(strResult, false);
            } else {
                if (failedInfoList.size() > 0) {
                    saveUsbRegisterFailInfo();
                } else {
                    String strResult = CommonUtils.getStrFromRes(R.string.batch_register_complete);
                    registerComplete(strResult, true);
                }
            }
        }
    }

    /**
     * 注册人脸照
     */
    private void faceEngineRegisterImage(Pair<Boolean, UsbFile> pair, ObservableEmitter<Integer> emitter) {
        UsbFile usbFile = pair.second;
        try {
            BufferedInputStream inputStream = UsbFileStreamFactory.createBufferedInputStream(usbFile, usbHelper.getFileSystem());
            Bitmap bitmap = CommonUtils.getBitmapFromUsbFile(inputStream, Constants.FACE_REGISTER_MAX_WIDTH,
                    Constants.FACE_REGISTER_MAX_HEIGHT, usbHelper.getFileSystem());
            if (pair.first) {
                inputStream.close();
            }
            if (bitmap == null) {
                failedInfoList.add(PersonListDataManager.createUsbRegisterFailedBean(usbFile.getName(),
                        PersonListDataManager.TYPE_REGISTER_FAILED_INFO_9, 0));
                emitter.onNext(0);
                return;
            }
            long time = System.currentTimeMillis();
            FaceExtractResult feResult = faceEngineManager.extract(bitmap);
            int resultCode = feResult.getResult();
            if (resultCode != BusinessErrorCode.BEC_COMMON_OK) {
                failedInfoList.add(PersonListDataManager.createUsbRegisterFailedBean(usbFile.getName(), resultCode, resultCode));
                emitter.onNext(0);
                return;
            }
            if (FileUtils.getSdcardAvailableSize() < Constants.SDCARD_STORAGE_SIZE_DELETE) {
                failedInfoList.add(PersonListDataManager.createUsbRegisterFailedBean(usbFile.getName(),
                        PersonListDataManager.TYPE_REGISTER_FAILED_INFO_8, ErrorInfo.MOK));
                emitter.onNext(0);
                return;
            }
            if (TextUtils.isEmpty(DeviceUtils.getMacAddress())) {
                emitter.onNext(0);
                return;
            }
            FaceInfo faceInfo = feResult.getFaceInfo();
            TablePerson tablePerson = CommonRepository.getInstance().createTablePerson(usbFile.getName());
            TablePersonFace personFace = CommonRepository.getInstance().createTablePersonFace(tablePerson, faceInfo.feature);
            Bitmap newSaveBmp = ImageFileUtils.getFaceRegisterCropBitmap(faceInfo.faceRect, faceInfo.faceOrient, bitmap);
            boolean flag = ImageFileUtils.save(newSaveBmp, personFace.imagePath, Bitmap.CompressFormat.JPEG);
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
            if (newSaveBmp != null && !newSaveBmp.isRecycled()) {
                newSaveBmp.recycle();
            }
            if (!flag) {
                failedInfoList.add(PersonListDataManager.createUsbRegisterFailedBean(usbFile.getName(),
                        PersonListDataManager.TYPE_REGISTER_FAILED_INFO_7, ErrorInfo.MOK));
                emitter.onNext(0);
                return;
            }
            TablePersonPermission personPermission = LocalHttpApiDataUtils.createNewPersonPermission(tablePerson, new Gson());
            if (!PersonPermissionDao.getInstance().addModel(personPermission)) {
                failedInfoList.add(PersonListDataManager.createUsbRegisterFailedBean(usbFile.getName(),
                        PersonListDataManager.TYPE_REGISTER_FAILED_INFO_7, ErrorInfo.MOK));
                emitter.onNext(0);
                return;
            }
            if (!PersonDao.getInstance().addPerson(tablePerson)) {
                failedInfoList.add(PersonListDataManager.createUsbRegisterFailedBean(usbFile.getName(),
                        PersonListDataManager.TYPE_REGISTER_FAILED_INFO_7, ErrorInfo.MOK));
                emitter.onNext(0);
                return;
            }
            String imageBase64 = ImageFileUtils.image2Base64(personFace.imagePath);
            personFace.imageMD5 = Md5Utils.encode(imageBase64).toLowerCase();
            if (!PersonFaceDao.getInstance().addPersonFace(personFace)) {
                failedInfoList.add(PersonListDataManager.createUsbRegisterFailedBean(usbFile.getName(),
                        PersonListDataManager.TYPE_REGISTER_FAILED_INFO_7, ErrorInfo.MOK));
                emitter.onNext(0);
                return;
            }
            long endTime = System.currentTimeMillis() - time;
            emitter.onNext(1);
        } catch (Exception e) {
            e.printStackTrace();
            failedInfoList.add(PersonListDataManager.createUsbRegisterFailedBean(usbFile.getName(),
                    PersonListDataManager.TYPE_REGISTER_FAILED_INFO_0, ErrorInfo.MOK));
            emitter.onNext(0);
        }
    }

    /**
     * 保存激活错误文件信息
     */
    @SuppressLint("MissingPermission")
    private void saveUsbRegisterFailInfo() {
        Disposable disposable = Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            List<UsbFile> usbFiles = usbHelper.readFilesFromDevice();
            boolean exist = false;
            UsbFile parentFile = null;
            for (UsbFile usbFile : usbFiles) {
                String fileName = usbFile.getName();
                if (USB_REGISTER_FAILED_LOG_DIR.equals(fileName)) {
                    parentFile = usbFile;
                    exist = true;
                }
            }
            String failedInfoContent = PersonListDataManager.getUsbRegisterInfoContent(failedInfoList);
            String deviceFileName = "SN_" + DeviceUtils.getSerial().toLowerCase() + ".txt";
            UsbFile childFile = null;
            if (!exist) {
                UsbFile rootFile = usbHelper.getRootFolder();
                parentFile = rootFile.createDirectory(USB_REGISTER_FAILED_LOG_DIR);
                childFile = parentFile.createFile(deviceFileName);
                OutputStream os = UsbFileStreamFactory.createBufferedOutputStream(childFile, usbHelper.getFileSystem());
                os.write(failedInfoContent.getBytes());
                os.close();
            } else {
                UsbFile[] childFiles = parentFile.listFiles();
                for (UsbFile usbFile : childFiles) {
                    if (deviceFileName.equals(usbFile.getName())) {
                        childFile = usbFile;
                        break;
                    }
                }
                if (null == childFile) {
                    childFile = parentFile.createFile(deviceFileName);
                    OutputStream os = UsbFileStreamFactory.createBufferedOutputStream(childFile, usbHelper.getFileSystem());
                    os.write(failedInfoContent.getBytes());
                    os.close();
                } else {
                    Set<String> lineStrings = CommonUtils.getFilesFromIo(childFile, usbHelper.getFileSystem());
                    StringBuilder sb = new StringBuilder();
                    for (String string : lineStrings) {
                        sb.append(string).append("\r\n");
                    }
                    sb.append(failedInfoContent);
                    String infoContent = sb.toString();
                    OutputStream os = UsbFileStreamFactory.createBufferedOutputStream(childFile, usbHelper.getFileSystem());
                    os.write(infoContent.getBytes());
                    os.close();
                }
            }
            emitter.onNext(true);
            emitter.onComplete();
        }).compose(RxUtils.ioToMain())
                .subscribe(aBoolean -> {
                    String strResult = CommonUtils.getStrFromRes(R.string.batch_register_complete);
                    registerComplete(strResult, true);
                }, throwable -> {
                    String strResult = CommonUtils.getStrFromRes(R.string.batch_register_complete);
                    registerComplete(strResult, true);
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        release();
    }

    public void release() {
        cancelSubscription();
        if (isRegisterReceiver) {
            isRegisterReceiver = false;
            usbHelper.unRegisterReceiver();
        }
        if (faceEngineManager != null) {
            faceEngineManager.unInitFaceEngine();
            faceEngineManager = null;
        }
        if (usbHelper != null) {
            usbHelper.unInit();
            usbHelper = null;
        }
    }

    private void cancelSubscription() {
        if (subscription != null) {
            subscription.cancel();
            subscription = null;
        }
    }
}
