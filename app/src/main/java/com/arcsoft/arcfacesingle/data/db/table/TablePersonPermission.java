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
import com.arcsoft.arcfacesingle.server.api.ServerConstants;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

@Table(database = DBManager.class)
public class TablePersonPermission  extends BaseModel {

    public static final int PRIORITY_HIGH = 1;

    @PrimaryKey(autoincrement = true)
    private long id;

    /**
     * tablePerson表中personSerial
     */
    @Column
    private String personSerial;

    /**
     * 权限开始日期，格式如“2020-03-04”
     */
    @Column
    private String startDate = ServerConstants.DEFAULT_START_DATE;

    /**
     * 权限结束日期，格式如“2020-03-04”
     */
    @Column
    private String endDate = ServerConstants.DEFAULT_END_DATE;

    /**
     * 工作日（周一~周日）
     */
    @Column
    private String workingDays = ServerConstants.DEFAULT_WORKING_DAYS;

    /**
     * 权限有效范围和描述（时分秒~时分秒）
     */
    @Column
    private String timeAndDesc;

    /**
     * 权限优先级（默认最高）
     */
    @Column
    private int priority = PRIORITY_HIGH;

    /**
     * 权限更新时间
     */
    @Column
    private long updateTime = System.currentTimeMillis();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPersonSerial() {
        return personSerial;
    }

    public void setPersonSerial(String personSerial) {
        this.personSerial = personSerial;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(String workingDays) {
        this.workingDays = workingDays;
    }

    public String getTimeAndDesc() {
        return timeAndDesc;
    }

    public void setTimeAndDesc(String timeAndDesc) {
        this.timeAndDesc = timeAndDesc;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }
}
