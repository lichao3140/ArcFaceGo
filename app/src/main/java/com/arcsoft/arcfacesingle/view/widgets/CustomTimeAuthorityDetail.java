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
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.arcsoft.arcfacesingle.R;

import java.util.LinkedHashMap;
import java.util.Map;

public class CustomTimeAuthorityDetail extends LinearLayout {

    public CustomTimeAuthorityDetail(Context context) {
        super(context);
        initView(context);
    }

    public CustomTimeAuthorityDetail(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public CustomTimeAuthorityDetail(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_time_authority_detail, this, false);
        setOrientation(VERTICAL);
    }

    public void setTimeList(LinkedHashMap<String, String> timeMap) {
        for (Map.Entry<String, String> entry : timeMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            LayoutInflater inflater = LayoutInflater.from(getContext());
            RelativeLayout rl = (RelativeLayout) inflater.inflate(R.layout.item_time_authority_detail, null);
            TextView tvName = rl.findViewById(R.id.tv_time_authority_name);
            TextView tvValue = rl.findViewById(R.id.tv_time_authority_detail);
            tvName.setText(key);
            tvValue.setText(value);
            addView(rl);
        }
        requestLayout();
    }
}
