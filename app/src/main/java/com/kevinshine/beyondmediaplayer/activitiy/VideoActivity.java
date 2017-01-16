package com.kevinshine.beyondmediaplayer.activitiy;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.kevinshine.beyondmediaplayer.R;
import com.kevinshine.beyondmediaplayer.SubtitleManager;
import com.kevinshine.beyondmediaplayer.widget.filepicker.FilePickerActivity;
import com.kevinshine.beyondmediaplayer.widget.player.AndroidMediaController;
import com.kevinshine.beyondmediaplayer.widget.player.MeasureHelper;
import com.kevinshine.beyondmediaplayer.widget.player.VideoView;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class VideoActivity extends AppCompatActivity implements AndroidMediaController.DisplayCallback {
    private static final String TAG = "VideoActivity";

    private static final int REQUEST_CODE_EXPLORER = 100;
    private static final String MEDIA_INDEX = "media_index";
    private String mVideoPath;
    private Uri    mVideoUri;

    private AndroidMediaController mMediaController;
    private VideoView mVideoView;
    private TextView mToastTextView;
    private TextView mSubtitleTextView;
    private SubtitleManager mSubtitleManager;

    private boolean mBackPressed;

    public static Intent newIntent(Context context, String videoPath, String videoTitle,int playlistIndex) {
        Intent intent = new Intent(context, VideoActivity.class);
        intent.putExtra("videoPath", videoPath);
        intent.putExtra("videoTitle", videoTitle);
        intent.putExtra("playlistIndex", playlistIndex);
        return intent;
    }

    public static void intentTo(Context context, String videoPath, String videoTitle,int playlistIndex) {
        context.startActivity(newIntent(context, videoPath, videoTitle, playlistIndex));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        setContentView(R.layout.activity_video);

        // handle arguments
        mVideoPath = getIntent().getStringExtra("videoPath");
        Log.d(TAG,"videoPath:" + mVideoPath);

        Intent intent = getIntent();
        String intentAction = intent.getAction();
        Log.d(TAG,"intentAction:" + intentAction);

        int index = intent.getIntExtra("playlistIndex",0);

        if (!TextUtils.isEmpty(intentAction)) {
            if (intentAction.equals(Intent.ACTION_VIEW)) {
                mVideoPath = intent.getDataString();
            } else if (intentAction.equals(Intent.ACTION_SEND)) {
                mVideoUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    String scheme = mVideoUri.getScheme();
                    if (TextUtils.isEmpty(scheme)) {
                        Log.e(TAG, "Null unknown scheme\n");
                        finish();
                        return;
                    }
                    if (scheme.equals(ContentResolver.SCHEME_ANDROID_RESOURCE)) {
                        mVideoPath = mVideoUri.getPath();
                    } else if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
                        Log.e(TAG, "Can not resolve content below Android-ICS\n");
                        finish();
                        return;
                    } else {
                        Log.e(TAG, "Unknown scheme " + scheme + "\n");
                        finish();
                        return;
                    }
                }
            }
        }

        // init UI
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        mMediaController = new AndroidMediaController(this, true);
        mMediaController.setSupportActionBar(actionBar);
        mMediaController.setDisplayCallback(this);

        mToastTextView = (TextView) findViewById(R.id.toast_text_view);

        // init player
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");

        mVideoView = (VideoView) findViewById(R.id.video_view);
        mVideoView.setMediaController(mMediaController);
        // prefer mVideoPath
        if (mVideoPath != null)
            mVideoView.setVideoPath(mVideoPath);
        else if (mVideoUri != null)
            mVideoView.setVideoURI(mVideoUri);
        else {
            Log.d(TAG, "Null Data Source\n");
            finish();
            return;
        }

        mVideoView.setPlaylistIndex(index);
        // set on complete callback
        mVideoView.setOnCompletionListener(completionListener);
        mVideoView.start();

        mSubtitleTextView = (TextView) findViewById(R.id.textview_subtitle);
        mSubtitleManager = new SubtitleManager(mSubtitleTextView,mVideoView);
    }

    @Override
    public void onBackPressed() {
        mBackPressed = true;
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mBackPressed) {
            mVideoView.stopPlayback();
            mVideoView.release(true);
        }

        IjkMediaPlayer.native_profileEnd();

        mSubtitleManager.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_select_subtitle) {
            Intent i = new Intent(this, FilePickerActivity.class);
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
            i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
            i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
//
            startActivityForResult(i, REQUEST_CODE_EXPLORER);
        } else if (id == R.id.action_show_info) {
            mVideoView.showMediaInfo();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_CODE_EXPLORER:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        Uri uri = data.getData();

                        Log.i(TAG, "select data = " + uri);
                        mSubtitleManager.loadSubtitleFile(uri.getPath());
                    }
                }
                break;
        }
    }

    private IMediaPlayer.OnCompletionListener completionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer mp) {
            Log.d(TAG, "IMediaPlayer Completion");
        }
    };

    @Override
    public void onMediaControllerHide() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
    }

    @Override
    public void onMediaControllerShow() {
    }
}
