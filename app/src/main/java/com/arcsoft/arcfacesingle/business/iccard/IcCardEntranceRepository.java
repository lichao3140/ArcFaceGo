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

package com.arcsoft.arcfacesingle.business.iccard;

import android.text.TextUtils;

import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.business.common.CommonRepository;
import com.arcsoft.arcfacesingle.business.common.FaceRecordDataManager;
import com.arcsoft.arcfacesingle.business.recognize.RecognizeRepDataManager;
import com.arcsoft.arcfacesingle.business.setting.ConfigConstants;
import com.arcsoft.arcfacesingle.data.db.dao.IdentifyRecordDao;
import com.arcsoft.arcfacesingle.data.db.dao.PersonDao;
import com.arcsoft.arcfacesingle.data.db.dao.PersonPermissionDao;
import com.arcsoft.arcfacesingle.data.db.table.TablePerson;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonPermission;
import com.arcsoft.arcfacesingle.data.db.table.TableSettingConfigInfo;
import com.arcsoft.arcfacesingle.data.db.table.TableSignRecord;
import com.arcsoft.arcfacesingle.data.model.http.ReqSignRecord;
import com.arcsoft.arcfacesingle.data.model.http.ReqSignRecordList;
import com.arcsoft.arcfacesingle.util.scheduler.BaseObserver;
import com.arcsoft.asg.libcommon.util.business.rx.RxUtils;
import com.arcsoft.asg.libnetwork.bean.RemoteResponseBase;
import com.arcsoft.asg.libnetwork.manage.DefaultRemoteApiManager;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class IcCardEntranceRepository {

    private static volatile IcCardEntranceRepository INSTANCE;
    private static final String TAG = IcCardEntranceRepository.class.getSimpleName();

    /**
     * 本地数据库人脸数据
     */
    private List<TablePerson> tablePersonList;

    private IcCardEntranceRepository() {
    }

    public static IcCardEntranceRepository getInstance() {
        if (INSTANCE == null) {
            synchronized (IcCardEntranceRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new IcCardEntranceRepository();
                }
            }
        }
        return INSTANCE;
    }

    public void init() {
        tablePersonList = new ArrayList<>();
        loadTablePersonList();
    }

    public void reloadPersonList() {
        if (tablePersonList == null) {
            tablePersonList = new ArrayList<>();
        } else {
            tablePersonList.clear();
        }
        loadTablePersonList();
    }

    private void loadTablePersonList() {
        Disposable disposable = Observable.create((ObservableEmitter<List<TablePerson>> emitter) -> {
            List<TablePerson> personList = PersonDao.getInstance().getPersonListWithIcCardNo();
            emitter.onNext(personList);
            emitter.onComplete();
        }).compose(RxUtils.ioToMain())
                .subscribeWith(new BaseObserver<List<TablePerson>>() {
                    @Override
                    public void onNext(List<TablePerson> list) {
                        if (list != null) {
                            tablePersonList.addAll(list);
                        }
                    }
                });
    }

    /**
     * 处理ic卡刷卡数据
     *
     * @param icCardNo
     */
    public void processIcCardEntrance(String icCardNo) {
        if (TextUtils.isEmpty(icCardNo)) {
            return;
        }
        icCardNo = icCardNo.replace(" ", "").trim();
        TableSettingConfigInfo settingConfigInfo = CommonRepository.getInstance().getSettingConfigInfo();
        if (tablePersonList == null || tablePersonList.size() == 0) {
            TableSignRecord signRecord = createSignRecord(settingConfigInfo, null, false, false);
            signRecord.icCardNo = icCardNo;
            uploadSignRecord(settingConfigInfo, signRecord);
            return;
        }
        TablePerson tablePerson = null;
        for (TablePerson person : tablePersonList) {
            String localIcCardNo = person.icCardNo.toLowerCase();
            if (localIcCardNo.equals(icCardNo.toLowerCase())) {
                tablePerson = person;
                break;
            }
        }
        if (tablePerson != null) {
            List<TablePersonPermission> permissions = PersonPermissionDao.getInstance().getListByPersonSerial(tablePerson.personSerial);
            boolean hasPermission = RecognizeRepDataManager.getInstance().doorAuthorityWithTime(tablePerson, permissions);
            if (hasPermission) {
                CommonRepository.getInstance().openDoor();
                TableSignRecord signRecord = createSignRecord(settingConfigInfo, tablePerson, true, true);
                uploadSignRecord(settingConfigInfo, signRecord);
            } else {
                TableSignRecord signRecord = createSignRecord(settingConfigInfo, tablePerson, true, false);
                uploadSignRecord(settingConfigInfo, signRecord);
            }
        } else {
            TableSignRecord signRecord = createSignRecord(settingConfigInfo, null, false, false);
            signRecord.icCardNo = icCardNo;
            uploadSignRecord(settingConfigInfo, signRecord);
        }
    }

    /**
     * 上传刷脸数据
     *
     * @param signRecord
     */
    private void uploadSignRecord(TableSettingConfigInfo settingConfigInfo, TableSignRecord signRecord) {
        if (!DefaultRemoteApiManager.getInstance().isInitSuccess()) {
            IdentifyRecordDao.getInstance().addModelTransactionAsync(signRecord);
            return;
        }
        ReqSignRecordList reqSignRecordList = IcCardEntranceRepository.getInstance().
                createSignRecordList(Collections.singletonList(signRecord), settingConfigInfo);
        String requestRecordList = new Gson().toJson(reqSignRecordList);
        Disposable disposable = DefaultRemoteApiManager.getInstance()
                .uploadIcCardRecord(requestRecordList)
                .compose(RxUtils.ioToMain())
                .subscribeWith(new BaseObserver<RemoteResponseBase<HashMap<String, Integer>>>() {
                    @Override
                    public void onNext(RemoteResponseBase<HashMap<String, Integer>> responseBase) {
                        if (responseBase.getCode() == Constants.HTTP_REQUEST_CODE_SUCCESS) {
                            HashMap<String, Integer> resultMap = responseBase.getData();
                            if (resultMap.size() > 0) {
                                Integer result = resultMap.get(signRecord.recordId);
                                if (result == null || result != FaceRecordDataManager.SIGN_RECORD_STATUS_SUCCESS) {
                                    IdentifyRecordDao.getInstance().addModelTransactionAsync(signRecord);
                                }
                            }
                        } else {
                            IdentifyRecordDao.getInstance().addModelTransactionAsync(signRecord);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        IdentifyRecordDao.getInstance().addModelTransactionAsync(signRecord);
                    }
                });
    }

    private TableSignRecord createSignRecord(TableSettingConfigInfo settingConfigInfo, TablePerson person,
                                             boolean success, boolean hasPermission) {
        TableSignRecord signRecord = new TableSignRecord();
        signRecord.recordId = CommonRepository.getInstance().createDatabaseId();
        signRecord.status = IdentifyRecordDao.STATUS_IC_CARD;
        signRecord.addTime = System.currentTimeMillis();
        signRecord.updateTime = signRecord.addTime;
        if (success) {
            if (hasPermission) {
                signRecord.signType = settingConfigInfo.getSignType();
            } else {
                signRecord.signType = ConfigConstants.TYPE_SIGN_UNAUTHORIZED;
            }
        } else {
            signRecord.signType = ConfigConstants.TYPE_SIGN_FAIL;
        }
        if (person != null) {
            signRecord.personSerial = person.personSerial;
            signRecord.icCardNo = person.icCardNo;
            signRecord.faceInfo = person.personName;
        }
        return signRecord;
    }

    private RequestBody createRequestBody(ReqSignRecordList recordList) {
        if (recordList == null || recordList.getVerificationList().size() == 0) {
            return null;
        }
        String recordJson = new Gson().toJson(recordList);
        return RequestBody.create(MediaType.parse("application/json"), recordJson);
    }

    private ReqSignRecordList createSignRecordList(List<TableSignRecord> recordList, TableSettingConfigInfo configInfo) {
        ReqSignRecordList reqSignRecordList = new ReqSignRecordList();
        for (TableSignRecord tableSignRecord : recordList) {
            ReqSignRecord reqSignRecord = new ReqSignRecord();
            reqSignRecord.setCheckTime(tableSignRecord.addTime);
            reqSignRecord.setEquipmentId(configInfo.getDeviceId());
            reqSignRecord.setEquipmentVerificationId(tableSignRecord.recordId);
            reqSignRecord.setPersonCode(tableSignRecord.personSerial);
            reqSignRecord.setVerificationType(tableSignRecord.signType);
            reqSignRecord.setIcCardNo(tableSignRecord.icCardNo);
            reqSignRecord.setRecognitionName(TextUtils.isEmpty(tableSignRecord.faceInfo) ? "" : tableSignRecord.faceInfo);
            reqSignRecord.setExistImage(FaceRecordDataManager.TYPE_RECORD_NOT_EXIST_IMAGE);
            reqSignRecordList.addRequestSignRecord(reqSignRecord);
        }
        return reqSignRecordList;
    }

    public void unInit() {
        if (tablePersonList != null) {
            tablePersonList.clear();
            tablePersonList = null;
        }
    }
}
