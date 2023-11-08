package kr.jeet.edu.manager.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import kr.jeet.edu.manager.model.data.HolidayData;
import kr.jeet.edu.manager.model.data.ScheduleData;

public class ScheduleListResponse extends BaseResponse{
    @SerializedName("data")
    @Expose
    public Data data;

    public static class Data{
        @SerializedName("scheduleList")
        public List<ScheduleData> scheduleList;

        @SerializedName("holidayList")
        public List<HolidayData> holidayList;
    }
}
