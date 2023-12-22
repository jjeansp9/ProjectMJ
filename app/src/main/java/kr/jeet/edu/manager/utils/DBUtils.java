package kr.jeet.edu.manager.utils;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.DataManager;
import kr.jeet.edu.manager.db.JeetDatabase;
import kr.jeet.edu.manager.db.NewBoardDao;
import kr.jeet.edu.manager.db.NewBoardData;
import kr.jeet.edu.manager.model.data.ReadData;


public class DBUtils {

    private static final String TAG = "DBUtils";
    public interface onQueryCompletedListener {
        void onComplete();
    }
    // 읽은 게시글 데이터 insert
    public static void insertReadDB(Context context, ReadData readData, int memberSeq, String type) {
        new Thread(() -> {
            NewBoardDao jeetDBNewBoard = JeetDatabase.getInstance(context).newBoardDao();

            NewBoardData boardInfo = jeetDBNewBoard.getReadInfo(memberSeq, type, DataManager.getInstance().sevenDaysAgo, readData.getSeq()); // 읽은글

            String date = "";
            try {
                if (readData.getDate() != null && !readData.getDate().isEmpty()) date = readData.getDate();
                if (readData.getTime() != null && !readData.getTime().isEmpty()) date += " " + readData.getTime();
            }catch (Exception e) {}

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATE_FORMATTER_YYYY_MM_DD_HH_mm);
            LocalDateTime insertDate = LocalDateTime.parse(date, formatter);

            if (boardInfo == null) {
                if (DataManager.getInstance().sevenDaysAgo.isBefore(insertDate)) {
                    // 최근 7일 이내의 데이터인 경우
                    NewBoardData newBoardData = new NewBoardData(
                            type,
                            readData.getSeq(),
                            memberSeq,
                            readData.getIsRead(),
                            insertDate,
                            insertDate
                    );
                    jeetDBNewBoard.insert(newBoardData);
                    LogMgr.e(TAG, "dbTest Insert!");
                }
            }
        }).start();
    }

    // 게시글 목록에 읽은 게시글 setting
    public static void setReadDB(Context context, ArrayList<ReadData> boardList, int memberSeq, String type, onQueryCompletedListener listener) {
        new Thread(() -> {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATE_FORMATTER_YYYY_MM_DD_HH_mm);
                NewBoardDao jeetDBNewBoard = JeetDatabase.getInstance(context).newBoardDao();
                List<NewBoardData> getReadList = jeetDBNewBoard.getReadInfoList(memberSeq, type, DataManager.getInstance().sevenDaysAgo); // yyyyMM


                for (ReadData listData : boardList) {
                    String date = "";
                    try {
                        if (listData.getDate() != null && !listData.getDate().isEmpty())
                            date = listData.getDate();
                        if (listData.getTime() != null && !listData.getTime().isEmpty())
                            date += " " + listData.getTime();
                    } catch (Exception e) {
                    }

                    LocalDateTime insertDate = LocalDateTime.parse(date, formatter);

                    if (DataManager.getInstance().sevenDaysAgo.isBefore(insertDate)) { // 최근 7일 이내의 데이터인 경우
                        if(getReadList.stream().anyMatch(item -> item.type.equals(listData.getType()) && item.memberSeq == memberSeq && item.connSeq == listData.getSeq())) {
                            //db에 데이터가 있는 경우
                            listData.setIsRead(true);
                        }else {

                        }
                    } else {
                        listData.setIsRead(true);
                    }
                }
            }catch(Exception ex){}
            finally {
                listener.onComplete();
            }

        }).start();
    }
}
