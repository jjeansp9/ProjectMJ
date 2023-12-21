package kr.jeet.edu.manager.model.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ScheduleData implements Parcelable {
    public int seq;
    public String title;
    public String content;
//    public String target;
//    public int targetCode;
    public String acaCode;
    public String acaName;
    public String acaGubunCode;
    public String acaGubunName;
    public int year;
    public int month;
    public int day;
    public int duration;
    public int writerSeq;
    public String isSendSMS;
    public ArrayList<RecipientData> receiverList;
    public MemberResponseVO memberResponseVO;

    public ScheduleData(){}

    protected ScheduleData(Parcel in) {
        seq = in.readInt();
        title = in.readString();
        content = in.readString();
        acaCode = in.readString();
        acaName = in.readString();
        acaGubunCode = in.readString();
        acaGubunName = in.readString();
        year = in.readInt();
        month = in.readInt();
        day = in.readInt();
        duration = in.readInt();
        writerSeq = in.readInt();
        isSendSMS = in.readString();
        receiverList = in.createTypedArrayList(RecipientData.CREATOR);
        memberResponseVO = in.readParcelable(MemberResponseVO.class.getClassLoader());
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
        dest.writeInt(year);
        dest.writeInt(month);
        dest.writeInt(day);
        dest.writeInt(duration);
        dest.writeInt(writerSeq);
        dest.writeString(isSendSMS);
        dest.writeTypedList(receiverList);
        dest.writeParcelable(memberResponseVO, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ScheduleData> CREATOR = new Creator<ScheduleData>() {
        @Override
        public ScheduleData createFromParcel(Parcel in) {
            return new ScheduleData(in);
        }

        @Override
        public ScheduleData[] newArray(int size) {
            return new ScheduleData[size];
        }
    };
}
