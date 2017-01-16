package com.kevinshine.beyondmediaplayer;

import android.util.Log;

import com.kevinshine.beyondmediaplayer.model.PlaylistModel;
import com.kevinshine.beyondmediaplayer.model.bean.PlaylistItemBean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.MediaInfo;

/**
 * Created by gary on 15-12-11.
 */
public class PlaylistManager {
    private static final String TAG = PlaylistManager.class.getSimpleName();
    private static PlaylistManager INSTANCE = null;
    public static int CURRENT_INDEX = 0;

    private PlaylistManager(){
        //Load jni library
        IjkMediaPlayer.loadLibrariesOnce(null);
    }

    public static PlaylistManager getInstance(){
        if (INSTANCE == null){
            INSTANCE = new PlaylistManager();
        }

        return INSTANCE;
    }

    public void initPlaylist(ArrayList<PlaylistItemBean> playlistItems){
        if (playlistItems != null){
            for (PlaylistItemBean itemBean : playlistItems){
                MediaInfo info = new MediaInfo();
                info.mediaID = itemBean.getId();
                info.mediaName = itemBean.getTitle();
                info.mediaPath = itemBean.getUri();
//                addPlaylistItem(info);
            }
        }
    }

    public List<MediaInfo> getMediaList(){
        List<MediaInfo> mediaList = null;

//        MediaInfo[] mediaInfos = getPlaylist();
//        if (mediaInfos == null){
//            mediaList = new ArrayList<>();
//        }else {
//            mediaList = Arrays.asList(mediaInfos);
//        }
//
//        Log.d(TAG, "Playlist:" + mediaList);
        return mediaList;
    }

    public MediaInfo getNextItem(){
        MediaInfo info = null;
//        MediaInfo[] mediaInfos = getPlaylist();
//        if (mediaInfos != null && mediaInfos.length > 0){
//            if (CURRENT_INDEX < mediaInfos.length-1){
//                info = mediaInfos[CURRENT_INDEX+1];
//            }else {
//                // get the first item
//                info = mediaInfos[0];
//            }
//        }
        return info;
    }

    public MediaInfo getPrevItem(){
        MediaInfo info = null;
//        MediaInfo[] mediaInfos = getPlaylist();
//        if (mediaInfos != null && mediaInfos.length > 0){
//            if (CURRENT_INDEX >= 1){
//                info = mediaInfos[CURRENT_INDEX - 1];
//            }else {
//                // get the last item
//                info = mediaInfos[mediaInfos.length - 1];
//            }
//        }
        return info;
    }

    public void addFileToPlaylist(String path,PlaylistModel model){
        if (path == null){
            Log.e(TAG,"File " + path + " no found.");
            return;
        }

        File selectedFile = new File(path);

        PlaylistItemBean itemBean = new PlaylistItemBean();
        itemBean.setUri(path);
        itemBean.setTitle(selectedFile.getName());
        itemBean.setDate(System.currentTimeMillis());
        long id = model.addPlaylistItemToDb(itemBean);

        MediaInfo info = new MediaInfo();
        info.mediaID = id;
        info.mediaName = selectedFile.getName();
        info.mediaPath = path;
//        addPlaylistItem(info);
    }

}
