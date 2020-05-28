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

import android.text.TextUtils;

import com.arcsoft.arcfacesingle.business.common.CommonRepository;
import com.arcsoft.arcfacesingle.business.setting.ConfigConstants;
import com.arcsoft.arcfacesingle.data.db.table.TablePerson;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonPermission;
import com.arcsoft.arcfacesingle.data.db.table.TableSettingConfigInfo;
import com.arcsoft.arcfacesingle.server.pojo.base.ResponseBase;
import com.arcsoft.arcfacesingle.server.pojo.request.RequestDoorAuthority;
import com.arcsoft.arcfacesingle.server.pojo.request.RequestDoorAuthorityV2;
import com.arcsoft.asg.libcommon.util.common.Md5Utils;
import com.arcsoft.asg.libcommon.util.common.StringUtils;
import com.arcsoft.asg.libcommon.util.common.TimeUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class LocalHttpApiDataUtils {

    public static final String TAG_AUTHORITY_MORNING = "morning";
    public static final String TAG_AUTHORITY_NOON = "afternoon";
    public static final String TAG_AUTHORITY_NIGHT = "evening";

    public static String getResponseJson(ResponseBase response) {
        return new Gson().toJson(response);
    }

    public static String getResponseStringFail(int errorCode, String errorMessage) {
        ResponseBase responseBase = new ResponseBase();
        responseBase.setCode(errorCode);
        responseBase.setMsg(errorMessage);
        return getResponseJson(responseBase);
    }

    public static String getResponseStringFail() {
        return getResponseStringFail(ServerConstants.RESPONSE_CODE_FAILED_BASE, ServerConstants.MSG_RESPONSE_FAILED);
    }

    public static String getResponseStringFail(int errorCode, String errorMsg, Object data) {
        ResponseBase responseBase = new ResponseBase();
        responseBase.setCode(errorCode);
        responseBase.setMsg(errorMsg);
        responseBase.setData(data);
        return getResponseJson(responseBase);
    }

    public static String getResponseStringFail(String errorMessage) {
        ResponseBase responseBase = new ResponseBase();
        responseBase.setCode(ServerConstants.RESPONSE_CODE_FAILED_BASE);
        responseBase.setMsg(errorMessage);
        return LocalHttpApiDataUtils.getResponseJson(responseBase);
    }

    public static String getResponseStringSuccess() {
        ResponseBase responseBase = new ResponseBase();
        responseBase.setCode(ServerConstants.RESPONSE_CODE_SUCCESS);
        responseBase.setMsg(ServerConstants.MSG_RESPONSE_REQUEST_SUCCESS);
        return LocalHttpApiDataUtils.getResponseJson(responseBase);
    }

    public static String getResponseStringSuccess(String errorMessage) {
        ResponseBase responseBase = new ResponseBase();
        responseBase.setCode(ServerConstants.RESPONSE_CODE_SUCCESS);
        responseBase.setMsg(errorMessage);
        return LocalHttpApiDataUtils.getResponseJson(responseBase);
    }

    public static String getResponseStringSuccess(Object data) {
        ResponseBase responseBase = new ResponseBase();
        responseBase.setCode(ServerConstants.RESPONSE_CODE_SUCCESS);
        responseBase.setMsg(ServerConstants.MSG_RESPONSE_REQUEST_SUCCESS);
        responseBase.setData(data);
        return LocalHttpApiDataUtils.getResponseJson(responseBase);
    }

    public static String getResponseStringSuccess(String errorMessage, Object data) {
        ResponseBase responseBase = new ResponseBase();
        responseBase.setCode(ServerConstants.RESPONSE_CODE_SUCCESS);
        responseBase.setMsg(errorMessage);
        responseBase.setData(data);
        return LocalHttpApiDataUtils.getResponseJson(responseBase);
    }

    public static ResponseBase getResponseBaseSuccess(String message) {
        return getResponseBase(ServerConstants.RESPONSE_CODE_SUCCESS, message);
    }

    public static ResponseBase getResponseBase(int code, String message) {
        ResponseBase responseBase = new ResponseBase();
        responseBase.setCode(code);
        responseBase.setMsg(message);
        return responseBase;
    }

    public static String checkAuthorityParam(RequestDoorAuthority doorAuthority, TablePerson tablePerson,
                                             RequestDoorAuthorityV2.DoorAuthorityDetail.TimeAuthority morningTimeAuthority,
                                             RequestDoorAuthorityV2.DoorAuthorityDetail.TimeAuthority noonTimeAuthority,
                                             RequestDoorAuthorityV2.DoorAuthorityDetail.TimeAuthority nightTimeAuthority) {
        String morStartTime = doorAuthority.getMorningStartTime();
        if (!TextUtils.isEmpty(morStartTime)) {
            if (!TimeUtils.checkDoorAuthority(morStartTime)) {
                return getResponseStringFail(ServerConstants.RESPONSE_CODE_TIME_FORMAT_INVALID, ServerConstants.MSG_RESPONSE_TIME_FORMAT_ERROR);
            }
            tablePerson.authMorningStartTime = morStartTime;
        }
        String morEndTime = doorAuthority.getMorningEndTime();
        if (!TextUtils.isEmpty(morEndTime)) {
            if (!TimeUtils.checkDoorAuthority(morEndTime)) {
                return getResponseStringFail(ServerConstants.RESPONSE_CODE_TIME_FORMAT_INVALID, ServerConstants.MSG_RESPONSE_TIME_FORMAT_ERROR);
            }
            tablePerson.authMorningEndTime = morEndTime;
        }
        boolean morningFail = !TextUtils.isEmpty(morStartTime) && !TextUtils.isEmpty(morEndTime) && !TimeUtils.compareTime(morStartTime, morEndTime);
        if (morningFail) {
            if (!(ServerConstants.DEFAULT_START_TIME.equals(morStartTime) &&
                    ServerConstants.DEFAULT_START_TIME.equals(morEndTime))) {
                return getResponseStringFail(ServerConstants.RESPONSE_CODE_TIME_RANGE_INVALID, ServerConstants.MSG_RESPONSE_TIME_RANGE_ERROR);
            }
        }
        if (TextUtils.isEmpty(morStartTime) && !TextUtils.isEmpty(morEndTime)) {
            String localStartTime;
            if (morningTimeAuthority != null) {
                localStartTime = morningTimeAuthority.getStartTime();
            } else {
                localStartTime = tablePerson.authMorningStartTime;
            }
            if (!TimeUtils.compareTime(localStartTime, morEndTime)) {
                return getResponseStringFail(ServerConstants.RESPONSE_CODE_TIME_RANGE_INVALID, ServerConstants.MSG_RESPONSE_TIME_RANGE_ERROR);
            }
        }
        if (!TextUtils.isEmpty(morStartTime) && TextUtils.isEmpty(morEndTime)) {
            String localEndTime;
            if (morningTimeAuthority != null) {
                localEndTime = morningTimeAuthority.getEndTime();
            } else {
                localEndTime = tablePerson.authMorningEndTime;
            }
            if (!TimeUtils.compareTime(morStartTime, localEndTime)) {
                return getResponseStringFail(ServerConstants.RESPONSE_CODE_TIME_RANGE_INVALID, ServerConstants.MSG_RESPONSE_TIME_RANGE_ERROR);
            }
        }
        String noonStart = doorAuthority.getNoonStartTime();
        if (!TextUtils.isEmpty(noonStart)) {
            if (!TimeUtils.checkDoorAuthority(noonStart)) {
                return getResponseStringFail(ServerConstants.RESPONSE_CODE_TIME_FORMAT_INVALID, ServerConstants.MSG_RESPONSE_TIME_FORMAT_ERROR);
            }
            tablePerson.authNoonStartTime = noonStart;
        }
        String noonEnd = doorAuthority.getNoonEndTime();
        if (!TextUtils.isEmpty(noonEnd)) {
            if (!TimeUtils.checkDoorAuthority(noonEnd)) {
                return getResponseStringFail(ServerConstants.RESPONSE_CODE_TIME_FORMAT_INVALID, ServerConstants.MSG_RESPONSE_TIME_FORMAT_ERROR);
            }
            tablePerson.authNoonEndTime = noonEnd;
        }
        boolean noonFail = !TextUtils.isEmpty(noonStart) && !TextUtils.isEmpty(noonEnd) && !TimeUtils.compareTime(noonStart, noonEnd);
        if (noonFail) {
            if (!(ServerConstants.DEFAULT_START_TIME.equals(noonStart) &&
                    ServerConstants.DEFAULT_START_TIME.equals(noonEnd))) {
                return getResponseStringFail(ServerConstants.RESPONSE_CODE_TIME_RANGE_INVALID, ServerConstants.MSG_RESPONSE_TIME_RANGE_ERROR);
            }
        }
        if (TextUtils.isEmpty(noonStart) && !TextUtils.isEmpty(noonEnd)) {
            String localNoonStart;
            if (noonTimeAuthority != null) {
                localNoonStart = noonTimeAuthority.getStartTime();
            } else {
                localNoonStart = tablePerson.authNoonStartTime;
            }
            if (!TimeUtils.compareTime(localNoonStart, noonEnd)) {
                return getResponseStringFail(ServerConstants.RESPONSE_CODE_TIME_RANGE_INVALID, ServerConstants.MSG_RESPONSE_TIME_RANGE_ERROR);
            }
        }
        if (!TextUtils.isEmpty(noonStart) && TextUtils.isEmpty(noonEnd)) {
            String localNoonEnd;
            if (noonTimeAuthority != null) {
                localNoonEnd = noonTimeAuthority.getEndTime();
            } else {
                localNoonEnd = tablePerson.authNoonEndTime;
            }
            if (!TimeUtils.compareTime(noonStart, localNoonEnd)) {
                return getResponseStringFail(ServerConstants.RESPONSE_CODE_TIME_RANGE_INVALID, ServerConstants.MSG_RESPONSE_TIME_RANGE_ERROR);
            }
        }
        String nightStart = doorAuthority.getNightStartTime();
        if (!TextUtils.isEmpty(nightStart)) {
            if (!TimeUtils.checkDoorAuthority(nightStart)) {
                return getResponseStringFail(ServerConstants.RESPONSE_CODE_TIME_FORMAT_INVALID, ServerConstants.MSG_RESPONSE_TIME_FORMAT_ERROR);
            }
            tablePerson.authNightStartTime = nightStart;
        }
        String nightEnd = doorAuthority.getNightEndTime();
        if (!TextUtils.isEmpty(nightEnd)) {
            if (!TimeUtils.checkDoorAuthority(nightEnd)) {
                return getResponseStringFail(ServerConstants.RESPONSE_CODE_TIME_FORMAT_INVALID, ServerConstants.MSG_RESPONSE_TIME_FORMAT_ERROR);
            }
            tablePerson.authNightEndTime = nightEnd;
        }
        boolean nightFail = !TextUtils.isEmpty(nightStart) && !TextUtils.isEmpty(nightEnd) && !TimeUtils.compareTime(nightStart, nightEnd);
        if (nightFail) {
            if (!(ServerConstants.DEFAULT_START_TIME.equals(nightStart) &&
                    ServerConstants.DEFAULT_START_TIME.equals(nightEnd))) {
                return getResponseStringFail(ServerConstants.RESPONSE_CODE_TIME_RANGE_INVALID, ServerConstants.MSG_RESPONSE_TIME_RANGE_ERROR);
            }
        }
        if (TextUtils.isEmpty(nightStart) && !TextUtils.isEmpty(nightEnd)) {
            String localNightStart;
            if (nightTimeAuthority != null) {
                localNightStart = nightTimeAuthority.getStartTime();
            } else {
                localNightStart = tablePerson.authNightStartTime;
            }
            if (!TimeUtils.compareTime(localNightStart, nightEnd)) {
                return getResponseStringFail(ServerConstants.RESPONSE_CODE_TIME_RANGE_INVALID, ServerConstants.MSG_RESPONSE_TIME_RANGE_ERROR);
            }
        }
        if (!TextUtils.isEmpty(nightStart) && TextUtils.isEmpty(nightEnd)) {
            String localNightEnd;
            if (nightTimeAuthority != null) {
                localNightEnd = nightTimeAuthority.getEndTime();
            } else {
                localNightEnd = tablePerson.authNightEndTime;
            }
            if (!TimeUtils.compareTime(nightStart, localNightEnd)) {
                return getResponseStringFail(ServerConstants.RESPONSE_CODE_TIME_RANGE_INVALID, ServerConstants.MSG_RESPONSE_TIME_RANGE_ERROR);
            }
        }
        return null;
    }

    public static String checkAuthorityParamV2(RequestDoorAuthorityV2 doorAuthority) {
        if (TextUtils.isEmpty(doorAuthority.getPersonSerial())) {
            return getResponseStringFail(ServerConstants.RESPONSE_CODE_PARAM_PERSON_SERIAL_INVALID, ServerConstants.MSG_RESPONSE_PARAM_PERSON_SERIAL_EMPTY);
        }
        List<RequestDoorAuthorityV2.DoorAuthorityDetail> authorityDetails = doorAuthority.getAuthorityDetails();
        if (authorityDetails == null || authorityDetails.isEmpty()) {
            return getResponseStringFail(ServerConstants.RESPONSE_CODE_PARAM_AUTHORITY_LIST_INVALID, ServerConstants.MSG_RESPONSE_PARAM_AUTHORITY_LIST_INVALID);
        }
        RequestDoorAuthorityV2.DoorAuthorityDetail authorityDetail = authorityDetails.get(0);
        String dateStart = authorityDetail.getStartDate();
        if (!TextUtils.isEmpty(dateStart) && !TimeUtils.isValidDate(dateStart, TimeUtils.DATE_PATTERN_3)) {
            return getResponseStringFail(ServerConstants.RESPONSE_CODE_DATE_FORMAT_INVALID, ServerConstants.MSG_RESPONSE_DATE_FORMAT_ERROR);
        }
        String dateEnd = authorityDetail.getEndDate();
        if (!TextUtils.isEmpty(dateEnd) && !TimeUtils.isValidDate(dateEnd, TimeUtils.DATE_PATTERN_3)) {
            return getResponseStringFail(ServerConstants.RESPONSE_CODE_DATE_FORMAT_INVALID, ServerConstants.MSG_RESPONSE_DATE_FORMAT_ERROR);
        }
        if (!TextUtils.isEmpty(dateStart) && !TextUtils.isEmpty(dateEnd) && !TimeUtils.compareDate2(dateStart, dateEnd)) {
            return getResponseStringFail(ServerConstants.RESPONSE_CODE_DATE_RANGE_INVALID, ServerConstants.MSG_RESPONSE_DATE_RANGE_ERROR);
        }
        String workingDays = authorityDetail.getWorkingDays();
        if (!TextUtils.isEmpty(workingDays) && !StringUtils.checkWorkingDays(workingDays)) {
            return getResponseStringFail(ServerConstants.RESPONSE_CODE_PARAM_WORK_DAY_INVALID, ServerConstants.MSG_RESPONSE_PARAM_WORK_DAY_INVALID);
        }
        List<RequestDoorAuthorityV2.DoorAuthorityDetail.TimeAuthority> timeList = authorityDetail.getTimeRangeList();
        if (timeList == null) {
            return getResponseStringFail(ServerConstants.RESPONSE_CODE_PARAM_TIME_LIST_INVALID, ServerConstants.MSG_RESPONSE_PARAM_TIME_LIST_INVALID);
        }
        boolean checkTime = true;
        boolean compareTime = true;
        for (RequestDoorAuthorityV2.DoorAuthorityDetail.TimeAuthority timeAuthority : timeList) {
            if (timeAuthority == null) {
                checkTime = false;
                break;
            }
            String timeStart = timeAuthority.getStartTime();
            String timeEnd = timeAuthority.getEndTime();
            if (!TextUtils.isEmpty(timeStart) && !TextUtils.isEmpty(timeEnd)) {
                if (!(TimeUtils.checkDoorAuthority(timeStart) && TimeUtils.checkDoorAuthority(timeEnd))) {
                    checkTime = false;
                    break;
                }
                if (!TimeUtils.compareTime(timeStart, timeEnd)) {
                    compareTime = false;
                    break;
                }
            }
            if (TextUtils.isEmpty(timeStart) && !TextUtils.isEmpty(timeEnd)) {
                if (!TimeUtils.compareTime(ServerConstants.DEFAULT_START_TIME, timeEnd)) {
                    compareTime = false;
                    break;
                }
            }
            if (!TextUtils.isEmpty(timeStart) && TextUtils.isEmpty(timeEnd)) {
                if (!TimeUtils.compareTime(timeStart, ServerConstants.DEFAULT_END_TIME)) {
                    compareTime = false;
                    break;
                }
            }
        }
        if (!checkTime) {
            return getResponseStringFail(ServerConstants.RESPONSE_CODE_TIME_FORMAT_INVALID, ServerConstants.MSG_RESPONSE_TIME_FORMAT_ERROR);
        }
        if (!compareTime) {
            return getResponseStringFail(ServerConstants.RESPONSE_CODE_TIME_RANGE_INVALID, ServerConstants.MSG_RESPONSE_TIME_RANGE_ERROR);
        }
        return null;
    }

    /**
     * 创建权限时间范围集
     *
     * @param createNew
     * @param morningTimeAuthority
     * @param noonTimeAuthority
     * @param nightTimeAuthority
     * @param doorAuthority
     * @return
     */
    public static List<RequestDoorAuthorityV2.DoorAuthorityDetail.TimeAuthority> createTimeAuthorities(
            boolean createNew,
            RequestDoorAuthorityV2.DoorAuthorityDetail.TimeAuthority morningTimeAuthority,
            RequestDoorAuthorityV2.DoorAuthorityDetail.TimeAuthority noonTimeAuthority,
            RequestDoorAuthorityV2.DoorAuthorityDetail.TimeAuthority nightTimeAuthority,
            RequestDoorAuthority doorAuthority, TablePerson tablePerson) {
        List<RequestDoorAuthorityV2.DoorAuthorityDetail.TimeAuthority> timeAuthorities = new ArrayList<>();
        if (createNew) {
            RequestDoorAuthorityV2.DoorAuthorityDetail.TimeAuthority newTime =
                    new RequestDoorAuthorityV2.DoorAuthorityDetail.TimeAuthority();
            newTime.setStartTime(tablePerson.authMorningStartTime);
            newTime.setEndTime(tablePerson.authMorningEndTime);
            timeAuthorities.add(newTime);
        } else {
            if (morningTimeAuthority == null) {
                morningTimeAuthority = new RequestDoorAuthorityV2.DoorAuthorityDetail.TimeAuthority();
                morningTimeAuthority.setStartTime(tablePerson.authMorningStartTime);
                morningTimeAuthority.setEndTime(tablePerson.authMorningEndTime);
                morningTimeAuthority.setTimeDesc(LocalHttpApiDataUtils.TAG_AUTHORITY_MORNING);
            } else {
                if (!TextUtils.isEmpty(doorAuthority.getMorningStartTime())) {
                    morningTimeAuthority.setStartTime(doorAuthority.getMorningStartTime());
                }
                if (!TextUtils.isEmpty(doorAuthority.getMorningEndTime())) {
                    morningTimeAuthority.setEndTime(doorAuthority.getMorningEndTime());
                }
            }
            timeAuthorities.add(morningTimeAuthority);
            if (noonTimeAuthority == null) {
                noonTimeAuthority = new RequestDoorAuthorityV2.DoorAuthorityDetail.TimeAuthority();
                noonTimeAuthority.setStartTime(tablePerson.authNoonStartTime);
                noonTimeAuthority.setEndTime(tablePerson.authNoonEndTime);
                noonTimeAuthority.setTimeDesc(LocalHttpApiDataUtils.TAG_AUTHORITY_NOON);
            } else {
                if (!TextUtils.isEmpty(doorAuthority.getNoonStartTime())) {
                    noonTimeAuthority.setStartTime(doorAuthority.getNoonStartTime());
                }
                if (!TextUtils.isEmpty(doorAuthority.getNoonEndTime())) {
                    noonTimeAuthority.setEndTime(doorAuthority.getNoonEndTime());
                }
            }
            timeAuthorities.add(noonTimeAuthority);
            if (nightTimeAuthority == null) {
                nightTimeAuthority = new RequestDoorAuthorityV2.DoorAuthorityDetail.TimeAuthority();
                nightTimeAuthority.setStartTime(tablePerson.authNightStartTime);
                nightTimeAuthority.setEndTime(tablePerson.authNightEndTime);
                nightTimeAuthority.setTimeDesc(LocalHttpApiDataUtils.TAG_AUTHORITY_NIGHT);
            } else {
                if (!TextUtils.isEmpty(doorAuthority.getNightStartTime())) {
                    nightTimeAuthority.setStartTime(doorAuthority.getNightStartTime());
                }
                if (!TextUtils.isEmpty(doorAuthority.getNightEndTime())) {
                    nightTimeAuthority.setEndTime(doorAuthority.getNightEndTime());
                }
            }
            timeAuthorities.add(nightTimeAuthority);
        }
        return timeAuthorities;
    }

    /**
     * 创建人员权限数据（兼容V1接口）
     *
     * @param createNew 兼容V1接口
     * @param tablePerson
     * @param gson
     * @return
     */
    public static TablePersonPermission createNewPersonPermission(boolean createNew, TablePerson tablePerson, Gson gson) {
        TablePersonPermission localPermission = new TablePersonPermission();
        localPermission.setPersonSerial(tablePerson.personSerial);
        localPermission.setWorkingDays(ServerConstants.DEFAULT_WORKING_DAYS);
        localPermission.setStartDate(ServerConstants.DEFAULT_START_DATE);
        localPermission.setEndDate(ServerConstants.DEFAULT_END_DATE);
        List<RequestDoorAuthorityV2.DoorAuthorityDetail.TimeAuthority> timeAuthorities = createTimeAuthorities(createNew,
                null, null, null, null, tablePerson);
        localPermission.setTimeAndDesc(gson.toJson(timeAuthorities));
        return localPermission;
    }

    /**
     * 创建人员权限数据（针对V2接口）
     *
     * @param tablePerson
     * @param gson
     * @return
     */
    public static TablePersonPermission createNewPersonPermission(TablePerson tablePerson, Gson gson) {
        return createNewPersonPermission(true, tablePerson, gson);
    }

    public static List<TablePersonPermission> createNewPersonPermissions(List<RequestDoorAuthorityV2.DoorAuthorityDetail>
                                                                                 authorityDetails, String personSerial, Gson gson) {
        List<TablePersonPermission> savePermissions = new ArrayList<>(authorityDetails.size());
        for (RequestDoorAuthorityV2.DoorAuthorityDetail authorityDetail : authorityDetails) {
            TablePersonPermission savePermission = new TablePersonPermission();
            savePermission.setPersonSerial(personSerial);
            String startDate = authorityDetail.getStartDate();
            savePermission.setStartDate(TextUtils.isEmpty(startDate) ? ServerConstants.DEFAULT_START_DATE : startDate);
            String endDate = authorityDetail.getEndDate();
            savePermission.setEndDate(TextUtils.isEmpty(endDate) ? ServerConstants.DEFAULT_END_DATE : endDate);
            String workDays = authorityDetail.getWorkingDays();
            savePermission.setWorkingDays(workDays == null ? ServerConstants.DEFAULT_WORKING_DAYS : workDays);
            List<RequestDoorAuthorityV2.DoorAuthorityDetail.TimeAuthority> authorities = authorityDetail.getTimeRangeList();
            if (authorities.isEmpty()) {
                RequestDoorAuthorityV2.DoorAuthorityDetail.TimeAuthority timeAuthority =
                        new RequestDoorAuthorityV2.DoorAuthorityDetail.TimeAuthority();
                timeAuthority.setStartTime(ServerConstants.DEFAULT_START_TIME);
                timeAuthority.setEndTime(ServerConstants.DEFAULT_END_TIME);
                authorities.add(timeAuthority);
            } else {
                for (RequestDoorAuthorityV2.DoorAuthorityDetail.TimeAuthority timeAuthority : authorities) {
                    if (TextUtils.isEmpty(timeAuthority.getStartTime())) {
                        timeAuthority.setStartTime(ServerConstants.DEFAULT_START_TIME);
                    }
                    if (TextUtils.isEmpty(timeAuthority.getEndTime())) {
                        timeAuthority.setEndTime(ServerConstants.DEFAULT_END_TIME);
                    }
                }
            }
            savePermission.setTimeAndDesc(gson.toJson(authorityDetail.getTimeRangeList()));
            savePermissions.add(savePermission);
        }
        return savePermissions;
    }

    public static List<String> getServerUrlList() {
        List<String> urlList = new ArrayList<>();
        urlList.add(ServerConstants.URI_PERSON_MANAGE_ADD);
        urlList.add(ServerConstants.URI_PERSON_MANAGE_ADD_MULTIPLE);
        urlList.add(ServerConstants.URI_PERSON_MANAGE_DELETE);
        urlList.add(ServerConstants.URI_PERSON_MANAGE_PERSON_LIST);
        urlList.add(ServerConstants.URI_PERSON_MANAGE_DOOR_AUTHORITY);
        urlList.add(ServerConstants.URI_PERSON_MANAGE_DOOR_AUTHORITY_V2);
        urlList.add(ServerConstants.URI_EQUIPMENT_CONNECT);
        urlList.add(ServerConstants.URI_EQUIPMENT_DISCONNECT);
        urlList.add(ServerConstants.URI_EQUIPMENT_SETTING);
        urlList.add(ServerConstants.URI_EQUIPMENT_GET_SETTING);
        urlList.add(ServerConstants.URI_EQUIPMENT_GET_FINGER_PRINT_INFO);
        urlList.add(ServerConstants.URI_EQUIPMENT_SET_LOGO);
        urlList.add(ServerConstants.URI_EQUIPMENT_GET_SERIAL);
        urlList.add(ServerConstants.URI_EQUIPMENT_CLEAN_DATA);
        urlList.add(ServerConstants.URI_EQUIPMENT_OPEN_DOOR);
        urlList.add(ServerConstants.URI_EQUIPMENT_REBOOT);
        urlList.add(ServerConstants.URI_EQUIPMENT_SYSTEM_TIME);
        urlList.add(ServerConstants.URI_EQUIPMENT_GET_LOGO);
        urlList.add(ServerConstants.URI_PACKAGE_AUTHORITY);
        urlList.add(ServerConstants.URI_PACKAGE_TRANSFER);
        return urlList;
    }

    /**
     * 对API数据进行签名校验
     * @param strEncode
     * @param signKey
     * @param netSign
     * @return
     */
    public static boolean compareSign(String strEncode, String signKey, String netSign) {
        String strMd5 = strEncode + signKey;
        String localSign = Md5Utils.encode(strMd5);
        return netSign.equals(localSign);
    }

    /**
     * 获取对外开放API监听端口
     * @return
     */
    public static int getLocalApiPort() {
        TableSettingConfigInfo configInfo = CommonRepository.getInstance().getSettingConfigInfo();
        int serverPort = Integer.parseInt(ConfigConstants.DEFAULT_DEVICE_PORT);
        String devicePort = configInfo.getDevicePort();
        if (!TextUtils.isEmpty(devicePort)) {
            serverPort = Integer.parseInt(devicePort);
        }
        return serverPort;
    }
}
