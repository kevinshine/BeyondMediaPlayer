package com.kevinshine.beyondmediaplayer.widget.filepicker;

import android.annotation.SuppressLint;

import com.kevinshine.beyondmediaplayer.upnp.UpnpFile;

@SuppressLint("Registered")
public class DlnaPickerActivity extends AbstractFilePickerActivity<UpnpFile> {

    public DlnaPickerActivity() {
        super();
    }

    @Override
    protected AbstractFilePickerFragment<UpnpFile> getFragment(String startPath, int mode, boolean allowMultiple, boolean allowCreateDir) {
        AbstractFilePickerFragment<UpnpFile> fragment = new DlnaPickerFragment();
        fragment.setArgs(startPath, mode, allowMultiple, allowCreateDir);
        return fragment;
    }
}
