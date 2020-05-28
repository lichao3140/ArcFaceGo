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
import com.arcsoft.arcfacesingle.data.db.dao.IdentifyRecordDao;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

@Table(database = DBManager.class)
public class TableSignRecord extends BaseModel {

    @PrimaryKey(autoincrement = true)
    public long id;

    /**
     * 云模式：设备ID
     */
    @Column
    public int deviceId;

    /**
     * 人员标识码，一般用于离线模式
     * 离线模式（用于标识唯一人员，由代码自动生成）
     * 云模式（由personId和personSetId经过MD5加密后生成）
     */
    @Column
    public String personSerial;

    /**
     * 人脸识别记录类型：-1 识别失败；1 签到；2 签退；3 签到、签退；4 无权限
     */
    @Column
    public int signType;

    /**
     * 图片地址
     */
    @Column
    public String imagePath;

    /**
     * 数据添加时间
     */
    @Column
    public long addTime;

    //*******************以下为V1.2.2版本新增参数**********************//
    /**
     * 状态
     * 离线模式（0 默认新增数据；1 已上传文本，未上传图片；2 文本和图片都已上传，可以删除记录了；
     *                      3 刷IC卡记录）；4 服务器查不到该人员或该条文本记录
     * 云模式(0 默认新增数据)
     */
    @Column
    public int status = IdentifyRecordDao.STATUS_DEFAULT;

    //*******************以下为V2.0版本新增参数**********************//
    /**
     * 识别记录唯一标识
     */
    @Column
    public String recordId;

    /**
     * 人员编号
     * 云模式（人员编号）
     */
    @Column
    public String personId;

    /**
     * 人员所属组编号
     * 云模式（人员所属组编号）
     */
    @Column
    public String personSetId;

    /**
     * 云模式：人脸编号，对应TablePersonFace中的faceId
     */
    @Column
    public String faceId;

    /**
     * 离线模式：对应人员姓名等信息
     * 云模式：对应TablePersonFace中的faceInfo
     */
    @Column
    public String faceInfo;

    /**
     * 数据更新时间
     */
    @Column
    public Long updateTime;

    /**
     * 1 识别成功；2 识别失败；3 识别成功但无权限
     */
    @Column
    public int type;

    //*******************以下为V2.0版本新增参数**********************//
    /**
     * IC卡编号
     */
    @Column
    public String icCardNo;
}