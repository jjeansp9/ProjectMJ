package kr.jeet.edu.manager.model.data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import kr.jeet.edu.manager.common.DataManager;

public class AnnouncementData implements Parcelable, ReadData {
    @SerializedName("seq")
    public int seq;

    @SerializedName("title")
    public String title;

    @SerializedName("content")
    public String content;

    @SerializedName("rdcnt")
    public int rdcnt;

    @SerializedName("fileId")
    public String fileId;

    @SerializedName("acaCode")
    public String acaCode;
    @SerializedName("acaName")
    public String acaName;
    @SerializedName("acaGubunCode")
    public String acaGubunCode;
    @SerializedName("acaGubunName")
    public String acaGubunName;
    @SerializedName("insertDate")
    public String insertDate;

    @SerializedName("memberResponseVO")
    public MemberResponseVO memberResponseVO;

    @SerializedName("fileList")
    public List<FileData> fileList;
    public ArrayList<RecipientData> receiverList;
    public boolean isRead = false;
    String type = DataManager.BOARD_NOTICE;
    public AnnouncementData(){}

    protected AnnouncementData(Parcel in) {
        seq = in.readInt();
        title = in.readString();
        content = in.readString();
        rdcnt = in.readInt();
        fileId = in.readString();
        acaCode = in.readString();
        acaName = in.readString();
        acaGubunCode = in.readString();
        acaGubunName = in.readString();
        insertDate = in.readString();
        memberResponseVO = in.readParcelable(MemberResponseVO.class.getClassLoader());
        fileList = in.createTypedArrayList(FileData.CREATOR);
        receiverList = in.createTypedArrayList(RecipientData.CREATOR);
        isRead = in.readByte() != 0;
        type = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(seq);
        dest.writeString(title);
        dest.writeString(content);
        dest.writeInt(rdcnt);
        dest.writeString(fileId);
        dest.writeString(acaCode);
        dest.writeString(acaName);
        dest.writeString(acaGubunCode);
        dest.writeString(acaGubunName);
        dest.writeString(insertDate);
        dest.writeParcelable(memberResponseVO, flags);
        dest.writeTypedList(fileList);
        dest.writeTypedList(receiverList);
        dest.writeByte((byte) (isRead ? 1 : 0));
        dest.writeString(type);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AnnouncementData> CREATOR = new Creator<AnnouncementData>() {
        @Override
        public AnnouncementData createFromParcel(Parcel in) {
            return new AnnouncementData(in);
        }

        @Override
        public AnnouncementData[] newArray(int size) {
            return new AnnouncementData[size];
        }
    };

    @NonNull
    @Override
    public String toString() {
        String jsonString = new Gson().toJson(this);
        return jsonString;
    }

    @Override
    public String getDate() {
        return insertDate;
    }

    @Override
    public String getTime() {
        return null;
    }

    @Override
    public int getSeq() {
        return seq;
    }

    @Override
    public boolean getIsRead() {
        return isRead;
    }

    @Override
    public void setDate(String date) {
        this.insertDate = date; // yyyy-MM-dd HH:ss
    }

    @Override
    public void setTime(String time) {
        // 공지사항엔 time 파라미터가 없음. date 파라미터 참고
    }

    @Override
    public void setSeq(int seq) {
        this.seq = seq;
    }

    @Override
    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }

    @Override
    public String getType() {
        return type;
    }
}
