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

package com.arcsoft.arcfacesingle.business.personlist;

import android.net.Uri;

import com.arcsoft.arcfacesingle.data.model.FaceExtractResult;
import com.arcsoft.faceengine.FaceInfo;

public interface IPersonList {

    interface FaceDetectCallback {

        /**
         * 人脸检测结果
         * @param faceExtractResult 检测结果对象
         */
        void faceDetect(FaceExtractResult faceExtractResult);
    }

    /**
     * 初始化
     */
    void init();

    /**
     * 资源释放
     */
    void unUnit();

    /**
     * 设置监听事件
     * @param listener 事件监听
     */
    void setListener(PersonListRespListener listener);

    /**
     * 初始化人脸数据
     */
    void initFaceList();

    /**
     * 将人脸特征值升级至V3.0
     */
    void upgradeFaceFeatureToV3();

    /**
     * 设置CheckBox选择状态
     * @param isChecked 是否选择
     */
    void setCheckBoxChanged(boolean isChecked);

    /**
     * adapter点击事件
     * @param position item下标
     */
    void setAdapterItemClick(int position);

    /**
     * adapter长点击事件
     * @param position item下标
     */
    void setAdapterItemLongClick(int position);

    /**
     * 下拉加载更多数据
     */
    void loadMoreListData();

    /**
     * 使用ArcFace SDK检测图片
     * @param source 来源
     * @param uri 图片地址
     * @param filePath 图片地址
     * @param personSerial 人员唯一标识
     * @param callback 回调
     */
    void processPictureByFaceEngine(int source, Uri uri, String filePath, String personSerial, FaceDetectCallback callback);

    /**
     * 注册人脸到本地数据库
     * @param name 人员姓名
     * @param personId 人员id
     * @param personSerial 人员唯一标识码
     * @param faceInfo 人员信息
     * @param callback 回调
     */
    void savePersonToDatabase(String name, String personId, String personSerial, FaceInfo faceInfo, FaceDetectCallback callback);

    /**
     * 取消注册人脸
     */
    void cancelRegister();

    /**
     * 确认删除已选择的人脸图片数据
     */
    void confirmDeleteSelectImage();

    /**
     * 保存拍照图片
     * @param personName 人员姓名
     * @param personId 人员id
     * @return 是否保存成功
     */
    boolean saveSelectImage(String personName, String personId);

    /**
     * 根据查询条件查询人员数据
     * @param stringTag 查询标识
     */
    void searchPersonsByTag(String stringTag);

    /**
     * 清除查询结果
     */
    void clearSearchResult();

    /**
     * 是否处于编辑状态
     * @return true 编辑状态；false 未编辑状态
     */
    boolean isEditing();

    /**
     * 重置编辑状态
     */
    void resetEditing();

    /**
     * 导出数据库
     */
    void exportDatabase();

    /**
     * 导入数据库中的人员数据
     * @param listener 事件监听
     */
    void importPersonFromDatabase(PersonListRepository.ImportDatabaseListener listener);

    /**
     * 是否处于查询成功状态
     * @return true 成功； false 失败
     */
    boolean searchPersonSuccess();
}
