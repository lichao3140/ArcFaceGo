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

package com.arcsoft.arcfacesingle.util;

import android.os.Environment;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.asg.libcommon.util.common.FileUtils;
import com.arcsoft.asg.libcommon.util.common.TimeUtils;
import com.arcsoft.asg.libcommon.util.common.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SdcardUtils {

    private static volatile SdcardUtils mInstance;
    private static final Object SINGLE_BYTE = new Object();
    public static final String DIR_FACE_DATABASE_IMAGES = "faceDatabaseImages";

    private SdcardUtils() {
    }

    public static SdcardUtils getInstance() {
        if (mInstance == null) {
            synchronized (SINGLE_BYTE) {
                if (mInstance == null) {
                    mInstance = new SdcardUtils();
                }
            }
        }
        return mInstance;
    }

    /**
     * 创建所需文件夹
     */
    public void init() {
        FileUtils.createDirOrDirExist(getBatchRegisterOriDir());
        FileUtils.createDirOrDirExist(getRegisteredDirPath());
        FileUtils.createDirOrDirExist(getSignRecordDirPath());
        FileUtils.createDirOrDirExist(getCompanyLogo());
        FileUtils.createDirOrDirExist(getCrashPathDir());
        FileUtils.createDirOrDirExist(getDeviceActiveFilePathDir());
        FileUtils.createDirOrDirExist(getBackUpFileDirPath());
        FileUtils.createDirOrDirExist(getBackUpDbDirPath());
        if (Constants.SWITCH_SAVE_IR_PICTURE) {
            FileUtils.createDirOrDirExist(getIrFdFailDirPath());
            FileUtils.createDirOrDirExist(getIrLiveFailDirPath());
            FileUtils.createDirOrDirExist(getIrRgbCompareFailDirPath());
            FileUtils.createDirOrDirExist(getIrProcessFailDirPath());
        }
    }

    /**
     * 获取存放应用相关文件根目录
     *
     * @return
     */
    public String getRootPathDir() {
        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
        rootPath += Utils.getApp().getResources().getString(R.string.sdcard_package_name);
        return rootPath;
    }

    /**
     * 获取存放批量注册原图文件目录
     *
     * @return
     */
    public String getBatchRegisterOriDir() {
        return getRootPathDir() + File.separator + "batchRegisterPicturesOri";
    }

    /**
     * 获取存放注册成功的照片文件目录
     *
     * @return
     */
    public String getRegisteredDirPath() {
        return getRootPathDir() + File.separator + DIR_FACE_DATABASE_IMAGES;
    }

    /**
     * 获取存放刷脸记录图片文件目录
     *
     * @return
     */
    public String getSignRecordDirPath() {
        return getRootPathDir() + File.separator + "signRecordImg";
    }

    /**
     * IR FD失败图片保存路径
     *
     * @return
     */
    public String getIrFdFailDirPath() {
        return getRootPathDir() + File.separator + "irFail" + File.separator + "irFdFail";
    }

    /**
     * IR活体失败图片保存路径
     *
     * @return
     */
    public String getIrLiveFailDirPath() {
        return getRootPathDir() + File.separator + "irFail" + File.separator + "irLiveFail";
    }

    /**
     * IR活体成功图片保存路径
     *
     * @return
     */
    public String getIrLiveSuccessDirPath() {
        return getRootPathDir() + File.separator + "irFail" + File.separator + "irLiveSuccess";
    }

    /**
     * IR和RGB人脸框对齐失败图片保存路径
     *
     * @return
     */
    public String getIrRgbCompareFailDirPath() {
        return getRootPathDir() + File.separator + "irFail" + File.separator + "irRgbCompareFail";
    }

    /**
     * IR的process失败图片保存路径
     *
     * @return
     */
    public String getIrProcessFailDirPath() {
        return getRootPathDir() + File.separator + "irFail" + File.separator + "irProcessFail";
    }

    /**
     * 最新APK下载保存路径
     *
     * @return
     */
    public String getAPKPath() {
        return getRootPathDir() + File.separator + "apk" + File.separator + "arcfacesingle";
    }

    /**
     * 拍照注册封面保存路径
     *
     * @return
     */
    public String getSavePhotoPath() {
        return getRootPathDir() + File.separator + "captureImages";
    }

    /**
     * 设备自动化配置文件路径
     *
     * @return
     */
    public String getSettingsPath() {
        return getRootPathDir() + File.separator + "settings";
    }

    /**
     * logo存放路径
     *
     * @return
     */
    public String getCompanyLogo() {
        return getRootPathDir() + File.separator + "companyLogo";
    }

    /**
     * log日志文件路径
     *
     * @return
     */
    private String getTimeLogFilePath() {
        return getRootPathDir() + File.separator + "testLog" + File.separator + "timeLog.txt";
    }

    private String getTimeLogFileDirPath() {
        return getRootPathDir() + File.separator + "testLog" + File.separator;
    }

    /**
     * 应用crash文件存放路径
     *
     * @return
     */
    public String getCrashPathDir() {
        return getRootPathDir() + File.separator + "crash";
    }

    /**
     * 设备激活配置文件存放路径
     *
     * @return
     */
    public String getDeviceActiveFilePathDir() {
        return getRootPathDir() + File.separator + "activeConfig";
    }

    /**
     * 设备激活配置文件存放路径
     *
     * @return
     */
    public String getDeviceActiveTxtFile() {
        return getDeviceActiveFilePathDir() + File.separator + "activeConfig.txt";
    }

    /**
     * 应用数据备份路径
     * @return
     */
    private String getBackUpFileDirPath() {
        return getRootPathDir() + File.separator + "backup";
    }

    /**
     * 应用Database数据备份路径
     * @return
     */
    private String getBackUpDbDirPath() {
        return getBackUpFileDirPath() + File.separator + "database";
    }

    /**
     * 应用Database数据备份路径
     * @return
     */
    public String getBackUpDbFilePath() {
        return getBackUpDbDirPath() + File.separator + Constants.DATA_BASE_NAME_PATH;
    }

    /**
     * 获取DCIM文件夹下.thumbnail文件路径
     * @return
     */
    public String getDCIMThumbnailDirPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "DCIM" +
                File.separator + ".thumbnails";
    }

    /**
     * 保存log日志到SDCard
     * @param content
     */
    public void saveLogTest(String content) {
        Disposable mLogDisposable = Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
            String logDirPath = getTimeLogFileDirPath();
            long time = System.currentTimeMillis();
            String fileName = logDirPath + TimeUtils.getShortStringDay(time) + File.separator + TimeUtils.getShortStringHour(time) +
                    File.separator + "timeLog.txt";
            saveLog(fileName, content);
        }).subscribeOn(Schedulers.single()).subscribe();
    }

    /**
     * 保存日志通用方法
     * @param fileName
     * @param content
     */
    private void saveLog(String fileName, String content) {
        try {
            File file = new File(fileName);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            FileWriter writer = new FileWriter(fileName, true);
            writer.write(content + "\r\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
