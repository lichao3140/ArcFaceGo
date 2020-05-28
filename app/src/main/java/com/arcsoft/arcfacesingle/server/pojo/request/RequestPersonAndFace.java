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

import java.util.HashMap;

public class RequestPersonAndFace {

    private String requestHashcode;

    private String netSign;

    private String content;

    private RequestPersonAddList personAdd;

    private HashMap<String, byte[]> faceMap;

    public String getNetSign() {
        return netSign;
    }

    public void setNetSign(String netSign) {
        this.netSign = netSign;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setRequestHashcode(String requestHashcode) {
        this.requestHashcode = requestHashcode;
    }

    public String getRequestHashcode() {
        return requestHashcode;
    }

    public void setRequestHashCode(String requestHashcode) {
        this.requestHashcode = requestHashcode;
    }

    public HashMap<String, byte[]> getFaceMap() {
        return faceMap;
    }

    public void setFaceMap(HashMap<String, byte[]> faceMap) {
        this.faceMap = faceMap;
    }

    public RequestPersonAddList getPersonAdd() {
        return personAdd;
    }

    public void setPersonAdd(RequestPersonAddList personAdd) {
        this.personAdd = personAdd;
    }
}
