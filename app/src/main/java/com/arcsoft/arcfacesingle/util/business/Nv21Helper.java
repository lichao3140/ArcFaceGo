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

package com.arcsoft.arcfacesingle.util.business;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.*;

public class Nv21Helper {

    private RenderScript rs;
    private ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic;
    private Type.Builder yuvType, rgbaType;
    private Allocation in, out;
    private int mWidth;
    private int mHeight;
    private final Object lock = new Object();

    public Nv21Helper(Context context) {
        rs = RenderScript.create(context);
        yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));
    }

    public Bitmap nv21ToBitmap(byte[] nv21, int width, int height){
        synchronized (lock) {
            if (yuvType == null || mWidth != width || mHeight != height) {
                mWidth = width;
                mHeight = height;
                yuvType = new Type.Builder(rs, Element.U8(rs)).setX(nv21.length);
                in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);

                rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(width).setY(height);
                out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);
            }

            in.copyFrom(nv21);

            yuvToRgbIntrinsic.setInput(in);
            yuvToRgbIntrinsic.forEach(out);

            Bitmap bmpout = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            out.copyTo(bmpout);

            return bmpout;
        }
    }
}
