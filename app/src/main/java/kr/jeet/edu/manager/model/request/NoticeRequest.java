package kr.jeet.edu.manager.model.request;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.model.data.BriefingData;
import kr.jeet.edu.manager.model.data.NoticeData;
import kr.jeet.edu.manager.model.data.RecipientData;

public class NoticeRequest {
    //seq
    public int writerSeq;
    //title
    public String title;
    //content
    public String content;
    public String isSendSMS;
    public int receiverCnt;
    //acaCode
    public String acaCode;
    //acaName
    public String acaName;
    //acaCode
    public String acaGubunCode;
    //acaName
    public String acaGubunName;

    public static NoticeRequest initNewData(int writerSeq, String title, String content, boolean isSendSMS, int receiverCnt, String acaCode, String acaName, String acaGubunCode, String acagubunName) {
        NoticeRequest request = new NoticeRequest();
        request.writerSeq = writerSeq;
        request.title = title;
        request.content = content;
        request.isSendSMS = isSendSMS? "Y" : "N";
        request.receiverCnt = receiverCnt;
        request.acaCode = acaCode;
        request.acaName = acaName;
        request.acaGubunCode = acaGubunCode;
        request.acaGubunName = acagubunName;
        return request;
    }

}