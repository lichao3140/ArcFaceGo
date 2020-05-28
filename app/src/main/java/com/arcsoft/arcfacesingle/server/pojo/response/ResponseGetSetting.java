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

package com.arcsoft.arcfacesingle.server.pojo.response;

import java.io.Serializable;

public class ResponseGetSetting implements Serializable {

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
     * 关门延迟时间
     */
    private String openDelay;

    /**
     * 显示模式
     */
    private String displayMode;
    /**
     * 显示自定义
     */
    private String displayCustom;
    /**
     * 陌生人模式
     */
    private String strangerMode;

    /**
     * 失败显示模式自定义内容
     */
    private String strangerCustom;
    /**
     * 陌生人语音模式
     */
    private String strangerVoiceMode;
    /**
     * 陌生人语音自定义
     */
    private String strangerVoiceCustom;
    /**
     * 成功语音模式
     */
    private String voiceMode;
    /**
     * 成功语音自定义
     */
    private String voiceCustom;
    /**
     * 最大识别人脸数
     */
    private int maxFaceSize;
    /**
     * 活体检测类型：
     */
    private int livenessType;
    /**
     * 识别距离
     */
    private int signDistance;

    /**
     * 识别失败重试间隔
     */
    private String interval;

    //*****************************以下为V2.0.0版本新增参数*****************************//
    /**
     * 识别成功重试间隔
     */
    private String successRetryDelay;

    /**
     * 识别重试开关：1 开；0 关，默认关
     */
    private int successRetry;

    /**
     * 上传刷脸记录图片：1 开；0 关，默认开
     */
    private int uploadRecordImage;

    /**
     * IR活体预览显示：1 开；0 关，默认开
     */
    private int irLivePreview;

    /**
     * 设备每日重启：1 开；0 关，默认关
     */
    private int rebootEveryDay;

    /**
     * 设备每日重启时间的小时数
     */
    private String rebootHour;

    /**
     * 设备每日重启时间的分钟
     */
    private String rebootMin;

    private int versionCode;

    private String packageName;

    private String versionName;

    private int faceQuality;

    private String faceQualityThreshold;

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

    public int getMaxFaceSize() {
        return maxFaceSize;
    }

    public void setMaxFaceSize(int maxFaceSize) {
        this.maxFaceSize = maxFaceSize;
    }

    public int getLivenessType() {
        return livenessType;
    }

    public void setLivenessType(int livenessType) {
        this.livenessType = livenessType;
    }

    public int getSignDistance() {
        return signDistance;
    }

    public void setSignDistance(int signDistance) {
        this.signDistance = signDistance;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getSuccessRetryDelay() {
        return successRetryDelay;
    }

    public void setSuccessRetryDelay(String successRetryDelay) {
        this.successRetryDelay = successRetryDelay;
    }

    public int getSuccessRetry() {
        return successRetry;
    }

    public void setSuccessRetry(int successRetry) {
        this.successRetry = successRetry;
    }

    public int getUploadRecordImage() {
        return uploadRecordImage;
    }

    public void setUploadRecordImage(int uploadRecordImage) {
        this.uploadRecordImage = uploadRecordImage;
    }

    public int getIrLivePreview() {
        return irLivePreview;
    }

    public void setIrLivePreview(int irLivePreview) {
        this.irLivePreview = irLivePreview;
    }

    public int getRebootEveryDay() {
        return rebootEveryDay;
    }

    public void setRebootEveryDay(int rebootEveryDay) {
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

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public int getFaceQuality() {
        return faceQuality;
    }

    public void setFaceQuality(int faceQuality) {
        this.faceQuality = faceQuality;
    }

    public String getFaceQualityThreshold() {
        return faceQualityThreshold;
    }

    public void setFaceQualityThreshold(String faceQualityThreshold) {
        this.faceQualityThreshold = faceQualityThreshold;
    }
}
