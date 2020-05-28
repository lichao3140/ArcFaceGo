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

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.Nullable;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.business.setting.ConfigConstants;
import com.arcsoft.arcfacesingle.data.db.table.TableSettingConfigInfo;
import com.arcsoft.arcfacesingle.databinding.FragmentCustomSettingBinding;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.arcfacesingle.view.dialog.DeleteLogoConfirmDialog;
import com.arcsoft.arcfacesingle.viewmodel.setting.CustomSettingViewModel;
import com.arcsoft.asg.libcommon.base.BaseLazyFragment;
import com.arcsoft.asg.libcommon.util.common.FileUtils;
import com.arcsoft.asg.libcommon.util.common.ToastUtils;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class CustomSettingFragment extends BaseLazyFragment implements CustomSettingViewModel.CustomSettingListener {

    private Spinner failVoiceSpinner;
    private Spinner successVoiceSpinner;
    private FragmentCustomSettingBinding binding;
    private CustomSettingViewModel customSettingViewModel;

    public CustomSettingFragment() {
        // Required empty public constructor
    }

    @Override
    protected View initView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        switchAutoSizeDp();

        customSettingViewModel = new CustomSettingViewModel();
        customSettingViewModel.setCustomSettingListener(this);
        binding = FragmentCustomSettingBinding.inflate(inflater);
        binding.setViewModel(customSettingViewModel);
        CommonUtils.setEditTextInputFilter(binding.etInputCompanyName);
        CommonUtils.setEditTextInputFilter(binding.etFailedShowModeCustom);
        CommonUtils.setEditTextInputFilter(binding.etSuccessShowModeCustom);
        CommonUtils.setEditTextInputFilter(binding.etFailedVoiceModeCustom);
        CommonUtils.setEditTextInputFilter(binding.etSuccessVoiceModeCustom);
        return binding.getRoot();
    }

    @Override
    protected void initPrepare() {
        AdapterView.OnItemSelectedListener selectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getId() == R.id.sr_success_preview_voice) {
                    customSettingViewModel.updateSuccessPreviewVoiceMode(position);
                } else if (parent.getId() == R.id.sr_fail_preview_voice) {
                    customSettingViewModel.updateFailPreviewVoiceMode(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        failVoiceSpinner = getView().findViewById(R.id.sr_fail_preview_voice);
        String[] items = getResources().getStringArray(R.array.fail_preview_voice);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getView().getContext(), R.layout.custom_spinner_item, items);
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        failVoiceSpinner.setAdapter(adapter);
        failVoiceSpinner.setOnItemSelectedListener(selectedListener);

        successVoiceSpinner = getView().findViewById(R.id.sr_success_preview_voice);
        String[] itemsSuccess = getResources().getStringArray(R.array.success_preview_voice);
        ArrayAdapter<String> adapterSuccess = new ArrayAdapter<>(getView().getContext(), R.layout.custom_spinner_item, itemsSuccess);
        adapterSuccess.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        successVoiceSpinner.setAdapter(adapterSuccess);
        successVoiceSpinner.setOnItemSelectedListener(selectedListener);
    }

    @Override
    protected void onInvisible() {

    }

    @Override
    protected void onVisible() {
        if (customSettingViewModel != null) {
//            customSettingViewModel.onVisible();
        }
    }

    @Override
    protected void initData() {

    }

    @Override
    public void onDestroy() {
        if (customSettingViewModel != null) {
            customSettingViewModel.onDestroy();
        }
        customSettingViewModel = null;
        binding = null;
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (customSettingViewModel != null) {
            customSettingViewModel.onResume();
//            customSettingViewModel.setMainLogo(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (customSettingViewModel != null) {
            customSettingViewModel.onPause();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ConfigConstants.CHOOSE_PICTURE_MAIN_LOGO:
                    ArrayList<Uri> mUris = new ArrayList<>(Matisse.obtainResult(data));
                    if (mUris.size() > 0) {
                        if (customSettingViewModel != null) {
                            customSettingViewModel.setCompanyLogo(mUris.get(0), ConfigConstants.CHOOSE_PICTURE_MAIN_LOGO);
                        }
                    } else {
                        ToastUtils.showShortToast(R.string.setting_image_invalid);
                    }
                    break;
                case ConfigConstants.CHOOSE_PICTURE_SECOND_LOGO:
                    ArrayList<Uri> mUris2 = new ArrayList<>(Matisse.obtainResult(data));
                    if (mUris2.size() > 0) {
                        if (customSettingViewModel != null) {
                            customSettingViewModel.setCompanyLogo(mUris2.get(0), ConfigConstants.CHOOSE_PICTURE_SECOND_LOGO);
                        }
                    } else {
                        ToastUtils.showShortToast(R.string.setting_image_invalid);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void showDeleteCompanyLogoDialog(boolean deleteMain) {
        String filePath;
        if (deleteMain) {
            filePath = ConfigConstants.DEFAULT_MAIN_LOGO_FILE_PATH;
        } else {
            filePath = ConfigConstants.DEFAULT_SECOND_LOGO_FILE_PATH;
        }
        if (FileUtils.isFileExists(filePath)) {
            String content = deleteMain ? getResources().getString(R.string.confirm_delete_main_logo) :
                    getResources().getString(R.string.confirm_delete_second_logo);
            DeleteLogoConfirmDialog dialog = new DeleteLogoConfirmDialog();
            Bundle bundle = new Bundle();
            bundle.putString("content", content);
            dialog.setArguments(bundle);
            dialog.setDialogSize((int) getResources().getDimension(R.dimen.x550),
                    (int) getResources().getDimension(R.dimen.x350))
                    .setOutCancel(false)
                    .setConvertViewListener(((holder, baseDialog) -> {
                        Button btConfirm = holder.getView(R.id.btn_delete_logo_confirm);
                        btConfirm.setOnClickListener(v -> {
                            if (FileUtils.deleteFile(filePath)) {
                                ToastUtils.showShortToast(R.string.delete_success);
                                customSettingViewModel.deleteLogo(deleteMain);
                            } else {
                                ToastUtils.showShortToast(R.string.delete_failure);
                            }
                            baseDialog.dismissAllowingStateLoss();
                        });
                        holder.getView(R.id.iv_delete_logo_cancel).setOnClickListener(v ->
                                baseDialog.dismissAllowingStateLoss()
                        );
                        holder.getView(R.id.btn_delete_logo_cancel).setOnClickListener(v ->
                                baseDialog.dismissAllowingStateLoss()
                        );
                    }))
                    .show(getFragmentManager());
        } else {
            ToastUtils.showShortToast(R.string.logo_not_exist);
        }
    }

    @Override
    public void goGallery(int requestCode) {
        Matisse.from(this)
                .choose(MimeType.ofAll())
                .theme(R.style.Matisse_Zhihu)
                .countable(true)
                .capture(false)
                .captureStrategy(new CaptureStrategy(true, ConfigConstants.GALLERY_AUTHORITY))
                .maxSelectable(1)
                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.sp_120))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR)
                .forResult(requestCode);
    }

    public void setMainLogo(boolean mainLogo, String path) {
        if (customSettingViewModel != null) {
            customSettingViewModel.setMainLogo(mainLogo, path);
        }
    }

    public void getConfig(TableSettingConfigInfo configInfo) {
        if (customSettingViewModel != null) {
            customSettingViewModel.getConfig(configInfo);
        }
    }

    public void reloadConfig() {
        if (customSettingViewModel != null) {
            customSettingViewModel.onVisible();
        }
    }
}
