package kr.jeet.edu.manager.model.data;

import static kr.jeet.edu.manager.adapter.ReportCardShowType0Adapter.LAYOUT_HEADER;

import kr.jeet.edu.manager.activity.menu.reportcard.ShowReportCardActivity;

public class ReportCardExamHeaderData implements ShowReportCardActivity.ExamListTypeItem{
    private int esGubun;
    public String esTitle;

    public ReportCardExamHeaderData(int esGubun, String title) {
        this.esGubun = esGubun;
        this.esTitle = title;
    }
    @Override
    public int getEsGubun() {
        return esGubun;
    }

    @Override
    public int getType() {
        return LAYOUT_HEADER;
    }

    @Override
    public int compareTo(ShowReportCardActivity.ExamListTypeItem item) {
        int comparisonGubun = 0;
        comparisonGubun = this.esGubun - item.getEsGubun();
        if(comparisonGubun == 0) {
            return getType() - item.getType();
        }else{
            return comparisonGubun;
        }
    }
}
