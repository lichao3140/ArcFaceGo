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

package com.arcsoft.arcfacesingle.business.setting;

import android.graphics.Bitmap;
import android.net.Uri;

import com.arcsoft.arcfacesingle.data.db.table.TableSettingConfigInfo;

public interface ISetting {

    /**
     * 获取主logo
     * @param path logo图片地址
     * @return logo的Bitmap
     */
    Bitmap getMainLogo(String path);

    /**
     * 获取副logo
     * @param path logo图片地址
     * @return logo的Bitmap
     */
    Bitmap getViceLogo(String path);

    /**
     * 设置公司logo
     * @param uri logo的uri
     * @param type 来源
     * @param callBack 回调
     */
    void setCompanyLogo(Uri uri, int type, SettingRepository.SettingLogoCallBack callBack);

    /**
     * 保存配置信息
     * @param modifiedConfigInfo 配置信息
     * @return true 保存成功；false 保存失败
     */
    boolean saveSettingConfigInfo(TableSettingConfigInfo modifiedConfigInfo);

    /**
     * 核查已修改的参数
     * @param configInfo 配置信息
     * @return  true 核查成功；false 核查失败
     */
    boolean checkSaveSettingConfigInfo(TableSettingConfigInfo configInfo);

    /**
     * 开始 5分钟任何操作，关闭页面任务
     * @param callBack 回调
     *
     */
    void startDelayClosePageTimer(SettingRepository.SettingCloseCallBack callBack);

    /**
     * 关闭 5分钟任何操作，关闭页面任务
     */
    void disposeDelayClosePageTimer();

    /**
     * 点击多次退出应用
     */
    void clickMultipleExitApp();

    /**
     * 释放资源
     */
    void unInit();
}
