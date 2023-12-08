package kr.jeet.edu.manager.model.data;

import android.os.Parcel;
import android.os.Parcelable;

public class QnaDetailData extends QnaData {
    public String reply;        //답변 내용
    public String replyMberSeq; //답변자 SEQ
    public String replyMberNm;  //답변자명
    public int sfCode;       //답변자 고유번호
    public String isSubAdmin;   //중간관리자여부
    public String isReplyAdmin; //답변관리자여부
    public QnaDetailData(){}
    protected QnaDetailData(Parcel in) {
        super(in);
        reply = in.readString();
        replyMberSeq = in.readString();
        replyMberNm = in.readString();
        sfCode = in.readInt();
        isSubAdmin = in.readString();
        isReplyAdmin = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(reply);
        dest.writeString(replyMberSeq);
        dest.writeString(replyMberNm);
        dest.writeInt(sfCode);
        dest.writeString(isSubAdmin);
        dest.writeString(isReplyAdmin);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<QnaDetailData> CREATOR = new Creator<QnaDetailData>() {
        @Override
        public QnaDetailData createFromParcel(Parcel in) {
            return new QnaDetailData(in);
        }

        @Override
        public QnaDetailData[] newArray(int size) {
            return new QnaDetailData[size];
        }
    };
}
