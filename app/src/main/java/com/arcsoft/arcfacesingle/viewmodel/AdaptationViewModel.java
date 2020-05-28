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

import android.view.View;

import androidx.databinding.BaseObservable;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.business.adaptation.AdaptationRepository;

public class AdaptationViewModel extends BaseObservable {

    private AdaptationListener adaptationListener;
    private AdaptationRepository repository;

    public AdaptationViewModel() {
        repository = new AdaptationRepository();
    }

    public void setAdaptationListener(AdaptationListener listener) {
        this.adaptationListener = listener;
    }

    public interface AdaptationListener {

        /**
         * 保存适配信息
         */
        void saveSettings();
    }

    public String getAdaptationInfo() {
        return repository.getAdaptationInfo();
    }

    public void saveConfig(String configJson) {
        if (repository != null) {
            repository.saveConfig(configJson);
        }
    }

    public void saveSecondCameraConfig() {
        if (repository != null) {
            repository.saveSecondCameraConfig();
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_setting_save:
                if (adaptationListener != null) {
                    adaptationListener.saveSettings();
                }
                break;
            default:
                break;
        }
    }

    public void unInit() {
        repository = null;
    }
}
