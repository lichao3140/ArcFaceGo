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

import androidx.annotation.Nullable;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.data.event.DeviceResultDialogEvent;
import com.arcsoft.asg.libcommon.base.BaseDialogFragment;
import com.arcsoft.asg.libcommon.base.BaseViewHolder;
import com.arcsoft.asg.libcommon.util.business.DoubleClickUtils;
import com.arcsoft.asg.libcommon.util.common.ScreenUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import me.jessyan.autosize.AutoSize;

public class ActiveResultDialog extends BaseDialogFragment implements View.OnClickListener {

    public static final String BUNDLE_KEY_CONTENT = "BUNDLE_KEY_CONTENT";
    public static final String BUNDLE_KEY_SUCCESS = "BUNDLE_KEY_SUCCESS";
    public static final String BUNDLE_KEY_BTN_CONFIRM_ENABLE = "BUNDLE_KEY_BTN_CONFIRM_ENABLE";

    private String strContent;
    private boolean successful;
    private Button btnConfirm;
    private boolean btnConfirmEnable;

    @Override
    protected boolean useEventBus() {
        return true;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (null != bundle) {
            strContent = bundle.getString(BUNDLE_KEY_CONTENT);
            successful = bundle.getBoolean(BUNDLE_KEY_SUCCESS);
            btnConfirmEnable = bundle.getBoolean(BUNDLE_KEY_BTN_CONFIRM_ENABLE);
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
        return R.layout.dialog_active_result;
    }

    @Override
    protected void convertView(BaseViewHolder holder, BaseDialogFragment baseDialog) {
        super.convertView(holder,baseDialog);
        holder.setText(R.id.tv_active_result_content, strContent);
        btnConfirm = holder.getView(R.id.btn_active_result_confirm);
        btnConfirm.setEnabled(btnConfirmEnable);
        if (successful) {
            holder.setBackgroundResource(R.id.iv_common_dialog_warn, R.mipmap.ic_device_active_success);
        } else {
            holder.setBackgroundResource(R.id.iv_common_dialog_warn, R.mipmap.ic_warn);
        }
    }

    public void setBtnConfirmEnable(boolean enable) {
        if (btnConfirm != null) {
            btnConfirm.setEnabled(enable);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshBtnStatus(DeviceResultDialogEvent event) {
        if (null != event) {
            setBtnConfirmEnable(event.btnConfirmEnable);
        }
    }

    @Override
    public void onClick(View v) {
        if (DoubleClickUtils.isFastDoubleClick(v.getId())) {
            return;
        }
        dismissAllowingStateLoss();
    }
}
