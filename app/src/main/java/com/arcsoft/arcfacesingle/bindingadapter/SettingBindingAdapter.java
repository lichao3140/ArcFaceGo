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

package com.arcsoft.arcfacesingle.bindingadapter;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.databinding.BindingAdapter;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.business.common.CommonRepository;
import com.arcsoft.arcfacesingle.business.recognize.RecognizeRepository;
import com.arcsoft.arcfacesingle.business.setting.ConfigConstants;
import com.arcsoft.arcfacesingle.data.db.table.TableSettingConfigInfo;
import com.arcsoft.arcfacesingle.util.glide.GlideUtils;

public class SettingBindingAdapter {
    @BindingAdapter({"imageUrl"})
    public static void loadTakeResultImage(ImageView imageView, String path) {
        GlideUtils.loadRecognizeHead(path, imageView);
    }

    @BindingAdapter(("imagePersonDetail"))
    public static void loadPersonDetailHead(ImageView imageView, String path) {
        GlideUtils.loadPersonAdapterImage(path, imageView);
    }

    @BindingAdapter({"imageLogoBmp"})
    public static void loadBmp(ImageView imageView, Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
//        GlideUtils.loadImageLogo(bitmap, imageView);
    }

    @BindingAdapter("screenBright")
    public static void bindScreenBright(ViewGroup viewGroup, TableSettingConfigInfo tableSettingConfigInfo) {
        EditText editText = viewGroup.findViewById(R.id.et_screen_default_bright);
        boolean isClose = tableSettingConfigInfo.isScreenBrightFollowSys();
        editText.setEnabled(!isClose);
        if (isClose) {
            editText.setSelection(tableSettingConfigInfo.getScreenDefBrightPercent().length());
        }
        viewGroup.setBackground(viewGroup.getResources().getDrawable(isClose ?
                R.drawable.bg_btn_grey_round_border : R.drawable.bg_btn_white_round_border));
    }

    @BindingAdapter("deviceHour")
    public static void bindDeviceHour(ViewGroup viewGroup, TableSettingConfigInfo tableSettingConfigInfo) {
        EditText editText = viewGroup.findViewById(R.id.et_device_reboot_hour);
        boolean isRebootEveryday = tableSettingConfigInfo.isRebootEveryDay();
        editText.setEnabled(isRebootEveryday);
        if (isRebootEveryday) {
            editText.setSelection(tableSettingConfigInfo.getRebootHour().length());
        }
        viewGroup.setBackground(viewGroup.getResources().getDrawable(isRebootEveryday ?
                R.drawable.bg_btn_white_round_border : R.drawable.bg_btn_grey_round_border));
    }

    @BindingAdapter("deviceMin")
    public static void bindDeviceMin(ViewGroup viewGroup, TableSettingConfigInfo tableSettingConfigInfo) {
        EditText editText = viewGroup.findViewById(R.id.et_device_reboot_minute);
        boolean isRebootEveryday = tableSettingConfigInfo.isRebootEveryDay();
        editText.setEnabled(isRebootEveryday);
        if (isRebootEveryday) {
            editText.setSelection(tableSettingConfigInfo.getRebootMin().length());
        }
        viewGroup.setBackground(viewGroup.getResources().getDrawable(isRebootEveryday ?
                R.drawable.bg_btn_white_round_border : R.drawable.bg_btn_grey_round_border));
    }

    @BindingAdapter("successRetry")
    public static void bindSuccessRetry(ViewGroup viewGroup, boolean isSuccessRetry) {
        EditText editText = viewGroup.findViewById(R.id.et_recognition_success_interval_value);
        editText.setEnabled(isSuccessRetry);
        viewGroup.setBackground(viewGroup.getResources().getDrawable(isSuccessRetry ?
                R.drawable.bg_btn_white_round_border : R.drawable.bg_btn_grey_round_border));
    }

    @BindingAdapter("screenDefBright")
    public static void bindScreenDefBright(EditText view, TableSettingConfigInfo tableSettingConfigInfo) {
        boolean isClose = tableSettingConfigInfo.isScreenBrightFollowSys();
        view.setEnabled(!isClose);
        if (isClose) {
            view.setText("");
        } else {
            view.setSelection(tableSettingConfigInfo.getScreenDefBrightPercent().length());
        }
        view.setBackground(view.getResources().getDrawable(isClose ?
                R.drawable.bg_btn_grey_round_border : R.drawable.bg_btn_white_round_border));
    }

    @BindingAdapter("deviceRebootHour")
    public static void bindDeviceRebootHour(EditText view, TableSettingConfigInfo tableSettingConfigInfo) {
        boolean isRebootEveryday = tableSettingConfigInfo.isRebootEveryDay();
        view.setEnabled(isRebootEveryday);
        if (!isRebootEveryday) {
            view.setText("");
        } else {
            view.setSelection(tableSettingConfigInfo.getRebootHour().length());
        }
        view.setBackground(view.getResources().getDrawable(isRebootEveryday ?
                R.drawable.bg_btn_white_round_border : R.drawable.bg_btn_grey_round_border));
    }

    @BindingAdapter("deviceRebootMin")
    public static void bindDeviceRebootMin(EditText view, TableSettingConfigInfo tableSettingConfigInfo) {
        boolean isRebootEveryday = tableSettingConfigInfo.isRebootEveryDay();
        view.setEnabled(isRebootEveryday);
        if (!isRebootEveryday) {
            view.setText("");
        } else {
            view.setSelection(tableSettingConfigInfo.getRebootMin().length());
        }
        view.setBackground(view.getResources().getDrawable(isRebootEveryday ?
                R.drawable.bg_btn_white_round_border : R.drawable.bg_btn_grey_round_border));
    }

    @BindingAdapter("displayModeShow")
    public static void bindDisplayModeShow(TextView view, int displayMode) {
        view.setText(displayMode == ConfigConstants.DISPLAY_MODE_SUCCESS_NAME ?
                R.string.name : R.string.custom1);
    }

    @BindingAdapter("displayModeFailShow")
    public static void bindDisplayModeFailShow(TextView view, int displayModeFail) {
        int resDisplayModeFail = R.string.default_markup;
        if (displayModeFail == ConfigConstants.DISPLAY_MODE_FAILED_NOT_FEEDBACK) {
            resDisplayModeFail = R.string.not_feedback;
        } else if (displayModeFail == ConfigConstants.DISPLAY_MODE_FAILED_CUSTOM) {
            resDisplayModeFail = R.string.custom1;
        }
        view.setText(resDisplayModeFail);
    }

    @BindingAdapter("voiceModeShow")
    public static void bindVoiceModeShow(TextView view, int voiceMode) {
        int bgRes = R.drawable.bg_btn_grey_round_border;
        if (getSuccessPreviewVoiceFlag(voiceMode)) {
            bgRes = R.drawable.bg_btn_white_round_border;
        }
        view.setEnabled(getSuccessPreviewVoiceFlag(voiceMode));
        view.setBackground(view.getResources().getDrawable(bgRes));
    }

    @BindingAdapter("voiceModeFailShow")
    public static void bindVoiceModeFailShow(TextView view, int voiceModeFail) {
        int bgRes = R.drawable.bg_btn_grey_round_border;
        if (getFailPreviewVoiceFlag(voiceModeFail)) {
            bgRes = R.drawable.bg_btn_white_round_border;
        }
        view.setEnabled(getFailPreviewVoiceFlag(voiceModeFail));
        view.setBackground(view.getResources().getDrawable(bgRes));
    }

    @BindingAdapter("displayMode")
    public static void bindDisplayMode(EditText view, int displayMode) {
        boolean isCustom = displayMode == ConfigConstants.DISPLAY_MODE_SUCCESS_CUSTOM;
        view.setEnabled(isCustom);
//        view.setBackground(view.getResources().getDrawable(isCustom ?
//                R.drawable.bg_btn_white_round_border : R.drawable.bg_btn_grey_round_border));
    }

    @BindingAdapter("displayModeFail")
    public static void bindDisplayModeFail(EditText view, int displayModeFail) {
        boolean isCustom = displayModeFail == ConfigConstants.DISPLAY_MODE_FAILED_CUSTOM;
        view.setEnabled(isCustom);
        if (!isCustom) {
            view.setText("");
        }
//        view.setBackground(view.getResources().getDrawable(isCustom ?
//                R.drawable.bg_btn_white_round_border : R.drawable.bg_btn_grey_round_border));
    }

    @BindingAdapter("voiceMode")
    public static void bindVoiceMode(EditText view, int voiceMode) {
        boolean isCustom = voiceMode == ConfigConstants.SUCCESS_VOICE_MODE_CUSTOM;
        view.setEnabled(isCustom);
        if (!isCustom) {
            view.setText("");
        }
//        view.setBackground(view.getResources().getDrawable(isCustom ?
//                R.drawable.bg_btn_white_round_border : R.drawable.bg_btn_grey_round_border));
    }

    @BindingAdapter("voiceModeFail")
    public static void bindVoiceModeFail(EditText view, int voiceModeFail) {
        boolean isCustom = voiceModeFail == ConfigConstants.FAILED_VOICE_MODE_CUSTOM;
        view.setEnabled(isCustom);
        if (!isCustom) {
            view.setText("");
        }
//        view.setBackground(view.getResources().getDrawable(isCustom ? R.drawable.bg_btn_white_round_border
//                : R.drawable.bg_btn_grey_round_border));
    }

    @BindingAdapter("irPreview")
    public static void bindIrPreview(TextView textView, TableSettingConfigInfo tableSettingConfigInfo) {
        boolean isLiveness = tableSettingConfigInfo.isLivenessDetect();
        boolean isIR = tableSettingConfigInfo.getLiveDetectType() == ConfigConstants.DEFAULT_LIVE_DETECT_IR;
        textView.setVisibility((isLiveness && isIR) ? View.VISIBLE : View.GONE);
    }

    @BindingAdapter("distanceType")
    public static void bindDistanceType(Spinner spinner, TableSettingConfigInfo tableSettingConfigInfo) {
        boolean isLiveness = tableSettingConfigInfo.isLivenessDetect();
        boolean isIR = isLiveness && tableSettingConfigInfo.getLiveDetectType() == ConfigConstants.DEFAULT_LIVE_DETECT_IR;
        spinner.setEnabled(!isIR);
        spinner.setBackground(spinner.getResources().getDrawable(!isIR ?
                R.drawable.spinner_white_border : R.drawable.spinner_disable_border));
    }

    @BindingAdapter("previewVoiceMode")
    public static void bindPreviewVoiceMode(Spinner spinner, int voiceMode) {
        boolean isPreview = getSuccessPreviewVoiceFlag(voiceMode);
        spinner.setEnabled(isPreview);
        spinner.setBackground(spinner.getResources().getDrawable(isPreview ?
                R.drawable.spinner_white_border : R.drawable.spinner_disable_border));
    }

    @BindingAdapter("previewVoiceModeFail")
    public static void bindPreviewVoiceModeFail(Spinner spinner, int voiceMode) {
        boolean isPreview = getFailPreviewVoiceFlag(voiceMode);
        spinner.setEnabled(isPreview);
        spinner.setBackground(spinner.getResources().getDrawable(isPreview ?
                R.drawable.spinner_white_border : R.drawable.spinner_disable_border));
    }

    @BindingAdapter("rbSuccessVoiceChecked")
    public static void bindRbSuccessVoiceChecked(RadioButton view, int voiceMode) {
        view.setChecked(getSuccessPreviewVoiceFlag(voiceMode));
    }

    @BindingAdapter("rbFailVoiceChecked")
    public static void bindRbFailVoiceChecked(RadioButton view, int voiceModeFail) {
        view.setChecked(getFailPreviewVoiceFlag(voiceModeFail));
    }

    @BindingAdapter("appendCursor")
    public static void bindAppendCursor(EditText editText, String value) {
        editText.setText(value);
        if (!TextUtils.isEmpty(value)) {
            editText.setSelection(value.length());
        }
    }

    @BindingAdapter("FaceQuality")
    public static void bindFaceQuality(EditText editText, Boolean value) {
        editText.setEnabled(value);
    }

    private static boolean getSuccessPreviewVoiceFlag(int voiceMode) {
        return voiceMode == ConfigConstants.SUCCESS_VOICE_MODE_NAME ||
                voiceMode == ConfigConstants.SUCCESS_VOICE_MODE_PREVIEW_TYPE3 ||
                voiceMode == ConfigConstants.SUCCESS_VOICE_MODE_PREVIEW_TYPE4 ||
                voiceMode == ConfigConstants.SUCCESS_VOICE_MODE_PREVIEW_TYPE5 ||
                voiceMode == ConfigConstants.SUCCESS_VOICE_MODE_PREVIEW_TYPE6 ||
                voiceMode == ConfigConstants.SUCCESS_VOICE_MODE_NONE;
    }

    private static boolean getFailPreviewVoiceFlag(int voiceModeFail) {
        return voiceModeFail == ConfigConstants.FAILED_VOICE_MODE_WARN ||
                voiceModeFail == ConfigConstants.FAILED_VOICE_MODE_PREVIEW_TYPE3 ||
                voiceModeFail == ConfigConstants.FAILED_VOICE_MODE_PREVIEW_TYPE4 ||
                voiceModeFail == ConfigConstants.FAILED_VOICE_MODE_NONE;
    }

    @BindingAdapter("changeBorder")
    public static void bindChangeBorder(FrameLayout frameLayout, Integer status) {
        if (status.equals(RecognizeRepository.FACE_RESULT_SUCCESS)) {
            frameLayout.setBackground(frameLayout.getResources().getDrawable(R.drawable.border_success_texture));
        } else if (status.equals(RecognizeRepository.FACE_RESULT_FAILED)) {
            TableSettingConfigInfo configInfo = CommonRepository.getInstance().getSettingConfigInfo();
            if (configInfo.getDisplayModeFail() != ConfigConstants.DISPLAY_MODE_FAILED_NOT_FEEDBACK) {
                frameLayout.setBackground(frameLayout.getResources().getDrawable(R.drawable.border_fail_texture));
            }
        } else if (status.equals(RecognizeRepository.FACE_RESULT_UNAUTHORIZED)) {
            frameLayout.setBackground(frameLayout.getResources().getDrawable(R.drawable.border_deny_texture));
        } else {
            frameLayout.setBackground(frameLayout.getResources().getDrawable(R.drawable.border_normal_texture));
        }
    }
}
