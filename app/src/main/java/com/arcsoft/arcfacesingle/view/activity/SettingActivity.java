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
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.base.BaseBusinessActivity;
import com.arcsoft.arcfacesingle.business.personlist.adapter.SettingFragmentPagerAdapter;
import com.arcsoft.arcfacesingle.data.db.table.TableSettingConfigInfo;
import com.arcsoft.arcfacesingle.data.event.ChangeMainLogoEvent;
import com.arcsoft.arcfacesingle.data.event.ChangeSecondLogoEvent;
import com.arcsoft.arcfacesingle.data.event.ClosePageEvent;
import com.arcsoft.arcfacesingle.data.event.DisconnectEvent;
import com.arcsoft.arcfacesingle.data.event.SettingConfigChangedEvent;
import com.arcsoft.arcfacesingle.databinding.ActivitySettingBinding;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.arcfacesingle.view.fragment.CustomSettingFragment;
import com.arcsoft.arcfacesingle.view.fragment.DeviceSettingFragment;
import com.arcsoft.arcfacesingle.view.fragment.RecognitionSettingFragment;
import com.arcsoft.arcfacesingle.view.widgets.CustomTopBar;
import com.arcsoft.arcfacesingle.viewmodel.setting.SettingViewModel;
import com.arcsoft.asg.libcommon.util.business.DoubleClickUtils;
import com.arcsoft.asg.libcommon.util.common.ToastUtils;
import com.arcsoft.asg.libcommon.util.common.Utils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class SettingActivity extends BaseBusinessActivity implements OnClickListener, OnPageChangeListener, SettingViewModel.NewSettingListener {

    private static final String TAG = SettingActivity.class.getSimpleName();
    private static final int INDEX_FRAGMENT_ONE = 0;
    private static final int INDEX_FRAGMENT_TWO = 1;
    private static final int INDEX_FRAGMENT_THREE = 2;

    private SettingViewModel newSettingViewModel;
    private ActivitySettingBinding activityNewSettingBinding;
    private SettingFragmentPagerAdapter settingFragmentPagerAdapter;

    private Button btnTabOne;
    private Button btnTabTwo;
    private Button btnTabThree;
    private ViewPager myViewPager;

    private DeviceSettingFragment deviceSettingFragment;
    private CustomSettingFragment customSettingFragment;
    private RecognitionSettingFragment recognitionSettingFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityNewSettingBinding = DataBindingUtil.setContentView(this, R.layout.activity_setting);
        newSettingViewModel = new SettingViewModel();
        newSettingViewModel.setNewSettingListener(this);
        activityNewSettingBinding.setViewModel(newSettingViewModel);

        btnTabOne = findViewById(R.id.tv_item_one);
        btnTabTwo = findViewById(R.id.tv_item_two);
        btnTabThree = findViewById(R.id.tv_item_three);
        myViewPager = findViewById(R.id.myViewPager);
        CustomTopBar customTopBar = findViewById(R.id.custom_top_bar);
        customTopBar.setVisibleSetting(true);
        customTopBar.setVisibleClose(true);

        btnTabOne.setOnClickListener(this);
        btnTabTwo.setOnClickListener(this);
        btnTabThree.setOnClickListener(this);
        findViewById(R.id.btn_setting_save).setOnClickListener(this);
        myViewPager.addOnPageChangeListener(this);
        initTab(savedInstanceState == null);
    }

    private void initTab(boolean state) {
        if (state) {
            deviceSettingFragment = new DeviceSettingFragment();
            customSettingFragment = new CustomSettingFragment();
            recognitionSettingFragment = new RecognitionSettingFragment();
        } else {
            List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
            for (Fragment item : fragmentList) {
                String fragmentTag = item.getClass().getSimpleName();
                if (fragmentTag.compareTo(DeviceSettingFragment.class.getSimpleName()) == 0) {
                    deviceSettingFragment = (DeviceSettingFragment) item;
                } else if (fragmentTag.compareTo(RecognitionSettingFragment.class.getSimpleName()) == 0) {
                    recognitionSettingFragment = (RecognitionSettingFragment) item;
                } else if (fragmentTag.compareTo(CustomSettingFragment.class.getSimpleName()) == 0) {
                    customSettingFragment = (CustomSettingFragment) item;
                }
            }
        }

        List<Fragment> list = new ArrayList<>();
        list.add(deviceSettingFragment);
        list.add(recognitionSettingFragment);
        list.add(customSettingFragment);
        settingFragmentPagerAdapter = new SettingFragmentPagerAdapter(getSupportFragmentManager(), list);
        myViewPager.setAdapter(settingFragmentPagerAdapter);
        myViewPager.setCurrentItem(0);
        myViewPager.setOffscreenPageLimit(3);
        btnTabOne.setSelected(true);
    }

    @Override
    public void onClick(View v) {
        int resId = v.getId();
        if (DoubleClickUtils.isFastDoubleClick(resId)) {
            return;
        }
        switch (resId) {
            case R.id.tv_item_one:
                myViewPager.setCurrentItem(INDEX_FRAGMENT_ONE);
                btnTabOne.setSelected(true);
                btnTabTwo.setSelected(false);
                btnTabThree.setSelected(false);
                break;
            case R.id.tv_item_two:
                myViewPager.setCurrentItem(INDEX_FRAGMENT_TWO);
                btnTabOne.setSelected(false);
                btnTabTwo.setSelected(true);
                btnTabThree.setSelected(false);
                break;
            case R.id.tv_item_three:
                myViewPager.setCurrentItem(INDEX_FRAGMENT_THREE);
                btnTabOne.setSelected(false);
                btnTabTwo.setSelected(false);
                btnTabThree.setSelected(true);
                break;
            case R.id.btn_setting_save:
                if (newSettingViewModel != null) {
                    TableSettingConfigInfo configInfo = new TableSettingConfigInfo();
                    deviceSettingFragment.getConfig(configInfo);
                    customSettingFragment.getConfig(configInfo);
                    recognitionSettingFragment.getConfig(configInfo);
                    if (newSettingViewModel.saveSettings(deviceSettingFragment.getDevicePort(), configInfo)) {
                        ToastUtils.showShortToast(CommonUtils.getStrFromRes(R.string.settings_save_success));
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
    }

    @Override
    public void onPageSelected(int arg0) {
        switch (arg0) {
            case 0:
                btnTabOne.setSelected(true);
                btnTabTwo.setSelected(false);
                btnTabThree.setSelected(false);
                break;
            case 1:
                btnTabOne.setSelected(false);
                btnTabTwo.setSelected(true);
                btnTabThree.setSelected(false);
                break;
            case 2:
                btnTabOne.setSelected(false);
                btnTabTwo.setSelected(false);
                btnTabThree.setSelected(true);
                break;
            case 3:
                btnTabOne.setSelected(false);
                btnTabTwo.setSelected(false);
                btnTabThree.setSelected(false);
                break;
            default:
                break;
        }
    }

    @Override
    protected IntentFilter createIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        return filter;
    }

    @Override
    protected void onReceiveBroadcast(Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            if (deviceSettingFragment != null) {
                deviceSettingFragment.setIpAndMacAddress();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (newSettingViewModel != null) {
            newSettingViewModel.release();
        }
        newSettingViewModel = null;
        activityNewSettingBinding = null;
        settingFragmentPagerAdapter = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (newSettingViewModel != null) {
            newSettingViewModel.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (newSettingViewModel != null) {
            newSettingViewModel.onPause();
        }
    }

    @Override
    public void goRecognize() {
        if (!isFinishing()) {
            startActivity(new Intent(this, RecognizeActivity.class));
            finish();
        }
    }

    @Override
    public void goFaceManager() {
        startActivity(new Intent(this, PersonListActivity.class));
    }

    @Override
    public void goDeviceActive() {
        startActivity(new Intent(this, SettingSelectActivity.class));
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN || ev.getAction() == MotionEvent.ACTION_MOVE) {
            if (newSettingViewModel != null) {
                newSettingViewModel.onResume();
            }
        }
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            hideNavigationBar();
        }
        return super.dispatchTouchEvent(ev);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConfigChange(SettingConfigChangedEvent event) {
        if (null != event) {
            String strTopActivity = Utils.getTopActivityOrApp().getClass().getSimpleName();
            if (strTopActivity.equals(SettingActivity.class.getSimpleName())) {
                if (newSettingViewModel != null) {
                    newSettingViewModel.release();
                }
                finish();
                Intent intent = new Intent(this, SettingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                if (deviceSettingFragment != null) {
                    deviceSettingFragment.reloadConfig();
                }
                if (recognitionSettingFragment != null) {
                    recognitionSettingFragment.reloadConfig();
                }
                if (customSettingFragment != null) {
                    customSettingFragment.reloadConfig();
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void changeMainLogo(ChangeMainLogoEvent event) {
        if (null != event) {
            if (customSettingFragment != null) {
                customSettingFragment.setMainLogo(true, event.getLogoPath());
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void changeSecondLogo(ChangeSecondLogoEvent event) {
        if (null != event) {
            if (customSettingFragment != null) {
                customSettingFragment.setMainLogo(false, event.getLogoPath());
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void disconnect(DisconnectEvent event) {
        if (null != event) {
            if (deviceSettingFragment != null) {
                deviceSettingFragment.disconnect(event.isConnected(), event.getIpAddress());
            }
        }
    }

    /**
     * 设置副Logo
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void changeSecondLogo(ClosePageEvent event) {
        if (null != event) {
            if (newSettingViewModel != null) {
                newSettingViewModel.release();
            }
        }
    }
}
