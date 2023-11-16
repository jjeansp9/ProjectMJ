package kr.jeet.edu.manager.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.dialog.Confirm2LeaveDialog;
import kr.jeet.edu.manager.dialog.PopupDialog;
import kr.jeet.edu.manager.model.data.ManagerInfo;
import kr.jeet.edu.manager.model.response.BaseResponse;
import kr.jeet.edu.manager.model.response.GetManagerInfoResponse;
import kr.jeet.edu.manager.server.RetrofitApi;
import kr.jeet.edu.manager.server.RetrofitClient;
import kr.jeet.edu.manager.sns.AppleLoginManager;
import kr.jeet.edu.manager.sns.GoogleLoginManager;
import kr.jeet.edu.manager.sns.KaKaoLoginManager;
import kr.jeet.edu.manager.sns.NaverLoginManager;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.PreferenceUtil;
import kr.jeet.edu.manager.utils.Utils;
import kr.jeet.edu.manager.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SetAccountActivity extends BaseActivity {

    private String TAG = SetAccountActivity.class.getSimpleName();

    private RetrofitApi mRetrofitApi;
    private ConstraintLayout layoutUpdateProfile, layoutUpdatePhoneNo, layoutUpdatePW, layoutUpdateSNS;
    private TextView mTvUserGubun, mTvName, mTvUpdatePhoneNum, mTvPhoneNum, mTvUpdatePw, mTvUpdateSns, mTvUpdateProfile;
    private ImageView mImgSns;
    private View line_1, line_2, line_3;
    private EditText mEditPw;

    private int _memberSeq = 0;
    private int _sfCode = 0;
    private int _userGubun = 0;
    private int _loginType = 0;
    private String userGubun = "";
    private String phoneNum = "";
    private String memberName = "";
    private int stCodeParent = 0;

    private final String MEMBER = "Y";
    Confirm2LeaveDialog _leaveDialog;

    private NaverLoginManager mNaverLogin = null;
    private KaKaoLoginManager mKaKaoLogin = null;
    private GoogleLoginManager mGoogleLogin = null;
    private AppleLoginManager mAppleLogin = null;
    private AppCompatActivity mActivity = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_account);
        mActivity = this;
        mContext = this;
        initData();
        initAppbar();
        initView();
        setAnimMove(Constants.MOVE_DOWN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 부모인 경우 자신의 이름을 가져오기 위해 자신의 seq, stCode는 0으로
        requestMemberInfo(_memberSeq, _sfCode);
    }

    private void initData(){
        _memberSeq = PreferenceUtil.getUserSeq(mContext);
        _sfCode = PreferenceUtil.getUserSFCode(mContext);
        _userGubun = PreferenceUtil.getUserGubun(mContext);
        _loginType = PreferenceUtil.getLoginType(mContext);

        switch (_userGubun){
            case Constants.USER_TYPE_ADMIN:
                userGubun = getString(R.string.managers);
                break;

            case Constants.USER_TYPE_TEACHER:
                userGubun = getString(R.string.teachers);
                break;
        }
    }

    /**
     * 타입별 보여지는 View
     *
     * 회원 일반 : 비밀번호 수정
     * 비회원 일반 : 프로필정보 수정, 전화번호 수정, 비밀번호 수정
     * SNS 비회원 : 프로필정보 수정, 전화번호 수정, SNS 로그인
     * SNS 회원 : SNS 로그인
     * */
    @Override
    void initView() {
        mTvName = (TextView) findViewById(R.id.tv_account_name);
        mTvUserGubun = (TextView) findViewById(R.id.tv_account_user_gubun);
        mTvUpdateProfile = (TextView) findViewById(R.id.tv_update_profile);
        mTvUpdatePhoneNum = (TextView) findViewById(R.id.tv_update_phone_num);
        mTvPhoneNum = (TextView) findViewById(R.id.tv_account_phone_num);
        mTvUpdatePw = (TextView) findViewById(R.id.tv_update_pw);
        mTvUpdateSns = (TextView) findViewById(R.id.tv_update_sns_login);

        mImgSns = (ImageView) findViewById(R.id.img_update_sns_login);

        switch(PreferenceUtil.getLoginType(mContext)){
            case Constants.LOGIN_TYPE_NORMAL:
                mImgSns.setVisibility(View.GONE);
                break;
            case Constants.LOGIN_TYPE_SNS_NAVER:
                mImgSns.setImageResource(R.drawable.btn_sns_naver);
                break;
            case Constants.LOGIN_TYPE_SNS_KAKAO:
                mImgSns.setImageResource(R.drawable.btn_sns_kakao);
                break;
            case Constants.LOGIN_TYPE_SNS_GOOGLE:
                mImgSns.setImageResource(R.drawable.btn_sns_google);
                break;
            case Constants.LOGIN_TYPE_SNS_APPLE:
                mImgSns.setImageResource(R.drawable.btn_sns_apple);
                break;
        }

        LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout customDialogLayout= (RelativeLayout) vi.inflate(R.layout.custom_dialog, null, false);

        mEditPw = customDialogLayout.findViewById(R.id.edit);

//        Utils.setFocusChangeListener(mEditPw, R.drawable.et_sign_up_inputbox_sel, R.drawable.et_sign_up_inputbox_nor);

        line_1 = findViewById(R.id.line_account_first);
        line_2 = findViewById(R.id.line_account_second);
        line_3 = findViewById(R.id.line_account_third);

        layoutUpdateProfile = findViewById(R.id.layout_update_profile);
        layoutUpdateProfile.setOnClickListener(this);
        layoutUpdatePhoneNo = findViewById(R.id.layout_update_phone_num);
        layoutUpdatePhoneNo.setOnClickListener(this);
        layoutUpdatePW = findViewById(R.id.layout_update_pw);
        layoutUpdatePW.setOnClickListener(this);
        layoutUpdateSNS = findViewById(R.id.layout_update_sns_login);
        layoutUpdateSNS.setOnClickListener(this);
        findViewById(R.id.layout_account_un_link).setOnClickListener(this);
        findViewById(R.id.layout_account_logout).setOnClickListener(this);

        int loginType = PreferenceUtil.getLoginType(mContext);
//        String isOriginalMember = PreferenceUtil.getUserIsOriginal(mContext);

        if (loginType == Constants.LOGIN_TYPE_NORMAL){ // 일반 로그인인 경우
//            if (isOriginalMember.equals(MEMBER)){ // 회원
            layoutUpdateProfile.setVisibility(View.GONE);
            mTvUpdateProfile.setVisibility(View.GONE);
            layoutUpdatePhoneNo.setVisibility(View.VISIBLE);
            mTvUpdatePhoneNum.setVisibility(View.VISIBLE);
            mTvPhoneNum.setVisibility(View.VISIBLE);
            layoutUpdateSNS.setVisibility(View.GONE);
            mTvUpdateSns.setVisibility(View.GONE);
            line_1.setVisibility(View.GONE);
            line_2.setVisibility(View.VISIBLE);
            line_3.setVisibility(View.GONE);
//            }
//            else{ // 비회원
//                mTvUpdateSns.setVisibility(View.GONE);
//            }


        }else{ // sns 로그인인 경우
            layoutUpdateProfile.setVisibility(View.GONE);
            mTvUpdateProfile.setVisibility(View.GONE);
            layoutUpdatePhoneNo.setVisibility(View.VISIBLE);
            mTvUpdatePhoneNum.setVisibility(View.VISIBLE);
            mTvPhoneNum.setVisibility(View.VISIBLE);
            layoutUpdatePW.setVisibility(View.GONE);
            mTvUpdatePw.setVisibility(View.GONE);
            line_1.setVisibility(View.GONE);
            line_2.setVisibility(View.VISIBLE);
            line_3.setVisibility(View.GONE);
        }
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.settings_account_title);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    private PopupDialog popupDialog = null;
    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.layout_update_profile:
//                Intent intent = new Intent(mContext, SetProfileInfoActivity.class);
//                intent.putExtra(IntentParams.PARAM_LOGIN_USER_NAME, memberName);
//                startActivity(intent);
                break;

            case R.id.layout_update_phone_num:
                startActivity(new Intent(mContext, SetPhoneNumActivity.class));
                break;

            case R.id.layout_update_pw:
                showDialog();
                break;

            case R.id.layout_update_sns_login:
                break;

            case R.id.layout_account_un_link:
                showConfirm2LeaveDialog();
                break;

            case R.id.layout_account_logout:
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

                break;
        }
    }
    private void showConfirm2LeaveDialog() {
        if(_leaveDialog != null && _leaveDialog.isShowing()) {
            _leaveDialog.dismiss();
        }
        _leaveDialog = new Confirm2LeaveDialog(mContext);
        _leaveDialog.setOnOkButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideConfirm2LeaveDialog();
                switch(_loginType){
                    case Constants.LOGIN_TYPE_NORMAL:
                        requestLeave(_memberSeq);
                        break;
                    case Constants.LOGIN_TYPE_SNS_NAVER:
                        mNaverLogin = new NaverLoginManager(mContext);
                        mNaverLogin.DeleteAccountProcess();
                        requestLeave(_memberSeq);
                        break;
                    case Constants.LOGIN_TYPE_SNS_KAKAO:
                        mKaKaoLogin = new KaKaoLoginManager(mContext);
                        mKaKaoLogin.DeleteAccountProcess();
                        requestLeave(_memberSeq);
                        break;
                    case Constants.LOGIN_TYPE_SNS_GOOGLE:
                        mGoogleLogin.DeleteAccountProcess();
                        requestLeave(_memberSeq);
                        break;
                    case Constants.LOGIN_TYPE_SNS_APPLE:
                        mAppleLogin = new AppleLoginManager(mActivity);
                        mAppleLogin.DeleteAccountProcess();
                        requestLeave(_memberSeq);
                        break;
                }
            }
        });
        _leaveDialog.setOnCancelButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideConfirm2LeaveDialog();
            }
        });
        _leaveDialog.show();
    }
    private void hideConfirm2LeaveDialog() {
        if(_leaveDialog != null) _leaveDialog.dismiss();
    }

    private void showDialog(){
        if(popupDialog != null && popupDialog.isShowing()) popupDialog.dismiss();

        popupDialog = new PopupDialog(mContext);
        popupDialog.setTitle(getString(R.string.dialog_title_pw_confirm));
        popupDialog.setContent(getString(R.string.msg_pw_confirm));
        popupDialog.setEdit(true);
        popupDialog.setEditMinMaxLines(1,1);
        popupDialog.setEditText("");
        popupDialog.setEditTextInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        popupDialog.setOnOkButtonClickListener(ok -> { if(checkLogin()) { confirmPw(); } });
        popupDialog.setOnCancelButtonClickListener(cancel -> { if(popupDialog != null && popupDialog.isShowing()) popupDialog.dismiss(); });

        popupDialog.show();
    }

    private boolean checkLogin() {
        if(popupDialog.getInputText().isEmpty()) {
            Toast.makeText(mContext, getString(R.string.password) + " " + getString(R.string.empty_info), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void confirmPw(){
        String pw = PreferenceUtil.getUserPw(mContext);
        String confirmPw = popupDialog.getInputText();
        LogMgr.e("pwConfirmTest", pw + "," + confirmPw);
        if (pw.equals(confirmPw)){
            if(popupDialog != null && popupDialog.isShowing()) popupDialog.dismiss();
            startActivity(new Intent(mContext, SetPasswordActivity.class));

        }else{
            if(popupDialog != null && popupDialog.isShowing()) popupDialog.setNotMatchTv(true);
        }
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


                                mTvName.setText(getData.name); // 오리지널 이름
                                memberName = getData.name;

                                mTvUserGubun.setText(userGubun);
                                mTvUserGubun.setVisibility(View.VISIBLE);

                                if (getData.phoneNumber != null) {
                                    phoneNum = Utils.formatPhoneNumber(getData.phoneNumber);
                                    mTvPhoneNum.setText(phoneNum);
                                } else mTvPhoneNum.setText(getText(R.string.empty_phonenumber));
                            }

                        }else{
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                            LogMgr.e(TAG, "requestMemberInfo() errBody : " + response.errorBody().string());
                        }

                    }catch (Exception e){ LogMgr.e(TAG + "requestMemberInfo() Exception : ", e.getMessage()); }
                }

                @Override
                public void onFailure(Call<GetManagerInfoResponse> call, Throwable t) {
                    try { LogMgr.e(TAG, "requestMemberInfo() onFailure >> " + t.getMessage()); }
                    catch (Exception e) { LogMgr.e(TAG + "requestMemberInfo() Exception : ", e.getMessage()); }

                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
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
    // 회원탈퇴
    private void requestLeave(int managerSeq){
        if(RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.leave(managerSeq).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    try {
                        if (response.isSuccessful()){
                            requestLogOut(_memberSeq);
                            Toast.makeText(mContext, R.string.success_leave, Toast.LENGTH_SHORT).show();
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
}