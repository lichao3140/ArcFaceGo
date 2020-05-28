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
import android.os.PersistableBundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.base.BaseBusinessActivity;
import com.arcsoft.arcfacesingle.databinding.ActivityAdaptationBinding;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.arcfacesingle.view.widgets.CustomTopBar;
import com.arcsoft.arcfacesingle.viewmodel.AdaptationViewModel;
import com.arcsoft.asg.libcommon.util.common.FragmentUtils;
import com.arcsoft.asg.libcommon.util.common.SPUtils;
import com.arcsoft.asg.libcommon.util.common.Utils;
import com.arcsoft.asg.libdeviceadapt.view.fragment.AdaptationInfoCallback;
import com.arcsoft.asg.libdeviceadapt.view.fragment.AdaptationInfoFragment;

public class AdaptationActivity extends BaseBusinessActivity implements AdaptationInfoCallback,
        AdaptationViewModel.AdaptationListener {

    private static final String TAG = AdaptationActivity.class.getSimpleName();

    private Fragment[] fragments;
    private int curIndex;
    private boolean fromSplash;
    private AdaptationViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            curIndex = savedInstanceState.getInt("curIndex");
        }

        Intent intent = getIntent();
        fromSplash = intent.getBooleanExtra(Constants.SP_KEY_FROM_SPLASH, false);

        ActivityAdaptationBinding dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_adaptation);
        viewModel = new AdaptationViewModel();
        viewModel.setAdaptationListener(this);
        dataBinding.setViewModel(viewModel);
        updateUi(dataBinding.customTopBar, fromSplash);

        AdaptationInfoFragment adaptationInfoFragment =
                AdaptationInfoFragment.getInstance(viewModel.getAdaptationInfo(), CommonUtils.getDeviceCameraInfo(), fromSplash);
        fragments = new Fragment[1];
        fragments[0] = adaptationInfoFragment;
        String[] fragTags = new String[1];
        fragTags[0] = AdaptationInfoFragment.class.getSimpleName();
        FragmentUtils.add(getSupportFragmentManager(), fragments, R.id.fragment_adaptation, fragTags, curIndex);
        adaptationInfoFragment.setCallback(this);
    }

    private void updateUi(CustomTopBar customTopBar, boolean fromSplash) {
        customTopBar.setVisibleTitle(true);
        customTopBar.setStringTitle(CommonUtils.getStrFromRes(R.string.adaptation_info));
        if (fromSplash) {
            customTopBar.setVisibleClose(false);
            customTopBar.setVisibleSkip(true);
        } else {
            customTopBar.setVisibleClose(true);
        }
    }

    @Override
    public void onSecondCameraOpened() {
        if (viewModel != null) {
            viewModel.saveSecondCameraConfig();
        }
    }

    @Override
    public void onBackPressed() {
        if (!FragmentUtils.dispatchBackPress(fragments[curIndex])) {
            super.onBackPressed();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putInt("curIndex", curIndex);
    }

    @Override
    public void saveSettings() {
        if (viewModel != null) {
            AdaptationInfoFragment fragment = (AdaptationInfoFragment) fragments[0];
            String stringConfig = fragment.getConfigInfo();
            viewModel.saveConfig(stringConfig);
        }
        if (fromSplash) {
            skipPage();
        } else {
            finish();
        }
    }

    private void skipPage() {
        int appMode = SPUtils.getInstance().getInt(Constants.SP_KEY_APP_MODE, Constants.APP_MODE_NONE);
        if (appMode == Constants.APP_MODE_NONE) {
            Intent intent = new Intent(this, SelectModeActivity.class);
            intent.putExtra(Constants.SP_KEY_FROM_SPLASH, true);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, RecognizeActivity.class);
            intent.putExtra(Constants.SP_KEY_APP_MODE, appMode);
            startActivity(intent);
        }
        Utils.finishAllActivity();
    }

    @Override
    protected void onDestroy() {
        if (viewModel != null) {
            viewModel.unInit();
        }
        super.onDestroy();
    }
}
