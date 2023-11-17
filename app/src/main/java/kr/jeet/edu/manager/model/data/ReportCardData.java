package kr.jeet.edu.manager.model.data;

import android.os.Parcel;
import android.os.Parcelable;

public class ReportCardData implements Parcelable {
    public String etGubun;  //eg. "6-2 B형"
    public String etName;   //eg. "KJ 입학고사"
    public int etCode;
    public int etTitleGubun;
    public String etTitleGubunName;
    public String regDate;
    public String msgYn;
    public boolean isSelected = false;

    protected ReportCardData(Parcel in) {
        etGubun = in.readString();
        etName = in.readString();
        etCode = in.readInt();
        etTitleGubun = in.readInt();
        etTitleGubunName = in.readString();
        regDate = in.readString();
        msgYn = in.readString();
        isSelected = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(etGubun);
        dest.writeString(etName);
        dest.writeInt(etCode);
        dest.writeInt(etTitleGubun);
        dest.writeString(etTitleGubunName);
        dest.writeString(regDate);
        dest.writeString(msgYn);
        dest.writeByte((byte) (isSelected ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ReportCardData> CREATOR = new Creator<ReportCardData>() {
        @Override
        public ReportCardData createFromParcel(Parcel in) {
            return new ReportCardData(in);
        }

        @Override
        public ReportCardData[] newArray(int size) {
            return new ReportCardData[size];
        }
    };
}
