package kr.jeet.edu.manager.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;


import java.io.IOException;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.dialog.PopupDialog;
import kr.jeet.edu.manager.model.request.SmsRequest;
import kr.jeet.edu.manager.model.response.BaseResponse;
import kr.jeet.edu.manager.model.response.FindIDResponse;
import kr.jeet.edu.manager.model.response.FindPWResponse;
import kr.jeet.edu.manager.server.RetrofitApi;
import kr.jeet.edu.manager.server.RetrofitClient;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.Utils;
import kr.jeet.edu.manager.view.AuthPhoneNumberView;
import kr.jeet.edu.manager.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FindCredentialsActivity extends BaseActivity {

    private String TAG = "FindCredential";

    private ViewGroup mViewGroupIdPwd;
    private EditText mEditId;
    private RadioGroup mFindTypeToggleGroup;

    private int mFindType = Constants.FIND_TYPE_ID;
    private String mUserSnsId = null;

    private RetrofitApi mRetrofitApi;
    AuthPhoneNumberView _authPhoneNoView;

    //메세지 팝업
    private PopupDialog popupDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_credentials);
        setStatusAndNavigatinBar(true);
        mContext = this;

        Intent intent = getIntent();
        if(intent != null) {
//            mLoginType = intent.getIntExtra(IntentParams.PARAM_LOGIN_TYPE, Constants.LOGIN_TYPE_NORMAL);
//            if(mLoginType == Constants.LOGIN_TYPE_SNS_NAVER || mLoginType == Constants.LOGIN_TYPE_SNS_KAKAO) {
//                mUserName = intent.getStringExtra(IntentParams.PARAM_LOGIN_USER_NAME);
//                mUserGender = intent.getStringExtra(IntentParams.PARAM_LOGIN_USER_GENDER);
//                mUserSnsId = intent.getStringExtra(IntentParams.PARAM_LOGIN_USER_SNSID);
//            }
        }
        initView();
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.find_id_pw);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    @Override
    void initView() {
//        findViewById(R.id.layout_root).setOnClickListener(this);
        findViewById(R.id.btn_next).setOnClickListener(this);
//        findViewById(R.id.btn_check_phone).setOnClickListener(this);

        mViewGroupIdPwd = findViewById(R.id.layout_idpwd);

        mEditId = (EditText) findViewById(R.id.edit_id);
        mFindTypeToggleGroup = (RadioGroup) findViewById(R.id.radio_group);
        mFindTypeToggleGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.choice1) {
                    if(mFindType != Constants.FIND_TYPE_ID) {
                        mFindType = Constants.FIND_TYPE_ID;
                        mViewGroupIdPwd.setVisibility(View.GONE);
                    }
                }else if(checkedId == R.id.choice2) {
                    if(mFindType != Constants.FIND_TYPE_PW) {
                        mFindType = Constants.FIND_TYPE_PW;
                        mViewGroupIdPwd.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        _authPhoneNoView = findViewById(R.id.cv_auth_phoneno);
        _authPhoneNoView.setShowProgressDelegate(new AuthPhoneNumberView.ShowProgressDialogDelegate() {
            @Override
            public void onRequestShow() {
                showProgressDialog();
            }

            @Override
            public void onRequestHide() {
                hideProgressDialog();
            }
        });
        initAppbar();
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);

        switch (view.getId()) {
            case R.id.btn_next:
                if(checkFind()) {
                    if(mFindType == Constants.FIND_TYPE_ID) {
                        LogMgr.e(TAG, "findID");
                        findID();
                    }
                    else {
                        LogMgr.e(TAG, "findPW");
                        findPassword();
                    }

                }
                break;

        }
    }

    private boolean checkFind() {

        // ID 인 경우 id 체크
        if(mFindType == Constants.FIND_TYPE_PW) {
            if (TextUtils.isEmpty(mEditId.getText().toString())) {
                showKeyboard(mEditId);
                Toast.makeText(mContext, R.string.check_id_empty, Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        if(_authPhoneNoView != null) {
            return _authPhoneNoView.checkValid();
        }
        return true;
    }


    private void findID() {
        // ID찾기 요청
        String phoneNo = _authPhoneNoView.getAuthPhoneNo();
        requestFindId(phoneNo);
    }

    private void requestFindId(String phoneNo) {

        if(RetrofitClient.getInstance() != null) {
            showProgressDialog();
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.findID(phoneNo, Constants.APP_TYPE).enqueue(new Callback<FindIDResponse>() {
                @Override
                public void onResponse(Call<FindIDResponse> call, Response<FindIDResponse> response) {
                    hideProgressDialog();
                    LogMgr.e(TAG, "requestFindID() : " + response.toString());
                    // todo. 사용자구분 타입에 따른 아이디 중복여부 체크 처리 필요
                    if (response.isSuccessful()) {
                        if(response.body()!= null && response.body().data != null) {
                            popupDialog = new PopupDialog(mContext);
                            popupDialog.setTitle(getString(R.string.find_type_id));
                            if(response.body().data.id == null) {   //SNS 로그인
                                String snsCode = response.body().data.snsType;
                                String snsString = Utils.getSNSProvider(snsCode);
                                popupDialog.setContent(getString(R.string.msg_find_snsid, snsString, snsString));
                            }else { //일반 로그인
                                String id = response.body().data.id;
                                popupDialog.setContent(getString(R.string.msg_find_id, Utils.makeBlind(id)));
                            }

                            popupDialog.setOnOkButtonClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            popupDialog.dismiss();
                                            finish();
                                        }
                                    });
                            popupDialog.show();
                        }else{
                            Toast.makeText(mContext, R.string.error_message_invalid_info2, Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        try {
                            LogMgr.e(TAG, "findID() errBody : " + response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        Toast.makeText(mContext, R.string.error_message_invalid_info2, Toast.LENGTH_SHORT).show();

                    }
                }

                @Override
                public void onFailure(Call<FindIDResponse> call, Throwable t) {
                    hideProgressDialog();
                    LogMgr.e(TAG, "requestJoin() onFailure >> " + t.getMessage());
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void findPassword() {
        // PW 찾기
        String id = mEditId.getText().toString();
        String phoneNo = _authPhoneNoView.getAuthPhoneNo();
        requestFindPassword(phoneNo, id);
    }

    private void requestFindPassword(String phoneNo, String id) {

        if(RetrofitClient.getInstance() != null) {
            showProgressDialog();
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.findPW(id, phoneNo).enqueue(new Callback<FindPWResponse>() {
                @Override
                public void onResponse(Call<FindPWResponse> call, Response<FindPWResponse> response) {
                    hideProgressDialog();
                    LogMgr.e(TAG, "findPW() : " + response.toString());
                    // todo. 사용자구분 타입에 따른 아이디 중복여부 체크 처리 필요
                    if (response.isSuccessful()) {
                        if(response.body()!= null && response.body().data != null) {
                            String pw = response.body().data.pw;
                            sendSMS(phoneNo, getString(R.string.msg_instant_pw, pw));
                            popupDialog = new PopupDialog(mContext);
                            popupDialog.setTitle(getString(R.string.find_type_pw));
                            popupDialog.setContent(getString(R.string.msg_find_pw));
                            popupDialog.setOnOkButtonClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    popupDialog.dismiss();
                                    finish();
                                }
                            });
                            popupDialog.show();
                        }else{
                            Toast.makeText(mContext, R.string.server_data_empty, Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        if(response.code() == RetrofitApi.RESPONSE_CODE_NOT_FOUND) {
                            Toast.makeText(mContext, R.string.error_message_invalid_info2, Toast.LENGTH_SHORT).show();
                        }else {
                            try {
                                LogMgr.e(TAG, "findID() errBody : " + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<FindPWResponse> call, Throwable t) {
                    hideProgressDialog();
                    LogMgr.e(TAG, "requestJoin() onFailure >> " + t.getMessage());
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });

        }

    }
    private void sendSMS(String phoneNo, String msg) {

        SmsRequest request = new SmsRequest();
        request.msg = msg;
        request.receiver = phoneNo;
        request.sender = "@";
        request.senderCode = Constants.SMS_SENDER_CODE;
        request.receiverCode = Constants.SMS_RECEIVER_CODE;

        if(RetrofitClient.getInstance() != null) {
            showProgressDialog();
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.sendSms(request).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    hideProgressDialog();
                    LogMgr.e(TAG, "sendSms() : " + response.toString());
                    if(response.isSuccessful()) {

                    } else {
                        Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        try {
                            LogMgr.e(TAG, "sendSms() errBody : " + response.errorBody().string());
                        } catch (IOException e) {
                        }
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    hideProgressDialog();
                    LogMgr.e(TAG, "sendSms() onFailure >> " + t.getMessage());
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(_authPhoneNoView != null) {
            _authPhoneNoView.release();
        }
    }
}