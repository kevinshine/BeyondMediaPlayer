package com.kevinshine.beyondmediaplayer.widget.floatingview;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.kevinshine.beyondmediaplayer.R;
import com.kevinshine.beyondmediaplayer.widget.player.VideoView;

/**
 * Created by gary on 6/12/16.
 */
public final class FloatingViewManager implements View.OnClickListener, View.OnTouchListener {

    private WindowManager mWindowManager;
    private Context mContext;
    private VideoView mVideoView;
    private ViewDismissHandler mViewDismissHandler;

    public FloatingViewManager(Context application) {
        mContext = application;
        mWindowManager = (WindowManager) application.getSystemService(Context.WINDOW_SERVICE);
    }

    public void setViewDismissHandler(ViewDismissHandler viewDismissHandler) {
        mViewDismissHandler = viewDismissHandler;
    }

    public void show(String mediaUri) {
        mVideoView = (VideoView) View.inflate(mContext, R.layout.view_floating_player, null);

        // event listeners
        mVideoView.setOnTouchListener(this);
//        mVideoView.setKeyEventHandler(this);

        int w = WindowManager.LayoutParams.MATCH_PARENT;
        int h = WindowManager.LayoutParams.MATCH_PARENT;
//        int w = 640;
//        int h = 360;

        int flags = 0;
        int type = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            type = WindowManager.LayoutParams.TYPE_TOAST;
        } else {
            type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(w, h, type, flags, PixelFormat.TRANSLUCENT);
        layoutParams.gravity = Gravity.START | Gravity.BOTTOM;

        mWindowManager.addView(mVideoView, layoutParams);

        mVideoView.setVideoPath(mediaUri);
        mVideoView.start();
    }

    @Override
    public void onClick(View v) {
//        ViewGroup.LayoutParams layoutParams = mWholeView.getLayoutParams();
//        if (layoutParams.width == ViewGroup.LayoutParams.MATCH_PARENT
//                && layoutParams.height == ViewGroup.LayoutParams.MATCH_PARENT){
//            layoutParams.width = 640;
//            layoutParams.height = 360;
//        }else {
//            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
//            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
//        }
//
//        mWindowManager.updateViewLayout(mWholeView, layoutParams);

    }

    private void removePoppedViewAndClear() {
        // remove view
        if (mWindowManager != null) {
            mWindowManager.removeView(mVideoView);
        }

        if (mViewDismissHandler != null) {
            mViewDismissHandler.onViewDismiss();
        }

        if (mVideoView != null){
            mVideoView.setOnTouchListener(null);
//            mVideoView.setKeyEventHandler(null);
            mVideoView.stopPlayback();
            mVideoView.release(true);
        }
    }

    /**
     * touch the outside of the content view, remove the popped view
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        Rect rect = new Rect();
        mVideoView.getGlobalVisibleRect(rect);
        if (!rect.contains(x, y)) {
            removePoppedViewAndClear();
        }
        return false;
    }

    public interface ViewDismissHandler {
        void onViewDismiss();
    }
}