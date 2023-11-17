package kr.jeet.edu.manager.common;

public class IntentParams {

    public final static String PARAM_LOGIN_TYPE = "loginType";  // 로그인 타입
    public final static String PARAM_MANAGER_SEQ = "managerSeq";  // 로그인 타입
    public final static String PARAM_LOGIN_USER_NAME = "loginUserName"; //사용자 이름
    public final static String PARAM_LOGIN_USER_GENDER = "loginUserGender"; //사용자 성별
    public final static String PARAM_LOGIN_USER_SNSID = "loginUserSnsId"; //sns id
    public final static String PARAM_LOGIN_USER_GUBUN = "loginUserGubun"; // 로그인 유저 타입
    public final static String PARAM_LOGIN_USER_TYPE = "loginUserType"; // [ 회원, 비회원 구분 ]
    public final static String PARAM_MEMBER_SEQ = "memberSeq";  //memberSeq

    public final static String PARAM_STU_SEQ = "stuSeq";  // 원생 seq
    public final static String PARAM_STU_ACACODE = "acaCode";  // 캠퍼스 코드
    public final static String PARAM_STU_DEPTCODE = "deptCode";  // 부서 코드
    public final static String PARAM_STU_CLSTCODE = "clstCode";  // 학년 코드
    public final static String PARAM_STU_CLASSCODE = "clsCode";  // 반 코드
    public final static String PARAM_STU_STCODE = "stCode";  // 원생 코드
    public final static String PARAM_STU_STNAME = "stName";  // 원생 이름
    public final static String PARAM_STU_ACANAME = "acaName";  // 캠퍼스 이름
    public final static String PARAM_STU_DEPTNAME = "deptName";  // 부서 이름
    public final static String PARAM_STU_CLSTNAME = "clstName";  // 학년 이름
    public final static String PARAM_BCNAME = "bcName";  // 캠퍼스 이름
    public final static String PARAM_STU_GRADECODE = "gradeCode";  // 구분 코드


    public final static String PARAM_STUDENT_INFO = "studentInfo";  // 원생 정보
    public final static String PARAM_ANNOUNCEMENT_INFO = "announcementInfo";  // 선택한 공지사항 정보
    public final static String PARAM_ANNOUNCEMENT_SEQ = "announcementSeq";  // 공지사항 게시글 seq
    public final static String PARAM_BOARD_POSITION = "board_position";  // 선택한 아이템 position
    public final static String PARAM_BOARD_ITEM = "board_item";  // 수정한 아이템
    public final static String PARAM_BOARD_SEQ = "boardSeq";  // 게시글 seq
    public final static String PARAM_SELECTED_DATE = "selectedDate";  // 선택한 일자 정보
    public final static String PARAM_APPBAR_TITLE = "appbarTitle";
    public final static String PARAM_WEB_VIEW_URL = "webViewUrl";  // 웹뷰에서 로드할 url
    public final static String PARAM_PUSH_MESSAGE = "pushMessage";  // pushMessage

    public final static String PARAM_BRIEFING_INFO = "briefingInfo";  // 선택한 설명회 정보
    public final static String PARAM_EXIST_RECIPIENT_INFO = "existRecipientInfo";  // 선택한 수신인 정보 여부
    public final static String PARAM_BRIEFING_PT_SEQ = "briefingPtSeq";  // 설명회 예약 글 seq
    public final static String PARAM_BRIEFING_PARTICIPANTS_CNT = "briefingParticipantsCnt";  // 참가인원
    public final static String PARAM_BRIEFING_RESERVATION_CNT = "briefingReservationCnt";  // 현재예약수
    public final static String PARAM_BRIEFING_TYPE = "briefingType";  // 타입

    //board resultLauncher
    public final static String PARAM_BOARD_ADDED = "board_added";
    public final static String PARAM_BOARD_EDITED = "board_edited";
    public final static String PARAM_BOARD_DELETED = "board_deleted";

    public final static String PARAM_ANNOUNCEMENT_DETAIL_IMG = "announcementDetailImg";  // 공지사항 상세화면 이미지 item
    public final static String PARAM_ANNOUNCEMENT_DETAIL_IMG_POSITION = "announcementDetailImgPosition";  // 공지사항 상세화면 이미지 item position

    public final static String PARAM_NOTICE_INFO = "noticeinfo"; // 알림장 목록에서 클릭한 data
//    public final static String PARAM_ADDED_RECIPIENT_COUNT = "added_recipient_count"; // 선택된 수신인 수
    public final static String PARAM_SCHEDULE_INFO = "scheduleInfo";  // 선택한 설명회 정보
    public final static String PARAM_NOTICE_TYPE = "noticeType";  // Main에서 상담요청 클릭 시 전달
    public final static String PARAM_MANAGER_INFO = "managerInfo"; // Main에서 강사프로필 클릭 시 전달
    public final static String PARAM_LEVELTEST_INFO = "levelTestInfo";  // 선택한 레벨테스트 정보
    public final static String PARAM_RECIPIENT_FILTERTYPE = "recipientFilterType";  //띄워야 할 수신인 FilterType
    //성적표
    public final static String PARAM_REPORT_CARD_LIST = "reportcardlist";  // 선택한 성적표 정보
}
