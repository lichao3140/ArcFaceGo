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

package com.arcsoft.arcfacesingle.business.arclink;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.business.setting.ConfigConstants;
import com.arcsoft.arcfacesingle.data.db.dao.IdentifyRecordDao;
import com.arcsoft.arcfacesingle.data.db.dao.PersonDao;
import com.arcsoft.arcfacesingle.data.db.dao.PersonFaceDao;
import com.arcsoft.arcfacesingle.data.db.dao.PersonPermissionDao;
import com.arcsoft.arcfacesingle.data.db.table.TablePerson;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonFace;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonPermission;
import com.arcsoft.arcfacesingle.data.db.table.TableSignRecord;
import com.arcsoft.arcfacesingle.data.event.RefreshAdapterEvent;
import com.arcsoft.arcfacesingle.data.event.arclink.ArcLinkUpdatePersonEvent;
import com.arcsoft.arcfacesingle.libutil.ImageFileUtils;
import com.arcsoft.arcfacesingle.server.api.ServerConstants;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.arcfacesingle.util.SdcardUtils;
import com.arcsoft.arcfacesingle.util.download.FileDownloadManager;
import com.arcsoft.arcfacesingle.util.scheduler.BaseObserver;
import com.arcsoft.arcfacesingle.util.scheduler.ExceptionHandler;
import com.arcsoft.arcfacesingle.view.activity.PersonDetailActivity;
import com.arcsoft.arcfacesingle.view.activity.PersonListActivity;
import com.arcsoft.arcfacesingle.view.activity.RecognizeActivity;
import com.arcsoft.arcsoftlink.enums.ArcFaceVersionEnum;
import com.arcsoft.arcsoftlink.enums.MqttResponseCodeEnum;
import com.arcsoft.arcsoftlink.enums.MqttResponseStateEnum;
import com.arcsoft.arcsoftlink.enums.PersonSyncActionEnum;
import com.arcsoft.arcsoftlink.enums.SyncTypeEnum;
import com.arcsoft.arcsoftlink.enums.WeekEnum;
import com.arcsoft.arcsoftlink.http.bean.res.BaseResponse;
import com.arcsoft.arcsoftlink.http.bean.res.PersonSyncResponse;
import com.arcsoft.arcsoftlink.mqtt.ArcLinkEngine;
import com.arcsoft.arcsoftlink.mqtt.ArcLinkException;
import com.arcsoft.arcsoftlink.mqtt.EventCallback;
import com.arcsoft.arcsoftlink.mqtt.bean.PersonSyncInfo;
import com.arcsoft.asg.libcommon.util.business.rx.RxUtils;
import com.arcsoft.asg.libcommon.util.common.ActivityUtils;
import com.arcsoft.asg.libcommon.util.common.FileUtils;
import com.arcsoft.asg.libcommon.util.common.SPUtils;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.disposables.Disposable;

public class ArcLinkPersonManager {

    private static final String TAG = ArcLinkPersonManager.class.getSimpleName();

    private static ArcLinkPersonManager instance;
    /**
     * 是否正在同步人脸数据
     */
    private boolean syncPersonFlag;

    private ArcLinkPersonManager() {
    }

    public static ArcLinkPersonManager getInstance() {
        if (instance == null) {
            synchronized (ArcLinkPersonManager.class) {
                if (instance == null) {
                    instance = new ArcLinkPersonManager();
                }
            }
        }
        return instance;
    }

    public void syncPersonInfoFromArcLink(EventCallback eventCallback) {
        if (syncPersonFlag) {
            setEventCallback(eventCallback, false, "正在进行人员同步操作，请稍后再试");
            return;
        }
        syncPersonFlag = true;
        Disposable syncPersonDisposable = Observable.create(((ObservableEmitter<Boolean> emitter) -> {
            String signKey = SPUtils.getInstance().getString(Constants.SP_KEY_ARC_LINK_PERSON_SYNC_KEY);
            syncPersonInfo(signKey, eventCallback, emitter);
        })).compose(RxUtils.ioToMain())
                .subscribeWith(new BaseObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean success) {
                        syncPersonFlag = false;
                        SPUtils.getInstance().put(Constants.SP_KEY_ARC_LINK_PERSON_SYNC_EXCEPTION, !success);
                    }

                    @Override
                    public void onError(ExceptionHandler.ResponseThrowable throwable) {
                        syncPersonFlag = false;
                        SPUtils.getInstance().put(Constants.SP_KEY_ARC_LINK_PERSON_SYNC_EXCEPTION, true);
                        setEventCallback(eventCallback, false, throwable.message);
                    }
                });
    }

    private void syncPersonInfo(String syncKey, EventCallback eventCallback, ObservableEmitter<Boolean> emitter) {
        try {
            PersonSyncResponse syncResponse = ArcLinkEngine.getInstance().getPersonSyncInfo(syncKey,
                    ArcFaceVersionEnum.V_3_0);
            if (syncResponse.getCode() != BaseResponse.CODE_SUCCESS) {
                setEventCallback(eventCallback, false, syncResponse.getMsg());
                emitter.onNext(false);
                emitter.onComplete();
            } else {
                PersonSyncInfo personSyncInfo = syncResponse.getData();
                if (personSyncInfo != null) {
                    if (savePersonAndFace(personSyncInfo)) {
                        String newSyncKey = personSyncInfo.getSyncKey();
                        refreshPersonListUi();
                        SPUtils.getInstance().put(Constants.SP_KEY_ARC_LINK_PERSON_SYNC_KEY, newSyncKey);
                        if (personSyncInfo.getSyncEnd()) {
                            boolean processExceptionFlag = SPUtils.getInstance().getBoolean(Constants.SP_KEY_ARC_LINK_PERSON_SYNC_EXCEPTION);
                            if (processExceptionFlag) {
                                /**
                                 * 需要处理旧任务异常中断导致的异常：新任务开始前需要将旧任务执行至完成。
                                 */
                                SPUtils.getInstance().put(Constants.SP_KEY_ARC_LINK_PERSON_SYNC_EXCEPTION, false);
                                syncPersonInfo(newSyncKey, eventCallback, emitter);
                            } else {
                                setEventCallback(eventCallback, true, null);
                                emitter.onNext(true);
                                emitter.onComplete();
                            }
                        } else {
                            SPUtils.getInstance().put(Constants.SP_KEY_ARC_LINK_PERSON_SYNC_EXCEPTION, true);
                            syncPersonInfo(newSyncKey, eventCallback, emitter);
                        }
                    } else {
                        SPUtils.getInstance().put(Constants.SP_KEY_ARC_LINK_PERSON_SYNC_EXCEPTION, true);
                        refreshPersonListUi();
                        setEventCallback(eventCallback, false, "人员信息更新失败");
                        emitter.onNext(false);
                        emitter.onComplete();
                    }
                } else {
                    setEventCallback(eventCallback, false, "人员库信息数据为空");
                    emitter.onNext(false);
                    emitter.onComplete();
                }
            }
        } catch (ArcLinkException e) {
            e.printStackTrace();
            setEventCallback(eventCallback, false, e.getMessage());
            emitter.onNext(false);
            emitter.onComplete();
        }
    }

    private void setEventCallback(EventCallback eventCallback, boolean state, String msg) {
        eventCallback.onEventFinished(
                MqttResponseCodeEnum.CODE_SUCCESS,
                state ? MqttResponseStateEnum.STATE_SUCCESS : MqttResponseStateEnum.STATE_FAILED,
                msg);
    }

    /**
     * 保存人员和人脸数据
     *
     * @param personSyncInfo
     * @return
     */
    private boolean savePersonAndFace(PersonSyncInfo personSyncInfo) {
        List<PersonSyncInfo.PersonInfoBean> personInfoList = personSyncInfo.getPersonInfoList();
        SyncTypeEnum syncTypeEnum = personSyncInfo.getSyncType();
        if (syncTypeEnum.equals(SyncTypeEnum.FULL)) {
            long count1 = PersonDao.getInstance().getTotalCount();
            long count2 = PersonFaceDao.getInstance().getTotalCount();
            long count3 = IdentifyRecordDao.getInstance().getTotalCount();
            if (count1 + count2 + count3 > 0) {
                PersonDao.getInstance().deleteTable();
                PersonPermissionDao.getInstance().deleteTable();
                List<TablePersonFace> personList = PersonFaceDao.getInstance().queryAllFace();
                deletePersonFaces(personList);
                List<TableSignRecord> recordList = IdentifyRecordDao.getInstance().queryAllRecord();
                for (TableSignRecord record : recordList) {
                    if (record.delete()) {
                        FileUtils.delete(record.imagePath);
                    }
                }
            }
        }
        boolean updateSuccess = true;
        for (PersonSyncInfo.PersonInfoBean personInfoBean : personInfoList) {
            PersonSyncActionEnum actionEnum = personInfoBean.getAction();
            if (actionEnum.equals(PersonSyncActionEnum.REMOVE)) {
                TablePerson tablePerson = PersonDao.getInstance().getPersonById(personInfoBean.getId());
                if (null != tablePerson) {
                    if (PersonDao.getInstance().deletePerson(tablePerson)) {
                        List<TablePersonPermission> permissions = PersonPermissionDao.getInstance().getListByPersonSerial(tablePerson.personSerial);
                        PersonPermissionDao.getInstance().deleteListTransaction(permissions, null);
                        List<TablePersonFace> personFaceModels = PersonFaceDao.getInstance().getPersonListById(personInfoBean.getId());
                        deletePersonFaces(personFaceModels);
                    }
                }
            } else {
                Long personId = null;
                String personSerial = null;
                TablePerson tablePerson = createTablePerson(personInfoBean, actionEnum);
                if (tablePerson != null) {
                    personId = tablePerson.id;
                    personSerial = tablePerson.personSerial;
                }
                List<PersonSyncInfo.FaceListBean> personFaceListList = personInfoBean.getFaceInfoList();
                if (personFaceListList == null || personFaceListList.size() == 0) {
                    updateSuccess = tablePerson != null && tablePerson.save() && updateSuccess;
                    continue;
                }
                /*
                 * 该人员的所有人脸是否保存成功
                 */
                boolean updateFaceAllSuccess = true;
                /*
                 * 该人员只要有一张人脸保存成功即可
                 */
                boolean updateFacePartSuccess = false;
                for (PersonSyncInfo.FaceListBean personFace : personFaceListList) {
                    PersonSyncActionEnum faceActionEnum = personFace.getAction();
                    if (PersonSyncActionEnum.FACE_ADD.equals(faceActionEnum)) {
                        String serverFaceId = personFace.getFaceId();
                        String serverImageUrl = personFace.getImageUrl();
                        String serverFeatureUrl = personFace.getFeatureUrl();
                        boolean saveSuccess = saveTablePersonFace(personId, personSerial, serverFaceId, serverImageUrl, serverFeatureUrl);
                        updateFacePartSuccess |= saveSuccess;
                        updateFaceAllSuccess &= saveSuccess;
                    } else if (PersonSyncActionEnum.FACE_REMOVE.equals(faceActionEnum)) {
                        TablePersonFace personFaceModel = PersonFaceDao.getInstance().getPersonByFaceId(personFace.getFaceId());
                        if (personFaceModel != null) {
                            boolean deleteSuccess = deletePersonFace(personFaceModel);
                            updateFacePartSuccess |= deleteSuccess;
                            updateFaceAllSuccess &= deleteSuccess;
                        } else {
                            updateFaceAllSuccess = false;
                        }
                    }
                }
                if (updateFacePartSuccess) {
                    updateSuccess = tablePerson != null && tablePerson.save() && updateSuccess && updateFaceAllSuccess;
                } else {
                    updateSuccess = false;
                }
            }
        }
        return updateSuccess;
    }

    private TablePerson createTablePerson(PersonSyncInfo.PersonInfoBean personInfoBean, PersonSyncActionEnum actionEnum) {
        String name = personInfoBean.getName();
        String serverName = null;
        if (null != name) {
            serverName = personInfoBean.getName().replaceAll("'", "");
        }
        String serverPersonId = personInfoBean.getPersonId();
        Integer mainFaceId = personInfoBean.getPrimaryFaceId();
        List<PersonSyncInfo.AccessTimeBean> accessTimeList = personInfoBean.getAccessTime();
        TablePerson tablePerson = null;
        if (PersonSyncActionEnum.ADD.equals(actionEnum)) {
            tablePerson = createNewPerson(personInfoBean.getId(), serverName, serverPersonId, mainFaceId, accessTimeList);
        } else if (PersonSyncActionEnum.MODIFY.equals(actionEnum)) {
            tablePerson = PersonDao.getInstance().getPersonById(personInfoBean.getId());
            if (!TextUtils.isEmpty(serverName)) {
                tablePerson.personName = serverName;
            }
            if (!TextUtils.isEmpty(serverPersonId)) {
                tablePerson.personId = serverPersonId;
                tablePerson.personInfoNo = tablePerson.personId.replaceAll("'", "");
            }
            if (null != mainFaceId) {
                tablePerson.mainFaceId = String.valueOf(mainFaceId);
            }
            if (accessTimeList != null && !accessTimeList.isEmpty()) {
                if (!setLimitTime(tablePerson, accessTimeList)) {
                    tablePerson = null;
                }
            }
        }
        return tablePerson;
    }

    /**
     * 删除人脸集合数据
     */
    private void deletePersonFaces(List<TablePersonFace> personFaceModels) {
        for (TablePersonFace personFaceModel : personFaceModels) {
            if (personFaceModel.delete()) {
                File file = FileUtils.getFileByPath(personFaceModel.imagePath);
                if (file != null) {
                    if (FileUtils.delete(file)) {
                        File parentFile = file.getParentFile();
                        if (parentFile.isDirectory() && parentFile.list().length == 0) {
                            FileUtils.deleteDir(parentFile);
                        }
                    }
                }
            }
        }
    }

    /**
     * 删除人脸数据
     */
    private boolean deletePersonFace(TablePersonFace personFaceModel) {
        if (personFaceModel.delete()) {
            File file = FileUtils.getFileByPath(personFaceModel.imagePath);
            if (FileUtils.delete(file)) {
                File parentFile = file.getParentFile();
                if (parentFile.isDirectory() && parentFile.list().length == 0) {
                    FileUtils.deleteDir(parentFile);
                }
                return true;
            }
        }
        return false;
    }

    private TablePerson createNewPerson(long serverPid, String serverName, String serverPersonId,
                                        Integer mainFaceId, List<PersonSyncInfo.AccessTimeBean> accessTimeList) {
        TablePerson tablePerson = PersonDao.getInstance().getPersonById(serverPid);
        if (tablePerson != null) {
            return tablePerson;
        }
        tablePerson = new TablePerson();
        tablePerson.id = serverPid;
        tablePerson.personSerial = CommonUtils.createPersonSerial();
        tablePerson.addTime = System.currentTimeMillis();
        tablePerson.updateTime = tablePerson.addTime;
        tablePerson.personName = serverName;
        tablePerson.personId = serverPersonId;
        tablePerson.personInfoNo = tablePerson.personId;
        tablePerson.icCardNo = "";
        tablePerson.personInfoType = PersonDao.TYPE_PERSON_INFO_ONLY_FACE;
        tablePerson.doorAuthorityDetail = CommonUtils.getStrFromRes(R.string.face_manager_allow_pass);
        tablePerson.authMorningStartTime = ConfigConstants.DOOR_AUTHORITY_DEFAULT_START_TIME;
        tablePerson.authMorningEndTime = ConfigConstants.DOOR_AUTHORITY_DEFAULT_END_TIME;
        tablePerson.authNoonStartTime = tablePerson.authMorningStartTime;
        tablePerson.authNoonEndTime = tablePerson.authMorningEndTime;
        tablePerson.authNightStartTime = tablePerson.authMorningStartTime;
        tablePerson.authNightEndTime = tablePerson.authMorningEndTime;
        if (null != mainFaceId) {
            tablePerson.mainFaceId = String.valueOf(mainFaceId);
        }
        if (accessTimeList != null && !accessTimeList.isEmpty()) {
            if (!setLimitTime(tablePerson, accessTimeList)) {
                tablePerson = null;
            }
        }
        return tablePerson;
    }

    /**
     * 设置门禁权限
     *
     * @param person
     * @param accessTimeList
     */
    private boolean setLimitTime(TablePerson person, List<PersonSyncInfo.AccessTimeBean> accessTimeList) {
        if (person == null) {
            return false;
        }
        Gson gson = new Gson();
        String personSerial = person.personSerial;
        List<TablePersonPermission> permissions = PersonPermissionDao.getInstance().getListByPersonSerial(personSerial);
        if (permissions != null && !permissions.isEmpty()) {
            boolean deleteSuccess = true;
            for (TablePersonPermission permission : permissions) {
                deleteSuccess &= PersonPermissionDao.getInstance().deleteModel(permission);
            }
            if (!deleteSuccess) {
                return false;
            }
        }
        PersonSyncInfo.AccessTimeBean accessTime = accessTimeList.get(0);
        TablePersonPermission permission = new TablePersonPermission();
        List<PersonSyncInfo.DetailTimeBean> timeList = accessTime.getTimeList();
        if (timeList == null || timeList.isEmpty()) {
            timeList = new ArrayList<>();
            PersonSyncInfo.DetailTimeBean detailTimeBean = new PersonSyncInfo.DetailTimeBean();
            detailTimeBean.setStartTime(ServerConstants.DEFAULT_START_TIME);
            detailTimeBean.setEndTime(ServerConstants.DEFAULT_END_TIME);
            detailTimeBean.setTimeDesc(ServerConstants.TAG_MORNING);

            PersonSyncInfo.DetailTimeBean detailTimeBean2 = new PersonSyncInfo.DetailTimeBean();
            detailTimeBean2.setStartTime(ServerConstants.DEFAULT_START_TIME);
            detailTimeBean2.setEndTime(ServerConstants.DEFAULT_START_TIME);
            detailTimeBean2.setTimeDesc(ServerConstants.TAG_AFTERNOON);

            PersonSyncInfo.DetailTimeBean detailTimeBean3 = new PersonSyncInfo.DetailTimeBean();
            detailTimeBean3.setStartTime(ServerConstants.DEFAULT_START_TIME);
            detailTimeBean3.setEndTime(ServerConstants.DEFAULT_START_TIME);
            detailTimeBean3.setTimeDesc(ServerConstants.TAG_EVENING);

            timeList.add(detailTimeBean);
            timeList.add(detailTimeBean2);
            timeList.add(detailTimeBean3);
        }
        List<WeekEnum> weekRangeList = accessTime.getWeekRangeList();
        String strWeekRange;
        if (weekRangeList == null || weekRangeList.isEmpty()) {
            strWeekRange = ServerConstants.DEFAULT_WORKING_DAYS;
        } else {
            strWeekRange = CommonUtils.getWorkingDay(weekRangeList);
        }
        PersonSyncInfo.DateRange dateRange = accessTime.getDateRange();
        if (dateRange == null) {
            dateRange = new PersonSyncInfo.DateRange();
            dateRange.setStartTime(ServerConstants.DEFAULT_SERVER_START_DATE);
            dateRange.setEndTime(ServerConstants.DEFAULT_SERVER_END_DATE);
        }
        permission.setPersonSerial(personSerial);
        permission.setStartDate(dateRange.getStartTime());
        permission.setEndDate(dateRange.getEndTime());
        permission.setWorkingDays(strWeekRange);
        permission.setTimeAndDesc(gson.toJson(timeList));
        return permission.save();
    }

    /**
     * 保存人脸数据
     */
    private boolean saveTablePersonFace(Long personId, String personSerial, String serverFaceId,
                                        String serverImageUrl, String serverFeatureUrl) {
        TablePersonFace tablePersonFace = PersonFaceDao.getInstance().getPersonFaceByFaceId(serverFaceId);
        if (tablePersonFace != null) {
            return true;
        }
        tablePersonFace = new TablePersonFace();
        tablePersonFace.faceId = serverFaceId;
        tablePersonFace.personId = personId;
        tablePersonFace.personSerial = personSerial;
        tablePersonFace.addTime = System.currentTimeMillis();
        tablePersonFace.updateTime = tablePersonFace.addTime;
        tablePersonFace.imageUrl = serverImageUrl;
        try {
            byte[] feature = ArcLinkEngine.getInstance().getFeatureData(serverFeatureUrl);
            if (feature != null) {
                tablePersonFace.feature = feature;
                tablePersonFace.featureVersion = Constants.FACE_FEATURE_VERSION_V30;
                String imagePath = SdcardUtils.getInstance().getRegisteredDirPath()
                        + File.separator + "" + personId
                        + File.separator + serverImageUrl.substring(serverImageUrl.lastIndexOf("/") + 1);
                Bitmap bitmap = getBitmap(serverImageUrl);
                if (bitmap != null) {
                    tablePersonFace.imagePath = imagePath;
                    if (ImageFileUtils.save(bitmap, imagePath, Bitmap.CompressFormat.JPEG)) {
                        return tablePersonFace.save();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 通知Ui刷新
     */
    private void refreshPersonListUi() {
        String topClassName = ActivityUtils.getTopActivity().getClass().getSimpleName();
        if (topClassName.equals(PersonListActivity.class.getSimpleName()) ||
                topClassName.equals(PersonDetailActivity.class.getSimpleName())) {
            EventBus.getDefault().post(new RefreshAdapterEvent());
        } else if (topClassName.equals(RecognizeActivity.class.getSimpleName())) {
            EventBus.getDefault().post(new ArcLinkUpdatePersonEvent(true));
        }
    }

    private Bitmap getBitmap(String url) {
        try {
            byte[] imgArray = FileDownloadManager.getInstance().downloadFile(url);
            return ImageFileUtils.getBitmap(imgArray);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
