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

import android.widget.EditText;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.asg.libcommon.base.BaseDialogFragment;
import com.arcsoft.asg.libcommon.base.BaseViewHolder;
import com.arcsoft.asg.libcommon.util.common.ScreenUtils;

import me.jessyan.autosize.AutoSize;

public class RegisterFaceNameDialog extends BaseDialogFragment {

    @Override
    protected int setUpLayoutId() {
        if (null != getActivity()) {
            if (ScreenUtils.isPortrait()) {
                AutoSize.autoConvertDensity(getActivity(), 720, true);
            } else {
                AutoSize.autoConvertDensity(getActivity(), 1280, true);
            }
        }
        return R.layout.dialog_register_face_input_name;
    }

    @Override
    protected void convertView(BaseViewHolder holder, BaseDialogFragment baseDialog) {
        super.convertView(holder, baseDialog);
        EditText etPersonName = holder.getView(R.id.et_input_face_name);
        EditText etPersonNo = holder.getView(R.id.et_input_face_id);
        CommonUtils.setEditTextInputFilter(etPersonName);
        CommonUtils.setEditTextInputFilter(etPersonNo);
    }
}
