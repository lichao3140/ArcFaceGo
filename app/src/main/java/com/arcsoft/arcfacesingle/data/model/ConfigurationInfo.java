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

package com.arcsoft.arcfacesingle.data.model;

import androidx.annotation.NonNull;

import com.arcsoft.arcfacesingle.business.setting.ConfigConstants;
import com.arcsoft.arcfacesingle.data.db.table.TableSettingConfigInfo;

import java.io.Serializable;

public class ConfigurationInfo implements Serializable {

    private String companyName;
    private String mainLogoPath;
    private String subLogoPath;
    private Integer voiceMode;
    private Integer displayMode;
    private Integer displayModeFail;
    private Integer voiceModeFail;
    private String customDisplayString;
    private String customVoiceString;
    private String customDisplayStringFail;
    private String customVoiceStringFail;

    private Integer devicePort;
    private Boolean sleepFollowSystem;
    private Boolean screenBrightFollowSystem;
    private Integer screenBrightPercent;
    private Boolean screenSaver;
    private Boolean rebootDaily;
    private Integer rebootHour;
    private Integer rebootMinute;
    private Float closeDoorDelay;
    private Integer uploadRecordImage;

    private Float similarThreshold;
    private Float recognitionRetryDelay;
    private Integer successRetry;
    private Float successRetryDelay;
    private Integer liveDetectType;
    private Integer irLivePreview;
    private Float liveThreshold;
    private Integer recognizeDistance;

    private Boolean faceQuality;
    private Float faceQualityThreshold;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getMainLogoPath() {
        return mainLogoPath;
    }

    public void setMainLogoPath(String mainLogoPath) {
        this.mainLogoPath = mainLogoPath;
    }

    public String getSubLogoPath() {
        return subLogoPath;
    }

    public void setSubLogoPath(String subLogoPath) {
        this.subLogoPath = subLogoPath;
    }

    public Integer getVoiceMode() {
        return voiceMode;
    }

    public void setVoiceMode(Integer voiceMode) {
        this.voiceMode = voiceMode;
    }

    public Integer getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(Integer displayMode) {
        this.displayMode = displayMode;
    }

    public Integer getDisplayModeFail() {
        return displayModeFail;
    }

    public void setDisplayModeFail(Integer displayModeFail) {
        this.displayModeFail = displayModeFail;
    }

    public Integer getVoiceModeFail() {
        return voiceModeFail;
    }

    public void setVoiceModeFail(Integer voiceModeFail) {
        this.voiceModeFail = voiceModeFail;
    }

    public String getCustomDisplayString() {
        return customDisplayString;
    }

    public void setCustomDisplayString(String customDisplayString) {
        this.customDisplayString = customDisplayString;
    }

    public String getCustomVoiceString() {
        return customVoiceString;
    }

    public void setCustomVoiceString(String customVoiceString) {
        this.customVoiceString = customVoiceString;
    }

    public String getCustomDisplayStringFail() {
        return customDisplayStringFail;
    }

    public void setCustomDisplayStringFail(String customDisplayStringFail) {
        this.customDisplayStringFail = customDisplayStringFail;
    }

    public String getCustomVoiceStringFail() {
        return customVoiceStringFail;
    }

    public void setCustomVoiceStringFail(String customVoiceStringFail) {
        this.customVoiceStringFail = customVoiceStringFail;
    }

    public Integer getDevicePort() {
        return devicePort;
    }

    public void setDevicePort(Integer devicePort) {
        this.devicePort = devicePort;
    }

    public Boolean getSleepFollowSystem() {
        return sleepFollowSystem;
    }

    public void setSleepFollowSystem(Boolean sleepFollowSystem) {
        this.sleepFollowSystem = sleepFollowSystem;
    }

    public Boolean getScreenBrightFollowSystem() {
        return screenBrightFollowSystem;
    }

    public void setScreenBrightFollowSystem(Boolean screenBrightFollowSystem) {
        this.screenBrightFollowSystem = screenBrightFollowSystem;
    }

    public Integer getScreenBrightPercent() {
        return screenBrightPercent;
    }

    public void setScreenBrightPercent(Integer screenBrightPercent) {
        this.screenBrightPercent = screenBrightPercent;
    }

    public Boolean getScreenSaver() {
        return screenSaver;
    }

    public void setScreenSaver(Boolean screenSaver) {
        this.screenSaver = screenSaver;
    }

    public Boolean getRebootDaily() {
        return rebootDaily;
    }

    public void setRebootDaily(Boolean rebootDaily) {
        this.rebootDaily = rebootDaily;
    }

    public Integer getRebootHour() {
        return rebootHour;
    }

    public void setRebootHour(Integer rebootHour) {
        this.rebootHour = rebootHour;
    }

    public Integer getRebootMinute() {
        return rebootMinute;
    }

    public void setRebootMinute(Integer rebootMinute) {
        this.rebootMinute = rebootMinute;
    }

    public Float getCloseDoorDeylay() {
        return closeDoorDelay;
    }

    public void setCloseDoorDeylay(Float closeDoorDelay) {
        this.closeDoorDelay = closeDoorDelay;
    }

    public Integer getUploadRecordImage() {
        return uploadRecordImage;
    }

    public void setUploadRecordImage(Integer uploadRecordImage) {
        this.uploadRecordImage = uploadRecordImage;
    }

    public Float getSimilarThreshold() {
        return similarThreshold;
    }

    public void setSimilarThreshold(Float similarThreshold) {
        this.similarThreshold = similarThreshold;
    }

    public Float getRecognitionRetryDelay() {
        return recognitionRetryDelay;
    }

    public void setRecognitionRetryDelay(Float recognitionRetryDelay) {
        this.recognitionRetryDelay = recognitionRetryDelay;
    }

    public Integer getSuccessRetry() {
        return successRetry;
    }

    public void setSuccessRetry(Integer successRetry) {
        this.successRetry = successRetry;
    }

    public Float getSuccessRetryDelay() {
        return successRetryDelay;
    }

    public void setSuccessRetryDelay(Float successRetryDelay) {
        this.successRetryDelay = successRetryDelay;
    }

    public Integer getLiveDetectType() {
        return liveDetectType;
    }

    public void setLiveDetectType(Integer liveDetectType) {
        this.liveDetectType = liveDetectType;
    }

    public Integer getIrLivePreview() {
        return irLivePreview;
    }

    public void setIrLivePreview(Integer irLivePreview) {
        this.irLivePreview = irLivePreview;
    }

    public Float getLiveThreshold() {
        return liveThreshold;
    }

    public void setLiveThreshold(Float liveThreshold) {
        this.liveThreshold = liveThreshold;
    }

    public Integer getRecognizeDistance() {
        return recognizeDistance;
    }

    public void setRecognizeDistance(Integer recognizeDistance) {
        this.recognizeDistance = recognizeDistance;
    }

    public Boolean getFaceQuality() {
        return faceQuality;
    }

    public void setFaceQuality(Boolean faceQuality) {
        this.faceQuality = faceQuality;
    }

    public Float getFaceQualityThreshold() {
        return faceQualityThreshold;
    }

    public void setFaceQualityThreshold(Float faceQualityThreshold) {
        this.faceQualityThreshold = faceQualityThreshold;
    }

    public void loadFromDatabase(@NonNull TableSettingConfigInfo srcInfo) {
        this.companyName = srcInfo.getCompanyName();
        this.mainLogoPath = srcInfo.getMainImagePath();
        this.subLogoPath = srcInfo.getViceImagePath();
        this.voiceMode = srcInfo.getVoiceMode();
        this.displayMode = srcInfo.getDisplayMode();
        this.displayModeFail = srcInfo.getDisplayModeFail();
        this.voiceModeFail = srcInfo.getVoiceModeFail();
        this.customDisplayString = srcInfo.getCustomDisplayModeFormat();
        this.customVoiceString = srcInfo.getCustomVoiceModeFormat();
        this.customDisplayStringFail = srcInfo.getCustomFailDisplayModeFormat();
        this.customVoiceStringFail = srcInfo.getCustomFailVoiceModeFormat();

        this.devicePort = Integer.valueOf(srcInfo.getDevicePort());
        this.sleepFollowSystem = srcInfo.isDeviceSleepFollowSys();
        this.screenBrightFollowSystem = srcInfo.isScreenBrightFollowSys();
        this.screenBrightPercent = Integer.valueOf(srcInfo.getScreenDefBrightPercent());
        this.screenSaver = srcInfo.isIndexScreenDefShow();
        this.rebootDaily = srcInfo.isRebootEveryDay();
        this.rebootHour = Integer.valueOf(srcInfo.getRebootHour());
        this.rebootMinute = Integer.valueOf(srcInfo.getRebootMin());
        this.closeDoorDelay = Float.valueOf(srcInfo.getCloseDoorDelay());
        this.uploadRecordImage = srcInfo.getUploadRecordImage();

        this.similarThreshold = Float.valueOf(srcInfo.getSimilarThreshold());
        this.recognitionRetryDelay = Float.valueOf(srcInfo.getRecognitionRetryDelay());
        this.successRetry = srcInfo.getSuccessRetry();
        this.successRetryDelay = Float.valueOf(srcInfo.getSuccessRetryDelay());
        this.liveDetectType = srcInfo.getLiveDetectType();
        this.irLivePreview = srcInfo.getIrLivePreview();
        this.liveThreshold = Float.valueOf(srcInfo.getIrLiveThreshold());
        this.recognizeDistance = srcInfo.getRecognizeDistance();

        this.faceQuality = srcInfo.isFaceQuality();
        this.faceQualityThreshold = Float.valueOf(srcInfo.getFaceQualityThreshold());
    }

    public void saveToDatabase(@NonNull TableSettingConfigInfo desInfo) {
        if (this.companyName != null) {
            desInfo.setCompanyName(this.companyName);
        }

        if (this.mainLogoPath != null) {
            if (this.mainLogoPath.length() > 0) {
                desInfo.setMainImagePath(ConfigConstants.DEFAULT_MAIN_LOGO_FILE_PATH);
            } else {
                desInfo.setMainImagePath("");
            }
        }

        if (this.subLogoPath != null) {
            if (this.subLogoPath.length() > 0) {
                desInfo.setViceImagePath(ConfigConstants.DEFAULT_SECOND_LOGO_FILE_PATH);
            } else {
                desInfo.setViceImagePath("");
            }
        }

        if (this.voiceMode != null) {
            if (this.voiceMode >= ConfigConstants.SUCCESS_VOICE_MODE_NONE && this.voiceMode <= ConfigConstants.SUCCESS_VOICE_MODE_PREVIEW_TYPE6) {
                desInfo.setVoiceMode(this.voiceMode);
            } else if (this.voiceMode == ConfigConstants.SUCCESS_VOICE_MODE_CUSTOM) {
                desInfo.setVoiceMode(this.voiceMode);
            } else {
                desInfo.setVoiceMode(ConfigConstants.SUCCESS_VOICE_MODE_NONE);
            }
        }

        if (this.displayMode != null) {
            if (this.displayMode != ConfigConstants.DISPLAY_MODE_SUCCESS_NAME
                    && this.displayMode != ConfigConstants.DISPLAY_MODE_HIDE_LAST_CHAR
                    && this.displayMode != ConfigConstants.DISPLAY_MODE_SUCCESS_CUSTOM) {
                desInfo.setDisplayMode(ConfigConstants.DISPLAY_MODE_SUCCESS_NAME);
            } else {
                desInfo.setDisplayMode(this.displayMode);
            }
        }

        if (this.displayModeFail != null) {
            if (this.displayModeFail != ConfigConstants.DISPLAY_MODE_FAILED_DEFAULT_MARKUP
                    && this.displayModeFail != ConfigConstants.DISPLAY_MODE_FAILED_NOT_FEEDBACK
                    && this.displayModeFail != ConfigConstants.DISPLAY_MODE_FAILED_CUSTOM) {
                desInfo.setDisplayModeFail(ConfigConstants.DISPLAY_MODE_FAILED_DEFAULT_MARKUP);
            } else {
                desInfo.setDisplayModeFail(this.displayModeFail);
            }
        }

        if (this.voiceModeFail != null) {
            if (this.voiceModeFail >= ConfigConstants.FAILED_VOICE_MODE_NONE && this.voiceModeFail <= ConfigConstants.FAILED_VOICE_MODE_PREVIEW_TYPE4) {
                desInfo.setVoiceModeFail(this.voiceModeFail);
            } else if (this.voiceModeFail == ConfigConstants.FAILED_VOICE_MODE_CUSTOM) {
                desInfo.setVoiceModeFail(this.voiceModeFail);
            } else {
                desInfo.setVoiceModeFail(ConfigConstants.FAILED_VOICE_MODE_NONE);
            }
        }

        if (this.customDisplayString != null) {
            desInfo.setCustomDisplayModeFormat(this.customDisplayString);
        }

        if (this.customVoiceString != null) {
            desInfo.setCustomVoiceModeFormat(this.customVoiceString);
        }

        if (this.customDisplayStringFail != null) {
            desInfo.setCustomFailDisplayModeFormat(this.customDisplayStringFail);
        }

        if (this.customVoiceStringFail != null) {
            desInfo.setCustomFailVoiceModeFormat(this.customVoiceStringFail);
        }

        if (this.devicePort != null) {
            if (this.devicePort >= ConfigConstants.DEVICE_PORT_MIN && this.devicePort <= ConfigConstants.DEVICE_PORT_MAX) {
                desInfo.setDevicePort(this.devicePort.toString());
            } else {
                desInfo.setDevicePort(ConfigConstants.DEFAULT_DEVICE_PORT);
            }
        }

        if (this.sleepFollowSystem != null) {
            desInfo.setDeviceSleepFollowSys(this.sleepFollowSystem);
        }

        if (this.screenBrightFollowSystem != null) {
            desInfo.setScreenBrightFollowSys(this.screenBrightFollowSystem);
        }

        if (this.screenBrightPercent != null) {
            desInfo.setScreenDefBrightPercent(this.screenBrightPercent.toString());
        }

        if (this.screenSaver != null) {
            desInfo.setIndexScreenDefShow(this.screenSaver);
        }

        if (this.rebootDaily != null) {
            desInfo.setRebootEveryDay(this.rebootDaily);
        }

        if (this.rebootHour != null) {
            if (this.rebootHour <= 0) {
                desInfo.setRebootHour("0");
            } else if (this.rebootHour > ConfigConstants.DEVICE_REBOOT_HOUR) {
                desInfo.setRebootHour("23");
            } else {
                desInfo.setRebootHour(this.rebootHour.toString());
            }
        }

        if (this.rebootMinute != null) {
            if (this.rebootMinute <= 0) {
                desInfo.setRebootMin("0");
            } else if (this.rebootMinute > ConfigConstants.DEVICE_REBOOT_MIN) {
                desInfo.setRebootMin("59");
            } else {
                desInfo.setRebootMin(this.rebootMinute.toString());
            }
        }

        if (this.closeDoorDelay != null) {
            if (this.closeDoorDelay <= 0) {
                desInfo.setCloseDoorDelay("0");
            } else if (this.closeDoorDelay > ConfigConstants.TIME_DELAY_MAX) {
                desInfo.setCloseDoorDelay("100");
            } else {
                float result = (int) (this.closeDoorDelay * 10) * 1.0f / 10;
                desInfo.setCloseDoorDelay(Float.toString(result));
            }
        }

        if (this.uploadRecordImage != null) {
            desInfo.setUploadRecordImage(this.uploadRecordImage);
        }

        if (this.similarThreshold != null) {
            if (this.similarThreshold <= 0) {
                desInfo.setSimilarThreshold("0");
            } else if (this.similarThreshold > 1) {
                desInfo.setSimilarThreshold("1");
            } else {
                float result = (int) (this.similarThreshold * 100) * 1.0f / 100;
                desInfo.setSimilarThreshold(Float.toString(result));
            }
        }

        if (this.recognitionRetryDelay != null) {
            if (this.recognitionRetryDelay <= ConfigConstants.RETRY_DELAY_MIN) {
                desInfo.setRecognitionRetryDelay("1.0");
            } else if (this.recognitionRetryDelay > ConfigConstants.RETRY_DELAY_MAX) {
                desInfo.setRecognitionRetryDelay("10.0");
            } else {
                float result = (int) (this.recognitionRetryDelay * 10) * 1.0f / 10;
                desInfo.setRecognitionRetryDelay(Float.toString(result));
            }
        }

        if (this.successRetry != null) {
            desInfo.setSuccessRetry(this.successRetry);
        }

        if (this.successRetryDelay != null) {
            if (this.successRetryDelay <= ConfigConstants.RETRY_DELAY_MIN) {
                desInfo.setSuccessRetryDelay("1.0");
            } else if (this.successRetryDelay > ConfigConstants.RETRY_DELAY_MAX) {
                desInfo.setSuccessRetryDelay("10.0");
            } else {
                float result = (int) (this.successRetryDelay * 10) * 1.0f / 10;
                desInfo.setSuccessRetryDelay(Float.toString(result));
            }
        }

        if (this.liveDetectType != null) {
            desInfo.setLiveDetectType(this.liveDetectType);
            desInfo.setLivenessDetect(this.liveDetectType != ConfigConstants.DEFAULT_LIVE_DETECT_CLOSE);
        }

        if (this.irLivePreview != null) {
            desInfo.setIrLivePreview(this.irLivePreview);
        }

        if (this.liveThreshold != null) {
            if (this.liveThreshold <= 0) {
                desInfo.setIrLiveThreshold("0");
            } else if (this.liveThreshold > 1) {
                desInfo.setIrLiveThreshold("1");
            } else {
                float result = (int) (this.liveThreshold * 100) * 1.0f / 100;
                desInfo.setIrLiveThreshold(Float.toString(result));
            }
        }

        if (this.recognizeDistance != null) {
            desInfo.setRecognizeDistance(this.recognizeDistance);
        }

        if (this.faceQuality != null) {
            desInfo.setFaceQuality(this.faceQuality);
        }

        if (this.faceQualityThreshold != null) {
            if (this.faceQualityThreshold <= 0) {
                desInfo.setFaceQualityThreshold("0");
            } else if (this.faceQualityThreshold > 1) {
                desInfo.setFaceQualityThreshold("1");
            } else {
                float result = (int) (this.faceQualityThreshold * 100) * 1.0f / 100;
                desInfo.setFaceQualityThreshold(Float.toString(result));
            }
        }
    }

}
