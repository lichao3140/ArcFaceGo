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

package com.arcsoft.arcfacesingle.app;

import com.arcsoft.asg.libcommon.util.common.ArithmeticUtils;

public class Constants {

    //*****************************************全局配置相关常量*********************************************//
    /**
     * 应用模式 0 没有选择任何模式；1 离线局域网模式；2云端AIoT模式
     */
    public static final int APP_MODE_NONE = 0;
    public static final int APP_MODE_OFFLINE_LAN = 1;
    public static final int APP_MODE_CLOUD_AI_OT = 2;

    /**
     * AIot服务器地址
     */
    public static final String ARC_LINK_SERVER_ADDRESS = "https://link.arcsoftai.com";

    /**
     * log日志保存至本地开关
     */
    public static final boolean SWITCH_SAVE_LOG = false;
    /**
     * log打印到控制台开关
     */
    public static final boolean SWITCH_PRINT_LOG = false;
    /**
     * IR图片保存开关
     */
    public static final boolean SWITCH_SAVE_IR_PICTURE = false;
    /**
     * 人脸检测小失败图片保存开关
     */
    public static final boolean SWITCH_SAVE_PLEASE_FACE_UP_CAMERA = false;
    /**
     * 识别记录NV21裸数据保存至本地开关
     */
    public static final boolean SWITCH_SAVE_NV21_RECOGNIZE_RECORD = false;

    /**
     * 数据库名称
     */
    public static final String DATA_BASE_NAME = "AppDatabase";
    public static final String DATA_BASE_NAME_PATH = "AppDatabase.db";

    /**
     * 人脸特征值版本：V2.0
     */
    public static final String FACE_FEATURE_VERSION_V2 = "V2";
    /**
     * 人脸特征值版本：V3.0
     */
    public static final String FACE_FEATURE_VERSION_V30 = "V3_0";
    public static final String FACE_FEATURE_VERSION_V3_0 = "3.0";

    /**
     * ArcFace SDK大版本：V3.0
     */
    public static final String FACE_ENGINE_VERSION_V3 = "3";

    /**
     * ArcFace SDK大版本：V4.0
     */
    public static final String FACE_ENGINE_VERSION_V4 = "4";
    //******************************************************************************************************//

    /**
     * IR相机预览和RGB相机预览大小比例
     */
    public static final int IR_PREVIEW_RECT_RATIO = 4;
    public static final int TYPE_CAMERA_RGB = 0;
    public static final int TYPE_CAMERA_IR = 1;

    /**
     * 系统存储空间限制相关常量：150 * 1024 * 1024
     */
    public static final long SDCARD_STORAGE_SIZE_SAFE = (long) ArithmeticUtils.mul(ArithmeticUtils.mul(150, 1024), 1024);
    public static final long SDCARD_STORAGE_SIZE_DELETE = (long) ArithmeticUtils.mul(ArithmeticUtils.mul(300, 1024), 1024);

    /**
     * 首页相机预览全屏判断需要的边距值
     */
    public static final double SCREEN_PREVIEW_RATIO_SUB = 0.30;
    public static final int DEFAULT_TRACK_ID = 0;

    //*****************************************存储相关Key值*********************************************//

    public static final String SP_KEY_TRACK_ID = "KEY_TRACK_ID";
    public static final String SP_KEY_APP_ID = "KEY_APP_ID";
    public static final String SP_KEY_SDK_KEY = "KEY_SDK_KEY";
    public static final String SP_KEY_ACTIVE_KEY = "KEY_ACTIVE_KEY";
    public static final String SP_KEY_ADAPTATION_INFO = "SP_KEY_ADAPTATION_INFO";
    public static final String SP_KEY_APP_MODE = "SP_KEY_APP_MODE";
    public static final String SP_KEY_MAIN_LOGO_ID = "SP_KEY_MAIN_LOGO_ID";
    public static final String SP_KEY_SECOND_LOGO_ID = "SP_KEY_SECOND_LOGO_ID";
    public static final String SP_KEY_FROM_SPLASH = "KEY_FROM_SPLASH";
    public static final String SP_KEY_DEVICE_ACCESS_SUCCESS = "SP_KEY_DEVICE_ACCESS_SUCCESS";
    public static final String SP_KEY_DEVICE_ACCESS_ID = "SP_KEY_DEVICE_ACCESS_ID";
    public static final String SP_KEY_DEVICE_OLD_ACCESS_ID = "SP_KEY_DEVICE_OLD_ACCESS_ID";
    public static final String SP_KEY_DEVICE_TAG = "SP_KEY_DEVICE_TAG";
    public static final String SP_KEY_ARC_LINK_SERVER_ADDRESS = "SP_KEY_ARC_LINK_SERVER_ADDRESS";
    public static final String SP_KEY_CLOUD_MAIN_LOGO_URL = "SP_KEY_CLOUD_MAIN_LOGO_URL";
    public static final String SP_KEY_CLOUD_SECOND_LOGO_URL = "SP_KEY_CLOUD_SECOND_LOGO_URL";
    public static final String SP_KEY_LOCAL_APK_CRC = "SP_KEY_LOCAL_APK_CRC";
    public static final String SP_KEY_OLD_VERSION_CODE = "SP_KEY_OLD_VERSION_CODE";
    public static final String SP_KEY_UPGRADE_ID = "SP_KEY_UPGRADE_ID";
    public static final String SP_KEY_ARC_LINK_PERSON_SYNC_KEY = "SP_KEY_AIOT_PERSON_SYNC_KEY";
    public static final String SP_KEY_ARC_LINK_PERSON_SYNC_EXCEPTION = "SP_KEY_AIOT_PERSON_SYNC_EXCEPTION";
    public static final String SP_KEY_LATEST_VERSION_CODE = "SP_KEY_LATEST_VERSION_CODE";
    public static final String SP_KEY_BROADCAST_PACKAGE_NAME = "SP_KEY_BROADCAST_PACKAGE_NAME";
    public static final String SP_KEY_AGREE_OPEN_SOURCE = "SP_KEY_AGREE_OPEN_SOURCE";

    public static final String BUNDLE_KEY_PERSON_SERIAL = "KEY_PERSON_SERIAL";

    public static final String HEADER_SIGN = "sign";
    //***************************************************************************************************//

    /**
     * 首页View展示类型
     * 0 整个界面竖屏显示，且摄像头预览为竖屏；
     * 1 整个界面竖屏显示，且摄像头预览为横屏；
     * 3 整个界面横屏显示，且摄像头预览为横屏；
     * 4 整个界面横屏显示，且摄像头预览为竖屏；
     */
    public static final int MAIN_VIEW_SHOW_PORTRAIT_FULL = 0;
    public static final int MAIN_VIEW_SHOW_PORTRAIT_HALF = 1;
    public static final int MAIN_VIEW_SHOW_LANDSCAPE_FULL = 3;
    public static final int MAIN_VIEW_SHOW_LANDSCAPE_HALF = 4;

    /**
     * 人脸总数
     */
    public static final int SIZE_FACE_TOTAL_LEVEL0 = 0;
    public static final int SIZE_FACE_TOTAL_LEVEL1 = 1000;
    public static final int SIZE_FACE_TOTAL_LEVEL2 = 2000;

    /**
     * 向外开放广播类型
     */
    public static final String ACTION_IDENTIFY_SUCCESSFUL = "com.arcsoft.arcfacesingle.ACTION_IDENTIFY_SUCCESSFUL";
    public static final String ACTION_IDENTIFY_SUCCESS_PERSON_SERIAL = "com.arcsoft.arcfacesingle.ACTION_IDENTIFY_SUCCESS_PERSON_SERIAL";
    public static final String ACTION_IDENTIFY_FAILED = "com.arcsoft.arcfacesingle.ACTION_IDENTIFY_FAILED";
    public static final String ACTION_IDENTIFY_NO_PERMISSION_ACCESS = "com.arcsoft.arcfacesingle.ACTION_NO_PERMISSION_ACCESS";
    public static final String ACTION_OPEN_DOOR = "com.arcsoft.arcfacesingle.ACTION_OPEN_DOOR";
    public static final String ACTION_CLOSE_DOOR = "com.arcsoft.arcfacesingle.ACTION_CLOSE_DOOR";
    public static final String ACTION_TURN_ON_RED_LIGHT = "com.arcsoft.arcfacesingle.ACTION_TURN_ON_RED_LIGHT";
    public static final String ACTION_TURN_ON_GREEN_LIGHT = "com.arcsoft.arcfacesingle.ACTION_TURN_ON_GREEN_LIGHT";
    public static final String PERMISSION_DEVICE_REMOTE_SERVICE = "com.arcsoft.arcfacesingle.permissions.DEVICE_SERVICE";
    public static final String ACTION_START_IDENTIFY = "com.arcsoft.arcfacesingle.ACTION_START_IDENTIFY";
    public static final String ACTION_STOP_IDENTIFY = "com.arcsoft.arcfacesingle.ACTION_STOP_IDENTIFY";
    public static final String ACTION_DETECT_FACE_HAS_FACE = "com.arcsoft.arcfacesingle.ACTION_FACE_DETECT_HAS_FACE";
    public static final String ACTION_DETECT_FACE_NO_FACE = "com.arcsoft.arcfacesingle.ACTION_FACE_DETECT_NO_FACE";

    /**
     * 注册相关
     */
    public static final int FACE_REGISTER_MAX_WIDTH = 1024;
    public static final int FACE_REGISTER_MAX_WIDTH_1536 = 1024;
    public static final int FACE_REGISTER_MAX_HEIGHT = 1024;
    public static final int MAX_LOGO_PICTURE_SIZE = 2 * 1024 * 1024;
    public static final int MAX_PICTURE_SCALE = 150;
    public static final int FACE_DETECT_IMAGE_WIDTH_LIMIT = 4;
    public static final int FACE_DETECT_IMAGE_HEIGHT_LIMIT = 2;
    /**
     * face3d角度
     */
    public static final int FACE_ANGLE_YAW_MAX = 30;
    public static final int FACE_ANGLE_PITCH_MAX = 25;

    /**
     * 人脸检测引擎配置相关
     */
    public static final String ASF_OP_0_ONLY = "0°";
    public static final String DEGREE_90_STRING = "90°";
    public static final String DEGREE_180_STRING = "180°";
    public static final String DEGREE_270_STRING = "270°";
    public static final String DEGREE_ALL_STRING = "360°";
    public static final int DEGREE_0 = 0;
    public static final int DEGREE_90 = 90;
    public static final int DEGREE_180 = 180;
    public static final int DEGREE_270 = 270;
    public static final int DEGREE_ALL = 360;
    public static final int FACE_ENGINE_IMAGE_SCALE = 30;
    public static final int FACE_ENGINE_IMAGE_MAX_NUMBER = 5;
    public static final int FACE_DETECT_NUMBER_ONE = 1;
    public static final int FACE_DETECT_NUMBER_FIVE = 5;

    /**
     * 心跳相关
     */
    public static final int DELAY_SEND_HEART = 60 * 1000;
    public static final int INIT_SEND_HEART = 4 * 1000;

    /**
     * 图片后缀类型
     */
    public static final String SUFFIX_IMAGE_JPG = "jpg";
    public static final String SUFFIX_IMAGE_PNG = "png";
    public static final String SUFFIX_IMAGE_JPEG = "jpeg";
    public static final String SUFFIX_IMAGE_BMP = "bmp";

    //*****************************************参数设置相关常量********************************************//
    /**
     * 重启延迟时间
     */
    public static final int DEVICE_REBOOT_DELAY = 1500;

    public static final String STRING_ACTIVE_FILE_COMPILE =
            "`~!@#$%^&*()+=|\\{\\}':;',\\[\\].<>/?~！@#￥%……&*（）+|\\{\\}【】‘；：”“’。，、？";
    /**
     * 字符输入长度限制
     */
    public static final int MESSAGE_INPUT_STRING_MAX_LENGTH = 30;

    /**
     * 基本符号类型
     */
    public static final String CHAR_DOUBLE_QUOTATION_MARKS = "";
    public static final String CHAR_POINT_MARKS = ".";

    public static final float DEFAULT_FACE_QUALITY = 0.35f;

    /**
     * 警告弹框持续显示时间
     */
    public static final int DIALOG_DISMISS_DELAY = 10 * 1000;

    //***************************************************************************************************//

    public static final int HTTP_REQUEST_CODE_SUCCESS = 200;

    public static final String USB_FILE_NAME_ADAPTATION = "adaptation.dat";
    public static final String USB_FILE_NAME_SETTING = "configuration.dat";
    public static final String USB_FILE_MAIN_LOGO = "main_logo.png";
    public static final String USB_FILE_SUB_LOGO = "sub_logo.png";

    public static final String STRING_EMPTY = "";
}
