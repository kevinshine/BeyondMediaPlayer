package com.kevinshine.beyondmediaplayer.activitiy;

import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.github.clans.fab.FloatingActionMenu;
import com.kevinshine.beyondmediaplayer.R;
import com.kevinshine.beyondmediaplayer.database.SQLiteHelper;
import com.kevinshine.beyondmediaplayer.model.PlaylistModel;
import com.kevinshine.beyondmediaplayer.model.bean.PlaylistItemBean;
import com.kevinshine.beyondmediaplayer.upnp.BeyondUpnpService;
import com.kevinshine.beyondmediaplayer.upnp.UpnpManager;
import com.kevinshine.beyondmediaplayer.widget.filepicker.AbstractFilePickerActivity;
import com.kevinshine.beyondmediaplayer.widget.filepicker.DlnaPickerActivity;
import com.kevinshine.beyondmediaplayer.widget.filepicker.FilePickerActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE_EXPLORER = 100;
    private static final int REQUEST_CODE_DLNA = 200;

    private PlaylistAdapter mPlaylistAdapter;
    private FloatingActionMenu mFabMenu;
    private PlaylistModel mPlaylistModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPlaylistModel = new PlaylistModel(this);

        mFabMenu = (FloatingActionMenu) findViewById(R.id.fab_menu);
        mFabMenu.setOnMenuButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFabMenu.toggle(true);
            }
        });
        mFabMenu.showMenuButton(true);
        mFabMenu.setClosedOnTouchOutside(true);
        // Add item onclick listener
        findViewById(R.id.fab1).setOnClickListener(this);
        findViewById(R.id.fab2).setOnClickListener(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view_playlist);
        recyclerView.setHasFixedSize(true);
        // use a linear layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mPlaylistAdapter = new PlaylistAdapter(this);
        recyclerView.setAdapter(mPlaylistAdapter);

        findViewById(R.id.btn_clear).setOnClickListener(this);

        // Bind UPnP service
        Intent upnpServiceIntent = new Intent(this, BeyondUpnpService.class);
        bindService(upnpServiceIntent, mUpnpServiceConnection, Context.BIND_AUTO_CREATE);

        refreshPlaylist();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SQLiteHelper.getInstance(this).close();
        unbindService(mUpnpServiceConnection);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_EXPLORER:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        String[] nameArray = data.getStringArrayExtra(AbstractFilePickerActivity.EXTRA_NAME_LIST);
                        String[] uriArray = data.getStringArrayExtra(AbstractFilePickerActivity.EXTRA_URI_LIST);

                        String playUri = null;

                        if (nameArray != null && uriArray != null) {
                            for (int i = 0; i < nameArray.length && i < uriArray.length; i++) {
                                PlaylistItemBean item = new PlaylistItemBean();
                                item.setTitle(nameArray[i]);
                                item.setUri(uriArray[i]);
                                long id = addPlaylistItemToDatabase(item);
                                Log.d(TAG,"insert item to db:" + id);

                                playUri = uriArray[i];
                            }

                            refreshPlaylist();
                        }

                        if (playUri != null){
                            VideoActivity.intentTo(this, playUri, "Video", 0);
                        }
                    }
                }
                break;
            case REQUEST_CODE_DLNA:
                if (data != null) {
                    Uri uri = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        ClipData clipData = data.getClipData();
                        for (int i = clipData.getItemCount() - 1; i >= 0; i--) {
                            uri = clipData.getItemAt(i).getUri();
                        }
                    } else {
                        uri = data.getData();
                    }

                    if (uri != null) {
                        VideoActivity.intentTo(this, uri.toString(), "Video", 0);
                    }
                }
                break;
        }
    }

    private long addPlaylistItemToDatabase(PlaylistItemBean itemBean) {
        return mPlaylistModel.addPlaylistItemToDb(itemBean);
    }

    /**
     * Refresh playlist
     */
    protected void refreshPlaylist() {
        getSupportLoaderManager().restartLoader(100, null, playlistLoaderCallback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_explorer) {
            openStorageExplorer();
            return true;
        } else if (id == R.id.action_dlna) {
            openDlnaExplorer();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v == null)
            return;

        switch (v.getId()) {
            case R.id.fab1:
                openStorageExplorer();
                break;
            case R.id.fab2:
                openDlnaExplorer();
                break;
            case R.id.btn_clear:
                clearPlaylistData();
                break;
        }

        mFabMenu.close(false);
    }

    private void clearPlaylistData(){
        mPlaylistModel.clearPlaylist();
        mPlaylistAdapter.reset();
        mPlaylistAdapter.notifyDataSetChanged();
    }

    private void openStorageExplorer() {
        Intent i = new Intent(this, FilePickerActivity.class);
        // Set these depending on your use case. These are the defaults.
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, true);
        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
        i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
        startActivityForResult(i, REQUEST_CODE_EXPLORER);
    }

    private void openDlnaExplorer() {
        Intent i = new Intent(this, DlnaPickerActivity.class);
        i.putExtra(DlnaPickerActivity.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(i, REQUEST_CODE_DLNA);
    }

    private ServiceConnection mUpnpServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            BeyondUpnpService.LocalBinder binder = (BeyondUpnpService.LocalBinder) service;
            BeyondUpnpService beyondUpnpService = binder.getService();

            UpnpManager upnpManager = UpnpManager.getInstance();
            upnpManager.setUpnpService(beyondUpnpService);

            //Search on service created.
            upnpManager.searchAllDevices();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            UpnpManager upnpManager = UpnpManager.getInstance();
            upnpManager.getUpnpService().shutdownUpnpService();
            upnpManager.setUpnpService(null);
        }
    };

    private LoaderManager.LoaderCallbacks<ArrayList<PlaylistItemBean>> playlistLoaderCallback
            = new LoaderManager.LoaderCallbacks<ArrayList<PlaylistItemBean>>() {
        @Override
        public Loader<ArrayList<PlaylistItemBean>> onCreateLoader(int id, Bundle args) {
            return new AsyncTaskLoader<ArrayList<PlaylistItemBean>>(MainActivity.this){

                @Override
                protected void onStartLoading() {
                    super.onStartLoading();
                    forceLoad();
                }

                @Override
                public ArrayList<PlaylistItemBean> loadInBackground() {
                    ArrayList<PlaylistItemBean> items = mPlaylistModel.getPlaylistItems();
                    return items;
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<ArrayList<PlaylistItemBean>> loader, ArrayList<PlaylistItemBean> data) {
            mPlaylistAdapter.setList(data);
        }

        @Override
        public void onLoaderReset(Loader<ArrayList<PlaylistItemBean>> loader) {
            mPlaylistAdapter.reset();
        }
    };
}
