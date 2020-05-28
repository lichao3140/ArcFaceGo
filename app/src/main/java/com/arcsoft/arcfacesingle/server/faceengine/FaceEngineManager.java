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

package com.arcsoft.arcfacesingle.server.faceengine;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.arcsoft.arcfacesingle.app.BusinessErrorCode;
import com.arcsoft.arcfacesingle.business.common.CommonRepository;
import com.arcsoft.arcfacesingle.data.model.FaceExtractResult;
import com.arcsoft.asg.libcommon.util.common.Utils;
import com.arcsoft.faceengine.Config;
import com.arcsoft.faceengine.ErrorInfo;
import com.arcsoft.faceengine.FaceEngine;
import com.arcsoft.faceengine.FaceInfo;
import com.arcsoft.faceengine.PersonInfo;

import java.util.ArrayList;
import java.util.List;

public class FaceEngineManager {

    private FaceEngine faceEngine;

    public void createFaceEngine() {
        if (faceEngine == null) {
            faceEngine = new FaceEngine();
        }
    }

    public FaceEngine getFaceEngine() {
        return faceEngine;
    }

    public void createFaceEngine(FaceEngine.RecognizeCallback callback) {
        if (faceEngine == null) {
            faceEngine = new FaceEngine();
            faceEngine.setRecognizeCallback(callback);
        }
    }

    public int initFaceEngine(@NonNull Config config) {
        if (faceEngine != null) {
            return faceEngine.init(Utils.getApp(), config);
        }
        return ErrorInfo.MERR_ASF_ENGINE_BASE;
    }

    public int updateConfig(@NonNull Config config) {
        if (faceEngine != null) {
            return faceEngine.updateConfig(config);
        }
        return ErrorInfo.MERR_ASF_ENGINE_BASE;
    }

    public int initFaceEngine() {
        if (faceEngine != null) {
            Config config = CommonRepository.getInstance().getDefaultFaceEngineConfig();
            return faceEngine.init(Utils.getApp(), config);
        }
        return ErrorInfo.MERR_ASF_ENGINE_BASE;
    }

    public void unInitFaceEngine() {
        if (faceEngine != null) {
            faceEngine.uninit();
        }
    }

    public void unInitFaceEngine(boolean release) {
        if (faceEngine != null) {
            faceEngine.uninit();
            if (release) {
                faceEngine = null;
            }
        }
    }

    public void setFaceEngineCallback(FaceEngine.RecognizeCallback callback) {
        if (faceEngine != null) {
            faceEngine.setRecognizeCallback(callback);
        }
    }

    public void clearPersonInfo() {
        if (faceEngine != null) {
            faceEngine.clearPerson();
        }
    }

    public void addPersonInfo(@NonNull PersonInfo personInfo) {
        if (faceEngine != null) {
            List<PersonInfo> personInfoList = new ArrayList<>(1);
            personInfoList.add(personInfo);
            addPersonInfoList(personInfoList);
        }
    }

    public void addPersonInfoList(@NonNull List<PersonInfo> personInfoList) {
        if (faceEngine != null) {
            faceEngine.addPerson(personInfoList);
        }
    }

    public void updatePersonInfo(@NonNull PersonInfo personInfo) {
        if (faceEngine != null) {
            faceEngine.updatePerson(personInfo);
        }
    }

    public void removePersonInfo(long personId) {
        if (faceEngine != null) {
            faceEngine.removePerson(personId);
        }
    }

    public int detect(Bitmap bitmap, List<FaceInfo> faceInfo) {
        if (faceEngine != null) {
            return faceEngine.faceDetect(bitmap, faceInfo);
        }
        return ErrorInfo.MERR_ASF_ENGINE_BASE;
    }

    public void track(byte[] nv21, int width, int height, List<FaceInfo> faceInfoList) {
        if (faceEngine != null) {
            faceEngine.faceTrack(nv21, width, height, faceInfoList);
        }
    }

    public FaceExtractResult extract(Bitmap tempBitmap) {
        FaceInfo faceInfo = new FaceInfo();
        FaceExtractResult fdResult = new FaceExtractResult();
        int detectRes = faceEngine.faceExtract(tempBitmap, faceInfo);
        if (detectRes == ErrorInfo.MERR_ASF_ENGINE_NO_FACE) {
            fdResult.setResult(BusinessErrorCode.BEC_FACE_MANAGER_NO_FACE);
            return fdResult;
        }
        if (detectRes == ErrorInfo.MERR_ASF_ENGINE_MULTI_FACE) {
            fdResult.setResult(BusinessErrorCode.BEC_FACE_MANAGER_MORE_THAN_ONE_FACE);
            return fdResult;
        }
        if (detectRes == ErrorInfo.MERR_ASF_ENGINE_EXTRACT_FAIL) {
            fdResult.setResult(BusinessErrorCode.BEC_FACE_MANAGER_RECOGNIZE_FAIL);
            return fdResult;
        }
        if (detectRes == ErrorInfo.MERR_ASF_ENGINE_QUALITY_FAIL) {
            fdResult.setResult(BusinessErrorCode.BEC_FACE_MANAGER_FACE_QUALITY_FAIL);
            return fdResult;
        }
        if (detectRes != ErrorInfo.MOK) {
            fdResult.setResult(detectRes);
        } else {
            fdResult.setResult(BusinessErrorCode.BEC_COMMON_OK);
            fdResult.setFaceInfo(faceInfo);
        }
        return fdResult;
    }

    public int recognize(byte[] rgbData, byte[] irData, int width, int height, FaceInfo faceInfo) {
        if (faceEngine != null) {
            return faceEngine.faceRecognize(rgbData, irData, width, height, true, faceInfo);
        }
        return ErrorInfo.MERR_ASF_ENGINE_BASE;
    }

    public void pauseRecognize() {
        if (faceEngine != null) {
            faceEngine.pauseRecognize();
        }
    }

    public void release() {
        faceEngine = null;
    }
}
