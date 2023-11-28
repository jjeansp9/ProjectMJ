package kr.jeet.edu.manager.model.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import kr.jeet.edu.manager.activity.menu.leveltest.LevelTestSettingActivity;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.utils.LogMgr;

public class LevelTestTimeData implements LevelTestSettingActivity.TimeListItem {
    public int seq;
    public int memberSeq;
    public int iGrade = 0;
    public String grade = "";
    public int weekend = -1;
    public String time = "";
    public Date testTime;
    public String insertDate;
    public String updateDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.TIME_FORMATTER_HH_MM);
    @Override
    public boolean isHeader() {
        return false;
    }

    @Override
    public Constants.SchoolGradeType getGrade() {
        return Constants.SchoolGradeType.getByName(grade);
    }
    public Constants.WeekdayType getWeekEnd() {
        return Constants.WeekdayType.getByCode(weekend);
    }

    @Override
    public int compareTo(LevelTestSettingActivity.TimeListItem timeListItem) {

        int gradeComparison = 0;
        Constants.SchoolGradeType type = Constants.SchoolGradeType.getByName(grade);
        Constants.SchoolGradeType itemType = null;
        if(timeListItem instanceof LevelTestTimeHeaderData) {
            itemType = Constants.SchoolGradeType.getByName(((LevelTestTimeHeaderData)timeListItem).grade);
            LogMgr.e("compare", "grade : " + grade + " / new grade : " + ((LevelTestTimeHeaderData)timeListItem).grade);
        }else if(timeListItem instanceof LevelTestTimeData) {
            itemType = Constants.SchoolGradeType.getByName(((LevelTestTimeData)timeListItem).grade);
            LogMgr.e("compare", "grade : " + grade + " / new grade : " + ((LevelTestTimeData)timeListItem).grade);
        }

        if (type != null&& itemType != null) {
            gradeComparison = type.getCode() - itemType.getCode();
        }
        LogMgr.e("compare", "grade Comparison : " + gradeComparison);
        if(gradeComparison != 0) {
            return gradeComparison;
        }else{
            if (this.isHeader() && !timeListItem.isHeader()) {
                LogMgr.e("compare", "if (this.isHeader() && !timeListItem.isHeader())");
                return -1;
            } else if (!this.isHeader() && timeListItem.isHeader()) {
                LogMgr.e("compare", "if (!this.isHeader() && timeListItem.isHeader())");
                return 1;
            } else {
                // isHeader 값이 동일한 경우
                // weekend 값을 비교
                if(timeListItem instanceof LevelTestTimeData) {
                    LevelTestTimeData data = (LevelTestTimeData) timeListItem;
                    int weekendComparison = Integer.compare(this.weekend, data.weekend);

                    if (weekendComparison != 0) {
                        LogMgr.e("compare", "return weekendComparison " + weekendComparison);
                        return weekendComparison; // weekend 값이 다른 경우 반환
                    } else {
                        // weekend 값이 동일한 경우
                        // testTime 값을 비교
                        try {
                            this.testTime = dateFormat.parse(this.time);
                            data.testTime = dateFormat.parse(data.time);
                            LogMgr.e("compare", "testTime.compareTo(data.testTime)" + testTime.compareTo(data.testTime));
                            return this.testTime.compareTo(data.testTime);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        LogMgr.e("compare", "return 0");
                        return 0;
                    }
                }
                LogMgr.e("compare", "return 0");
                return 0;
            }
        }
    }
}
