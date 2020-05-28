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

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;

import androidx.databinding.BaseObservable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.app.BusinessErrorCode;
import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.business.personlist.IPersonList;
import com.arcsoft.arcfacesingle.business.personlist.PersonListRepository;
import com.arcsoft.arcfacesingle.business.personlist.PersonListRespListener;
import com.arcsoft.arcfacesingle.business.setting.ConfigConstants;
import com.arcsoft.arcfacesingle.data.model.PersonInfo;
import com.arcsoft.arcfacesingle.navigator.IPersonListNavigator;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.arcfacesingle.util.SdcardUtils;
import com.arcsoft.arcfacesingle.view.activity.PersonListActivity;
import com.arcsoft.arcfacesingle.view.activity.TakePhotoActivity;
import com.arcsoft.asg.libcommon.util.business.DoubleClickUtils;
import com.arcsoft.asg.libcommon.util.common.ActivityUtils;
import com.arcsoft.asg.libcommon.util.common.AppUtils;
import com.arcsoft.asg.libcommon.util.common.DeviceUtils;
import com.arcsoft.asg.libcommon.util.common.FileUtils;
import com.arcsoft.asg.libcommon.util.common.KeyboardUtils;
import com.arcsoft.asg.libcommon.util.common.ToastUtils;
import com.arcsoft.asg.libcommon.util.common.Utils;
import com.arcsoft.faceengine.FaceInfo;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import java.io.File;
import java.util.List;

public class PersonListViewModel extends BaseObservable {

    private static final String TAG = PersonListViewModel.class.getSimpleName();

    public final ObservableBoolean ivAddFaceVisible = new ObservableBoolean(true);
    public final ObservableBoolean rlDeleteVisible = new ObservableBoolean(true);
    public final ObservableBoolean cbSelectAllChecked = new ObservableBoolean(true);
    public final ObservableField<String> strFaceTotal = new ObservableField<>();
    public final ObservableField<String> strFaceSearchTotal = new ObservableField<>();
    public final ObservableField<String> cbSelectTitle = new ObservableField<>();
    public final ObservableField<String> fieldSearchContent = new ObservableField<>();
    public final ObservableBoolean fieldSearchResultVisible = new ObservableBoolean();
    public final ObservableField<String> fieldImportExportContent = new ObservableField<>();
    public final ObservableBoolean fieldImportExportVisible = new ObservableBoolean();

    private long faceTotalCount;
    private long chosenFaceCount;
    private IPersonListNavigator navigator;
    private IPersonList repository;

    public PersonListViewModel() {
        repository = new PersonListRepository();
        repository.setListener(faceManagerListener);
    }

    public void init() {
        fieldSearchResultVisible.set(false);
        rlDeleteVisible.set(false);
        if (CommonUtils.isOfflineLanAppMode()) {
            ivAddFaceVisible.set(true);
            fieldImportExportVisible.set(true);
            cbSelectTitle.set(AppUtils.getString(R.string.select_all));
            cbSelectAllChecked.set(false);
            navigator.setBottomDeleteEnable(false);
        }
        if (CommonUtils.isCloudAiotAppMode()) {
            ivAddFaceVisible.set(false);
            fieldImportExportVisible.set(false);
        }
        repository.init();
    }

    private void initBottomControlUi() {
        if (CommonUtils.isOfflineLanAppMode()) {
            ivAddFaceVisible.set(true);
            fieldImportExportVisible.set(true);
            cbSelectTitle.set(AppUtils.getString(R.string.select_all));
            cbSelectAllChecked.set(false);
            navigator.setBottomDeleteEnable(false);
            rlDeleteVisible.set(false);
        }
    }

    public void setNavigator(IPersonListNavigator personListNavigator) {
        this.navigator = personListNavigator;
    }

    public void upgradeFaceFeature() {
        repository.upgradeFaceFeatureToV3();
    }

    public void initFaceList() {
        if (!TextUtils.isEmpty(fieldSearchContent.get())) {
            fieldSearchContent.set("");
        }
        if (repository != null) {
            repository.initFaceList();
        }
    }

    public void resetEditingAndSearch() {
        if (!TextUtils.isEmpty(fieldSearchContent.get())) {
            fieldSearchContent.set("");
        }
        if (repository.isEditing()) {
            initBottomControlUi();
            repository.resetEditing();
        }
    }

    private PersonListRespListener faceManagerListener = new PersonListRespListener() {

        @Override
        public void initPersonList(long personTotal) {
            fieldSearchResultVisible.set(false);
            faceTotalCount = personTotal;
            String strTotal = AppUtils.getString(R.string.face_total) + personTotal;
            strFaceTotal.set(strTotal);
            if (personTotal > 0) {
                fieldImportExportContent.set(CommonUtils.getStrFromRes(R.string.export_data));
            } else {
                fieldImportExportContent.set(CommonUtils.getStrFromRes(R.string.import_data));
            }
        }

        @Override
        public void searchPersonList(long personTotal) {
            fieldSearchResultVisible.set(true);
            String strContent = String.format(CommonUtils.getStrFromRes(R.string.search_person_count_result), personTotal);
            strFaceSearchTotal.set(strContent);
        }

        @Override
        public void firstLoadComplete(int totalPage, List<PersonInfo> mAdapterDataList) {
            navigator.refreshRecyclerView(PersonListActivity.TYPE_ADAPTER_INIT_DATA, 0, mAdapterDataList);
            if (totalPage <= 1) {
                navigator.refreshRecyclerView(PersonListActivity.TYPE_ADAPTER_LOAD_MORE_END, 0, mAdapterDataList);
            }
            if (navigator != null) {
                navigator.moveToTop();
            }
        }

        @Override
        public void loadMoreComplete(List<PersonInfo> tablePeoples) {
            navigator.refreshRecyclerView(PersonListActivity.TYPE_ADAPTER_ADD_DATA, 0, tablePeoples);
            navigator.refreshRecyclerView(PersonListActivity.TYPE_ADAPTER_LOAD_MORE_COMPLETE, 0, tablePeoples);
        }

        @Override
        public void loadMoreEnd() {
            navigator.refreshRecyclerView(PersonListActivity.TYPE_ADAPTER_LOAD_MORE_END, 0, null);
        }

        @Override
        public void onAdapterItemClick(int position) {
            navigator.refreshRecyclerView(PersonListActivity.TYPE_ADAPTER_NOTIFY_ITEM, position, null);
        }

        @Override
        public void onAdapterItemLongClick(int position) {
            navigator.setBottomDeleteEnable(false);
            rlDeleteVisible.set(true);
            ivAddFaceVisible.set(false);
            fieldImportExportVisible.set(false);
            cbSelectTitle.set(AppUtils.getString(R.string.select_all));
            cbSelectAllChecked.set(false);
        }

        @Override
        public void onAdapterItemChosen(List<PersonInfo> chosenList) {
            chosenFaceCount = chosenList.size();
            if (chosenList.size() > 0) {
                navigator.setBottomDeleteEnable(true);
            } else {
                navigator.setBottomDeleteEnable(false);
            }
        }

        @Override
        public void onAdapterItemDeleteProgress(int deletedCount, int chosenCount) {
            if (navigator != null) {
                navigator.showDeletingFaceImageProgressDialog(deletedCount, chosenCount);
            }
        }

        @Override
        public void onAdapterItemDeleteComplete() {
            navigator.refreshRecyclerView(PersonListActivity.TYPE_ADAPTER_NOTIFY_ALL, 0, null);
            if (navigator != null) {
                navigator.deleteFaceImageComplete();
                navigator.moveToTop();
            }
            initBottomControlUi();
        }

        @Override
        public void showPersonInfoDialog(int position, PersonInfo personInfo) {
            navigator.showPersonInfoDialog(position, personInfo);
        }

        @Override
        public void clearSearchStatus() {
            initFaceList();
        }

        @Override
        public void clearSearchContent() {
            if (!TextUtils.isEmpty(fieldSearchContent.get())) {
                fieldSearchContent.set("");
            }
        }

        @Override
        public void showUpgradeFaceFeatureDialog(long total) {
            navigator.showUpgradeFaceFeatureDialog(total);
        }
    };

    /**
     * 下拉加载更多
     */
    public void loadMoreListData() {
        if (repository != null) {
            repository.loadMoreListData();
        }
    }

    /**
     * 单次点击事件
     *
     * @param position
     */
    public void setAdapterItemClick(int position) {
        if (repository != null) {
            repository.setAdapterItemClick(position);
        }
    }

    /**
     * 长点击事件
     *
     * @param position
     */
    public void setAdapterItemLongClick(int position) {
        if (repository != null) {
            repository.setAdapterItemLongClick(position);
        }
    }

    /**
     * 确认删除选择的图片
     */
    public void confirmDeleteSelectedImage() {
        if (navigator != null) {
            navigator.showDeletingFaceImageProgressDialog(0, 0);
        }
        if (repository != null) {
            repository.confirmDeleteSelectImage();
        }
    }

    public void onActivityDestroyed() {
        if (repository != null) {
            repository.unUnit();
            repository = null;
        }
        faceManagerListener = null;
        navigator = null;
    }

    public void onPersonSearchTextChanged(Editable editable) {
        String content = editable.toString().trim();
        if (!TextUtils.isEmpty(content)) {
            fieldSearchContent.set(content);
        } else {
            resetSearchAndEditStatus();
        }
    }

    public void onClick(View v) {
        int resId = v.getId();
        if (DoubleClickUtils.isFastDoubleClick(resId)) {
            return;
        }
        switch (resId) {
            case R.id.btn_face_add:
                if (TextUtils.isEmpty(DeviceUtils.getMacAddress())) {
                    ToastUtils.showShortToast(R.string.device_mac_address_empty);
                    return;
                }
                if (FileUtils.getSdcardAvailableSize() < Constants.SDCARD_STORAGE_SIZE_DELETE) {
                    ToastUtils.showShortToast(R.string.device_storage_warn_tip1);
                    return;
                }
                if (navigator != null) {
                    navigator.showAddFaceDialog();
                }
                break;
            case R.id.btn_person_search:
                String tagSearch = fieldSearchContent.get();
                if (!TextUtils.isEmpty(tagSearch)) {
                    if (repository.isEditing()) {
                        initBottomControlUi();
                        repository.resetEditing();
                    }
                    repository.searchPersonsByTag(tagSearch);
                }
                break;
            case R.id.cb_select_all:
                boolean checked = !cbSelectAllChecked.get();
                cbSelectAllChecked.set(checked);
                ivAddFaceVisible.set(!checked);
                fieldImportExportVisible.set(!checked);
                cbSelectTitle.set(checked ? AppUtils.getString(R.string.cancel_select_all) : AppUtils.getString(R.string.select_all));
                navigator.setBottomDeleteEnable(checked);
                rlDeleteVisible.set(checked);
                if (repository != null) {
                    repository.setCheckBoxChanged(checked);
                }
                navigator.refreshRecyclerView(PersonListActivity.TYPE_ADAPTER_NOTIFY_ALL, 0, null);
                break;
            case R.id.tv_delete:
                String content;
                if (chosenFaceCount >= faceTotalCount) {
                    content = AppUtils.getString(R.string.face_delete_all);
                } else {
                    content = AppUtils.getString(R.string.face_delete_select) + chosenFaceCount +
                            AppUtils.getString(R.string.face_delete_select_suffix);
                }
                if (navigator != null) {
                    navigator.showDeleteConfirmDialog(content);
                }
                break;
            case R.id.btn_data_back_up:
                String tip = fieldImportExportContent.get();
                if (CommonUtils.getStrFromRes(R.string.import_data).equals(tip)) {
                    if (repository != null) {
                        repository.importPersonFromDatabase(new PersonListRepository.ImportDatabaseListener() {
                            @Override
                            public void getCount(long count) {
                                navigator.showImportDatabaseDialog(count);
                            }

                            @Override
                            public void onProgress(long total, long current) {
                                navigator.setImportDatabaseProgress(total, current);
                            }

                            @Override
                            public void onFailed() {
                                navigator.closeImportDatabaseDialog();
                            }
                        });
                    }
                } else {
                    if (repository != null) {
                        repository.exportDatabase();
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * 回退键点击事件
     */
    public void clickBackEvent(boolean fromSplash) {
        if (resetSearchAndEditStatus()) {
            navigator.closePage(fromSplash);
        }
    }

    /**
     * 重置编辑和查询状态
     *
     * @return
     */
    private boolean resetSearchAndEditStatus() {
        boolean flag1 = !TextUtils.isEmpty(fieldSearchContent.get());
        if (flag1 && !repository.searchPersonSuccess()) {
            return true;
        }
        boolean flag2 = repository.isEditing();
        if (flag1 || flag2) {
            if (flag1) {
                fieldSearchContent.set("");
                KeyboardUtils.hideSoftInput(ActivityUtils.getTopActivity());
                if (CommonUtils.isOfflineLanAppMode()) {
                    ivAddFaceVisible.set(true);
                    fieldImportExportVisible.set(true);
                }
                repository.clearSearchResult();
            }
            if (flag2) {
                initBottomControlUi();
                if (CommonUtils.isOfflineLanAppMode()) {
                    repository.setCheckBoxChanged(false);
                }
                navigator.refreshRecyclerView(PersonListActivity.TYPE_ADAPTER_NOTIFY_ALL, 0, null);
                KeyboardUtils.hideSoftInput(ActivityUtils.getTopActivity());
            }
            return false;
        }
        return true;
    }

    /**
     * 从相册选择图片
     */
    public void selectImageFromAlbum(Activity activity, int requestCode) {
        if (TextUtils.isEmpty(DeviceUtils.getMacAddress())) {
            ToastUtils.showShortToast(R.string.device_mac_address_empty);
            return;
        }
        if (FileUtils.getSdcardAvailableSize() < Constants.SDCARD_STORAGE_SIZE_DELETE) {
            ToastUtils.showShortToast(R.string.device_storage_warn_tip1);
            return;
        }
        Matisse.from(activity)
                .choose(MimeType.ofAll())
                .theme(R.style.Matisse_Zhihu)
                .countable(true)
                .capture(false)
                .captureStrategy(new CaptureStrategy(true, ConfigConstants.GALLERY_AUTHORITY))
                .maxSelectable(1)
                .gridExpectedSize(Utils.getApp().getResources().getDimensionPixelSize(R.dimen.sp_120))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR)
                .forResult(requestCode);
    }

    /**
     * 拍照选择照片
     */
    public void selectImageTakePhoto(Activity activity, int requestCode) {
        if (TextUtils.isEmpty(DeviceUtils.getMacAddress())) {
            ToastUtils.showShortToast(R.string.device_mac_address_empty);
            return;
        }
        if (FileUtils.getSdcardAvailableSize() < Constants.SDCARD_STORAGE_SIZE_DELETE) {
            ToastUtils.showShortToast(R.string.device_storage_warn_tip1);
            return;
        }
        activity.startActivityForResult(new Intent(activity, TakePhotoActivity.class), requestCode);
    }

    /**
     * 批量注册
     */
    public void batchRegisterImage() {
        if (TextUtils.isEmpty(DeviceUtils.getMacAddress())) {
            ToastUtils.showShortToast(R.string.device_mac_address_empty);
            return;
        }
        if (FileUtils.getSdcardAvailableSize() < Constants.SDCARD_STORAGE_SIZE_DELETE) {
            ToastUtils.showShortToast(R.string.device_storage_warn_tip1);
            return;
        }
        String strBatchDir = SdcardUtils.getInstance().getBatchRegisterOriDir();
        File fileDir = new File(strBatchDir);
        if (!fileDir.exists()) {
            ToastUtils.showShortToast(R.string.register_error);
            return;
        }
        if (fileDir.isDirectory()) {
            String[] fileArr = fileDir.list();
            if (fileArr.length == 0) {
                ToastUtils.showShortToast(R.string.register_error);
                return;
            }
        }
        if (navigator != null) {
            navigator.showBatchRegisterDialog();
        }
    }

    /**
     * 通过人脸引擎处理图片，并保存到本地和数据库
     *
     * @param source REQUEST_CODE_SELECT_ALBUM 从相册选择图片；REQUEST_CODE_TAKE_PHOTO 拍照选择图片
     */
    public void savePersonFaceInfo(int source, Uri uri, String filePath, String personSerial) {
        if (repository != null) {
            repository.processPictureByFaceEngine(source, uri, filePath, personSerial, faceExtractResult -> {
                int toastRes = -1;
                int res = faceExtractResult.getResult();
                switch (res) {
                    case BusinessErrorCode.BEC_COMMON_OK:
                        if (navigator != null) {
                            navigator.showRegisterFaceNameDialog(personSerial, faceExtractResult.getFaceInfo());
                        }
                        break;
                    case BusinessErrorCode.BEC_COMMON_UNKNOWN:
                        toastRes = R.string.face_manager_tip_common_fail;
                        break;
                    case BusinessErrorCode.BEC_FACE_MANAGER_DETECT_FAIL:
                    case BusinessErrorCode.BEC_FACE_MANAGER_NO_FACE:
                        toastRes = R.string.face_manager_tip_detect_no_face_image;
                        break;
                    case BusinessErrorCode.BEC_FACE_MANAGER_MORE_THAN_ONE_FACE:
                        toastRes = R.string.face_manager_tip_more_than_one_face;
                        break;
                    case BusinessErrorCode.BEC_FACE_MANAGER_DEGREE_BIG:
                        toastRes = R.string.face_manager_tip_degree_big_image;
                        break;
                    case BusinessErrorCode.BEC_FACE_MANAGER_IMAGE_INVALID:
                        toastRes = R.string.setting_image_invalid;
                        break;
                    case BusinessErrorCode.BEC_FACE_MANAGER_PERSON_SAVE_FAILED:
                        toastRes = R.string.face_manager_person_add_failed;
                        break;
                    case BusinessErrorCode.BEC_FACE_MANAGER_FACE_QUALITY_FAIL:
                        toastRes = R.string.face_quality_fail;
                        break;
                    case BusinessErrorCode.BEC_FACE_MANAGER_RECOGNIZE_FAIL:
                        toastRes = R.string.face_manager_tip_recognize_fail;
                        break;
                    default:
                        break;
                }
                if (toastRes != -1) {
                    ToastUtils.showLongToast(toastRes);
                }
            });
        }
    }

    /**
     * 注册人脸到本地数据库
     */
    public void savePersonToDatabase(String name, String strPersonId, String personSerial, FaceInfo faceInfo) {
        if (repository != null) {
            repository.savePersonToDatabase(name, strPersonId, personSerial, faceInfo, faceExtractResult -> {
                int toastRes = -1;
                int res = faceExtractResult.getResult();
                switch (res) {
                    case BusinessErrorCode.BEC_COMMON_OK:
                        toastRes = R.string.face_manager_tip_register_success;
                        break;
                    case BusinessErrorCode.BEC_FACE_MANAGER_PERSON_SAVE_FAILED:
                        toastRes = R.string.face_manager_person_add_failed;
                        break;
                    case BusinessErrorCode.BEC_FACE_MANAGER_SAVE_BITMAP_FAILED:
                        toastRes = R.string.face_manager_person_save_bitmap_failed;
                        break;
                    case BusinessErrorCode.BEC_FACE_MANAGER_ADD_FACE_FAILED:
                        toastRes = R.string.face_manager_person_add_face_failed;
                        break;
                    case BusinessErrorCode.BEC_COMMON_UNKNOWN:
                        toastRes = R.string.face_manager_tip_common_fail;
                        break;
                    default:
                        break;
                }
                if (toastRes != -1) {
                    ToastUtils.showLongToast(toastRes);
                }
            });
        }
    }

    /**
     * 取消人脸注册
     */
    public void cancelRegisterFace() {
        if (repository != null) {
            repository.cancelRegister();
        }
    }

    public void saveSelectImage(String personName, String personId) {
        if (repository != null) {
            repository.saveSelectImage(personName, personId);
        }
    }
}
