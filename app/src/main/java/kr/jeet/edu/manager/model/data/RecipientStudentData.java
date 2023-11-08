package kr.jeet.edu.manager.model.data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import kotlin.jvm.Transient;
import kr.jeet.edu.manager.model.response.BaseResponse;

/**
 * 수신인 RecyclerView 에서 사용
 */
public class RecipientStudentData implements Parcelable {
    @SerializedName("stName")
    public String stName = ""; // 학생 이름
    @SerializedName("stCode")
    public int stCode;// 학생 코드
    @SerializedName("acaCode")
    public String acaCode = ""; // 캠퍼스 코드
//    @SerializedName("deptName")
//    public String deptName = ""; // 부서 이름
    @SerializedName("stInstall")
    public String stInstall = ""; // 앱설치여부(학생)
    @SerializedName("parentInstall")
    public String parentInstall = ""; // 앱설치여부(부모)
    @SerializedName("stPhoneNumber")
    public String stPhoneNumber = ""; // 학생 전화번호
    @SerializedName("parentPhoneNumber")
    public String parentPhoneNumber = ""; // 학부모 전화번호
    @Transient
    public boolean isCheckStudent = false;
    @Transient
    public boolean isCheckParent = false;
    public RecipientStudentData(){}
    public RecipientStudentData(String name, int code, /*String campusName, String deptName, String clstName, String clsName,*/ String stPhone, String parentPhone) {
        this.stName = name;
        this.stCode = code;
//        this.acaName = campusName;
//        this.deptName = deptName;
//        this.clstName = clstName;
//        this.clsName = clsName;
        this.stPhoneNumber = stPhone;
        this.parentPhoneNumber = parentPhone;
    }

    protected RecipientStudentData(Parcel in) {
        stName = in.readString();
        stCode = in.readInt();
        acaCode = in.readString();
        stInstall = in.readString();
        parentInstall = in.readString();
        stPhoneNumber = in.readString();
        parentPhoneNumber = in.readString();
//        isCheckStudent = in.readByte() != 0;
//        isCheckParent = in.readByte() != 0;
    }

    public static final Creator<RecipientStudentData> CREATOR = new Creator<RecipientStudentData>() {
        @Override
        public RecipientStudentData createFromParcel(Parcel in) {
            return new RecipientStudentData(in);
        }

        @Override
        public RecipientStudentData[] newArray(int size) {
            return new RecipientStudentData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(stName);
        parcel.writeInt(stCode);
        parcel.writeString(acaCode);
        parcel.writeString(stInstall);
        parcel.writeString(parentInstall);
        parcel.writeString(stPhoneNumber);
        parcel.writeString(parentPhoneNumber);
//        parcel.writeByte((byte) (isCheckStudent ? 1 : 0));
//        parcel.writeByte((byte) (isCheckParent ? 1 : 0));
    }
}
