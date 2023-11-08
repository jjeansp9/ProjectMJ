package kr.jeet.edu.manager.db;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeFormatterBuilder;
import org.threeten.bp.temporal.ChronoField;

import java.util.Date;
import java.util.Map;

@Entity(tableName = "tbl_push_message")
public class PushMessage implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    public long id;
    @ColumnInfo(name="title")
    public String title;
    @ColumnInfo(name="body")
    public String body;
    @ColumnInfo(name="acaCode")
    public String acaCode;
    @ColumnInfo(name="date")
//    public String date;
    public LocalDateTime date;

    @ColumnInfo(name="pushType")
    public String pushType;
    @ColumnInfo(name="connSeq")
    public int connSeq;
    @ColumnInfo(name="pushId")
    public String pushId;
    @ColumnInfo(name="isRead", defaultValue = "false")
    public boolean isRead = false;
    public PushMessage() {
    }
    public PushMessage(long id, String title, String body, String acaCode, String date, String pushType,int connSeq, String pushId, boolean isRead) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.acaCode = acaCode;
        if(TextUtils.isEmpty(date)){
            this.date = LocalDateTime.now();
        }else {
            this.date = LocalDateTime.parse(date, dateTimeFormatter);
        }
        this.pushType = pushType;
        this.connSeq = connSeq;
        this.pushId = pushId;
        this.isRead = isRead;
    }

    protected PushMessage(Parcel in) {
        readFromParcel(in);
    }
    private static final DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm:ss")
            .optionalStart()
            .appendFraction(ChronoField.MILLI_OF_SECOND, 0, 3, true)
            .optionalEnd()
            .toFormatter();
    public static final Creator<PushMessage> CREATOR = new Creator<PushMessage>() {

        @Override
        public PushMessage createFromParcel(Parcel in) {
            return new PushMessage(in);
        }

        @Override
        public PushMessage[] newArray(int size) {
            return new PushMessage[size];
        }
    };

    public static PushMessage buildFromMap(Map<String, String> map) {
        long id = 0L;
        String title = map.containsKey("title")? map.get("title") : "";
        String content = map.containsKey("body")? map.get("body") : "";
        String acaCode = map.containsKey("acaCode")? map.get("acaCode") : "";
        String date = map.containsKey("date")? map.get("date") : "";
        int connSeq = -1;
        String connSeqStr = map.containsKey("connSeq")? map.get("connSeq") : "";
        try{
            connSeq = Integer.parseInt(connSeqStr);
        }catch(Exception ex){}
        String pushType = map.containsKey("pushType")? map.get("pushType") : "";
        String pushId = map.containsKey("pushId")? map.get("pushId") : "";
        return new PushMessage(id, title, content, acaCode, date, pushType, connSeq, pushId, false);
    }

    @Override
    public int describeContents() {
        return 0;
    }
    public void readFromParcel(Parcel in) {
        id = in.readLong();
        title = in.readString();
        body = in.readString();
        acaCode = in.readString();
        date = LocalDateTime.parse(in.readString(), dateTimeFormatter);
        pushType = in.readString();
        connSeq = in.readInt();
        pushId = in.readString();
        isRead = in.readByte() != 0;
    }
    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(title);
        parcel.writeString(body);
        parcel.writeString(acaCode);
        parcel.writeString(date.format(dateTimeFormatter));
        parcel.writeString(pushType);
        parcel.writeInt(connSeq);
        parcel.writeString(pushId);
        parcel.writeByte((byte) (isRead ? 1 : 0));
    }
}
