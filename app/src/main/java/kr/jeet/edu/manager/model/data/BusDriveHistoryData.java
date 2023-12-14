package kr.jeet.edu.manager.model.data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class BusDriveHistoryData implements Parcelable{
    public int busDriveHistSeq; //버스 history seq
    public String busAcaName;   //버스 캠퍼스 이름
    public String busName;
    public int busCode;
    public String isDrive;
    public String startDate;    //운행 시작 시간
    public String endDate;      //운행 종료 시간

    protected BusDriveHistoryData(Parcel in) {
        busDriveHistSeq = in.readInt();
        busAcaName = in.readString();
        busName = in.readString();
        busCode = in.readInt();
        isDrive = in.readString();
        startDate = in.readString();
        endDate = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(busDriveHistSeq);
        dest.writeString(busAcaName);
        dest.writeString(busName);
        dest.writeInt(busCode);
        dest.writeString(isDrive);
        dest.writeString(startDate);
        dest.writeString(endDate);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BusDriveHistoryData> CREATOR = new Creator<BusDriveHistoryData>() {
        @Override
        public BusDriveHistoryData createFromParcel(Parcel in) {
            return new BusDriveHistoryData(in);
        }

        @Override
        public BusDriveHistoryData[] newArray(int size) {
            return new BusDriveHistoryData[size];
        }
    };
}
