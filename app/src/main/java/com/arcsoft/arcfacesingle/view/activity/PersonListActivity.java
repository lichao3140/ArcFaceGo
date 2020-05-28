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

package com.arcsoft.arcfacesingle.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.base.BaseBusinessActivity;
import com.arcsoft.arcfacesingle.business.personlist.adapter.PersonListAdapter;
import com.arcsoft.arcfacesingle.data.event.RefreshAdapterEvent;
import com.arcsoft.arcfacesingle.data.event.UsbEnableEvent;
import com.arcsoft.arcfacesingle.data.model.PersonInfo;
import com.arcsoft.arcfacesingle.databinding.ActivityPersonListBinding;
import com.arcsoft.arcfacesingle.navigator.IPersonListNavigator;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.arcfacesingle.util.glide.GlideApp;
import com.arcsoft.arcfacesingle.view.dialog.AllowDeleteFaceDialog;
import com.arcsoft.arcfacesingle.view.dialog.BatchRegisterDialog;
import com.arcsoft.arcfacesingle.view.dialog.CommonTipDialog;
import com.arcsoft.arcfacesingle.view.dialog.DeleteSelectFaceDialog;
import com.arcsoft.arcfacesingle.view.dialog.ImportPersonFromDbDialog;
import com.arcsoft.arcfacesingle.view.dialog.PersonRegisterMethodSelectDialog;
import com.arcsoft.arcfacesingle.view.dialog.RegisterFaceNameDialog;
import com.arcsoft.arcfacesingle.view.dialog.UpgradeFaceFeatureDialog;
import com.arcsoft.arcfacesingle.view.dialog.UsbBatchRegisterDialog;
import com.arcsoft.arcfacesingle.viewmodel.PersonListViewModel;
import com.arcsoft.asg.libcommon.util.business.DoubleClickUtils;
import com.arcsoft.asg.libcommon.util.common.ActivityUtils;
import com.arcsoft.asg.libcommon.util.common.KeyboardUtils;
import com.arcsoft.asg.libcommon.util.common.ScreenUtils;
import com.arcsoft.asg.libcommon.util.common.ToastUtils;
import com.arcsoft.asg.libcommon.util.common.Utils;
import com.arcsoft.faceengine.FaceInfo;
import com.zhihu.matisse.Matisse;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class PersonListActivity extends BaseBusinessActivity implements IPersonListNavigator {

    private static final String TAG = PersonListActivity.class.getSimpleName();
    public static final int REQUEST_CODE_SELECT_ALBUM = 1;
    public static final int REQUEST_CODE_TAKE_PHOTO = 2;
    public static final int REQUEST_CODE_START_ACTIVITY = 3;
    public static final int MESSAGE_NAME_MAX_LENGTH = 30;
    public static final int TYPE_ADAPTER_INIT_DATA = 0;
    public static final int TYPE_ADAPTER_ADD_DATA = 1;
    public static final int TYPE_ADAPTER_LOAD_MORE_COMPLETE = 2;
    public static final int TYPE_ADAPTER_LOAD_MORE_END = 3;
    public static final int TYPE_ADAPTER_NOTIFY_ALL = 4;
    public static final int TYPE_ADAPTER_NOTIFY_ITEM = 5;

    private PersonListViewModel viewModel;
    private AllowDeleteFaceDialog allowDeleteFaceDialog;
    private ActivityPersonListBinding dataBinding;
    private PersonListAdapter personListAdapter;
    private DeleteSelectFaceDialog deleteSelectFaceDialog;
    private ImportPersonFromDbDialog importPersonFromDbDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        boolean fromSplash = intent.getBooleanExtra(Constants.SP_KEY_FROM_SPLASH, false);

        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_person_list);
        viewModel = new PersonListViewModel();
        dataBinding.setViewModel(viewModel);
        viewModel.setNavigator(this);
        viewModel.init();
        initView(fromSplash);
        if (fromSplash) {
            viewModel.upgradeFaceFeature();
        }
        viewModel.initFaceList();
    }

    private void initView(boolean fromSplash) {
        RecyclerView recyclerView = dataBinding.rvFaces;
        List<PersonInfo> mAdapterDataList = new ArrayList<>();
        personListAdapter = new PersonListAdapter(mAdapterDataList);
        GridLayoutManager gridManager = new GridLayoutManager(this, ScreenUtils.isPortrait() ? 3 : 5);
        recyclerView.setLayoutManager(gridManager);
        recyclerView.setAdapter(personListAdapter);
        setRecyclerViewAdapter(recyclerView);

        dataBinding.rlDelete.setOnTouchListener((view, motionEvent) -> {
            return true;
        });
        dataBinding.customTopBar.setVisibleClose(true);
        dataBinding.customTopBar.setBackOnClickListener(() -> {
            viewModel.clickBackEvent(fromSplash);
        });
    }

    private void setRecyclerViewAdapter(RecyclerView recyclerView) {
        personListAdapter.setOnItemClickListener(((adapter, view, position) -> {
            if (DoubleClickUtils.isFastDoubleClick(view.getId())) {
                return;
            }
            if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                if (viewModel != null) {
                    viewModel.setAdapterItemClick(position);
                }
            }
        }));
        personListAdapter.setOnItemLongClickListener((adapter, view, position) -> {
            if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                if (viewModel != null && isOfflineLanAppMode()) {
                    viewModel.setAdapterItemLongClick(position);
                }
            }
            return false;
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (!isFinishing()) {
                        GlideApp.with(PersonListActivity.this).resumeRequests();
                    }
                } else {
                    GlideApp.with(PersonListActivity.this).pauseRequests();
                }
            }
        });
        personListAdapter.setOnLoadMoreListener(() -> {
            if (viewModel != null) {
                viewModel.loadMoreListData();
            }
        }, recyclerView);
    }

    @Override
    public void refreshRecyclerView(int type, int position, List<PersonInfo> personInfoList) {
        switch (type) {
            case TYPE_ADAPTER_INIT_DATA:
                if (personListAdapter != null) {
                    personListAdapter.setNewData(personInfoList);
                }
                break;
            case TYPE_ADAPTER_ADD_DATA:
                if (personListAdapter != null) {
                    personListAdapter.addData(personInfoList);
                }
                break;
            case TYPE_ADAPTER_LOAD_MORE_COMPLETE:
                if (personListAdapter != null) {
                    personListAdapter.loadMoreComplete();
                }
                break;
            case TYPE_ADAPTER_LOAD_MORE_END:
                if (personListAdapter != null) {
                    personListAdapter.loadMoreEnd(true);
                }
                break;
            case TYPE_ADAPTER_NOTIFY_ALL:
                if (personListAdapter != null) {
                    personListAdapter.notifyDataSetChanged();
                }
                break;
            case TYPE_ADAPTER_NOTIFY_ITEM:
                if (personListAdapter != null) {
                    personListAdapter.notifyItemChanged(position);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void setBottomDeleteEnable(boolean enable) {
        dataBinding.tvDelete.setEnabled(enable);
    }

    @Override
    public void moveToTop() {
        dataBinding.rvFaces.scrollToPosition(0);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshAdapter(RefreshAdapterEvent event) {
        if (null != event) {
            if (deleteSelectFaceDialog != null) {
                deleteSelectFaceDialog.dismissAllowingStateLoss();
                deleteSelectFaceDialog = null;
            }
            viewModel.resetEditingAndSearch();
            viewModel.initFaceList();
        }
    }

    @Override
    protected void onDestroy() {
        if (viewModel != null) {
            viewModel.onActivityDestroyed();
        }
        viewModel = null;
        allowDeleteFaceDialog = null;
        dataBinding = null;
        personListAdapter = null;
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        GlideApp.with(this).pauseAllRequests();
    }

    @Override
    protected void onResume() {
        super.onResume();
        GlideApp.with(this).resumeRequests();
    }

    @Override
    public void showAddFaceDialog() {
        PersonRegisterMethodSelectDialog dialog = new PersonRegisterMethodSelectDialog();
        dialog.setDialogSize((int) getResources().getDimension(R.dimen.x550),
                (int) getResources().getDimension(R.dimen.y720))
                .setOutCancel(false)
                .setConvertViewListener(((holder, baseDialog) -> {
                    holder.getView(R.id.tv_choose_photo).setOnClickListener(v -> {
                        if (viewModel != null) {
                            viewModel.selectImageFromAlbum(PersonListActivity.this, REQUEST_CODE_SELECT_ALBUM);
                            dialog.releaseUsb();
                            baseDialog.dismissAllowingStateLoss();
                        }
                    });
                    holder.getView(R.id.tv_take_photo).setOnClickListener(v -> {
                        if (viewModel != null) {
                            viewModel.selectImageTakePhoto(PersonListActivity.this, REQUEST_CODE_START_ACTIVITY);
                            dialog.releaseUsb();
                            baseDialog.dismissAllowingStateLoss();
                        }
                    });
                    holder.getView(R.id.tv_batch_register).setOnClickListener(v -> {
                        if (viewModel != null) {
                            viewModel.batchRegisterImage();
                            dialog.releaseUsb();
                            baseDialog.dismissAllowingStateLoss();
                        }
                    });
                    holder.getView(R.id.tv_usb_register).setOnClickListener(v -> {
                        dialog.usbRegisterFace();
                    });
                    holder.getView(R.id.btn_dialog_cancel).setOnClickListener(v -> {
                        dialog.releaseUsb();
                        baseDialog.dismissAllowingStateLoss();
                    });
                }))
                .show(getSupportFragmentManager());
        dialog.setAddFaceInfoListener(new PersonRegisterMethodSelectDialog.AddFaceInfoListener() {

            @Override
            public void onRegisterCheckFailed(String msg) {
                showUsbMessageDialog(msg);
            }

            @Override
            public void startRegisterFace() {
                dialog.releaseUsb();
                dialog.dismissAllowingStateLoss();
                showUsbBatchRegisterDialog();
            }

            @Override
            public void removeUsb(UsbDevice usbDevice) {
                EventBus.getDefault().post(new UsbEnableEvent(false));
            }
        });
    }

    private void showUsbMessageDialog(String msg) {
        CommonTipDialog dialog = CommonTipDialog
                .getInstance(msg,
                        Utils.getApp().getResources().getString(R.string.confirm),
                        "",
                        true,
                        false,
                        true);
        dialog.setDialogSize((int) getResources().getDimension(R.dimen.x550),
                (int) getResources().getDimension(R.dimen.x370))
                .setOutCancel(false)
                .setConvertViewListener(((holder, baseDialog) -> {
                    holder.getView(R.id.btn_common_dialog_confirm).setOnClickListener(v -> {
                        baseDialog.dismissAllowingStateLoss();
                    });
                }))
                .show(getSupportFragmentManager());
    }

    @Override
    public void showBatchRegisterDialog() {
        BatchRegisterDialog registerDialog = new BatchRegisterDialog();
        registerDialog.setDialogSize((int) getResources().getDimension(R.dimen.x520),
                (int) getResources().getDimension(R.dimen.x320))
                .setOutCancel(false)
                .setConvertViewListener((holder, baseDialog) -> {
                            holder.getView(R.id.btn_batch_register_confirm).setOnClickListener(v -> {
                                registerDialog.release();
                                if (viewModel != null) {
                                    viewModel.initFaceList();
                                }
                                registerDialog.dismissAllowingStateLoss();
                            });
                        }
                )
                .show(getSupportFragmentManager());
    }

    @Override
    public void showUsbBatchRegisterDialog() {
        UsbBatchRegisterDialog registerDialog = new UsbBatchRegisterDialog();
        registerDialog.setDialogSize((int) getResources().getDimension(R.dimen.x520),
                (int) getResources().getDimension(R.dimen.x320))
                .setOutCancel(false)
                .setConvertViewListener((holder, baseDialog) -> {
                            holder.getView(R.id.btn_batch_register_confirm).setOnClickListener(v -> {
                                registerDialog.release();
                                if (viewModel != null) {
                                    viewModel.initFaceList();
                                }
                                registerDialog.dismissAllowingStateLoss();
                            });
                        }
                )
                .show(getSupportFragmentManager());
    }

    @Override
    public void showRegisterFaceNameDialog(String personSerial, FaceInfo faceInfo) {
        switchAutoSizeDp();
        RegisterFaceNameDialog dialog = new RegisterFaceNameDialog();
        dialog.setDialogSize((int) getResources().getDimension(R.dimen.x550),
                (int) getResources().getDimension(R.dimen.x500))
                .setOutCancel(false)
                .setConvertViewListener(((holder, baseDialog) -> {
                    EditText etFaceName = holder.getView(R.id.et_input_face_name);
                    EditText etFaceId = holder.getView(R.id.et_input_face_id);
                    holder.setOnClickListener(R.id.btn_input_face_name_confirm, v -> {
                        String strName = etFaceName.getText().toString().trim();
                        if (TextUtils.isEmpty(strName)) {
                            ToastUtils.showShortToast(R.string.input_register_person_name);
                        } else {
                            if (strName.length() > MESSAGE_NAME_MAX_LENGTH) {
                                ToastUtils.showShortToast(R.string.register_face_name_length_invalid);
                            } else {
                                String strPersonId = etFaceId.getText().toString().trim();
                                if (!TextUtils.isEmpty(strPersonId) && strPersonId.length() > MESSAGE_NAME_MAX_LENGTH) {
                                    ToastUtils.showShortToast(R.string.register_face_id_length_invalid);
                                } else {
                                    if (viewModel != null) {
                                        viewModel.saveSelectImage(strName, strPersonId);
                                        viewModel.savePersonToDatabase(strName, strPersonId, personSerial, faceInfo);
                                    }
                                    KeyboardUtils.hideSoftInput(this);
                                    baseDialog.dismissAllowingStateLoss();
                                }
                            }
                        }
                    });
                    holder.setOnClickListener(R.id.iv_register_name_cancel, v -> {
                        if (viewModel != null) {
                            viewModel.cancelRegisterFace();
                        }
                        KeyboardUtils.hideSoftInput(this);
                        baseDialog.dismissAllowingStateLoss();
                    });
                }))
                .show(getSupportFragmentManager());
    }

    @Override
    public void showDeleteConfirmDialog(String strContent) {
        if (deleteSelectFaceDialog == null) {
            deleteSelectFaceDialog = new DeleteSelectFaceDialog();
            Bundle bundle = new Bundle();
            bundle.putString("content", strContent);
            deleteSelectFaceDialog.setArguments(bundle);
            deleteSelectFaceDialog.setDialogSize((int) getResources().getDimension(R.dimen.x550),
                    (int) getResources().getDimension(R.dimen.x350))
                    .setOutCancel(false)
                    .setConvertViewListener(((holder, baseDialog) -> {
                        Button btnConfirm = holder.getView(R.id.btn_delete_face_confirm);
                        btnConfirm.setOnClickListener(v -> {
                            deleteSelectFaceDialog.dismissAllowingStateLoss();
                            if (viewModel != null) {
                                viewModel.confirmDeleteSelectedImage();
                            }
                            deleteSelectFaceDialog = null;
                        });
                        holder.getView(R.id.btn_delete_face_cancel).setOnClickListener(v -> {
                            deleteSelectFaceDialog.dismissAllowingStateLoss();
                            deleteSelectFaceDialog = null;
                        });
                    }))
                    .show(getSupportFragmentManager());
        }
    }

    @Override
    public void showDeletingFaceImageProgressDialog(int deletedCount, int chosenCount) {
        if (allowDeleteFaceDialog == null) {
            allowDeleteFaceDialog = new AllowDeleteFaceDialog();
            allowDeleteFaceDialog.setDialogSize((int) getResources().getDimension(R.dimen.x550),
                    (int) getResources().getDimension(R.dimen.x350))
                    .setOutCancel(false)
                    .show(getSupportFragmentManager());
        } else {
            if (chosenCount > 0) {
                if (chosenCount != deletedCount) {
                    String content = CommonUtils.getStrFromRes(R.string.delete_face_result, deletedCount, chosenCount);
                    allowDeleteFaceDialog.setStrContent(content);
                }
            }
        }
    }

    @Override
    public void deleteFaceImageComplete() {
        String content = getResources().getString(R.string.delete_faces_complete);
        allowDeleteFaceDialog.setStrContent(content);
        allowDeleteFaceDialog.setBtnConfirm(true);
        allowDeleteFaceDialog = null;
    }

    @Override
    public void showPersonInfoDialog(int position, PersonInfo personInfo) {
        Intent intent = new Intent(this, PersonDetailActivity.class);
        intent.putExtra(PersonDetailActivity.STRING_PERSON_MODEL, personInfo);
        startActivity(intent);
    }

    @Override
    public void showImportDatabaseDialog(long total) {
        closeImportDatabaseDialog();
        if (importPersonFromDbDialog == null) {
            importPersonFromDbDialog = new ImportPersonFromDbDialog();
            importPersonFromDbDialog.setDialogSize((int) getResources().getDimension(R.dimen.x550),
                    (int) getResources().getDimension(R.dimen.x400))
                    .setOutCancel(false)
                    .setConvertViewListener(((holder, baseDialog) -> {
                        holder.getView(R.id.btn_dialog_confirm).setOnClickListener(v -> {
                            if (viewModel != null) {
                                viewModel.initFaceList();
                            }
                            closeImportDatabaseDialog();
                        });
                    }))
                    .show(getSupportFragmentManager());
        }
    }

    @Override
    public void setImportDatabaseProgress(long total, long current) {
        if (importPersonFromDbDialog != null) {
            importPersonFromDbDialog.setProgressCurrent(total, current);
        }
    }

    @Override
    public void closeImportDatabaseDialog() {
        if (importPersonFromDbDialog != null) {
            importPersonFromDbDialog.dismissAllowingStateLoss();
            importPersonFromDbDialog = null;
        }
    }

    @Override
    public void showUpgradeFaceFeatureDialog(long total) {
        UpgradeFaceFeatureDialog registerDialog = new UpgradeFaceFeatureDialog();
        registerDialog.setDialogSize((int) getResources().getDimension(R.dimen.x520),
                (int) getResources().getDimension(R.dimen.x320))
                .setOutCancel(false)
                .setConvertViewListener((holder, baseDialog) -> {
                            holder.getView(R.id.btn_batch_register_confirm).setOnClickListener(v -> {
                                registerDialog.release();
                                registerDialog.dismissAllowingStateLoss();
                                Intent intent = new Intent(PersonListActivity.this, RecognizeActivity.class);
                                startActivity(intent);
                                ActivityUtils.finishActivity(PersonListActivity.this);
                            });
                        }
                )
                .show(getSupportFragmentManager());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_SELECT_ALBUM:
                    ArrayList<Uri> mUris = new ArrayList<>(Matisse.obtainResult(data));
                    if (mUris.size() > 0) {
                        if (viewModel != null) {
                            viewModel.savePersonFaceInfo(REQUEST_CODE_SELECT_ALBUM, mUris.get(0), null, null);
                        }
                    } else {
                        ToastUtils.showShortToast(R.string.setting_image_invalid);
                    }
                    break;
                case REQUEST_CODE_START_ACTIVITY:
                    String imagePath = data.getStringExtra(TakePhotoActivity.KEY_TAKE_PHOTO_IMAGE_PATH);
                    String personSerial = data.getStringExtra(TakePhotoActivity.KEY_TAKE_PHOTO_PERSON_SERIAL);
                    if (viewModel != null) {
                        viewModel.savePersonFaceInfo(REQUEST_CODE_TAKE_PHOTO, null, imagePath, personSerial);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void closePage(boolean fromSplash) {
        if (fromSplash) {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
        } else {
            ActivityUtils.finishActivity(this);
        }
    }
}
