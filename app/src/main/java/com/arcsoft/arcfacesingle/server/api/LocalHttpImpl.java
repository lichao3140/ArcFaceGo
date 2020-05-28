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

import com.arcsoft.arcfacesingle.app.Constants;
import com.arcsoft.arcfacesingle.business.common.CommonRepository;
import com.arcsoft.arcfacesingle.business.serverapi.MultipleRegisterPersonManager;
import com.arcsoft.arcfacesingle.data.db.table.TableSettingConfigInfo;
import com.arcsoft.arcfacesingle.util.CommonUtils;
import com.arcsoft.arcfacesingle.util.SdcardUtils;
import com.arcsoft.asg.libcommon.util.common.FileUtils;
import com.arcsoft.asg.libnetwork.constant.NetworkConstants;
import com.arcsoft.asg.libnetwork.contract.LocalHttpCallback;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.koushikdutta.async.http.body.AsyncHttpRequestBody;
import com.koushikdutta.async.http.body.MultipartFormDataBody;
import com.koushikdutta.async.http.body.StringBody;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class LocalHttpImpl implements LocalHttpCallback {

    private static final String[] ARRAY_CONTENT_TYPE = {"text/plain", "multipart/form-data"};
    private static final String PARAM_FACE_LIST = "faceList";
    private static final String PARAM_MAIN_LOGO = "mainLogoBase64";
    private static final String PARAM_SECOND_LOGO = "viceLogoBase64";

    private LocalHttpImpl() {
    }

    private static class LocalApiImplHolder {
        private static LocalHttpImpl INSTANCE = new LocalHttpImpl();
    }

    public static LocalHttpImpl getInstance() {
        return LocalApiImplHolder.INSTANCE;
    }

    @Override
    public void onRetryListenPort(int i) {
        TableSettingConfigInfo configInfo = CommonRepository.getInstance().getSettingConfigInfo();
        configInfo.setDevicePort(String.valueOf(i));
    }

    @Override
    public String requestValidityCheckResult(int type) {
        String msg = ServerConstants.MSG_REQUEST_INVALID_AND_RETRY;
        switch (type) {
            case NetworkConstants.CODE_HEADER_INVALID:
                msg = ServerConstants.MSG_RESPONSE_HEADER_EMPTY;
                break;
            case NetworkConstants.CODE_CONTENT_TYPE_INVALID:
                msg = ServerConstants.MSG_RESPONSE_CONTENT_TYPE_EMPTY;
                break;
            case NetworkConstants.CODE_URL_INVALID:
                msg = ServerConstants.MSG_RESPONSE_URI_INVALID;
                break;
            default:
                break;
        }
        return LocalHttpApiDataUtils.getResponseStringFail(msg);
    }

    @Override
    public List<String> getUrlList() {
        return LocalHttpApiDataUtils.getServerUrlList();
    }

    @Override
    public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
        String uri = request.getPath();
        if (ServerConstants.URI_EQUIPMENT_GET_FINGER_PRINT_INFO.equals(uri)) {
            //获取设备指纹数据
            response.send(LocalHttpApiDataManager.getInstance().getFingerprintInfo());
            return;
        }
        AsyncHttpRequestBody requestBody = request.getBody();
        if (requestBody.getContentType().contains(ARRAY_CONTENT_TYPE[0])) {
            handleStringBody(request, uri, response);
        } else if (requestBody.getContentType().contains(ARRAY_CONTENT_TYPE[1])) {
            handleMultipartFormDataBody(request, uri, response);
        } else {
            response.send(LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_CONTENT_TYPE_EMPTY,
                    ServerConstants.MSG_RESPONSE_CONTENT_TYPE_EMPTY));
        }
    }

    private void handleStringBody(AsyncHttpServerRequest request, String uri, AsyncHttpServerResponse response) {
        StringBody stringBody = (StringBody) request.getBody();
        String requestJson = CommonUtils.uniCodeToCN(stringBody.toString());
        try {
            JSONObject jsonObject = new JSONObject(requestJson);
        } catch (Exception e) {
            response.send(LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_JSON_INVALID,
                    ServerConstants.MSG_RESPONSE_JSON_INVALID));
            return;
        }
        String netSign = request.getHeaders().getMultiMap().getString(Constants.HEADER_SIGN);
        if (TextUtils.isEmpty(netSign)) {
            response.send(LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_SIGN_PARAM_INVALID,
                    ServerConstants.MSG_RESPONSE_SIGN_INVALID));
            return;
        }

        if (ServerConstants.URI_EQUIPMENT_CONNECT.equals(uri)) {
            //设备连接
            response.send(LocalHttpApiDataManager.getInstance().connect(requestJson, netSign));
        } else {
            TableSettingConfigInfo configInfo = CommonRepository.getInstance().getSettingConfigInfo();
            String signKey = configInfo.getSignKey();
            if (TextUtils.isEmpty(signKey)) {
                response.send(LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_SIGN_LOCAL_EMPTY,
                        ServerConstants.MSG_RESPONSE_SIGN_KEY_INVALID));
                return;
            }
            if (uri.equals(ServerConstants.URI_PERSON_MANAGE_ADD)) {
                try {
                    JsonObject jsonObject = (JsonObject) new JsonParser().parse(requestJson);
                    if (jsonObject.has(PARAM_FACE_LIST)) {
                        jsonObject.remove(PARAM_FACE_LIST);
                    }
                    String personJson = jsonObject.toString();
                    if (!LocalHttpApiDataUtils.compareSign(personJson, signKey, netSign)) {
                        response.send(LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_SIGN_CHECK_FAILED,
                                ServerConstants.MSG_RESPONSE_SIGN_FAILED));
                        return;
                    }
                } catch (Exception e) {
                    response.send(LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_JSON_INVALID,
                            ServerConstants.MSG_RESPONSE_JSON_INVALID));
                    return;
                }
            } else if (uri.equals(ServerConstants.URI_EQUIPMENT_SET_LOGO)) {
                try {
                    JsonObject jsonObject = (JsonObject) new JsonParser().parse(requestJson);
                    if (jsonObject.has(PARAM_MAIN_LOGO)) {
                        jsonObject.remove(PARAM_MAIN_LOGO);
                    }
                    if (jsonObject.has(PARAM_SECOND_LOGO)) {
                        jsonObject.remove(PARAM_SECOND_LOGO);
                    }
                    String setLogoJson = jsonObject.toString();
                    if (!LocalHttpApiDataUtils.compareSign(setLogoJson, signKey, netSign)) {
                        response.send(LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_SIGN_CHECK_FAILED,
                                ServerConstants.MSG_RESPONSE_SIGN_FAILED));
                        return;
                    }
                } catch (Exception e) {
                    response.send(LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_JSON_INVALID,
                            ServerConstants.MSG_RESPONSE_JSON_INVALID));
                    return;
                }
            } else {
                if (!LocalHttpApiDataUtils.compareSign(requestJson, signKey, netSign)) {
                    response.send(LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_SIGN_CHECK_FAILED,
                            ServerConstants.MSG_RESPONSE_SIGN_FAILED));
                    return;
                }
            }
            responseData(response, requestJson, uri);
        }
    }

    private void handleMultipartFormDataBody(AsyncHttpServerRequest request, String uri, AsyncHttpServerResponse response) {
        if (ServerConstants.URI_PACKAGE_TRANSFER.equals(uri)) {
            if (LocalHttpApiDataManager.getInstance().isInstall) {
                response.send(LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_IN_PROCESS,
                        ServerConstants.MSG_PACKAGE_IN_PROCESS));
            }
            if (LocalHttpApiDataManager.getInstance().installVersionCode == -1) {
                response.send(LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_AUTHORITY_INVALID,
                        ServerConstants.MSG_PACKAGE_AUTHORITY_INVALID));
                return;
            }
            if (FileUtils.getSdcardAvailableSize() <= Constants.SDCARD_STORAGE_SIZE_DELETE) {
                responseData(response, null, uri);
                LocalHttpApiDataManager.getInstance().installVersionCode = -1;
                return;
            }
            LocalHttpApiDataManager.getInstance().isInstall = true;
            String fileName = SdcardUtils.getInstance().getAPKPath() + File.separator + LocalHttpApiDataManager.getInstance().installVersionCode + ".apk";
            File file = new File(fileName);
            if (file.exists()) {
                responseData(response, fileName, uri);
                LocalHttpApiDataManager.getInstance().installVersionCode = -1;
                LocalHttpApiDataManager.getInstance().isInstall = false;
                return;
            }
            FileUtils.createFileByDeleteOldFile(file);
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                MultipartFormDataBody multipartFormDataBody = (MultipartFormDataBody) request.getBody();
                multipartFormDataBody.setMultipartCallback(part -> {
                    if (!part.isFile()) {
                        return;
                    }
                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                    multipartFormDataBody.setDataCallback((emitter, bb) -> {
                        byte[] content = bb.getAllByteArray();
                        try {
                            bufferedOutputStream.write(content, 0, content.length);
                            bufferedOutputStream.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        bb.recycle();
                    });
                });
                request.setEndCallback(ex -> {
                    boolean isClose = true;
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        isClose = false;
                        e.printStackTrace();
                    }
                    if (ex == null && isClose) {
                        responseData(response, fileName, uri);
                    } else {
                        file.delete();
                        responseData(response, null, uri);
                    }
                    LocalHttpApiDataManager.getInstance().installVersionCode = -1;
                    LocalHttpApiDataManager.getInstance().isInstall = false;
                });
            } catch (FileNotFoundException e) {
                response.send(LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_PACKAGE_SAVE_FAILED,
                        ServerConstants.MSG_RESPONSE_DEVICE_SDCARD_STORAGE_LESS_MIN));
                LocalHttpApiDataManager.getInstance().installVersionCode = -1;
                LocalHttpApiDataManager.getInstance().isInstall = false;
            }
        } else if (ServerConstants.URI_PERSON_MANAGE_ADD_MULTIPLE.equals(uri)) {
            MultipleRegisterPersonManager.getInstance().registeringPerson(request, uri, response);
        } else {
            response.send(LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_API_EMPTY,
                    ServerConstants.MSG_RESPONSE_URI_INVALID));
        }
    }

    private void responseData(AsyncHttpServerResponse response, String requestJson, String uri) {
        switch (uri) {
            case ServerConstants.URI_PERSON_MANAGE_ADD:
                //人员添加
                response.send(LocalHttpApiDataManager.getInstance().personAdd(requestJson));
                break;
            case ServerConstants.URI_PERSON_MANAGE_DELETE:
                //人员删除
                LocalHttpApiDataManager.getInstance().personDelete(response, requestJson);
                break;
            case ServerConstants.URI_PERSON_MANAGE_PERSON_LIST:
                //人员列表
                response.send(LocalHttpApiDataManager.getInstance().personList(requestJson));
                break;
            case ServerConstants.URI_PERSON_MANAGE_DOOR_AUTHORITY:
                //人员权限修改
                response.send(LocalHttpApiDataManager.getInstance().doorAuthorityV1(requestJson));
                break;
            case ServerConstants.URI_PERSON_MANAGE_DOOR_AUTHORITY_V2:
                LocalHttpApiDataManager.getInstance().doorAuthorityV2(response, requestJson);
                break;
            case ServerConstants.URI_EQUIPMENT_SET_LOGO:
                //设置Logo
                response.send(LocalHttpApiDataManager.getInstance().setLogo(requestJson));
                break;
            case ServerConstants.URI_EQUIPMENT_GET_SERIAL:
                //获取序列号
                response.send(LocalHttpApiDataManager.getInstance().getSnCode());
                break;
            case ServerConstants.URI_EQUIPMENT_CLEAN_DATA:
                //清除数据
                response.send(LocalHttpApiDataManager.getInstance().cleanData());
                break;
            case ServerConstants.URI_EQUIPMENT_OPEN_DOOR:
                //远程开门
                response.send(LocalHttpApiDataManager.getInstance().openDoor());
                break;
            case ServerConstants.URI_EQUIPMENT_SETTING:
                //参数设置
                response.send(LocalHttpApiDataManager.getInstance().setting(requestJson));
                break;
            case ServerConstants.URI_EQUIPMENT_GET_SETTING:
                //获取参数设置
                response.send(LocalHttpApiDataManager.getInstance().getSetting(requestJson));
                break;
            case ServerConstants.URI_EQUIPMENT_REBOOT:
                //设备重启
                response.send(LocalHttpApiDataManager.getInstance().reboot());
                break;
            case ServerConstants.URI_EQUIPMENT_SYSTEM_TIME:
                //设置系统时间
                response.send(LocalHttpApiDataManager.getInstance().systemTime(requestJson));
                break;
            case ServerConstants.URI_EQUIPMENT_GET_LOGO:
                //获取设备logo
                response.send(LocalHttpApiDataManager.getInstance().getLogo(requestJson));
                break;
            case ServerConstants.URI_PACKAGE_AUTHORITY:
                response.send(LocalHttpApiDataManager.getInstance().packageAuthority(requestJson));
                break;
            case ServerConstants.URI_PACKAGE_TRANSFER:
                response.send(LocalHttpApiDataManager.getInstance().packageTransfer(requestJson));
                break;
            case ServerConstants.URI_EQUIPMENT_DISCONNECT:
                response.send(LocalHttpApiDataManager.getInstance().disconnect(requestJson));
                break;
            default:
                response.send(LocalHttpApiDataUtils.getResponseStringFail(ServerConstants.RESPONSE_CODE_API_EMPTY,
                        ServerConstants.MSG_RESPONSE_URI_INVALID));
                break;
        }
    }
}
