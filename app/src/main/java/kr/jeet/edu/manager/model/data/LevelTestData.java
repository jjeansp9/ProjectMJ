package kr.jeet.edu.manager.model.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import kr.jeet.edu.manager.utils.LogMgr;

public class LevelTestData implements Parcelable {
    public int seq;                // 게시글 seq
    public int stCode;             // 원생 stCode
    public int memberSeq;          // member seq

    public String name;            // 이름
    public String sex;             // 성별 (남:1 여:2)
    public String address;         // 주소
    public String addressSub;      // 상세주소
    public String birth;           // 생년월일 (yyyy-MM-dd)
    public String grade;           // 학년 (중1, 고3…)
    public int scCode;             // 학교 코드
    public String phoneNumber;     // 학생 휴대폰 번호
    public String parentName;      // 학부모 이름
    public String parentPhoneNumber; // 학부모 휴대폰 번호
    public String cashReceiptNumber; // 현금 영수증 번호
    public String reason;          // 유입 경로
    public String bigo;            // 비고 (캠퍼스)
    public String bigoText;        // 비고 (캠퍼스 Text)
    public String reservationDate; // 예약일
    public String registerDate;    // 등록일

    public String time1;           // 시간1
    public String time2;           // 시간2
    public String time3;           // 시간3
    public String time4;           // 시간4
    public String date1;           // 기간1
    public String date2;           // 기간2
    public String date3;           // 기간3
    public String date4;           // 기간4
    public String process1;        // 프로세스1
    public String processEtc1;     // 프로세스1 기타
    public String processText1;     // 프로세스1 텍스트
    public String process2;        // 프로세스2
    public String processEtc2;     // 프로세스2 기타
    public String processText2;     // 프로세스2 텍스트
    public String process3;        // 프로세스3
    public String processEtc3;     // 프로세스3 기타
    public String processText3;     // 프로세스3 텍스트

    public String wish;            // 희망요일 (0: 월수금, 1: 화목토, 2: 실력 점검 후 결정, 3: 요일무관)
    public String study;           // 공부하고 싶은 분야 (응용^사고력…)
    public String highSchool;      // 희망 고등학교 (북과학고^외대부고…)
    public String gifted;          // 영재센터 입학 희망 (Y/N)
    public String etc;             // 궁금사항

    public String progressName1;   // 진도/심화학습여부(1) 학원명
    public int subjectCode;        // 과목코드
    public String subjectName;     // 과목명

    public String check1;          // 개인정보 동의 1 (Y/N )
    public String check2;          // 개인정보 동의 2 (Y/N)
    public String check3;          // 개인정보 동의 3 (Y/N)
    public String check4;          // 개인정보 동의 4 (마케팅 활용 동의 - Y/N)
    public String insertDate;

    protected LevelTestData(Parcel in) {
        seq = in.readInt();
        stCode = in.readInt();
        memberSeq = in.readInt();
        name = in.readString();
        sex = in.readString();
        address = in.readString();
        addressSub = in.readString();
        birth = in.readString();
        grade = in.readString();
        scCode = in.readInt();
        phoneNumber = in.readString();
        parentName = in.readString();
        parentPhoneNumber = in.readString();
        cashReceiptNumber = in.readString();
        reason = in.readString();
        bigo = in.readString();
        bigoText = in.readString();
        reservationDate = in.readString();
        wish = in.readString();
        time1 = in.readString();
        time2 = in.readString();
        time3 = in.readString();
        time4 = in.readString();
        date1 = in.readString();
        date2 = in.readString();
        date3 = in.readString();
        date4 = in.readString();
        process1 = in.readString();
        process2 = in.readString();
        process3 = in.readString();
        processEtc1 = in.readString();
        processEtc2 = in.readString();
        processEtc3 = in.readString();
        processText1 = in.readString();
        processText2 = in.readString();
        processText3 = in.readString();
        study = in.readString();
        highSchool = in.readString();
        gifted = in.readString();
        etc = in.readString();
        progressName1 = in.readString();
        subjectCode = in.readInt();
        subjectName = in.readString();
        check1 = in.readString();
        check2 = in.readString();
        check3 = in.readString();
        check4 = in.readString();
        registerDate = in.readString();
        insertDate = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(seq);
        dest.writeInt(stCode);
        dest.writeInt(memberSeq);
        dest.writeString(name);
        dest.writeString(sex);
        dest.writeString(address);
        dest.writeString(addressSub);
        dest.writeString(birth);
        dest.writeString(grade);
        dest.writeInt(scCode);
        dest.writeString(phoneNumber);
        dest.writeString(parentName);
        dest.writeString(parentPhoneNumber);
        dest.writeString(cashReceiptNumber);
        dest.writeString(reason);
        dest.writeString(bigo);
        dest.writeString(bigoText);
        dest.writeString(reservationDate);
        dest.writeString(wish);
        dest.writeString(time1);
        dest.writeString(time2);
        dest.writeString(time3);
        dest.writeString(time4);
        dest.writeString(date1);
        dest.writeString(date2);
        dest.writeString(date3);
        dest.writeString(date4);
        dest.writeString(process1);
        dest.writeString(process2);
        dest.writeString(process3);
        dest.writeString(processEtc1);
        dest.writeString(processEtc2);
        dest.writeString(processEtc3);
        dest.writeString(processText1);
        dest.writeString(processText2);
        dest.writeString(processText3);
        dest.writeString(study);
        dest.writeString(highSchool);
        dest.writeString(gifted);
        dest.writeString(etc);
        dest.writeString(progressName1);
        dest.writeInt(subjectCode);
        dest.writeString(subjectName);
        dest.writeString(check1);
        dest.writeString(check2);
        dest.writeString(check3);
        dest.writeString(check4);
        dest.writeString(registerDate);
        dest.writeString(insertDate);
    }

    public static final Creator<LevelTestData> CREATOR = new Creator<LevelTestData>() {
        @Override
        public LevelTestData createFromParcel(Parcel in) {
            return new LevelTestData(in);
        }

        @Override
        public LevelTestData[] newArray(int size) {
            return new LevelTestData[size];
        }
    };

    @Override
    public boolean equals(@Nullable Object obj) {
        if((obj instanceof  LevelTestData) == false) return false;
        LevelTestData data = (LevelTestData) obj;
        boolean result =  this.seq == data.seq &&
                this.memberSeq == data.memberSeq &&
                ((TextUtils.isEmpty(this.name) && TextUtils.isEmpty(data.name)) || this.name.equals(data.name)) &&
                ((TextUtils.isEmpty(this.insertDate) && TextUtils.isEmpty(data.insertDate)) || this.insertDate.equals(data.insertDate));
        return result;
    }
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + seq;
        result = 31 * result + memberSeq;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (insertDate != null ? insertDate.hashCode() : 0);
        return result;
    }
}
