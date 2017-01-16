package com.kevinshine.beyondmediaplayer.model.bean;

/**
 * Created by gary on 16-2-17.
 */
public class DrawerItemBean {
    private long id;
    private String uri;
    private String title;
    private String type;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
