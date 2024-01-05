package kr.jeet.edu.manager.model.data;

import com.google.gson.annotations.SerializedName;

public class ACAData {
    @SerializedName("acaName")
    public String acaName = ""; // 캠퍼스 이름
    @SerializedName("acaCode")
    public String acaCode = ""; // 캠퍼스 코드
    @SerializedName("acaTel")
    public String acaTel = ""; // 캠퍼스 대표번호(문자발신번호로 사용)
    public ACAData(){}
    public ACAData(String code, String name, String acaTel) {
        this.acaName = name;
        this.acaCode = code;
        this.acaTel = acaTel;
    }
}
