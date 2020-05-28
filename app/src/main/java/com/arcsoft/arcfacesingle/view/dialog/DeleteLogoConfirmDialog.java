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

import androidx.annotation.Nullable;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.asg.libcommon.base.BaseDialogFragment;
import com.arcsoft.asg.libcommon.base.BaseViewHolder;
import com.arcsoft.asg.libcommon.util.common.ScreenUtils;

import me.jessyan.autosize.AutoSize;

public class DeleteLogoConfirmDialog extends BaseDialogFragment {

    private String strContent;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (null != bundle) {
            strContent = bundle.getString("content");
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
        return R.layout.dialog_delete_logo_confirm;
    }

    @Override
    protected void convertView(BaseViewHolder holder, BaseDialogFragment baseDialog) {
        super.convertView(holder, baseDialog);
        holder.setText(R.id.tv_delete_logo_content, strContent);
    }
}