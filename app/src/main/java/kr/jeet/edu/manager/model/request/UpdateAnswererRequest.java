package kr.jeet.edu.manager.model.request;

import java.util.ArrayList;
import java.util.List;

import kr.jeet.edu.manager.model.data.AnswererData;
import kr.jeet.edu.manager.model.data.ReportCardData;

public class UpdateAnswererRequest {
    public int qnaSeq;
    public List<AnswererData> list;

    public UpdateAnswererRequest initRequest(
            int seq,
            List<AnswererData> list
    ) {
        this.qnaSeq = seq;
        this.list = new ArrayList<>(list);
        return this;
    }
}
