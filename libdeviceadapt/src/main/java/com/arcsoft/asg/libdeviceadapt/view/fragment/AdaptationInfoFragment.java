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

package com.arcsoft.asg.libdeviceadapt.view.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.arcsoft.asg.libcamera.contract.ICamera;
import com.arcsoft.asg.libcamera.view.CameraFaceView;
import com.arcsoft.asg.libcommon.base.BaseLazyFragment;
import com.arcsoft.asg.libcommon.util.common.ActivityUtils;
import com.arcsoft.asg.libcommon.util.common.ArithmeticUtils;
import com.arcsoft.asg.libcommon.util.common.LogUtils;
import com.arcsoft.asg.libcommon.util.common.ScreenUtils;
import com.arcsoft.asg.libcommon.util.common.ToastUtils;
import com.arcsoft.asg.libcommon.util.common.Utils;
import com.arcsoft.asg.libdeviceadapt.R;
import com.arcsoft.asg.libdeviceadapt.bean.DeviceAdaptationInfo;
import com.arcsoft.asg.libdeviceadapt.bean.DeviceCameraInfo;
import com.arcsoft.asg.libdeviceadapt.databinding.FragmentAdaptationInfoBinding;
import com.arcsoft.asg.libdeviceadapt.view.seekbar.SettingsSeekBar;
import com.arcsoft.asg.libdeviceadapt.viewmodel.AdaptationInfoViewModel;
import com.google.gson.Gson;

public class AdaptationInfoFragment extends BaseLazyFragment implements IAdaptationInfoNavigator {

    public static final String KEY_INTENT_ADAPTATION_INFO = "KEY_INTENT_ADAPTATION_INFO";
    public static final String KEY_INTENT_DEVICE_CAMERA_INFO = "KEY_INTENT_DEVICE_CAMERA_INFO";
    public static final String KEY_INTENT_FROM_SPLASH = "KEY_INTENT_FROM_SPLASH";

    private static final int SEEK_BAR_MAX = 200;
    private static final int SEEK_BAR_MIN = -200;
    private static final int DEGREE_90 = 90;
    private static final int DEGREE_180 = 180;
    private static final int DEGREE_270 = 270;
    private static final int SEEK_BAR_STEP = 1;

    private FragmentAdaptationInfoBinding dataBinding;
    private AdaptationInfoViewModel viewModel;
    private DeviceAdaptationInfo deviceAdaptationInfo;
    private DeviceCameraInfo deviceCameraInfo;
    private CameraFaceView firstViceView;
    private CameraFaceView secondViceView;
    private boolean fromSplash;
    private AdaptationInfoCallback callback;

    /**
     * 获取硬件适配信息：一般在修改了硬件适配信息后，可以调用此方法获取到最新的数据
     *
     * @return 适配信息
     */
    public String getConfigInfo() {
        return viewModel.getConfigInfo();
    }

    /**
     * 创建Fragment实例
     *
     * @param configInfo 硬件适配信息
     * @param cameraInfo 相机信息对象
     * @param fromSplash 是否从启动页进入到此页面
     * @return
     */
    public static AdaptationInfoFragment getInstance(String configInfo, DeviceCameraInfo cameraInfo, boolean fromSplash) {
        AdaptationInfoFragment dialog = new AdaptationInfoFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_INTENT_ADAPTATION_INFO, configInfo);
        bundle.putSerializable(KEY_INTENT_DEVICE_CAMERA_INFO, cameraInfo);
        bundle.putBoolean(KEY_INTENT_FROM_SPLASH, fromSplash);
        dialog.setArguments(bundle);
        return dialog;
    }

    /**
     * @param callback
     */
    public void setCallback(AdaptationInfoCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (null != bundle) {
            String configInfo = bundle.getString(KEY_INTENT_ADAPTATION_INFO);
            if (!TextUtils.isEmpty(configInfo)) {
                try {
                    deviceAdaptationInfo = new Gson().fromJson(configInfo, DeviceAdaptationInfo.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            fromSplash = bundle.getBoolean(KEY_INTENT_FROM_SPLASH);
            deviceCameraInfo = (DeviceCameraInfo) bundle.getSerializable(KEY_INTENT_DEVICE_CAMERA_INFO);
            if (deviceCameraInfo == null) {
                LogUtils.e("AdaptationInfoFragment", "DeviceCameraInfo is invalid");
            }
        }
    }

    @Override
    protected void initPrepare() {
    }

    @Override
    protected View initView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        switchAutoSizeDp();
        viewModel = new AdaptationInfoViewModel(fromSplash);
        dataBinding = FragmentAdaptationInfoBinding.inflate(inflater);
        dataBinding.setViewModel(viewModel);
        viewModel.setNavigator(this);
        return dataBinding.getRoot();
    }

    @Override
    protected void onInvisible() {
        if (viewModel != null) {
            viewModel.onInvisible();
        }
    }

    @Override
    protected void onVisible() {
        if (viewModel != null) {
            viewModel.onVisible();
        }
    }

    @Override
    protected void initData() {
        if (deviceCameraInfo == null) {
            LogUtils.e("AdaptationInfoFragment", "DeviceCameraInfo is invalid");
        } else {
            viewModel.setAdaptationInfo(deviceAdaptationInfo, deviceCameraInfo);
        }
    }

    @Override
    public void setUiData(DeviceAdaptationInfo deviceAdaptationInfo) {
        viewModel.setCameraRatioSpinner(deviceAdaptationInfo, dataBinding.srSettingCameraPreviewRatio, firstViceView, secondViceView);
        viewModel.setCameraPositionSpinner(deviceAdaptationInfo, dataBinding.srPreviewFrameCamera);
        viewModel.setFaceDetectDegreeSpinner(deviceAdaptationInfo, dataBinding.srPreviewFaceDetectDegree);

        SettingsSeekBar topSeekBar = dataBinding.seekBarSettingsHorizontalDisplacement;
        SettingsSeekBar bottomSeekBar = dataBinding.seekBarSettingsVerticalDisplacement;
        topSeekBar.setBarHeight(getResources().getDimension(R.dimen.y2));
        bottomSeekBar.setBarHeight(getResources().getDimension(R.dimen.y2));
        topSeekBar.setSteps(SEEK_BAR_STEP);
        topSeekBar.setMaxValue(SEEK_BAR_MAX);
        topSeekBar.setMinValue(SEEK_BAR_MIN);
        bottomSeekBar.setSteps(SEEK_BAR_STEP);
        bottomSeekBar.setMaxValue(SEEK_BAR_MAX);
        bottomSeekBar.setMinValue(SEEK_BAR_MIN);
        topSeekBar.setMinStartValue(viewModel.getHorizontalDisplacement());
        topSeekBar.apply();
        bottomSeekBar.setMinStartValue(viewModel.getVerticalDisplacement());
        bottomSeekBar.apply();
        if (deviceAdaptationInfo.getCameraCount() > 1) {
            topSeekBar.setOnSeekBarChangeListener(value -> {
                viewModel.setHorizontalDisplace(value.intValue());
            });
            bottomSeekBar.setOnSeekBarChangeListener(value -> {
                viewModel.setVerticalDisplace(value.intValue());
            });
        }
    }

    @Override
    public void setCamera(DeviceAdaptationInfo deviceAdaptationInfo, ICamera camera1, ICamera camera2,
                          boolean init, boolean previewRatio) {
        CameraFaceView cameraFaceViewFirst = dataBinding.adaptationInfoCameraFaceViewFirst;
        if (camera1 == null) {
            cameraFaceViewFirst.setVisibility(View.GONE);
        } else {
            changeMainCameraUi(deviceAdaptationInfo, cameraFaceViewFirst, dataBinding.viewMainPreviewBorder, previewRatio);
            addFirstViceViewAndBind(deviceAdaptationInfo, cameraFaceViewFirst, init, previewRatio);
        }

        CameraFaceView cameraFaceViewSecond = dataBinding.adaptationInfoCameraFaceViewSecond;
        if (camera2 == null) {
            cameraFaceViewSecond.setVisibility(View.GONE);
        } else {
            changeSecondCameraUi(deviceAdaptationInfo, cameraFaceViewSecond, dataBinding.viewSecondPreviewBorder, previewRatio);
            addSecondViceViewAndBind(deviceAdaptationInfo, cameraFaceViewSecond, init, previewRatio);
        }
    }

    @Override
    public void onSecondCameraOpened() {
        if (callback != null) {
            callback.onSecondCameraOpened();
        }
    }

    private void changeMainCameraUi(DeviceAdaptationInfo deviceAdaptationInfo, CameraFaceView cameraFaceView, View previewBorder,
                                    boolean previewRatio) {
        FrameLayout.LayoutParams textureParams = (FrameLayout.LayoutParams) cameraFaceView.getLayoutParams();
        FrameLayout.LayoutParams borderParams = (FrameLayout.LayoutParams) previewBorder.getLayoutParams();
        int oldTextureParamW = textureParams.width;
        int oldTextureParamH = textureParams.height;
        int previewW = deviceAdaptationInfo.getPreviewWidth();
        int previewH = deviceAdaptationInfo.getPreviewHeight();
        int screenWidth = ScreenUtils.getScreenWidth();
        if (previewW > 0 && previewH > 0) {
            double sizeRatio = ArithmeticUtils.div(previewW, previewH);
            int cameraWidth = 0;
            int cameraHeight = 0;
            int borderWidth;
            if (ScreenUtils.isPortrait()) {
                borderWidth = Utils.getApp().getResources().getDimensionPixelOffset(R.dimen.x60);
                cameraWidth = screenWidth - borderWidth;
                cameraHeight = (int) ArithmeticUtils.div(cameraWidth, sizeRatio);
            }
            if (ScreenUtils.isLandscape()) {
                cameraWidth = Utils.getApp().getResources().getDimensionPixelOffset(R.dimen.x705);
                cameraHeight = (int) ArithmeticUtils.div(cameraWidth, sizeRatio);
            }
            textureParams.gravity = Gravity.CENTER_HORIZONTAL;
            borderParams.gravity = Gravity.CENTER_HORIZONTAL;
            if (deviceAdaptationInfo.isMainCameraChangeWidthHeight()) {
                //宽高互换
                if (cameraWidth >= cameraHeight) {
                    textureParams.width = cameraHeight;
                    textureParams.height = cameraWidth;
                    borderParams.width = cameraHeight;
                    borderParams.height = cameraWidth;
                } else {
                    if (ScreenUtils.isPortrait()) {
                        int width = cameraWidth - Utils.getApp().getResources().getDimensionPixelOffset(R.dimen.x200);
                        int height = (int) ArithmeticUtils.div(width, sizeRatio);
                        textureParams.width = width;
                        textureParams.height = height;
                        borderParams.width = width;
                        borderParams.height = height;
                    }
                    if (ScreenUtils.isLandscape()) {
                        textureParams.width = cameraWidth;
                        int height = (int) ArithmeticUtils.mul(cameraWidth, sizeRatio);
                        textureParams.height = height;
                        borderParams.width = cameraWidth;
                        borderParams.height = height;
                    }
                }
            } else {
                //宽高不互换
                if (cameraWidth >= cameraHeight) {
                    textureParams.width = cameraWidth;
                    textureParams.height = cameraHeight;
                    borderParams.width = cameraWidth;
                    borderParams.height = cameraHeight;
                } else {
                    if (ScreenUtils.isPortrait()) {
                        textureParams.width = cameraWidth;
                        int height = (int) ArithmeticUtils.mul(cameraWidth, sizeRatio);
                        textureParams.height = height;
                        borderParams.width = cameraWidth;
                        borderParams.height = height;
                    }
                    if (ScreenUtils.isLandscape()) {
                        int width = Utils.getApp().getResources().getDimensionPixelOffset(R.dimen.x505);
                        int height = (int) ArithmeticUtils.div(width, sizeRatio);
                        textureParams.width = width;
                        textureParams.height = height;
                        borderParams.width = width;
                        borderParams.height = height;
                    }
                }
            }
            if (previewRatio) {
                textureParams.width = textureParams.width - 1;
                textureParams.height = textureParams.height - 1;
                if (textureParams.width == oldTextureParamW) {
                    textureParams.width = textureParams.width - 1;
                }
                if (textureParams.height == oldTextureParamH) {
                    textureParams.height = textureParams.height - 1;
                }
                borderParams.width = borderParams.width - 1;
                borderParams.height = borderParams.height - 1;
            }
        }
        cameraFaceView.setLayoutParams(textureParams);
        previewBorder.setLayoutParams(borderParams);
    }

    private void addFirstViceViewAndBind(DeviceAdaptationInfo deviceAdaptationInfo, CameraFaceView cameraFaceView,
                                         boolean init, boolean previewRatio) {
        int degree = deviceAdaptationInfo.getLeftGlSurfaceViewRotation();
        int sizeWidth = deviceAdaptationInfo.getPreviewWidth();
        int sizeHeight = deviceAdaptationInfo.getPreviewHeight();
        if (sizeWidth > 0 && sizeHeight > 0) {
            double sizeRatio = ArithmeticUtils.div(sizeWidth, sizeHeight);
            int viewWidth;
            int viewHeight;
            FrameLayout frameLayout = dataBinding.flGlSurfaceViewSettingLeft;
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) frameLayout.getLayoutParams();
            if (degree == DEGREE_90 || degree == DEGREE_270) {
                viewHeight = Utils.getApp().getResources().getDimensionPixelOffset(R.dimen.x240);
                viewWidth = (int) ArithmeticUtils.div(viewHeight, sizeRatio);
            } else {
                if (ScreenUtils.isPortrait()) {
                    viewWidth = (ScreenUtils.getScreenWidth() -
                            Utils.getApp().getResources().getDimensionPixelOffset(R.dimen.x100)) / 2;
                    viewHeight = (int) ArithmeticUtils.div(viewWidth, sizeRatio);
                } else {
                    viewWidth = Utils.getApp().getResources().getDimensionPixelOffset(R.dimen.x602);
                    viewHeight = (int) ArithmeticUtils.div(viewWidth, sizeRatio);
                }
            }
            layoutParams.width = viewWidth;
            layoutParams.height = viewHeight;
            frameLayout.setLayoutParams(layoutParams);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(viewWidth, viewHeight);
            if (init) {
                firstViceView = new CameraFaceView(ActivityUtils.getTopActivity());
                frameLayout.addView(firstViceView, params);
                viewModel.bindCamera(true, cameraFaceView, firstViceView);
            } else {
                if (previewRatio) {
                    params.width = params.width - 1;
                    params.height = params.height - 1;
                    FrameLayout.LayoutParams viceParam = (FrameLayout.LayoutParams) firstViceView.getLayoutParams();
                    int oldTextureParamW = viceParam.width;
                    int oldTextureParamH = viceParam.height;
                    if (params.width == oldTextureParamW) {
                        params.width = params.width - 1;
                    }
                    if (params.height == oldTextureParamH) {
                        params.height = params.height - 1;
                    }
                }
                firstViceView.setLayoutParams(params);
            }
        }
    }

    private void addSecondViceViewAndBind(DeviceAdaptationInfo deviceAdaptationInfo, CameraFaceView cameraFaceView,
                                          boolean init, boolean previewRatio) {
        int degreeLeft = deviceAdaptationInfo.getLeftGlSurfaceViewRotation();
        int degree = deviceAdaptationInfo.getRightGlSurfaceViewRotation();
        FrameLayout frameLayout = dataBinding.flGlSurfaceViewSettingRight;
        FrameLayout leftFrameLayout = dataBinding.flGlSurfaceViewSettingLeft;
        RelativeLayout.LayoutParams leftParams = (RelativeLayout.LayoutParams) leftFrameLayout.getLayoutParams();
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) frameLayout.getLayoutParams();
        int subDegree = Math.abs(degree - degreeLeft) % DEGREE_180;
        if (subDegree == 0) {
            layoutParams.width = leftParams.width;
            layoutParams.height = leftParams.height;
        } else {
            layoutParams.width = leftParams.height;
            layoutParams.height = leftParams.width;
        }
        frameLayout.setLayoutParams(layoutParams);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(layoutParams.width, layoutParams.height);
        if (init) {
            secondViceView = new CameraFaceView(ActivityUtils.getTopActivity());
            frameLayout.addView(secondViceView, params);
            viewModel.bindCamera(false, cameraFaceView, secondViceView);
        } else {
            if (previewRatio) {
                params.width = params.width - 1;
                params.height = params.height - 1;
                FrameLayout.LayoutParams viceParam = (FrameLayout.LayoutParams) secondViceView.getLayoutParams();
                int oldTextureParamW = viceParam.width;
                int oldTextureParamH = viceParam.height;
                if (params.width == oldTextureParamW) {
                    params.width = params.width - 1;
                }
                if (params.height == oldTextureParamH) {
                    params.height = params.height - 1;
                }
            }
            secondViceView.setLayoutParams(params);
        }
    }

    private void changeSecondCameraUi(DeviceAdaptationInfo deviceAdaptationInfo, CameraFaceView cameraFaceView,
                                      View previewBorder, boolean previewRatio) {
        int screenWidth = ScreenUtils.getScreenWidth();
        int borderWidth = Utils.getApp().getResources().getDimensionPixelOffset(R.dimen.x60);
        int cameraWidth = screenWidth - borderWidth;
        int previewW = deviceAdaptationInfo.getPreviewWidth();
        int previewH = deviceAdaptationInfo.getPreviewHeight();
        if (previewW > 0 && previewH > 0) {
            double sizeRatio = ArithmeticUtils.div(previewW, previewH);
            int cameraHeight = (int) ArithmeticUtils.div(cameraWidth, sizeRatio);
            FrameLayout.LayoutParams textureParams = (FrameLayout.LayoutParams) cameraFaceView.getLayoutParams();
            FrameLayout.LayoutParams borderParams = (FrameLayout.LayoutParams) previewBorder.getLayoutParams();
            int oldTextureParamW = textureParams.width;
            int oldTextureParamH = textureParams.height;
            if (deviceAdaptationInfo.isSecondCameraChangeWidthHeight()) {
                textureParams.width = cameraHeight * 3 / 10;
                textureParams.height = cameraWidth * 3 / 10;
                borderParams.width = cameraHeight * 3 / 10;
                borderParams.height = cameraWidth * 3 / 10;
            } else {
                textureParams.width = cameraWidth * 3 / 10;
                textureParams.height = cameraHeight * 3 / 10;
                borderParams.width = cameraWidth * 3 / 10;
                borderParams.height = cameraHeight * 3 / 10;
            }
            if (previewRatio) {
                textureParams.width = textureParams.width - 1;
                textureParams.height = textureParams.height - 1;
                if (textureParams.width == oldTextureParamW) {
                    textureParams.width = textureParams.width - 1;
                }
                if (textureParams.height == oldTextureParamH) {
                    textureParams.height = textureParams.height - 1;
                }
                borderParams.width = borderParams.width - 1;
                borderParams.height = borderParams.height - 1;
            }
            cameraFaceView.setLayoutParams(textureParams);
            previewBorder.setLayoutParams(borderParams);
        }
    }

    @Override
    public void changeSecondViceViewWidthHeight() {
        if (secondViceView != null) {
            FrameLayout frameLayout = dataBinding.flGlSurfaceViewSettingRight;
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) frameLayout.getLayoutParams();
            int temp = params.height;
            params.height = params.width;
            params.width = temp;
            frameLayout.setLayoutParams(params);
            FrameLayout.LayoutParams secondParams = new FrameLayout.LayoutParams(params.width, params.height);
            secondViceView.setLayoutParams(secondParams);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.onActivityResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (viewModel != null) {
            viewModel.onActivityPause();
        }
    }

    @Override
    public void onDestroy() {
        if (viewModel != null) {
            viewModel.onActivityDestroy();
        }
        super.onDestroy();
    }

    @Override
    public void setHorizontalDisplace(int value) {
        SettingsSeekBar seekBar = dataBinding.seekBarSettingsHorizontalDisplacement;
        seekBar.setMinStartValue(value);
        seekBar.apply();
    }

    @Override
    public void setVerticalDisplace(int value) {
        SettingsSeekBar seekBar = dataBinding.seekBarSettingsVerticalDisplacement;
        seekBar.setMinStartValue(value);
        seekBar.apply();
    }

    @Override
    public void onCameraOpenError(boolean mainCamera, String msg) {
        CameraFaceView cameraFaceView = dataBinding.adaptationInfoCameraFaceViewSecond;
        cameraFaceView.getCameraView().setVisibility(View.INVISIBLE);
        cameraFaceView.getFaceRectView().setVisibility(View.INVISIBLE);
        if (secondViceView != null) {
            secondViceView.getCameraView().setVisibility(View.INVISIBLE);
            secondViceView.getFaceRectView().setVisibility(View.INVISIBLE);
        }
        ToastUtils.showShortToast(getResources().getString(R.string.camera_open_fail));
    }
}
