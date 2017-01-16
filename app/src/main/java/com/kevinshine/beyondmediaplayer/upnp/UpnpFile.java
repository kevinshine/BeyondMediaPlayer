package com.kevinshine.beyondmediaplayer.upnp;

import org.fourthline.cling.model.meta.Device;

public class UpnpFile {
    public static final int TYPE_DEVICE = 1;
    public static final int TYPE_DIR = 1 << 1;
    public static final int TYPE_FILE = 1 << 2;

    private String name = "";
    private String uri = "";
    private UpnpFile parent;
    private String fullPath = "";
    private String path = "";
    private UpnpFile root;
    private Device upnpDevice;
    private int fileType;

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    private String objectId;

    public Device getUpnpDevice() {
        return upnpDevice;
    }

    public void setUpnpDevice(Device upnpDevice) {
        this.upnpDevice = upnpDevice;
    }

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    public boolean isDirectory() {
        return (fileType & TYPE_DIR) > 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public UpnpFile getParent() {
        return parent;
    }

    public void setParent(UpnpFile parent) {
        this.parent = parent;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public UpnpFile getRoot() {
        return root;
    }

    public void setRoot(UpnpFile root) {
        this.root = root;
    }

    public boolean isDevice() {
        return (fileType & TYPE_DEVICE) > 0;
    }

    public boolean isFile() {
        return (fileType & TYPE_FILE) > 0;
    }
}
