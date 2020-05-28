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

package com.arcsoft.arcfacesingle.app;

import android.annotation.SuppressLint;

import androidx.multidex.MultiDexApplication;

import com.arcsoft.arcfacesingle.BuildConfig;
import com.arcsoft.arcfacesingle.business.common.CommonRepository;
import com.arcsoft.arcfacesingle.data.db.table.TableSettingConfigInfo;
import com.arcsoft.arcfacesingle.util.SdcardUtils;
import com.arcsoft.arcfacesingle.util.business.KeyboardObserverHelper;
import com.arcsoft.asg.libcommon.util.common.LogUtils;
import com.arcsoft.asg.libcommon.util.common.Utils;
import com.arcsoft.asg.libnetwork.bean.NetworkException;
import com.arcsoft.asg.libnetwork.manage.DefaultRemoteApiManager;
import com.bumptech.glide.Glide;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;

import io.reactivex.plugins.RxJavaPlugins;
import me.jessyan.autosize.AutoSizeConfig;
import xcrash.ICrashCallback;
import xcrash.XCrash;

public class ArcFaceSingleApp extends MultiDexApplication {

    private static final String TAG = ArcFaceSingleApp.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
        LogUtils.getConfig()
                .setLogSwitch(BuildConfig.DEBUG)
                .setLog2ConsoleSwitch(BuildConfig.DEBUG);
        FlowManager.init(new FlowConfig.Builder(this).build());
        initCrash();
        initHttp();
        AutoSizeConfig.getInstance().setExcludeFontScale(true);
        KeyboardObserverHelper.getInstance().init(this);
        if (!BuildConfig.DEBUG) {
            RxJavaPlugins.setErrorHandler(throwable -> {
                
            });
        }
    }

    private void initHttp() {
        TableSettingConfigInfo tableSettingConfigInfo = CommonRepository.getInstance().getSettingConfigInfo();
        String ipAddress = tableSettingConfigInfo.getServerIp();
        int ipPort = Integer.parseInt(tableSettingConfigInfo.getServerPort());
        try {
            String httpUrl = "http://" + ipAddress + ":" + ipPort + "/";
            DefaultRemoteApiManager.getInstance().init(httpUrl);
        } catch (NetworkException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            Glide.get(this).clearMemory();
        }
        Glide.get(this).trimMemory(level);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Glide.get(this).clearMemory();
    }

    ICrashCallback callback = ((logPath, emergency) -> {
        if (!BuildConfig.DEBUG) {
            System.exit(1);
        }
    });

    @SuppressLint("MissingPermission")
    private void initCrash() {
        xcrash.XCrash.init(this, new XCrash.InitParameters()
                .setNativeCallback(callback)
                .setJavaCallback(callback)
                .setAnrCallback(callback)
                .setLogDir(SdcardUtils.getInstance().getCrashPathDir())
                .setNativeDumpAllThreads(false));
    }
}
