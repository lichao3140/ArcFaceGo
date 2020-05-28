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

package com.arcsoft.arcfacesingle.business.deviceaccess;

import com.arcsoft.arcsoftlink.http.bean.res.DeviceInfo;

public interface DeviceAccessListener {

    /**
     * accessId接入新设备
     * @param deviceInfo 设备信息DeviceInfo
     * @param accessId 接入码
     * @param deviceTag 设备标识
     * @param url 接入地址
     */
    void accessNewDevice(DeviceInfo deviceInfo, String accessId, String deviceTag, String url);

    /**
     * 设备接入失败
     * @param code 错误码
     * @param msg 错误信息
     */
    void deviceAccessFail(int code, String msg);

    /**
     * 设备接入失败
     * @param msg 失败信息
     */
    void deviceAccessFail(String msg);

    /**
     * 显示错误码提示信息弹框
     * @param code 错误码
     * @param msg 错误信息
     */
    void showMsgDialog(int code, String msg);

    /**
     * 设备第二次确认接入成功
     * @param message 提示信息
     */
    void deviceSecondSuccess(String message);

    /**
     * 更新设备标识成功
     * @param message 提示信息
     */
    void updateDeviceTagSuccess(String message);

    /**
     * 显示是否覆盖原有已接入的设备
     * @param accessId 接入码
     * @param deviceTag 设备标识
     * @param url 接入地址
     */
    void showCoverDeviceDialog(String accessId, String deviceTag, String url);
}
