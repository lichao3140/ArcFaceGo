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

package com.arcsoft.arcfacesingle.business.recognize;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.business.common.CommonRepository;
import com.arcsoft.arcfacesingle.business.setting.ConfigConstants;
import com.arcsoft.arcfacesingle.business.setting.SettingRepDataManager;
import com.arcsoft.arcfacesingle.data.db.dao.PersonDao;
import com.arcsoft.arcfacesingle.data.db.dao.PersonFaceDao;
import com.arcsoft.arcfacesingle.data.db.dao.PersonPermissionDao;
import com.arcsoft.arcfacesingle.data.db.table.TablePerson;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonFace;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonPermission;
import com.arcsoft.arcfacesingle.data.db.table.TableSettingConfigInfo;
import com.arcsoft.arcfacesingle.data.event.DynamicAddPersonInfoEvent;
import com.arcsoft.arcfacesingle.libutil.CameraEngineUtils;
import com.arcsoft.arcfacesingle.server.faceengine.FaceEngineManager;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.arcfacesingle.util.scheduler.BaseObserver;
import com.arcsoft.asg.libcamera.bean.CameraViewParam;
import com.arcsoft.asg.libcamera.bean.FaceRectInfo;
import com.arcsoft.asg.libcamera.contract.ICamera;
import com.arcsoft.asg.libcamera.controller.CameraViewController;
import com.arcsoft.asg.libcamera.impl.Camera1Manager;
import com.arcsoft.asg.libcamera.view.CameraFaceView;
import com.arcsoft.asg.libcommon.manage.SoundPlayerManager;
import com.arcsoft.asg.libcommon.manage.TextToSpeechManager;
import com.arcsoft.asg.libcommon.util.business.rx.RxUtils;
import com.arcsoft.asg.libcommon.util.common.AppUtils;
import com.arcsoft.asg.libcommon.util.common.ToastUtils;
import com.arcsoft.asg.libdeviceadapt.bean.DeviceAdaptationInfo;
import com.arcsoft.faceengine.Config;
import com.arcsoft.faceengine.ErrorInfo;
import com.arcsoft.faceengine.FaceEngine;
import com.arcsoft.faceengine.FaceInfo;
import com.arcsoft.faceengine.PersonInfo;
import com.arcsoft.faceengine.RecognitionResult;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class RecognizeRepository implements IRecognize, CameraViewController.Callback, FaceEngine.RecognizeCallback {

    private static final String TAG = RecognizeRepository.class.getSimpleName();
    private static final int DELAY_CLOSE_PASSWORD_DIALOG_TIME = 2 * 60 * 1000;
    private static final int DELAY_CLOSE_SHOW_FACE_CAMERA = 1000;
    private static final int EXIT_TIME_DELAY = 1000;
    public static final int FACE_RESULT_NORMAL = 0;
    public static final int FACE_RESULT_SUCCESS = 1;
    public static final int FACE_RESULT_FAILED = 2;
    public static final int FACE_RESULT_UNAUTHORIZED = 3;

    private volatile ConcurrentHashMap<Integer, Disposable> showHintViewTimerMap = new ConcurrentHashMap<>();

    private volatile byte[] irData;
    private boolean viceCameraPreviewShow;
    private long exitTime = 0;
    private boolean indexDialogShow;
    private int recognizeResultTrackId;
    private int showFaceUpCameraTrackId;
    private boolean onResume;

    private DeviceAdaptationInfo adaptationInfo;
    private CameraViewController cameraController;
    private ICamera mainCameraManager;
    private ICamera viceCameraManager;
    private FaceEngineManager faceEngineManager;
    private Config faceEngineConfig;
    private List<PersonInfo> personInfoList;
    private List<FaceRectInfo> drawInfoList;
    private List<FaceRectInfo> irDrawInfoList;
    private Disposable delayClosePassDis;
    private ViceCameraCallback viceCameraCallback;
    private WeakReference<RecognizeRespListener> recognizeListener;

    public RecognizeRepository(RecognizeRespListener recognizeRespListener) {
        if (recognizeRespListener != null) {
            recognizeListener = new WeakReference<>(recognizeRespListener);
        }
    }

    @Override
    public void init() {
        drawInfoList = new ArrayList<>();
        irDrawInfoList = new ArrayList<>();
        personInfoList = new ArrayList<>();
        adaptationInfo = CommonRepository.getInstance().getAdaptationInfo();

        cameraController = new CameraViewController();
        cameraController.setCallback(this);
        createMainCameraManager();
        if (getAdaptationInfo().getCameraCount() > 1) {
            createViceCameraManager();
            boolean useIrLiveCheck = SettingRepDataManager.getInstance().isIrFaceLiveOpen(getSettingConfigInfo());
            if (!useIrLiveCheck) {
                setViceCameraPreviewShow(true);
            }
        } else {
            setViceCameraPreviewShow(true);
        }

        faceEngineManager = new FaceEngineManager();
        faceEngineManager.createFaceEngine(this);

        CommonRepository.getInstance().initService();
        TextToSpeechManager.getInstance().init();
        SoundPlayerManager.getInstance().init(RecognizeRepDataManager.getInstance().getSoundDataList());
        CommonRepository.getInstance().initMainLogo();
        CommonRepository.getInstance().initSecondLogo();
    }

    public ICamera getMainCameraManager() {
        return mainCameraManager;
    }

    public ICamera getViceCameraManager() {
        return viceCameraManager;
    }

    private void createMainCameraManager() {
        CameraViewParam mainCameraParam = RecognizeRepDataManager.getInstance().getCameraParam(true, getAdaptationInfo());
        mainCameraManager = new Camera1Manager(CameraEngineUtils.createCameraFaceEngine());
        mainCameraManager.setMainCamera(true);
        mainCameraManager.setParam(mainCameraParam);
    }

    private void createViceCameraManager() {
        CameraViewParam viceCameraParam = RecognizeRepDataManager.getInstance().getCameraParam(false, getAdaptationInfo());
        viceCameraManager = new Camera1Manager(CameraEngineUtils.createCameraFaceEngine());
        viceCameraManager.setMainCamera(false);
        viceCameraManager.setParam(viceCameraParam);
    }

    public TableSettingConfigInfo getSettingConfigInfo() {
        return CommonRepository.getInstance().getSettingConfigInfo();
    }

    public void bindMainCamera(CameraFaceView faceView) {
        cameraController.bind(getMainCameraManager(), faceView);
    }

    public void bindViceCamera(CameraFaceView faceView, ViceCameraCallback cameraCallback) {
        this.viceCameraCallback = cameraCallback;
        cameraController.bind(getViceCameraManager(), faceView);
    }

    private ViceCameraCallback getViceCameraCallback() {
        return viceCameraCallback;
    }

    public DeviceAdaptationInfo getAdaptationInfo() {
        return adaptationInfo != null ? adaptationInfo : CommonRepository.getInstance().getAdaptationInfo();
    }

    @Override
    public void setIndexDialogShow(boolean indexDialogShow) {
        this.indexDialogShow = indexDialogShow;
        if (indexDialogShow && faceEngineManager != null) {
            faceEngineManager.pauseRecognize();
        }
    }

    @Override
    public Bitmap getMainLogo(String uri) {
        return RecognizeRepDataManager.getInstance().getMainLogo(uri);
    }

    @Override
    public Bitmap getSecondLogo(String url) {
        return RecognizeRepDataManager.getInstance().getSecondLogo(url);
    }

    @Override
    public void loadFaceInfoList() {
        Disposable disposable = Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
            List<TablePersonFace> tablePersonFaces = PersonFaceDao.getInstance().queryAllFace();
            personInfoList.clear();
            for (TablePersonFace face : tablePersonFaces) {
                PersonInfo personInfo = new PersonInfo(face.id, face.feature);
                personInfoList.add(personInfo);
            }
            if (faceEngineManager != null && faceEngineManager.getFaceEngine() != null) {
                faceEngineManager.clearPersonInfo();
                faceEngineManager.addPersonInfoList(personInfoList);
            }
            emitter.onNext(1);
            emitter.onComplete();
        }).compose(RxUtils.ioToMain())
                .subscribe();
    }

    public void addPersonFaceFromRemote(DynamicAddPersonInfoEvent event) {
        PersonInfo personInfo = new PersonInfo(event.personFaceId, event.feature);
        if (event.bAdd) {
            personInfoList.add(personInfo);
            if (faceEngineManager != null) {
                faceEngineManager.addPersonInfo(personInfo);
            }
        } else {
            int idx = -1;
            for (int i = 0; i < personInfoList.size(); i++) {
                if (personInfoList.get(i).getPersonId() == personInfo.getPersonId()) {
                    idx = i;
                    break;
                }
            }
            if (idx != -1) {
                personInfoList.set(idx, personInfo);
                if (faceEngineManager != null) {
                    faceEngineManager.updatePersonInfo(personInfo);
                }
            }
        }
    }

    public void deletePersonFaceFromRemote(List<Long> deletedId) {
        if (deletedId != null && !deletedId.isEmpty()) {
            for (long faceId : deletedId) {
                for (int i = 0; i < personInfoList.size(); i++) {
                    PersonInfo personInfo = personInfoList.get(i);
                    long personFaceId = personInfo.getPersonId();
                    if (personFaceId == faceId) {
                        personInfoList.remove(i);
                        if (faceEngineManager != null) {
                            faceEngineManager.removePersonInfo(personFaceId);
                        }
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void stopCameraAndRelease() {
        if (onResume) {
            cameraController.stopAllCamera();
        }
        setViceCameraPreviewShow(false);
        if (getListener() != null) {
            getListener().onFaceTrackNoFaceInfo();
        }
    }

    @Override
    public void startMainCamera() {
        setIndexDialogShow(false);
        updateFaceEngineConfig();
        setViceCameraPreviewShow(true);
        if (onResume) {
            cameraController.startCameraDelayed(getMainCameraManager());
        }
    }

    @Override
    public void startMainAndViceCamera(CameraFaceView viceCameraView) {
        setIndexDialogShow(false);
        CameraViewParam mainCameraParam = RecognizeRepDataManager.getInstance().getCameraParam(true, getAdaptationInfo());
        getMainCameraManager().setParam(mainCameraParam);
        updateFaceEngineConfig();
        if (onResume) {
            cameraController.startAllCamera();
        }
    }

    public void initConfigAndFaceEngine() {
        try {
            Config faceEngineConfig = RecognizeRepDataManager.getInstance().getFaceEngineConfig(adaptationInfo);
            if (faceEngineManager != null) {
                Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
                    unInitFaceEngine();
                    faceEngineManager.createFaceEngine(RecognizeRepository.this);
                    int resultCode = faceEngineManager.initFaceEngine(faceEngineConfig);
                    emitter.onNext(resultCode);
                    emitter.onComplete();
                }).compose(RxUtils.ioToMain())
                        .subscribe(new BaseObserver<Integer>() {
                            @Override
                            public void onNext(Integer resultCode) {
                                if (resultCode == ErrorInfo.MOK) {
                                    loadFaceInfoList();
                                } else {
                                    String strToast = CommonUtils.getInitFaceEngineResult(resultCode);
                                    if (getListener() != null) {
                                        getListener().onEngineCallBack(strToast);
                                    }
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                if (getListener() != null) {
                                    getListener().onEngineCallBack(e.getMessage());
                                }
                            }
                        });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateFaceEngineConfig() {
        try {
            Config faceEngineConfig = RecognizeRepDataManager.getInstance().getFaceEngineConfig(adaptationInfo);
            if (faceEngineManager != null) {
                int result = faceEngineManager.updateConfig(faceEngineConfig);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unInitFaceEngine() {
        if (faceEngineManager != null) {
            faceEngineManager.unInitFaceEngine(true);
        }
    }

    public boolean reInitEngine() {
        if (faceEngineManager != null) {
            unInitFaceEngine();
            faceEngineManager.createFaceEngine(RecognizeRepository.this);
            int resultCode = faceEngineManager.initFaceEngine(faceEngineConfig);
            if (resultCode == ErrorInfo.MOK) {
                loadFaceInfoList();
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    @Override
    public void onCameraOpened(ICamera camera) {
        if (camera.isMainCamera()) {
            CommonRepository.getInstance().sendBroadcast(Constants.ACTION_START_IDENTIFY);
        }
        if (getListener() != null) {
            getListener().onCameraOpened(camera.isMainCamera());
        }
    }

    @Override
    public void onCameraClosed(ICamera camera) {
        if (!camera.isMainCamera()) {
            irData = null;
        }
    }

    @Override
    public void onCameraError(ICamera camera, Exception e) {
        if (camera.isMainCamera()) {
            if (cameraController != null) {
                cameraController.stopCamera(camera);
            }
        } else {
            boolean useIrLiveCheck = SettingRepDataManager.getInstance().isIrFaceLiveOpen(getSettingConfigInfo());
            if (useIrLiveCheck) {
                if (getViceCameraCallback() != null) {
                    getViceCameraCallback().onViceCameraFail();
                }
                if (cameraController != null) {
                    cameraController.stopCamera(camera);
                }
                irData = null;
            }
        }
    }

    @Override
    public void onPreviewCreated(ICamera camera, CameraFaceView cameraFaceView) {
        if (!camera.isMainCamera() && getListener() != null) {
            getListener().setViceCameraViewVisible();
        }
    }

    @Override
    public void onPreviewChanged(ICamera iCamera, CameraFaceView cameraFaceView, int i2, int i3) {
    }

    @Override
    public void onCameraPreview(byte[] bytes, ICamera iCamera) {
        if (iCamera.isMainCamera()) {
            processRgbCamera(bytes);
        } else {
            processIrCamera(bytes);
        }
    }

    private void processRgbCamera(byte[] bytes) {
        if (indexDialogShow) {
            updateFaceView(null);
            if (getListener() != null) {
                getListener().onFaceTrackNoFaceInfo();
            }
            return;
        }
        if (faceEngineManager != null) {
            if (!isViceCameraPreviewShow()) {
                return;
            }
            FaceInfo faceInfo = new FaceInfo();
            faceEngineManager.recognize(bytes, irData, adaptationInfo.getPreviewWidth(), adaptationInfo.getPreviewHeight(), faceInfo);
            if (faceInfo.faceId == -1) {
                //无人脸
                recognizeResultTrackId = -1;
                updateFaceView(null);
                if (getListener() != null) {
                    getListener().onFaceTrackNoFaceInfo();
                }
            } else {
                //有人脸
                updateFaceView(faceInfo);
                if (getListener() != null) {
                    getListener().onFaceTrackHasFaceInfo(faceInfo.faceId);
                }
            }
        }
    }

    private void processIrCamera(byte[] bytes) {
        setViceCameraPreviewShow(true);
        if (irData == null || irData.length != bytes.length) {
            irData = new byte[bytes.length];
        }
        try {
            System.arraycopy(bytes, 0, irData, 0, bytes.length);
        } catch (Exception e) {
            irData = null;
        }
    }

    private void updateFaceView(FaceInfo faceInfo) {
        drawInfoList.clear();
        irDrawInfoList.clear();
        boolean useIrLiveCheck = SettingRepDataManager.getInstance().isIrFaceLiveOpen(getSettingConfigInfo());
        if (faceInfo != null) {
            Rect rect = faceInfo.faceRect;
            drawInfoList.add(new FaceRectInfo(rect, "", RecognizeRepDataManager.RECT_BORDER_WIDTH));
            if (useIrLiveCheck) {
                RecognizeRepDataManager.getInstance().transferIrFaceInfoList(rect, getAdaptationInfo(), irDrawInfoList);
            }
        }
        if (cameraController != null) {
            cameraController.refreshFaceRect(getMainCameraManager(), drawInfoList);
            if (useIrLiveCheck && isViceCameraPreviewShow() && getViceCameraManager() != null) {
                cameraController.refreshFaceRect(getViceCameraManager(), irDrawInfoList);
            }
        }
    }

    @Override
    public void onRecognizeStart(int faceId) {
        if (getListener() != null) {
            getListener().setFaceLiveResult("");
        }
        if (getListener() != null) {
            getListener().onHideRecognitionResult();
        }
    }

    @Override
    public void onRecognizeLiving(int faceId, int isLive) {
        String content = "";
        if (isLive == 0) {
            content = AppUtils.getString(R.string.non_living);
        } else if (isLive == 1) {
            content = AppUtils.getString(R.string.living);
        }
        if (getListener() != null) {
            getListener().setFaceLiveResult(content);
        }
    }

    @Override
    public void onRecognizeFeedback(int faceId, int result) {
        if (result == ErrorInfo.MERR_ASF_ENGINE_QUALITY_FAIL) {
            showHintView(AppUtils.getString(R.string.fq_fail_hint), faceId);
        } else if (result != ErrorInfo.MERR_ASF_ENGINE_NO_PERSON) {
            showHintView(AppUtils.getString(R.string.please_face_up_the_screen), faceId);
        }
    }

    @Override
    public void onRecognizeComplete(RecognitionResult recognitionResult) {
        if (recognitionResult != null) {
            FaceInfo faceInfo = recognitionResult.faceInfo;
            int faceId = faceInfo.faceId;
            int result = recognitionResult.result;
            byte[] nv21Data = recognitionResult.rgbByte;
            switch (result) {
                case ErrorInfo.MERR_ASF_ENGINE_NOT_LIVENESS:
                    faceCheckFailed(faceInfo, faceId, nv21Data, RecognizeRepDataManager.FAILED_LIVE_DETECT_CHECK);
                    break;
                case ErrorInfo.MERR_ASF_ENGINE_LIVENESS_FAIL:
                case ErrorInfo.MERR_ASF_ENGINE_NOT_SAME_FACE:
                case ErrorInfo.MERR_ASF_ENGINE_IR_NO_FACE:
                case ErrorInfo.MERR_ASF_ENGINE_EXTRACT_FAIL:
                    //活体和特征提取并行处理失败
                    faceCheckFailed(faceInfo, faceId, nv21Data, RecognizeRepDataManager.FAILED_LIVE_AND_EXTRACT);
                    break;
                case ErrorInfo.MERR_ASF_ENGINE_LOW_LEVEL:
                    //比对不通过
                    faceCheckFailed(faceInfo, faceId, nv21Data, RecognizeRepDataManager.FAILED_THRESHOLD_COMPARE);
                    break;
                case ErrorInfo.MOK:
                    //比对通过
                    processFaceComplete(faceInfo, recognitionResult);
                    break;
                default:
                    break;
            }
        }
    }

    private void processFaceComplete(FaceInfo faceInfo, RecognitionResult recognitionResult) {
        int faceId = faceInfo.faceId;
        byte[] nv21Data = recognitionResult.rgbByte;
        TablePersonFace tablePersonFace = PersonFaceDao.getInstance().getPersonById(recognitionResult.personId);
        if (tablePersonFace != null) {
            String personSerial = tablePersonFace.personSerial;
            TablePerson tablePerson = PersonDao.getInstance().getPersonByPersonSerial(personSerial);
            List<TablePersonPermission> permissions = PersonPermissionDao.getInstance().getListByPersonSerial(tablePerson.personSerial);
            boolean hasPermission = RecognizeRepDataManager.getInstance().doorAuthorityWithTime(tablePerson, permissions);
            int successType = RecognizeRepDataManager.SUCCESS_THRESHOLD_COMPARE;
            if (hasPermission) {
                if (getListener() != null) {
                    recognizeResultTrackId = faceId;
                    getListener().onRecognitionSuccess(tablePerson, tablePersonFace, faceId);
                }
                RecognizeRepDataManager.getInstance().playRecognitionSuccessSound(tablePerson, getSettingConfigInfo());
                CommonRepository.getInstance().openDoor();
                CommonRepository.getInstance().openLamp(CommonRepository.OPEN_GREEN_LAMP);
                CommonRepository.getInstance().sendBroadcast(Constants.ACTION_IDENTIFY_SUCCESSFUL);
                Bundle bundle = new Bundle();
                bundle.putString(Constants.BUNDLE_KEY_PERSON_SERIAL, personSerial);
                CommonRepository.getInstance().sendBroadcastWithBundle(Constants.ACTION_IDENTIFY_SUCCESS_PERSON_SERIAL, bundle);
            } else {
                if (getListener() != null) {
                    recognizeResultTrackId = faceId;
                    getListener().onRecognitionUnauthorized(tablePerson, tablePersonFace, faceId);
                }
                RecognizeRepDataManager.getInstance().playRecognitionUnauthorizedSound();
                CommonRepository.getInstance().sendBroadcast(Constants.ACTION_IDENTIFY_NO_PERMISSION_ACCESS);
                successType = RecognizeRepDataManager.SUCCESS_THRESHOLD_COMPARE_UNAUTHORIZED;
            }
            RecognizeRepDataManager.getInstance().saveRecognitionResult(successType, faceInfo, nv21Data, tablePersonFace,
                    getSettingConfigInfo(), getAdaptationInfo().getPreviewWidth(), getAdaptationInfo().getPreviewHeight());
        } else {
            faceCheckFailed(faceInfo, faceId, nv21Data, RecognizeRepDataManager.FAILED_PROCESSING_FACE_PERSON_REMOVE);
        }
    }

    private void showHintView(String content, Integer faceId) {
        if (getListener() != null) {
            getListener().setHintContent(content);
        }
        Disposable disposable = showHintViewTimerMap.get(faceId);
        if (disposable == null) {
            if (getListener() != null) {
                showFaceUpCameraTrackId = faceId;
                getListener().setFaceUpCamera(true);
            }
            showHintViewTimerMap.put(faceId, Observable.timer(DELAY_CLOSE_SHOW_FACE_CAMERA, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aLong -> {
                        showHintViewTimerMap.remove(faceId);
                        if (getListener() != null) {
                            getListener().setFaceUpCamera(false);
                        }
                    }));
        }
    }

    public void cancelHintViewDisposable(Integer trackId) {
        Disposable disposable = showHintViewTimerMap.get(trackId);
        if (disposable != null) {
            if (!disposable.isDisposed()) {
                disposable.dispose();
            }
            showHintViewTimerMap.remove(trackId);
        }
    }

    /**
     * 处理显示人脸检测失败弹框相关逻辑
     */
    private void faceCheckFailed(FaceInfo faceInfo, Integer faceId, byte[] nv21Data, int failedType) {
        TableSettingConfigInfo settingConfigInfo = getSettingConfigInfo();
        if (settingConfigInfo.getDisplayModeFail() != ConfigConstants.DISPLAY_MODE_FAILED_NOT_FEEDBACK) {
            CommonRepository.getInstance().openLamp(CommonRepository.OPEN_RED_LAMP);
        }
        if (getListener() != null) {
            recognizeResultTrackId = faceId;
            getListener().onRecognitionFail(faceId);
        }
        RecognizeRepDataManager.getInstance().playRecognitionFailureSound(settingConfigInfo);
        CommonRepository.getInstance().sendBroadcast(Constants.ACTION_IDENTIFY_FAILED);
        RecognizeRepDataManager.getInstance().saveRecognitionResult(failedType, faceInfo, nv21Data, null,
                settingConfigInfo, adaptationInfo.getPreviewWidth(), adaptationInfo.getPreviewHeight());
    }

    @Override
    public void startClosePasswordDialogTimer(RecognizeCallback callback) {
        disposeClosePassDialogTimer();
        delayClosePassDis = Observable.timer(DELAY_CLOSE_PASSWORD_DIALOG_TIME, TimeUnit.MILLISECONDS)
                .compose(RxUtils.computingToMain())
                .subscribe(aLong -> {
                    callback.cancelPasswordDialog();
                    delayClosePassDis = null;
                });
    }

    @Override
    public void disposeClosePassDialogTimer() {
        if (delayClosePassDis != null && !delayClosePassDis.isDisposed()) {
            delayClosePassDis.dispose();
            delayClosePassDis = null;
        }
    }

    @Override
    public void onActivityResume() {
        initConfigAndFaceEngine();
        cameraController.startCameraDelayed(getMainCameraManager());
        boolean useIrLiveCheck = SettingRepDataManager.getInstance().isIrFaceLiveOpen(getSettingConfigInfo());
        if (useIrLiveCheck && adaptationInfo.getCameraCount() > 1) {
            cameraController.startCameraDelayed(getViceCameraManager());
        } else {
            setViceCameraPreviewShow(true);
        }
        onResume = true;
    }

    @Override
    public void onActivityPause() {
        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            unInitFaceEngine();
        }).compose(RxUtils.ioToMain()).subscribe();
        cameraController.stopAllCamera();
        drawInfoList.clear();
        irDrawInfoList.clear();
        cameraController.refreshFaceRect(getMainCameraManager(), drawInfoList);
        boolean useIrLiveCheck = SettingRepDataManager.getInstance().isIrFaceLiveOpen(getSettingConfigInfo());
        if (useIrLiveCheck && isViceCameraPreviewShow()) {
            cameraController.refreshFaceRect(getViceCameraManager(), irDrawInfoList);
        }
        if (getListener() != null) {
            getListener().onFaceTrackNoFaceInfo();
        }
        setViceCameraPreviewShow(false);
        CommonRepository.getInstance().sendBroadcast(Constants.ACTION_STOP_IDENTIFY);
        onResume = false;
    }

    @Override
    public void onActivityDestroy() {
        disposeClosePassDialogTimer();
        if (faceEngineManager != null) {
            unInitFaceEngine();
            faceEngineManager = null;
        }
        if (cameraController != null) {
            cameraController.unInit();
            cameraController = null;
        }
        adaptationInfo = null;
        mainCameraManager = null;
        viceCameraManager = null;
        faceEngineConfig = null;
        showHintViewTimerMap = null;
        personInfoList = null;
        drawInfoList = null;
        irDrawInfoList = null;
    }

    private RecognizeRespListener getListener() {
        return recognizeListener != null ? recognizeListener.get() : null;
    }

    /**
     * 双击退出APP
     */
    public void doubleClickExitApp() {
        if ((System.currentTimeMillis() - exitTime) > EXIT_TIME_DELAY) {
            ToastUtils.showShortToast(R.string.press_the_exit_procedure_again);
            exitTime = System.currentTimeMillis();
        } else {
            CommonRepository.getInstance().exitApp();
        }
    }

    public boolean isViceCameraPreviewShow() {
        return viceCameraPreviewShow;
    }

    public void setViceCameraPreviewShow(boolean viceCameraPreviewShow) {
        this.viceCameraPreviewShow = viceCameraPreviewShow;
    }

    public int getRecognizeResultTrackId() {
        return recognizeResultTrackId;
    }

    public int getShowFaceUpCameraTrackId() {
        return showFaceUpCameraTrackId;
    }
}
