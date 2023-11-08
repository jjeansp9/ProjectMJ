package kr.jeet.edu.manager.model.data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class BusInfoData implements Parcelable {
    public String bcName;
    public String busName;
    public int busCode;
    public String busPhoneNumber;
    public String isDrive;

    protected BusInfoData(Parcel in) {
        bcName = in.readString();
        busName = in.readString();
        busCode = in.readInt();
        busPhoneNumber = in.readString();
        isDrive = in.readString();
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
        parcel.writeString(bcName);
        parcel.writeString(busName);
        parcel.writeInt(busCode);
        parcel.writeString(busPhoneNumber);
        parcel.writeString(isDrive);
    }
}
