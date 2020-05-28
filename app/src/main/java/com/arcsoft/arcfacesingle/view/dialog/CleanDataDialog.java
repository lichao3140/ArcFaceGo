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
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.asg.libcommon.base.BaseDialogFragment;
import com.arcsoft.asg.libcommon.base.BaseViewHolder;
import com.arcsoft.asg.libcommon.util.business.rx.RxUtils;
import com.arcsoft.asg.libcommon.util.common.ArithmeticUtils;
import com.arcsoft.asg.libcommon.util.common.ScreenUtils;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import me.jessyan.autosize.AutoSize;

public class CleanDataDialog extends BaseDialogFragment {

    public static final String BUNDLE_KEY_START_TIMER = "BUNDLE_KEY_START_TIMER";
    public static final int COUNT_TOTAL = 100;
    private TextView tvProgressResult;
    private ProgressBar progressBar;
    private TextView tvTimer;
    private Button btConfirm;

    private int total;
    private boolean startTimer;
    private int delayCurrent;
    private CleanDataCallback cleanDataCallback;
    private Disposable delayTimerDisposable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (null != bundle) {
            startTimer = bundle.getBoolean(BUNDLE_KEY_START_TIMER);
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
        return R.layout.dialog_clean_data;
    }

    @Override
    protected void convertView(BaseViewHolder holder, BaseDialogFragment baseDialog) {
        super.convertView(holder, baseDialog);
        progressBar = holder.getView(R.id.progress_horizontal);
        tvProgressResult = holder.getView(R.id.tv_clean_data_tip);
        tvTimer = holder.getView(R.id.tv_clean_data_progress_result_timer);
        btConfirm = holder.getView(R.id.btn_dialog_confirm);
    }

    public void setCleanDataCallback(CleanDataCallback cleanDataCallback) {
        this.cleanDataCallback = cleanDataCallback;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public void setProgressCurrent(int current) {
        double div = ArithmeticUtils.div(current, total, 3);
        double mul = ArithmeticUtils.mul(div, 100, 1);
        int progress = (int) mul;
        setProgress(progress);
    }

    public void setProgress(int progress) {
        if (progress >= COUNT_TOTAL) {
            progressBar.setVisibility(View.GONE);
            tvProgressResult.setTextSize(TypedValue.COMPLEX_UNIT_SP, 34);
            tvProgressResult.setText(CommonUtils.getStrFromRes(R.string.cleaning_data_complete));
            if (btConfirm != null) {
                btConfirm.setEnabled(true);
            }
            if (startTimer) {
                startCloseTimer();
            }
        }
    }

    private void startCloseTimer() {
        delayCurrent = 5;
        tvTimer.setText(delayCurrent + "s");
        cancelDelayTimer();
        delayTimerDisposable = Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                .compose(RxUtils.computingToMain())
                .subscribe(aLong -> {
                    delayCurrent--;
                    tvTimer.setText(delayCurrent + "s");
                    if (delayCurrent == 0 && cleanDataCallback != null) {
                        cleanDataCallback.gotoSplashPage();
                    }
                });
    }

    public void cancelDelayTimer() {
        if (delayTimerDisposable != null && !delayTimerDisposable.isDisposed()) {
            delayTimerDisposable.dispose();
            delayTimerDisposable = null;
        }
    }

    public interface CleanDataCallback {

        void gotoSplashPage();
    }
}
