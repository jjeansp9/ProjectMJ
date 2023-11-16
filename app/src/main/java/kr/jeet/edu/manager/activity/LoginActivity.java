package kr.jeet.edu.manager.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.model.request.SigninRequest;
import kr.jeet.edu.manager.model.response.LoginResponse;
import kr.jeet.edu.manager.server.RetrofitApi;
import kr.jeet.edu.manager.server.RetrofitClient;
import kr.jeet.edu.manager.sns.AppleLoginManager;
import kr.jeet.edu.manager.sns.GoogleLoginManager;
import kr.jeet.edu.manager.sns.KaKaoLoginManager;
import kr.jeet.edu.manager.sns.NaverLoginManager;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.PreferenceUtil;
import kr.jeet.edu.manager.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {

    private static final String TAG = "login";

    private EditText mEditId, mEditPw;
    private CheckBox mAutoLoginCb;
    private RetrofitApi mRetrofitApi;

    private int userType = Constants.USER_TYPE_TEACHER;
    private NaverLoginManager mNaverLogin = null;
    private KaKaoLoginManager mKaKaoLogin = null;
    private GoogleLoginManager mGoogleLogin = null;
    private AppleLoginManager mAppleLogin = null;
    private int selectedSNSLoginType = -1;

    private AppCompatActivity mActivity = null;

    private String snsName = "";
    private String snsType = "";

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case Constants.HANDLER_SNS_LOGIN_COMPLETE:
                    LogMgr.e(TAG, "SNS_LOGIN_COMPLETE");

                    String snsId = (String) msg.obj;

                    Bundle data = msg.getData();
                    if (data != null){
                        snsName = data.getString("name");
                        snsType = data.getString("loginType");
                    }

                    if(snsId != null && !snsId.isEmpty()) {
                        requestLoginFromSns(snsId);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setStatusAndNavigatinBar(true);
        mActivity = this;
        mContext = this;
        mGoogleLogin = new GoogleLoginManager(mActivity);

        //String HashKey = Utility.INSTANCE.getKeyHash(mContext);
        //LogMgr.e(TAG, HashKey);
        initView();
    }
    @Override
    void initAppbar() {
        //do nothing
    }

    @Override
    void initView() {
        findViewById(R.id.layout_root).setOnClickListener(this);
        findViewById(R.id.tv_find).setOnClickListener(this);
        findViewById(R.id.tv_join).setOnClickListener(this);
        findViewById(R.id.btn_naver).setOnClickListener(this);
        findViewById(R.id.btn_kakao).setOnClickListener(this);
        findViewById(R.id.btn_google).setOnClickListener(this);
        findViewById(R.id.btn_apple).setOnClickListener(this);
        findViewById(R.id.btn_login).setOnClickListener(this);
        findViewById(R.id.checkbox_text).setOnClickListener(this);

        mEditId = (EditText) findViewById(R.id.edit_id);
        mEditPw = (EditText) findViewById(R.id.edit_pw);
        mEditId.setText(PreferenceUtil.getUserId(mContext));

        mAutoLoginCb = (CheckBox) findViewById(R.id.checkbox_login);
        mAutoLoginCb.setChecked(PreferenceUtil.getAutoLogin(mContext));
    }


    @Override
    public void onClick(View view) {
        super.onClick(view);
        Intent intent = null;

        String snsUserId = "";
        int loginType = Constants.LOGIN_TYPE_NORMAL;

        switch (view.getId()) {
            case R.id.layout_root:
                Utils.hideKeyboard(mContext, mEditId, mEditPw);
                break;
            case R.id.btn_naver:

                selectedSNSLoginType = Constants.LOGIN_TYPE_SNS_NAVER;
                mNaverLogin = new NaverLoginManager(mContext);
                mNaverLogin.setHandler(mHandler);
                mNaverLogin.LoginProcess();

                break;

            case R.id.btn_kakao:

                selectedSNSLoginType = Constants.LOGIN_TYPE_SNS_KAKAO;
                mKaKaoLogin = new KaKaoLoginManager(mContext);
                mKaKaoLogin.setHandler(mHandler);
                mKaKaoLogin.LoginProcess();

                break;

            case R.id.btn_google:

                selectedSNSLoginType = Constants.LOGIN_TYPE_SNS_GOOGLE;

                mGoogleLogin.setHandler(mHandler);
                mGoogleLogin.LoginProcess();
                break;

            case R.id.btn_apple:
                selectedSNSLoginType = Constants.LOGIN_TYPE_SNS_APPLE;
                mAppleLogin = new AppleLoginManager(mActivity);
                mAppleLogin.setHandler(mHandler);
                mAppleLogin.LoginProcess();
                break;

            case R.id.tv_join :
                intent = new Intent(this, AgreeTermsActivity.class);
                intent.putExtra(IntentParams.PARAM_LOGIN_TYPE, Constants.LOGIN_TYPE_NORMAL);
                startActivity(intent);
                break;

            case R.id.tv_find :
                intent = new Intent(this, FindCredentialsActivity.class);
                startActivity(intent);
                break;

            case R.id.btn_login :
                Utils.hideKeyboard(mContext, mEditId, mEditPw);

                if(checkLogin()) {
                    requestLogin();
                }

                break;
            case R.id.checkbox_text:    //체크박스 범위 확대
                mAutoLoginCb.setChecked(!mAutoLoginCb.isChecked());
                break;
        }
    }

    private boolean checkLogin() {
        if(mEditId.getText().toString().trim().isEmpty()) {
            showKeyboard(mContext, mEditId);
            Toast.makeText(mContext, getString(R.string.id) + " " + getString(R.string.empty_info), Toast.LENGTH_SHORT).show();
            return false;
        }
        if(mEditPw.getText().toString().trim().isEmpty()) {
            showKeyboard(mContext, mEditPw);
            Toast.makeText(mContext, getString(R.string.password) + " " + getString(R.string.empty_info), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
    private void clearLoginInfo() {
        if(mEditId != null) mEditId.setText("");
        if(mEditPw != null) mEditPw.setText("");
    }

    private void requestLogin() {

        SigninRequest request = new SigninRequest();
        request.id = mEditId.getText().toString().trim();
        request.pw = mEditPw.getText().toString().trim();
        request.userGubun = userType;

        if(RetrofitClient.getInstance() != null) {
            showProgressDialog();
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.signIn(request.id, request.pw).enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    hideProgressDialog();
                    if (response.isSuccessful()) {
                        PreferenceUtil.setUserId(mContext, request.id);
                        PreferenceUtil.setAutoLogin(mContext, mAutoLoginCb.isChecked());
                        PreferenceUtil.setLoginType(mContext, Constants.LOGIN_TYPE_NORMAL);
                        if (PreferenceUtil.getAutoLogin(mContext)) {
                            PreferenceUtil.setUserPw(mContext, request.pw);
                        }
                        LoginResponse res = response.body();
                        if (res != null && res.data != null) {
                            //super관리자
                            if(res.data.userGubun == Constants.USER_TYPE_SUPER_ADMIN){

                                PreferenceUtil.setUserSeq(mContext, res.data.seq);
                                PreferenceUtil.setUserGubun(mContext, res.data.userGubun);
                                PreferenceUtil.setUserSFCode(mContext, res.data.sfCode);

                                Utils.refreshPushToken(mContext, res.data.seq);
                                if(res.data.pushStatus != null){
                                    //공지사항
                                    PreferenceUtil.setNotificationAnnouncement(mContext, res.data.pushStatus.pushNotice.equals("Y"));
                                    //설명회
                                    PreferenceUtil.setNotificationSeminar(mContext, res.data.pushStatus.pushInformationSession.equals("Y"));
                                    //출석
                                    PreferenceUtil.setNotificationAttendance(mContext, res.data.pushStatus.pushAttendance.equals("Y"));
                                    //시스템알림
                                    PreferenceUtil.setNotificationSystem(mContext, res.data.pushStatus.pushSystem.equals("Y"));
                                }else{
                                    //공지사항
                                    PreferenceUtil.setNotificationAnnouncement(mContext, true);
                                    //설명회
                                    PreferenceUtil.setNotificationSeminar(mContext, true);
                                    //출석
                                    PreferenceUtil.setNotificationAttendance(mContext, true);
                                    //시스템알림
                                    PreferenceUtil.setNotificationSystem(mContext, true);
                                }
                                Intent intent = new Intent(mContext, MainActivity.class);
                                startActivity(intent);
                                finish();

                            } else if (res.data.userGubun <= Constants.USER_TYPE_TEACHER) {
                                if (res.data.sfCode > 0) {
                                    PreferenceUtil.setUserSeq(mContext, res.data.seq);
                                    PreferenceUtil.setUserGubun(mContext, res.data.userGubun);
                                    PreferenceUtil.setUserSFCode(mContext, res.data.sfCode);
                                    PreferenceUtil.setUserPw(mContext, request.pw);
                                    if(res.data.pushStatus != null){
                                        //공지사항
                                        PreferenceUtil.setNotificationAnnouncement(mContext, res.data.pushStatus.pushNotice.equals("Y"));
                                        //설명회
                                        PreferenceUtil.setNotificationSeminar(mContext, res.data.pushStatus.pushInformationSession.equals("Y"));
                                        //출석
                                        PreferenceUtil.setNotificationAttendance(mContext, res.data.pushStatus.pushAttendance.equals("Y"));
                                        //시스템알림
                                        PreferenceUtil.setNotificationSystem(mContext, res.data.pushStatus.pushSystem.equals("Y"));
                                    }else{
                                        //공지사항
                                        PreferenceUtil.setNotificationAnnouncement(mContext, true);
                                        //설명회
                                        PreferenceUtil.setNotificationSeminar(mContext, true);
                                        //출석
                                        PreferenceUtil.setNotificationAttendance(mContext, true);
                                        //시스템알림
                                        PreferenceUtil.setNotificationSystem(mContext, true);
                                    }
                                    Utils.refreshPushToken(mContext, res.data.seq);
                                    Intent intent = new Intent(mContext, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
//                                    Utils.refreshPushToken(mContext, PreferenceUtil.getUserSeq(mContext), "");
                                    PreferenceUtil.setPrefPushToken(mContext, "");
                                    PreferenceUtil.setUserSeq(mContext, -1);
                                    PreferenceUtil.setLoginType(mContext, Constants.LOGIN_TYPE_NORMAL);
                                    PreferenceUtil.setUserId(mContext, "");
                                    PreferenceUtil.setUserPw(mContext, "");
                                    PreferenceUtil.setSNSUserId(mContext, "");
                                    PreferenceUtil.setAutoLogin(mContext, false);
//                                    Toast.makeText(mContext, R.string.error_message_invalid_info, Toast.LENGTH_SHORT).show();
                                    showMessageDialog(getString(R.string.dialog_title_alarm), getString(R.string.error_message_invalid_info), new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    clearLoginInfo();
                                                    hideMessageDialog();
                                                }
                                            },
                                            null);
                                }
//                                finish();
                            } else {  //학부모, 학생인 경우
                                //preference 저장값 초기화
//                                Utils.refreshPushToken(mContext, PreferenceUtil.getUserSeq(mContext), "");
                                PreferenceUtil.setPrefPushToken(mContext, "");
                                PreferenceUtil.setUserSeq(mContext, -1);
                                PreferenceUtil.setLoginType(mContext, Constants.LOGIN_TYPE_NORMAL);
                                PreferenceUtil.setUserId(mContext, "");
                                PreferenceUtil.setUserPw(mContext, "");
                                PreferenceUtil.setSNSUserId(mContext, "");
                                PreferenceUtil.setAutoLogin(mContext, false);
//                                Toast.makeText(mContext, R.string.error_message_get_out, Toast.LENGTH_SHORT).show();
                                showMessageDialog(getString(R.string.dialog_title_alarm), getString(R.string.error_message_get_out), new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                clearLoginInfo();
                                                hideMessageDialog();
                                            }
                                        },
                                        null);
//                                finish();
                            }
                        } else {
                            Toast.makeText(mContext, R.string.server_data_empty, Toast.LENGTH_SHORT).show();
                            clearLoginInfo();
                        }
                    } else {

                        try {
                            LogMgr.e(TAG, "requestLogin() errBody : " + response.errorBody().string());

                            if (response.code() == 400) {

                                String title = getString(R.string.dialog_title_alarm);
                                String msgMismatch = getString(R.string.msg_user_gubun_mismatch);
                                String msgNotJeetMember = getString(R.string.msg_user_0_not_jeet_member);

                                if (response.body().msg.equals(Constants.ALREADY_LOGIN_IN)) {
                                    Toast.makeText(mContext, R.string.msg_already_login_in, Toast.LENGTH_SHORT).show();

                                } else if (response.body().msg.equals(Constants.PASSWORD_MISMATCH)) {
                                    Toast.makeText(mContext, R.string.msg_password_mismatch, Toast.LENGTH_SHORT).show();

                                } else if (response.body().msg.equals(Constants.PARAMETER_BINDING_ERROR)) {
                                    Toast.makeText(mContext, R.string.msg_parameter_binding_error, Toast.LENGTH_SHORT).show();

                                } else if (response.body().msg.equals(Constants.USER_GUBUN_MISMATCH)) {
                                    showMessageDialog(title, msgMismatch, clickOK -> hideMessageDialog(), null);

                                } else if (response.body().msg.equals(Constants.USER_NOT_JEET_MEMBER)) {
                                    showMessageDialog(title, msgNotJeetMember, clickOK -> hideMessageDialog(), null);
                                }
                            } else if (response.code() == 404 || response.code() == 401) {
                                // {"msg":"NOT_FOUND_MEMBER"}
                                Toast.makeText(mContext, R.string.login_not_found_member, Toast.LENGTH_SHORT).show();
//                                Utils.refreshPushToken(mContext, PreferenceUtil.getUserSeq(mContext), "");
                                PreferenceUtil.setPrefPushToken(mContext, "");
                                PreferenceUtil.setUserSeq(mContext, -1);
                                PreferenceUtil.setLoginType(mContext, Constants.LOGIN_TYPE_NORMAL);
                                PreferenceUtil.setUserId(mContext, "");
                                PreferenceUtil.setUserPw(mContext, "");
                                PreferenceUtil.setSNSUserId(mContext, "");
                                PreferenceUtil.setAutoLogin(mContext, false);
                            } else {
                                Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                        }
                    }
                }
                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    hideProgressDialog();
                    LogMgr.e(TAG, "requestLogin() onFailure >> " + t.getMessage());
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void requestLoginFromSns(String SnsId) {

        if(RetrofitClient.getInstance() != null) {
            showProgressDialog();
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.signInSNS(SnsId, Constants.APP_TYPE).enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    hideProgressDialog();
                    if(response.isSuccessful()) {
                        PreferenceUtil.setAutoLogin(mContext, mAutoLoginCb.isChecked());
                        if(response.body() != null) {
                            LoginResponse res = response.body();

                            //관리자, 강사여부 체크
                            if (res.data.userGubun <= Constants.USER_TYPE_TEACHER) {
                                if(res.data.sfCode > 0) {
                                    PreferenceUtil.setUserSeq(mContext, res.data.seq);
                                    PreferenceUtil.setUserGubun(mContext, res.data.userGubun);
                                    PreferenceUtil.setUserSFCode(mContext, res.data.sfCode);
                                    if(res.data.pushStatus != null){
                                        //공지사항
                                        PreferenceUtil.setNotificationAnnouncement(mContext, res.data.pushStatus.pushNotice.equals("Y"));
                                        //설명회
                                        PreferenceUtil.setNotificationSeminar(mContext, res.data.pushStatus.pushInformationSession.equals("Y"));
                                        //출석
                                        PreferenceUtil.setNotificationAttendance(mContext, res.data.pushStatus.pushAttendance.equals("Y"));
                                        //시스템알림
                                        PreferenceUtil.setNotificationSystem(mContext, res.data.pushStatus.pushSystem.equals("Y"));
                                    }else{
                                        //공지사항
                                        PreferenceUtil.setNotificationAnnouncement(mContext, true);
                                        //설명회
                                        PreferenceUtil.setNotificationSeminar(mContext, true);
                                        //출석
                                        PreferenceUtil.setNotificationAttendance(mContext, true);
                                        //시스템알림
                                        PreferenceUtil.setNotificationSystem(mContext, true);
                                    }
                                    Utils.refreshPushToken(mContext, res.data.seq);
                                    Intent intent = new Intent(mContext, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }else{
//                                    Utils.refreshPushToken(mContext, PreferenceUtil.getUserSeq(mContext), "");
                                    PreferenceUtil.setPrefPushToken(mContext, "");
                                    PreferenceUtil.setUserSeq(mContext, -1);
                                    PreferenceUtil.setLoginType(mContext, Constants.LOGIN_TYPE_NORMAL);
                                    PreferenceUtil.setUserId(mContext, "");
                                    PreferenceUtil.setUserPw(mContext, "");
                                    PreferenceUtil.setSNSUserId(mContext, "");
                                    PreferenceUtil.setAutoLogin(mContext, false);
//                                    Toast.makeText(mContext, R.string.error_message_invalid_info, Toast.LENGTH_SHORT).show();
                                    showMessageDialog(getString(R.string.dialog_title_alarm), getString(R.string.error_message_invalid_info), new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    hideMessageDialog();
                                                }
                                            },
                                            null);
                                }
                            }else{  //학부모, 학생인 경우
                                //preference 저장값 초기화
//                                Utils.refreshPushToken(mContext, PreferenceUtil.getUserSeq(mContext), "");
                                PreferenceUtil.setPrefPushToken(mContext, "");
                                PreferenceUtil.setUserSeq(mContext, -1);
                                PreferenceUtil.setLoginType(mContext, Constants.LOGIN_TYPE_NORMAL);
                                PreferenceUtil.setUserId(mContext, "");
                                PreferenceUtil.setUserPw(mContext, "");
                                PreferenceUtil.setSNSUserId(mContext, "");
                                PreferenceUtil.setAutoLogin(mContext, false);
//                                Toast.makeText(mContext, R.string.error_message_get_out, Toast.LENGTH_SHORT).show();
                                showMessageDialog(getString(R.string.dialog_title_alarm), getString(R.string.error_message_get_out), new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                hideMessageDialog();
                                            }
                                        },
                                        null);
//                                finish();

                                mAppleLogin.DeleteAccountProcess();
                            }
                        }
                    } else {

                        try {
                            LogMgr.e(TAG, "requestLogin() errBody : " + response.errorBody().string());

                            if (response.code() == 400){

                                String title = getString(R.string.dialog_title_alarm);
                                String msgMismatch = getString(R.string.msg_user_gubun_mismatch);
                                String msgNotJeetMember = getString(R.string.msg_user_0_not_jeet_member);

                                if (response.body().msg.equals(Constants.PARAMETER_BINDING_ERROR)){
                                    Toast.makeText(mContext, R.string.msg_parameter_binding_error, Toast.LENGTH_SHORT).show();

                                }else if (response.body().msg.equals(Constants.USER_GUBUN_MISMATCH)){
                                    showMessageDialog(title, msgMismatch, clickOK -> hideMessageDialog(), null);

                                }else if (response.body().msg.equals(Constants.USER_NOT_JEET_MEMBER)){
                                    showMessageDialog(title, msgNotJeetMember, clickOK -> hideMessageDialog(), null);
                                }

                                mAppleLogin.DeleteAccountProcess();

                            }else if(response.code() == 404) {
                            // {"msg":"NOT_FOUND_MEMBER"}
                            //Toast.makeText(mContext, R.string.login_not_found_member, Toast.LENGTH_SHORT).show();

                                //로그인 정보가 없을 때..
                                if(selectedSNSLoginType != -1) {
                                    Intent intent = null;
                                    intent = new Intent(mContext, AgreeTermsActivity.class);
                                    intent.putExtra(IntentParams.PARAM_LOGIN_TYPE, selectedSNSLoginType);
                                    intent.putExtra(IntentParams.PARAM_LOGIN_USER_NAME, snsName);
                                    startActivity(intent);
                                }else{
                                    mAppleLogin.DeleteAccountProcess();
                                }
                            } else {
                                Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                                mAppleLogin.DeleteAccountProcess();
                            }
                        } catch (IOException e) {
                        }
                    }
                    hideProgressDialog();
                }
                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    hideProgressDialog();
                    LogMgr.e(TAG, "requestLogin() onFailure >> " + t.getMessage());
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


//    private void hideKeyboard() {
//        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(mEditId.getWindowToken(), 0);
//        imm.hideSoftInputFromWindow(mEditPw.getWindowToken(), 0);
//    }
}