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

package com.arcsoft.arcfacesingle.base;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.core.content.FileProvider;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.broadcast.KeyHomeBroadcastReceiver;
import com.arcsoft.arcfacesingle.business.common.CommonRepository;
import com.arcsoft.arcfacesingle.business.setting.ConfigConstants;
import com.arcsoft.arcfacesingle.data.db.dao.IdentifyRecordDao;
import com.arcsoft.arcfacesingle.data.db.dao.PersonDao;
import com.arcsoft.arcfacesingle.data.db.dao.PersonFaceDao;
import com.arcsoft.arcfacesingle.data.db.dao.PersonPermissionDao;
import com.arcsoft.arcfacesingle.data.db.table.TableSettingConfigInfo;
import com.arcsoft.arcfacesingle.data.event.CleanDataEvent;
import com.arcsoft.arcfacesingle.data.event.DeviceStorageCheckEvent;
import com.arcsoft.arcfacesingle.data.event.InstallPackageEvent;
import com.arcsoft.arcfacesingle.data.event.KeyboardVisibleEvent;
import com.arcsoft.arcfacesingle.data.event.arclink.ArcLinkAuthEvent;
import com.arcsoft.arcfacesingle.data.event.arclink.ArcLinkDisconnectedEvent;
import com.arcsoft.arcfacesingle.libutil.ImageFileUtils;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.arcfacesingle.util.SdcardUtils;
import com.arcsoft.arcfacesingle.view.activity.DeviceAccessActivity;
import com.arcsoft.arcfacesingle.view.activity.RecognizeActivity;
import com.arcsoft.arcfacesingle.view.activity.SplashActivity;
import com.arcsoft.arcfacesingle.view.dialog.CleanDataDialog;
import com.arcsoft.arcfacesingle.view.dialog.CommonTipDialog;
import com.arcsoft.asg.libcommon.base.BaseActivity;
import com.arcsoft.asg.libcommon.util.business.rx.RxUtils;
import com.arcsoft.asg.libcommon.util.common.ActivityUtils;
import com.arcsoft.asg.libcommon.util.common.AppUtils;
import com.arcsoft.asg.libcommon.util.common.FileUtils;
import com.arcsoft.asg.libcommon.util.common.SPUtils;
import com.arcsoft.asg.libcommon.util.common.ScreenUtils;
import com.arcsoft.asg.libcommon.util.common.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import me.jessyan.autosize.AutoSize;

public abstract class BaseBusinessActivity extends BaseActivity {

    private static final String TAG = BaseBusinessActivity.class.getSimpleName();
    private static final int REBOOT_DELAY = 3;

    /**
     * 剩余空间不足弹框
     */
    private CommonTipDialog sdCardStorageWarnDialog;
    private KeyHomeBroadcastReceiver mHomeKeyReceiver;
    private CleanDataDialog cleanDataDialog;
    private Disposable cleanDataDisposable;
    private Disposable arcLinkDisconnectDis;
    private CommonTipDialog installDialog;
    private CommonTipDialog rebootDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        switchAutoSizeDp();
        TableSettingConfigInfo configInfo = CommonRepository.getInstance().getSettingConfigInfo();
        boolean sleepFollowSys = configInfo.isDeviceSleepFollowSys();
        if (sleepFollowSys) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        if (useEventBus()) {
            EventBus.getDefault().register(this);
        }
    }

    /**
     * 是否使用EventBus,默认使用，若不使用可以重写此方法返回false
     *
     * @return
     */
    protected boolean useEventBus() {
        return true;
    }

    /**
     * 离线局域网模式
     */
    protected boolean isOfflineLanAppMode() {
        return CommonUtils.isOfflineLanAppMode();
    }

    /**
     * 云端AIoT模式
     */
    protected boolean isCloudAIotAppMode() {
        return CommonUtils.isCloudAiotAppMode();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void arcLinkDisconnected(ArcLinkDisconnectedEvent event) {
        if (event != null && event.disconnected && ActivityUtils.getTopActivity() == this) {
            showDisconnectDialog(true);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void arcLinkDisconnected(ArcLinkAuthEvent event) {
        if (event != null && event.authorized && ActivityUtils.getTopActivity() == this) {
            showDisconnectDialog(false);
        }
    }

    private void showDisconnectDialog(boolean disconnected) {
        Activity activity = ActivityUtils.getTopActivity();
        WeakReference<Activity> weakReference = new WeakReference<>(activity);
        String topClassName = activity.getClass().getSimpleName();
        if (topClassName.equals(DeviceAccessActivity.class.getSimpleName())) {
            ((DeviceAccessActivity) activity).unBindDevice();
        } else {
            if (topClassName.equals(RecognizeActivity.class.getSimpleName())) {
                ((RecognizeActivity) activity).setIndexDialogShow(true);
            }
            CommonRepository.getInstance().arcLinkDisconnected();
        }
        String strContent;
        if (disconnected) {
            strContent = CommonUtils.getStrFromRes(R.string.arc_link_device_disconnected);
        } else {
            strContent = CommonUtils.getStrFromRes(R.string.authorization_expired);
        }
        CommonTipDialog dialog = CommonTipDialog.getInstance(
                strContent,
                strContent = CommonUtils.getStrFromRes(R.string.confirm),
                "",
                true,
                false,
                true);
        dialog.setDialogSize((int) getResources().getDimension(R.dimen.x550),
                (int) getResources().getDimension(R.dimen.x370))
                .setOutCancel(false)
                .setConvertViewListener(((holder, baseDialog) -> {
                    holder.getView(R.id.btn_common_dialog_confirm).setOnClickListener(v -> {
                        if (weakReference.get().getClass().getSimpleName().equals(RecognizeActivity.class.getSimpleName())) {
                            ((RecognizeActivity) weakReference.get()).setIndexDialogShow(false);
                        }
                        dialog.dismissAllowingStateLoss();
                    });
                }))
                .show(getSupportFragmentManager());
        if (topClassName.equals(RecognizeActivity.class.getSimpleName())) {
            if (arcLinkDisconnectDis != null && !arcLinkDisconnectDis.isDisposed()) {
                arcLinkDisconnectDis.dispose();
                arcLinkDisconnectDis = null;
            }
            arcLinkDisconnectDis = Observable.timer(Constants.DIALOG_DISMISS_DELAY, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aLong -> {
                        if (weakReference.get().getClass().getSimpleName().equals(RecognizeActivity.class.getSimpleName())) {
                            ((RecognizeActivity) weakReference.get()).setIndexDialogShow(false);
                        }
                        dialog.dismissAllowingStateLoss();
                    });
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void deviceStorageWarnDialog(DeviceStorageCheckEvent event) {
        if (null != event && event.show && ActivityUtils.getTopActivity() == this) {
            showDeviceStorageWarnDialog();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(KeyboardVisibleEvent event) {
        if (!event.isVisible && ActivityUtils.getTopActivity() == this) {
            hideNavigationBar();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCleanDataEvent(CleanDataEvent event) {
        if (event.clean && ActivityUtils.getTopActivity() == this) {
            cleanDataFromPcManager();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onInstallPackageEvent(InstallPackageEvent event) {
        if (ActivityUtils.getTopActivity() == this) {
            if (event.type == InstallPackageEvent.INSTALL_TYPE_NO_SILENCE) {
                showInstallDialog(event.path);
            } else {
                Disposable disposable = Observable.create(emitter -> {
                    if (installSilence(event.path) == 0) {
                        emitter.onNext(0);
                        emitter.onComplete();
                    } else {
                        emitter.onError(new Throwable(Constants.STRING_EMPTY));
                    }
                }).compose(RxUtils.ioToMain()).subscribe(along -> {
                    if (along.equals(0)) {
                        showRebootDialog(true, null);
                    }
                }, Throwable::printStackTrace);
            }
        }
    }

    /**
     * 收到管理客户端端的指令后，清除数据
     */
    private void cleanDataFromPcManager() {
        if (cleanDataDisposable != null && !cleanDataDisposable.isDisposed()) {
            cleanDataDisposable.dispose();
            cleanDataDisposable = null;
            if (cleanDataDialog != null && cleanDataDialog.isVisible()) {
                cleanDataDialog.dismissAllowingStateLoss();
                cleanDataDialog = null;
            }
        }
        beforeShowCleanDataDialog();
        cleanDataDisposable = Observable.create((ObservableEmitter<Boolean> emitter) -> {
            String filePath1 = SdcardUtils.getInstance().getSignRecordDirPath();
            String filePath2 = SdcardUtils.getInstance().getRegisteredDirPath();
            FileUtils.deleteDir(filePath1);
            FileUtils.deleteDir(filePath2);
            PersonDao.getInstance().deleteTable();
            PersonPermissionDao.getInstance().deleteTable();
            PersonFaceDao.getInstance().deleteTable();
            IdentifyRecordDao.getInstance().deleteTable();
            emitter.onNext(true);
            emitter.onComplete();
        })
                .compose(RxUtils.ioToMain())
                .subscribe(flag -> cleanDataDialog.setProgress(CleanDataDialog.COUNT_TOTAL),
                        throwable -> cleanDataDialog.setProgress(CleanDataDialog.COUNT_TOTAL));
    }

    /**
     * 提前显示清除数据弹框
     */
    private void beforeShowCleanDataDialog() {
        if (cleanDataDialog == null) {
            cleanDataDialog = new CleanDataDialog();
            Bundle bundle = new Bundle();
            bundle.putBoolean(CleanDataDialog.BUNDLE_KEY_START_TIMER, true);
            cleanDataDialog.setArguments(bundle);
            cleanDataDialog.setDialogSize((int) getResources().getDimension(R.dimen.x450),
                    (int) getResources().getDimension(R.dimen.x300))
                    .setOutCancel(false)
                    .setConvertViewListener(((holder, baseDialog) -> {
                        holder.getView(R.id.btn_dialog_confirm).setOnClickListener(v -> {
                            confirmCleanData();
                            cleanDataDialog.cancelDelayTimer();
                            cleanDataDialog.dismissAllowingStateLoss();
                            cleanDataDialog = null;
                        });
                    }))
                    .show(getSupportFragmentManager());
            cleanDataDialog.setCleanDataCallback(() -> {
                confirmCleanData();
                cleanDataDialog.dismissAllowingStateLoss();
                cleanDataDialog = null;
            });
            Activity activity = ActivityUtils.getTopActivity();
            String topClassName = activity.getClass().getSimpleName();
            if (topClassName.equals(RecognizeActivity.class.getSimpleName())) {
                ((RecognizeActivity) activity).setIndexDialogShow(true);
            }
        }
    }

    private void confirmCleanData() {
        Disposable disposable = Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
            resetAndSaveDeviceConfig();
            Bitmap mainBmp = ImageFileUtils.getBitmap(R.mipmap.ic_company_main_logo);
            if (ImageFileUtils.save(mainBmp, ConfigConstants.DEFAULT_MAIN_LOGO_FILE_PATH, Bitmap.CompressFormat.PNG)) {
                String mainLogoId = CommonRepository.getInstance().createDatabaseId();
                CommonRepository.getInstance().saveMainLogoId(mainLogoId);
                if (!mainBmp.isRecycled()) {
                    mainBmp.recycle();
                }
            }
            Bitmap secondBmp = ImageFileUtils.getBitmap(R.mipmap.ic_company_main_logo);
            if (ImageFileUtils.save(secondBmp, ConfigConstants.DEFAULT_SECOND_LOGO_FILE_PATH, Bitmap.CompressFormat.PNG)) {
                String secondId = CommonRepository.getInstance().createDatabaseId();
                CommonRepository.getInstance().saveSecondLogoId(secondId);
                if (!secondBmp.isRecycled()) {
                    secondBmp.recycle();
                }
            }

            //*************V3.0版本新增的参数修改******************//
            SPUtils.getInstance().put(Constants.SP_KEY_ARC_LINK_PERSON_SYNC_EXCEPTION, false);
            SPUtils.getInstance().put(Constants.SP_KEY_ARC_LINK_PERSON_SYNC_KEY, "");
            SPUtils.getInstance().put(Constants.SP_KEY_CLOUD_SECOND_LOGO_URL, "");
            SPUtils.getInstance().put(Constants.SP_KEY_CLOUD_MAIN_LOGO_URL, "");
            if (CommonUtils.isCloudAiotAppMode()) {
                CommonRepository.getInstance().uploadArcLinkMainLogo();
                CommonRepository.getInstance().uploadArcLinkSecondLogo();
            }

            emitter.onNext(1);
            emitter.onComplete();
        }).compose(RxUtils.ioToMain())
                .subscribeWith(new DisposableObserver<Integer>() {
                    @Override
                    public void onNext(Integer aLong) {
                        Intent intent = new Intent(BaseBusinessActivity.this, SplashActivity.class);
                        startActivity(intent);
                        finish();
                        Utils.finishAllActivity();
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private void resetAndSaveDeviceConfig() {
        TableSettingConfigInfo configInfo = CommonRepository.getInstance().getSettingConfigInfo();
        configInfo.setSimilarThreshold(ConfigConstants.DEFAULT_THRESHOLD);
        configInfo.setDevicePassword(ConfigConstants.DEFAULT_DEVICE_PASSWORD);
        configInfo.setMaxFaceTrackNumber(ConfigConstants.DEFAULT_MAX_RECOGNIZE_NUM);
        configInfo.setDisplayMode(ConfigConstants.DISPLAY_MODE_SUCCESS_NAME);
        configInfo.setDisplayModeFail(ConfigConstants.DISPLAY_MODE_FAILED_DEFAULT_MARKUP);
        configInfo.setCustomDisplayModeFormat(ConfigConstants.DISPLAY_MODE_SUCCESS_CUSTOM_VALUE);
        configInfo.setCustomFailDisplayModeFormat(ConfigConstants.DISPLAY_MODE_FAILED_CUSTOM_VALUE);
        configInfo.setRecognitionRetryDelay(ConfigConstants.DEFAULT_RETRY_DELAY);
        configInfo.setRecognizeDistance(ConfigConstants.RECOGNITION_DISTANCE_TYPE3);
        configInfo.setCloseDoorDelay(ConfigConstants.DEFAULT_CLOSE_DOOR_DELAY);
        configInfo.setVoiceMode(ConfigConstants.SUCCESS_VOICE_MODE_PREVIEW_TYPE6);
        configInfo.setVoiceModeFail(ConfigConstants.FAILED_VOICE_MODE_PREVIEW_TYPE3);
        configInfo.setCustomVoiceModeFormat(ConfigConstants.DEFAULT_SUCCESS_VOICE_MODE_CUSTOM_VALUE);
        configInfo.setCustomFailVoiceModeFormat(ConfigConstants.FAILED_VOICE_MODE_CUSTOM_DEFAULT);
        configInfo.setMainImagePath(ConfigConstants.DEFAULT_MAIN_LOGO_FILE_PATH);
        configInfo.setViceImagePath(ConfigConstants.DEFAULT_SECOND_LOGO_FILE_PATH);
        String companyName = Utils.getApp().getResources().getString(R.string.company_name);
        configInfo.setCompanyName(companyName);
        int fdDegreeDef = Utils.getApp().getResources().getInteger(R.integer.face_detect_degree);
        configInfo.setFaceDetectDegree(fdDegreeDef);
        String openDoorTypeDef = Utils.getApp().getResources().getString(R.string.default_open_door_type);
        configInfo.setOpenDoorType(openDoorTypeDef);

        //****************V2.0.0版本修改的地方***********************//
        configInfo.setSuccessRetryDelay(ConfigConstants.DEFAULT_RETRY_DELAY);
        configInfo.setSuccessRetry(ConfigConstants.DEFAULT_RECOGNITION_SUCCESS_RETRY);
        configInfo.setIrLiveThreshold(ConfigConstants.DEFAULT_IR_LIVE_THRESHOLD);
        configInfo.setFaceQuality(ConfigConstants.DEFAULT_BOOL_TRUE);
        configInfo.setFaceQualityThreshold(ConfigConstants.DEFAULT_FACE_QUALITY_THRESHOLD);
        configInfo.setUploadRecordImage(ConfigConstants.DEFAULT_UPLOAD_RECORD_IMAGE);
        configInfo.setRebootEveryDay(ConfigConstants.DEFAULT_BOOL_TRUE);
        configInfo.setRebootHour(ConfigConstants.DEFAULT_DEVICE_REBOOT_HOUR);
        configInfo.setRebootMin(ConfigConstants.DEFAULT_DEVICE_REBOOT_MIN);
        configInfo.setDeviceSleepFollowSys(ConfigConstants.DEFAULT_BOOL_FALSE);
        CommonRepository.getInstance().saveSettingConfigAsync(configInfo, null);
    }

    /**
     * 显示设备存储不足弹框
     */
    private void showDeviceStorageWarnDialog() {
        if (sdCardStorageWarnDialog == null) {
            Activity activity = ActivityUtils.getTopActivity();
            String topClassName = activity.getClass().getSimpleName();
            final WeakReference<Activity> weakReference = new WeakReference<>(activity);
            if (topClassName.equals(RecognizeActivity.class.getSimpleName())) {
                ((RecognizeActivity) activity).setIndexDialogShow(true);
            }
            sdCardStorageWarnDialog = CommonTipDialog
                    .getInstance(Utils.getApp().getResources().getString(R.string.device_storage_warn_tip3),
                            Utils.getApp().getResources().getString(R.string.confirm),
                            "",
                            true,
                            false, true);
            sdCardStorageWarnDialog.setDialogSize((int) getResources().getDimension(R.dimen.x550),
                    (int) getResources().getDimension(R.dimen.x370))
                    .setOutCancel(false)
                    .setConvertViewListener(((holder, baseDialog) -> {
                        holder.getView(R.id.btn_common_dialog_confirm).setOnClickListener(v -> {
                            if (weakReference.get().getClass().getSimpleName().equals(RecognizeActivity.class.getSimpleName())) {
                                ((RecognizeActivity) weakReference.get()).setIndexDialogShow(false);
                            }
                            sdCardStorageWarnDialog.dismissAllowingStateLoss();
                            sdCardStorageWarnDialog = null;
                        });
                    }))
                    .show(getSupportFragmentManager());
        }
    }

    /**
     * 切换UI适配基础配置
     */
    protected void switchAutoSizeDp() {
        if (ScreenUtils.isPortrait()) {
            AutoSize.autoConvertDensity(this, 720, true);
        } else {
            AutoSize.autoConvertDensity(this, 1280, true);
        }
    }

    /**
     * 注册点击home键广播接收器
     */
    private void registerHomeKeyReceiver() {
        mHomeKeyReceiver = new KeyHomeBroadcastReceiver();
        final IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mHomeKeyReceiver, homeFilter);
    }

    private void unregisterHomeKeyReceiver() {
        if (null != mHomeKeyReceiver) {
            unregisterReceiver(mHomeKeyReceiver);
        }
    }

    @Override
    public void onWindowFocusChanged(final boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideNavigationBar();
        }
    }

    @Override
    protected void onResume() {
        switchAutoSizeDp();
        super.onResume();
        registerHomeKeyReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterHomeKeyReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (useEventBus()) {
            EventBus.getDefault().unregister(this);
        }
        sdCardStorageWarnDialog = null;
    }

    private void showInstallDialog(String apkPath) {
        if (installDialog == null) {
            WeakReference<Activity> weakReference = new WeakReference<>(ActivityUtils.getTopActivity());
            installDialog = CommonTipDialog
                    .getInstance(Utils.getApp().getResources().getString(R.string.install_warn_tip),
                            Utils.getApp().getResources().getString(R.string.confirm),
                            Utils.getApp().getResources().getString(R.string.cancel),
                            false,
                            false,
                            true);
            installDialog.setDialogSize((int) getResources().getDimension(R.dimen.x550),
                    (int) getResources().getDimension(R.dimen.x370))
                    .setOutCancel(false)
                    .setConvertViewListener(((holder, baseDialog) -> {
                        holder.getView(R.id.btn_common_dialog_confirm).setOnClickListener(v -> {
                            if (weakReference.get().getClass().getSimpleName().equals(RecognizeActivity.class.getSimpleName())) {
                                ((RecognizeActivity) weakReference.get()).setIndexDialogShow(false);
                            }
                            installDialog.dismissAllowingStateLoss();
                            installDialog = null;

                            installNoSilence(apkPath);
                        });
                        holder.getView(R.id.btn_common_dialog_cancel).setOnClickListener(v -> {
                            if (weakReference.get().getClass().getSimpleName().equals(RecognizeActivity.class.getSimpleName())) {
                                ((RecognizeActivity) weakReference.get()).setIndexDialogShow(false);
                            }
                            installDialog.dismissAllowingStateLoss();
                            installDialog = null;
                        });
                    }))
                    .show(getSupportFragmentManager());

            if (weakReference.get().getClass().getSimpleName().equals(RecognizeActivity.class.getSimpleName())) {
                ((RecognizeActivity) weakReference.get()).setIndexDialogShow(true);
            }
        }
    }

    private void showRebootDialog(boolean autoReboot, InstallCallBack callback) {
        if (rebootDialog == null) {
            rebootDialog = CommonTipDialog
                    .getInstance(Utils.getApp().getResources().getString(R.string.install_reboot_tip),
                            "", "", true, true, true);
            rebootDialog.setDialogSize((int) getResources().getDimension(R.dimen.x550),
                    (int) getResources().getDimension(R.dimen.x370))
                    .setOutCancel(false)
                    .show(getSupportFragmentManager());
            Activity activity = ActivityUtils.getTopActivity();
            String topClassName = activity.getClass().getSimpleName();
            if (topClassName.equals(RecognizeActivity.class.getSimpleName())) {
                ((RecognizeActivity) activity).setIndexDialogShow(true);
            }
            Disposable disposable = Observable.timer(REBOOT_DELAY, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aLong -> {
                        if (callback != null) {
                            callback.installCallBack();
                        }
                        if (rebootDialog != null && rebootDialog.isVisible()) {
                            rebootDialog.dismissAllowingStateLoss();
                            rebootDialog = null;
                        }
                        if (autoReboot) {
                            rebootApp();
                        }
                    });
        }
    }

    private void rebootApp() {
        Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        PendingIntent restartIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, restartIntent);
        CommonRepository.getInstance().exitApp();
    }

    private void installNoSilence(String filePath) {
        File file = new File(filePath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            data = FileProvider.getUriForFile(this, AppUtils.getAppPackageName() + ".fileprovider", file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            data = Uri.fromFile(file);
        }
        intent.setDataAndType(data, "application/vnd.android.package-archive");
        startActivity(intent);
        CommonRepository.getInstance().exitApp();
    }

    private int installSilence(String filePath) {
        File file = new File(filePath);
        if (filePath == null || filePath.length() == 0 || file.length() <= 0 || !file.exists() || !file.isFile()) {
            return 1;
        }

        String[] args = {"pm", "install", "-r", filePath};
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder errorMsg = new StringBuilder();
        int result;
        try {
            process = processBuilder.start();
            successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String s;
            while ((s = successResult.readLine()) != null) {
                successMsg.append(s);
            }
            while ((s = errorResult.readLine()) != null) {
                errorMsg.append(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }

        if (successMsg.toString().contains("Success") || successMsg.toString().contains("success")) {
            result = 0;
        } else {
            result = 2;
        }
        return result;
    }

    public interface InstallCallBack {
        /**
         * 回调
         */
        void installCallBack();
    }
}
