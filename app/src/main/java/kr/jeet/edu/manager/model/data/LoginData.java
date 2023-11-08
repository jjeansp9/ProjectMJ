package kr.jeet.edu.manager.model.data;

import com.google.gson.annotations.SerializedName;

public class LoginData {

    @SerializedName("seq")
    public int seq;

    @SerializedName("userGubun")
    public int userGubun;

    @SerializedName("stCode")
    public int stCode;

    @SerializedName("sfCode")
    public int sfCode;

    @SerializedName("pushStatus")
    public StudentInfo.PushStatus pushStatus;
}
