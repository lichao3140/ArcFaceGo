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
import com.arcsoft.arcfacesingle.data.db.table.TablePerson;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonPermission;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonPermission_Table;
import com.arcsoft.arcfacesingle.data.db.table.TablePerson_Table;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.List;

public class PersonPermissionDao {

    private PersonPermissionDao() {
    }

    private static final class SingletonHolder {
        private static final PersonPermissionDao INSTANCE = new PersonPermissionDao();
    }

    public static PersonPermissionDao getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 判断数据是否存在
     * @return
     */
    public boolean existModel(String personSerial) {
        return SQLite.selectCountOf()
                .from(TablePerson.class)
                .where(TablePerson_Table.personSerial.eq(personSerial))
                .count() > 0;
    }

    /**
     * 获取所有数据计数
     *
     * @return
     */
    public long getTotalCount() {
        return SQLite
                .selectCountOf()
                .from(TablePersonPermission.class)
                .count();
    }

    /**
     * 查询数据
     * @param personSerial
     * @return
     */
    public void deleteModelBySerial(String personSerial) {
        SQLite.delete()
                .from(TablePersonPermission.class)
                .where(TablePersonPermission_Table.personSerial.eq(personSerial))
                .execute();
    }

    /**
     * 更新数据
     *
     * @param permission
     * @return
     */
    public boolean updateModel(TablePersonPermission permission) {
        return permission.update();
    }

    /**
     * 新增数据
     *
     * @param permission
     * @return
     */
    public boolean addModel(TablePersonPermission permission) {
        return permission.save();
    }

    /**
     * 获取所有数据
     *
     * @return
     */
    public List<TablePersonPermission> getAllList() {
        return SQLite
                .select()
                .from(TablePersonPermission.class)
                .where()
                .orderBy(TablePersonPermission_Table.id, false)
                .queryList();
    }

    /**
     * 获取集合数据
     *
     * @return
     */
    public List<TablePersonPermission> getListByPersonSerial(String personSerial) {
        return SQLite.select()
                .from(TablePersonPermission.class)
                .where(TablePersonPermission_Table.personSerial.eq(personSerial))
                .queryList();
    }

    /**
     * 获取单个数据
     *
     * @return
     */
    public TablePersonPermission getModelByPersonSerial(String personSerial) {
        return SQLite.select()
                .from(TablePersonPermission.class)
                .where(TablePersonPermission_Table.personSerial.eq(personSerial))
                .querySingle();
    }

    /**
     * 删除数据
     *
     * @param permission
     * @return
     */
    public boolean deleteModel(TablePersonPermission permission) {
        return permission.delete();
    }

    /**
     * 批量新增
     */
    public void saveListTransaction(List<TablePersonPermission> permissions, OnPermissionListener listener) {
        if (permissions == null || permissions.isEmpty()) {
            if (listener != null) {
                listener.onError("");
            }
            return;
        }
        FlowManager.getDatabase(DBManager.class).beginTransactionAsync(databaseWrapper -> {
            for (TablePersonPermission permission : permissions) {
                permission.save(databaseWrapper);
            }
        }).success(transaction -> {
            if (listener != null) {
                listener.onSuccess();
            }
        }).error((transaction, error) -> {
            if (listener != null) {
                listener.onError(error.getMessage());
            }
        }).build().execute();
    }

    /**
     * 批量删除
     */
    public void deleteListTransaction(List<TablePersonPermission> permissions, OnPermissionListener listener) {
        if (permissions == null || permissions.isEmpty()) {
            if (listener != null) {
                listener.onError("");
            }
            return;
        }
        FlowManager.getDatabase(DBManager.class).beginTransactionAsync(databaseWrapper -> {
            for (TablePersonPermission permission : permissions) {
                permission.delete(databaseWrapper);
            }
        }).success(transaction -> {
            if (listener != null) {
                listener.onSuccess();
            }
        }).error((transaction, error) -> {
            if (listener != null) {
                listener.onError(error.getMessage());
            }
        }).build().execute();
    }

    public void deleteTable() {
        Delete.table(TablePersonPermission.class);
    }

    public interface OnPermissionListener {

        /**
         * 成功
         */
        void onSuccess();

        /**
         * 失败
         * @param msg 失败信息
         */
        void onError(String msg);
    }
}
