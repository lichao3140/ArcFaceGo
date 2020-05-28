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

package com.arcsoft.arcfacesingle.util.glide;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;

import com.arcsoft.arcfacesingle.R;
import com.arcsoft.asg.libcommon.util.common.ActivityUtils;
import com.arcsoft.asg.libcommon.util.common.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

public final class GlideUtils {

    private GlideUtils() {
        throw new UnsupportedOperationException("无法初始化！");
    }

    public static void loadRecognizeHead(String imagePath, ImageView imageView) {
        RequestOptions options = new RequestOptions()
                .error(R.drawable.d_icon)
                .placeholder(R.drawable.d_icon)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE);
        Activity activity = ActivityUtils.getTopActivity();
        if (ActivityUtils.getTopActivity() == null || activity.isFinishing()) {
            return;
        }
        Glide.with(ActivityUtils.getTopActivity())
                .load(imagePath)
                .apply(options)
                .into(imageView);
    }

    public static void loadImageLogo(String imagePath, ImageView imageView) {
        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE);
        Activity activity = ActivityUtils.getTopActivity();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        Glide.with(ActivityUtils.getTopActivity())
                .load(imagePath)
                .apply(options)
                .into(imageView);
    }

    public static void loadImageLogo(Bitmap bmp, ImageView imageView) {
        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE);
        Activity activity = ActivityUtils.getTopActivity();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        Glide.with(ActivityUtils.getTopActivity())
                .load(bmp)
                .apply(options)
                .into(imageView);
    }

    public static void loadPersonAdapterImage(String imagePath, ImageView imageView) {
        Activity activity = ActivityUtils.getTopActivity();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        GlideApp.with(activity)
                .load(imagePath)
                .transform(new GlideRoundTransform(Utils.getApp().getResources().getDimensionPixelOffset(R.dimen.x5)))
                .error(R.mipmap.ic_default_head)
                .placeholder(R.mipmap.ic_default_head)
                .into(imageView);
    }

    private static RequestBuilder<Drawable> loadTransform(Activity activity, @DrawableRes int placeholderId) {
        return GlideApp.with(activity)
                .load(placeholderId)
                .centerCrop()
                .transform(new GlideRoundTransform(activity.getResources().getDimensionPixelOffset(R.dimen.x10)));
    }

    public static void loadPersonAdapterImage2(String imagePath, ImageView imageView) {
        Activity activity = ActivityUtils.getTopActivity();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        int placeId = R.mipmap.ic_default_head;
        int errorId = R.mipmap.ic_default_head;
        RequestOptions requestOptionsX10 = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .placeholder(placeId)
                .error(errorId)
                .transform(new GlideRoundTransform(activity.getResources().getDimensionPixelOffset(R.dimen.x10)));
        Glide.with(ActivityUtils.getTopActivity())
                .load(imagePath)
                .apply(requestOptionsX10)
                .thumbnail(loadTransform2(activity, placeId))
                .thumbnail(loadTransform2(activity, errorId))
                .into(imageView);
    }

    private static RequestBuilder<Drawable> loadTransform2(Activity activity, @DrawableRes int placeholderId) {
        return Glide.with(activity)
                .load(placeholderId)
                .apply(new RequestOptions()
                        .centerCrop()
                        .transform(new GlideRoundTransform(activity.getResources().getDimensionPixelOffset(R.dimen.x10))));

    }
}
