package kr.jeet.edu.manager.activity;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.server.RetrofitApi;
import kr.jeet.edu.manager.sns.AppleLoginManager;
import kr.jeet.edu.manager.sns.GoogleLoginManager;
import kr.jeet.edu.manager.sns.KaKaoLoginManager;
import kr.jeet.edu.manager.sns.NaverLoginManager;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.view.CustomAppbarLayout;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AgreeTermsActivity extends BaseActivity {

    private CheckBox mAllCheckBox, mCheckBox1, mCheckBox2;
    private TextView mTvCheck1, mTvCheck2;
    private Button mBtnNext;

    private int mLoginType = Constants.LOGIN_TYPE_NORMAL;

    private NaverLoginManager mNaverLogin = null;
    private KaKaoLoginManager mKakaoLogin = null;
    private GoogleLoginManager mGoogleLogin = null;
    private AppleLoginManager mAppleLogin = null;

    private AppCompatActivity mActivity = null;
    private String url = "";

    private String snsName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agree_terms);
        setStatusAndNavigatinBar(true);
        mContext = this;
        mActivity = this;

        Intent intent = getIntent();
        if(intent != null) {
            if(intent.hasExtra(IntentParams.PARAM_LOGIN_TYPE)) {
                mLoginType = intent.getIntExtra(IntentParams.PARAM_LOGIN_TYPE, Constants.LOGIN_TYPE_NORMAL);
            }else{
                LogMgr.e("no intent extra");
            }

            if (intent.hasExtra(IntentParams.PARAM_LOGIN_USER_NAME)) snsName = intent.getStringExtra(IntentParams.PARAM_LOGIN_USER_NAME);
        }

        if(mLoginType == Constants.LOGIN_TYPE_SNS_NAVER) mNaverLogin = new NaverLoginManager(mContext);
        else if(mLoginType == Constants.LOGIN_TYPE_SNS_KAKAO) mKakaoLogin = new KaKaoLoginManager(mContext);
        else if(mLoginType == Constants.LOGIN_TYPE_SNS_GOOGLE) mGoogleLogin = new GoogleLoginManager(mActivity);
        else if(mLoginType == Constants.LOGIN_TYPE_SNS_APPLE) mAppleLogin = new AppleLoginManager(mActivity);
        initView();
        initAppbar();
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.terms_agree);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    void initView() {
        mAllCheckBox = (CheckBox) findViewById(R.id.cb_all);
        mCheckBox1 = (CheckBox) findViewById(R.id.check1);
        mCheckBox2 = (CheckBox) findViewById(R.id.check2);
        mTvCheck1 = findViewById(R.id.tv_check1);
        mTvCheck2 = findViewById(R.id.tv_check2);
        mBtnNext = findViewById(R.id.btn_next);
        mAllCheckBox.setOnClickListener(this);
//        mCheckBox1.setOnClickListener(this);
//        mCheckBox2.setOnClickListener(this);
        findViewById(R.id.layout_check1).setOnClickListener(this);
        findViewById(R.id.layout_check2).setOnClickListener(this);
        //내용보기
        findViewById(R.id.layout_view_check1).setOnClickListener(this);
        findViewById(R.id.layout_view_check2).setOnClickListener(this);
        mBtnNext.setOnClickListener(this);
        checkAllChecked();
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);

        switch (view.getId()) {
            case R.id.btn_next:
                if(mAllCheckBox.isChecked() == false) {
                    Toast.makeText(mContext, R.string.terms_agreement_msg, Toast.LENGTH_SHORT).show();
                    return ;
                }

                if(mLoginType == Constants.LOGIN_TYPE_SNS_NAVER) {
                    if(mNaverLogin != null) {
                        mNaverLogin.setJoinProcess();
                        mNaverLogin.LoginProcess();
                    }
                    break;
                }
                else if(mLoginType == Constants.LOGIN_TYPE_SNS_KAKAO) {
                    if(mKakaoLogin != null) {
                        mKakaoLogin.setJoinProcess();
                        mKakaoLogin.LoginProcess();
                    }
                    break;
                }
                else if (mLoginType == Constants.LOGIN_TYPE_SNS_GOOGLE){
                    if (mGoogleLogin != null){
                        mGoogleLogin.setJoinProcess();
                        mGoogleLogin.LoginProcess();
                    }
                    break;
                }else if (mLoginType == Constants.LOGIN_TYPE_SNS_APPLE){
                    if (mAppleLogin != null){

                        if (!TextUtils.isEmpty(snsName)) {
                            navigate2JoinActivity();
                        } else {
                            mAppleLogin.setJoinProcess();
                            mAppleLogin.LoginProcess();
                        }
                    }
                    break;
                }else{
                    navigate2JoinActivity();
                }

                break;

            case R.id.cb_all :
                if(mAllCheckBox.isChecked()) {
                    mCheckBox1.setChecked(true);
                    mCheckBox2.setChecked(true);
                } else {
                    mCheckBox1.setChecked(false);
                    mCheckBox2.setChecked(false);
                }
                checkAllChecked();
                break;

//            case R.id.check1:
//            case R.id.check2:
//                if(mCheckBox1.isChecked() && mCheckBox2.isChecked()) {
//                    mAllCheckBox.setChecked(true);
//                } else {
//                    mAllCheckBox.setChecked(false);
//                }
//                break;
            case R.id.layout_check1:
                mCheckBox1.setChecked(!mCheckBox1.isChecked());
                checkAllChecked();
                break;
            case R.id.layout_check2:
                mCheckBox2.setChecked(!mCheckBox2.isChecked());
                checkAllChecked();
                break;

            case R.id.layout_view_check1:
                url = RetrofitApi.SERVER_BASE_URL+"web/api/policy/service";
                navigate2TermsActivity(mTvCheck1.getText().toString());
                break;
            case R.id.layout_view_check2:
                url = RetrofitApi.SERVER_BASE_URL+"web/api/policy/privacy";
                navigate2TermsActivity(mTvCheck2.getText().toString());
                break;

        }
    }
    private void checkAllChecked() {
        if(mCheckBox1.isChecked() && mCheckBox2.isChecked()) {
            mAllCheckBox.setChecked(true);
            mBtnNext.setEnabled(true);

        } else {
            mAllCheckBox.setChecked(false);
            mBtnNext.setEnabled(false);
        }
    }
    private void navigate2JoinActivity(){
        Intent intent = new Intent(this, JoinActivity.class);
        intent.putExtra(IntentParams.PARAM_LOGIN_TYPE, mLoginType);
        if (!TextUtils.isEmpty(snsName)) intent.putExtra(IntentParams.PARAM_LOGIN_USER_NAME, snsName);
        startActivity(intent);
        finish();
    }

    private void navigate2TermsActivity(String title){
        Intent intent = new Intent(mContext, WebViewActivity.class);
        intent.putExtra(IntentParams.PARAM_APPBAR_TITLE, title);
        intent.putExtra(IntentParams.PARAM_WEB_VIEW_URL, url);
        startActivity(intent);
    }

}