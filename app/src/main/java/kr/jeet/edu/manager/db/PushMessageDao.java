package kr.jeet.edu.manager.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PushMessageDao {
    @Query("SELECT * FROM tbl_push_message ORDER BY id DESC")
    List<PushMessage> getAllMessage();

    @Query("SELECT * FROM tbl_push_message WHERE id =:id ORDER BY id DESC")
    List<PushMessage> getMessageById(int id);

    @Query("SELECT * FROM tbl_push_message WHERE isRead =:isread ORDER BY id DESC")
    List<PushMessage> getMessageByReadFlag(boolean isread);

    @Query("SELECT * FROM tbl_push_message WHERE isRead =:isread AND pushType=:type ORDER BY id DESC")
    List<PushMessage> getMessageByReadFlagNType(boolean isread, String type);

    @Query("SELECT * FROM tbl_push_message WHERE connSeq=:connSeq AND pushType=:type ORDER BY id DESC")
    List<PushMessage> getMessageBySeqNType(int connSeq, String type);

    @Query("SELECT * FROM tbl_push_message WHERE strftime('%Y', date) = :year AND strftime('%m', date) = :month AND pushType=:type ORDER BY id DESC")
    List<PushMessage> getMessageTypeAtTime(String type, String year, String month);
    @Update
    void update(PushMessage... message);

    @Insert
    void insertAll(PushMessage... message);

    @Delete
    void delete(PushMessage message);
}
