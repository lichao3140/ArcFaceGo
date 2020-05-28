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
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Switch;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.ObservableField;

import com.arcsoft.arcfacesingle.BR;
import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.business.common.CommonRepository;
import com.arcsoft.arcfacesingle.business.setting.ConfigConstants;
import com.arcsoft.arcfacesingle.data.db.table.TableSettingConfigInfo;

public class RecognizeSettingViewModel extends BaseObservable {

    private static final float FACE_QUALITY_MAX = 1.0F;

    private TableSettingConfigInfo settingConfigInfo;
    private int cameraNumbers;

    @Bindable
    public final ObservableField<TableSettingConfigInfo> settingInfo = new ObservableField<>();
    public final ObservableField<String> strThreshold = new ObservableField<>();
    public final ObservableField<String> fieldStringIrThreshold = new ObservableField<>();
    public final ObservableField<Boolean> irFaceLiveOpen = new ObservableField<>();
    public final ObservableField<Boolean> irFaceLivePreviewShow = new ObservableField<>();
    public final ObservableField<Boolean> needSuccessRetry = new ObservableField<>();
    public final ObservableField<Integer> distanceType = new ObservableField<>();
    public final ObservableField<String> strFailRetry = new ObservableField<>();
    public final ObservableField<String> strSuccessRetry = new ObservableField<>();
    public final ObservableField<Boolean> rgDetectType = new ObservableField<>();
    public final ObservableField<Boolean> irTypeVisible = new ObservableField<>();
    public final ObservableField<Boolean> fieldFaceQualityOpen = new ObservableField<>();
    public final ObservableField<String> fieldFaceQualityThreshold = new ObservableField<>();

    public RecognizeSettingViewModel(int nCamera) {
        cameraNumbers = nCamera;
        settingConfigInfo = new TableSettingConfigInfo();
        settingInfo.set(settingConfigInfo);
        onVisible();
    }

    @SuppressLint("MissingPermission")
    private void init() {
        if (settingConfigInfo.isLivenessDetect()) {
            //初始化活体检测
            if (settingConfigInfo.getLiveDetectType() == ConfigConstants.DEFAULT_LIVE_DETECT_CLOSE) {
                settingConfigInfo.setLiveDetectType(cameraNumbers > 1 ? ConfigConstants.DEFAULT_LIVE_DETECT_IR : ConfigConstants.DEFAULT_LIVE_DETECT_RGB);
            } else if (settingConfigInfo.getLiveDetectType() == ConfigConstants.DEFAULT_LIVE_DETECT_IR) {
                if (cameraNumbers <= 1) {
                    settingConfigInfo.setLiveDetectType(ConfigConstants.DEFAULT_LIVE_DETECT_RGB);
                }
            }
        }
        notifySettingInfo();
    }

    public void onThresholdTextChanged(Editable editable) {
        String strContent = editable.toString().trim();
        if (!TextUtils.isEmpty(strContent)) {
            if (".".equals(strContent)) {
                strContent = "0.";
                editable.insert(0, strContent);
            }
            float threshold = Float.parseFloat(strContent);
            if (threshold > ConfigConstants.THRESHOLD_MAX) {
                threshold = ConfigConstants.THRESHOLD_MAX;
                strThreshold.set(String.valueOf(threshold));
            }
            settingInfo.get().setSimilarThreshold(String.valueOf(threshold));
        } else {
            settingInfo.get().setSimilarThreshold(strContent);
        }
        notifySettingInfo();
    }

    public void onRecognitionIntervalTextChanged(Editable editable) {
        String strContent = editable.toString().trim();
        if (!TextUtils.isEmpty(strContent)) {
            if (".".equals(strContent)) {
                strContent = "1.";
                editable.insert(0, strContent);
            }
            String strAdjust = adjustString(strContent);
            if (strContent.compareTo(strAdjust) != 0) {
                strFailRetry.set(strAdjust);
                strContent = strAdjust;
            }
            float maxInterval = Float.parseFloat(strContent);
            if (maxInterval > ConfigConstants.RETRY_DELAY_MAX) {
                maxInterval = ConfigConstants.RETRY_DELAY_MAX;
                strFailRetry.set(String.valueOf(maxInterval));
            } else if (maxInterval < ConfigConstants.RETRY_DELAY_MIN) {
                maxInterval = ConfigConstants.RETRY_DELAY_MIN;
                strFailRetry.set(String.valueOf(maxInterval));
            }
            settingInfo.get().setRecognitionRetryDelay(String.valueOf(maxInterval));
        } else {
            settingInfo.get().setRecognitionRetryDelay(strContent);
        }
        notifySettingInfo();
    }

    public void onRecognitionSuccessIntervalTextChanged(Editable editable) {
        String strContent = editable.toString().trim();
        if (!TextUtils.isEmpty(strContent)) {
            if (".".equals(strContent)) {
                strContent = "1.";
                editable.insert(0, strContent);
            }
            String strAdjust = adjustString(strContent);
            if (strContent.compareTo(strAdjust) != 0) {
                strSuccessRetry.set(strAdjust);
                strContent = strAdjust;
            }
            float maxInterval = Float.parseFloat(strContent);
            if (maxInterval > ConfigConstants.RETRY_DELAY_MAX) {
                maxInterval = ConfigConstants.RETRY_DELAY_MAX;
                strSuccessRetry.set(String.valueOf(maxInterval));
            } else if (maxInterval < ConfigConstants.RETRY_DELAY_MIN) {
                maxInterval = ConfigConstants.RETRY_DELAY_MIN;
                strSuccessRetry.set(String.valueOf(maxInterval));
            }
            settingInfo.get().setSuccessRetryDelay(String.valueOf(maxInterval));
        } else {
            settingInfo.get().setSuccessRetryDelay(strContent);
        }
    }

    public void onIrThresholdTextChanged(Editable editable) {
        String strContent = editable.toString().trim();
        if (!TextUtils.isEmpty(strContent)) {
            if (".".equals(strContent)) {
                strContent = "0.";
                editable.insert(0, strContent);
            }
            float threshold = Float.parseFloat(strContent);
            if (threshold > ConfigConstants.THRESHOLD_MAX) {
                threshold = ConfigConstants.THRESHOLD_MAX;
                fieldStringIrThreshold.set(String.valueOf(threshold));
            }
            settingInfo.get().setIrLiveThreshold(String.valueOf(threshold));
        } else {
            settingInfo.get().setIrLiveThreshold(strContent);
        }
        notifySettingInfo();
    }

    public void onFaceQualityTextChanged(Editable editable) {
        String strContent = editable.toString().trim();
        if (!TextUtils.isEmpty(strContent)) {
            if (".".equals(strContent)) {
                strContent = "0.";
                editable.insert(0, strContent);
            }
            try {
                float value = Float.parseFloat(strContent);
                if (value > FACE_QUALITY_MAX) {
                    editable.clear();
                    editable.append("1.0");
                    String strContent2 = editable.toString().trim();
                    settingConfigInfo.setFaceQualityThreshold(strContent2);
                } else {
                    settingConfigInfo.setFaceQualityThreshold(strContent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            settingConfigInfo.setFaceQualityThreshold(strContent);
        }
    }

    public void onIrSwitchLiveCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == R.id.rb_switch_ir_live_ir) {
            settingConfigInfo.setLiveDetectType(ConfigConstants.DEFAULT_LIVE_DETECT_IR);
        } else {
            settingConfigInfo.setLiveDetectType(ConfigConstants.DEFAULT_LIVE_DETECT_RGB);
        }
        resetIrThreshold();
        notifySettingInfo();
    }

    public void onRecognitionDistanceChanged(int distanceType1, boolean notify) {
        if (notify) {
            distanceType.set(distanceType1 - ConfigConstants.RECOGNITION_DISTANCE_TYPE1);
        }
        setRecognitionDistance(distanceType1);
    }

    private void resetIrThreshold() {
        if (settingInfo.get().getLiveDetectType() != ConfigConstants.DEFAULT_LIVE_DETECT_IR) {
            if (TextUtils.isEmpty(settingInfo.get().getIrLiveThreshold())) {
                settingInfo.get().setIrLiveThreshold(ConfigConstants.DEFAULT_IR_LIVE_THRESHOLD);
            }
        }
        fieldStringIrThreshold.set(settingInfo.get().getIrLiveThreshold());
    }

    public void onSwitchClick(View view) {
        Switch switcher = (Switch) view;
        boolean value = switcher.isChecked();

        if (view.getId() == R.id.sh_switch_live) {
            settingInfo.get().setLivenessDetect(value);
            if (value) {
                if (cameraNumbers > 1) {
                    settingInfo.get().setLiveDetectType(ConfigConstants.DEFAULT_LIVE_DETECT_IR);
                    settingInfo.get().setIrLivePreview(ConfigConstants.DEFAULT_IR_LIVE_PREVIEW);
                    irFaceLivePreviewShow.set(true);
                    rgDetectType.set(true);
                } else {
                    settingInfo.get().setLiveDetectType(ConfigConstants.DEFAULT_LIVE_DETECT_RGB);
                    settingInfo.get().setIrLivePreview(ConfigConstants.DEFAULT_IR_LIVE_PREVIEW_HIDE);
                    irFaceLivePreviewShow.set(false);
                    rgDetectType.set(false);
                }
            } else {
                settingInfo.get().setLiveDetectType(ConfigConstants.DEFAULT_LIVE_DETECT_CLOSE);
            }
            resetIrThreshold();

            irFaceLiveOpen.set(value);
            if (cameraNumbers > 1) {
                irTypeVisible.set(value);
            }
            notifySettingInfo();
        } else if (view.getId() == R.id.sh_switch_ir_live_preview) {
            settingConfigInfo.setIrLivePreview(value ? ConfigConstants.DEFAULT_IR_LIVE_PREVIEW : 0);
        } else if (view.getId() == R.id.sh_recognition_success_interval_key) {
            settingConfigInfo.setSuccessRetry(value ? ConfigConstants.DEFAULT_RECOGNITION_SUCCESS_RETRY + 1 : ConfigConstants.DEFAULT_RECOGNITION_SUCCESS_RETRY);
            if (TextUtils.isEmpty(settingConfigInfo.getSuccessRetryDelay())) {
                settingConfigInfo.setSuccessRetryDelay(ConfigConstants.DEFAULT_RETRY_DELAY);
            }
            strSuccessRetry.set(settingConfigInfo.getSuccessRetryDelay());
        } else if (view.getId() == R.id.switch_face_quality_open) {
            settingConfigInfo.setFaceQuality(value);
            if (TextUtils.isEmpty(settingConfigInfo.getFaceQualityThreshold())) {
                settingConfigInfo.setFaceQualityThreshold(ConfigConstants.DEFAULT_FACE_QUALITY_THRESHOLD);
            }
            fieldFaceQualityThreshold.set(settingConfigInfo.getFaceQualityThreshold());
        }
    }

    private void notifySettingInfo() {
        notifyPropertyChanged(BR.settingInfo);
    }

    public void selectIrFaceLiveOpen(boolean open) {
        if (!open && irFaceLiveOpen.get()) {
            irFaceLiveOpen.set(false);
        }
        irFaceLivePreviewShow.set(open);
        settingInfo.get().setLivenessDetect(open);
        settingInfo.get().setIrLivePreview(open ? ConfigConstants.DEFAULT_IR_LIVE_PREVIEW : 0);
        notifySettingInfo();
    }

    private void setRecognitionDistance(int type) {
        TableSettingConfigInfo configInfo = settingInfo.get();
        if (configInfo != null) {
            configInfo.setRecognizeDistance(type);
            notifySettingInfo();
        }
    }

    private String adjustString(String value) {
        String[] strArr = value.split("\\.");
        if (strArr.length > 1) {
            if (strArr[1].length() > 1) {
                return strArr[0] + "." + strArr[1].substring(0, 1);
            }
        }
        return value;
    }

    public void onResume() {
    }

    public void onDestroy() {
        settingConfigInfo = null;
    }

    public void onVisible() {
        reloadSettingInfo(CommonRepository.getInstance().getSettingConfigInfo(), settingConfigInfo);
        init();
        if (settingConfigInfo.isLivenessDetect() && settingConfigInfo.getLiveDetectType() == ConfigConstants.DEFAULT_LIVE_DETECT_IR) {
            rgDetectType.set(true);
        } else {
            rgDetectType.set(false);
        }
        strThreshold.set(settingConfigInfo.getSimilarThreshold());
        strFailRetry.set(settingConfigInfo.getRecognitionRetryDelay());
        strSuccessRetry.set(settingConfigInfo.getSuccessRetryDelay());
        irFaceLiveOpen.set(settingConfigInfo.isLivenessDetect());
        irTypeVisible.set(cameraNumbers > 1 ? settingConfigInfo.isLivenessDetect() : false);
        irFaceLivePreviewShow.set(settingConfigInfo.getIrLivePreview() == ConfigConstants.DEFAULT_IR_LIVE_PREVIEW);
        needSuccessRetry.set(settingConfigInfo.getSuccessRetry() != ConfigConstants.DEFAULT_RECOGNITION_SUCCESS_RETRY);
        distanceType.set(settingConfigInfo.getRecognizeDistance() - ConfigConstants.RECOGNITION_DISTANCE_TYPE1);
        fieldStringIrThreshold.set(settingConfigInfo.getIrLiveThreshold());
        fieldFaceQualityOpen.set(settingConfigInfo.isFaceQuality());
        fieldFaceQualityThreshold.set(settingConfigInfo.getFaceQualityThreshold());
    }

    public void reloadSettingInfo(TableSettingConfigInfo srcInfo, TableSettingConfigInfo desInfo) {
        desInfo.setSimilarThreshold(srcInfo.getSimilarThreshold());
        desInfo.setRecognitionRetryDelay(srcInfo.getRecognitionRetryDelay());
        desInfo.setSuccessRetry(srcInfo.getSuccessRetry());
        desInfo.setSuccessRetryDelay(srcInfo.getSuccessRetryDelay());
        desInfo.setLivenessDetect(srcInfo.isLivenessDetect());
        desInfo.setLiveDetectType(srcInfo.getLiveDetectType());
        desInfo.setIrLivePreview(srcInfo.getIrLivePreview());
        desInfo.setIrLiveThreshold(srcInfo.getIrLiveThreshold());
        desInfo.setRecognizeDistance(srcInfo.getRecognizeDistance());
        desInfo.setFaceQuality(srcInfo.isFaceQuality());
        desInfo.setFaceQualityThreshold(srcInfo.getFaceQualityThreshold());
    }

    public void getConfig(TableSettingConfigInfo configInfo) {
        reloadSettingInfo(settingConfigInfo, configInfo);
    }

    public interface RecognitionSettingListener {
        /**
         * IR检测距离提示
         */
        void showSelectIrLiveTipDialog();
    }
}
