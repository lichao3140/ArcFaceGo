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

package com.arcsoft.arcfacesingle.business.active;

import com.arcsoft.arcfacesingle.data.model.ParamCheckActivation;

import java.util.List;

public interface IActive {

    /**
     * 激活结果回调
     */
    interface ActiveCallback {

        /**
         * 激活成功
         */
        void onSuccess();

        /**
         * 激活失败
         * @param result 类型
         * @param errorMsg 失败信息
         * @param needSaveActiveFileToUsb 是否需要将激活信息保存到Usb
         */
        void onFail(int result, String errorMsg, boolean needSaveActiveFileToUsb);

        /**
         * 保存激活文件结果
         * @param success 保存是否成功
         */
        void onSaveActiveFile(boolean success);
    }

    /**
     * 设置是否来源于启动页
     * @param fromSplash
     */
    void setFromSplash(boolean fromSplash);

    /**
     * 初始化权限
     */
    void initPermission();

    /**
     * 读取SDCard中的激活文件
     * @return 激活文件数据
     */
    List<String> readActivationFileFromSdCard();

    /**
     * 读取U盘中的激活文件
     * @param callback 事件回调
     */
    void readActivationFileFromUsb(DeviceActiveRepository.ReadUsbFileCallback callback);

    /**
     * 设备激活
     * @param activeCallback 事件回调
     * @param appId appId信息
     * @param sdkKey sdkKey信息
     * @param activeKey 激活码信息
     */
    void onlineActive(ActiveCallback activeCallback, String appId, String sdkKey, String activeKey);

    /**
     * Usb设备是否可用
     * @return true 可用；false 不可用
     */
    boolean usbDeviceAvailable();

    /**
     * 是否选择USB激活文件
     * @param selected true 选择；false 不选择
     */
    void setSelectUsbActiveFileFlag(boolean selected);

    /**
     * 是否选择Usb激活文件
     * @return true 选择；false 不选择
     */
    boolean isSelectUsbActiveFile();

    /**
     * 校验激活文件APP_ID正确性
     * @param oriList 激活文件数据
     * @param useUsb 是否使用U盘激活
     * @return 校验结果信息
     */
    ParamCheckActivation checkActivationAppId(List<String> oriList, boolean useUsb);

    /**
     * 校验激活文件SDK_KEY正确性
     * @param oriList 激活文件数据
     * @param useUsb 是否使用U盘激活
     * @return 校验结果信息
     */
    ParamCheckActivation checkActivationSdkKey(List<String> oriList, boolean useUsb);

    /**
     * 校验激活文件ACTIVE_KEY正确性
     * @param oriList 激活文件数据
     * @param useUsb 是否使用U盘激活
     * @return 校验结果信息
     */
    ParamCheckActivation checkActivationActiveKey(List<String> oriList, boolean useUsb);

    /**
     * 是否需要保存激活码信息至USB设备
     * @param errorCode 错误码
     * @return true 不需要；false 需要
     */
    boolean needSaveActiveKeyToUsb(int errorCode);

    /**
     * 离线激活
     * @param activeCallback 回调
     * @param path 激活文件地址
     */
    void offlineActive(ActiveCallback activeCallback, String path);

    /**
     * 读取本地/data/data/包名下的激活文件
     * @param callback 回调
     */
    void saveOfflineActiveFile(ReadActiveFileCallback callback);

    interface ReadActiveFileCallback {

        /**
         * 读取成功
         * @param appId appId数据
         * @param sdkKey sdkKey数据
         * @param activeKey 激活码数据
         */
        void onSuccess(String appId, String sdkKey, String activeKey);

        /**
         * 读取失败
         */
        void onFail();
    }
}
