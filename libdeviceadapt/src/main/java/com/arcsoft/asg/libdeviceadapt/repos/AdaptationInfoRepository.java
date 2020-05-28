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

import android.graphics.Rect;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.arcsoft.arcfacesingle.libutil.CameraEngineUtils;
import com.arcsoft.asg.libcamera.bean.CameraViewParam;
import com.arcsoft.asg.libcamera.bean.FaceRectInfo;
import com.arcsoft.asg.libcamera.bean.GlDrawerParam;
import com.arcsoft.asg.libcamera.contract.ICamera;
import com.arcsoft.asg.libcamera.controller.CameraViewController;
import com.arcsoft.asg.libcamera.impl.Camera1Manager;
import com.arcsoft.asg.libcamera.util.CameraUtils;
import com.arcsoft.asg.libcamera.view.BaseGLSurfaceView;
import com.arcsoft.asg.libcamera.view.CameraFaceView;
import com.arcsoft.asg.libcommon.contract.ICameraEngine;
import com.arcsoft.asg.libcommon.util.business.rx.RxUtils;
import com.arcsoft.asg.libcommon.util.common.ArithmeticUtils;
import com.arcsoft.asg.libcommon.util.common.StringUtils;
import com.arcsoft.asg.libcommon.util.common.ToastUtils;
import com.arcsoft.asg.libcommon.util.common.Utils;
import com.arcsoft.asg.libdeviceadapt.R;
import com.arcsoft.asg.libdeviceadapt.bean.DeviceAdaptationInfo;
import com.arcsoft.asg.libdeviceadapt.bean.DeviceCameraInfo;
import com.arcsoft.faceengine.Config;
import com.arcsoft.faceengine.ErrorInfo;
import com.arcsoft.faceengine.FaceEngine;
import com.arcsoft.faceengine.FaceInfo;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

public class AdaptationInfoRepository implements CameraViewController.Callback {

    private static final String TAG = AdaptationInfoRepository.class.getSimpleName();
    private static final int DEGREE_360 = 360;
    private static final int RECT_BORDER_WIDTH = 2;
    private static final int RECT_NORMAL_PADDING = 0;
    /**
     * 界面是否可见 true 可见；false 不可见
     */
    private boolean viewVisible;
    /**
     * 是否选中主视图：true 选中主视图；false 选中副视图
     */
    private boolean selectMainPreview;
    /**
     * 是否从欢迎页进入
     */
    private boolean fromSplash;

    private int firstAdditionalRotation;
    private int secondAdditionalRotation;

    private boolean firstPreviewSizeChange;
    private boolean secondPreviewSizeChange;

    private boolean firstPreviewMirror;
    private boolean secondPreviewMirror;

    private int leftSurfaceAdditionalRotation;
    private int rightSurfaceAdditionalRotation;

    private boolean leftSurfaceMirror;
    private boolean rightSurfaceMirror;

    private int horizontalDisplacement;
    private int verticalDisplacement;

    private DeviceAdaptationInfo deviceAdaptationInfo;
    private CameraViewController cameraViewController;
    private List<FaceInfo> faceInfoList;
    private List<FaceRectInfo> infoList;
    private ICamera cameraManage1;
    private ICamera cameraManage2;
    private FaceEngine faceEngine;
    private IAdaptationInfo weakCallback;

    public AdaptationInfoRepository(@NonNull IAdaptationInfo callback) {
        this.weakCallback = callback;
        selectMainPreview = true;
        faceInfoList = new ArrayList<>();
        infoList = new ArrayList<>();
        cameraViewController = new CameraViewController();
        cameraViewController.setCallback(this);
    }

    public void setFromSplash(boolean fromSplash) {
        this.fromSplash = fromSplash;
    }

    private IAdaptationInfo getCallback() {
        return weakCallback;
    }

    public List<String> getFaceDetectDegreeList() {
        return AdaptationInfoDataUtils.getFaceDetectDegreeList();
    }

    public DeviceAdaptationInfo getNewDeviceAdaptationInfo(DeviceCameraInfo cameraInfo) {
        deviceAdaptationInfo = AdaptationInfoDataUtils.getDeviceCameraInfoAsync(cameraInfo);
        setDeviceAdaptationInfo(deviceAdaptationInfo, cameraInfo);
        return deviceAdaptationInfo;
    }

    public void setDeviceAdaptationInfo(DeviceAdaptationInfo deviceAdaptationInfo, DeviceCameraInfo cameraInfo) {
        List<Integer> posList = deviceAdaptationInfo.getCameraPositionList();
        List<Pair<Integer, Integer>> ratioList = deviceAdaptationInfo.getCameraSizeList();
        if (posList == null || posList.isEmpty() || ratioList == null || ratioList.isEmpty()) {
            deviceAdaptationInfo.setCameraSizeList(cameraInfo.getCameraSizeList());
            deviceAdaptationInfo.setCameraPositionList(cameraInfo.getCameraPosList());
        }
        this.deviceAdaptationInfo = deviceAdaptationInfo;
        initFaceEngine();
        CameraViewParam param1 = AdaptationInfoDataUtils.getCameraParam(deviceAdaptationInfo, true);
        ICameraEngine cameraEngine = CameraEngineUtils.createCameraFaceEngine();
        cameraManage1 = new Camera1Manager(cameraEngine);
        cameraManage1.setParam(param1);
        cameraManage1.setMainCamera(true);
        if (deviceAdaptationInfo.getCameraCount() > 1) {
            CameraViewParam param2 = AdaptationInfoDataUtils.getCameraParam(deviceAdaptationInfo, false);
            ICameraEngine cameraEngine2 = CameraEngineUtils.createCameraFaceEngine();
            cameraManage2 = new Camera1Manager(cameraEngine2);
            cameraManage2.setParam(param2);
            cameraManage2.setMainCamera(false);
        }
        firstAdditionalRotation = deviceAdaptationInfo.getMainCameraRotation();
        if (firstAdditionalRotation > 0) {
            firstAdditionalRotation = 360 - firstAdditionalRotation;
        }
        secondAdditionalRotation = deviceAdaptationInfo.getSecondCameraRotation();
        if (secondAdditionalRotation > 0) {
            secondAdditionalRotation = 360 - secondAdditionalRotation;
        }

        firstPreviewSizeChange = deviceAdaptationInfo.isMainCameraChangeWidthHeight();
        secondPreviewSizeChange = deviceAdaptationInfo.isSecondCameraChangeWidthHeight();

        firstPreviewMirror = deviceAdaptationInfo.isMainCameraMirror();
        secondPreviewMirror = deviceAdaptationInfo.isSecondCameraMirror();

        leftSurfaceAdditionalRotation = deviceAdaptationInfo.getLeftGlSurfaceViewRotation();
        rightSurfaceAdditionalRotation = deviceAdaptationInfo.getRightGlSurfaceViewRotation();

        leftSurfaceMirror = deviceAdaptationInfo.isLeftGlSurfaceViewMirror();
        rightSurfaceMirror = deviceAdaptationInfo.isRightGlSurfaceViewMirror();

        horizontalDisplacement = deviceAdaptationInfo.getHorizontalDisplacement();
        verticalDisplacement = deviceAdaptationInfo.getVerticalDisplacement();
    }

    public void bindCamera(boolean mainCamera, CameraFaceView cameraFaceView1, CameraFaceView cameraFaceView2) {
        cameraViewController.bind(mainCamera ? getCameraManage1() : getCameraManage2(),
                cameraFaceView1, cameraFaceView2);
    }

    public void setSelectMainPreview(boolean selectMainPreview) {
        this.selectMainPreview = selectMainPreview;
    }

    public void setHorizontalDisplace(int displace) {
        deviceAdaptationInfo.setHorizontalDisplacement(displace);
    }

    public void setVerticalDisplace(int displace) {
        deviceAdaptationInfo.setVerticalDisplacement(displace);
    }

    /**
     * 预览比例是否相等
     * @param string
     * @return true 相等；false 不等
     */
    public boolean previewRatio(String string) {
        String[] sizeArr = string.split("\\*");
        int width = Integer.parseInt(sizeArr[0]);
        int height = Integer.parseInt(sizeArr[1]);
        int oldPreW = deviceAdaptationInfo.getPreviewWidth();
        int oldPreH = deviceAdaptationInfo.getPreviewHeight();
        double ratioOld = ArithmeticUtils.div(oldPreW, oldPreH);
        double ratioNew = ArithmeticUtils.div(width, height);
        return ratioNew == ratioOld;
    }

    public void selectPreviewRatio(String string, CameraFaceView firstViceView, CameraFaceView secondViceView) {
        String[] sizeArr = string.split("\\*");
        int width = Integer.parseInt(sizeArr[0]);
        int height = Integer.parseInt(sizeArr[1]);
        deviceAdaptationInfo.setPreviewWidth(width);
        deviceAdaptationInfo.setPreviewHeight(height);
        getCameraManage1().getCameraViewParam().setPreviewWidth(width);
        getCameraManage1().getCameraViewParam().setPreviewHeight(height);
        GlDrawerParam glDrawerParam1 = CameraUtils.getDrawerParam(getCameraManage1().getCameraViewParam(), false);
        ((BaseGLSurfaceView) firstViceView.getCameraView()).updateVertices(glDrawerParam1);
        getCameraManage2().getCameraViewParam().setPreviewWidth(width);
        getCameraManage2().getCameraViewParam().setPreviewHeight(height);
        GlDrawerParam glDrawerParam2 = CameraUtils.getDrawerParam(getCameraManage2().getCameraViewParam(), false);
        ((BaseGLSurfaceView) secondViceView.getCameraView()).updateVertices(glDrawerParam2);
    }

    public void startAllCamera() {
        if (cameraViewController != null) {
            cameraViewController.startAllCamera();
        }
    }

    public void stopAllCamera() {
        if (cameraViewController != null) {
            cameraViewController.stopAllCamera();
        }
    }

    /**
     * 切换前后置相机
     */
    public void selectCameraId(int mainCameraId, int viceCameraId) {
        stopAllCamera();
        deviceAdaptationInfo.setMainCameraId(mainCameraId);
        deviceAdaptationInfo.setSecondCameraId(viceCameraId);
        getCameraManage1().getCameraViewParam().setCameraId(mainCameraId);
        getCameraManage2().getCameraViewParam().setCameraId(viceCameraId);
        startAllCamera();
    }

    /**
     * 切换人脸检测角度
     *
     * @param degree
     */
    public void selectFaceDetectDegree(String degree) {
        deviceAdaptationInfo.setFaceDetectDegree(degree);
        unInitFaceEngine();
        initFaceEngine();
    }

    /**
     * 旋转相机预览角度
     */
    public void setAdditionalRotation() {
        if (selectMainPreview) {
            if (getCameraManage1() == null) {
                return;
            }
            CameraViewParam cameraViewParam = getCameraManage1().getCameraViewParam();
            if (cameraViewParam == null) {
                return;
            }
            cameraViewController.stopCamera(getCameraManage1());
            firstAdditionalRotation += 90;
            if (firstAdditionalRotation >= DEGREE_360) {
                firstAdditionalRotation = firstAdditionalRotation % DEGREE_360;
            }
            if (firstAdditionalRotation > 0) {
                int rotation = 360 - firstAdditionalRotation;
                int previewRotation = CameraUtils.getDisplayDegree(rotation, cameraViewParam.getCameraId());
                cameraViewParam.setPreviewRotation(previewRotation);
                cameraViewParam.setMainViewAdditionalRotation(rotation);
                deviceAdaptationInfo.setMainCameraRotation(rotation);
            } else {
                int previewRotation = CameraUtils.getDisplayDegree(firstAdditionalRotation, cameraViewParam.getCameraId());
                cameraViewParam.setPreviewRotation(previewRotation);
                cameraViewParam.setMainViewAdditionalRotation(firstAdditionalRotation);
                deviceAdaptationInfo.setMainCameraRotation(firstAdditionalRotation);
            }
            cameraViewController.startCameraDelayed(getCameraManage1());
        } else {
            if (getCameraManage2() == null) {
                return;
            }
            CameraViewParam cameraViewParam = getCameraManage2().getCameraViewParam();
            if (cameraViewParam == null) {
                return;
            }
            cameraViewController.stopCamera(getCameraManage2());
            secondAdditionalRotation += 90;
            if (secondAdditionalRotation >= DEGREE_360) {
                secondAdditionalRotation = secondAdditionalRotation % DEGREE_360;
            }
            if (secondAdditionalRotation > 0) {
                int rotation = 360 - secondAdditionalRotation;
                int previewRotation = CameraUtils.getDisplayDegree(rotation, cameraViewParam.getCameraId());
                cameraViewParam.setPreviewRotation(previewRotation);
                cameraViewParam.setMainViewAdditionalRotation(rotation);
                deviceAdaptationInfo.setSecondCameraRotation(rotation);
            } else {
                int previewRotation = CameraUtils.getDisplayDegree(secondAdditionalRotation, cameraViewParam.getCameraId());
                cameraViewParam.setPreviewRotation(previewRotation);
                cameraViewParam.setMainViewAdditionalRotation(secondAdditionalRotation);
                deviceAdaptationInfo.setSecondCameraRotation(secondAdditionalRotation);
            }
            cameraViewController.startCameraDelayed(getCameraManage2());
        }
        cameraViewController.updateMainCameraView(isSelectMainPreview() ? getCameraManage1() : getCameraManage2());
    }

    /**
     * 切换相机宽高
     */
    public boolean changeWidthHeight() {
        if (deviceAdaptationInfo == null) {
            return false;
        }
        if (selectMainPreview) {
            firstPreviewSizeChange = !firstPreviewSizeChange;
            deviceAdaptationInfo.setMainCameraChangeWidthHeight(firstPreviewSizeChange);
        } else {
            secondPreviewSizeChange = !secondPreviewSizeChange;
            deviceAdaptationInfo.setSecondCameraChangeWidthHeight(secondPreviewSizeChange);
        }
        return true;
    }

    /**
     * 设置预览镜像
     */
    public void setPreviewMirror() {
        if (deviceAdaptationInfo == null) {
            return;
        }
        if (selectMainPreview) {
            CameraViewParam cameraViewParam = getCameraManage1().getCameraViewParam();
            if (cameraViewParam == null) {
                return;
            }
            cameraViewController.stopCamera(getCameraManage1());
            firstPreviewMirror = !firstPreviewMirror;
            deviceAdaptationInfo.setMainCameraMirror(firstPreviewMirror);
            cameraViewParam.setMainViewMirror(firstPreviewMirror);
            cameraViewController.startCameraDelayed(getCameraManage1());
        } else {
            CameraViewParam cameraViewParam = getCameraManage2().getCameraViewParam();
            if (cameraViewParam == null) {
                return;
            }
            cameraViewController.stopCamera(getCameraManage2());
            secondPreviewMirror = !secondPreviewMirror;
            deviceAdaptationInfo.setSecondCameraMirror(secondPreviewMirror);
            cameraViewParam.setMainViewMirror(secondPreviewMirror);
            cameraViewController.startCameraDelayed(getCameraManage2());
        }
        cameraViewController.updateMainCameraView(isSelectMainPreview() ? getCameraManage1() : getCameraManage2());
    }

    /**
     * 修改副相机副view旋转角度
     */
    public boolean changeGlSurfaceViewRightDegree() {
        rightSurfaceAdditionalRotation += 90;
        if (rightSurfaceAdditionalRotation >= DEGREE_360) {
            rightSurfaceAdditionalRotation = rightSurfaceAdditionalRotation % DEGREE_360;
        }
        deviceAdaptationInfo.setRightGlSurfaceViewRotation(rightSurfaceAdditionalRotation);
        getCameraManage2().getCameraViewParam().setViceViewAdditionalRotation(rightSurfaceAdditionalRotation);
        cameraViewController.updateViceCameraView(getCameraManage2());
        return true;
    }

    /**
     * 修改副相机副view镜像
     */
    public void changeGlSurfaceViewRightMirror() {
        rightSurfaceMirror = !rightSurfaceMirror;
        deviceAdaptationInfo.setRightGlSurfaceViewMirror(rightSurfaceMirror);
        getCameraManage2().getCameraViewParam().setViceViewMirror(rightSurfaceMirror);
        cameraViewController.updateViceCameraView(getCameraManage2());
    }

    /**
     * 脸框水平镜像
     *
     * @param mirror
     */
    public void setPreviewHorizontalMirror(Boolean mirror) {
        if (selectMainPreview) {
            deviceAdaptationInfo.setRectHorizontalMirror(mirror);
            getCameraManage1().getCameraViewParam().setFaceRectMirrorHorizontal(mirror);
        } else {
            deviceAdaptationInfo.setSecondRectHorizontalMirror(mirror);
            getCameraManage2().getCameraViewParam().setFaceRectMirrorHorizontal(mirror);
        }
        cameraViewController.updateCameraView(selectMainPreview ? getCameraManage1() : getCameraManage2());
    }

    /**
     * 脸框竖直镜像
     *
     * @param mirror
     */
    public void setPreviewVerticalMirror(boolean mirror) {
        if (selectMainPreview) {
            deviceAdaptationInfo.setRectVerticalMirror(mirror);
            getCameraManage1().getCameraViewParam().setFaceRectMirrorVertical(mirror);
        } else {
            deviceAdaptationInfo.setSecondRectVerticalMirror(mirror);
            getCameraManage2().getCameraViewParam().setFaceRectMirrorVertical(mirror);
        }
        cameraViewController.updateCameraView(selectMainPreview ? getCameraManage1() : getCameraManage2());
    }

    public void onInvisible() {
        viewVisible = false;
        unInitFaceEngine();
    }

    public void onVisible() {
        viewVisible = true;
        if (deviceAdaptationInfo != null) {
            initFaceEngine();
        }
    }

    private void initFaceEngine() {
        unInitFaceEngine();
        Config config = new Config();
        config.detectPriority = AdaptationInfoDataUtils.getFaceEnginePri(deviceAdaptationInfo);
        config.distance = AdaptationInfoDataUtils.getFaceEngineDistance(deviceAdaptationInfo);
        faceEngine = new FaceEngine();
        Disposable disposable = Observable.create((ObservableEmitter<Integer> emitter) -> {
            emitter.onNext(faceEngine.init(Utils.getApp(), config));
            emitter.onComplete();
        }).compose(RxUtils.ioToMain())
                .subscribeWith(new DisposableObserver<Integer>() {
                    @Override
                    public void onNext(Integer aLong) {
                        if (aLong != ErrorInfo.MOK) {
                            ToastUtils.showShortToast(StringUtils.getStrFromRes(R.string.face_engine_init_fail) + ":" + aLong);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        ToastUtils.showShortToast(StringUtils.getStrFromRes(R.string.face_engine_init_fail));
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private void unInitFaceEngine() {
        if (faceEngine != null) {
            faceEngine.uninit();
            faceEngine = null;
        }
    }

    public void onViewResume() {
        startAllCamera();
    }

    public void onViewPause() {
        stopAllCamera();
    }

    public void onViewDestroy() {
        unInitFaceEngine();
        if (cameraViewController != null) {
            cameraViewController.unInit();
            cameraViewController = null;
        }
        deviceAdaptationInfo = null;
        faceInfoList = null;
        infoList = null;
        cameraManage1 = null;
        cameraManage2 = null;
    }

    @Override
    public void onCameraOpened(ICamera camera) {
        if (camera != null && !camera.isMainCamera() && fromSplash && getCallback() != null) {
            getCallback().onSecondCameraOpened();
        }
    }

    @Override
    public void onCameraClosed(ICamera camera) {
    }

    @Override
    public void onCameraError(ICamera camera, Exception e) {
        if (getCallback() != null) {
            getCallback().onCameraOpenError(camera.isMainCamera(), e);
        }
    }

    @Override
    public void onPreviewCreated(ICamera camera, CameraFaceView cameraFaceView) {

    }

    @Override
    public void onPreviewChanged(ICamera camera, CameraFaceView cameraFaceView, int viewWidth, int viewHeight) {
        getCameraManage2().getCameraViewParam().setViceViewAdditionalRotation(rightSurfaceAdditionalRotation);
        cameraViewController.updateViceCameraView(getCameraManage2());
    }

    @Override
    public void onCameraPreview(byte[] cameraData, ICamera camera) {
        if (!viewVisible) {
            return;
        }
        if (faceEngine == null) {
            return;
        }
        CameraViewParam cameraViewParam = camera.getCameraViewParam();
        if (cameraViewParam.getCameraId() == deviceAdaptationInfo.getMainCameraId()) {
            faceInfoList.clear();
            infoList.clear();
            faceEngine.faceTrack(cameraData, cameraViewParam.getPreviewWidth(), cameraViewParam.getPreviewHeight(), faceInfoList);
            if (faceInfoList.size() > 0) {
                AdaptationInfoDataUtils.keepMaxFace(faceInfoList);
                infoList.add(new FaceRectInfo(faceInfoList.get(0).faceRect, "", RECT_BORDER_WIDTH));
            }
        }
        if (cameraViewParam.getCameraId() == deviceAdaptationInfo.getMainCameraId()) {
            cameraViewController.refreshFaceRect(camera, infoList);
        } else {
            cameraViewController.refreshFaceRect(camera, transferIrRect(infoList));
        }
    }

    private List<FaceRectInfo> transferIrRect(List<FaceRectInfo> faceRectInfoList) {
        if (faceRectInfoList == null || faceRectInfoList.isEmpty()) {
            return faceRectInfoList;
        }
        int x = deviceAdaptationInfo.getHorizontalDisplacement();
        int y = deviceAdaptationInfo.getVerticalDisplacement();
        List<FaceRectInfo> rightInfoList = new ArrayList<>(faceRectInfoList.size());
        if (x != 0 || y != 0) {
            FaceRectInfo drawInfo = faceRectInfoList.get(0);
            Rect rightRect = AdaptationInfoDataUtils.getPaddingIrRect(drawInfo.getRect());
            rightRect.right += x;
            rightRect.left += x;
            rightRect.top += y;
            rightRect.bottom += y;
            Rect copyRect = new Rect(rightRect);
            int rectWidth = copyRect.width();
            int rectHeight = copyRect.height();
            if (rightRect.left < 0) {
                rightRect.left = RECT_NORMAL_PADDING;
                rightRect.right = rectWidth + RECT_NORMAL_PADDING;
                int hor = x - copyRect.left;
                if (getCallback() != null) {
                    getCallback().setSeekBarDisplace(hor, y);
                }
            } else if (rightRect.right > deviceAdaptationInfo.getPreviewWidth()) {
                rightRect.right = deviceAdaptationInfo.getPreviewWidth() - RECT_NORMAL_PADDING;
                rightRect.left = deviceAdaptationInfo.getPreviewWidth() - rectWidth - RECT_NORMAL_PADDING;
                int hor = x - copyRect.right + deviceAdaptationInfo.getPreviewWidth();
                if (getCallback() != null) {
                    getCallback().setSeekBarDisplace(hor, y);
                }
            } else if (rightRect.top < 0) {
                rightRect.top = RECT_NORMAL_PADDING;
                rightRect.bottom = rectHeight + RECT_NORMAL_PADDING;
                int ver = y - copyRect.top;
                if (getCallback() != null) {
                    getCallback().setSeekBarDisplace(x, ver);
                }
            } else if (rightRect.bottom > deviceAdaptationInfo.getPreviewHeight()) {
                rightRect.bottom = deviceAdaptationInfo.getPreviewHeight() - RECT_NORMAL_PADDING;
                rightRect.top = deviceAdaptationInfo.getPreviewHeight() - rectHeight - RECT_NORMAL_PADDING;
                int ver = y - copyRect.bottom + deviceAdaptationInfo.getPreviewHeight();
                if (getCallback() != null) {
                    getCallback().setSeekBarDisplace(x, ver);
                }
            } else if (rightRect.left < 0 && rightRect.top < 0) {
                rightRect.left = RECT_NORMAL_PADDING;
                rightRect.right = rectWidth + RECT_NORMAL_PADDING;
                rightRect.top = RECT_NORMAL_PADDING;
                rightRect.bottom = rectHeight + RECT_NORMAL_PADDING;
                int hor = x - copyRect.left;
                int ver = y - copyRect.top;
                if (getCallback() != null) {
                    getCallback().setSeekBarDisplace(hor, ver);
                }
            } else if (rightRect.right > deviceAdaptationInfo.getPreviewWidth() && rightRect.top < 0) {
                rightRect.top = RECT_NORMAL_PADDING;
                rightRect.bottom = rectHeight + RECT_NORMAL_PADDING;
                rightRect.right = deviceAdaptationInfo.getPreviewWidth() - RECT_NORMAL_PADDING;
                rightRect.left = deviceAdaptationInfo.getPreviewWidth() - RECT_NORMAL_PADDING - rectWidth;
                int hor = x - copyRect.right + deviceAdaptationInfo.getPreviewWidth();
                int ver = y - copyRect.top;
                if (getCallback() != null) {
                    getCallback().setSeekBarDisplace(hor, ver);
                }
            } else if (rightRect.left < 0 && rightRect.bottom > deviceAdaptationInfo.getPreviewHeight()) {
                rightRect.left = RECT_NORMAL_PADDING;
                rightRect.right = rectWidth + RECT_NORMAL_PADDING;
                rightRect.bottom = deviceAdaptationInfo.getPreviewHeight() - RECT_NORMAL_PADDING;
                rightRect.top = deviceAdaptationInfo.getPreviewHeight() - RECT_NORMAL_PADDING - rectHeight;
                int hor = x - copyRect.left;
                int ver = y - copyRect.bottom + deviceAdaptationInfo.getPreviewHeight();
                if (getCallback() != null) {
                    getCallback().setSeekBarDisplace(hor, ver);
                }
            } else if (rightRect.right > deviceAdaptationInfo.getPreviewWidth()
                    && rightRect.bottom > deviceAdaptationInfo.getPreviewHeight()) {
                rightRect.right = deviceAdaptationInfo.getPreviewWidth() - RECT_NORMAL_PADDING;
                rightRect.left = deviceAdaptationInfo.getPreviewWidth() - rectWidth - RECT_NORMAL_PADDING;
                rightRect.bottom = deviceAdaptationInfo.getPreviewHeight() - RECT_NORMAL_PADDING;
                rightRect.top = deviceAdaptationInfo.getPreviewHeight() - rectHeight - RECT_NORMAL_PADDING;
                int hor = x - copyRect.right + deviceAdaptationInfo.getPreviewWidth();
                int ver = y - copyRect.bottom + deviceAdaptationInfo.getPreviewHeight();
                if (getCallback() != null) {
                    getCallback().setSeekBarDisplace(hor, ver);
                }
            }
            rightInfoList.add(new FaceRectInfo(rightRect, drawInfo.getText(), RECT_BORDER_WIDTH));
            return rightInfoList;
        } else {
            return faceRectInfoList;
        }
    }

    public DeviceAdaptationInfo getDeviceAdaptationInfo() {
        return deviceAdaptationInfo;
    }

    public String getConfigInfo() {
        Gson gson = new Gson();
        if (deviceAdaptationInfo == null) {
            return "";
        } else {
            return gson.toJson(deviceAdaptationInfo);
        }
    }

    public List<String> getCameraRatioList() {
        return AdaptationInfoDataUtils.getCameraRatioList(deviceAdaptationInfo);
    }

    public List<String> getCameraPositionList() {
        return AdaptationInfoDataUtils.getCameraPositionList(deviceAdaptationInfo);
    }

    public boolean isSelectMainPreview() {
        return selectMainPreview;
    }

    public int getFirstAdditionalRotation() {
        return firstAdditionalRotation;
    }

    public int getSecondAdditionalRotation() {
        return secondAdditionalRotation;
    }

    public int getLeftSurfaceAdditionalRotation() {
        return leftSurfaceAdditionalRotation;
    }

    public int getRightSurfaceAdditionalRotation() {
        return rightSurfaceAdditionalRotation;
    }

    public boolean isLeftSurfaceMirror() {
        return leftSurfaceMirror;
    }

    public boolean isRightSurfaceMirror() {
        return rightSurfaceMirror;
    }

    public int getHorizontalDisplacement() {
        return horizontalDisplacement;
    }

    public int getVerticalDisplacement() {
        return verticalDisplacement;
    }

    public ICamera getCameraManage1() {
        return cameraManage1;
    }

    public ICamera getCameraManage2() {
        return cameraManage2;
    }
}
