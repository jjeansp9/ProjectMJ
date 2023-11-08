package kr.jeet.edu.manager.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BoardRegisterResponse extends BaseResponse{
    @SerializedName("data")
    @Expose
    public int data;
}
