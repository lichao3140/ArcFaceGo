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

package com.arcsoft.arcfacesingle.app;

public class BusinessErrorCode {

    /**
     * 通用
     */
    private final static int BEC_COMMON_BASE = 0;
    /**
     * 人脸管理模块
     */
    private final static int BEC_FACE_MANAGER_BASE = 1000;

    /***************************通用错误码******************************/

    /**
     * 成功
     */
    public static final int BEC_COMMON_OK = BEC_COMMON_BASE;
    /**
     * 未知错误
     */
    public static final int BEC_COMMON_UNKNOWN = -1;


    /************************人脸管理错误码******************************/
    /**
     * 检测失败
     */
    public static final int BEC_FACE_MANAGER_DETECT_FAIL = BEC_FACE_MANAGER_BASE + 1;
    /**
     * 多于一个人脸
     */
    public static final int BEC_FACE_MANAGER_MORE_THAN_ONE_FACE = BEC_FACE_MANAGER_BASE + 2;
    /**
     * 角度检测失败
     */
    public static final int BEC_FACE_MANAGER_DEGREE_DETECT_FAIL = BEC_FACE_MANAGER_BASE + 3;
    /**
     * 角度过大
     */
    public static final int BEC_FACE_MANAGER_DEGREE_BIG = BEC_FACE_MANAGER_BASE + 4;
    /**
     * 特征提取失败
     */
    public static final int BEC_FACE_MANAGER_RECOGNIZE_FAIL = BEC_FACE_MANAGER_BASE + 5;
    /**
     * 获取图片失败
     */
    public static final int BEC_FACE_MANAGER_IMAGE_INVALID = BEC_FACE_MANAGER_BASE + 6;
    /**
     * 人员保存失败
     */
    public static final int BEC_FACE_MANAGER_PERSON_SAVE_FAILED = BEC_FACE_MANAGER_BASE + 7;
    /**
     * 人员图片保存至本地失败
     */
    public static final int BEC_FACE_MANAGER_SAVE_BITMAP_FAILED = BEC_FACE_MANAGER_BASE + 8;
    /**
     * 人员图片添加失败
     */
    public static final int BEC_FACE_MANAGER_ADD_FACE_FAILED = BEC_FACE_MANAGER_BASE + 9;
    /**
     * 没有检测到人脸
     */
    public static final int BEC_FACE_MANAGER_NO_FACE = BEC_FACE_MANAGER_BASE + 10;
    /**
     * 质量检测不过
     */
    public static final int BEC_FACE_MANAGER_FACE_QUALITY_FAIL = BEC_FACE_MANAGER_BASE + 11;
    /**
     * 设备存储空间不足
     */
    public static final int BEC_FACE_MANAGER_LESS_DEVICE_STORAGE = BEC_FACE_MANAGER_BASE + 12;
}
