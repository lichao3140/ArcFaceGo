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

package com.arcsoft.arcfacesingle.business.photo;

import android.graphics.Bitmap;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.business.common.CommonRepository;
import com.arcsoft.arcfacesingle.business.recognize.RecognizeRepDataManager;
import com.arcsoft.arcfacesingle.libutil.CameraEngineUtils;
import com.arcsoft.arcfacesingle.libutil.ImageFileUtils;
import com.arcsoft.arcfacesingle.server.faceengine.FaceEngineManager;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.asg.libcamera.bean.CameraViewParam;
import com.arcsoft.asg.libcamera.contract.ICamera;
import com.arcsoft.asg.libcamera.controller.CameraViewController;
import com.arcsoft.asg.libcamera.impl.Camera1Manager;
import com.arcsoft.asg.libcamera.view.CameraFaceView;
import com.arcsoft.asg.libcommon.util.business.rx.RxUtils;
import com.arcsoft.asg.libcommon.util.common.StringUtils;
import com.arcsoft.asg.libcommon.util.common.ToastUtils;
import com.arcsoft.asg.libcommon.util.common.UUIDUtils;
import com.arcsoft.asg.libdeviceadapt.bean.DeviceAdaptationInfo;
import com.arcsoft.asg.libdeviceadapt.repos.AdaptationInfoDataUtils;
import com.arcsoft.faceengine.Config;
import com.arcsoft.faceengine.ErrorInfo;
import com.arcsoft.faceengine.FaceInfo;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

public class TakePhotoRepository implements IPhoto, CameraViewController.Callback {

    private boolean clickTakePhoto;
    private int previewWidth;
    private int previewHeight;
    private DeviceAdaptationInfo adaptationInfo;

    private List<FaceInfo> faceInfoList;
    private CameraViewParam mainCameraParam;
    private ICamera mainCameraManager;
    private CameraViewController cameraController;
    private ITakePhotoCallback callback;
    private FaceEngineManager faceEngineManager;

    public TakePhotoRepository() {
    }

    @Override
    public void init() {
        adaptationInfo = CommonRepository.getInstance().getAdaptationInfo();
        previewWidth = adaptationInfo.getPreviewWidth();
        previewHeight = adaptationInfo.getPreviewHeight();
        cameraController = new CameraViewController();
        cameraController.setCallback(this);
        mainCameraParam = RecognizeRepDataManager.getInstance().getCameraParam(true, adaptationInfo);
        faceInfoList = new ArrayList<>();
        getMainCameraManager().setParam(mainCameraParam);
        initEngine();
    }

    private ICamera getMainCameraManager() {
        if (mainCameraManager == null) {
            mainCameraManager = new Camera1Manager(CameraEngineUtils.createCameraFaceEngine());
            mainCameraManager.setMainCamera(true);
        }
        return mainCameraManager;
    }

    public void bindCamera(CameraFaceView cameraFaceView, ITakePhotoCallback callback) {
        this.callback = callback;
        cameraController.bind(getMainCameraManager(), cameraFaceView);
    }

    private void initEngine() {
        faceEngineManager = new FaceEngineManager();
        faceEngineManager.createFaceEngine();
        Config config = new Config();
        config.detectPriority = AdaptationInfoDataUtils.getFaceEnginePri(adaptationInfo);
        config.distance = AdaptationInfoDataUtils.getFaceEngineDistance(adaptationInfo);
        Disposable disposable = Observable.create((ObservableEmitter<Integer> emitter) -> {
            emitter.onNext(faceEngineManager.initFaceEngine(config));
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

    public void onActivityResume() {
        cameraController.startAllCamera();
    }

    public void onActivityPause() {
        cameraController.stopAllCamera();
    }

    public void onActivityDestroyed() {
        if (faceEngineManager != null) {
            faceEngineManager.unInitFaceEngine();
            faceEngineManager = null;
        }
        cameraController.unInit();
        cameraController = null;
        adaptationInfo = null;
        mainCameraParam = null;
        mainCameraManager = null;
    }

    public void setClickTakePhoto(boolean clickTakePhoto) {
        this.clickTakePhoto = clickTakePhoto;
    }

    @Override
    public void onCameraOpened(ICamera iCamera) {
        if (callback != null) {
            callback.onCameraOpened();
        }
    }

    @Override
    public void onCameraClosed(ICamera iCamera) {
    }

    @Override
    public void onCameraError(ICamera iCamera, Exception e) {
    }

    @Override
    public void onPreviewCreated(ICamera iCamera, CameraFaceView cameraFaceView) {

    }

    @Override
    public void onPreviewChanged(ICamera iCamera, CameraFaceView cameraFaceView, int i2, int i3) {
    }

    @Override
    public void onCameraPreview(byte[] nv21, ICamera iCamera) {
        if (iCamera.isMainCamera()) {
            faceInfoList.clear();
            if (faceEngineManager == null) {
                return;
            }
            faceEngineManager.track(nv21, previewWidth, previewHeight, faceInfoList);
            if (faceInfoList.size() == 0) {
                if (clickTakePhoto) {
                    clickTakePhoto = false;
                    ToastUtils.showShortToast(CommonUtils.getStrFromRes(R.string.face_manager_tip_detect_no_face_video));
                }
                return;
            }
            if (faceInfoList.size() > 1) {
                if (clickTakePhoto) {
                    clickTakePhoto = false;
                    ToastUtils.showShortToast(CommonUtils.getStrFromRes(R.string.face_manager_tip_more_than_one_face));
                }
                return;
            }
            if (clickTakePhoto) {
                clickTakePhoto = false;
                Bitmap bitmap = ImageFileUtils.nv21ToBitmap(nv21, previewWidth, previewHeight);
                Bitmap rotationBmp;
                switch (adaptationInfo.getFaceDetectDegree()) {
                    case Constants.DEGREE_90_STRING:
                        rotationBmp = ImageFileUtils.getRotateBitmap(bitmap, 90, adaptationInfo.isMainCameraMirror());
                        break;
                    case Constants.DEGREE_180_STRING:
                        rotationBmp = ImageFileUtils.getRotateBitmap(bitmap, 180, adaptationInfo.isMainCameraMirror());
                        break;
                    case Constants.DEGREE_270_STRING:
                        rotationBmp = ImageFileUtils.getRotateBitmap(bitmap, 270, adaptationInfo.isMainCameraMirror());
                        break;
                    default:
                        rotationBmp = ImageFileUtils.getRotateBitmap(bitmap, 0, adaptationInfo.isMainCameraMirror());
                        break;
                }
                String personSerial = CommonUtils.createPersonSerial();
                String imageName = personSerial + "_" + UUIDUtils.getUUID32();
                String imgPath = CommonUtils.getPersonFaceLocalPath(imageName);
                if (ImageFileUtils.save(rotationBmp, imgPath, Bitmap.CompressFormat.JPEG, true)) {
                    if (callback != null) {
                        callback.onTakePhoto(personSerial, imgPath);
                    }
                }
            }
        }
    }
}
