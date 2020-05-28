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

public class RequestSetLogo implements Serializable {

    private static final int ADD_MAIN = 1;
    private static final int DELETE_MAIN = 2;
    private static final int ADD_SECOND = 3;
    private static final int DELETE_SECOND = 4;

    /**
     * 操作类型：1:设置主Logo 2:删除主Logo 3:设置副Logo 4:删除副Logo
     */
    private int operation;


    /**
     * 主logo
     */
    private String mainLogoBase64;

    /**
     * 主logoId
     */
    private String mainLogoId;

    /**
     * 副logo
     */
    private String viceLogoBase64;

    /**
     * 主logoId
     */
    private String secondLogoId;

    public int getOperation() {
        return operation;
    }

    public void setOperation(int operation) {
        this.operation = operation;
    }

    public String getMainLogoId() {
        return mainLogoId;
    }

    public void setMainLogoId(String mainLogoId) {
        this.mainLogoId = mainLogoId;
    }

    public String getSecondLogoId() {
        return secondLogoId;
    }

    public void setSecondLogoId(String secondLogoId) {
        this.secondLogoId = secondLogoId;
    }

    public String getMainLogoBase64() {
        return mainLogoBase64;
    }

    public void setMainLogoBase64(String mainLogoBase64) {
        this.mainLogoBase64 = mainLogoBase64;
    }

    public String getViceLogoBase64() {
        return viceLogoBase64;
    }

    public void setViceLogoBase64(String viceLogoBase64) {
        this.viceLogoBase64 = viceLogoBase64;
    }

    public boolean checkOperation() {
        return operation == ADD_MAIN || operation == DELETE_MAIN || operation == ADD_SECOND || operation == DELETE_SECOND;
    }

    public boolean addMain() {
        return operation == ADD_MAIN;
    }

    public boolean deleteMain() {
        return operation == DELETE_MAIN;
    }

    public boolean addSecond() {
        return operation == ADD_SECOND;
    }

    public boolean deleteSecond() {
        return operation == DELETE_SECOND;
    }
}
