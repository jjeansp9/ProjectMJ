package kr.jeet.edu.manager.model.data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class FileData implements Parcelable {
    @SerializedName("seq")
    public int seq;

    @SerializedName("fileId")
    public String fileId;

    @SerializedName("orgName")
    public String orgName;

    @SerializedName("saveName")
    public String saveName;

    @SerializedName("path")
    public String path;

    @SerializedName("extension")
    public String extension;

    public String tempFileName;
//    public boolean isTempDownloaded;
    public FileData(){}
    protected FileData(Parcel in) {
        seq = in.readInt();
        fileId = in.readString();
        orgName = in.readString();
        saveName = in.readString();
        path = in.readString();
        extension = in.readString();
        tempFileName = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(seq);
        dest.writeString(fileId);
        dest.writeString(orgName);
        dest.writeString(saveName);
        dest.writeString(path);
        dest.writeString(extension);
        dest.writeString(tempFileName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FileData> CREATOR = new Creator<FileData>() {
        @Override
        public FileData createFromParcel(Parcel in) {
            return new FileData(in);
        }

        @Override
        public FileData[] newArray(int size) {
            return new FileData[size];
        }
    };

    public void initTempFileName() {
        this.tempFileName = String.format("%d_%s", seq, orgName);
    }

}
