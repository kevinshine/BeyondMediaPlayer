// Created by gary on 12/5/15.
//
#include "playlist.h"
#include <android/log.h>
#include <string>
#include <jni.h>

static PlayList playList;

vector <MediaItem> *PlayList::getMediaVector() {
    vector<MediaItem>::size_type size = mMediaVector.size();
    __android_log_print(ANDROID_LOG_INFO, "JNIMsg", "Your params is %i", size);
    return &mMediaVector;
}

void PlayList::addMediaItem(MediaItem &mediaItem) {
    vector<MediaItem>::iterator it;
    it = mMediaVector.begin();
    mMediaVector.insert(it,mediaItem);
}

// C API
#ifdef __cplusplus
extern "C" {
#endif

jobjectArray PlaylistManager_getPlaylist(JNIEnv *env){

    vector<MediaItem> *vec = playList.getMediaVector();
    const vector<MediaItem>::size_type vec_size = vec->size();

    jclass itemClazz = env->FindClass(JNI_CLASS_MEDIA_INFO);
    jobjectArray itemArray = env->NewObjectArray(vec_size, itemClazz, NULL);
    jmethodID mid = env->GetMethodID(itemClazz,"<init>","()V");

    jfieldID mediaIndex=env->GetFieldID(itemClazz,"mediaIndex","I");
    jfieldID nameID=env->GetFieldID(itemClazz,"mediaName","Ljava/lang/String;");
    jfieldID pathID=env->GetFieldID(itemClazz,"mediaPath","Ljava/lang/String;");

    for (vector<MediaItem>::size_type i = 0; i < vec_size; ++i) {
        MediaItem item = (*vec)[i];

        jobject obj=env->NewObject(itemClazz,mid);
        jobject mediaName=env->NewStringUTF(item.media_name.c_str());
        jobject mediaPath=env->NewStringUTF(item.media_path.c_str());

        env->SetIntField(obj,mediaIndex,i);
        env->SetObjectField(obj,nameID,mediaName);
        env->SetObjectField(obj,pathID,mediaPath);
        env->SetObjectArrayElement(itemArray, i, obj);

        env->DeleteLocalRef(obj);
        env->DeleteLocalRef(mediaName);
        env->DeleteLocalRef(mediaPath);
    }

    env->DeleteLocalRef(itemClazz);
    return itemArray;
}

void PlaylistManager_addPlaylistItem(JNIEnv *env,jobject thiz, jobject mediaInfo){
    jclass cls = env->GetObjectClass(mediaInfo);

    jfieldID nameID=env->GetFieldID(cls,"mediaName","Ljava/lang/String;");
    jfieldID pathID=env->GetFieldID(cls,"mediaPath","Ljava/lang/String;");

    jstring mediaName = (jstring)env->GetObjectField(mediaInfo,nameID);
    jstring mediaPath = (jstring)env->GetObjectField(mediaInfo,pathID);

    const char *strCharsName;
    const char *strCharsPath;
    strCharsName = env->GetStringUTFChars(mediaName, 0);
    strCharsPath = env->GetStringUTFChars(mediaPath, 0);
    __android_log_print(ANDROID_LOG_INFO, "JNIMsg", "Add media item: mediaName %s,mediaPath %s", strCharsName,strCharsPath);

    MediaItem item;
    item.media_name = strCharsName;
    item.media_path = strCharsPath;
    playList.addMediaItem(item);

    // release
    env->ReleaseStringUTFChars(mediaName,strCharsName);
    env->ReleaseStringUTFChars(mediaPath,strCharsPath);
    env->DeleteLocalRef(cls);
}

void PlaylistManager_clearPlaylist(JNIEnv *env){
    vector<MediaItem> *vec = playList.getMediaVector();
    vec->clear();
}

void updateCurrentIndex(int index){
    playList.current_index = index;
    __android_log_print(ANDROID_LOG_INFO, "JNIMsg", "updateCurrentIndex %i", index);
}

void getNextPlaylistItemIndexAndPath(int* index,char* path){
    vector<MediaItem> *vec = playList.getMediaVector();

    unsigned int nextIndex = playList.current_index + 1;
    if(nextIndex >= vec->size()){
        nextIndex = 0;
    }

    if(vec->size() == 0){
        path = NULL;
        return;
    }

    MediaItem item = (*vec)[nextIndex];
    *index = nextIndex;
    strcpy(path,item.media_path.c_str());
    __android_log_print(ANDROID_LOG_INFO, "JNIMsg", "getNextPlaylistItemIndexAndPath %s", path);
}

#ifdef __cplusplus
} // extern "C"
#endif