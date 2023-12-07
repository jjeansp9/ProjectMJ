package kr.jeet.edu.manager.model.data;

import android.os.Parcel;
import android.os.Parcelable;

public class QnaData implements Parcelable {
    public int seq;             // 글의 고유 식별 번호
    public int writerSeq;       // 작성자의 고유 식별 번호
    public String writerNm;     // 작성자의 이름
    public int userGubun;       // 작성자의 유저 구분 (예: 관리자, 사용자 등)
    public int stCode;          // (부모앱) 원생 고유번호
    public String acaCode;      // 캠퍼스의 고유 코드
    public String acaName;      // 캠퍼스 이름
    public String acaGubunCode; // 캠퍼스 구분 코드
    public String acaGubunName; // 캠퍼스 구분 이름
    public String title;        // 글 제목
    public String content;      // 글 내용
    public String isOpen;       // 공개 여부(Y/N)
    public String isMain;       // 공지 여부(Y/N)
    public String state;        // 상태 (신청1/접수2/완료3)
    public int rdcnt;           // 조회수
    public String insertDate;   // 작성일

    public QnaData(){}
    protected QnaData(Parcel in) {
        seq = in.readInt();
        writerSeq = in.readInt();
        writerNm = in.readString();
        userGubun = in.readInt();
        stCode = in.readInt();
        acaCode = in.readString();
        acaName = in.readString();
        acaGubunCode = in.readString();
        acaGubunName = in.readString();
        title = in.readString();
        content = in.readString();
        isOpen = in.readString();
        isMain = in.readString();
        state = in.readString();
        rdcnt = in.readInt();
        insertDate = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(seq);
        dest.writeInt(writerSeq);
        dest.writeString(writerNm);
        dest.writeInt(userGubun);
        dest.writeInt(stCode);
        dest.writeString(acaCode);
        dest.writeString(acaName);
        dest.writeString(acaGubunCode);
        dest.writeString(acaGubunName);
        dest.writeString(title);
        dest.writeString(content);
        dest.writeString(isOpen);
        dest.writeString(isMain);
        dest.writeString(state);
        dest.writeInt(rdcnt);
        dest.writeString(insertDate);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<QnaData> CREATOR = new Creator<QnaData>() {
        @Override
        public QnaData createFromParcel(Parcel in) {
            return new QnaData(in);
        }

        @Override
        public QnaData[] newArray(int size) {
            return new QnaData[size];
        }
    };
}
