package kr.jeet.edu.manager.view;

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
    private Context _context;
    private EditText mEditPhoneNo, mEditAuthNo;
    private TextView mCheckPhoneTxt, mAuthTimerTxt;
    private LinearLayout layoutAuthNo;
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

        btnCheckNumber = findViewById(R.id.btn_check_phone);
        btnCheckNumber.setOnClickListener(this);
        btnCheckNumber.setEnabled(false);
        mEditPhoneNo = (EditText) findViewById(R.id.edit_phonenum);
        mEditPhoneNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if(charSequence!= null) {
                    if(charSequence.length() >=11) {
                        btnCheckNumber.setEnabled(true);
                        mAuthPhoneNo = charSequence.toString().trim();
                    }else{
                        btnCheckNumber.setEnabled(false);
                        mAuthPhoneNo = charSequence.toString().trim();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable != null) {
                    if(!editable.toString().equals(mAuthCompletedPhoneNo)) {
                        mEditAuthNo.setText("");
                    }
                }
            }
        });
        mEditPhoneNo.setOnFocusChangeListener(new View.OnFocusChangeListener(){

            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus) {
                    mEditPhoneNo.setText("");
                    mAuthPhoneNo = "";
                    mEditAuthNo.setEnabled(false);
                    release();
                    mAuthTimerTxt.setVisibility(View.INVISIBLE);
                }else{

                }
            }
        });
        mEditAuthNo = (EditText) findViewById(R.id.edit_phone_authnum);
        mCheckPhoneTxt = (TextView) findViewById(R.id.check_txt_phone);
        layoutAuthNo = (LinearLayout) findViewById(R.id.layout_input_auth_no);
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
        if(Utils.isEmptyContainSpace(mAuthPhoneNo)) {
            Toast.makeText(_context, R.string.empty_phonenumber, Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!mAuthCompletedPhoneNo.equals(mAuthPhoneNo)) {
            Toast.makeText(_context, R.string.msg_re_auth, Toast.LENGTH_SHORT).show();
            return false;
        }
        // 인증번호 체크
        if(mEditAuthNo.getText().toString().isEmpty()) {
            Toast.makeText(_context, _context.getString(R.string.input_auth_number) + " " + _context.getString(R.string.empty_info), Toast.LENGTH_SHORT).show();
            return false;
        } else if(mTimer == null || mAuthNum != Integer.parseInt(mEditAuthNo.getText().toString().trim())) {
            Toast.makeText(_context, R.string.check_auth_number, Toast.LENGTH_SHORT).show();
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
                if(Utils.isEmptyContainSpace(mEditPhoneNo.getText().toString())) {
                    Toast.makeText(_context, R.string.empty_phonenumber, Toast.LENGTH_SHORT).show();
                    return ;
                }
                mEditAuthNo.setEnabled(true);
                requestAuth();
                mEditAuthNo.requestFocus();
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
