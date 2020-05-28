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

package com.arcsoft.arcfacesingle.libutil;

import com.arcsoft.asg.libcommon.contract.ICameraEngine;

public final class CameraEngineUtils {

    public static ICameraEngine createCameraFaceEngine() {
        //todo 需要自行实现ICameraEngine，用于管理Camera生命周期，如开启Camera，关闭Camera等
        return new Camera1Engine();
    }
}
