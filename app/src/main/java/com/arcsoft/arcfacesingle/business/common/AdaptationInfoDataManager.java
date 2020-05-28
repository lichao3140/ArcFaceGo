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

package com.arcsoft.arcfacesingle.business.common;

import android.hardware.Camera;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.asg.libcommon.util.common.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

public class AdaptationInfoDataManager {

    private static volatile AdaptationInfoDataManager INSTANCE;

    private static final int COUNT_CAMERA_ONE = 1;
    private static final int COUNT_CAMERA_TWO = 2;

    private AdaptationInfoDataManager() {
    }

    public static AdaptationInfoDataManager getInstance() {
        if (INSTANCE == null) {
            synchronized (AdaptationInfoDataManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AdaptationInfoDataManager();
                }
            }
        }
        return INSTANCE;
    }

    public List<String> getCameraRatioArray() {
        List<Camera.Size> ratioList = getCameraPreviewSizeList();
        List<String> items = new ArrayList<>(ratioList.size());
        for (int i = 0; i < ratioList.size(); i++) {
            Camera.Size pair = ratioList.get(i);
            items.add(pair.width + "*" + pair.height);
        }
        return items;
    }

    public List<String> getFaceDetectDegreeArray() {
        String[] strings = Utils.getApp().getResources().getStringArray(R.array.face_detect_degree);
        return Arrays.asList(strings);
    }

    /**
     * 获取摄像头预览分辨率集合
     *
     * @return
     */
    public List<Camera.Size> getCameraPreviewSizeList() {
        List<Camera.Size> commonSizeList = new ArrayList<>();
        int cameraCount = Camera.getNumberOfCameras();
        if (cameraCount == COUNT_CAMERA_ONE) {
            try {
                Camera backCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                commonSizeList = backCamera.getParameters().getSupportedPreviewSizes();
                backCamera.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (cameraCount >= COUNT_CAMERA_TWO) {
            List<Camera.Size> frontSizeList;
            try {
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, cameraInfo);
                Camera frontCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
                frontSizeList = frontCamera.getParameters().getSupportedPreviewSizes();
                frontCamera.release();
            } catch (Exception e) {
                frontSizeList = new ArrayList<>();
            }
            List<Camera.Size> backSizeList;
            try {
                Camera.CameraInfo cameraInfo2 = new Camera.CameraInfo();
                Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, cameraInfo2);
                Camera backCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                backSizeList = backCamera.getParameters().getSupportedPreviewSizes();
                backCamera.release();
            } catch (Exception e) {
                backSizeList = new ArrayList<>();
            }
            if (frontSizeList.size() > 0 && backSizeList.size() > 0) {
                for (Camera.Size rgbPreviewSize : frontSizeList) {
                    for (Camera.Size irPreviewSize : backSizeList) {
                        if (irPreviewSize.width == rgbPreviewSize.width && irPreviewSize.height == rgbPreviewSize.height) {
                            commonSizeList.add(irPreviewSize);
                        }
                    }
                }
            } else if (frontSizeList.size() > 0) {
                commonSizeList = frontSizeList;
            } else if (backSizeList.size() > 0) {
                commonSizeList = backSizeList;
            }
        }

        Collections.sort(commonSizeList, ((size, t1) -> {
            int leftWidth = size.width;
            int rightWidth = t1.width;
            return leftWidth - rightWidth;
        }));

        return new ArrayList<>(new LinkedHashSet<>(commonSizeList));
    }

    /**
     * 获取相机数量集合
     *
     * @return
     */
    public List<String> getCameraPositionList() {
        List<String> positionList = new ArrayList<>();
        int cameraCount = Camera.getNumberOfCameras();
        if (cameraCount == COUNT_CAMERA_ONE) {
            positionList.add(String.valueOf(Camera.CameraInfo.CAMERA_FACING_BACK));
        } else if (cameraCount >= COUNT_CAMERA_TWO) {
            positionList.add(String.valueOf(Camera.CameraInfo.CAMERA_FACING_BACK));
            positionList.add(String.valueOf(Camera.CameraInfo.CAMERA_FACING_FRONT));
        }
        return positionList;
    }
}
