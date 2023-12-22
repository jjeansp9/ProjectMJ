package kr.jeet.edu.manager.model.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import kr.jeet.edu.manager.common.DataManager;

public class BriefingData implements Parcelable, ReadData {
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
    public boolean isRead = false; // 읽음 유무
    @SerializedName("memberResponseVO")
    public MemberResponseVO memberResponseVO;
    @SerializedName("fileList")
    public List<FileData> fileList = new ArrayList<>();
    String type = DataManager.BOARD_PT;

    public BriefingData(){}
    public BriefingData(int seq, String title, String content, String acaCode, String acaName, String date, String ptTime, String place, int participantsCnt, int reservationCnt, String isSendSMS, String fileId, MemberResponseVO memberResponseVO, List<FileData> fileList) {
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
        this.memberResponseVO = memberResponseVO;
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
        isRead = in.readByte() != 0;
        memberResponseVO = in.readParcelable(MemberResponseVO.class.getClassLoader());
        fileList = in.createTypedArrayList(FileData.CREATOR);
        type = in.readString();
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
        dest.writeByte((byte) (isRead ? 1 : 0));
        dest.writeParcelable(memberResponseVO, flags);
        dest.writeTypedList(fileList);
        dest.writeString(type);
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

    @Override
    public String getDate() {
        return date;
    }

    @Override
    public String getTime() {
        return ptTime;
    }

    @Override
    public boolean getIsRead() {
        return isRead;
    }

    @Override
    public int getSeq() {
        return seq;
    }

    @Override
    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public void setTime(String time) {
        this.ptTime = time;
    }

    @Override
    public void setSeq(int seq) {
        this.seq = seq;
    }

    @Override
    public void setIsRead(boolean read) {
        isRead = read;
    }

    @Override
    public String getType() {
        return type;
    }
}

