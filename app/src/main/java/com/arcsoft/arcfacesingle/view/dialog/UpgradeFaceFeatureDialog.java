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

package com.arcsoft.arcfacesingle.view.dialog;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.app.BusinessErrorCode;
import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.data.db.DBManager;
import com.arcsoft.arcfacesingle.data.db.dao.ArcFaceVersionDao;
import com.arcsoft.arcfacesingle.data.db.dao.PersonDao;
import com.arcsoft.arcfacesingle.data.db.dao.PersonFaceDao;
import com.arcsoft.arcfacesingle.data.db.table.TableArcFaceVersion;
import com.arcsoft.arcfacesingle.data.db.table.TablePerson;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonFace;
import com.arcsoft.arcfacesingle.data.model.FaceExtractResult;
import com.arcsoft.arcfacesingle.libutil.ImageFileUtils;
import com.arcsoft.arcfacesingle.server.faceengine.FaceEngineManager;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.arcfacesingle.view.widgets.HorizontalProgressBar;
import com.arcsoft.asg.libcommon.base.BaseDialogFragment;
import com.arcsoft.asg.libcommon.base.BaseViewHolder;
import com.arcsoft.asg.libcommon.util.business.rx.RxUtils;
import com.arcsoft.asg.libcommon.util.common.AppUtils;
import com.arcsoft.asg.libcommon.util.common.ArithmeticUtils;
import com.arcsoft.asg.libcommon.util.common.DeviceUtils;
import com.arcsoft.asg.libcommon.util.common.FileUtils;
import com.arcsoft.asg.libcommon.util.common.ScreenUtils;
import com.arcsoft.asg.libcommon.util.common.Utils;
import com.arcsoft.faceengine.ErrorInfo;
import com.arcsoft.faceengine.FaceEngine;
import com.arcsoft.faceengine.FaceInfo;
import com.arcsoft.faceengine.VersionInfo;
import com.raizlabs.android.dbflow.config.FlowManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import me.jessyan.autosize.AutoSize;

public class UpgradeFaceFeatureDialog extends BaseDialogFragment {

    public static final int SIZE_FACE_TOTAL_LEVEL1 = 500;
    public static final int SIZE_FACE_TOTAL_LEVEL2 = 1000;

    private TextView tvRegisterResult;
    private TextView tvRegisterComplete;
    private TextView tvProgressValue;
    private Button btConfirm;
    private HorizontalProgressBar progressBar;

    private String faceEngineVersion;
    private int registerSuccessCount;
    private int registerFailureCount;
    private long imageFileTotal;
    private CompositeDisposable compositeDisposable;
    private ConcurrentHashMap<Integer, List<TablePersonFace>> batchRegisterFileMap;
    private FaceEngineManager faceEngineManager1;
    private FaceEngineManager faceEngineManager2;
    private FaceEngineManager faceEngineManager3;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        compositeDisposable = new CompositeDisposable();
        batchRegisterFileMap = new ConcurrentHashMap<>();
    }

    @Override
    protected int setUpLayoutId() {
        if (null != getActivity()) {
            if (ScreenUtils.isPortrait()) {
                AutoSize.autoConvertDensity(getActivity(), 720, true);
            } else {
                AutoSize.autoConvertDensity(getActivity(), 1280, true);
            }
        }
        return R.layout.dialog_batch_register_progress;
    }

    @Override
    protected void convertView(BaseViewHolder holder, BaseDialogFragment baseDialog) {
        super.convertView(holder, baseDialog);
        tvRegisterResult = holder.getView(R.id.tv_batch_register_progress_result);
        tvRegisterComplete = holder.getView(R.id.tv_batch_register_complete_result);
        tvProgressValue = holder.getView(R.id.tv_progress_value);
        progressBar = holder.getView(R.id.progress_horizontal);
        btConfirm = holder.getView(R.id.btn_batch_register_confirm);
        btConfirm.setEnabled(false);
        btConfirm.setText(AppUtils.getString(R.string.wait));
        holder.setText(R.id.tv_dialog_title, CommonUtils.getStrFromRes(R.string.upgrade_arc_face_dialog_title));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        registerSuccessCount = 0;
        registerFailureCount = 0;

        Disposable disposable = Observable.create((ObservableEmitter<List<TablePersonFace>> emitter) -> {
            List<TablePersonFace> personFaceList = PersonFaceDao.getInstance().queryAllFace();
            imageFileTotal = personFaceList.size();
            emitter.onNext(personFaceList);
            emitter.onComplete();
        }).compose(RxUtils.ioToMain())
                .subscribe(personFaceList -> {
                    if (imageFileTotal < SIZE_FACE_TOTAL_LEVEL1) {
                        initFaceEngine1();
                        splitRegisterFile(0, personFaceList);
                    } else if (imageFileTotal < SIZE_FACE_TOTAL_LEVEL2) {
                        initFaceEngine1();
                        initFaceEngine2();
                        splitRegisterFile(1, personFaceList);
                    } else {
                        initFaceEngine1();
                        initFaceEngine2();
                        initFaceEngine3();
                        splitRegisterFile(2, personFaceList);
                    }
                }, throwable -> {
                    String strResult = AppUtils.getString(R.string.register_error);
                    setBtConfirmUi(strResult, false);
                });
    }

    private void initFaceEngine1() {
        VersionInfo versionInfo = FaceEngine.getVersionInfo();
        if (versionInfo != null) {
            faceEngineVersion = versionInfo.getVersion();
        } else {
            faceEngineVersion = Constants.FACE_FEATURE_VERSION_V3_0;
        }
        faceEngineManager1 = new FaceEngineManager();
        faceEngineManager1.createFaceEngine();
        int res = faceEngineManager1.initFaceEngine();
        if (res != ErrorInfo.MOK) {
            String strResult = AppUtils.getString(R.string.face_engine_init_fail) + ":" + res;
            setBtConfirmUi(strResult, false);
        }
    }

    private void initFaceEngine2() {
        faceEngineManager2 = new FaceEngineManager();
        faceEngineManager2.createFaceEngine();
        int res = faceEngineManager2.initFaceEngine();
        if (res != ErrorInfo.MOK) {
            String strResult = AppUtils.getString(R.string.face_engine_init_fail) + ":" + res;
            setBtConfirmUi(strResult, false);
        }
    }

    private void initFaceEngine3() {
        faceEngineManager3 = new FaceEngineManager();
        faceEngineManager3.createFaceEngine();
        int res = faceEngineManager3.initFaceEngine();
        if (res != ErrorInfo.MOK) {
            String strResult = AppUtils.getString(R.string.face_engine_init_fail) + ":" + res;
            setBtConfirmUi(strResult, false);
        }
    }

    private void setBtConfirmUi(String strResult, boolean success) {
        if (success) {
            tvRegisterComplete.setTextColor(getResources().getColor(R.color.color_success_bg_color));
            TableArcFaceVersion arcFaceVersion = new TableArcFaceVersion();
            arcFaceVersion.version = faceEngineVersion;
            ArcFaceVersionDao.getInstance().saveModelAsync(arcFaceVersion);
        } else {
            tvRegisterComplete.setTextColor(getResources().getColor(R.color.color_failed_bg_color));
        }
        btConfirm.setEnabled(true);
        btConfirm.setText(AppUtils.getString(R.string.confirm));
        tvRegisterComplete.setText(strResult);
    }

    /**
     * 将批量注册的文件等量划分
     */
    private void splitRegisterFile(int type, List<TablePersonFace> filePaths) {
        int faceTotal = filePaths.size();
        if (type == 0) {
            batchRegisterFileMap.put(0, filePaths);
        } else if (type == 1) {
            List<TablePersonFace> tablePeople1 = new ArrayList<>(filePaths.subList(0, faceTotal / 2));
            List<TablePersonFace> tablePeople2 = new ArrayList<>(filePaths.subList(faceTotal / 2, faceTotal));
            batchRegisterFileMap.put(0, tablePeople1);
            batchRegisterFileMap.put(1, tablePeople2);
        } else {
            List<TablePersonFace> tablePeople1 = new ArrayList<>(filePaths.subList(0, faceTotal / 3));
            List<TablePersonFace> tablePeople2 = new ArrayList<>(filePaths.subList(faceTotal / 3, faceTotal * 2 / 3));
            List<TablePersonFace> tablePeople3 = new ArrayList<>(filePaths.subList(faceTotal * 2 / 3, faceTotal));
            batchRegisterFileMap.put(0, tablePeople1);
            batchRegisterFileMap.put(1, tablePeople2);
            batchRegisterFileMap.put(2, tablePeople3);
        }
        beginRegisterPictures(type);
    }

    private void beginRegisterPictures(int type) {
        if (type == 0) {
            Disposable disposable = Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
                List<TablePersonFace> files1 = batchRegisterFileMap.get(0);
                for (int i = 0; i < files1.size(); i++) {
                    TablePersonFace personFace = files1.get(i);
                    if (FileUtils.getSdcardAvailableSize() < Constants.SDCARD_STORAGE_SIZE_DELETE) {
                        emitter.onNext(0);
                    } else if (TextUtils.isEmpty(DeviceUtils.getMacAddress())) {
                        emitter.onNext(0);
                    } else {
                        faceEngineRegisterImage(faceEngineManager1, personFace, emitter);
                    }
                }
                emitter.onComplete();
            }).compose(RxUtils.ioToMain())
                    .subscribeWith(new DisposableObserver<Integer>() {
                        @Override
                        public void onNext(Integer type) {
                            if (type == 1) {
                                registerSuccessCount++;
                            } else {
                                registerFailureCount++;
                            }
                            setRegisterResultUi(registerSuccessCount, registerFailureCount);
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {
                        }
                    });
            compositeDisposable.add(disposable);
        } else if (type == 1) {
            List<TablePersonFace> files1 = batchRegisterFileMap.get(0);
            List<TablePersonFace> files2 = batchRegisterFileMap.get(1);
            Disposable disposable = Observable
                    .merge(getRegisterObservable(faceEngineManager1, files1), getRegisterObservable(faceEngineManager2, files2))
                    .subscribeWith(new DisposableObserver<Integer>() {
                        @Override
                        public void onNext(Integer count) {
                            if (count > 0) {
                                registerSuccessCount += 1;
                            } else {
                                registerFailureCount += 1;
                            }
                            setRegisterResultUi(registerSuccessCount, registerFailureCount);
                        }

                        @Override
                        public void onError(Throwable e) {
                        }

                        @Override
                        public void onComplete() {
                        }
                    });
            compositeDisposable.add(disposable);
        } else {
            List<TablePersonFace> files1 = batchRegisterFileMap.get(0);
            List<TablePersonFace> files2 = batchRegisterFileMap.get(1);
            List<TablePersonFace> files3 = batchRegisterFileMap.get(2);
            Disposable disposable = Observable
                    .merge(getRegisterObservable(faceEngineManager1, files1), getRegisterObservable(faceEngineManager2, files2),
                            getRegisterObservable(faceEngineManager3, files3))
                    .subscribeWith(new DisposableObserver<Integer>() {
                        @Override
                        public void onNext(Integer count) {
                            if (count > 0) {
                                registerSuccessCount += 1;
                            } else {
                                registerFailureCount += 1;
                            }
                            setRegisterResultUi(registerSuccessCount, registerFailureCount);
                        }

                        @Override
                        public void onError(Throwable e) {
                        }

                        @Override
                        public void onComplete() {
                        }
                    });
            compositeDisposable.add(disposable);
        }
    }

    private void setRegisterResultUi(int successCount, int failureCount) {
        double div = ArithmeticUtils.div(successCount + failureCount, imageFileTotal, 3);
        double mul = ArithmeticUtils.mul(div, 100, 1);
        int progress = (int) mul;
        progressBar.setProgressBar(progress);
        String strProgress = progress + "%";
        tvProgressValue.setText(strProgress);

        String tip = Utils.getApp().getString(R.string.batch_register_count_detail, imageFileTotal,
                successCount, failureCount);
        tvRegisterResult.setText(tip);

        if ((successCount + failureCount) == imageFileTotal) {
            if (FileUtils.getSdcardAvailableSize() < Constants.SDCARD_STORAGE_SIZE_DELETE) {
                String strResult = CommonUtils.getStrFromRes(R.string.device_storage_warn_tip1);
                setBtConfirmUi(strResult, false);
            } else if (TextUtils.isEmpty(DeviceUtils.getMacAddress())) {
                String strResult = CommonUtils.getStrFromRes(R.string.device_mac_address_empty);
                setBtConfirmUi(strResult, false);
            } else {
                String strResult = CommonUtils.getStrFromRes(R.string.batch_register_complete);
                setBtConfirmUi(strResult, true);
            }
        }
    }

    private Observable<Integer> getRegisterObservable(FaceEngineManager faceEngineManager, List<TablePersonFace> files) {
        int size = files.size();
        if (size > 0) {
            return Observable.create((ObservableEmitter<Integer> emitter) -> {
                for (int i = 0; i < size; i++) {
                    TablePersonFace personFace = files.get(i);
                    if (FileUtils.getSdcardAvailableSize() < Constants.SDCARD_STORAGE_SIZE_DELETE) {
                        emitter.onNext(0);
                    } else if (TextUtils.isEmpty(DeviceUtils.getMacAddress())) {
                        emitter.onNext(0);
                    } else {
                        faceEngineRegisterImage(faceEngineManager, personFace, emitter);
                    }
                }
                emitter.onComplete();
            }).compose(RxUtils.ioToMain());
        } else {
            return Observable.just(-1);
        }
    }

    /**
     * 注册人脸照
     */
    private void faceEngineRegisterImage(FaceEngineManager manager, TablePersonFace personFace, ObservableEmitter<Integer> emitter) {
        try {
            String imagePath = personFace.imagePath;
            if (!new File(imagePath).exists()) {
                onNextFailed(personFace, emitter);
                return;
            }
            Bitmap oriBmp = ImageFileUtils.decodeFileWithThreshold(personFace.imagePath, Constants.FACE_REGISTER_MAX_WIDTH,
                    Constants.FACE_REGISTER_MAX_HEIGHT);
            if (oriBmp == null) {
                onNextFailed(personFace, emitter);
                return;
            }
            FaceExtractResult faceExtractResult = manager.extract(oriBmp);
            if (faceExtractResult.getResult() != BusinessErrorCode.BEC_COMMON_OK) {
                onNextFailed(personFace, emitter);
                return;
            }
            if (FileUtils.getSdcardAvailableSize() < Constants.SDCARD_STORAGE_SIZE_DELETE) {
                onNextFailed(personFace, emitter);
                return;
            }
            FaceInfo faceInfo = faceExtractResult.getFaceInfo();
            personFace.updateTime = System.currentTimeMillis();
            personFace.feature = faceInfo.feature;
            personFace.featureVersion = Constants.FACE_FEATURE_VERSION_V30;
            if (!PersonFaceDao.getInstance().updatePersonFace(personFace)) {
                onNextFailed(personFace, emitter);
                return;
            }
            emitter.onNext(1);
        } catch (Exception e) {
            e.printStackTrace();
            onNextFailed(personFace, emitter);
        }
    }

    private void onNextFailed(TablePersonFace personFace, ObservableEmitter<Integer> emitter) {
        deletePersonAndFace(personFace);
        emitter.onNext(0);
    }

    private void deletePersonAndFace(TablePersonFace tablePersonFace) {
        String personSerial = tablePersonFace.personSerial;
        FlowManager.getDatabase(DBManager.class)
                .beginTransactionAsync(databaseWrapper -> {
                    TablePerson tablePerson = PersonDao.getInstance().getPersonByPersonSerial(personSerial);
                    if (tablePerson != null) {
                        tablePerson.delete(databaseWrapper);
                    }
                    tablePersonFace.delete(databaseWrapper);
                })
                .success(transaction -> FileUtils.delete(tablePersonFace.imagePath))
                .error((transaction, error) -> {
                }).build().execute();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        release();
    }

    public void release() {
        if (compositeDisposable != null) {
            compositeDisposable.clear();
            compositeDisposable = null;
        }
        if (faceEngineManager1 != null) {
            faceEngineManager1.unInitFaceEngine();
            faceEngineManager1 = null;
        }
        if (faceEngineManager2 != null) {
            faceEngineManager2.unInitFaceEngine();
            faceEngineManager2 = null;
        }
        if (faceEngineManager3 != null) {
            faceEngineManager3.unInitFaceEngine();
            faceEngineManager3 = null;
        }
    }
}
