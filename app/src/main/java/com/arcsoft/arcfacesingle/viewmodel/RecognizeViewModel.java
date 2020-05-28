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

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.business.common.CommonRepository;
import com.arcsoft.arcfacesingle.business.recognize.RecognizeRepDataManager;
import com.arcsoft.arcfacesingle.business.recognize.RecognizeRepository;
import com.arcsoft.arcfacesingle.business.recognize.RecognizeRespListener;
import com.arcsoft.arcfacesingle.business.setting.ConfigConstants;
import com.arcsoft.arcfacesingle.business.setting.SettingRepDataManager;
import com.arcsoft.arcfacesingle.data.db.table.TablePerson;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonFace;
import com.arcsoft.arcfacesingle.data.db.table.TableSettingConfigInfo;
import com.arcsoft.arcfacesingle.data.event.DynamicAddPersonInfoEvent;
import com.arcsoft.arcfacesingle.navigator.RecognizeNavigator;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.asg.libcamera.view.CameraFaceView;
import com.arcsoft.asg.libcommon.base.BaseDialogFragment;
import com.arcsoft.asg.libcommon.util.business.DoubleClickUtils;
import com.arcsoft.asg.libcommon.util.business.rx.RxUtils;
import com.arcsoft.asg.libcommon.util.common.ActivityUtils;
import com.arcsoft.asg.libcommon.util.common.ArithmeticUtils;
import com.arcsoft.asg.libcommon.util.common.DeviceUtils;
import com.arcsoft.asg.libcommon.util.common.KeyboardUtils;
import com.arcsoft.asg.libcommon.util.common.PermissionUtils;
import com.arcsoft.asg.libcommon.util.common.ScreenUtils;
import com.arcsoft.asg.libcommon.util.common.TimeUtils;
import com.arcsoft.asg.libcommon.util.common.ToastUtils;
import com.arcsoft.asg.libcommon.util.common.Utils;
import com.arcsoft.asg.libdeviceadapt.bean.DeviceAdaptationInfo;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class RecognizeViewModel extends BaseObservable {

    private static final String TAG = RecognizeViewModel.class.getSimpleName();
    private static final int DELAY_SHOW_NO_FACE_SCREEN_BACKGROUND = 2000;

    /**
     * 系统亮度
     */
    private int systemBright;
    /**
     * 屏保背景亮度
     */
    private int screenDefaultBright;

    /**
     * 是否已切换至无人脸检测时的UI
     */
    private volatile boolean alreadyShowNoFaceScreen;
    /**
     * 从有人脸-无人脸，不能立马切换UI，需要加一个延迟，否则UI切换台频繁会闪烁
     */
    private Disposable noFaceDelayDisposable;

    private RecognizeNavigator navigator;
    private RecognizeRepository repository;
    private Disposable timerDisposable;
    private Disposable mainLogoDisposable;
    private Disposable subLogoDisposable;

    @Bindable
    public final ObservableBoolean rlDefBackgroundPorVisible = new ObservableBoolean(false);
    public final ObservableBoolean flTextureViewVisible = new ObservableBoolean(true);
    public final ObservableBoolean rlRecognizeFaceResultVisible = new ObservableBoolean(false);
    public final ObservableField<String> time = new ObservableField<>();
    public final ObservableField<String> faceResult = new ObservableField<>();
    public final ObservableField<String> faceResultHeadPath = new ObservableField<>();
    public final ObservableField<Bitmap> mainBmpField = new ObservableField<>();
    public final ObservableField<Boolean> ivSettingVisible = new ObservableField<>();
    public final ObservableField<Boolean> irPreviewVisible = new ObservableField<>();
    public final ObservableField<String> strFaceLiveResult = new ObservableField<>();
    public final ObservableField<Integer> faceResultType = new ObservableField<>(RecognizeRepository.FACE_RESULT_NORMAL);
    public final ObservableBoolean fieldFaceCameraVisible = new ObservableBoolean(false);
    public final ObservableField<String> fieldMainTitle = new ObservableField<>();
    public final ObservableField<Bitmap> subBmpField = new ObservableField<>();
    public final ObservableField<String> fieldSimpleDate = new ObservableField<>();
    public final ObservableField<String> fieldHint = new ObservableField<>();
    public final ObservableField<Boolean> fieldBorderVisible = new ObservableField<>(false);
    public final ObservableField<Boolean> fieldFullScreen = new ObservableField<>(true);

    public RecognizeViewModel() {
        repository = new RecognizeRepository(recognizeListener);
        TableSettingConfigInfo settingConfigInfo = repository.getSettingConfigInfo();
        if (!settingConfigInfo.isScreenBrightFollowSys()) {
            systemBright = DeviceUtils.getSystemBrightness(ActivityUtils.getTopActivity());
            String strPercent = settingConfigInfo.getScreenDefBrightPercent();
            if (!TextUtils.isEmpty(strPercent)) {
                int screenDefaultBrightPercent = Integer.parseInt(settingConfigInfo.getScreenDefBrightPercent());
                screenDefaultBright = (int) ArithmeticUtils.mul(ArithmeticUtils.div(screenDefaultBrightPercent, 100), 255);
            } else {
                screenDefaultBright = (int) ArithmeticUtils.mul(ArithmeticUtils.div(100, 100), 255);
            }
        }
    }

    public void setNavigator(RecognizeNavigator navigator) {
        this.navigator = navigator;
        initPermissions();
        ivSettingVisible.set(true);
    }

    /**
     * 初始化权限设置
     */
    private void initPermissions() {
        PermissionUtils.permission(CommonUtils.getNeededPermissions())
                .callback(new PermissionUtils.FullCallback() {
                    @Override
                    public void onGranted(List<String> permissionsGrantedList) {
                        if (repository != null) {
                            repository.init();
                            DeviceAdaptationInfo adaptationInfo = repository.getAdaptationInfo();
                            if (adaptationInfo.getCameraCount() <= 1) {
                                showIrPreviewVisible(false);
                            }
                        }
                        if (navigator != null) {
                            navigator.initCamera();
                        }
                    }

                    @Override
                    public void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied) {
                        CommonRepository.getInstance().exitApp();
                    }
                })
                .request();
    }

    /**
     * 初始化View
     */
    public void initView() {
        setTopLogo();
        setSecondLogo();
        initCompanyInfo();
    }

    /**
     * 加载人员信息数据
     */
    public void loadPersonFaceList() {
        if (repository != null) {
            repository.loadFaceInfoList();
        }
    }

    /**
     * 初始化相机和预览View
     * @param mainFaceView 主相机和预览View
     * @param viceFaceView 副相机和预览View
     */
    public void initCameraView(CameraFaceView mainFaceView, CameraFaceView viceFaceView) {
        DeviceAdaptationInfo adaptationInfo = repository.getAdaptationInfo();
        repository.bindMainCamera(mainFaceView);
        TableSettingConfigInfo settingConfigInfo = repository.getSettingConfigInfo();
        if (adaptationInfo.getCameraCount() > 1) {
            boolean useIrLiveCheck = SettingRepDataManager.getInstance().isIrFaceLiveOpen(settingConfigInfo);
            if (useIrLiveCheck) {
                boolean showPreview = SettingRepDataManager.getInstance().isIrFaceLivePreviewShow(settingConfigInfo);
                if (showPreview) {
                    changeViceCameraViewUi(viceFaceView, false);
                } else {
                    changeViceCameraViewUi(viceFaceView, true);
                }
            } else {
                changeViceCameraViewUi(viceFaceView, true);
            }
            repository.bindViceCamera(viceFaceView, () -> {
                showIrPreviewVisible(false);
                if (navigator != null) {
                    navigator.showIrDialog();
                }
            });
        }
    }

    /**
     * 重新开启相机预览
     * @param useIrFaceLive 是否使用ir活体检测
     * @param viceCameraView 副相机View
     */
    public void restartCamera(boolean useIrFaceLive, CameraFaceView viceCameraView) {
        clearTimerDisposables();
        refreshTime();
        if (repository != null) {
            repository.stopCameraAndRelease();
            if (useIrFaceLive) {
                repository.startMainAndViceCamera(viceCameraView);
            } else {
                repository.startMainCamera();
            }
        }
    }

    /**
     * 设置IR活体检测预览UI效果
     * @param cameraFaceView 相机预览View
     */
    public void setIrPreviewVisible(CameraFaceView cameraFaceView) {
        TableSettingConfigInfo settingConfigInfo = repository.getSettingConfigInfo();
        boolean useIrLiveCheck = SettingRepDataManager.getInstance().isIrFaceLiveOpen(settingConfigInfo);
        if (useIrLiveCheck) {
            boolean showPreview = SettingRepDataManager.getInstance().isIrFaceLivePreviewShow(settingConfigInfo);
            if (showPreview) {
                changeViceCameraViewUi(cameraFaceView, false);
            }
            showIrPreviewVisible(showPreview);
        } else {
            showIrPreviewVisible(false);
        }
    }

    private void showIrPreviewVisible(boolean show) {
        irPreviewVisible.set(show);
        if (navigator != null) {
            navigator.setViceCameraFaceVisible(show);
        }
    }

    /**
     * 设置主Logo
     */
    public void setTopLogo() {
        TableSettingConfigInfo settingConfigInfo = repository.getSettingConfigInfo();
        if (mainLogoDisposable != null && !mainLogoDisposable.isDisposed()) {
            mainLogoDisposable.dispose();
        }
        String url = settingConfigInfo.getMainImagePath();
        mainLogoDisposable = Observable.create((ObservableOnSubscribe<Bitmap>) emitter -> {
            Bitmap bitmap = repository.getMainLogo(url);
            if (bitmap != null) {
                emitter.onNext(bitmap);
                emitter.onComplete();
            } else {
                emitter.onError(new Throwable());
            }
        }).compose(RxUtils.ioToMain())
                .subscribe(bitmap -> {
                    mainBmpField.set(bitmap);
                    mainLogoDisposable = null;
                }, throwable -> {
                    mainBmpField.set(null);
                    mainLogoDisposable = null;
                });
    }

    /**
     * 设置副logo
     */
    public void setSecondLogo() {
        TableSettingConfigInfo settingConfigInfo = repository.getSettingConfigInfo();
        if (subLogoDisposable != null && !subLogoDisposable.isDisposed()) {
            subLogoDisposable.dispose();
        }
        String url = settingConfigInfo.getViceImagePath();
        subLogoDisposable = Observable.create((ObservableOnSubscribe<Bitmap>) emitter -> {
            Bitmap bitmap = repository.getSecondLogo(url);
            if (bitmap != null) {
                emitter.onNext(bitmap);
                emitter.onComplete();
            } else {
                emitter.onError(new Throwable());
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bmpLogo -> {
                    subBmpField.set(bmpLogo);
                    subLogoDisposable = null;
                }, throwable -> {
                    subBmpField.set(null);
                    subLogoDisposable = null;
                });
    }

    /**
     * 初始化顶部和底部信息
     */
    public void initCompanyInfo() {
        TableSettingConfigInfo settingConfigInfo = repository.getSettingConfigInfo();
        String name = settingConfigInfo.getCompanyName();
        if (null == name) {
            name = Utils.getApp().getString(R.string.company_name);
        }
        fieldMainTitle.set(name);
    }

    /**
     * 从PC管理端新增人脸数据
     *
     * @param event 事件
     */
    public void addPersonFromRemote(DynamicAddPersonInfoEvent event) {
        if (repository != null && event != null) {
            repository.addPersonFaceFromRemote(event);
        }
    }

    /**
     * 从PC管理端删除人脸数据
     */
    public void deletePersonFromPc(List<Long> deletedId) {
        if (repository != null) {
            repository.deletePersonFaceFromRemote(deletedId);
        }
    }

    /**
     * 设置无人脸框时UI效果
     */
    public void setScreenBackgroundNoFace() {
        if (repository != null) {
            TableSettingConfigInfo settingConfigInfo = repository.getSettingConfigInfo();
            boolean showScreenDefault = settingConfigInfo.isIndexScreenDefShow();
            if (showScreenDefault) {
                rlDefBackgroundPorVisible.set(true);
                flTextureViewVisible.set(false);
                if (!settingConfigInfo.isScreenBrightFollowSys()) {
                    DeviceUtils.changeAppBrightness(screenDefaultBright, ActivityUtils.getTopActivity());
                }
            } else {
                flTextureViewVisible.set(true);
                if (!settingConfigInfo.isScreenBrightFollowSys()) {
                    DeviceUtils.changeAppBrightness(systemBright, ActivityUtils.getTopActivity());
                }
            }
        }
        faceResultType.set(RecognizeRepository.FACE_RESULT_NORMAL);
        setRecognizeFaceResultVisible(false);
        changeFaceRectColor(RecognizeRepository.FACE_RESULT_NORMAL, false);

        if (!TextUtils.isEmpty(strFaceLiveResult.get())) {
            strFaceLiveResult.set("");
        }
        alreadyShowNoFaceScreen = true;
    }

    /**
     * 设置有人脸框时UI效果
     */
    private void setScreenBackgroundHasFace() {
        TableSettingConfigInfo settingConfigInfo = repository.getSettingConfigInfo();
        flTextureViewVisible.set(true);
        rlDefBackgroundPorVisible.set(false);
        if (!settingConfigInfo.isScreenBrightFollowSys()) {
            DeviceUtils.changeAppBrightness(systemBright, ActivityUtils.getTopActivity());
        }
        alreadyShowNoFaceScreen = false;
    }

    /**
     * 显示&隐藏识别结果弹框
     */
    private void setRecognizeFaceResultVisible(boolean show) {
        if (navigator != null) {
            navigator.showResultView(show);
        }
    }

    private void changeFaceRectColor(Integer status, boolean animated) {
        if (navigator != null) {
            navigator.changeRectColor(status, animated);
        }
    }

    public void onActivityResume() {
        setScreenBackgroundHasFace();
        refreshTime();
        if (repository != null) {
            repository.onActivityResume();
        }
    }

    public void onActivityPause() {
        clearTimerDisposables();
        if (repository != null) {
            repository.onActivityPause();
        }
        rlDefBackgroundPorVisible.set(false);
    }

    public void onActivityDestroyed() {
        if (repository != null) {
            repository.onActivityDestroy();
        }
        clearTimerDisposables();
        if (mainLogoDisposable != null && !mainLogoDisposable.isDisposed()) {
            mainLogoDisposable.dispose();
        }
        mainLogoDisposable = null;
        if (subLogoDisposable != null && !subLogoDisposable.isDisposed()) {
            subLogoDisposable.dispose();
        }
        subLogoDisposable = null;
        navigator = null;
        repository = null;
        noFaceDelayDisposable = null;
        recognizeListener = null;
    }

    private RecognizeRespListener recognizeListener = new RecognizeRespListener() {

        @Override
        public void onCameraOpened(boolean mainCamera) {
            if (navigator != null && mainCamera) {
                navigator.updateMainCameraViewUi(true);
            }
        }

        @Override
        public void setFaceUpCamera(boolean visible) {
            if (navigator != null) {
                navigator.showHintView(visible, true);
            }
        }

        @Override
        public void setHintContent(String content) {
            fieldHint.set(content);
        }

        @Override
        public void onFaceTrackHasFaceInfo(Integer faceId) {
            //从有人脸检测->无人脸检测，需要平滑切换UI，所以加上延迟处理
            disposeDisposable(noFaceDelayDisposable);
            noFaceDelayDisposable = null;

            int faceUpId = repository.getShowFaceUpCameraTrackId();
            if (faceId != faceUpId) {
                repository.cancelHintViewDisposable(faceUpId);
                setFaceUpCamera(false);
            }
            if (faceId != repository.getRecognizeResultTrackId()) {
                onHideRecognitionResult();
            }

            if (!alreadyShowNoFaceScreen) {
                return;
            }
            CommonRepository.getInstance().sendBroadcast(Constants.ACTION_DETECT_FACE_HAS_FACE);
            setScreenBackgroundHasFace();
        }

        @Override
        public void onFaceTrackNoFaceInfo() {
            if (!alreadyShowNoFaceScreen && noFaceDelayDisposable == null) {
                noFaceDelayDisposable = Observable.timer(DELAY_SHOW_NO_FACE_SCREEN_BACKGROUND, TimeUnit.MILLISECONDS)
                        .compose(RxUtils.computingToMain())
                        .subscribe(aLong -> {
                            CommonRepository.getInstance().sendBroadcast(Constants.ACTION_DETECT_FACE_NO_FACE);
                            setScreenBackgroundNoFace();
                            noFaceDelayDisposable = null;
                        });
            }
        }

        @Override
        public void onRecognitionUnauthorized(TablePerson tablePerson, TablePersonFace tablePersonFace, int faceId) {
            repository.cancelHintViewDisposable(repository.getShowFaceUpCameraTrackId());
            setFaceUpCamera(false);
            String headPath = RecognizeRepDataManager.getInstance().getFaceHeadUri(tablePerson, tablePersonFace);
            String strFaceResult = CommonUtils.getStrFromRes(R.string.you_have_no_access_right);
            faceResultType.set(RecognizeRepository.FACE_RESULT_UNAUTHORIZED);
            setRecognizeFaceResultVisible(true);
            changeFaceRectColor(RecognizeRepository.FACE_RESULT_UNAUTHORIZED, true);
            faceResult.set(strFaceResult);
            faceResultHeadPath.set(headPath);
        }

        @Override
        public void onRecognitionSuccess(TablePerson tablePerson, TablePersonFace tablePersonFace, int faceId) {
            if (tablePersonFace == null) {
                return;
            }
            TableSettingConfigInfo settingConfigInfo = repository.getSettingConfigInfo();
            if (settingConfigInfo == null) {
                settingConfigInfo = CommonRepository.getInstance().getSettingConfigInfo();
            }
            repository.cancelHintViewDisposable(repository.getShowFaceUpCameraTrackId());
            setFaceUpCamera(false);
            String strFaceResult = RecognizeRepDataManager.getInstance().getRecognizeResult(tablePerson, settingConfigInfo);
            String headPath = RecognizeRepDataManager.getInstance().getFaceHeadUri(tablePerson, tablePersonFace);
            faceResultType.set(RecognizeRepository.FACE_RESULT_SUCCESS);
            setRecognizeFaceResultVisible(true);
            changeFaceRectColor(RecognizeRepository.FACE_RESULT_SUCCESS, true);
            faceResult.set(strFaceResult);
            faceResultHeadPath.set(headPath);
        }

        @Override
        public void onRecognitionFail(int faceId) {
            TableSettingConfigInfo settingConfigInfo = repository.getSettingConfigInfo();
            repository.cancelHintViewDisposable(repository.getShowFaceUpCameraTrackId());
            setFaceUpCamera(false);
            String strFaceResult = RecognizeRepDataManager.getInstance().getRecognizeResult(settingConfigInfo);
            faceResultType.set(RecognizeRepository.FACE_RESULT_FAILED);
            if (settingConfigInfo.getDisplayModeFail() == ConfigConstants.DISPLAY_MODE_FAILED_NOT_FEEDBACK) {
                setRecognizeFaceResultVisible(false);
            } else {
                setRecognizeFaceResultVisible(true);
                changeFaceRectColor(RecognizeRepository.FACE_RESULT_FAILED, true);
                faceResult.set(strFaceResult);
                faceResultHeadPath.set("");
            }
        }

        @Override
        public void onHideRecognitionResult() {
            faceResultType.set(RecognizeRepository.FACE_RESULT_NORMAL);
            setRecognizeFaceResultVisible(false);
            changeFaceRectColor(RecognizeRepository.FACE_RESULT_NORMAL, false);
        }

        @Override
        public void setFaceLiveResult(String strResult) {
            strFaceLiveResult.set(strResult);
        }

        @Override
        public void onEngineCallBack(String message) {
            if (navigator != null) {
                navigator.showEngineDialog(message);
            }
        }

        @Override
        public void setViceCameraViewVisible() {
            TableSettingConfigInfo settingConfigInfo = repository.getSettingConfigInfo();
            boolean useIrLiveCheck = SettingRepDataManager.getInstance().isIrFaceLiveOpen(settingConfigInfo);
            if (useIrLiveCheck) {
                boolean showPreview = SettingRepDataManager.getInstance().isIrFaceLivePreviewShow(settingConfigInfo);
                showIrPreviewVisible(showPreview);
            } else {
                showIrPreviewVisible(false);
            }
        }
    };

    public void onClick(View v) {
        int resId = v.getId();
        if (resId == R.id.rl_setting) {
            if (DoubleClickUtils.isFastDoubleClick(resId)) {
                return;
            }
            if (repository != null) {
                repository.setIndexDialogShow(true);
            }
            if (navigator != null) {
                navigator.showPasswordDialog();
            }
            if (repository != null) {
                repository.startClosePasswordDialogTimer(() -> {
                    if (navigator != null) {
                        navigator.cancelPasswordDialog();
                    }
                });
            }
        }
    }

    public DeviceAdaptationInfo getAdaptationInfo() {
        return repository == null ? null : repository.getAdaptationInfo();
    }

    public void updateViewType() {
        fieldBorderVisible.set(true);
        fieldFullScreen.set(false);
    }

    /**
     * 修改副相机UI效果
     * @param faceView view布局
     * @param setDefSize 是否设置默认布局大小
     */
    private void changeViceCameraViewUi(CameraFaceView faceView, boolean setDefSize) {
        FrameLayout.LayoutParams textureParams = (FrameLayout.LayoutParams) faceView.getLayoutParams();
        DeviceAdaptationInfo adaptationInfo = repository.getAdaptationInfo();
        int screenWidth = ScreenUtils.getScreenWidth();
        int previewWidth = adaptationInfo.getPreviewWidth();
        int previewHeight = adaptationInfo.getPreviewHeight();
        if (setDefSize) {
            int divisor = CommonUtils.getCommonDivisor(previewWidth, previewHeight);
            textureParams.width = previewWidth / divisor;
            textureParams.height = previewHeight / divisor;
        } else {
            double sizeRatio = ArithmeticUtils.div(previewWidth, previewHeight);
            if (adaptationInfo.isSecondCameraChangeWidthHeight()) {
                textureParams.width = screenWidth;
                textureParams.height = (int) ArithmeticUtils.mul(textureParams.width, sizeRatio);
            } else {
                textureParams.width = screenWidth;
                textureParams.height = (int) ArithmeticUtils.div(textureParams.width, sizeRatio);
            }
            textureParams.width = textureParams.width / Constants.IR_PREVIEW_RECT_RATIO;
            textureParams.height = textureParams.height / Constants.IR_PREVIEW_RECT_RATIO;
        }
        textureParams.topMargin = Utils.getApp().getResources().getDimensionPixelOffset(R.dimen.y140);
        textureParams.leftMargin = Utils.getApp().getResources().getDimensionPixelOffset(R.dimen.x20);
        faceView.setLayoutParams(textureParams);
    }

    /**
     * 延迟关闭密码弹框
     */
    public void startClosePasswordDialogTimer() {
        if (repository != null) {
            repository.startClosePasswordDialogTimer(() -> {
                if (navigator != null) {
                    navigator.cancelPasswordDialog();
                }
            });
        }
    }

    /**
     * 取消延迟关闭密码框任务
     */
    public void disposeClosePasswordDialogTimer() {
        if (repository != null) {
            repository.disposeClosePassDialogTimer();
        }
    }

    private void refreshTime() {
        clearTimerDisposables();
        timerDisposable = Observable.interval(0, 1, TimeUnit.MINUTES).observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    String timeStr = TimeUtils.formatHourMinString();
                    String dateSimpleStr = TimeUtils.formatSimpleDateStampString();
                    time.set(timeStr);
                    fieldSimpleDate.set(dateSimpleStr);
                });
    }

    public boolean loginAdmin(String strPassword, BaseDialogFragment baseDialog) {
        if (TextUtils.isEmpty(strPassword)) {
            ToastUtils.showShortToast(R.string.recognize_password_cannot_empty);
            return false;
        }
        TableSettingConfigInfo settingConfigInfo = repository.getSettingConfigInfo();
        String password = settingConfigInfo.getDevicePassword();
        if (!strPassword.equals(password)) {
            ToastUtils.showShortToast(R.string.recognize_password_wrong);
            return false;
        }

        baseDialog.dismissAllowingStateLoss();
        KeyboardUtils.hideSoftInput(ActivityUtils.getTopActivity());
        return true;
    }

    public void updateFaceEngineConfig() {
        if (repository != null) {
            repository.updateFaceEngineConfig();
        }
    }

    public boolean reInitEngine() {
        return repository.reInitEngine();
    }

    private void clearTimerDisposables() {
        if (timerDisposable != null && !timerDisposable.isDisposed()) {
            timerDisposable.dispose();
            timerDisposable = null;
        }
    }

    private void disposeDisposable(Disposable disposable) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    public void setIndexDialogShow(boolean indexDialogShow) {
        if (repository != null) {
            repository.setIndexDialogShow(indexDialogShow);
        }
    }

    public void doubleClickExitApp() {
        if (repository != null) {
            repository.doubleClickExitApp();
        }
    }
}
