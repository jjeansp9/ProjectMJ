package kr.jeet.edu.manager.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.IOException;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.model.request.SmsRequest;
import kr.jeet.edu.manager.model.response.BaseResponse;
import kr.jeet.edu.manager.server.RetrofitApi;
import kr.jeet.edu.manager.server.RetrofitClient;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthPhoneNumberView extends LinearLayout implements View.OnClickListener {
    private static final String TAG = "authPhoneNo";


    public interface ShowProgressDialogDelegate {
        void onRequestShow();
        void onRequestHide();
    }
    private ShowProgressDialogDelegate showProgressDelegate;
    private ConstraintLayout mLayoutAuthNo;
    private Context _context;
    private EditText mEditPhoneNo, mEditAuthNo;
    private TextView mCheckPhoneTxt, mAuthTimerTxt, mTvPatternMismatch;
    private Button btnCheckNumber;
    private AuthNumberTimer mTimer = null;
    private int mAuthNum = 0;
    private String mAuthPhoneNo = "";
    private String mAuthCompletedPhoneNo = "";

    private long AUTH_TIMER_INTERVAL = 1000*60*5;   // 5분

    private RetrofitApi mRetrofitApi;

    public AuthPhoneNumberView(Context context) {
        super(context);
        _context = context;
        initView();
    }

    public AuthPhoneNumberView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        _context = context;
        initView();
    }

    public AuthPhoneNumberView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        _context = context;
        initView();
    }

    public AuthPhoneNumberView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        _context = context;
        initView();
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_auth_phone_number, this, true);

        mLayoutAuthNo = findViewById(R.id.layout_input_auth_no);
        btnCheckNumber = findViewById(R.id.btn_check_phone);
        mTvPatternMismatch = findViewById(R.id.tv_pattern_mismatch);
        mCheckPhoneTxt = findViewById(R.id.check_txt_phone);
        btnCheckNumber.setOnClickListener(this);
        //btnCheckNumber.setEnabled(false);
        btnCheckNumber.setBackgroundResource(R.drawable.bt_demand_nor);
        mEditPhoneNo = (EditText) findViewById(R.id.edit_phonenum);
        mEditAuthNo = (EditText) findViewById(R.id.edit_phone_authnum);
        mEditPhoneNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if(charSequence!= null) {
                    if(Utils.checkPhoneNumber(charSequence.toString().trim())) {
                        btnCheckNumber.setBackgroundResource(R.drawable.bt_demand_sel);
                    }else{
                        btnCheckNumber.setBackgroundResource(R.drawable.bt_demand_nor);
                    }
                    mAuthPhoneNo = charSequence.toString().trim();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        mEditPhoneNo.setOnFocusChangeListener(new OnFocusChangeListener(){

            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus) {
//                    mEditPhoneNo.setText("");
//                    mAuthPhoneNo = "";
                    //mEditAuthNo.setEnabled(false);
                    release();
                    mAuthTimerTxt.setVisibility(View.INVISIBLE);

                    if (mTvPatternMismatch.getVisibility() == View.VISIBLE) {
                        mTvPatternMismatch.setVisibility(View.GONE);
                    }
                    if (mLayoutAuthNo.getVisibility() == View.VISIBLE) {
                        mLayoutAuthNo.setVisibility(View.GONE);
                    }
                    if (mEditAuthNo.getVisibility() == View.VISIBLE) {
                        mEditAuthNo.setVisibility(View.GONE);
                    }
                    if (mCheckPhoneTxt.getVisibility() == View.VISIBLE) {
                        mCheckPhoneTxt.setVisibility(View.GONE);
                    }
                }else{
                    if(Utils.checkPhoneNumber(mEditPhoneNo.getText().toString().trim())) {
                        if (mTvPatternMismatch.getVisibility() == View.VISIBLE) {
                            mTvPatternMismatch.setVisibility(View.GONE);
                        }
                    }else{
                        if (mTvPatternMismatch.getVisibility() == View.GONE) {
                            mTvPatternMismatch.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        });
        mEditAuthNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if(charSequence!= null) {
                    try {
                        if (charSequence.length() == String.valueOf(mAuthNum).length()) {
                            int inputNumber = Integer.parseInt(mEditAuthNo.getText().toString().trim());
                            if (mTimer != null && mAuthNum == inputNumber) {
                                mCheckPhoneTxt.setText(_context.getString(R.string.auth_complete));
                                mCheckPhoneTxt.setVisibility(View.VISIBLE);
                                mEditAuthNo.setEnabled(false);
                                Utils.hideKeyboard(_context, mEditAuthNo);
                            } else {
                                mCheckPhoneTxt.setText(_context.getString(R.string.mismatch_phonenumber));
                            }
                        }
                    } catch (NumberFormatException e) {
                        if (e.getMessage() != null) LogMgr.e(TAG, e.getMessage());
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        mEditAuthNo.setOnFocusChangeListener(new OnFocusChangeListener(){
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus) {
                    if (mLayoutAuthNo.getVisibility() == View.VISIBLE) {
                        if (!mEditAuthNo.getText().toString().isEmpty()) {
                            try {
                                int inputNumber = Integer.parseInt(mEditAuthNo.getText().toString().trim());
                                if(mTimer == null || mAuthNum != inputNumber) {
                                    //Toast.makeText(_context, R.string.check_auth_number, Toast.LENGTH_SHORT).show();
                                    if (mLayoutAuthNo.getVisibility() == View.VISIBLE) {
                                        mCheckPhoneTxt.setText(_context.getString(R.string.mismatch_phonenumber));
                                        mCheckPhoneTxt.setVisibility(View.VISIBLE);
                                    } else {
                                        mCheckPhoneTxt.setVisibility(View.GONE);
                                    }
                                }
                            }catch (NumberFormatException e) {
                                if (e.getMessage() != null) LogMgr.e(TAG, e.getMessage());
                            }
                        }

                    } else {
                        mCheckPhoneTxt.setVisibility(View.GONE);
                    }
                }
            }
        });
        mEditAuthNo = (EditText) findViewById(R.id.edit_phone_authnum);
        mCheckPhoneTxt = (TextView) findViewById(R.id.check_txt_phone);
        mAuthTimerTxt = (TextView) findViewById(R.id.timer);
        mAuthTimerTxt.setVisibility(View.INVISIBLE);

    }
    public void setShowProgressDelegate(ShowProgressDialogDelegate delegate ) {
        this.showProgressDelegate = delegate;
    }

    public String getAuthPhoneNo() {
        return mAuthPhoneNo;
    }

    // 인증번호 요청 타이머
    public void startAuthTimer() {
        if(mTimer != null) {
            mTimer.cancel();
        } else {
            mTimer = new AuthNumberTimer(AUTH_TIMER_INTERVAL, 1000);
        }
        mTimer.start();
        mAuthTimerTxt.setVisibility(View.VISIBLE);
    }
    public Boolean checkValid() {
        if (mAuthPhoneNo.contains("01288411004")) { // 테스트용 번호로 회원가입시 휴대폰인증 하지 않고 넘어가게 하기
            return true;
        }
        if(Utils.isEmptyContainSpace(mAuthPhoneNo)) {
            Toast.makeText(_context, R.string.empty_phonenumber, Toast.LENGTH_SHORT).show();
            return false;
        }

        // 인증번호 체크
        if(mEditAuthNo.getText().toString().isEmpty()) {
            Toast.makeText(_context, _context.getText(R.string.input_auth_number)  + " " + _context.getText(R.string.empty_info), Toast.LENGTH_SHORT).show();
            return false;

        } else if(mTimer == null || mAuthNum != Integer.parseInt(mEditAuthNo.getText().toString().trim())) {
            if (mLayoutAuthNo.getVisibility() == View.VISIBLE) {
                mCheckPhoneTxt.setText(_context.getString(R.string.mismatch_phonenumber));
                mCheckPhoneTxt.setVisibility(View.VISIBLE);
            } else {
                mCheckPhoneTxt.setVisibility(View.GONE);
            }
            Toast.makeText(_context, R.string.check_auth_number, Toast.LENGTH_SHORT).show();

            return false;
        }

        if (!mAuthCompletedPhoneNo.equals(mAuthPhoneNo)){
            Toast.makeText(_context, R.string.msg_re_auth, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        LogMgr.d("onClick " + view.getId());
        switch (view.getId()) {

            case R.id.btn_check_phone:
                LogMgr.d("btn check phone clicked");
                mCheckPhoneTxt.setVisibility(View.GONE);
                mEditAuthNo.setText("");
                mAuthNum = 0;
                mEditAuthNo.setEnabled(true);

                if(Utils.isEmptyContainSpace(mEditPhoneNo.getText().toString())) {
                    Toast.makeText(_context, R.string.empty_phonenumber, Toast.LENGTH_SHORT).show();
                    return ;
                }

                if(Utils.checkPhoneNumber(mEditPhoneNo.getText().toString().trim())) {
                    //mEditAuthNo.setEnabled(true);
                    mLayoutAuthNo.setVisibility(View.VISIBLE);
                    mEditAuthNo.setVisibility(View.VISIBLE);
                    requestAuth();
                    mEditAuthNo.requestFocus();
                }else{
                    Toast.makeText(_context, R.string.pattern_mismatch_phonenumber, Toast.LENGTH_SHORT).show();
                    return ;
                }
                break;
        }
    }

    private void requestAuth() {

        mAuthNum = Utils.generateAuthNum();

        SmsRequest request = new SmsRequest();
        request.msg = _context.getString(R.string.auth_number_msg, mAuthNum);
        mAuthCompletedPhoneNo = mAuthPhoneNo;
        request.receiver = mAuthPhoneNo;
        request.sender = "@";   //대표번호
        request.senderCode = Constants.SMS_SENDER_CODE;
        request.receiverCode = Constants.SMS_RECEIVER_CODE;

        if(RetrofitClient.getInstance() != null) {
//            showProgressDialog();
            showProgressDelegate.onRequestShow();
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.sendSms(request).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    showProgressDelegate.onRequestHide();
                    LogMgr.e(TAG, "sendSms() : " + response.toString());
                    if(response.isSuccessful()) {
                        // 인증번호 요청 타이머
                        startAuthTimer();
                    } else {
                        Toast.makeText(_context, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        try {
                            LogMgr.e(TAG, "sendSms() errBody : " + response.errorBody().string());
                        } catch (IOException e) {
                        }
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    showProgressDelegate.onRequestHide();
                    LogMgr.e(TAG, "sendSms() onFailure >> " + t.getMessage());
                    Toast.makeText(_context, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private class AuthNumberTimer extends CountDownTimer {

        public AuthNumberTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {
            // 총 남은 초
            int remainSec = (int)l / 1000;
            String sec = String.format("%02d:", remainSec/60) + String.format("%02d", remainSec%60);
            mAuthTimerTxt.setText(sec);
        }

        @Override
        public void onFinish() {
            mAuthTimerTxt.setText(R.string.timeout);
            mTimer = null;
        }
    }
//    public void hideKeyboard(InputMethodManager imm) {
//        imm.hideSoftInputFromWindow(mEditPhoneNo.getWindowToken(), 0);
//        imm.hideSoftInputFromWindow(mEditAuthNo.getWindowToken(), 0);
////        mEditName.clearFocus();
//    }
    public void release() {
        if(mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

}
