package kr.jeet.edu.manager.model.request;

import java.util.ArrayList;
import java.util.List;

import kr.jeet.edu.manager.model.data.ReportCardData;

public class UpdateReportCardRequest {
    int writerSeq;  //작성자 seq
    String writerName;  //작성자명
    int sfCode; //작성자 고유번호
    int stCode; //원생 고유번호
    String stName;  //원생명
    String content; //내용
    String acaCode; //캠퍼스코드
    String acaName; //캠퍼스명
    String isSendSMS;   //문자발송여부
    String smsSender;   //캠퍼스 대표번호
    String parentPhoneNumber;   //부모핸드폰번호
    List<ReportCardData> reportList;    //성적표리스트
    public UpdateReportCardRequest initRequest(
            int writerSeq,
            String writerName,
            int sfCode,
            int stCode,
            String stName,
            String content,
            String acaCode,
            String acaName,
            String isSendSMS,
            String smsSender,
            String parentPhoneNumber,
            List<ReportCardData> list
    ) {
        this.writerSeq = writerSeq;
        this.writerName = writerName;
        this.sfCode = sfCode;
        this.stCode = stCode;
        this.stName = stName;
        this.content = content;
        this.acaCode = acaCode;
        this.acaName = acaName;
        this.isSendSMS = isSendSMS;
        this.smsSender = smsSender;
        this.parentPhoneNumber = parentPhoneNumber;
        this.reportList = new ArrayList<>(list);
        return this;
    }
}
