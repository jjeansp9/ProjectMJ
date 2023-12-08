package kr.jeet.edu.manager.model.request;

public class QnaAddRequest implements BaseRequest{
    public int writerSeq;       // 작성자의 고유 식별 번호
    public String writerNm;     // 작성자의 이름
    public int userGubun;       // 작성자의 유저 구분 (예: 관리자, 사용자 등)
    public String acaCode;      // 캠퍼스의 고유 코드
    public String acaName;      // 캠퍼스 이름
    public String acaGubunCode; // 캠퍼스 구분 코드
    public String acaGubunName; // 캠퍼스 구분 이름
    public String title;        // 글 제목
    public String content;      // 글 내용
    public String isOpen;       // 공개 여부(Y/N)
    public String isMain;       // 공지 여부(Y/N)
    public String reply;        //답변 내용
    public int replyMemberSeq; //답변자 SEQ
    public String replyMemberNm;  //답변자명
    public int sfCode;       //답변자 고유번호

    public QnaAddRequest(){}
    public QnaAddRequest(int writerSeq, String writerNm, int userGubun, String acaCode, String acaName, String acaGubunCode, String acaGubunName, String title, String content, String isOpen, String isMain, String reply, int replyMberSeq, String replyMberNm, int sfCode) {
        this.writerSeq = writerSeq;
        this.writerNm = writerNm;
        this.userGubun = userGubun;
        this.acaCode = acaCode;
        this.acaName = acaName;
        this.acaGubunCode = acaGubunCode;
        this.acaGubunName = acaGubunName;
        this.title = title;
        this.content = content;
        this.isOpen = isOpen;
        this.isMain = isMain;
        this.reply = reply;
        this.replyMemberSeq = replyMberSeq;
        this.replyMemberNm = replyMberNm;
        this.sfCode = sfCode;
    }
}
