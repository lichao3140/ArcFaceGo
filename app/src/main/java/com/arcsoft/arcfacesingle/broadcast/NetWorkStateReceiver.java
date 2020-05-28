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

package com.arcsoft.arcfacesingle.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.arcsoft.arcfacesingle.server.api.LocalHttpApiDataUtils;
import com.arcsoft.arcfacesingle.server.api.LocalHttpImpl;
import com.arcsoft.arcfacesingle.service.CloudAIotService;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.asg.libcommon.util.common.NetworkUtils;
import com.arcsoft.asg.libcommon.util.common.Utils;
import com.arcsoft.asg.libnetwork.helper.LocalHttpHelper;

public class NetWorkStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (CommonUtils.isOfflineLanAppMode()) {
            if(NetworkUtils.isConnected()) {
                LocalHttpHelper.getInstance().setLocalHttpCallback(LocalHttpImpl.getInstance());
                LocalHttpHelper.getInstance().startServer(LocalHttpApiDataUtils.getLocalApiPort());
            }else {
                LocalHttpHelper.getInstance().stopServer();
            }
        }
        if (CommonUtils.isCloudAiotAppMode() && NetworkUtils.isConnected()) {
            if (!CommonUtils.isServiceRunning(Utils.getApp(), CloudAIotService.class.getName())) {
                CloudAIotService.start();
            } else {
                CloudAIotService.connectArcLink();
            }
        }
    }
}
