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

public class CommonTipDialog extends BaseDialogFragment {

    private String strContent;
    private String strConfirm;
    private String strCancel;
    private boolean hideBtnConfirm;
    private boolean hideBtnCancel;
    private boolean hideIvCancel = true;
    private boolean hideIvWarn;

    public static CommonTipDialog getInstance(String content, String strConfirm, String strCancel, boolean hideBtnCancel,
                                              boolean hideBtnConfirm) {
        CommonTipDialog dialog = new CommonTipDialog();
        Bundle bundle = new Bundle();
        bundle.putString("content", content);
        bundle.putString("strConfirm", strConfirm);
        bundle.putString("strCancel", strCancel);
        bundle.putBoolean("hideBtnConfirm", hideBtnConfirm);
        bundle.putBoolean("hideBtnCancel", hideBtnCancel);
        dialog.setArguments(bundle);
        return dialog;
    }

    public static CommonTipDialog getInstance(String content, String strConfirm, String strCancel, boolean hideBtnCancel,
                                              boolean hideBtnConfirm, boolean hideIvCancel) {
        CommonTipDialog dialog = new CommonTipDialog();
        Bundle bundle = new Bundle();
        bundle.putString("content", content);
        bundle.putString("strConfirm", strConfirm);
        bundle.putString("strCancel", strCancel);
        bundle.putBoolean("hideBtnConfirm", hideBtnConfirm);
        bundle.putBoolean("hideBtnCancel", hideBtnCancel);
        bundle.putBoolean("hideIvCancel", hideIvCancel);
        dialog.setArguments(bundle);
        return dialog;
    }

    public static CommonTipDialog getInstance(String content, String strConfirm, String strCancel, boolean hideBtnCancel,
                                              boolean hideBtnConfirm, boolean hideIvCancel, boolean hideIvWarn) {
        CommonTipDialog dialog = new CommonTipDialog();
        Bundle bundle = new Bundle();
        bundle.putString("content", content);
        bundle.putString("strConfirm", strConfirm);
        bundle.putString("strCancel", strCancel);
        bundle.putBoolean("hideBtnConfirm", hideBtnConfirm);
        bundle.putBoolean("hideBtnCancel", hideBtnCancel);
        bundle.putBoolean("hideIvCancel", hideIvCancel);
        bundle.putBoolean("hideIvWarn", hideIvWarn);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (null != bundle) {
            strContent = bundle.getString("content");
            strConfirm = bundle.getString("strConfirm");
            strCancel = bundle.getString("strCancel");
            hideBtnConfirm = bundle.getBoolean("hideBtnConfirm");
            hideBtnCancel = bundle.getBoolean("hideBtnCancel");
            hideIvCancel = bundle.getBoolean("hideIvCancel");
            hideIvWarn = bundle.getBoolean("hideIvWarn");
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
        return R.layout.dialog_common_tip;
    }

    @Override
    protected void convertView(BaseViewHolder holder, BaseDialogFragment baseDialog) {
        super.convertView(holder, baseDialog);

        ImageView ivCancel = holder.getView(R.id.iv_common_dialog_cancel);
        ivCancel.setVisibility(hideIvCancel ? View.GONE : View.VISIBLE);

        Button btnConfirm = holder.getView(R.id.btn_common_dialog_confirm);
        btnConfirm.setVisibility(hideBtnConfirm ? View.GONE : View.VISIBLE);
        btnConfirm.setText(strConfirm);

        Button btnCancel = holder.getView(R.id.btn_common_dialog_cancel);
        btnCancel.setVisibility(hideBtnCancel ? View.GONE : View.VISIBLE);
        btnCancel.setText(strCancel);

        ImageView ivWarn = holder.getView(R.id.iv_common_dialog_warn);
        ivWarn.setVisibility(hideIvWarn ? View.GONE : View.VISIBLE);

        holder.setText(R.id.tv_common_dialog_warning_content, strContent);
    }
}
