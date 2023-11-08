package kr.jeet.edu.manager.model.data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class MemberResponseVO implements Parcelable {
    @SerializedName("seq")
    public int seq;

    @SerializedName("name")
    public String name;

    @SerializedName("userGubun")
    public int userGubun;

    protected MemberResponseVO(Parcel in) {
        seq = in.readInt();
        name = in.readString();
        userGubun = in.readInt();
    }

    public static final Creator<MemberResponseVO> CREATOR = new Creator<MemberResponseVO>() {
        @Override
        public MemberResponseVO createFromParcel(Parcel in) {
            return new MemberResponseVO(in);
        }

        @Override
        public MemberResponseVO[] newArray(int size) {
            return new MemberResponseVO[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(seq);
        parcel.writeString(name);
        parcel.writeInt(userGubun);
    }
}
