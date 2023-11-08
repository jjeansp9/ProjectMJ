package kr.jeet.edu.manager.model.request;

import com.google.gson.annotations.SerializedName;

public class AttendanceRequest {
    @SerializedName("stCode")
    public String stCode;

    @SerializedName("stName")
    public String stName;

    @SerializedName("attendanceGubun")
    public String attendanceGubun;

    @SerializedName("attendanceDate")   //yyyyMMdd
    public String attendanceDate;

    @SerializedName("clsCode")
    public int clsCode;

    @SerializedName("clsName")
    public String clsName;

    @SerializedName("acaCode")
    public String acaCode;

    @SerializedName("acaName")
    public String acaName;

    @SerializedName("attendanceBigo")
    public String attendanceBigo;

    @SerializedName("isPushSend")
    public String isPushSend;

    //memberSeq
    @SerializedName("memberSeq")
    public int memberSeq;

    //parentPhoneNumber
    @SerializedName("parentPhoneNumber")
    public String parentPhoneNumber;

    //senderSMS
    @SerializedName("smsSender")
    public String smsSender;
}
