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

package com.arcsoft.arcfacesingle.data.event;

public class InstallPackageEvent {
    public static final int INSTALL_TYPE_NO_SILENCE = 0;
    public static final int INSTALL_TYPE_SILENCE = 1;

    public String path;
    public int type;

    public InstallPackageEvent(String path,int type) {
        this.path = path;
        this.type = type;
    }
}
