package com.kevinshine.beyondmediaplayer.widget.filepicker;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.util.SortedList;
import android.support.v7.widget.util.SortedListAdapterCallback;

import com.kevinshine.beyondmediaplayer.upnp.UpnpFile;
import com.kevinshine.beyondmediaplayer.upnp.UpnpManager;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DlnaPickerFragment extends AbstractFilePickerFragment<UpnpFile> {
    private UpnpFile mRoot;
    private ArrayList<UpnpFile> mListFiles = new ArrayList<>();
    private CountDownLatch mObtainListCountDown;

    public DlnaPickerFragment() {
        mRoot = new UpnpFile();
        mRoot.setParent(null);
    }

    @Override
    public boolean isDir(UpnpFile path) {
        return path.isDirectory() || path.isDevice();
    }

    @Override
    public String getName(UpnpFile path) {
        return path.getName();
    }

    @Override
    public Uri toUri(UpnpFile path) {
        return Uri.parse(path.getUri());
    }

    @Override
    public UpnpFile getParent(UpnpFile from) {
        return from.getParent();
    }

    @Override
    public String getFullPath(UpnpFile path) {
        return path.getFullPath();
    }

    @Override
    public UpnpFile getPath(String path) {
        return null;
    }

    @Override
    public UpnpFile getRoot() {
        return mRoot;
    }

    @Override
    public Loader<SortedList<UpnpFile>> getLoader() {
        return new AsyncTaskLoader<SortedList<UpnpFile>>(getActivity()) {

            /**
             * Handles a request to start the Loader.
             */
            @Override
            protected void onStartLoading() {
                super.onStartLoading();

                // handle if directory does not exist. Fall back to root.
                if (mCurrentPath == null) {
                    mCurrentPath = getRoot();
                }

                forceLoad();
            }

            @Override
            public SortedList<UpnpFile> loadInBackground() {
                // root's parent node is null
                if (mCurrentPath.getParent() == null) {
                    mListFiles = getDevices();
                } else if (isDir(mCurrentPath)) {
                    loadFiles(mCurrentPath);
                }

                return createSortedList(mListFiles);
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

    @Override
    protected void onFilesPicked(ArrayList<UpnpFile> items) {
        if (items != null){
            String[] nameArray = new String[items.size()];
            String[] uriArray = new String[items.size()];

            for (int i = 0; i < items.size(); i++) {
                nameArray[i] = items.get(i).getName();
                uriArray[i] = items.get(i).getUri();
            }
            Intent i = new Intent();
            i.putExtra(AbstractFilePickerActivity.EXTRA_NAME_LIST,nameArray);
            i.putExtra(AbstractFilePickerActivity.EXTRA_URI_LIST,uriArray);
            getActivity().setResult(Activity.RESULT_OK, i);
        }
        getActivity().finish();
    }

    private SortedList<UpnpFile> createSortedList(ArrayList<UpnpFile> listFiles) {
        final int initCap = listFiles == null ? 0 : listFiles.size();

        SortedList<UpnpFile> files = new SortedList<>(UpnpFile.class, new SortedListAdapterCallback<UpnpFile>(getDummyAdapter()) {
            @Override
            public int compare(UpnpFile lhs, UpnpFile rhs) {
                return compareFiles(lhs, rhs);
            }

            @Override
            public boolean areContentsTheSame(UpnpFile file, UpnpFile file2) {
                return file.getUri().equals(file2.getUri());
            }

            @Override
            public boolean areItemsTheSame(UpnpFile file, UpnpFile file2) {
                return areContentsTheSame(file, file2);
            }
        }, initCap);

        files.beginBatchedUpdates();
        if (listFiles != null) {
            for (UpnpFile f : listFiles) {
                files.add(f);
            }
        }
        files.endBatchedUpdates();

        return files;
    }

    private ArrayList<UpnpFile> getDevices() {
        Collection<Device> devices = UpnpManager.getInstance().getDmcDevices();
        ArrayList<UpnpFile> result = new ArrayList<>(devices.size());

        for (Device device : devices) {
            UpnpFile file = new UpnpFile();
            file.setFileType(UpnpFile.TYPE_DEVICE);
            file.setUpnpDevice(device);
            file.setName(device.getDetails().getFriendlyName());
            file.setParent(mRoot);
            // default value is 0
            file.setObjectId("0");
            result.add(file);
        }
        return result;
    }

    /**
     * Load the directories and files from remote devices Asynchronously.
     *
     * @param upnpFile
     */
    private void loadFiles(final UpnpFile upnpFile) {
        final Device device = upnpFile.getUpnpDevice();

        // clear old data
        mListFiles.clear();

        if (device != null) {
            //Get cds to browse children directories.
            Service contentDeviceService = device.findService(UpnpManager.CONTENT_DIRECTORY_SERVICE);
            //Execute Browse action and init list view
            UpnpManager.getInstance().getControlPoint().execute(new Browse(contentDeviceService, upnpFile.getObjectId(), BrowseFlag.DIRECT_CHILDREN, "*", 0,
                    null, new SortCriterion(true, "dc:title")) {
                @Override
                public void received(ActionInvocation actionInvocation, DIDLContent didl) {
                    UpnpFile tempFile;
                    for (Container container : didl.getContainers()) {
                        tempFile = new UpnpFile();
                        tempFile.setFileType(UpnpFile.TYPE_DIR);
                        tempFile.setObjectId(container.getId());
                        tempFile.setParent(upnpFile);
                        tempFile.setUpnpDevice(device);
                        tempFile.setName(container.getTitle());
                        mListFiles.add(tempFile);
                    }

                    for (Item item : didl.getItems()) {
                        tempFile = new UpnpFile();
                        tempFile.setFileType(UpnpFile.TYPE_FILE);
                        tempFile.setObjectId(item.getId());
                        tempFile.setParent(upnpFile);
                        tempFile.setUpnpDevice(device);
                        tempFile.setName(item.getTitle());

                        // set uri
                        Res res = item.getFirstResource();
                        tempFile.setUri(res.getValue());
                        mListFiles.add(tempFile);
                    }

                    if (mObtainListCountDown != null)
                        mObtainListCountDown.countDown();
                }

                @Override
                public void updateStatus(Status status) {
                }

                @Override
                public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                }
            });

            // wait for the result
            mObtainListCountDown = new CountDownLatch(1);
            try {
                // timeout after 3 seconds
                mObtainListCountDown.await(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Compare two files to determine their relative sort order. This follows the usual
     * comparison interface. Override to determine your own custom sort order.
     * <p/>
     * Default behaviour is to place directories before files, but sort them alphabetically
     * otherwise.
     *
     * @param lhs UpnpFile on the "left-hand side"
     * @param rhs UpnpFile on the "right-hand side"
     * @return -1 if if lhs should be placed before rhs, 0 if they are equal,
     * and 1 if rhs should be placed before lhs
     */
    protected int compareFiles(UpnpFile lhs, UpnpFile rhs) {
        if (lhs.isDirectory() && !rhs.isDirectory()) {
            return -1;
        } else if (rhs.isDirectory() && !lhs.isDirectory()) {
            return 1;
        } else {
            return lhs.getName().compareToIgnoreCase(rhs.getName());
        }
    }
}
