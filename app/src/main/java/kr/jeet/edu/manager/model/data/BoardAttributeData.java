package kr.jeet.edu.manager.model.data;

import com.google.gson.annotations.SerializedName;

public class BoardAttributeData {
    @SerializedName("boardType")
    public String boardType;

    @SerializedName("boardNm")
    public String boardNm;

    @SerializedName("atchPosblFileNum")
    public int atchPosblFileNum;

    @SerializedName("commentAt")
    public String commentAt;

    @SerializedName("cntPerPage")
    public int cntPerPage;
}
