package kr.jeet.edu.manager.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.threeten.bp.LocalDateTime;

@Entity(tableName = "tbl_new_board")
public class NewBoardData {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "connSeq")
    public int connSeq;

    @ColumnInfo(name = "memberSeq")
    public int memberSeq;

    @ColumnInfo(name = "isRead")
    public boolean isRead;

    @ColumnInfo(name = "insertDate")
    public LocalDateTime insertDate;

    @ColumnInfo(name = "updateDate")
    public LocalDateTime updateDate;

    public NewBoardData() {}

    public NewBoardData(String type, int connSeq, int memberSeq, boolean isRead, LocalDateTime insertDate, LocalDateTime updateDate) {
        this.type = type;
        this.connSeq = connSeq;
        this.memberSeq = memberSeq;
        this.isRead = isRead;
        this.insertDate = insertDate;
        this.updateDate = updateDate;
    }
}
