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

package com.arcsoft.arcfacesingle.server.api;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.app.BusinessErrorCode;
import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.business.common.CommonRepository;
import com.arcsoft.arcfacesingle.business.iccard.IcCardEntranceRepository;
import com.arcsoft.arcfacesingle.business.setting.ConfigConstants;
import com.arcsoft.arcfacesingle.data.db.dao.PersonDao;
import com.arcsoft.arcfacesingle.data.db.dao.PersonFaceDao;
import com.arcsoft.arcfacesingle.data.db.dao.PersonPermissionDao;
import com.arcsoft.arcfacesingle.data.db.table.TablePerson;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonFace;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonPermission;
import com.arcsoft.arcfacesingle.data.db.table.TableSettingConfigInfo;
import com.arcsoft.arcfacesingle.data.event.ChangeMainLogoEvent;
import com.arcsoft.arcfacesingle.data.event.ChangeSecondLogoEvent;
import com.arcsoft.arcfacesingle.data.event.CleanDataEvent;
import com.arcsoft.arcfacesingle.data.event.DisconnectEvent;
import com.arcsoft.arcfacesingle.data.event.DynamicAddPersonInfoEvent;
import com.arcsoft.arcfacesingle.data.event.DynamicDeletePersonInfoEvent;
import com.arcsoft.arcfacesingle.data.event.InstallPackageEvent;
import com.arcsoft.arcfacesingle.data.event.PersonAddEvent;
import com.arcsoft.arcfacesingle.data.event.PersonDeleteEvent;
import com.arcsoft.arcfacesingle.data.event.RefreshAdapterEvent;
import com.arcsoft.arcfacesingle.data.model.DeviceFingerPrintInfo;
import com.arcsoft.arcfacesingle.data.model.FaceExtractResult;
import com.arcsoft.arcfacesingle.libutil.ImageFileUtils;
import com.arcsoft.arcfacesingle.server.faceengine.FaceEngineManager;
import com.arcsoft.arcfacesingle.server.pojo.base.ResponseBase;
import com.arcsoft.arcfacesingle.server.pojo.base.ResponsePageBase;
import com.arcsoft.arcfacesingle.server.pojo.request.RequestConnect;
import com.arcsoft.arcfacesingle.server.pojo.request.RequestDisconnect;
import com.arcsoft.arcfacesingle.server.pojo.request.RequestDoorAuthority;
import com.arcsoft.arcfacesingle.server.pojo.request.RequestDoorAuthorityV2;
import com.arcsoft.arcfacesingle.server.pojo.request.RequestGetLogo;
import com.arcsoft.arcfacesingle.server.pojo.request.RequestGetSetting;
import com.arcsoft.arcfacesingle.server.pojo.request.RequestPackageAuthority;
import com.arcsoft.arcfacesingle.server.pojo.request.RequestPersonAdd;
import com.arcsoft.arcfacesingle.server.pojo.request.RequestPersonAddFace;
import com.arcsoft.arcfacesingle.server.pojo.request.RequestPersonDelete;
import com.arcsoft.arcfacesingle.server.pojo.request.RequestPersonList;
import com.arcsoft.arcfacesingle.server.pojo.request.RequestSetLogo;
import com.arcsoft.arcfacesingle.server.pojo.request.RequestSetting;
import com.arcsoft.arcfacesingle.server.pojo.request.RequestSystemTime;
import com.arcsoft.arcfacesingle.server.pojo.response.ResponseGetLogo;
import com.arcsoft.arcfacesingle.server.pojo.response.ResponseGetSerial;
import com.arcsoft.arcfacesingle.server.pojo.response.ResponseGetSetting;
import com.arcsoft.arcfacesingle.server.pojo.response.ResponsePersonAddFace;
import com.arcsoft.arcfacesingle.server.pojo.response.ResponsePersonFace;
import com.arcsoft.arcfacesingle.server.pojo.response.ResponsePersonList;
import com.arcsoft.arcfacesingle.server.pojo.response.ResponsePersonUpdate;
import com.arcsoft.arcfacesingle.service.OfflineLanService;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.arcfacesingle.util.gson.CustomGsonTypeAdapter;
import com.arcsoft.arcfacesingle.view.activity.PersonDetailActivity;
import com.arcsoft.arcfacesingle.view.activity.PersonListActivity;
import com.arcsoft.arcfacesingle.view.activity.RecognizeActivity;
import com.arcsoft.asg.libcommon.util.common.ActivityUtils;
import com.arcsoft.asg.libcommon.util.common.AppUtils;
import com.arcsoft.asg.libcommon.util.common.ArithmeticUtils;
import com.arcsoft.asg.libcommon.util.common.DeviceUtils;
import com.arcsoft.asg.libcommon.util.common.FileUtils;
import com.arcsoft.asg.libcommon.util.common.Md5Utils;
import com.arcsoft.asg.libcommon.util.common.PermissionUtils;
import com.arcsoft.asg.libcommon.util.common.SPUtils;
import com.arcsoft.asg.libcommon.util.common.StringUtils;
import com.arcsoft.asg.libcommon.util.common.TimeUtils;
import com.arcsoft.asg.libcommon.util.common.Utils;
import com.arcsoft.asg.libnetwork.bean.NetworkException;
import com.arcsoft.asg.libnetwork.manage.DefaultRemoteApiManager;
import com.arcsoft.faceengine.Config;
import com.arcsoft.faceengine.ErrorInfo;
import com.arcsoft.faceengine.FaceInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.raizlabs.android.dbflow.sql.saveable.ModelSaver;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocalHttpApiDataManager {

    private static final String TAG = LocalHttpApiDataManager.class.getSimpleName();
    private static volatile LocalHttpApiDataManager mInstance;
    private static final Object LOCK = new Object();

    public int installVersionCode = -1;
    public boolean isInstall = false;

    private LocalHttpApiDataManager() {
    }

    public static LocalHttpApiDataManager getInstance() {
        if (null == mInstance) {
            synchronized (LOCK) {
                if (null == mInstance) {
                    mInstance = new LocalHttpApiDataManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 获取设备指纹信息
     *
     * @param
     * @return
     */
    @SuppressLint("MissingPermission")
    public String getFingerprintInfo() {
        TableSettingConfigInfo configInfo = CommonRepository.getInstance().getSettingConfigInfo();
        DeviceFingerPrintInfo printInfo = new DeviceFingerPrintInfo();
        printInfo.setClientIp(configInfo.getDeviceIp());
        printInfo.setClientPort(configInfo.getDevicePort());
        printInfo.setMacAddress(DeviceUtils.getMacAddress());
        printInfo.setSerialNumber(DeviceUtils.getSerial());
        printInfo.setDeviceName(configInfo.getDeviceName());
        printInfo.setVersionCode(AppUtils.getAppVersionCode());
        printInfo.setVersionName(AppUtils.getAppVersionName());
        return LocalHttpApiDataUtils.getResponseStringSuccess(printInfo);
    }

    /**
     * 设备连接
     *
     * @param requestData
     * @return
     */
    @SuppressLint("MissingPermission")
    public String connect(String requestData, String netSign) {
        RequestConnect requestConnect = null;
        try {
            requestConnect = new Gson().fromJson(requestData, RequestConnect.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null == requestConnect) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_JSON_INVALID, ServerConstants.MSG_RESPONSE_JSON_INVALID);
        }
        String signKey = requestConnect.getSignKey();
        if (TextUtils.isEmpty(signKey)) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_SIGN_KEY_INVALID, ServerConstants.MSG_RESPONSE_PARAM_SIGN_KEY_INVALID);
        }
        String localSign = Md5Utils.encode(requestData + signKey);
        if (!localSign.equals(netSign)) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_SIGN_CHECK_FAILED, ServerConstants.MSG_RESPONSE_SIGN_FAILED);
        }
        String serialNumber = requestConnect.getSerialNumber();
        if (TextUtils.isEmpty(serialNumber)) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_SN_INVALID, ServerConstants.MSG_RESPONSE_PARAM_SN_INVALID);
        }
        TableSettingConfigInfo settingConfigInfo = CommonRepository.getInstance().getSettingConfigInfo();
        String localSerial = DeviceUtils.getSerial().trim().replaceAll(" ", "");
        if (!localSerial.equals(serialNumber)) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_DEVICE_SN_EQUAL_FAILED, ServerConstants.MSG_RESPONSE_SERIAL_NUMBER_EQUAL_FAILED);
        }
        String netMacAddress = requestConnect.getMacAddress();
        if (TextUtils.isEmpty(netMacAddress)) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_MAC_ADDRESS_INVALID, ServerConstants.MSG_RESPONSE_PARAM_MAC_ADDRESS_INVALID);
        } else {
            netMacAddress = netMacAddress.toUpperCase();
        }
        String localMacAddress = DeviceUtils.getMacAddress();
        if (TextUtils.isEmpty(localMacAddress)) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_DEVICE_MAC_EMPTY, ServerConstants.MSG_RESPONSE_DEVICE_MAC_ADDRESS_EMPTY);
        }
        if (!localMacAddress.equals(netMacAddress)) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_DEVICE_MAC_EQUAL_FAILED, ServerConstants.MSG_RESPONSE_MAC_ADDRESS_EQUAL_FAILED);
        }
        String netIp = requestConnect.getIp();
        if (TextUtils.isEmpty(netIp)) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_IP_INVALID, ServerConstants.MSG_RESPONSE_PARAM_IP_INVALID);
        }
        if (!CommonUtils.checkAddress(netIp)) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_SERVER_IP_INVALID, ServerConstants.MSG_RESPONSE_SERVER_IP_INVALID);
        }
        int netPort = requestConnect.getPort();
        if (netPort < ServerConstants.NUMBER_PORT_MIN || netPort > ServerConstants.NUMBER_PORT_MAX) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_SERVER_PORT_INVALID, ServerConstants.MSG_RESPONSE_SERVER_PORT_INVALID);
        }
        int deviceId = requestConnect.getDeviceId();
        if (!TextUtils.isEmpty(settingConfigInfo.getSignKey())) {
            if (deviceId != settingConfigInfo.getDeviceId() || !signKey.equals(settingConfigInfo.getSignKey())) {
                return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_CONNECT_FAILED, ServerConstants.MSG_RESPONSE_CONNECT_FAILED);
            }
        }
        int signType = requestConnect.getSignType();
        if (signType != ConfigConstants.TYPE_SIGN_IN && signType != ConfigConstants.TYPE_SIGN_OUT &&
                signType != ConfigConstants.TYPE_SIGN_BOTH) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PARAM_SIGN_TYPE_INVALID, ServerConstants.MSG_RESPONSE_PARAM_SIGN_TYPE_INVALID);
        }
        settingConfigInfo.setSignType(signType);
        settingConfigInfo.setSignKey(signKey);
        settingConfigInfo.setServerPort(String.valueOf(netPort));
        settingConfigInfo.setServerIp(netIp);
        settingConfigInfo.setDeviceId(deviceId);

        CommonRepository.getInstance().saveSettingConfigAsync(settingConfigInfo, null);
        try {
            DefaultRemoteApiManager.getInstance().unInit();
            String httpUrl = "http://" + netIp + ":" + netPort + "/";
            DefaultRemoteApiManager.getInstance().init(httpUrl);
        } catch (NetworkException e) {
            e.printStackTrace();
        }
        EventBus.getDefault().post(new DisconnectEvent(true, netIp));

        return LocalHttpApiDataUtils.getResponseStringSuccess();
    }

    /**
     * 添加人员
     *
     * @return
     */
    public String personAdd(String requestData) {
        if (FileUtils.getSdcardAvailableSize() < Constants.SDCARD_STORAGE_SIZE_DELETE) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_DEVICE_NOT_ENOUGH_STORAGE,
                    ServerConstants.MSG_RESPONSE_DEVICE_SDCARD_STORAGE_LESS_MIN);
        }
        RequestPersonAdd requestPerson = null;
        try {
            requestPerson = new Gson().fromJson(requestData, RequestPersonAdd.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null == requestPerson) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_JSON_INVALID, ServerConstants.MSG_RESPONSE_JSON_INVALID);
        }
        if (TextUtils.isEmpty(requestPerson.getPersonSerial())) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PARAM_PERSON_SERIAL_INVALID, ServerConstants.MSG_RESPONSE_PARAM_PERSON_SERIAL_EMPTY);
        }
        if (TextUtils.isEmpty(requestPerson.getPersonName())) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PARAM_PERSON_NAME_INVALID, ServerConstants.MSG_RESPONSE_PARAM_PERSON_NAME_EMPTY);
        }
        if (requestPerson.getTotal() > ServerConstants.SHOW_WARN_DIALOG_MAX_FACE_LIMIT) {
            String topClassName = ActivityUtils.getTopActivity().getClass().getSimpleName();
            if (topClassName.equals(RecognizeActivity.class.getSimpleName())) {
                EventBus.getDefault().post(new PersonAddEvent(requestPerson.getTotal(), requestPerson.getCurrent()));
            }
        }
        String personSerial = requestPerson.getPersonSerial();
        int personInfoType = requestPerson.getPersonInfoType();
        TablePerson localPerson = PersonDao.getInstance().getPersonByPersonSerial(personSerial);
        if (null != localPerson) {
            localPerson.updateTime = System.currentTimeMillis();
            String personName = requestPerson.getPersonName();
            if (!TextUtils.isEmpty(personName)) {
                localPerson.personName = personName.replaceAll("'", "");
            }
            String personIdentifier = requestPerson.getPersonIdentifier();
            if (!TextUtils.isEmpty(personIdentifier)) {
                localPerson.personInfoNo = personIdentifier.replaceAll("'", "");
            }
            localPerson.personInfoType = personInfoType;
            if (personInfoType == PersonDao.TYPE_PERSON_INFO_ONLY_IC_CARD) {
                //只有卡号，删本地人脸，更新卡号
                String strIcCardNo = requestPerson.getIcCardNo();
                if (TextUtils.isEmpty(strIcCardNo)) {
                    return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PARAM_IC_INVALID, ServerConstants.MSG_RESPONSE_PARAM_IC_CARD_INVALID);
                }
                if (strIcCardNo.length() > ServerConstants.IC_CARD_NO_MAX_LENGTH) {
                    return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_IC_CARD_LENGTH_INVALID, ServerConstants.MSG_IC_CARD_NO_LENGTH_INVALID);
                }
                if (!StringUtils.matcherPassword(strIcCardNo)) {
                    return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_IC_CARD_FORMAT_INVALID, ServerConstants.MSG_IC_CARD_NO_FORMAT_INVALID);
                }
                localPerson.icCardNo = strIcCardNo;
                TablePersonFace tablePersonFace = PersonFaceDao.getInstance().getPersonFaceBySerial(personSerial);
                if (tablePersonFace != null) {
                    //删本地人脸
                    if (!PersonFaceDao.getInstance().deletePersonFace(tablePersonFace)) {
                        return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PERSON_UPDATE_FAILED, ServerConstants.MSG_RESPONSE_PERSON_UPDATE_FAILED);
                    }
                    FileUtils.deleteFile(tablePersonFace.imagePath);
                }
                if (PersonDao.getInstance().updatePerson(localPerson)) {
                    //更新卡号成功
                    String topClassName = ActivityUtils.getTopActivity().getClass().getSimpleName();
                    if (topClassName.equals(PersonListActivity.class.getSimpleName()) ||
                            topClassName.equals(PersonDetailActivity.class.getSimpleName())) {
                        EventBus.getDefault().post(new RefreshAdapterEvent());
                    }
                    IcCardEntranceRepository.getInstance().reloadPersonList();
                    return LocalHttpApiDataUtils.getResponseStringSuccess(ServerConstants.MSG_RESPONSE_PERSON_UPDATE_SUCCESS,
                            createResponseUpdate(localPerson.personName, personSerial));
                } else {
                    return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PERSON_UPDATE_FAILED, ServerConstants.MSG_RESPONSE_PERSON_UPDATE_FAILED);
                }
            } else if (personInfoType == PersonDao.TYPE_PERSON_INFO_ONLY_FACE) {
                //只有人脸，更新本地人脸，删除卡号
                localPerson.icCardNo = "";
                return addOrUpdatePerson(personSerial, requestPerson, localPerson, false);
            } else if (personInfoType == PersonDao.TYPE_PERSON_INFO_BOTH) {
                //有人脸和卡号，更新本地人脸，更新卡号
                String strIcCardNo = requestPerson.getIcCardNo();
                if (TextUtils.isEmpty(strIcCardNo)) {
                    return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PARAM_IC_INVALID, ServerConstants.MSG_RESPONSE_PARAM_IC_CARD_INVALID);
                }
                if (strIcCardNo.length() > ServerConstants.IC_CARD_NO_MAX_LENGTH) {
                    return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_IC_CARD_LENGTH_INVALID, ServerConstants.MSG_IC_CARD_NO_LENGTH_INVALID);
                }
                if (!StringUtils.matcherPassword(strIcCardNo)) {
                    return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_IC_CARD_FORMAT_INVALID, ServerConstants.MSG_IC_CARD_NO_FORMAT_INVALID);
                }
                localPerson.icCardNo = strIcCardNo;
                return addOrUpdatePerson(personSerial, requestPerson, localPerson, false);
            } else {
                return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PARAM_PERSON_INFO_TYPE_INVALID,
                        ServerConstants.MSG_RESPONSE_PERSON_TYPE_INVALID);
            }
        } else {
            TablePerson tablePerson = new TablePerson();
            tablePerson.personSerial = personSerial;
            String personName = requestPerson.getPersonName();
            if (!TextUtils.isEmpty(personName)) {
                tablePerson.personName = personName.replaceAll("'", "");
            }
            String personIdentifier = requestPerson.getPersonIdentifier();
            if (!TextUtils.isEmpty(personIdentifier)) {
                tablePerson.personInfoNo = personIdentifier.replaceAll("'", "");
            }
            tablePerson.addTime = System.currentTimeMillis();
            tablePerson.updateTime = tablePerson.addTime;
            tablePerson.personInfoType = personInfoType;
            tablePerson.authMorningStartTime = ServerConstants.DEFAULT_START_TIME;
            tablePerson.authMorningEndTime = ServerConstants.DEFAULT_END_TIME;
            tablePerson.authNoonStartTime = ServerConstants.DEFAULT_START_TIME;
            tablePerson.authNoonEndTime = ServerConstants.DEFAULT_END_TIME;
            tablePerson.authNightStartTime = ServerConstants.DEFAULT_START_TIME;
            tablePerson.authNightEndTime = ServerConstants.DEFAULT_END_TIME;
            Gson gson = new Gson();
            if (personInfoType == PersonDao.TYPE_PERSON_INFO_ONLY_IC_CARD) {
                //只有卡号，无需新增人脸，更新卡号
                String strIcCardNo = requestPerson.getIcCardNo();
                if (TextUtils.isEmpty(strIcCardNo)) {
                    return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PARAM_IC_INVALID, ServerConstants.MSG_RESPONSE_PARAM_IC_CARD_INVALID);
                }
                if (strIcCardNo.length() > ServerConstants.IC_CARD_NO_MAX_LENGTH) {
                    return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_IC_CARD_LENGTH_INVALID, ServerConstants.MSG_IC_CARD_NO_LENGTH_INVALID);
                }
                if (!StringUtils.matcherPassword(strIcCardNo)) {
                    return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_IC_CARD_FORMAT_INVALID, ServerConstants.MSG_IC_CARD_NO_FORMAT_INVALID);
                }
                tablePerson.icCardNo = strIcCardNo;
                TablePersonPermission localPermission = LocalHttpApiDataUtils.createNewPersonPermission(tablePerson, gson);
                if (!PersonPermissionDao.getInstance().addModel(localPermission)) {
                    return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PERSON_UPDATE_FAILED, ServerConstants.MSG_RESPONSE_PERSON_UPDATE_FAILED);
                }
                if (!PersonDao.getInstance().addPerson(tablePerson)) {
                    return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PERSON_UPDATE_FAILED, ServerConstants.MSG_RESPONSE_PERSON_UPDATE_FAILED);
                }
                String topClassName = ActivityUtils.getTopActivity().getClass().getSimpleName();
                if (topClassName.equals(PersonListActivity.class.getSimpleName()) ||
                        topClassName.equals(PersonDetailActivity.class.getSimpleName())) {
                    EventBus.getDefault().post(new RefreshAdapterEvent());
                }
                IcCardEntranceRepository.getInstance().reloadPersonList();
                String stringReason = ServerConstants.MSG_PARAM_SUCCESS;
                return LocalHttpApiDataUtils.getResponseStringSuccess(ServerConstants.MSG_RESPONSE_PERSON_UPDATE_SUCCESS,
                        createResponseUpdateSuccess(stringReason, "", requestPerson.getPersonName(), personSerial));
            } else if (personInfoType == PersonDao.TYPE_PERSON_INFO_ONLY_FACE) {
                //只有人脸，添加本地人脸
                tablePerson.icCardNo = "";
                return addOrUpdatePerson(personSerial, requestPerson, tablePerson, true);
            } else if (personInfoType == PersonDao.TYPE_PERSON_INFO_BOTH) {
                //有人脸和卡号，更新本地人脸，更新卡号
                String strIcCardNo = requestPerson.getIcCardNo();
                if (TextUtils.isEmpty(strIcCardNo)) {
                    return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PARAM_IC_INVALID, ServerConstants.MSG_RESPONSE_PARAM_IC_CARD_INVALID);
                }
                if (strIcCardNo.length() > ServerConstants.IC_CARD_NO_MAX_LENGTH) {
                    return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_IC_CARD_LENGTH_INVALID, ServerConstants.MSG_IC_CARD_NO_LENGTH_INVALID);
                }
                if (!StringUtils.matcherPassword(strIcCardNo)) {
                    return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_IC_CARD_FORMAT_INVALID, ServerConstants.MSG_IC_CARD_NO_FORMAT_INVALID);
                }
                tablePerson.icCardNo = strIcCardNo;
                return addOrUpdatePerson(personSerial, requestPerson, tablePerson, true);
            } else {
                return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PARAM_PERSON_INFO_TYPE_INVALID,
                        ServerConstants.MSG_RESPONSE_PERSON_TYPE_INVALID);
            }
        }
    }

    /**
     * 更新或新增人员
     */
    private String addOrUpdatePerson(String personSerial, RequestPersonAdd requestPerson, TablePerson localPerson, boolean addPerson) {
        List<RequestPersonAddFace> faceList = requestPerson.getFaceList();
        if (faceList == null || faceList.isEmpty()) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PARAM_FACE_LIST_INVALID, ServerConstants.MSG_RESPONSE_PARAM_FACE_LIST_EMPTY);
        }
        RequestPersonAddFace face = faceList.get(0);
        if (face == null) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PARAM_FACE_LIST_INVALID, ServerConstants.MSG_RESPONSE_PARAM_FACE_LIST_EMPTY);
        }
        String imageName = face.getImageName();
        if (TextUtils.isEmpty(imageName)) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PARAM_IMAGE_NAME_INVALID, ServerConstants.MSG_RESPONSE_PARAM_IMAGE_NAME_INVALID);
        }
        String imageBase64 = face.getImageBase64();
        if (TextUtils.isEmpty(imageBase64)) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PARAM_IMAGE_BASE64_INVALID, ServerConstants.MSG_RESPONSE_PARAM_IMAGE_BASE64_INVALID);
        }
        String imageMD5 = face.getImageMD5();
        if (TextUtils.isEmpty(imageMD5)) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PARAM_IMAGE_MD5_INVALID, ServerConstants.MSG_RESPONSE_PARAM_IMAGE_MD5_INVALID);
        }
        if (addPerson) {
            //本地不存在人脸，新增人脸
            return processImageByEngine(face, localPerson, null, personSerial, true);
        } else {
            TablePersonFace personFacesFromDb = PersonFaceDao.getInstance().getPersonFaceBySerial(personSerial);
            if (personFacesFromDb == null) {
                //本地不存在人脸，新增人脸
                return processImageByEngine(face, localPerson, null, personSerial, true);
            } else {
                //本地存在该人脸
                if (imageMD5.equals(personFacesFromDb.imageMD5)) {
                    //人脸照片未发生变化，无需更新人脸
                    if (PersonDao.getInstance().updatePerson(localPerson)) {
                        String topClassName = ActivityUtils.getTopActivity().getClass().getSimpleName();
                        if (topClassName.equals(PersonListActivity.class.getSimpleName()) || topClassName.equals(PersonDetailActivity.class.getSimpleName())) {
                            EventBus.getDefault().post(new RefreshAdapterEvent());
                        }
                        IcCardEntranceRepository.getInstance().reloadPersonList();
                        String stringReason = ServerConstants.MSG_PARAM_SUCCESS;
                        return LocalHttpApiDataUtils.getResponseStringSuccess(ServerConstants.MSG_RESPONSE_PERSON_UPDATE_SUCCESS, createResponseUpdateSuccess(stringReason, imageName, localPerson.personName, personSerial));
                    } else {
                        return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PERSON_UPDATE_FAILED, ServerConstants.MSG_RESPONSE_PERSON_UPDATE_FAILED);
                    }
                } else {
                    //人脸照片发生变化，需要更新人脸
                    return processImageByEngine(face, localPerson, personFacesFromDb, personSerial, false);
                }
            }
        }
    }

    /**
     * 使用引擎处理图片
     */
    private String processImageByEngine(RequestPersonAddFace face, TablePerson localPerson, TablePersonFace tablePersonFace, String personSerial, boolean addPerson) {
        FaceEngineManager faceEngineManager = new FaceEngineManager();
        faceEngineManager.createFaceEngine();
        int ret = faceEngineManager.initFaceEngine();
        if (ret != ErrorInfo.MOK) {
            String errorMsg = CommonUtils.getStrFromRes(R.string.face_engine_init_fail);
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_FACE_ENGINE_ERROR, errorMsg);
        }
        String imageBase64 = face.getImageBase64();
        String imageMd5 = face.getImageMD5();
        String imageName = face.getImageName();
        String localPersonName = localPerson.personName;
        Bitmap serverBmp = ImageFileUtils.base64ToBitmap(imageBase64);
        if (serverBmp == null) {
            faceEngineManager.unInitFaceEngine();
            String failedReason = ServerConstants.MSG_RESPONSE_IMAGE_BASE_64_INVALID;
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_IMAGE_BASE_64_INVALID, failedReason, createResponseUpdate(failedReason, imageName, localPersonName, personSerial));
        }
        Bitmap oriBmp;
        if (serverBmp.getWidth() > Constants.FACE_REGISTER_MAX_WIDTH_1536) {
            oriBmp = serverBmp;
            int oriWidth = oriBmp.getWidth();
            int oriHeight = oriBmp.getHeight();
            int newWidth = Constants.FACE_REGISTER_MAX_WIDTH;
            int newHeight = (int) ArithmeticUtils.div(newWidth, ArithmeticUtils.div(oriWidth, oriHeight)) & ~1;
            oriBmp = ImageFileUtils.zoomImg(oriBmp, newWidth, newHeight);
            if (!serverBmp.isRecycled()) {
                serverBmp.recycle();
            }
        } else {
            oriBmp = serverBmp;
        }
        FaceExtractResult faceResult = faceEngineManager.extract(oriBmp);
        int resultCode = faceResult.getResult();
        if (resultCode == BusinessErrorCode.BEC_FACE_MANAGER_NO_FACE) {
            faceEngineManager.unInitFaceEngine();
            String failedReason = CommonUtils.getStrFromRes(R.string.face_manager_tip_detect_no_face_image);
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PERSON_NO_FACE, failedReason, createResponseUpdate(failedReason, imageName, localPersonName, personSerial));
        }
        if (resultCode == BusinessErrorCode.BEC_FACE_MANAGER_MORE_THAN_ONE_FACE) {
            faceEngineManager.unInitFaceEngine();
            String failedReason = CommonUtils.getStrFromRes(R.string.face_manager_tip_more_than_one_face);
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PERSON_MULTIPLE_FACE, failedReason, createResponseUpdate(failedReason, imageName, localPersonName, personSerial));
        }
        if (resultCode == BusinessErrorCode.BEC_FACE_MANAGER_FACE_QUALITY_FAIL) {
            faceEngineManager.unInitFaceEngine();
            String failedReason = CommonUtils.getStrFromRes(R.string.face_quality_fail);
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PERSON_LOW_IMAGE_QUALITY, failedReason, createResponseUpdate(failedReason, imageName, localPersonName, personSerial));
        }
        if (resultCode == BusinessErrorCode.BEC_FACE_MANAGER_RECOGNIZE_FAIL) {
            faceEngineManager.unInitFaceEngine();
            String failedReason = CommonUtils.getStrFromRes(R.string.face_manager_tip_recognize_fail);
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PERSON_EXTRACT_FEATURE_FAILED, failedReason, createResponseUpdate(failedReason, imageName, localPersonName, personSerial));
        }
        if (resultCode != BusinessErrorCode.BEC_COMMON_OK) {
            faceEngineManager.unInitFaceEngine();
            String failedReason = CommonUtils.getStrFromRes(R.string.face_engine_init_fail_error_code, resultCode);
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_FACE_ENGINE_ERROR, failedReason, createResponseUpdate(failedReason, imageName, localPersonName, personSerial));
        }
        faceEngineManager.unInitFaceEngine();
        return savePersonToLocal(addPerson, localPerson, tablePersonFace, personSerial, localPersonName, imageName,
                imageMd5, faceResult.getFaceInfo(), oriBmp);
    }

    /**
     * 保存人员照片至本地
     */
    private String savePersonToLocal(boolean addPerson, TablePerson localPerson, TablePersonFace tablePersonFace, String personSerial,
                                     String localPersonName, String imageName, String imageMd5,
                                     FaceInfo faceInfo, Bitmap bitmap) {
        String oldImagePath = null;
        if (addPerson) {
            tablePersonFace = new TablePersonFace();
        } else {
            oldImagePath = tablePersonFace.imagePath;
        }
        tablePersonFace.personSerial = personSerial;
        tablePersonFace.faceInfo = localPersonName;
        byte[] faceFeatureByte = faceInfo.feature;
        tablePersonFace.feature = faceFeatureByte;
        String newImagePath = CommonUtils.getPersonFaceLocalPath(imageName);
        tablePersonFace.imageMD5 = imageMd5;
        Bitmap saveBmp = ImageFileUtils.getFaceRegisterCropBitmap(faceInfo.faceRect, faceInfo.faceOrient, bitmap);
        if (saveBmp == null) {
            saveBmp = bitmap;
        } else if (!saveBmp.equals(bitmap)) {
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        if (!ImageFileUtils.save(saveBmp, newImagePath, Bitmap.CompressFormat.JPEG)) {
            if (!saveBmp.isRecycled()) {
                saveBmp.recycle();
            }
            String failedReason = ServerConstants.MSG_RESPONSE_PERSON_UPDATE_FAILED;
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PERSON_UPDATE_FAILED, failedReason, createResponseUpdate(failedReason, imageName, localPersonName, personSerial));
        }
        tablePersonFace.imagePath = newImagePath;
        tablePersonFace.addTime = System.currentTimeMillis();
        tablePersonFace.updateTime = tablePersonFace.addTime;
        tablePersonFace.featureVersion = Constants.FACE_FEATURE_VERSION_V30;
        if (addPerson) {
            TablePersonPermission personPermission = LocalHttpApiDataUtils.createNewPersonPermission(localPerson, new Gson());
            if (!PersonPermissionDao.getInstance().addModel(personPermission)) {
                String failedReason = ServerConstants.MSG_RESPONSE_PERSON_UPDATE_FAILED;
                return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PERSON_UPDATE_FAILED, failedReason, createResponseUpdate(failedReason, imageName, localPersonName, personSerial));
            }
        }
        long personFaceId;
        if (addPerson) {
            personFaceId = tablePersonFace.insert();
            if (personFaceId <= ModelSaver.INSERT_FAILED) {
                String failedReason = ServerConstants.MSG_RESPONSE_PERSON_UPDATE_FAILED;
                return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PERSON_UPDATE_FAILED, failedReason, createResponseUpdate(failedReason, imageName, localPersonName, personSerial));
            }
        } else {
            boolean updateSuccess = PersonFaceDao.getInstance().updatePersonFace(tablePersonFace);
            personFaceId = tablePersonFace.id;
            if (!updateSuccess) {
                String failedReason = ServerConstants.MSG_RESPONSE_PERSON_UPDATE_FAILED;
                return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PERSON_UPDATE_FAILED, failedReason, createResponseUpdate(failedReason, imageName, localPersonName, personSerial));
            }
        }
        if (addPerson ? !PersonDao.getInstance().addPerson(localPerson) : !PersonDao.getInstance().updatePerson(localPerson)) {
            String failedReason = ServerConstants.MSG_RESPONSE_PERSON_UPDATE_FAILED;
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PERSON_UPDATE_FAILED, failedReason, createResponseUpdate(failedReason, imageName, localPersonName, personSerial));
        }
        if (!addPerson && !newImagePath.equals(oldImagePath)) {
            FileUtils.delete(oldImagePath);
        }
        String topClassName = ActivityUtils.getTopActivity().getClass().getSimpleName();
        if (topClassName.equals(PersonListActivity.class.getSimpleName()) || topClassName.equals(PersonDetailActivity.class.getSimpleName())) {
            EventBus.getDefault().post(new RefreshAdapterEvent());
        } else if (topClassName.equals(RecognizeActivity.class.getSimpleName())) {
            EventBus.getDefault().post(new DynamicAddPersonInfoEvent(personFaceId, tablePersonFace.feature, addPerson));
        }
        IcCardEntranceRepository.getInstance().reloadPersonList();
        String stringReason = ServerConstants.MSG_PARAM_SUCCESS;
        return LocalHttpApiDataUtils.getResponseStringSuccess(ServerConstants.MSG_RESPONSE_PERSON_UPDATE_SUCCESS, createResponseUpdateSuccess(stringReason, imageName, localPersonName, personSerial, faceInfo.faceOrient));
    }

    /**
     * 人员删除
     *
     * @param requestData
     * @return
     */
    public void personDelete(AsyncHttpServerResponse response, String requestData) {
        RequestPersonDelete requestPersonDelete = null;
        try {
            requestPersonDelete = new Gson().fromJson(requestData, RequestPersonDelete.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null == requestPersonDelete) {
            response.send(LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_JSON_INVALID,
                    ServerConstants.MSG_RESPONSE_JSON_INVALID));
            return;
        }
        String netPersonSerial = requestPersonDelete.getPersonSerial();
        if (TextUtils.isEmpty(netPersonSerial)) {
            response.send(LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PARAM_PERSON_SERIAL_INVALID,
                    ServerConstants.MSG_RESPONSE_PARAM_PERSON_SERIAL_EMPTY));
            return;
        }
        TablePerson tablePerson = PersonDao.getInstance().getPersonByPersonSerial(netPersonSerial);
        if (null == tablePerson) {
            response.send(LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PERSON_NOT_EXIST,
                    ServerConstants.MSG_RESPONSE_PERSON_NOT_EXIST));
            return;
        }
        List<TablePersonPermission> permissions = PersonPermissionDao.getInstance().getListByPersonSerial(netPersonSerial);
        int total = requestPersonDelete.getTotal();
        if (total > ServerConstants.SHOW_WARN_DIALOG_MAX_FACE_LIMIT) {
            EventBus.getDefault().post(new PersonDeleteEvent(true));
        }
        PersonPermissionDao.getInstance().deleteListTransaction(permissions, new PersonPermissionDao.OnPermissionListener() {
            @Override
            public void onSuccess() {
                if (!PersonDao.getInstance().deleteByPersonSerial(netPersonSerial)) {
                    response.send(LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PERSON_DELETED_FAILED,
                            ServerConstants.MSG_RESPONSE_PERSON_DELETE_FAILED));
                }
                List<TablePersonFace> personFacesFromDb = PersonFaceDao.getInstance().getListByPersonSerial(netPersonSerial);
                List<Long> deletedId = new ArrayList<>(personFacesFromDb.size());
                if (personFacesFromDb.size() > 0) {
                    for (TablePersonFace tablePersonFace : personFacesFromDb) {
                        long id = tablePersonFace.id;
                        PersonFaceDao.getInstance().deletePersonFace(tablePersonFace);
                        deletedId.add(id);
                        String filePath = tablePersonFace.imagePath;
                        FileUtils.deleteFile(filePath);
                    }
                }
                String topClassName = ActivityUtils.getTopActivity().getClass().getSimpleName();
                if (topClassName.equals(PersonListActivity.class.getSimpleName()) || topClassName.equals(PersonDetailActivity.class.getSimpleName())) {
                    EventBus.getDefault().post(new RefreshAdapterEvent());
                } else if (topClassName.equals(RecognizeActivity.class.getSimpleName())) {
                    if (!deletedId.isEmpty()) {
                        EventBus.getDefault().post(new DynamicDeletePersonInfoEvent(deletedId));
                    }
                }
                IcCardEntranceRepository.getInstance().reloadPersonList();
                response.send(LocalHttpApiDataUtils.getResponseStringSuccess(ServerConstants.MSG_RESPONSE_PERSON_DELETE_SUCCESS));
            }

            @Override
            public void onError(String msg) {
                response.send(LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PERSON_DELETED_FAILED,
                        ServerConstants.MSG_RESPONSE_PERSON_DELETE_FAILED));
            }
        });
    }

    /**
     * 获取人员列表
     *
     * @param requestData
     * @return
     */
    public String personList(String requestData) {
        RequestPersonList list = null;
        try {
            list = new Gson().fromJson(requestData, RequestPersonList.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null == list) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_JSON_INVALID, ServerConstants.MSG_RESPONSE_JSON_INVALID);
        }
        if (list.getPageIndex() < 0) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PARAM_PAGE_INDEX_ERROR, ServerConstants.MSG_RESPONSE_PARAM_PAGE_INDEX_ERROR);
        } else if (list.getPageSize() <= 0) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PARAM_PAGE_SIZE_ERROR, ServerConstants.MSG_RESPONSE_PARAM_PAGE_SIZE_ERROR);
        } else if (list.getUpdateTime() < 0) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PARAM_UPDATE_TIME_ERROR, ServerConstants.MSG_RESPONSE_PARAM_UPDATE_TIME_ERROR);
        }
        int pageIndex = list.getPageIndex();
        int pageSize = list.getPageSize();
        long updateTime = list.getUpdateTime();
        int totalCount = (int) PersonDao.getInstance().getPersonFacePageCount(updateTime);
        if (totalCount == 0) {
            ResponsePageBase responsePageBase = new ResponsePageBase();
            responsePageBase.setPageSize(pageSize);
            responsePageBase.setPageIndex(pageIndex);
            responsePageBase.setTotalCount(0);
            responsePageBase.setPageCount(0);
            List<ResponsePersonList> responsePersonLists = new ArrayList<>();
            responsePageBase.setDataInfo(responsePersonLists);
            return LocalHttpApiDataUtils.getResponseStringSuccess(responsePageBase);
        }
        int pageCount = totalCount / pageSize;
        if (totalCount % pageSize > 0) {
            pageCount = pageCount + 1;
        }
        if (pageIndex >= pageCount) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PARAM_PAGE_INDEX_ERROR, ServerConstants.MSG_RESPONSE_PARAM_PAGE_INDEX_ERROR);
        }
        List<TablePerson> personList = PersonDao.getInstance().getPersonPage(pageSize, pageIndex, updateTime);
        ResponsePageBase responsePageBase = new ResponsePageBase();
        responsePageBase.setPageSize(pageSize);
        responsePageBase.setPageIndex(pageIndex);
        responsePageBase.setTotalCount(totalCount);
        responsePageBase.setPageCount(pageCount);
        List<ResponsePersonList> responsePersonLists = new ArrayList<>();
        for (TablePerson tablePerson : personList) {
            ResponsePersonList responsePersonList = new ResponsePersonList();
            responsePersonList.setPersonSerial(tablePerson.personSerial);
            responsePersonList.setPersonName(tablePerson.personName);
            responsePersonList.setAddTime(tablePerson.addTime);
            responsePersonList.setUpdateTime(tablePerson.updateTime);
            responsePersonList.setPersonIdentifier(tablePerson.personInfoNo);
            String icCardNo = tablePerson.icCardNo;
            if (TextUtils.isEmpty(icCardNo)) {
                responsePersonList.setIcCardNo("");
            } else {
                responsePersonList.setIcCardNo(icCardNo);
            }
            if (tablePerson.personInfoType == null) {
                responsePersonList.setPersonInfoType(PersonDao.TYPE_PERSON_INFO_ONLY_FACE);
            } else {
                responsePersonList.setPersonInfoType(tablePerson.personInfoType);
            }
            List<TablePersonFace> tablePersonList = PersonFaceDao.getInstance().getListByPersonSerial(tablePerson.personSerial);
            if (tablePersonList != null && tablePersonList.size() > 0) {
                List<ResponsePersonFace> faces = new ArrayList<>();
                for (TablePersonFace tablePersonFace : tablePersonList) {
                    ResponsePersonFace face = new ResponsePersonFace();
                    String imagePath = tablePersonFace.imagePath;
                    String imageName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
                    face.setImageName(imageName);
                    String imageBase64 = ImageFileUtils.image2Base64(tablePersonFace.imagePath);
                    face.setImageBase64(imageBase64);
                    String localMd5 = tablePersonFace.imageMD5;
                    if (TextUtils.isEmpty(localMd5)) {
                        localMd5 = Md5Utils.encode(imageBase64);
                    }
                    face.setImageMD5(localMd5);
                    faces.add(face);
                }
                responsePersonList.setFaceList(faces);
            }
            responsePersonLists.add(responsePersonList);
        }
        responsePageBase.setDataInfo(responsePersonLists);
        return LocalHttpApiDataUtils.getResponseStringSuccess(responsePageBase);
    }

    /**
     * 修改人员权限
     *
     * @param requestData
     * @return
     */
    public String doorAuthorityV1(String requestData) {
        RequestDoorAuthority doorAuthority = null;
        Gson gson = new Gson();
        try {
            doorAuthority = gson.fromJson(requestData, RequestDoorAuthority.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null == doorAuthority) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_JSON_INVALID, ServerConstants.MSG_RESPONSE_JSON_INVALID);
        }
        String personSerial = doorAuthority.getPersonSerial();
        if (TextUtils.isEmpty(personSerial)) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PARAM_PERSON_SERIAL_INVALID, ServerConstants.MSG_RESPONSE_PARAM_PERSON_SERIAL_EMPTY);
        }
        TablePerson tablePerson = PersonDao.getInstance().getPersonByPersonSerial(personSerial);
        if (null == tablePerson) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PERSON_NOT_EXIST, ServerConstants.MSG_RESPONSE_PERSON_NOT_EXIST);
        }
        if (!TextUtils.isEmpty(doorAuthority.getAuthorityName())) {
            tablePerson.doorAuthorityDetail = doorAuthority.getAuthorityName();
        }
        RequestDoorAuthorityV2.DoorAuthorityDetail.TimeAuthority morningTimeAuthority = null;
        RequestDoorAuthorityV2.DoorAuthorityDetail.TimeAuthority noonTimeAuthority = null;
        RequestDoorAuthorityV2.DoorAuthorityDetail.TimeAuthority nightTimeAuthority = null;
        TablePersonPermission localPermission = PersonPermissionDao.getInstance().getModelByPersonSerial(personSerial);
        boolean existPermission = localPermission != null;
        if (existPermission) {
            List<RequestDoorAuthorityV2.DoorAuthorityDetail.TimeAuthority> timeAuthorities = gson.fromJson(localPermission.getTimeAndDesc(),
                    new TypeToken<List<RequestDoorAuthorityV2.DoorAuthorityDetail.TimeAuthority>>() {
                    }.getType());
            for (RequestDoorAuthorityV2.DoorAuthorityDetail.TimeAuthority timeAuthority : timeAuthorities) {
                String timeDesc = timeAuthority.getTimeDesc();
                if (LocalHttpApiDataUtils.TAG_AUTHORITY_MORNING.equals(timeDesc)) {
                    morningTimeAuthority = timeAuthority;
                } else if (LocalHttpApiDataUtils.TAG_AUTHORITY_NOON.equals(timeDesc)) {
                    noonTimeAuthority = timeAuthority;
                } else if (LocalHttpApiDataUtils.TAG_AUTHORITY_NIGHT.equals(timeDesc)) {
                    nightTimeAuthority = timeAuthority;
                }
            }
        }
        String checkParam = LocalHttpApiDataUtils.checkAuthorityParam(doorAuthority, tablePerson, morningTimeAuthority,
                noonTimeAuthority, nightTimeAuthority);
        if (!TextUtils.isEmpty(checkParam)) {
            return checkParam;
        }
        tablePerson.updateTime = System.currentTimeMillis();
        if (!existPermission) {
            localPermission = LocalHttpApiDataUtils.createNewPersonPermission(false, tablePerson, gson);
        } else {
            List<RequestDoorAuthorityV2.DoorAuthorityDetail.TimeAuthority> timeAuthorities =
                    LocalHttpApiDataUtils.createTimeAuthorities(false, morningTimeAuthority, noonTimeAuthority, nightTimeAuthority,
                            doorAuthority, tablePerson);
            localPermission.setTimeAndDesc(gson.toJson(timeAuthorities));
        }
        if (existPermission ? PersonPermissionDao.getInstance().updateModel(localPermission) :
                PersonPermissionDao.getInstance().addModel(localPermission)) {
            return LocalHttpApiDataUtils.getResponseStringSuccess(ServerConstants.MSG_RESPONSE_PERSON_PERMISSION_SUCCESS);
        } else {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PERSON_UPDATE_FAILED, ServerConstants.MSG_RESPONSE_PERSON_UPDATE_FAILED);
        }
    }

    /**
     * 修改人员权限
     *
     * @param requestData
     * @return
     */
    public void doorAuthorityV2(AsyncHttpServerResponse response, String requestData) {
        RequestDoorAuthorityV2 doorAuthority = null;
        Gson gson = new Gson();
        try {
            doorAuthority = gson.fromJson(requestData, RequestDoorAuthorityV2.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null == doorAuthority) {
            response.send(LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_JSON_INVALID, ServerConstants.MSG_RESPONSE_JSON_INVALID));
            return;
        }
        String personSerial = doorAuthority.getPersonSerial();
        if (TextUtils.isEmpty(personSerial)) {
            response.send(LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PARAM_PERSON_SERIAL_INVALID, ServerConstants.MSG_RESPONSE_PARAM_PERSON_SERIAL_EMPTY));
            return;
        }
        TablePerson tablePerson = PersonDao.getInstance().getPersonByPersonSerial(personSerial);
        if (null == tablePerson) {
            response.send(LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PERSON_NOT_EXIST, ServerConstants.MSG_RESPONSE_PERSON_NOT_EXIST));
            return;
        }
        String checkParam = LocalHttpApiDataUtils.checkAuthorityParamV2(doorAuthority);
        if (!TextUtils.isEmpty(checkParam)) {
            response.send(checkParam);
            return;
        }
        List<RequestDoorAuthorityV2.DoorAuthorityDetail> authorityDetails = doorAuthority.getAuthorityDetails();
        List<TablePersonPermission> permissions = PersonPermissionDao.getInstance().getListByPersonSerial(personSerial);
        if (permissions == null || permissions.isEmpty()) {
            saveDoorAuthority(authorityDetails, personSerial, gson, response);
            return;
        }
        PersonPermissionDao.getInstance().deleteListTransaction(permissions, new PersonPermissionDao.OnPermissionListener() {
            @Override
            public void onSuccess() {
                saveDoorAuthority(authorityDetails, personSerial, gson, response);
            }

            @Override
            public void onError(String msg) {
                response.send(LocalHttpApiDataUtils.getResponseStringFail());
            }
        });
    }

    /**
     * 保存人员权限数据至本地
     */
    private void saveDoorAuthority(List<RequestDoorAuthorityV2.DoorAuthorityDetail> authorityDetails, String personSerial,
                                   Gson gson, AsyncHttpServerResponse response) {
        List<TablePersonPermission> savePermissions = LocalHttpApiDataUtils.createNewPersonPermissions(authorityDetails, personSerial, gson);
        PersonPermissionDao.getInstance().saveListTransaction(savePermissions,
                new PersonPermissionDao.OnPermissionListener() {
                    @Override
                    public void onSuccess() {
                        response.send(LocalHttpApiDataUtils.getResponseStringSuccess());
                    }

                    @Override
                    public void onError(String msg) {
                        response.send(LocalHttpApiDataUtils.getResponseStringFail());
                    }
                });
    }

    /**
     * 获取设备sn码
     *
     * @return
     */
    @SuppressLint("MissingPermission")
    public String getSnCode() {
        ResponseGetSerial responseGetSerial = new ResponseGetSerial();
        responseGetSerial.setSerialNumber(DeviceUtils.getSerial());
        return LocalHttpApiDataUtils.getResponseStringSuccess(responseGetSerial);
    }

    /**
     * 设备数据重置
     *
     * @return
     */
    public String cleanData() {
        if (CommonUtils.isOfflineLanAppMode()) {
            OfflineLanService.stopReboot();
        }
        EventBus.getDefault().post(new CleanDataEvent(true));
        return LocalHttpApiDataUtils.getResponseStringSuccess();
    }

    /**
     * 远程开门
     *
     * @return
     */
    public String openDoor() {
        CommonRepository.getInstance().openDoor();
        return LocalHttpApiDataUtils.getResponseStringSuccess(ServerConstants.MSG_RESPONSE_OPEN_DOOR_SEND_SUCCESS);
    }

    /**
     * 设备参数设置
     *
     * @param requestData
     * @return
     */
    public String setting(String requestData) {
        Gson gson = new GsonBuilder().registerTypeAdapterFactory(new CustomGsonTypeAdapter()).create();
        RequestSetting requestSetting = null;
        try {
            requestSetting = gson.fromJson(requestData, RequestSetting.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null == requestSetting) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_JSON_INVALID, ServerConstants.MSG_RESPONSE_JSON_INVALID);
        }
        ResponseBase responseBase = CommonRepository.getInstance().checkDeviceParams(requestSetting, true);
        return new Gson().toJson(responseBase);
    }

    /**
     * 获取参数设置
     *
     * @param requestStr
     * @return
     */
    @SuppressLint("MissingPermission")
    public String getSetting(String requestStr) {
        RequestGetSetting getSetting = null;
        try {
            getSetting = new Gson().fromJson(requestStr, RequestGetSetting.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null == getSetting) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_JSON_INVALID, ServerConstants.MSG_RESPONSE_JSON_INVALID);
        }
        String localMac = DeviceUtils.getMacAddress();
        if (TextUtils.isEmpty(localMac)) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_DEVICE_MAC_EMPTY, ServerConstants.MSG_RESPONSE_DEVICE_MAC_ADDRESS_EMPTY);
        }
        String netMac = getSetting.getMacAddress();
        if (TextUtils.isEmpty(netMac)) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_MAC_ADDRESS_INVALID, ServerConstants.MSG_RESPONSE_PARAM_MAC_ADDRESS_INVALID);
        }
        if (!localMac.equals(netMac.toUpperCase())) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_DEVICE_MAC_EQUAL_FAILED, ServerConstants.MSG_RESPONSE_MAC_ADDRESS_EQUAL_FAILED);
        }
        String netSerial = getSetting.getSerialNumber();
        if (TextUtils.isEmpty(netSerial)) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_SN_INVALID, ServerConstants.MSG_RESPONSE_PARAM_SN_INVALID);
        }
        if (!DeviceUtils.getSerial().equals(netSerial)) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_DEVICE_SN_EQUAL_FAILED, ServerConstants.MSG_RESPONSE_SERIAL_NUMBER_EQUAL_FAILED);
        }

        TableSettingConfigInfo configInfo = CommonRepository.getInstance().getSettingConfigInfo();
        ResponseGetSetting setting = new ResponseGetSetting();
        setting.setCompanyName(configInfo.getCompanyName());
        setting.setDevicePassword(configInfo.getDevicePassword());
        setting.setDisplayCustom(configInfo.getCustomDisplayModeFormat());
        setting.setDisplayMode(String.valueOf(configInfo.getDisplayMode()));
        setting.setInterval(configInfo.getRecognitionRetryDelay());
        setting.setLivenessType(configInfo.isLivenessDetect() ? configInfo.getLiveDetectType() : ConfigConstants.DEFAULT_LIVE_DETECT_CLOSE);
        setting.setMaxFaceSize(Integer.parseInt(configInfo.getMaxFaceTrackNumber()));
        setting.setOpenDelay(configInfo.getCloseDoorDelay());
        setting.setStrangerMode(String.valueOf(configInfo.getDisplayModeFail()));
        setting.setStrangerCustom(configInfo.getCustomFailDisplayModeFormat());
        setting.setStrangerVoiceMode(String.valueOf(configInfo.getVoiceModeFail()));
        setting.setStrangerVoiceCustom(configInfo.getCustomFailVoiceModeFormat());
        setting.setThreshold(configInfo.getSimilarThreshold());
        setting.setVoiceMode(String.valueOf(configInfo.getVoiceMode()));
        setting.setVoiceCustom(configInfo.getCustomVoiceModeFormat());
        setting.setSignDistance(configInfo.getRecognizeDistance());
        setting.setSuccessRetryDelay(configInfo.getSuccessRetryDelay());
        setting.setSuccessRetry(configInfo.getSuccessRetry());
        setting.setUploadRecordImage(configInfo.getUploadRecordImage());
        setting.setIrLivePreview(configInfo.getIrLivePreview());
        setting.setRebootEveryDay(configInfo.isRebootEveryDay() ? ConfigConstants.DEVICE_REBOOT_OPEN :
                ConfigConstants.DEVICE_REBOOT_CLOSE);
        setting.setRebootHour(configInfo.getRebootHour());
        setting.setRebootMin(configInfo.getRebootMin());
        setting.setVersionCode(AppUtils.getAppVersionCode());
        setting.setPackageName(AppUtils.getAppPackageName());
        setting.setVersionName(AppUtils.getAppVersionName());
        setting.setFaceQuality(configInfo.isFaceQuality() ? 1 : 0);
        setting.setFaceQualityThreshold(configInfo.getFaceQualityThreshold());
        setting.setDeviceName(configInfo.getDeviceName());

        return LocalHttpApiDataUtils.getResponseStringSuccess(setting);
    }

    /**
     * 设备logo设置
     *
     * @param requestData
     * @return
     */
    public String setLogo(String requestData) {
        RequestSetLogo requestSetLogo = null;
        try {
            requestSetLogo = new Gson().fromJson(requestData, RequestSetLogo.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null == requestSetLogo) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_JSON_INVALID, ServerConstants.MSG_RESPONSE_JSON_INVALID);
        }
        if (!requestSetLogo.checkOperation()) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_OPERATION_INVALID, ServerConstants.MSG_RESPONSE_PARAM_OPERATION_INVALID);
        }
        TableSettingConfigInfo configInfo = CommonRepository.getInstance().getSettingConfigInfo();
        String localMainLogoId = CommonRepository.getInstance().getMainLogoId();
        String localSecondLogoId = CommonRepository.getInstance().getSecondLogoId();
        if (requestSetLogo.addMain()) {
            String base64Main = requestSetLogo.getMainLogoBase64();
            if (TextUtils.isEmpty(base64Main)) {
                return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_MAIN_LOGO_INVALID, ServerConstants.MSG_RESPONSE_PARAM_MAIN_LOGO_EMPTY);
            }
            String mainLogoId = requestSetLogo.getMainLogoId();
            if (TextUtils.isEmpty(mainLogoId)) {
                return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_MAIN_LOGO_ID_INVALID, ServerConstants.MSG_RESPONSE_PARAM_MAIN_LOGO_ID_EMPTY);
            }
            if (!localMainLogoId.equals(mainLogoId)) {
                File mainFilePath = new File(ConfigConstants.DEFAULT_MAIN_LOGO_FILE_PATH);
                Bitmap mainBmp = ImageFileUtils.base64ToBitmap(base64Main);
                if (mainBmp == null) {
                    return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_IMAGE_BASE_64_INVALID, ServerConstants.MSG_RESPONSE_IMAGE_BASE_64_INVALID);
                }
                Bitmap resizeMainBmp;
                if (mainBmp.getWidth() > CommonRepository.LOGO_MAX_WIDTH ||
                        mainBmp.getHeight() > CommonRepository.LOGO_MAX_WIDTH) {
                    resizeMainBmp = ImageFileUtils.resizeImage(mainBmp, CommonRepository.LOGO_MAX_WIDTH,
                            CommonRepository.LOGO_MAX_WIDTH);
                    if (!mainBmp.isRecycled()) {
                        mainBmp.recycle();
                    }
                } else {
                    resizeMainBmp = mainBmp;
                }
                if (ImageFileUtils.save(resizeMainBmp, mainFilePath, Bitmap.CompressFormat.PNG)) {
                    if (!resizeMainBmp.isRecycled()) {
                        resizeMainBmp.recycle();
                    }
                    CommonRepository.getInstance().saveMainLogoId(requestSetLogo.getMainLogoId());
                    configInfo.setMainImagePath(mainFilePath.getAbsolutePath());
                    EventBus.getDefault().post(new ChangeMainLogoEvent(mainFilePath.getAbsolutePath()));
                    CommonRepository.getInstance().saveSettingConfigAsync(configInfo, null);
                }
            }
        } else if (requestSetLogo.deleteMain()) {
            configInfo.setMainImagePath("");
            CommonRepository.getInstance().saveMainLogoId("");
            EventBus.getDefault().post(new ChangeMainLogoEvent(""));
            CommonRepository.getInstance().saveSettingConfigAsync(configInfo, null);
        } else if (requestSetLogo.addSecond()) {
            String base64Second = requestSetLogo.getViceLogoBase64();
            if (TextUtils.isEmpty(base64Second)) {
                return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_SECOND_LOGO_INVALID, ServerConstants.MSG_RESPONSE_PARAM_SECOND_LOGO_EMPTY);
            }
            String secondLogoId = requestSetLogo.getSecondLogoId();
            if (TextUtils.isEmpty(secondLogoId)) {
                return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_SECOND_LOGO_ID_INVALID, ServerConstants.MSG_RESPONSE_PARAM_SECOND_LOGO_ID_EMPTY);
            }
            if (!localSecondLogoId.equals(secondLogoId)) {
                File secondFilePath = new File(ConfigConstants.DEFAULT_SECOND_LOGO_FILE_PATH);
                Bitmap secondBmp = ImageFileUtils.base64ToBitmap(base64Second);
                if (secondBmp == null) {
                    return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_IMAGE_BASE_64_INVALID, ServerConstants.MSG_RESPONSE_IMAGE_BASE_64_INVALID);
                }
                Bitmap resizeSecondBmp;
                if (secondBmp.getWidth() > CommonRepository.LOGO_MAX_WIDTH ||
                        secondBmp.getHeight() > CommonRepository.LOGO_MAX_WIDTH) {
                    resizeSecondBmp = ImageFileUtils.resizeImage(secondBmp, CommonRepository.LOGO_MAX_WIDTH,
                            CommonRepository.LOGO_MAX_WIDTH);
                    if (!secondBmp.isRecycled()) {
                        secondBmp.recycle();
                    }
                } else {
                    resizeSecondBmp = secondBmp;
                }
                if (ImageFileUtils.save(resizeSecondBmp, secondFilePath, Bitmap.CompressFormat.PNG)) {
                    if (!resizeSecondBmp.isRecycled()) {
                        resizeSecondBmp.recycle();
                    }
                    CommonRepository.getInstance().saveSecondLogoId(requestSetLogo.getSecondLogoId());
                    configInfo.setViceImagePath(secondFilePath.getAbsolutePath());
                    EventBus.getDefault().post(new ChangeSecondLogoEvent(secondFilePath.getAbsolutePath()));
                    CommonRepository.getInstance().saveSettingConfigAsync(configInfo, null);
                }
            }
        } else if (requestSetLogo.deleteSecond()) {
            configInfo.setViceImagePath("");
            CommonRepository.getInstance().saveSecondLogoId("");
            EventBus.getDefault().post(new ChangeSecondLogoEvent(""));
            CommonRepository.getInstance().saveSettingConfigAsync(configInfo, null);
        }

        return LocalHttpApiDataUtils.getResponseStringSuccess();
    }

    /**
     * 获取设备端logo
     *
     * @return
     */
    public String getLogo(String requestJson) {
        RequestGetLogo requestGetLogo = null;
        try {
            requestGetLogo = new Gson().fromJson(requestJson, RequestGetLogo.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (requestGetLogo == null) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_JSON_INVALID, ServerConstants.MSG_RESPONSE_JSON_INVALID);
        }
        ResponseGetLogo getLogo = new ResponseGetLogo();
        TableSettingConfigInfo configInfo = CommonRepository.getInstance().getSettingConfigInfo();
        String mainPath = configInfo.getMainImagePath();
        String secondPath = configInfo.getViceImagePath();
        String localMainLogoId = CommonRepository.getInstance().getMainLogoId();
        String localSecondLogoId = CommonRepository.getInstance().getSecondLogoId();
        String requestMainId = requestGetLogo.getMainLogoId();
        String requestSecondId = requestGetLogo.getSecondLogoId();
        if (requestGetLogo.isMain()) {
            if (TextUtils.isEmpty(mainPath) || !FileUtils.isFileExists(mainPath)) {
                getLogo.setMainLogo("");
                getLogo.setMainLogoId(CommonRepository.getInstance().getMainLogoId());
                return LocalHttpApiDataUtils.getResponseStringSuccess(getLogo);
            }
            if (!TextUtils.isEmpty(requestMainId) && requestMainId.equals(localMainLogoId)) {
                getLogo.setMainLogo("");
                getLogo.setMainLogoId(CommonRepository.getInstance().getMainLogoId());
                return LocalHttpApiDataUtils.getResponseStringSuccess(getLogo);
            }
            String mainBase64 = ImageFileUtils.image2Base64(mainPath);
            getLogo.setMainLogo(mainBase64);
            getLogo.setMainLogoId(CommonRepository.getInstance().getMainLogoId());
            return LocalHttpApiDataUtils.getResponseStringSuccess(getLogo);
        }
        if (requestGetLogo.isSecond()) {
            if (TextUtils.isEmpty(secondPath) || !FileUtils.isFileExists(secondPath)) {
                getLogo.setSecondLogo("");
                getLogo.setSecondLogoId(CommonRepository.getInstance().getSecondLogoId());
                return LocalHttpApiDataUtils.getResponseStringSuccess(getLogo);
            }
            if (!TextUtils.isEmpty(requestSecondId) && requestSecondId.equals(localSecondLogoId)) {
                getLogo.setSecondLogo("");
                getLogo.setSecondLogoId(CommonRepository.getInstance().getSecondLogoId());
                return LocalHttpApiDataUtils.getResponseStringSuccess(getLogo);
            }
            String secondBase64 = ImageFileUtils.image2Base64(secondPath);
            getLogo.setSecondLogo(secondBase64);
            getLogo.setSecondLogoId(CommonRepository.getInstance().getSecondLogoId());
            return LocalHttpApiDataUtils.getResponseStringSuccess(getLogo);
        }
        if (requestGetLogo.isBoth()) {
            if (TextUtils.isEmpty(mainPath) || !FileUtils.isFileExists(mainPath)) {
                getLogo.setMainLogo("");
                getLogo.setMainLogoId(CommonRepository.getInstance().getMainLogoId());
            } else if (!TextUtils.isEmpty(requestMainId) && requestMainId.equals(localMainLogoId)) {
                getLogo.setMainLogo("");
                getLogo.setMainLogoId(CommonRepository.getInstance().getMainLogoId());
            } else {
                String mainBase64 = ImageFileUtils.image2Base64(mainPath);
                getLogo.setMainLogo(mainBase64);
                getLogo.setMainLogoId(CommonRepository.getInstance().getMainLogoId());
            }
            if (TextUtils.isEmpty(secondPath) || !FileUtils.isFileExists(secondPath)) {
                getLogo.setSecondLogo("");
                getLogo.setSecondLogoId(CommonRepository.getInstance().getSecondLogoId());
            } else if (!TextUtils.isEmpty(requestSecondId) && requestSecondId.equals(localSecondLogoId)) {
                getLogo.setSecondLogo("");
                getLogo.setSecondLogoId(CommonRepository.getInstance().getSecondLogoId());
            } else {
                String secondBase64 = ImageFileUtils.image2Base64(secondPath);
                getLogo.setSecondLogo(secondBase64);
                String secondLogoId = CommonRepository.getInstance().getSecondLogoId();
                getLogo.setSecondLogoId(secondLogoId);
            }
            return LocalHttpApiDataUtils.getResponseStringSuccess(getLogo);
        }
        return LocalHttpApiDataUtils.getResponseStringSuccess();
    }

    /**
     * 设备重启
     *
     * @return
     */
    public String reboot() {
        if (!DeviceUtils.isRooted()) {
            return LocalHttpApiDataUtils.getResponseStringFail(CommonUtils.getStrFromRes(R.string.device_is_not_root));
        }
        CommonRepository.getInstance().rebootDelay(Constants.DEVICE_REBOOT_DELAY);
        return LocalHttpApiDataUtils.getResponseStringSuccess();
    }

    /**
     * 设置系统时间
     *
     * @param requestJson
     * @return
     */
    @SuppressLint("MissingPermission")
    public String systemTime(String requestJson) {
        RequestSystemTime systemTime = null;
        try {
            systemTime = new Gson().fromJson(requestJson, RequestSystemTime.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (systemTime == null) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_JSON_INVALID, ServerConstants.MSG_RESPONSE_JSON_INVALID);
        }
        String macAddress = systemTime.getMacAddress();
        String deviceSerialNumber = systemTime.getDeviceSN();
        String systemDateTime = systemTime.getSystemDateTime();
        String myMacAddress = DeviceUtils.getMacAddress();
        if (TextUtils.isEmpty(myMacAddress)) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_DEVICE_MAC_EMPTY, ServerConstants.MSG_RESPONSE_DEVICE_MAC_ADDRESS_EMPTY);
        }
        if (!myMacAddress.equals(macAddress.toUpperCase())) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_DEVICE_MAC_EQUAL_FAILED, ServerConstants.MSG_RESPONSE_MAC_ADDRESS_EQUAL_FAILED);
        }
        if (!DeviceUtils.getSerial().equals(deviceSerialNumber)) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_DEVICE_SN_EQUAL_FAILED, ServerConstants.MSG_RESPONSE_SERIAL_NUMBER_EQUAL_FAILED);
        }
        if (TextUtils.isEmpty(systemDateTime)) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PARAM_SYSTEM_TIME_EMPTY, ServerConstants.MSG_RESPONSE_PARAM_SYSTEM_TIME_EMPTY);
        }
        if (systemDateTime.length() < ServerConstants.DATE_TIME_STRING_MAX_LENGTH ||
                !TimeUtils.isValidDate(systemDateTime, TimeUtils.DATE_PATTERN_6)) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PARAM_SYSTEM_TIME_EMPTY, ServerConstants.MSG_RESPONSE_PARAM_SYSTEM_TIME_EMPTY);
        }
        if (TimeUtils.setSystemTime(systemDateTime)) {
            return LocalHttpApiDataUtils.getResponseStringSuccess();
        } else {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PARAM_SYSTEM_TIME_NOT_GRANTED, ServerConstants.MSG_RESPONSE_DEVICE_SYSTEM_PERMISSION_NOT_GRANTED);
        }
    }

    /**
     * @param requestJson
     * @return
     */
    public String packageAuthority(String requestJson) {
        RequestPackageAuthority authority = null;
        try {
            authority = new Gson().fromJson(requestJson, RequestPackageAuthority.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (authority == null) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_JSON_INVALID, ServerConstants.MSG_RESPONSE_JSON_INVALID);
        }
        String packageName = authority.getPackageName();
        if (!AppUtils.getAppPackageName().equals(packageName)) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PACKAGE_NAME_INVALID, ServerConstants.MSG_PACKAGE_NAME_INVALID);
        }
        int versionCode = authority.getVersionCode();
        if (AppUtils.getAppVersionCode() >= versionCode) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_VERSION_CODE_INVALID, ServerConstants.MSG_PACKAGE_VERSION_CODE_INVALID);
        }
        installVersionCode = versionCode;
        return LocalHttpApiDataUtils.getResponseStringSuccess();
    }

    public String packageTransfer(String requestJson) {
        if (requestJson == null) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PACKAGE_SAVE_FAILED, ServerConstants.MSG_RESPONSE_DEVICE_SDCARD_STORAGE_LESS_MIN);
        }
        if (AppUtils.checkPackageInfo(Utils.getApp(), requestJson)) {
            SPUtils.getInstance().put(Constants.SP_KEY_LOCAL_APK_CRC, requestJson);
            if (!PermissionUtils.isGranted(PermissionUtils.PERMISSION_INSTALL_PACKAGE)) {
                EventBus.getDefault().post(new InstallPackageEvent(requestJson, InstallPackageEvent.INSTALL_TYPE_NO_SILENCE));
                return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_NOT_GRANT_PERMISSION, ServerConstants.MSG_PACKAGE_NOT_GRANT_PERMISSION);
            } else {
                EventBus.getDefault().post(new InstallPackageEvent(requestJson, InstallPackageEvent.INSTALL_TYPE_SILENCE));
            }
        } else {
            FileUtils.deleteFile(requestJson);
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_AUTHORITY_INVALID, ServerConstants.MSG_PACKAGE_AUTHORITY_INVALID);
        }

        return LocalHttpApiDataUtils.getResponseStringSuccess();
    }

    /**
     * 解绑设备端
     *
     * @param requestJson
     * @return
     */
    public String disconnect(String requestJson) {
        RequestDisconnect request = null;
        try {
            request = new Gson().fromJson(requestJson, RequestDisconnect.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (request == null) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_JSON_INVALID, ServerConstants.MSG_RESPONSE_JSON_INVALID);
        }
        String verification = request.getVerification();
        if (TextUtils.isEmpty(verification)) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_VERIFICATION_FAILED, ServerConstants.MSG_RESPONSE_PARAM_VERIFICATION_FAILED);
        }
        TableSettingConfigInfo settingConfigInfo = CommonRepository.getInstance().getSettingConfigInfo();
        String signKey = settingConfigInfo.getSignKey();
        int deviceId = settingConfigInfo.getDeviceId();
        if (!Md5Utils.encode(deviceId + "|" + signKey).equals(request.getVerification())) {
            return LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_DISCONNECT_FAILED, ServerConstants.MSG_RESPONSE_DISCONNECT_FAILED);
        }
        settingConfigInfo.setDeviceId(0);
        settingConfigInfo.setSignKey("");
        settingConfigInfo.setServerIp(ConfigConstants.DEFAULT_SERVER_IP);
        settingConfigInfo.setServerPort(ConfigConstants.DEFAULT_SERVER_PORT);
        CommonRepository.getInstance().saveSettingConfigSync(settingConfigInfo);
        DefaultRemoteApiManager.getInstance().unInit();

        EventBus.getDefault().post(new DisconnectEvent(false, ConfigConstants.DEFAULT_SERVER_IP));

        return LocalHttpApiDataUtils.getResponseStringSuccess();
    }

    private ResponsePersonUpdate createResponseUpdate(String failedReason, String imageName,
                                                      String personName, String personSerial) {
        ResponsePersonAddFace responsePersonFace = new ResponsePersonAddFace();
        responsePersonFace.setImageName(imageName);
        responsePersonFace.setResult(false);
        responsePersonFace.setReason(failedReason);

        ResponsePersonUpdate update = new ResponsePersonUpdate();
        int deviceId = CommonRepository.getInstance().getSettingConfigInfo().getDeviceId();
        update.setEquipmentId(deviceId);
        update.setPersonName(personName);
        update.setPersonSerial(personSerial);
        update.setFaceResults(Collections.singletonList(responsePersonFace));
        return update;
    }

    private ResponsePersonUpdate createResponseUpdateSuccess(String failedReason, String imageName,
                                                             String personName, String personSerial) {
        return createResponseUpdateSuccess(failedReason, imageName, personName, personSerial, Config.DetectPriority.DP_0_ONLY.value());
    }

    private ResponsePersonUpdate createResponseUpdateSuccess(String failedReason, String imageName,
                                                             String personName, String personSerial, int faceOri) {
        ResponsePersonAddFace responsePersonFace = new ResponsePersonAddFace();
        responsePersonFace.setImageName(imageName);
        responsePersonFace.setResult(true);
        responsePersonFace.setReason(failedReason);
        responsePersonFace.setFaceOrient(faceOri);

        ResponsePersonUpdate update = new ResponsePersonUpdate();
        int deviceId = CommonRepository.getInstance().getSettingConfigInfo().getDeviceId();
        update.setEquipmentId(deviceId);
        update.setPersonName(personName);
        update.setPersonSerial(personSerial);
        update.setFaceResults(Collections.singletonList(responsePersonFace));
        return update;
    }

    private ResponsePersonUpdate createResponseUpdate(String personName, String personSerial) {
        ResponsePersonUpdate update = new ResponsePersonUpdate();
        int deviceId = CommonRepository.getInstance().getSettingConfigInfo().getDeviceId();
        update.setEquipmentId(deviceId);
        update.setPersonName(personName);
        update.setPersonSerial(personSerial);
        return update;
    }
}
