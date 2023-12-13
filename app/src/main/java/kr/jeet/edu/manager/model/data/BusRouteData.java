package kr.jeet.edu.manager.model.data;

public class BusRouteData {
    public String bpCode; // 노선 코드
    public String bpName; // 노선 이름
    public String isArrive; // 도착여부
    public boolean isAtThisStop = false;
    public boolean isSuccess = false; // 서버 통신 성공여부
    public String startDate;
    public BusRouteData(){}
    public BusRouteData(String name, String isArrive, boolean isAtThisStop) {
        this.bpName = name;
        this.isArrive = isArrive;
        this.isAtThisStop = isAtThisStop;
    }
}
