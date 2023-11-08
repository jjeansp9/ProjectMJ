package kr.jeet.edu.manager.model.data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class ManagerInfo implements Parcelable {
    @SerializedName("name")
    public String name; // 강사 이름
    @SerializedName("phoneNumber")
    public String phoneNumber; // 강사 전화번호
    @SerializedName("gender")
    public String gender; // 성별
    @SerializedName("acaCode")
    public String acaCode; // 캠퍼스 코드
    @SerializedName("appAcaCode")
    public String appAcaCode; // Local캠퍼스 코드
    @SerializedName("deptCode")
    public int deptCode;    //부서코드
    @SerializedName("clstCode")
    public int clstCode;    //학년 코드
    @SerializedName("acaName")
    public String acaName; // 캠퍼스 이름
    @SerializedName("appAcaName")
    public String appAcaName; // Local캠퍼스 이름
    @SerializedName("deptName")
    public String deptName; // 부서 이름
    @SerializedName("clstName")
    public String clstName; // 담당학년
    @SerializedName("birth")
    public String birth; // 생일
    @SerializedName("sfGrade")
    public String sfGrade; // 담당학년
    @SerializedName("smsSender")
    public String smsSender; // 전화번호
    @SerializedName("pushStatus")
    public StudentInfo.PushStatus pushStatus;
    public ManagerInfo(){}

    public static class PushStatus {
        @SerializedName("seq")
        public int seq;

        @SerializedName("pushNotice")
        public String pushNotice;

        @SerializedName("pushInformationSession")
        public String pushInformationSession;

        @SerializedName("pushAttendance")
        public String pushAttendance;

        @SerializedName("pushSystem")
        public String pushSystem;
    }

    protected ManagerInfo(Parcel in) {
        name = in.readString();
        phoneNumber = in.readString();
        gender = in.readString();
        acaCode = in.readString();
        appAcaCode = in.readString();
        deptCode = in.readInt();
        clstCode = in.readInt();
        acaName = in.readString();
        appAcaName = in.readString();
        deptName = in.readString();
        clstName = in.readString();
        birth = in.readString();
        sfGrade = in.readString();
        smsSender = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(phoneNumber);
        dest.writeString(gender);
        dest.writeString(acaCode);
        dest.writeString(appAcaCode);
        dest.writeInt(deptCode);
        dest.writeInt(clstCode);
        dest.writeString(acaName);
        dest.writeString(appAcaName);
        dest.writeString(deptName);
        dest.writeString(clstName);
        dest.writeString(birth);
        dest.writeString(sfGrade);
        dest.writeString(smsSender);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ManagerInfo> CREATOR = new Creator<ManagerInfo>() {
        @Override
        public ManagerInfo createFromParcel(Parcel in) {
            return new ManagerInfo(in);
        }

        @Override
        public ManagerInfo[] newArray(int size) {
            return new ManagerInfo[size];
        }
    };
}
