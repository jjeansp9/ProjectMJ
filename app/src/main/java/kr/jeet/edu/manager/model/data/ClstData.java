package kr.jeet.edu.manager.model.data;

import com.google.gson.annotations.SerializedName;

public class ClstData {
    @SerializedName("clstName")
    public String clstName = ""; // 부서 이름
    @SerializedName("clstCode")
    public int clstCode = 0; // 부서 코드
    public ClstData(){}
    public ClstData(String name, int code) {
        this.clstName = name;
        this.clstCode = code;
    }
}
