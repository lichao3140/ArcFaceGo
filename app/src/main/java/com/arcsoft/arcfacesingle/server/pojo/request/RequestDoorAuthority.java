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

package com.arcsoft.arcfacesingle.server.pojo.request;

import java.io.Serializable;

public class RequestDoorAuthority implements Serializable {

    /**
     * 人员编号
     */
    private String personSerial;

    /**
     * 人员权限名称
     */
    private String authorityName;

    /**
     * 权限早上生效时间
     */
    private String morningStartTime;

    /**
     * 权限早上失效时间
     */
    private String morningEndTime;

    /**
     * 权限中午生效时间
     */
    private String noonStartTime;

    /**
     * 权限中午失效时间
     */
    private String noonEndTime;

    /**
     * 权限晚上生效时间
     */
    private String nightStartTime;

    /**
     * 权限晚上失效时间
     */
    private String nightEndTime;

    public String getPersonSerial() {
        return personSerial;
    }

    public void setPersonSerial(String personSerial) {
        this.personSerial = personSerial;
    }

    public String getAuthorityName() {
        return authorityName;
    }

    public void setAuthorityName(String authorityName) {
        this.authorityName = authorityName;
    }

    public String getMorningStartTime() {
        return morningStartTime;
    }

    public void setMorningStartTime(String morningStartTime) {
        this.morningStartTime = morningStartTime;
    }

    public String getMorningEndTime() {
        return morningEndTime;
    }

    public void setMorningEndTime(String morningEndTime) {
        this.morningEndTime = morningEndTime;
    }

    public String getNoonStartTime() {
        return noonStartTime;
    }

    public void setNoonStartTime(String noonStartTime) {
        this.noonStartTime = noonStartTime;
    }

    public String getNoonEndTime() {
        return noonEndTime;
    }

    public void setNoonEndTime(String noonEndTime) {
        this.noonEndTime = noonEndTime;
    }

    public String getNightStartTime() {
        return nightStartTime;
    }

    public void setNightStartTime(String nightStartTime) {
        this.nightStartTime = nightStartTime;
    }

    public String getNightEndTime() {
        return nightEndTime;
    }

    public void setNightEndTime(String nightEndTime) {
        this.nightEndTime = nightEndTime;
    }
}
