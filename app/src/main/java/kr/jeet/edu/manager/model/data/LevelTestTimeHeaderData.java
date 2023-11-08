package kr.jeet.edu.manager.model.data;

import java.text.ParseException;

import kr.jeet.edu.manager.activity.LevelTestSettingActivity;
import kr.jeet.edu.manager.common.Constants;

public class LevelTestTimeHeaderData implements LevelTestSettingActivity.TimeListItem {
    public int iGrade = 0;
    public String grade = "";
    public String title = "";
    public LevelTestTimeHeaderData(String grade) {
        this.grade = grade;
        this.title = grade + "";
    }
    @Override
    public boolean isHeader() {
        return true;
    }

    @Override
    public Constants.SchoolGradeType getGrade() {
        return Constants.SchoolGradeType.getByName(grade);
    }
    @Override
    public int compareTo(LevelTestSettingActivity.TimeListItem timeListItem) {
        int gradeComparison = 0;
        Constants.SchoolGradeType type = Constants.SchoolGradeType.getByName(grade);
        Constants.SchoolGradeType itemType = null;
        if(timeListItem instanceof LevelTestTimeHeaderData) {
            itemType = Constants.SchoolGradeType.getByName(((LevelTestTimeHeaderData)timeListItem).grade);
        }else if(timeListItem instanceof LevelTestTimeData) {
            itemType = Constants.SchoolGradeType.getByName(((LevelTestTimeData)timeListItem).grade);
        }
        if (type != null&& itemType != null) {
            gradeComparison = type.getCode() - itemType.getCode();
        }
        if(gradeComparison != 0) {
            return gradeComparison;
        }else {
            if (this.isHeader() && !timeListItem.isHeader()) {
                return -1;
            } else if (!this.isHeader() && timeListItem.isHeader()) {
                return 1;
            }
        }
        return 0;
    }
}
