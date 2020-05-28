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

import com.arcsoft.arcfacesingle.data.model.PersonInfo;
import com.arcsoft.faceengine.FaceInfo;

import java.util.List;

public interface IPersonListNavigator {

    /**
     * 显示注册人脸姓名弹框
     * @param personSerial 人员唯一标识码
     * @param faceInfoList 人员列表
     */
    void showRegisterFaceNameDialog(String personSerial, FaceInfo faceInfoList);

    /**
     * 显示注册选择弹框
     */
    void showAddFaceDialog();

    /**
     * 显示批量注册进度弹框
     */
    void showBatchRegisterDialog();

    /**
     * 显示Usb批量注册进度弹框
     */
    void showUsbBatchRegisterDialog();

    /**
     * 显示删除人脸确认弹框
     * @param strContent 显示信息
     */
    void showDeleteConfirmDialog(String strContent);

    /**
     * 显示删除人脸照片进度弹框
     * @param deletedCount 已删除数量
     * @param chosenCount 已选择总数
     */
    void showDeletingFaceImageProgressDialog(int deletedCount, int chosenCount);

    /**
     * 删除人脸照片完成
     */
    void deleteFaceImageComplete();

    /**
     * 将列表移动到顶部
     */
    void moveToTop();

    /**
     * 刷新列表
     * @param type 刷新类型
     * @param position item下标
     * @param personInfoList 待刷新人员数据
     */
    void refreshRecyclerView(int type, int position, List<PersonInfo> personInfoList);

    /**
     * 显示人员详情弹框
     * @param position 当前人员下标
     * @param personInfo 人员信息
     */
    void showPersonInfoDialog(int position, PersonInfo personInfo);

    /**
     * 设置底部删除按钮状态
     * @param enable 是否可用
     */
    void setBottomDeleteEnable(boolean enable);

    /**
     * 关闭页面
     * @param fromSplash 是否来源于启动页
     */
    void closePage(boolean fromSplash);

    /**
     * 显示导入外部数据库人脸弹框
     * @param total 人脸总数
     */
    void showImportDatabaseDialog(long total);

    /**
     * 设置导入人脸库进度
     * @param total 总数
     * @param current 当前进度
     */
    void setImportDatabaseProgress(long total, long current);

    /**
     * 关闭导入数据弹框
     */
    void closeImportDatabaseDialog();

    /**
     * 显示批量升级人员照片Feature弹框
     * @param total 人员总数
     */
    void showUpgradeFaceFeatureDialog(long total);
}
