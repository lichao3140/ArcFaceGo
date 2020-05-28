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

package com.arcsoft.arcfacesingle.view.widgets;

import android.content.Context;
import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.business.common.CommonRepository;
import com.arcsoft.arcfacesingle.business.recognize.RecognizeRepository;
import com.arcsoft.arcfacesingle.business.setting.ConfigConstants;
import com.arcsoft.arcfacesingle.data.db.table.TableSettingConfigInfo;
import com.arcsoft.arcfacesingle.databinding.LayoutRecognizeBottomViewBinding;
import com.arcsoft.arcfacesingle.viewmodel.widget.BottomViewModel;

public class RecognizeBottomView extends RelativeLayout {

    private BottomViewModel viewModel;
    private LayoutRecognizeBottomViewBinding dataBinding;

    @BindingAdapter("bottomLogo")
    public static void bindBottomLogo(RecognizeBottomView bottomView, Bitmap bitmap) {
        bottomView.setLogo(bitmap);
    }

    @BindingAdapter("bottomDate")
    public static void bindBottomDate(RecognizeBottomView bottomView, String value) {
        bottomView.setDate(value);
    }

    @BindingAdapter("bottomTime")
    public static void bindBottomTime(RecognizeBottomView bottomView, String value) {
        bottomView.setTime(value);
    }

    @BindingAdapter("bottomBackgroundStatus")
    public static void bindBottomBackgroundStatus(ViewGroup viewGroup, Integer status) {
        RelativeLayout rlTime = viewGroup.findViewById(R.id.rl_timezone);
        RelativeLayout rlLogo = viewGroup.findViewById(R.id.iv_bg_logo);
        ImageView imageView = viewGroup.findViewById(R.id.iv_bg_arrow);

        if (status.equals(RecognizeRepository.FACE_RESULT_SUCCESS)) {
            rlTime.setBackground(viewGroup.getResources().getDrawable(R.drawable.bg_success_bottom_1));
            rlLogo.setBackground(viewGroup.getResources().getDrawable(R.drawable.bg_success_bottom_2));
            imageView.setBackground(viewGroup.getResources().getDrawable(R.drawable.bg_success_bottom_3));
        } else if (status.equals(RecognizeRepository.FACE_RESULT_FAILED)) {
            TableSettingConfigInfo configInfo = CommonRepository.getInstance().getSettingConfigInfo();
            if (configInfo.getDisplayModeFail() != ConfigConstants.DISPLAY_MODE_FAILED_NOT_FEEDBACK) {
                rlTime.setBackground(viewGroup.getResources().getDrawable(R.drawable.bg_fail_bottom_1));
                rlLogo.setBackground(viewGroup.getResources().getDrawable(R.drawable.bg_fail_bottom_2));
                imageView.setBackground(viewGroup.getResources().getDrawable(R.drawable.bg_fail_bottom_3));
            }
        } else if (status.equals(RecognizeRepository.FACE_RESULT_UNAUTHORIZED)) {
            rlTime.setBackground(viewGroup.getResources().getDrawable(R.drawable.bg_deny_bottom_1));
            rlLogo.setBackground(viewGroup.getResources().getDrawable(R.drawable.bg_deny_bottom_2));
            imageView.setBackground(viewGroup.getResources().getDrawable(R.drawable.bg_deny_bottom_3));
        } else {
            rlTime.setBackground(viewGroup.getResources().getDrawable(R.drawable.bg_normal_bottom_1));
            rlLogo.setBackground(viewGroup.getResources().getDrawable(R.drawable.bg_normal_bottom_2));
            imageView.setBackground(viewGroup.getResources().getDrawable(R.drawable.bg_normal_bottom_3));
        }
    }

    public RecognizeBottomView(Context context) {
        super(context);
        init(context, null);
    }

    public RecognizeBottomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        dataBinding = DataBindingUtil.inflate(inflater, R.layout.layout_recognize_bottom_view, this, true);
        viewModel = new BottomViewModel();
        dataBinding.setViewModel(viewModel);
    }

    public void setLogo(Bitmap bitmap) {
        if (viewModel != null) {
            viewModel.fieldLogo.set(bitmap);
        }
    }

    public void setDate(String value) {
        if (viewModel != null) {
            viewModel.fieldDate.set(value);
        }
    }

    public void setTime(String value) {
        if (viewModel != null) {
            viewModel.fieldTime.set(value);
        }
    }
}
