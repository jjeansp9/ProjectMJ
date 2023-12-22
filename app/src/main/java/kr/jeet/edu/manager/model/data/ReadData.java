package kr.jeet.edu.manager.model.data;

public interface ReadData {
    String getDate();
    String getTime();
    int getSeq();
    boolean getIsRead();
    default String getType() {
        return "";
    }

    void setDate(String date);
    void setTime(String time);
    void setSeq(int seq);
    void setIsRead(boolean isRead);
}
