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

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.databinding.DataBindingUtil;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.base.BaseBusinessActivity;
import com.arcsoft.arcfacesingle.business.recognize.RecognizeRepository;
import com.arcsoft.arcfacesingle.data.event.ChangeMainLogoEvent;
import com.arcsoft.arcfacesingle.data.event.ChangeSecondLogoEvent;
import com.arcsoft.arcfacesingle.data.event.DynamicAddPersonInfoEvent;
import com.arcsoft.arcfacesingle.data.event.DynamicDeletePersonInfoEvent;
import com.arcsoft.arcfacesingle.data.event.FullRefreshPersonEvent;
import com.arcsoft.arcfacesingle.data.event.InstallPackageEvent;
import com.arcsoft.arcfacesingle.data.event.PersonAddEvent;
import com.arcsoft.arcfacesingle.data.event.PersonAddMultipleEvent;
import com.arcsoft.arcfacesingle.data.event.PersonDeleteEvent;
import com.arcsoft.arcfacesingle.data.event.ReInitFaceEngineEvent;
import com.arcsoft.arcfacesingle.data.event.ReOpenCameraEvent;
import com.arcsoft.arcfacesingle.data.event.arclink.ArcLinkUpdatePersonEvent;
import com.arcsoft.arcfacesingle.databinding.ActivityRecognizeBinding;
import com.arcsoft.arcfacesingle.navigator.RecognizeNavigator;
import com.arcsoft.arcfacesingle.view.dialog.CommonTipDialog;
import com.arcsoft.arcfacesingle.view.dialog.PasswordManageDialog;
import com.arcsoft.arcfacesingle.view.widgets.RecognizeResultView;
import com.arcsoft.arcfacesingle.view.widgets.SimpleHintView;
import com.arcsoft.arcfacesingle.viewmodel.RecognizeViewModel;
import com.arcsoft.asg.libcamera.view.BaseTextureView;
import com.arcsoft.asg.libcamera.view.CameraFaceView;
import com.arcsoft.asg.libcamera.view.FaceRectView;
import com.arcsoft.asg.libcommon.util.business.rx.RxUtils;
import com.arcsoft.asg.libcommon.util.common.AppUtils;
import com.arcsoft.asg.libcommon.util.common.ArithmeticUtils;
import com.arcsoft.asg.libcommon.util.common.FileUtils;
import com.arcsoft.asg.libcommon.util.common.KeyboardUtils;
import com.arcsoft.asg.libcommon.util.common.PermissionUtils;
import com.arcsoft.asg.libcommon.util.common.SPUtils;
import com.arcsoft.asg.libcommon.util.common.ScreenUtils;
import com.arcsoft.asg.libcommon.util.common.ToastUtils;
import com.arcsoft.asg.libcommon.util.common.Utils;
import com.arcsoft.asg.libdeviceadapt.bean.DeviceAdaptationInfo;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class RecognizeActivity extends BaseBusinessActivity implements RecognizeNavigator {

    private static final String TAG = RecognizeActivity.class.getSimpleName();

    private RecognizeViewModel viewModel;
    private ActivityRecognizeBinding dataBinding;

    private Disposable personAddDis;
    private CommonTipDialog personAddDialog;
    private Disposable personDeleteDis;
    private CommonTipDialog personDeleteDialog;
    private CommonTipDialog irDialog;
    private PasswordManageDialog passwordDialog;
    private CommonTipDialog engineDialog;

    private ObjectAnimator simpleShowAnimator;
    private ObjectAnimator simpleHideAnimator;

    private AnimatorSet resultShowAnimator;
    private AnimatorSet resultHideAnimator;

    private Disposable colorDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_recognize);

        viewModel = new RecognizeViewModel();
        viewModel.setNavigator(this);
        viewModel.initView();
        dataBinding.setViewModel(viewModel);

        initSimpleHintViewAnimator();
        initResultViewAnimator();

        checkInstall();
    }

    @Override
    public void initCamera() {
        if (viewModel != null) {
            viewModel.initCameraView(dataBinding.cameraFaceRectViewMain, dataBinding.cameraFaceRectViewVice);
        }
    }

    @Override
    public void updateMainCameraViewUi(boolean mainCamera) {
        CameraFaceView faceView = dataBinding.cameraFaceRectViewMain;
        FrameLayout flBorder = dataBinding.flPreviewBorder;
        int screenWidth = ScreenUtils.getScreenWidth();
        int screenHeight = ScreenUtils.getScreenHeight();
        DeviceAdaptationInfo adaptationInfo = viewModel.getAdaptationInfo();
        double sizeRatio = ArithmeticUtils.div(adaptationInfo.getPreviewWidth(), adaptationInfo.getPreviewHeight());
        double screenRatio;
        if (ScreenUtils.isPortrait()) {
            screenRatio = ArithmeticUtils.div(screenHeight, screenWidth);
        } else {
            screenRatio = ArithmeticUtils.div(screenWidth, screenHeight);
        }
        double sub = Math.abs(ArithmeticUtils.sub(sizeRatio, screenRatio));
        boolean previewFullscreen = false;
        if (sub < Constants.SCREEN_PREVIEW_RATIO_SUB) {
            previewFullscreen = true;
        }
        FrameLayout.LayoutParams textureParams = (FrameLayout.LayoutParams) faceView.getLayoutParams();
        if (previewFullscreen) {
            if (adaptationInfo.isMainCameraChangeWidthHeight()) {
                textureParams.width = screenWidth;
                textureParams.height = (int) ArithmeticUtils.mul(textureParams.width, sizeRatio);
                if (textureParams.height < screenHeight) {
                    textureParams.height = screenHeight;
                    textureParams.width = (int) ArithmeticUtils.div(textureParams.height, sizeRatio);
                }
            } else {
                if (ScreenUtils.isPortrait()) {
                    textureParams.width = screenWidth - Utils.getApp().getResources().getDimensionPixelOffset(R.dimen.x100);
                    textureParams.height = (int) ArithmeticUtils.div(textureParams.width, sizeRatio);
                    int offset = Utils.getApp().getResources().getDimensionPixelOffset(R.dimen.y600) - textureParams.height;
                    textureParams.topMargin = Utils.getApp().getResources().getDimensionPixelOffset(R.dimen.y140) + (offset > 0 ? offset / 2 : 0);
                    viewModel.updateViewType();
                } else {
                    textureParams.width = screenWidth;
                    textureParams.height = (int) ArithmeticUtils.div(textureParams.width, sizeRatio);
                    if (textureParams.height < screenHeight) {
                        textureParams.height = screenHeight;
                        textureParams.width = (int) ArithmeticUtils.mul(textureParams.height, sizeRatio);
                    }
                }
            }
        } else {
            if (adaptationInfo.isMainCameraChangeWidthHeight()) {
                if (ScreenUtils.isPortrait()) {
                    textureParams.width = screenWidth - Utils.getApp().getResources().getDimensionPixelOffset(R.dimen.x100);
                    if (sizeRatio >= 1) {
                        textureParams.height = (int) ArithmeticUtils.mul(textureParams.width, sizeRatio);
                    } else {
                        textureParams.height = (int) ArithmeticUtils.div(textureParams.width, sizeRatio);
                    }
                    int offset = Utils.getApp().getResources().getDimensionPixelOffset(R.dimen.y600) - textureParams.height;
                    textureParams.topMargin = Utils.getApp().getResources().getDimensionPixelOffset(R.dimen.y140) + (offset > 0 ? offset / 2 : 0);
                    viewModel.updateViewType();
                } else {
                    textureParams.height = screenHeight - Utils.getApp().getResources().getDimensionPixelOffset(R.dimen.y240);
                    textureParams.width = (int) ArithmeticUtils.div(textureParams.height, sizeRatio);
                    viewModel.updateViewType();
                }
            } else {
                if (ScreenUtils.isPortrait()) {
                    textureParams.width = screenWidth - Utils.getApp().getResources().getDimensionPixelOffset(R.dimen.x100);
                    if (sizeRatio >= 1) {
                        textureParams.height = (int) ArithmeticUtils.div(textureParams.width, sizeRatio);
                    } else {
                        textureParams.height = (int) ArithmeticUtils.mul(textureParams.width, sizeRatio);
                    }
                    int offset = Utils.getApp().getResources().getDimensionPixelOffset(R.dimen.y600) - textureParams.height;
                    textureParams.topMargin = Utils.getApp().getResources().getDimensionPixelOffset(R.dimen.y140) + (offset > 0 ? offset / 2 : 0);
                    viewModel.updateViewType();
                } else {
                    textureParams.height = screenHeight - Utils.getApp().getResources().getDimensionPixelOffset(R.dimen.y240);
                    textureParams.width = (int) ArithmeticUtils.mul(textureParams.height, sizeRatio);
                    viewModel.updateViewType();
                }
            }
        }
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) flBorder.getLayoutParams();
        layoutParams.width = textureParams.width + Utils.getApp().getResources().getDimensionPixelOffset(R.dimen.x31);
        layoutParams.height = textureParams.height + Utils.getApp().getResources().getDimensionPixelOffset(R.dimen.y31);
        if (ScreenUtils.isPortrait()) {
            layoutParams.topMargin = textureParams.topMargin - Utils.getApp().getResources().getDimensionPixelOffset(R.dimen.x15);
        }
        flBorder.setLayoutParams(layoutParams);
        View cameraView = faceView.getCameraView();
        if (cameraView instanceof BaseTextureView) {
            BaseTextureView textureView = (BaseTextureView) cameraView;
            textureView.getSurfaceTextureListener().onSurfaceTextureSizeChanged(textureView.getSurfaceTexture(),
                    textureParams.width, textureParams.height);
        }
        faceView.setLayoutParams(textureParams);
    }

    @Override
    protected void onDestroy() {
        simpleHideAnimator.removeAllListeners();
        simpleHideAnimator.cancel();
        simpleShowAnimator.cancel();
        resultHideAnimator.removeAllListeners();
        resultHideAnimator.cancel();
        resultShowAnimator.cancel();
        if (viewModel != null) {
            viewModel.onActivityDestroyed();
        }
        viewModel = null;
        dataBinding = null;
        personAddDis = null;
        personAddDialog = null;
        personDeleteDis = null;
        personDeleteDialog = null;
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.onActivityResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (viewModel != null) {
            viewModel.onActivityPause();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCameraConfigChange(ReOpenCameraEvent event) {
        if (null != event) {
            if (event.needReOpenCamera && event.needInitFaceEngine) {
                if (viewModel != null) {
                    viewModel.setIndexDialogShow(true);
                    viewModel.setIrPreviewVisible(dataBinding.cameraFaceRectViewVice);
                    viewModel.setScreenBackgroundNoFace();
                    viewModel.restartCamera(event.useIrFaceLive, dataBinding.cameraFaceRectViewVice);
                }
            }
            if (!event.needReOpenCamera && !event.needInitFaceEngine) {
                if (viewModel != null) {
                    viewModel.initCompanyInfo();
                    viewModel.setIrPreviewVisible(dataBinding.cameraFaceRectViewVice);
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFaceEngineConfigChange(ReInitFaceEngineEvent event) {
        if (null != event && event.reInitEngine) {
            if (viewModel != null) {
                viewModel.updateFaceEngineConfig();
            }
        }
    }

    /**
     * 设置主Logo
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void changeMainLogo(ChangeMainLogoEvent event) {
        if (null != event) {
            if (viewModel != null) {
                viewModel.setTopLogo();
            }
        }
    }

    /**
     * 设置副Logo
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void changeSecondLogo(ChangeSecondLogoEvent event) {
        if (null != event) {
            viewModel.setSecondLogo();
        }
    }

    /**
     * PC端向客户端下发人脸数据
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void addPersonFaceFromServer(DynamicAddPersonInfoEvent event) {
        if (event != null) {
            if (viewModel != null) {
                viewModel.addPersonFromRemote(event);
            }
        }
    }

    /**
     * PC端向客户端删除人脸数据
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void deletePersonFaceFromServer(DynamicDeletePersonInfoEvent event) {
        if (event != null) {
            if (viewModel != null) {
                viewModel.deletePersonFromPc(event.deletedId);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updatePersonEvent(ArcLinkUpdatePersonEvent event) {
        if (null != event && event.success) {
            if (viewModel != null && isCloudAIotAppMode()) {
                viewModel.loadPersonFaceList();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void fullRefreshPersonEvent(FullRefreshPersonEvent event) {
        if (null != event && event.fullRefresh && viewModel != null) {
            viewModel.loadPersonFaceList();
        }
    }

    /**
     * 管理端向设备端增加人脸数据
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void personAddFromServer(PersonAddEvent event) {
        if (event != null) {
            showPersonAddFromServerDialog();
            if (personAddDis != null && !personAddDis.isDisposed()) {
                personAddDis.dispose();
                personAddDis = null;
            }
            personAddDis = Observable.timer(Constants.DIALOG_DISMISS_DELAY, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aLong -> cancelPersonAddFromServerDialog());
        }
    }

    /**
     * 管理端向设备端增加人脸数据
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showPersonAddFromServer(PersonAddMultipleEvent event) {
        if (event != null) {
            if (event.showDialog) {
                showPersonAddFromServerDialog();
            } else {
                cancelPersonAddFromServerDialog();
            }
        }
    }

    private void showPersonAddFromServerDialog() {
        if (personAddDialog == null) {
            personAddDialog = CommonTipDialog
                    .getInstance(Utils.getApp().getResources().getString(R.string.add_person_from_server_to_device),
                            "", "", true, true, true);
            personAddDialog.setDialogSize((int) getResources().getDimension(R.dimen.x550),
                    (int) getResources().getDimension(R.dimen.x370))
                    .setOutCancel(false)
                    .show(getSupportFragmentManager());
            if (viewModel != null) {
                viewModel.setIndexDialogShow(true);
            }
        }
    }

    private void cancelPersonAddFromServerDialog() {
        if (personAddDialog != null && personAddDialog.isVisible()) {
            personAddDialog.dismissAllowingStateLoss();
            personAddDialog = null;
            if (viewModel != null) {
                viewModel.setIndexDialogShow(false);
            }
        }
    }

    /**
     * 管理端向设备端增加人脸数据
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void personDeleteFromServer(PersonDeleteEvent event) {
        if (event != null) {
            if (personDeleteDialog == null) {
                personDeleteDialog = CommonTipDialog
                        .getInstance(Utils.getApp().getResources().getString(R.string.delete_person_from_server_to_device),
                                "", "", true, true, true);
                personDeleteDialog.setDialogSize((int) getResources().getDimension(R.dimen.x550),
                        (int) getResources().getDimension(R.dimen.x370))
                        .setOutCancel(false)
                        .show(getSupportFragmentManager());
                if (viewModel != null) {
                    viewModel.setIndexDialogShow(true);
                }
            }
            if (personDeleteDis != null && !personDeleteDis.isDisposed()) {
                personDeleteDis.dispose();
                personDeleteDis = null;
            }
            personDeleteDis = Observable.timer(Constants.DIALOG_DISMISS_DELAY, TimeUnit.MILLISECONDS)
                    .compose(RxUtils.computingToMain())
                    .subscribe(aLong -> {
                        if (personDeleteDialog != null && personDeleteDialog.isVisible()) {
                            personDeleteDialog.dismissAllowingStateLoss();
                            personDeleteDialog = null;
                            if (viewModel != null) {
                                viewModel.setIndexDialogShow(false);
                            }
                        }
                    });
        }
    }

    @Override
    public void showPasswordDialog() {
        if (passwordDialog == null) {
            passwordDialog = new PasswordManageDialog();
            passwordDialog.setDialogSize((int) getResources().getDimension(R.dimen.x550),
                    (int) getResources().getDimension(R.dimen.x370))
                    .setOutCancel(false)
                    .setConvertViewListener(((holder, baseDialog) -> {
                        EditText etPassword = holder.getView(R.id.et_password_manage);
                        etPassword.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                String content = s.toString();
                                if (TextUtils.isEmpty(content)) {
                                    if (viewModel != null) {
                                        viewModel.startClosePasswordDialogTimer();
                                    }
                                } else {
                                    if (viewModel != null) {
                                        viewModel.disposeClosePasswordDialogTimer();
                                    }
                                }
                            }
                        });
                        holder.setOnClickListener(R.id.btn_manage_password_confirm, v -> {
                            String strPassword = etPassword.getText().toString().trim();
                            if (viewModel.loginAdmin(strPassword, baseDialog)) {
                                passwordDialog.dismissAllowingStateLoss();
                                passwordDialog = null;

                                Intent intent = new Intent(RecognizeActivity.this, SettingActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });

                        holder.setOnClickListener(R.id.iv_password_cancel, v -> {
                            passwordDialog.dismissAllowingStateLoss();
                            passwordDialog = null;
                            if (viewModel != null) {
                                viewModel.disposeClosePasswordDialogTimer();
                                viewModel.setIndexDialogShow(false);
                            }
                            KeyboardUtils.hideSoftInput(RecognizeActivity.this);
                        });
                    }))
                    .show(getSupportFragmentManager());
        }
    }

    @Override
    public void cancelPasswordDialog() {
        if (passwordDialog != null) {
            passwordDialog.dismissAllowingStateLoss();
            passwordDialog = null;
            if (viewModel != null) {
                viewModel.setIndexDialogShow(false);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            viewModel.doubleClickExitApp();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void showIrDialog() {
        if (irDialog == null) {
            irDialog = CommonTipDialog
                    .getInstance(getResources().getString(R.string.ir_camera_warn),
                            getResources().getString(R.string.confirm),
                            getResources().getString(R.string.cancel), true, false, true);
            irDialog.setDialogSize((int) getResources().getDimension(R.dimen.x550),
                    (int) getResources().getDimension(R.dimen.x370))
                    .setOutCancel(false)
                    .setConvertViewListener(((holder, baseDialog) -> {
                        holder.getView(R.id.btn_common_dialog_confirm).setOnClickListener(v -> {
                            baseDialog.dismissAllowingStateLoss();
                            irDialog = null;
                            if (viewModel != null) {
                                viewModel.setIndexDialogShow(true);
                            }
                            showPasswordDialog();
                        });
                        holder.getView(R.id.iv_common_dialog_cancel).setOnClickListener(v -> {
                            baseDialog.dismissAllowingStateLoss();
                            irDialog = null;
                            if (viewModel != null) {
                                viewModel.setIndexDialogShow(false);
                            }
                        });
                        holder.getView(R.id.btn_common_dialog_cancel).setOnClickListener(v -> {
                            baseDialog.dismissAllowingStateLoss();
                            irDialog = null;
                            if (viewModel != null) {
                                viewModel.setIndexDialogShow(false);
                            }
                        });
                    }))
                    .show(getSupportFragmentManager());
            if (viewModel != null) {
                viewModel.setIndexDialogShow(true);
            }
        }

    }

    @Override
    public void showEngineDialog(String message) {
        if (engineDialog == null) {
            if (viewModel != null) {
                viewModel.setIndexDialogShow(true);
            }
            engineDialog = CommonTipDialog
                    .getInstance(AppUtils.getString(R.string.face_engine_init_fail_please_retry),
                            getResources().getString(R.string.retry),
                            getResources().getString(R.string.cancel), false, false, false);
            engineDialog.setDialogSize((int) getResources().getDimension(R.dimen.x550),
                    (int) getResources().getDimension(R.dimen.x370))
                    .setOutCancel(false)
                    .setConvertViewListener(((holder, baseDialog) -> {
                        holder.getView(R.id.btn_common_dialog_confirm).setOnClickListener(v -> {
                            if (viewModel != null && viewModel.reInitEngine()) {
                                engineDialog.dismissAllowingStateLoss();
                                engineDialog = null;
                                viewModel.setIndexDialogShow(false);
                            } else {
                                ToastUtils.showLongToast(R.string.face_engine_init_fail);
                            }
                        });
                        holder.getView(R.id.iv_common_dialog_cancel).setOnClickListener(v -> {
                            engineDialog.dismissAllowingStateLoss();
                            engineDialog = null;
                            if (viewModel != null) {
                                viewModel.setIndexDialogShow(false);
                            }
                        });
                        holder.getView(R.id.btn_common_dialog_cancel).setOnClickListener(v -> {
                            engineDialog.dismissAllowingStateLoss();
                            engineDialog = null;
                            if (viewModel != null) {
                                viewModel.setIndexDialogShow(false);
                            }
                        });
                    }))
                    .show(getSupportFragmentManager());
        }
    }

    @Override
    public void showResultView(boolean show) {
        RecognizeResultView recognizeResultView = dataBinding.rlFaceResultCenter;
        if (show) {
            resultHideAnimator.end();
            if (!resultShowAnimator.isStarted() && recognizeResultView.getVisibility() != View.VISIBLE) {
                recognizeResultView.setVisibility(View.VISIBLE);
                resultShowAnimator.start();
            }
        } else {
            resultShowAnimator.end();
            if (!resultHideAnimator.isStarted() && recognizeResultView.getVisibility() == View.VISIBLE) {
                resultHideAnimator.start();
            }
        }
    }

    @Override
    public void showHintView(boolean show, boolean animated) {
        SimpleHintView simpleHintView = dataBinding.tvTipFaceToCamera;
        if (animated) {
            if (show) {
                simpleHideAnimator.end();
                if (!simpleShowAnimator.isStarted() && simpleHintView.getVisibility() != View.VISIBLE) {
                    simpleHintView.setVisibility(View.VISIBLE);
                    simpleShowAnimator.start();
                }
            } else {
                simpleShowAnimator.end();
                if (!simpleHideAnimator.isStarted() && simpleHintView.getVisibility() == View.VISIBLE) {
                    simpleHideAnimator.start();
                }
            }
        } else {
            simpleHideAnimator.end();
            simpleShowAnimator.end();
            simpleHintView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void changeRectColor(Integer status, boolean animated) {
        if (colorDisposable != null && !colorDisposable.isDisposed()) {
            colorDisposable.dispose();
        }
        int color = -1;
        if (status.equals(RecognizeRepository.FACE_RESULT_SUCCESS)) {
            color = getResources().getColor(R.color.color_recognize_success);
        } else if (status.equals(RecognizeRepository.FACE_RESULT_UNAUTHORIZED)) {
            color = getResources().getColor(R.color.color_recognize_permission);
        } else if (status.equals(RecognizeRepository.FACE_RESULT_FAILED)) {
            color = getResources().getColor(R.color.color_recognize_fail);
        }
        CameraFaceView cameraFaceView = dataBinding.cameraFaceRectViewMain;
        FaceRectView faceRectView = cameraFaceView.getFaceRectView();
        if (faceRectView != null) {
            faceRectView.setRectColor(color);
            if (animated) {
                colorDisposable = Observable.intervalRange(0, 6, 0, 80, TimeUnit.MILLISECONDS).subscribe(aLong -> {
                    faceRectView.setCanDraw(!faceRectView.isCanDraw());
                });
            } else {
                faceRectView.setCanDraw(true);
            }
        }
    }

    @Override
    public void setViceCameraFaceVisible(boolean visible) {
        CameraFaceView cameraFaceView = dataBinding.cameraFaceRectViewVice;
        cameraFaceView.getCameraView().setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        cameraFaceView.getFaceRectView().setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            hideNavigationBar();
        }
        return super.dispatchTouchEvent(ev);
    }

    private void checkInstall() {
        String path = SPUtils.getInstance().getString(Constants.SP_KEY_LOCAL_APK_CRC);
        if (!TextUtils.isEmpty(path)) {
            if (AppUtils.checkPackageInfo(this, path)) {
                if (!PermissionUtils.isGranted(PermissionUtils.PERMISSION_INSTALL_PACKAGE)) {
                    super.onInstallPackageEvent(new InstallPackageEvent(path, InstallPackageEvent.INSTALL_TYPE_NO_SILENCE));
                } else {
                    super.onInstallPackageEvent(new InstallPackageEvent(path, InstallPackageEvent.INSTALL_TYPE_SILENCE));
                }
            } else {
                FileUtils.delete(path);
                SPUtils.getInstance().remove(Constants.SP_KEY_LOCAL_APK_CRC);
            }
        }
    }

    private void initSimpleHintViewAnimator() {
        SimpleHintView simpleHintView = dataBinding.tvTipFaceToCamera;
        WeakReference<SimpleHintView> weakReference = new WeakReference<>(simpleHintView);

        simpleShowAnimator = ObjectAnimator.ofFloat(simpleHintView, "alpha", 0, 1);
        simpleShowAnimator.setDuration(100);

        simpleHideAnimator = ObjectAnimator.ofFloat(simpleHintView, "alpha", 1, 0);
        simpleHideAnimator.setDuration(100);

        simpleHideAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                SimpleHintView view = weakReference.get();
                if (view != null) {
                    simpleHintView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
    }

    private void initResultViewAnimator() {
        RecognizeResultView recognizeResultView = dataBinding.rlFaceResultCenter;
        WeakReference<RecognizeResultView> weakReference = new WeakReference<>(recognizeResultView);

        ObjectAnimator showAnimator = ObjectAnimator.ofFloat(recognizeResultView, "alpha", 0, 1);
        ObjectAnimator showAnimator1 = ObjectAnimator.ofFloat(recognizeResultView, "scaleX", 0.5f, 1);
        ObjectAnimator showAnimator2 = ObjectAnimator.ofFloat(recognizeResultView, "scaleY", 0.5f, 1);

        resultShowAnimator = new AnimatorSet();
        resultShowAnimator.setDuration(200);
        resultShowAnimator.playTogether(showAnimator, showAnimator1, showAnimator2);

        ObjectAnimator hideAnimator = ObjectAnimator.ofFloat(recognizeResultView, "alpha", 1, 0);

        resultHideAnimator = new AnimatorSet();
        resultHideAnimator.setDuration(100);
        resultHideAnimator.playTogether(hideAnimator);
        resultHideAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                RecognizeResultView view = weakReference.get();
                if (view != null) {
                    view.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
    }

    public void setIndexDialogShow(boolean indexDialogShow) {
        if (viewModel != null) {
            viewModel.setIndexDialogShow(indexDialogShow);
        }
    }
}
