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

package com.arcsoft.arcfacesingle.business.persondetail;

import android.text.TextUtils;
import android.util.Pair;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.business.recognize.RecognizeRepDataManager;
import com.arcsoft.arcfacesingle.data.db.dao.PersonDao;
import com.arcsoft.arcfacesingle.data.db.dao.PersonFaceDao;
import com.arcsoft.arcfacesingle.data.db.dao.PersonPermissionDao;
import com.arcsoft.arcfacesingle.data.db.table.TablePerson;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonFace;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonPermission;
import com.arcsoft.arcfacesingle.server.api.ServerConstants;
import com.arcsoft.arcfacesingle.server.pojo.request.RequestDoorAuthorityV2;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.asg.libcommon.util.business.rx.RxUtils;
import com.arcsoft.asg.libcommon.util.common.TimeUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;

public class PersonDetailRepository implements IPersonDetailRep {

    private static final String WORKING_DAY = "1,2,3,4,5";
    public static final int TYPE_EDIT_NAME = 0;
    public static final int TYPE_EDIT_ID = 1;
    private static final int SIZE_WEEK_COUNT = 7;

    private Disposable initDisposable;

    @Override
    public void initPersonData(String personSerial, InitPersonDataCallback callback) {
        initDisposable = Observable.create((ObservableOnSubscribe<Pair<TablePerson, List<TablePersonFace>>>) emitter -> {
            TablePerson tablePerson = PersonDao.getInstance().getPersonByPersonSerial(personSerial);
            List<TablePersonFace> faceList = PersonFaceDao.getInstance().getListByPersonSerial(personSerial);
            int pos = 0;
            for (int i = 0; i < faceList.size(); i++) {
                TablePersonFace face = faceList.get(i);
                if (!TextUtils.isEmpty(face.faceId) && face.faceId.equals(tablePerson.mainFaceId)) {
                    pos = i;
                    break;
                }
            }
            if (pos != 0) {
                Collections.swap(faceList, pos, 0);
            }
            emitter.onNext(new Pair<>(tablePerson, faceList));
            emitter.onComplete();
        }).compose(RxUtils.ioToMain())
                .subscribe(callback::loadDataSuccess, throwable -> {
                });
    }

    @Override
    public boolean savePerson(TablePerson tablePerson, int type, String editContent) {
        if (TextUtils.isEmpty(editContent)) {
            if (type == TYPE_EDIT_NAME) {
                return false;
            } else {
                tablePerson.personInfoNo = "";
                return PersonDao.getInstance().updatePerson(tablePerson);
            }
        } else {
            if (type == TYPE_EDIT_NAME) {
                tablePerson.personName = editContent;
            } else {
                tablePerson.personInfoNo = editContent;
            }
            return PersonDao.getInstance().updatePerson(tablePerson);
        }
    }

    @Override
    public boolean existMainFace(List<TablePersonFace> faceList, String mainId) {
        if (faceList == null || faceList.isEmpty() || TextUtils.isEmpty(mainId)) {
            return false;
        }
        boolean exist = false;
        for (TablePersonFace face : faceList) {
            if (mainId.equals(face.faceId)) {
                exist = true;
                break;
            }
        }
        return exist;
    }

    private void disposeDisposable() {
        if (initDisposable != null && !initDisposable.isDisposed()) {
            initDisposable.dispose();
        }
    }

    @Override
    public TablePersonPermission getPermissionFromDb(String personSerial) {
        List<TablePersonPermission> permissions = PersonPermissionDao.getInstance().getListByPersonSerial(personSerial);
        if (permissions != null && !permissions.isEmpty()) {
            return permissions.get(0);
        }
        return null;
    }

    @Override
    public boolean compareDate(TablePerson tablePerson, TablePersonPermission permission) {
        if (permission == null) {
            return true;
        }
        String currentDate = TimeUtils.millis2String(System.currentTimeMillis(), TimeUtils.DATE_PATTERN_3);
        return RecognizeRepDataManager.getInstance().permissionDate(permission.getStartDate(), currentDate,
                permission.getEndDate());
    }

    @Override
    public String getDateString(TablePerson tablePerson, TablePersonPermission permission) {
        String notLimit = CommonUtils.getStrFromRes(R.string.not_limit);
        String stringDate = notLimit + " - " + notLimit;
        if (permission == null) {
            return stringDate;
        }
        String dateStart = permission.getStartDate();
        String dateEnd = permission.getEndDate();
        String stringStart = TextUtils.isEmpty(dateStart) ? notLimit : dateStart.replaceAll("-", ".");
        String stringEnd = TextUtils.isEmpty(dateEnd) ? notLimit : dateEnd.replaceAll("-", ".");
        return stringStart + " - " + stringEnd;
    }

    @Override
    public String getWorkingDaysString(TablePerson tablePerson, TablePersonPermission permission) {
        if (permission == null) {
            return CommonUtils.getStrFromRes(R.string.permission_every_day);
        }
        String oriWorkdays = permission.getWorkingDays();
        if (ServerConstants.WEEK_NO_PERMISSION.equals(oriWorkdays)) {
            return CommonUtils.getStrFromRes(R.string.permission_no_permission);
        }
        List<String> oriWeeks = Arrays.asList(oriWorkdays.split(","));
        List<String> weeks = new ArrayList<>(new HashSet<>(oriWeeks));
        Collections.sort(weeks);
        int size = weeks.size();
        if (size == SIZE_WEEK_COUNT) {
            return CommonUtils.getStrFromRes(R.string.permission_every_day);
        } else {
            StringBuilder workBuilder = new StringBuilder();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < size; i++) {
                int day = Integer.parseInt(weeks.get(i));
                String strDay = CommonUtils.getWeekDayString(day);
                builder.append(strDay);
                workBuilder.append(day);
                if (i < size - 1) {
                    builder.append("、");
                    workBuilder.append(",");
                }
            }
            String strWork = workBuilder.toString();
            if (WORKING_DAY.equals(strWork)) {
                return CommonUtils.getStrFromRes(R.string.working_day);
            } else {
                return builder.toString();
            }
        }
    }

    @Override
    public LinkedHashMap<String, String> getTimeMap(TablePerson tablePerson, TablePersonPermission permission) {
        LinkedHashMap<String, String> hashMap = new LinkedHashMap<>(3);
        String strRange = CommonUtils.getStrFromRes(R.string.permission_time_range);
        String strColon = CommonUtils.getStrFromRes(R.string.colon);
        if (permission == null) {
            hashMap.put(strRange + 1 + strColon, tablePerson.authMorningStartTime + " - " + tablePerson.authMorningEndTime);
            hashMap.put(strRange + 2 + strColon, tablePerson.authNoonStartTime + " - " + tablePerson.authNoonEndTime);
            hashMap.put(strRange + 3 + strColon, tablePerson.authNightStartTime + " - " + tablePerson.authNightEndTime);
        } else {
            List<RequestDoorAuthorityV2.DoorAuthorityDetail.TimeAuthority> times =
                    new Gson().fromJson(permission.getTimeAndDesc(),
                            new TypeToken<List<RequestDoorAuthorityV2.DoorAuthorityDetail.TimeAuthority>>() {
                            }.getType());
            for (int i = 0; i < times.size(); i++) {
                RequestDoorAuthorityV2.DoorAuthorityDetail.TimeAuthority time = times.get(i);
                hashMap.put(strRange + (i + 1) + strColon, time.getStartTime() + " - " + time.getEndTime());
            }
        }
        return hashMap;
    }

    @Override
    public void release() {
        disposeDisposable();
    }
}