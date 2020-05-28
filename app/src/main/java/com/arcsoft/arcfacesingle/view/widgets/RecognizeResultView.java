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
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.business.common.CommonRepository;
import com.arcsoft.arcfacesingle.business.recognize.RecognizeRepository;
import com.arcsoft.arcfacesingle.business.setting.ConfigConstants;
import com.arcsoft.arcfacesingle.data.db.table.TableSettingConfigInfo;
import com.arcsoft.arcfacesingle.databinding.LayoutRecognizeResultBinding;
import com.arcsoft.arcfacesingle.viewmodel.widget.RecognizeResultViewModel;

public class RecognizeResultView extends RelativeLayout {

    private LayoutRecognizeResultBinding resultBinding;
    private RecognizeResultViewModel viewModel;

    @BindingAdapter("recognizeResult")
    public static void bindRecognizeResult(RecognizeResultView resultView, String value) {
        resultView.setResult(value);
    }

    @BindingAdapter("recognizeHeadPath")
    public static void bindRecognizeHeadPath(RecognizeResultView resultView, String value) {
        resultView.setHeadPath(value);
    }

    @BindingAdapter("resultBackgroundStatus")
    public static void bindResultBackgroundStatus(RecognizeResultView resultView, Integer status) {
        RelativeLayout rlHead = resultView.findViewById(R.id.rl_head_shot);
        TextView tvMainTitle = resultView.findViewById(R.id.tv_main_title);
        TextView tvSubTitle = resultView.findViewById(R.id.tv_sub_title);
        ImageView ivCorner = resultView.findViewById(R.id.iv_corner);
        ImageView ivLeft = resultView.findViewById(R.id.iv_left);
        ImageView ivRight = resultView.findViewById(R.id.iv_right);

        if (status.equals(RecognizeRepository.FACE_RESULT_SUCCESS)) {
            rlHead.setBackground(resultView.getResources().getDrawable(R.drawable.bg_success_round_container));
            ivCorner.setBackground(resultView.getResources().getDrawable(R.drawable.bg_success_corner));
            ivLeft.setBackground(resultView.getResources().getDrawable(R.drawable.ic_success));
            ivRight.setBackground(resultView.getResources().getDrawable(R.drawable.ic_success));
            tvMainTitle.setTextColor(resultView.getResources().getColor(R.color.color_recognize_success));
            tvSubTitle.setTextColor(resultView.getResources().getColor(R.color.color_recognize_success));
        } else if (status.equals(RecognizeRepository.FACE_RESULT_FAILED)) {
            TableSettingConfigInfo configInfo = CommonRepository.getInstance().getSettingConfigInfo();
            if (configInfo.getDisplayModeFail() != ConfigConstants.DISPLAY_MODE_FAILED_NOT_FEEDBACK) {
                rlHead.setBackground(resultView.getResources().getDrawable(R.drawable.bg_fail_round_container));
                ivCorner.setBackground(resultView.getResources().getDrawable(R.drawable.bg_fail_corner));
                ivLeft.setBackground(resultView.getResources().getDrawable(R.drawable.ic_fail));
                ivRight.setBackground(resultView.getResources().getDrawable(R.drawable.ic_fail));
                tvMainTitle.setTextColor(resultView.getResources().getColor(R.color.color_recognize_fail));
                tvSubTitle.setTextColor(resultView.getResources().getColor(R.color.color_recognize_fail));
            }
        } else if (status.equals(RecognizeRepository.FACE_RESULT_UNAUTHORIZED)) {
            rlHead.setBackground(resultView.getResources().getDrawable(R.drawable.bg_deny_round_container));
            ivCorner.setBackground(resultView.getResources().getDrawable(R.drawable.bg_deny_corner));
            ivLeft.setBackground(resultView.getResources().getDrawable(R.drawable.ic_deny));
            ivRight.setBackground(resultView.getResources().getDrawable(R.drawable.ic_deny));
            tvMainTitle.setTextColor(resultView.getResources().getColor(R.color.color_recognize_permission));
            tvSubTitle.setTextColor(resultView.getResources().getColor(R.color.color_recognize_permission));
        }

        resultView.setSubResult(status);
    }

    @BindingAdapter("backgroundAlpha")
    public static void bindBackgroundAlpha(RecognizeResultView resultView, boolean fullScreen) {
        if (fullScreen) {
            resultView.setBackgroundAlpha(0.8f);
        } else {
            resultView.setBackgroundAlpha(1.0f);
        }
    }

    public RecognizeResultView(Context context) {
        super(context);
        init(context, null);
    }

    public RecognizeResultView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        resultBinding = DataBindingUtil.inflate(inflater, R.layout.layout_recognize_result, this, true);
        viewModel = new RecognizeResultViewModel();
        resultBinding.setViewModel(viewModel);
    }

    public void setResult(String value) {
        if (viewModel != null) {
            viewModel.fieldResult.set(value);
        }
    }

    public void setSubResult(Integer status) {
        if (viewModel != null) {
            String value = "";
            if (status.equals(RecognizeRepository.FACE_RESULT_SUCCESS)) {
                value = "Recognition succeeded";
            }else if(status.equals(RecognizeRepository.FACE_RESULT_FAILED)){
                value = "Recognition failed";
            }else if(status.equals(RecognizeRepository.FACE_RESULT_UNAUTHORIZED)) {
                value = "Permission denied";
            }
            viewModel.fieldSubResult.set(value);
        }
    }

    public void setHeadPath(String value) {
        if (viewModel != null) {
            viewModel.fieldHeadPath.set(value);
        }
    }

    public void setBackgroundAlpha(Float value) {
        if (viewModel != null) {
            viewModel.fieldBgAlpha.set(value);
        }
    }

}
