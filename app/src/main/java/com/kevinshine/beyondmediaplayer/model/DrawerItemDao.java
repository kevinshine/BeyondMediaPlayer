package com.kevinshine.beyondmediaplayer.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kevinshine.beyondmediaplayer.database.DrawerItemContract.DrawerItemTable;
import com.kevinshine.beyondmediaplayer.database.SQLiteHelper;
import com.kevinshine.beyondmediaplayer.model.bean.DrawerItemBean;

import java.util.ArrayList;

/**
 * Created by gary on 16-2-17.
 */
public class DrawerItemDao {
    private SQLiteHelper mHelper;

    public DrawerItemDao(Context context) {
        mHelper = SQLiteHelper.getInstance(context);
    }

    public ArrayList<DrawerItemBean> getItems(String itemType) {
        SQLiteDatabase db = mHelper.getReadableDatabase();
        ArrayList<DrawerItemBean> list = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor = db.query(DrawerItemTable.DRAWER_ITEM_TABLE, null, DrawerItemTable.ITEM_TYPE + "=?", new String[]{itemType}, null, null, null);
            if (cursor != null) {
                int index_id = cursor.getColumnIndex(DrawerItemTable._ID);
                int index_title = cursor.getColumnIndex(DrawerItemTable.ITEM_TITLE);
                int index_uri = cursor.getColumnIndex(DrawerItemTable.ITEM_URI);
                while (cursor.moveToNext()) {
                    DrawerItemBean bean = new DrawerItemBean();
                    bean.setId(cursor.getInt(index_id));
                    bean.setTitle(cursor.getString(index_title));
                    bean.setUri(cursor.getString(index_uri));

                    list.add(bean);
                }
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }

        return list;
    }

    public void addDrawerItem(DrawerItemBean bean) {
        if (bean == null) return;

        SQLiteDatabase db = mHelper.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(DrawerItemTable.ITEM_TITLE, bean.getTitle());
        values.put(DrawerItemTable.ITEM_URI, bean.getUri());
        values.put(DrawerItemTable.ITEM_TYPE, bean.getType());
        db.insert(DrawerItemTable.DRAWER_ITEM_TABLE, null, values);
    }
}
