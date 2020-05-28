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
public class TablePersonFace extends BaseModel {

    @PrimaryKey(autoincrement = true)
    public long id;

    /**
     * 人员标识码
     */
    @Column
    public String personSerial;

    /**
     * 人员注册照地址
     */
    @Column
    public String imagePath;

    /**
     * 人员注册照md5值，用于标识注册照是否发生改变，若md5值有变，则注册照发生变化
     */
    @Column
    public String imageMD5;

    /**
     * 人脸特征值
     */
    @Column
    public byte[] feature;

    /**
     * 首次添加时间
     */
    @Column
    public long addTime;

    /**
     * 更新时间
     */
    @Column
    public long updateTime;

    //*******************以下为V2.0.0版本新增参数**********************//
    /**
     * 离线模式：对应TablePerson中的id
     * 云模式：对应TablePerson中的id
     */
    @Column
    public Long personId;

    /**
     * 云模式：人脸编号
     */
    @Column
    public String faceId;

    /**
     * 云模式：人脸信息
     * 离线模式：人脸姓名
     */
    @Column
    public String faceInfo;

    /**
     * 云模式：人脸图片网络存储路径
     */
    @Column
    public String imageUrl;

    //*******************以下为ArcFace SDK 3.0版本新增参数**********************//
    /**
     * 人脸特征值版本
     */
    @Column
    public String featureVersion;
}