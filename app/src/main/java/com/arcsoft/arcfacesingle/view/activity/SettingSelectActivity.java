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
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.base.BaseBusinessActivity;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.asg.libcommon.util.business.DoubleClickUtils;
import com.arcsoft.asg.libcommon.util.common.SPUtils;

public class SettingSelectActivity extends BaseBusinessActivity implements View.OnClickListener {

    private Button btnDeviceAccess;
    private ImageView ivPackageConfirm;
    private ImageView ivPackageEdit;
    private EditText etPackageDetail;
    private TextView tvPackageDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_select);
        setClickEvent();
    }

    private void setClickEvent() {
        btnDeviceAccess = findViewById(R.id.btn_setting_select_device_access);
        ivPackageConfirm = findViewById(R.id.iv_broadcast_package_name_confirm);
        ivPackageEdit = findViewById(R.id.iv_broadcast_package_name_edit);
        etPackageDetail = findViewById(R.id.et_broadcast_package_name_detail);
        tvPackageDetail = findViewById(R.id.tv_broadcast_package_name_detail);
        findViewById(R.id.btn_setting_select_device_active).setOnClickListener(this);
        findViewById(R.id.btn_setting_select_mode_change).setOnClickListener(this);
        findViewById(R.id.btn_setting_select_device_adaptation).setOnClickListener(this);
        findViewById(R.id.btn_top_bar_close).setOnClickListener(this);
        btnDeviceAccess.setOnClickListener(this);
        ivPackageConfirm.setOnClickListener(this);
        ivPackageEdit.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        btnDeviceAccess.setVisibility(CommonUtils.isCloudAiotAppMode() ? View.VISIBLE : View.GONE);
        String packageName = SPUtils.getInstance().getString(Constants.SP_KEY_BROADCAST_PACKAGE_NAME);
        if (TextUtils.isEmpty(packageName)) {
            etPackageDetail.setVisibility(View.VISIBLE);
            tvPackageDetail.setVisibility(View.GONE);
            ivPackageConfirm.setVisibility(View.VISIBLE);
            ivPackageEdit.setVisibility(View.GONE);
            etPackageDetail.setText("");
            tvPackageDetail.setText("");
        } else {
            etPackageDetail.setVisibility(View.GONE);
            tvPackageDetail.setVisibility(View.VISIBLE);
            ivPackageConfirm.setVisibility(View.GONE);
            ivPackageEdit.setVisibility(View.VISIBLE);
            etPackageDetail.setText(packageName);
            tvPackageDetail.setText(packageName);
        }
    }

    @Override
    public void onClick(View v) {
        int resId = v.getId();
        if (DoubleClickUtils.isFastDoubleClick(resId)) {
            return;
        }
        switch (resId) {
            case R.id.btn_top_bar_close:
                finish();
                break;
            case R.id.btn_setting_select_device_active:
                Intent intent = new Intent(SettingSelectActivity.this, DeviceActiveActivity.class);
                intent.putExtra(Constants.SP_KEY_FROM_SPLASH, false);
                startActivity(intent);
                break;
            case R.id.btn_setting_select_mode_change:
                Intent intent2 = new Intent(SettingSelectActivity.this, SelectModeActivity.class);
                startActivity(intent2);
                break;
            case R.id.btn_setting_select_device_access:
                Intent intent3 = new Intent(SettingSelectActivity.this, DeviceAccessActivity.class);
                startActivity(intent3);
                break;
            case R.id.btn_setting_select_device_adaptation:
                Intent intent4 = new Intent(SettingSelectActivity.this, AdaptationActivity.class);
                intent4.putExtra(Constants.SP_KEY_FROM_SPLASH, false);
                startActivity(intent4);
                break;
            case R.id.iv_broadcast_package_name_edit:
                etPackageDetail.setVisibility(View.VISIBLE);
                tvPackageDetail.setVisibility(View.GONE);
                ivPackageConfirm.setVisibility(View.VISIBLE);
                ivPackageEdit.setVisibility(View.GONE);
                break;
            case R.id.iv_broadcast_package_name_confirm:
                String nameDetail = etPackageDetail.getText().toString();
                if (!"".equals(nameDetail)) {
                    nameDetail = nameDetail.replaceAll(" ", "").trim();
                }
                SPUtils.getInstance().put(Constants.SP_KEY_BROADCAST_PACKAGE_NAME, nameDetail);
                if (!TextUtils.isEmpty(nameDetail)) {
                    etPackageDetail.setVisibility(View.GONE);
                    tvPackageDetail.setVisibility(View.VISIBLE);
                    ivPackageConfirm.setVisibility(View.GONE);
                    ivPackageEdit.setVisibility(View.VISIBLE);
                }
                finish();
                break;
            default:
                break;
        }
    }
}
