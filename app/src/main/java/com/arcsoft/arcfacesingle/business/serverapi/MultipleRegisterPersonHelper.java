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

package com.arcsoft.arcfacesingle.business.serverapi;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.business.personlist.PersonListDataManager;
import com.arcsoft.arcfacesingle.business.setting.ConfigConstants;
import com.arcsoft.arcfacesingle.data.db.dao.PersonDao;
import com.arcsoft.arcfacesingle.data.db.table.TablePerson;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonFace;
import com.arcsoft.arcfacesingle.libutil.ImageFileUtils;
import com.arcsoft.arcfacesingle.server.api.ServerConstants;
import com.arcsoft.arcfacesingle.server.faceengine.FaceEngineManager;
import com.arcsoft.arcfacesingle.server.pojo.request.RequestPersonAdd;
import com.arcsoft.arcfacesingle.server.pojo.response.ResponsePersonAddFace;
import com.arcsoft.arcfacesingle.server.pojo.response.ResponsePersonUpdate;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.asg.libcommon.util.common.StringUtils;
import com.arcsoft.faceengine.ErrorInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.ObservableEmitter;

public class MultipleRegisterPersonHelper {

    /**
     * icCard卡号核验
     */
    public static boolean checkIcCardNo(String strIcCardNo, ResponsePersonUpdate result, ObservableEmitter<ResponsePersonUpdate> emitter) {
        if (TextUtils.isEmpty(strIcCardNo)) {
            registerResultEmitter(result, ServerConstants.MSG_RESPONSE_PARAM_IC_INVALID, false, emitter);
            return false;
        }
        if (!TextUtils.isEmpty(strIcCardNo) && strIcCardNo.length() > ServerConstants.IC_CARD_NO_MAX_LENGTH) {
            registerResultEmitter(result, ServerConstants.MSG_IC_CARD_NO_LENGTH_INVALID, false, emitter);
            return false;
        }
        if (!StringUtils.matcherPassword(strIcCardNo)) {
            registerResultEmitter(result, ServerConstants.MSG_IC_CARD_NO_FORMAT_INVALID, false, emitter);
            return false;
        }
        return true;
    }

    /**
     * 统一处理Emitter成功结果
     */
    public static void registerSuccessEmitter(ResponsePersonUpdate result, ObservableEmitter<ResponsePersonUpdate> emitter) {
        MultipleRegisterPersonHelper.registerResultEmitter(result, ServerConstants.MSG_RESPONSE_PERSON_UPDATE_SUCCESS,
                true, emitter);
    }

    /**
     * 统一处理Emitter失败结果
     */
    public static void registerFailEmitter(ResponsePersonUpdate result, int type, ObservableEmitter<ResponsePersonUpdate> emitter) {
        String msg = PersonListDataManager.getStringInfoByType(type);
        MultipleRegisterPersonHelper.registerResultEmitter(result, msg, false, emitter);
    }

    /**
     * 统一处理Emitter结果
     */
    public static void registerResultEmitter(ResponsePersonUpdate result, String msg, boolean registerSuccess,
                                             ObservableEmitter<ResponsePersonUpdate> emitter) {
        List<ResponsePersonAddFace> faceResults = createResponsePersonFaces(registerSuccess, null, 0, msg);
        result.setFaceResults(faceResults);
        emitter.onNext(result);
    }

    public static List<ResponsePersonAddFace> createResponsePersonFaces(boolean registerSuccess, String faceId, int faceOrient,
                                                                        String reason) {
        ResponsePersonAddFace faceResult = new ResponsePersonAddFace();
        faceResult.setFaceId(faceId);
        faceResult.setFaceOrient(faceOrient);
        faceResult.setResult(registerSuccess);
        faceResult.setReason(reason);
        List<ResponsePersonAddFace> faceResults = new ArrayList<>(1);
        faceResults.add(faceResult);
        return faceResults;
    }

    /**
     * byte流转bitmap
     */
    public static Bitmap getBitmapFromByte(Map<String, byte[]> imageMap, String imgKey) {
        if (imageMap == null || TextUtils.isEmpty(imgKey)) {
            return null;
        }
        byte[] imgByte = imageMap.get(imgKey);
        Bitmap oriBmp = ImageFileUtils.decodeFileWithThreshold(imgByte, Constants.FACE_REGISTER_MAX_WIDTH, Constants.FACE_REGISTER_MAX_WIDTH);
        if (oriBmp == null) {
            return null;
        }
        Bitmap bitmap;
        if (oriBmp.getWidth() % Constants.FACE_DETECT_IMAGE_WIDTH_LIMIT != 0 || oriBmp.getHeight() % Constants.FACE_DETECT_IMAGE_HEIGHT_LIMIT != 0) {
            bitmap = ImageFileUtils.setBitmap4Align(oriBmp);
            if (!oriBmp.isRecycled()) {
                oriBmp.recycle();
            }
        } else {
            bitmap = oriBmp;
        }
        return bitmap;
    }

    /**
     * 获取数据库人员
     *
     * @param oriPerson
     * @return
     */
    public static TablePerson createPerson(RequestPersonAdd oriPerson) {
        TablePerson faceInfo = PersonDao.getInstance().getPersonByPersonSerial(oriPerson.getPersonSerial());
        if (faceInfo != null) {
            if (!TextUtils.isEmpty(oriPerson.getPersonName())) {
                faceInfo.personName = oriPerson.getPersonName();
            }
            faceInfo.updateTime = System.currentTimeMillis();
        } else {
            faceInfo = new TablePerson();
            faceInfo.personSerial = oriPerson.getPersonSerial();
            faceInfo.personName = oriPerson.getPersonName();
            faceInfo.addTime = System.currentTimeMillis();
            faceInfo.updateTime = faceInfo.addTime;
            faceInfo.doorAuthorityDetail = CommonUtils.getStrFromRes(R.string.face_manager_allow_pass);
            faceInfo.authMorningStartTime = ConfigConstants.DOOR_AUTHORITY_DEFAULT_START_TIME;
            faceInfo.authMorningEndTime = ConfigConstants.DOOR_AUTHORITY_DEFAULT_END_TIME;
            faceInfo.authNoonStartTime = faceInfo.authMorningStartTime;
            faceInfo.authNoonEndTime = faceInfo.authMorningEndTime;
            faceInfo.authNightStartTime = faceInfo.authMorningStartTime;
            faceInfo.authNightEndTime = faceInfo.authMorningEndTime;
        }
        if (!TextUtils.isEmpty(oriPerson.getPersonIdentifier())) {
            faceInfo.personInfoNo = oriPerson.getPersonIdentifier();
        }
        int personType = oriPerson.getPersonInfoType();
        if (personType == MultipleRegisterPersonManager.TYPE_PERSON_ONLY_PICTURE) {
            faceInfo.icCardNo = "";
        } else if (personType == MultipleRegisterPersonManager.TYPE_PERSON_BOTH ||
                personType == MultipleRegisterPersonManager.TYPE_PERSON_ONLY_IC_CARD) {
            faceInfo.icCardNo = oriPerson.getIcCardNo();
        }
        faceInfo.personInfoType = personType;
        return faceInfo;
    }

    /**
     * 获取数据库人脸
     *
     * @param faceInfo
     * @param faceFeature
     * @return
     */
    public static TablePersonFace createPersonFace(TablePerson faceInfo, TablePersonFace personFace, String imageName, byte[] faceFeature) {
        if (personFace != null) {
            personFace.faceInfo = faceInfo.personName;
            personFace.updateTime = System.currentTimeMillis();
        } else {
            personFace = new TablePersonFace();
            personFace.faceInfo = faceInfo.personName;
            personFace.personSerial = faceInfo.personSerial;
            personFace.addTime = faceInfo.addTime;
            personFace.updateTime = personFace.addTime;
            personFace.featureVersion = Constants.FACE_FEATURE_VERSION_V30;
        }
        int index = imageName.lastIndexOf(".");
        if (index != -1) {
            imageName = imageName.substring(0, index + 1);
        }
        personFace.imagePath = CommonUtils.getPersonFaceLocalPath(imageName);
        personFace.feature = faceFeature;
        return personFace;
    }

    /**
     * 创建引擎
     *
     * @param engineCount
     * @return
     */
    public static List<FaceEngineManager> initFaceEngine(int engineCount) {
        List<FaceEngineManager> faceEngineList = new ArrayList<>();
        for (int i = 0; i < engineCount; i++) {
            FaceEngineManager faceEngineManager = new FaceEngineManager();
            faceEngineManager.createFaceEngine();
            int res = faceEngineManager.initFaceEngine();
            if (res == ErrorInfo.MOK) {
                faceEngineList.add(faceEngineManager);
            }
        }
        return faceEngineList;
    }

    /**
     * 获取faceEngine并发处理数
     *
     * @param concurrent
     * @return
     */
    public static int getFaceEngineCount(int concurrent, int personCount) {
        int cpuCount = Runtime.getRuntime().availableProcessors();
        //引擎最大并发数，不超过cpu核数的一半；
        int countFaceEngineMax;
        if (cpuCount > MultipleRegisterPersonManager.NUMBER_FACE_THREAD_ONE) {
            countFaceEngineMax = cpuCount / 2;
        } else {
            countFaceEngineMax = cpuCount;
        }
        //实际引擎并发数
        int faceEngineCount;
        if (concurrent > 0) {
            if (concurrent > countFaceEngineMax) {
                faceEngineCount = countFaceEngineMax;
            } else {
                faceEngineCount = concurrent;
            }
        } else {
            faceEngineCount = countFaceEngineMax;
        }
        if (faceEngineCount > personCount) {
            faceEngineCount = personCount;
        }
        return faceEngineCount;
    }
}
