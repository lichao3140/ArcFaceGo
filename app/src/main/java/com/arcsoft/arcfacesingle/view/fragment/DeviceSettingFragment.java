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

package com.arcsoft.arcfacesingle.view.fragment;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.data.db.table.TableSettingConfigInfo;
import com.arcsoft.arcfacesingle.databinding.FragmentDeviceSettingBinding;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.arcfacesingle.view.dialog.CommonTipDialog;
import com.arcsoft.arcfacesingle.view.dialog.CommonWarnTitleDialog;
import com.arcsoft.arcfacesingle.viewmodel.setting.DeviceSettingViewModel;
import com.arcsoft.asg.libcommon.base.BaseLazyFragment;
import com.arcsoft.asg.libcommon.util.common.NetworkUtils;

import java.lang.ref.WeakReference;

public class DeviceSettingFragment extends BaseLazyFragment implements DeviceSettingViewModel.DeviceSettingListener {

    private FragmentDeviceSettingBinding dataBinding;
    private DeviceSettingViewModel viewModel;

    public DeviceSettingFragment() {
    }

    @Override
    protected View initView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        switchAutoSizeDp();

        viewModel = new DeviceSettingViewModel();
        dataBinding = FragmentDeviceSettingBinding.inflate(inflater);
        CommonUtils.setEditTextInputFilter(dataBinding.etDeviceValue);
        dataBinding.setViewModel(viewModel);
        viewModel.setDeviceSettingListener(this);
        return dataBinding.getRoot();
    }

    @Override
    protected void initPrepare() {
    }

    @Override
    protected void onInvisible() {

    }

    @Override
    protected void onVisible() {
        if (viewModel != null) {
//            viewModel.onVisible();
        }
    }

    @Override
    protected void initData() {

    }

    @Override
    public void onResume() {
        if (viewModel != null) {
            viewModel.onResume();
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        if (viewModel != null) {
            viewModel.onDestroy();
        }
        viewModel = null;
        dataBinding = null;
        super.onDestroy();
    }

    @Override
    public void showWifiWarnDialog() {
        assert getFragmentManager() != null;
        CommonTipDialog dialog = CommonTipDialog
                .getInstance(CommonUtils.getStrFromRes(R.string.device_mac_address_empty),
                        CommonUtils.getStrFromRes(R.string.confirm_open), "", true, false, false);
        dialog.setDialogSize((int) getResources().getDimension(R.dimen.x550),
                (int) getResources().getDimension(R.dimen.x370))
                .setOutCancel(false)
                .setConvertViewListener(((holder, baseDialog) -> {
                    holder.getView(R.id.btn_common_dialog_confirm).setOnClickListener(v -> {
                        NetworkUtils.setWifiEnabled(true);
                        baseDialog.dismissAllowingStateLoss();
                        if (viewModel != null) {
                            viewModel.setIpAndMacAddress();
                        }
                    });
                    holder.getView(R.id.iv_common_dialog_cancel).setOnClickListener(v -> {
                        baseDialog.dismissAllowingStateLoss();
                    });
                }))
                .show(getFragmentManager());
    }

    @Override
    public void updateMacAddressUi(boolean firstText) {
        TextView textView;
        if (firstText) {
            textView = dataBinding.tvMacAddressValue;
        } else {
            textView = dataBinding.tvMacAddressValue2;
        }
        textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
    }

    @Override
    public void showDisconnectDialog(String content) {
        assert getFragmentManager() != null;
        WeakReference<DeviceSettingViewModel> weakReference = new WeakReference<>(viewModel);
        CommonWarnTitleDialog dialog = CommonWarnTitleDialog.getInstance(CommonUtils.getStrFromRes(R.string.connect_title), content,
                CommonUtils.getStrFromRes(R.string.disconnect), CommonUtils.getStrFromRes(R.string.cancel), false, false, true);
        dialog.setDialogSize((int) getResources().getDimension(R.dimen.x550),
                (int) getResources().getDimension(R.dimen.x320))
                .setOutCancel(false)
                .setConvertViewListener(((holder, baseDialog) -> {
                    holder.getView(R.id.btn_common_dialog_confirm).setOnClickListener(v -> {
                        DeviceSettingViewModel model = weakReference.get();
                        if (model != null) {
                            model.disconnect();
                        }
                        baseDialog.dismissAllowingStateLoss();
                    });
                    holder.getView(R.id.btn_common_dialog_cancel).setOnClickListener(v -> {
                        baseDialog.dismissAllowingStateLoss();
                    });
                }))
                .show(getFragmentManager());
    }

    public void setIpAndMacAddress() {
        if (viewModel != null) {
            viewModel.setIpAndMacAddress();
        }
    }

    public String getDevicePort() {
        if (viewModel != null) {
            return viewModel.strPort.get();
        }
        return "";
    }

    public void getConfig(TableSettingConfigInfo configInfo) {
        if (viewModel != null) {
            viewModel.getConfig(configInfo);
        }
    }

    public void reloadConfig() {
        if (viewModel != null) {
            viewModel.onVisible();
        }
    }

    public void disconnect(boolean connected, String ip) {
        if (viewModel != null) {
            viewModel.updateConnectStatus(connected, ip);
        }
    }

}
