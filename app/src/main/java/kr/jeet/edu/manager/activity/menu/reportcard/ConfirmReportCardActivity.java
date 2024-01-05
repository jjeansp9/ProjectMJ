package kr.jeet.edu.manager.activity.menu.reportcard;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;
import com.skydoves.powerspinner.OnSpinnerOutsideTouchListener;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.util.ArrayList;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.activity.BaseActivity;
import kr.jeet.edu.manager.adapter.ColoredSpinnerAdapter;
import kr.jeet.edu.manager.adapter.SelectReportCardListAdapter;
import kr.jeet.edu.manager.adapter.WrapContentSpinnerAdapter;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.DataManager;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.model.data.RecipientStudentData;
import kr.jeet.edu.manager.model.data.ReportCardData;
import kr.jeet.edu.manager.model.request.UpdateReportCardRequest;
import kr.jeet.edu.manager.model.response.BaseResponse;
import kr.jeet.edu.manager.server.RetrofitClient;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.PreferenceUtil;
import kr.jeet.edu.manager.view.CustomAppbarLayout;
import kr.jeet.edu.manager.view.LimitableEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmReportCardActivity extends BaseActivity {
    private static final String TAG = "confirmReportCard";

    private RecyclerView _recyclerView;
    private SelectReportCardListAdapter _listAdapter;
    private TextView tvStudentName, tvCampus, tvCount;
//    private TextView tvEmptyList;
    private LimitableEditText etContent;
    private PowerSpinnerView _spinnerReportCardMultiple;
    LinearLayout layoutCheckSMS;
    CheckBox cbSendSMS;

    private Constants.ReportCardType _selectReportCardType = null;
    private RecipientStudentData _currentStudent = null;
    private ArrayList<ReportCardData> _reportCardList = new ArrayList<ReportCardData>();

    int _userGubun = 1;
    int _seq = 0;
    int _sfCode = 0;
    Menu _menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_report_card);
        mContext = this;
        _userGubun = PreferenceUtil.getUserGubun(this);
        _seq = PreferenceUtil.getUserSeq(this);
        _sfCode = PreferenceUtil.getUserSFCode(this);
        initIntentData();
        initAppbar();
        initView();
        initData();
        setAnimMove(Constants.MOVE_DETAIL_RIGHT);
    }

    private void initIntentData() {
        Intent intent = getIntent();
        if(intent != null) {
            if (intent.hasExtra(IntentParams.PARAM_REPORT_CARD_LIST)){
                _reportCardList = intent.getParcelableArrayListExtra(IntentParams.PARAM_REPORT_CARD_LIST);
            }
            if (intent.hasExtra(IntentParams.PARAM_STUDENT_INFO)){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    _currentStudent = intent.getParcelableExtra(IntentParams.PARAM_STUDENT_INFO, RecipientStudentData.class);
                }else{
                    _currentStudent = intent.getParcelableExtra(IntentParams.PARAM_STUDENT_INFO);
                }
            }
        }
        if(_reportCardList == null || _reportCardList.isEmpty()) {
            finish();
        }

    }
    void initView() {
//        tvEmptyList = findViewById(R.id.tv_empty_list);
        tvStudentName = findViewById(R.id.tv_name);
        tvCampus = findViewById(R.id.tv_campus);
        tvCount = findViewById(R.id.tv_select_count);
        etContent = findViewById(R.id.et_content);
        layoutCheckSMS = findViewById(R.id.layout_send_sms);
        layoutCheckSMS.setOnClickListener(this);
        cbSendSMS = findViewById(R.id.check_sms);
        //region set recyclerview
        _recyclerView = findViewById(R.id.recyclerview_report_card);
        _listAdapter = new SelectReportCardListAdapter(mContext, _reportCardList, Constants.ReportCardListType.VIEW, new SelectReportCardListAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int position, ReportCardData item) {
            }

        });

        _recyclerView.setAdapter(_listAdapter);
        _recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        //region spinner
        _spinnerReportCardMultiple = findViewById(R.id.spinner_reportcard_type_all);
        _spinnerReportCardMultiple.setIsFocusable(true);
//        _spinnerReportCardMultiple.setItems(Constants.ReportCardType.getNameList());
        ColoredSpinnerAdapter adapter = new ColoredSpinnerAdapter(mContext, Constants.ReportCardType.getColoredNameList(mContext), _spinnerReportCardMultiple);
        _spinnerReportCardMultiple.setSpinnerAdapter(adapter);
        _spinnerReportCardMultiple.setSpinnerPopupHeight(ConstraintLayout.LayoutParams.WRAP_CONTENT);

        _spinnerReportCardMultiple.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener<SpannableString>() {
            @Override
            public void onItemSelected(int oldIndex, @Nullable SpannableString oldItem, int newIndex, SpannableString newItem) {
                LogMgr.w(TAG, "oldItem = " + oldItem + " / newItem = " + newItem);
                if(newItem.equals(oldItem)) return;
                Constants.ReportCardType selectedType = Constants.ReportCardType.values()[newIndex];
                _selectReportCardType = selectedType;
                LogMgr.w(TAG, "selectedType" + _selectReportCardType.getNameKor());
                _reportCardList.forEach(item->{
                    item.etTitleGubun = _selectReportCardType.getCode();
                    LogMgr.w(TAG, "etTitleGubun set : " + item.etTitleGubun);
                });
                _listAdapter.notifyDataSetChanged();
                _spinnerReportCardMultiple.clearSelectedItem();
            }
        });
        _spinnerReportCardMultiple.setSpinnerOutsideTouchListener(new OnSpinnerOutsideTouchListener() {
            @Override
            public void onSpinnerOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                _spinnerReportCardMultiple.dismiss();
            }
        });
        _spinnerReportCardMultiple.setVisibility(View.GONE);
        //endregion
    }
    private void initData() {
        if(_currentStudent != null) {
            tvStudentName.setText(_currentStudent.stName);
            if(!TextUtils.isEmpty(_currentStudent.acaCode)) {
                tvCampus.setText(DataManager.getInstance().getACAData(_currentStudent.acaCode).acaName);
            }
            int count = 0;
            if(_reportCardList != null) count = _reportCardList.size();
            tvCount.setText(getString(R.string.content_item_counts, count));
            _reportCardList.forEach(t->t.stCode = _currentStudent.stCode);
        }
    }
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.title_send_report_card);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_send, menu);
        int positionOfMenuItem = 0;
        try {
            MenuItem item = menu.getItem(positionOfMenuItem);
            SpannableString span = new SpannableString(item.getTitle());
            span.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.red)), 0, span.length(), 0);
            item.setTitle(span);
        }catch(Exception ex){}

        this._menu = menu;
        return (super.onCreateOptionsMenu(menu));
    }
    private boolean checkValid() {
        if(TextUtils.isEmpty(etContent.getText())){
            Toast.makeText(mContext, R.string.empty_reportcard_content, Toast.LENGTH_SHORT).show();
            showKeyboard(etContent.getEditText());
            return false;
        }
        if(_reportCardList.stream().anyMatch(t->t.etTitleGubun < 0)) {
            Toast.makeText(mContext, R.string.error_reportcard_select_type, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_send:
                if(checkValid()) {
                    StringBuilder sb = new StringBuilder(getString(R.string.msg_confirm_send_reportcard));
                    if(_reportCardList.stream().anyMatch(t->t.etTitleGubun == Constants.ReportCardType.MIDDLE.getCode())) {
                        sb.append("\n\n");
                        sb.append("성적표에 포함된 [ 중등 ] 유형은 성적표 출력이 되지 않습니다.");
                    }
                    showMessageDialog(getString(R.string.dialog_title_alarm)
                            , sb.toString()
                            , new View.OnClickListener() {  //OKClickListener
                                @Override
                                public void onClick(View view) {
                                    requestSendReportCard();
                                    hideMessageDialog();
                                }
                            },
                            new View.OnClickListener() {    //cancelClickListener
                                @Override
                                public void onClick(View view) {
                                    hideMessageDialog();
                                }
                            });

                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if(view.equals(layoutCheckSMS)) {
            cbSendSMS.setChecked(!cbSendSMS.isChecked());
        }
    }

    private void requestSendReportCard() {
        if (RetrofitClient.getInstance() != null) {
            UpdateReportCardRequest request = new UpdateReportCardRequest();
            request.initRequest(
                    _seq,
                    PreferenceUtil.getUserName(mContext),
                    _sfCode,
                    _currentStudent.stCode,
                    _currentStudent.stName,
                    etContent.getText(),
                    _currentStudent.acaCode,
                    DataManager.getInstance().getACAData(_currentStudent.acaCode).acaName,
                    cbSendSMS.isChecked()? "Y" : "N",
                    DataManager.getInstance().getACAData(_currentStudent.acaCode).acaTel,
                    _currentStudent.parentPhoneNumber,  //"01095150029", //
                    _reportCardList
            );
            RetrofitClient.getApiInterface().updateReportCard(request).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {

                    try {
                        if (response.isSuccessful()) {
                            Intent intent = new Intent();
                            intent.putExtra(IntentParams.PARAM_BOARD_ADDED, true);
                            setResult(RESULT_OK, intent);
                            overridePendingTransition(R.anim.horizontal_in, R.anim.horizontal_exit);
                            finishAfterTransition();
                            Toast.makeText(mContext, R.string.msg_send_reportcard_success, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestTestReserveList() Exception : ", e.getMessage());
                    }


//                    tvEmptyList.setVisibility(_reportCardList.isEmpty() ? View.VISIBLE : View.GONE);
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    _reportCardList.clear();
                    if(_listAdapter != null) _listAdapter.notifyDataSetChanged();
//                    tvEmptyList.setVisibility(_reportCardList.isEmpty() ? View.VISIBLE : View.GONE);

                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });

        }
    }
}