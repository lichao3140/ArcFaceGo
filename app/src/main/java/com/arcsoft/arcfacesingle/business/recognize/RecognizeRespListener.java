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

import com.arcsoft.arcfacesingle.data.db.table.TablePerson;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonFace;

public interface RecognizeRespListener {

    /**
     * 识别成功
     * @param tablePerson 人员信息
     * @param tablePersonFace 人脸信息
     * @param faceId 人脸id
     */
    void onRecognitionSuccess(TablePerson tablePerson, TablePersonFace tablePersonFace, int faceId);

    /**
     * 识别无权限通过
     * @param tablePerson 人员信息
     * @param tablePersonFace 人脸信息
     * @param faceId 人脸id
     */
    void onRecognitionUnauthorized(TablePerson tablePerson, TablePersonFace tablePersonFace, int faceId);

    /**
     * 识别失败
     * @param faceId 人脸id
     */
    void onRecognitionFail(int faceId);

    /**
     * 隐藏结果UI信息
     */
    void onHideRecognitionResult();

    /**
     * ft检测到人脸
     * @param faceId 人脸id
     */
    void onFaceTrackHasFaceInfo(Integer faceId);

    /**
     * ft没有检测到人脸
     */
    void onFaceTrackNoFaceInfo();

    /**
     * 设置活体检测结果
     * @param strResult 活体结果信息
     */
    void setFaceLiveResult(String strResult);

    /**
     * 人脸识别引擎初始化结果回调
     * @param message 结果信息
     */
    void onEngineCallBack(String message);

    /**
     * 控制“请正视摄像头”显隐
     * @param visible true 显示；false 隐藏
     */
    void setFaceUpCamera(boolean visible);

    /**
     * 设置提示内容
     * @param content 内容信息
     */
    void setHintContent(String content);

    /**
     * 设置副View显示隐藏
     */
    void setViceCameraViewVisible();

    /**
     * 相机已开启成功
     * @param mainCamera true 主相机；false 副相机
     */
    void onCameraOpened(boolean mainCamera);
}
