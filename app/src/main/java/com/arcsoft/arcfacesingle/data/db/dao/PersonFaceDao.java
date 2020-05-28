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

package com.arcsoft.arcfacesingle.data.db.dao;

import com.arcsoft.arcfacesingle.data.db.DBManager;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonFace;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonFace_Table;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.List;

public class PersonFaceDao {

    private PersonFaceDao() {
    }

    private static final class SingletonHolder {
        private static final PersonFaceDao INSTANCE = new PersonFaceDao();
    }

    public static PersonFaceDao getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 获取表数据总数
     *
     * @return
     */
    public long getTotalCount() {
        return SQLite
                .selectCountOf()
                .from(TablePersonFace.class)
                .count();
    }

    /**
     * 判断数据是否存在
     * @return
     */
    public boolean existFace(String personSerial) {
        return SQLite.selectCountOf()
                .from(TablePersonFace.class)
                .where(TablePersonFace_Table.personSerial.eq(personSerial))
                .count() > 0;
    }

    /**
     * 查询所有人脸数据
     * @return
     */
    public List<TablePersonFace> queryAllFace() {
        return SQLite
                .select()
                .from(TablePersonFace.class)
                .where()
                .queryList();
    }

    /**
     * 添加一个人脸注册照
     * @param tablePersonFace
     */
    public boolean addPersonFace(TablePersonFace tablePersonFace) {
        return tablePersonFace.save();
    }

    /**
     * 更新一个人脸注册照
     * @param tablePersonFace
     */
    public boolean updatePersonFace(TablePersonFace tablePersonFace) {
        return tablePersonFace.update();
    }

    /**
     * 根据人员Id查询人脸列表
     * @param personSerial
     * @return
     */
    public List<TablePersonFace> getListByPersonSerial(String personSerial) {
        return SQLite
                .select()
                .from(TablePersonFace.class)
                .where(TablePersonFace_Table.personSerial.eq(personSerial))
                .queryList();
    }

    /**
     * 根据人员Id查询人脸列表
     * @param personSerial
     * @return
     */
    public TablePersonFace getPersonFaceBySerial(String personSerial) {
        return SQLite
                .select()
                .from(TablePersonFace.class)
                .where(TablePersonFace_Table.personSerial.eq(personSerial))
                .querySingle();
    }

    /**
     * 根据人员Id查询人脸列表
     * @param faceId
     * @return
     */
    public TablePersonFace getPersonFaceByFaceId(String faceId) {
        return SQLite
                .select()
                .from(TablePersonFace.class)
                .where(TablePersonFace_Table.faceId.eq(faceId))
                .querySingle();
    }

    /**
     * 删除人脸
     * @param tablePersonFace
     * @return
     */
    public boolean deletePersonFace(TablePersonFace tablePersonFace) {
        return tablePersonFace.delete();
    }

    /**
     * 删除数据
     *
     * @param personId
     * @return
     */
    public List<TablePersonFace> getPersonListById(long personId) {
        return SQLite.select()
                .from(TablePersonFace.class)
                .where(TablePersonFace_Table.personId.eq(personId))
                .queryList();
    }

    /**
     * 删除数据
     *
     * @param id
     * @return
     */
    public TablePersonFace getPersonById(long id) {
        return SQLite.select()
                .from(TablePersonFace.class)
                .where(TablePersonFace_Table.id.eq(id))
                .querySingle();
    }

    /**
     * 获取数据
     *
     * @param faceId
     * @return
     */
    public List<TablePersonFace> getPersonsByFaceId(String faceId) {
        return SQLite.select()
                .from(TablePersonFace.class)
                .where(TablePersonFace_Table.faceId.eq(faceId))
                .queryList();
    }

    /**
     * 获取数据
     *
     * @param faceId
     * @return
     */
    public TablePersonFace getPersonByFaceId(String faceId) {
        return SQLite.select()
                .from(TablePersonFace.class)
                .where(TablePersonFace_Table.faceId.eq(faceId))
                .querySingle();
    }

    public void deleteTable() {
        Delete.table(TablePersonFace.class);
    }

    public void deleteListAsync(List<TablePersonFace> deleteList, OnDeleteListener listener) {
        FlowManager.getDatabase(DBManager.class).beginTransactionAsync(databaseWrapper -> {
            for (TablePersonFace face : deleteList) {
                face.delete(databaseWrapper);
            }
        }).success(transaction -> {
            if (listener != null) {
                listener.onSuccess();
            }
        }).error((transaction, error) -> {
            if (listener != null) {
                listener.onError();
            }
        }).build().execute();
    }

    public interface OnDeleteListener {

        /**
         * 删除成功
         */
        void onSuccess();

        /**
         * 删除失败
         */
        void onError();
    }
}
