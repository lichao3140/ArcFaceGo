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

package com.arcsoft.arcfacesingle.server.pojo.request;

import java.io.Serializable;

public class RequestSetting implements Serializable {

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 设备密码
     */
    private String devicePassword;
    /**
     * 设备名称
     */
    private String deviceName;
    /**
     * 识别阈值
     */
    private String threshold;

    /**
     * 成功语音模式
     */
    private String voiceMode;
    /**
     * 成功语音自定义内容
     */
    private String voiceCustom;
    /**
     * 成功显示模式
     */
    private String displayMode;
    /**
     * 成功显示自定义内容
     */
    private String displayCustom;
    /**
     * 失败显示模式
     */
    private String strangerMode;

    /**
     * 失败显示模式
     */
    private String displayModeFail;

    /**
     * 失败显示自定义内容
     */
    private String strangerCustom;

    /**
     * 失败语音模式
     */
    private String strangerVoiceMode;

    /**
     * 失败语音模式
     */
    private String voiceModeFail;

    /**
     * 失败语音自定义内容
     */
    private String strangerVoiceCustom;
    /**
     * 最大人脸数
     */
    private Integer maxFaceSize;
    /**
     * 活体检测类型
     */
    private Integer livenessType;

    /**
     * 识别距离
     */
    private Integer signDistance;
    /**
     * 识别距离
     */
    private Integer recognizeDistance;
    /**
     * 关门延时
     */
    private String openDelay;

    /**
     * 失败重试间隔
     */
    private String interval;

    /**
     * 识别成功重试间隔，单位秒
     */
    private String successRetryDelay;
    /**
     * 识别成功重试开关：默认关闭
     */
    private Integer successRetry;
    /**
     * 上传刷脸记录图片
     */
    private Integer uploadRecordImage;
    /**
     * IR活体预览显示
     */
    private Integer irLivePreview;
    /**
     * 设备每日重启
     */
    private Integer rebootEveryDay;
    /**
     * 重启小时
     */
    private String rebootHour;
    /**
     * 重启分钟
     */
    private String rebootMin;

    /**
     * 主logo url地址
     */
    private String mainLogoUrl;

    /**
     * 副logo url地址
     */
    private String secondLogoUrl;

    /**
     * 1 签到；2 签退；3 签到、签退
     */
    private Integer signType;

    /**
     * 人脸质量开关：1 开启；0 关闭；
     */
    private Integer faceQuality;

    /**
     * 人脸质量阈值
     */
    private String faceQualityThreshold;

    public String getDisplayModeFail() {
        return displayModeFail;
    }

    public void setDisplayModeFail(String displayModeFail) {
        this.displayModeFail = displayModeFail;
    }

    public String getVoiceModeFail() {
        return voiceModeFail;
    }

    public void setVoiceModeFail(String voiceModeFail) {
        this.voiceModeFail = voiceModeFail;
    }

    public Integer getRecognizeDistance() {
        return recognizeDistance;
    }

    public void setRecognizeDistance(Integer recognizeDistance) {
        this.recognizeDistance = recognizeDistance;
    }

    public Integer getSignType() {
        return signType;
    }

    public void setSignType(Integer signType) {
        this.signType = signType;
    }

    public String getMainLogoUrl() {
        return mainLogoUrl;
    }

    public void setMainLogoUrl(String mainLogoUrl) {
        this.mainLogoUrl = mainLogoUrl;
    }

    public String getSecondLogoUrl() {
        return secondLogoUrl;
    }

    public void setSecondLogoUrl(String secondLogoUrl) {
        this.secondLogoUrl = secondLogoUrl;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getDevicePassword() {
        return devicePassword;
    }

    public void setDevicePassword(String devicePassword) {
        this.devicePassword = devicePassword;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getThreshold() {
        return threshold;
    }

    public void setThreshold(String threshold) {
        this.threshold = threshold;
    }

    public String getOpenDelay() {
        return openDelay;
    }

    public void setOpenDelay(String openDelay) {
        this.openDelay = openDelay;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getVoiceMode() {
        return voiceMode;
    }

    public void setVoiceMode(String voiceMode) {
        this.voiceMode = voiceMode;
    }

    public String getVoiceCustom() {
        return voiceCustom;
    }

    public void setVoiceCustom(String voiceCustom) {
        this.voiceCustom = voiceCustom;
    }

    public String getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(String displayMode) {
        this.displayMode = displayMode;
    }

    public String getDisplayCustom() {
        return displayCustom;
    }

    public void setDisplayCustom(String displayCustom) {
        this.displayCustom = displayCustom;
    }

    public String getStrangerMode() {
        return strangerMode;
    }

    public void setStrangerMode(String strangerMode) {
        this.strangerMode = strangerMode;
    }

    public String getStrangerCustom() {
        return strangerCustom;
    }

    public void setStrangerCustom(String strangerCustom) {
        this.strangerCustom = strangerCustom;
    }

    public String getStrangerVoiceMode() {
        return strangerVoiceMode;
    }

    public void setStrangerVoiceMode(String strangerVoiceMode) {
        this.strangerVoiceMode = strangerVoiceMode;
    }

    public String getStrangerVoiceCustom() {
        return strangerVoiceCustom;
    }

    public void setStrangerVoiceCustom(String strangerVoiceCustom) {
        this.strangerVoiceCustom = strangerVoiceCustom;
    }

    public Integer getMaxFaceSize() {
        return maxFaceSize;
    }

    public void setMaxFaceSize(Integer maxFaceSize) {
        this.maxFaceSize = maxFaceSize;
    }

    public Integer getLivenessType() {
        return livenessType;
    }

    public void setLivenessType(Integer livenessType) {
        this.livenessType = livenessType;
    }

    public Integer getSignDistance() {
        return signDistance;
    }

    public void setSignDistance(Integer signDistance) {
        this.signDistance = signDistance;
    }

    public String getSuccessRetryDelay() {
        return successRetryDelay;
    }

    public void setSuccessRetryDelay(String successRetryDelay) {
        this.successRetryDelay = successRetryDelay;
    }

    public Integer getSuccessRetry() {
        return successRetry;
    }

    public void setSuccessRetry(Integer successRetry) {
        this.successRetry = successRetry;
    }

    public Integer getUploadRecordImage() {
        return uploadRecordImage;
    }

    public void setUploadRecordImage(Integer uploadRecordImage) {
        this.uploadRecordImage = uploadRecordImage;
    }

    public Integer getIrLivePreview() {
        return irLivePreview;
    }

    public void setIrLivePreview(Integer irLivePreview) {
        this.irLivePreview = irLivePreview;
    }

    public Integer getRebootEveryDay() {
        return rebootEveryDay;
    }

    public void setRebootEveryDay(Integer rebootEveryDay) {
        this.rebootEveryDay = rebootEveryDay;
    }

    public String getRebootHour() {
        return rebootHour;
    }

    public void setRebootHour(String rebootHour) {
        this.rebootHour = rebootHour;
    }

    public String getRebootMin() {
        return rebootMin;
    }

    public void setRebootMin(String rebootMin) {
        this.rebootMin = rebootMin;
    }

    public Integer getFaceQuality() {
        return faceQuality;
    }

    public void setFaceQuality(Integer faceQuality) {
        this.faceQuality = faceQuality;
    }

    public String getFaceQualityThreshold() {
        return faceQualityThreshold;
    }

    public void setFaceQualityThreshold(String faceQualityThreshold) {
        this.faceQualityThreshold = faceQualityThreshold;
    }
}
