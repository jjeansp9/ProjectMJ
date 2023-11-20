package kr.jeet.edu.manager.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.model.data.ManagerInfo;
import kr.jeet.edu.manager.model.request.UpdatePushStatusRequest;
import kr.jeet.edu.manager.model.response.BaseResponse;
import kr.jeet.edu.manager.model.response.GetManagerInfoResponse;
import kr.jeet.edu.manager.server.RetrofitApi;
import kr.jeet.edu.manager.server.RetrofitClient;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.PreferenceUtil;
import kr.jeet.edu.manager.utils.Utils;
import kr.jeet.edu.manager.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SettingsActivity extends BaseActivity {

    private String TAG = SettingsActivity.class.getSimpleName();

    private TextView mTvUserGubun, mTvName, mTvPhoneNum, mTvAppVersion, mTvAppVersionBadge;
    private Switch mSwAnnouncement, mSwInformationSession, mSwAttendance, mSwSystem, mSwAll;
    private AppCompatButton btnSetAccount;
    private RetrofitApi mRetrofitApi;
    private ConstraintLayout layoutFirst, layoutSecond, layoutThird;

    private UpdatePushStatusRequest updatePushStatus;

    private String pushNotice = "";
    private String pushInformationSession = "";
    private String pushAttendance = "";
    private String pushSystem = "";

    private final String CHECKED_OK = "Y";
    private final String CHECKED_CANCEL = "N";

    private int _memberSeq = 0;
    private int _sfCode = 0;
    private int _userGubun = 0;
    private String userGubun = "";

    private int pushSeq = 0;
    private AppCompatActivity mActivity = null;
    private String url = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mContext = this;
        initData();
        initAppbar();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestMemberInfo(_memberSeq, _sfCode);
    }

    private void initData(){
        _memberSeq = PreferenceUtil.getUserSeq(mContext);
        _sfCode = PreferenceUtil.getUserSFCode(mContext);
        _userGubun = PreferenceUtil.getUserGubun(mContext);

        updatePushStatus = new UpdatePushStatusRequest();

        switch (_userGubun){
            case Constants.USER_TYPE_SUPER_ADMIN:
                userGubun = getString(R.string.superadmin);
                break;
            case Constants.USER_TYPE_ADMIN:
                userGubun = getString(R.string.managers);
                break;

            case Constants.USER_TYPE_TEACHER:
                userGubun = getString(R.string.teachers);
                break;
        }
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.settings_title);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    void initView() {
        mTvUserGubun = (TextView) findViewById(R.id.tv_set_user_gubun);
        mTvName = (TextView) findViewById(R.id.tv_set_name);
        mTvPhoneNum = (TextView) findViewById(R.id.tv_set_phone_num);
        if(_userGubun == Constants.USER_TYPE_SUPER_ADMIN) {
            mTvPhoneNum.setVisibility(View.GONE);
        }else{
            mTvPhoneNum.setVisibility(View.VISIBLE);
        }
        mTvAppVersionBadge = (TextView) findViewById(R.id.tv_app_version_update);
        mTvAppVersion = (TextView) findViewById(R.id.tv_app_version);

        layoutFirst = findViewById(R.id.layout_first);
        layoutSecond = findViewById(R.id.layout_second);
        layoutThird = findViewById(R.id.layout_third);

        mSwAnnouncement = (Switch) findViewById(R.id.sw_set_announcement_state);
        mSwInformationSession = (Switch) findViewById(R.id.sw_set_information_session_state);
        mSwAttendance = (Switch) findViewById(R.id.sw_set_attendance_state);
        mSwSystem = (Switch) findViewById(R.id.sw_set_system_state);
        mSwAll = (Switch) findViewById(R.id.sw_set_all);

        mSwAnnouncement.setOnClickListener(this);
        mSwInformationSession.setOnClickListener(this);
        mSwAttendance.setOnClickListener(this);
        mSwSystem.setOnClickListener(this);
        mSwAll.setOnClickListener(this);

        setSwitch();

        findViewById(R.id.layout_set_operation_policy).setOnClickListener(this);
        findViewById(R.id.layout_privacy_policy).setOnClickListener(this);
        findViewById(R.id.tv_set_app_info).setOnClickListener(this);

        btnSetAccount = findViewById(R.id.btn_set_account);
        if(_userGubun == Constants.USER_TYPE_SUPER_ADMIN) {
            btnSetAccount.setText(R.string.settings_logout);
        }else{
            btnSetAccount.setText(R.string.settings_account_title);
        }
        btnSetAccount.setOnClickListener(this);

        try {
            PackageManager packageManager = getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            String versionName = packageInfo.versionName;

            if (versionName != null) mTvAppVersion.setText("v"+versionName);
            else mTvAppVersionBadge.setVisibility(View.GONE);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        long delayed = getResources().getInteger(R.integer.screen_in_time);
        new Handler().postDelayed(() -> {
            animateLayout(layoutFirst);
            Utils.animateLayoutMoveLeft(layoutFirst, mContext);
        }, delayed);
    }

    public void setSwitch() {

        mSwAll.setOnCheckedChangeListener((view, isChecked) -> {
            if(mSwAll.isChecked()) checkAll(true);
            else checkAll(false);
        });

        mSwAnnouncement.setOnCheckedChangeListener((view, isChecked) -> checkConfirm());
        mSwInformationSession.setOnCheckedChangeListener((view, isChecked) -> checkConfirm());
        mSwAttendance.setOnCheckedChangeListener((view, isChecked) -> checkConfirm());
        mSwSystem.setOnCheckedChangeListener((view, isChecked) -> checkConfirm());
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);

        switch (view.getId()) {
            case R.id.btn_set_account:
                if(_userGubun == Constants.USER_TYPE_SUPER_ADMIN) {
                    showMessageDialog(getString(R.string.settings_logout), getString(R.string.msg_confirm_logout), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    hideMessageDialog();
                                    requestLogOut(_memberSeq);
                                }
                            },
                            new View.OnClickListener(){
                                @Override
                                public void onClick(View view) {
                                    hideMessageDialog();
                                }
                            });
                }else {
                    navigateAccountActivity();
                }
                break;

            case R.id.sw_set_announcement_state:
            case R.id.sw_set_attendance_state:
            case R.id.sw_set_information_session_state:
            case R.id.sw_set_system_state:
                checkConfirm();
                break;

            case R.id.sw_set_all:
                if(mSwAll.isChecked()) checkAll(true);
                else checkAll(false);
                break;

            case R.id.layout_set_operation_policy:
                url = RetrofitApi.SERVER_BASE_URL+"web/api/policy/service";
                navigate2WebViewActivity(mContext.getString(R.string.terms_agree));
                break;

            case R.id.layout_privacy_policy:
                url = RetrofitApi.SERVER_BASE_URL+"web/api/policy/privacy";
                navigate2WebViewActivity(mContext.getString(R.string.terms_agreement_private_info));
                break;

            case R.id.tv_set_app_info:
                break;
        }

    }

    private void checkConfirm(){
        requestUpdatePush();
        if (mSwAnnouncement.isChecked() &&
                mSwInformationSession.isChecked() &&
                mSwAttendance.isChecked() &&
                mSwSystem.isChecked()) {
            setSwAll(true);

        } else {
            setSwAll(false);
        }
    }

    private void setSwAll (boolean isCheck) {
        mSwAll.setOnCheckedChangeListener(null);
        mSwAll.setChecked(isCheck);
        mSwAll.setOnCheckedChangeListener((view, isChecked) -> {
            if(mSwAll.isChecked()) checkAll(true);
            else checkAll(false);
        });
    }

    private void checkAll(boolean setCheck){
        mSwAnnouncement.setChecked(setCheck);
        mSwInformationSession.setChecked(setCheck);
        mSwAttendance.setChecked(setCheck);
        mSwSystem.setChecked(setCheck);

        requestUpdatePush();
    }

    // 강사 정보 조회
    private void requestMemberInfo(int managerSeq, int sfCode){
        if(RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.getManagerInfo(managerSeq, sfCode).enqueue(new Callback<GetManagerInfoResponse>() {
                @Override
                public void onResponse(Call<GetManagerInfoResponse> call, Response<GetManagerInfoResponse> response) {
                    try {
                        if (response.isSuccessful()){
                            ManagerInfo getData = new ManagerInfo();
                            if (response.body() != null)  getData= response.body().data;

                            if (getData != null) {

                                mTvName.setText(getData.name); // 원생 오리지널 이름
                                mTvUserGubun.setText(userGubun);
                                mTvUserGubun.setVisibility(View.VISIBLE);
                                if(_userGubun >= Constants.USER_TYPE_ADMIN) {
                                    if (getData.phoneNumber != null)
                                        mTvPhoneNum.setText(Utils.formatPhoneNumber(getData.phoneNumber));
                                    else mTvPhoneNum.setText(getText(R.string.empty_phonenumber));
                                }
                                pushSeq = getData.pushStatus.seq;

                                mSwAnnouncement.setChecked(getData.pushStatus.pushNotice.equals(CHECKED_OK));
                                mSwInformationSession.setChecked(getData.pushStatus.pushInformationSession.equals(CHECKED_OK));
                                mSwAttendance.setChecked(getData.pushStatus.pushAttendance.equals(CHECKED_OK));
                                mSwSystem.setChecked(getData.pushStatus.pushSystem.equals(CHECKED_OK));

                                pushNotice = getData.pushStatus.pushNotice;
                                pushInformationSession = getData.pushStatus.pushInformationSession;
                                pushAttendance = getData.pushStatus.pushAttendance;
                                pushSystem = getData.pushStatus.pushSystem;
                            }

                        }else{
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                            LogMgr.e(TAG, "requestMemberInfo() errBody : " + response.errorBody().string());
                        }

                    }catch (Exception e){ LogMgr.e(TAG + "requestMemberInfo() Exception : ", e.getMessage()); }
                    checkConfirm();
                }

                @Override
                public void onFailure(Call<GetManagerInfoResponse> call, Throwable t) {
                    try { LogMgr.e(TAG, "requestMemberInfo() onFailure >> " + t.getMessage()); }
                    catch (Exception e) { LogMgr.e(TAG + "requestMemberInfo() Exception : ", e.getMessage()); }
                    checkConfirm();
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void requestUpdatePush(){

        if (mSwAnnouncement.isChecked()) pushNotice = CHECKED_OK;
        else pushNotice = CHECKED_CANCEL;
        if (mSwInformationSession.isChecked()) pushInformationSession = CHECKED_OK;
        else pushInformationSession = CHECKED_CANCEL;
        if (mSwAttendance.isChecked()) pushAttendance = CHECKED_OK;
        else pushAttendance = CHECKED_CANCEL;
        if (mSwSystem.isChecked()) pushSystem = CHECKED_OK;
        else pushSystem = CHECKED_CANCEL;

        updatePushStatus.seq = pushSeq;
        updatePushStatus.pushNotice = pushNotice;
        updatePushStatus.pushInformationSession = pushInformationSession;
        updatePushStatus.pushAttendance = pushAttendance;
        updatePushStatus.pushSystem = pushSystem;

        if(RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.updatePushStatus(updatePushStatus).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    if (response.isSuccessful()){
                        Log.i("updateTest", response.body().msg);
                        //공지사항
                        PreferenceUtil.setNotificationAnnouncement(SettingsActivity.this, pushNotice.equals("Y"));
                        //설명회
                        PreferenceUtil.setNotificationSeminar(SettingsActivity.this, pushInformationSession.equals("Y"));
                        //출석
                        PreferenceUtil.setNotificationAttendance(SettingsActivity.this, pushAttendance.equals("Y"));
                        //시스템알림
                        PreferenceUtil.setNotificationSystem(SettingsActivity.this, pushSystem.equals("Y"));
//                        if(isUpdateSubscribeTopic) {
//                            Utils.requestUpdatePushTopic(mContext, _memberSeq);
//                        }
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {

                }
            });
        }
    }
    // 로그아웃
    private void requestLogOut(int managerSeq){
        if(RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.logout(managerSeq).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    try {
                        if (response.isSuccessful()){
//                            Utils.refreshPushToken(mContext, PreferenceUtil.getUserSeq(mContext), "");
                            PreferenceUtil.setPrefPushToken(mContext, "");
                            PreferenceUtil.setUserSeq(mContext, -1);
                            PreferenceUtil.setLoginType(mContext, Constants.LOGIN_TYPE_NORMAL);
                            PreferenceUtil.setUserId(mContext, "");
                            PreferenceUtil.setUserPw(mContext, "");
                            PreferenceUtil.setSNSUserId(mContext, "");
                            PreferenceUtil.setAutoLogin(mContext, false);
                            Intent intent = new Intent(mContext, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }else{
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                            LogMgr.e(TAG, "requestMemberInfo() errBody : " + response.errorBody().string());
                        }

                    }catch (Exception e){ LogMgr.e(TAG + "requestMemberInfo() Exception : ", e.getMessage()); }
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    try { LogMgr.e(TAG, "requestMemberInfo() onFailure >> " + t.getMessage()); }
                    catch (Exception e) { LogMgr.e(TAG + "requestMemberInfo() Exception : ", e.getMessage()); }

                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void animateLayout(final View view) {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(Constants.LAYOUT_ANIM_DURATION);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());

        animator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            view.setAlpha(progress); // 애니메이션 중간값을 알파값으로 설정하여 서서히 보이도록 함
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // 애니메이션 종료 후 다음 레이아웃으로 전환
                if (view == layoutFirst) {
                    animateLayout(layoutSecond);
                    Utils.animateLayoutMoveLeft(layoutSecond, mContext);
                }
                else if (view == layoutSecond) {
                    animateLayout(layoutThird);
                    Utils.animateLayoutMoveLeft(layoutThird, mContext);
                }
            }
        });
        animator.start();
    }
    private void navigateAccountActivity(){
        startActivity(new Intent(mContext, SetAccountActivity.class));
        overridePendingTransition(R.anim.vertical_enter, R.anim.none);
    }
    private void navigate2WebViewActivity(String title){
        Intent intent = new Intent(mContext, WebViewActivity.class);
        intent.putExtra(IntentParams.PARAM_APPBAR_TITLE, title);
        intent.putExtra(IntentParams.PARAM_WEB_VIEW_URL, url);
        startActivity(intent);
    }
}