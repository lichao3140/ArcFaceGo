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

package com.arcsoft.arcfacesingle.business.personlist;

import android.text.TextUtils;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.app.BusinessErrorCode;
import com.arcsoft.arcfacesingle.business.setting.ConfigConstants;
import com.arcsoft.arcfacesingle.data.db.dao.PersonDao;
import com.arcsoft.arcfacesingle.data.db.table.TablePerson;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonFace;
import com.arcsoft.arcfacesingle.data.model.PersonInfo;
import com.arcsoft.arcfacesingle.data.model.UsbRegisterFailedInfo;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.asg.libcommon.util.common.TimeUtils;
import com.arcsoft.asg.libcommon.util.common.UUIDUtils;

import java.util.ArrayList;
import java.util.List;

public class PersonListDataManager {

    /**
     * 成功
     */
    public static final int TYPE_REGISTER_FAILED_INFO_SUCCESS= BusinessErrorCode.BEC_COMMON_OK;

    /**
     * 错误类型1：未知错误
     */
    public static final int TYPE_REGISTER_FAILED_INFO_0 = BusinessErrorCode.BEC_COMMON_UNKNOWN;

    /**
     * 错误类型2：检测失败
     */
    public static final int TYPE_REGISTER_FAILED_INFO_1 = BusinessErrorCode.BEC_FACE_MANAGER_DETECT_FAIL;
    /**
     * 错误类型3：检测不到人脸
     */
    public static final int TYPE_REGISTER_FAILED_INFO_2 = BusinessErrorCode.BEC_FACE_MANAGER_NO_FACE;
    /**
     * 错误类型4：检测到多张人脸
     */
    public static final int TYPE_REGISTER_FAILED_INFO_3 = BusinessErrorCode.BEC_FACE_MANAGER_MORE_THAN_ONE_FACE;
    /**
     * 错误类型5：人脸3DAngle超过了规定范围
     */
    public static final int TYPE_REGISTER_FAILED_INFO_4 = BusinessErrorCode.BEC_FACE_MANAGER_DEGREE_BIG;
    /**
     * 错误类型6：人脸质量检测不过
     */
    public static final int TYPE_REGISTER_FAILED_INFO_5 = BusinessErrorCode.BEC_FACE_MANAGER_FACE_QUALITY_FAIL;
    /**
     * 错误类型7：人脸特征提取失败
     */
    public static final int TYPE_REGISTER_FAILED_INFO_6 = BusinessErrorCode.BEC_FACE_MANAGER_RECOGNIZE_FAIL;
    /**
     * 错误类型8：人员保存失败
     */
    public static final int TYPE_REGISTER_FAILED_INFO_7 = BusinessErrorCode.BEC_FACE_MANAGER_PERSON_SAVE_FAILED;
    /**
     * 错误类型9：设备存储空间不足
     */
    public static final int TYPE_REGISTER_FAILED_INFO_8 = BusinessErrorCode.BEC_FACE_MANAGER_LESS_DEVICE_STORAGE;
    /**
     * 错误类型10：图片资源读取失败
     */
    public static final int TYPE_REGISTER_FAILED_INFO_9 = BusinessErrorCode.BEC_FACE_MANAGER_IMAGE_INVALID;

    public static List<PersonInfo> getAdapterDataList(List<TablePerson> dbFaceList) {
        List<PersonInfo> personInfos = new ArrayList<>();
        for (TablePerson tablePerson : dbFaceList) {
            PersonInfo personFace = new PersonInfo();
            personFace.setId(tablePerson.id);
            personFace.setPersonSerial(tablePerson.personSerial);
            personFace.setPersonInfoNo(tablePerson.personInfoNo);
            personFace.setPersonName(tablePerson.personName);
            personFace.setSelected(false);
            Integer infoType = tablePerson.personInfoType;
            if (infoType == null) {
                personFace.setPersonInfoType(PersonDao.TYPE_PERSON_INFO_ONLY_FACE);
            } else {
                personFace.setPersonInfoType(infoType);
            }
            personFace.setIcCardNo(tablePerson.icCardNo);
            personFace.setMainFaceId(tablePerson.mainFaceId);
            personInfos.add(personFace);
        }
        return personInfos;
    }

    public static String getStringInfoByType(int type) {
        return getStringInfoByType(type, 0);
    }

    public static String getStringInfoByType(int type, int errorCode) {
        String failedInfo;
        switch (type) {
            case TYPE_REGISTER_FAILED_INFO_SUCCESS:
                failedInfo = CommonUtils.getStrFromRes(R.string.successful_operation);
                break;
            case TYPE_REGISTER_FAILED_INFO_0:
                failedInfo = CommonUtils.getStrFromRes(R.string.face_manager_tip_common_fail);
                break;
            case TYPE_REGISTER_FAILED_INFO_1:
                failedInfo = String.format(CommonUtils.getStrFromRes(R.string.face_engine_init_fail_error_code), errorCode);
                break;
            case TYPE_REGISTER_FAILED_INFO_2:
                failedInfo = CommonUtils.getStrFromRes(R.string.face_manager_tip_detect_no_face_image);
                break;
            case TYPE_REGISTER_FAILED_INFO_3:
                failedInfo = CommonUtils.getStrFromRes(R.string.face_manager_tip_more_than_one_face);
                break;
            case TYPE_REGISTER_FAILED_INFO_4:
                failedInfo = CommonUtils.getStrFromRes(R.string.face_manager_tip_degree_big_image);
                break;
            case TYPE_REGISTER_FAILED_INFO_5:
                failedInfo = CommonUtils.getStrFromRes(R.string.face_quality_fail);
                break;
            case TYPE_REGISTER_FAILED_INFO_6:
                failedInfo = CommonUtils.getStrFromRes(R.string.face_manager_tip_recognize_fail);
                break;
            case TYPE_REGISTER_FAILED_INFO_7:
                failedInfo = CommonUtils.getStrFromRes(R.string.face_manager_person_save_bitmap_failed);
                break;
            case TYPE_REGISTER_FAILED_INFO_8:
                failedInfo = CommonUtils.getStrFromRes(R.string.device_storage_warn_tip1);
                break;
            case TYPE_REGISTER_FAILED_INFO_9:
                failedInfo = CommonUtils.getStrFromRes(R.string.setting_image_invalid);
                break;
            default:
                failedInfo = String.format(CommonUtils.getStrFromRes(R.string.face_engine_init_fail_error_code), errorCode);
                break;
        }
        return failedInfo;
    }

    public static UsbRegisterFailedInfo createUsbRegisterFailedBean(String imageName, int type, int errorCode) {
        UsbRegisterFailedInfo info = new UsbRegisterFailedInfo();
        info.setTimestamp(TimeUtils.getUsbRegisterFailTime());
        info.setImageName(imageName);
        info.setFailedInfo(getStringInfoByType(type, errorCode));
        return info;
    }

    public static String getUsbRegisterInfoContent(List<UsbRegisterFailedInfo> infoList) {
        StringBuilder builder = new StringBuilder();
        int total = infoList.size();
        for (int i = 0; i < total; i++) {
            UsbRegisterFailedInfo info = infoList.get(i);
            builder.append(info.getTimestamp())
                    .append("\t")
                    .append(info.getImageName())
                    .append("\t")
                    .append(info.getFailedInfo());
            if (i < total - 1) {
                builder.append("\r\n");
            }
        }
        return builder.toString();
    }

    public static TablePerson createTablePerson(String name, String personId, String personSerial) {
        TablePerson tablePerson = new TablePerson();
        tablePerson.personName = name;
        tablePerson.personInfoNo = personId;
        if (TextUtils.isEmpty(personSerial)) {
            tablePerson.personSerial = CommonUtils.createPersonSerial();
        } else {
            tablePerson.personSerial = personSerial;
        }
        tablePerson.addTime = System.currentTimeMillis();
        tablePerson.updateTime = tablePerson.addTime;
        tablePerson.authMorningStartTime = ConfigConstants.DOOR_AUTHORITY_DEFAULT_START_TIME;
        tablePerson.authMorningEndTime = ConfigConstants.DOOR_AUTHORITY_DEFAULT_END_TIME;
        tablePerson.authNoonStartTime = tablePerson.authMorningStartTime;
        tablePerson.authNoonEndTime = tablePerson.authMorningEndTime;
        tablePerson.authNightStartTime = tablePerson.authMorningStartTime;
        tablePerson.authNightEndTime = tablePerson.authMorningEndTime;
        tablePerson.doorAuthorityDetail = CommonUtils.getStrFromRes(R.string.face_manager_allow_pass);
        tablePerson.personInfoType = PersonDao.TYPE_PERSON_INFO_ONLY_FACE;
        tablePerson.icCardNo = "";
        return tablePerson;
    }

    public static TablePersonFace createTablePersonFace(TablePerson tablePerson, String addPersonImagePath) {
        TablePersonFace personFace = new TablePersonFace();
        personFace.faceInfo = tablePerson.personName;
        personFace.personSerial = tablePerson.personSerial;
        personFace.addTime = tablePerson.addTime;
        personFace.updateTime = personFace.addTime;
        if (!TextUtils.isEmpty(addPersonImagePath)) {
            //拍照
            personFace.imagePath = addPersonImagePath;
        } else {
            //相册
            String imageName = personFace.personSerial + "_" + UUIDUtils.getUUID32();
            personFace.imagePath = CommonUtils.getPersonFaceLocalPath(imageName);
        }
        return personFace;
    }
}
