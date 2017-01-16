package com.kevinshine.beyondmediaplayer.database;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by gary on 16-2-17.
 */
public class DrawerItemContract {
    public static final String SDCARD = "sdcard";
    public static final String DLNA = "dlna";
    public static final String SMB = "smb";
    public static final String COLLECTION = "collection";
    public static final String RECENTLY = "recently";

    interface DrawerItemColumns {
        /**
         * The item title for this table.
         */
        String ITEM_TITLE = "item_title";

        /**
         * The item uri for this table.
         */
        String ITEM_URI = "item_uri";

        /**
         * The item thumb for this table.
         */
        String ITEM_THUMB = "item_thumb";

        /**
         * The item metadata for this table.
         */
        String ITEM_SUBTITLE = "item_subtitle";

        /**
         * The item join date for this table.
         */
        String ITEM_DATE = "item_date";

        /**
         * The item protocol type
         */
        String ITEM_TYPE = "item_type";
    }

    public static class DrawerItemTable implements DrawerItemColumns, BaseColumns {
        public static String DRAWER_ITEM_TABLE = "drawer_item";

        public static final Uri CONTENT_URI =
                SQLiteHelper.BASE_CONTENT_URI.buildUpon().appendPath(DRAWER_ITEM_TABLE).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.beyondupnp.drawer_item";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.beyondupnp.drawer_item";
    }
}
