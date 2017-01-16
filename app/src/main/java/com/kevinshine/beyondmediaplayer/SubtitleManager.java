package com.kevinshine.beyondmediaplayer;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.widget.MediaController;
import android.widget.TextView;

import com.kevinshine.beyondmediaplayer.utils.subtitle.Caption;
import com.kevinshine.beyondmediaplayer.utils.subtitle.FormatASS;
import com.kevinshine.beyondmediaplayer.utils.subtitle.FormatSRT;
import com.kevinshine.beyondmediaplayer.utils.subtitle.TimedTextObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by gary on 7/12/16.
 */
public class SubtitleManager {
    private static final String TAG = SubtitleManager.class.getSimpleName();

    private TimedTextObject mSubOjb;
    private ScheduledExecutorService mScheduler;
    private SubtitleHandler mHandler;
    private MediaController.MediaPlayerControl mController;
    private Integer mCurrentKey;
    private int mLastKey;

    public static final String MSG_SUBTITLE_CONTENT = "MSG_SUBTITLE_CONTENT";
    public static final String MSG_SUBTITLE_POSITION = "MSG_SUBTITLE_POSITION";

    public SubtitleManager(TextView targetView,MediaController.MediaPlayerControl controller){
        mHandler = new SubtitleHandler(targetView);
        mController = controller;
    }

    public void loadSubtitleFile(String filePath){
        new SubtitleProcessingTask().execute(filePath);
    }

    private void start(){
        if (mSubOjb == null || mSubOjb.captions.size() == 0){
            Log.e(TAG, "analyze subtitle file failed");
            return;
        }

        // get all subtitle keys
        final NavigableSet<Integer> keySet = mSubOjb.captions.navigableKeySet();
        //init mCurrentKey with the first item
        mCurrentKey = keySet.first();

        if (mScheduler != null){
            mScheduler.shutdownNow();
        }

        mScheduler = Executors.newSingleThreadScheduledExecutor();
        // update every 200 ms
        mScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (mController != null && mController.isPlaying() && mSubOjb != null) {
                    int currentPos = mController.getCurrentPosition();
                    Caption curCaption = getCurrentCaption(currentPos,keySet);

                    // when the curCaption is null,we need clean current subtitle.
                    if (curCaption != null){
                        if (mLastKey != mCurrentKey){
                            mLastKey = mCurrentKey;
                            Log.d(TAG, "update subtitle and key:" + mLastKey);
                            Spanned content = Html.fromHtml(curCaption.content);
                            Message msg = Message.obtain(mHandler);
                            msg.getData().putCharSequence(MSG_SUBTITLE_CONTENT,content);
                            msg.sendToTarget();
                        }
                    }else {
                        Message msg = Message.obtain(mHandler);
                        msg.getData().putCharSequence(MSG_SUBTITLE_CONTENT,null);
                        msg.sendToTarget();
                    }
                }
            }
        },0,200, TimeUnit.MILLISECONDS);
    }

    public void stop(){
        if (mScheduler != null){
            mScheduler.shutdownNow();
        }
    }

    public void release(){

    }

    private Caption getCurrentCaption(int position, NavigableSet<Integer> keySet){
        // check current caption
        Caption curCaption = mSubOjb.captions.get(mCurrentKey);
        if (position >= curCaption.start.getMseconds()
                && position <= curCaption.end.getMseconds()){
            Log.d(TAG, "return current subtitle");
            return curCaption;
        }

        Integer nextKey = keySet.higher(mCurrentKey);
        // if nextkey is null,current item is the last
        if (nextKey == null)
            return null;

        // check next caption
        Caption nextCaption = mSubOjb.captions.get(nextKey);
        if (position >= nextCaption.start.getMseconds()
                && position <= nextCaption.end.getMseconds()){
            mCurrentKey = nextKey;
            Log.d(TAG, "return next subtitle");
            return nextCaption;
        }

        // the position is between current and next caption,so return null
        if (position > curCaption.end.getMseconds() && position < nextCaption.start.getMseconds()){
            Log.d(TAG, "between current and next");
            return null;
        }

        // loop and find the right item
        for (Iterator<Integer> iterator = keySet.iterator(); iterator.hasNext();) {
            Log.d(TAG, "loop and find subtitle");
            Integer tempKey = iterator.next();
            if (position < tempKey)
                continue;

            Caption tempCaption = mSubOjb.captions.get(tempKey);
            if (position >= tempCaption.start.getMseconds()
                    && position <= tempCaption.end.getMseconds()){
                mCurrentKey = tempKey;
                return tempCaption;
            }
        }

        return null;
    }

    private class SubtitleProcessingTask extends AsyncTask<String, Void, TimedTextObject> {
        @Override
        protected TimedTextObject doInBackground(String... params) {
            if (params == null || params.length == 0)
                return null;

            TimedTextObject result = null;

            try {
                String path = params[0];
                File file = new File(path);
                InputStream stream = new FileInputStream(file);

                String fileExt = getExtensionName(file.getName());
                if (fileExt != null){
                    if (fileExt.equalsIgnoreCase("SRT")){
                        FormatSRT formatSRT = new FormatSRT();
                        result = formatSRT.parseFile(file.getName(), stream);
                    }else if (fileExt.equalsIgnoreCase("ASS")){
                        FormatASS formatASS = new FormatASS();
                        result = formatASS.parseFile(file.getName(), stream);
                    }else {
                        Log.e(TAG, "unsupported file type :" + file.getName());
                        result = null;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "error in downloadinf subs");
            }

            return result;
        }

        @Override
        protected void onPostExecute(TimedTextObject result) {
            Log.i(TAG, "load subtitle file finished");
            if (result != null){
                mSubOjb = result;
                start();
            }
        }
    }

    private static String getExtensionName(String filename) {
        String ext = null;
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot >-1) && (dot < (filename.length() - 1))) {
                ext = filename.substring(dot + 1);
            }
        }
        return ext;
    }

    private static class SubtitleHandler extends Handler{
        private WeakReference<TextView> textViewWeakReference;

        SubtitleHandler(TextView targetTextView){
            textViewWeakReference = new WeakReference<TextView>(targetTextView);
        }

        @Override
        public void handleMessage(Message msg) {

            TextView textView = textViewWeakReference.get();
            if (textView != null){
                Spanned content = (Spanned)msg.getData().getCharSequence(MSG_SUBTITLE_CONTENT);
                textView.setText(content);
            }
        }
    }
}
