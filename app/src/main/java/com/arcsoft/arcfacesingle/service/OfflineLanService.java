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

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.arcsoft.asg.libcommon.util.common.AppUtils;
import com.arcsoft.asg.libcommon.util.common.Utils;

public class OfflineLanService extends Service {

    public static final String ACTION_POST_IC_EVENT_SERVICE = AppUtils.getAppPackageName() + ".POST_IC_EVENT_SERVICE";

    private static LanServiceBinder binder;
    private static ServiceConnection connection;

    public static void start() {
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binder = (LanServiceBinder) service;
                binder.start();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                if (binder != null && binder.isBinderAlive()) {
                    binder.stop();
                }
                binder = null;
            }
        };
        Intent intent = new Intent(Utils.getApp(), OfflineLanService.class);
        Utils.getApp().bindService(intent, connection, BIND_AUTO_CREATE);
    }

    public static void stop() {
        if (binder != null) {
            binder.stop();
            binder = null;
        }
         if (connection != null) {
            Utils.getApp().unbindService(connection);
            connection = null;
        }
    }

    public static void startReboot() {
        if (binder != null) {
            binder.startReboot();
        }
    }

    public static void stopReboot() {
        if (binder != null) {
            binder.stopReboot();
        }
    }

    public static void postIcEvent(Intent intent) {
        if (binder != null) {
            binder.postIcEvent(intent);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new LanServiceBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stop();
    }
}
