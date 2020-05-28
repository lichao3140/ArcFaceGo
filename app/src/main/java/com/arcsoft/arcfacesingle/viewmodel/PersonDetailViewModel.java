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

package com.arcsoft.arcfacesingle.viewmodel;

import android.text.Editable;
import android.text.TextUtils;
import android.view.View;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.ObservableField;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.business.persondetail.IPersonDetailRep;
import com.arcsoft.arcfacesingle.business.persondetail.PersonDetailRepository;
import com.arcsoft.arcfacesingle.data.db.table.TablePerson;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonFace;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonPermission;
import com.arcsoft.arcfacesingle.data.event.RefreshAdapterEvent;
import com.arcsoft.arcfacesingle.navigator.IPersonDetailNavigator;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.asg.libcommon.util.business.DoubleClickUtils;
import com.arcsoft.asg.libcommon.util.common.ActivityUtils;
import com.arcsoft.asg.libcommon.util.common.KeyboardUtils;
import com.arcsoft.asg.libcommon.util.common.ToastUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class PersonDetailViewModel extends BaseObservable {

    private static final int SIZE_FACE_LIST = 5;
    private IPersonDetailRep iPersonDetailRep;
    private IPersonDetailNavigator navigator;
    private TablePerson tablePerson;

    @Bindable
    public final ObservableField<String> fieldPersonNameTitle = new ObservableField<>();
    public final ObservableField<String> fieldPersonName = new ObservableField<>();
    public final ObservableField<String> fieldPersonId = new ObservableField<>();
    public final ObservableField<String> fieldIcCardNo = new ObservableField<>();
    public final ObservableField<String> fieldPersonPermission1 = new ObservableField<>();
    public final ObservableField<String> fieldPersonPermission2 = new ObservableField<>();
    public final ObservableField<String> fieldHeadPath1 = new ObservableField<>("");
    public final ObservableField<String> fieldHeadPath2 = new ObservableField<>("");
    public final ObservableField<String> fieldHeadPath3 = new ObservableField<>("");
    public final ObservableField<String> fieldHeadPath4 = new ObservableField<>("");
    public final ObservableField<String> fieldHeadPath5 = new ObservableField<>("");
    public final ObservableField<String> fieldHeadPath6 = new ObservableField<>("");
    public final ObservableField<Boolean> fieldHeadVisible1 = new ObservableField<>(false);
    public final ObservableField<Boolean> fieldHeadVisible2 = new ObservableField<>(false);
    public final ObservableField<Boolean> fieldHeadVisible3 = new ObservableField<>(false);
    public final ObservableField<Boolean> fieldHeadVisible4 = new ObservableField<>(false);
    public final ObservableField<Boolean> fieldHeadVisible5 = new ObservableField<>(false);
    public final ObservableField<Boolean> fieldHeadVisible6 = new ObservableField<>(false);
    public final ObservableField<Boolean> fieldEditNameVisible = new ObservableField<>(false);
    public final ObservableField<Boolean> fieldEditIdVisible = new ObservableField<>(false);
    public final ObservableField<Boolean> fieldEditVisible = new ObservableField<>(false);
    public final ObservableField<Boolean> fieldIcCardVisible = new ObservableField<>();
    public final ObservableField<Boolean> fieldBorderVisible = new ObservableField<>(false);
    public final ObservableField<Boolean> fieldNoPermissionVisible = new ObservableField<>(false);

    public PersonDetailViewModel() {
        iPersonDetailRep = new PersonDetailRepository();
    }

    public void setNavigator(IPersonDetailNavigator navigator) {
        this.navigator = navigator;
    }

    public void initData(String personSerial) {
        fieldEditVisible.set(CommonUtils.isOfflineLanAppMode());
        fieldIcCardVisible.set(CommonUtils.isOfflineLanAppMode());
        iPersonDetailRep.initPersonData(personSerial, pair -> {
            tablePerson = pair.first;
            List<TablePersonFace> faceList = pair.second;
            fieldPersonNameTitle.set(tablePerson.personName);
            fieldPersonName.set(tablePerson.personName);
            fieldPersonId.set(tablePerson.personInfoNo);
            fieldIcCardNo.set(tablePerson.icCardNo);

            TablePersonPermission permission = iPersonDetailRep.getPermissionFromDb(personSerial );
            navigator.updateDateUi(iPersonDetailRep.compareDate(tablePerson, permission));
            fieldPersonPermission1.set(iPersonDetailRep.getDateString(tablePerson, permission));
            fieldPersonPermission2.set(iPersonDetailRep.getWorkingDaysString(tablePerson, permission));
            navigator.updateTimePermission(iPersonDetailRep.getTimeMap(tablePerson, permission));

            int faceSize = faceList.size();
            if (faceSize > SIZE_FACE_LIST) {
                faceList = faceList.subList(0, SIZE_FACE_LIST);
                faceSize = SIZE_FACE_LIST;
            }
            fieldBorderVisible.set(iPersonDetailRep.existMainFace(faceList, tablePerson.mainFaceId));
            switch (faceSize) {
                case 0:
                    fieldBorderVisible.set(false);
                    break;
                case 1:
                    fieldHeadPath1.set(faceList.get(0).imagePath);
                    fieldHeadVisible1.set(!TextUtils.isEmpty(faceList.get(0).imagePath));
                    break;
                case 2:
                    fieldHeadPath1.set(faceList.get(0).imagePath);
                    fieldHeadVisible1.set(!TextUtils.isEmpty(faceList.get(0).imagePath));
                    fieldHeadPath2.set(faceList.get(1).imagePath);
                    fieldHeadVisible2.set(!TextUtils.isEmpty(faceList.get(1).imagePath));
                    break;
                case 3:
                    fieldHeadPath1.set(faceList.get(0).imagePath);
                    fieldHeadVisible1.set(!TextUtils.isEmpty(faceList.get(0).imagePath));
                    fieldHeadPath2.set(faceList.get(1).imagePath);
                    fieldHeadVisible2.set(!TextUtils.isEmpty(faceList.get(1).imagePath));
                    fieldHeadPath3.set(faceList.get(2).imagePath);
                    fieldHeadVisible3.set(!TextUtils.isEmpty(faceList.get(2).imagePath));
                    break;
                case 4:
                    fieldHeadPath1.set(faceList.get(0).imagePath);
                    fieldHeadVisible1.set(!TextUtils.isEmpty(faceList.get(0).imagePath));
                    fieldHeadPath2.set(faceList.get(1).imagePath);
                    fieldHeadVisible2.set(!TextUtils.isEmpty(faceList.get(1).imagePath));
                    fieldHeadPath3.set(faceList.get(2).imagePath);
                    fieldHeadVisible3.set(!TextUtils.isEmpty(faceList.get(2).imagePath));
                    fieldHeadPath4.set(faceList.get(3).imagePath);
                    fieldHeadVisible4.set(!TextUtils.isEmpty(faceList.get(3).imagePath));
                    break;
                case 5:
                    fieldHeadPath1.set(faceList.get(0).imagePath);
                    fieldHeadVisible1.set(!TextUtils.isEmpty(faceList.get(0).imagePath));
                    fieldHeadPath2.set(faceList.get(1).imagePath);
                    fieldHeadVisible2.set(!TextUtils.isEmpty(faceList.get(1).imagePath));
                    fieldHeadPath3.set(faceList.get(2).imagePath);
                    fieldHeadVisible3.set(!TextUtils.isEmpty(faceList.get(2).imagePath));
                    fieldHeadPath4.set(faceList.get(3).imagePath);
                    fieldHeadVisible4.set(!TextUtils.isEmpty(faceList.get(3).imagePath));
                    fieldHeadPath5.set(faceList.get(4).imagePath);
                    fieldHeadVisible5.set(!TextUtils.isEmpty(faceList.get(4).imagePath));
                    break;
                default:
                    break;
            }
        });
    }

    public void onPersonNameTextChanged(Editable editable) {
        String content = editable.toString().trim();
        if (!TextUtils.isEmpty(content)) {
            fieldPersonName.set(content);
        } else {
            fieldPersonName.set("");
        }
    }

    public void onPersonIdTextChanged(Editable editable) {
        String content = editable.toString().trim();
        if (TextUtils.isEmpty(content)) {
            fieldPersonId.set("");
        } else {
            fieldPersonId.set(content);
        }
    }

    public void onClick(View v) {
        int resId = v.getId();
        if (DoubleClickUtils.isFastDoubleClick(resId)) {
            return;
        }
        switch (resId) {
            case R.id.iv_person_detail_name_edit:
                fieldEditNameVisible.set(true);
                break;
            case R.id.iv_person_detail_name_confirm:
                String fieldName = fieldPersonName.get();
                if (fieldName == null) {
                    ToastUtils.showShortToast(CommonUtils.getStrFromRes(R.string.please_input_name));
                    return;
                }
                String strName = fieldName.trim().replaceAll(" ", "");
                if (TextUtils.isEmpty(strName)) {
                    ToastUtils.showShortToast(CommonUtils.getStrFromRes(R.string.please_input_name));
                    return;
                }
                if (iPersonDetailRep.savePerson(tablePerson, PersonDetailRepository.TYPE_EDIT_NAME, fieldPersonName.get())) {
                    fieldPersonNameTitle.set(tablePerson.personName);
                    fieldEditNameVisible.set(false);
                    KeyboardUtils.hideSoftInput(ActivityUtils.getTopActivity());
                    EventBus.getDefault().post(new RefreshAdapterEvent());
                }
                break;
            case R.id.iv_person_detail_id_edit:
                fieldEditIdVisible.set(true);
                break;
            case R.id.iv_person_detail_id_confirm:
                if (iPersonDetailRep.savePerson(tablePerson, PersonDetailRepository.TYPE_EDIT_ID, fieldPersonId.get())) {
                    fieldEditIdVisible.set(false);
                    KeyboardUtils.hideSoftInput(ActivityUtils.getTopActivity());
                    EventBus.getDefault().post(new RefreshAdapterEvent());
                }
                break;
            default:
                break;
        }
    }

    public void release() {
        iPersonDetailRep = null;
        tablePerson = null;
    }
}
