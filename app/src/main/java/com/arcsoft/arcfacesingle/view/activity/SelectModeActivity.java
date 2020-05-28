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
import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.base.BaseBusinessActivity;
import com.arcsoft.arcfacesingle.databinding.ActivitySelectModeBinding;
import com.arcsoft.arcfacesingle.navigator.SelectModeNavigator;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.arcfacesingle.view.dialog.CleanDataDialog;
import com.arcsoft.arcfacesingle.view.dialog.SelectModeDialog;
import com.arcsoft.arcfacesingle.viewmodel.SelectModeViewModel;
import com.arcsoft.asg.libcommon.util.common.ToastUtils;
import com.arcsoft.asg.libcommon.util.common.Utils;

public class SelectModeActivity extends BaseBusinessActivity implements SelectModeNavigator {

    private static final String TAG = SelectModeActivity.class.getSimpleName();
    private ActivitySelectModeBinding activityDataBinding;
    private SelectModeViewModel viewModel;

    private CleanDataDialog cleanDataDialog;
    private boolean fromSplash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        fromSplash = intent.getBooleanExtra(Constants.SP_KEY_FROM_SPLASH, false);

        activityDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_select_mode);
        viewModel = new SelectModeViewModel();
        activityDataBinding.setViewModel(viewModel);
        viewModel.setNavigator(this);
        viewModel.setFromSplash(fromSplash);
    }

    @Override
    protected void onResume() {
        switchAutoSizeDp();
        super.onResume();
        if (viewModel != null) {
            viewModel.onActivityResume(activityDataBinding.customTopBar, fromSplash);
        }
    }

    @Override
    public void finishPage() {
        finish();
    }

    @Override
    public void gotoDeviceAccessPage() {
        Intent intent = new Intent(this, DeviceAccessActivity.class);
        intent.putExtra(Constants.SP_KEY_FROM_SPLASH, fromSplash);
        startActivity(intent);
    }

    @Override
    public void gotoRecognitionPage() {
        Intent intent = new Intent(this, RecognizeActivity.class);
        startActivity(intent);
        Utils.finishAllActivity();
    }

    @Override
    public void switch2DeviceInfoPage() {
        Intent intent = new Intent(this, DeviceInfoActivity.class);
        startActivity(intent);
    }

    @Override
    public void showDialog(int lastAppMode, int newMode, String content, String title) {
        SelectModeDialog dialog = new SelectModeDialog();
        Bundle bundle = new Bundle();
        bundle.putString(SelectModeDialog.BUNDLE_KEY_CONTENT, content);
        bundle.putString(SelectModeDialog.BUNDLE_KEY_TITLE, title);
        bundle.putInt(SelectModeDialog.BUNDLE_KEY_NEW_APP_MODE, newMode);
        dialog.setArguments(bundle);
        dialog.setDialogSize((int) getResources().getDimension(R.dimen.x550),
                (int) getResources().getDimension(R.dimen.x370))
                .setOutCancel(false)
                .setConvertViewListener(((holder, baseDialog) -> {
                    holder.getView(R.id.btn_dialog_confirm).setOnClickListener(v -> {
                        if (viewModel != null) {
                            viewModel.confirmSelectMode();
                        }
                        dialog.dismissAllowingStateLoss();
                    });
                    holder.getView(R.id.btn_dialog_cancel).setOnClickListener(v -> {
                        dialog.dismissAllowingStateLoss();
                    });
                }))
                .show(getSupportFragmentManager());
    }

    @Override
    public void setCleanDataDialog() {
        if (cleanDataDialog == null) {
            cleanDataDialog = new CleanDataDialog();
            cleanDataDialog.setDialogSize((int) getResources().getDimension(R.dimen.x450),
                    (int) getResources().getDimension(R.dimen.x345))
                    .setOutCancel(false)
                    .setConvertViewListener(((holder, baseDialog) -> {
                        holder.getView(R.id.btn_dialog_confirm).setOnClickListener(v -> {
                            if (viewModel != null) {
                                viewModel.cleanDataComplete();
                            }
                            cleanDataDialog.dismissAllowingStateLoss();
                            cleanDataDialog = null;
                        });
                    }))
                    .show(getSupportFragmentManager());
        }
    }

    @Override
    public void setCleanDataProgress(int total, int current) {
        if (cleanDataDialog != null) {
            if (total > 0) {
                cleanDataDialog.setTotal(total);
                cleanDataDialog.setProgressCurrent(current);
            } else {
                cleanDataDialog.setProgress(CleanDataDialog.COUNT_TOTAL);
            }
        }
    }

    @Override
    public void cancelCleanDataDialog() {
        if (cleanDataDialog != null) {
            ToastUtils.showShortToast(CommonUtils.getStrFromRes(R.string.operation_failed));
            cleanDataDialog.dismissAllowingStateLoss();
            cleanDataDialog = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (viewModel != null) {
            viewModel.release();
            viewModel = null;
        }
        activityDataBinding = null;
    }
}
