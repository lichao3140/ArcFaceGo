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

public interface RecognizeNavigator {

    /**
     * 显示密码弹框
     */
    void showPasswordDialog();

    /**
     * 关闭密码弹框
     */
    void cancelPasswordDialog();

    /**
     * 初始化相机
     */
    void initCamera();

    /**
     * 显示IR弹框
     */
    void showIrDialog();

    /**
     * 显示Engine弹框
     * @param message 提示信息
     */
    void showEngineDialog(String message);

    /**
     * 显示结果
     * @param show true 显示；false 不显示
     */
    void showResultView(boolean show);

    /**
     * 显示提示
     * @param show true 显示；false 不显示
     * @param animated true 使用动画；false 不使用动画
     */
    void showHintView(boolean show, boolean animated);

    /**
     * 更改颜色
     * @param status 类型
     * @param animated true 使用动画；false 不使用动画
     */
    void changeRectColor(Integer status, boolean animated);

    /**
     * 设置副相机显示隐藏
     * @param visible true 显示；false 隐藏
     */
    void setViceCameraFaceVisible(boolean visible);

    /**
     * 更新主相机预览UI
     * @param mainCamera true 主相机；false 副相机
     */
    void updateMainCameraViewUi(boolean mainCamera);
}
