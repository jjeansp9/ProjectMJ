package kr.jeet.edu.manager.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ScheduleRegisterResponse extends BaseResponse{
    @SerializedName("data")
    @Expose
    public ArrayList<Integer> data;
}
