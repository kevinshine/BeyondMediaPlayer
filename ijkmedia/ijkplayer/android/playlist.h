// Created by gary on 12/5/15.

#ifndef BEYONDMEDIAPLAYER_PLAYLIST_H
#define BEYONDMEDIAPLAYER_PLAYLIST_H

#include <string>
#include <vector>
#include "ijkplayer_android_def.h"

using namespace std;

struct MediaItem{
    int media_index;
    int media_id;
    string media_name;
    string media_path;
};

class PlayList {
public:
    PlayList() {
        // add items
        /*for (int j = 0; j < 10; ++j) {
            MediaItem item;
            string path("/storage/emulated/0/VID_20151029_155959.mp4");
            item.media_name = "VID_20151029_155959 " + to_string(j);
            item.media_path = path;
            addMediaItem(item);
        }*/
    }

    ~PlayList() { }

    int current_index;

    vector<MediaItem>* getMediaVector();

    void addMediaItem(MediaItem &mediaItem);

private:
    vector<MediaItem> mMediaVector;
};

#endif //BEYONDMEDIAPLAYER_PLAYLIST_H
