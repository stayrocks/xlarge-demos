/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.photoalbum;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

/**
 * Displays a photo and its shadow.
 */
class PhotoView extends View {
    private static Bitmap sShadow;
    private static Rect sSrc;

    private final Rect mDst = new Rect();
    private final Bitmap mPhotoBitmap;

    PhotoView(Context context, Bitmap photoBitmap) {
        super(context);

        mPhotoBitmap = photoBitmap;

        if (sShadow == null) {
            sShadow = BitmapFactory.decodeResource(context.getResources(), R.drawable.photo_shadow);
            sSrc = new Rect(0, 0, sShadow.getWidth(), sShadow.getHeight());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getContentWidth(), mPhotoBitmap.getHeight());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        final int shadowWidth = sShadow.getWidth();
        mDst.set(w - shadowWidth, 0, w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mPhotoBitmap, 0, 0, null);
        canvas.drawBitmap(sShadow, sSrc, mDst, null);
    }

    /**
     * Returns the content width, in pixel, of this view. This method
     * can be safely used before a layout pass happens to know how
     * wide the view is going to be.
     */
    int getContentWidth() {
        return mPhotoBitmap.getWidth() + sShadow.getWidth();
    }
}
