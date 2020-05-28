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

import android.text.TextUtils;

import com.arcsoft.arcfacesingle.data.db.DBManager;
import com.arcsoft.arcfacesingle.data.db.table.TableSettingConfigInfo;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.SQLite;

public class SettingConfigInfoDao {

    private SettingConfigInfoDao() {
    }

    private static final class SingletonHolder {
        private static final SettingConfigInfoDao INSTANCE = new SettingConfigInfoDao();
    }

    public static SettingConfigInfoDao getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 获取设置信息
     *
     * @return
     */
    public TableSettingConfigInfo getSetting() {
        return SQLite.select()
                .from(TableSettingConfigInfo.class)
                .querySingle();
    }

    /**
     * 保存设置信息
     *
     * @param tableSettingConfigInfo
     * @param callback
     */
    public void saveSetting(TableSettingConfigInfo tableSettingConfigInfo, SettingConfigCallback callback) {
        FlowManager.getDatabase(DBManager.class).beginTransactionAsync(databaseWrapper ->
                tableSettingConfigInfo.save(databaseWrapper))
                .success(transaction -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .error((transaction, error) -> {
                })
                .build()
                .executeSync();
    }

    /**
     * 保存设置信息
     *
     * @param tableSettingConfigInfo
     */
    public boolean saveSetting(TableSettingConfigInfo tableSettingConfigInfo) {
        return tableSettingConfigInfo.save();
    }

    /**
     * 更新主logo地址
     *
     * @param path
     * @return
     */
    public void updateMainLogoPathSync(String path) {
        FlowManager.getDatabase(DBManager.class).beginTransactionAsync(databaseWrapper -> {
            TableSettingConfigInfo configInfo = getSetting();
            configInfo.setMainImagePath(TextUtils.isEmpty(path) ? "" : path);
            configInfo.update(databaseWrapper);
        }).success(transaction -> {
        }).error((transaction, error) -> {
        }).build().executeSync();
    }

    /**
     * 更新副logo地址
     *
     * @param path
     * @return
     */
    public void updateSecondLogoPathSync(String path) {
        FlowManager.getDatabase(DBManager.class).beginTransactionAsync(databaseWrapper -> {
            TableSettingConfigInfo configInfo = getSetting();
            configInfo.setViceImagePath(TextUtils.isEmpty(path) ? "" : path);
            configInfo.update(databaseWrapper);
        }).success(transaction -> {
        }).error((transaction, error) -> {
        }).build().executeSync();
    }

    public void updateDeviceName(String deviceName) {
        FlowManager.getDatabase(DBManager.class).beginTransactionAsync(databaseWrapper -> {
            TableSettingConfigInfo configInfo = getSetting();
            configInfo.setDeviceName(deviceName);
            configInfo.update(databaseWrapper);
        }).success(transaction -> {
        }).error((transaction, error) -> {
        }).build().executeSync();
    }

    /**
     * 更新设置信息
     * t
     *
     * @param tableSettingConfigInfo
     * @return
     */
    public boolean updateSetting(TableSettingConfigInfo tableSettingConfigInfo) {
        return tableSettingConfigInfo.update();
    }

    /**
     * 删除设置信息
     *
     * @param tableSettingConfigInfo
     */
    public boolean deleteSetting(TableSettingConfigInfo tableSettingConfigInfo) {
        return tableSettingConfigInfo.delete();
    }

    /**
     * 清空表
     */
    public void clearAll() {
        Delete.table(TableSettingConfigInfo.class);
    }

    public interface SettingConfigCallback {

        /**
         * 成功
         */
        void onSuccess();
    }
}
