package kr.jeet.edu.manager.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import kr.jeet.edu.manager.model.data.CounselData;

public class CounselDetailResponse {
    @SerializedName("data")
    @Expose
    public CounselData data;
}
