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

package com.arcsoft.asg.libdeviceadapt.view.fragment;

import com.arcsoft.asg.libcamera.contract.ICamera;
import com.arcsoft.asg.libdeviceadapt.bean.DeviceAdaptationInfo;

public interface IAdaptationInfoNavigator {

    /**
     * 设置UI
     * @param deviceAdaptationInfo
     */
    void setUiData(DeviceAdaptationInfo deviceAdaptationInfo);

    /**
     * 设置相机UI
     * @param deviceAdaptationInfo
     * @param camera1
     * @param camera2
     * @param init
     * @param previewRatio
     */
    void setCamera(DeviceAdaptationInfo deviceAdaptationInfo, ICamera camera1, ICamera camera2, boolean init, boolean previewRatio);

    /**
     * 设置水平位移UI偏移
     * @param value
     */
    void setHorizontalDisplace(int value);

    /**
     * 设置垂直位移UI偏移
     * @param value
     */
    void setVerticalDisplace(int value);

    /**
     * 切换副相机的副View宽高
     */
    void changeSecondViceViewWidthHeight();

    /**
     * 副相机开启成功
     */
    void onSecondCameraOpened();

    /**
     * 相机开启失败
     * @param mainCamera
     * @param msg
     */
    void onCameraOpenError(boolean mainCamera, String msg);
}
