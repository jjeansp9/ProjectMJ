package kr.jeet.edu.manager.model.data;

import android.os.Parcel;
import android.os.Parcelable;

public class AnswererData implements Parcelable {
    public int sfCode;
    public String sfName;
    public boolean isSelected;
    public boolean isWrited;

    protected AnswererData(Parcel in) {
        sfCode = in.readInt();
        sfName = in.readString();
        isSelected = in.readByte() != 0;
        isWrited = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(sfCode);
        dest.writeString(sfName);
        dest.writeByte((byte) (isSelected ? 1 : 0));
        dest.writeByte((byte) (isWrited ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AnswererData> CREATOR = new Creator<AnswererData>() {
        @Override
        public AnswererData createFromParcel(Parcel in) {
            return new AnswererData(in);
        }

        @Override
        public AnswererData[] newArray(int size) {
            return new AnswererData[size];
        }
    };
}
