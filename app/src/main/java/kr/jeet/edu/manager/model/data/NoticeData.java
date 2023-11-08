package kr.jeet.edu.manager.model.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class NoticeData implements Parcelable {
    @SerializedName("seq")
    public int seq;
    @SerializedName("userGubun")
    public int userGubun;
    @SerializedName("writerSeq")
    public int writerSeq;
    @SerializedName("writerName")
    public String writerName;
    @SerializedName("memberSeq")
    public int memberSeq;
    @SerializedName("memberName")
    public String memberName;
    @SerializedName("title")
    public String title;
    @SerializedName("content")
    public String content;
    @SerializedName("isSendSMS")
    public String isSendSMS;
    @SerializedName("receiverCnt")
    public int receiverCnt;
    @SerializedName("acaCode")
    public String acaCode;
    @SerializedName("acaName")
    public String acaName;
    @SerializedName("acaGubunCode")
    public String acaGubunCode;
    @SerializedName("insertDate")
    public String insertDate;
    @SerializedName("counselDate")
    public String counselDate;
    @SerializedName("searchType")
    public String searchType;
    @SerializedName("fileId")
    public String fileId;
    @SerializedName("fileList")
    public List<FileData> fileList = new ArrayList<>();
    @SerializedName("receiverList")
    public List<RecipientData> receiverList = new ArrayList<>();

    public NoticeData(){}
//    public NoticeData(int seq, int writerSeq, String writerName, String title, String content, String sendSms, int receiverCnt, String acaCode, String acaName, String insertDate, String searchType, String fileId, List<FileData> fileList, List<RecipientData> receiverList) {
//        this.seq = seq;
//        this.writerSeq = writerSeq;
//        this.writerName = writerName;
//        this.title = title;
//        this.content = content;
//        this.isSendSMS = sendSms;
//        this.receiverCnt = receiverCnt;
//        this.acaCode = acaCode;
//        this.acaName = acaName;
//        this.insertDate = insertDate;
//        this.searchType = searchType;
//        this.fileId = fileId;
//        if(fileList != null)
//            this.fileList = fileList;
//        if(receiverList != null)
//            this.receiverList = receiverList;
//    }


    protected NoticeData(Parcel in) {
        seq = in.readInt();
        writerSeq = in.readInt();
        writerName = in.readString();
        title = in.readString();
        content = in.readString();
        isSendSMS = in.readString();
        receiverCnt = in.readInt();
        acaCode = in.readString();
        acaName = in.readString();
        acaGubunCode = in.readString();
        insertDate = in.readString();
        searchType = in.readString();
        fileId = in.readString();
        fileList = in.createTypedArrayList(FileData.CREATOR);
        receiverList = in.createTypedArrayList(RecipientData.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(seq);
        dest.writeInt(writerSeq);
        dest.writeString(writerName);
        dest.writeString(title);
        dest.writeString(content);
        dest.writeString(isSendSMS);
        dest.writeInt(receiverCnt);
        dest.writeString(acaCode);
        dest.writeString(acaName);
        dest.writeString(acaGubunCode);
        dest.writeString(insertDate);
        dest.writeString(searchType);
        dest.writeString(fileId);
        dest.writeTypedList(fileList);
        dest.writeTypedList(receiverList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<NoticeData> CREATOR = new Creator<NoticeData>() {
        @Override
        public NoticeData createFromParcel(Parcel in) {
            return new NoticeData(in);
        }

        @Override
        public NoticeData[] newArray(int size) {
            return new NoticeData[size];
        }
    };
}
