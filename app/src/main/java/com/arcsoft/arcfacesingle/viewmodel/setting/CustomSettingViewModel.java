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

package com.arcsoft.arcfacesingle.viewmodel.setting;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.Editable;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Switch;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.ObservableField;

import com.arcsoft.arcfacesingle.BR;
import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.business.common.CommonRepository;
import com.arcsoft.arcfacesingle.business.setting.ConfigConstants;
import com.arcsoft.arcfacesingle.business.setting.ISetting;
import com.arcsoft.arcfacesingle.business.setting.SettingRepository;
import com.arcsoft.arcfacesingle.data.db.dao.SettingConfigInfoDao;
import com.arcsoft.arcfacesingle.data.db.table.TableSettingConfigInfo;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.asg.libcommon.util.common.SPUtils;
import com.arcsoft.asg.libcommon.util.common.ToastUtils;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CustomSettingViewModel extends BaseObservable {

    private ISetting settingRepository;
    private TableSettingConfigInfo settingConfigInfo;
    private CustomSettingListener settingNavigator;

    private Disposable mainLogoDisposable;
    private Disposable viceLogoDisposable;

    @Bindable
    public final ObservableField<TableSettingConfigInfo> settingInfo = new ObservableField<>();
    public final ObservableField<Bitmap> mainBmpField = new ObservableField<>();
    public final ObservableField<Bitmap> subBmpField = new ObservableField<>();

    public final ObservableField<Boolean> rbPreviewVoiceSuccess = new ObservableField<>();
    public final ObservableField<Integer> srPreviewVoiceSuccess = new ObservableField<>();

    public final ObservableField<Boolean> rbPreviewShowFail = new ObservableField<>();

    public final ObservableField<Boolean> rbPreviewVoiceFail = new ObservableField<>();
    public final ObservableField<Integer> srPreviewVoiceFail = new ObservableField<>();

    public CustomSettingViewModel() {
        settingRepository = SettingRepository.getInstance();
        settingConfigInfo = new TableSettingConfigInfo();
        settingInfo.set(settingConfigInfo);
        onVisible();
    }

    @SuppressLint("MissingPermission")
    private void init() {
        if (settingConfigInfo.getDisplayMode() == ConfigConstants.DISPLAY_MODE_SUCCESS_NAME) {
            settingConfigInfo.setCustomDisplayModeFormat(ConfigConstants.DISPLAY_MODE_FAILED_CUSTOM_VALUE);
        }

        notifySettingInfo();
    }

    private void notifySettingInfo() {
        notifyPropertyChanged(BR.settingInfo);
    }

    public void onSuccessDisplayModeCheckedChanged(RadioGroup group, int checkedId) {
        if (group.getId() == R.id.rg_name_mode) {
            if (checkedId == R.id.rb_full_name) {
                settingInfo.get().setDisplayMode(ConfigConstants.DISPLAY_MODE_SUCCESS_NAME);
            } else if (checkedId == R.id.rb_surname) {
                settingInfo.get().setDisplayMode(ConfigConstants.DISPLAY_MODE_HIDE_LAST_CHAR);
            } else if (checkedId == R.id.rb_custom_name) {
                settingInfo.get().setDisplayMode(ConfigConstants.DISPLAY_MODE_SUCCESS_CUSTOM);
            }
        }

        notifySettingInfo();
    }

    public void onSuccessVoiceModeCheckedChanged(RadioGroup group, int checkedId) {
        if (group.getId() == R.id.rb_success_voice_mode) {
            if (settingConfigInfo.getVoiceMode() == ConfigConstants.SUCCESS_VOICE_MODE_NO_PLAY) {
                return;
            }
            if (checkedId == R.id.rb_success_voice_mode_preview_voice) {
                int voiceMode = settingInfo.get().getVoiceMode();
                if (voiceMode == ConfigConstants.SUCCESS_VOICE_MODE_CUSTOM || voiceMode == ConfigConstants.SUCCESS_VOICE_MODE_NO_PLAY) {
                    voiceMode = ConfigConstants.SUCCESS_VOICE_MODE_PREVIEW_TYPE3;
                    settingInfo.get().setVoiceMode(ConfigConstants.SUCCESS_VOICE_MODE_PREVIEW_TYPE3);
                    srPreviewVoiceFail.set(voiceMode - ConfigConstants.SUCCESS_VOICE_MODE_NAME);
                }
            } else if (checkedId == R.id.rb_success_voice_mode_custom) {
                settingInfo.get().setVoiceMode(ConfigConstants.SUCCESS_VOICE_MODE_CUSTOM);
            }
        }

        notifySettingInfo();
    }

    public void onFailShowModeCheckedChanged(RadioGroup group, int checkedId) {
        if (group.getId() == R.id.rg_fail_show_mode) {
            if (settingConfigInfo.getDisplayModeFail() == ConfigConstants.DISPLAY_MODE_FAILED_NOT_FEEDBACK) {
                return;
            }
            if (checkedId == R.id.rb_fail_show_mode_preview_voice) {
                settingInfo.get().setDisplayModeFail(ConfigConstants.DISPLAY_MODE_FAILED_DEFAULT_MARKUP);
            } else if (checkedId == R.id.rb_fail_show_mode_custom) {
                settingInfo.get().setDisplayModeFail(ConfigConstants.DISPLAY_MODE_FAILED_CUSTOM);
            }
        }

        notifySettingInfo();
    }

    public void onFailVoiceModeCheckedChanged(RadioGroup group, int checkedId) {
        if (group.getId() == R.id.rg_fail_voice_mode) {
            if (settingConfigInfo.getVoiceModeFail() == ConfigConstants.FAILED_VOICE_MODE_NO_PLAY) {
                return;
            }
            if (checkedId == R.id.rb_fail_voice_mode_preview_voice) {
                int voiceMode = settingInfo.get().getVoiceModeFail();
                if (voiceMode == ConfigConstants.FAILED_VOICE_MODE_CUSTOM || voiceMode == ConfigConstants.FAILED_VOICE_MODE_NO_PLAY) {
                    voiceMode = ConfigConstants.FAILED_VOICE_MODE_PREVIEW_TYPE3;
                    settingInfo.get().setVoiceModeFail(ConfigConstants.FAILED_VOICE_MODE_PREVIEW_TYPE3);
                    srPreviewVoiceFail.set(voiceMode - ConfigConstants.FAILED_VOICE_MODE_WARN);
                }
            } else if (checkedId == R.id.rb_fail_voice_mode_custom) {
                settingInfo.get().setVoiceModeFail(ConfigConstants.FAILED_VOICE_MODE_CUSTOM);
            }
        }

        notifySettingInfo();
    }

    public void onInputCompanyNameTextChanged(Editable editable) {
        String strContent = editable.toString();
        if (!CommonUtils.compileExChar(strContent)) {
            if (!settingInfo.get().getCompanyName().equals(strContent)) {
                settingInfo.get().setCompanyName(strContent.trim());
                notifySettingInfo();
            }
        }
    }

    public void onSuccessShowModeTextChanged(Editable editable) {
        String strContent = editable.toString();
        if (!CommonUtils.compileExChar(strContent)) {
            if (!settingInfo.get().getCustomDisplayModeFormat().equals(strContent)) {
                settingInfo.get().setCustomDisplayModeFormat(strContent.trim());
                notifySettingInfo();
            }
        }
    }

    public void onSuccessCustomVoiceTextChanged(Editable editable) {
        String strContent = editable.toString();
        if (!CommonUtils.compileExChar(strContent)) {
            if (!settingInfo.get().getCustomVoiceModeFormat().equals(strContent)) {
                settingInfo.get().setCustomVoiceModeFormat(strContent.trim());
                notifySettingInfo();
            }
        }
    }

    public void onFailedShowModeTextChanged(Editable editable) {
        String strContent = editable.toString();
        if (!CommonUtils.compileExChar(strContent)) {
            if (!settingInfo.get().getCustomFailDisplayModeFormat().equals(strContent)) {
                settingInfo.get().setCustomFailDisplayModeFormat(strContent.trim());
                notifySettingInfo();
            }
        }
    }

    public void onFailCustomVoiceTextChanged(Editable editable) {
        String strContent = editable.toString();
        if (!CommonUtils.compileExChar(strContent)) {
            if (!settingInfo.get().getCustomFailVoiceModeFormat().equals(strContent)) {
                settingInfo.get().setCustomFailVoiceModeFormat(strContent.trim());
                notifySettingInfo();
            }
        }
    }

    public void onSuccessVoiceModeSwitchClicked(View view) {
        if (view.getId() == R.id.sh_success_voice_mode) {
            Switch switcher = (Switch) view;
            if (switcher.isChecked()) {
                settingInfo.get().setVoiceMode(ConfigConstants.SUCCESS_VOICE_MODE_PREVIEW_TYPE3);
            } else {
                settingInfo.get().setVoiceMode(ConfigConstants.SUCCESS_VOICE_MODE_NO_PLAY);
            }
        }
        notifySettingInfo();
        updateSuccessVoiceMode();
    }

    public void onFailShowModeSwitchClicked(View view) {
        if (view.getId() == R.id.sh_fail_show_mode) {
            Switch switcher = (Switch) view;
            if (switcher.isChecked()) {
                settingInfo.get().setDisplayModeFail(ConfigConstants.DISPLAY_MODE_FAILED_DEFAULT_MARKUP);
            } else {
                settingInfo.get().setDisplayModeFail(ConfigConstants.DISPLAY_MODE_FAILED_NOT_FEEDBACK);
            }
        }
        notifySettingInfo();
        updateFailShowMode();
    }

    public void onFailVoiceSwitchClicked(View view) {
        if (view.getId() == R.id.sh_fail_voice_mode) {
            Switch switcher = (Switch) view;
            if (switcher.isChecked()) {
                settingInfo.get().setVoiceModeFail(ConfigConstants.FAILED_VOICE_MODE_PREVIEW_TYPE3);
            } else {
                settingInfo.get().setVoiceModeFail(ConfigConstants.FAILED_VOICE_MODE_NO_PLAY);
            }
        }
        notifySettingInfo();
        updateFailVoiceMode();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_set_company_main_logo:
                if (mainBmpField.get() != null) {
                    if (settingNavigator != null) {
                        settingNavigator.showDeleteCompanyLogoDialog(true);
                    }
                } else {
                    if (settingNavigator != null) {
                        settingNavigator.goGallery(ConfigConstants.CHOOSE_PICTURE_MAIN_LOGO);
                    }
                    if (settingRepository != null) {
                        settingRepository.disposeDelayClosePageTimer();
                    }
                }
                break;
            case R.id.btn_set_company_second_logo:
                if (subBmpField.get() != null) {
                    if (settingNavigator != null) {
                        settingNavigator.showDeleteCompanyLogoDialog(false);
                    }
                } else {
                    if (settingNavigator != null) {
                        settingNavigator.goGallery(ConfigConstants.CHOOSE_PICTURE_SECOND_LOGO);
                    }
                    if (settingRepository != null) {
                        settingRepository.disposeDelayClosePageTimer();
                    }
                }
                break;
            case R.id.tv_show_settings:
                if (settingRepository != null) {
                    settingRepository.clickMultipleExitApp();
                }
                break;
            default:
                break;
        }

    }

    /**
     * 更新识别成功预置语音类型
     * P
     *
     * @param itemPosition Spinner位置
     */
    public void updateSuccessPreviewVoiceMode(int itemPosition) {
        int voiceMode = settingInfo.get().getVoiceMode();
        if (voiceMode != ConfigConstants.SUCCESS_VOICE_MODE_NO_PLAY && voiceMode != ConfigConstants.SUCCESS_VOICE_MODE_CUSTOM) {
            srPreviewVoiceSuccess.set(itemPosition);
            settingInfo.get().setVoiceMode(ConfigConstants.SUCCESS_VOICE_MODE_NAME + itemPosition);
            notifySettingInfo();
        }
    }

    /**
     * 更新识别失败预置语音类型
     * P
     *
     * @param itemPosition Spinner位置
     */
    public void updateFailPreviewVoiceMode(int itemPosition) {
        int voiceMode = settingInfo.get().getVoiceModeFail();
        if (voiceMode != ConfigConstants.FAILED_VOICE_MODE_NO_PLAY && voiceMode != ConfigConstants.FAILED_VOICE_MODE_CUSTOM) {
            srPreviewVoiceFail.set(itemPosition);
            settingInfo.get().setVoiceModeFail(ConfigConstants.FAILED_VOICE_MODE_WARN + itemPosition);
            notifySettingInfo();
        }
    }

    private void updateSuccessVoiceMode() {
        int voiceMode = settingInfo.get().getVoiceMode();
        if (voiceMode == ConfigConstants.SUCCESS_VOICE_MODE_NO_PLAY) {
            rbPreviewVoiceSuccess.set(true);
        } else if (voiceMode >= ConfigConstants.SUCCESS_VOICE_MODE_NAME && voiceMode <= ConfigConstants.SUCCESS_VOICE_MODE_PREVIEW_TYPE6) {
            srPreviewVoiceSuccess.set(voiceMode - ConfigConstants.SUCCESS_VOICE_MODE_NAME);
            rbPreviewVoiceSuccess.set(true);
        } else if (voiceMode == ConfigConstants.SUCCESS_VOICE_MODE_CUSTOM) {
            rbPreviewVoiceSuccess.set(false);
        }
    }

    private void updateFailShowMode() {
        int showMode = settingInfo.get().getDisplayModeFail();
        if (showMode == ConfigConstants.DISPLAY_MODE_FAILED_NOT_FEEDBACK) {
            rbPreviewShowFail.set(true);
        } else if (showMode == ConfigConstants.DISPLAY_MODE_FAILED_DEFAULT_MARKUP) {
            rbPreviewShowFail.set(true);
        } else if (showMode == ConfigConstants.DISPLAY_MODE_FAILED_CUSTOM) {
            rbPreviewShowFail.set(false);
        }
    }

    private void updateFailVoiceMode() {
        int voiceMode = settingInfo.get().getVoiceModeFail();
        if (voiceMode == ConfigConstants.FAILED_VOICE_MODE_NO_PLAY) {
            rbPreviewVoiceFail.set(true);
        } else if (voiceMode >= ConfigConstants.FAILED_VOICE_MODE_WARN && voiceMode <= ConfigConstants.FAILED_VOICE_MODE_PREVIEW_TYPE4) {
            srPreviewVoiceFail.set(voiceMode - ConfigConstants.FAILED_VOICE_MODE_WARN);
            rbPreviewVoiceFail.set(true);
        } else if (voiceMode == ConfigConstants.FAILED_VOICE_MODE_CUSTOM) {
            rbPreviewVoiceFail.set(false);
        }
    }

    /**
     * 设置logo
     *
     * @param uri
     * @param type
     */
    public void setCompanyLogo(Uri uri, int type) {
        if (settingRepository != null) {
            settingRepository.setCompanyLogo(uri, type, (isMain, path) -> {
                setMainLogo(isMain, path);
                ToastUtils.showShortToast(isMain ? R.string.main_logo_setting_success : R.string.second_logo_setting_success);
            });
        }
    }

    public void setMainLogo(boolean mainLogo, String path) {
        if (mainLogo) {
            if (mainLogoDisposable != null && !mainLogoDisposable.isDisposed()) {
                mainLogoDisposable.dispose();
            }
            mainLogoDisposable = Observable.create((ObservableOnSubscribe<Bitmap>) emitter -> {
                Bitmap bitmap = settingRepository.getMainLogo(path);
                if (bitmap != null) {
                    emitter.onNext(bitmap);
                    emitter.onComplete();
                } else {
                    emitter.onError(new Throwable());
                }
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bitmap -> {
                        settingConfigInfo.setMainImagePath(path);
                        mainBmpField.set(bitmap);
                        mainLogoDisposable = null;
                    }, throwable -> {
                        settingConfigInfo.setMainImagePath("");
                        mainBmpField.set(null);
                        mainLogoDisposable = null;
                    });
        } else {
            if (viceLogoDisposable != null && !viceLogoDisposable.isDisposed()) {
                viceLogoDisposable.dispose();
            }
            viceLogoDisposable = Observable.create((ObservableOnSubscribe<Bitmap>) emitter -> {
                Bitmap bitmap = settingRepository.getViceLogo(path);
                if (bitmap != null) {
                    emitter.onNext(bitmap);
                    emitter.onComplete();
                } else {
                    emitter.onError(new Throwable());
                }
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bitmap -> {
                        settingConfigInfo.setViceImagePath(path);
                        subBmpField.set(bitmap);
                        viceLogoDisposable = null;
                    }, throwable -> {
                        settingConfigInfo.setViceImagePath("");
                        subBmpField.set(null);
                        viceLogoDisposable = null;
                    });
        }
    }

    public void deleteLogo(boolean mainLogo) {
        if (mainLogo) {
            settingConfigInfo.setMainImagePath("");
            CommonRepository.getInstance().getSettingConfigInfo().setMainImagePath("");
            SettingConfigInfoDao.getInstance().updateMainLogoPathSync("");
            CommonRepository.getInstance().saveMainLogoId("");
            SPUtils.getInstance().put(Constants.SP_KEY_CLOUD_MAIN_LOGO_URL, "");
            mainBmpField.set(null);
        } else {
            settingConfigInfo.setViceImagePath("");
            CommonRepository.getInstance().getSettingConfigInfo().setViceImagePath("");
            SettingConfigInfoDao.getInstance().updateSecondLogoPathSync("");
            CommonRepository.getInstance().saveSecondLogoId("");
            subBmpField.set(null);
            SPUtils.getInstance().put(Constants.SP_KEY_CLOUD_SECOND_LOGO_URL, "");
        }
    }

    public void setCustomSettingListener(CustomSettingListener customSettingListener) {
        settingNavigator = customSettingListener;
    }

    public void onResume() {

    }

    public void onPause() {
    }

    public void onDestroy() {
        settingRepository = null;
        settingConfigInfo = null;
        settingNavigator = null;
        if (mainLogoDisposable != null && !mainLogoDisposable.isDisposed()) {
            mainLogoDisposable.dispose();
        }
        if (viceLogoDisposable != null && !viceLogoDisposable.isDisposed()) {
            viceLogoDisposable.dispose();
        }
    }

    public void onVisible() {
        reloadSettingInfo(CommonRepository.getInstance().getSettingConfigInfo(), settingConfigInfo);
        init();
        setMainLogo(true, settingConfigInfo.getMainImagePath());
        setMainLogo(false, settingConfigInfo.getViceImagePath());
        updateSuccessVoiceMode();
        updateFailShowMode();
        updateFailVoiceMode();
    }

    public void reloadSettingInfo(TableSettingConfigInfo srcInfo, TableSettingConfigInfo desInfo) {
        desInfo.setCompanyName(srcInfo.getCompanyName());
        desInfo.setMainImagePath(srcInfo.getMainImagePath());
        desInfo.setViceImagePath(srcInfo.getViceImagePath());
        desInfo.setDisplayMode(srcInfo.getDisplayMode());
        desInfo.setVoiceMode(srcInfo.getVoiceMode());
        desInfo.setDisplayModeFail(srcInfo.getDisplayModeFail());
        desInfo.setVoiceModeFail(srcInfo.getVoiceModeFail());
        desInfo.setCustomDisplayModeFormat(srcInfo.getCustomDisplayModeFormat());
        desInfo.setCustomVoiceModeFormat(srcInfo.getCustomVoiceModeFormat());
        desInfo.setCustomFailDisplayModeFormat(srcInfo.getCustomFailDisplayModeFormat());
        desInfo.setCustomFailVoiceModeFormat(srcInfo.getCustomFailVoiceModeFormat());
    }

    public void getConfig(TableSettingConfigInfo configInfo) {
        reloadSettingInfo(settingConfigInfo, configInfo);
    }

    public interface CustomSettingListener {
        /**
         * 删除logo
         *
         * @param deleteMain logo类型
         */
        void showDeleteCompanyLogoDialog(boolean deleteMain);

        /**
         * 跳转相册
         *
         * @param requestCode logo类型
         */
        void goGallery(int requestCode);
    }
}
