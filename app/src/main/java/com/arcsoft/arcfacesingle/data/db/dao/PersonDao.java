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

import com.arcsoft.arcfacesingle.data.db.table.TablePerson;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonFace_Table;
import com.arcsoft.arcfacesingle.data.db.table.TablePerson_Table;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.List;

public class PersonDao {

    public static final int TYPE_PERSON_INFO_ONLY_IC_CARD = 1;
    public static final int TYPE_PERSON_INFO_ONLY_FACE = 2;
    public static final int TYPE_PERSON_INFO_BOTH = 3;

    private PersonDao() {
    }

    private static final class SingletonHolder {
        private static final PersonDao INSTANCE = new PersonDao();
    }

    public static PersonDao getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 获取所有数据计数
     *
     * @return
     */
    public long getTotalCount() {
        return SQLite
                .selectCountOf()
                .from(TablePerson.class)
                .count();
    }

    /**
     * 查询所有数据
     *
     * @return
     */
    public List<TablePerson> queryAllPerson() {
        return SQLite
                .select()
                .from(TablePerson.class)
                .where()
                .orderBy(TablePerson_Table.id, false)
                .queryList();
    }

    public void getDatasAsync(QueryPersonCallback callback) {
        SQLite.select()
                .from(TablePerson.class)
                .orderBy(TablePerson_Table.id.desc())
                .async()
                .queryListResultCallback((transaction, tResult) -> {
                    if (tResult.size() > 0) {
                        if (callback != null) {
                            callback.onListQueryResult(tResult);
                        }
                    } else {
                        if (callback != null) {
                            callback.onListQueryFailed();
                        }
                    }
                });
    }

    /**
     * 判断数据是否存在
     * @return
     */
    public boolean existPerson(String personSerial) {
        return SQLite.selectCountOf()
                .from(TablePerson.class)
                .where(TablePerson_Table.personSerial.eq(personSerial))
                .count() > 0;
    }

    /**
     * 查询数据
     * @param personSerial
     * @return
     */
    public TablePerson getPersonByPersonSerial(String personSerial) {
        return SQLite.select()
                .from(TablePerson.class)
                .where(TablePerson_Table.personSerial.eq(personSerial))
                .querySingle();
    }

    /**
     * 查询数据
     * @param id
     * @return
     */
    public TablePerson getPersonById(long id) {
        return SQLite.select()
                .from(TablePerson.class)
                .where(TablePersonFace_Table.id.eq(id))
                .querySingle();
    }

    /**
     * 添加数据
     *
     * @param tablePerson
     */
    public boolean addPerson(TablePerson tablePerson) {
        return tablePerson.save();
    }

    /**
     * 更新数据
     *
     * @param tablePerson
     * @return
     */
    public boolean updatePerson(TablePerson tablePerson) {
        return tablePerson.update();
    }

    /**
     * 删除数据
     *
     * @return
     */
    public boolean deleteByPersonSerial(String personSerial) {
        TablePerson tablePerson = getPersonByPersonSerial(personSerial);
        if (null == tablePerson) {
            return false;
        }
        return tablePerson.delete();
    }

    /**
     * 删除数据
     *
     * @return
     */
    public void deleteModelByPersonSerial(String personSerial) {
        SQLite.delete()
                .from(TablePerson.class)
                .where(TablePerson_Table.personSerial.eq(personSerial))
                .execute();
    }

    /**
     * 删除数据
     *
     * @param person
     * @return
     */
    public boolean deletePerson(TablePerson person) {
        return person.delete();
    }

    /**
     * 获取分页数据
     *
     * @param pageSize
     * @param pageIndex
     * @param updateTime
     * @return
     */
    public List<TablePerson> getPersonPage(int pageSize, int pageIndex, long updateTime) {
        return SQLite
                .select()
                .from(TablePerson.class)
                .where(TablePerson_Table.updateTime.between(updateTime).and(System.currentTimeMillis()))
                .limit(pageSize)
                .offset(pageIndex * pageSize)
                .queryList();
    }

    /**
     * 获取给定时间范围内符合条件的数据计数
     *
     * @param updateTime
     * @return
     */
    public long getPersonFacePageCount(long updateTime) {
        return SQLite
                .selectCountOf()
                .from(TablePerson.class)
                .where(TablePerson_Table.updateTime.between(updateTime).and(System.currentTimeMillis()))
                .count();
    }

    /**
     * 查询数据
     *
     * @param personId
     * @return
     */
    public TablePerson getPersonByPersonIdAndPersonSetId(String personId, String personSetId) {
        return SQLite.select()
                .from(TablePerson.class)
                .where(TablePerson_Table.personId.eq(personId))
                .and(TablePerson_Table.personSetId.eq(personSetId))
                .querySingle();
    }

    /**
     * 获取所有带IC卡编号的人员
     *
     * @return
     */
    public List<TablePerson> getPersonListWithIcCardNo() {
        return SQLite.select()
                .from(TablePerson.class)
                .where(TablePerson_Table.icCardNo.isNotNull())
                .and(TablePerson_Table.icCardNo.isNot(""))
                .queryList();
    }

    /**
     * 异步模糊查询
     *
     * @param tag
     * @return
     */
    public List<TablePerson> getPersonListByTag(String tag) {
        return SQLite.select()
                .from(TablePerson.class)
                .where(TablePerson_Table.personName.like("%" + tag + "%"))
                .or(TablePerson_Table.personInfoNo.like("%" + tag + "%"))
                .queryList();
    }

    /**
     * 异步模糊查询
     *
     * @param tag
     * @param callback
     */
    public void getPersonListLikeTagAsync(String tag, QueryPersonCallback callback) {
        SQLite.select().from(TablePerson.class)
                .where(TablePerson_Table.personName.like("%" + tag + "%"))
                .or(TablePerson_Table.personInfoNo.like("%" + tag + "%"))
                .orderBy(TablePerson_Table.id.desc())
                .async()
                .queryListResultCallback((transaction, tResult) -> {
                    if (callback != null) {
                        callback.onListQueryResult(tResult);
                    }
                }).execute();
    }

    /**
     * 异步模糊查询
     *
     * @param tag
     * @param callback
     */
    public void getPersonListEqTagAsync(String tag, QueryPersonCallback callback) {
        SQLite.select().from(TablePerson.class)
                .where(TablePerson_Table.personName.eq(tag))
                .or(TablePerson_Table.personInfoNo.eq(tag))
                .orderBy(TablePerson_Table.id.desc())
                .async()
                .queryListResultCallback((transaction, tResult) -> {
                    if (callback != null) {
                        callback.onListQueryResult(tResult);
                    }
                }).execute();
    }

    public void deleteTable() {
        Delete.table(TablePerson.class);
    }

    public interface QueryPersonCallback {

        /**
         * 列表查询
         * @param personList 人员数据集合
         */
        void onListQueryResult(List<TablePerson> personList);

        /**
         * 人员列表查询失败
         */
        void onListQueryFailed();
    }
}
