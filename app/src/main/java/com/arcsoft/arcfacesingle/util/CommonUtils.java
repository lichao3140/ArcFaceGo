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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.util.Pair;
import android.util.SparseArray;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.business.common.CommonRepository;
import com.arcsoft.arcfacesingle.business.personlist.PersonListRepository;
import com.arcsoft.arcfacesingle.business.setting.ConfigConstants;
import com.arcsoft.arcfacesingle.data.db.table.TableSettingConfigInfo;
import com.arcsoft.arcfacesingle.libutil.CameraEngineUtils;
import com.arcsoft.arcfacesingle.libutil.ImageFileUtils;
import com.arcsoft.arcfacesingle.server.pojo.request.RequestSetting;
import com.arcsoft.arcfacesingle.util.business.UsbHelper;
import com.arcsoft.arcsoftlink.enums.WeekEnum;
import com.arcsoft.asg.libcommon.contract.ICameraEngine;
import com.arcsoft.asg.libcommon.util.common.AppUtils;
import com.arcsoft.asg.libcommon.util.common.DeviceUtils;
import com.arcsoft.asg.libcommon.util.common.FileUtils;
import com.arcsoft.asg.libcommon.util.common.Md5Utils;
import com.arcsoft.asg.libcommon.util.common.SPUtils;
import com.arcsoft.asg.libcommon.util.common.TimeUtils;
import com.arcsoft.asg.libcommon.util.common.Utils;
import com.arcsoft.asg.libdeviceadapt.bean.DeviceCameraInfo;
import com.arcsoft.faceengine.FaceInfo;
import com.github.mjdev.libaums.fs.FileSystem;
import com.github.mjdev.libaums.fs.UsbFile;
import com.github.mjdev.libaums.fs.UsbFileStreamFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CommonUtils {

    private static final String TAG = CommonUtils.class.getSimpleName();

    private static ArrayList<String> allFile = new ArrayList<>();
    private static ArrayList<UsbFile> allUsbFiles = new ArrayList<>();
    private static final int FT_SKIP_FRAME_TOTAL = 3;
    private static final double IR_FACE_RECT_PADDING_RATIO = 0.08;
    private static final int LENGTH_CHAR_MAX_30 = 30;
    private static final int LENGTH_CHAR_MAX_19 = 30;

    public static final int DETECT_ORIENT_90 = 2;
    public static final int DETECT_ORIENT_270 = 3;
    public static final int DETECT_ORIENT_180 = 4;
    private static final int DEF_PREVIEW_WIDTH = 640;
    private static final int DEF_PREVIEW_HEIGHT = 480;

    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE
    };

    private CommonUtils() {
        throw new UnsupportedOperationException("无法初始化！");
    }

    /**
     * 获取引擎错误码注释集合
     *
     * @return
     */
    public static SparseArray<String> initFaceEngineErrorMsgList() {
        String[] errMsgArr = Utils.getApp().getResources().getStringArray(R.array.error_message_list);
        SparseArray errMsgMap = new SparseArray<>(errMsgArr.length);
        try {
            for (String msg : errMsgArr) {
                String[] arr = msg.split(":");
                if (arr.length > 1) {
                    int errorCode = Integer.parseInt(arr[0]);
                    String errorMsg = arr[1];
                    errMsgMap.put(errorCode, errorMsg);
                }
            }
            return errMsgMap;
        } catch (Exception e) {
            e.printStackTrace();
            return errMsgMap;
        }
    }

    /**
     * 判断是否包含特殊字符
     *
     * @param str
     * @return
     */
    public static boolean compileExChar(String str) {
        String limitEx = "[']";
        Pattern pattern = Pattern.compile(limitEx);
        Matcher m = pattern.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }

    /**
     * 禁止EditText输入特殊字符
     *
     * @param editText
     */
    public static void setEditTextInputFilter(EditText editText) {
        InputFilter filter = (source, start, end, dest, dstart, dend) -> {
            String speChat = "[']";
            Pattern pattern = Pattern.compile(speChat);
            Matcher matcher = pattern.matcher(source.toString());
            if (matcher.find()) {
                return "";
            } else {
                return null;
            }
        };
        editText.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(LENGTH_CHAR_MAX_30)});
    }

    public static void setEditTextDeviceAccessInputFilter(EditText editText) {
        InputFilter inputFilter = (source, start, end, dest, dstart, dend) -> {
            SpannableString ss = new SpannableString(source);
            Object[] spans = ss.getSpans(0, ss.length(), Object.class);
            if (spans != null) {
                for (Object span : spans) {
                    if (span instanceof UnderlineSpan) {
                        return "";
                    }
                }
            }
            return null;
        };
        editText.setFilters(new InputFilter[]{inputFilter, new InputFilter.LengthFilter(LENGTH_CHAR_MAX_19)});
    }

    /**
     * 递归获取所有图片文件
     *
     * @param strPath
     * @return
     */
    public static ArrayList<String> refreshFileList(String strPath) {
        String filename;
        String suf;
        File dir = new File(strPath);
        File[] files = dir.listFiles();
        if (files == null) {
            return null;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                refreshFileList(file.getAbsolutePath());
            } else {
                filename = file.getName();
                int j = filename.lastIndexOf(".");
                suf = filename.substring(j + 1);
                if (Constants.SUFFIX_IMAGE_JPG.equalsIgnoreCase(suf) ||
                        Constants.SUFFIX_IMAGE_PNG.equalsIgnoreCase(suf) ||
                        Constants.SUFFIX_IMAGE_JPEG.equalsIgnoreCase(suf) ||
                        Constants.SUFFIX_IMAGE_BMP.equalsIgnoreCase(suf)) {
                    allFile.add(file.getAbsolutePath());
                }
            }
        }
        return allFile;
    }

    public static void clearFileList() {
        if (allFile != null) {
            allFile.clear();
        }
    }

    public static List<UsbFile> getAllImageFiles(UsbFile oriUsbFile) {
        try {
            if (oriUsbFile.isDirectory()) {
                UsbFile[] usbFiles = oriUsbFile.listFiles();
                for (UsbFile usbFile : usbFiles) {
                    getAllImageFiles(usbFile);
                }
            } else {
                String fileName = oriUsbFile.getName();
                int j = fileName.lastIndexOf(".");
                String suf = fileName.substring(j + 1);
                if (Constants.SUFFIX_IMAGE_JPG.equalsIgnoreCase(suf) ||
                        Constants.SUFFIX_IMAGE_PNG.equalsIgnoreCase(suf) ||
                        Constants.SUFFIX_IMAGE_JPEG.equalsIgnoreCase(suf) ||
                        Constants.SUFFIX_IMAGE_BMP.equalsIgnoreCase(suf)) {
                    allUsbFiles.add(oriUsbFile);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return allUsbFiles;
    }

    public static void clearUsbFileList() {
        if (allUsbFiles != null) {
            allUsbFiles.clear();
        }
    }

    /**
     * 刷新重试次数
     */
    public static int increaseRetryMapCount(ConcurrentHashMap<Integer, Integer> countMap, int requestId) {
        Integer count = countMap.get(requestId);
        int currentCount = count == null ? 1 : (count + 1);
        countMap.put(requestId, currentCount);
        return currentCount;
    }

    /**
     * 刷新跳帧次数
     */
    public static int increaseFtDelayMapCount(ConcurrentHashMap<Integer, Integer> countMap, int requestId) {
        Integer count = countMap.get(requestId);
        int currentCount = count == null ? 1 : (count + 1);
        if (currentCount > FT_SKIP_FRAME_TOTAL) {
            return currentCount;
        }
        countMap.put(requestId, currentCount);
        return currentCount;
    }

    /**
     * 刷新hashMap数据
     */
    public static void putConcurrentMapStatus(ConcurrentHashMap<Integer, Integer> hashMap, int trackId, int status) {
        if (hashMap != null && hashMap.containsKey(trackId)) {
            hashMap.put(trackId, status);
        }
    }

    /**
     * 获取HashMap中的值
     */
    public static int getMapTimeCount(ConcurrentHashMap<Integer, Integer> countMap, int requestId) {
        Integer count = countMap.get(requestId);
        return count == null ? 0 : count;
    }

    /**
     * 增加map中的值
     *
     * @param countMap
     * @param requestId
     * @return
     */
    public static int increaseMapValueCount(ConcurrentHashMap<Integer, Integer> countMap, int requestId) {
        Integer count = countMap.get(requestId);
        int newCount = (count == null) ? 1 : (count + 1);
        countMap.put(requestId, newCount);
        return newCount;
    }

    /**
     * 检测ip地址合理性
     *
     * @param s
     * @return
     */
    public static boolean checkAddress(String s) {
        return s.matches("((25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))");
    }

    /**
     * 检测端口号合理性
     *
     * @param s
     * @return
     */
    public static boolean checkPort(String s) {
        return s.matches("^[1-9]$|(^[1-9][0-9]$)|(^[1-9][0-9][0-9]$)|(^[1-9][0-9][0-9][0-9]$)|(^[1-6][0-5][0-5][0-3][0-5]$)");
    }

    /**
     * 创建新的人员personSerial
     *
     * @return
     */
    public static String createPersonSerial() {
        String macAddress = DeviceUtils.getMacAddress().replaceAll(":", "").trim();
        return macAddress + "_" + CommonRepository.getInstance().createDatabaseId();
    }

    /**
     * 离线局域网模式
     */
    public static boolean isOfflineLanAppMode() {
        int appMode = SPUtils.getInstance().getInt(Constants.SP_KEY_APP_MODE, Constants.APP_MODE_NONE);
        return appMode == Constants.APP_MODE_OFFLINE_LAN;
    }

    /**
     * 云端AIoT模式
     */
    public static boolean isCloudAiotAppMode() {
        int appMode = SPUtils.getInstance().getInt(Constants.SP_KEY_APP_MODE, Constants.APP_MODE_NONE);
        return appMode == Constants.APP_MODE_CLOUD_AI_OT;
    }

    /**
     * 将FaceDetect结果转成字符串
     *
     * @param faceInfo
     */
    public static String getFaceDetectResult(FaceInfo faceInfo) {
        if (faceInfo == null) {
            return "[]";
        }
        Rect faceRect = faceInfo.faceRect;
        if (faceRect == null) {
            return "[]";
        }
        return "[" + faceRect.left + "_" + faceRect.top + "_" + faceRect.right + "_"
                + faceRect.bottom + "_" + faceInfo.faceOrient + "]_";
    }

    /**
     * 将FaceDetect结果转成字符串
     *
     * @param faceInfo
     */
    public static String getStringFaceResult(FaceInfo faceInfo) {
        if (faceInfo == null) {
            return "[]";
        }
        Rect faceRect = faceInfo.faceRect;
        if (faceRect == null) {
            return "[]";
        }
        return "[" + faceRect.left + "_" + faceRect.top + "_" + faceRect.right + "_"
                + faceRect.bottom + "_" + faceInfo.faceOrient + "]";
    }

    /**
     * Unicode转中文
     *
     * @param unicodeStr
     * @return
     */
    public static String uniCodeToCN(String unicodeStr) {
        if (unicodeStr == null) {
            return null;
        }
        StringBuffer stringBuffer = new StringBuffer();
        int maxLoop = unicodeStr.length();
        for (int i = 0; i < maxLoop; i++) {
            if (unicodeStr.charAt(i) == '\\') {
                boolean flag = (i < maxLoop - 5) && ((unicodeStr.charAt(i + 1) == 'u') || (unicodeStr.charAt(i + 1) == 'U'));
                if (flag) {
                    try {
                        stringBuffer.append((char) Integer.parseInt(unicodeStr.substring(i + 2, i + 6), 16));
                        i += 5;
                    } catch (NumberFormatException localNumberFormatException) {
                        stringBuffer.append(unicodeStr.charAt(i));
                    }
                } else {
                    stringBuffer.append(unicodeStr.charAt(i));
                }
            } else {
                stringBuffer.append(unicodeStr.charAt(i));
            }
        }
        return stringBuffer.toString();
    }

    /**
     * 根据给定code获取提示
     *
     * @param res
     * @return
     */
    public static String getInitFaceEngineResult(int res) {
        SparseArray<String> msgList = CommonUtils.initFaceEngineErrorMsgList();
        return AppUtils.getString(R.string.face_engine_init_fail) + res + ":" + msgList.get(res);
    }

    /**
     * 获取字符串
     *
     * @param resId
     * @return
     */
    public static String getStrFromRes(int resId) {
        return Utils.getApp().getString(resId);
    }

    /**
     * 获取字符串
     *
     * @param resId
     * @return
     */
    public static String getStrFromRes(int resId, Object... formatArgs) {
        return Utils.getApp().getString(resId, formatArgs);
    }

    public static Drawable getDrawableFromRes(int resId) {
        return Utils.getApp().getResources().getDrawable(resId);
    }

    public static int getDimenFromRes(int resId) {
        return Utils.getApp().getResources().getDimensionPixelOffset(resId);
    }

    public static boolean isServiceRunning(Context context, String serviceName) {
        if (TextUtils.isEmpty(serviceName)) {
            return false;
        }
        ActivityManager myManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager
                .getRunningServices(50);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().equals(serviceName)) {
                return true;
            }
        }
        return false;
    }

    public static void hideNavigator(Window window) {
        int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        uiFlags |= View.SYSTEM_UI_FLAG_IMMERSIVE;
        try {
            window.getDecorView().setSystemUiVisibility(uiFlags);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 数据转换
     *
     * @return
     */
    public static RequestSetting getRequestSetting() {
        RequestSetting setting = new RequestSetting();
        TableSettingConfigInfo configInfo = CommonRepository.getInstance().getSettingConfigInfo();
        setting.setCompanyName(configInfo.getCompanyName());
        setting.setDevicePassword(configInfo.getDevicePassword());
        setting.setDisplayCustom(configInfo.getCustomDisplayModeFormat());
        setting.setDisplayMode(String.valueOf(configInfo.getDisplayMode()));
        setting.setInterval(configInfo.getRecognitionRetryDelay());
        setting.setLivenessType(configInfo.isLivenessDetect() ? configInfo.getLiveDetectType() : ConfigConstants.DEFAULT_LIVE_DETECT_CLOSE);
        setting.setMaxFaceSize(Integer.parseInt(configInfo.getMaxFaceTrackNumber()));
        setting.setOpenDelay(configInfo.getCloseDoorDelay());
        setting.setStrangerMode(String.valueOf(configInfo.getDisplayModeFail()));
        setting.setStrangerCustom(configInfo.getCustomFailDisplayModeFormat());
        setting.setStrangerVoiceMode(String.valueOf(configInfo.getVoiceModeFail()));
        setting.setStrangerVoiceCustom(configInfo.getCustomFailVoiceModeFormat());
        setting.setThreshold(configInfo.getSimilarThreshold());
        setting.setVoiceMode(String.valueOf(configInfo.getVoiceMode()));
        setting.setVoiceCustom(configInfo.getCustomVoiceModeFormat());
        setting.setSignDistance(configInfo.getRecognizeDistance());
        setting.setSuccessRetryDelay(configInfo.getSuccessRetryDelay());
        setting.setSuccessRetry(configInfo.getSuccessRetry());
        setting.setUploadRecordImage(configInfo.getUploadRecordImage());
        setting.setIrLivePreview(configInfo.getIrLivePreview());
        setting.setRebootEveryDay(configInfo.isRebootEveryDay() ? ConfigConstants.DEVICE_REBOOT_OPEN :
                ConfigConstants.DEVICE_REBOOT_CLOSE);
        setting.setRebootHour(configInfo.getRebootHour());
        setting.setRebootMin(configInfo.getRebootMin());
        setting.setSignType(configInfo.getSignType());
        String mainLogo = SPUtils.getInstance().getString(Constants.SP_KEY_CLOUD_MAIN_LOGO_URL);
        setting.setMainLogoUrl(mainLogo);
        String secondLogo = SPUtils.getInstance().getString(Constants.SP_KEY_CLOUD_SECOND_LOGO_URL);
        setting.setSecondLogoUrl(secondLogo);
        setting.setFaceQuality(configInfo.isFaceQuality() ? 1 : 0);
        setting.setFaceQualityThreshold(configInfo.getFaceQualityThreshold());
        setting.setDeviceName(configInfo.getDeviceName());
        return setting;
    }

    /**
     * 获取扩大后的IR人脸框
     *
     * @param oriRect
     * @return
     */
    public static Rect getPaddingIrRect(Rect oriRect) {
        int widthRect = oriRect.width();
        int heightRect = oriRect.height();
        int widthPadding = (int) (widthRect * IR_FACE_RECT_PADDING_RATIO);
        int heightPadding = (int) (heightRect * IR_FACE_RECT_PADDING_RATIO);
        return new Rect(oriRect.left - widthPadding, oriRect.top - heightPadding,
                oriRect.right + widthPadding, oriRect.bottom + heightPadding);
    }

    /**
     * 获取图片压缩格式
     *
     * @param suffix
     * @return
     */
    public static Bitmap.CompressFormat getCompressFormat(String suffix) {
        if (TextUtils.isEmpty(suffix)) {
            return null;
        }
        Bitmap.CompressFormat format;
        switch (suffix) {
            case Constants.SUFFIX_IMAGE_PNG:
                format = Bitmap.CompressFormat.PNG;
                break;
            case Constants.SUFFIX_IMAGE_BMP:
                format = Bitmap.CompressFormat.WEBP;
                break;
            default:
                format = Bitmap.CompressFormat.JPEG;
                break;
        }
        return format;
    }

    public static Bitmap getBitmapFromUsbFile(final InputStream inputStream, int maxWidth, int maxHeight,
                                              FileSystem fileSystem) throws IOException {
        Bitmap tempBitmap = ImageFileUtils.getBitmap(inputStream, maxWidth, maxHeight, fileSystem.getChunkSize());
        if (tempBitmap != null) {
            if (tempBitmap.getWidth() > maxWidth || tempBitmap.getHeight() > maxHeight) {
                Bitmap scaleBitmap = ImageFileUtils.resizeImage(tempBitmap, maxWidth, maxHeight);
                if (!tempBitmap.isRecycled()) {
                    tempBitmap.recycle();
                }
                return scaleBitmap;
            }
            return tempBitmap;
        }
        return null;
    }

    @SuppressLint("SdCardPath")
    public static void backUpDataBase(Context context, String dbName, String desPath,
                                      PersonListRepository.BackUpDatabaseListener listener) {
        InputStream myInput;
        try {
            File dbFile = context.getDatabasePath(dbName);
            myInput = new FileInputStream(dbFile);
            OutputStream myOutput = new FileOutputStream(desPath);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }
            myOutput.flush();
            myOutput.close();
            myInput.close();
            listener.onSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            listener.onFailed();
        }
    }

    /**
     * 判断小数点位数
     *
     * @param strParam
     * @return
     */
    public static int checkIsDoublePointTwo(String strParam) {
        if (TextUtils.isEmpty(strParam)) {
            return 0;
        }
        BigDecimal bd = new BigDecimal(strParam);
        String[] ss = bd.toString().split("\\.");
        if (ss.length <= 1) {
            return 0;
        }
        return ss[1].length();
    }

    /**
     * 读取U盘中的文件内容
     *
     * @param usbFile
     * @return
     */
    public static LinkedHashSet<String> getFilesFromIo(UsbFile usbFile) {
        return getFilesFromIo(usbFile, UsbHelper.getInstance().getFileSystem());
    }

    /**
     * 读取U盘中的文件内容
     *
     * @param usbFile
     * @param fs
     * @return
     */
    public static LinkedHashSet<String> getFilesFromIo(UsbFile usbFile, FileSystem fs) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        BufferedInputStream inputStream = null;
        try {
            inputStream = UsbFileStreamFactory.createBufferedInputStream(usbFile, fs);
            List<String> list = FileUtils.readFileByLine(inputStream, 0, 0X7FFFFFFF);
            if (list != null && list.size() > 0) {
                for (String s : list) {
                    if (!TextUtils.isEmpty(s)) {
                        set.add(s);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return set;
    }

    /**
     * 比较时间大小
     *
     * @return 允许通行:time2 <= time1 < time3
     */
    public static boolean permissionTimeCompare(String time1, String time2, String time3) {
        return !TimeUtils.compareTime(time1, time2) && TimeUtils.compareTime(time1, time3);
    }

    /**
     * 将一组数据平均分成n组
     *
     * @param source 要分组的数据源
     * @param n      平均分成n组
     * @param <T>
     * @return
     */
    public static <T> List<List<T>> averageAssign(List<T> source, int n) {
        List<List<T>> result = new ArrayList<>();
        int remainder = source.size() % n;
        int number = source.size() / n;
        int offset = 0;
        for (int i = 0; i < n; i++) {
            List<T> value = null;
            if (remainder > 0) {
                value = source.subList(i * number + offset, (i + 1) * number + offset + 1);
                remainder--;
                offset++;
            } else {
                value = source.subList(i * number + offset, (i + 1) * number + offset);
            }
            result.add(value);
        }
        return result;
    }

    /**
     * 获取人员图片保存文件夹名
     *
     * @param imageName
     * @return
     */
    public static String getPersonFaceLocalPath(String imageName) {
        String localDir = "0";
        if (TextUtils.isEmpty(imageName)) {
            imageName = CommonRepository.getInstance().createDatabaseId();
        }
        String imageNameMd5 = Md5Utils.encode(imageName);
        if (!TextUtils.isEmpty(imageNameMd5)) {
            localDir = imageNameMd5.substring(0, 1);
        }
        return SdcardUtils.getInstance().getRegisteredDirPath() + File.separator + localDir +
                File.separator + imageName + ".jpg";
    }

    /**
     * 获取人员图片保存文件夹名
     * @param weekList 工作日列表
     * @return 工作日对应符号
     */
    public static String getWorkingDay(List<WeekEnum> weekList) {
        if (weekList == null || weekList.isEmpty()) {
            return "";
        }
        String stringWorkingDay;
        StringBuilder stringBuilder = new StringBuilder();
        int size = weekList.size();
        for (int i = 0; i < size; i++) {
            stringBuilder.append(getWeekDay(weekList.get(i)));
            if (i < size - 1) {
                stringBuilder.append(",");
            }
        }
        stringWorkingDay = stringBuilder.toString();
        return stringWorkingDay;
    }

    private static int getWeekDay(WeekEnum weekEnum) {
        int day;
        switch (weekEnum) {
            case TUES:
                day = 2;
                break;
            case WED:
                day = 3;
                break;
            case THUR:
                day = 4;
                break;
            case FRI:
                day = 5;
                break;
            case SAT:
                day = 6;
                break;
            case SUN:
                day = 7;
                break;
            default:
                day = 1;
        }
        return day;
    }

    public static String getWeekDayString(int day) {
        String strDay;
        switch (day) {
            case 1:
                strDay = CommonUtils.getStrFromRes(R.string.monday);
                break;
            case 2:
                strDay = CommonUtils.getStrFromRes(R.string.tuesday);
                break;
            case 3:
                strDay = CommonUtils.getStrFromRes(R.string.wednesday);
                break;
            case 4:
                strDay = CommonUtils.getStrFromRes(R.string.thursday);
                break;
            case 5:
                strDay = CommonUtils.getStrFromRes(R.string.friday);
                break;
            case 6:
                strDay = CommonUtils.getStrFromRes(R.string.saturday);
                break;
            default:
                strDay = CommonUtils.getStrFromRes(R.string.sunday);
        }
        return strDay;
    }

    /**
     * 求最大公约数
     * @param m
     * @param n
     * @return
     */
    public static int getCommonDivisor(int m, int n) {
        int t = Math.min(m, n);
        while (m % t != 0 || n % t != 0) {
            t--;
        }
        return t;
    }

    public static void keepMaxFace(List<FaceInfo> ftFaceList) {
        if (ftFaceList == null || ftFaceList.size() <= 1) {
            return;
        }
        FaceInfo maxFaceInfo = ftFaceList.get(0);
        for (FaceInfo faceInfo : ftFaceList) {
            if (faceInfo.faceRect.width() > maxFaceInfo.faceRect.width()) {
                maxFaceInfo = faceInfo;
            }
        }
        ftFaceList.clear();
        ftFaceList.add(maxFaceInfo);
    }

    public static String[] getNeededPermissions() {
        return NEEDED_PERMISSIONS;
    }

    /**
     * 给定Uri,获取图片绝对路径
     *
     * @param context
     * @param contentUri
     * @return
     */
    public static String getRealPathFromUri(Context context, Uri contentUri) {
        String result;
        Cursor cursor = context.getContentResolver().query(contentUri,
                new String[]{MediaStore.Images.ImageColumns.DATA},
                null, null, null);
        if (cursor == null) {
            result = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(index);
            cursor.close();
        }
        return result;
    }

    public static DeviceCameraInfo getDeviceCameraInfo() {
        DeviceCameraInfo deviceCameraInfo = new DeviceCameraInfo();
        ICameraEngine cameraEngine = CameraEngineUtils.createCameraFaceEngine();
        deviceCameraInfo.setCameraPosList(cameraEngine.getCameraPositionList());
        deviceCameraInfo.setCameraSizeList(cameraEngine.getCameraPreviewSizeList());
        for (Pair<Integer, Integer> pair : deviceCameraInfo.getCameraSizeList()) {
            if (pair.first == DEF_PREVIEW_WIDTH && pair.second == DEF_PREVIEW_HEIGHT) {
                deviceCameraInfo.setDefPreviewW(DEF_PREVIEW_WIDTH);
                deviceCameraInfo.setDefPreviewH(DEF_PREVIEW_HEIGHT);
                break;
            }
        }
        return deviceCameraInfo;
    }
}