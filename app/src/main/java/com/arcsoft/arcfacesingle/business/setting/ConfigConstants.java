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

package com.arcsoft.arcfacesingle.business.setting;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.util.SdcardUtils;
import com.arcsoft.asg.libcommon.util.common.Utils;

import java.io.File;

public class ConfigConstants {

    /**
     * 刷脸记录类型：1 签到；2 签退；3 签到、签退；4 人员无权限；-1 失败
     */
    public static final int TYPE_SIGN_FAIL = -1;
    public static final int TYPE_SIGN_IN = 1;
    public static final int TYPE_SIGN_OUT = 2;
    public static final int TYPE_SIGN_BOTH = 3;
    public static final int TYPE_SIGN_UNAUTHORIZED = 4;

    /**
     * AIot模式刷脸记录类型
     */
    public static final int TYPE_AIOT_RECOGNIZE_SUCCESSFUL = 1;
    public static final int TYPE_AIOT_RECOGNIZE_FAILED = 2;
    public static final int TYPE_AIOT_RECOGNIZE_UNAUTHORIZED = 3;

    /**
     * 设备端口最小值
     */
    public static final int DEVICE_PORT_MIN = 1024;

    /**
     * 设备端口最大值
     */
    public static final int DEVICE_PORT_MAX = 65535;

    /**
     * 识别阈值最大值
     */
    public static final float THRESHOLD_MAX = 1;
    public static final float THRESHOLD_MIN = 0;

    /**
     * 默认待机屏幕亮度
     */
    public static final int SCREEN_DEFAULT_BRIGHT = 100;

    /**
     * 设备重启小时数最大值
     */
    public static final int DEVICE_REBOOT_HOUR = 23;
    public static final int DEVICE_REBOOT_HOUR_MIN = 0;
    public static final String DEFAULT_DEVICE_REBOOT_HOUR = "4";

    /**
     * 设备重启分钟数最大值
     */
    public static final int DEVICE_REBOOT_MIN = 59;
    public static final int DEVICE_REBOOT_MIN_MIN = 0;
    public static final String DEFAULT_DEVICE_REBOOT_MIN = "30";

    /**
     *重试延迟时间最小值
     */
    public static final int RETRY_DELAY_MIN = 1;

    /**
     *重试延迟时间最大值
     */
    public static final int RETRY_DELAY_MAX = 10;

    /**
     *延迟时间最大值
     */
    public static final int TIME_DELAY_MAX = 100;

    /**
     * 检测距离默认值
     */
    public static final int DEFAULT_N_SCALE = 16;

    /**
     * 输出信息，开门
     */
    public static final int OUT_PUT_MODE_OPEN_DOOR = 2;
    /**
     * 输出信息，自定义
     */
    public static final int OUT_PUT_MODE_CUSTOM = 100;
    /**
     * 输出信息，自定义
     */
    public static final int OUT_PUT_MODE_CUSTOM_MIN = 0;

    /**
     * 成功语音模式：不选择任何类型
     */
    public static final int SUCCESS_VOICE_MODE_NONE = 0;
    /**
     * 成功语音模式：不播报
     */
    public static final int SUCCESS_VOICE_MODE_NO_PLAY = 1;
    /**
     * 成功语音模式：播报姓名
     */
    public static final int SUCCESS_VOICE_MODE_NAME = 2;
    /**
     * 成功语音模式：播报预置音（3：识别成功；4：欢迎光临；5：门已打开；6：门已打开，欢迎光临）
     */
    public static final int SUCCESS_VOICE_MODE_PREVIEW_TYPE3 = 3;
    public static final int SUCCESS_VOICE_MODE_PREVIEW_TYPE4 = 4;
    public static final int SUCCESS_VOICE_MODE_PREVIEW_TYPE5 = 5;
    public static final int SUCCESS_VOICE_MODE_PREVIEW_TYPE6 = 6;
    /**
     * 成功语音模式：播报自定义内容
     */
    public static final int SUCCESS_VOICE_MODE_CUSTOM = 100;
    public static final String DEFAULT_SUCCESS_VOICE_MODE_CUSTOM_VALUE = "";

    /**
     * ----------------------------------------------------------------------------------------
     * 失败语音模式：不选择任何类型
     */
    public static final int FAILED_VOICE_MODE_NONE = 0;
    /**
     * 失败语音模式：不播报
     */
    public static final int FAILED_VOICE_MODE_NO_PLAY = 1;
    /**
     * 失败语音模式：播报警报音
     */
    public static final int FAILED_VOICE_MODE_WARN = 2;
    /**
     * 失败语音模式：播报预置音（3：识别失败；4：验证不通过）
     */
    public static final int FAILED_VOICE_MODE_PREVIEW_TYPE3 = 3;
    public static final int FAILED_VOICE_MODE_PREVIEW_TYPE4 = 4;
    /**
     * 失败语音模式：播报自定义内容
     */
    public static final int FAILED_VOICE_MODE_CUSTOM = 100;
    public static final String FAILED_VOICE_MODE_CUSTOM_DEFAULT = "";

    /**
     * ----------------------------------------------------------------------------------------
     * 成功显示模式：显示姓名
     */
    public static final int DISPLAY_MODE_SUCCESS_NAME = 1;
    /**
     * 成功显示模式：隐藏最后一个字符
     */
    public static final int DISPLAY_MODE_HIDE_LAST_CHAR = 2;
    /**
     * 成功显示模式：显示自定义内容
     */
    public static final int DISPLAY_MODE_SUCCESS_CUSTOM = 100;
    public static final String DISPLAY_MODE_SUCCESS_CUSTOM_VALUE = "";

    /**
     * ----------------------------------------------------------------------------------------
     * 失败显示模式：默认标示
     */
    public static final int DISPLAY_MODE_FAILED_DEFAULT_MARKUP = 1;
    /**
     * 失败显示模式：不反馈
     */
    public static final int DISPLAY_MODE_FAILED_NOT_FEEDBACK = 2;
    /**
     * 失败显示模式：自定义
     */
    public static final int DISPLAY_MODE_FAILED_CUSTOM = 100;
    /**
     * 失败显示自定义内容
     */
    public static final String DISPLAY_MODE_FAILED_CUSTOM_VALUE = "";

    /**
     * ----------------------------------------------------------------------------------------
     * 人脸识别模式，仅识别最大人脸
     */
    public static final int RECOGNIZE_MODE_MAX_FACE = 1;

    /**
     * 人脸识别模式，多人脸识别
     */
    public static final int RECOGNIZE_MODE_MULTI_FACE = 2;

    /**
     * 签到类型
     */
    public static final int DEFAULT_SIGN_TYPE = 3;

    /**
     * 识别距离：很近
     */
    public static final int RECOGNITION_DISTANCE_TYPE1 = 1;

    /**
     * 识别距离：近
     */
    public static final int RECOGNITION_DISTANCE_TYPE2 = 2;

    /**
     * 识别距离：中等
     */
    public static final int RECOGNITION_DISTANCE_TYPE3 = 3;

    /**
     * 识别距离：较远
     */
    public static final int RECOGNITION_DISTANCE_TYPE4 = 4;

    /**
     * 识别距离：远
     */
    public static final int RECOGNITION_DISTANCE_TYPE5 = 5;

    /**
     * 相册选择包名
     */
    public static final String GALLERY_AUTHORITY = Utils.getApp().getPackageName() + ".fileprovider";

    /**
     * 选择主LOGO的RequestCode
     */
    public static final int CHOOSE_PICTURE_MAIN_LOGO = 0;

    /**
     * 选择副LOGO的RequestCode
     */
    public static final int CHOOSE_PICTURE_SECOND_LOGO = 1;

    /**
     * 0 活体检测关闭；1 RGB活体检测；2 IR活体检测
     */
    public static final int DEFAULT_LIVE_DETECT_CLOSE = 0;
    public static final int DEFAULT_LIVE_DETECT_RGB = 1;
    public static final int DEFAULT_LIVE_DETECT_IR = 2;

    /**
     * 设置信息默认值
     */
    public static final String DEFAULT_THRESHOLD = "0.8";
    public static final boolean DEFAULT_BOOL_FALSE = false;
    public static final boolean DEFAULT_BOOL_TRUE = true;
    public static final float MAX_THRESHOLD = 1;
    public static final float MIN_THRESHOLD = 0;
    public static final String DEFAULT_MAX_RECOGNIZE_NUM = "1";
    public static final boolean DEFAULT_IS_VERTICAL_MIRROR = false;
    public static final boolean DEFAULT_IS_HORIZONTAL_MIRROR = false;
    public static final boolean DEFAULT_IS_USE_FRONT_CAMERA = true;
    public static final boolean DEFAULT_IR_FACE_RECT_VERTICAL_MIRROR = false;
    public static final boolean DEFAULT_IR_FACE_RECT_HORIZONTAL_MIRROR = false;
    public static final String DEFAULT_CLOSE_DOOR_DELAY = "6.0";
    public static final String DEFAULT_DEVICE_PASSWORD = "123456";
    public static final String DEFAULT_RETRY_DELAY = "2.0";
    public static final String DEFAULT_DEVICE_PORT = "3639";
    public static final String DEFAULT_SERVER_PORT = "8080";
    public static final String DEFAULT_SERVER_IP = "";
    public static final String DEFAULT_OPEN_DOOR_TYPE = Utils.getApp().getResources().getString(R.string.default_open_door_type);
    public static final int DEFAULT_CAMERA_PREVIEW_DEGREE = Utils.getApp().getResources().getInteger(R.integer.camera_preview_degree);
    public static final int DEFAULT_FACE_DETECT_DEGREE = Utils.getApp().getResources().getInteger(R.integer.face_detect_degree);
    public static final String DEFAULT_MAIN_LOGO_FILE_PATH = SdcardUtils.getInstance().getCompanyLogo() + File.separator + "main.png";
    public static final String DEFAULT_SECOND_LOGO_FILE_PATH = SdcardUtils.getInstance().getCompanyLogo() + File.separator + "second.png";
    public static final String DEFAULT_COMPANY_NAME = Utils.getApp().getString(R.string.company_name);
    public static final String DEFAULT_IR_LIVE_THRESHOLD = "0.7";
    public static final String DEFAULT_FACE_QUALITY_THRESHOLD = "0.35";
    /**
     * 设置参数中的int值：1 true；0 false
     */
    public static final int DEFAULT_UPLOAD_RECORD_IMAGE = 1;
    public static final int DEFAULT_UPLOAD_RECORD_IMAGE_CLOSE = 0;
    public static final int RECOGNITION_SUCCESS_RETRY_OPEN = 1;
    public static final int DEFAULT_RECOGNITION_SUCCESS_RETRY = 0;
    public static final int DEFAULT_IR_LIVE_PREVIEW = 1;
    public static final int DEFAULT_IR_LIVE_PREVIEW_HIDE = 0;
    public static final int DEVICE_REBOOT_OPEN = 1;
    public static final int DEVICE_REBOOT_CLOSE = 0;

    /**
     * 门禁权限默认开始时间
     */
    public static final String DOOR_AUTHORITY_DEFAULT_START_TIME = "00:00:00";

    /**
     * 门禁权限默认结束时间
     */
    public static final String DOOR_AUTHORITY_DEFAULT_END_TIME = "23:59:59";
}
