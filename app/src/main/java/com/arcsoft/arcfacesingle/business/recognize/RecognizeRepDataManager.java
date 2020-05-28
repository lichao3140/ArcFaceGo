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

package com.arcsoft.arcfacesingle.business.recognize;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.SparseIntArray;

import androidx.annotation.NonNull;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.business.common.CommonRepository;
import com.arcsoft.arcfacesingle.business.common.FaceRecordDataManager;
import com.arcsoft.arcfacesingle.business.setting.ConfigConstants;
import com.arcsoft.arcfacesingle.data.db.dao.IdentifyRecordDao;
import com.arcsoft.arcfacesingle.data.db.dao.PersonDao;
import com.arcsoft.arcfacesingle.data.db.dao.PersonFaceDao;
import com.arcsoft.arcfacesingle.data.db.table.TablePerson;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonFace;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonPermission;
import com.arcsoft.arcfacesingle.data.db.table.TableSettingConfigInfo;
import com.arcsoft.arcfacesingle.data.db.table.TableSignRecord;
import com.arcsoft.arcfacesingle.data.model.http.ReqSignRecord;
import com.arcsoft.arcfacesingle.data.model.http.ReqSignRecordImage;
import com.arcsoft.arcfacesingle.data.model.http.ReqSignRecordImageList;
import com.arcsoft.arcfacesingle.data.model.http.ReqSignRecordList;
import com.arcsoft.arcfacesingle.libutil.ImageFileUtils;
import com.arcsoft.arcfacesingle.server.api.ServerConstants;
import com.arcsoft.arcfacesingle.server.pojo.request.RequestDoorAuthorityV2;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.arcfacesingle.util.SdcardUtils;
import com.arcsoft.arcfacesingle.util.business.Nv21Helper;
import com.arcsoft.arcfacesingle.util.scheduler.BaseObserver;
import com.arcsoft.arcsoftlink.http.bean.req.RecognizeRecord;
import com.arcsoft.arcsoftlink.http.bean.res.UploadRecordResponse;
import com.arcsoft.arcsoftlink.mqtt.ArcLinkEngine;
import com.arcsoft.asg.libcamera.bean.CameraViewParam;
import com.arcsoft.asg.libcamera.bean.FaceRectInfo;
import com.arcsoft.asg.libcamera.constant.CameraViewConstants;
import com.arcsoft.asg.libcommon.manage.SoundPlayerManager;
import com.arcsoft.asg.libcommon.manage.TextToSpeechManager;
import com.arcsoft.asg.libcommon.util.business.rx.RxUtils;
import com.arcsoft.asg.libcommon.util.common.FileUtils;
import com.arcsoft.asg.libcommon.util.common.NetworkUtils;
import com.arcsoft.asg.libcommon.util.common.StringUtils;
import com.arcsoft.asg.libcommon.util.common.TimeUtils;
import com.arcsoft.asg.libcommon.util.common.Utils;
import com.arcsoft.asg.libdeviceadapt.bean.DeviceAdaptationInfo;
import com.arcsoft.asg.libnetwork.bean.RemoteResponseBase;
import com.arcsoft.asg.libnetwork.manage.DefaultRemoteApiManager;
import com.arcsoft.faceengine.Config;
import com.arcsoft.faceengine.FaceEngine;
import com.arcsoft.faceengine.FaceInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;

public final class RecognizeRepDataManager {

    private static final String TAG = RecognizeRepDataManager.class.getSimpleName();
    private static final int VALUE_SAVE_RECORD_PICTURE_QUALITY = 60;
    public static final int RECT_BORDER_WIDTH = 4;
    private static final int RECT_BORDER_COLOR = Color.argb(255, 8, 108, 202);

    /**
     * 自定义声音文件id
     */
    private static final int SOUND_RECOGNITION_SUCCESS = 1;
    private static final int SOUND_DOOR_OPENED = 2;
    private static final int SOUND_WELCOME = 3;
    private static final int SOUND_DOOR_OPENED_WELCOME = 4;
    private static final int SOUND_RECOGNITION_FAILED = 5;
    private static final int SOUND_VERIFICATION_FAILED = 6;
    private static final int SOUND_FAILED_DEFAULT = 7;

    /**
     * 识别结果类型
     */
    public static final int FAILED_LIVE_AND_EXTRACT = 0;
    public static final int FAILED_LIVE_DETECT_CHECK = 1;
    public static final int FAILED_THRESHOLD_COMPARE = 2;
    public static final int SUCCESS_THRESHOLD_COMPARE = 3;
    public static final int SUCCESS_THRESHOLD_COMPARE_UNAUTHORIZED = 4;
    /**
     * 新增失败类型：识别过程中人员被删除了
     */
    public static final int FAILED_PROCESSING_FACE_PERSON_REMOVE = 5;

    private Nv21Helper converter = new Nv21Helper(Utils.getApp());
    private static volatile RecognizeRepDataManager INSTANCE;

    private RecognizeRepDataManager() {
    }

    public static RecognizeRepDataManager getInstance() {
        if (INSTANCE == null) {
            synchronized (RecognizeRepDataManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RecognizeRepDataManager();
                }
            }
        }
        return INSTANCE;
    }

    public Bitmap getSecondLogo(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        } else {
            File secondLogoFile = new File(url);
            if (secondLogoFile.exists()) {
                String secondLogoId = CommonRepository.getInstance().getSecondLogoId();
                if (TextUtils.isEmpty(secondLogoId)) {
                    String logoId = CommonRepository.getInstance().createDatabaseId();
                    CommonRepository.getInstance().saveSecondLogoId(logoId);
                }
                return ImageFileUtils.getBitmap(secondLogoFile.getAbsoluteFile());
            } else {
                Bitmap bmpSecondLogo = ImageFileUtils.getBitmap(R.mipmap.ic_company_second_logo);
                if (ImageFileUtils.save(bmpSecondLogo, secondLogoFile, Bitmap.CompressFormat.PNG)) {
                    String logoId = CommonRepository.getInstance().createDatabaseId();
                    CommonRepository.getInstance().saveSecondLogoId(logoId);
                }
                return bmpSecondLogo;
            }
        }
    }

    public Bitmap getMainLogo(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        } else {
            File mainLogoFile = new File(url);
            if (mainLogoFile.exists()) {
                String mainLogoId = CommonRepository.getInstance().getMainLogoId();
                if (TextUtils.isEmpty(mainLogoId)) {
                    String logoId = CommonRepository.getInstance().createDatabaseId();
                    CommonRepository.getInstance().saveMainLogoId(logoId);
                }
                return ImageFileUtils.getBitmap(mainLogoFile.getAbsoluteFile());
            } else {
                Bitmap bmpMainLogo = ImageFileUtils.getBitmap(R.mipmap.ic_company_main_logo);
                if (ImageFileUtils.save(bmpMainLogo, mainLogoFile, Bitmap.CompressFormat.PNG)) {
                    String logoId = CommonRepository.getInstance().createDatabaseId();
                    CommonRepository.getInstance().saveMainLogoId(logoId);
                }
                return bmpMainLogo;
            }
        }
    }

    public SparseIntArray getSoundDataList() {
        SparseIntArray soundList = new SparseIntArray(7);
        soundList.put(SOUND_RECOGNITION_SUCCESS, R.raw.recognition_success);
        soundList.put(SOUND_DOOR_OPENED, R.raw.door_opened);
        soundList.put(SOUND_WELCOME, R.raw.welcome);
        soundList.put(SOUND_DOOR_OPENED_WELCOME, R.raw.door_opened_welcome);
        soundList.put(SOUND_RECOGNITION_FAILED, R.raw.recognition_failed);
        soundList.put(SOUND_VERIFICATION_FAILED, R.raw.verification_failed);
        soundList.put(SOUND_FAILED_DEFAULT, R.raw.failed_default);
        return soundList;
    }

    public void playRecognitionUnauthorizedSound() {
        String strWarn = Utils.getApp().getResources().getString(R.string.unauthorized_access);
        TextToSpeechManager.getInstance().speak(strWarn, null, null);
    }

    /**
     * 播放识别成功声音
     */
    public void playRecognitionSuccessSound(TablePerson tablePerson, TableSettingConfigInfo settingConfigInfo) {
        String personName = tablePerson.personName;
        if (settingConfigInfo != null && settingConfigInfo.getVoiceMode() != ConfigConstants.SUCCESS_VOICE_MODE_NO_PLAY) {
            int voiceMode = settingConfigInfo.getVoiceMode();
            if (voiceMode != ConfigConstants.SUCCESS_VOICE_MODE_NO_PLAY) {
                switch (voiceMode) {
                    case ConfigConstants.SUCCESS_VOICE_MODE_NAME:
                        TextToSpeechManager.getInstance().speak(personName, null, null);
                        break;
                    case ConfigConstants.SUCCESS_VOICE_MODE_CUSTOM:
                        String strVoiceCustom = settingConfigInfo.getCustomVoiceModeFormat();
                        String personId = TextUtils.isEmpty(tablePerson.personInfoNo) ? "" : tablePerson.personInfoNo;
                        if (!TextUtils.isEmpty(strVoiceCustom)) {
                            String voiceCustom = StringUtils.getRecognitionShowCustom(strVoiceCustom.trim(), personName, personId);
                            TextToSpeechManager.getInstance().speak(voiceCustom, null, null);
                        }
                        break;
                    case ConfigConstants.SUCCESS_VOICE_MODE_PREVIEW_TYPE4:
                        playSoundInThread(SOUND_WELCOME);
                        break;
                    case ConfigConstants.SUCCESS_VOICE_MODE_PREVIEW_TYPE5:
                        playSoundInThread(SOUND_DOOR_OPENED);
                        break;
                    case ConfigConstants.SUCCESS_VOICE_MODE_PREVIEW_TYPE6:
                        playSoundInThread(SOUND_DOOR_OPENED_WELCOME);
                        break;
                    default:
                        playSoundInThread(SOUND_RECOGNITION_SUCCESS);
                        break;
                }
            }
        }
    }

    /**
     * 播放识别失败声音
     */
    public void playRecognitionFailureSound(TableSettingConfigInfo settingConfigInfo) {
        if (settingConfigInfo != null && settingConfigInfo.getVoiceModeFail() != ConfigConstants.FAILED_VOICE_MODE_NO_PLAY) {
            int failedVoiceMode = settingConfigInfo.getVoiceModeFail();
            switch (failedVoiceMode) {
                case ConfigConstants.FAILED_VOICE_MODE_CUSTOM:
                    String stringFail = settingConfigInfo.getCustomFailVoiceModeFormat();
                    if (!TextUtils.isEmpty(stringFail)) {
                        TextToSpeechManager.getInstance().speak(stringFail, null, null);
                    }
                    break;
                case ConfigConstants.FAILED_VOICE_MODE_PREVIEW_TYPE3:
                    playSoundInThread(SOUND_RECOGNITION_FAILED);
                    break;
                case ConfigConstants.FAILED_VOICE_MODE_PREVIEW_TYPE4:
                    playSoundInThread(SOUND_VERIFICATION_FAILED);
                    break;
                default:
                    playSoundInThread(SOUND_FAILED_DEFAULT);
                    break;
            }
        }
    }

    /**
     * 保存识别记录并上传
     */
    public void saveRecognitionResult(int type, FaceInfo faceInfo, byte[] nv21Data, TablePersonFace tablePersonFace,
                                      TableSettingConfigInfo configInfo, int sizeWidth, int sizeHeight) {
        boolean uploadImage = CommonRepository.getInstance().uploadRecordImage(configInfo);
        if (uploadImage) {
            String imageNameSuf;
            String strTime = TimeUtils.getCurrentTimeRecordImg();
            switch (type) {
                case FAILED_LIVE_AND_EXTRACT:
                    imageNameSuf = strTime + "_failed_fr_" + CommonUtils.getStringFaceResult(faceInfo);
                    break;
                case FAILED_LIVE_DETECT_CHECK:
                    imageNameSuf = strTime + "_failed_liveness_" + CommonUtils.getStringFaceResult(faceInfo);
                    break;
                case FAILED_THRESHOLD_COMPARE:
                    if (tablePersonFace != null) {
                        imageNameSuf = strTime + "_failed_threshold_" + CommonUtils.getStringFaceResult(faceInfo) + "_";
                    } else {
                        imageNameSuf = strTime + "_failed_threshold_" + CommonUtils.getStringFaceResult(faceInfo);
                    }
                    break;
                case FAILED_PROCESSING_FACE_PERSON_REMOVE:
                    if (tablePersonFace != null) {
                        imageNameSuf = strTime + "_failed_person_remove_" + CommonUtils.getStringFaceResult(faceInfo) + "_";
                    } else {
                        imageNameSuf = strTime + "_failed_person_remove_" + CommonUtils.getStringFaceResult(faceInfo);
                    }
                    break;
                case SUCCESS_THRESHOLD_COMPARE:
                    imageNameSuf = strTime + "_success_" + CommonUtils.getStringFaceResult(faceInfo);
                    break;
                case SUCCESS_THRESHOLD_COMPARE_UNAUTHORIZED:
                    imageNameSuf = strTime + "_unauthorized_" + CommonUtils.getStringFaceResult(faceInfo);
                    break;
                default:
                    imageNameSuf = strTime + "_signRecord_" + CommonUtils.getStringFaceResult(faceInfo);
                    break;
            }
            int rotate;
            switch (faceInfo.faceOrient) {
                case FaceEngine.ASF_OC_90:
                    rotate = 90;
                    break;
                case FaceEngine.ASF_OC_180:
                    rotate = 180;
                    break;
                case FaceEngine.ASF_OC_270:
                    rotate = 270;
                    break;
                default:
                    rotate = 0;
                    break;
            }
            if (FileUtils.getSdcardAvailableSize() > Constants.SDCARD_STORAGE_SIZE_DELETE) {
                saveImageWithThread(type, nv21Data, rotate, imageNameSuf, tablePersonFace, configInfo, sizeWidth,
                        sizeHeight);
            }
        } else {
            TableSignRecord signRecord = getTableSignRecord(type, null, tablePersonFace, configInfo);
            if (CommonUtils.isCloudAiotAppMode()) {
                if (CommonRepository.getInstance().getDeviceAccessStatus()) {
                    postArcLinkRecord(signRecord, false);
                } else {
                    IdentifyRecordDao.getInstance().addModelTransactionAsync(signRecord);
                }
            }
            if (CommonUtils.isOfflineLanAppMode()) {
                if (DefaultRemoteApiManager.getInstance().isInitSuccess() && NetworkUtils.isConnected()) {
                    postLocalServerRecord(signRecord, configInfo, false);
                } else {
                    IdentifyRecordDao.getInstance().addModelTransactionAsync(signRecord);
                }
            }
        }
    }

    private void saveImageWithThread(int type, byte[] nv21, int rotate, String imageNameSuf, TablePersonFace tablePersonFace,
                                     TableSettingConfigInfo configInfo, int sizeWidth, int sizeHeight) {
        Disposable disposable = Observable.create((ObservableEmitter<Bitmap> emitter) -> {
            Bitmap rotateBmp;
            Bitmap nv21Bmp;
            nv21Bmp = converter.nv21ToBitmap(nv21, sizeWidth, sizeHeight);
            if (rotate > 0) {
                rotateBmp = ImageFileUtils.rotate(nv21Bmp, rotate, 0, 0);
                if (!nv21Bmp.isRecycled()) {
                    nv21Bmp.recycle();
                }
            } else {
                rotateBmp = nv21Bmp;
            }
            if (rotateBmp != null) {
                emitter.onNext(rotateBmp);
                emitter.onComplete();
            } else {
                emitter.onError(new Throwable(""));
            }
        }).compose(RxUtils.computingToIo()).flatMap((Function<Bitmap, ObservableSource<TableSignRecord>>) bitmap -> Observable.create((ObservableEmitter<TableSignRecord> emitter) -> {
            String tempImageName = imageNameSuf + ".jpg";
            String path = SdcardUtils.getInstance().getSignRecordDirPath() + File.separator +
                    TimeUtils.getShortStringDay(System.currentTimeMillis());

            File recordFile = new File(path, tempImageName);
            if (ImageFileUtils.save(bitmap, recordFile, VALUE_SAVE_RECORD_PICTURE_QUALITY, Bitmap.CompressFormat.JPEG)) {
                TableSignRecord tableSignRecord = getTableSignRecord(type, recordFile.getAbsolutePath(), tablePersonFace,
                        configInfo);
                if (CommonUtils.isOfflineLanAppMode()) {
                    if (DefaultRemoteApiManager.getInstance().isInitSuccess() && NetworkUtils.isConnected()) {
                        emitter.onNext(tableSignRecord);
                    } else {
                        IdentifyRecordDao.getInstance().addModelTransactionAsync(tableSignRecord);
                        emitter.onError(new Throwable(Utils.getApp().getString(R.string.network_invalid)));
                    }
                }
                if (CommonUtils.isCloudAiotAppMode()) {
                    emitter.onNext(tableSignRecord);
                }
            } else {
                emitter.onError(new Throwable(Utils.getApp().getString(R.string.save_picture_fail)));
            }
            emitter.onComplete();
        })).compose(RxUtils.ioToMain()).subscribe(tableSignRecord -> {
            if (CommonUtils.isOfflineLanAppMode()) {
                postLocalServerRecord(tableSignRecord, configInfo, true);
            }
            if (CommonUtils.isCloudAiotAppMode()) {
                if (CommonRepository.getInstance().getDeviceAccessStatus()) {
                    postArcLinkRecord(tableSignRecord, true);
                } else {
                    IdentifyRecordDao.getInstance().addModelTransactionAsync(tableSignRecord);
                }
            }
        }, throwable -> {
        });
    }

    private void postLocalServerRecord(TableSignRecord tableSignRecord, TableSettingConfigInfo configInfo, boolean existImage) {
        String imageName;
        if (existImage) {
            String imagePath = tableSignRecord.imagePath;
            if (!TextUtils.isEmpty(imagePath)) {
                String tempName;
                try {
                    tempName = imagePath.substring(imagePath.lastIndexOf("/") + 1, imagePath.lastIndexOf("."));
                } catch (StringIndexOutOfBoundsException | ArrayIndexOutOfBoundsException e) {
                    tempName = "";
                }
                imageName = tempName;
            } else {
                imageName = "";
            }
        } else {
            imageName = "";
        }
        String requestBody = getUploadRecordTextBody(tableSignRecord, imageName, configInfo.getDeviceId(), existImage);
        Disposable disposable = DefaultRemoteApiManager.getInstance()
                .uploadRecordText(requestBody)
                .concatMap(responseBase -> {
                    if (responseBase.getCode() == Constants.HTTP_REQUEST_CODE_SUCCESS) {
                        HashMap<String, Integer> resultMap = responseBase.getData();
                        String key = TextUtils.isEmpty(tableSignRecord.recordId) ? String.valueOf(tableSignRecord.addTime) :
                                tableSignRecord.recordId;
                        Integer status = resultMap.get(key);
                        if (status != null && status == FaceRecordDataManager.SIGN_RECORD_STATUS_SUCCESS) {
                            if (existImage) {
                                String imagePath = tableSignRecord.imagePath;
                                tableSignRecord.status = 1;
                                String requestBodyImg = getUploadRecordImgBody(imageName, imagePath,
                                        configInfo, tableSignRecord);
                                return DefaultRemoteApiManager.getInstance().uploadRecordImage(requestBodyImg);
                            } else {
                                return justResponseBase(responseBase.getCode(), responseBase.getMsg());
                            }
                        } else {
                            return justResponseBase(responseBase.getCode(), responseBase.getMsg());
                        }
                    } else {
                        return justResponseBase(responseBase.getCode(), responseBase.getMsg());
                    }
                })
                .compose(RxUtils.ioToMain())
                .subscribeWith(new DisposableObserver<RemoteResponseBase<HashMap<String, Integer>>>() {

                    @Override
                    public void onNext(RemoteResponseBase<HashMap<String, Integer>> responseBase) {
                        if (responseBase.getCode() == Constants.HTTP_REQUEST_CODE_SUCCESS && existImage) {
                            HashMap<String, Integer> resultMap = responseBase.getData();
                            if (resultMap != null && resultMap.size() > 0) {
                                for (Map.Entry<String, Integer> entry : resultMap.entrySet()) {
                                    int status = entry.getValue();
                                    if (status == FaceRecordDataManager.SIGN_RECORD_STATUS_SUCCESS) {
                                        FileUtils.delete(tableSignRecord.imagePath);
                                    } else if (status == FaceRecordDataManager.SIGN_RECORD_STATUS_SERVER_PERSON_INVALID) {
                                        tableSignRecord.status = IdentifyRecordDao.STATUS_IC_SERVER_DATA_INVALID;
                                        IdentifyRecordDao.getInstance().updateAsync(tableSignRecord);
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        IdentifyRecordDao.getInstance().addModelTransactionAsync(tableSignRecord);
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private void postArcLinkRecord(TableSignRecord tableSignRecord, boolean existImage) {
        Disposable disposable = Observable.create((ObservableEmitter<UploadRecordResponse> emitter) -> {
            String imagePath = tableSignRecord.imagePath;
            RecognizeRecord recordBean = new RecognizeRecord(
                    tableSignRecord.personId,
                    tableSignRecord.faceId,
                    existImage ? ImageFileUtils.image2Base64(imagePath) : "",
                    tableSignRecord.addTime,
                    tableSignRecord.recordId,
                    tableSignRecord.type);
            UploadRecordResponse baseResponse = ArcLinkEngine.getInstance().uploadSignRecord(recordBean);
            if (baseResponse == null) {
                baseResponse = new UploadRecordResponse();
                baseResponse.setCode(UploadRecordResponse.CODE_UNKNOWN_ERROR);
                emitter.onNext(new UploadRecordResponse());
            } else {
                emitter.onNext(baseResponse);
            }
            emitter.onComplete();
        })
                .compose(RxUtils.ioToMain())
                .subscribeWith(new BaseObserver<UploadRecordResponse>() {
                    @Override
                    public void onNext(UploadRecordResponse baseResponse) {
                        if (baseResponse.success()) {
                            FileUtils.delete(tableSignRecord.imagePath);
                        } else {
                            IdentifyRecordDao.getInstance().addModelTransactionAsync(tableSignRecord);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        IdentifyRecordDao.getInstance().addModelTransactionAsync(tableSignRecord);
                    }
                });
    }

    private Observable<RemoteResponseBase<HashMap<String, Integer>>> justResponseBase(int code, String msg) {
        RemoteResponseBase<HashMap<String, Integer>> responseBase = new RemoteResponseBase<>();
        responseBase.setCode(code);
        responseBase.setMsg(msg);
        return Observable.just(responseBase);
    }

    private TableSignRecord getTableSignRecord(int type, String imgFilePath, TablePersonFace tablePersonFace,
                                               TableSettingConfigInfo settingConfigInfo) {
        TableSignRecord tableSignRecord = new TableSignRecord();
        if (tablePersonFace != null) {
            tableSignRecord.recordId = CommonRepository.getInstance().createDatabaseId();
            tableSignRecord.addTime = System.currentTimeMillis();
            tableSignRecord.updateTime = tableSignRecord.addTime;
            if (imgFilePath != null) {
                tableSignRecord.imagePath = imgFilePath;
            }
            if (type == SUCCESS_THRESHOLD_COMPARE || type == SUCCESS_THRESHOLD_COMPARE_UNAUTHORIZED) {
                if (type == SUCCESS_THRESHOLD_COMPARE) {
                    tableSignRecord.signType = settingConfigInfo.getSignType();
                    tableSignRecord.type = ConfigConstants.TYPE_AIOT_RECOGNIZE_SUCCESSFUL;
                } else {
                    tableSignRecord.signType = ConfigConstants.TYPE_SIGN_UNAUTHORIZED;
                    tableSignRecord.type = ConfigConstants.TYPE_AIOT_RECOGNIZE_UNAUTHORIZED;
                }
                tableSignRecord.personSerial = tablePersonFace.personSerial;
                TablePerson tablePerson = PersonDao.getInstance().getPersonByPersonSerial(tableSignRecord.personSerial);
                if (tablePerson != null) {
                    tableSignRecord.faceInfo = tablePerson.personName;
                }
                if (CommonUtils.isCloudAiotAppMode()) {
                    tableSignRecord.faceId = tablePersonFace.faceId;
                    if (tablePerson != null) {
                        tableSignRecord.personId = tablePerson.personId;
                        tableSignRecord.personSetId = tablePerson.personSetId;
                        tableSignRecord.faceInfo = tablePerson.personName;
                    }
                }
            } else {
                tableSignRecord.signType = ConfigConstants.TYPE_SIGN_FAIL;
                tableSignRecord.type = ConfigConstants.TYPE_AIOT_RECOGNIZE_FAILED;
            }
        } else {
            tableSignRecord.recordId = CommonRepository.getInstance().createDatabaseId();
            tableSignRecord.addTime = System.currentTimeMillis();
            tableSignRecord.updateTime = tableSignRecord.addTime;
            if (imgFilePath != null) {
                tableSignRecord.imagePath = imgFilePath;
            }
            tableSignRecord.signType = ConfigConstants.TYPE_SIGN_FAIL;
            tableSignRecord.type = ConfigConstants.TYPE_AIOT_RECOGNIZE_FAILED;
            tableSignRecord.faceInfo = "";
        }
        return tableSignRecord;
    }

    private String getUploadRecordTextBody(TableSignRecord signRecord, String imageName, int deviceId, boolean existImage) {
        if (signRecord == null) {
            return null;
        }
        ReqSignRecord reqSignRecord = new ReqSignRecord();
        reqSignRecord.setCheckTime(signRecord.addTime);
        reqSignRecord.setEquipmentId(deviceId);
        //以下EquipmentVerificationId修改让V2.0.0版本兼容老版本数据
        String recordId = signRecord.recordId;
        if (TextUtils.isEmpty(recordId)) {
            reqSignRecord.setEquipmentVerificationId(String.valueOf(signRecord.addTime));
        } else {
            reqSignRecord.setEquipmentVerificationId(recordId);
        }
        reqSignRecord.setPersonCode(signRecord.personSerial);
        reqSignRecord.setVerificationType(signRecord.signType);
        reqSignRecord.setImageName(imageName);
        reqSignRecord.setRecognitionName(TextUtils.isEmpty(signRecord.faceInfo) ? "" : signRecord.faceInfo);
        reqSignRecord.setExistImage(existImage ? FaceRecordDataManager.TYPE_RECORD_EXIST_IMAGE :
                FaceRecordDataManager.TYPE_RECORD_NOT_EXIST_IMAGE);
        reqSignRecord.setRecordType(ReqSignRecord.TYPE_RECORD_FACE);
        ReqSignRecordList reqSignRecordList = new ReqSignRecordList();
        reqSignRecordList.addRequestSignRecord(reqSignRecord);
        return new Gson().toJson(reqSignRecordList);
    }

    private String getUploadRecordImgBody(String imageName, String imagePath, TableSettingConfigInfo settingConfigInfo, TableSignRecord tableSignRecord) {
        ReqSignRecordImageList signRecordImageList = new ReqSignRecordImageList();
        ReqSignRecordImage image = new ReqSignRecordImage();
        image.setImageName(imageName);
        String imageBase64 = ImageFileUtils.image2Base64(imagePath);
        image.setImageBase64(imageBase64);
        signRecordImageList.addRequestSignRecord(image);
        int deviceId = settingConfigInfo.getDeviceId();
        image.setEquipmentId(deviceId);
        //以下EquipmentVerificationId修改让V2.0.0版本兼容老版本数据
        String recordId = tableSignRecord.recordId;
        if (TextUtils.isEmpty(recordId)) {
            image.setEquipmentVerificationId(String.valueOf(tableSignRecord.addTime));
        } else {
            image.setEquipmentVerificationId(recordId);
        }
        return new Gson().toJson(signRecordImageList);
    }

    private void playSoundInThread(int sound) {
        Observable.create(emitter -> SoundPlayerManager.getInstance().playSound(sound, 0)).compose(RxUtils.ioToMain()).subscribe();
    }

    public String getRecognizeResult(TablePerson tablePerson, TableSettingConfigInfo settingConfigInfo) {
        String personName = tablePerson == null ? "" : tablePerson.personName;
        String personInfoNo = (tablePerson == null || TextUtils.isEmpty(tablePerson.personInfoNo)) ? "" :
                tablePerson.personInfoNo;
        String strFaceResult = "";
        int displayMode = settingConfigInfo.getDisplayMode();
        if (displayMode == ConfigConstants.DISPLAY_MODE_SUCCESS_NAME) {
            strFaceResult = personName;
        } else if (displayMode == ConfigConstants.DISPLAY_MODE_HIDE_LAST_CHAR) {
            strFaceResult = personName.substring(0, personName.length() - 1) + "＊";
        } else {
            String faceResultCustom = settingConfigInfo.getCustomDisplayModeFormat();
            if (!TextUtils.isEmpty(faceResultCustom)) {
                strFaceResult = StringUtils.getRecognitionShowCustom(faceResultCustom.trim(), personName, personInfoNo);
            }
        }
        return strFaceResult;
    }

    public String getFaceHeadUri(TablePerson tablePerson, TablePersonFace tablePersonFace) {
        String mainFaceId = tablePerson.mainFaceId;
        if (!TextUtils.isEmpty(mainFaceId)) {
            tablePersonFace = PersonFaceDao.getInstance().getPersonFaceByFaceId(mainFaceId);
        }
        return tablePersonFace.imagePath;
    }

    public String getRecognizeResult(TableSettingConfigInfo settingConfigInfo) {
        int failedShowMode = settingConfigInfo.getDisplayModeFail();
        String strFaceResult = "";
        if (failedShowMode == ConfigConstants.DISPLAY_MODE_FAILED_DEFAULT_MARKUP) {
            strFaceResult = CommonUtils.getStrFromRes(R.string.recognition_failed);
        } else if (failedShowMode == ConfigConstants.DISPLAY_MODE_FAILED_CUSTOM) {
            strFaceResult = settingConfigInfo.getCustomFailDisplayModeFormat();
        }
        return strFaceResult;
    }

    /**
     * 判断是否有通行权限
     *
     * @param tablePerson
     * @return
     */
    public boolean doorAuthorityWithTime(TablePerson tablePerson, List<TablePersonPermission> permissions) {
        if (null == tablePerson) {
            return false;
        }
        long time = System.currentTimeMillis();
        if (permissions != null && permissions.size() > 0) {
            boolean permissionSuccess = false;
            String currentDate = TimeUtils.millis2String(time, TimeUtils.DATE_PATTERN_3);
            String currentTime = TimeUtils.millis2String(time, TimeUtils.DATE_PATTERN_12);
            for (TablePersonPermission permission : permissions) {
                String dateStart = permission.getStartDate();
                String dateEnd = permission.getEndDate();
                String workingDays = permission.getWorkingDays();
                String timeAndDesc = permission.getTimeAndDesc();
                boolean compareDate = permissionDate(dateStart, currentDate, dateEnd);
                boolean compareWeek = permissionWeek(workingDays, time);
                boolean compareTime = permissionTime(timeAndDesc, currentTime);
                if (compareDate && compareWeek && compareTime) {
                    permissionSuccess = true;
                    break;
                }
            }
            return permissionSuccess;
        } else {
            String strCurrentTime = TimeUtils.getStringCurrentTime();
            String morningStartTime = tablePerson.authMorningStartTime;
            String morningEndTime = tablePerson.authMorningEndTime;
            String noonStartTime = tablePerson.authNoonStartTime;
            String noonEndTime = tablePerson.authNoonEndTime;
            String nightStartTime = tablePerson.authNightStartTime;
            String nightEndTime = tablePerson.authNightEndTime;
            return CommonUtils.permissionTimeCompare(strCurrentTime, morningStartTime, morningEndTime) ||
                    CommonUtils.permissionTimeCompare(strCurrentTime, noonStartTime, noonEndTime) ||
                    CommonUtils.permissionTimeCompare(strCurrentTime, nightStartTime, nightEndTime);
        }
    }

    /**
     * 比较日期权限
     */
    public boolean permissionDate(String date1, String date2, String date3) {
        boolean compareSuccess1;
        boolean compareSuccess2;
        if (TextUtils.isEmpty(date1)) {
            compareSuccess1 = true;
        } else {
            compareSuccess1 = TimeUtils.compareDate2(date1, date2);
        }
        if (TextUtils.isEmpty(date3)) {
            compareSuccess2 = true;
        } else {
            compareSuccess2 = TimeUtils.compareDate2(date2, date3);
        }
        return compareSuccess1 && compareSuccess2;
    }

    /**
     * 比对周期权限
     */
    private boolean permissionWeek(String workingDay, long time) {
        if (ServerConstants.WEEK_NO_PERMISSION.equals(workingDay)) {
            return false;
        }
        String dayOfWeek = TimeUtils.getDayOfWeek();
        return workingDay.contains(dayOfWeek);
    }

    /**
     * 比对时间权限
     */
    private boolean permissionTime(String timeAndDesc, String currentTime) {
        List<RequestDoorAuthorityV2.DoorAuthorityDetail.TimeAuthority> timeAuthorities =
                new Gson().fromJson(timeAndDesc, new TypeToken<List<RequestDoorAuthorityV2.DoorAuthorityDetail.TimeAuthority>>() {
                }.getType());
        boolean permissionSuccess = false;
        for (RequestDoorAuthorityV2.DoorAuthorityDetail.TimeAuthority timeAuthority : timeAuthorities) {
            String timeStart = timeAuthority.getStartTime();
            String timeEnd = timeAuthority.getEndTime();
            permissionSuccess |= CommonUtils.permissionTimeCompare(currentTime, timeStart, timeEnd);
        }
        return permissionSuccess;
    }

    public CameraViewParam getCameraParam(boolean mainCamera, DeviceAdaptationInfo adaptationInfo) {
        CameraViewParam cameraParam = new CameraViewParam();
        cameraParam.setCameraId(mainCamera ? adaptationInfo.getMainCameraId() : adaptationInfo.getSecondCameraId());
        cameraParam.setPreviewWidth(adaptationInfo.getPreviewWidth());
        cameraParam.setPreviewHeight(adaptationInfo.getPreviewHeight());
        cameraParam.setPreviewFormat(17);
        cameraParam.setUsePreAlloc(mainCamera);
        cameraParam.setFaceRectColor(mainCamera ? RECT_BORDER_COLOR : Color.YELLOW);
        cameraParam.setMainViewMirror(mainCamera ? adaptationInfo.isMainCameraMirror() : adaptationInfo.isSecondCameraMirror());
        cameraParam.setScaleType(CameraViewConstants.TYPE_DISPLAY_NORMAL);
        cameraParam.setMainViewAdditionalRotation(mainCamera ? adaptationInfo.getMainCameraRotation() : adaptationInfo.getSecondCameraRotation());
        cameraParam.setFaceRectMirrorVertical(mainCamera ? adaptationInfo.isRectVerticalMirror() : adaptationInfo.isSecondRectVerticalMirror());
        cameraParam.setFaceRectMirrorHorizontal(mainCamera ? adaptationInfo.isRectHorizontalMirror() : adaptationInfo.isSecondRectHorizontalMirror());
        return cameraParam;
    }

    public void clearDisposableMap(@NonNull ConcurrentHashMap<Integer, Disposable> map) {
        for (Integer faceId : map.keySet()) {
            Disposable disposable = map.get(faceId);
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
            }
        }
        map.clear();
    }

    /**
     * 取消map集合中的disposable请求
     */
    public void cancelDisposableFromMapById(@NonNull ConcurrentHashMap<Integer, Disposable> map, int newFaceId) {
        for (Integer oldFaceId : map.keySet()) {
            if (!oldFaceId.equals(newFaceId)) {
                Disposable disposable = map.get(oldFaceId);
                if (disposable != null && !disposable.isDisposed()) {
                    disposable.dispose();
                }
                map.remove(oldFaceId);
            }
        }
    }

    public void removeFromMapById(@NonNull Map<Integer, Integer> map, int newFaceId) {
        for (Integer oldFaceId : map.keySet()) {
            if (!oldFaceId.equals(newFaceId)) {
                map.remove(oldFaceId);
            }
        }
    }

    public void transferIrFaceInfoList(Rect rect, DeviceAdaptationInfo adaptationInfo, List<FaceRectInfo> irInfoList) {
        int widthRect = rect.width();
        int heightRect = rect.height();
        int widthPadding = (int) (widthRect * 0.05);
        int heightPadding = (int) (heightRect * 0.05);
        Rect irRect = new Rect(rect.left - widthPadding, rect.top - heightPadding,
                rect.right + widthPadding, rect.bottom + heightPadding);
        irRect.left = (irRect.left + adaptationInfo.getHorizontalDisplacement());
        irRect.right = (irRect.right + adaptationInfo.getHorizontalDisplacement());
        irRect.top = (irRect.top + adaptationInfo.getVerticalDisplacement());
        irRect.bottom = (irRect.bottom + adaptationInfo.getVerticalDisplacement());
        irInfoList.add(new FaceRectInfo(irRect, "", RECT_BORDER_WIDTH / 2));
    }

    public Config getFaceEngineConfig(@NonNull DeviceAdaptationInfo adaptationInfo) {
        TableSettingConfigInfo configInfo = CommonRepository.getInstance().getSettingConfigInfo();
        float irLiveThreshold = Float.parseFloat(configInfo.getIrLiveThreshold());
        float fqThreshold = Float.parseFloat(configInfo.getFaceQualityThreshold());
        float fcThreshold = Float.parseFloat(configInfo.getSimilarThreshold());
        boolean successRetry = configInfo.getSuccessRetry() == ConfigConstants.RECOGNITION_SUCCESS_RETRY_OPEN;
        float successRetryTime = Float.parseFloat(configInfo.getSuccessRetryDelay());
        float failedRetryTime = Float.parseFloat(configInfo.getRecognitionRetryDelay());
        return CommonRepository.getInstance().getFaceEngineConfig(true,
                configInfo.getRecognizeDistance(), adaptationInfo.getFaceDetectDegree(),
                configInfo.getLiveDetectType(), fcThreshold, fqThreshold, irLiveThreshold,
                adaptationInfo.isRightGlSurfaceViewMirror(), adaptationInfo.getRightGlSurfaceViewRotation(),
                adaptationInfo.getHorizontalDisplacement(), adaptationInfo.getVerticalDisplacement(), successRetry,
                successRetryTime, failedRetryTime);
    }
}
