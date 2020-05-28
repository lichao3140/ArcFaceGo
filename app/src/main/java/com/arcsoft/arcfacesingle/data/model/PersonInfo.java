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

package com.arcsoft.arcfacesingle.data.model;

import com.arcsoft.arcfacesingle.data.db.table.TablePersonFace;

import java.io.Serializable;
import java.util.List;

public class PersonInfo implements Serializable {

    private long id;

    private String personSerial;

    private String personName;

    private String personInfoNo;

    private int personInfoType;

    private String icCardNo;

    private boolean selected;

    private String mainFaceId;

    private List<TablePersonFace> faceResult;

    public String getPersonInfoNo() {
        return personInfoNo;
    }

    public void setPersonInfoNo(String personInfoNo) {
        this.personInfoNo = personInfoNo;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public List<TablePersonFace> getFaceResult() {
        return faceResult;
    }

    public void setFaceResult(List<TablePersonFace> faceResult) {
        this.faceResult = faceResult;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getPersonInfoType() {
        return personInfoType;
    }

    public void setPersonInfoType(int personInfoType) {
        this.personInfoType = personInfoType;
    }

    public String getIcCardNo() {
        return icCardNo;
    }

    public void setIcCardNo(String icCardNo) {
        this.icCardNo = icCardNo;
    }

    public String getMainFaceId() {
        return mainFaceId;
    }

    public void setMainFaceId(String mainFaceId) {
        this.mainFaceId = mainFaceId;
    }
}
