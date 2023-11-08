package kr.jeet.edu.manager.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import kr.jeet.edu.manager.model.data.PWData;

public class FindPWResponse extends BaseResponse{

    @SerializedName("data")
    @Expose
    public PWData data;
}
