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

import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.data.db.table.TableSettingConfigInfo;
import com.arcsoft.arcfacesingle.databinding.FragmentRecognitionSettingBinding;
import com.arcsoft.arcfacesingle.viewmodel.setting.RecognizeSettingViewModel;
import com.arcsoft.asg.libcommon.base.BaseLazyFragment;

public class RecognitionSettingFragment extends BaseLazyFragment {

    private FragmentRecognitionSettingBinding fragmentRecognitionSettingBinding;
    private RecognizeSettingViewModel recognizeSettingViewModel;

    private Spinner distanceSpinner;

    public RecognitionSettingFragment() {
        // Required empty public constructor
    }

    @Override
    protected View initView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        switchAutoSizeDp();
        fragmentRecognitionSettingBinding = FragmentRecognitionSettingBinding.inflate(inflater);

        int cameraNumbers = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CameraManager cameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
            try {
                cameraNumbers = cameraManager.getCameraIdList().length;
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else {
            cameraNumbers = Camera.getNumberOfCameras();
        }

        recognizeSettingViewModel = new RecognizeSettingViewModel(cameraNumbers);
        fragmentRecognitionSettingBinding.setViewModel(recognizeSettingViewModel);

        return fragmentRecognitionSettingBinding.getRoot();
    }

    @Override
    protected void initPrepare() {
        distanceSpinner = getView().findViewById(R.id.sr_recognition_distance);
        String[] items = getResources().getStringArray(R.array.recognition_distance);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getView().getContext(), R.layout.custom_spinner_item, items);
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        distanceSpinner.setAdapter(adapter);
        distanceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getId() == R.id.sr_recognition_distance) {
                    recognizeSettingViewModel.onRecognitionDistanceChanged(position + 1, true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    protected void onInvisible() {

    }

    @Override
    protected void onVisible() {
        if (recognizeSettingViewModel != null) {
//            recognizeSettingViewModel.onVisible();
        }
    }

    @Override
    protected void initData() {
    }

    @Override
    public void onResume() {
        if (recognizeSettingViewModel != null) {
            recognizeSettingViewModel.onResume();
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        if (recognizeSettingViewModel != null) {
            recognizeSettingViewModel.onDestroy();
        }
        recognizeSettingViewModel = null;
        fragmentRecognitionSettingBinding = null;
        super.onDestroy();
    }

    public void getConfig(TableSettingConfigInfo configInfo) {
        if (recognizeSettingViewModel != null) {
            recognizeSettingViewModel.getConfig(configInfo);
        }
    }

    public void reloadConfig() {
        if (recognizeSettingViewModel != null) {
            recognizeSettingViewModel.onVisible();
        }
    }

}
