package com.kevinshine.beyondmediaplayer.utils;

import java.net.URLEncoder;
import java.util.regex.Pattern;

import jcifs.smb.SmbFile;

/**
 * Created by gary on 16-1-8.
 */
public class SmbUtil {
    private static Pattern pattern = Pattern.compile("^.*\\.(?i)(mp3|wma|wav|aac|ogg|m4a|flac|mp4|avi|mpg|mpeg|3gp|3gpp|mkv|flv|rmvb)$");

    public static String getSmbServerUrl(String[] auth, boolean anonym) {
        String smbFilePath;
        try {
            String yourPeerIP = auth[0], domain = "";
            smbFilePath = "smb://" + (anonym ? "" : (URLEncoder.encode(auth[1] + ":" + auth[2], "UTF-8") + "@")) + yourPeerIP + "/";
            return smbFilePath;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getNameFromPath(String path){
        if(path == null || path.length() <2)
            return null;
        int slash = path.lastIndexOf('/');
        if(slash == -1)
            return path;
        else
            return path.substring(slash+1);
    }

    public static boolean isStreamMedia(SmbFile file) {
        return pattern.matcher(file.getName()).matches();
    }

    public boolean isSmb(String path) {
        return path != null && path.startsWith("smb:/");
    }
}
