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

package com.arcsoft.arcfacesingle.business.recognize;

import android.graphics.Bitmap;

import com.arcsoft.asg.libcamera.view.CameraFaceView;

public interface IRecognize {

    interface RecognizeCallback {

        /**
         * 关闭密码弹框
         */
        void cancelPasswordDialog();
    }

    interface ViceCameraCallback {

        /**
         * 副相机开启失败
         */
        void onViceCameraFail();
    }

    /**
     * 开始关闭密码弹框的定时任务
     *
     * @param callback 事件回调
     */
    void startClosePasswordDialogTimer(RecognizeCallback callback);

    /**
     * 取消关闭密码弹框的定时任务
     */
    void disposeClosePassDialogTimer();

    /**
     * 首页密码弹框是否显示
     *
     * @param indexDialogShow 是否显示弹框
     */
    void setIndexDialogShow(boolean indexDialogShow);

    /**
     * 获取主logo
     *
     * @param uri logo的url地址
     * @return logo的Bitmap
     */
    Bitmap getMainLogo(String uri);

    /**
     * 获取副logo
     *
     * @param url logo的url地址
     * @return logo的Bitmap
     */
    Bitmap getSecondLogo(String url);

    /**
     * 初始化资源数据
     */
    void init();

    /**
     * 加载人员数据
     */
    void loadFaceInfoList();

    /**
     * activity的onPause回调
     */
    void onActivityPause();

    /**
     * activity的onResume回调
     */
    void onActivityResume();

    /**
     * activity的onDestroy回调
     */
    void onActivityDestroy();

    /**
     * 开始识别，一般用于管理端向设备端下发参数，根据参数做相关调整
     */
    void startMainCamera();

    /**
     * 打开副相机
     * @param viceCameraView 副相机View
     */
    void startMainAndViceCamera(CameraFaceView viceCameraView);

    /**
     * 停止识别，一般用于管理端向设备端下发参数，根据参数做相关调整
     */
    void stopCameraAndRelease();
}
