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
import java.util.List;

public class RequestPersonAddList implements Serializable {

    private boolean registerComplete;

    private int threadCount;

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public boolean isRegisterComplete() {
        return registerComplete;
    }

    public void setRegisterComplete(boolean registerComplete) {
        this.registerComplete = registerComplete;
    }

    private List<RequestPersonAdd> personAddList;

    public List<RequestPersonAdd> getPersonAddList() {
        return personAddList;
    }

    public void setPersonAddList(List<RequestPersonAdd> personAddList) {
        this.personAddList = personAddList;
    }
}
