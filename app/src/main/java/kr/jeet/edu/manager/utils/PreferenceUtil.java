package kr.jeet.edu.manager.utils;

import static kr.jeet.edu.manager.common.Constants.EMPTY;

import android.content.Context;
import android.content.SharedPreferences;

import kr.jeet.edu.manager.common.Constants;

public class PreferenceUtil {

    private static final String PREFERENCE_NAME = "kr.jeet.edu.manager.preferences";

    public static final String PREF_AUTO_LOGIN = "auto_login"; // 자동로그인
    public static final String PREF_SNS_USERID = "sns_user_id"; // SNS USER ID
    public static final String PREF_LOGIN_TYPE = "login_type"; // LOGIN TYPE
    public static final String PREF_USER_TYPE = "user_type"; // USER TYPE

    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_USER_PW = "user_pw";
    public static final String PREF_USER_SEQ = "user_seq"; // 사용자 seq
    public static final String PREF_USER_GUBUN = "user_gubun"; // 사용자 구분
    public static final String PREF_USER_STCODE = "user_stcode";    //사용자 코드(학생용)
    public static final String PREF_USER_SFCODE = "user_sfcode";    //사용자 코드(강사용)
    public static final String PREF_USER_NAME = "user_name";    //사용자 이름
    public static final String PREF_SMS_SENDER = "sms_sender";    //강사 SMS 발신번호

    public static final String PREF_ACA_CODE = "aca_code"; // 캠퍼스 코드
    public static final String PREF_ACA_NAME = "aca_name"; // 캠퍼스 이름
    public static final String PREF_APP_ACA_CODE = "app_aca_code"; // local캠퍼스 코드
    public static final String PREF_APP_ACA_NAME = "app_aca_name"; // local캠퍼스 이름
    public static final String PREF_DEPARTMENT_NAME = "dept_name";  //부서 이름
    public static final String PREF_GRADE_NAME = "sfgrade_name";  //학년 이름

    public static final String PREF_PUSH_TOKEN = "push_token";    //PUSH TOKEN

    public static final String PREF_NOTIFICATION_ANNOUNCEMENT = "notification_announcement";    //공지사항 push 여부
    public static final String PREF_NOTIFICATION_SEMINAR = "notification_seminar";    //설명회 push 여부
    public static final String PREF_NOTIFICATION_ATTENDANCE = "notification_attendance";    //출석 push 여부
    public static final String PREF_NOTIFICATION_SYSTEM = "notification_system";    //시스템알림 push 여부
    public static final String PREF_NOTIFICATION_LEVEL_TEST = "notification_level_test";    //테스트예약 push 여부

    //Auto Login
    public static void setAutoLogin(Context context, boolean set) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(PREF_AUTO_LOGIN, set).apply();
    }

    public static boolean getAutoLogin(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(PREF_AUTO_LOGIN, false);
    }

    //Login Type
    public static void setLoginType(Context context, int loginType) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putInt(PREF_LOGIN_TYPE, loginType).apply();
    }

    public static int getLoginType(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getInt(PREF_LOGIN_TYPE, Constants.LOGIN_TYPE_NORMAL);
    }

    //SNS User ID
    public static void setSNSUserId(Context context, String id) {
        if (id != null) {
            SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
            pref.edit().putString(PREF_SNS_USERID, id).apply();
        }
    }

    public static String getSNSUserId(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getString(PREF_SNS_USERID, EMPTY);
    }

    // PREF_MEMBER_SEQ
    public static void setUserSeq(Context context, int seq) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putInt(PREF_USER_SEQ, seq).apply();
    }

    public static int getUserSeq(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getInt(PREF_USER_SEQ, -1);
    }

    // member id
    public static void setUserId(Context context, String id) {
        if (id != null) {
            SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
            pref.edit().putString(PREF_USER_ID, id).apply();
        }
    }

    public static String getUserId(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getString(PREF_USER_ID, EMPTY);
    }

    // member pw
    public static void setUserPw(Context context, String id) {
        if (id != null) {
            SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
            pref.edit().putString(PREF_USER_PW, id).apply();
        }
    }

    public static String getUserPw(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getString(PREF_USER_PW, EMPTY);
    }

    // PREF_USER_GUBUN
    public static void setUserGubun(Context context, int seq) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putInt(PREF_USER_GUBUN, seq).apply();
    }

    public static int getUserGubun(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getInt(PREF_USER_GUBUN, Constants.USER_TYPE_ADMIN);
    }

    // PREF_USER_SFCODE
    public static void setUserSFCode(Context context, int code) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putInt(PREF_USER_SFCODE, code).apply();
    }

    public static int getUserSFCode(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getInt(PREF_USER_SFCODE, 0);
    }
    //PREF_USER_NAME
    public static void setUserName(Context context, String name) {
        if (name != null) {
            SharedPreferences pref = context.getSharedPreferences(PREF_USER_NAME, Context.MODE_PRIVATE);
            pref.edit().putString(PREF_USER_NAME, name).apply();
        }
    }
    public static String getUserName(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_USER_NAME, Context.MODE_PRIVATE);
        return pref.getString(PREF_USER_NAME, EMPTY);
    }
    //PUSH TOKEN
    public static void setPrefPushToken(Context context, String token) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(PREF_PUSH_TOKEN, token).apply();
    }

    public static String getPrefPushToken(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getString(PREF_PUSH_TOKEN, EMPTY);
    }

    // aca code
    public static void setAcaCode(Context context, String acaCode) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        if(acaCode == null) acaCode = "";
        pref.edit().putString(PREF_ACA_CODE, acaCode).apply();
    }

    public static String getAcaCode(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getString(PREF_ACA_CODE, Constants.EMPTY);
    }

    // aca name
    public static void setAcaName(Context context, String acaName) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(PREF_ACA_NAME, acaName).apply();
    }

    public static String getAcaName(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getString(PREF_ACA_NAME, EMPTY);
    }
    // aca code
    public static void setAppAcaCode(Context context, String acaCode) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(PREF_APP_ACA_CODE, acaCode).apply();
    }

    public static String getAppAcaCode(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getString(PREF_APP_ACA_CODE, Constants.EMPTY);
    }

    // aca name
    public static void setAppAcaName(Context context, String acaName) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(PREF_APP_ACA_NAME, acaName).apply();
    }

    public static String getAppAcaName(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getString(PREF_APP_ACA_NAME, EMPTY);
    }
    // sms sender
    public static void setSMSSender(Context context, String smsSender) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(PREF_SMS_SENDER, smsSender).apply();
    }

    public static String getSMSSender(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getString(PREF_SMS_SENDER, EMPTY);
    }

    // sms sender
    public static void setSFGrade(Context context, String sfGrade) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(PREF_GRADE_NAME, sfGrade).apply();
    }

    public static String getSFGrade(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getString(PREF_GRADE_NAME, Constants.EMPTY);
    }

    // sms sender
    public static void setDepartmentName(Context context, String smsSender) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(PREF_DEPARTMENT_NAME, smsSender).apply();
    }

    public static String getDepartmentName(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getString(PREF_DEPARTMENT_NAME, Constants.EMPTY);
    }

    // notification announcement
    public static void setNotificationAnnouncement(Context context, boolean flag) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(PREF_NOTIFICATION_ANNOUNCEMENT, flag).apply();
    }
    public static boolean getNotificationAnnouncement(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(PREF_NOTIFICATION_ANNOUNCEMENT, false);
    }

    // notification seminar
    public static void setNotificationSeminar(Context context, boolean flag) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(PREF_NOTIFICATION_SEMINAR, flag).apply();
    }
    public static boolean getNotificationSeminar(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(PREF_NOTIFICATION_SEMINAR, false);
    }

    // notification attendance
    public static void setNotificationAttendance(Context context, boolean flag) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(PREF_NOTIFICATION_ATTENDANCE, flag).apply();
    }
    public static boolean getNotificationAttendance(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(PREF_NOTIFICATION_ATTENDANCE, false);
    }

    // notification system
    public static void setNotificationSystem(Context context, boolean flag) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(PREF_NOTIFICATION_SYSTEM, flag).apply();
    }
    public static boolean getNotificationSystem(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(PREF_NOTIFICATION_SYSTEM, false);
    }
}
