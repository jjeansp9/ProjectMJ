package kr.jeet.edu.manager.activity.menu.reportcard;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.activity.BaseActivity;
import kr.jeet.edu.manager.adapter.SelectReportCardListAdapter;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.model.data.ReportCardData;
import kr.jeet.edu.manager.model.data.ReportCardSummaryData;
import kr.jeet.edu.manager.model.response.ReportCardSummaryResponse;
import kr.jeet.edu.manager.server.RetrofitClient;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.PreferenceUtil;
import kr.jeet.edu.manager.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailReportCardActivity extends BaseActivity {
    private static final String TAG = "detailReportCard";
    private static final int CMD_GET_REPORT_CARD = 1;
    private RecyclerView _recyclerView;
    private SelectReportCardListAdapter _listAdapter;
    private TextView tvStudentName, tvCampus, tvContent;
    private TextView tvEmptyList;

    private ArrayList<ReportCardData> _reportCardList = new ArrayList<ReportCardData>();
    ReportCardSummaryData _currentData = null;
    int _userGubun = 1;
    int _seq = 0;
    int _sfCode = 0;
    int _currentDataPosition = 0;
    Menu _menu;
    private Handler _handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case CMD_GET_REPORT_CARD:
                    //todo request logic
                    requestReportCardDetailList();
                    break;

            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_report_card);
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
            if(intent.hasExtra(IntentParams.PARAM_BOARD_ITEM)) {
                LogMgr.w("param is recived");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    _currentData = intent.getParcelableExtra(IntentParams.PARAM_BOARD_ITEM, ReportCardSummaryData.class);
                }else{
                    _currentData = intent.getParcelableExtra(IntentParams.PARAM_BOARD_ITEM);
                }
            }

        }
        if(_currentData == null) {
            finish();
        }
    }
    void initView() {
        tvStudentName = findViewById(R.id.tv_name);
        tvCampus = findViewById(R.id.tv_campus);
        tvContent = findViewById(R.id.tv_content);
        tvEmptyList = findViewById(R.id.tv_empty_list);
        //region set recyclerview
        _recyclerView = findViewById(R.id.recyclerview_report_card);
        _listAdapter = new SelectReportCardListAdapter(mContext, _reportCardList, Constants.ReportCardListType.VIEW, new SelectReportCardListAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int position, ReportCardData item) {
                if(item.etTitleGubun == Constants.ReportCardType.MIDDLE.getCode()) {
                    Toast.makeText(mContext, R.string.err_msg_middle_not_displayed, Toast.LENGTH_SHORT).show();
                }else {
                    navigate2ShowActivity(item);
                }
            }

        });
        _recyclerView.setAdapter(_listAdapter);
        _recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
    }
    void initData() {
        _handler.sendEmptyMessage(CMD_GET_REPORT_CARD);
        tvStudentName.setText(_currentData.stName);
        tvCampus.setText(_currentData.acaName);
        tvContent.setText(_currentData.content);
    }

    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.title_detail);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        if(_currentData != null) {
//            if ((_userGubun == Constants.USER_TYPE_ADMIN && _currentData.writerSeq == _seq)
//                    || _userGubun == Constants.USER_TYPE_SUPER_ADMIN){
//                getMenuInflater().inflate(R.menu.menu_edit, menu);
//                this._menu = menu;
//            }
//        }
//        return (super.onCreateOptionsMenu(menu));
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch(item.getItemId()) {
//            case R.id.action_delete:
//                showMessageDialog(getString(R.string.dialog_title_alarm)
//                        , getString(R.string.board_item_confirm_delete)
//                        , new View.OnClickListener() {  //OKClickListener
//                            @Override
//                            public void onClick(View view) {
//                                if (_currentData != null) {
//                                    requestReportCardDelete(_currentData.seq);
//                                }
//                                hideMessageDialog();
//                            }
//                        },
//                        new View.OnClickListener() {    //cancelClickListener
//                            @Override
//                            public void onClick(View view) {
//                                hideMessageDialog();
//                            }
//                        });
//
//                return true;
//            default:
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//    }
    private void requestReportCardDetailList(){
        if (RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getReportCardDetailList(_currentData.seq).enqueue(new Callback<ReportCardSummaryResponse>() {
                @Override
                public void onResponse(Call<ReportCardSummaryResponse> call, Response<ReportCardSummaryResponse> response) {


                    try {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                _reportCardList.clear();
                                ReportCardSummaryData data = response.body().data;
                                if (data != null) {
                                    _reportCardList.addAll(data.reportList);
                                }
                            }
                        } else {
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestTestReserveList() Exception : ", e.getMessage());
                    }

                    if(_listAdapter != null) {
                        _listAdapter.notifyDataSetChanged();
                    }
                    tvEmptyList.setVisibility(_reportCardList.isEmpty() ? View.VISIBLE : View.GONE);

                }

                @Override
                public void onFailure(Call<ReportCardSummaryResponse> call, Throwable t) {
                    _reportCardList.clear();
                    if(_listAdapter != null) _listAdapter.notifyDataSetChanged();
                    tvEmptyList.setVisibility(_reportCardList.isEmpty() ? View.VISIBLE : View.GONE);

                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
//    private void requestReportCardDelete(int seq) {
//        if(RetrofitClient.getInstance() != null) {
//            RetrofitClient.getApiInterface().deleteReportCard(_currentData.seq).enqueue(new Callback<BaseResponse>() {
//                @Override
//                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
//                    try{
//                        if(response.isSuccessful()) {
//                            Toast.makeText(mContext, R.string.reportcard_item_deleted, Toast.LENGTH_SHORT).show();
//                            Intent deleteIntent = new Intent();
//                            deleteIntent.putExtra(IntentParams.PARAM_BOARD_DELETED, true);
//                            deleteIntent.putExtra(IntentParams.PARAM_BOARD_POSITION, _currentDataPosition);
//                            setResult(RESULT_OK, deleteIntent);
//                            finish();
//                        }else{
//                            Toast.makeText(mContext, R.string.board_item_deleted_fail, Toast.LENGTH_SHORT).show();
//                        }
//                    }catch(Exception ex) {
//
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<BaseResponse> call, Throwable t) {
//                    Toast.makeText(mContext, R.string.board_item_deleted_fail, Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//    }
    private void navigate2ShowActivity(ReportCardData item) {
        Intent intent = new Intent(DetailReportCardActivity.this, ShowReportCardActivity.class);
        intent.putExtra(IntentParams.PARAM_BOARD_SEQ, _currentData.seq);
        intent.putExtra(IntentParams.PARAM_BOARD_ITEM, item);
        startActivity(intent);
        overridePendingTransition(R.anim.horizontal_enter, R.anim.horizontal_out);
    }
}