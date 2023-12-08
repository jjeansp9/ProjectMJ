package kr.jeet.edu.manager.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import kr.jeet.edu.manager.model.data.QnaData;
import kr.jeet.edu.manager.model.data.QnaDetailData;

public class QnaDetailResponse extends BaseResponse{
    @SerializedName("data")
    @Expose
    public QnaDetailData data;
}
