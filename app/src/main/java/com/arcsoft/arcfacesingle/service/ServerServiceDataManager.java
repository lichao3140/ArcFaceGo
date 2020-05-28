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

package com.arcsoft.arcfacesingle.service;

import android.text.TextUtils;

import com.arcsoft.arcfacesingle.business.common.FaceRecordDataManager;
import com.arcsoft.arcfacesingle.data.db.dao.IdentifyRecordDao;
import com.arcsoft.arcfacesingle.data.db.table.TableSettingConfigInfo;
import com.arcsoft.arcfacesingle.data.db.table.TableSignRecord;
import com.arcsoft.arcfacesingle.data.model.http.ReqSignRecord;
import com.arcsoft.arcfacesingle.data.model.http.ReqSignRecordList;

import java.io.File;
import java.util.List;

public class ServerServiceDataManager {

    private static volatile ServerServiceDataManager INSTANCE;

    public static ServerServiceDataManager getInstance() {
        if (INSTANCE == null) {
            synchronized (ServerServiceDataManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ServerServiceDataManager();
                }
            }
        }
        return INSTANCE;
    }

    public ReqSignRecordList getReqSignRecordList(List<TableSignRecord> records, TableSettingConfigInfo configInfo,
                                                  boolean existImg) {
        ReqSignRecordList reqSignRecordList = new ReqSignRecordList();
        for (TableSignRecord tableSignRecord : records) {
            ReqSignRecord reqSignRecord = new ReqSignRecord();
            reqSignRecord.setCheckTime(tableSignRecord.addTime);
            int deviceId = configInfo.getDeviceId();
            reqSignRecord.setEquipmentId(deviceId);
            String recordId = tableSignRecord.recordId;
            if (TextUtils.isEmpty(recordId)) {
                reqSignRecord.setEquipmentVerificationId(String.valueOf(tableSignRecord.addTime));
            } else {
                reqSignRecord.setEquipmentVerificationId(recordId);
            }
            reqSignRecord.setPersonCode(tableSignRecord.personSerial);
            reqSignRecord.setVerificationType(tableSignRecord.signType);
            reqSignRecord.setRecognitionName(TextUtils.isEmpty(tableSignRecord.faceInfo) ? "" : tableSignRecord.faceInfo);
            String imagePath = tableSignRecord.imagePath;
            if (!TextUtils.isEmpty(imagePath)) {
                try {
                    String imageName = imagePath.substring(imagePath.lastIndexOf("/") + 1, imagePath.lastIndexOf("."));
                    reqSignRecord.setImageName(imageName);
                } catch (StringIndexOutOfBoundsException | ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                    reqSignRecord.setImageName("");
                }
                if (new File(imagePath).exists()) {
                    reqSignRecord.setExistImage(existImg ? FaceRecordDataManager.TYPE_RECORD_EXIST_IMAGE :
                            FaceRecordDataManager.TYPE_RECORD_NOT_EXIST_IMAGE);
                } else {
                    reqSignRecord.setExistImage(FaceRecordDataManager.TYPE_RECORD_NOT_EXIST_IMAGE);
                }
            } else {
                reqSignRecord.setExistImage(FaceRecordDataManager.TYPE_RECORD_NOT_EXIST_IMAGE);
            }
            if (tableSignRecord.status == IdentifyRecordDao.STATUS_IC_CARD) {
                reqSignRecord.setIcCardNo(tableSignRecord.icCardNo);
                reqSignRecord.setRecordType(ReqSignRecord.TYPE_RECORD_IC_CARD);
            } else {
                reqSignRecord.setRecordType(ReqSignRecord.TYPE_RECORD_FACE);
            }
            reqSignRecordList.addRequestSignRecord(reqSignRecord);
        }
        return reqSignRecordList;
    }
}
