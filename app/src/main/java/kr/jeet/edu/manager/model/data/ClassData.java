package kr.jeet.edu.manager.model.data;

import com.google.gson.annotations.SerializedName;

public class ClassData {
    @SerializedName("clsName")
    public String clsName = ""; // 학급 이름
    @SerializedName("clsCode")
    public int clsCode;// 학급 코드
    public ClassData(){}
    public ClassData(String name, int code) {
        this.clsName = name;
        this.clsCode = code;
    }
}
