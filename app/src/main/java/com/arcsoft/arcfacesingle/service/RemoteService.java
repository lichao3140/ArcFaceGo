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

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.arcsoft.arcfacesingle.charge.IHeartbeatInterface;
import com.arcsoft.asg.libcommon.util.common.ActivityUtils;

public class RemoteService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IHeartbeatInterface.Stub mBinder = new IHeartbeatInterface.Stub() {

        @Override
        public boolean getAliveResult() {
            Activity activity = ActivityUtils.getTopActivity();
            return null != activity;
        }

        @Override
        public void setAliveResult(boolean alive) {
        }
    };
}
