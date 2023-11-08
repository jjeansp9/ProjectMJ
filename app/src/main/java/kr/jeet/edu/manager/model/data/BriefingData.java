package kr.jeet.edu.manager.model.data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class BriefingData implements Parcelable {
    @SerializedName("seq")
    public int seq;
    @SerializedName("title")
    public String title;
    @SerializedName("content")
    public String content;
    @SerializedName("acaCode")
    public String acaCode;
    @SerializedName("acaName")
    public String acaName;
    @SerializedName("acaGubunCode")
    public String acaGubunCode;
    @SerializedName("acaGubunName")
    public String acaGubunName;
    @SerializedName("date")
    public String date;
    @SerializedName("ptTime")
    public String ptTime;
    @SerializedName("place")
    public String place;
    @SerializedName("writerSeq")
    public int writerSeq;
    @SerializedName("participantsCnt")
    public int participantsCnt;
    @SerializedName("reservationCnt")
    public int reservationCnt;
    @SerializedName("rdcnt")
    public int rdcnt; // 조회수
    @SerializedName("isSendSMS")
    public String isSendSMS;
    @SerializedName("fileId")
    public String fileId;
    @SerializedName("fileList")
    public List<FileData> fileList = new ArrayList<>();


    public BriefingData(){}
    public BriefingData(int seq, String title, String content, String acaCode, String acaName, String date, String ptTime, String place, int participantsCnt, int reservationCnt, String isSendSMS, String fileId, List<FileData> fileList) {
        this.seq = seq;
        this.title = title;
        this.content = content;
        this.acaCode = acaCode;
        this.acaName = acaName;
        this.date = date;
        this.ptTime = ptTime;
        this.place = place;
        this.participantsCnt = participantsCnt;
        this.reservationCnt = reservationCnt;
        this.isSendSMS = isSendSMS;
        this.fileId = fileId;
        if(fileList != null)
            this.fileList = fileList;
    }

    protected BriefingData(Parcel in) {
        seq = in.readInt();
        title = in.readString();
        content = in.readString();
        acaCode = in.readString();
        acaName = in.readString();
        acaGubunCode = in.readString();
        acaGubunName = in.readString();
        date = in.readString();
        ptTime = in.readString();
        place = in.readString();
        writerSeq = in.readInt();
        participantsCnt = in.readInt();
        reservationCnt = in.readInt();
        rdcnt = in.readInt();
        isSendSMS = in.readString();
        fileId = in.readString();
        fileList = in.createTypedArrayList(FileData.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(seq);
        dest.writeString(title);
        dest.writeString(content);
        dest.writeString(acaCode);
        dest.writeString(acaName);
        dest.writeString(acaGubunCode);
        dest.writeString(acaGubunName);
        dest.writeString(date);
        dest.writeString(ptTime);
        dest.writeString(place);
        dest.writeInt(writerSeq);
        dest.writeInt(participantsCnt);
        dest.writeInt(reservationCnt);
        dest.writeInt(rdcnt);
        dest.writeString(isSendSMS);
        dest.writeString(fileId);
        dest.writeTypedList(fileList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BriefingData> CREATOR = new Creator<BriefingData>() {
        @Override
        public BriefingData createFromParcel(Parcel in) {
            return new BriefingData(in);
        }

        @Override
        public BriefingData[] newArray(int size) {
            return new BriefingData[size];
        }
    };
}

