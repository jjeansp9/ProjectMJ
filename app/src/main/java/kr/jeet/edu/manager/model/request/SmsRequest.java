package kr.jeet.edu.manager.model.request;

public class SmsRequest {

    public String msg;

    public String receiver;

    public String sender;

    public String senderCode; // 발신자 코드 (sfCode)

    public String receiverCode; // 수신자 코드 (stCode)
}
