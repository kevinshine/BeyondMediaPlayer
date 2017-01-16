/*
 * Copyright (C) 2014 Kevin Shen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kevinshine.beyondmediaplayer.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class SQLiteHelper extends SQLiteOpenHelper {
    public static final String TAG = SQLiteHelper.class.getSimpleName();

    public static final String CONTENT_AUTHORITY = "com.kevinshen.beyondmediaplayer";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final String DATABASE_NAME = "beyondmp.db";
    private static final int DATABASE_VERSION = 1;

    private static SQLiteHelper sInstance = null;

    public static SQLiteHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SQLiteHelper(context);
        }

        return sInstance;
    }

    private SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String playlistItemSql = "CREATE TABLE `" + PlaylistContract.PlaylistItem.PLAYLIST_ITEM_TABLE + "` (  `"
                + PlaylistContract.PlaylistItem._ID + "` INTEGER PRIMARY KEY AUTOINCREMENT,  `"
                + PlaylistContract.PlaylistItem.ITEM_TITLE + "` TEXT , `"
                + PlaylistContract.PlaylistItem.ITEM_THUMB + "` TEXT , `"
                + PlaylistContract.PlaylistItem.ITEM_METADATA + "` TEXT , `"
                + PlaylistContract.PlaylistItem.ITEM_VERIFICATION_CODE + "` TEXT , `"
                + PlaylistContract.PlaylistItem.ITEM_DATE + "` INTEGER , `"
                + PlaylistContract.PlaylistItem.ITEM_URI + "` TEXT );";

        String drawerItemSql = "CREATE TABLE `" + DrawerItemContract.DrawerItemTable.DRAWER_ITEM_TABLE + "` (  `"
                + DrawerItemContract.DrawerItemTable._ID + "` INTEGER PRIMARY KEY AUTOINCREMENT,  `"
                + DrawerItemContract.DrawerItemTable.ITEM_TITLE + "` TEXT , `"
                + DrawerItemContract.DrawerItemTable.ITEM_THUMB + "` TEXT , `"
                + DrawerItemContract.DrawerItemTable.ITEM_SUBTITLE + "` TEXT , `"
                + DrawerItemContract.DrawerItemTable.ITEM_DATE + "` INTEGER , `"
                + DrawerItemContract.DrawerItemTable.ITEM_TYPE + "` TEXT , `"
                + DrawerItemContract.DrawerItemTable.ITEM_URI + "` TEXT );";

        db.execSQL(playlistItemSql);
        db.execSQL(drawerItemSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

        if (oldVersion < DATABASE_VERSION) {
            db.execSQL("DROP TABLE IF EXISTS `" + PlaylistContract.PlaylistItem.PLAYLIST_ITEM_TABLE + "`;");
            onCreate(db);
        }
    }
}
