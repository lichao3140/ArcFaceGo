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

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.asg.libcommon.base.BaseDialogFragment;
import com.arcsoft.asg.libcommon.base.BaseViewHolder;
import com.arcsoft.asg.libcommon.util.common.ScreenUtils;

import me.jessyan.autosize.AutoSize;

public class CommonWarnTitleDialog extends BaseDialogFragment {

    private static final String BUNDLE_KEY_STR_TITLE = "BUNDLE_KEY_STR_TITLE";
    private static final String BUNDLE_KEY_STR_CONNTENT= "BUNDLE_KEY_STR_CONNTENT";
    private static final String BUNDLE_KEY_STR_CONFIRM = "BUNDLE_KEY_STR_CONFIRM";
    private static final String BUNDLE_KEY_STR_CANCEL = "BUNDLE_KEY_STR_CANCEL";
    private static final String BUNDLE_KEY_HIDE_BTN_CONFIRM = "BUNDLE_KEY_HIDE_BTN_CONFIRM";
    private static final String BUNDLE_KEY_HIDE_BTN_CANCEL = "BUNDLE_KEY_HIDE_BTN_CANCEL";
    private static final String BUNDLE_KEY_HIDE_IV_CANCEL = "BUNDLE_KEY_HIDE_IV_CANCEL";

    private String strTitle;
    private String strContent;
    private String strConfirm;
    private String strCancel;
    private boolean hideBtnConfirm;
    private boolean hideBtnCancel;
    private boolean hideIvCancel = true;

    public static CommonWarnTitleDialog getInstance(String title, String content, String strConfirm, String strCancel,
                                                    boolean hideBtnCancel, boolean hideBtnConfirm, boolean hideIvCancel) {
        CommonWarnTitleDialog dialog = new CommonWarnTitleDialog();
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY_STR_TITLE, title);
        bundle.putString(BUNDLE_KEY_STR_CONNTENT, content);
        bundle.putString(BUNDLE_KEY_STR_CONFIRM, strConfirm);
        bundle.putString(BUNDLE_KEY_STR_CANCEL, strCancel);
        bundle.putBoolean(BUNDLE_KEY_HIDE_BTN_CONFIRM, hideBtnConfirm);
        bundle.putBoolean(BUNDLE_KEY_HIDE_BTN_CANCEL, hideBtnCancel);
        bundle.putBoolean(BUNDLE_KEY_HIDE_IV_CANCEL, hideIvCancel);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (null != bundle) {
            strTitle = bundle.getString(BUNDLE_KEY_STR_TITLE);
            strContent = bundle.getString(BUNDLE_KEY_STR_CONNTENT);
            strConfirm = bundle.getString(BUNDLE_KEY_STR_CONFIRM);
            strCancel = bundle.getString(BUNDLE_KEY_STR_CANCEL);
            hideBtnConfirm = bundle.getBoolean(BUNDLE_KEY_HIDE_BTN_CONFIRM);
            hideBtnCancel = bundle.getBoolean(BUNDLE_KEY_HIDE_BTN_CANCEL);
            hideIvCancel = bundle.getBoolean(BUNDLE_KEY_HIDE_IV_CANCEL);
        }
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
        return R.layout.dialog_common_warn_title;
    }

    @Override
    protected void convertView(BaseViewHolder holder, BaseDialogFragment baseDialog) {
        super.convertView(holder, baseDialog);

        holder.setText(R.id.tv_dialog_title, strTitle);

        ImageView ivCancel = holder.getView(R.id.iv_common_dialog_cancel);
        ivCancel.setVisibility(hideIvCancel ? View.GONE : View.VISIBLE);

        holder.setText(R.id.tv_common_dialog_warning_content, strContent);

        Button btnConfirm = holder.getView(R.id.btn_common_dialog_confirm);
        btnConfirm.setVisibility(hideBtnConfirm ? View.GONE : View.VISIBLE);
        btnConfirm.setText(strConfirm);

        Button btnCancel = holder.getView(R.id.btn_common_dialog_cancel);
        btnCancel.setVisibility(hideBtnCancel ? View.GONE : View.VISIBLE);
        btnCancel.setText(strCancel);
    }
}
