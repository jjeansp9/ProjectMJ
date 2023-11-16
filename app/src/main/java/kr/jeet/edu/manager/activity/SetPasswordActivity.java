package kr.jeet.edu.manager.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.model.response.BaseResponse;
import kr.jeet.edu.manager.server.RetrofitApi;
import kr.jeet.edu.manager.server.RetrofitClient;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.PreferenceUtil;
import kr.jeet.edu.manager.utils.Utils;
import kr.jeet.edu.manager.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SetPasswordActivity extends BaseActivity {

    private String TAG = SetPasswordActivity.class.getSimpleName();

    private RetrofitApi mRetrofitApi;

    private EditText mEtPw, mEtPwConfirm;
    private EditText[] mEditList;
    private TextView mCheckPwTxt;

    private int _userGubun = 0;
    private int _memberSeq = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_password);
        mContext = this;
        initData();
        initAppbar();
        initView();
    }

    private void initData(){
        _userGubun = PreferenceUtil.getUserGubun(mContext);
        _memberSeq = PreferenceUtil.getUserSeq(mContext);
    }

    @Override
    void initView() {
        findViewById(R.id.set_pw_root).setOnClickListener(this);
        findViewById(R.id.btn_update_pw_complete).setOnClickListener(this);

        mEtPw = findViewById(R.id.et_set_pw);
        mEtPwConfirm = findViewById(R.id.et_set_pw_confirm);
        mCheckPwTxt = findViewById(R.id.check_txt_pw);
        new Handler().postDelayed(() -> showKeyboard(mContext, mEtPw), Constants.SHOW_KEBOARD_DELAY);
        mEtPw.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                if(checkPassword(editable.toString())) {
                    mCheckPwTxt.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.settings_pw_title);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.set_pw_root:
                Utils.hideKeyboard(mContext, mEtPw, mEtPwConfirm);
                break;
            case R.id.btn_update_pw_complete:
                if (checkPw()){
                    requestSetPw();
                }
                break;
        }
    }

    private boolean checkPw() {
        if (mEtPw.length() < 8){
            Toast.makeText(mContext, R.string.settings_update_pw_lenth, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(mEtPw.getText().toString()) || TextUtils.isEmpty(mEtPwConfirm.getText().toString())){
            Toast.makeText(mContext, R.string.password_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!mEtPw.getText().toString().equals(mEtPwConfirm.getText().toString())) {
            Toast.makeText(mContext, R.string.password_does_not_match, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean checkPassword(String pwd) {
        // 정규식 (숫자, 영문 포함 최소 8글자)
        Pattern pattern = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$");
        Matcher matcher = pattern.matcher(pwd);

        if(!matcher.find()) {
            // "비밀번호는 영문과 특수문자 숫자를 포함하며 8자 이상이어야 합니다.";
            mCheckPwTxt.setText(getString(R.string.check_password));
            mCheckPwTxt.setVisibility(View.VISIBLE);
            return false;
        }

        // 반복된 문자 확인
        Pattern pattern1 = Pattern.compile("(\\w)\\1\\1\\1");
        Matcher matcher1 = pattern1.matcher(pwd);

        if(matcher1.find()){
            // "비밀번호에 동일한 문자를 과도하게 연속해서 사용할 수 없습니다.";
            mCheckPwTxt.setText(getString(R.string.check_password2));
            mCheckPwTxt.setVisibility(View.VISIBLE);
            return false;
        }

        return true;
    }

    private void requestSetPw(){

        String pw = mEtPw.getText().toString().trim();

        if(RetrofitClient.getInstance() != null) {
            showProgressDialog();
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.updatePassword(_memberSeq, pw).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    hideProgressDialog();
                    try {
                        if (response.isSuccessful()){

                            Toast.makeText(mContext, R.string.settings_update_pw, Toast.LENGTH_SHORT).show();
                            PreferenceUtil.setUserPw(mContext, pw);
                            finish();

                        }else{
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                            LogMgr.e(TAG, "requestSetPw() errBody : " + response.errorBody().string());
                        }

                    }catch (Exception e){ LogMgr.e(TAG + "requestSetPw() Exception : ", e.getMessage()); }
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    hideProgressDialog();
                    try { LogMgr.e(TAG, "requestSetPw() onFailure >> " + t.getMessage()); }
                    catch (Exception e) { LogMgr.e(TAG + "requestSetPw() Exception : ", e.getMessage()); }

                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}