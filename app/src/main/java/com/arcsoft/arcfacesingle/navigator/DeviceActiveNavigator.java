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

public interface DeviceActiveNavigator {

    /**
     * 显示激活结果弹框
     * @param result 状态码
     * @param msg 信息
     * @param useUsb true 使用usb；false 不使用usb
     * @param offline true 离线激活；false 在线激活
     */
    void showActiveDialog(long result, String msg, boolean useUsb, boolean offline);

    /**
     * 显示读取激活文件结果弹框
     * @param msg 结果信息
     */
    void showReadActivationFileDialog(String msg);

    /**
     * 设置激活结果弹框按钮样式
     * @param enable true 可用；false 不可用
     */
    void setActiveResultBtnEnable(boolean enable);

    /**
     * 设置激活按钮样式
     * @param enable true 可用；false 不可用
     */
    void setBtnActiveEnable(boolean enable);

    /**
     * 移动光标
     */
    void setEditTextClearFocus();

    /**
     * 打开离线激活文件
     */
    void openOfflineFile();
}
