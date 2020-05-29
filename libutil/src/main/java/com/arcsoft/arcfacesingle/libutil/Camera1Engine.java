package com.arcsoft.arcfacesingle.libutil;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Pair;

import com.arcsoft.asg.libcommon.contract.ICameraEngine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * ICameraEngine的实现
 */
public class Camera1Engine extends ICameraEngine {

    private static final int COUNT_CAMERA_ONE = 1;
    private static final int COUNT_CAMERA_TWO = 2;
    private Camera camera;
    private boolean usePreAlloc;

    @Override
    public void setUsePreAlloc(boolean usePreAlloc) {
        this.usePreAlloc = usePreAlloc;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        super.onPreviewFrame(data, camera);
        if (usePreAlloc) {
            camera.addCallbackBuffer(data);
        }
    }

    @Override
    public void startCamera(SurfaceTexture surfaceTexture, int cameraId, int previewRotation, int previewWidth,
                            int previewHeight, int bufferCount, int bufferSize) throws IOException {
        camera = Camera.open(cameraId);
        camera.setDisplayOrientation(previewRotation);
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewFormat(ImageFormat.NV21);
        parameters.setPreviewSize(previewWidth, previewHeight);
        List<String> supportedFocusModes = parameters.getSupportedFocusModes();
        if (supportedFocusModes != null && supportedFocusModes.size() > 0) {
            if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
        }
        camera.setParameters(parameters);
        camera.setPreviewTexture(surfaceTexture);
        if (usePreAlloc) {
            for (int i = 0; i < bufferCount; i++) {
                camera.addCallbackBuffer(new byte[bufferSize]);
            }
            camera.setPreviewCallbackWithBuffer(this);
        } else {
            camera.setPreviewCallback(this);
        }
        camera.startPreview();
    }

    @Override
    public void setPreviewTexture(SurfaceTexture surfaceTexture) {
        try {
            if (camera != null) {
                camera.setPreviewTexture(surfaceTexture);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void releaseCamera() {
        if (camera != null) {
            if (usePreAlloc) {
                camera.setPreviewCallbackWithBuffer(null);
            } else {
                camera.setPreviewCallback(null);
            }
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    public List<Integer> getCameraPositionList() {
        List<Integer> cameraPosList = new ArrayList<>();
        int cameraPosBack = Camera.CameraInfo.CAMERA_FACING_BACK;
        int cameraPosFront = Camera.CameraInfo.CAMERA_FACING_FRONT;
        int cameraCount = Camera.getNumberOfCameras();
        if (cameraCount == COUNT_CAMERA_ONE) {
            cameraPosList.add(cameraPosBack);
        } else if (cameraCount >= COUNT_CAMERA_TWO) {
            cameraPosList.add(cameraPosBack);
            cameraPosList.add(cameraPosFront);
        }
        return cameraPosList;
    }

    @Override
    public List<Pair<Integer, Integer>> getCameraPreviewSizeList() {
        List<Camera.Size> commonSizeList = new ArrayList<>();
        int cameraPosBack = Camera.CameraInfo.CAMERA_FACING_BACK;
        int cameraPosFront = Camera.CameraInfo.CAMERA_FACING_FRONT;
        int cameraCount = Camera.getNumberOfCameras();
        if (cameraCount == COUNT_CAMERA_ONE) {
            try {
                Camera backCamera = Camera.open(cameraPosBack);
                commonSizeList = backCamera.getParameters().getSupportedPreviewSizes();
                backCamera.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (cameraCount >= COUNT_CAMERA_TWO) {
            List<Camera.Size> backSizeList;
            try {
                Camera.CameraInfo cameraInfo2 = new Camera.CameraInfo();
                Camera.getCameraInfo(cameraPosBack, cameraInfo2);
                Camera backCamera = Camera.open(cameraPosBack);
                backSizeList = backCamera.getParameters().getSupportedPreviewSizes();
                backCamera.release();
            } catch (Exception e) {
                backSizeList = new ArrayList<>();
            }
            List<Camera.Size> frontSizeList;
            try {
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(cameraPosFront, cameraInfo);
                Camera frontCamera = Camera.open(cameraPosFront);
                frontSizeList = frontCamera.getParameters().getSupportedPreviewSizes();
                frontCamera.release();
            } catch (Exception e) {
                frontSizeList = new ArrayList<>();
            }
            if (frontSizeList.size() > 0 && backSizeList.size() > 0) {
                for (Camera.Size rgbPreviewSize : frontSizeList) {
                    for (Camera.Size irPreviewSize : backSizeList) {
                        if (irPreviewSize.width == rgbPreviewSize.width && irPreviewSize.height == rgbPreviewSize.height) {
                            commonSizeList.add(irPreviewSize);
                        }
                    }
                }
            } else if (frontSizeList.size() > 0) {
                commonSizeList = frontSizeList;
            } else if (backSizeList.size() > 0) {
                commonSizeList = backSizeList;
            }
        }
        Collections.sort(commonSizeList, ((size, t1) -> {
            int leftWidth = size.width;
            int rightWidth = t1.width;
            return leftWidth - rightWidth;
        }));
        List<Camera.Size> sizeList = new ArrayList<>(new LinkedHashSet<>(commonSizeList));
        List<Pair<Integer, Integer>> cameraPairList = new ArrayList<>();
        if (sizeList.size() > 0) {
            for (Camera.Size size : sizeList) {
                int sizeWidth = size.width;
                int sizeHeight = size.height;
                Pair<Integer, Integer> sizePair = new Pair<>(sizeWidth, sizeHeight);
                cameraPairList.add(sizePair);
            }
        }
        return cameraPairList;
    }
}
