package com.kevinshine.beyondmediaplayer.widget.filepicker;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.util.SortedList;
import android.support.v7.widget.util.SortedListAdapterCallback;

import java.net.MalformedURLException;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * Created by gary on 16-1-7.
 */
public class SmbPickerFragment extends AbstractFilePickerFragment<SmbFile> {
    protected SmbFile mRoot;
    protected boolean showHiddenItems = false;
    public static String ROOT_PATH = "root_path";

    public static SmbPickerFragment newInstance(String root){
        SmbPickerFragment fragment = new SmbPickerFragment();

        //Set arguments
        Bundle bundle = new Bundle();
        bundle.putString(ROOT_PATH,root);
        fragment.setArguments(bundle);
        return fragment;
    }

    public SmbPickerFragment(){
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Init root
        String rootPath = getArguments().getString(ROOT_PATH);
        if (rootPath != null){
            try {
                mRoot = new SmbFile(rootPath);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isDir(SmbFile path) {
        boolean result = false;
        try {
            result = path.isDirectory();
        } catch (SmbException e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public String getName(SmbFile path) {
        return path.getName();
    }

    @Override
    public Uri toUri(SmbFile path) {
        return Uri.parse(path.getPath());
    }

    @Override
    public SmbFile getParent(SmbFile from) {
        SmbFile parent = null;
        try {
            parent = new SmbFile(from.getParent());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return parent;
    }

    @Override
    public String getFullPath(SmbFile path) {
        return path.getPath();
    }

    @Override
    public SmbFile getPath(String path) {
        SmbFile file = null;
        try {
            file = new SmbFile(path);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return file;
    }

    @Override
    public SmbFile getRoot() {
        return null;
    }

    @Override
    public Loader<SortedList<SmbFile>> getLoader() {
        return new AsyncTaskLoader<SortedList<SmbFile>>(getActivity()) {

            @Override
            public SortedList<SmbFile> loadInBackground() {
                SmbFile[] listFiles = null;
                try {
                    listFiles = mCurrentPath.listFiles();
                } catch (SmbException e) {
                    e.printStackTrace();
                }
                final int initCap = listFiles == null ? 0 : listFiles.length;

                SortedList<SmbFile> files = new SortedList<>(SmbFile.class, new SortedListAdapterCallback<SmbFile>(getDummyAdapter()) {
                    @Override
                    public int compare(SmbFile lhs, SmbFile rhs) {
                        return compareFiles(lhs, rhs);
                    }

                    @Override
                    public boolean areContentsTheSame(SmbFile file, SmbFile file2) {
                        boolean result = false;
                        try {
                            result = file.getPath().equals(file2.getPath()) && (file.isFile() == file2.isFile());
                        } catch (SmbException e) {
                            e.printStackTrace();
                        }
                        return result;
                    }

                    @Override
                    public boolean areItemsTheSame(SmbFile file, SmbFile file2) {
                        return areContentsTheSame(file, file2);
                    }
                }, initCap);


                files.beginBatchedUpdates();
                if (listFiles != null) {
                    for (SmbFile f : listFiles) {
                        try {
                            if (isItemVisible(f)) {
                                files.add(f);
                            }
                        } catch (SmbException e) {
                            e.printStackTrace();
                        }
                    }
                }
                files.endBatchedUpdates();

                return files;
            }

            /**
             * Handles a request to start the Loader.
             */
            @Override
            protected void onStartLoading() {
                super.onStartLoading();

                // handle if directory does not exist. Fall back to root.
                try {
                    if (mCurrentPath == null || !mCurrentPath.isDirectory()) {
                        mCurrentPath = mRoot;
                    }
                } catch (SmbException e) {
                    mCurrentPath = null;
                    e.printStackTrace();
                }

                forceLoad();
            }

            /**
             * Handles a request to completely reset the Loader.
             */
            @Override
            protected void onReset() {
                super.onReset();
            }
        };
    }

    /**
     * Compare two files to determine their relative sort order. This follows the usual
     * comparison interface. Override to determine your own custom sort order.
     * <p/>
     * Default behaviour is to place directories before files, but sort them alphabetically
     * otherwise.
     *
     * @param lhs File on the "left-hand side"
     * @param rhs File on the "right-hand side"
     * @return -1 if if lhs should be placed before rhs, 0 if they are equal,
     * and 1 if rhs should be placed before lhs
     */
    protected int compareFiles(SmbFile lhs, SmbFile rhs) {
        int result = 0;
        try {
            if (lhs.isDirectory() && !rhs.isDirectory()) {
                result = -1;
            } else if (rhs.isDirectory() && !lhs.isDirectory()) {
                result = 1;
            } else {
                result = lhs.getName().compareToIgnoreCase(rhs.getName());
            }
        } catch (SmbException e) {
            e.printStackTrace();
        }
        return result;
    }

    protected boolean isItemVisible(final SmbFile file) throws SmbException {
        if(!showHiddenItems && file.isHidden()){
            return false;
        }
        return (isDir(file) || (mode == MODE_FILE || mode == MODE_FILE_AND_DIR));
    }
}
