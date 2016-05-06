package com.anddle.anddleplayer;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Gym on 16/4/25.
 */
public class VideoItem {

    String name;
    String path;
    Bitmap thumb;
    String createdTime;

    VideoItem(String strPath, String strName, String createdTime) {

        this.path = strPath;
        this.name = strName;

        SimpleDateFormat sf = new SimpleDateFormat("yy年MM月dd日HH时mm分");
        Date d = new Date(Long.valueOf(createdTime)*1000);
        this.createdTime = sf.format(d);
    }

    void createThumb()
    {
        if(this.thumb == null)
        {
            this.thumb = ThumbnailUtils.createVideoThumbnail(this.path, MediaStore.Images.Thumbnails.MINI_KIND);
        }
    }

    void releaseThumb()
    {
        if(this.thumb != null){
            this.thumb.recycle();
            this.thumb = null;
        }
    }

    @Override
    public boolean equals(Object o) {
        VideoItem another = (VideoItem) o;

        return another.path.equals(this.path);
    }
}