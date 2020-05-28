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

public class RequestGetLogo implements Serializable {

    private static final int TYPE_MAIN = 0;

    private static final int TYPE_SECOND = 1;

    private static final int TYPE_BOTH = 2;

    private int type;

    private String mainLogoId;

    private String secondLogoId;

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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isMain() {
        return type == TYPE_MAIN;
    }

    public boolean isSecond() {
        return type == TYPE_SECOND;
    }

    public boolean isBoth() {
        return type == TYPE_BOTH;
    }
}
