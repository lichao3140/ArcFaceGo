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
import android.text.TextPaint;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.databinding.DataBindingUtil;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.base.BaseBusinessActivity;
import com.arcsoft.arcfacesingle.databinding.ActivityDeviceAccessBinding;
import com.arcsoft.arcfacesingle.navigator.DeviceAccessNavigator;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.arcfacesingle.view.dialog.ActiveResultDialog;
import com.arcsoft.arcfacesingle.view.dialog.CommonTipDialog;
import com.arcsoft.arcfacesingle.view.dialog.DeviceAccessConflictDialog;
import com.arcsoft.arcfacesingle.view.widgets.CustomTopBar;
import com.arcsoft.arcfacesingle.view.widgets.UpperCaseTransform;
import com.arcsoft.arcfacesingle.viewmodel.DeviceAccessViewModel;
import com.arcsoft.arcsoftlink.http.bean.res.DeviceInfo;
import com.arcsoft.asg.libcommon.util.business.DoubleClickUtils;
import com.arcsoft.asg.libcommon.util.common.ActivityUtils;
import com.arcsoft.asg.libcommon.util.common.Utils;

public class DeviceAccessActivity extends BaseBusinessActivity implements DeviceAccessNavigator {

    private DeviceAccessViewModel deviceAccessViewModel;
    private ActivityDeviceAccessBinding dataBinding;
    private boolean fromSplash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        fromSplash = intent.getBooleanExtra(Constants.SP_KEY_FROM_SPLASH, false);

        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_device_access);
        deviceAccessViewModel = new DeviceAccessViewModel();
        deviceAccessViewModel.setNavigator(this);
        dataBinding.setViewModel(deviceAccessViewModel);
        dataBinding.etAccessId.setTransformationMethod(new UpperCaseTransform());
        deviceAccessViewModel.init(dataBinding.customTopBar, fromSplash);
        CommonUtils.setEditTextInputFilter(dataBinding.etDeviceTag);
        CommonUtils.setEditTextDeviceAccessInputFilter(dataBinding.etAccessId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (deviceAccessViewModel != null) {
            deviceAccessViewModel.unInit();
        }
    }

    @Override
    public void showConfirmAccessDeviceDialog(DeviceInfo deviceInfo, String accessId, String deviceTag, String url) {
        String content = CommonUtils.getStrFromRes(R.string.confirm_is_access_device, deviceInfo.getAppName());
        CommonTipDialog dialog = CommonTipDialog
                .getInstance(content, CommonUtils.getStrFromRes(R.string.confirm),
                        CommonUtils.getStrFromRes(R.string.cancel), false,
                        false, true, true);
        dialog.setDialogSize((int) getResources().getDimension(R.dimen.x550),
                (int) getResources().getDimension(R.dimen.x370))
                .setOutCancel(false)
                .setConvertViewListener(((holder, baseDialog) -> {
                    holder.getView(R.id.btn_common_dialog_confirm).setOnClickListener(v -> {
                        if (deviceAccessViewModel != null) {
                            deviceAccessViewModel.confirmAccess(deviceInfo, accessId, deviceTag, url);
                        }
                        baseDialog.dismissAllowingStateLoss();
                    });
                    holder.getView(R.id.btn_common_dialog_cancel).setOnClickListener(v -> {
                        setUiEnable(true);
                        baseDialog.dismissAllowingStateLoss();
                    });
                }))
                .show(getSupportFragmentManager());
    }

    @Override
    public void showConfirmUnbindDialog() {
        String content = CommonUtils.getStrFromRes(R.string.confirm_unbind_device);
        CommonTipDialog dialog = CommonTipDialog
                .getInstance(content, CommonUtils.getStrFromRes(R.string.confirm),
                        CommonUtils.getStrFromRes(R.string.cancel), false,
                        false, true, false);
        dialog.setDialogSize((int) getResources().getDimension(R.dimen.x550),
                (int) getResources().getDimension(R.dimen.x370))
                .setOutCancel(false)
                .setConvertViewListener(((holder, baseDialog) -> {
                    holder.getView(R.id.btn_common_dialog_confirm).setOnClickListener(v -> {
                        unBindDevice();
                        baseDialog.dismissAllowingStateLoss();
                    });
                    holder.getView(R.id.btn_common_dialog_cancel).setOnClickListener(v -> {
                        baseDialog.dismissAllowingStateLoss();
                    });
                }))
                .show(getSupportFragmentManager());
    }

    public void unBindDevice() {
        if (deviceAccessViewModel != null) {
            deviceAccessViewModel.unBindDevice();
        }
    }

    @Override
    public void showAccessFailDialog(int code, String msg) {
        String content = code + ":" + msg;
        CommonTipDialog dialog = CommonTipDialog
                .getInstance(content, CommonUtils.getStrFromRes(R.string.confirm), "", true,
                        false, true, false);
        dialog.setDialogSize((int) getResources().getDimension(R.dimen.x550),
                (int) getResources().getDimension(R.dimen.x370))
                .setOutCancel(false)
                .setConvertViewListener(((holder, baseDialog) -> {
                    holder.getView(R.id.btn_common_dialog_confirm).setOnClickListener(v -> {
                        baseDialog.dismissAllowingStateLoss();
                    });
                    holder.getView(R.id.btn_common_dialog_cancel).setOnClickListener(v -> {
                        baseDialog.dismissAllowingStateLoss();
                    });
                }))
                .show(getSupportFragmentManager());
    }

    @Override
    public void showCoverDeviceDialog(String accessId, String deviceTag, String url) {
        DeviceAccessConflictDialog dialog = new DeviceAccessConflictDialog();
        dialog.setDialogSize((int) getResources().getDimension(R.dimen.x550),
                (int) getResources().getDimension(R.dimen.x370))
                .setOutCancel(false)
                .setConvertViewListener(((holder, baseDialog) -> {
                    holder.getView(R.id.btn_dialog_confirm).setOnClickListener(v -> {
                        if (!DoubleClickUtils.isFastDoubleClick(v.getId())) {
                            if (deviceAccessViewModel != null) {
                                deviceAccessViewModel.coverAlreadyBindDevice(accessId, deviceTag, url);
                            }
                            baseDialog.dismissAllowingStateLoss();
                        }
                    });
                    holder.getView(R.id.btn_dialog_cancel).setOnClickListener(v -> {
                        if (!DoubleClickUtils.isFastDoubleClick(v.getId())) {
                            setUiEnable(true);
                            baseDialog.dismissAllowingStateLoss();
                        }
                    });
                }))
                .show(getSupportFragmentManager());
    }

    private String getStringFromRes(int resId) {
        return getResources().getString(resId);
    }

    @Override
    public void deviceAccessSuccess(String message) {
        if (fromSplash) {
            CustomTopBar customTopBar = dataBinding.customTopBar;
            Button btnSkip = customTopBar.findViewById(R.id.btn_top_bar_skip);
            btnSkip.setEnabled(false);
            RelativeLayout rlClose = customTopBar.findViewById(R.id.rl_top_bar_close);
            rlClose.setEnabled(false);
        }
        ActiveResultDialog dialog = new ActiveResultDialog();
        Bundle bundle = new Bundle();
        bundle.putBoolean(ActiveResultDialog.BUNDLE_KEY_SUCCESS, true);
        bundle.putString(ActiveResultDialog.BUNDLE_KEY_CONTENT, message);
        bundle.putBoolean(ActiveResultDialog.BUNDLE_KEY_BTN_CONFIRM_ENABLE, true);
        dialog.setArguments(bundle);
        dialog.setDialogSize((int) getResources().getDimension(R.dimen.x550),
                (int) getResources().getDimension(R.dimen.x370))
                .setOutCancel(false)
                .setConvertViewListener(((holder, baseDialog) -> {
                    holder.getView(R.id.btn_active_result_confirm).setOnClickListener(v -> {
                        baseDialog.dismissAllowingStateLoss();
                        if (fromSplash) {
                            Intent intent = new Intent(this, RecognizeActivity.class);
                            startActivity(intent);
                            Utils.finishAllActivity();
                        } else {
                            ActivityUtils.finishActivity(DeviceAccessActivity.this);
                        }
                    });
                }))
                .show(getSupportFragmentManager());
    }

    @Override
    public void setUiEnable(boolean enable) {
        Button button = dataBinding.btnDeviceAccessUnbind;
        setUiEnable(enable, button.getText().toString());
    }

    @Override
    public void setUiEnable(boolean enable, String text) {
        dataBinding.etAccessId.setEnabled(enable);
        dataBinding.etDeviceTag.setEnabled(enable);
        Button button = dataBinding.btnDeviceAccessUnbind;
        button.setEnabled(enable);
        ProgressBar progressBar = dataBinding.progressBar;
        TextPaint textPaint = button.getPaint();
        int textWidth = (int) textPaint.measureText(text);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) button.getLayoutParams();
        if (enable) {
            params.width = textWidth + CommonUtils.getDimenFromRes(R.dimen.x50);
            button.setLayoutParams(params);
            button.setGravity(Gravity.CENTER);
            button.setPadding(0, 0, 0, 0);
            progressBar.setVisibility(View.GONE);
        } else {
            params.width = textWidth + CommonUtils.getDimenFromRes(R.dimen.x85);
            button.setLayoutParams(params);
            button.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            button.setPadding(CommonUtils.getDimenFromRes(R.dimen.x25), 0, 0, 0);
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setTopBarEnable(boolean enable) {
        CustomTopBar customTopBar = dataBinding.customTopBar;
        Button btnSkip = customTopBar.findViewById(R.id.btn_top_bar_skip);
        btnSkip.setEnabled(enable);
        RelativeLayout rlClose = customTopBar.findViewById(R.id.rl_top_bar_close);
        rlClose.setEnabled(enable);
    }
}
