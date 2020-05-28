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

import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.data.db.DBManager;
import com.arcsoft.arcfacesingle.data.db.table.TableSignRecord;
import com.arcsoft.arcfacesingle.data.db.table.TableSignRecord_Table;
import com.arcsoft.asg.libcommon.util.common.FileUtils;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.List;

public class IdentifyRecordDao {

    /**
     * 类型一：新增的数据默认状态
     */
    public static final int STATUS_DEFAULT = 0;
    /**
     * 类型二：已上传文本，未上传图片
     */
    public static final int STATUS_TEXT = 1;
    /**
     * 类型三：已上传文本，已上传图片
     */
    public static final int STATUS_IMAGE = 2;
    /**
     * 类型四：IC卡刷卡记录默认状态
     */
    public static final int STATUS_IC_CARD = 3;
    /**
     * 类型五：服务器查不到相关人员记录或文本记录，设备端无法上传数据，数据会一直留存在本地，导致后期数据越来越多，需要优化
     */
    public static final int STATUS_IC_SERVER_DATA_INVALID = 4;

    public interface OnIdentifyListener {

        /**
         * 成功
         */
        void onSuccess();

        /**
         * 失败
         */
        void onError();
    }

    private IdentifyRecordDao() {
    }

    private static final class SingletonHolder {
        private static final IdentifyRecordDao INSTANCE = new IdentifyRecordDao();
    }

    public static IdentifyRecordDao getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 查询所有数据
     *
     * @return
     */
    public List<TableSignRecord> queryAllRecord() {
        return SQLite
                .select()
                .from(TableSignRecord.class)
                .where()
                .queryList();
    }

    /**
     * 获取表数据总数
     *
     * @return
     */
    public long getTotalCount() {
        return SQLite
                .selectCountOf()
                .from(TableSignRecord.class)
                .count();
    }

    public List<TableSignRecord> queryRecords(int count) {
        return SQLite.select().from(TableSignRecord.class).limit(count).queryList();
    }

    /**
     * 更新数据
     *
     * @param signRecord
     * @return
     */
    public void updateAsync(TableSignRecord signRecord) {
        signRecord.async().update();
    }

    /**
     * 分页获取刷脸记录文字
     *
     * @return
     */
    public List<TableSignRecord> getListStatusIsDefault(int pageSize) {
        return SQLite
                .select()
                .from(TableSignRecord.class)
                .where(TableSignRecord_Table.status.eq(STATUS_DEFAULT))
                .or(TableSignRecord_Table.status.eq(STATUS_IC_CARD))
                .or(TableSignRecord_Table.status.isNull())
                .orderBy(TableSignRecord_Table.id.asc())
                .limit(pageSize)
                .queryList();
    }

    /**
     * 分页获取刷脸记录文字
     *
     * @return
     */
    public List<TableSignRecord> getListByDefaultStatus(int pageSize) {
        return SQLite
                .select()
                .from(TableSignRecord.class)
                .where(TableSignRecord_Table.status.eq(STATUS_DEFAULT))
                .or(TableSignRecord_Table.status.isNull())
                .orderBy(TableSignRecord_Table.id.asc())
                .limit(pageSize)
                .queryList();
    }

    /**
     * 获取包含IC卡记录数
     *
     * @return
     */
    public long getCountIcCardDesc() {
        return SQLite
                .selectCountOf()
                .from(TableSignRecord.class)
                .where(TableSignRecord_Table.status.eq(IdentifyRecordDao.STATUS_IC_CARD))
                .orderBy(TableSignRecord_Table.id.desc())
                .count();
    }

    /**
     * 获取包含有IC卡刷脸记录
     *
     * @return
     */
    public List<TableSignRecord> getListWithIcCard() {
        return SQLite
                .select()
                .from(TableSignRecord.class)
                .where(TableSignRecord_Table.status.eq(STATUS_IC_CARD))
                .orderBy(TableSignRecord_Table.id.desc())
                .queryList();
    }

    /**
     * 获取status为STATUS_TEXT时的数据总数
     *
     * @return
     */
    public long getCountStatusIsTextDesc() {
        return SQLite
                .selectCountOf()
                .from(TableSignRecord.class)
                .where(TableSignRecord_Table.status.eq(IdentifyRecordDao.STATUS_TEXT))
                .count();
    }

    /**
     * 获取status为STATUS_TEXT时的数据集合
     *
     * @return
     */
    public List<TableSignRecord> getListStatusIsTextDesc() {
        return SQLite
                .select()
                .from(TableSignRecord.class)
                .where(TableSignRecord_Table.status.eq(STATUS_TEXT))
                .orderBy(TableSignRecord_Table.id.desc())
                .queryList();
    }

    /**
     * 获取status为STATUS_TEXT时的数据集合
     *
     * @return
     */
    public List<TableSignRecord> getListStatusIsText(int pageSize) {
        return SQLite
                .select()
                .from(TableSignRecord.class)
                .where(TableSignRecord_Table.status.eq(STATUS_TEXT))
                .orderBy(TableSignRecord_Table.id.asc())
                .limit(pageSize)
                .queryList();
    }

    public TableSignRecord getSignRecordByTime(long time) {
        return SQLite
                .select()
                .from(TableSignRecord.class)
                .where(TableSignRecord_Table.addTime.eq(time))
                .querySingle();
    }

    public TableSignRecord getSignRecordByRecordId(String recordId) {
        return SQLite
                .select()
                .from(TableSignRecord.class)
                .where(TableSignRecord_Table.recordId.eq(recordId))
                .querySingle();
    }

    /**
     * 异步添加单条数据
     */
    public void addModelTransactionAsync(TableSignRecord signRecord) {
        if (signRecord == null) {
            return;
        }
        if (FileUtils.getSdcardAvailableSize() > Constants.SDCARD_STORAGE_SIZE_DELETE) {
            FlowManager.getDatabase(DBManager.class)
                    .beginTransactionAsync(databaseWrapper -> signRecord.save(databaseWrapper))
                    .success(transaction -> {
                    })
                    .error((transaction, error) -> {
                    })
                    .build()
                    .execute();
        }
    }

    /**
     * 异步更新多条数据
     */
    public void updateListAsync(List<TableSignRecord> recordList, OnIdentifyListener listener) {
        if (recordList == null || recordList.isEmpty()) {
            if (listener != null) {
                listener.onError();
            }
            return;
        }
        FlowManager.getDatabase(DBManager.class).beginTransactionAsync(databaseWrapper -> {
            for (TableSignRecord signRecord : recordList) {
                signRecord.update(databaseWrapper);
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

    /**
     * 异步删除多条数据
     */
    public void deleteListTransactionAsync(List<TableSignRecord> recordList, OnIdentifyListener listener) {
        if (recordList == null || recordList.isEmpty()) {
            if (listener != null) {
                listener.onError();
            }
            return;
        }
        FlowManager.getDatabase(DBManager.class).beginTransactionAsync(databaseWrapper -> {
            for (TableSignRecord signRecord : recordList) {
                signRecord.delete(databaseWrapper);
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

    /**
     * 异步删除单条数据
     */
    public void deleteModelTransactionAsync(TableSignRecord signRecord) {
        if (signRecord == null) {
            return;
        }
        FlowManager.getDatabase(DBManager.class).beginTransactionAsync(databaseWrapper -> {
            signRecord.delete(databaseWrapper);
        }).success(transaction -> {
        }).error((transaction, error) -> {
        }).build().execute();
    }

    /**
     * 异步删除单条数据
     */
    public void deleteModelTransactionAsync(TableSignRecord signRecord, OnIdentifyListener listener) {
        if (signRecord == null) {
            return;
        }
        FlowManager.getDatabase(DBManager.class)
                .beginTransactionAsync(databaseWrapper -> signRecord.delete(databaseWrapper))
                .success(transaction -> {
                    if (listener != null) {
                        listener.onSuccess();
                    }
                })
                .error((transaction, error) -> {
                    if (listener != null) {
                        listener.onError();
                    }
                }).build().execute();
    }

    /**
     * 异步添加多条数据
     */
    public void updateAndDeleteListAsync(List<TableSignRecord> updateList, List<TableSignRecord> deleteList,
                                         OnIdentifyListener listener) {
        FlowManager.getDatabase(DBManager.class).beginTransactionAsync(databaseWrapper -> {
            for (TableSignRecord signRecord : updateList) {
                signRecord.update(databaseWrapper);
            }
            for (TableSignRecord signRecord : deleteList) {
                signRecord.delete(databaseWrapper);
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

    public void deleteTable() {
        Delete.table(TableSignRecord.class);
    }
}
