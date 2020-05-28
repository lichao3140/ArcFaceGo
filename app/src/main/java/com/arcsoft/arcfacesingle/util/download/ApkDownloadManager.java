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

package com.arcsoft.arcfacesingle.util.download;

import android.text.TextUtils;

import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.util.SdcardUtils;
import com.arcsoft.arcsoftlink.enums.UpgradeStatusEnum;
import com.arcsoft.arcsoftlink.mqtt.bean.UpgradeResult;
import com.arcsoft.asg.libcommon.util.common.AppUtils;
import com.arcsoft.asg.libcommon.util.common.SPUtils;
import com.arcsoft.asg.libcommon.util.common.Utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.Call;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ApkDownloadManager {

    private static final String TAG = ApkDownloadManager.class.getSimpleName();
    public static final int VALUE_APK_UPGRADE_RESET = -1;
    public static final int VALUE_APK_UPGRADE_FAIL = -2;

    private static volatile ApkDownloadManager manager;
    private DownloadListener downloadListener;

    /**
     * 是否正在下载Apk
     */
    private boolean downloadApk;

    public static ApkDownloadManager getInstance() {
        if (manager == null) {
            synchronized (ApkDownloadManager.class) {
                if (manager == null) {
                    manager = new ApkDownloadManager();
                }
            }
        }
        return manager;
    }

    public void setDownloadApk(boolean downloadApk) {
        this.downloadApk = downloadApk;
    }

    public boolean isDownloadApk() {
        return downloadApk;
    }

    public void setDownloadListener(DownloadListener downloadListener) {
        this.downloadListener = downloadListener;
    }

    public void downloadApk(String filePath, String url, DownloadListener downloadListener) {
        setDownloadApk(true);
        FileDownloadManager.getInstance().downloadFile(url, new FileDownloadManager.FileDownloadListener() {

            @Override
            public void onFailure(Call call, IOException e) {
                setDownloadApk(false);
            }

            @Override
            public void onResponse(Call call, Response response) {
                ResponseBody body = response.body();
                if (null != body) {
                    downloadApkAndSave(body, filePath, downloadListener);
                }
            }
        });
    }

    public boolean checkPkgName(String netPkgName) {
        return !TextUtils.isEmpty(netPkgName) && netPkgName.equals(AppUtils.getAppPackageName());
    }

    public boolean packageTransfer(String filePath) {
        return AppUtils.checkPackageInfo(Utils.getApp(), filePath);
    }

    public String getFilePath(String fileName) {
        return SdcardUtils.getInstance().getAPKPath() + File.separator + fileName;
    }

    public UpgradeResult getUpgradeResult() {
        int oldVersionCode = SPUtils.getInstance().getInt(Constants.SP_KEY_OLD_VERSION_CODE);
        if (oldVersionCode == VALUE_APK_UPGRADE_RESET) {
            return null;
        } else if (oldVersionCode == VALUE_APK_UPGRADE_FAIL) {
            int upgradeId = SPUtils.getInstance().getInt(Constants.SP_KEY_UPGRADE_ID);
            return new UpgradeResult(upgradeId, UpgradeStatusEnum.UPGRADE_STATUS_FAILED);
        } else {
            int upgradeId = SPUtils.getInstance().getInt(Constants.SP_KEY_UPGRADE_ID);
            int newVersionCode = AppUtils.getAppVersionCode();
            if (newVersionCode > oldVersionCode) {
                //升级成功
                return new UpgradeResult(upgradeId, UpgradeStatusEnum.UPGRADE_STATUS_SUCCESS);
            } else {
                //正常状态
                return new UpgradeResult(upgradeId, UpgradeStatusEnum.UPGRADE_STATUS_NORMAL);
            }
        }
    }

    /**
     * 保存apk
     * @param body
     * @param filePath
     */
    private void downloadApkAndSave(ResponseBody body, String filePath, DownloadListener downloadListener) {
        long length = body.contentLength();
        if (length == 0) {
            return;
        }
        InputStream is = null;
        BufferedInputStream bis = null;
        OutputStream fos = null;
        try {
            is = body.byteStream();
            if (is != null) {
                File file = new File(filePath);
                bis = new BufferedInputStream(is);
                fos = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int size;
                while ((size = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, size);
                }
                if (downloadListener != null) {
                    downloadListener.complete(filePath);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            setDownloadApk(false);
            if (downloadListener != null) {
                downloadListener.loadFail(e.getMessage());
            }
        } finally {
            setDownloadApk(false);
            try {
                if (is != null) {
                    is.close();
                }
                if (bis != null) {
                    bis.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public interface DownloadListener {

        /**
         * 下载完成
         */
        void complete(String path);

        /**
         * 下载失败
         */
        void loadFail(String message);
    }
}
