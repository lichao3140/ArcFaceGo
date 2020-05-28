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

package com.arcsoft.arcfacesingle.view.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.databinding.DataBindingUtil;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.base.BaseBusinessActivity;
import com.arcsoft.arcfacesingle.business.common.CommonRepository;
import com.arcsoft.arcfacesingle.databinding.ActivityTakePhotoBinding;
import com.arcsoft.arcfacesingle.navigator.TakePhotoNavigator;
import com.arcsoft.arcfacesingle.viewmodel.TakePhotoViewModel;
import com.arcsoft.asg.libcamera.view.CameraFaceView;
import com.arcsoft.asg.libcommon.util.common.ArithmeticUtils;
import com.arcsoft.asg.libcommon.util.common.PermissionUtils;
import com.arcsoft.asg.libcommon.util.common.ScreenUtils;
import com.arcsoft.asg.libcommon.util.common.Utils;
import com.arcsoft.asg.libdeviceadapt.bean.DeviceAdaptationInfo;

import java.util.List;

public class TakePhotoActivity extends BaseBusinessActivity implements TakePhotoNavigator {

    public static final String KEY_TAKE_PHOTO_IMAGE_PATH = "KEY_TAKE_PHOTO_IMAGE_PATH";
    public static final String KEY_TAKE_PHOTO_PERSON_SERIAL = "KEY_TAKE_PHOTO_PERSON_SERIAL";
    private static final double TEXTURE_PARAM_RATIO = 0.85;
    private static final String[] CAMERA_PERMISSION = new String[]{
            Manifest.permission.CAMERA
    };

    private TakePhotoViewModel viewModel;
    private ActivityTakePhotoBinding dataBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_take_photo);
        viewModel = new TakePhotoViewModel();
        viewModel.setNavigator(this);
        dataBinding.setViewModel(viewModel);
        dataBinding.customTopBar.setVisibleClose(true);
        initPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.onActivityResume();
        }
    }

    private void initPermissions() {
        PermissionUtils.permission(CAMERA_PERMISSION)
                .callback(new PermissionUtils.FullCallback() {
                    @Override
                    public void onGranted(List<String> permissionsGrantedList) {
                        setCameraViewParams();
                    }

                    @Override
                    public void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied) {
                        finish();
                    }
                })
                .request();
    }

    @Override
    protected void onPause() {
        if (viewModel != null) {
            viewModel.onActivityPause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (viewModel != null) {
            viewModel.onActivityDestroyed();
        }
        viewModel = null;
        dataBinding = null;
        super.onDestroy();
    }

    private void setCameraViewParams() {
        DeviceAdaptationInfo adaptationInfo = CommonRepository.getInstance().getAdaptationInfo();
        double sizeRatio = ArithmeticUtils.div(adaptationInfo.getPreviewWidth(), adaptationInfo.getPreviewHeight());
        int screenHeight = ScreenUtils.getScreenHeight();
        int screenWidth = ScreenUtils.getScreenWidth();
        int cameraWidth;
        int cameraHeight;
        if (adaptationInfo.isMainCameraChangeWidthHeight()) {
            if (ScreenUtils.isPortrait()) {
                cameraHeight = screenHeight - Utils.getApp().getResources().getDimensionPixelOffset(R.dimen.x170);
                if (sizeRatio >= 1) {
                    cameraWidth = (int) ArithmeticUtils.div(cameraHeight, sizeRatio);
                    if (cameraWidth >= (screenWidth - Utils.getApp().getResources().getDimensionPixelOffset(R.dimen.x60))) {
                        cameraWidth = screenWidth - Utils.getApp().getResources().getDimensionPixelOffset(R.dimen.x60);
                        cameraHeight = (int) ArithmeticUtils.mul(cameraWidth, sizeRatio);
                    }
                } else {
                    cameraWidth = screenWidth - Utils.getApp().getResources().getDimensionPixelOffset(R.dimen.x100);
                    cameraHeight = (int) ArithmeticUtils.div(cameraWidth, sizeRatio);
                    if (cameraHeight > screenHeight - Utils.getApp().getResources().getDimensionPixelOffset(R.dimen.x170)) {
                        cameraHeight = screenHeight - Utils.getApp().getResources().getDimensionPixelOffset(R.dimen.x170);
                        cameraWidth = (int) ArithmeticUtils.mul(cameraHeight, sizeRatio);
                    }
                }
            } else {
                cameraHeight = screenHeight - Utils.getApp().getResources().getDimensionPixelOffset(R.dimen.x170);
                cameraWidth = (int) ArithmeticUtils.div(cameraHeight, sizeRatio);
            }
        } else {
            if (ScreenUtils.isPortrait()) {
                cameraWidth = screenWidth - Utils.getApp().getResources().getDimensionPixelOffset(R.dimen.x60);
                if (sizeRatio >= 1) {
                    cameraHeight = (int) ArithmeticUtils.div(cameraWidth, sizeRatio);
                } else {
                    cameraHeight = (int) ArithmeticUtils.mul(cameraWidth, sizeRatio);
                }
            } else {
                cameraHeight = screenHeight - Utils.getApp().getResources().getDimensionPixelOffset(R.dimen.x170);
                cameraWidth = (int) ArithmeticUtils.mul(cameraHeight, sizeRatio);
            }
        }
        cameraWidth = (int) (cameraWidth * TEXTURE_PARAM_RATIO);
        cameraHeight = (int) (cameraHeight * TEXTURE_PARAM_RATIO);

        CameraFaceView cameraFaceView = dataBinding.cameraFaceRectView;
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) cameraFaceView.getLayoutParams();
        params.gravity = Gravity.CENTER;
        params.width = cameraWidth;
        params.height = cameraHeight;
        cameraFaceView.setLayoutParams(params);
        viewModel.bindCamera(cameraFaceView);
    }

    @Override
    public void setPhotoResultParams(int width, int height) {
        ImageView ivTakeResult = dataBinding.ivTakePhotoResult;
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) ivTakeResult.getLayoutParams();
        int screenWidth = ScreenUtils.getScreenWidth();
        int screenHeight = ScreenUtils.getScreenHeight();
        double ratio = ArithmeticUtils.div(width, height);
        double sizeRatio = 0.7;
        if (ScreenUtils.isLandscape()) {
            sizeRatio = 0.6;
        }
        if (ratio >= 1) {
            width = (int) (screenWidth * sizeRatio);
            height = (int) ArithmeticUtils.div(width, ratio);
        } else {
            if (ScreenUtils.isLandscape()) {
                height = (int) (screenHeight * sizeRatio);
                width = (int) ArithmeticUtils.mul(height, ratio);
            }
            if (ScreenUtils.isPortrait()) {
                width = (int) (screenWidth * sizeRatio);
                height = (int) ArithmeticUtils.div(width, ratio);
            }
        }
        params.width = width;
        params.height = height;
        params.gravity = Gravity.CENTER;
        ivTakeResult.setLayoutParams(params);
    }

    @Override
    public void confirmTakePhoto(String imagePath, String personSerial) {
        Intent intent = new Intent(this, PersonListActivity.class);
        intent.putExtra(KEY_TAKE_PHOTO_IMAGE_PATH, imagePath);
        intent.putExtra(KEY_TAKE_PHOTO_PERSON_SERIAL, personSerial);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void setCameraFaceViceVisible(boolean visible) {
        CameraFaceView cameraFaceView = dataBinding.cameraFaceRectView;
        cameraFaceView.getCameraView().setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }
}
