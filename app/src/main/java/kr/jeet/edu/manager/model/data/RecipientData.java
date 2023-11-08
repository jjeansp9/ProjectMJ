package kr.jeet.edu.manager.model.data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

import kotlin.jvm.Transient;
import kr.jeet.edu.manager.utils.LogMgr;

/**
 * 수신인 ChipGroup 에서 사용
 */
public class RecipientData implements Parcelable, Comparable<RecipientData> {
    @SerializedName("seq")
    public int seq;
    //systemNoticeSeq
    @SerializedName("systemNoticeSeq")
    public int systemNoticeSeq;
    //stCode
    @SerializedName("stCode")
    public int stCode;
    //stName
    @SerializedName("stName")
    public String stName;
    //phoneNumber
    @SerializedName("phoneNumber")
    public String phoneNumber;
    @SerializedName("userGubun")
    public int userGubun;
    @SerializedName("acaCode")
    public String acaCode;
    @SerializedName("acaName")
    public String acaName;
    @SerializedName("isApp")
    public String isApp;
    @SerializedName("isRead")
    public String isRead;

    public RecipientData(){}
    protected RecipientData(Parcel in) {
        seq = in.readInt();
        stCode = in.readInt();
        stName = in.readString();
        phoneNumber = in.readString();
        userGubun = in.readInt();
        acaCode = in.readString();
        acaName = in.readString();
        isApp = in.readString();
        isRead = in.readString();
    }

    public static final Creator<RecipientData> CREATOR = new Creator<RecipientData>() {
        @Override
        public RecipientData createFromParcel(Parcel in) {
            return new RecipientData(in);
        }

        @Override
        public RecipientData[] newArray(int size) {
            return new RecipientData[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecipientData)) return false;
        RecipientData that = (RecipientData) o;
        LogMgr.e(stName + "<->" + that.stName + " : " + (seq == that.seq)  + " / " +  (stCode == that.stCode) + " / " +  (userGubun == that.userGubun) + " / " +  Objects.equals(stName, that.stName) + " / " +  Objects.equals(phoneNumber, that.phoneNumber) + " / " );
        return seq == that.seq && stCode == that.stCode && userGubun == that.userGubun && Objects.equals(stName, that.stName) && Objects.equals(phoneNumber, that.phoneNumber) && Objects.equals(acaCode, that.acaCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(seq, stCode, stName, phoneNumber, userGubun);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(seq);
        parcel.writeInt(stCode);
        parcel.writeString(stName);
        parcel.writeString(phoneNumber);
        parcel.writeInt(userGubun);
        parcel.writeString(acaCode);
        parcel.writeString(acaName);
        parcel.writeString(isApp);
        parcel.writeString(isRead);
    }

    @Override
    public int compareTo(RecipientData data) {
        if(data.stName == null) return 1;
        if(this.stName == null) return -1;
        if(this.stName.equals(data.stName)){
            return this.userGubun - data.userGubun;
        }else {
            return this.stName.compareTo(data.stName);
        }
    }
}
