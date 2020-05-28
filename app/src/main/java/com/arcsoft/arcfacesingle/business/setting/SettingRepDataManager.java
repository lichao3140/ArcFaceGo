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

package com.arcsoft.arcfacesingle.business.setting;

import com.arcsoft.arcfacesingle.data.db.table.TableSettingConfigInfo;

public class SettingRepDataManager {

    private static volatile SettingRepDataManager INSTANCE;

    public static SettingRepDataManager getInstance() {
        if (INSTANCE == null) {
            synchronized (SettingRepDataManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SettingRepDataManager();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * IR活体检测是否开启
     * @return
     */
    public boolean isFaceLiveOpen(TableSettingConfigInfo configInfo) {
        int liveType = configInfo.getLiveDetectType();
        return liveType != ConfigConstants.DEFAULT_LIVE_DETECT_CLOSE;
    }

    /**
     * IR活体检测是否开启
     * @return
     */
    public boolean isIrFaceLiveOpen(TableSettingConfigInfo configInfo) {
        int liveType = configInfo.getLiveDetectType();
        return liveType == ConfigConstants.DEFAULT_LIVE_DETECT_IR;
    }

    /**
     * IR相机预览是否开启
     * @return
     */
    public boolean isIrFaceLivePreviewShow(TableSettingConfigInfo configInfo) {
        return configInfo.getIrLivePreview() == ConfigConstants.DEFAULT_IR_LIVE_PREVIEW;
    }
}
