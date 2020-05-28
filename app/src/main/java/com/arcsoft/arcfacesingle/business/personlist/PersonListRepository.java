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

import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.app.BusinessErrorCode;
import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.data.db.DBManager;
import com.arcsoft.arcfacesingle.data.db.dao.PersonDao;
import com.arcsoft.arcfacesingle.data.db.dao.PersonFaceDao;
import com.arcsoft.arcfacesingle.data.db.dao.PersonPermissionDao;
import com.arcsoft.arcfacesingle.data.db.helper.ExternalDatabaseHelper;
import com.arcsoft.arcfacesingle.data.db.table.TableArcFaceVersion;
import com.arcsoft.arcfacesingle.data.db.table.TablePerson;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonFace;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonPermission;
import com.arcsoft.arcfacesingle.data.model.FaceExtractResult;
import com.arcsoft.arcfacesingle.data.model.PersonInfo;
import com.arcsoft.arcfacesingle.libutil.ImageFileUtils;
import com.arcsoft.arcfacesingle.server.api.LocalHttpApiDataUtils;
import com.arcsoft.arcfacesingle.server.faceengine.FaceEngineManager;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.arcfacesingle.util.SdcardUtils;
import com.arcsoft.arcfacesingle.util.business.UsbHelper;
import com.arcsoft.arcfacesingle.util.scheduler.BaseObserver;
import com.arcsoft.arcfacesingle.util.scheduler.ExceptionHandler;
import com.arcsoft.arcfacesingle.view.activity.PersonListActivity;
import com.arcsoft.asg.libcommon.util.business.rx.RxUtils;
import com.arcsoft.asg.libcommon.util.common.ActivityUtils;
import com.arcsoft.asg.libcommon.util.common.FileUtils;
import com.arcsoft.asg.libcommon.util.common.KeyboardUtils;
import com.arcsoft.asg.libcommon.util.common.Md5Utils;
import com.arcsoft.asg.libcommon.util.common.ToastUtils;
import com.arcsoft.asg.libcommon.util.common.Utils;
import com.arcsoft.faceengine.FaceEngine;
import com.arcsoft.faceengine.FaceInfo;
import com.arcsoft.faceengine.VersionInfo;
import com.google.gson.Gson;
import com.raizlabs.android.dbflow.config.FlowManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

import static com.arcsoft.arcfacesingle.util.SdcardUtils.DIR_FACE_DATABASE_IMAGES;

public class PersonListRepository implements IPersonList {

    private static final String TAG = PersonListRepository.class.getSimpleName();
    private static final int PAGE_SIZE = 30;
    private static final String TAG_SEARCH_PERSON_FILTER = "%";

    private FaceEngineManager faceEngineManager;
    private Bitmap tempBitmap;
    private String addPersonImagePath;
    private List<PersonInfo> adapterDataList;
    private List<PersonInfo> chosenList;
    private PersonListRespListener listener;
    private int totalPage;
    private int pageIndex;
    /**
     * 是否处于编辑状态
     */
    private boolean isEditing;
    /**
     * 已删除的人脸数据
     */
    private int deleteCount;

    /**
     * 是否查询成功
     */
    private boolean searchSuccess;

    private Disposable importPersonDisposable;
    private Disposable importPersonFaceDisposable;

    @Override
    public void setListener(PersonListRespListener listener) {
        this.listener = listener;
    }

    @Override
    public void init() {
        adapterDataList = new ArrayList<>();
        chosenList = new ArrayList<>();
        faceEngineManager = new FaceEngineManager();
        faceEngineManager.createFaceEngine();
        Observable.create((ObservableOnSubscribe<Integer>) emitter -> faceEngineManager.initFaceEngine())
                .compose(RxUtils.ioToMain()).subscribe();
    }

    @Override
    public void searchPersonsByTag(String stringTag) {
        if (stringTag.contains(TAG_SEARCH_PERSON_FILTER)) {
            PersonDao.getInstance().getPersonListEqTagAsync(stringTag, new PersonDao.QueryPersonCallback() {
                @Override
                public void onListQueryResult(List<TablePerson> personList) {
                    processQueryList(personList);
                }

                @Override
                public void onListQueryFailed() {
                }
            });
        } else {
            PersonDao.getInstance().getPersonListLikeTagAsync(stringTag, new PersonDao.QueryPersonCallback() {
                @Override
                public void onListQueryResult(List<TablePerson> personList) {
                    processQueryList(personList);
                }

                @Override
                public void onListQueryFailed() {
                }
            });
        }
    }

    private void processQueryList(List<TablePerson> personList) {
        searchSuccess = true;
        long count = personList.size();
        listener.searchPersonList(count);
        List<PersonInfo> personInfos = PersonListDataManager.getAdapterDataList(personList);
        setPagingData(personInfos);
        KeyboardUtils.hideSoftInput(ActivityUtils.getTopActivity());
    }

    @Override
    public boolean searchPersonSuccess() {
        return searchSuccess;
    }

    @Override
    public void clearSearchResult() {
        if (searchSuccess) {
            initFaceList();
        }
    }

    @Override
    public void upgradeFaceFeatureToV3() {
        long count = PersonFaceDao.getInstance().getTotalCount();
        listener.showUpgradeFaceFeatureDialog(count);
    }

    @Override
    public void initFaceList() {
        searchSuccess = false;
        long total = PersonDao.getInstance().getTotalCount();
        if (total > 0) {
            listener.initPersonList(total);
            Disposable disposable = Observable.create((ObservableEmitter<List<PersonInfo>> emitter) -> {
                List<TablePerson> dbFaceList = PersonDao.getInstance().queryAllPerson();
                List<PersonInfo> personInfoList = PersonListDataManager.getAdapterDataList(dbFaceList);
                emitter.onNext(personInfoList);
                emitter.onComplete();
            })
                    .compose(RxUtils.ioToMain())
                    .subscribeWith(new DisposableObserver<List<PersonInfo>>() {
                        @Override
                        public void onNext(List<PersonInfo> personInfos) {
                            setPagingData(personInfos);
                        }

                        @Override
                        public void onError(Throwable e) {
                        }

                        @Override
                        public void onComplete() {
                        }
                    });
        } else {
            listener.initPersonList(0);
            setPagingData(null);
        }
    }

    /**
     * 设置分页数据
     *
     * @param personInfos
     */
    private void setPagingData(List<PersonInfo> personInfos) {
        if (personInfos == null || personInfos.size() == 0) {
            pageIndex = 0;
            totalPage = 0;
            adapterDataList.clear();
            if (listener != null) {
                listener.firstLoadComplete(0, new ArrayList<>());
            }
        } else {
            pageIndex = 0;
            totalPage = 0;
            adapterDataList.clear();
            adapterDataList.addAll(personInfos);
            int totalCount = adapterDataList.size();
            if (totalCount % PAGE_SIZE == 0) {
                totalPage = totalCount / PAGE_SIZE;
            } else {
                totalPage = totalCount / PAGE_SIZE + 1;
            }
            if (listener != null) {
                listener.firstLoadComplete(totalPage, getPersonDataFromPage());
            }
        }
    }

    @Override
    public void loadMoreListData() {
        if (totalPage > 1) {
            pageIndex++;
            if (pageIndex == totalPage) {
                if (listener != null) {
                    listener.loadMoreEnd();
                }
            } else {
                if (listener != null) {
                    listener.loadMoreComplete(getPersonDataFromPage());
                }
            }
        }
    }

    @Override
    public void resetEditing() {
        isEditing = false;
        chosenList.clear();
    }

    @Override
    public boolean isEditing() {
        return isEditing;
    }

    @Override
    public void setCheckBoxChanged(boolean isChecked) {
        chosenList.clear();
        if (isChecked) {
            for (PersonInfo personFace : adapterDataList) {
                personFace.setSelected(true);
                Collections.addAll(chosenList, personFace);
            }
            if (listener != null) {
                listener.onAdapterItemChosen(chosenList);
            }
        } else {
            isEditing = false;
            for (PersonInfo personFace : adapterDataList) {
                personFace.setSelected(false);
            }
        }
    }

    @Override
    public void setAdapterItemClick(int position) {
        PersonInfo personFace = adapterDataList.get(position);
        if (isEditing && CommonUtils.isOfflineLanAppMode()) {
            if (personFace.isSelected()) {
                personFace.setSelected(false);
                chosenList.remove(personFace);
            } else {
                personFace.setSelected(true);
                chosenList.add(personFace);
            }
            if (listener != null) {
                listener.onAdapterItemClick(position);
            }
            if (listener != null) {
                listener.onAdapterItemChosen(chosenList);
            }
        } else {
            listener.showPersonInfoDialog(position, personFace);
        }
    }

    @Override
    public void setAdapterItemLongClick(int position) {
        if (!isEditing) {
            isEditing = true;
            if (listener != null) {
                listener.onAdapterItemLongClick(position);
            }
        }
    }

    /**
     * 分页加载数据
     */
    private List<PersonInfo> getPersonDataFromPage() {
        List<PersonInfo> personFaces = new ArrayList<>();
        if (null != adapterDataList && adapterDataList.size() > 0) {
            if (pageIndex < totalPage - 1) {
                personFaces.addAll(adapterDataList.subList(pageIndex * PAGE_SIZE, (pageIndex + 1) * PAGE_SIZE));
            } else {
                personFaces.addAll(adapterDataList.subList(pageIndex * PAGE_SIZE, adapterDataList.size()));
            }
        }
        return personFaces;
    }

    @Override
    public void confirmDeleteSelectImage() {
        int choseTotal = chosenList.size();
        deleteCount = 0;
        Disposable disposable = Observable.create((ObservableEmitter<Integer> emitter) -> {
            for (PersonInfo personFace : chosenList) {
                String personSerial = personFace.getPersonSerial();
                PersonDao.getInstance().deleteModelByPersonSerial(personSerial);
                List<TablePersonFace> faces = PersonFaceDao.getInstance().getListByPersonSerial(personSerial);
                for (TablePersonFace tablePersonFace : faces) {
                    PersonFaceDao.getInstance().deletePersonFace(tablePersonFace);
                    String imagePath = tablePersonFace.imagePath;
                    FileUtils.deleteFile(new File(imagePath));
                }
                List<TablePersonPermission> permissions = PersonPermissionDao.getInstance().getListByPersonSerial(personSerial);
                PersonPermissionDao.getInstance().deleteListTransaction(permissions, null);
                deleteCount++;
                emitter.onNext(deleteCount);
            }
            if (deleteCount == choseTotal) {
                emitter.onComplete();
            }
        })
                .compose(RxUtils.ioToMain())
                .subscribeWith(new DisposableObserver<Integer>() {
                    @Override
                    public void onNext(Integer deleteCount) {
                        if (listener != null) {
                            listener.onAdapterItemDeleteProgress(deleteCount, choseTotal);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                        chosenList.clear();
                        adapterDataList.clear();
                        isEditing = false;
                        if (listener != null) {
                            listener.onAdapterItemDeleteComplete();
                            listener.clearSearchStatus();
                        }
                    }
                });
    }

    @Override
    public void processPictureByFaceEngine(int source, Uri uri, String filePath, String personSerial, FaceDetectCallback callback) {
        FaceExtractResult fdResult = new FaceExtractResult();
        if (PersonListActivity.REQUEST_CODE_SELECT_ALBUM == source && null == uri) {
            fdResult.setResult(BusinessErrorCode.BEC_FACE_MANAGER_IMAGE_INVALID);
            callback.faceDetect(fdResult);
            return;
        }
        if (PersonListActivity.REQUEST_CODE_TAKE_PHOTO == source && TextUtils.isEmpty(filePath)) {
            fdResult.setResult(BusinessErrorCode.BEC_FACE_MANAGER_IMAGE_INVALID);
            callback.faceDetect(fdResult);
            return;
        }
        String imagePath;
        if (PersonListActivity.REQUEST_CODE_SELECT_ALBUM == source) {
            addPersonImagePath = null;
            imagePath = CommonUtils.getRealPathFromUri(Utils.getApp(), uri);
        } else {
            addPersonImagePath = filePath;
            imagePath = filePath;
        }
        tempBitmap = ImageFileUtils.decodeFileWithThreshold(imagePath, Constants.FACE_REGISTER_MAX_WIDTH,
                Constants.FACE_REGISTER_MAX_HEIGHT);
        if (tempBitmap == null) {
            fdResult.setResult(BusinessErrorCode.BEC_FACE_MANAGER_IMAGE_INVALID);
            callback.faceDetect(fdResult);
            return;
        }
        processPictureIo(callback);
    }

    private void processPictureIo(FaceDetectCallback callback) {
        Disposable disposable = Observable.create((ObservableEmitter<FaceExtractResult> emitter) -> {
            FaceExtractResult faceExtractResult = faceEngineManager.extract(tempBitmap);
            emitter.onNext(faceExtractResult);
            emitter.onComplete();
        }).compose(RxUtils.ioToMain())
                .subscribeWith(new BaseObserver<FaceExtractResult>() {
                    @Override
                    public void onNext(FaceExtractResult faceDetectResult) {
                        callback.faceDetect(faceDetectResult);
                    }

                    @Override
                    public void onError(Throwable e) {
                        FaceExtractResult fdResult = new FaceExtractResult();
                        fdResult.setResult(BusinessErrorCode.BEC_COMMON_UNKNOWN);
                        callback.faceDetect(fdResult);
                    }
                });
    }

    @Override
    public void savePersonToDatabase(String name, String personId, String personSerial, FaceInfo faceInfo, FaceDetectCallback callback) {
        Disposable disposable = Observable.create((ObservableEmitter<Integer> emitter) -> {
            try {
                TablePerson tablePerson = PersonListDataManager.createTablePerson(name, personId, personSerial);
                TablePersonFace personFace = PersonListDataManager.createTablePersonFace(tablePerson, addPersonImagePath);
                Bitmap saveBmp = ImageFileUtils.getFaceRegisterCropBitmap(faceInfo.faceRect, faceInfo.faceOrient, tempBitmap);
                if (saveBmp == null) {
                    saveBmp = tempBitmap;
                }
                if (!ImageFileUtils.save(saveBmp, personFace.imagePath, Bitmap.CompressFormat.JPEG)) {
                    if (!saveBmp.isRecycled()) {
                        saveBmp.recycle();
                    }
                    emitter.onNext(BusinessErrorCode.BEC_FACE_MANAGER_SAVE_BITMAP_FAILED);
                    emitter.onComplete();
                    return;
                }
                personFace.feature = faceInfo.feature;
                personFace.featureVersion = Constants.FACE_FEATURE_VERSION_V30;
                String imageBase64 = ImageFileUtils.bitmapToBase64(saveBmp);
                personFace.imageMD5 = Md5Utils.encode(imageBase64).toLowerCase();
                if (!saveBmp.isRecycled()) {
                    saveBmp.recycle();
                }
                TablePersonPermission personPermission = LocalHttpApiDataUtils.createNewPersonPermission(tablePerson, new Gson());
                if (!PersonPermissionDao.getInstance().addModel(personPermission)) {
                    emitter.onNext(BusinessErrorCode.BEC_FACE_MANAGER_PERSON_SAVE_FAILED);
                    emitter.onComplete();
                    return;
                }
                if (!PersonDao.getInstance().addPerson(tablePerson)) {
                    emitter.onNext(BusinessErrorCode.BEC_FACE_MANAGER_PERSON_SAVE_FAILED);
                    emitter.onComplete();
                    return;
                }
                if (!PersonFaceDao.getInstance().addPersonFace(personFace)) {
                    emitter.onNext(BusinessErrorCode.BEC_FACE_MANAGER_ADD_FACE_FAILED);
                    emitter.onComplete();
                    return;
                }
                emitter.onNext(BusinessErrorCode.BEC_COMMON_OK);
                emitter.onComplete();
            } catch (Exception e) {
                e.printStackTrace();
                emitter.onNext(BusinessErrorCode.BEC_COMMON_UNKNOWN);
                emitter.onComplete();
            }
        }).compose(RxUtils.ioToMain())
                .subscribeWith(new BaseObserver<Integer>() {

                    @Override
                    public void onNext(Integer integer) {
                        FaceExtractResult fdResult = new FaceExtractResult();
                        fdResult.setResult(integer);
                        if (callback != null) {
                            callback.faceDetect(fdResult);
                        }
                        if (integer == BusinessErrorCode.BEC_COMMON_OK) {
                            if (listener != null) {
                                listener.clearSearchContent();
                            }
                            initFaceList();
                        }
                        recycleBitmap();
                    }
                });
    }

    @Override
    public void cancelRegister() {
        if (!FileUtils.isFileExists(addPersonImagePath)) {
            ToastUtils.showShortToast(R.string.register_cancel);
        }
        if (FileUtils.delete(addPersonImagePath)) {
            addPersonImagePath = null;
            recycleBitmap();
            ToastUtils.showShortToast(R.string.register_cancel);
        }
    }

    @Override
    public void unUnit() {
        if (faceEngineManager != null) {
            faceEngineManager.unInitFaceEngine();
            faceEngineManager = null;
        }
        UsbHelper.getInstance().unInit();
        recycleBitmap();
        clearImportPersonDisposable();
        clearImportPersonFaceDisposable();
        addPersonImagePath = null;
        adapterDataList = null;
        chosenList = null;
    }

    @Override
    public boolean saveSelectImage(@NonNull String personName, @Nullable String personId) {
        if (!TextUtils.isEmpty(addPersonImagePath)) {
            File srcFile = new File(addPersonImagePath);
            StringBuilder stringBuilder = new StringBuilder(SdcardUtils.getInstance().getSavePhotoPath());
            stringBuilder.append(File.separator);
            stringBuilder.append(personName);
            if (!TextUtils.isEmpty(personId)) {
                stringBuilder.append("_");
                stringBuilder.append(personId);
            }
            stringBuilder.append("_").append(System.currentTimeMillis()).append(".jpg");
            File desFile = new File(stringBuilder.toString());
            if (FileUtils.createFileByDeleteOldFile(desFile)) {
                FileInputStream inStream = null;
                try {
                    inStream = new FileInputStream(srcFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                FileOutputStream outStream = null;
                try {
                    outStream = new FileOutputStream(desFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                if (inStream != null && outStream != null) {
                    FileChannel inChannel = inStream.getChannel();
                    FileChannel outChannel = outStream.getChannel();
                    try {
                        inChannel.transferTo(0, inChannel.size(), outChannel);
                        outStream.flush();
                        outStream.getFD().sync();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (inStream != null) {
                    try {
                        inStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (outStream != null) {
                    try {
                        outStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void importPersonFromDatabase(ImportDatabaseListener listener) {
        String dataPath = SdcardUtils.getInstance().getBackUpDbFilePath();
        File file = new File(dataPath);
        if (!file.exists()) {
            ToastUtils.showShortToast(CommonUtils.getStrFromRes(R.string.import_failed_data_empty));
            return;
        }
        SQLiteDatabase database = ExternalDatabaseHelper.getDatabase(dataPath);
        long count1 = ExternalDatabaseHelper.getTablePersonCount(database);
        long count2 = ExternalDatabaseHelper.getTablePersonFaceCount(database);
        if (count1 == 0 || count2 == 0) {
            ToastUtils.showShortToast(CommonUtils.getStrFromRes(R.string.import_failed_data_empty));
            return;
        }
        List<TableArcFaceVersion> versions = ExternalDatabaseHelper.getArcFaceVersionList(database);
        if (versions.size() == 0) {
            ToastUtils.showShortToast(CommonUtils.getStrFromRes(R.string.import_failed_feature_version_invalid));
            return;
        }
        TableArcFaceVersion arcFaceVersion = versions.get(0);
        String oldVersion = arcFaceVersion.version;
        String oldVersionMain = oldVersion.substring(0, 1);
        VersionInfo versionInfo = FaceEngine.getVersionInfo();
        if (versionInfo != null) {
            String currentEngineVersion = versionInfo.getVersion();
            String mainVersion = currentEngineVersion.substring(0, 1);
            if (!oldVersionMain.equals(mainVersion)) {
                //外部人脸特征值版本和APP ArcFace SDK版本不一致，不允许导入
                ToastUtils.showShortToast(CommonUtils.getStrFromRes(R.string.import_failed_feature_version_invalid));
                return;
            }
        }
        long total = count1 + count2;
        listener.getCount(total);
        clearImportPersonDisposable();
        importPersonDisposable = Observable.create((ObservableEmitter<List<TablePerson>> emitter) -> {
            List<TablePerson> personList = ExternalDatabaseHelper.getPersonListFromDb(database);
            emitter.onNext(personList);
        }).flatMap(personList -> {
            List<TablePersonFace> faceList = ExternalDatabaseHelper.getPersonFaceList(database);
            Pair<List<TablePerson>, List<TablePersonFace>> pair = new Pair<>(personList, faceList);
            return Observable.just(pair);
        }).compose(RxUtils.ioToMain())
                .subscribeWith(new BaseObserver<Pair<List<TablePerson>, List<TablePersonFace>>>() {
                    @Override
                    public void onNext(Pair<List<TablePerson>, List<TablePersonFace>> pair) {
                        savePersonToDb(total, database, pair, listener);
                    }

                    @Override
                    public void onError(ExceptionHandler.ResponseThrowable throwable) {
                        ToastUtils.showShortToast(throwable.message);
                        database.close();
                    }
                });
    }

    private void savePersonToDb(long total, SQLiteDatabase database, Pair<List<TablePerson>, List<TablePersonFace>> pair,
                                ImportDatabaseListener listener) {
        List<TablePerson> personList = pair.first;
        List<TablePersonFace> personFaceList = pair.second;
        importPersonFaceDisposable = Observable.create((ObservableEmitter<Integer> emitter) -> {
            String localFacePath = SdcardUtils.getInstance().getRegisteredDirPath();
            FlowManager.getDatabase(DBManager.class)
                    .executeTransaction(databaseWrapper -> {
                        int i;
                        for (i = 0; i < personList.size(); i++) {
                            TablePerson person = personList.get(i);
                            person.save(databaseWrapper);
                            emitter.onNext(i + 1);
                        }
                        for (int j = 0; j < personFaceList.size(); j++) {
                            TablePersonFace face = personFaceList.get(j);
                            try {
                                String oriImagePath = face.imagePath;
                                if (!TextUtils.isEmpty(oriImagePath)) {
                                    int index = oriImagePath.indexOf(DIR_FACE_DATABASE_IMAGES);
                                    String oriImageName = oriImagePath.substring(index + DIR_FACE_DATABASE_IMAGES.length() + 1);
                                    face.imagePath = localFacePath + File.separator + oriImageName;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            face.save(databaseWrapper);
                            emitter.onNext(i + j + 1);
                        }
                        emitter.onComplete();
                    });
        }).compose(RxUtils.ioToMain())
                .subscribeWith(new BaseObserver<Integer>() {
                    @Override
                    public void onNext(Integer current) {
                        listener.onProgress(total, current);
                    }

                    @Override
                    public void onError(ExceptionHandler.ResponseThrowable throwable) {
                        listener.onFailed();
                        database.close();
                    }
                });
    }

    @Override
    public void exportDatabase() {
        Disposable disposable = Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            String dbPath = SdcardUtils.getInstance().getBackUpDbFilePath();
            CommonUtils.backUpDataBase(Utils.getApp(), Constants.DATA_BASE_NAME_PATH, dbPath, new BackUpDatabaseListener() {
                @Override
                public void onSuccess() {
                    emitter.onNext(true);
                    emitter.onComplete();
                }

                @Override
                public void onFailed() {
                    emitter.onError(new Throwable());
                }
            });
        }).compose(RxUtils.ioToMain())
                .subscribeWith(new BaseObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        ToastUtils.showShortToast(CommonUtils.getStrFromRes(R.string.export_data_successful));
                    }

                    @Override
                    public void onError(ExceptionHandler.ResponseThrowable throwable) {
                        ToastUtils.showShortToast(CommonUtils.getStrFromRes(R.string.export_data_failed));
                    }
                });
    }

    private void clearImportPersonDisposable() {
        if (importPersonDisposable != null && !importPersonDisposable.isDisposed()) {
            importPersonDisposable.dispose();
            importPersonDisposable = null;
        }
    }

    private void clearImportPersonFaceDisposable() {
        if (importPersonFaceDisposable != null && !importPersonFaceDisposable.isDisposed()) {
            importPersonFaceDisposable.dispose();
            importPersonFaceDisposable = null;
        }
    }

    private void recycleBitmap() {
        if (tempBitmap != null && !tempBitmap.isRecycled()) {
            tempBitmap.recycle();
        }
        tempBitmap = null;
    }

    public interface BackUpDatabaseListener {

        /**
         * 备份成功
         */
        void onSuccess();

        /**
         * 备份失败
         */
        void onFailed();
    }

    public interface ImportDatabaseListener {

        /**
         * 获取数量
         * @param count 数量
         */
        void getCount(long count);

        /**
         * 导入进度
         * @param total 总数
         * @param current 当前数量
         */
        void onProgress(long total, long current);

        /**
         * 导入失败
         */
        void onFailed();
    }
}
