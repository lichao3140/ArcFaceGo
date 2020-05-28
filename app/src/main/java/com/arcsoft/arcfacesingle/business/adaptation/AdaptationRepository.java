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

package com.arcsoft.arcfacesingle.business.adaptation;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.business.common.CommonRepository;
import com.arcsoft.arcfacesingle.business.setting.ConfigConstants;
import com.arcsoft.arcfacesingle.data.db.table.TableSettingConfigInfo;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.arcfacesingle.util.SdcardUtils;
import com.arcsoft.arcfacesingle.util.scheduler.BaseObserver;
import com.arcsoft.asg.libcommon.util.business.rx.RxUtils;
import com.arcsoft.asg.libcommon.util.common.FileUtils;
import com.arcsoft.asg.libcommon.util.common.SPUtils;
import com.arcsoft.asg.libcommon.util.common.ToastUtils;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;

public class AdaptationRepository {

    public void saveConfig(String configJson) {
        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            SPUtils.getInstance().put(Constants.SP_KEY_ADAPTATION_INFO, configJson);
            String path = SdcardUtils.getInstance().getSettingsPath() + File.separator + Constants.USB_FILE_NAME_ADAPTATION;
            File file = new File(path);
            FileUtils.createFileByDeleteOldFile(file);
            FileUtils.write(file, configJson);
            emitter.onNext(true);
            emitter.onComplete();
        }).compose(RxUtils.ioToMain())
                .subscribe(new BaseObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        ToastUtils.showShortToast(CommonUtils.getStrFromRes(R.string.adaptation_info_save_success));
                    }
                });
    }

    public void saveSecondCameraConfig() {
        TableSettingConfigInfo configInfo = CommonRepository.getInstance().getSettingConfigInfo();
        if (configInfo.getLiveDetectType() != ConfigConstants.DEFAULT_LIVE_DETECT_CLOSE) {
            configInfo.setLiveDetectType(ConfigConstants.DEFAULT_LIVE_DETECT_IR);
            configInfo.setIrLivePreview(ConfigConstants.DEFAULT_IR_LIVE_PREVIEW);
            CommonRepository.getInstance().saveSettingConfigAsync(configInfo, null);
        }
    }

    public String getAdaptationInfo() {
        return SPUtils.getInstance().getString(Constants.SP_KEY_ADAPTATION_INFO);
    }
}
