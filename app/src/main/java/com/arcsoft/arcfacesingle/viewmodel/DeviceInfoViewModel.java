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

package com.arcsoft.arcfacesingle.viewmodel;

import android.text.TextUtils;
import android.view.View;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.ObservableField;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.business.common.CommonRepository;
import com.arcsoft.arcfacesingle.data.db.dao.SettingConfigInfoDao;
import com.arcsoft.arcfacesingle.data.db.table.TableSettingConfigInfo;
import com.arcsoft.asg.libcommon.util.common.ToastUtils;

public class DeviceInfoViewModel extends BaseObservable {

    private DeviceInfoCallback callback;

    @Bindable
    public final ObservableField<String> fieldDeviceName = new ObservableField<>();

    public DeviceInfoViewModel() {
        TableSettingConfigInfo configInfo = CommonRepository.getInstance().getSettingConfigInfo();
        fieldDeviceName.set(configInfo.getDeviceName());
    }

    public void onClick(View view) {
        int resId = view.getId();
        if (resId == R.id.btn_save) {
            String deviceName = fieldDeviceName.get().trim();
            if (!TextUtils.isEmpty(deviceName)) {
                SettingConfigInfoDao.getInstance().updateDeviceName(deviceName);
                CommonRepository.getInstance().getSettingConfigInfo().setDeviceName(deviceName);
                ToastUtils.showLongToast(R.string.settings_save_success);
                if (callback != null) {
                    callback.onCallback();
                }

            } else {
                ToastUtils.showLongToast(R.string.device_name_tip);
            }
        }
    }

    public void setCallback(DeviceInfoCallback callback) {
        this.callback = callback;
    }

    public interface DeviceInfoCallback {
        void onCallback();
    }
}
