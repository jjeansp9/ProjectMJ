package kr.jeet.edu.manager.model.data;

import com.google.gson.annotations.SerializedName;

public class StudentGradeData {
    @SerializedName("gubunCode")
    public String gubunCode; // 학교 코드
    @SerializedName("gubunName")
    public String gubunName; // 학교 이름
    public StudentGradeData(String code, String name) {
        this.gubunCode = code;
        this.gubunName = name;
    }
}
