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

package com.arcsoft.arcfacesingle.business.common;

import android.text.TextUtils;
import android.util.Pair;

import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.data.db.dao.IdentifyRecordDao;
import com.arcsoft.arcfacesingle.data.db.table.TableSettingConfigInfo;
import com.arcsoft.arcfacesingle.data.db.table.TableSignRecord;
import com.arcsoft.arcfacesingle.data.model.http.ReqSignRecordImage;
import com.arcsoft.arcfacesingle.data.model.http.ReqSignRecordImageList;
import com.arcsoft.arcfacesingle.data.model.http.ReqSignRecordList;
import com.arcsoft.arcfacesingle.libutil.ImageFileUtils;
import com.arcsoft.arcfacesingle.service.ServerServiceDataManager;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.arcfacesingle.util.scheduler.BaseObserver;
import com.arcsoft.arcfacesingle.util.scheduler.ExceptionHandler;
import com.arcsoft.arcsoftlink.http.bean.req.RecognizeRecord;
import com.arcsoft.arcsoftlink.http.bean.res.UploadRecordResponse;
import com.arcsoft.arcsoftlink.mqtt.ArcLinkEngine;
import com.arcsoft.asg.libcommon.util.business.rx.RxUtils;
import com.arcsoft.asg.libcommon.util.common.FileUtils;
import com.arcsoft.asg.libcommon.util.common.NetworkUtils;
import com.arcsoft.asg.libnetwork.bean.RemoteResponseBase;
import com.arcsoft.asg.libnetwork.manage.DefaultRemoteApiManager;
import com.google.gson.Gson;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class FaceRecordDataManager {

    private static final String TAG = FaceRecordDataManager.class.getSimpleName();
    private static volatile FaceRecordDataManager INSTANCE;
    private static final String STRING_UPLOAD_FAILED = "STRING_UPLOAD_FAILED";
    private static final String STRING_UPDATE_FAILED = "STRING_UPDATE_FAILED";
    private static final int PAGE_SIZE_RECORD_TEXT = 50;
    private static final int PAGE_SIZE_RECORD_IMAGE = 10;
    private static final int PAGE_SIZE_AIOT_RECORD = 10;
    public static final int TYPE_RECORD_EXIST_IMAGE = 1;
    public static final int TYPE_RECORD_NOT_EXIST_IMAGE = 2;
    /**
     * 管理端无此人信息
     */
    public static final int SIGN_RECORD_STATUS_SERVER_PERSON_INVALID = -1;
    /**
     * 操作成功
     */
    public static final int SIGN_RECORD_STATUS_SUCCESS = 1;

    /**
     * 任务是否已开始
     */
    private boolean processingTask;
    private Disposable uploadRecordDisposable;
    private Subscription uploadImageSub;
    private Subscription aIotUploadRecordSub;

    private FaceRecordDataManager() {
    }

    public static FaceRecordDataManager getInstance() {
        if (INSTANCE == null) {
            synchronized (FaceRecordDataManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new FaceRecordDataManager();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 开始上传任务
     */
    public void startUploadTask() {
        if (processingTask) {
            return;
        }
        processingTask = true;
        if (CommonUtils.isOfflineLanAppMode()) {
            if (DefaultRemoteApiManager.getInstance().isInitSuccess() && NetworkUtils.isConnected()) {
                uploadRecordTextToPcServer();
            } else {
                processingTask = false;
            }
        }
        if (CommonUtils.isCloudAiotAppMode()) {
            if (CommonRepository.getInstance().getDeviceAccessStatus()) {
                uploadAIotRecord();
            } else {
                processingTask = false;
            }
        }
    }

    /**
     * 上传刷脸记录文本至PC服务器
     */
    private void uploadRecordTextToPcServer() {
        List<TableSignRecord> dbList = IdentifyRecordDao.getInstance().getListStatusIsDefault(PAGE_SIZE_RECORD_TEXT);
        if (dbList.size() == 0) {
            //上传记录图片
            uploadRecordsImage();
        } else {
            //上传记录文字
            TableSettingConfigInfo configInfo = CommonRepository.getInstance().getSettingConfigInfo();
            boolean uploadImage = CommonRepository.getInstance().uploadRecordImage(configInfo);
            ReqSignRecordList reqSignRecordList = ServerServiceDataManager
                    .getInstance()
                    .getReqSignRecordList(dbList, configInfo, uploadImage);
            String recordJson = new Gson().toJson(reqSignRecordList);
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), recordJson);
            uploadRecordDisposable = DefaultRemoteApiManager.getInstance()
                    .uploadRecordText(recordJson)
                    .compose(RxUtils.ioToMain())
                    .subscribeWith(new DisposableObserver<RemoteResponseBase<HashMap<String, Integer>>>() {

                        @Override
                        public void onNext(RemoteResponseBase<HashMap<String, Integer>> responseResult) {
                            if (responseResult.getCode() == Constants.HTTP_REQUEST_CODE_SUCCESS) {
                                if (uploadImage) {
                                    //先修改本地记录，再上传图片
                                    updateRecords(dbList, responseResult);
                                } else {
                                    //删除本地记录
                                    deleteRecords(dbList, responseResult);
                                }
                            } else {
                                processingTask = false;
                            }
                        }

                        @Override
                        public void onComplete() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            processingTask = false;
                        }
                    });
        }
    }

    /**
     * 更新刷脸记录
     */
    private void updateRecords(List<TableSignRecord> records, RemoteResponseBase<HashMap<String, Integer>> responseResult) {
        Disposable disposable = Observable.create((ObservableEmitter<Boolean> emitter) -> {
            HashMap<String, Integer> resultMap = responseResult.getData();
            if (resultMap != null && resultMap.size() > 0) {
                List<TableSignRecord> updateRecordList = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : resultMap.entrySet()) {
                    int status = entry.getValue();
                    if (status == SIGN_RECORD_STATUS_SUCCESS) {
                        String recordId = entry.getKey();
                        TableSignRecord signRecord = getRecordById(records, recordId);
                        if (signRecord == null) {
                            long time = Long.parseLong(entry.getKey());
                            signRecord = getRecordByTime(records, time);
                        }
                        if (signRecord != null) {
                            if (signRecord.status == IdentifyRecordDao.STATUS_IC_CARD) {
                                //如果是IC卡刷卡记录，则直接删除该条记录
                                IdentifyRecordDao.getInstance().deleteModelTransactionAsync(signRecord);
                            } else {
                                signRecord.status = IdentifyRecordDao.STATUS_TEXT;
                                updateRecordList.add(signRecord);
                            }
                        }
                    }
                }
                IdentifyRecordDao.getInstance().updateListAsync(updateRecordList,
                        new IdentifyRecordDao.OnIdentifyListener() {
                            @Override
                            public void onSuccess() {
                                emitter.onNext(true);
                                emitter.onComplete();
                            }

                            @Override
                            public void onError() {
                                emitter.onError(new Throwable(STRING_UPDATE_FAILED));
                            }
                        });
            }
        }).compose(RxUtils.ioToMain())
                .subscribeWith(new DisposableObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean success) {
                        if (success) {
                            uploadRecordsImage();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        processingTask = false;
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    /**
     * 背压上传刷脸记录图片
     */
    private void uploadRecordsImage() {
        List<TableSignRecord> dbList = IdentifyRecordDao.getInstance().getListStatusIsText(PAGE_SIZE_RECORD_IMAGE);
        if (dbList != null && dbList.size() > 0) {
            Flowable.create((FlowableEmitter<TableSignRecord> e) -> {
                for (TableSignRecord record : dbList) {
                    e.onNext(record);
                }
                e.onComplete();
            }, BackpressureStrategy.BUFFER)
                    .subscribe(new Subscriber<TableSignRecord>() {
                        @Override
                        public void onSubscribe(Subscription s) {
                            uploadImageSub = s;
                            uploadImageSub.request(1);
                        }

                        @Override
                        public void onNext(TableSignRecord tableSignRecord) {
                            uploadRecordImageToPcServer(tableSignRecord);
                        }

                        @Override
                        public void onError(Throwable t) {
                            processingTask = false;
                        }

                        @Override
                        public void onComplete() {
                            processingTask = false;
                        }
                    });
        } else {
            processingTask = false;
        }
    }

    /**
     * 上传刷脸图片数据至PC端服务器
     */
    private void uploadRecordImageToPcServer(TableSignRecord tableSignRecord) {
        Disposable disposable = Observable.create((ObservableEmitter<ReqSignRecordImageList> emitter) -> {
            ReqSignRecordImageList signRecordImageList = new ReqSignRecordImageList();
            String imagePath = tableSignRecord.imagePath;
            if (!TextUtils.isEmpty(imagePath)) {
                ReqSignRecordImage image = getReqSignRecordImage(imagePath, tableSignRecord);
                File file = new File(imagePath);
                if (file.exists()) {
                    String imageBase64 = ImageFileUtils.image2Base64(imagePath);
                    image.setImageBase64(imageBase64);
                    signRecordImageList.addRequestSignRecord(image);
                    emitter.onNext(signRecordImageList);
                    emitter.onComplete();
                } else {
                    IdentifyRecordDao.getInstance().deleteModelTransactionAsync(tableSignRecord,
                            new IdentifyRecordDao.OnIdentifyListener() {
                                @Override
                                public void onSuccess() {
                                    emitter.onError(new Throwable());
                                }

                                @Override
                                public void onError() {
                                    emitter.onError(new Throwable());
                                }
                            });
                }
            } else {
                IdentifyRecordDao.getInstance().deleteModelTransactionAsync(tableSignRecord,
                        new IdentifyRecordDao.OnIdentifyListener() {
                            @Override
                            public void onSuccess() {
                                emitter.onError(new Throwable());
                            }

                            @Override
                            public void onError() {
                                emitter.onError(new Throwable());
                            }
                        });
            }
        }).concatMap(imageList -> {
            String imageListJson = new Gson().toJson(imageList);
            return DefaultRemoteApiManager.getInstance().uploadRecordImage(imageListJson);
        })
                .compose(RxUtils.ioToMain())
                .subscribeWith(new BaseObserver<RemoteResponseBase<HashMap<String, Integer>>>() {
                    @Override
                    public void onNext(RemoteResponseBase<HashMap<String, Integer>> responseResult) {
                        if (responseResult.getCode() == Constants.HTTP_REQUEST_CODE_SUCCESS) {
                            deleteRecordImage(tableSignRecord, responseResult);
                        } else {
                            uploadImageSub.request(1);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        uploadImageSub.request(1);
                    }
                });
    }

    /**
     * 删除刷脸记录
     */
    private void deleteRecordImage(TableSignRecord record, RemoteResponseBase<HashMap<String, Integer>> responseResult) {
        HashMap<String, Integer> resultMap = responseResult.getData();
        if (resultMap != null && resultMap.size() > 0) {
            List<TableSignRecord> deleteRecordList = new ArrayList<>();
            List<TableSignRecord> updateRecordList = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : resultMap.entrySet()) {
                int status = entry.getValue();
                if (status == SIGN_RECORD_STATUS_SUCCESS) {
                    deleteRecordList.add(record);
                } else if (status == SIGN_RECORD_STATUS_SERVER_PERSON_INVALID) {
                    record.status = IdentifyRecordDao.STATUS_IC_SERVER_DATA_INVALID;
                    updateRecordList.add(record);
                }
            }
            IdentifyRecordDao.getInstance().updateAndDeleteListAsync(updateRecordList, deleteRecordList,
                    new IdentifyRecordDao.OnIdentifyListener() {
                        @Override
                        public void onSuccess() {
                            if (deleteRecordList.size() > 0) {
                                boolean success = FileUtils.deleteFile(record.imagePath);
                            } else {
                            }
                            uploadImageSub.request(1);
                        }

                        @Override
                        public void onError() {
                            uploadImageSub.request(1);
                        }
                    });
        } else {
            uploadImageSub.request(1);
        }
    }

    /**
     * 删除多条刷脸记录
     */
    private void deleteRecords(List<TableSignRecord> records, RemoteResponseBase<HashMap<String, Integer>> responseResult) {
        HashMap<String, Integer> resultMap = responseResult.getData();
        if (resultMap != null && resultMap.size() > 0) {
            List<TableSignRecord> deleteRecordsList = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : resultMap.entrySet()) {
                int status = entry.getValue();
                if (status == SIGN_RECORD_STATUS_SUCCESS) {
                    String recordId = entry.getKey();
                    TableSignRecord tableSignRecord = getRecordById(records, recordId);
                    if (null != tableSignRecord) {
                        deleteRecordsList.add(tableSignRecord);
                    } else {
                        long time = Long.parseLong(entry.getKey());
                        TableSignRecord signRecord = getRecordByTime(records, time);
                        if (null != signRecord) {
                            deleteRecordsList.add(signRecord);
                        }
                    }
                }
            }
            IdentifyRecordDao.getInstance().deleteListTransactionAsync(deleteRecordsList,
                    new IdentifyRecordDao.OnIdentifyListener() {
                        @Override
                        public void onSuccess() {
                            deleteImagesIo(deleteRecordsList);
                        }

                        @Override
                        public void onError() {
                            processingTask = false;
                        }
                    });
        } else {
            processingTask = false;
        }
    }

    /**
     * 在线程中删除图片
     */
    private void deleteImagesIo(List<TableSignRecord> deleteRecordsList) {
        Disposable disposable = Observable.create(emitter -> {
            for (TableSignRecord signRecord : deleteRecordsList) {
                String imagePath = signRecord.imagePath;
                FileUtils.deleteFile(imagePath);
            }
        }).subscribeWith(new BaseObserver<Object>() {
            @Override
            public void onNext(Object o) {
                processingTask = false;
            }

            @Override
            public void onError(ExceptionHandler.ResponseThrowable throwable) {
                processingTask = false;
            }
        });
    }

    /**
     * 上传刷脸记录至AIot
     */
    private void uploadAIotRecord() {
        List<TableSignRecord> dbList = IdentifyRecordDao.getInstance().getListByDefaultStatus(PAGE_SIZE_AIOT_RECORD);
        if (dbList != null && dbList.size() > 0) {
            Flowable.create((FlowableEmitter<TableSignRecord> e) -> {
                for (TableSignRecord record : dbList) {
                    e.onNext(record);
                }
                e.onComplete();
            }, BackpressureStrategy.BUFFER)
                    .subscribe(new Subscriber<TableSignRecord>() {
                        @Override
                        public void onSubscribe(Subscription s) {
                            aIotUploadRecordSub = s;
                            aIotUploadRecordSub.request(1);
                        }

                        @Override
                        public void onNext(TableSignRecord signRecord) {
                            uploadImageToAIotServer(signRecord);
                        }

                        @Override
                        public void onError(Throwable t) {
                            processingTask = false;
                        }

                        @Override
                        public void onComplete() {
                            processingTask = false;
                        }
                    });
        } else {
            processingTask = false;
        }
    }

    /**
     * 上传刷脸数据至服务器
     */
    private void uploadImageToAIotServer(TableSignRecord signRecord) {
        Disposable disposable = Observable.create((ObservableEmitter<Pair<UploadRecordResponse, TableSignRecord>> emitter) -> {
            String imagePath = signRecord.imagePath;
            RecognizeRecord recordBean = new RecognizeRecord(
                    signRecord.personId,
                    signRecord.faceId,
                    TextUtils.isEmpty(imagePath) ? "" : ImageFileUtils.image2Base64(imagePath),
                    signRecord.addTime,
                    signRecord.recordId,
                    signRecord.type);
            UploadRecordResponse baseResponse = ArcLinkEngine.getInstance().uploadSignRecord(recordBean);
            if (baseResponse == null) {
                baseResponse = new UploadRecordResponse();
                baseResponse.setCode(UploadRecordResponse.CODE_UNKNOWN_ERROR);
                emitter.onNext(new Pair<>(baseResponse, signRecord));
            } else {
                emitter.onNext(new Pair<>(baseResponse, signRecord));
            }
            emitter.onComplete();
        }).compose(RxUtils.ioToMain())
                .subscribeWith(new DisposableObserver<Pair<UploadRecordResponse, TableSignRecord>>() {

                    @Override
                    public void onNext(Pair<UploadRecordResponse, TableSignRecord> pair) {
                        UploadRecordResponse baseResponse = pair.first;
                        if (baseResponse.success()) {
                            TableSignRecord signRecord = pair.second;
                            String imagePath = signRecord.imagePath;
                            if (!TextUtils.isEmpty(imagePath)) {
                                if (FileUtils.delete(imagePath)) {
                                    deleteRecordAIot(signRecord);
                                }
                            } else {
                                deleteRecordAIot(signRecord);
                            }
                        }
                    }

                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        aIotUploadRecordSub.request(1);
                    }
                });
    }

    private void deleteRecordAIot(TableSignRecord signRecord) {
        IdentifyRecordDao.getInstance().deleteModelTransactionAsync(signRecord, new IdentifyRecordDao.OnIdentifyListener() {
            @Override
            public void onSuccess() {
                aIotUploadRecordSub.request(1);
            }

            @Override
            public void onError() {
                aIotUploadRecordSub.request(1);
            }
        });
    }

    public void stopUploadTask() {
        processingTask = false;
        if (uploadRecordDisposable != null && !uploadRecordDisposable.isDisposed()) {
            uploadRecordDisposable.dispose();
        }
        uploadRecordDisposable = null;
        if (uploadImageSub != null) {
            uploadImageSub.cancel();
        }
        uploadImageSub = null;
        if (aIotUploadRecordSub != null) {
            aIotUploadRecordSub.cancel();
        }
        aIotUploadRecordSub = null;
    }

    private TableSignRecord getRecordById(List<TableSignRecord> records, String oriId) {
        TableSignRecord descRecord = null;
        for (TableSignRecord signRecord : records) {
            String descId = signRecord.recordId;
            if (descId.equals(oriId)) {
                descRecord = signRecord;
                break;
            }
        }
        return descRecord;
    }

    private TableSignRecord getRecordByTime(List<TableSignRecord> records, long oriTime) {
        TableSignRecord descRecord = null;
        for (TableSignRecord signRecord : records) {
            long descTime = signRecord.addTime;
            if (descTime == oriTime) {
                descRecord = signRecord;
                break;
            }
        }
        return descRecord;
    }

    private ReqSignRecordImage getReqSignRecordImage(String imagePath, TableSignRecord tableSignRecord) {
        ReqSignRecordImage image = new ReqSignRecordImage();
        String imageName;
        try {
            imageName = imagePath.substring(imagePath.lastIndexOf("/") + 1, imagePath.lastIndexOf("."));
        } catch (StringIndexOutOfBoundsException e) {
            imageName = "";
        }
        image.setImageName(imageName);
        TableSettingConfigInfo settingConfigInfo = CommonRepository.getInstance().getSettingConfigInfo();
        int deviceId = settingConfigInfo.getDeviceId();
        image.setEquipmentId(deviceId);
        //以下EquipmentVerificationId修改让V2.0.0版本兼容老版本数据
        String recordId = tableSignRecord.recordId;
        if (TextUtils.isEmpty(recordId)) {
            image.setEquipmentVerificationId(String.valueOf(tableSignRecord.addTime));
        } else {
            image.setEquipmentVerificationId(recordId);
        }
        return image;
    }
}
