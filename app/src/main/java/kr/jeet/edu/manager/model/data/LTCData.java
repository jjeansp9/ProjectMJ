package kr.jeet.edu.manager.model.data;

import com.google.gson.annotations.SerializedName;

public class LTCData {
    @SerializedName("ltcName")
    public String ltcName; // 캠퍼스 이름(테스트예약)
    @SerializedName("ltcCode")
    public String ltcCode; // 캠퍼스 코드(테스트예약)
    public LTCData(String code, String name) {
        this.ltcCode = code;
        this.ltcName = name;
    }
}
