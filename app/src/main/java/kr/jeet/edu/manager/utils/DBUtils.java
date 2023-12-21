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

            LocalDateTime today = LocalDateTime.now(); // 현재날짜
            LocalDateTime sevenDaysAgo = today.minusDays(Constants.IS_READ_DELETE_DAY); // 현재 날짜에서 7일을 뺀 날짜
            NewBoardData boardInfo = jeetDBNewBoard.getReadInfo(memberSeq, type, sevenDaysAgo, readData.getSeq()); // 읽은글

            String date = "";
            try {
                if (readData.getDate() != null && !readData.getDate().isEmpty()) date = readData.getDate();
                if (readData.getTime() != null && !readData.getTime().isEmpty()) date += " " + readData.getTime();
            }catch (Exception e) {}

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATE_FORMATTER_YYYY_MM_DD_HH_mm);
            LocalDateTime insertDate = LocalDateTime.parse(date, formatter);

            if (boardInfo == null) {
                if (sevenDaysAgo.isBefore(insertDate)) {
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
                LocalDateTime today = LocalDateTime.now(); // 현재날짜
                LocalDateTime sevenDaysAgo = today.minusDays(Constants.IS_READ_DELETE_DAY); // 현재 날짜에서 7일을 뺀 날짜
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATE_FORMATTER_YYYY_MM_DD_HH_mm);

                NewBoardDao jeetDBNewBoard = JeetDatabase.getInstance(context).newBoardDao();
//                List<NewBoardData> getReadList = jeetDBNewBoard.getReadInfoList(memberSeq, type, sevenDaysAgo); // yyyyMM
                List<NewBoardData> getReadList = jeetDBNewBoard.getReadInfoList(memberSeq, type); // yyyyMM

                HashSet<String> getAfterKeyList = new HashSet<>();

                LogMgr.e(TAG, "getReadList size: " + getReadList.size());

                for (NewBoardData boardData : getReadList) {
                    String key = boardData.type + "," + boardData.connSeq + "," + boardData.memberSeq;
                    getAfterKeyList.add(key);
                }

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

//                    if (sevenDaysAgo.isBefore(insertDate)) { // 최근 7일 이내의 데이터인 경우
//                        if (!getReadList.isEmpty()) {
//                            if (sevenDaysAgo.isBefore(insertDate)) {
//                                String key = type + "," + listData.getSeq() + "," + memberSeq;
//                                if (getAfterKeyList.contains(key)) listData.setIsRead(true);
//
//                            } else { // 최근 7일이 지난 데이터인 경우
//                                for (NewBoardData dbData : getReadList)
//                                    jeetDBNewBoard.delete(memberSeq, type, sevenDaysAgo, listData.getSeq());
//                                listData.setIsRead(true);
//                            }
//                        }
//                    } else {
//                        listData.setIsRead(true);
//                    }
                    if (sevenDaysAgo.isBefore(insertDate)) { // 최근 7일 이내의 데이터인 경우
                        String key = type + "," + listData.getSeq() + "," + memberSeq;
                        if (getAfterKeyList.contains(key)) listData.setIsRead(true);

                    } else {
                        for (NewBoardData dbData : getReadList)
                            jeetDBNewBoard.delete(memberSeq, type, sevenDaysAgo, listData.getSeq());
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
