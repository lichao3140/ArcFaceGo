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

package com.arcsoft.arcfacesingle.business.common;

import com.arcsoft.arcfacesingle.data.db.dao.SettingConfigInfoDao;
import com.arcsoft.arcfacesingle.data.db.table.TableSettingConfigInfo;
import com.arcsoft.arcfacesingle.server.pojo.base.ResponseBase;
import com.arcsoft.arcfacesingle.server.pojo.request.RequestSetting;

public interface ICommon {

    /**
     * 设备重启
     */
    void reboot();

    /**
     * 设备延迟重启
     * @param delayTime 延时毫秒数
     */
    void rebootDelay(long delayTime);

    /**
     * 开灯
     * @param lightType 灯类型
     */
    void openLamp(int lightType);

    /**
     * 关灯
     * @param lightType 灯类型
     */
    void closeLamp(int lightType);

    /**
     * 开门
     */
    void openDoor();

    /**
     * 存储配置信息
     * @param configInfo 配置信息数据
     * @return
     */
    boolean saveSettingConfigSync(TableSettingConfigInfo configInfo);

    /**
     * 存储配置信息
     * @param configInfo 配置信息数据
     * @param callback 回调
     * @return
     */
    void saveSettingConfigAsync(TableSettingConfigInfo configInfo, SettingConfigInfoDao.SettingConfigCallback callback);

    /**
     * 获取配置信息
     * @return 配置信息数据
     */
    TableSettingConfigInfo getSettingConfigInfo();

    /**
     * 创建表Id
     * @return id信息
     */
    String createDatabaseId();

    /**
     * 退出应用
     */
    void exitApp();

    /**
     * 设置IP，MAC
     * @param ipAddress ip地址
     * @param macAddress mac地址
     */
    void setIpAndMacAddress(String ipAddress, String macAddress);

    /**
     * 设置设备参数
     * @param requestSetting 参数数据
     * @param offline true 离线模式；false 云端模式
     * @return 结果信息
     */
    ResponseBase checkDeviceParams(RequestSetting requestSetting, boolean offline);

    /**
     * 上传logo至aiot服务器
     */
    void uploadArcLinkMainLogo();

    /**
     * 上传logo至ArcLink服务器
     */
    void uploadArcLinkSecondLogo();
}
