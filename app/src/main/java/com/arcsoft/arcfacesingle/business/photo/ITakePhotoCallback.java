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

package com.arcsoft.arcfacesingle.business.photo;

public interface ITakePhotoCallback {

    /**
     * 相机已开启
     */
    void onCameraOpened();

    /**
     * 拍照失败
     * @param message 失败信息
     */
    void onTakePhotoError(String message);

    /**
     * 拍照
     * @param personSerial 人员唯一标识码
     * @param imgPath 拍照图片本地绝对地址
     */
    void onTakePhoto(String personSerial, String imgPath);
}
