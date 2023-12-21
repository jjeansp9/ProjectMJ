package kr.jeet.edu.manager.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BoardNewResponse extends BaseResponse{
    @SerializedName("data")
    @Expose
    public List<Integer> data;
}
