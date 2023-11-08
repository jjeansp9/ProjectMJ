package kr.jeet.edu.manager.receiver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.widget.Toast;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.model.data.FileData;

public class DownloadReceiver extends BroadcastReceiver {
    int position;
    FileData currentFileData;
//    String originalDownloadPath;
    boolean isRequireRun = false;
    public interface DownloadListener {
        void onDownloadComplete(int position, FileData fileData, Uri uri);
        void onShow(FileData data);
    }
    DownloadListener _listener;
    public DownloadReceiver(DownloadListener listener) {
        this._listener = listener;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            handleDownloadComplete(context, downloadId);
        } else if (DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(action)) {

        }
    }
    public void setCurrentPosition(int position) {
        this.position = position;
    }
    public void setCurrentFileData(FileData data) {
        this.currentFileData = data;
    }
//    public void setOriginalDownloadPath(String path) {
//        this.originalDownloadPath = path;
//    }
    public void setRequireRun(boolean flag) {this.isRequireRun = flag; }

    private void handleDownloadComplete(Context context, long downloadId) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);
        Cursor cursor = downloadManager.query(query);
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            int status = cursor.getInt(columnIndex);
            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                Toast.makeText(context, R.string.msg_success_to_download, Toast.LENGTH_SHORT).show();
                Uri uri = downloadManager.getUriForDownloadedFile(downloadId);
                _listener.onDownloadComplete(position, currentFileData, uri);
                if(isRequireRun) _listener.onShow(currentFileData);
            } else {
                Toast.makeText(context, R.string.error_msg_fail_to_download, Toast.LENGTH_SHORT).show();
                _listener.onDownloadComplete(position, currentFileData, null);
            }
        }
        cursor.close();
    }
}
