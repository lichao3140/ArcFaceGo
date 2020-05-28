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

import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.broadcast.UsbReceiver;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.arcfacesingle.util.business.UsbHelper;
import com.arcsoft.arcfacesingle.util.scheduler.BaseObserver;
import com.arcsoft.asg.libcommon.base.BaseDialogFragment;
import com.arcsoft.asg.libcommon.base.BaseViewHolder;
import com.arcsoft.asg.libcommon.util.business.rx.RxUtils;
import com.arcsoft.asg.libcommon.util.common.ScreenUtils;
import com.github.mjdev.libaums.fs.UsbFile;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import me.jessyan.autosize.AutoSize;

public class PersonRegisterMethodSelectDialog extends BaseDialogFragment {

    private static final String TAG = PersonRegisterMethodSelectDialog.class.getSimpleName();
    public static final String REGISTER_FACE_INFO_DIR_NAME = "facePhotoSet";

    private TextView tvUsbRegister;
    private Disposable readUsbDisposable;
    private AddFaceInfoListener addFaceInfoListener;
    private UsbHelper usbHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        usbHelper = new UsbHelper();
        usbHelper.init(getContext(), usbListener);
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
        return R.layout.dialog_add_person_face_info;
    }

    @Override
    protected void convertView(BaseViewHolder holder, BaseDialogFragment baseDialog) {
        super.convertView(holder, baseDialog);
        tvUsbRegister = holder.getView(R.id.tv_usb_register);
        TextView tvUsbRegisterWarn = holder.getView(R.id.tv_usb_register_warn);
        tvUsbRegister.setVisibility(View.VISIBLE);
        tvUsbRegisterWarn.setVisibility(View.VISIBLE);
        changeUsbRegister(false);
        readUsbAsync();
    }

    public void setAddFaceInfoListener(AddFaceInfoListener addFaceInfoListener) {
        this.addFaceInfoListener = addFaceInfoListener;
    }

    public void changeUsbRegister(boolean enable) {
        if (tvUsbRegister != null) {
            tvUsbRegister.setEnabled(enable);
        }
    }

    private UsbReceiver.UsbListener usbListener = new UsbReceiver.UsbListener() {
        @Override
        public void insertUsb(UsbDevice deviceAdd) {
            readUsbAsync();
        }

        @Override
        public void removeUsb(UsbDevice deviceRemove) {
            changeUsbRegister(false);
            if (addFaceInfoListener != null) {
                addFaceInfoListener.removeUsb(deviceRemove);
            }
        }

        @Override
        public void getReadUsbPermission(UsbDevice usbDevice) {
            readUsbSync(usbDevice);
        }

        @Override
        public void readFailedUsb(UsbDevice usbDevice) {
            if (addFaceInfoListener != null) {
                addFaceInfoListener.onRegisterCheckFailed(
                        CommonUtils.getStrFromRes(R.string.u_disk_is_not_available));
            }
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
                        changeUsbRegister(success);
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
                        changeUsbRegister(success);
                    }

                    @Override
                    public void onError(Throwable e) {
                    }
                });

    }

    public void usbRegisterFace() {
        disposableReadUsbDisposable();
        readUsbDisposable = Observable.create((ObservableOnSubscribe<List<UsbFile>>) emitter -> {
            List<UsbFile> list = new ArrayList<>();
            if (usbHelper != null) {
                list.addAll(usbHelper.readFilesFromDevice());
            }
            emitter.onNext(list);
            emitter.onComplete();
        }).compose(RxUtils.ioToMain())
                .subscribeWith(new BaseObserver<List<UsbFile>>() {
                    @Override
                    public void onNext(List<UsbFile> usbFiles) {
                        if (usbFiles.size() > 0) {
                            registerFace(usbFiles);
                        } else {
                            if (addFaceInfoListener != null) {
                                addFaceInfoListener.onRegisterCheckFailed(CommonUtils.getStrFromRes(R.string.registered_picture_not_detected_please_check));
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        changeUsbRegister(false);
                    }
                });
    }

    public void registerFace(List<UsbFile> usbRootFiles) {
        boolean exist = false;
        for (UsbFile usbFile : usbRootFiles) {
            String fileName = usbFile.getName();
            if (REGISTER_FACE_INFO_DIR_NAME.equals(fileName)) {
                exist = true;
            }
        }
        if (!exist) {
            if (addFaceInfoListener != null) {
                addFaceInfoListener.onRegisterCheckFailed(CommonUtils.getStrFromRes(R.string.registered_picture_not_detected_please_check));
            }
        } else {
            if (addFaceInfoListener != null) {
                addFaceInfoListener.startRegisterFace();
            }
        }
    }

    private void disposableReadUsbDisposable() {
        if (readUsbDisposable != null && !readUsbDisposable.isDisposed()) {
            readUsbDisposable.dispose();
            readUsbDisposable = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        addFaceInfoListener = null;
    }

    public void releaseUsb() {
        if (usbHelper != null) {
            usbHelper.unRegisterReceiver();
            usbHelper.unInit();
            usbHelper = null;
        }
        disposableReadUsbDisposable();
        usbListener = null;
    }

    public interface AddFaceInfoListener {

        /**
         * 注册结果核验失败
         * @param msg 失败信息
         */
        void onRegisterCheckFailed(String msg);

        /**
         * 开始注册人员
         */
        void startRegisterFace();

        /**
         * 移除U盘
         * @param usbDevice UsbDevice
         */
        void removeUsb(UsbDevice usbDevice);
    }
}