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
import androidx.databinding.DataBindingUtil;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.databinding.LayoutTopBarBinding;
import com.arcsoft.arcfacesingle.viewmodel.widget.TopBarViewModel;

public class CustomTopBar extends RelativeLayout {

    private LayoutTopBarBinding barBinding;
    private TopBarViewModel viewModel;

    public CustomTopBar(Context context) {
        super(context);
        initView(context);
    }

    public CustomTopBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public CustomTopBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        barBinding = DataBindingUtil.inflate(inflater, R.layout.layout_top_bar, this, true);
        viewModel = new TopBarViewModel();
        barBinding.setViewModel(viewModel);
        viewModel.setFieldStringTitle("");
        viewModel.setFieldVisibleSetting(false);
        viewModel.setFieldVisibleSkip(false);
    }

    public void setVisibleClose(boolean visible) {
        if (viewModel != null) {
            viewModel.setFieldVisibleClose(visible);
        }
    }

    public void setStringTitle(String title) {
        if (viewModel != null) {
            viewModel.setFieldStringTitle(title);
        }
    }

    public void setVisibleTitle(boolean visible) {
        if (viewModel != null) {
            viewModel.setFieldVisibleTitle(visible);
        }
    }

    public void setVisibleSkip(boolean visible) {
        if (viewModel != null) {
            viewModel.setFieldVisibleSkip(visible);
        }
    }

    public void setVisibleSetting(boolean visible) {
        if (viewModel != null) {
            viewModel.setFieldVisibleSetting(visible);
        }
    }

    public void setBackOnClickListener(TopBarViewModel.OnBackClickListener backOnClickListener) {
        if (viewModel != null) {
            viewModel.setBackClickListener(backOnClickListener);
        }
    }
}
