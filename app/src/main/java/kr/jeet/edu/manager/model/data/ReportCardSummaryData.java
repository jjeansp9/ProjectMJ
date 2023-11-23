package kr.jeet.edu.manager.model.data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.List;

public class ReportCardSummaryData implements Parcelable {
    public int seq; //seq
    public String content;  //content
    public String stName;   //원생명
    public int writerSeq;   //작성자Seq
    public String writerName;   //작성자명
    public String acaName;   //캠퍼스명
    public String insertDate;
    public List<ReportCardData> reportList;

    protected ReportCardSummaryData(Parcel in) {
        seq = in.readInt();
        content = in.readString();
        stName = in.readString();
        writerName = in.readString();
        acaName = in.readString();
        insertDate = in.readString();
        reportList = in.createTypedArrayList(ReportCardData.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(seq);
        dest.writeString(content);
        dest.writeString(stName);
        dest.writeString(writerName);
        dest.writeString(acaName);
        dest.writeString(insertDate);
        dest.writeTypedList(reportList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ReportCardSummaryData> CREATOR = new Creator<ReportCardSummaryData>() {
        @Override
        public ReportCardSummaryData createFromParcel(Parcel in) {
            return new ReportCardSummaryData(in);
        }

        @Override
        public ReportCardSummaryData[] newArray(int size) {
            return new ReportCardSummaryData[size];
        }
    };
}
