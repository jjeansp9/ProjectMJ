package kr.jeet.edu.manager.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.db.PushMessage;
import kr.jeet.edu.manager.model.data.CounselData;
import kr.jeet.edu.manager.model.response.CounselDetailResponse;
import kr.jeet.edu.manager.server.RetrofitClient;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.PreferenceUtil;
import kr.jeet.edu.manager.utils.Utils;
import kr.jeet.edu.manager.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CounselDetailActivity extends BaseActivity {
    private static final String TAG = "detail_counsel";
    //region views
    TextView tvDate, tvTime, tvCampus, tvClass, tvName, tvBag, tvMemo, tvPhone;
    //endregion views

    //region variables
    CounselData _currentData = null;
    int _userGubun = 1;
    int _seq = 0;
    int _sfCode = 0;
    private int _currentSeq = -1;
    //endregion variables
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_counsel_detail);
        mContext = this;
        _userGubun = PreferenceUtil.getUserGubun(this);
        _seq = PreferenceUtil.getUserSeq(this);
        _sfCode = PreferenceUtil.getUserSFCode(this);
        initIntentData();
        initView();
        initAppbar();
        setAnimMove(Constants.MOVE_DETAIL_RIGHT);
    }
    private void initIntentData(){
        Intent intent = getIntent();
        if(intent != null) {

            if (intent.hasExtra(IntentParams.PARAM_BOARD_SEQ)){

                _currentSeq = intent.getIntExtra(IntentParams.PARAM_BOARD_SEQ, 0);
            }else if(intent.hasExtra(IntentParams.PARAM_PUSH_MESSAGE)) {
                PushMessage message = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    message = intent.getParcelableExtra(IntentParams.PARAM_PUSH_MESSAGE, PushMessage.class);
                }else{
                    message = intent.getParcelableExtra(IntentParams.PARAM_PUSH_MESSAGE);
                }
                _currentSeq = message.connSeq;
            }
        }
        if(_currentSeq == 0) {
            finish();
        }
    }
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.title_request_consulting);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    void initView() {
        tvDate = findViewById(R.id.tv_content_date);
        tvTime = findViewById(R.id.tv_content_time);
        tvCampus = findViewById(R.id.tv_content_campus);
        tvClass = findViewById(R.id.tv_content_class);
        tvName = findViewById(R.id.tv_content_name);
        tvBag = findViewById(R.id.tv_content_bag);
        tvMemo = findViewById(R.id.tv_content_memo);
        tvPhone = findViewById(R.id.tv_content_phone_num);
        initData();
    }
    private void initData() {
        if (_currentData != null) {
            tvDate.setText(Utils.getStr(_currentData.counselDate));
            tvTime.setText(Utils.getStr(_currentData.callWishDate));
            tvCampus.setText(Utils.getStr(_currentData.acaName));
            tvClass.setText(Utils.getStr(_currentData.clsName));
            StringBuilder sb = new StringBuilder();
            if(_currentData.userGubun == Constants.USER_TYPE_STUDENT) {
                if(!TextUtils.isEmpty(_currentData.writerName)) {
                    sb.append(_currentData.writerName);
                }else if(!TextUtils.isEmpty(_currentData.memberName)) {
                    sb.append(_currentData.memberName);
                }
            }else if(_currentData.userGubun == Constants.USER_TYPE_PARENTS) {
                if(!TextUtils.isEmpty(_currentData.writerName)) {
                    sb.append(_currentData.writerName);
                }
                if(!TextUtils.isEmpty(_currentData.memberName) && !_currentData.writerName.equals(_currentData.memberName)) {
                    sb.append(String.format(" (%s 학부모)", _currentData.memberName));
                }
            }
            tvName.setText(sb.toString());
            tvBag.setText(Utils.getStr(_currentData.bagName));
            tvMemo.setText(Utils.getStr(_currentData.memo));
            tvPhone.setText(Utils.formatPhoneNumber(_currentData.phoneNumber));

        }else if(_currentSeq != -1) {
            requestCounselDetail(_currentSeq);
        }
    }
    private void requestCounselDetail(int ptSeq){
        if (RetrofitClient.getInstance() != null){
            RetrofitClient.getApiInterface().getCounselDetail(ptSeq).enqueue(new Callback<CounselDetailResponse>() {
                @Override
                public void onResponse(Call<CounselDetailResponse> call, Response<CounselDetailResponse> response) {
                    try {
                        if (response.isSuccessful()){
                            if (response.body() != null) {
                                CounselData data = response.body().data;
                                if (data != null){
                                    LogMgr.e(TAG,"data != null");
                                    _currentData = data;
                                    initData();
                                }else LogMgr.e(TAG," DetailData is null");
                            }else{
                                LogMgr.e(TAG,"body.data is null");
                            }
                        }else{
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e){
                        LogMgr.e(TAG,"requestNoticeDetail() Exception : " + e.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<CounselDetailResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "requestBrfDetail() onFailure >> " + t.getMessage());
                    }catch (Exception e){
                    }
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}