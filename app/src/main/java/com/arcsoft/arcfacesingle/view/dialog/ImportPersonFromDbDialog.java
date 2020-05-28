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

import android.widget.Button;
import android.widget.TextView;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.view.widgets.HorizontalProgressBar;
import com.arcsoft.asg.libcommon.base.BaseDialogFragment;
import com.arcsoft.asg.libcommon.base.BaseViewHolder;
import com.arcsoft.asg.libcommon.util.common.ArithmeticUtils;
import com.arcsoft.asg.libcommon.util.common.ScreenUtils;

import me.jessyan.autosize.AutoSize;

public class ImportPersonFromDbDialog extends BaseDialogFragment {

    public static final int COUNT_TOTAL = 100;

    private TextView tvProgressValue;
    private Button btConfirm;
    private HorizontalProgressBar progressBar;

    @Override
    protected int setUpLayoutId() {
        if (null != getActivity()) {
            if (ScreenUtils.isPortrait()) {
                AutoSize.autoConvertDensity(getActivity(), 720, true);
            } else {
                AutoSize.autoConvertDensity(getActivity(), 1280, true);
            }
        }
        return R.layout.dialog_import_data_base_progress;
    }

    @Override
    protected void convertView(BaseViewHolder holder, BaseDialogFragment baseDialog) {
        super.convertView(holder, baseDialog);
        tvProgressValue = holder.getView(R.id.tv_progress_value);
        progressBar = holder.getView(R.id.progress_horizontal);
        btConfirm = holder.getView(R.id.btn_dialog_confirm);
    }

    public void setProgressCurrent(long total, long current) {
        double div = ArithmeticUtils.div(current, total, 3);
        double mul = ArithmeticUtils.mul(div, 100, 1);
        int progress = (int) mul;
        if (progressBar != null) {
            progressBar.setProgressBar(progress);
        }
        String strProgress = progress + "%";
        if (tvProgressValue != null) {
            tvProgressValue.setText(strProgress);
        }
        if (progress >= COUNT_TOTAL) {
            if (btConfirm != null) {
                btConfirm.setEnabled(true);
            }
        }
    }
}
