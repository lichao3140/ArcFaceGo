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

package com.arcsoft.arcfacesingle.business.persondetail;

import android.util.Pair;

import com.arcsoft.arcfacesingle.data.db.table.TablePerson;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonFace;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonPermission;

import java.util.LinkedHashMap;
import java.util.List;

public interface IPersonDetailRep {

    interface InitPersonDataCallback {

        /**
         * 加载数据成功
         * @param pair 人员数据集
         */
        void loadDataSuccess(Pair<TablePerson, List<TablePersonFace>> pair);
    }

    /**
     * 初始化数据
     * @param personSerial 人员唯一标识码
     * @param callback 回调
     */
    void initPersonData(String personSerial, InitPersonDataCallback callback);

    /**
     * 保存修改内容
     * @param tablePerson 人员数据
     * @param type 类型
     * @param editContent 编辑内容信息
     * @return
     */
    boolean savePerson(TablePerson tablePerson, int type, String editContent);

    /**
     * 是否存在封面
     * @param faceList 人脸数据集
     * @param mainId 主人脸Id
     * @return
     */
    boolean existMainFace(List<TablePersonFace> faceList, String mainId);

    /**
     * 获取数据库权限
     * @param personSerial 人员唯一标识码
     * @return
     */
    TablePersonPermission getPermissionFromDb(String personSerial);

    /**
     * 比对日期权限
     * @param tablePerson 人员数据
     * @param permission 人员权限数据
     * @return true 比对通过；false 比对不通过
     */
    boolean compareDate(TablePerson tablePerson, TablePersonPermission permission);

    /**
     * 日期转换
     * @param tablePerson 人员数据
     * @param permission 人员权限数据
     * @return 日期数据
     */
    String getDateString(TablePerson tablePerson, TablePersonPermission permission);

    /**
     * 周期转换
     * @param tablePerson 人员数据
     * @param permission 人员权限数据
     * @return 周期数据
             */
    String getWorkingDaysString(TablePerson tablePerson, TablePersonPermission permission);

    /**
     * 时间转换
     * @param tablePerson 人员数据
     * @param permission 人员权限数据
     * @return 时间数据
     */
    LinkedHashMap<String, String> getTimeMap(TablePerson tablePerson, TablePersonPermission permission);

    /**
     * 释放相关资源
     */
    void release();
}
