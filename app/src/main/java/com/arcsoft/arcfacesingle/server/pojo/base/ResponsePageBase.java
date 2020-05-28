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

package com.arcsoft.arcfacesingle.server.pojo.base;

import java.io.Serializable;
import java.util.List;

public class ResponsePageBase<T> implements Serializable {

    /**
     * 总数
     */
    private int totalCount;

    /**
     * 当前页数
     */
    private int pageIndex;

    /**
     * 总页数
     */
    private int pageCount;

    /**
     * 当前页数据数量
     */
    private int pageSize;

    /**
     * 数据列表
     */
    private List<T> dataInfo;

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public List<T> getDataInfo() {
        return dataInfo;
    }

    public void setDataInfo(List<T> dataInfo) {
        this.dataInfo = dataInfo;
    }
}
