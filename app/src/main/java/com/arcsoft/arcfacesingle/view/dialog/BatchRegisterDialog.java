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
import com.arcsoft.arcfacesingle.business.common.CommonRepository;
import com.arcsoft.arcfacesingle.data.db.dao.PersonDao;
import com.arcsoft.arcfacesingle.data.db.dao.PersonFaceDao;
import com.arcsoft.arcfacesingle.data.db.dao.PersonPermissionDao;
import com.arcsoft.arcfacesingle.data.db.table.TablePerson;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonFace;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonPermission;
import com.arcsoft.arcfacesingle.data.model.FaceExtractResult;
import com.arcsoft.arcfacesingle.libutil.ImageFileUtils;
import com.arcsoft.arcfacesingle.server.api.LocalHttpApiDataUtils;
import com.arcsoft.arcfacesingle.server.faceengine.FaceEngineManager;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.arcfacesingle.util.SdcardUtils;
import com.arcsoft.arcfacesingle.view.widgets.HorizontalProgressBar;
import com.arcsoft.asg.libcommon.base.BaseDialogFragment;
import com.arcsoft.asg.libcommon.base.BaseViewHolder;
import com.arcsoft.asg.libcommon.util.business.rx.RxUtils;
import com.arcsoft.asg.libcommon.util.common.AppUtils;
import com.arcsoft.asg.libcommon.util.common.ArithmeticUtils;
import com.arcsoft.asg.libcommon.util.common.DeviceUtils;
import com.arcsoft.asg.libcommon.util.common.FileUtils;
import com.arcsoft.asg.libcommon.util.common.Md5Utils;
import com.arcsoft.asg.libcommon.util.common.ScreenUtils;
import com.arcsoft.asg.libcommon.util.common.ToastUtils;
import com.arcsoft.asg.libcommon.util.common.Utils;
import com.arcsoft.faceengine.ErrorInfo;
import com.arcsoft.faceengine.FaceInfo;
import com.google.gson.Gson;

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

public class BatchRegisterDialog extends BaseDialogFragment {

    public static final int SIZE_FACE_TOTAL_LEVEL1 = 5;
    public static final int SIZE_FACE_TOTAL_LEVEL2 = 10;

    private TextView tvRegisterResult;
    private TextView tvRegisterComplete;
    private TextView tvProgressValue;
    private Button btConfirm;
    private HorizontalProgressBar progressBar;

    private int registerSuccessCount;
    private int registerFailureCount;
    private int imageFileTotal;
    private CompositeDisposable compositeDisposable;
    private ConcurrentHashMap<Integer, List<String>> batchRegisterFileMap;
    private FaceEngineManager faceEngineManager1;
    private FaceEngineManager faceEngineManager2;
    private FaceEngineManager faceEngineManager3;

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
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        compositeDisposable = new CompositeDisposable();
        batchRegisterFileMap = new ConcurrentHashMap<>();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String strBatchDir = SdcardUtils.getInstance().getBatchRegisterOriDir();
        final File fileDir = new File(strBatchDir);
        if (fileDir.isDirectory()) {
            registerSuccessCount = 0;
            registerFailureCount = 0;
            CommonUtils.clearFileList();
            List<String> fileList = CommonUtils.refreshFileList(strBatchDir);
            if (null == fileList || fileList.size() == 0) {
                dismissAllowingStateLoss();
                ToastUtils.showShortToast(R.string.register_error);
                return;
            }
            imageFileTotal = fileList.size();
            if (imageFileTotal < SIZE_FACE_TOTAL_LEVEL1) {
                initFaceEngine1();
                splitRegisterFile(0, fileList);
            } else if (imageFileTotal < SIZE_FACE_TOTAL_LEVEL2) {
                initFaceEngine1();
                initFaceEngine2();
                splitRegisterFile(1, fileList);
            } else {
                initFaceEngine1();
                initFaceEngine2();
                initFaceEngine3();
                splitRegisterFile(2, fileList);
            }
        }
    }

    private void initFaceEngine1() {
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
    private void splitRegisterFile(int type, List<String> filePaths) {
        int faceTotal = filePaths.size();
        if (type == 0) {
            batchRegisterFileMap.put(0, filePaths);
        } else if (type == 1) {
            List<String> tablePeople1 = new ArrayList<>(filePaths.subList(0, faceTotal / 2));
            List<String> tablePeople2 = new ArrayList<>(filePaths.subList(faceTotal / 2, faceTotal));
            batchRegisterFileMap.put(0, tablePeople1);
            batchRegisterFileMap.put(1, tablePeople2);
        } else {
            List<String> tablePeople1 = new ArrayList<>(filePaths.subList(0, faceTotal / 3));
            List<String> tablePeople2 = new ArrayList<>(filePaths.subList(faceTotal / 3, faceTotal * 2 / 3));
            List<String> tablePeople3 = new ArrayList<>(filePaths.subList(faceTotal * 2 / 3, faceTotal));
            batchRegisterFileMap.put(0, tablePeople1);
            batchRegisterFileMap.put(1, tablePeople2);
            batchRegisterFileMap.put(2, tablePeople3);
        }
        beginRegisterPictures(type);
    }

    private void beginRegisterPictures(int type) {
        if (type == 0) {
            List<String> files1 = batchRegisterFileMap.get(0);
            Disposable disposable = Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
                if (files1 != null) {
                    for (int i = 0; i < files1.size(); i++) {
                        String filePath = files1.get(i);
                        if (FileUtils.getSdcardAvailableSize() < Constants.SDCARD_STORAGE_SIZE_DELETE) {
                            emitter.onNext(0);
                        } else if (TextUtils.isEmpty(DeviceUtils.getMacAddress())) {
                            emitter.onNext(0);
                        } else {
                            faceEngineRegisterImage(faceEngineManager1, filePath, emitter);
                        }
                    }
                } else {
                    emitter.onNext(0);
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
            List<String> files1 = batchRegisterFileMap.get(0);
            List<String> files2 = batchRegisterFileMap.get(1);
            Disposable disposable = Observable
                    .merge(getRegisterObservable(faceEngineManager1, files1),
                            getRegisterObservable(faceEngineManager2, files2))
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
            List<String> files1 = batchRegisterFileMap.get(0);
            List<String> files2 = batchRegisterFileMap.get(1);
            List<String> files3 = batchRegisterFileMap.get(2);
            Disposable disposable = Observable
                    .merge(getRegisterObservable(faceEngineManager1, files1),
                            getRegisterObservable(faceEngineManager2, files2),
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

    private Observable<Integer> getRegisterObservable(FaceEngineManager manager, List<String> files) {
        int size = files.size();
        if (size > 0) {
            return Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
                for (int i = 0; i < size; i++) {
                    String filePath = files.get(i);
                    if (FileUtils.getSdcardAvailableSize() < Constants.SDCARD_STORAGE_SIZE_DELETE) {
                        emitter.onNext(0);
                    } else if (TextUtils.isEmpty(DeviceUtils.getMacAddress())) {
                        emitter.onNext(0);
                    } else {
                        faceEngineRegisterImage(manager, filePath, emitter);
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
    private void faceEngineRegisterImage(FaceEngineManager manager, String oriPicFilePath, ObservableEmitter<Integer> emitter) {
        final File picFile = new File(oriPicFilePath);
        try {
            Bitmap bitmap = ImageFileUtils.decodeFileWithThreshold(oriPicFilePath, Constants.FACE_REGISTER_MAX_WIDTH,
                    Constants.FACE_REGISTER_MAX_HEIGHT);
            if (bitmap == null) {
                emitter.onNext(0);
                return;
            }
            FaceExtractResult feResult = manager.extract(bitmap);
            if (feResult.getResult() != BusinessErrorCode.BEC_COMMON_OK) {
                emitter.onNext(0);
                return;
            }
            if (FileUtils.getSdcardAvailableSize() < Constants.SDCARD_STORAGE_SIZE_DELETE) {
                emitter.onNext(0);
                return;
            }
            if (TextUtils.isEmpty(DeviceUtils.getMacAddress())) {
                emitter.onNext(0);
                return;
            }
            FaceInfo faceInfo = feResult.getFaceInfo();
            TablePerson tablePerson = CommonRepository.getInstance().createTablePerson(picFile.getName());
            TablePersonFace personFace = CommonRepository.getInstance().createTablePersonFace(tablePerson, faceInfo.feature);
            Bitmap newSaveBmp = ImageFileUtils.getFaceRegisterCropBitmap(faceInfo.faceRect, faceInfo.faceOrient, bitmap);
            boolean flag = ImageFileUtils.save(newSaveBmp, personFace.imagePath, Bitmap.CompressFormat.JPEG);
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
            if (newSaveBmp != null && !newSaveBmp.isRecycled()) {
                newSaveBmp.recycle();
            }
            if (!flag) {
                emitter.onNext(0);
                return;
            }
            TablePersonPermission personPermission = LocalHttpApiDataUtils.createNewPersonPermission(tablePerson, new Gson());
            if (!PersonPermissionDao.getInstance().addModel(personPermission)) {
                emitter.onNext(0);
                return;
            }
            if (!PersonDao.getInstance().addPerson(tablePerson)) {
                emitter.onNext(0);
                return;
            }
            String imageBase64 = ImageFileUtils.image2Base64(personFace.imagePath);
            personFace.imageMD5 = Md5Utils.encode(imageBase64).toLowerCase();
            if (!PersonFaceDao.getInstance().addPersonFace(personFace)) {
                emitter.onNext(0);
                return;
            }
            //注册成功删除原图
            FileUtils.delete(oriPicFilePath);
            emitter.onNext(1);
        } catch (Exception e) {
            e.printStackTrace();
            emitter.onNext(0);
        }
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
        unInitFaceEngine();
    }

    private void unInitFaceEngine() {
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
