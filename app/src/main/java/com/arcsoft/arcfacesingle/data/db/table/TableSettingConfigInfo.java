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

package com.arcsoft.arcfacesingle.data.db.table;

import com.arcsoft.arcfacesingle.business.setting.ConfigConstants;
import com.arcsoft.arcfacesingle.data.db.DBManager;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.io.Serializable;

@Table(database = DBManager.class)
public class TableSettingConfigInfo extends BaseModel implements Serializable {

    @PrimaryKey(autoincrement = true)
    private long id;

    /**
     * 人脸检测方向：0 90 180 270 all
     */
    @Column
    private int faceDetectDegree = ConfigConstants.DEFAULT_FACE_DETECT_DEGREE;

    /**
     * 引擎初始化时的nScale值
     */
    @Column
    private int scale = ConfigConstants.DEFAULT_N_SCALE;

    /**
     * 开门方式
     */
    @Column
    private String openDoorType = ConfigConstants.DEFAULT_OPEN_DOOR_TYPE;

    /**
     * 设备设置管理密码
     */
    @Column
    private String devicePassword = ConfigConstants.DEFAULT_DEVICE_PASSWORD;

    /**
     * 输出信息
     *
     * 可为{@link ConfigConstants#OUT_PUT_MODE_CUSTOM, SettingConfigConstants#OUT_PUT_MODE_OPEN_DOOR ,SettingConfigConstants#OUT_PUT_MODE_PERSON_ID,SettingConfigConstants#OUT_PUT_MODE_NONE}
     * 中的一项
     */
    @Column
    private int outPutMode = ConfigConstants.OUT_PUT_MODE_OPEN_DOOR;

    /**
     * 输出信息自定义时的格式，仅当{@link TableSettingConfigInfo#outPutMode} 的值为{@link ConfigConstants#OUT_PUT_MODE_CUSTOM}时才有用
     */
    @Column
    private String customOutPutFormat = "";

    /**
     * 成功语音模式
     *
     * 可为{@link ConfigConstants#SUCCESS_VOICE_MODE_NO_PLAY , SettingConfigConstants#SUCCESS_VOICE_MODE_NAME ,SettingConfigConstants#SUCCESS_VOICE_MODE_CUSTOM}
     * 中的一项
     */
    @Column
    private int voiceMode = ConfigConstants.SUCCESS_VOICE_MODE_PREVIEW_TYPE6;

    /**
     * 成功语音模式自定义时的格式，仅当{@link TableSettingConfigInfo#voiceMode} 的值为{@link ConfigConstants#SUCCESS_VOICE_MODE_CUSTOM}时才有用
     */
    @Column
    private String customVoiceModeFormat = ConfigConstants.DEFAULT_SUCCESS_VOICE_MODE_CUSTOM_VALUE;

    /**
     * 失败语音模式
     *
     * 可为{@link ConfigConstants#FAILED_VOICE_MODE_NO_PLAY , SettingConfigConstants#FAILED_VOICE_MODE_WARN ,
     * SettingConfigConstants#FAILED_VOICE_MODE_PREVIEW_TYPE3 , SettingConfigConstants#FAILED_VOICE_MODE_PREVIEW_TYPE4 ,
     * SettingConfigConstants#FAILED_VOICE_MODE_CUSTOM}
     * 中的一项
     */
    @Column
    private int voiceModeFail = ConfigConstants.FAILED_VOICE_MODE_PREVIEW_TYPE3;

    /**
     * 失败语音模式自定义时的格式，仅当{@link TableSettingConfigInfo#voiceModeFail} 的值为{@link ConfigConstants#FAILED_VOICE_MODE_CUSTOM}时才有用
     */
    @Column
    private String customFailVoiceModeFormat = "";

    /**
     * 成功显示模式
     *
     * 可为{@link ConfigConstants#DISPLAY_MODE_SUCCESS_CUSTOM , SettingConfigConstants#DISPLAY_MODE_SUCCESS_NAME}
     * 中的一项
     */
    @Column
    private int displayMode = ConfigConstants.DISPLAY_MODE_SUCCESS_NAME;

    /**
     * 成功显示模式自定义时的格式，仅当{@link TableSettingConfigInfo#displayMode} 的值为{@link ConfigConstants#DISPLAY_MODE_SUCCESS_CUSTOM}时才有用
     */
    @Column
    private String customDisplayModeFormat = ConfigConstants.DISPLAY_MODE_SUCCESS_CUSTOM_VALUE;
    /**
     * 失败显示模式
     *
     * 可为{@link ConfigConstants#DISPLAY_MODE_FAILED_DEFAULT_MARKUP , SettingConfigConstants#DISPLAY_MODE_FAILED_NOT_FEEDBACK , SettingConfigConstants#DISPLAY_MODE_FAILED_CUSTOM}
     * 中的一项
     */
    @Column
    private int displayModeFail = ConfigConstants.DISPLAY_MODE_FAILED_DEFAULT_MARKUP;

    /**
     * 失败显示模式自定义时的格式，仅当{@link TableSettingConfigInfo#displayModeFail} 的值为{@link ConfigConstants#DISPLAY_MODE_FAILED_CUSTOM}时才有用
     */
    @Column
    private String customFailDisplayModeFormat = "";

    /**
     * 人脸识别模式
     *
     * 可为{@link ConfigConstants#RECOGNIZE_MODE_MULTI_FACE, SettingConfigConstants#RECOGNIZE_MODE_MAX_FACE}
     * 中的一项
     */
    @Column
    private int recognizeMode = ConfigConstants.RECOGNIZE_MODE_MAX_FACE;

    /**
     * 活体检测类型： 0：关闭；1：RGB；2：IR
     */
    @Column
    private boolean livenessDetect = true;

    /**
     * 阈值
     */
    @Column
    private String similarThreshold = ConfigConstants.DEFAULT_THRESHOLD;

    /**
     * 门禁权限
     */
    @Column
    private String permission = "";

    /**
     * 公司名称
     */
    @Column
    private String companyName = ConfigConstants.DEFAULT_COMPANY_NAME;

    /**
     * 关门延时
     */
    @Column
    private String closeDoorDelay = ConfigConstants.DEFAULT_CLOSE_DOOR_DELAY;

    /**
     * 失败重试间隔
     */
    @Column
    private String recognitionRetryDelay = ConfigConstants.DEFAULT_RETRY_DELAY;

    /**
     * 最多跟踪人脸数
     */
    @Column
    private String maxFaceTrackNumber = ConfigConstants.DEFAULT_MAX_RECOGNIZE_NUM;

    /**
     * 识别距离，算法未提供，所以只能根据该值获取不同的nScale
     */
    @Column
    private int recognizeDistance = ConfigConstants.RECOGNITION_DISTANCE_TYPE3;

    /**
     * 是否镜像预览
     */
    @Column
    private boolean mirror = false;

    /**
     * 是否横屏显示，否则为竖屏
     */
    @Column
    private boolean landscape = false;

    /**
     * 主图路径（供显示用，正上方居中）
     */
    @Column
    private String mainImagePath;

    /**
     * 副图路径（供显示用，右下角图）
     */
    @Column
    private String viceImagePath;

    /**
     * 设备网络端口号
     */
    @Column
    private String devicePort = ConfigConstants.DEFAULT_DEVICE_PORT;

    /**
     * 设备ip地址
     */
    @Column
    private String deviceIp = "";

    /**
     * 管理端端口号
     */
    @Column
    private String serverPort = ConfigConstants.DEFAULT_SERVER_PORT;

    /**
     * 管理端Ip
     */
    @Column
    private String serverIp = ConfigConstants.DEFAULT_SERVER_IP;

    /**
     * 设备mac地址
     */
    @Column
    private String macAddress = "";

    /**
     * 设备序列号
     */
    @Column
    private String serialNumber = "";

    /**
     * 设备ID
     */
    @Column
    private int deviceId;

    /**
     * 设备名称
     */
    @Column
    private String deviceName = "";

    /**
     * 签到类型
     */
    @Column
    private int signType = ConfigConstants.DEFAULT_SIGN_TYPE;

    /**
     * 签名密钥
     */
    @Column
    private String signKey = "";

    /**
     * 设备休眠是否跟随系统
     */
    @Column
    private boolean deviceSleepFollowSys = false;

    /**
     * 待机屏幕亮度是否跟随系统
     */
    @Column
    private boolean screenBrightFollowSys = true;

    /**
     * 待机屏幕亮度百分比
     */
    @Column
    private String screenDefBrightPercent = String.valueOf(ConfigConstants.SCREEN_DEFAULT_BRIGHT);

    /**
     * 首页屏保是否显示
     */
    @Column
    private boolean indexScreenDefShow = true;

    /**
     * 设备每日重启
     */
    @Column
    private boolean rebootEveryDay = true;

    /**
     * 重启小时
     */
    @Column
    private String rebootHour = "4";

    /**
     * 重启分钟
     */
    @Column
    private String rebootMin = "30";

    /**
     * 是否使用前置摄像头
     */
    @Column
    private boolean useFrontCamera = ConfigConstants.DEFAULT_IS_USE_FRONT_CAMERA;

    /**
     * 相机预览角度
     */
    @Column
    private int cameraDegree = ConfigConstants.DEFAULT_CAMERA_PREVIEW_DEGREE;

    /**
     * 是否预览半屏显示
     */
    @Column
    private boolean previewShowHalf = false;

    /**
     * 是否横向移动镜像
     */
    @Column
    private boolean landscapeMoveMirror = ConfigConstants.DEFAULT_IS_HORIZONTAL_MIRROR;

    /**
     * 是否竖向移动镜像
     */
    @Column
    private boolean portraitMoveMirror = ConfigConstants.DEFAULT_IS_VERTICAL_MIRROR;

    //***********************以下为V2.0.0版本新增参数**************************
    /**
     * 识别成功重试间隔，单位秒
     */
    @Column
    private String successRetryDelay = ConfigConstants.DEFAULT_RETRY_DELAY;

    /**
     * 识别重试开关：默认关闭
     */
    @Column
    private Integer successRetry = ConfigConstants.DEFAULT_RECOGNITION_SUCCESS_RETRY;

    /**
     * 上传刷脸记录图片
     */
    @Column
    private Integer uploadRecordImage = ConfigConstants.DEFAULT_UPLOAD_RECORD_IMAGE;

    /**
     * 活体检测类型： 0：关闭；1：RGB；2：IR
     */
    @Column
    private Integer liveDetectType = ConfigConstants.DEFAULT_LIVE_DETECT_RGB;

    /**
     * IR活体预览显示
     */
    @Column
    private Integer irLivePreview = ConfigConstants.DEFAULT_IR_LIVE_PREVIEW_HIDE;

    /**
     * ir活体阈值
     */
    @Column
    private String irLiveThreshold = ConfigConstants.DEFAULT_IR_LIVE_THRESHOLD;

    @Column
    private boolean faceQuality = true;

    @Column
    private String faceQualityThreshold = ConfigConstants.DEFAULT_FACE_QUALITY_THRESHOLD;

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

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public int getSignType() {
        return signType;
    }

    public void setSignType(int signType) {
        this.signType = signType;
    }

    public String getSignKey() {
        return signKey;
    }

    public void setSignKey(String signKey) {
        this.signKey = signKey;
    }

    public int getOutPutMode() {
        return outPutMode;
    }

    public void setOutPutMode(int outPutMode) {
        this.outPutMode = outPutMode;
    }

    public String getCustomOutPutFormat() {
        return customOutPutFormat;
    }

    public void setCustomOutPutFormat(String customOutPutFormat) {
        this.customOutPutFormat = customOutPutFormat;
    }

    public int getVoiceMode() {
        return voiceMode;
    }

    public void setVoiceMode(int voiceMode) {
        this.voiceMode = voiceMode;
    }

    public String getCustomVoiceModeFormat() {
        return customVoiceModeFormat;
    }

    public void setCustomVoiceModeFormat(String customVoiceModeFormat) {
        this.customVoiceModeFormat = customVoiceModeFormat;
    }

    public int getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(int displayMode) {
        this.displayMode = displayMode;
    }

    public String getCustomDisplayModeFormat() {
        return customDisplayModeFormat;
    }

    public void setCustomDisplayModeFormat(String customDisplayModeFormat) {
        this.customDisplayModeFormat = customDisplayModeFormat;
    }

    public int getRecognizeMode() {
        return recognizeMode;
    }

    public void setRecognizeMode(int recognizeMode) {
        this.recognizeMode = recognizeMode;
    }

    public boolean isLivenessDetect() {
        return livenessDetect;
    }

    public void setLivenessDetect(boolean livenessDetect) {
        this.livenessDetect = livenessDetect;
    }

    public String getSimilarThreshold() {
        return similarThreshold;
    }

    public void setSimilarThreshold(String similarThreshold) {
        this.similarThreshold = similarThreshold;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCloseDoorDelay() {
        return closeDoorDelay;
    }

    public void setCloseDoorDelay(String closeDoorDelay) {
        this.closeDoorDelay = closeDoorDelay;
    }

    public String getRecognitionRetryDelay() {
        return recognitionRetryDelay;
    }

    public void setRecognitionRetryDelay(String recognitionRetryDelay) {
        this.recognitionRetryDelay = recognitionRetryDelay;
    }

    public String getMaxFaceTrackNumber() {
        return maxFaceTrackNumber;
    }

    public void setMaxFaceTrackNumber(String maxFaceTrackNumber) {
        this.maxFaceTrackNumber = maxFaceTrackNumber;
    }

    public int getRecognizeDistance() {
        return recognizeDistance;
    }

    public void setRecognizeDistance(int recognizeDistance) {
        this.recognizeDistance = recognizeDistance;
    }

    public boolean isMirror() {
        return mirror;
    }

    public void setMirror(boolean mirror) {
        this.mirror = mirror;
    }

    public boolean isLandscape() {
        return landscape;
    }

    public void setLandscape(boolean landscape) {
        this.landscape = landscape;
    }

    public String getMainImagePath() {
        return mainImagePath;
    }

    public void setMainImagePath(String mainImagePath) {
        this.mainImagePath = mainImagePath;
    }

    public String getViceImagePath() {
        return viceImagePath;
    }

    public void setViceImagePath(String viceImagePath) {
        this.viceImagePath = viceImagePath;
    }

    public String getDevicePort() {
        return devicePort;
    }

    public void setDevicePort(String devicePort) {
        this.devicePort = devicePort;
    }

    public String getDeviceIp() {
        return deviceIp;
    }

    public void setDeviceIp(String deviceIp) {
        this.deviceIp = deviceIp;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public int getFaceDetectDegree() {
        return faceDetectDegree;
    }

    public void setFaceDetectDegree(int faceDetectDegree) {
        this.faceDetectDegree = faceDetectDegree;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public boolean isDeviceSleepFollowSys() {
        return deviceSleepFollowSys;
    }

    public void setDeviceSleepFollowSys(boolean deviceSleepFollowSys) {
        this.deviceSleepFollowSys = deviceSleepFollowSys;
    }

    public boolean isScreenBrightFollowSys() {
        return screenBrightFollowSys;
    }

    public void setScreenBrightFollowSys(boolean screenBrightFollowSys) {
        this.screenBrightFollowSys = screenBrightFollowSys;
    }

    public String getScreenDefBrightPercent() {
        return screenDefBrightPercent;
    }

    public void setScreenDefBrightPercent(String screenDefBrightPercent) {
        this.screenDefBrightPercent = screenDefBrightPercent;
    }

    public boolean isIndexScreenDefShow() {
        return indexScreenDefShow;
    }

    public void setIndexScreenDefShow(boolean indexScreenDefShow) {
        this.indexScreenDefShow = indexScreenDefShow;
    }

    public boolean isRebootEveryDay() {
        return rebootEveryDay;
    }

    public void setRebootEveryDay(boolean rebootEveryDay) {
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

    public int getDisplayModeFail() {
        return displayModeFail;
    }

    public void setDisplayModeFail(int displayModeFail) {
        this.displayModeFail = displayModeFail;
    }

    public String getCustomFailDisplayModeFormat() {
        return customFailDisplayModeFormat;
    }

    public void setCustomFailDisplayModeFormat(String customFailDisplayModeFormat) {
        this.customFailDisplayModeFormat = customFailDisplayModeFormat;
    }

    public int getVoiceModeFail() {
        return voiceModeFail;
    }

    public void setVoiceModeFail(int voiceModeFail) {
        this.voiceModeFail = voiceModeFail;
    }

    public String getCustomFailVoiceModeFormat() {
        return customFailVoiceModeFormat;
    }

    public void setCustomFailVoiceModeFormat(String customFailVoiceModeFormat) {
        this.customFailVoiceModeFormat = customFailVoiceModeFormat;
    }

    public boolean isUseFrontCamera() {
        return useFrontCamera;
    }

    public void setUseFrontCamera(boolean useFrontCamera) {
        this.useFrontCamera = useFrontCamera;
    }

    public int getCameraDegree() {
        return cameraDegree;
    }

    public void setCameraDegree(int cameraDegree) {
        this.cameraDegree = cameraDegree;
    }

    public boolean isPreviewShowHalf() {
        return previewShowHalf;
    }

    public void setPreviewShowHalf(boolean previewShowHalf) {
        this.previewShowHalf = previewShowHalf;
    }

    public boolean isLandscapeMoveMirror() {
        return landscapeMoveMirror;
    }

    public void setLandscapeMoveMirror(boolean landscapeMoveMirror) {
        this.landscapeMoveMirror = landscapeMoveMirror;
    }

    public boolean isPortraitMoveMirror() {
        return portraitMoveMirror;
    }

    public void setPortraitMoveMirror(boolean portraitMoveMirror) {
        this.portraitMoveMirror = portraitMoveMirror;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDevicePassword() {
        return devicePassword;
    }

    public void setDevicePassword(String devicePassword) {
        this.devicePassword = devicePassword;
    }

    public String getServerPort() {
        return serverPort;
    }

    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public String getOpenDoorType() {
        return openDoorType;
    }

    public void setOpenDoorType(String openDoorType) {
        this.openDoorType = openDoorType;
    }

    public String getIrLiveThreshold() {
        return irLiveThreshold;
    }

    public void setIrLiveThreshold(String irLiveThreshold) {
        this.irLiveThreshold = irLiveThreshold;
    }

    public boolean isFaceQuality() {
        return faceQuality;
    }

    public void setFaceQuality(boolean faceQuality) {
        this.faceQuality = faceQuality;
    }

    public String getFaceQualityThreshold() {
        return faceQualityThreshold;
    }

    public void setFaceQualityThreshold(String faceQualityThreshold) {
        this.faceQualityThreshold = faceQualityThreshold;
    }
}
