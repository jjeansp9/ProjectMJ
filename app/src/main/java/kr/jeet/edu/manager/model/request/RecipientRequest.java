package kr.jeet.edu.manager.model.request;

import java.util.ArrayList;
import java.util.List;

import kr.jeet.edu.manager.model.data.RecipientData;

public class RecipientRequest {
    public int seq;
    public ArrayList<Integer> seqs;
    public int sfCode;
    public String smsSender;
    public List<RecipientData> receiverList;
}
