package kr.jeet.edu.manager.activity;

import static kr.jeet.edu.manager.common.Constants.USER_TYPE_STUDENT;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.activity.login.LoginActivity;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.DataManager;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.db.PushMessage;
import kr.jeet.edu.manager.model.request.SigninRequest;
import kr.jeet.edu.manager.model.response.LoginResponse;
import kr.jeet.edu.manager.server.RetrofitApi;
import kr.jeet.edu.manager.server.RetrofitClient;
import kr.jeet.edu.manager.sns.AppleLoginManager;
import kr.jeet.edu.manager.sns.GoogleLoginManager;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.PreferenceUtil;
import kr.jeet.edu.manager.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import kr.jeet.edu.manager.sns.KaKaoLoginManager;
import kr.jeet.edu.manager.sns.NaverLoginManager;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.android.play.core.tasks.Task;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import java.util.List;

public class IntroActivity extends BaseActivity {

    private String TAG = IntroActivity.class.getSimpleName();

    private RetrofitApi mRetrofitApi;

    // handler
    private final int HANDLER_CHECK_UPDATE = 1;  // 업데이트
    private final int HANDLER_CHECK_PERMISSION = 2;  // check permission
    private final int HANDLER_START_INTRO = 3;  // intro 시작
    private final int HANDLER_AUTO_LOGIN = 4;  // 자동로그인
    private final int HANDLER_REQUEST_LOGIN = 5;       // 로그인 화면으로 이동

    private NaverLoginManager mNaverLogin = null;
    private KaKaoLoginManager mKaKaoLogin = null;
    private GoogleLoginManager mGoogleLogin = null;
    private AppleLoginManager mAppleLogin = null;

    private AppCompatActivity mActivity = null;
    private PushMessage _pushMessage = null;

    private AppUpdateManager appUpdateManager = null;
    private final int REQUEST_INAPP_UPDATE = 1000;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case HANDLER_CHECK_UPDATE :
                    checkUpdate();
                    break;
                case HANDLER_CHECK_PERMISSION:
                    checkPermissions();
                    break;
                case HANDLER_START_INTRO:
                    startIntro();
                    break;
                case HANDLER_AUTO_LOGIN :
                    int loginType = PreferenceUtil.getLoginType(mContext);
                    LogMgr.e(TAG,"LoginType = " + loginType);

                    if(loginType == Constants.LOGIN_TYPE_NORMAL) {
                        String userId = PreferenceUtil.getUserId(mContext);
                        String userPw = PreferenceUtil.getUserPw(mContext);
                        LogMgr.e("id=" + userId + " / pw=" + userPw);
                        if(!userId.isEmpty() && !userPw.isEmpty()) {
                            SigninRequest request = new SigninRequest();
                            request.id = userId;
                            request.pw = userPw;
                            request.userGubun = PreferenceUtil.getUserGubun(mContext);

                            requestLogin(request);
                        } else {
                            Toast.makeText(mContext, R.string.login_not_found_member, Toast.LENGTH_SHORT).show();
                            startLogin();
                        }
                    }
                    else {
                        String SnsUserId = PreferenceUtil.getSNSUserId(mContext);
                        LogMgr.e(TAG,"SnsUserId = " + SnsUserId);

                        if(SnsUserId != null && !SnsUserId.equals("")) requestLoginFromSns(SnsUserId);
                        else emptyUserInfo();

//                        if(loginType == Constants.LOGIN_TYPE_SNS_NAVER) {
//                            if(SnsUserId != null && !SnsUserId.equals("")) {
//                                mNaverLogin = new NaverLoginManager(mContext);
//                                mNaverLogin.setHandler(mHandler);
//                                mNaverLogin.LoginProcess();
//                            }
//                            else { emptyUserInfo(); }
//                        }
//                        else if(loginType == Constants.LOGIN_TYPE_SNS_KAKAO) {
//                            if(SnsUserId != null && !SnsUserId.equals("")) {
//                                mKaKaoLogin = new KaKaoLoginManager(mContext);
//                                mKaKaoLogin.setHandler(mHandler);
//                                mKaKaoLogin.LoginProcess();
//                            }
//                            else { emptyUserInfo(); }
//                        }
//                        else if(loginType == Constants.LOGIN_TYPE_SNS_GOOGLE) {
//                            if (SnsUserId != null && !SnsUserId.equals("")){
//                                mGoogleLogin.setHandler(mHandler);
//                                mGoogleLogin.LoginProcess();
//                            }
//                            else { emptyUserInfo(); }
//                        }
//                        else if(loginType == Constants.LOGIN_TYPE_SNS_APPLE) {
//                            if (SnsUserId != null && !SnsUserId.equals("")){
//                                mAppleLogin = new AppleLoginManager(mActivity);
//                                mAppleLogin.setHandler(mHandler);
//                                mAppleLogin.LoginProcess();
//                            }
//                            else { emptyUserInfo(); }
//                        }
                    }

                    break;

                case HANDLER_REQUEST_LOGIN :
                    startLogin();
                    break;

//                case Constants.HANDLER_SNS_LOGIN_COMPLETE:
//                    LogMgr.e(TAG, "SNS_LOGIN_COMPLETE");
//
//                    String snsId = (String) msg.obj;
//                    if(snsId != null && !snsId.isEmpty()) {
//                        requestLoginFromSns(snsId);
//                    }
//                    break;
            }
        }
    };

    private void emptyUserInfo(){
        Toast.makeText(mContext, R.string.empty_user_info, Toast.LENGTH_SHORT).show();
        //Login 화면으로..
        startActivity(new Intent(mContext, LoginActivity.class));
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        setStatusAndNavigatinBar(true);
        mContext = this;
        mActivity = this;
        mGoogleLogin = new GoogleLoginManager(mActivity);

        // todo. 앱 버전 체크
        // todo. 권한 처리
        initView();
        initIntentData();
        mHandler.sendEmptyMessage(HANDLER_CHECK_UPDATE);
    }
    private void checkUpdate() {
        if(appUpdateManager == null) {
            try {
                appUpdateManager = AppUpdateManagerFactory.create(mContext);
                Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

                appUpdateInfoTask.addOnSuccessListener(updateInfo -> {
                    if (updateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                        try {
                            requestVersionUpdate(updateInfo);
                        } catch (Exception e) {
                            LogMgr.e(TAG, "FAIL REQUEST UPDATE\n" + Log.getStackTraceString(e));
                        }
                    }else{
                        mHandler.sendEmptyMessage(HANDLER_CHECK_PERMISSION);
                    }
                });
                appUpdateInfoTask.addOnFailureListener(e -> {
                    LogMgr.e(TAG, "FAIL REQUEST APP_UPDATE_INFO\n" + Log.getStackTraceString(e));
                    mHandler.sendEmptyMessage(HANDLER_CHECK_PERMISSION);
                });
            }catch(Exception e) {
                e.printStackTrace();
                mHandler.sendEmptyMessage(HANDLER_CHECK_PERMISSION);
            }
        } else {
            mHandler.sendEmptyMessage(HANDLER_CHECK_PERMISSION);
        }
    }

    private void requestVersionUpdate(AppUpdateInfo appUpdateInfo) throws IntentSender.SendIntentException {
        if (appUpdateManager != null) {
            appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo, AppUpdateType.IMMEDIATE, IntroActivity.this,
                    REQUEST_INAPP_UPDATE
            );
        }
    }
    private void initIntentData() {
        Intent intent = getIntent();
        if(intent != null) {
//            if(intent.hasExtra(IntentParams.PARAM_PUSH_MESSAGE)){
//                LogMgr.e(TAG, "push msg ");
//                _pushMessage = intent.getParcelableExtra(IntentParams.PARAM_PUSH_MESSAGE);
//                LogMgr.e(TAG, "msg = " + _pushMessage.body);
//            }else{
//                LogMgr.e(TAG, "push msg is null");
//            }
            Bundle bundle = intent.getExtras();
            if(bundle != null) {
//                Bundle bundle = intent.getExtras();
//                if(bundle.containsKey(IntentParams.PARAM_PUSH_MESSAGE)){
//                    LogMgr.e(TAG, "push msg ");
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                        _pushMessage = bundle.getSerializable(IntentParams.PARAM_PUSH_MESSAGE, PushMessage.class);
//                    }else{
//                        _pushMessage = (PushMessage) bundle.getSerializable(IntentParams.PARAM_PUSH_MESSAGE);
//                    }
//                    LogMgr.e(TAG, "msg = " + _pushMessage.body);
//                }else{
//                    LogMgr.e(TAG, "push msg is null");
//                }
                _pushMessage = Utils.getSerializableExtra(bundle, IntentParams.PARAM_PUSH_MESSAGE, PushMessage.class);
//                for (String key : map.keySet()) {
//                    LogMgr.e(TAG, "key = " + key + " : value = " + map.get(key));
//                }
            }
        }else{
            LogMgr.e(TAG, "intent is null");
        }
    }
    void initAppbar() {
        //do nothing
    }
    void initView() {
        //DOT Size
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenHeight = displayMetrics.heightPixels;
        int radius = screenHeight / 200;
        DataManager.getInstance().DOT_SIZE = radius;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(appUpdateManager == null) return;
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(
                new OnSuccessListener<AppUpdateInfo>() {
                    @Override
                    public void onSuccess(AppUpdateInfo appUpdateInfo) {
                        if (appUpdateInfo.updateAvailability()
                                == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                            // 인 앱 업데이트가 이미 실행중이었다면 계속해서 진행하도록
                            try {
                                appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, IntroActivity.this, REQUEST_INAPP_UPDATE);
                            } catch (IntentSender.SendIntentException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    private void checkPermissions() {
        String[] requiredPermissions = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissions = new String[]{
                    Manifest.permission.READ_PHONE_NUMBERS,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.POST_NOTIFICATIONS
            };
        } else {
            requiredPermissions = new String[]{
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
        }

        TedPermission.create()
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        LogMgr.e(TAG, "permission granted");
                        mHandler.sendEmptyMessage(HANDLER_START_INTRO);
                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {
                        LogMgr.e(TAG, "denied permission = " + deniedPermissions);
                        showMessageDialog(getString(R.string.dialog_title_alarm), getString(R.string.intro_failed_request_permission), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        hideMessageDialog();
                                        finish();
                                    }
                                },
                                null);
                    }
                })
                .setPermissions(requiredPermissions)
                .setDeniedMessage(getString(R.string.msg_intro_denied_permission))
                .setDeniedCloseButtonText(getString(R.string.title_permission_close))
                .setGotoSettingButton(true)
                .setGotoSettingButtonText(getString(R.string.title_permission_setting))
                .check();
    }
    private void startIntro() {
        LogMgr.e(TAG, PreferenceUtil.getUserGubun(this) + " / " + PreferenceUtil.getUserSeq(this));
        //푸쉬전송을 눌러서 들어온 경우에 로그인이 되어 있을 때
        if(_pushMessage != null && PreferenceUtil.getUserGubun(this) < USER_TYPE_STUDENT && PreferenceUtil.getUserSeq(this) != -1) {  //isFromPush
            Intent intent = new Intent(this, MainActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(IntentParams.PARAM_PUSH_MESSAGE, _pushMessage);
            intent.putExtras(bundle);
//            intent.putExtra(IntentParams.PARAM_PUSH_MESSAGE, _pushMessage);
            startActivity(intent);
            finish();

        }else { //일반 실행
            long delayTime = 1000;
            if (PreferenceUtil.getAutoLogin(mContext)) {
                // 자동 로그인
                mHandler.sendEmptyMessageDelayed(HANDLER_AUTO_LOGIN, delayTime);
            } else {
                mHandler.sendEmptyMessageDelayed(HANDLER_REQUEST_LOGIN, delayTime);
            }
        }
    }

    private void startLogin() {
        startActivity(new Intent(mContext, LoginActivity.class));
//        startActivity(new Intent(mContext, MainActivity.class));
        finish();
    }

    private void requestLogin(SigninRequest request) {

        if(RetrofitClient.getInstance() != null) {
            showProgressDialog();
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.signIn(request.id, request.pw).enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    hideProgressDialog();
                    if(response.isSuccessful()) {
                        if(response.body() != null) {
                            PreferenceUtil.setLoginType(mContext, Constants.LOGIN_TYPE_NORMAL);
                            LoginResponse res = response.body();
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

                            }
                            //관리자, 강사여부 체크
                            else if (res.data.userGubun <= Constants.USER_TYPE_TEACHER) {
                                if(res.data.sfCode > 0) {
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
                                            startLogin();
                                            hideMessageDialog();
                                            finish();
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
                                                startLogin();
                                                hideMessageDialog();
                                                finish();
                                            }
                                        },
                                        null);
                            }
                        }

                    } else {
//                        Utils.refreshPushToken(mContxt, PreferenceUtil.getUserSeq(mContext), "");
                        PreferenceUtil.setPrefPushToken(mContext, "");
                        PreferenceUtil.setUserSeq(mContext, -1);
                        PreferenceUtil.setLoginType(mContext, Constants.LOGIN_TYPE_NORMAL);
                        PreferenceUtil.setUserId(mContext, "");
                        PreferenceUtil.setUserPw(mContext, "");
                        PreferenceUtil.setSNSUserId(mContext, "");
                        PreferenceUtil.setAutoLogin(mContext, false);

                        if(response.code() == 404) {
                            // {"msg":"NOT_FOUND_MEMBER"}
                            Toast.makeText(mContext, R.string.login_not_found_member, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();

                        }
                        startLogin();
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    hideProgressDialog();
                    LogMgr.e(TAG, "requestLogin() onFailure >> " + t.getMessage());
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                    startLogin();
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


                        LoginResponse res = response.body();
                        PreferenceUtil.setUserSeq(mContext, res.data.seq);
                        PreferenceUtil.setUserGubun(mContext, res.data.userGubun);
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
                        startActivity(new Intent(mContext, MainActivity.class));
                        finish();

                    } else {
//                        Utils.refreshPushToken(mContext, PreferenceUtil.getUserSeq(mContext), "");
                        PreferenceUtil.setPrefPushToken(mContext, "");
                        PreferenceUtil.setUserSeq(mContext, -1);
                        PreferenceUtil.setLoginType(mContext, Constants.LOGIN_TYPE_NORMAL);
                        PreferenceUtil.setUserId(mContext, "");
                        PreferenceUtil.setUserPw(mContext, "");
                        PreferenceUtil.setSNSUserId(mContext, "");
                        PreferenceUtil.setAutoLogin(mContext, false);

                        if(response.code() == 404) {
                            // {"msg":"NOT_FOUND_MEMBER"}
                            Toast.makeText(mContext, R.string.login_not_found_member, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();

                        }
                        startLogin();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_INAPP_UPDATE) {
            if (resultCode != RESULT_OK) {
                Log.d("AppUpdate", "Update flow failed! Result code: " + resultCode); //
                finishAffinity(); // 앱 종료
                Toast.makeText(mContext, R.string.msg_inappupdate_fail, Toast.LENGTH_SHORT).show();
            }
        }
    }
}