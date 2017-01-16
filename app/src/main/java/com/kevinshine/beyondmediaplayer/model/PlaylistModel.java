package com.kevinshine.beyondmediaplayer.model;

import android.content.Context;

import com.kevinshine.beyondmediaplayer.model.bean.PlaylistItemBean;

import java.util.ArrayList;

/**
 * Created by gary on 16-2-19.
 */
public class PlaylistModel {
    private PlaylistDao mPlaylistDao;

    public PlaylistModel(Context context) {
        mPlaylistDao = new PlaylistDao(context);

    }

    public ArrayList<PlaylistItemBean> getPlaylistItems() {
        return mPlaylistDao.getPlaylistItems();
    }

    public long addPlaylistItemToDb(PlaylistItemBean bean) {
        return mPlaylistDao.addPlaylistItem(bean);
    }

    public void clearPlaylist(){
        mPlaylistDao.clearPlaylist();
    }

}
