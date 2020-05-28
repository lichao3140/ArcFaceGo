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
import java.util.List;

public class RequestPersonAdd implements Serializable {

    /**
     * 即将添加到设备端人脸的总数
     */
    private int total;

    /**
     * 当前添加到设备端人脸数
     */
    private int current;

    /**
     * 人员编号
     */
    private String personSerial;

    /**
     * 人员姓名
     */
    private String personName;

    /**
     * 人员工号或学号等
     */
    private String personIdentifier;

    /**
     * 人员注册照列表
     */
    private List<RequestPersonAddFace> faceList;

    //*****************V2.0版本新增参数******************//
    /**
     * IC卡编号
     */
    private String icCardNo;

    /**
     * 人员信息类型：1 只含有IC卡号；2 只含有人脸信息；3 以上两者都有
     */
    private int personInfoType;

    public int getPersonInfoType() {
        return personInfoType;
    }

    public void setPersonInfoType(int personInfoType) {
        this.personInfoType = personInfoType;
    }

    public String getPersonSerial() {
        return personSerial;
    }

    public void setPersonSerial(String personSerial) {
        this.personSerial = personSerial;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public List<RequestPersonAddFace> getFaceList() {
        return faceList;
    }

    public void setFaceList(List<RequestPersonAddFace> faceList) {
        this.faceList = faceList;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public String getPersonIdentifier() {
        return personIdentifier;
    }

    public void setPersonIdentifier(String personIdentifier) {
        this.personIdentifier = personIdentifier;
    }

    public String getIcCardNo() {
        return icCardNo;
    }

    public void setIcCardNo(String icCardNo) {
        this.icCardNo = icCardNo;
    }
}
