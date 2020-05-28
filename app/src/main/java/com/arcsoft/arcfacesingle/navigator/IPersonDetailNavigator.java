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

package com.arcsoft.arcfacesingle.navigator;

import java.util.LinkedHashMap;

public interface IPersonDetailNavigator {

    /**
     * 更新时段UI
     * @param timeMap 时段数据
     */
    void updateTimePermission(LinkedHashMap<String, String> timeMap);

    /**
     * 更新日期UI
     * @param hasPermission 是否有权限
     */
    void updateDateUi(boolean hasPermission);
}
