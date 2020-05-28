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

package com.arcsoft.asg.libdeviceadapt.repos;

import com.arcsoft.asg.libdeviceadapt.bean.DeviceAdaptationInfo;

public interface IAdaptationInfo {

    /**
     * 设置SeekBar偏移量
     * @param x 水平偏移
     * @param y 垂直偏移
     */
    void setSeekBarDisplace(int x, int y);

    /**
     * 副相机开启成功
     */
    void onSecondCameraOpened();

    /**
     * 相机开启异常
     * @param mainCamera 是否为主相机
     * @param e 异常信息
     */
    void onCameraOpenError(boolean mainCamera, Exception e);

    interface OnAdaptationInfoCallback {

        /**
         * 适配信息获取成功
         * @param adaptInfo 适配信息
         */
        void onAdaptationInfoCreated(DeviceAdaptationInfo adaptInfo);

        /**
         * 适配信息获取异常
         * @param throwable 异常信息
         */
        void onAdaptationInfoError(Throwable throwable);
    }
}
