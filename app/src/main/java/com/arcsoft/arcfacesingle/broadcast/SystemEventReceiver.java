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

import com.arcsoft.arcfacesingle.service.SystemEventIntentService;

import static com.arcsoft.arcfacesingle.service.SystemEventIntentService.ACTION_CLEAN;

public class SystemEventReceiver extends BroadcastReceiver {
    final static String STORAGE_LOW = "android.intent.action.ACTION_DEVICE_STORAGE_LOW";
    final static String STORAGE_OK = "android.intent.action.ACTION_DEVICE_STORAGE_OK";

    @Override
    public void onReceive(Context context, Intent intent){
        String event = intent.getAction();
        if (event.equals(STORAGE_LOW)) {
            Intent serviceIntent = new Intent(context,SystemEventIntentService.class);
            serviceIntent.setAction(ACTION_CLEAN);
            context.startService(serviceIntent);
        }
    }
}
