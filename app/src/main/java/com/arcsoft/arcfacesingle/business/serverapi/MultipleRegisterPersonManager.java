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

package com.arcsoft.arcfacesingle.business.serverapi;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.app.BusinessErrorCode;
import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.business.common.CommonRepository;
import com.arcsoft.arcfacesingle.business.iccard.IcCardEntranceRepository;
import com.arcsoft.arcfacesingle.business.personlist.PersonListDataManager;
import com.arcsoft.arcfacesingle.data.db.dao.PersonDao;
import com.arcsoft.arcfacesingle.data.db.dao.PersonFaceDao;
import com.arcsoft.arcfacesingle.data.db.dao.PersonPermissionDao;
import com.arcsoft.arcfacesingle.data.db.table.TablePerson;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonFace;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonPermission;
import com.arcsoft.arcfacesingle.data.db.table.TableSettingConfigInfo;
import com.arcsoft.arcfacesingle.data.event.FullRefreshPersonEvent;
import com.arcsoft.arcfacesingle.data.event.PersonAddMultipleEvent;
import com.arcsoft.arcfacesingle.data.event.RefreshAdapterEvent;
import com.arcsoft.arcfacesingle.data.model.FaceExtractResult;
import com.arcsoft.arcfacesingle.libutil.ImageFileUtils;
import com.arcsoft.arcfacesingle.server.api.LocalHttpApiDataUtils;
import com.arcsoft.arcfacesingle.server.api.ServerConstants;
import com.arcsoft.arcfacesingle.server.faceengine.FaceEngineManager;
import com.arcsoft.arcfacesingle.server.pojo.request.RequestPersonAdd;
import com.arcsoft.arcfacesingle.server.pojo.request.RequestPersonAddFace;
import com.arcsoft.arcfacesingle.server.pojo.request.RequestPersonAddList;
import com.arcsoft.arcfacesingle.server.pojo.request.RequestPersonAndFace;
import com.arcsoft.arcfacesingle.server.pojo.response.ResponsePersonAddFace;
import com.arcsoft.arcfacesingle.server.pojo.response.ResponsePersonUpdate;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.arcfacesingle.util.scheduler.BaseObserver;
import com.arcsoft.arcfacesingle.util.scheduler.ExceptionHandler;
import com.arcsoft.arcfacesingle.view.activity.PersonDetailActivity;
import com.arcsoft.arcfacesingle.view.activity.PersonListActivity;
import com.arcsoft.arcfacesingle.view.activity.RecognizeActivity;
import com.arcsoft.asg.libcommon.util.business.rx.RxUtils;
import com.arcsoft.asg.libcommon.util.common.ActivityUtils;
import com.arcsoft.asg.libcommon.util.common.FileUtils;
import com.arcsoft.asg.libcommon.util.common.Md5Utils;
import com.arcsoft.faceengine.FaceInfo;
import com.google.gson.Gson;
import com.koushikdutta.async.http.Multimap;
import com.koushikdutta.async.http.body.MultipartFormDataBody;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class MultipleRegisterPersonManager implements IMultipleRegister {

    private static final String TAG = MultipleRegisterPersonManager.class.getSimpleName();
    private static volatile MultipleRegisterPersonManager instance;
    private static final int ADD_DELETE_PERSON_DIALOG_DISMISS_DELAY = 10 * 1000;
    public static final int NUMBER_FACE_THREAD_ONE = 1;
    public static final int TYPE_PERSON_ONLY_IC_CARD = 1;
    public static final int TYPE_PERSON_ONLY_PICTURE = 2;
    public static final int TYPE_PERSON_BOTH = 3;
    private static final String TAG_CONTENT = "content";
    private static final String TAG_CONTENT_LENGTH = "content-length";
    private static final int SIZE_HTTP_FILE_MAX = 100 * 1024 * 1024;
    private static final Object PROCESS_LOCK = new Object();

    private List<FaceEngineManager> faceEngineList;
    private Gson gson;
    private ConcurrentHashMap<String, RequestPersonAndFace> personAndFaceMap;
    private boolean processing;
    private Disposable releaseResourcesDisposable;

    private MultipleRegisterPersonManager() {
        if (faceEngineList == null) {
            faceEngineList = new ArrayList<>();
        }
        personAndFaceMap = new ConcurrentHashMap<>();
        gson = new Gson();
    }

    public static MultipleRegisterPersonManager getInstance() {
        if (instance == null) {
            synchronized (MultipleRegisterPersonManager.class) {
                if (instance == null) {
                    instance = new MultipleRegisterPersonManager();
                }
            }
        }
        return instance;
    }

    @Override
    public void registeringPerson(AsyncHttpServerRequest request, String uri, AsyncHttpServerResponse response) {
        try {
            if (FileUtils.getSdcardAvailableSize() <= Constants.SDCARD_STORAGE_SIZE_DELETE) {
                responseFailResult(response, false, ServerConstants.RESPONSE_CODE_PACKAGE_SAVE_FAILED,
                        ServerConstants.MSG_RESPONSE_DEVICE_SDCARD_STORAGE_LESS_MIN, null, null);
                return;
            }
            Multimap multimap = request.getHeaders().getMultiMap();
            String strFileLength = multimap.getString(TAG_CONTENT_LENGTH);
            long fileLength = Long.parseLong(strFileLength);
            if (fileLength > SIZE_HTTP_FILE_MAX) {
                responseFailResult(response, false, ServerConstants.RESPONSE_CODE_FAILED_BASE,
                        ServerConstants.MSG_RESPONSE_HTTP_FILE_MAX, null, null);
                return;
            }
            EventBus.getDefault().post(new PersonAddMultipleEvent(true));
            cancelReleaseSourcesDisposable();

            String stringHashCode = String.valueOf(request.hashCode());
            TableSettingConfigInfo configInfo = CommonRepository.getInstance().getSettingConfigInfo();
            String signKey = configInfo.getSignKey();
            RequestPersonAndFace personAndFace = new RequestPersonAndFace();
            personAndFace.setRequestHashCode(stringHashCode);
            HashMap<String, byte[]> imageMap = new HashMap<>();
            personAndFace.setFaceMap(imageMap);
            personAndFace.setNetSign(multimap.getString(Constants.HEADER_SIGN));
            personAndFaceMap.put(stringHashCode, personAndFace);

            setCallback(request, personAndFace, imageMap);
            request.setEndCallback(ex -> {
                if (!personAndFaceMap.containsKey(stringHashCode)) {
                    responseFailResult(response, true, ServerConstants.RESPONSE_CODE_JSON_INVALID,
                            ServerConstants.MSG_RESPONSE_JSON_INVALID, null, stringHashCode);
                    return;
                }
                RequestPersonAndFace personFace = personAndFaceMap.get(stringHashCode);
                if (personFace == null) {
                    responseFailResult(response, true, ServerConstants.RESPONSE_CODE_JSON_INVALID,
                            ServerConstants.MSG_RESPONSE_JSON_INVALID, null, stringHashCode);
                    return;
                }
                String contentJson = personFace.getContent();
                String localSign = Md5Utils.encode(contentJson + signKey);
                if (!localSign.equals(personFace.getNetSign())) {
                    responseFailResult(response, true, ServerConstants.RESPONSE_CODE_SIGN_CHECK_FAILED,
                            ServerConstants.MSG_RESPONSE_SIGN_FAILED, null, stringHashCode);
                    return;
                }
                try {
                    RequestPersonAddList personList = gson.fromJson(contentJson, RequestPersonAddList.class);
                    personFace.setPersonAdd(personList);
                    prepareDataAndFaceEngine(personFace, stringHashCode, response);
                } catch (Exception e) {
                    responseFailResult(response, true, ServerConstants.RESPONSE_CODE_JSON_INVALID,
                            ServerConstants.MSG_RESPONSE_JSON_INVALID, null, null);
                }
            });
        } catch (Exception e) {
            responseFailResult(response, true, ServerConstants.RESPONSE_CODE_FAILED_BASE,
                    ServerConstants.MSG_RESPONSE_FAILED, null, null);
        }
    }

    private void setCallback(AsyncHttpServerRequest request, RequestPersonAndFace personAndFace, HashMap<String, byte[]> imageMap) {
        MultipartFormDataBody formDataBody = (MultipartFormDataBody) request.getBody();
        formDataBody.setMultipartCallback(part -> {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            formDataBody.setDataCallback((emitter, bb) -> {
                try {
                    byte[] content = bb.getAllByteArray();
                    bos.write(content, 0, content.length);
                    bos.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        byte[] bosByte = bos.toByteArray();
                        String partName = part.getName();
                        if (!part.isFile()) {
                            String contentJson = new String(bosByte, StandardCharsets.UTF_8);
                            if (TAG_CONTENT.equals(partName)) {
                                personAndFace.setContent(contentJson);
                            }
                        } else {
                            imageMap.put(part.getName(), bosByte);
                        }
                        bos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                bb.recycle();
            });
        });
    }

    /**
     * 准备好需要处理的数据和创建人脸识别引擎
     */
    private void prepareDataAndFaceEngine(RequestPersonAndFace personAndFace, String personKey, AsyncHttpServerResponse response) {
        RequestPersonAddList personList = personAndFace.getPersonAdd();
        if (personList == null) {
            responseFailResult(response, true, ServerConstants.RESPONSE_CODE_JSON_INVALID,
                    ServerConstants.MSG_RESPONSE_JSON_INVALID, null, personKey);
            return;
        }
        List<RequestPersonAdd> personAddList = personList.getPersonAddList();
        if (personAddList == null || personAddList.isEmpty()) {
            responseFailResult(response, true, ServerConstants.RESPONSE_CODE_PARAM_PERSON_LIST_INVALID,
                    ServerConstants.MSG_RESPONSE_PARAM_FACE_LIST_INVALID, personList, personKey);
            return;
        }
        int faceEngineCount = MultipleRegisterPersonHelper.getFaceEngineCount(personList.getThreadCount(), personAddList.size());
        if (faceEngineList.isEmpty() || faceEngineCount != faceEngineList.size()) {
            unInitFaceEngine();
            long time = System.currentTimeMillis();
            faceEngineList.clear();
            faceEngineList.addAll(MultipleRegisterPersonHelper.initFaceEngine(faceEngineCount));
            long endTime = System.currentTimeMillis() - time;
        }
        List<List<RequestPersonAdd>> personAddListList;
        if (faceEngineCount <= NUMBER_FACE_THREAD_ONE) {
            personAddListList = new ArrayList<>();
            personAddListList.add(personAddList);
        } else {
            personAddListList = CommonUtils.averageAssign(personAddList, faceEngineCount);
        }
        concurrentRegisterPerson(personAddList.size(), personAndFace, personKey, personAddListList, response);
    }

    /**
     * 多线程并发注册人员
     */
    private void concurrentRegisterPerson(int oriPersonTotal, RequestPersonAndFace personAndFace, String personKey,
                                          List<List<RequestPersonAdd>> personListList, AsyncHttpServerResponse response) {
        List<ResponsePersonUpdate> personResultList = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        List<Observable<ResponsePersonUpdate>> observableList = new ArrayList<>();
        for (int i = 0; i < personListList.size(); i++) {
            List<RequestPersonAdd> personList = personListList.get(i);
            FaceEngineManager faceEngineManager = faceEngineList.get(i);
            Observable<ResponsePersonUpdate> observable = createRegisterObservable(personAndFace, personList, faceEngineManager);
            observableList.add(observable);
        }
        Observable.merge(observableList).subscribe(new BaseObserver<ResponsePersonUpdate>() {
            @Override
            public void onNext(ResponsePersonUpdate responsePersonUpdate) {
                personResultList.add(responsePersonUpdate);
                if (personResultList.size() == oriPersonTotal) {
                    long endTime = System.currentTimeMillis() - startTime;
                    responseSuccessResult(response, personAndFace.getPersonAdd(), personKey, personResultList);
                }
            }

            @Override
            public void onError(ExceptionHandler.ResponseThrowable throwable) {
                responseFailResult(response, true, ServerConstants.RESPONSE_CODE_FAILED_BASE,
                        ServerConstants.MSG_RESPONSE_FAILED, personAndFace.getPersonAdd(), personKey);
            }
        });
    }

    /**
     * 创建线程Observable
     */
    private Observable<ResponsePersonUpdate> createRegisterObservable(RequestPersonAndFace personAndFace,
                                                                      List<RequestPersonAdd> oriPersonList, FaceEngineManager faceEngineManager) {
        return Observable.create((ObservableOnSubscribe<ResponsePersonUpdate>) emitter -> {
            for (int i = 0; i < oriPersonList.size(); i++) {
                ResponsePersonUpdate result = new ResponsePersonUpdate();
                try {
                    long time = System.currentTimeMillis();
                    RequestPersonAdd oriPerson = oriPersonList.get(i);
                    int personInfoType = oriPerson.getPersonInfoType();
                    if (personInfoType != TYPE_PERSON_ONLY_IC_CARD && personInfoType != TYPE_PERSON_ONLY_PICTURE &&
                            personInfoType != TYPE_PERSON_BOTH) {
                        MultipleRegisterPersonHelper.registerResultEmitter(result, ServerConstants.MSG_RESPONSE_PERSON_TYPE_INVALID,
                                false, emitter);
                        continue;
                    }
                    String personSerial = oriPerson.getPersonSerial();
                    if (TextUtils.isEmpty(personSerial)) {
                        MultipleRegisterPersonHelper.registerResultEmitter(result,
                                ServerConstants.MSG_RESPONSE_PARAM_PERSON_SERIAL_EMPTY, false, emitter);
                        continue;
                    }
                    result.setPersonSerial(personSerial);
                    String personName = oriPerson.getPersonName();
                    if (!TextUtils.isEmpty(personName)) {
                        result.setPersonName(personName);
                    }
                    result.setEquipmentId(CommonRepository.getInstance().getSettingConfigInfo().getDeviceId());
                    if (personInfoType == TYPE_PERSON_ONLY_IC_CARD) {  //仅有ic卡
                        if (!MultipleRegisterPersonHelper.checkIcCardNo(oriPerson.getIcCardNo(), result, emitter)) {
                            continue;
                        }
                        processOnlyIcCard(oriPerson, result, emitter);
                    } else {    //有人脸图片
                        if (personInfoType == TYPE_PERSON_BOTH && !MultipleRegisterPersonHelper.checkIcCardNo(oriPerson.getIcCardNo(), result, emitter)) {
                            continue;
                        }
                        Map<String, byte[]> imageMap = personAndFace.getFaceMap();
                        processHasPicture(oriPerson, result, imageMap, faceEngineManager, emitter, time);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    MultipleRegisterPersonHelper.registerFailEmitter(result, PersonListDataManager.TYPE_REGISTER_FAILED_INFO_0, emitter);
                }
            }
        }).compose(RxUtils.ioToMain());
    }

    /**
     * 处理仅有ic卡的情况
     */
    private void processOnlyIcCard(RequestPersonAdd netPerson, ResponsePersonUpdate result, ObservableEmitter<ResponsePersonUpdate> emitter) {
        boolean exist = PersonDao.getInstance().existPerson(netPerson.getPersonSerial());
        if (exist) {
            TablePersonFace tablePersonFace = PersonFaceDao.getInstance().getPersonFaceBySerial(netPerson.getPersonSerial());
            if (tablePersonFace != null) {
                if (!PersonFaceDao.getInstance().deletePersonFace(tablePersonFace)) {
                    MultipleRegisterPersonHelper.registerResultEmitter(result, ServerConstants.MSG_RESPONSE_PERSON_UPDATE_FAILED,
                            false, emitter);
                    return;
                }
                FileUtils.deleteFile(tablePersonFace.imagePath);
            }
        } else {
            if (TextUtils.isEmpty(netPerson.getPersonName())) {
                MultipleRegisterPersonHelper.registerResultEmitter(result, ServerConstants.MSG_RESPONSE_PARAM_PERSON_NAME_EMPTY,
                        false, emitter);
                return;
            }
        }
        TablePerson localPerson = MultipleRegisterPersonHelper.createPerson(netPerson);
        TablePersonPermission personPermission = LocalHttpApiDataUtils.createNewPersonPermission(localPerson, new Gson());
        if (!exist && !PersonPermissionDao.getInstance().addModel(personPermission)) {
            MultipleRegisterPersonHelper.registerResultEmitter(result, ServerConstants.MSG_RESPONSE_PERSON_UPDATE_FAILED,
                    false, emitter);
            return;
        }
        if (exist ? PersonDao.getInstance().updatePerson(localPerson) : PersonDao.getInstance().addPerson(localPerson)) {
            MultipleRegisterPersonHelper.registerSuccessEmitter(result, emitter);
        } else {
            MultipleRegisterPersonHelper.registerResultEmitter(result, ServerConstants.MSG_RESPONSE_PERSON_UPDATE_FAILED,
                    false, emitter);
        }
    }

    /**
     * 处理有ic卡和人脸的情况
     */
    private void processHasPicture(RequestPersonAdd oriPerson, ResponsePersonUpdate result, Map<String, byte[]> imageMap,
                                   FaceEngineManager faceEngineManager, ObservableEmitter<ResponsePersonUpdate> emitter, long startTime) {
        List<RequestPersonAddFace> faceList = oriPerson.getFaceList();
        boolean exist = PersonDao.getInstance().existPerson(oriPerson.getPersonSerial());
        if (faceList == null || faceList.isEmpty()) {
            if (exist) {
                TablePerson faceInfo = MultipleRegisterPersonHelper.createPerson(oriPerson);
                if (!PersonDao.getInstance().updatePerson(faceInfo)) {
                    MultipleRegisterPersonHelper.registerFailEmitter(result, PersonListDataManager.TYPE_REGISTER_FAILED_INFO_7, emitter);
                    return;
                }
                MultipleRegisterPersonHelper.registerSuccessEmitter(result, emitter);
            } else {
                MultipleRegisterPersonHelper.registerResultEmitter(result, ServerConstants.MSG_RESPONSE_PARAM_FACE_LIST_EMPTY,
                        false, emitter);
            }
        } else {
            if (!exist && TextUtils.isEmpty(oriPerson.getPersonName())) {
                MultipleRegisterPersonHelper.registerResultEmitter(result, ServerConstants.MSG_RESPONSE_PARAM_PERSON_NAME_EMPTY,
                        false, emitter);
                return;
            }
            RequestPersonAddFace personFace = faceList.get(0);
            if (personFace == null) {
                MultipleRegisterPersonHelper.registerResultEmitter(result, ServerConstants.MSG_RESPONSE_PARAM_FACE_LIST_EMPTY,
                        false, emitter);
                return;
            }
            String faceId = personFace.getFaceId();
            if (TextUtils.isEmpty(faceId)) {
                MultipleRegisterPersonHelper.registerResultEmitter(result, ServerConstants.MSG_RESPONSE_PARAM_FACE_ID_INVALID,
                        false, emitter);
                return;
            }
            if (TextUtils.isEmpty(personFace.getImageName())) {
                MultipleRegisterPersonHelper.registerResultEmitter(result, ServerConstants.MSG_RESPONSE_PARAM_IMAGE_NAME_INVALID,
                        false, emitter);
                return;
            }
            Bitmap bitmap = MultipleRegisterPersonHelper.getBitmapFromByte(imageMap, faceId);
            if (bitmap == null) {
                MultipleRegisterPersonHelper.registerFailEmitter(result, PersonListDataManager.TYPE_REGISTER_FAILED_INFO_9, emitter);
                return;
            }
            processImageByEngine(faceEngineManager, oriPerson, result, bitmap, emitter, startTime);
        }
    }

    /**
     * 使用Engine处理人员照片
     */
    private void processImageByEngine(FaceEngineManager faceEngineManager, RequestPersonAdd oriPerson, ResponsePersonUpdate result, Bitmap bitmap,
                                      ObservableEmitter<ResponsePersonUpdate> emitter, long startTime) {
        FaceExtractResult faceResult = faceEngineManager.extract(bitmap);
        int resultCode = faceResult.getResult();
        if (resultCode  == BusinessErrorCode.BEC_FACE_MANAGER_NO_FACE) {
            MultipleRegisterPersonHelper.registerResultEmitter(result,
                    CommonUtils.getStrFromRes(R.string.face_manager_tip_detect_no_face_image), false, emitter);
            return;
        }
        if (resultCode == BusinessErrorCode.BEC_FACE_MANAGER_MORE_THAN_ONE_FACE) {
            MultipleRegisterPersonHelper.registerFailEmitter(result, PersonListDataManager.TYPE_REGISTER_FAILED_INFO_3, emitter);
            return;
        }
        if (resultCode == BusinessErrorCode.BEC_FACE_MANAGER_FACE_QUALITY_FAIL) {
            MultipleRegisterPersonHelper.registerFailEmitter(result, PersonListDataManager.TYPE_REGISTER_FAILED_INFO_5, emitter);
            return;
        }
        if (resultCode == BusinessErrorCode.BEC_FACE_MANAGER_RECOGNIZE_FAIL) {
            MultipleRegisterPersonHelper.registerFailEmitter(result, PersonListDataManager.TYPE_REGISTER_FAILED_INFO_6, emitter);
            return;
        }
        savePersonToLocal(oriPerson, result, bitmap, faceResult.getFaceInfo(), emitter, startTime);
    }

    /**
     * 保存人员至本地
     */
    private void savePersonToLocal(RequestPersonAdd oriPerson, ResponsePersonUpdate result, Bitmap bitmap,
                                   FaceInfo faceInfo, ObservableEmitter<ResponsePersonUpdate> emitter, long startTime) {
        if (FileUtils.getSdcardAvailableSize() < Constants.SDCARD_STORAGE_SIZE_DELETE) {
            MultipleRegisterPersonHelper.registerFailEmitter(result, PersonListDataManager.TYPE_REGISTER_FAILED_INFO_8, emitter);
            return;
        }
        TablePerson tablePerson = MultipleRegisterPersonHelper.createPerson(oriPerson);
        TablePersonFace personFace = PersonFaceDao.getInstance().getPersonFaceBySerial(tablePerson.personSerial);
        String oldImagePath = null;
        if (personFace != null) {
            oldImagePath = personFace.imagePath;
        }
        RequestPersonAddFace face = oriPerson.getFaceList().get(0);
        String imageName = face.getImageName();
        personFace = MultipleRegisterPersonHelper.createPersonFace(tablePerson, personFace, imageName, faceInfo.feature);
        Bitmap newSaveBmp = ImageFileUtils.getFaceRegisterCropBitmap(faceInfo.faceRect, faceInfo.faceOrient, bitmap);
        boolean flag = ImageFileUtils.save(newSaveBmp, personFace.imagePath, Bitmap.CompressFormat.JPEG);
        if (!bitmap.isRecycled()) {
            bitmap.recycle();
        }
        if (newSaveBmp != null && !newSaveBmp.isRecycled()) {
            newSaveBmp.recycle();
        }
        if (!flag) {
            MultipleRegisterPersonHelper.registerFailEmitter(result, PersonListDataManager.TYPE_REGISTER_FAILED_INFO_7, emitter);
            return;
        }
        boolean exist = PersonDao.getInstance().existPerson(oriPerson.getPersonSerial());
        TablePersonPermission personPermission = LocalHttpApiDataUtils.createNewPersonPermission(tablePerson, new Gson());
        if (!exist && !PersonPermissionDao.getInstance().addModel(personPermission)) {
            MultipleRegisterPersonHelper.registerFailEmitter(result, PersonListDataManager.TYPE_REGISTER_FAILED_INFO_7, emitter);
            return;
        }
        if (exist ? !PersonDao.getInstance().updatePerson(tablePerson) : !PersonDao.getInstance().addPerson(tablePerson)) {
            MultipleRegisterPersonHelper.registerFailEmitter(result, PersonListDataManager.TYPE_REGISTER_FAILED_INFO_7, emitter);
            return;
        }
        boolean existFace = PersonFaceDao.getInstance().existFace(oriPerson.getPersonSerial());
        if (existFace ? !PersonFaceDao.getInstance().updatePersonFace(personFace) : !PersonFaceDao.getInstance().addPersonFace(personFace)) {
            MultipleRegisterPersonHelper.registerFailEmitter(result, PersonListDataManager.TYPE_REGISTER_FAILED_INFO_7, emitter);
            return;
        }
        long endTime = System.currentTimeMillis() - startTime;
        if (!personFace.imagePath.equals(oldImagePath)) {
            FileUtils.delete(oldImagePath);
        }
        List<ResponsePersonAddFace> faceResults = MultipleRegisterPersonHelper.createResponsePersonFaces(true,
                face.getFaceId(), faceInfo.faceOrient, ServerConstants.MSG_RESPONSE_PERSON_UPDATE_SUCCESS);
        result.setFaceResults(faceResults);
        emitter.onNext(result);
    }

    /**
     * 反馈人员注册失败结果
     */
    private void responseFailResult(AsyncHttpServerResponse response, boolean sendEvent, int code, String msg,
                                    RequestPersonAddList personList, String requestKey) {
        createReleaseSourcesDisposable(sendEvent);
        response.send(LocalHttpApiDataUtils.getResponseStringFail(code, msg));
        releaseEngineAndResources(personList, requestKey);
    }

    /**
     * 反馈人员注册成功结果
     */
    private void responseSuccessResult(AsyncHttpServerResponse response, RequestPersonAddList personList, String requestKey,
                                       Object data) {
        createReleaseSourcesDisposable(true);
        response.send(LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_SUCCESS,
                ServerConstants.MSG_RESPONSE_REQUEST_SUCCESS, data));
        releaseEngineAndResources(personList, requestKey);

        String topClassName = ActivityUtils.getTopActivity().getClass().getSimpleName();
        if (topClassName.equals(PersonListActivity.class.getSimpleName()) || topClassName.equals(PersonDetailActivity.class.getSimpleName())) {
            EventBus.getDefault().post(new RefreshAdapterEvent());
        } else if (topClassName.equals(RecognizeActivity.class.getSimpleName())) {
            EventBus.getDefault().post(new FullRefreshPersonEvent(true));
        }
        IcCardEntranceRepository.getInstance().reloadPersonList();
    }

    /**
     * 释放引擎和资源
     */
    private void releaseEngineAndResources(RequestPersonAddList personList, String requestKey) {
        if (personList == null || personList.isRegisterComplete()) {
            synchronized (PROCESS_LOCK) {
                processing = true;
            }
        }
        if (!TextUtils.isEmpty(requestKey)) {
            personAndFaceMap.remove(requestKey);
        }
        if (personAndFaceMap.size() == 0 && processing) {
            unInitFaceEngine();
        }
    }

    /**
     * 延时释放引擎和资源
     */
    private void createReleaseSourcesDisposable(boolean sendEvent) {
        cancelReleaseSourcesDisposable();
        releaseResourcesDisposable = Observable.timer(ADD_DELETE_PERSON_DIALOG_DISMISS_DELAY, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    if (sendEvent) {
                        EventBus.getDefault().post(new PersonAddMultipleEvent(false));
                    }
                    releaseEngineAndResources(null, null);
                });
    }

    private void cancelReleaseSourcesDisposable() {
        if (releaseResourcesDisposable != null && !releaseResourcesDisposable.isDisposed()) {
            releaseResourcesDisposable.dispose();
        }
    }

    /**
     * 释放人脸检测引擎
     */
    private void unInitFaceEngine() {
        synchronized (PROCESS_LOCK) {
            processing = false;
        }
        for (FaceEngineManager faceEngineManager : faceEngineList) {
            faceEngineManager.unInitFaceEngine();
        }
        faceEngineList.clear();
    }
}
