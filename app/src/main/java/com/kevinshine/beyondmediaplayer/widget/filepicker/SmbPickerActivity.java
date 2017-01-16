package com.kevinshine.beyondmediaplayer.widget.filepicker;

import android.os.Environment;

import jcifs.smb.SmbFile;

/**
 * Created by gary on 16-1-7.
 */
public class SmbPickerActivity extends AbstractFilePickerActivity<SmbFile> {
    @Override
    protected AbstractFilePickerFragment<SmbFile> getFragment(String startPath, int mode, boolean allowMultiple, boolean allowCreateDir) {
        SmbPickerFragment fragment = SmbPickerFragment.newInstance(startPath);
        fragment.setArgs(startPath != null ? startPath : Environment.getExternalStorageDirectory().getPath(),
                mode, allowMultiple, allowCreateDir);
        return fragment;
    }
}
