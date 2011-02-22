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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import java.util.List;

@SuppressWarnings({"UnusedDeclaration"})
public class AlbumActivity extends MapActivity {
    private static final int LIST_TOP_PADDING = 13;

    private ViewGroup mStack;
    private List<Photo> mPhotos;

    private View mPanel;
    private boolean mPanelVisible = true;
    private ObjectAnimator mPanelAnimator;

    private LruCache<Integer,Bitmap> mCache;

    private View mPhotoInfo;
    private MapView mLocationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.album_activity);
        getWindow().setBackgroundDrawable(null);

        mPanel = findViewById(R.id.panel);

        // A real application should do this on a background thread
        mPhotos = new AlbumLoader(this).loadPhotos();

        setupDeck();
        setupAlbumList();
        setupStack();
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    /**
     * Prepares the cards deck.
     */
    private void setupDeck() {
        mPhotoInfo = findViewById(R.id.photo_info);

        mLocationView = (MapView) findViewById(R.id.photo_location);
        mLocationView.setSatellite(true);
        mLocationView.getController().setZoom(12);
    }

    /**
     * Prepares the photos stack.
     */
    private void setupStack() {
        mCache = new LruCache<Integer, Bitmap>(5);

        mStack = (ViewGroup) findViewById(R.id.stack);
        // Reset the stack and display the first photo
        setFullPhoto(mPhotos.get(0));

        mStack.setClickable(true);
        mStack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePanel();
            }
        });
    }

    /**
     * Resets the photo stack and displays the specified photo only.
     * 
     * @param photo The photo to display in the stack
     */
    private void setFullPhoto(Photo photo) {
        mStack.removeAllViews();

        // This should be done on a background thread in a real application
        final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), photo.photoResource);
        mCache.put(photo.photoResource, bitmap);

        addPhotoInStack(photo, bitmap);
        bindLocation(photo, makeMarker(photo.thumbnail));
    }

    /**
     * Adds the specified photo to the photos stack.
     * 
     *
     * @param photo The photo to display in the stack
     * @param bitmap The bitmap to display in the stack
     * 
     * @return The generated PhotoView used to display the specified photo
     */
    private PhotoView addPhotoInStack(Photo photo, Bitmap bitmap) {
        PhotoView view = new PhotoView(this, bitmap);
        PhotoTag tag = new PhotoTag(photo);
        view.setTag(tag);

        bindPhotoInfo(photo);
        
        mStack.addView(view, createStackLayoutParams());

        return view;
    }

    /**
     * Displays info about the specified photo.
     * 
     * @param photo The photo object to bind to the UI
     */
    private void bindPhotoInfo(Photo photo) {
        setText(R.id.photo_name, photo.name);
        setText(R.id.photo_camera, photo.camera);
        setText(R.id.photo_exposure, photo.exposure, R.string.label_exposure_format);
        setText(R.id.photo_aperture, photo.aperture, R.string.label_aperture_format);
        setText(R.id.photo_focal, photo.focal, R.string.label_focal_format);
        setText(R.id.photo_iso, photo.iso);
    }

    /**
     * Displays the specified photo's location on the map.
     */
    private void bindLocation(Photo photo, Drawable marker) {
        final GeoPoint location = new GeoPoint(photo.latitude, photo.longitude);
        mLocationView.getController().setCenter(location);

        final PhotoOverlay overlay = new PhotoOverlay(marker);
        overlay.addOverlay(new OverlayItem(location, "", ""));

        final List<Overlay> mapOverlays = mLocationView.getOverlays();
        mapOverlays.clear();
        mapOverlays.add(overlay);
    }

    /**
     * Creates the map marker for the specified drawable.
     */
    private Drawable makeMarker(Drawable drawable) {
        if (!(drawable instanceof BitmapDrawable)) return drawable;
        
        final Bitmap source = ((BitmapDrawable) drawable).getBitmap();

        final int width = source.getWidth() / 2;
        final int height = source.getHeight() / 2;
        final float arrowSize = width / 5.0f;

        final Bitmap bitmap = Bitmap.createBitmap(width + 2, (int) (height + 2 + arrowSize),
                Bitmap.Config.ARGB_8888);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.0f);
        paint.setColor(Color.WHITE);

        Path path = new Path();
        path.moveTo((width - arrowSize) * 0.5f, height);
        path.lineTo((width + arrowSize) * 0.5f, height);
        path.lineTo(width * 0.5f, height + arrowSize);
        path.close();

        Canvas c = new Canvas(bitmap);
        c.translate(1.0f, 1.0f);
        // Draw the image
        c.save();
        c.scale(0.5f, 0.5f);
        c.drawBitmap(source, 0.0f, 0.0f, null);
        c.restore();
        // Draw the frame
        c.drawRect(0.0f, 0.0f, width, height, paint);
        // Draw the arrow
        paint.setStyle(Paint.Style.FILL);
        c.drawPath(path, paint);

        return new BitmapDrawable(getResources(), bitmap);
    }

    private void setText(int id, String value) {
        ((TextView) mPhotoInfo.findViewById(id)).setText(value);
    }

    private void setText(int id, String value, int format) {
        ((TextView) mPhotoInfo.findViewById(id)).setText(getResources().getString(format, value));
    }

    /**
     * Adds the specified photo in the stack with animations.
     */
    private void addFullPhoto(Photo photo) {
        final View stackTop = mStack.getChildAt(mStack.getChildCount() - 1);
        // Don't switch if the selected photo is the one currently displayed
        final PhotoTag tag = (PhotoTag) stackTop.getTag();
        if (tag.photo == photo) {
            return;
        }

        new PhotoSwap(photo, stackTop, tag).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    /**
     * Asynchronous task that loads a large bitmap on a background thread.
     * After the bitmap is loaded, it is added to the photos stack.
     */
    private class PhotoSwap extends AsyncTask<Void, Void, Bitmap> {
        private final Photo mPhoto;
        private final View mStackTop;
        private final PhotoTag mTag;

        private Drawable mMarker;

        PhotoSwap(Photo photo, View stackTop, PhotoTag tag) {
            mPhoto = photo;
            mStackTop = stackTop;
            mTag = tag;
        }
        
        @Override
        protected Bitmap doInBackground(Void... params) {
            Bitmap bitmap = mCache.get(mPhoto.photoResource);
            if (bitmap == null) {
                bitmap = BitmapFactory.decodeResource(getResources(), mPhoto.photoResource);
                mCache.put(mPhoto.photoResource, bitmap);
            }
            mMarker = makeMarker(mPhoto.thumbnail);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            final PhotoView newStackTop = addPhotoInStack(mPhoto, bitmap);
            animateIn(newStackTop);

            // Don't run an animation if the view is already animating
            if (mTag.animator == null) {
                animateOut(mStackTop);
            }
            
            bindLocation(mPhoto, mMarker);
        }
    } 

    /**
     * Animates the specified view to appear in the stack.
     */
    private void animateIn(final PhotoView view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX",
                mPanel.getWidth() - view.getContentWidth(), 0.0f);
        animator.setDuration(600);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Remove the previous view in the stack now that we
                // completely cover it
                int index = mStack.indexOfChild(view) - 1;
                if (index >= 0) {
                    View previous = mStack.getChildAt(index);
                    Animator animator = ((PhotoTag) previous.getTag()).animator;
                    if (animator != null && animator.isRunning()) {
                        animator.cancel();
                    }
                    mStack.removeView(previous);
                }
                ((PhotoTag) view.getTag()).animator = null;
            }
        });
        animator.start();

        // Remember this animator to cancel it later
        ((PhotoTag) view.getTag()).animator = animator;
    }

    /**
     * Animates the specified view to disappear from the stack.
     */
    private void animateOut(final View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX",
                mPanel.getWidth() * 2.0f);
        animator.setDuration(1500);
        animator.start();

        // Remember this animator to cancel it later
        ((PhotoTag) view.getTag()).animator = animator;
    }

    /**
     * Show or hide the album panel.
     */
    private void togglePanel() {
        mPanelVisible = !mPanelVisible;

        mPanel.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        if (mPanelAnimator != null && mPanelAnimator.isRunning()) {
            mPanelAnimator.reverse();
            return;
        }

        if (mPanelVisible) {
            // Display the panel, move it back to its original location
            mPanelAnimator = ObjectAnimator.ofFloat(mPanel, "x", 0.0f);
        } else {
            // Hide the panel, move it out of the screen
            mPanelAnimator = ObjectAnimator.ofFloat(mPanel, "x", -mPanel.getWidth());
        }

        mPanelAnimator.setDuration(250);
        mPanelAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mPanel.setLayerType(View.LAYER_TYPE_NONE, null);
            }
        });
        mPanelAnimator.start();
    }

    /**
     * Creates layout parameters used by children of the photos stack.
     */
    private static FrameLayout.LayoutParams createStackLayoutParams() {
        return new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
    }

    /**
     * Creates the album's list adapter and binds it to the list view.
     * This method also setups the various list controllers (list item click
     * listener for instance.)
     */
    private void setupAlbumList() {
        ListView list = (ListView) findViewById(R.id.album_list);

        list.addHeaderView(createSpacer(), null, false);
        list.addFooterView(createSpacer(), null, false);

        list.setAdapter(new AlbumAdapter(this, mPhotos));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                addFullPhoto((Photo) parent.getItemAtPosition(position));
            }
        });
    }

    private View createSpacer() {
        final View header = new View(this);
        header.setLayoutParams(new ListView.LayoutParams(
                ListView.LayoutParams.MATCH_PARENT,
                getResources().getDimensionPixelSize(R.dimen.list_padding)));
        return header;
    }

    private static class PhotoTag {
        final Photo photo;
        Animator animator;

        PhotoTag(Photo photo) {
            this.photo = photo;
        }
    }
}
