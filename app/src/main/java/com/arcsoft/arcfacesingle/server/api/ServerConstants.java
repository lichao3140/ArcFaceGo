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

package com.arcsoft.arcfacesingle.server.api;

public class ServerConstants {

    /********************************************APP向外开放的API接口一些基本常量**********************************************/
    public static final int SHOW_WARN_DIALOG_MAX_FACE_LIMIT = 100;
    public static final int DATE_TIME_STRING_MAX_LENGTH = 9;
    public static final String DEFAULT_START_TIME = "00:00:00";
    public static final String DEFAULT_END_TIME = "23:59:59";
    public static final String DEFAULT_WORKING_DAYS = "1,2,3,4,5,6,7";
    public static final String DEFAULT_START_DATE = "";
    public static final String DEFAULT_END_DATE = "";
    public static final String DEFAULT_SERVER_START_DATE = "2020-04-13";
    public static final String DEFAULT_SERVER_END_DATE = "2070-04-13";
    public static final String WEEK_NO_PERMISSION = "";
    public static final String MSG_PARAM_SUCCESS = "success";
    public static final int IC_CARD_NO_MAX_LENGTH = 50;
    public static final int THRESHOLD_POINT_MAX_LENGTH = 2;
    public static final int FAILED_RETRY_POINT_MAX_LENGTH = 1;
    public static final int NUMBER_PORT_MIN = 1;
    public static final int NUMBER_PORT_MAX = 65535;
    public static final String TAG_MORNING = "morning";
    public static final String TAG_AFTERNOON = "afternoon";
    public static final String TAG_EVENING = "evening";

    /********************************************APP向外开放的API接口返回值和信息常量**********************************************/
    public static final String MSG_RESPONSE_REQUEST_SUCCESS = "请求成功";
    public static final String MSG_RESPONSE_FAILED = "请求失败";
    public static final String MSG_RESPONSE_HEADER_EMPTY = "请求的header无效";
    public static final String MSG_RESPONSE_CONTENT_TYPE_EMPTY = "请求的content-type无效";
    public static final String MSG_RESPONSE_URI_INVALID = "请求的API地址无效";
    public static final String MSG_REQUEST_INVALID_AND_RETRY = "无效请求，请检查后重试";
    public static final String MSG_RESPONSE_JSON_INVALID = "请求的Json数据无效";
    public static final String MSG_RESPONSE_SIGN_INVALID = "请求参数‘sign’为空";
    public static final String MSG_RESPONSE_SIGN_KEY_INVALID = "设备端signKey为空";
    public static final String MSG_RESPONSE_SIGN_FAILED = "签名校验错误";
    public static final String MSG_RESPONSE_DEVICE_MAC_ADDRESS_EMPTY = "设备端Mac地址无效";
    public static final String MSG_RESPONSE_MAC_ADDRESS_EQUAL_FAILED = "Mac地址匹配失败";
    public static final String MSG_RESPONSE_SERIAL_NUMBER_EQUAL_FAILED = "设备SN号匹配失败";
    public static final String MSG_RESPONSE_DEVICE_SDCARD_STORAGE_LESS_MIN = "设备端存储空间不足，请删除一些文件";
    public static final String MSG_RESPONSE_SERVER_IP_INVALID = "给定的管理端ip地址无效";
    public static final String MSG_RESPONSE_SERVER_PORT_INVALID = "给定的管理端端口号无效";
    public static final String MSG_RESPONSE_PARAM_SIGN_KEY_INVALID = "参数‘signKey’无效";
    public static final String MSG_RESPONSE_PARAM_SN_INVALID = "参数‘serialNumber’无效";
    public static final String MSG_RESPONSE_PARAM_MAC_ADDRESS_INVALID = "参数‘macAddress’无效";
    public static final String MSG_RESPONSE_PARAM_IP_INVALID = "参数‘ip’无效";
    public static final String MSG_RESPONSE_PARAM_SIGN_TYPE_INVALID = "参数‘signType’无效";
    public static final String MSG_RESPONSE_CONNECT_FAILED = "连接失败";
    public static final String MSG_RESPONSE_DISCONNECT_FAILED = "解绑失败";
    public static final String MSG_RESPONSE_PARAM_VERIFICATION_FAILED = "参数‘verification’无效";
    public static final String MSG_RESPONSE_PERSON_TYPE_INVALID = "参数‘personInfoType’无效";
    public static final String MSG_RESPONSE_PARAM_PERSON_NAME_EMPTY = "参数‘personName’无效";
    public static final String MSG_RESPONSE_PARAM_FACE_LIST_EMPTY = "参数‘faceList’无效";
    public static final String MSG_RESPONSE_PARAM_PERSON_SERIAL_EMPTY = "参数‘personSerial’无效";
    public static final String MSG_RESPONSE_IMAGE_BASE_64_INVALID = "无效的图片资源";
    public static final String MSG_RESPONSE_PERSON_UPDATE_FAILED = "人员信息更新失败";
    public static final String MSG_RESPONSE_PERSON_UPDATE_SUCCESS = "人员信息更新成功";
    public static final String MSG_RESPONSE_PARAM_IC_INVALID = "参数‘icCardNo’无效";
    public static final String MSG_IC_CARD_NO_LENGTH_INVALID = "IC卡号长度不能超过50个字符";
    public static final String MSG_IC_CARD_NO_FORMAT_INVALID = "IC卡号只能由字母和数字组成";
    public static final String MSG_RESPONSE_PARAM_IC_CARD_INVALID = "参数‘icCardNo’无效";
    public static final String MSG_RESPONSE_PARAM_FACE_LIST_INVALID = "参数‘personAddList’无效";
    public static final String MSG_RESPONSE_PARAM_IMAGE_NAME_INVALID = "参数‘imageName’无效";
    public static final String MSG_RESPONSE_PARAM_IMAGE_BASE64_INVALID = "参数‘imageBase64’无效";
    public static final String MSG_RESPONSE_HTTP_FILE_MAX = "文件总大小不能超过100M";
    public static final String MSG_RESPONSE_PARAM_IMAGE_MD5_INVALID = "参数‘imageMD5’无效";
    public static final String MSG_RESPONSE_PARAM_FACE_ID_INVALID = "参数‘faceId’无效";
    public static final String MSG_RESPONSE_PERSON_NOT_EXIST = "设备端不存在该人员";
    public static final String MSG_RESPONSE_PERSON_DELETE_SUCCESS = "人员删除成功";
    public static final String MSG_RESPONSE_PERSON_DELETE_FAILED = "人员删除失败";
    public static final String MSG_RESPONSE_PARAM_PAGE_INDEX_ERROR = "参数‘pageIndex’无效";
    public static final String MSG_RESPONSE_PARAM_PAGE_SIZE_ERROR = "参数‘pageSize’无效";
    public static final String MSG_RESPONSE_PARAM_UPDATE_TIME_ERROR = "参数‘updateTime’无效";
    public static final String MSG_RESPONSE_PERSON_PERMISSION_SUCCESS = "人员信息更新成功";
    public static final String MSG_RESPONSE_OPEN_DOOR_SEND_SUCCESS = "开门信号已发送，请注意查看";
    public static final String MSG_RESPONSE_PARAM_SYSTEM_TIME_EMPTY = "设备时间无效";
    public static final String MSG_RESPONSE_DEVICE_SYSTEM_PERMISSION_NOT_GRANTED = "非系统应用，无权限修改";
    public static final String MSG_PACKAGE_VERSION_CODE_INVALID = "发布的APK版本过低";
    public static final String MSG_PACKAGE_NAME_INVALID = "包名验证失败";
    public static final String MSG_PACKAGE_IN_PROCESS = "设备端正在更新APK，请勿重复发布";
    public static final String MSG_PACKAGE_NOT_GRANT_PERMISSION = "设备端没有系统权限，无法静默安装，需要手动安装";
    public static final String MSG_PACKAGE_AUTHORITY_INVALID = "版本验证失败";
    public static final String MSG_FILE_DOWNLOAD_FAILED = "APK更新失败，请重试";
    public static final String MSG_IS_CURRENTLY_THE_LATEST_VERSION = "设备端当前已是最新版本，无需更新";
    public static final String MSG_RESPONSE_PARAM_MAIN_LOGO_EMPTY = "参数‘mainLogoBase64’为空";
    public static final String MSG_RESPONSE_PARAM_MAIN_LOGO_ID_EMPTY = "参数‘mainLogoId’为空";
    public static final String MSG_RESPONSE_PARAM_SECOND_LOGO_EMPTY = "参数‘viceLogoBase64’为空";
    public static final String MSG_RESPONSE_PARAM_SECOND_LOGO_ID_EMPTY = "参数‘secondLogoId’为空";
    public static final String MSG_RESPONSE_PARAM_OPERATION_INVALID = "参数‘operation’无效";
    public static final String MSG_RESPONSE_TIME_FORMAT_ERROR = "时间格式有误";
    public static final String MSG_RESPONSE_TIME_RANGE_ERROR = "结束时间不能早于或等于开始时间";
    public static final String MSG_RESPONSE_DATE_FORMAT_ERROR = "日期格式有误";
    public static final String MSG_RESPONSE_DATE_RANGE_ERROR = "结束日期不能早于或等于开始日期";
    public static final String MSG_RESPONSE_PARAM_AUTHORITY_LIST_INVALID = "参数‘authorityDetails’无效";
    public static final String MSG_RESPONSE_PARAM_WORK_DAY_INVALID = "参数‘workingDays’格式有误";
    public static final String MSG_RESPONSE_PARAM_TIME_LIST_INVALID = "参数‘timeRangeList’无效";

    /********************************************APP向外开放的API错误码（请求成功）*********************************/
    public static final int RESPONSE_CODE_SUCCESS = 200;
    /********************************************APP向外开放的API错误码（请求失败）*********************************/
    public static final int RESPONSE_CODE_FAILED_BASE = 400;
    /********************************************APP向外开放的API错误码（通用错误）*********************************/
    public static final int RESPONSE_FAILED_COMMON_BASE = 700;
    public static final int RESPONSE_CODE_CONTENT_TYPE_EMPTY = RESPONSE_FAILED_COMMON_BASE + 1;
    public static final int RESPONSE_CODE_API_EMPTY = RESPONSE_FAILED_COMMON_BASE + 2;
    public static final int RESPONSE_CODE_REQUEST_BODY_EMPTY = RESPONSE_FAILED_COMMON_BASE + 3;
    public static final int RESPONSE_CODE_JSON_INVALID = RESPONSE_FAILED_COMMON_BASE + 4;
    /********************************************APP向外开放的API错误码（签名相关）*********************************/
    public static final int RESPONSE_CODE_COMMON_BUSINESS_BASE = 800;
    public static final int RESPONSE_CODE_SIGN_PARAM_INVALID = RESPONSE_CODE_COMMON_BUSINESS_BASE + 1;
    public static final int RESPONSE_CODE_SIGN_LOCAL_EMPTY = RESPONSE_CODE_COMMON_BUSINESS_BASE + 2;
    public static final int RESPONSE_CODE_SIGN_CHECK_FAILED = RESPONSE_CODE_COMMON_BUSINESS_BASE + 3;
    /********************************************APP向外开放的API错误码（设备端错误相关）*********************************/
    public static final int RESPONSE_CODE_DEVICE_BASE = 900;
    public static final int RESPONSE_CODE_DEVICE_MAC_EMPTY = RESPONSE_CODE_DEVICE_BASE + 1;
    public static final int RESPONSE_CODE_DEVICE_MAC_EQUAL_FAILED = RESPONSE_CODE_DEVICE_BASE + 2;
    public static final int RESPONSE_CODE_DEVICE_SN_EQUAL_FAILED = RESPONSE_CODE_DEVICE_BASE + 3;
    public static final int RESPONSE_CODE_DEVICE_NOT_ENOUGH_STORAGE = RESPONSE_CODE_DEVICE_BASE + 4;
    public static final int RESPONSE_CODE_FACE_ENGINE_ERROR = RESPONSE_CODE_DEVICE_BASE + 5;
    /********************************************APP向外开放的API错误码（apk更新相关）*********************************/
    public static final int RESPONSE_CODE_BASE = 1000;
    public static final int RESPONSE_CODE_PACKAGE_NAME_INVALID = RESPONSE_CODE_BASE + 1;
    public static final int RESPONSE_CODE_VERSION_CODE_INVALID = RESPONSE_CODE_BASE + 2;
    public static final int RESPONSE_CODE_IN_PROCESS = RESPONSE_CODE_BASE + 3;
    public static final int RESPONSE_CODE_AUTHORITY_INVALID = RESPONSE_CODE_BASE + 4;
    public static final int RESPONSE_CODE_PACKAGE_EXIST = RESPONSE_CODE_BASE + 5;
    public static final int RESPONSE_CODE_PACKAGE_SAVE_FAILED = RESPONSE_CODE_BASE + 6;
    public static final int RESPONSE_CODE_NOT_GRANT_PERMISSION = RESPONSE_CODE_BASE + 7;
    /********************************************APP向外开放的API错误码（绑定相关）*********************************/
    public static final int RESPONSE_CODE_CONNECT_BASE = 1100;
    public static final int RESPONSE_CODE_CONNECT_FAILED = RESPONSE_CODE_CONNECT_BASE + 1;
    public static final int RESPONSE_CODE_SERVER_IP_INVALID = RESPONSE_CODE_CONNECT_BASE + 2;
    public static final int RESPONSE_CODE_SERVER_PORT_INVALID = RESPONSE_CODE_CONNECT_BASE + 3;
    public static final int RESPONSE_CODE_SIGN_KEY_INVALID = RESPONSE_CODE_CONNECT_BASE + 4;
    public static final int RESPONSE_CODE_SN_INVALID = RESPONSE_CODE_CONNECT_BASE + 5;
    public static final int RESPONSE_CODE_MAC_ADDRESS_INVALID = RESPONSE_CODE_CONNECT_BASE + 6;
    public static final int RESPONSE_CODE_IP_INVALID = RESPONSE_CODE_CONNECT_BASE + 7;
    public static final int RESPONSE_CODE_PARAM_SIGN_TYPE_INVALID = RESPONSE_CODE_CONNECT_BASE + 8;
    public static final int RESPONSE_CODE_DISCONNECT_BASE = 1200;
    public static final int RESPONSE_CODE_DISCONNECT_FAILED = RESPONSE_CODE_DISCONNECT_BASE + 1;
    public static final int RESPONSE_CODE_VERIFICATION_FAILED = RESPONSE_CODE_DISCONNECT_BASE + 2;
    /********************************************APP向外开放的API错误码（人员相关）*******************************/
    public static final int RESPONSE_CODE_PERSON_BASE = 1300;
    public static final int RESPONSE_CODE_IMAGE_BASE_64_INVALID = RESPONSE_CODE_PERSON_BASE + 1;
    public static final int RESPONSE_CODE_PERSON_NO_FACE = RESPONSE_CODE_PERSON_BASE + 2;
    public static final int RESPONSE_CODE_PERSON_MULTIPLE_FACE = RESPONSE_CODE_PERSON_BASE + 3;
    public static final int RESPONSE_CODE_PERSON_LOW_IMAGE_QUALITY = RESPONSE_CODE_PERSON_BASE + 4;
    public static final int RESPONSE_CODE_PERSON_EXTRACT_FEATURE_FAILED = RESPONSE_CODE_PERSON_BASE + 6;
    public static final int RESPONSE_CODE_PERSON_UPDATE_FAILED = RESPONSE_CODE_PERSON_BASE + 7;
    public static final int RESPONSE_CODE_PARAM_PERSON_INFO_TYPE_INVALID = RESPONSE_CODE_PERSON_BASE + 8;
    public static final int RESPONSE_CODE_IC_CARD_LENGTH_INVALID = RESPONSE_CODE_PERSON_BASE + 9;
    public static final int RESPONSE_CODE_IC_CARD_FORMAT_INVALID = RESPONSE_CODE_PERSON_BASE + 10;
    public static final int RESPONSE_CODE_PERSON_NOT_EXIST = RESPONSE_CODE_PERSON_BASE + 11;
    public static final int RESPONSE_CODE_PERSON_DELETED_FAILED = RESPONSE_CODE_PERSON_BASE + 12;
    public static final int RESPONSE_CODE_PARAM_PERSON_SERIAL_INVALID = RESPONSE_CODE_PERSON_BASE + 13;
    public static final int RESPONSE_CODE_PARAM_PERSON_NAME_INVALID = RESPONSE_CODE_PERSON_BASE + 14;
    public static final int RESPONSE_CODE_PARAM_FACE_LIST_INVALID = RESPONSE_CODE_PERSON_BASE + 15;
    public static final int RESPONSE_CODE_PARAM_PAGE_INDEX_ERROR = RESPONSE_CODE_PERSON_BASE + 16;
    public static final int RESPONSE_CODE_PARAM_PAGE_SIZE_ERROR = RESPONSE_CODE_PERSON_BASE + 17;
    public static final int RESPONSE_CODE_PARAM_UPDATE_TIME_ERROR = RESPONSE_CODE_PERSON_BASE + 18;
    public static final int RESPONSE_CODE_PARAM_IC_INVALID = RESPONSE_CODE_PERSON_BASE + 19;
    public static final int RESPONSE_CODE_PARAM_IMAGE_NAME_INVALID = RESPONSE_CODE_PERSON_BASE + 20;
    public static final int RESPONSE_CODE_PARAM_IMAGE_BASE64_INVALID = RESPONSE_CODE_PERSON_BASE + 21;
    public static final int RESPONSE_CODE_PARAM_IMAGE_MD5_INVALID = RESPONSE_CODE_PERSON_BASE + 22;
    public static final int RESPONSE_CODE_PARAM_FACE_ID_INVALID = RESPONSE_CODE_PERSON_BASE + 23;
    public static final int RESPONSE_CODE_PARAM_PERSON_LIST_INVALID = RESPONSE_CODE_PERSON_BASE + 24;
    /********************************************APP向外开放的API错误码（系统时间设置相关）*******************************/
    public static final int RESPONSE_CODE_SETTING_BASE = 1400;
    public static final int RESPONSE_CODE_PARAM_SYSTEM_TIME_EMPTY = RESPONSE_CODE_SETTING_BASE + 1;
    public static final int RESPONSE_CODE_PARAM_SYSTEM_TIME_NOT_GRANTED = RESPONSE_CODE_SETTING_BASE + 2;
    /********************************************APP向外开放的API错误码（设备配置参数相关）*******************************/
    public static final int RESPONSE_CODE_DEVICE_SETTING_BASE = 1500;
    public static final int RESPONSE_CODE_PASSWORD_INVALID = RESPONSE_CODE_DEVICE_SETTING_BASE + 1;
    public static final int RESPONSE_CODE_DEVICE_NAME_INVALID = RESPONSE_CODE_DEVICE_SETTING_BASE + 2;
    public static final int RESPONSE_CODE_REBOOT_EVERYDAY_INVALID = RESPONSE_CODE_DEVICE_SETTING_BASE + 3;
    public static final int RESPONSE_CODE_OPEN_DELAY_INVALID = RESPONSE_CODE_DEVICE_SETTING_BASE + 4;
    public static final int RESPONSE_CODE_UPLOAD_RECORD_IMAGE_INVALID = RESPONSE_CODE_DEVICE_SETTING_BASE + 5;
    public static final int RESPONSE_CODE_THRESHOLD_INVALID = RESPONSE_CODE_DEVICE_SETTING_BASE + 6;
    public static final int RESPONSE_CODE_INTERVAL_INVALID = RESPONSE_CODE_DEVICE_SETTING_BASE + 7;
    public static final int RESPONSE_CODE_SUCCESS_RETRY_INVALID = RESPONSE_CODE_DEVICE_SETTING_BASE + 8;
    public static final int RESPONSE_CODE_LIVE_INVALID = RESPONSE_CODE_DEVICE_SETTING_BASE + 9;
    public static final int RESPONSE_CODE_SIGN_DISTANCE_INVALID = RESPONSE_CODE_DEVICE_SETTING_BASE + 10;
    public static final int RESPONSE_CODE_COMPANY_NAME_INVALID = RESPONSE_CODE_DEVICE_SETTING_BASE + 11;
    public static final int RESPONSE_CODE_DISPLAY_MODE_INVALID = RESPONSE_CODE_DEVICE_SETTING_BASE + 12;
    public static final int RESPONSE_CODE_VOICE_MODE_INVALID = RESPONSE_CODE_DEVICE_SETTING_BASE + 13;
    public static final int RESPONSE_CODE_STRANGER_MODE_INVALID = RESPONSE_CODE_DEVICE_SETTING_BASE + 14;
    public static final int RESPONSE_CODE_STRANGER_VOICE_MODE_INVALID = RESPONSE_CODE_DEVICE_SETTING_BASE + 15;
    public static final int RESPONSE_CODE_FACE_QUALITY_INVALID = RESPONSE_CODE_DEVICE_SETTING_BASE + 16;
    public static final int RESPONSE_CODE_SIGN_TYPE_INVALID = RESPONSE_CODE_DEVICE_SETTING_BASE + 17;
    /********************************************APP向外开放的API错误码（logo相关）*******************************/
    public static final int RESPONSE_CODE_LOGO_BASE = 1600;
    public static final int RESPONSE_CODE_MAIN_LOGO_INVALID = RESPONSE_CODE_LOGO_BASE + 1;
    public static final int RESPONSE_CODE_MAIN_LOGO_ID_INVALID = RESPONSE_CODE_LOGO_BASE + 2;
    public static final int RESPONSE_CODE_SECOND_LOGO_INVALID = RESPONSE_CODE_LOGO_BASE + 3;
    public static final int RESPONSE_CODE_SECOND_LOGO_ID_INVALID = RESPONSE_CODE_LOGO_BASE + 4;
    public static final int RESPONSE_CODE_OPERATION_INVALID = RESPONSE_CODE_LOGO_BASE + 5;
    /********************************************APP向外开放的API错误码（人员时间权限相关）*******************************/
    public static final int RESPONSE_CODE_PERSON_AUTHORITY_BASE = 1700;
    public static final int RESPONSE_CODE_TIME_FORMAT_INVALID = RESPONSE_CODE_PERSON_AUTHORITY_BASE + 1;
    public static final int RESPONSE_CODE_TIME_RANGE_INVALID = RESPONSE_CODE_PERSON_AUTHORITY_BASE + 2;
    public static final int RESPONSE_CODE_DATE_FORMAT_INVALID = RESPONSE_CODE_PERSON_AUTHORITY_BASE + 3;
    public static final int RESPONSE_CODE_DATE_RANGE_INVALID = RESPONSE_CODE_PERSON_AUTHORITY_BASE + 4;
    public static final int RESPONSE_CODE_PARAM_AUTHORITY_LIST_INVALID = RESPONSE_CODE_PERSON_AUTHORITY_BASE + 5;
    public static final int RESPONSE_CODE_PARAM_WORK_DAY_INVALID = RESPONSE_CODE_PERSON_AUTHORITY_BASE + 6;
    public static final int RESPONSE_CODE_PARAM_TIME_LIST_INVALID = RESPONSE_CODE_PERSON_AUTHORITY_BASE + 7;

    /********************************************APP向外开放的API接口**********************************************/
    /**
     * 人员添加（单线程）
     */
    public static final String URI_PERSON_MANAGE_ADD = "/person/add";
    /**
     * 人员添加（多线程）
     */
    public static final String URI_PERSON_MANAGE_ADD_MULTIPLE = "/person/addMultiple";
    /**
     * 人员删除
     */
    public static final String URI_PERSON_MANAGE_DELETE = "/person/delete";
    /**
     * 人员列表
     */
    public static final String URI_PERSON_MANAGE_PERSON_LIST = "/person/list";
    /**
     * 人员权限设置
     */
    public static final String URI_PERSON_MANAGE_DOOR_AUTHORITY = "/person/doorAuthority";
    /**
     * 人员权限设置，新接口
     */
    public static final String URI_PERSON_MANAGE_DOOR_AUTHORITY_V2 = "/person/v2/doorAuthority";
    /**
     * 设备连接
     */
    public static final String URI_EQUIPMENT_CONNECT = "/equipment/connect";
    /**
     * 设备断开
     */
    public static final String URI_EQUIPMENT_DISCONNECT = "/equipment/disconnect";
    /**
     * 设备参数设置
     */
    public static final String URI_EQUIPMENT_SETTING = "/equipment/setting";
    /**
     * 获取设备参数设置
     */
    public static final String URI_EQUIPMENT_GET_SETTING = "/equipment/getSetting";
    /**
     * 获取设备指纹信息
     */
    public static final String URI_EQUIPMENT_GET_FINGER_PRINT_INFO = "/equipment/getFingerprintInfo";
    /**
     * 设备logo设置
     */
    public static final String URI_EQUIPMENT_SET_LOGO = "/equipment/setLogo";
    /**
     * 设备SN号
     */
    public static final String URI_EQUIPMENT_GET_SERIAL = "/equipment/getSerial";
    /**
     * 设备数据重置
     */
    public static final String URI_EQUIPMENT_CLEAN_DATA = "/equipment/cleanData";
    /**
     * 远程开门
     */
    public static final String URI_EQUIPMENT_OPEN_DOOR = "/equipment/openDoor";
    /**
     * 设备重启
     */
    public static final String URI_EQUIPMENT_REBOOT = "/equipment/reboot";
    /**
     * 设置系统时间
     */
    public static final String URI_EQUIPMENT_SYSTEM_TIME = "/equipment/systemTime";
    /**
     * 获取logo
     */
    public static final String URI_EQUIPMENT_GET_LOGO = "/equipment/getLogo";
    /**
     * 校验APK信息
     */
    public static final String URI_PACKAGE_AUTHORITY = "/package/authority";
    /**
     * 下发apk文件
     */
    public static final String URI_PACKAGE_TRANSFER = "/package/transfer";

}


