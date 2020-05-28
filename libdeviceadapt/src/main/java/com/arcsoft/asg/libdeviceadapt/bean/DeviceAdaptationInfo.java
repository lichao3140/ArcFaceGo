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

package com.arcsoft.asg.libdeviceadapt.bean;

import android.util.Pair;

import java.io.Serializable;
import java.util.List;

public class DeviceAdaptationInfo implements Serializable {

    private int cameraCount;
    private int previewWidth;
    private int previewHeight;
    private int mainCameraId;
    private int secondCameraId;
    private String faceDetectDegree;
    private int mainCameraRotation;
    private int secondCameraRotation;
    private boolean mainCameraChangeWidthHeight;
    private boolean secondCameraChangeWidthHeight;
    private boolean mainCameraMirror;
    private boolean secondCameraMirror;
    private boolean rectHorizontalMirror;
    private boolean secondRectHorizontalMirror;
    private boolean rectVerticalMirror;
    private boolean secondRectVerticalMirror;
    private int leftGlSurfaceViewRotation;
    private boolean leftGlSurfaceViewMirror;
    private int rightGlSurfaceViewRotation;
    private boolean rightGlSurfaceViewMirror;
    private int horizontalDisplacement;
    private int verticalDisplacement;
    private int recognizeDistance;
    private List<Pair<Integer, Integer>> cameraSizeList;
    private List<Integer> cameraPositionList;

    /**
     * 获取设备相机数量
     * @return 相机数量值
     */
    public int getCameraCount() {
        return cameraCount;
    }

    /**
     * 设置设备相机数量
     * @param cameraCount 相机数量值
     */
    public void setCameraCount(int cameraCount) {
        this.cameraCount = cameraCount;
    }

    /**
     * 获取相机预览宽
     * @return 相机预览宽度
     */
    public int getPreviewWidth() {
        return previewWidth;
    }

    /**
     * 设置相机预览宽
     * @param previewWidth 相机预览宽度
     */
    public void setPreviewWidth(int previewWidth) {
        this.previewWidth = previewWidth;
    }

    /**
     * 获取相机预览高度
     * @return 相机预览高度
     */
    public int getPreviewHeight() {
        return previewHeight;
    }

    /**
     * 获取相机预览高度
     * @param previewHeight 相机预览高度
     */
    public void setPreviewHeight(int previewHeight) {
        this.previewHeight = previewHeight;
    }

    /**
     * 获取主相机id
     * @return 相机id
     */
    public int getMainCameraId() {
        return mainCameraId;
    }

    /**
     * 设置主相机id
     * @param mainCameraId 相机id
     */
    public void setMainCameraId(int mainCameraId) {
        this.mainCameraId = mainCameraId;
    }

    /**
     * 获取副相机id
     * @return 相机id
     */
    public int getSecondCameraId() {
        return secondCameraId;
    }

    /**
     * 设置副相机id
     * @param secondCameraId 相机id
     */
    public void setSecondCameraId(int secondCameraId) {
        this.secondCameraId = secondCameraId;
    }

    /**
     * 获取预览识别角度
     * @return 角度值：0°，90°，180°，270°
     */
    public String getFaceDetectDegree() {
        return faceDetectDegree;
    }

    /**
     * 设置预览识别角度
     * @param faceDetectDegree 角度值：0°，90°，180°，270°
     */
    public void setFaceDetectDegree(String faceDetectDegree) {
        this.faceDetectDegree = faceDetectDegree;
    }

    /**
     * 获取主相机预览旋转角度
     * @return 旋转角度：0，90，180，270
     */
    public int getMainCameraRotation() {
        return mainCameraRotation;
    }

    /**
     * 设置主相机预览旋转角度
     * @param mainCameraRotation 旋转角度：0，90，180，270
     */
    public void setMainCameraRotation(int mainCameraRotation) {
        this.mainCameraRotation = mainCameraRotation;
    }

    /**
     * 获取副相机预览旋转角度
     * @return 旋转角度：0，90，180，270
     */
    public int getSecondCameraRotation() {
        return secondCameraRotation;
    }

    /**
     * 设置副相机预览旋转角度
     * @param secondCameraRotation 旋转角度：0，90，180，270
     */
    public void setSecondCameraRotation(int secondCameraRotation) {
        this.secondCameraRotation = secondCameraRotation;
    }

    /**
     * 主相机预览宽高互换
     * @return true 互换；false 不互换
     */
    public boolean isMainCameraChangeWidthHeight() {
        return mainCameraChangeWidthHeight;
    }

    /**
     * 设置主相机预览宽高互换
     * @param mainCameraChangeWidthHeight true 互换；false 不互换
     */
    public void setMainCameraChangeWidthHeight(boolean mainCameraChangeWidthHeight) {
        this.mainCameraChangeWidthHeight = mainCameraChangeWidthHeight;
    }

    /**
     * 副相机预览宽高互换
     * @return true 互换；false 不互换
     */
    public boolean isSecondCameraChangeWidthHeight() {
        return secondCameraChangeWidthHeight;
    }

    /**
     * 设置副相机预览宽高互换
     * @param secondCameraChangeWidthHeight true 互换；false 不互换
     */
    public void setSecondCameraChangeWidthHeight(boolean secondCameraChangeWidthHeight) {
        this.secondCameraChangeWidthHeight = secondCameraChangeWidthHeight;
    }

    /**
     * 主相机预览镜像
     * @return true 镜像；false 不镜像
     */
    public boolean isMainCameraMirror() {
        return mainCameraMirror;
    }

    /**
     * 设置主相机预览镜像
     * @param mainCameraMirror true 镜像；false 不镜像
     */
    public void setMainCameraMirror(boolean mainCameraMirror) {
        this.mainCameraMirror = mainCameraMirror;
    }

    /**
     * 副相机预览镜像
     * @return true 镜像；false 不镜像
     */
    public boolean isSecondCameraMirror() {
        return secondCameraMirror;
    }

    /**
     * 设置副相机预览镜像
     * @param secondCameraMirror true 镜像；false 不镜像
     */
    public void setSecondCameraMirror(boolean secondCameraMirror) {
        this.secondCameraMirror = secondCameraMirror;
    }

    /**
     * 主相机预览人脸框水平镜像
     * @return true 镜像；false 不镜像
     */
    public boolean isRectHorizontalMirror() {
        return rectHorizontalMirror;
    }

    /**
     * 设置主相机预览人脸框水平镜像
     * @param rectHorizontalMirror  true 镜像；false 不镜像
     */
    public void setRectHorizontalMirror(boolean rectHorizontalMirror) {
        this.rectHorizontalMirror = rectHorizontalMirror;
    }

    /**
     * 副相机预览人脸框水平镜像
     * @return true 镜像；false 不镜像
     */
    public boolean isSecondRectHorizontalMirror() {
        return secondRectHorizontalMirror;
    }

    /**
     * 设置副相机预览人脸框水平镜像
     * @param secondRectHorizontalMirror  true 镜像；false 不镜像
     */
    public void setSecondRectHorizontalMirror(boolean secondRectHorizontalMirror) {
        this.secondRectHorizontalMirror = secondRectHorizontalMirror;
    }

    /**
     * 主相机预览垂直镜像
     * @return true 镜像；false 不镜像
     */
    public boolean isRectVerticalMirror() {
        return rectVerticalMirror;
    }

    /**
     * 设置主相机预览垂直镜像
     * @param rectVerticalMirror true 镜像；false 不镜像
     */
    public void setRectVerticalMirror(boolean rectVerticalMirror) {
        this.rectVerticalMirror = rectVerticalMirror;
    }

    /**
     * 副相机预览垂直镜像
     * @return true 镜像；false 不镜像
     */
    public boolean isSecondRectVerticalMirror() {
        return secondRectVerticalMirror;
    }

    /**
     * 设置副相机预览垂直镜像
     * @param secondRectVerticalMirror true 镜像；false 不镜像
     */
    public void setSecondRectVerticalMirror(boolean secondRectVerticalMirror) {
        this.secondRectVerticalMirror = secondRectVerticalMirror;
    }

    /**
     * 主相机识别算法采集旋转角度
     * @return 旋转角度：0，90，180，270
     */
    public int getLeftGlSurfaceViewRotation() {
        return leftGlSurfaceViewRotation;
    }

    /**
     * 设置主相机识别算法采集旋转角度
     * @param leftGlSurfaceViewRotation 旋转角度：0，90，180，270
     */
    public void setLeftGlSurfaceViewRotation(int leftGlSurfaceViewRotation) {
        this.leftGlSurfaceViewRotation = leftGlSurfaceViewRotation;
    }

    /**
     * 主相机识别算法采集镜像
     * @return true 镜像；false 不镜像
     */
    public boolean isLeftGlSurfaceViewMirror() {
        return leftGlSurfaceViewMirror;
    }

    /**
     * 设置主相机识别算法采集镜像
     * @param leftGlSurfaceViewMirror true 镜像；false 不镜像
     */
    public void setLeftGlSurfaceViewMirror(boolean leftGlSurfaceViewMirror) {
        this.leftGlSurfaceViewMirror = leftGlSurfaceViewMirror;
    }

    /**
     * 副相机识别算法采集旋转角度
     * @return 旋转角度：0，90，180，270
     */
    public int getRightGlSurfaceViewRotation() {
        return rightGlSurfaceViewRotation;
    }

    /**
     * 设置副相机识别算法采集旋转角度
     * @param rightGlSurfaceViewRotation 旋转角度：0，90，180，270
     */
    public void setRightGlSurfaceViewRotation(int rightGlSurfaceViewRotation) {
        this.rightGlSurfaceViewRotation = rightGlSurfaceViewRotation;
    }

    /**
     * 副相机识别算法采集镜像
     * @return true 镜像；false 不镜像
     */
    public boolean isRightGlSurfaceViewMirror() {
        return rightGlSurfaceViewMirror;
    }

    /**
     * 设置副相机识别算法采集镜像
     * @param rightGlSurfaceViewMirror true 镜像；false 不镜像
     */
    public void setRightGlSurfaceViewMirror(boolean rightGlSurfaceViewMirror) {
        this.rightGlSurfaceViewMirror = rightGlSurfaceViewMirror;
    }

    /**
     * 获取水平位移距离
     * @return 位移距离
     */
    public int getHorizontalDisplacement() {
        return horizontalDisplacement;
    }

    /**
     * 设置水平位移距离
     * @param horizontalDisplacement 位移距离
     */
    public void setHorizontalDisplacement(int horizontalDisplacement) {
        this.horizontalDisplacement = horizontalDisplacement;
    }

    /**
     * 设置垂直位移距离
     * @return 位移距离
     */
    public int getVerticalDisplacement() {
        return verticalDisplacement;
    }

    /**
     * 设置垂直位移距离
     * @param verticalDisplacement 位移距离
     */
    public void setVerticalDisplacement(int verticalDisplacement) {
        this.verticalDisplacement = verticalDisplacement;
    }

    /**
     * 获取人脸检测引擎识别距离
     * @return 识别距离:1,2,3,4,5
     */
    public int getRecognizeDistance() {
        return recognizeDistance;
    }

    /**
     * 设置人脸检测引擎识别距离
     * @param recognizeDistance 识别距离:1,2,3,4,5
     */
    public void setRecognizeDistance(int recognizeDistance) {
        this.recognizeDistance = recognizeDistance;
    }

    /**
     * 获取设备相机所支持的预览分辨率
     * @return 分辨率集，如：640*480等
     */
    public List<Pair<Integer, Integer>> getCameraSizeList() {
        return cameraSizeList;
    }

    /**
     * 设置设备相机所支持的预览分辨率
     * @param cameraSizeList 分辨率集，如：640*480等
     */
    public void setCameraSizeList(List<Pair<Integer, Integer>> cameraSizeList) {
        this.cameraSizeList = cameraSizeList;
    }

    /**
     * 获取设备相机数量
     * @return 相机位置集，如：Camera.CameraInfo.CAMERA_FACING_FRONT等
     */
    public List<Integer> getCameraPositionList() {
        return cameraPositionList;
    }

    /**
     * 设置设备相机数量
     * @param cameraPositionList 相机位置集，如：Camera.CameraInfo.CAMERA_FACING_FRONT等
     */
    public void setCameraPositionList(List<Integer> cameraPositionList) {
        this.cameraPositionList = cameraPositionList;
    }
}
