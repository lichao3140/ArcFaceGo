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

import android.util.Pair;

import com.arcsoft.arcsoftlink.http.bean.res.DeviceInfo;

public interface IDeviceAccess {

    /**
     * 初始化资源数据
     * @param fromSplash true 从启动页跳转过来；false 从其他页面跳转过来
     */
    void init(boolean fromSplash);

    /**
     * 释放资源
     */
    void unInit();

    /**
     * 设置ArcLink的服务器地址
     * @param url 服务器地址
     */
    void initUrl(String url);

    /**
     * 接入设备，首次确认
     *
     * @param accessId 接入码
     * @param deviceTag 设备标识
     * @param url 服务器地址
     * @param callback 回调
     */
    void accessDeviceFirst(String accessId, String deviceTag, String url, AccessDeviceFirstCallback callback);

    /**
     * 接入设备，二次确认
     *
     * @param deviceInfo 设备信息
     * @param accessId 接入码
     * @param deviceTag 设备标识
     * @param url 服务器地址
     */
    void accessDeviceSecond(DeviceInfo deviceInfo, String accessId, String deviceTag, String url);

    /**
     * 核查接入码
     *
     * @param accessId 接入码
     * @return 核查结果集
     */
    Pair<Integer, String> checkAccessId(String accessId);

    /**
     * 核查设备标识
     *
     * @param deviceTag 设备标识
     * @return 核查结果集
     */
    Pair<Integer, String> checkDeviceTag(String deviceTag);

    /**
     * 核查服务器地址
     * @param serverUrl 服务器地址
     * @return 核查结果集
     */
    Pair<Integer, String> checkServerUrl(String serverUrl);

    /**
     * 更新设备标识
     *
     * @param accessId 接入码
     * @param deviceTag 设备标识
     * @param url 服务器地址
     */
    void updateDeviceTag(String accessId, String deviceTag, String url);

    /**
     * 更新服务器地址
     * @param url 服务器地址
     * @return true 更新成功；false 更新失败
     */
    boolean updateServerUrl(String url);

    /**
     * 解绑设备
     */
    void unBindDevice();

    /**
     * 获取设备接入状态
     * @return
     */
    boolean getDeviceAccessStatus();

    /**
     * 覆盖原有设备
     *
     * @param accessId 接入码
     * @param deviceTag 设备标识
     * @param url 服务器地址
     */
    void coverAlreadyBindDevice(String accessId, String deviceTag, String url);

    interface AccessDeviceFirstCallback {

        /**
         * 接入失败
         * @param type 失败类型
         * @param msg 失败信息
         */
        void onFail(int type, String msg);
    }

}
