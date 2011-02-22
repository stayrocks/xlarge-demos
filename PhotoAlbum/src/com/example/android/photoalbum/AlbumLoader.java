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

import java.util.ArrayList;
import java.util.List;

/**
 * Loads the photos of an album. This implementation is very simple
 * and simply returns a static list of photos.
 */
class AlbumLoader {
    private final Context mContext;

    AlbumLoader(Context context) {
        mContext = context;
    }

    List<Photo> loadPhotos() {
        ArrayList<Photo> photos = new ArrayList<Photo>();
        addPhoto(photos, "Antelope Lights", "Canon 5D Mk II", "1.3", "10", "32", "320",
                R.drawable.photo_1, R.drawable.photo_1_small, 36879466, -111389393);
        addPhoto(photos, "The Photographer", "Canon 5D Mk II", "1/60", "3.5", "70", "800",
                R.drawable.photo_2, R.drawable.photo_2_small, 36878891, -111510672);
        addPhoto(photos, "Green Grass", "Canon 5D Mk II", "1/100", "3.2", "100", "1600",
                R.drawable.photo_3, R.drawable.photo_3_small, 37785372, -122402876);
        addPhoto(photos, "Electric Storm", "Canon 5D Mk II", "1/125", "2.8", "65", "3200",
                R.drawable.photo_4, R.drawable.photo_4_small, 35660992, 139700131);
        addPhoto(photos, "Electric Storm", "Canon 5D Mk II", "1/40", "2.8", "45", "2000",
                R.drawable.photo_5, R.drawable.photo_5_small, 35011228, 135765094);
        addPhoto(photos, "Fog Valley", "Canon 5D Mk II", "1/250", "8", "17", "200",
                R.drawable.photo_6, R.drawable.photo_6_small, 3663206, -118821945);
        addPhoto(photos, "Antelope Hallway", "Canon 5D Mk II", "2", "11", "22", "400",
                R.drawable.photo_7, R.drawable.photo_7_small, 36862609, -111374437);
        addPhoto(photos, "Green Highway", "Canon 5D Mk II", "1/800", "4", "70", "1250",
                R.drawable.photo_8, R.drawable.photo_8_small, 19809906, -155094637);
        addPhoto(photos, "Windmill Sunrise", "Canon 5D Mk II", "1/200", "8", "200", "1000",
                R.drawable.photo_9, R.drawable.photo_9_small, 37719218, -121657233);
        addPhoto(photos, "Sunset Hills", "Canon 5D Mk II", "1/100", "4", "98", "2000",
                R.drawable.photo_10, R.drawable.photo_10_small, 37322683, -122210696);
        return photos;
    }

    private void addPhoto(ArrayList<Photo> photos, String name, String camera, String exposure,
            String aperture, String focal, String iso, int resource, int resourceSmall,
            int latitude, int longitude) {
        Photo photo = new Photo();
        photo.name = name;
        photo.camera = camera;
        photo.exposure = exposure;
        photo.aperture = aperture;
        photo.focal = focal;
        photo.iso = iso;
        photo.photoResource = resource;
        photo.thumbnail = mContext.getResources().getDrawable(resourceSmall);
        photo.latitude = latitude;
        photo.longitude = longitude;
        photos.add(photo);
    }
}
