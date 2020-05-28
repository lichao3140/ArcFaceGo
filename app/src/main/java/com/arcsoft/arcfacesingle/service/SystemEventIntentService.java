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

package com.arcsoft.arcfacesingle.service;

import android.app.IntentService;
import android.content.Intent;

import com.arcsoft.arcfacesingle.util.SdcardUtils;
import com.arcsoft.asg.libcommon.util.common.FileUtils;

public class SystemEventIntentService extends IntentService {

    public static final String ACTION_CLEAN = "com.arcsoft.arcfacesingle.service.action.clean";

    public SystemEventIntentService() {
        super("SystemEventIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CLEAN.equals(action)) {
                cleanDeviceStorage();
            }
        }
    }

    private void cleanDeviceStorage() {
        FileUtils.deleteDir(SdcardUtils.getInstance().getIrFdFailDirPath());
        FileUtils.deleteDir(SdcardUtils.getInstance().getIrLiveFailDirPath());
        FileUtils.deleteDir(SdcardUtils.getInstance().getIrLiveSuccessDirPath());
        FileUtils.deleteDir(SdcardUtils.getInstance().getIrRgbCompareFailDirPath());
        FileUtils.deleteDir(SdcardUtils.getInstance().getIrProcessFailDirPath());
    }

}
