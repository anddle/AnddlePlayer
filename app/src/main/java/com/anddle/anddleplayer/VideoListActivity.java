package com.anddle.anddleplayer;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class VideoListActivity extends AppCompatActivity {

    public static final String TAG = "Anddle Player";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        String[] searchKey = new String[] {
                MediaStore.Video.Media.TITLE,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_ADDED
        };
        String [] keywords = null;
        String where = MediaStore.Video.Media.DATA + " like \"%"+"/Video"+"%\"";
        String sortOrder = MediaStore.Video.Media.DEFAULT_SORT_ORDER;

        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(
                uri,
                searchKey,
                where,
                keywords,
                sortOrder);

        if(cursor != null)
        {
            while(cursor.moveToNext())
            {
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                String name = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));

                String createdTime = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED));

                VideoItem item = new VideoItem(path, name, createdTime);
                Log.d(TAG, "real video found: " + path);

            }

            cursor.close();
        }
    }
}
