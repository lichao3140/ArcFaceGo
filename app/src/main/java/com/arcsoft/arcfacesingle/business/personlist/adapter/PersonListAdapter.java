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

package com.arcsoft.arcfacesingle.business.personlist.adapter;

import android.text.TextUtils;
import android.widget.ImageView;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.data.db.dao.PersonDao;
import com.arcsoft.arcfacesingle.data.db.dao.PersonFaceDao;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonFace;
import com.arcsoft.arcfacesingle.data.model.PersonInfo;
import com.arcsoft.arcfacesingle.util.glide.GlideUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

public class PersonListAdapter extends BaseQuickAdapter<PersonInfo, BaseViewHolder> {

    public PersonListAdapter(List<PersonInfo> data) {
        super(R.layout.item_face_manage, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, PersonInfo item) {
        String strPersonName = item.getPersonName();
        helper.setText(R.id.tv_person_name, TextUtils.isEmpty(strPersonName) ? "" : strPersonName)
                .setVisible(R.id.cb_select_face, item.isSelected())
                .setChecked(R.id.cb_select_face, item.isSelected());
        String personIdInfo = item.getPersonInfoNo();
        helper.setText(R.id.tv_person_id_info, TextUtils.isEmpty(personIdInfo) ? "" : personIdInfo);
        ImageView imageView = helper.getView(R.id.iv_face_manage_head);
        int personInfoType = item.getPersonInfoType();
        if (personInfoType == PersonDao.TYPE_PERSON_INFO_ONLY_IC_CARD) {
            GlideUtils.loadPersonAdapterImage("", imageView);
        } else {
            String mainFaceId = item.getMainFaceId();
            TablePersonFace tablePersonFace;
            if (TextUtils.isEmpty(mainFaceId)) {
                tablePersonFace = PersonFaceDao.getInstance().getPersonFaceBySerial(item.getPersonSerial());
            } else {
                tablePersonFace = PersonFaceDao.getInstance().getPersonFaceByFaceId(mainFaceId);
            }
            if (tablePersonFace != null) {
                String fileUri = tablePersonFace.imagePath;
                GlideUtils.loadPersonAdapterImage(fileUri, imageView);
            } else {
                GlideUtils.loadPersonAdapterImage("", imageView);
            }
        }
    }
}
