package kr.jeet.edu.manager.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import kr.jeet.edu.manager.model.data.LevelTestTimeData;

public class LevelTestTimeResponse extends BaseResponse{
    @SerializedName("data")
    @Expose
    public LevelTestTimeData data;
}

