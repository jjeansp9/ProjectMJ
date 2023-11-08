package kr.jeet.edu.manager.model.response;

import com.google.gson.annotations.SerializedName;

public class BaseResponse {

    @SerializedName("msg")
    public String msg;

    @Override
    public String toString() {
        return "BaseResponse{" +
                "msg='" + msg + '\'' +
                '}';
    }
}
