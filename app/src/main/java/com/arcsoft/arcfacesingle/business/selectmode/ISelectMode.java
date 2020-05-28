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

package com.arcsoft.arcfacesingle.business.selectmode;

public interface ISelectMode {

    interface SelectModeCallback {

        /**
         * 清除数据
         * @param total 总数
         * @param progress 当前进度
         */
        void onCleanDataProgress(int total, int progress);

        /**
         * 数据清除失败
         */
        void onCleanDataFail();
    }

    interface ArcLinkInitCallback {

        /**
         * 初始化成功
         */
        void initSuccess();

        /**
         * 初始化失败
         * @param msg 失败信息
         */
        void initFail(String msg);
    }
}
