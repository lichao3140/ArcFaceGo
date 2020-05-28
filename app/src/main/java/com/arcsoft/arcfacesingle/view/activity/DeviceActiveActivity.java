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

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.base.BaseBusinessActivity;
import com.arcsoft.arcfacesingle.business.common.CommonRepository;
import com.arcsoft.arcfacesingle.databinding.ActivityDeviceActiveBinding;
import com.arcsoft.arcfacesingle.navigator.DeviceActiveNavigator;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.arcfacesingle.util.SdcardUtils;
import com.arcsoft.arcfacesingle.view.dialog.ActiveResultDialog;
import com.arcsoft.arcfacesingle.view.dialog.CommonWarnTitleDialog;
import com.arcsoft.arcfacesingle.view.widgets.CustomTopBar;
import com.arcsoft.arcfacesingle.view.widgets.UpperCaseTransform;
import com.arcsoft.arcfacesingle.viewmodel.DeviceActiveViewModel;
import com.arcsoft.asg.libcommon.util.common.ActivityUtils;
import com.arcsoft.asg.libcommon.util.common.AppUtils;
import com.arcsoft.asg.libcommon.util.common.SPUtils;
import com.arcsoft.asg.libcommon.util.common.ToastUtils;
import com.arcsoft.asg.libcommon.util.common.UriUtils;
import com.arcsoft.faceengine.ErrorInfo;

import java.io.File;

public class DeviceActiveActivity extends BaseBusinessActivity implements DeviceActiveNavigator {

    private static final String TAG = DeviceActiveActivity.class.getSimpleName();
    private static final int CODE_REQUEST_SELECT_ACTIVE_FILE = 1;

    private DeviceActiveViewModel viewModel;
    private boolean fromSplash;
    private ActiveResultDialog activeResultDialog;
    private ActivityDeviceActiveBinding dataBinding;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        fromSplash = intent.getBooleanExtra(Constants.SP_KEY_FROM_SPLASH, false);

        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_device_active);
        viewModel = new DeviceActiveViewModel();
        dataBinding.setViewModel(viewModel);
        viewModel.setNavigator(this);
        viewModel.init(fromSplash);
        initView();
    }

    private void initView() {
        CustomTopBar customTopBar = dataBinding.customTopBar;
        customTopBar.setVisibleClose(true);
        customTopBar.setVisibleTitle(true);
        customTopBar.setStringTitle(CommonUtils.getStrFromRes(R.string.device_active));
        dataBinding.etActiveKey.setTransformationMethod(new UpperCaseTransform());
        CommonUtils.setEditTextDeviceAccessInputFilter(dataBinding.etActiveKey);
        dataBinding.btnDeviceActive.post(() -> setBtnActiveEnable(true));
        TextView tvOffline = dataBinding.tvOfflineActive;
        tvOffline.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.onActivityResume();
        }
    }

    @Override
    protected void onDestroy() {
        if (viewModel != null) {
            viewModel.onActivityDestroy();
        }
        viewModel = null;
        super.onDestroy();
    }

    @Override
    public void showActiveDialog(long result, String msg, boolean useUsb, boolean offline) {
        if (activeResultDialog == null) {
            activeResultDialog = new ActiveResultDialog();
            Bundle bundle = new Bundle();
            bundle.putBoolean(ActiveResultDialog.BUNDLE_KEY_SUCCESS, result == ErrorInfo.MOK);
            bundle.putString(ActiveResultDialog.BUNDLE_KEY_CONTENT, msg);
            bundle.putBoolean(ActiveResultDialog.BUNDLE_KEY_BTN_CONFIRM_ENABLE, !useUsb);
            activeResultDialog.setArguments(bundle);
            activeResultDialog.setDialogSize((int) getResources().getDimension(R.dimen.x550),
                    (int) getResources().getDimension(R.dimen.x370))
                    .setOutCancel(false)
                    .setConvertViewListener(((holder, baseDialog) -> {
                        holder.getView(R.id.btn_active_result_confirm).setOnClickListener(v -> {
                            baseDialog.dismissAllowingStateLoss();
                            activeResultDialog = null;
                            if (result == ErrorInfo.MOK || result == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
                                if (offline) {
                                    viewModel.saveOfflineActiveFile();
                                }
                                if (fromSplash) {
                                    if (TextUtils.isEmpty(SPUtils.getInstance().getString(Constants.SP_KEY_ADAPTATION_INFO))) {
                                        Intent intent = new Intent(this, AdaptationActivity.class);
                                        intent.putExtra(Constants.SP_KEY_FROM_SPLASH, true);
                                        startActivity(intent);
                                    } else {
                                        int appMode = SPUtils.getInstance().getInt(Constants.SP_KEY_APP_MODE, Constants.APP_MODE_NONE);
                                        if (appMode == Constants.APP_MODE_NONE) {
                                            Intent intent = new Intent(this, SelectModeActivity.class);
                                            intent.putExtra(Constants.SP_KEY_FROM_SPLASH, true);
                                            startActivity(intent);
                                        } else {
                                            Intent intent = new Intent(this, RecognizeActivity.class);
                                            startActivity(intent);
                                        }
                                    }
                                }
                                finish();
                            }
                        });
                    }))
                    .show(getSupportFragmentManager());
        }
    }

    @Override
    public void setActiveResultBtnEnable(boolean enable) {
        if (activeResultDialog != null) {
            activeResultDialog.setBtnConfirmEnable(enable);
        }
    }

    @Override
    public void showReadActivationFileDialog(String msg) {
        String strTitle = CommonUtils.getStrFromRes(R.string.dialog_warn);
        String strConfirm = CommonUtils.getStrFromRes(R.string.confirm);
        String strCancel = CommonUtils.getStrFromRes(R.string.cancel);
        CommonWarnTitleDialog dialog = CommonWarnTitleDialog.getInstance(strTitle, msg, strConfirm, strCancel,
                true, false, true);
        dialog.setDialogSize((int) getResources().getDimension(R.dimen.x550),
                (int) getResources().getDimension(R.dimen.x370))
                .setOutCancel(false)
                .setConvertViewListener(((holder, baseDialog) -> {
                    holder.getView(R.id.btn_common_dialog_confirm).setOnClickListener(v -> {
                        baseDialog.dismissAllowingStateLoss();
                    });
                }))
                .show(getSupportFragmentManager());
    }

    @Override
    public void setBtnActiveEnable(boolean enable) {
        ProgressBar progressBar = dataBinding.progressBar;
        Button button = dataBinding.btnDeviceActive;
        TextPaint textPaint = button.getPaint();
        int textWidth = (int) textPaint.measureText(button.getText().toString());
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) button.getLayoutParams();
        if (enable) {
            params.width = textWidth + CommonUtils.getDimenFromRes(R.dimen.x90);
            button.setLayoutParams(params);
            button.setGravity(Gravity.CENTER);
            button.setPadding(0, 0, 0, 0);
            progressBar.setVisibility(View.GONE);
        } else {
            params.width = textWidth + CommonUtils.getDimenFromRes(R.dimen.x125);
            button.setLayoutParams(params);
            button.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            button.setPadding(CommonUtils.getDimenFromRes(R.dimen.x45), 0, 0, 0);
            progressBar.setVisibility(View.VISIBLE);
        }
        button.setEnabled(enable);
    }

    @Override
    public void setEditTextClearFocus() {
        dataBinding.llClearFocus.requestFocus();
    }

    @Override
    public void openOfflineFile() {
        File file = new File(SdcardUtils.getInstance().getRootPathDir());
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = FileProvider.getUriForFile(this, AppUtils.getAppPackageName() + ".fileprovider", file);
        intent.setData(uri);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, CODE_REQUEST_SELECT_ACTIVE_FILE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && ActivityUtils.activityCount() == 1) {
            CommonRepository.getInstance().sendExitAppBroadcast();
            CommonRepository.getInstance().exitApp();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CODE_REQUEST_SELECT_ACTIVE_FILE) {
                try {
                    Uri uri = data.getData();
                    if (uri != null) {
                        String fileDirPath = SdcardUtils.getInstance().getDeviceActiveFilePathDir();
                        File file = UriUtils.uri2File(uri, fileDirPath);
                        if (file == null) {
                            ToastUtils.showShortToast(CommonUtils.getStrFromRes(R.string.config_file_invalid));
                            return;
                        }
                        viewModel.offlineActive(file);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
