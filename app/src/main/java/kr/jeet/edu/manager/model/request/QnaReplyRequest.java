package kr.jeet.edu.manager.model.request;

import java.util.ArrayList;
import java.util.List;

import kr.jeet.edu.manager.model.data.AnswererData;

public class QnaReplyRequest implements BaseRequest {
    public int qnaSeq;
    public String reply;
    public int replyMemberSeq;
    public String replyMemberNm;
    public int sfCode;
    public QnaReplyRequest(
            int seq,
            String reply,
            int memberSeq,
            String memberNm,
            int sfCode
    ) {
        this.qnaSeq = seq;
        this.reply = reply;
        this.replyMemberSeq = memberSeq;
        this.replyMemberNm = memberNm;
        this.sfCode = sfCode;
    }
}
