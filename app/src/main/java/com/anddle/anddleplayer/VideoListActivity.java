package com.anddle.anddleplayer;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VideoListActivity extends AppCompatActivity {

    public static final String TAG = "Anddle Player";

    private AsyncTask mVideoUpdateTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        mVideoUpdateTask = new VideoUpdateTask();
        mVideoUpdateTask.execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if((mVideoUpdateTask != null) &&
                (mVideoUpdateTask.getStatus() == AsyncTask.Status.RUNNING))
        {
            mVideoUpdateTask.cancel(true);
        }

        mVideoUpdateTask = null;
    }

    private class VideoUpdateTask  extends AsyncTask<Object, VideoItem, Void> {

        List<VideoItem> mDataList = new ArrayList<VideoItem>();

        @Override
        protected Void doInBackground(Object... params) {

            Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            String[] searchKey = new String[] {
                    MediaStore.Video.Media.TITLE,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.DATE_ADDED
            };
            String where = MediaStore.Video.Media.DATA + " like \"%"+getString(R.string.search_path)+"%\"";
            String [] keywords = null;
            String sortOrder = MediaStore.Video.Media.DEFAULT_SORT_ORDER;

            ContentResolver resolver = getContentResolver();
            Cursor cursor = resolver.query(uri, searchKey, where, keywords, sortOrder);

            if(cursor != null)
            {
                while(cursor.moveToNext() && ! isCancelled())
                {
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                    String createdTime = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED));
                    VideoItem data = new VideoItem(path, name, createdTime);

                    Log.d(TAG, "real video found: " + path);
                    publishProgress(data);
                }

                cursor.close();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(VideoItem... values) {

            VideoItem data = values[0];

        }

        @Override
        protected void onPostExecute(Void result) {

            Log.d(TAG, "Task has been finished");

        }

        @Override
        protected void onCancelled() {

            Log.d(TAG, "Task has been cancelled");

        }

    }

}
