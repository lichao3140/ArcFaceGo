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

package com.arcsoft.arcfacesingle.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.base.BaseBusinessActivity;
import com.arcsoft.arcfacesingle.data.model.PersonInfo;
import com.arcsoft.arcfacesingle.databinding.ActivityPersonDetailBinding;
import com.arcsoft.arcfacesingle.navigator.IPersonDetailNavigator;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.arcfacesingle.view.widgets.CustomTimeAuthorityDetail;
import com.arcsoft.arcfacesingle.viewmodel.PersonDetailViewModel;

import java.util.LinkedHashMap;

public class PersonDetailActivity extends BaseBusinessActivity implements IPersonDetailNavigator {

    public static final String STRING_PERSON_MODEL = "STRING_PERSON_MODEL";

    private ActivityPersonDetailBinding dataBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_person_detail);
        PersonDetailViewModel viewModel = new PersonDetailViewModel();
        viewModel.setNavigator(this);
        dataBinding.setViewModel(viewModel);
        initView();

        Intent intent = getIntent();
        PersonInfo personInfo = (PersonInfo) intent.getSerializableExtra(STRING_PERSON_MODEL);
        String personSerial = personInfo.getPersonSerial();
        viewModel.initData(personSerial);
    }

    private void initView() {
        dataBinding.customTopBar.setVisibleClose(true);
        CommonUtils.setEditTextInputFilter(dataBinding.etPersonDetailName);
        CommonUtils.setEditTextInputFilter(dataBinding.etPersonDetailId);
    }

    @Override
    public void updateTimePermission(LinkedHashMap<String, String> timeMap) {
        CustomTimeAuthorityDetail viewGroup = dataBinding.layoutTimeAuthorityDetail;
        viewGroup.setTimeList(timeMap);
    }

    @Override
    public void updateDateUi(boolean hasPermission) {
        TextView tvDate = dataBinding.tvPersonPermissionDateDetail;
        tvDate.setTextColor(hasPermission ? getResources().getColor(R.color.color_white) :
                getResources().getColor(R.color.color_white_tran_60_percent));
    }
}
