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

package com.arcsoft.arcfacesingle.data.model.http;

import java.io.Serializable;

public class ReqSignRecord implements Serializable {

    public static final int TYPE_RECORD_FACE = 1;
    public static final int TYPE_RECORD_IC_CARD = 2;

    private int equipmentId;

    private long checkTime;

    private int verificationType;

    private String imageName;

    private String personCode;

    private String recognitionName;

    private String equipmentVerificationId;

    private String icCardNo;

    /**
     * 是否存在图片：1 有；2 没有；
     */
    private int existImage;

    /**
     * 记录类型：1 刷脸记录；2 IC卡记录
     */
    private int recordType;

    public String getRecognitionName() {
        return recognitionName;
    }

    public void setRecognitionName(String recognitionName) {
        this.recognitionName = recognitionName;
    }

    public int getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(int equipmentId) {
        this.equipmentId = equipmentId;
    }

    public long getCheckTime() {
        return checkTime;
    }

    public void setCheckTime(long checkTime) {
        this.checkTime = checkTime;
    }

    public int getVerificationType() {
        return verificationType;
    }

    public void setVerificationType(int verificationType) {
        this.verificationType = verificationType;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getPersonCode() {
        return personCode;
    }

    public void setPersonCode(String personCode) {
        this.personCode = personCode;
    }

    public String getEquipmentVerificationId() {
        return equipmentVerificationId;
    }

    public void setEquipmentVerificationId(String equipmentVerificationId) {
        this.equipmentVerificationId = equipmentVerificationId;
    }

    public String getIcCardNo() {
        return icCardNo;
    }

    public void setIcCardNo(String icCardNo) {
        this.icCardNo = icCardNo;
    }

    public int getExistImage() {
        return existImage;
    }

    public void setExistImage(int existImage) {
        this.existImage = existImage;
    }

    public int getRecordType() {
        return recordType;
    }

    public void setRecordType(int recordType) {
        this.recordType = recordType;
    }
}
