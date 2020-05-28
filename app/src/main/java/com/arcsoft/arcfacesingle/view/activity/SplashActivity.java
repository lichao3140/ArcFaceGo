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

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.base.BaseBusinessActivity;
import com.arcsoft.arcfacesingle.broadcast.UsbReceiver;
import com.arcsoft.arcfacesingle.business.common.CommonRepository;
import com.arcsoft.arcfacesingle.business.setting.ConfigConstants;
import com.arcsoft.arcfacesingle.business.setting.SettingRepository;
import com.arcsoft.arcfacesingle.data.db.dao.ArcFaceVersionDao;
import com.arcsoft.arcfacesingle.data.db.dao.PersonDao;
import com.arcsoft.arcfacesingle.data.db.dao.PersonFaceDao;
import com.arcsoft.arcfacesingle.data.db.table.TableArcFaceVersion;
import com.arcsoft.arcfacesingle.data.db.table.TableSettingConfigInfo;
import com.arcsoft.arcfacesingle.data.model.ConfigurationInfo;
import com.arcsoft.arcfacesingle.libutil.ImageFileUtils;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.arcfacesingle.util.SdcardUtils;
import com.arcsoft.arcfacesingle.util.business.UsbHelper;
import com.arcsoft.arcfacesingle.view.dialog.PasswordManageDialog;
import com.arcsoft.arcfacesingle.view.dialog.UpgradeArcFaceWarnDialog;
import com.arcsoft.asg.libcommon.util.business.rx.RxUtils;
import com.arcsoft.asg.libcommon.util.common.AppUtils;
import com.arcsoft.asg.libcommon.util.common.KeyboardUtils;
import com.arcsoft.asg.libcommon.util.common.PermissionUtils;
import com.arcsoft.asg.libcommon.util.common.SPUtils;
import com.arcsoft.asg.libcommon.util.common.ToastUtils;
import com.arcsoft.faceengine.FaceEngine;
import com.arcsoft.faceengine.VersionInfo;
import com.github.mjdev.libaums.fs.UsbFile;
import com.github.mjdev.libaums.fs.UsbFileInputStream;
import com.github.mjdev.libaums.fs.UsbFileStreamFactory;
import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;

public class SplashActivity extends BaseBusinessActivity {

    private static final int DELAY_CLOSE_PAGE_TIME = 2000;
    /**
     * V3.0.2线上版本第一次测试递交的versionCode
     */
    private static final int VERSION_CODE_V_3_0_2 = 46;

    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private Disposable mDisposable;
    private UsbHelper usbHelper;
    private String faceEngineVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        usbHelper = new UsbHelper();
        usbHelper.init(getApplicationContext(), new UsbReceiver.UsbListener() {
            @Override
            public void insertUsb(UsbDevice deviceAdd) {
            }

            @Override
            public void removeUsb(UsbDevice deviceRemove) {
            }

            @Override
            public void getReadUsbPermission(UsbDevice usbDevice) {
            }

            @Override
            public void readFailedUsb(UsbDevice usbDevice) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestPermission();
    }

    private void requestPermission() {
        PermissionUtils.permission(NEEDED_PERMISSIONS)
                .callback(new PermissionUtils.FullCallback() {
                    @Override
                    public void onGranted(List<String> permissionsGrantedList) {
                        Observable.create(emitter -> SdcardUtils.getInstance().init()).compose(RxUtils.ioToMain()).subscribe();
                        getArcFaceVersion();
                        checkArcFaceEngineVersion();
                    }

                    @Override
                    public void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied) {
                        CommonRepository.getInstance().exitApp();
                    }
                })
                .request();
    }

    private void getArcFaceVersion() {
        VersionInfo versionInfo = FaceEngine.getVersionInfo();
        if (versionInfo != null) {
            faceEngineVersion = versionInfo.getVersion();
        } else {
            faceEngineVersion = Constants.FACE_FEATURE_VERSION_V3_0;
        }
    }

    /**
     * 检查ArcFace版本信息
     */
    private void checkArcFaceEngineVersion() {
        try {
            TableArcFaceVersion arcFaceVersion = ArcFaceVersionDao.getInstance().getModel();
            if (null == arcFaceVersion) {
                long count1 = PersonDao.getInstance().getTotalCount();
                long count2 = PersonFaceDao.getInstance().getTotalCount();
                if (count1 + count2 > 0) {
                    //处理ArcFace Feature从2.0升级到3.0的兼容性问题：feature数据不兼容，需要将2.0版本升级至3.0
                    handleFeatureFromV2ToV3Compatibility();
                } else {
                    //重新安装新版本，无需处理；或者从旧版本升级到新版本，人员库无数据
                    arcFaceVersion = new TableArcFaceVersion();
                    arcFaceVersion.version = faceEngineVersion;
                    ArcFaceVersionDao.getInstance().saveModelAsync(arcFaceVersion);
                    goToPage();
                }
            } else {
                String oldVersion = arcFaceVersion.version;
                String oldVersionMain = oldVersion.substring(0, 1);
                String mainVersion = faceEngineVersion.substring(0, 1);
                if (oldVersionMain.equals(mainVersion)) {
                    //本地人脸特征值版本和ArcFace SDK版本一致，无需处理
                    goToPage();
                } else {
                    //处理ArcFace Feature从x.0升级到x.0的兼容性问题：当前最新版本为3.0，暂不需要处理，此方法为预留方法
                    handleFeatureFromVxToVxCompatibility();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            goToPage();
        }
    }

    /**
     * 处理ArcFace Feature从2.0升级到3.0的兼容性问题：跳转到人员管理页面，重新注册人脸
     */
    private void handleFeatureFromV2ToV3Compatibility() {
        UpgradeArcFaceWarnDialog dialog = new UpgradeArcFaceWarnDialog();
        dialog.setDialogSize((int) getResources().getDimension(R.dimen.x550),
                (int) getResources().getDimension(R.dimen.x495))
                .setOutCancel(false)
                .setConvertViewListener(((holder, baseDialog) -> {
                    holder.setOnClickListener(R.id.btn_dialog_cancel, v -> {
                        CommonRepository.getInstance().exitApp();
                    });

                    holder.setOnClickListener(R.id.btn_dialog_confirm, v -> {
                        handlePasswordDialog();
                    });
                }))
                .show(getSupportFragmentManager());
    }

    private void handlePasswordDialog() {
        PasswordManageDialog passwordDialog = new PasswordManageDialog();
        passwordDialog.setDialogSize((int) getResources().getDimension(R.dimen.x550),
                (int) getResources().getDimension(R.dimen.x370))
                .setOutCancel(false)
                .setConvertViewListener(((holder, baseDialog) -> {
                    EditText etPassword = holder.getView(R.id.et_password_manage);
                    holder.setOnClickListener(R.id.btn_manage_password_confirm, v -> {
                        String strPassword = etPassword.getText().toString().trim();
                        if (loginAdmin(strPassword)) {
                            baseDialog.dismissAllowingStateLoss();
                            KeyboardUtils.hideSoftInput(SplashActivity.this);
                            Intent intent = new Intent(SplashActivity.this, PersonListActivity.class);
                            intent.putExtra(Constants.SP_KEY_FROM_SPLASH, true);
                            startActivity(intent);
                            finish();
                        }
                    });

                    holder.setOnClickListener(R.id.iv_password_cancel, v -> {
                        baseDialog.dismissAllowingStateLoss();
                    });
                }))
                .show(getSupportFragmentManager());
    }

    /**
     * 处理ArcFace Feature从x.0升级到x.0的兼容性问题
     */
    private void handleFeatureFromVxToVxCompatibility() {
        goToPage();
    }

    private boolean loginAdmin(String strPassword) {
        if (TextUtils.isEmpty(strPassword)) {
            ToastUtils.showShortToast(R.string.recognize_password_cannot_empty);
            return false;
        }
        TableSettingConfigInfo settingConfigInfo = CommonRepository.getInstance().getSettingConfigInfo();
        String password = settingConfigInfo.getDevicePassword();
        if (!strPassword.equals(password)) {
            ToastUtils.showShortToast(R.string.recognize_password_wrong);
            return false;
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelTask();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTask();
        if (usbHelper != null) {
            usbHelper.unRegisterReceiver();
            usbHelper.unInit();
        }
    }

    private void goToPage() {
        int lastVersionCode = SPUtils.getInstance().getInt(Constants.SP_KEY_LATEST_VERSION_CODE);
        SPUtils.getInstance().put(Constants.SP_KEY_LATEST_VERSION_CODE, AppUtils.getAppVersionCode());
        if (lastVersionCode < VERSION_CODE_V_3_0_2) {
            SPUtils.getInstance().put(Constants.SP_KEY_ARC_LINK_PERSON_SYNC_KEY, "");
        }

        String id = SPUtils.getInstance().getString(Constants.SP_KEY_APP_ID);
        String key = SPUtils.getInstance().getString(Constants.SP_KEY_SDK_KEY);
        boolean needActivate = (id.isEmpty() || key.isEmpty());

        int appMode = SPUtils.getInstance().getInt(Constants.SP_KEY_APP_MODE, Constants.APP_MODE_NONE);

        Observable<Long> timerObservable = Observable.timer(DELAY_CLOSE_PAGE_TIME, TimeUnit.MILLISECONDS).compose(RxUtils.computingToMain());
        Observable<Long> usbObservable = Observable.create((ObservableOnSubscribe<Long>) emitter -> usbHelper.readUsbDiskDevList(getCallback(emitter))).compose(RxUtils.ioToMain());
        BiFunction<Long, Long, Long> function = (aLong, aLong2) -> Long.valueOf(1);

        mDisposable = Observable.zip(timerObservable, usbObservable, function).subscribe(along -> {
            if (needActivate) {
                Intent intent = new Intent(SplashActivity.this, DeviceActiveActivity.class);
                intent.putExtra(Constants.SP_KEY_FROM_SPLASH, true);
                startActivity(intent);
            } else if (appMode == Constants.APP_MODE_NONE) {
                Intent intent = new Intent(SplashActivity.this, SelectModeActivity.class);
                intent.putExtra(Constants.SP_KEY_FROM_SPLASH, true);
                startActivity(intent);
            } else {
                Intent intent = new Intent(SplashActivity.this, RecognizeActivity.class);
                intent.putExtra(Constants.SP_KEY_APP_MODE, appMode);
                startActivity(intent);
            }
            finish();
        });
    }

    private void cancelTask() {
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
        mDisposable = null;
    }

    private String loadFromFile(UsbFile file) {
        StringBuilder sb = new StringBuilder();
        UsbFileInputStream inputStream = new UsbFileInputStream(file);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String read;
        try {
            while ((read = bufferedReader.readLine()) != null) {
                sb.append(read);
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return sb.toString();
    }

    private Bitmap loadBitmap(UsbFile file) {
        Bitmap bitmap = null;
        BufferedInputStream inputStream = UsbFileStreamFactory.createBufferedInputStream(file,
                usbHelper.getFileSystem());
        try {
            bitmap = CommonUtils.getBitmapFromUsbFile(inputStream, CommonRepository.LOGO_MAX_WIDTH,
                    CommonRepository.LOGO_MAX_WIDTH, usbHelper.getFileSystem());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private UsbHelper.UsbHelperCallback getCallback(ObservableEmitter<Long> emitter) {
        return new UsbHelper.UsbHelperCallback() {
            @Override
            public void requestPermission(boolean hasPermission) {
                if (hasPermission) {
                    List<UsbFile> list = usbHelper.readFilesFromDevice();
                    for (UsbFile file : list) {
                        if (Constants.USB_FILE_NAME_ADAPTATION.equals(file.getName())) {
                            String content = loadFromFile(file);
                            if (content != null) {
                                SPUtils.getInstance().put(Constants.SP_KEY_ADAPTATION_INFO, content);
                            }
                        } else if (Constants.USB_FILE_NAME_SETTING.equals(file.getName())) {
                            String content = loadFromFile(file);
                            if (content != null) {
                                ConfigurationInfo configurationInfo = null;
                                try {
                                    configurationInfo = new Gson().fromJson(content, ConfigurationInfo.class);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    if (configurationInfo != null) {
                                        TableSettingConfigInfo desInfo = CommonRepository.getInstance().getSettingConfigInfo();
                                        configurationInfo.saveToDatabase(desInfo);
                                        SettingRepository.getInstance().saveSettingConfigInfo(desInfo);

                                        if (configurationInfo.getMainLogoPath() != null) {
                                            if (configurationInfo.getMainLogoPath().length() > 0) {
                                                for (UsbFile file1 : list) {
                                                    if (Constants.USB_FILE_MAIN_LOGO.equals(file1.getName())) {
                                                        Bitmap bitmap = loadBitmap(file1);
                                                        if (ImageFileUtils.save(bitmap, ConfigConstants.DEFAULT_MAIN_LOGO_FILE_PATH, Bitmap.CompressFormat.PNG, true)) {
                                                            CommonRepository.getInstance().saveMainLogoId(CommonRepository.getInstance().createDatabaseId());
                                                        }
                                                        break;
                                                    }
                                                }
                                            } else {
                                                CommonRepository.getInstance().saveMainLogoId("");
                                            }
                                        }
                                        if (configurationInfo.getSubLogoPath() != null) {
                                            if (configurationInfo.getSubLogoPath().length() > 0) {
                                                for (UsbFile file1 : list) {
                                                    if (Constants.USB_FILE_SUB_LOGO.equals(file1.getName())) {
                                                        Bitmap bitmap = loadBitmap(file1);
                                                        if (ImageFileUtils.save(bitmap, ConfigConstants.DEFAULT_SECOND_LOGO_FILE_PATH, Bitmap.CompressFormat.PNG, true)) {
                                                            CommonRepository.getInstance().saveSecondLogoId(CommonRepository.getInstance().createDatabaseId());
                                                        }
                                                        break;
                                                    }
                                                }
                                            } else {
                                                CommonRepository.getInstance().saveSecondLogoId("");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                emitter.onNext(Long.valueOf(1));
                emitter.onComplete();
            }

            @Override
            public void onError(String message) {
                emitter.onNext(Long.valueOf(0));
                emitter.onComplete();
            }
        };
    }
}
