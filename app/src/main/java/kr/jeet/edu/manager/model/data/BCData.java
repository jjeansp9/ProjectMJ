package kr.jeet.edu.manager.model.data;

import com.google.gson.annotations.SerializedName;

public class BCData {
    @SerializedName("bcName")
    public String bcName = ""; // 캠퍼스 이름
    @SerializedName("bcCode")
    public int bcCode;// 캠퍼스 코드
    public BCData(){}
    public BCData(String name, int code) {
        this.bcName = name;
        this.bcCode = code;
    }
}
