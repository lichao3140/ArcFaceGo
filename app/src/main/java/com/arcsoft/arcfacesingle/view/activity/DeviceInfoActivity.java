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

package com.arcsoft.arcfacesingle.view.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.base.BaseBusinessActivity;
import com.arcsoft.arcfacesingle.databinding.ActivityDeviceInfoBinding;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.arcfacesingle.viewmodel.DeviceInfoViewModel;
import com.arcsoft.asg.libcommon.util.common.Utils;

public class DeviceInfoActivity extends BaseBusinessActivity implements DeviceInfoViewModel.DeviceInfoCallback {

    private DeviceInfoViewModel viewModel;
    private ActivityDeviceInfoBinding dataBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new DeviceInfoViewModel();
        viewModel.setCallback(this);
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_device_info);
        CommonUtils.setEditTextInputFilter(dataBinding.etDeviceValue);
        dataBinding.setViewModel(viewModel);
        dataBinding.customTopBar.setVisibleClose(true);
        dataBinding.customTopBar.setVisibleSkip(false);
        dataBinding.customTopBar.setVisibleTitle(true);
        dataBinding.customTopBar.setStringTitle(CommonUtils.getStrFromRes(R.string.device_info));
    }

    @Override
    public void onCallback() {
        Intent intent = new Intent(this, RecognizeActivity.class);
        startActivity(intent);
        Utils.finishAllActivity();
    }
}
