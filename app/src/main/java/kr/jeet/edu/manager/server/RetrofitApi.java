package kr.jeet.edu.manager.server;

import java.util.List;

import kr.jeet.edu.manager.model.data.LevelTestTimeData;
import kr.jeet.edu.manager.model.data.ScheduleData;
import kr.jeet.edu.manager.model.request.AttendanceRequest;
import kr.jeet.edu.manager.model.request.QnaAddRequest;
import kr.jeet.edu.manager.model.request.QnaReplyRequest;
import kr.jeet.edu.manager.model.request.QnaUpdateRequest;
import kr.jeet.edu.manager.model.request.RecipientRequest;
import kr.jeet.edu.manager.model.request.SignupRequest;
import kr.jeet.edu.manager.model.request.SignupSNSRequest;
import kr.jeet.edu.manager.model.request.SmsRequest;
import kr.jeet.edu.manager.model.request.UpdateAnswererRequest;
import kr.jeet.edu.manager.model.request.UpdateProfileRequest;
import kr.jeet.edu.manager.model.request.UpdatePushStatusRequest;
import kr.jeet.edu.manager.model.request.UpdatePushTokenRequest;
import kr.jeet.edu.manager.model.request.UpdateReportCardRequest;
import kr.jeet.edu.manager.model.response.AnnouncementListResponse;
import kr.jeet.edu.manager.model.response.BCListResponse;
import kr.jeet.edu.manager.model.response.BaseResponse;
import kr.jeet.edu.manager.model.response.BoardAttributeResponse;
import kr.jeet.edu.manager.model.response.BoardDetailResponse;
import kr.jeet.edu.manager.model.response.BriefingDetailResponse;
import kr.jeet.edu.manager.model.response.BriefingRecipientListResponse;
import kr.jeet.edu.manager.model.response.BoardRegisterResponse;
import kr.jeet.edu.manager.model.response.BriefingReservedListResponse;
import kr.jeet.edu.manager.model.response.BriefingResponse;
import kr.jeet.edu.manager.model.response.BusInfoResponse;
import kr.jeet.edu.manager.model.response.BusRouteResponse;
import kr.jeet.edu.manager.model.response.CounselDetailResponse;
import kr.jeet.edu.manager.model.response.FindIDResponse;
import kr.jeet.edu.manager.model.response.FindPWResponse;
import kr.jeet.edu.manager.model.response.GetACAListResponse;
import kr.jeet.edu.manager.model.response.GetAnswererListResponse;
import kr.jeet.edu.manager.model.response.GetAttendanceInfoResponse;
import kr.jeet.edu.manager.model.response.GetClassListResponse;
import kr.jeet.edu.manager.model.response.GetClstListResponse;
import kr.jeet.edu.manager.model.response.GetDeptListResponse;
import kr.jeet.edu.manager.model.response.GetManagerClassInfoResponse;
import kr.jeet.edu.manager.model.response.GetManagerInfoResponse;
import kr.jeet.edu.manager.model.response.GetParentNotificationInfoResponse;
import kr.jeet.edu.manager.model.response.GetRecipientStudentResponse;
import kr.jeet.edu.manager.model.response.GetStudentInfoResponse;
import kr.jeet.edu.manager.model.response.LTCListResponse;
import kr.jeet.edu.manager.model.response.LevelTestListResponse;
import kr.jeet.edu.manager.model.response.LevelTestTimeListResponse;
import kr.jeet.edu.manager.model.response.LevelTestTimeResponse;
import kr.jeet.edu.manager.model.response.LoginResponse;
import kr.jeet.edu.manager.model.response.NoticeListResponse;
import kr.jeet.edu.manager.model.response.QnaDetailResponse;
import kr.jeet.edu.manager.model.response.QnaListResponse;
import kr.jeet.edu.manager.model.response.ReportCardListResponse;
import kr.jeet.edu.manager.model.response.ReportCardShowResponse;
import kr.jeet.edu.manager.model.response.ReportCardSummaryListResponse;
import kr.jeet.edu.manager.model.response.ReportCardSummaryResponse;
import kr.jeet.edu.manager.model.response.ScheduleDetailResponse;
import kr.jeet.edu.manager.model.response.ScheduleListResponse;
import kr.jeet.edu.manager.model.response.ScheduleRegisterResponse;
import kr.jeet.edu.manager.model.response.SchoolListResponse;
import kr.jeet.edu.manager.model.response.StringResponse;
import kr.jeet.edu.manager.model.response.StudentGradeListResponse;
import kr.jeet.edu.manager.model.response.SystemNoticeDetailResponse;
import kr.jeet.edu.manager.model.response.TeacherClsResponse;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RetrofitApi {

    // https://daldalhanstory.tistory.com/275
    // https://eunoia3jy.tistory.com/125

//    public final static String SERVER_BASE_URL = "http://192.168.2.51:7777/";   //kyt local
    //public final static String SERVER_BASE_URL = "http://192.168.2.55:7777/";   //pjh local
    //public final static String SERVER_BASE_URL = "http://192.168.2.77:7777/";  //khj local
    public final static String SERVER_BASE_URL = "http://211.252.86.237:7777/";  //cloud
    public final static String PREFIX = "mobile/api/";
    public final static String FILE_SUFFIX_URL = SERVER_BASE_URL + "attachFile/";

    public final static int RESPONSE_CODE_SUCCESS = 200;
    public final static int RESPONSE_CODE_SUCCESS2 = 201;
    public final static int RESPONSE_CODE_BINDING_ERROR= 400;
    public final static int RESPONSE_CODE_NOT_FOUND= 404;
    public final static int RESPONSE_CODE_DUPLICATE_ERROR= 409;
    public final static int RESPONSE_CODE_INTERNAL_SERVER_ERROR= 500;

    // 회원가입
    @POST("member/signUp")
    Call<BaseResponse> signUp(@Body SignupRequest signUpRequest);

    //  로그인(일반)
    @POST("member/signIn")
    Call<LoginResponse> signIn(@Query("id") String id, @Query("pw") String pw);

    // SMS 전송
    @POST("sms")
    Call<BaseResponse> sendSms(@Body SmsRequest request);

    // 회원가입 SNS
    @POST("member/sns/signUp")
    Call<BaseResponse> signUpFromSNS(@Body SignupSNSRequest signUpRequest);

    //  로그인(SNS)
    @POST("member/sns/signIn")
    Call<LoginResponse> signInSNS(@Query("snsId") String id, @Query("appType") String appType);

    //  아이디찾기
    @GET("member/id")
    Call<FindIDResponse> findID(@Query("phoneNumber") String phoneNumber, @Query("appType") String appType);

    //  PW 찾기
    @GET("member/pw")
    Call<FindPWResponse> findPW(@Query("memberId") String memberId, @Query("phoneNumber") String phoneNumber);

    // 학부모의 자녀정보
//    @GET("member/{parentMemberSeq}/students")
//    Call<SearchChildStudentsResponse> searchChildStudents(@Path("parentMemberSeq") int parentMemberSeq);
    // 로그아웃
    @POST("member/logout")
    Call<BaseResponse> logout(@Query("memberSeq") int memberSeq);
    // 회원탈퇴
    @POST("member/withdrawal")
    Call<BaseResponse> leave(@Query("memberSeq") int memberSeq);
    //UPDATE PUSH TOKEN
    @POST("push/token")
    Call<BaseResponse> updatePushToken(@Body UpdatePushTokenRequest request);

    //SUBSCRIBE / Unscribe PUSH TOPIC   //Topic 사용하지 않음
//    @POST("push/topic")
//    Call<BaseResponse> updateSubscribePushTopic(@Query("memberSeq") int memberSeq, @Query("token") String token);

    //푸쉬 수신처리
    @POST("push/receive")
    Call<BaseResponse> pushReceived(@Query("pushId") List<String> pushId);

    //푸쉬 확인처리
    @POST("push/confirm")
    Call<BaseResponse> pushConfirmed(@Query("pushId") List<String> pushId);
    // 앱 설치문자 내용
    @GET("push/appInstallMsg")
    Call<StringResponse> getAppInstallMsg();
    
    // 캠퍼스 목록 가져오기
    @GET("aca")
    Call<GetACAListResponse> getACAList();
    // 지역기준 캠퍼스 조회
    @GET("appAca")
    Call<GetACAListResponse> getLocalACAList();
    // 학생등급 조회 (지역기준 캠퍼스)
    @GET("appAcaGubun/{appAcaCode}")
    Call<StudentGradeListResponse> getStudentGradeList(@Path("appAcaCode") String appAcaCode);

    // 캠퍼스 목록 조회(테스트예약)
    @GET("levelTest/campuses")
    Call<LTCListResponse> getLTCList();
    // 학교 목록 조회
    @GET("school")
    Call<SchoolListResponse> getSchoolList();

    // 부서 목록 가져오기
    @GET("dept")
    Call<GetDeptListResponse> getDeptList(@Query("acaCode") String acaCode);
    // 학년 목록 가져오기
    @GET("clst")
    Call<GetClstListResponse> getClstList(@Query("acaCode") String acaCode, @Query("deptCode") int deptCode);
    // 학급 목록 가져오기
    @GET("class")
    Call<GetClassListResponse> getClassList(@Query("acaCode") String acaCode, @Query("deptCode") int deptCode, @Query("clstCode") int clstCode, @Query("clYm") String clYm);
    // 학급 목록 가져오기2
    @GET("class2")
    Call<GetClassListResponse> getClassList2(@Query("acaCode") String acaCode, @Query("deptCode") int deptCode, @Query("clstCode") int clstCode, @Query("clYm") String clYm, @Query("sfCode") int sfCode);
    //원생 목록 가져오기
    @GET("student")
    Call<GetRecipientStudentResponse> getRecipientStudentList(@Query("acaCode") String acaCode, @Query("deptCode") int deptCode, @Query("clstCode") int clstCode, @Query("clsCode") int clsCode, @Query("clYm") String clYm);
    //원생 목록 가져오기2
    @GET("student2")
    Call<GetRecipientStudentResponse> getRecipientStudentList2(@Query("acaCode") String acaCode, @Query("deptCode") int deptCode, @Query("clstCode") int clstCode, @Query("scCode") int scCode, @Query("gubun") int gubun);
    //원생 목록 가져오기3
    @GET("student3")
    Call<GetRecipientStudentResponse> getRecipientStudentList3(@Query("acaCode") String acaCode, @Query("deptCode") int deptCode, @Query("clstCode") int clstCode, @Query("clsCode") int clsCode, @Query("clYm") String clYm, @Query("sfCode") int sfCode);
    //  회원정보 조회
    @GET("member/{memberSeq}/sf/{sfCode}")
    Call<GetManagerInfoResponse> getManagerInfo(@Path("memberSeq") int memberSeq, @Path("sfCode") int sfCode);
    // 원생 정보 조회
    @GET("member/{memberSeq}/st/{stCode}")
    Call<GetStudentInfoResponse> getStudentInfo(@Path("memberSeq") int memberSeq, @Path("stCode") int stCode);
    // 부모 알림 수신 여부 조회
    @GET("member/pushStatus")
    Call<GetParentNotificationInfoResponse> getParentNotificationInfo(@Query("phoneNumber") String phoneNumber, @Query("userGubun") int userGubun);
    // 강사학급 정보 조회
    @GET("member/sf/{sfCode}/class")
    Call<GetManagerClassInfoResponse> getManagerClassInfo(@Path("sfCode") int sfCode);

    // Get-Student-Attendance (출결 조회 - 일별)
    @GET("attendance/{attendanceDate}/cls/{clsCode}")
    Call<GetAttendanceInfoResponse> getClassAttendanceInfo(@Path("attendanceDate") String attendanceDate, @Path("clsCode") int clsCode);

    // Get-Student-Attendance (출결 조회 - 월별)
    @GET("attendance/{attendanceDate}/cls/{clsCode}/st/{stCode}")
    Call<GetAttendanceInfoResponse> getMonthlyAttendanceInfo(@Path("attendanceDate") String attendanceDate, @Path("clsCode") int clsCode, @Path("stCode") int stCode);

    //출결 등록
    @POST("attendance")
    Call<BaseResponse> updateAttendance(@Body AttendanceRequest request);
    // 게시판 속성 조회
    @GET("board/attrb")
    Call<BoardAttributeResponse> getBoardAttribute();

    // 공지사항 목록 조회
    @GET("notices")
    Call<AnnouncementListResponse> getAnnouncementList(@Query("noticeSeq") int noticeSeq, @Query("acaCode") String acaCode, @Query("acaGubunCode") String acaGubunCode);

    // 공지사항 등록
    @Multipart
    @POST("notice")
    Call<BoardRegisterResponse> insertAnnouncement(@Part("item") RequestBody item, @Part List<MultipartBody.Part> files);
    // 공지사항 수정
    @Multipart
    @PATCH("notice")
    Call<BaseResponse> updateAnnouncement(@Part("item") RequestBody item, @Part List<MultipartBody.Part> files);
    //공지사항 수신인 등록
    @POST("notice/receiver")
    Call<BaseResponse> updateAnnounceRecipient(@Body RecipientRequest request);
    // 공지사항 글 상세보기
    @GET("notice/{noticeSeq}")
    Call<BoardDetailResponse> getBoardDetail(@Path("noticeSeq") int noticeSeq);
    //공지사항 삭제
    @DELETE("notice/{noticeSeq}")
    Call<BaseResponse> deleteAnnouncement(@Path("noticeSeq") int noticeSeq);

//    //파일 다운로드
//    @Streaming
//    @GET
//    Call<ResponseBody> downloadFile(@Url String url);

    // 회원 정보 수정(이름, 성별)
    @PATCH("member")
    Call<BaseResponse> updateProfile(@Body UpdateProfileRequest updateProfileRequest);

    // 회원 비밀번호 수정
    @PATCH("member/pw")
    Call<BaseResponse> updatePassword(@Query("memberSeq") int memberSeq, @Query("pw") String pw);

    // 회원 알림 수신 여부 수정
    @PATCH("member/pushStatus")
    Call<BaseResponse> updatePushStatus(@Body UpdatePushStatusRequest updatePushStatus);

    // 회원 휴대폰번호 수정
    @PATCH("member/pn")
    Call<BaseResponse> updatePhoneNum(@Query("memberSeq") int memberSeq, @Query("phoneNumber") String phoneNumber);

    // 원생 학급 정보 조회
    @GET("member/st/{stCode}/clsDate/{clsDate}/class")
    Call<TeacherClsResponse> requestTeacherCls(@Path("stCode") int stCode, @Path("clsDate") String clsDate);

    // 설명회 목록 조회
    @GET("pts")
    Call<BriefingResponse> getBriefingList(@Query("acaCode") String acaCode, @Query("acaGubunCode") String acaGubunCode, @Query("year") int year, @Query("month") int month);

    // 설명회 등록
    @Multipart
    @POST("pt")
    Call<BoardRegisterResponse> insertBriefing(@Part("item") RequestBody item, @Part List<MultipartBody.Part> files);
    // 설명회 수정
    @Multipart
    @PATCH("pt")
    Call<BaseResponse> updateBriefing(@Part("item") RequestBody item, @Part List<MultipartBody.Part> files);
    //설명회 수신인 등록
    @POST("pt/receiver")
    Call<BaseResponse> updateBriefingRecipient(@Body RecipientRequest request);
    // 설명회 글 상세보기
    @GET("pt/{ptSeq}")
    Call<BriefingDetailResponse> getBriefingDetail(@Path("ptSeq") int ptSeq);
    //설명회 삭제
    @DELETE("pt/{ptSeq}")
    Call<BaseResponse> deleteBriefing(@Path("ptSeq") int ptSeq);
    // 설명회 예약자 목록 조회
    @GET("pt/reservation/{ptSeq}/member/{memberSeq}")
    Call<BriefingReservedListResponse> getBrfReservedList(@Path("ptSeq") int ptSeq, @Path("memberSeq") int memberSeq);
    // 설명회 예약 취소
    @DELETE("pt/reservation")
    Call<StringResponse> deleteBriefingReservation(@Query("reservationSeq") int reservationSeq, @Query("memberSeq") int memberSeq, @Query("userGubun") int userGubun);
    // 수신인 목록 조회
    @GET("pt/receiver/{ptSeq}")
    Call<BriefingRecipientListResponse> getBriefingRecipientList(@Path("ptSeq") int ptSeq);
    //알림장 목록 조회
    @GET("systemNotices")
    Call<NoticeListResponse> getNoticeList(@Query("searchType") String searchType, @Query("searchDate") String searchDate, @Query("sfCode") int sfCode , @Query("memberSeq") int memberSeq, @Query("userGubun") int userGubun, @Query("seq") int seq, @Query("acaCode") String acaCode, @Query("acaGubunCode") String acaGubunCode);
    // 알림장 등록
    @Multipart
    @POST("systemNotice")
    Call<BoardRegisterResponse> insertNotice(@Part("item") RequestBody item, @Part List<MultipartBody.Part> files);
    //알림장 수신인 등록
    @POST("systemNotice/receiver")
    Call<BaseResponse> updateNoticeRecipient(@Body RecipientRequest request);
    //알림장 상세보기
    @GET("systemNotice/{seq}")
    Call<SystemNoticeDetailResponse> getSystemNoticeDetail(@Path("seq") int seq);

    //상담요청 상세보기
    @GET("counsel/{seq}")
    Call<CounselDetailResponse> getCounselDetail(@Path("seq") int seq);

    // 캠퍼스일정 목록 조회
    @GET("schedules")
    Call<ScheduleListResponse> getScheduleList(@Query("acaCode") String acaCode, @Query("acaGubunCode") String acaGubunCode, @Query("year") int year, @Query("month") int month);
    // 캠퍼스일정 상세 조회
    @GET("schedule/{scheduleSeq}")
    Call<ScheduleDetailResponse> getScheduleDetail(@Path("scheduleSeq") int scheduleSeq);
    //일정 등록
    @POST("schedule")
    Call<ScheduleRegisterResponse> insertSchedule(@Body ScheduleData request);
    //일정 수정
    @PATCH("schedule")
    Call<BaseResponse> updateSchedule(@Body ScheduleData request);
    //일정 수신인 등록
    @POST("schedule/receiver")
    Call<BaseResponse> updateScheduleRecipient(@Body RecipientRequest request);
    //일정 삭제
    @DELETE("schedule/{scheduleSeq}")
    Call<BaseResponse> deleteSchedule(@Path("scheduleSeq") int scheduleSeq);

    //레벨테스트 이력조회
    @GET("levelTests")
    Call<LevelTestListResponse> getLevelTestList(@Query("memberSeq") int memberSeq, @Query("ltcCode") String ltcCode, @Query("grade") String grade, @Query("lastSeq") int lastSeq);
    //레벨테스트 시간 조회
    @GET("levelTest/testTimes")
    Call<LevelTestTimeListResponse> getLevelTestTimeList();
    //레벨테스트 시간 등록/수정
    @POST("levelTest/testTime")
    Call<LevelTestTimeResponse> updateLevelTestTime(@Body LevelTestTimeData request);
    //레벨테스트 시간 삭제
    @DELETE("levelTest/testTime/{seq}")
    Call<BaseResponse> deleteLevelTestTime(@Path("seq") int seq);
    // 레벨테스트 예약 취소
    @DELETE("levelTest")
    Call<BaseResponse> cancelLevelTestReservation(@Query("seq") int seq, @Query("stCode") int stCode, @Query("sfCode") int sfCode, @Query("subjectCode") int subjectCode, @Query("memberSeq") int memberSeq);
    //버스 캠퍼스 조회
    @GET("bus/campuses")
    Call<BCListResponse> getBusCampusList();
    // 버스 정보 조회
    @GET("bus")
    Call<BusInfoResponse> getBusInfo(@Query("phoneNumber") String phoneNumber);
    // 버스 목록 조회
    @GET("buses")
    Call<BusInfoResponse> getBusesInfo(@Query("busAcaName") String busAcaName);
    // 버스 노선 조회
    @GET("bus/route")
    Call<BusRouteResponse> getBusRoute(@Query("busAcaName") String busAcaName, @Query("busCode") int busCode);

    //성적표 목록조회
    @GET("reportCards")
    Call<ReportCardSummaryListResponse> getReportCardSummaryList(@Query("memberSeq") int memberSeq, @Query("userGubun") int userGubun, @Query("searchKeyword") String searchKeyword, @Query("acaCode") String acaCode, @Query("reportSeq") int reportSeq);
    //원생별 성적표리스트 조회
    @GET("reportCard/stCode/{stCode}")
    Call<ReportCardListResponse> getReportCardList(@Path("stCode") int stCode);
    //성적표 상세조회
    @GET("reportCard/{reportSeq}")
    Call<ReportCardSummaryResponse> getReportCardDetailList(@Path("reportSeq") int reportSeq);
    //성적표별 데이터 상세조회
    @GET("reportCard/{reportSeq}/list/{reportListSeq}")
    Call<ReportCardShowResponse> getReportCardShowList(@Path("reportSeq") int reportSeq, @Path("reportListSeq") int reportListSeq);
    //성적표 등록
    @POST("reportCard")
    Call<BaseResponse> updateReportCard(@Body UpdateReportCardRequest request);
    //성적표 삭제
    @DELETE("reportCard/{reportSeq}")
    Call<BaseResponse> deleteReportCard(@Path("reportSeq") int reportSeq);

    //QnA 목록 조회
    @GET("qnas")
    Call<QnaListResponse> getQnaList(
            @Query("qnaSeq") int qnaSeq,
            @Query("memberSeq") int memberSeq,
            @Query("userGubun") int userGubun,
            @Query("acaCode") String acaCode,
            @Query("acaGubunCode") String acaGubunCode
    );
    //QnA 상세조회
    @GET("qna/detail")
    Call<QnaDetailResponse> getQnaDetail(@Query("qnaSeq") int qnaSeq, @Query("userGubun") int userGubun, @Query("sfCode") int sfCode);
    //QnA 등록 (매니저앱)
    @POST("qna/manager")
    Call<StringResponse> addQnaViaManager(@Body QnaAddRequest request);
    // 답변관리자 목록 조회
    @GET("qna/replyAdmin")
    Call<GetAnswererListResponse> getAnswererList();
    //답변관리자 등록
    @POST("qna/grant")
    Call<BaseResponse> updateAnswererList(@Body UpdateAnswererRequest request);
    //지정된 답변관리자 목록 조회
    @GET("qna/replyAdmin/{qnaSeq}")
    Call<GetAnswererListResponse> getSpecifiedAnswererList(@Path("qnaSeq") int qnaSeq);
    //QnA 수정
    @PATCH("qna")
    Call<BaseResponse> updateQna(@Body QnaUpdateRequest request);
    //답변관리자 답글등록
    @PATCH("qna/reply")
    Call<BaseResponse> updateReply(@Body QnaReplyRequest request);
    // QnA 공지/공개여부 수정(슈퍼관리자/관리자)
    @PATCH("qna/admin")
    Call<BaseResponse> updateQnaProperty(@Query("qnaSeq") int qnaSeq, @Query("userGubun") int userGubun, @Query("isOpen") String isOpen, @Query("isMain") String isMain);
    //QnA 삭제
    @DELETE("qna/{qnaSeq}")
    Call<BaseResponse> deleteQna(@Path("qnaSeq") int qnaSeq);
}
