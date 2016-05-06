package com.anddle.anddleplayer;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VideoListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    public static final String TAG = "Anddle Player";

    private AsyncTask mVideoUpdateTask;
    private List<VideoItem> mVideoList;
    private ListView mVideoListView;
    private MenuItem mRefreshMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        this.setTitle(R.string.video_list);

        mVideoList = new ArrayList<VideoItem>();
        mVideoListView = (ListView) findViewById(R.id.video_list);
        VideoItemAdapter adapter = new VideoItemAdapter(this, R.layout.video_item, mVideoList);
        mVideoListView.setAdapter(adapter);

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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        VideoItem item = mVideoList.get(position);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        //获取“刷新”菜单项
        mRefreshMenuItem = menu.findItem(R.id.menu_refresh);

        //当VideoUpdateTask处于运行的状态时，菜单项的标题显示“停止刷新”，
        if((mVideoUpdateTask != null) && (mVideoUpdateTask.getStatus() == AsyncTask.Status.RUNNING)) {
            mRefreshMenuItem.setTitle(R.string.in_refresh);
        }
        //当VideoUpdateTask没有处于运行的状态时，菜单项的标题显示“刷新”，
        else {
            mRefreshMenuItem.setTitle(R.string.refresh);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId())
        {
            case R.id.menu_refresh:
            {
                if((mVideoUpdateTask != null) && (mVideoUpdateTask.getStatus() == AsyncTask.Status.RUNNING)) {
                    //当VideoUpdateTask处于运行的状态时，取消VideoUpdateTask的工作
                    mVideoUpdateTask.cancel(true);
                    mVideoUpdateTask = null;
                }
                else {
                    //当VideoUpdateTask没有处于运行的状态时，启动VideoUpdateTask的工作
                    mVideoUpdateTask = new VideoUpdateTask();
                    mVideoUpdateTask.execute();
                    //修改菜单项的标题为“停止刷新”
                    if(mRefreshMenuItem != null) {
                        mRefreshMenuItem.setTitle(R.string.in_refresh);
                    }
                }
            }
            break;


            default:
                return super.onContextItemSelected(item);

        }

        return true;
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

                    if(mVideoList.contains(data) == false) {
                        //判断需要添加，才创建缩略图
                        data.createThumb();
                        publishProgress(data);
                    }

                    mDataList.add(data);

                }

                cursor.close();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(VideoItem... values) {

            VideoItem data = values[0];

            mVideoList.add(data);
            VideoItemAdapter adapter = (VideoItemAdapter) mVideoListView.getAdapter();
            adapter.notifyDataSetChanged();

        }

        @Override
        protected void onPostExecute(Void result) {

            Log.d(TAG, "Task has been finished");

            updateResult();

        }

        @Override
        protected void onCancelled() {

            Log.d(TAG, "Task has been cancelled");

            updateResult();

        }

        private void updateResult()
        {
            for(int i = 0; i < mVideoList.size(); i++)
            {
                if(!mDataList.contains(mVideoList.get(i)))
                {
                    //释放缩略图占用的内存资源
                    mVideoList.get(i).releaseThumb();
                    //从ListView的数据集中移除多余的视频信息
                    mVideoList.remove(i);
                    //因为移除了一个视频项，下一个视频项的序号就被减小了一个1
                    i--;
                }
            }
            mDataList.clear();

            VideoItemAdapter adapter = (VideoItemAdapter) mVideoListView.getAdapter();
            adapter.notifyDataSetChanged();

            //修改菜单项的标题为“停止刷新”
            if(mRefreshMenuItem != null) {
                mRefreshMenuItem.setTitle(R.string.refresh);
            }
        }

    }

}
