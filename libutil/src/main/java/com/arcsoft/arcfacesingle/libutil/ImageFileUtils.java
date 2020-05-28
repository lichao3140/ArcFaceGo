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

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Pair;

import com.arcsoft.asg.libcommon.util.common.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

//todo 此类包含图片编解码相关逻辑，其中的方法需要自行实现
public final class ImageFileUtils {

    private ImageFileUtils() {
    }

    /**
     * 将本地资源文件解码成bitmap
     * @param resId 资源id
     * @return
     */
    public static Bitmap getBitmap(int resId) {
        return ImageUtils.getBitmap(resId, Utils.getApp());
    }

    /**
     * 将指定文件解码成bitmap
     * @param file 指定文件
     * @return
     */
    public static Bitmap getBitmap(File file) {
        return ImageUtils.getBitmap(file);
    }

    /**
     * 将bitmap保存成图片至指定路径
     * @param src
     * @param filePath 指定路径
     * @param format
     * @return
     */
    public static boolean save(Bitmap src, String filePath, Bitmap.CompressFormat format) {
        return ImageUtils.save(src, filePath, format);
    }

    /**
     * 将bitmap保存成图片至指定文件路径
     * @param src
     * @param file
     * @param format
     * @return
     */
    public static boolean save(Bitmap src, File file, Bitmap.CompressFormat format) {
        return ImageUtils.save(src, file, format);
    }

    /**
     * 将bitmap保存成图片至指定路径
     * @param src
     * @param filePath
     * @param format
     * @param recycle
     * @return
     */
    public static boolean save(Bitmap src, String filePath, Bitmap.CompressFormat format, boolean recycle) {
        return ImageUtils.save(src, filePath, format, recycle);
    }

    /**
     * 将bitmap保存成图片至指定文件路径
     * @param src
     * @param file
     * @param quality
     * @param format
     * @return
     */
    public static boolean save(Bitmap src, File file, int quality, Bitmap.CompressFormat format) {
        return ImageUtils.save(src, file, format, quality, true);
    }

    /**
     * 将base64字符转成bitmap
     * @param base64String
     * @return
     */
    public static Bitmap base64ToBitmap(String base64String) {
        return ImageUtils.base64ToBitmap(base64String);
    }

    /**
     * 解码指定路径图片为bitmap
     * @param srcPath
     * @param size
     * @return
     */
    public static Bitmap getImagePng(String srcPath, int size) {
        return ImageUtils.getImagePng(srcPath, size);
    }

    /**
     * 图片压缩
     * @param bitmap
     * @param maxWidth
     * @param maxHeight
     * @return
     */
    public static Bitmap resizeImage(Bitmap bitmap, int maxWidth, int maxHeight) {
        return ImageUtils.resizeImage(bitmap, maxWidth, maxHeight);
    }

    /**
     * 将指定路径图片转成base64
     * @param path 图片路径
     * @return
     */
    public static String image2Base64(String path) {
        return ImageUtils.image2Base64(path);
    }

    /**
     * bitmap to base64
     * @param bitmap
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap) {
        return ImageUtils.bitmapToBase64(bitmap);
    }

    /**
     * 将图片压缩至指定宽、高
     * @param filePath
     * @param maxWidth
     * @param maxHeight
     * @return
     */
    public static Bitmap decodeFileWithThreshold(String filePath, int maxWidth, int maxHeight) {
        return ImageUtils.decodeFileWithThreshold(filePath, maxWidth, maxHeight);
    }

    /**
     * bitmap to bytes
     * @param bitmap
     * @param format
     * @return
     */
    public static byte[] bitmap2Bytes(Bitmap bitmap, Bitmap.CompressFormat format) {
        return ImageUtils.bitmap2Bytes(bitmap, format);
    }

    /**
     * 获取指定路径图片宽高
     * @param imgPath
     * @return Pair<宽度, 高度>
     */
    public static Pair<Integer, Integer> getImageOption(String imgPath) {
        return ImageUtils.getImageOption(imgPath);
    }

    /**
     * 将相机输出的nv21数据转成bitmap
     * @param nv21
     * @param width
     * @param height
     * @return
     */
    public static Bitmap nv21ToBitmap(byte[] nv21, int width, int height) {
        return ImageUtils.nv21ToBitmap(nv21, width, height);
    }

    /**
     * 变换bitmap
     * @param b 原始bitmap
     * @param rotateDegree 变换角度
     * @param mirror 镜像
     * @return
     */
    public static Bitmap getRotateBitmap(Bitmap b, float rotateDegree, boolean mirror) {
        return ImageUtils.getRotateBitmap(b, rotateDegree, mirror);
    }

    /**
     * 旋转bitmap
     * @param src
     * @param degrees
     * @param px
     * @param py
     * @return
     */
    public static Bitmap rotate(Bitmap src, int degrees, float px, float py) {
        return ImageUtils.rotate(src, degrees, px, py);
    }

    /**
     * 将byte数组转成bitmap
     * @param imgBytes
     * @param maxWidth
     * @param maxHeight
     * @return
     */
    public static Bitmap decodeFileWithThreshold(byte[] imgBytes, int maxWidth, int maxHeight) {
        return ImageUtils.decodeFileWithThreshold(imgBytes, maxWidth, maxHeight);
    }

    /**
     * 将bitmap的宽高对齐：宽是4的倍数，高是2的倍数
     * @param bm
     * @return
     */
    public static Bitmap setBitmap4Align(Bitmap bm) {
        return ImageUtils.setBitmap4Align(bm);
    }

    /**
     * bitmap压缩
     * @param bm
     * @param newWidth
     * @param newHeight
     * @return
     */
    public static Bitmap zoomImg(Bitmap bm, int newWidth, int newHeight) {
        return ImageUtils.zoomImg(bm, newWidth, newHeight);
    }

    /**
     * 将输入流转成bitmap
     * @param inputStream
     * @param maxWidth
     * @param maxHeight
     * @param chunkSize byte数组大小
     * @return
     * @throws IOException
     */
    public static Bitmap getBitmap(final InputStream inputStream, int maxWidth, int maxHeight, int chunkSize)
    throws IOException {
        return ImageUtils.getBitmap(inputStream, maxWidth, maxHeight, chunkSize);
    }

    /**
     * 按FD结果截取Bitmap
     *
     * @param rect
     * @param bitmap
     * @return
     */
    public static Bitmap getFaceRegisterCropBitmap(Rect rect, int orient, Bitmap bitmap) {
        return ImageUtils.getFaceRegisterCropBitmap(rect, orient, bitmap);
    }

    /**
     * byte to bitmap
     * @param imgArray
     * @return
     */
    public static Bitmap getBitmap(byte[] imgArray) {
        return ImageUtils.getBitmap(imgArray);
    }

    /**
     * inputStream to bitmap
     * @param inputStream
     * @return
     */
    public static Bitmap getBitmap(InputStream inputStream) {
        return ImageUtils.getBitmap(inputStream);
    }
}
