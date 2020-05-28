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
import android.os.Build;
import android.util.Base64;
import com.arcsoft.arcfacesingle.service.OfflineLanService;
import com.arcsoft.arcfacesingle.util.CommonUtils;

public class IntelligentCardEventReceiver extends BroadcastReceiver {

    public static final String ACTION_IC_NAME = "android.intent.action.IntelligentCard";

    private static final String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDG+eFDJJicO5kPijhEr5Z1BRTa/2GW7mQGuGhs\n" +
            "inWyX1BnpKe5yRctbw0fn+DC8t11ApDoJ2vdi5eGfmR7b38pm7osZpRXArO+yec6BfpJUSA3u3Q5\n" +
            "M5PbAFB2/gIBTxfkTr4pZkXUHyvk6lby6XBZrrTyBHxPpWvIZf+AizODWQIDAQAB\n";

    public static final String INTENT_NAME_CONTENT = "content";
    public static final String INTENT_NAME_RECEIVE_CONTENT = "content";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(ACTION_IC_NAME)) {
            String data = intent.getStringExtra(INTENT_NAME_RECEIVE_CONTENT);
            if (data != null && !data.isEmpty()) {
                if (CommonUtils.isOfflineLanAppMode()) {
                    byte[] content = Base64.decode(data, Base64.DEFAULT);
                    Intent icIntent = new Intent(context, OfflineLanService.class);
                    icIntent.setAction(OfflineLanService.ACTION_POST_IC_EVENT_SERVICE);
                    icIntent.putExtra(INTENT_NAME_CONTENT, content);
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        context.startForegroundService(icIntent);
//                    } else {
//                        context.startService(icIntent);
//                    }
                    OfflineLanService.postIcEvent(icIntent);
                }
            }
        }
    }
}
