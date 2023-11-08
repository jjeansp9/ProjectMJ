package kr.jeet.edu.manager.model.data;

import com.google.gson.annotations.SerializedName;

public class DepartmentData {
    @SerializedName("deptName")
    public String deptName = ""; // 부서 이름
    @SerializedName("deptCode")
    public int deptCode = 0; // 부서 코드
    public DepartmentData(){}
    public DepartmentData(String name, int code) {
        this.deptName = name;
        this.deptCode = code;
    }
}
