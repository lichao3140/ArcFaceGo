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

import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;

import androidx.databinding.BaseObservable;
import androidx.databinding.BindingAdapter;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.business.photo.ITakePhotoCallback;
import com.arcsoft.arcfacesingle.business.photo.TakePhotoRepository;
import com.arcsoft.arcfacesingle.libutil.ImageFileUtils;
import com.arcsoft.arcfacesingle.navigator.TakePhotoNavigator;
import com.arcsoft.arcfacesingle.util.glide.GlideUtils;
import com.arcsoft.asg.libcamera.view.CameraFaceView;
import com.arcsoft.asg.libcommon.util.business.DoubleClickUtils;
import com.arcsoft.asg.libcommon.util.common.DeviceUtils;
import com.arcsoft.asg.libcommon.util.common.FileUtils;
import com.arcsoft.asg.libcommon.util.common.ToastUtils;

public class TakePhotoViewModel extends BaseObservable implements ITakePhotoCallback {

    private TakePhotoNavigator navigator;
    private TakePhotoRepository repository;
    private String strCameraPhotoPath;
    private String strPersonSerial;

    public final ObservableBoolean llTakePhotoResultVisible = new ObservableBoolean(false);
    public final ObservableBoolean ivTakeResultVisible = new ObservableBoolean(false);
    public final ObservableBoolean btnConfirmVisible = new ObservableBoolean(true);
    public final ObservableBoolean radioButtonVisible = new ObservableBoolean(true);
    public final ObservableField<String> bitmapObservableField = new ObservableField<>();

    public TakePhotoViewModel() {
        repository = new TakePhotoRepository();
        repository.init();
    }

    public void setNavigator(TakePhotoNavigator takePhotoNavigator) {
        this.navigator = takePhotoNavigator;
    }

    public void onActivityResume() {
        if (repository != null) {
            repository.onActivityResume();
        }
    }

    public void onActivityPause() {
        if (repository != null) {
            repository.onActivityPause();
        }
    }

    public void onActivityDestroyed() {
        if (repository != null) {
            repository.onActivityDestroyed();
        }
        navigator = null;
        strCameraPhotoPath = null;
        strPersonSerial = null;
    }

    public void bindCamera(CameraFaceView cameraFaceView) {
        repository.bindCamera(cameraFaceView, this);
    }

    @Override
    public void onCameraOpened() {

    }

    @Override
    public void onTakePhotoError(String message) {
        llTakePhotoResultVisible.set(true);
        setCameraFaceViewVisible(false);
        radioButtonVisible.set(false);
        btnConfirmVisible.set(false);
        ToastUtils.showShortToast(message);
    }

    @Override
    public void onTakePhoto(String personSerial, String imgPath) {
        llTakePhotoResultVisible.set(true);
        setCameraFaceViewVisible(false);
        radioButtonVisible.set(false);
        btnConfirmVisible.set(true);
        ivTakeResultVisible.set(true);

        strPersonSerial = personSerial;
        strCameraPhotoPath = imgPath;
        Pair<Integer, Integer> pair = ImageFileUtils.getImageOption(imgPath);
        if (navigator != null) {
            navigator.setPhotoResultParams(pair.first, pair.second);
        }
        bitmapObservableField.set(strCameraPhotoPath);
    }

    private void setCameraFaceViewVisible(boolean visible) {
        if (navigator != null) {
            navigator.setCameraFaceViceVisible(visible);
        }
    }

    public void onClick(View v) {
        int resId = v.getId();
        if (DoubleClickUtils.isFastDoubleClick(resId)) {
            return;
        }
        switch (resId) {
            case R.id.bt_take_photo_confirm:
                if (FileUtils.getSdcardAvailableSize() < Constants.SDCARD_STORAGE_SIZE_DELETE) {
                    ToastUtils.showShortToast(R.string.device_storage_warn_tip1);
                    return;
                }
                if (TextUtils.isEmpty(DeviceUtils.getMacAddress())) {
                    ToastUtils.showShortToast(R.string.device_mac_address_empty);
                    return;
                }
                if (navigator != null) {
                    navigator.confirmTakePhoto(strCameraPhotoPath, strPersonSerial);
                }
                break;
            case R.id.bt_take_photo_retry:
                llTakePhotoResultVisible.set(false);
                ivTakeResultVisible.set(false);
                radioButtonVisible.set(true);
                setCameraFaceViewVisible(true);
                if (FileUtils.isFileExists(strCameraPhotoPath)) {
                    FileUtils.delete(strCameraPhotoPath);
                }
                if (repository != null) {
                    repository.setClickTakePhoto(false);
                }
                break;
            case R.id.take_photo_radio_button:
                if (FileUtils.getSdcardAvailableSize() < Constants.SDCARD_STORAGE_SIZE_DELETE) {
                    ToastUtils.showShortToast(R.string.device_storage_warn_tip1);
                    return;
                }
                if (TextUtils.isEmpty(DeviceUtils.getMacAddress())) {
                    ToastUtils.showShortToast(R.string.device_mac_address_empty);
                    return;
                }
                if (repository != null) {
                    repository.setClickTakePhoto(true);
                }
                break;
            default:
                break;
        }
    }

    @BindingAdapter({"imageBmp"})
    public static void loadTakeResultImage(ImageView imageView, String imgPath) {
        GlideUtils.loadPersonAdapterImage(imgPath, imageView);
    }

    @BindingAdapter("visibility")
    public static void bindVisibility(View view, boolean visible) {
        view.setVisibility(visible?View.VISIBLE:View.GONE);
    }
}
