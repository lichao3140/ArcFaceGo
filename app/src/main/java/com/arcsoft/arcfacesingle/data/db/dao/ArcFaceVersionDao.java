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

import com.arcsoft.arcfacesingle.data.db.table.TableArcFaceVersion;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.List;

public class ArcFaceVersionDao {

    private ArcFaceVersionDao() {
    }

    private static final class SingletonHolder {
        private static final ArcFaceVersionDao INSTANCE = new ArcFaceVersionDao();
    }

    public static ArcFaceVersionDao getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 获取设置信息
     * @return
     */
    public TableArcFaceVersion getModel() {
        return SQLite.select()
                .from(TableArcFaceVersion.class)
                .querySingle();
    }

    /**
     * 异步保存
     * @param model
     */
    public void saveModelAsync(TableArcFaceVersion model) {
        model.async().save();
    }

    /**
     * 同步保存
     * @param model
     * @return
     */
    public boolean saveModel(TableArcFaceVersion model) {
        return model.save();
    }

    /**
     * 获取所有数据
     * @return
     */
    public List<TableArcFaceVersion> queryAllModels() {
        return SQLite
                .select()
                .from(TableArcFaceVersion.class)
                .where()
                .queryList();
    }
}
