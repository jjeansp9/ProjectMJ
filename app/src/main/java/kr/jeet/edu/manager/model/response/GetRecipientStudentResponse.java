package kr.jeet.edu.manager.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import kr.jeet.edu.manager.model.data.RecipientStudentData;


public class GetRecipientStudentResponse extends BaseResponse{
    @SerializedName("data")
    @Expose
    public List<RecipientStudentData> data;
}
