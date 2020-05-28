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

public interface SelectModeNavigator {

    /**
     * 跳转到人员识别页
     */
    void gotoRecognitionPage();

    /**
     * 关闭页面
     */
    void finishPage();

    /**
     * 跳转到设备接入页
     */
    void gotoDeviceAccessPage();

    /**
     * 显示弹框
     * @param lastAppMode 旧模式
     * @param newMode 新模式
     * @param warnContent 显示信息
     * @param title 标题
     */
    void showDialog(int lastAppMode, int newMode, String warnContent, String title);

    /**
     * 设置清除数据弹框
     */
    void setCleanDataDialog();

    /**
     * 设置弹框进度条
     * @param total 总数
     * @param current 当前进度
     */
    void setCleanDataProgress(int total, int current);

    /**
     * 关闭数据清除弹框
     */
    void cancelCleanDataDialog();

    /**
     * 跳转到设备信息页
     */
    void switch2DeviceInfoPage();
}
