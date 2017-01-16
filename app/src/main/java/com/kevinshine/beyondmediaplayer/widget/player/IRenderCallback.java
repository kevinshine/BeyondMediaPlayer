package com.kevinshine.beyondmediaplayer.widget.player;

import android.support.annotation.NonNull;

/**
 * Created by gary on 16-1-14.
 */
public interface IRenderCallback {
    void onSurfaceCreated(@NonNull TextureRenderView renderView, int width, int height);

    void onSurfaceChanged(@NonNull TextureRenderView renderView, int format, int width, int height);

    void onSurfaceDestroyed(@NonNull TextureRenderView renderView);
}
