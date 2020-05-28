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

import com.arcsoft.arcfacesingle.data.model.PersonInfo;

import java.util.List;

public interface PersonListRespListener {

    /**
     * 加载本地人脸
     * @param personTotal 总数
     */
    void initPersonList(long personTotal);

    /**
     * 搜索人脸结果
     * @param personTotal 总数
     */
    void searchPersonList(long personTotal);

    /**
     * 首次人脸库加载完成
     * @param totalPage 总页数
     * @param mAdapterDataList 人脸底库数据
     */
    void firstLoadComplete(int totalPage, List<PersonInfo> mAdapterDataList);

    /**
     * 单次点击事件
     * @param position 点击的item下标
     */
    void onAdapterItemClick(int position);

    /**
     * 选中删除的数据
     * @param chosenList 已选择人员数据
     */
    void onAdapterItemChosen(List<PersonInfo> chosenList);

    /**
     * 长点击事件
     * @param position 点击的item下标
     */
    void onAdapterItemLongClick(int position);

    /**
     * 删除已选择的人脸数据进度
     * @param deletedCount 已删除数量
     * @param chosenCount 已选择总数
     */
    void onAdapterItemDeleteProgress(int deletedCount, int chosenCount);

    /**
     * 删除人脸照片完成
     */
    void onAdapterItemDeleteComplete();

    /**
     * 下拉加载更多-没有更多数据
     */
    void loadMoreEnd();

    /**
     * 下拉加载更多-本次加载完成
     * @param tablePeoples 人员数据集
     */
    void loadMoreComplete(List<PersonInfo> tablePeoples);

    /**
     * 显示人员详情弹框
     * @param position item下标
     * @param personInfo 人员数据
     */
    void showPersonInfoDialog(int position, PersonInfo personInfo);

    /**
     * 显示批量升级人员照片Feature进度弹框
     * @param total 总数
     */
    void showUpgradeFaceFeatureDialog(long total);

    /**
     * 清除搜索状态
     */
    void clearSearchStatus();

    /**
     * 清除搜搜索内容
     */
    void clearSearchContent();
}
