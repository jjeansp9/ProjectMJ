package kr.jeet.edu.manager.model.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class IDData {
    @SerializedName("id")
    @Expose
    public String id;
    @SerializedName("snsType")
    @Expose
    public String snsType;
}
