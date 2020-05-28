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

package com.arcsoft.arcfacesingle.business.setting;

import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Pair;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.business.common.CommonRepository;
import com.arcsoft.arcfacesingle.data.db.table.TableSettingConfigInfo;
import com.arcsoft.arcfacesingle.libutil.ImageFileUtils;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.arcfacesingle.util.scheduler.BaseObserver;
import com.arcsoft.arcsoftlink.http.bean.res.UploadDataResponse;
import com.arcsoft.arcsoftlink.mqtt.ArcLinkEngine;
import com.arcsoft.arcsoftlink.mqtt.ArcLinkException;
import com.arcsoft.asg.libcommon.util.business.rx.RxUtils;
import com.arcsoft.asg.libcommon.util.common.SPUtils;
import com.arcsoft.asg.libcommon.util.common.ToastUtils;
import com.arcsoft.asg.libcommon.util.common.Utils;

import java.io.File;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

public class SettingRepository implements ISetting {

    private static volatile SettingRepository INSTANCE;
    private static final int CLICK_EXIT_APP_TIMES_MAX = 5;
    private static final int CLICK_EXIT_APP_TIMES_INIT = 10 * 1000;
    private static final int DELAY_NO_TOUCH_CLOSE_PAGE = 5 * 60 * 1000;
    private static final int LOGO_WIDTH_OR_HEIGHT_MAX = 500;

    /**
     * 多次点击退出App
     */
    private int clickExitAppTimes;

    private TableSettingConfigInfo settingConfigInfo;
    private Disposable exitAppDisposable;
    private Disposable noTouchCloseDisposable;

    private SettingRepository() {
        settingConfigInfo = CommonRepository.getInstance().getSettingConfigInfo();
    }

    public static SettingRepository getInstance() {
        if (INSTANCE == null) {
            synchronized (SettingRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SettingRepository();
                }
            }
        } else {
            if (INSTANCE.settingConfigInfo == null) {
                INSTANCE.settingConfigInfo = CommonRepository.getInstance().getSettingConfigInfo();
            }
        }
        return INSTANCE;
    }

    @Override
    public Bitmap getMainLogo(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        } else {
            File mainLogoFile = new File(path);
            if (mainLogoFile.exists()) {
                return ImageFileUtils.getBitmap(mainLogoFile.getAbsoluteFile());
            } else {
                Bitmap bmpMainLogo = ImageFileUtils.getBitmap(R.mipmap.ic_company_main_logo);
                ImageFileUtils.save(bmpMainLogo, mainLogoFile, Bitmap.CompressFormat.PNG);
                return bmpMainLogo;
            }
        }
    }

    @Override
    public Bitmap getViceLogo(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        } else {
            File secondLogoFile = new File(path);
            if (secondLogoFile.exists()) {
                return ImageFileUtils.getBitmap(secondLogoFile.getAbsoluteFile());
            } else {
                Bitmap bmpViceLogo = ImageFileUtils.getBitmap(R.mipmap.ic_company_second_logo);
                ImageFileUtils.save(bmpViceLogo, secondLogoFile, Bitmap.CompressFormat.PNG);
                return bmpViceLogo;
            }
        }
    }

    @Override
    public void setCompanyLogo(Uri uri, int type, SettingLogoCallBack callBack) {
        Disposable disposable = Observable.just(uri)
                .flatMap((Function<Uri, ObservableSource<Bitmap>>) uri1 -> Observable.create(emitter -> {
                    try {
                        String logoPath = CommonUtils.getRealPathFromUri(Utils.getApp(), uri1);
                        Pair<Integer, Integer> pair = ImageFileUtils.getImageOption(logoPath);
                        int width = pair.first;
                        int height = pair.second;
                        if (width >= height && width > LOGO_WIDTH_OR_HEIGHT_MAX) {
                            emitter.onError(new Throwable(CommonUtils.getStrFromRes(R.string.logo_picture_width_should_not_exceed,
                                    LOGO_WIDTH_OR_HEIGHT_MAX)));
                            return;
                        }
                        if (height > width && height > LOGO_WIDTH_OR_HEIGHT_MAX) {
                            emitter.onError(new Throwable(CommonUtils.getStrFromRes(R.string.logo_picture_height_should_not_exceed,
                                    LOGO_WIDTH_OR_HEIGHT_MAX)));
                            return;
                        }
                        File pic = new File(logoPath);
                        if (pic.length() > Constants.MAX_LOGO_PICTURE_SIZE) {
                            emitter.onError(new Throwable(CommonUtils.getStrFromRes(R.string.logo_picture_is_too_big_please_try_again)));
                            return;
                        }
                        Bitmap bitmap = ImageFileUtils.getImagePng(CommonUtils.getRealPathFromUri(Utils.getApp(), uri1),
                                Constants.MAX_LOGO_PICTURE_SIZE);
                        if (bitmap == null) {
                            emitter.onError(new Throwable(CommonUtils.getStrFromRes(R.string.setting_image_invalid)));
                            return;
                        }
                        emitter.onNext(bitmap);
                        emitter.onComplete();
                    } catch (Exception e) {
                        e.printStackTrace();
                        emitter.onError(new Throwable(CommonUtils.getStrFromRes(R.string.logo_setup_failed_try_again)));
                    }
                })).flatMap((Function<Bitmap, ObservableSource<String>>) bitmap -> {
                    String imagePath;
                    if (type == ConfigConstants.CHOOSE_PICTURE_MAIN_LOGO) {
                        imagePath = ConfigConstants.DEFAULT_MAIN_LOGO_FILE_PATH;
                    } else {
                        imagePath = ConfigConstants.DEFAULT_SECOND_LOGO_FILE_PATH;
                    }
                    if (CommonUtils.isOfflineLanAppMode()) {
                        if (ImageFileUtils.save(bitmap, imagePath, Bitmap.CompressFormat.PNG)) {
                            return Observable.just(imagePath);
                        } else {
                            return Observable.just("");
                        }
                    } else {
                        byte[] logoBytes = ImageFileUtils.bitmap2Bytes(bitmap, Bitmap.CompressFormat.PNG);
                        try {
                            String fileName;
                            if (type == ConfigConstants.CHOOSE_PICTURE_MAIN_LOGO) {
                                fileName = "mainLogo.png";
                            } else {
                                fileName = "secondLogo.png";
                            }
                            UploadDataResponse response = ArcLinkEngine.getInstance().uploadData(logoBytes, fileName);
                            if (response != null) {
                                String logoUrl = response.getData().getHttpUrl();
                                if (ImageFileUtils.save(bitmap, imagePath, Bitmap.CompressFormat.PNG)) {
                                    if (type == ConfigConstants.CHOOSE_PICTURE_MAIN_LOGO) {
                                        SPUtils.getInstance().put(Constants.SP_KEY_CLOUD_MAIN_LOGO_URL, logoUrl);
                                    } else {
                                        SPUtils.getInstance().put(Constants.SP_KEY_CLOUD_SECOND_LOGO_URL, logoUrl);
                                    }
                                    return Observable.just(imagePath);
                                } else {
                                    return Observable.just("");
                                }
                            } else {
                                return Observable.just("");
                            }
                        } catch (ArcLinkException e) {
                            e.printStackTrace();
                            return Observable.just("");
                        }
                    }
                })
                .compose(RxUtils.ioToMain())
                .subscribeWith(new BaseObserver<String>() {

                    @Override
                    public void onNext(String value) {
                        if (!TextUtils.isEmpty(value)) {
                            String logoId = CommonRepository.getInstance().createDatabaseId();
                            if (type == ConfigConstants.CHOOSE_PICTURE_MAIN_LOGO) {
                                CommonRepository.getInstance().saveMainLogoId(logoId);
                                settingConfigInfo.setMainImagePath(value);
                                if (callBack != null) {
                                    callBack.onSetLogoSuccess(true, value);
                                }
                            } else {
                                CommonRepository.getInstance().saveSecondLogoId(logoId);
                                settingConfigInfo.setViceImagePath(value);
                                if (callBack != null) {
                                    callBack.onSetLogoSuccess(false, value);
                                }
                            }
                            CommonRepository.getInstance().saveSettingConfigAsync(settingConfigInfo, null);
                        } else {
                            ToastUtils.showShortToast(CommonUtils.getStrFromRes(R.string.logo_setup_failed_try_again));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        ToastUtils.showShortToast(e.getMessage());
                    }
                });
    }

    @Override
    public boolean checkSaveSettingConfigInfo(TableSettingConfigInfo configInfo) {
        if (configInfo == null) {
            ToastUtils.showShortToast(R.string.save_fail_try_again);
            return false;
        }
        if (TextUtils.isEmpty(configInfo.getDevicePort())) {
            ToastUtils.showShortToast(R.string.please_input_device_port);
            return false;
        }
        int devicePort = Integer.parseInt(configInfo.getDevicePort());
        if (devicePort < ConfigConstants.DEVICE_PORT_MIN || devicePort > ConfigConstants.DEVICE_PORT_MAX) {
            ToastUtils.showShortToast(R.string.device_port_range);
            return false;
        }

        if (CommonUtils.isOfflineLanAppMode() && TextUtils.isEmpty(configInfo.getDeviceName())) {
            ToastUtils.showLongToast(R.string.device_name_tip);
            return false;
        }
        if (!configInfo.isScreenBrightFollowSys()) {
            if (TextUtils.isEmpty(configInfo.getScreenDefBrightPercent())) {
                ToastUtils.showShortToast(R.string.please_enter_the_brightness_of_standby_screen);
                return false;
            }
        }
        if (configInfo.isRebootEveryDay()) {
            if (TextUtils.isEmpty(configInfo.getRebootHour()) || TextUtils.isEmpty(configInfo.getRebootMin())) {
                ToastUtils.showShortToast(R.string.please_enter_the_daily_reboot_time);
                return false;
            }
        }
        if (TextUtils.isEmpty(configInfo.getCloseDoorDelay())) {
            ToastUtils.showShortToast(R.string.please_enter_closing_delay);
            return false;
        }
        String strThres = configInfo.getSimilarThreshold();
        if (TextUtils.isEmpty(strThres)) {
            ToastUtils.showShortToast(R.string.please_enter_recognition_threshold);
            return false;
        }
        float threshold = Float.parseFloat(strThres);
        if (threshold < ConfigConstants.MIN_THRESHOLD || threshold > ConfigConstants.MAX_THRESHOLD) {
            ToastUtils.showShortToast(R.string.recognition_threshold_range_is_0_1);
            return false;
        }
        String strRetryDelay = configInfo.getRecognitionRetryDelay();
        if (TextUtils.isEmpty(strRetryDelay)) {
            ToastUtils.showShortToast(R.string.please_enter_the_failed_retry_interval);
            return false;
        }
        float retryDelay = Float.parseFloat(strRetryDelay);
        if (retryDelay < ConfigConstants.RETRY_DELAY_MIN || retryDelay > ConfigConstants.RETRY_DELAY_MAX) {
            ToastUtils.showShortToast(R.string.delay_range_hint);
            return false;
        }
        if (configInfo.getSuccessRetry() == ConfigConstants.RECOGNITION_SUCCESS_RETRY_OPEN) {
            String strSuccessRetryDelay = configInfo.getSuccessRetryDelay();
            if (TextUtils.isEmpty(strSuccessRetryDelay)) {
                ToastUtils.showShortToast(R.string.please_enter_the_success_retry_interval);
                return false;
            }
            float successRetryDelay = Float.parseFloat(strSuccessRetryDelay);
            if (successRetryDelay < ConfigConstants.RETRY_DELAY_MIN || successRetryDelay > ConfigConstants.RETRY_DELAY_MAX) {
                ToastUtils.showShortToast(R.string.delay_range_hint);
                return false;
            }
        }
        if (configInfo.getLiveDetectType() == ConfigConstants.DEFAULT_LIVE_DETECT_IR) {
            String stringIrThreshold = configInfo.getIrLiveThreshold();
            if (TextUtils.isEmpty(stringIrThreshold)) {
                ToastUtils.showShortToast(R.string.please_enter_the_threshold_of_ir);
                return false;
            }
            float irThreshold = Float.parseFloat(stringIrThreshold);
            if (irThreshold < ConfigConstants.MIN_THRESHOLD || irThreshold > ConfigConstants.MAX_THRESHOLD) {
                ToastUtils.showShortToast(R.string.the_threshold_of_ir_was_0_1);
                return false;
            }
        }
        if (configInfo.isFaceQuality()) {
            String strFqThreshold = configInfo.getFaceQualityThreshold();
            if (TextUtils.isEmpty(strFqThreshold)) {
                ToastUtils.showShortToast(R.string.fq_empty_hint);
                return false;
            }
            float irThreshold = Float.parseFloat(strFqThreshold);
            if (irThreshold < ConfigConstants.MIN_THRESHOLD || irThreshold > ConfigConstants.MAX_THRESHOLD) {
                ToastUtils.showShortToast(R.string.fq_range_hint);
                return false;
            }
        }
        if (configInfo.getDisplayMode() == ConfigConstants.DISPLAY_MODE_SUCCESS_CUSTOM) {
            String strContent = configInfo.getCustomDisplayModeFormat();
            if (CommonUtils.compileExChar(strContent)) {
                ToastUtils.showShortToast(R.string.custom_content_should_not_contain_single_quotation);
                return false;
            }
            if (!TextUtils.isEmpty(strContent) && strContent.length() > Constants.MESSAGE_INPUT_STRING_MAX_LENGTH) {
                ToastUtils.showShortToast(R.string.custom_content_should_not_exceed_30_characters);
                return false;
            }
        }
        if (configInfo.getVoiceMode() == ConfigConstants.SUCCESS_VOICE_MODE_CUSTOM) {
            String strContent = configInfo.getCustomVoiceModeFormat();
            if (CommonUtils.compileExChar(strContent)) {
                ToastUtils.showShortToast(R.string.custom_content_should_not_contain_single_quotation);
                return false;
            }
            if (!TextUtils.isEmpty(strContent) && strContent.length() > Constants.MESSAGE_INPUT_STRING_MAX_LENGTH) {
                ToastUtils.showShortToast(R.string.custom_content_should_not_exceed_30_characters);
                return false;
            }
        }
        if (configInfo.getDisplayModeFail() == ConfigConstants.DISPLAY_MODE_FAILED_CUSTOM) {
            String strContent = configInfo.getCustomFailDisplayModeFormat();
            if (CommonUtils.compileExChar(strContent)) {
                ToastUtils.showShortToast(R.string.custom_content_should_not_contain_single_quotation);
                return false;
            }
            if (!TextUtils.isEmpty(strContent) && strContent.length() > Constants.MESSAGE_INPUT_STRING_MAX_LENGTH) {
                ToastUtils.showShortToast(R.string.custom_content_should_not_exceed_30_characters);
                return false;
            }
        }
        if (configInfo.getVoiceMode() == ConfigConstants.SUCCESS_VOICE_MODE_NONE) {
            ToastUtils.showShortToast(R.string.please_select_the_successful_voice_preset_type);
            return false;
        }
        if (configInfo.getVoiceModeFail() == ConfigConstants.FAILED_VOICE_MODE_CUSTOM) {
            String strContent = configInfo.getCustomFailVoiceModeFormat();
            if (CommonUtils.compileExChar(strContent)) {
                ToastUtils.showShortToast(R.string.custom_content_should_not_contain_single_quotation);
                return false;
            }
            if (!TextUtils.isEmpty(strContent) && strContent.length() > Constants.MESSAGE_INPUT_STRING_MAX_LENGTH) {
                ToastUtils.showShortToast(R.string.custom_content_should_not_exceed_30_characters);
                return false;
            }
        }
        if (configInfo.getVoiceModeFail() == ConfigConstants.FAILED_VOICE_MODE_NONE) {
            ToastUtils.showShortToast(R.string.please_select_the_failed_voice_preset_type);
            return false;
        }
        String strCompanyName = configInfo.getCompanyName();
        if (CommonUtils.compileExChar(strCompanyName)) {
            ToastUtils.showShortToast(R.string.subscript_text_should_not_contain_single_quotation);
            return false;
        }
        if (!TextUtils.isEmpty(strCompanyName) && strCompanyName.length() > Constants.MESSAGE_INPUT_STRING_MAX_LENGTH) {
            ToastUtils.showShortToast(R.string.subscript_text_should_not_exceed_30_characters);
            return false;
        }
        return true;
    }

    @Override
    public boolean saveSettingConfigInfo(TableSettingConfigInfo modifiedConfigInfo) {
        if (settingConfigInfo != null && modifiedConfigInfo != null) {
            settingConfigInfo.setCameraDegree(modifiedConfigInfo.getCameraDegree());
            settingConfigInfo.setDeviceIp(modifiedConfigInfo.getDeviceIp());
            settingConfigInfo.setCloseDoorDelay(modifiedConfigInfo.getCloseDoorDelay());
            settingConfigInfo.setCompanyName(modifiedConfigInfo.getCompanyName());
            settingConfigInfo.setCustomDisplayModeFormat(modifiedConfigInfo.getCustomDisplayModeFormat());
            settingConfigInfo.setCustomFailDisplayModeFormat(modifiedConfigInfo.getCustomFailDisplayModeFormat());
            settingConfigInfo.setCustomFailVoiceModeFormat(modifiedConfigInfo.getCustomFailVoiceModeFormat());
            settingConfigInfo.setCustomOutPutFormat(modifiedConfigInfo.getCustomOutPutFormat());
            settingConfigInfo.setCustomVoiceModeFormat(modifiedConfigInfo.getCustomVoiceModeFormat());
            settingConfigInfo.setDeviceId(modifiedConfigInfo.getDeviceId());
            settingConfigInfo.setDeviceSleepFollowSys(modifiedConfigInfo.isDeviceSleepFollowSys());
            settingConfigInfo.setDisplayMode(modifiedConfigInfo.getDisplayMode());
            settingConfigInfo.setDisplayModeFail(modifiedConfigInfo.getDisplayModeFail());
            settingConfigInfo.setFaceDetectDegree(modifiedConfigInfo.getFaceDetectDegree());
            settingConfigInfo.setId(modifiedConfigInfo.getId());
            settingConfigInfo.setIndexScreenDefShow(modifiedConfigInfo.isIndexScreenDefShow());
            settingConfigInfo.setLandscape(modifiedConfigInfo.isLandscape());
            settingConfigInfo.setLandscapeMoveMirror(modifiedConfigInfo.isLandscapeMoveMirror());
            settingConfigInfo.setLivenessDetect(modifiedConfigInfo.isLivenessDetect());
            settingConfigInfo.setMacAddress(modifiedConfigInfo.getMacAddress());
            settingConfigInfo.setMainImagePath(modifiedConfigInfo.getMainImagePath());
            settingConfigInfo.setMirror(modifiedConfigInfo.isMirror());
            settingConfigInfo.setPermission(modifiedConfigInfo.getPermission());
            settingConfigInfo.setPortraitMoveMirror(modifiedConfigInfo.isPortraitMoveMirror());
            settingConfigInfo.setPreviewShowHalf(modifiedConfigInfo.isPreviewShowHalf());
            settingConfigInfo.setRebootEveryDay(modifiedConfigInfo.isRebootEveryDay());
            settingConfigInfo.setRebootHour(modifiedConfigInfo.getRebootHour());
            settingConfigInfo.setRebootMin(modifiedConfigInfo.getRebootMin());
            settingConfigInfo.setRecognitionRetryDelay(modifiedConfigInfo.getRecognitionRetryDelay());
            settingConfigInfo.setRecognizeDistance(modifiedConfigInfo.getRecognizeDistance());
            settingConfigInfo.setScale(modifiedConfigInfo.getScale());
            settingConfigInfo.setScreenBrightFollowSys(modifiedConfigInfo.isScreenBrightFollowSys());
            settingConfigInfo.setScreenDefBrightPercent(modifiedConfigInfo.getScreenDefBrightPercent());
            settingConfigInfo.setSerialNumber(modifiedConfigInfo.getSerialNumber());
            settingConfigInfo.setDevicePort(modifiedConfigInfo.getDevicePort());
            settingConfigInfo.setSignKey(modifiedConfigInfo.getSignKey());
            settingConfigInfo.setSignType(modifiedConfigInfo.getSignType());
            settingConfigInfo.setSimilarThreshold(modifiedConfigInfo.getSimilarThreshold());
            settingConfigInfo.setUseFrontCamera(modifiedConfigInfo.isUseFrontCamera());
            settingConfigInfo.setViceImagePath(modifiedConfigInfo.getViceImagePath());
            settingConfigInfo.setVoiceMode(modifiedConfigInfo.getVoiceMode());
            settingConfigInfo.setVoiceModeFail(modifiedConfigInfo.getVoiceModeFail());

            //V2.0
            settingConfigInfo.setSuccessRetryDelay(modifiedConfigInfo.getSuccessRetryDelay());
            settingConfigInfo.setSuccessRetry(modifiedConfigInfo.getSuccessRetry());
            settingConfigInfo.setLiveDetectType(modifiedConfigInfo.getLiveDetectType());
            settingConfigInfo.setIrLivePreview(modifiedConfigInfo.getIrLivePreview());
            settingConfigInfo.setUploadRecordImage(modifiedConfigInfo.getUploadRecordImage());

            //V2.0 2019-09-05
            settingConfigInfo.setIrLiveThreshold(modifiedConfigInfo.getIrLiveThreshold());

            settingConfigInfo.setFaceQuality(modifiedConfigInfo.isFaceQuality());
            settingConfigInfo.setFaceQualityThreshold(modifiedConfigInfo.getFaceQualityThreshold());
            settingConfigInfo.setDeviceName(modifiedConfigInfo.getDeviceName());

            CommonRepository.getInstance().saveSettingConfigAsync(settingConfigInfo, null);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void clickMultipleExitApp() {
        clickExitAppTimes++;
        if (clickExitAppTimes == CLICK_EXIT_APP_TIMES_MAX) {
            CommonRepository.getInstance().sendExitAppBroadcast();
            CommonRepository.getInstance().exitApp();
            return;
        }
        disposeExitAppTimer();
        exitAppDisposable = Observable.timer(CLICK_EXIT_APP_TIMES_INIT, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> clickExitAppTimes = 0);
    }

    private void disposeExitAppTimer() {
        if (exitAppDisposable != null && !exitAppDisposable.isDisposed()) {
            exitAppDisposable.dispose();
            exitAppDisposable = null;
        }
    }

    @Override
    public void startDelayClosePageTimer(SettingCloseCallBack callBack) {
        disposeDelayClosePageTimer();
        noTouchCloseDisposable = Observable.timer(DELAY_NO_TOUCH_CLOSE_PAGE, TimeUnit.MILLISECONDS)
                .compose(RxUtils.computingToMain())
                .subscribe(aLong -> {
                    if (callBack != null) {
                        callBack.closePage();
                    }
                });
    }

    @Override
    public void disposeDelayClosePageTimer() {
        if (noTouchCloseDisposable != null && !noTouchCloseDisposable.isDisposed()) {
            noTouchCloseDisposable.dispose();
            noTouchCloseDisposable = null;
        }
    }

    @Override
    public void unInit() {
        settingConfigInfo = null;
        disposeDelayClosePageTimer();
        disposeExitAppTimer();
    }

    public interface SettingLogoCallBack {

        /**
         * 设置logo成功
         * @param mainLogo
         * @param path
         */
        void onSetLogoSuccess(boolean mainLogo, String path);
    }

    public interface SettingCloseCallBack {

        /**
         * 关闭页面
         */
        void closePage();
    }
}
