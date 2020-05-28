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
import com.arcsoft.arcfacesingle.databinding.LayoutRecognizeTopViewBinding;
import com.arcsoft.arcfacesingle.viewmodel.widget.TopViewModel;

public class RecognizeTopView extends RelativeLayout {

    private TopViewModel viewModel;
    private LayoutRecognizeTopViewBinding dataBinding;

    @BindingAdapter("topTitle")
    public static void bindTopTitle(RecognizeTopView topView, String value) {
        topView.setTitle(value);
    }

    @BindingAdapter("topLogo")
    public static void bindTopLogo(RecognizeTopView topView, Bitmap bitmap) {
        topView.setLogo(bitmap);
    }

    @BindingAdapter("topBackgroundStatus")
    public static void bindTopBackgroundStatus(ViewGroup viewGroup, Integer status) {
        RelativeLayout relativeLayout = viewGroup.findViewById(R.id.rl_main_logo);
        ImageView imageView = viewGroup.findViewById(R.id.iv_bg_arrow);

        if (status.equals(RecognizeRepository.FACE_RESULT_SUCCESS)) {
            relativeLayout.setBackground(viewGroup.getResources().getDrawable(R.drawable.bg_success_top_1));
            imageView.setBackground(viewGroup.getResources().getDrawable(R.drawable.bg_success_top_2));
        } else if (status.equals(RecognizeRepository.FACE_RESULT_FAILED)) {
            TableSettingConfigInfo configInfo = CommonRepository.getInstance().getSettingConfigInfo();
            if (configInfo.getDisplayModeFail() != ConfigConstants.DISPLAY_MODE_FAILED_NOT_FEEDBACK) {
                relativeLayout.setBackground(viewGroup.getResources().getDrawable(R.drawable.bg_fail_top_1));
                imageView.setBackground(viewGroup.getResources().getDrawable(R.drawable.bg_fail_top_2));
            }
        } else if (status.equals(RecognizeRepository.FACE_RESULT_UNAUTHORIZED)) {
            relativeLayout.setBackground(viewGroup.getResources().getDrawable(R.drawable.bg_deny_top_1));
            imageView.setBackground(viewGroup.getResources().getDrawable(R.drawable.bg_deny_top_2));
        } else {
            relativeLayout.setBackground(viewGroup.getResources().getDrawable(R.drawable.bg_normal_top_1));
            imageView.setBackground(viewGroup.getResources().getDrawable(R.drawable.bg_normal_top_2));
        }

    }

    public RecognizeTopView(Context context) {
        super(context);
        init(context, null);
    }

    public RecognizeTopView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        dataBinding = DataBindingUtil.inflate(inflater, R.layout.layout_recognize_top_view, this, true);
        viewModel = new TopViewModel();
        dataBinding.setViewModel(viewModel);
    }

    public void setTitle(String value) {
        if (viewModel != null) {
            viewModel.fieldTitle.set(value);
        }
    }

    public void setLogo(Bitmap bitmap) {
        if (viewModel != null) {
            viewModel.fieldLogo.set(bitmap);
        }
    }
}
