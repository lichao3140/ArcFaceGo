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

package com.arcsoft.arcfacesingle.navigator;

import com.arcsoft.arcsoftlink.http.bean.res.DeviceInfo;

public interface DeviceAccessNavigator {

    /**
     * 显示确认设备接入弹框
     *
     * @param deviceInfo DeviceInfo信息
     * @param accessId 接入码
     * @param deviceTag 设备标识
     * @param url 接入服务器地址
     */
    void showConfirmAccessDeviceDialog(DeviceInfo deviceInfo, String accessId, String deviceTag, String url);

    /**
     * 显示确认解绑弹框
     */
    void showConfirmUnbindDialog();

    /**
     * 显示接入失败弹框
     *
     * @param code 错误码
     * @param msg 错误信息
     */
    void showAccessFailDialog(int code, String msg);

    /**
     * 显示是否覆盖原有已接入的设备
     *
     * @param accessId 接入码
     * @param deviceTag 设备标识
     * @param url 服务器地址
     */
    void showCoverDeviceDialog(String accessId, String deviceTag, String url);

    /**
     * 设备接入成功
     * @param message 成功信息
     */
    void deviceAccessSuccess(String message);

    /**
     * 设置UI控件可用状态
     * @param enable true 可用；false 不可用
     */
    void setUiEnable(boolean enable);

    /**
     * 设置UI控件可用状态
     * @param enable true 可用；false 不可用
     * @param text 控件信息
     */
    void setUiEnable(boolean enable, String text);

    /**
     * 设置顶部操作栏可用状态
     * @param enable true 可用；false 不可用
     */
    void setTopBarEnable(boolean enable);
}
