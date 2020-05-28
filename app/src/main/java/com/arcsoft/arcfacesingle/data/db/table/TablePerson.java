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

import com.arcsoft.arcfacesingle.data.db.DBManager;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

@Table(database = DBManager.class)
public class TablePerson extends BaseModel {

    @PrimaryKey(autoincrement = true)
    public long id;

    /**
     * 离线模式：人员编号，云端AIoT模式下需要额外生成此编号
     */
    @Column
    public String personSerial;

    /**
     * 离线模式：人员姓名
     * 云模式：人员姓名
     */
    @Column
    public String personName;

    /**
     * 人员注册照上的人脸特征值
     */
    @Column
    public byte[] faceFeature;

    /**
     * 离线模式：添加时间
     * 云模式：添加时间
     */
    @Column
    public long addTime;

    /**
     * 离线模式：更新时间
     * 云模式：添加时间
     */
    @Column
    public long updateTime;

    /**
     * 离线模式：门禁名称
     */
    @Column
    public String doorAuthorityDetail;

    /**
     * 离线模式：门禁早上开始时间
     */
    @Column
    public String authMorningStartTime;

    /**
     * 离线模式：门禁早上结束时间
     */
    @Column
    public String authMorningEndTime;

    /**
     * 离线模式：门禁中午开始时间
     */
    @Column
    public String authNoonStartTime;

    /**
     * 离线模式：门禁中午结束时间
     */
    @Column
    public String authNoonEndTime;

    /**
     * 离线模式：门禁晚上开始时间
     */
    @Column
    public String authNightStartTime;

    /**
     * 离线模式：门禁晚上结束时间
     */
    @Column
    public String authNightEndTime;

    /**
     * 离线模式：备注信息
     */
    @Column
    public String remark;

    //*******************以下为V2.0版本新增参数**********************//
    /**
     * 离线模式：一般为工号/学号等，用于额外的业务需求
     */
    @Column
    public String personInfoNo;

    /**
     * 云模式：人员编号
     */
    @Column
    public String personId;

    /**
     * 云模式：人员所属组编号
     */
    @Column
    public String personSetId;

    /**
     * 云模式：人员信息
     */
    @Column
    public String personInfo;

    /**
     * 云模式：当前版本
     */
    @Column
    public Integer version;

    //*******************以下为V2.0版本新增参数**********************//
    /**
     * IC卡编号
     */
    @Column
    public String icCardNo;

    /**
     * 人员信息类型：1 只含有IC卡号；2 只含有人脸信息；3 以上两者都有
     */
    @Column
    public Integer personInfoType;

    //*******************以下为V3.0.2版本新增参数**********************//
    /**
     * 主照对应的faceId
     */
    @Column
    public String mainFaceId;
}
