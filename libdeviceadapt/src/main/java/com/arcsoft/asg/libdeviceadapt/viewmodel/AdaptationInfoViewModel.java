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

package com.arcsoft.asg.libdeviceadapt.viewmodel;

import android.hardware.Camera;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.BindingAdapter;
import androidx.databinding.ObservableField;

import com.arcsoft.asg.libcamera.view.CameraFaceView;
import com.arcsoft.asg.libcommon.util.business.DoubleClickUtils;
import com.arcsoft.asg.libcommon.util.common.ActivityUtils;
import com.arcsoft.asg.libcommon.util.common.StringUtils;
import com.arcsoft.asg.libcommon.util.common.Utils;
import com.arcsoft.asg.libcommon.util.common.ViewUtils;
import com.arcsoft.asg.libdeviceadapt.R;
import com.arcsoft.asg.libdeviceadapt.bean.DeviceAdaptationInfo;
import com.arcsoft.asg.libdeviceadapt.bean.DeviceCameraInfo;
import com.arcsoft.asg.libdeviceadapt.repos.AdaptationInfoRepository;
import com.arcsoft.asg.libdeviceadapt.repos.IAdaptationInfo;
import com.arcsoft.asg.libdeviceadapt.view.fragment.IAdaptationInfoNavigator;
import com.arcsoft.asg.libdeviceadapt.view.seekbar.SettingsSeekBar;

import java.util.List;

public class AdaptationInfoViewModel extends BaseObservable {

    private boolean firstInit = true;
    private boolean firstInit2 = true;
    private boolean firstInit3 = true;

    private AdaptationInfoRepository repository;
    private IAdaptationInfoNavigator navigator;

    @Bindable
    public final ObservableField<String> previewAlreadyRotation = new ObservableField<>();
    public final ObservableField<String> previewLeftAlreadyRotation = new ObservableField<>();
    public final ObservableField<String> previewRightAlreadyRotation = new ObservableField<>();
    public final ObservableField<Boolean> faceRectHorizontalMirror = new ObservableField<>();
    public final ObservableField<Boolean> faceRectVerticalMirror = new ObservableField<>();
    public final ObservableField<String> strHorizontalDisplace = new ObservableField<>();
    public final ObservableField<String> strVerticalDisplace = new ObservableField<>();
    public final ObservableField<Boolean> mainPreviewBorderVisible = new ObservableField<>();
    public final ObservableField<Boolean> secondPreviewBorderVisible = new ObservableField<>();
    public final ObservableField<Boolean> secondCameraEnable = new ObservableField<>();

    public AdaptationInfoViewModel(boolean fromSplash) {
        IAdaptationInfo adaptationInfoCallback = new IAdaptationInfo() {

            @Override
            public void setSeekBarDisplace(int x, int y) {
                if (navigator != null) {
                    navigator.setHorizontalDisplace(x);
                    navigator.setVerticalDisplace(y);
                }
            }

            @Override
            public void onSecondCameraOpened() {
                if (navigator != null) {
                    navigator.onSecondCameraOpened();
                }
            }

            @Override
            public void onCameraOpenError(boolean mainCamera, Exception e) {
                if (!mainCamera) {
                    secondCameraEnable.set(false);
                }
                if (navigator != null) {
                    navigator.onCameraOpenError(mainCamera, e.getMessage());
                }
            }
        };
        repository = new AdaptationInfoRepository(adaptationInfoCallback);
        repository.setFromSplash(fromSplash);
    }

    public void setNavigator(IAdaptationInfoNavigator navigator) {
        this.navigator = navigator;
    }

    public void setAdaptationInfo(DeviceAdaptationInfo deviceInfo, DeviceCameraInfo cameraInfo) {
        if (deviceInfo == null) {
            DeviceAdaptationInfo adaptInfo = repository.getNewDeviceAdaptationInfo(cameraInfo);
            refreshUiData(adaptInfo);
        } else {
            repository.setDeviceAdaptationInfo(deviceInfo, cameraInfo);
            refreshUiData(deviceInfo);
        }
    }

    private void refreshUiData(DeviceAdaptationInfo deviceAdaptationInfo) {
        setObservableField(deviceAdaptationInfo);
        navigator.setCamera(deviceAdaptationInfo, repository.getCameraManage1(), repository.getCameraManage2(),
                true, false);
        navigator.setUiData(deviceAdaptationInfo);
    }

    private void setObservableField(DeviceAdaptationInfo deviceAdaptationInfo) {
        if (repository.isSelectMainPreview()) {
            previewAlreadyRotation.set(StringUtils.getStrFromRes(R.string.already_rotation_degree,
                    repository.getFirstAdditionalRotation()));
            faceRectHorizontalMirror.set(deviceAdaptationInfo.isRectHorizontalMirror());
            faceRectVerticalMirror.set(deviceAdaptationInfo.isRectVerticalMirror());
        } else {
            previewAlreadyRotation.set(StringUtils.getStrFromRes(R.string.already_rotation_degree,
                    repository.getSecondAdditionalRotation()));
            faceRectHorizontalMirror.set(deviceAdaptationInfo.isSecondRectHorizontalMirror());
            faceRectVerticalMirror.set(deviceAdaptationInfo.isSecondRectVerticalMirror());
        }
        previewLeftAlreadyRotation.set(StringUtils.getStrFromRes(R.string.already_rotation_degree,
                repository.getLeftSurfaceAdditionalRotation()));
        previewRightAlreadyRotation.set(StringUtils.getStrFromRes(R.string.already_rotation_degree,
                repository.getRightSurfaceAdditionalRotation()));
        strHorizontalDisplace.set(String.valueOf(repository.getHorizontalDisplacement()));
        strVerticalDisplace.set(String.valueOf(repository.getVerticalDisplacement()));
        mainPreviewBorderVisible.set(repository.isSelectMainPreview());
        secondPreviewBorderVisible.set(!repository.isSelectMainPreview());
        if (deviceAdaptationInfo.getCameraCount() <= 1) {
            secondCameraEnable.set(false);
        } else {
            secondCameraEnable.set(true);
        }
    }

    public void bindCamera(boolean mainCamera, CameraFaceView cameraFaceView1, CameraFaceView cameraFaceView2) {
        if (repository != null) {
            repository.bindCamera(mainCamera, cameraFaceView1, cameraFaceView2);
        }
    }

    public void setHorizontalDisplace(int value) {
        strHorizontalDisplace.set(String.valueOf(value));
        repository.setHorizontalDisplace(value);
    }

    public void setVerticalDisplace(int value) {
        strVerticalDisplace.set(String.valueOf(value));
        repository.setVerticalDisplace(value);
    }

    public int getHorizontalDisplacement() {
        return repository.getHorizontalDisplacement();
    }

    public int getVerticalDisplacement() {
        return repository.getVerticalDisplacement();
    }

    public void setCameraRatioSpinner(DeviceAdaptationInfo deviceAdaptationInfo, Spinner spinner,
                                      CameraFaceView firstViceView, CameraFaceView secondViceView) {
        List<String> ratioList = getCameraPreviewRatioList();
        ArrayAdapter<String> ratioAdapter = new ArrayAdapter<>(Utils.getApp(), R.layout.custom_spinner_item, ratioList);
        ratioAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        spinner.setAdapter(ratioAdapter);
        AdapterView.OnItemSelectedListener selectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (firstInit) {
                    firstInit = false;
                } else {
                    selectPreviewRatio(ratioList.get(position), firstViceView, secondViceView);
                    ViewUtils.hideNavigator(ActivityUtils.getTopActivity().getWindow());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        spinner.setOnItemSelectedListener(selectedListener);
        String localRatio = deviceAdaptationInfo.getPreviewWidth() + "*" + deviceAdaptationInfo.getPreviewHeight();
        spinner.setSelection(ratioList.indexOf(localRatio));
    }

    public void setCameraPositionSpinner(DeviceAdaptationInfo deviceAdaptationInfo, Spinner spinner) {
        List<String> cameraItems = getCameraPositionList();
        ArrayAdapter<String> cameraAdapter = new ArrayAdapter<>(Utils.getApp(), R.layout.custom_spinner_item, cameraItems);
        cameraAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        spinner.setAdapter(cameraAdapter);
        AdapterView.OnItemSelectedListener cameraSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (firstInit2) {
                    firstInit2 = false;
                } else {
                    selectCameraPosition(cameraItems, position);
                    ViewUtils.hideNavigator(ActivityUtils.getTopActivity().getWindow());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        spinner.setOnItemSelectedListener(cameraSelectedListener);
        int selection = cameraItems.indexOf(String.valueOf(deviceAdaptationInfo.getMainCameraId()));
        spinner.setSelection(selection);
    }

    public void setFaceDetectDegreeSpinner(DeviceAdaptationInfo deviceAdaptationInfo, Spinner spinner) {
        List<String> degreeItems = getFaceDetectDegreeList();
        ArrayAdapter<String> degreeAdapter = new ArrayAdapter<>(Utils.getApp(), R.layout.custom_spinner_item, degreeItems);
        degreeAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        spinner.setAdapter(degreeAdapter);
        AdapterView.OnItemSelectedListener degreeListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (firstInit3) {
                    firstInit3 = false;
                } else {
                    selectFaceDetectDegree(degreeItems.get(position));
                    ViewUtils.hideNavigator(ActivityUtils.getTopActivity().getWindow());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        spinner.setOnItemSelectedListener(degreeListener);
        int selection = degreeItems.indexOf(deviceAdaptationInfo.getFaceDetectDegree());
        spinner.setSelection(selection);
    }

    private List<String> getCameraPreviewRatioList() {
        return repository.getCameraRatioList();
    }

    private List<String> getCameraPositionList() {
        return repository.getCameraPositionList();
    }

    private List<String> getFaceDetectDegreeList() {
        return repository.getFaceDetectDegreeList();
    }

    /**
     * 切换相机分辨率
     *
     * @param string
     */
    private void selectPreviewRatio(String string, CameraFaceView firstViceView, CameraFaceView secondViceView) {
        if (repository != null) {
            repository.stopAllCamera();
            boolean previewRatio = repository.previewRatio(string);
            repository.selectPreviewRatio(string, firstViceView, secondViceView);
            navigator.setCamera(repository.getDeviceAdaptationInfo(), repository.getCameraManage1(),
                    repository.getCameraManage2(), false, previewRatio);
            repository.startAllCamera();
        }
    }

    /**
     * 切换前后置相机
     *
     * @param positionList
     * @param position
     */
    private void selectCameraPosition(List<String> positionList, int position) {
        int mainCameraId = Integer.parseInt(positionList.get(position));
        if (positionList.size() > 1) {
            int secondCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
            if (mainCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                secondCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
            }
            if (repository != null) {
                repository.selectCameraId(mainCameraId, secondCameraId);
            }
        }
    }

    /**
     * 切换人脸检测角度
     *
     * @param degree
     */
    private void selectFaceDetectDegree(String degree) {
        if (repository != null) {
            repository.selectFaceDetectDegree(degree);
        }
    }

    public void onInvisible() {
        if (repository != null) {
            repository.onInvisible();
        }
    }

    public void onVisible() {
        if (repository != null) {
            repository.onVisible();
        }
    }

    public void onActivityPause() {
        if (repository != null) {
            repository.onViewPause();
        }
    }

    public void onActivityResume() {
        if (repository != null) {
            repository.onViewResume();
        }
    }

    public void onActivityDestroy() {
        if (repository != null) {
            repository.onViewDestroy();
        }
    }

    public String getConfigInfo() {
        return repository.getConfigInfo();
    }

    public void onClick(View view) {
        int resId = view.getId();
        if (DoubleClickUtils.isFastDoubleClick(resId)) {
            return;
        }
        if (resId == R.id.btn_rotate_90_degrees) {
            if (repository != null) {
                repository.setAdditionalRotation();
                previewAlreadyRotation.set(StringUtils.getStrFromRes(R.string.already_rotation_degree, repository.isSelectMainPreview() ?
                        repository.getFirstAdditionalRotation() : repository.getSecondAdditionalRotation()));
            }
        } else if (resId == R.id.btn_width_height_change) {
            if (repository != null) {
                if (repository.changeWidthHeight()) {
                    navigator.setCamera(repository.getDeviceAdaptationInfo(), repository.getCameraManage1(),
                            repository.getCameraManage2(), false, false);
                }
            }
        } else if (resId == R.id.btn_mirror_image) {
            if (repository != null) {
                repository.setPreviewMirror();
            }
        } else if (resId == R.id.cb_face_rect_horizontal_mirror) {
            Boolean mirror = faceRectHorizontalMirror.get();
            if (mirror != null) {
                boolean mirrorValue = !mirror;
                faceRectHorizontalMirror.set(mirrorValue);
                if (repository != null) {
                    repository.setPreviewHorizontalMirror(mirrorValue);
                }
            }
        } else if (resId == R.id.cb_face_rect_vertical_mirror) {
            Boolean vMirror = faceRectVerticalMirror.get();
            if (vMirror != null) {
                boolean mirrorValue = !vMirror;
                faceRectVerticalMirror.set(mirrorValue);
                if (repository != null) {
                    repository.setPreviewVerticalMirror(mirrorValue);
                }
            }
        } else if (resId == R.id.btn_gl_surface_view_right_rotate_90_degrees) {
            if (repository != null) {
                if (repository.changeGlSurfaceViewRightDegree()) {
                    navigator.changeSecondViceViewWidthHeight();
                    previewRightAlreadyRotation.set(StringUtils.getStrFromRes(R.string.already_rotation_degree,
                            repository.getRightSurfaceAdditionalRotation()));
                }
            }
        } else if (resId == R.id.btn_gl_surface_view_right_mirror_image) {
            if (repository != null) {
                repository.changeGlSurfaceViewRightMirror();
            }
        } else if (resId == R.id.adaptation_info_camera_face_view_first) {
            mainPreviewBorderVisible.set(true);
            secondPreviewBorderVisible.set(false);
            previewAlreadyRotation.set(StringUtils.getStrFromRes(R.string.already_rotation_degree, repository.getFirstAdditionalRotation()));
            faceRectHorizontalMirror.set(repository.getDeviceAdaptationInfo().isRectHorizontalMirror());
            faceRectVerticalMirror.set(repository.getDeviceAdaptationInfo().isRectVerticalMirror());
            repository.setSelectMainPreview(true);
        } else if (resId == R.id.adaptation_info_camera_face_view_second) {
            mainPreviewBorderVisible.set(false);
            secondPreviewBorderVisible.set(true);
            previewAlreadyRotation.set(StringUtils.getStrFromRes(R.string.already_rotation_degree, repository.getSecondAdditionalRotation()));
            faceRectHorizontalMirror.set(repository.getDeviceAdaptationInfo().isSecondRectHorizontalMirror());
            faceRectVerticalMirror.set(repository.getDeviceAdaptationInfo().isSecondRectVerticalMirror());
            repository.setSelectMainPreview(false);
        }
    }

    @BindingAdapter("seekBarEnable")
    public static void setSeekBarEnable(SettingsSeekBar settingsSeekBar, boolean enable) {
        if (settingsSeekBar != null) {
            settingsSeekBar.setSeekBarTouchEnabled(enable);
        }
    }
}
