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

package com.arcsoft.arcfacesingle.business.selectmode;

import android.text.TextUtils;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.data.db.dao.IdentifyRecordDao;
import com.arcsoft.arcfacesingle.data.db.dao.PersonDao;
import com.arcsoft.arcfacesingle.data.db.dao.PersonFaceDao;
import com.arcsoft.arcfacesingle.data.db.dao.PersonPermissionDao;
import com.arcsoft.arcfacesingle.data.db.table.TablePerson;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonFace;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.arcfacesingle.util.SdcardUtils;
import com.arcsoft.asg.libcommon.util.business.rx.RxUtils;
import com.arcsoft.asg.libcommon.util.common.FileUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.disposables.Disposable;

public class SelectModeRepository {

    private static final String TAG = SelectModeRepository.class.getSimpleName();
    private Disposable selectOfflineLanDisposable;
    private Disposable selectCloudAIotDisposable;

    public String getTitle(int newMode) {
        return newMode == Constants.APP_MODE_OFFLINE_LAN ? CommonUtils.getStrFromRes(R.string.offline_lan_mode) :
                CommonUtils.getStrFromRes(R.string.cloud_ai_ot_mode);
    }

    public String getDialogWarn(int newMode) {
        if (newMode == Constants.APP_MODE_OFFLINE_LAN) {
            return CommonUtils.getStrFromRes(R.string.dialog_select_mode_warn2);
        }
        if (newMode == Constants.APP_MODE_CLOUD_AI_OT) {
            return CommonUtils.getStrFromRes(R.string.dialog_select_mode_warn1);
        }
        return "";
    }

    /**
     * 切换至离线局域网模式，需要清除刷脸记录数据，并且多余的人脸需要删除，保证一个人只有一个人脸
     */
    public void cleanDataToOfflineLanMode(ISelectMode.SelectModeCallback callback) {
        selectOfflineLanDisposable = Observable.create((ObservableEmitter<Boolean> emitter) -> {
            long count = PersonDao.getInstance().getTotalCount();
            if (count > 0) {
                List<TablePerson> personList = PersonDao.getInstance().queryAllPerson();
                List<TablePersonFace> needsDeleteFaceList = new ArrayList<>();
                for (TablePerson person : personList) {
                    String personSerial = person.personSerial;
                    String mainFaceId = person.mainFaceId;
                    List<TablePersonFace> faceList = PersonFaceDao.getInstance().getListByPersonSerial(personSerial);
                    int faceSize = faceList.size();
                    if (TextUtils.isEmpty(mainFaceId)) {
                        if (faceSize > 1) {
                            for (int i = 0; i < faceSize; i++) {
                                if (i > 0) {
                                    needsDeleteFaceList.add(faceList.get(i));
                                }
                            }
                        }
                    } else {
                        for (TablePersonFace face : faceList) {
                            if (!mainFaceId.equals(face.faceId)) {
                                needsDeleteFaceList.add(face);
                            }
                        }
                    }
                }
                if (needsDeleteFaceList.size() > 0) {
                    PersonFaceDao.getInstance().deleteListAsync(needsDeleteFaceList, new PersonFaceDao.OnDeleteListener() {
                        @Override
                        public void onSuccess() {
                            for (TablePersonFace face : needsDeleteFaceList) {
                                FileUtils.delete(face.imagePath);
                            }
                            cleanDataOnNext(emitter);
                        }

                        @Override
                        public void onError() {
                            emitter.onError(new Throwable());
                        }
                    });
                } else {
                    cleanDataOnNext(emitter);
                }
            } else {
                cleanDataOnNext(emitter);
            }
        })
                .compose(RxUtils.ioToMain())
                .subscribe(flag -> callback.onCleanDataProgress(100, 100),
                        throwable -> {
                            callback.onCleanDataFail();
                        });
    }

    private void cleanDataOnNext(ObservableEmitter<Boolean> emitter) {
        IdentifyRecordDao.getInstance().deleteTable();
        String filePath = SdcardUtils.getInstance().getSignRecordDirPath();
        FileUtils.deleteDir(filePath);
        emitter.onNext(true);
        emitter.onComplete();
    }

    /**
     * 切换至云端AIoT模式：清除人员数据、刷脸记录数据、权限数据
     */
    public void cleanDataToAiCloudMode(ISelectMode.SelectModeCallback callback) {
        selectCloudAIotDisposable = Observable.create((ObservableEmitter<Boolean> emitter) -> {
            String filePath1 = SdcardUtils.getInstance().getSignRecordDirPath();
            String filePath2 = SdcardUtils.getInstance().getRegisteredDirPath();
            FileUtils.deleteDir(filePath1);
            FileUtils.deleteDir(filePath2);
            PersonDao.getInstance().deleteTable();
            PersonPermissionDao.getInstance().deleteTable();
            PersonFaceDao.getInstance().deleteTable();
            IdentifyRecordDao.getInstance().deleteTable();
            emitter.onNext(true);
            emitter.onComplete();
        })
                .compose(RxUtils.ioToMain())
                .subscribe(flag -> callback.onCleanDataProgress(100, 100),
                        throwable -> {
                        });
    }

    public void release() {
        if (selectOfflineLanDisposable != null && !selectOfflineLanDisposable.isDisposed()) {
            selectOfflineLanDisposable.dispose();
            selectOfflineLanDisposable = null;
        }
        if (selectCloudAIotDisposable != null && !selectCloudAIotDisposable.isDisposed()) {
            selectCloudAIotDisposable.dispose();
            selectCloudAIotDisposable = null;
        }
    }
}
