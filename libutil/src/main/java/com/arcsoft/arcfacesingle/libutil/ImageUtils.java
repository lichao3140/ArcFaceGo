package com.arcsoft.arcfacesingle.libutil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Pair;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 图片管理工具类
 */
public final class ImageUtils {
    private static final int FACE_DETECT_IMAGE_WIDTH_LIMIT = 4;
    private static final int FACE_DETECT_IMAGE_HEIGHT_LIMIT = 2;

    private ImageUtils() {
        throw new UnsupportedOperationException("无法初始化！");
    }

    /**
     * 判断bitmap是否为空
     *
     * @param src Bitmap对象
     * @return bitmap是否为空
     */
    private static boolean isEmptyBitmap(final Bitmap src) {
        return src == null || src.getWidth() == 0 || src.getHeight() == 0;
    }


    /**
     * 根据压缩格式将Bitmap转换为压缩后的图像数据
     *
     * @param bitmap Bitmap对象
     * @param format 压缩格式
     * @return 压缩后的图像数据
     */
    public static byte[] bitmap2Bytes(final Bitmap bitmap, final Bitmap.CompressFormat format) {
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(format, 100, baos);
        return baos.toByteArray();
    }

    /**
     * 将图片文件转换为Bitmap
     *
     * @param file 图片文件
     * @return Bitmap对象
     */
    public static Bitmap getBitmap(final File file) {
        if (file == null) {
            return null;
        }
        return BitmapFactory.decodeFile(file.getAbsolutePath());
    }

    /**
     * 根据最大宽高，将文件转换为Bitmap
     *
     * @param filePath  文件路径
     * @param maxWidth  最大宽度
     * @param maxHeight 最大高度
     * @return Bitmap对象
     */
    public static Bitmap getBitmap(final String filePath, int maxWidth, int maxHeight) {
        if (isSpace(filePath)) {
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = calculateInSampleSize4(options, maxWidth, maxHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * 根据最大宽高，将压缩图像数据转换为Bitmap
     *
     * @param imageBytes 压缩的图像数据，如jpeg/png等
     * @param maxWidth   最大宽度
     * @param maxHeight  最大高度
     * @return Bitmap对象
     */
    public static Bitmap getBitmap(final byte[] imageBytes, int maxWidth, int maxHeight) {
        if (imageBytes == null || imageBytes.length == 0) {
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, options);
        options.inSampleSize = calculateInSampleSize4(options, maxWidth, maxHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, options);
    }

    /**
     * 根据最大宽高，将图片文件转换为Bitmap
     *
     * @param filePath  图片文件路径
     * @param maxWidth  最大宽度
     * @param maxHeight 最大高度
     * @return Bitmap对象
     */
    public static Bitmap decodeFileWithThreshold(final String filePath, int maxWidth, int maxHeight) {
        Bitmap tempBitmap = getBitmap(filePath, maxWidth, maxHeight);
        if (tempBitmap != null) {
            if (tempBitmap.getWidth() > maxWidth || tempBitmap.getHeight() > maxHeight) {
                Bitmap scaleBitmap = resizeImage(tempBitmap, maxWidth, maxHeight);
                if (!tempBitmap.isRecycled()) {
                    tempBitmap.recycle();
                }
                return scaleBitmap;
            }
            return tempBitmap;
        }
        return null;
    }

    /**
     * 根据最大宽高，将图片数据转换为Bitmap
     *
     * @param imgBytes  图片数据，可以是JPEG/PNG等格式的压缩数据
     * @param maxWidth  最大宽度
     * @param maxHeight 最大高度
     * @return Bitmap对象
     */
    public static Bitmap decodeFileWithThreshold(byte[] imgBytes, int maxWidth, int maxHeight) {
        Bitmap tempBitmap = getBitmap(imgBytes, maxWidth, maxHeight);
        if (tempBitmap != null) {
            if (tempBitmap.getWidth() > maxWidth || tempBitmap.getHeight() > maxHeight) {
                Bitmap scaleBitmap = resizeImage(tempBitmap, maxWidth, maxHeight);
                if (!tempBitmap.isRecycled()) {
                    tempBitmap.recycle();
                }
                return scaleBitmap;
            }
            return tempBitmap;
        }
        return null;
    }

    /**
     * 根据资源ID获取Bitmap
     *
     * @param resId 资源ID
     * @return Bitmap对象
     */
    public static Bitmap getBitmap(@DrawableRes final int resId, Context context) {
        Drawable drawable = ContextCompat.getDrawable(context, resId);
        if (drawable == null) {
            return null;
        }
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static int calculateInSampleSize4(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int sampleSize = 1;
        int picWidth = options.outWidth;
        int picHeight = options.outHeight;
        if (picWidth > reqWidth || picHeight > reqHeight) {
            final int widthRatio = picWidth / reqWidth;
            final int heightRatio = picHeight / reqHeight;
            sampleSize = Math.max(heightRatio, widthRatio);
        }
        return sampleSize;
    }


    /**
     * 保存图片
     *
     * @param src     Bitmap对象
     * @param file    要保存的file路径
     * @param format  图片格式
     * @param recycle 是否回收bitmap
     * @return 是否保存成功
     */
    public static boolean save(final Bitmap src, final File file, final Bitmap.CompressFormat format, final int quality, final boolean recycle) {
        if (isEmptyBitmap(src)) {
            return false;
        }
        if (!createFileByDeleteOldFile(file)) {
            if (recycle && !src.isRecycled()) {
                src.recycle();
            }
            return false;
        }
        OutputStream os;
        FileOutputStream fileOutputStream = null;
        boolean ret = false;
        try {
            fileOutputStream = new FileOutputStream(file);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                os = new BufferedOutputStream(fileOutputStream);
                ret = src.compress(format, quality, os);
                try {
                    os.flush();
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    fileOutputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (recycle && !src.isRecycled()) {
                    src.recycle();
                }
            }
        }
        return ret;
    }

    public static boolean save(final Bitmap src, final File file, final Bitmap.CompressFormat format) {
        return save(src, file, format, 100, false);
    }

    public static boolean save(final Bitmap src, final File file, final int quality, final Bitmap.CompressFormat format) {
        return save(src, file, format, quality, true);
    }

    public static boolean save(final Bitmap src, final String filePath, final Bitmap.CompressFormat format, final boolean recycle) {
        return save(src, getFileByPath(filePath), format, 100, recycle);
    }

    public static boolean save(final Bitmap src, final String filePath, final Bitmap.CompressFormat format) {
        return save(src, getFileByPath(filePath), format, 100, false);
    }

    /**
     * 对文件进行base64编码，获取编码后的字符串信息
     *
     * @param path 文件路径
     * @return base64编码后的文件内容
     */
    public static String image2Base64(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        InputStream is = null;
        byte[] data;
        String result = null;
        try {
            is = new FileInputStream(path);
            data = new byte[is.available()];
            int ret = is.read(data);
            result = Base64.encodeToString(data, Base64.DEFAULT);
            data = null;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return result;
    }

    /**
     * 对Bitmap进行压缩，以30的质量压缩为jpeg格式数据，再进行base64编码，获取编码后的字符串信息
     *
     * @param bitmap Bitmap对象
     * @return base64编码后的文件内容
     */
    public static String bitmapToBase64(Bitmap bitmap) {
        // 要返回的字符串
        String reslut = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                //压缩只对保存有效果bitmap还是原来的大小
                bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
                baos.flush();
                baos.close();
                // 转换为字节数组
                byte[] byteArray = baos.toByteArray();
                // 转换为字符串
                reslut = Base64.encodeToString(byteArray, Base64.DEFAULT);
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return reslut;
    }


    /**
     * 将base64编码的图像数据解码为Bitmap对象
     *
     * @param base64String base64编码的图像数据
     * @return Bitmap对象
     */
    public static Bitmap base64ToBitmap(String base64String) {
        Bitmap bitmap = null;
        byte[] decode = null;
        try {
            decode = Base64.decode(base64String, Base64.DEFAULT);
        } catch (Exception e) {
        }
        if (decode != null && decode.length != 0) {
            try {
                bitmap = BitmapFactory.decodeByteArray(decode, 0, decode.length);
            } catch (Exception e) {
            }
        }

        return bitmap;
    }


    /**
     * 将nv21数据转Bitmap
     *
     * @param nv21   NV21数据
     * @param width  NV21图像宽度
     * @param height NV21图像高度
     * @return 转换后的bitmap对象
     */
    public static Bitmap nv21ToBitmap(byte[] nv21, int width, int height) {
        Bitmap bitmap = null;
        try {
            YuvImage image = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compressToJpeg(new Rect(0, 0, width, height), 100, stream);
            bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 根据旋转角度、是否镜像处理Bitmap
     *
     * @param b            bitmap对象
     * @param rotateDegree 旋转角度
     * @param mirror       是否镜像
     * @return 处理后的Bitmap对象
     */
    public static Bitmap getRotateBitmap(Bitmap b, float rotateDegree, boolean mirror) {
        Matrix matrix = new Matrix();
        if (mirror) {
            if (rotateDegree == 90 || rotateDegree == 270) {
                matrix.postScale(1, -1);
            } else {
                matrix.postScale(-1, 1);
            }
        }
        matrix.postRotate(rotateDegree);
        int width = b.getWidth();
        int height = b.getHeight();
        if (width % 4 != 0) {
            width = width >> 2 << 2;
        }
        if (height % 4 != 0) {
            height = height >> 2 << 2;
        }
        return Bitmap.createBitmap(b, 0, 0, width, height, matrix, true);
    }

    /**
     * 旋转Bitmap
     *
     * @param src     原bitmap
     * @param degrees 旋转角度
     * @param px      旋转中心点的x坐标
     * @param py      旋转中心点的y坐标
     * @return Bitmap对象
     */
    public static Bitmap rotate(final Bitmap src, final int degrees, final float px, final float py) {
        return rotate(src, degrees, px, py, false);
    }

    /**
     * 旋转Bitmap
     *
     * @param src     原bitmap
     * @param degrees 旋转角度
     * @param px      旋转中心点的x坐标
     * @param py      旋转中心点的y坐标
     * @param recycle 是否回收bitmap
     * @return 旋转后的bitmap
     */
    public static Bitmap rotate(final Bitmap src, final int degrees, final float px, final float py, final boolean recycle) {
        if (isEmptyBitmap(src)) {
            return null;
        }
        if (degrees == 0) {
            return src;
        }
        Matrix matrix = new Matrix();
        matrix.setRotate(degrees, px, py);
        Bitmap ret = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, false);
        if (recycle && !src.isRecycled() && ret != src) {
            src.recycle();
        }
        return ret;
    }

    /**
     * 将图片文件转换为Bitmap对象
     * TODO: 未理解size参数的概念，可自行理解ArcFaceGo再修改该函数
     *
     * @param srcPath 图片文件
     * @param size
     * @return Bitmap对象
     */
    public static Bitmap getImagePng(String srcPath, int size) {
        return BitmapFactory.decodeFile(srcPath);
    }

    /**
     * 根据最大宽高等比缩放Bitmap
     *
     * @param bitmap    原Bitmap
     * @param maxWidth  最大宽度
     * @param maxHeight 最大高度
     * @return 等比缩放后的Bitmap对象
     */
    public static Bitmap resizeImage(Bitmap bitmap, int maxWidth, int maxHeight) {
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        float scale = Math.min((float) maxWidth / bitmapWidth, (float) maxHeight / bitmapHeight);
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, false);
    }

    /**
     * 确保Bitmap宽度是4的倍数，并且高度是2的倍数
     *
     * @param bm Bitmap对象
     * @return 宽度是4的倍数，并且高度是2的倍数的Bitmap
     */
    public static Bitmap setBitmap4Align(Bitmap bm) {
        Bitmap newBitmap;
        int width = bm.getWidth();
        int height = bm.getHeight();
        int newWidth = width;
        int newHeight = height;
        if (newWidth % FACE_DETECT_IMAGE_WIDTH_LIMIT != 0) {
            newWidth = newWidth >> 2 << 2;
        }
        if (newHeight % FACE_DETECT_IMAGE_HEIGHT_LIMIT != 0) {
            newHeight = newHeight & ~1;
        }
        // 计算缩放比例.
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数.
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片.
        newBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return newBitmap;
    }

    /**
     * 缩放Bitmap对象
     *
     * @param bitmap    Bitmap对象
     * @param newWidth  新的宽度
     * @param newHeight 新的高度
     * @return 缩放后的Bitmap对象
     */
    public static Bitmap zoomImg(Bitmap bitmap, int newWidth, int newHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }

    /**
     * 获取图片宽高
     *
     * @param imgPath 图片文件路径
     * @return 图片宽高
     */
    public static Pair<Integer, Integer> getImageOption(String imgPath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imgPath, options);
        return new Pair<>(options.outWidth, options.outHeight);
    }

    /**
     * 判断字符串是否为空格类字符串
     *
     * @param str 待判断的字符串
     * @return 是否为空格类字符串
     */
    public static boolean isSpace(final String str) {
        if (str == null) {
            return true;
        }
        for (int i = 0, len = str.length(); i < len; ++i) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 创建文件
     *
     * @param file 文件
     * @return 是否创建成果
     */
    public static boolean createFileByDeleteOldFile(final File file) {
        if (file == null) {
            return false;
        }
        if (file.exists() && !file.delete()) {
            return false;
        }
        if (!createDirOrDirExist(file.getParentFile())) {
            return false;
        }
        try {
            return file.createNewFile();
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 查看文件夹是否存在，如果不存在，则创建文件夹
     *
     * @param file 文件夹对象
     * @return 最终是否存在
     */
    public static boolean createDirOrDirExist(final File file) {
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    /**
     * 将文件路径转换为文件对象
     *
     * @param path 文件路径
     * @return 文件对象
     */
    public static File getFileByPath(final String path) {
        return isSpace(path) ? null : new File(path);
    }

    /**
     * 将输入流转换为Bitmap对象
     *
     * @param inputStream 输入流
     * @param maxWidth    最大宽度
     * @param maxHeight   最大高度
     * @param chunkSize   临时buffer大小
     * @return Bitmap对象
     * @throws IOException 读取inputStream时可能出现的异常
     */
    public static Bitmap getBitmap(final InputStream inputStream, int maxWidth, int maxHeight, int chunkSize)
            throws IOException {
        if (inputStream == null) {
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        byte[] data = inputStream2ByteArr(inputStream, chunkSize);

        BitmapFactory.decodeByteArray(data, 0, data.length, options);

        options.inSampleSize = ImageUtils.calculateInSampleSize4(options, maxWidth, maxHeight);
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }

    /**
     * 将输入流转换为字节数组
     *
     * @param inputStream 输入流
     * @param chunkSize   临时buffer的大小
     * @return 字节数组
     * @throws IOException 读取输入流时可能会产生的异常
     */
    private static byte[] inputStream2ByteArr(InputStream inputStream, int chunkSize) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buff = new byte[chunkSize];
        int len;
        while ((len = inputStream.read(buff)) != -1) {
            outputStream.write(buff, 0, len);
        }
        outputStream.flush();
        outputStream.close();
        return outputStream.toByteArray();
    }

    /**
     * 按人脸检测结果截取bitmap
     *
     * @param rect   人脸位置
     * @param orient 人脸角度，详见ArcFace文档
     * @param bitmap 原bitmap
     * @return 裁剪后的bitmap
     */
    public static Bitmap getFaceRegisterCropBitmap(Rect rect, int orient, Bitmap bitmap) {
        if (rect == null || bitmap == null) {
            return null;
        }
        int length = Math.max(rect.width(), rect.height()) / 2;
        int wPadding = Math.min(Math.min(rect.left, bitmap.getWidth() - rect.right), length);
        int width = rect.width() + 2 * wPadding;
        int x = rect.left - wPadding;
        int tPadding = Math.min(rect.top, length);
        int y = rect.top - tPadding;
        int bPadding = Math.min(bitmap.getHeight() - rect.bottom, length);
        int height = rect.height() + tPadding + bPadding;
        if ((width & 0b11) != 0) {
            width &= ~0b11;
        }
        if ((height & 1) != 0) {
            height &= ~1;
        }
        Matrix matrix = new Matrix();
        if (orient == 2) {
            matrix.setRotate(90);
        } else if (orient == 4) {
            matrix.setRotate(180);
        } else if (orient == 3) {
            matrix.setRotate(270);
        }

        return Bitmap.createBitmap(bitmap, x, y, width, height, matrix, true);
    }

    /**
     * 将图像信息转换为Bitmap对象
     *
     * @param imgArray 图像数据，可以是JPG\PNG等格式
     * @return Bitmap对象
     */
    public static Bitmap getBitmap(byte[] imgArray) {
        return BitmapFactory.decodeByteArray(imgArray, 0, imgArray.length);
    }

    /**
     * 将输入流转换为Bitmap对象
     *
     * @param inputStream 输入流
     * @return Bitmap对象
     */
    public static Bitmap getBitmap(InputStream inputStream) {
        return BitmapFactory.decodeStream(inputStream);
    }
}
