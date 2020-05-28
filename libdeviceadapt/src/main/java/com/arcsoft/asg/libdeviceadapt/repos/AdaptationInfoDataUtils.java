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

package com.arcsoft.asg.libdeviceadapt.repos;

import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.arcsoft.asg.libcamera.bean.CameraViewParam;
import com.arcsoft.asg.libcamera.constant.CameraViewConstants;
import com.arcsoft.asg.libcommon.util.common.Utils;
import com.arcsoft.asg.libdeviceadapt.R;
import com.arcsoft.asg.libdeviceadapt.bean.DeviceAdaptationInfo;
import com.arcsoft.asg.libdeviceadapt.bean.DeviceCameraInfo;
import com.arcsoft.faceengine.Config;
import com.arcsoft.faceengine.FaceInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdaptationInfoDataUtils {

    public static final int RECOGNITION_DISTANCE_TYPE1 = 1;
    public static final int RECOGNITION_DISTANCE_TYPE2 = 2;
    public static final int RECOGNITION_DISTANCE_TYPE3 = 3;
    public static final int RECOGNITION_DISTANCE_TYPE4 = 4;
    public static final int RECOGNITION_DISTANCE_TYPE5 = 5;
    private static final int COUNT_CAMERA_ONE = 1;
    private static final int COUNT_CAMERA_TWO = 2;
    private static final int DEF_PREVIEW_WIDTH = 640;
    private static final int DEF_PREVIEW_HEIGHT = 480;
    public static final String ASF_OP_0_ONLY = "0°";
    public static final String ASF_OP_90_ONLY = "90°";
    public static final String ASF_OP_180_ONLY = "180°";
    public static final String ASF_OP_270_ONLY = "270°";
    private static final double IR_FACE_RECT_PADDING_RATIO = 0.08;
    private static final int RECT_BORDER_COLOR = Color.argb(255, 8, 108, 202);

    public static CameraViewParam getCameraParam(DeviceAdaptationInfo deviceAdaptationInfo, boolean mainCamera) {
        return createCameraParam(deviceAdaptationInfo, mainCamera);
    }

    private static CameraViewParam createCameraParam(DeviceAdaptationInfo deviceAdaptationInfo, boolean mainCamera) {
        CameraViewParam cameraViewParam = new CameraViewParam();
        cameraViewParam.setPreviewFormat(ImageFormat.NV21);
        cameraViewParam.setUsePreAlloc(true);
        cameraViewParam.setFaceRectColor(mainCamera ? RECT_BORDER_COLOR : Color.YELLOW);
        if (deviceAdaptationInfo != null) {
            cameraViewParam.setCameraId(mainCamera ? deviceAdaptationInfo.getMainCameraId() : deviceAdaptationInfo.getSecondCameraId());
            cameraViewParam.setPreviewWidth(deviceAdaptationInfo.getPreviewWidth());
            cameraViewParam.setPreviewHeight(deviceAdaptationInfo.getPreviewHeight());
            cameraViewParam.setMainViewMirror(mainCamera ? deviceAdaptationInfo.isMainCameraMirror() : deviceAdaptationInfo.isSecondCameraMirror());
            cameraViewParam.setViceViewMirror(mainCamera ? deviceAdaptationInfo.isLeftGlSurfaceViewMirror() : deviceAdaptationInfo.isRightGlSurfaceViewMirror());
            cameraViewParam.setScaleType(CameraViewConstants.TYPE_DISPLAY_NORMAL);
            cameraViewParam.setMainViewAdditionalRotation(mainCamera ? deviceAdaptationInfo.getMainCameraRotation() :
                    deviceAdaptationInfo.getSecondCameraRotation());
            cameraViewParam.setViceViewAdditionalRotation(mainCamera ? deviceAdaptationInfo.getLeftGlSurfaceViewRotation() :
                    deviceAdaptationInfo.getRightGlSurfaceViewRotation());
            cameraViewParam.setFaceRectMirrorVertical(mainCamera ? deviceAdaptationInfo.isRectVerticalMirror() :
                    deviceAdaptationInfo.isSecondRectVerticalMirror());
            cameraViewParam.setFaceRectMirrorHorizontal(mainCamera ? deviceAdaptationInfo.isRectHorizontalMirror() :
                    deviceAdaptationInfo.isSecondRectHorizontalMirror());
        }
        return cameraViewParam;
    }

    public static List<String> getCameraRatioList(@NonNull DeviceAdaptationInfo deviceAdaptationInfo) {
        List<Pair<Integer, Integer>> ratioList = deviceAdaptationInfo.getCameraSizeList();
        List<String> items = new ArrayList<>();
        if (ratioList == null || ratioList.isEmpty()) {
            items.add(640 + "*" + 480);
            return items;
        }
        for (int i = 0; i < ratioList.size(); i++) {
            Pair<Integer, Integer> pair = ratioList.get(i);
            items.add(pair.first + "*" + pair.second);
        }
        return items;
    }

    public static List<String> getCameraPositionList(@NonNull DeviceAdaptationInfo deviceAdaptationInfo) {
        List<Integer> posIntList = deviceAdaptationInfo.getCameraPositionList();
        List<String> posList = new ArrayList<>();
        if (posIntList == null || posIntList.isEmpty()) {
            int defPos = Camera.CameraInfo.CAMERA_FACING_BACK;
            posList.add(String.valueOf(defPos));
        } else {
            for (Integer pos : posIntList) {
                posList.add(String.valueOf(pos));
            }
        }
        return posList;
    }

    public static DeviceAdaptationInfo getDeviceCameraInfoAsync(DeviceCameraInfo cameraInfo) {
        return AdaptationInfoDataUtils.getAdaptationInfo(cameraInfo, getFaceDetectDegreeList());
    }

    public static DeviceAdaptationInfo getAdaptationInfo(@NonNull DeviceCameraInfo cameraInfo, @NonNull List<String> faceDetectDegreeList) {
        DeviceAdaptationInfo deviceAdaptationInfo = new DeviceAdaptationInfo();
        deviceAdaptationInfo.setCameraSizeList(cameraInfo.getCameraSizeList());
        deviceAdaptationInfo.setCameraPositionList(cameraInfo.getCameraPosList());
        deviceAdaptationInfo.setPreviewWidth(cameraInfo.getDefPreviewW());
        deviceAdaptationInfo.setPreviewHeight(cameraInfo.getDefPreviewH());
        deviceAdaptationInfo.setCameraCount(cameraInfo.getCameraPosList().size());
        deviceAdaptationInfo.setMainCameraId(Camera.CameraInfo.CAMERA_FACING_BACK);
        deviceAdaptationInfo.setSecondCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT);
        if (faceDetectDegreeList.size() > 0) {
            String faceDetectDegree = faceDetectDegreeList.get(0);
            deviceAdaptationInfo.setFaceDetectDegree(faceDetectDegree);
        }
        deviceAdaptationInfo.setMainCameraRotation(0);
        deviceAdaptationInfo.setSecondCameraRotation(0);
        deviceAdaptationInfo.setMainCameraChangeWidthHeight(false);
        deviceAdaptationInfo.setMainCameraMirror(false);
        deviceAdaptationInfo.setRectHorizontalMirror(false);
        deviceAdaptationInfo.setRectVerticalMirror(false);
        deviceAdaptationInfo.setLeftGlSurfaceViewRotation(0);
        deviceAdaptationInfo.setLeftGlSurfaceViewMirror(false);
        deviceAdaptationInfo.setRightGlSurfaceViewRotation(0);
        deviceAdaptationInfo.setRightGlSurfaceViewMirror(false);
        deviceAdaptationInfo.setHorizontalDisplacement(0);
        deviceAdaptationInfo.setVerticalDisplacement(0);
        return deviceAdaptationInfo;
    }

    public static List<String> getFaceDetectDegreeList() {
        String[] strings = Utils.getApp().getResources().getStringArray(R.array.face_detect_degree);
        return Arrays.asList(strings);
    }

    public static Config.DetectPriority getFaceEnginePri(@NonNull DeviceAdaptationInfo adaptationInfo) {
        Config.DetectPriority priority;
        switch (adaptationInfo.getFaceDetectDegree()) {
            case ASF_OP_0_ONLY:
                priority = Config.DetectPriority.DP_0_ONLY;
                break;
            case ASF_OP_90_ONLY:
                priority = Config.DetectPriority.DP_90_ONLY;
                break;
            case ASF_OP_180_ONLY:
                priority = Config.DetectPriority.DP_180_ONLY;
                break;
            case ASF_OP_270_ONLY:
                priority = Config.DetectPriority.DP_270_ONLY;
                break;
            default:
                priority = Config.DetectPriority.DP_ALL;
                break;
        }
        return priority;
    }

    public static Config.DetectDistance getFaceEngineDistance(@NonNull DeviceAdaptationInfo adaptationInfo) {
        Config.DetectDistance distance;
        switch (adaptationInfo.getRecognizeDistance()) {
            case RECOGNITION_DISTANCE_TYPE1:
                distance = Config.DetectDistance.CLOSE;
                break;
            case RECOGNITION_DISTANCE_TYPE2:
                distance = Config.DetectDistance.NEAR;
                break;
            case RECOGNITION_DISTANCE_TYPE3:
                distance = Config.DetectDistance.REGULAR;
                break;
            case RECOGNITION_DISTANCE_TYPE4:
                distance = Config.DetectDistance.FAR;
                break;
            default:
                distance = Config.DetectDistance.DISTANT;
                break;
        }
        return distance;
    }

    public static void keepMaxFace(List<FaceInfo> ftFaceList) {
        if (ftFaceList == null || ftFaceList.size() <= 1) {
            return;
        }
        FaceInfo maxFaceInfo = ftFaceList.get(0);
        for (FaceInfo faceInfo : ftFaceList) {
            if (faceInfo.faceRect.width() > maxFaceInfo.faceRect.width()) {
                maxFaceInfo = faceInfo;
            }
        }
        ftFaceList.clear();
        ftFaceList.add(maxFaceInfo);
    }

    /**
     * 获取扩大后的IR人脸框
     *
     * @param oriRect
     * @return
     */
    public static Rect getPaddingIrRect(Rect oriRect) {
        int widthRect = oriRect.width();
        int heightRect = oriRect.height();
        int widthPadding = (int) (widthRect * IR_FACE_RECT_PADDING_RATIO);
        int heightPadding = (int) (heightRect * IR_FACE_RECT_PADDING_RATIO);
        return new Rect(oriRect.left - widthPadding, oriRect.top - heightPadding,
                oriRect.right + widthPadding, oriRect.bottom + heightPadding);
    }
}
