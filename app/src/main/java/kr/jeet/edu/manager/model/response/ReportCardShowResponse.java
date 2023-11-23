package kr.jeet.edu.manager.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import kr.jeet.edu.manager.model.data.ReportCardShowData;

public class ReportCardShowResponse extends BaseResponse{
    @SerializedName("data")
    @Expose
    public ReportCardShowData data;
}
