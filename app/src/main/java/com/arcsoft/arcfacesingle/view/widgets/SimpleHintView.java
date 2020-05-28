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
import android.widget.RelativeLayout;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.databinding.LayoutSimpleHintBinding;
import com.arcsoft.arcfacesingle.viewmodel.widget.SimpleHintViewModel;

public class SimpleHintView extends RelativeLayout {

    private SimpleHintViewModel viewModel;
    private LayoutSimpleHintBinding hintBinding;

    @BindingAdapter("hintContent")
    public static void bindHintContent(SimpleHintView simpleHintView, String value) {
        simpleHintView.setHint(value);
    }

    public SimpleHintView(Context context) {
        super(context);
        init(context, null);
    }

    public SimpleHintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        hintBinding = DataBindingUtil.inflate(inflater, R.layout.layout_simple_hint, this, true);
        viewModel = new SimpleHintViewModel();
        hintBinding.setViewModel(viewModel);
    }

    public void setHint(String value) {
        if (viewModel != null) {
            viewModel.fieldTitle.set(value);
        }
    }
}
