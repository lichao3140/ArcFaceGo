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

package com.arcsoft.arcfacesingle.data.model;

import com.arcsoft.arcfacesingle.data.db.table.TablePerson;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonFace;
import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.io.Serializable;
import java.util.List;

public class PersonDetailList implements Serializable, MultiItemEntity {

    public static final int TYPE_ITEM_TEXT = 0;
    public static final int TYPE_ITEM_IMAGE_THREE = 1;
    public static final int TYPE_ITEM_IMAGE_FIVE = 2;

    public static final int TYPE_EDIT_STATUS_NO = 0;
    public static final int TYPE_EDIT_STATUS_YES = TYPE_EDIT_STATUS_NO + 1;

    /**
     * 条目类型（0：人员图片；1 人员信息）
     */
    private int dataType;

    /**
     * 姓名编辑状态：0 不编辑；1 编辑
     */
    private int editName;

    /**
     * ID编辑状态：0 不编辑；1 编辑
     */
    private int editId;

    private List<TablePersonFace> faceList;

    private TablePerson person;

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public List<TablePersonFace> getFaceList() {
        return faceList;
    }

    public void setFaceList(List<TablePersonFace> faceList) {
        this.faceList = faceList;
    }

    public TablePerson getPerson() {
        return person;
    }

    public void setPerson(TablePerson person) {
        this.person = person;
    }

    public int getEditName() {
        return editName;
    }

    public void setEditName(int editName) {
        this.editName = editName;
    }

    public int getEditId() {
        return editId;
    }

    public void setEditId(int editId) {
        this.editId = editId;
    }

    @Override
    public int getItemType() {
        return dataType;
    }
}
