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

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FileDownloadManager {

    private static final int NETWORK_TIME_OUT = 15;

    private static volatile FileDownloadManager manager;
    private OkHttpClient okHttpClient;

    public static FileDownloadManager getInstance() {
        if (manager == null) {
            synchronized (FileDownloadManager.class) {
                if (manager == null) {
                    manager = new FileDownloadManager();
                }
            }
        }
        return manager;
    }

    private FileDownloadManager() {
        if (okHttpClient == null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.retryOnConnectionFailure(true)
                    .connectTimeout(NETWORK_TIME_OUT, TimeUnit.SECONDS)
                    .writeTimeout(NETWORK_TIME_OUT, TimeUnit.SECONDS)
                    .readTimeout(NETWORK_TIME_OUT, TimeUnit.SECONDS);
            okHttpClient = builder.build();
        }
    }

    public Request getRequest(String url) {
        return new Request.Builder()
                .url(url)
                .build();
    }

    public void downloadFile(String url, FileDownloadListener listener) {
        if (okHttpClient != null) {
            okHttpClient.newCall(getRequest(url)).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (listener != null) {
                        listener.onFailure(call, e);
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (listener != null) {
                        listener.onResponse(call, response);
                    }
                }
            });
        }
    }

    public byte[] downloadFile(String url) {
        try {
            Response response = okHttpClient.newCall(getRequest(url)).execute();
            if (response != null && response.body() != null) {
                return response.body().bytes();
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public interface FileDownloadListener {

        /**
         * 下载失败
         * @param call Call
         * @param e 异常信息
         */
        void onFailure(Call call, IOException e);

        /**
         * 下载回调
         * @param call Call
         * @param response Response回调
         */
        void onResponse(Call call, Response response);
    }
}
