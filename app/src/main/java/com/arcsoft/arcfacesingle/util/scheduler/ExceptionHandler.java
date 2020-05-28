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

package com.arcsoft.arcfacesingle.util.scheduler;

import android.util.SparseArray;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.arcsoftlink.enums.ArcLinkErrorCodeEnum;
import com.arcsoft.arcsoftlink.mqtt.ArcLinkException;
import com.google.gson.JsonParseException;

import org.json.JSONException;

import java.net.ConnectException;

import retrofit2.HttpException;

public class ExceptionHandler {

    private static final int ERR_UNAUTHORIZED = 401;
    private static final int ERR_FORBIDDEN = 403;
    private static final int ERR_NOT_FOUND = 404;
    private static final int ERR_REQUEST_TIMEOUT = 408;
    private static final int ERR_INTERNAL_SERVER = 500;
    private static final int ERR_BAD_GATEWAY = 502;
    private static final int ERR_SERVICE_UNAVAILABLE = 503;
    private static final int ERR_GATEWAY_TIMEOUT = 504;

    public static ResponseThrowable handleException(Throwable e) {
        ResponseThrowable ex;
        if (e instanceof ArcLinkException) {
            SparseArray msgList = initDeviceAccessMsgList();
            ArcLinkException arcLinkException = (ArcLinkException) e;
            int code = arcLinkException.getCode().getCode();
            ex = new ResponseThrowable(arcLinkException, code);
            ex.message = (String) msgList.get(code);
            return ex;
        } else if (e instanceof HttpException) {
            HttpException httpException = (HttpException) e;
            ex = new ResponseThrowable(e, ERROR.ERROR_HTTP);
            switch (httpException.code()) {
                case ERR_UNAUTHORIZED:
                case ERR_FORBIDDEN:
                case ERR_NOT_FOUND:
                case ERR_REQUEST_TIMEOUT:
                case ERR_GATEWAY_TIMEOUT:
                case ERR_INTERNAL_SERVER:
                case ERR_BAD_GATEWAY:
                case ERR_SERVICE_UNAVAILABLE:
                default:
                    ex.message = CommonUtils.getStrFromRes(R.string.server_address_is_not_valid);
                    break;
            }
            return ex;
        } else if (e instanceof ServerException) {
            ServerException resultException = (ServerException) e;
            ex = new ResponseThrowable(resultException, resultException.code);
            ex.message = resultException.message;
            return ex;
        } else if (e instanceof JsonParseException
                || e instanceof JSONException) {
            ex = new ResponseThrowable(e, ERROR.ERROR_PARSE);
            ex.message = CommonUtils.getStrFromRes(R.string.data_parsing_exception_please_check);
            return ex;
        } else if (e instanceof ConnectException) {
            ex = new ResponseThrowable(e, ERROR.ERROR_NETWORK);
            ex.message = CommonUtils.getStrFromRes(R.string.server_address_is_not_valid);
            return ex;
        } else if (e instanceof javax.net.ssl.SSLHandshakeException) {
            ex = new ResponseThrowable(e, ERROR.ERROR_SSL);
            ex.message = "证书验证失败，请检查";
            return ex;
        } else if (e instanceof CustomThrowable) {
            ex = new ResponseThrowable(e);
            CustomThrowable customThrowable = (CustomThrowable) e;
            ex.code = customThrowable.code;
            ex.message = customThrowable.message;
            return ex;
        } else {
            ex = new ResponseThrowable(e, ERROR.UNKNOWN);
            ex.message = e.getMessage();
            return ex;
        }
    }

    public static class ResponseThrowable extends Exception {
        public int code;
        public String message;

        public ResponseThrowable(Throwable throwable) {
            super(throwable);
        }

        public ResponseThrowable(Throwable throwable, int code) {
            super(throwable);
            this.code = code;
        }
    }

    public static class CustomThrowable extends Exception {
        public int code;
        public String message;

        public CustomThrowable(Throwable throwable, int code, String message) {
            super(throwable);
            this.code = code;
            this.message = message;
        }
    }

    /**
     * 约定异常
     */
    public static class ERROR {
        /**
         * 未知错误
         */
        public static final int UNKNOWN = 1000;
        /**
         * 解析错误
         */
        public static final int ERROR_PARSE = 1001;
        /**
         * 网络错误
         */
        public static final int ERROR_NETWORK = 1002;
        /**
         * 协议出错
         */
        public static final int ERROR_HTTP = 1003;

        /**
         * 证书出错
         */
        public static final int ERROR_SSL = 1005;

        /**
         * 自定义业务错误码
         */
        public static final int CUSTOM_BUSINESS = 2001;
    }

    /**
     * ServerException发生后，将自动转换为ResponseThrowable返回
     */
    class ServerException extends RuntimeException {
        int code;
        String message;
    }

    public static SparseArray initDeviceAccessMsgList() {
        SparseArray deviceAccessMsgList = new SparseArray();
        deviceAccessMsgList.put(ArcLinkErrorCodeEnum.PARAM_ACCESS_CODE_ID_IS_NOT_VALID.getCode(),
                CommonUtils.getStrFromRes(R.string.access_code_invalid));
        deviceAccessMsgList.put(ArcLinkErrorCodeEnum.PARAM_ACCESS_CODE_CANNOT_EMPTY.getCode(), ArcLinkErrorCodeEnum.PARAM_ACCESS_CODE_CANNOT_EMPTY.getMsgCn());
        deviceAccessMsgList.put(ArcLinkErrorCodeEnum.PARAM_ACCESS_CODES_CANNOT_EMPTY.getCode(), ArcLinkErrorCodeEnum.PARAM_ACCESS_CODES_CANNOT_EMPTY.getMsgCn());
        deviceAccessMsgList.put(ArcLinkErrorCodeEnum.PARAM_MAC_ADDRESS_CANNOT_BE_EMPTY.getCode(), ArcLinkErrorCodeEnum.PARAM_MAC_ADDRESS_CANNOT_BE_EMPTY.getMsgCn());
        deviceAccessMsgList.put(ArcLinkErrorCodeEnum.SDK_NOT_A_VALID_TOKEN.getCode(), ArcLinkErrorCodeEnum.SDK_NOT_A_VALID_TOKEN.getMsgCn());
        deviceAccessMsgList.put(ArcLinkErrorCodeEnum.SDK_INVALID_FEATURE_DATA_TAG.getCode(), ArcLinkErrorCodeEnum.SDK_INVALID_FEATURE_DATA_TAG.getMsgCn());
        deviceAccessMsgList.put(ArcLinkErrorCodeEnum.INVALID_IMAGE_FILE.getCode(), ArcLinkErrorCodeEnum.INVALID_IMAGE_FILE.getMsgCn());
        deviceAccessMsgList.put(ArcLinkErrorCodeEnum.IMAGE_STORAGE_FAILED.getCode(), ArcLinkErrorCodeEnum.IMAGE_STORAGE_FAILED.getMsgCn());
        deviceAccessMsgList.put(ArcLinkErrorCodeEnum.SDK_FACE_ID_CANNOT_BE_EMPTY.getCode(), ArcLinkErrorCodeEnum.SDK_FACE_ID_CANNOT_BE_EMPTY.getMsgCn());
        deviceAccessMsgList.put(ArcLinkErrorCodeEnum.SDK_RECG_ID_CANNOT_BE_EMPTY.getCode(), ArcLinkErrorCodeEnum.SDK_RECG_ID_CANNOT_BE_EMPTY.getMsgCn());
        deviceAccessMsgList.put(ArcLinkErrorCodeEnum.SDK_RECG_TYPE_CANNOT_BE_EMPTY.getCode(), ArcLinkErrorCodeEnum.SDK_RECG_TYPE_CANNOT_BE_EMPTY.getMsgCn());
        deviceAccessMsgList.put(ArcLinkErrorCodeEnum.PARAM_SDK_PERSON_ID_CANNOT_BE_EMPTY.getCode(), ArcLinkErrorCodeEnum.PARAM_SDK_PERSON_ID_CANNOT_BE_EMPTY.getMsgCn());
        deviceAccessMsgList.put(ArcLinkErrorCodeEnum.PARAM_SDK_PERSON_SET_ID_CANNOT_EMPTY.getCode(), ArcLinkErrorCodeEnum.PARAM_SDK_PERSON_SET_ID_CANNOT_EMPTY.getMsgCn());
        deviceAccessMsgList.put(ArcLinkErrorCodeEnum.PARAM_SDK_PERSON_SET_ID_IS_NOT_EXIST.getCode(), ArcLinkErrorCodeEnum.PARAM_SDK_PERSON_SET_ID_IS_NOT_EXIST.getMsgCn());
        deviceAccessMsgList.put(ArcLinkErrorCodeEnum.ERROR_ALREADY_CONNECTED.getCode(), ArcLinkErrorCodeEnum.ERROR_ALREADY_CONNECTED.getMsgCn());
        deviceAccessMsgList.put(ArcLinkErrorCodeEnum.ERROR_CANNOT_GET_APP_VERSION.getCode(), ArcLinkErrorCodeEnum.ERROR_CANNOT_GET_APP_VERSION.getMsgCn());
        deviceAccessMsgList.put(ArcLinkErrorCodeEnum.ERROR_CANNOT_GET_MAC.getCode(), ArcLinkErrorCodeEnum.ERROR_CANNOT_GET_MAC.getMsgCn());
        deviceAccessMsgList.put(ArcLinkErrorCodeEnum.ERROR_SERVER_UNKNOWN.getCode(), ArcLinkErrorCodeEnum.ERROR_SERVER_UNKNOWN.getMsgCn());
        deviceAccessMsgList.put(ArcLinkErrorCodeEnum.ERROR_CONNECT_MQTT_FAILED.getCode(), ArcLinkErrorCodeEnum.ERROR_CONNECT_MQTT_FAILED.getMsgCn());
        deviceAccessMsgList.put(ArcLinkErrorCodeEnum.ERROR_INVALID_PARAMS.getCode(), ArcLinkErrorCodeEnum.ERROR_INVALID_PARAMS.getMsgCn());
        deviceAccessMsgList.put(ArcLinkErrorCodeEnum.ERROR_NULL_URL.getCode(), ArcLinkErrorCodeEnum.ERROR_NULL_URL.getMsgCn());
        deviceAccessMsgList.put(ArcLinkErrorCodeEnum.ERROR_REQUEST.getCode(), CommonUtils.getStrFromRes(R.string.unable_to_connect_to_server));
        deviceAccessMsgList.put(ArcLinkErrorCodeEnum.ERROR_JSON_SYNTAX.getCode(), ArcLinkErrorCodeEnum.ERROR_JSON_SYNTAX.getMsgCn());
        deviceAccessMsgList.put(ArcLinkErrorCodeEnum.ERROR_ENGINE_NOT_INITIALIZED.getCode(), ArcLinkErrorCodeEnum.ERROR_ENGINE_NOT_INITIALIZED.getMsgCn());
        deviceAccessMsgList.put(ArcLinkErrorCodeEnum.TOKEN_EXPIRED_RECONNECT_FAILED.getCode(), ArcLinkErrorCodeEnum.TOKEN_EXPIRED_RECONNECT_FAILED.getMsgCn());
        deviceAccessMsgList.put(ArcLinkErrorCodeEnum.ERROR_ILLEGAL_URL.getCode(), CommonUtils.getStrFromRes(R.string.server_address_is_not_valid));
        deviceAccessMsgList.put(ArcLinkErrorCodeEnum.ERROR_NULL_DATA.getCode(), ArcLinkErrorCodeEnum.ERROR_NULL_DATA.getMsgCn());
        deviceAccessMsgList.put(ArcLinkErrorCodeEnum.ERROR_FILE_NOT_EXISTS.getCode(), ArcLinkErrorCodeEnum.ERROR_FILE_NOT_EXISTS.getMsgCn());
        deviceAccessMsgList.put(ArcLinkErrorCodeEnum.ERROR_PARAM_IS_DIRECTORY.getCode(), ArcLinkErrorCodeEnum.ERROR_PARAM_IS_DIRECTORY.getMsgCn());
        deviceAccessMsgList.put(ArcLinkErrorCodeEnum.ERROR_AUTHORIZATION_EXPIRED.getCode(), ArcLinkErrorCodeEnum.ERROR_AUTHORIZATION_EXPIRED.getMsgCn());
        return deviceAccessMsgList;
    }
}
