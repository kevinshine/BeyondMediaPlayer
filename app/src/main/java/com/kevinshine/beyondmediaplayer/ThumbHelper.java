package com.kevinshine.beyondmediaplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.provider.MediaStore;

import com.kevinshine.beyondmediaplayer.utils.Util;

/**
 * Created by gary on 16-2-16.
 */
public class ThumbHelper {
    private static final int THUMBNAIL_WIDTH = 60;
    private static final int THUMBNAIL_HEIGHT = 60;

    private int mThumbWidth;
    private int mThumbHeight;

    public ThumbHelper(Context context){
        mThumbWidth = Util.dip2px(context,THUMBNAIL_WIDTH);
        mThumbHeight = Util.dip2px(context,THUMBNAIL_HEIGHT);
    }

    public Bitmap createThumbImage(String videoPath){
        Bitmap bitmap = Util.getVideoThumbnail(videoPath,mThumbWidth,mThumbHeight, MediaStore.Images.Thumbnails.MICRO_KIND);
        return bitmap;
    }
}
