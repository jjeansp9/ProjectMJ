package kr.jeet.edu.manager.model.request;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.List;

import kr.jeet.edu.manager.model.data.AnnouncementData;

public class AnnouncementRequest implements Parcelable {
    //seq
    public Integer seq = null;

    //title
    public String title;

    //content
    public String content;

    //fileId
    public String fileId;

    //memberSeq
    public int memberSeq;

    //acaCode
    public String acaCode;
    public String acaName;  //test
    //grade Code
    public String acaGubunCode;
    public String acaGubunName;  //test
    //fileDeleteSeqList
    public List<Integer> fileDeleteSeqList;

    //isSendNoti
    public String isSendNoti;

    //isSendSms
    public String isSendSMS;

    //smsSender
    public String smsSender;

    public AnnouncementRequest(){}

    protected AnnouncementRequest(Parcel in) {
        if (in.readByte() == 0) {
            seq = null;
        } else {
            seq = in.readInt();
        }
        title = in.readString();
        content = in.readString();
        fileId = in.readString();
        memberSeq = in.readInt();
        acaCode = in.readString();
        acaName = in.readString();
        acaGubunCode = in.readString();
        acaGubunName = in.readString();
        isSendNoti = in.readString();
        isSendSMS = in.readString();
        smsSender = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (seq == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(seq);
        }
        dest.writeString(title);
        dest.writeString(content);
        dest.writeString(fileId);
        dest.writeInt(memberSeq);
        dest.writeString(acaCode);
        dest.writeString(acaName);
        dest.writeString(acaGubunCode);
        dest.writeString(acaGubunName);
        dest.writeString(isSendNoti);
        dest.writeString(isSendSMS);
        dest.writeString(smsSender);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AnnouncementRequest> CREATOR = new Creator<AnnouncementRequest>() {
        @Override
        public AnnouncementRequest createFromParcel(Parcel in) {
            return new AnnouncementRequest(in);
        }

        @Override
        public AnnouncementRequest[] newArray(int size) {
            return new AnnouncementRequest[size];
        }
    };

    public static AnnouncementRequest initNewDataFromAnnouncementData(AnnouncementData data, String acaCode, int memberSeq, boolean isSendSMS, String smsSender) {
        AnnouncementRequest request = new AnnouncementRequest();
        if(data == null) return request;
        if(data.seq != 0) {
            request.seq = data.seq;
        }
        request.title = data.title;
        request.content = data.content;
        request.fileId = data.fileId;
        if(data.memberResponseVO != null) {
            request.memberSeq = data.memberResponseVO.seq;
        }else{
            request.memberSeq = memberSeq;
        }
        //
        //request.acaCode = data.acaCode;
        request.acaCode = acaCode;
//        request.isSendNoti = isSendNotify? "Y" : "N";
        request.isSendSMS = isSendSMS? "Y" : "N";
        request.smsSender = smsSender;
        return request;
    }
    public static AnnouncementRequest initNewDataFromAnnouncementData(AnnouncementData data, String acaCode, String acaName, String acaGubunCode, String acaGubunName, int memberSeq, boolean isSendSMS, String smsSender) {
        AnnouncementRequest request = new AnnouncementRequest();
        if(data == null) return request;
        if(data.seq != 0) {
            request.seq = data.seq;
        }
        request.title = data.title;
        request.content = data.content;
        request.fileId = data.fileId;
        if(data.memberResponseVO != null) {
            request.memberSeq = data.memberResponseVO.seq;
        }else{
            request.memberSeq = memberSeq;
        }
        //
        //request.acaCode = data.acaCode;
        request.acaCode = acaCode;
        request.acaName = acaName;
        request.acaGubunCode = acaGubunCode;
        request.acaGubunName = acaGubunName;
//        request.isSendNoti = isSendNotify? "Y" : "N";
        request.isSendSMS = isSendSMS? "Y" : "N";
//        request.smsSender = smsSender;
        return request;
    }
    public static AnnouncementRequest initUpdateDataFromAnnouncementData(AnnouncementData data, List<Integer> deleteSeqList) {
        AnnouncementRequest request = new AnnouncementRequest();
        if(data == null) return request;
        if(data.seq != 0) {
            request.seq = data.seq;
        }
        request.title = data.title;
        request.content = data.content;
        request.fileId = data.fileId;
        request.fileDeleteSeqList = deleteSeqList;
//        request.isSendNoti = "N";
        request.isSendSMS = "N";
        return request;
    }
}
