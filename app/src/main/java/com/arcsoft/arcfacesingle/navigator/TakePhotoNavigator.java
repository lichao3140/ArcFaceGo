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

public interface TakePhotoNavigator {

    /**
     * 拍照确认
     * @param imagePath 图片地址
     * @param personSerial 人员唯一标识
     */
    void confirmTakePhoto(String imagePath, String personSerial);

    /**
     * 设置相机UI显示
     * @param width 宽度
     * @param height 高度
     */
    void setPhotoResultParams(int width, int height);

    /**
     * 设置相机View显示隐藏
     * @param visible true 显示；false 隐藏
     */
    void setCameraFaceViceVisible(boolean visible);
}
