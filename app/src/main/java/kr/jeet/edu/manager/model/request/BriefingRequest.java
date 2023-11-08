package kr.jeet.edu.manager.model.request;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.model.data.BriefingData;
import kr.jeet.edu.manager.model.data.RecipientData;

public class BriefingRequest {
    //seq
    public Integer seq = null;
    //title
    public String title;
    //content
    public String content;
    //acaCode
    public String acaCode;
    //acaName
    public String acaName;
    //acaCode
    public String acaGubunCode;
    //acaName
    public String acaGubunName;
    //memberSeq
    public int writerSeq;
    //date
    public int year;
    public int month;
    public int day;
    //time format hh:mm
    public String ptTime;
    //place
    public String place;
    //member count
    public int participantsCnt;
    public String isSendSMS;
    //recipient List
//    public List<RecipientData> receiverList;  //수신인 등록 분리 230824
    public String fileId;
    //fileDeleteSeqList
    public List<Integer> fileDeleteSeqList;
    public static BriefingRequest initNewDataFromBriefingData(BriefingData data, int writerSeq, Set<RecipientData> recipientDataList) {
        BriefingRequest request = new BriefingRequest();
        if(data == null) return request;
        if(data.seq != 0) {
            request.seq = data.seq;
        }
        request.title = data.title;
        request.content = data.content;
        request.acaCode = data.acaCode;
        request.acaName = data.acaName;
        request.acaGubunCode = data.acaGubunCode;
        request.acaGubunName = data.acaGubunName;
        request.writerSeq = writerSeq;
        SimpleDateFormat inputDateFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYY_MM_DD);
        Date insertDate = new Date();
        try {
            insertDate = inputDateFormat.parse(data.date);
            Calendar cal = Calendar.getInstance();
            cal.setTime(insertDate);
            request.year = cal.get(Calendar.YEAR);
            request.month = cal.get(Calendar.MONTH) + 1;
            request.day = cal.get(Calendar.DAY_OF_MONTH);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        request.ptTime = data.ptTime;
        request.participantsCnt = data.participantsCnt;
        request.place = data.place;
        request.isSendSMS = data.isSendSMS;
//        request.receiverList = recipientDataList;
        return request;
    }
    public static BriefingRequest initUpdateDataFromBriefingData(BriefingData data, List<Integer> deleteSeqList) {
        BriefingRequest request = new BriefingRequest();
        if(data == null) return request;
        if(data.seq != 0) {
            request.seq = data.seq;
        }
        request.title = data.title;
        request.content = data.content;
        request.acaCode = data.acaCode;
        request.acaName = data.acaName;
        SimpleDateFormat inputDateFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYY_MM_DD);
        Date insertDate = new Date();
        try {
            insertDate = inputDateFormat.parse(data.date);
            Calendar cal = Calendar.getInstance();
            cal.setTime(insertDate);
            request.year = cal.get(Calendar.YEAR);
            request.month = cal.get(Calendar.MONTH) + 1;
            request.day = cal.get(Calendar.DAY_OF_MONTH);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        request.ptTime = data.ptTime;
        request.participantsCnt = data.participantsCnt;
        request.place = data.place;
        request.isSendSMS = data.isSendSMS;
        request.fileId = data.fileId;
        request.fileDeleteSeqList = deleteSeqList;

        return request;
    }
}
