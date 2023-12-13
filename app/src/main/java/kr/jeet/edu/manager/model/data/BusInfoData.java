package kr.jeet.edu.manager.model.data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class BusInfoData extends BusDriveHistoryData {
    public String bcName;
    public String busPhoneNumber;
    public String busFile1;
    public String busFile2;

    protected BusInfoData(Parcel in) {
        super(in);
        bcName = in.readString();
        busPhoneNumber = in.readString();
        busFile1 = in.readString();
        busFile2 = in.readString();
    }

    public static final Creator<BusInfoData> CREATOR = new Creator<BusInfoData>() {
        @Override
        public BusInfoData createFromParcel(Parcel in) {
            return new BusInfoData(in);
        }

        @Override
        public BusInfoData[] newArray(int size) {
            return new BusInfoData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(bcName);
        parcel.writeString(busPhoneNumber);
        parcel.writeString(busFile1);
        parcel.writeString(busFile2);
    }
}
