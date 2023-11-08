package kr.jeet.edu.manager.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import kr.jeet.edu.manager.model.data.IDData;
import kr.jeet.edu.manager.model.data.LoginData;

public class FindIDResponse extends BaseResponse {

    @SerializedName("data")
    @Expose
    public IDData data;
}

