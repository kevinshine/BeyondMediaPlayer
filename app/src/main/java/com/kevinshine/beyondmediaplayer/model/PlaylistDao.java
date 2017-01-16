package com.kevinshine.beyondmediaplayer.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kevinshine.beyondmediaplayer.database.PlaylistContract.PlaylistItem;
import com.kevinshine.beyondmediaplayer.database.SQLiteHelper;
import com.kevinshine.beyondmediaplayer.model.bean.PlaylistItemBean;

import java.util.ArrayList;

/**
 * Created by gary on 16-2-19.
 */
public class PlaylistDao {
    private SQLiteHelper mHelper;

    public PlaylistDao(Context context) {
        mHelper = SQLiteHelper.getInstance(context);
    }

    public ArrayList<PlaylistItemBean> getPlaylistItems() {
        ArrayList<PlaylistItemBean> result = new ArrayList<>();
        SQLiteDatabase db = mHelper.getReadableDatabase();

        Cursor cursor = null;
        try {
            cursor = db.query(PlaylistItem.PLAYLIST_ITEM_TABLE, null, null, null, null, null, String.format("%s desc",PlaylistItem.ITEM_DATE));
            if (cursor != null) {
                int index_id = cursor.getColumnIndex(PlaylistItem._ID);
                int index_title = cursor.getColumnIndex(PlaylistItem.ITEM_TITLE);
                int index_uri = cursor.getColumnIndex(PlaylistItem.ITEM_URI);
                while (cursor.moveToNext()) {
                    PlaylistItemBean bean = new PlaylistItemBean();
                    bean.setId(cursor.getInt(index_id));
                    bean.setTitle(cursor.getString(index_title));
                    bean.setUri(cursor.getString(index_uri));

                    result.add(bean);
                }
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }

        return result;
    }

    public long addPlaylistItem(PlaylistItemBean bean) {
        long id = -1;
        if (bean == null) return id;

        SQLiteDatabase db = mHelper.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(PlaylistItem.ITEM_TITLE, bean.getTitle());
        values.put(PlaylistItem.ITEM_URI, bean.getUri());
        values.put(PlaylistItem.ITEM_DATE, bean.getDate());
        id = db.insert(PlaylistItem.PLAYLIST_ITEM_TABLE, null, values);
        return id;
    }

    public void deletePlaylistItemById(int id){
        if (id < 0) return;

        SQLiteDatabase db = mHelper.getReadableDatabase();
        db.delete(PlaylistItem.PLAYLIST_ITEM_TABLE,"id = ?",new String[]{String.valueOf(id)});
    }

    public void clearPlaylist(){
        SQLiteDatabase db = mHelper.getReadableDatabase();
        db.execSQL("DELETE FROM " + PlaylistItem.PLAYLIST_ITEM_TABLE);
    }

}
