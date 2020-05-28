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

package com.arcsoft.asg.libdeviceadapt.bean;

import android.util.Pair;

import java.io.Serializable;
import java.util.List;

public class DeviceCameraInfo implements Serializable {

    private List<Pair<Integer, Integer>> cameraSizeList;
    private int defPreviewW;
    private int defPreviewH;
    private List<Integer> cameraPosList;

    /**
     * 获取相机预览分辨率集
     *
     * @return 相机预览分辨率集合
     */
    public List<Pair<Integer, Integer>> getCameraSizeList() {
        return cameraSizeList;
    }

    /**
     * 设置相机预览分辨率集
     *
     * @param cameraSizeList 相机预览分辨率集合
     */
    public void setCameraSizeList(List<Pair<Integer, Integer>> cameraSizeList) {
        this.cameraSizeList = cameraSizeList;
    }

    /**
     * 获取默认的相机预览分辨率宽
     *
     * @return 分辨率宽度
     */
    public int getDefPreviewW() {
        return defPreviewW;
    }

    /**
     * 设置默认的相机预览分辨率宽
     *
     * @param defPreviewW 分辨率宽度
     */
    public void setDefPreviewW(int defPreviewW) {
        this.defPreviewW = defPreviewW;
    }

    /**
     * 获取默认的相机预览分辨率高
     *
     * @return 分辨率高度
     */
    public int getDefPreviewH() {
        return defPreviewH;
    }

    /**
     * 设置默认的相机预览分辨率高
     *
     * @param defPreviewH 分辨率高度
     */
    public void setDefPreviewH(int defPreviewH) {
        this.defPreviewH = defPreviewH;
    }

    /**
     * 获取相机位置集
     *
     * @return 相机位置集合，集合中包含参数{@code Camera.CameraInfo.CAMERA_FACING_BACK}等
     */
    public List<Integer> getCameraPosList() {
        return cameraPosList;
    }

    /**
     * 设置相机位置集合
     *
     * @param cameraPosList 相机位置集合
     */
    public void setCameraPosList(List<Integer> cameraPosList) {
        this.cameraPosList = cameraPosList;
    }
}
