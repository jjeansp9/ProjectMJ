package kr.jeet.edu.manager.model.data;

import com.google.gson.annotations.SerializedName;

public class CounselData {

    @SerializedName("seq")
    public int seq;
    @SerializedName("userGubun")
    public int userGubun;
    @SerializedName("writerSeq")
    public int writerSeq;
    @SerializedName("writerName")
    public String writerName;

    @SerializedName("memberSeq")
    public int memberSeq;

    @SerializedName("memberName")
    public String memberName;

    @SerializedName("counselDate")
    public String counselDate;

    @SerializedName("acaCode")
    public String acaCode;

    @SerializedName("acaName")
    public String acaName;

    @SerializedName("sfCode")
    public int sfCode;

    @SerializedName("sfName")
    public String sfName;

    @SerializedName("memo")
    public String memo;

    @SerializedName("clsName")
    public String clsName;

    @SerializedName("phoneNumber")
    public String phoneNumber;

    @SerializedName("managerPhoneNumber")
    public String managerPhoneNumber;

    @SerializedName("smsSender")
    public String smsSender;

    @SerializedName("isSendNoti")
    public String isSendNoti;

    @SerializedName("insertDate")
    public String insertDate;

    @SerializedName("updateDate")
    public String updateDate;
}
