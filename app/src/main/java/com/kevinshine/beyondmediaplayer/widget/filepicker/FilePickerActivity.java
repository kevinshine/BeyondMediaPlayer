package com.kevinshine.beyondmediaplayer.widget.filepicker;

import android.annotation.SuppressLint;
import android.os.Environment;

import java.io.File;

@SuppressLint("Registered")
public class FilePickerActivity extends AbstractFilePickerActivity<File> {

    public FilePickerActivity() {
        super();
    }

    @Override
    protected AbstractFilePickerFragment<File> getFragment(
            final String startPath, final int mode, final boolean allowMultiple,
            final boolean allowCreateDir) {
        AbstractFilePickerFragment<File> fragment = new FilePickerFragment();
        // startPath is allowed to be null. In that case, default folder should be SD-card and not "/"
        fragment.setArgs(startPath != null ? startPath : Environment.getExternalStorageDirectory().getPath(),
                mode, allowMultiple, allowCreateDir);
        return fragment;
    }
}
