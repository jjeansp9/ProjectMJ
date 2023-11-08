package kr.jeet.edu.manager.model.data;

import android.net.Uri;
import android.os.Parcelable;
import android.text.TextUtils;

public class AttachFileData {
    public Uri uri; //uri   원본파일의 URI
    public String mimeType;
    public String originalFilePath;
    public String tempFilePath; // 사본의 파일 경로
    public String fileName;
    public String fileExt;
    public long fileSize;
    public int seq = -1;
    public AttachFileData(){}
    public AttachFileData(Uri uri, String mimeType, String originalFilePath, String fileName, long fileSize) {
        this.uri = uri;
        this.mimeType = mimeType;
        this.originalFilePath = originalFilePath;
        this.fileName = fileName;
        if(!TextUtils.isEmpty(fileName)) {
            if(fileName.contains(".")) {
                fileExt = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            }
        }
        this.fileSize = fileSize;
    }
}
