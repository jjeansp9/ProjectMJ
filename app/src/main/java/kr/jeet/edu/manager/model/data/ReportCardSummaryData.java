package kr.jeet.edu.manager.model.data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class ReportCardSummaryData implements Parcelable {

    public String progress;
    public String title;
    public int etCode;

    protected ReportCardSummaryData(Parcel in) {
        progress = in.readString();
        title = in.readString();
        etCode = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(progress);
        dest.writeString(title);
        dest.writeInt(etCode);
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
