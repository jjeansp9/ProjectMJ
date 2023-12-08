package kr.jeet.edu.manager.model.request;

public class QnaUpdateRequest implements BaseRequest{
    public int seq;             // 글 Seq
    public int userGubun;       // 작성자의 유저 구분 (예: 관리자, 사용자 등)
    public String title;        // 글 제목
    public String content;      // 글 내용
    public String isOpen;       // 공개 여부(Y/N)
    public String isMain;       // 공지 여부(Y/N)
    public String reply;        //답변 내용
    public QnaUpdateRequest() {}

    public QnaUpdateRequest(int seq, int userGubun, String title, String content, String isOpen, String isMain, String reply) {
        this.seq = seq;
        this.userGubun = userGubun;
        this.title = title;
        this.content = content;
        this.isOpen = isOpen;
        this.isMain = isMain;
        this.reply = reply;
    }
}
