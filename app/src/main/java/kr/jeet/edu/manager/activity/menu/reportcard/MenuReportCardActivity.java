package kr.jeet.edu.manager.activity.menu.reportcard;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;
import com.skydoves.powerspinner.OnSpinnerOutsideTouchListener;
import com.skydoves.powerspinner.PowerSpinnerView;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.activity.BaseActivity;
import kr.jeet.edu.manager.adapter.ReportCardListAdapter;
import kr.jeet.edu.manager.adapter.WrapContentSpinnerAdapter;
import kr.jeet.edu.manager.common.Constants;

import kr.jeet.edu.manager.common.DataManager;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.model.data.ACAData;
import kr.jeet.edu.manager.model.data.ReportCardSummaryData;
import kr.jeet.edu.manager.model.response.ReportCardSummaryListResponse;
import kr.jeet.edu.manager.server.RetrofitApi;
import kr.jeet.edu.manager.server.RetrofitClient;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.PreferenceUtil;
import kr.jeet.edu.manager.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MenuReportCardActivity extends BaseActivity {
    private static final String TAG = "report_card";
    private static final int CMD_GET_REPORT_CARD = 1;

    PowerSpinnerView spinnerCampus;
    private RecyclerView _recyclerViewReportCard;
    private ReportCardListAdapter _reportCardListAdapter;
    private SwipeRefreshLayout _swipeRefreshLayout;
    private TextView tvEmptyList;
    EditText etSearch;
    TextView tvSearchBtn;

    List<ReportCardSummaryData> _reportCardList = new ArrayList<>();
    List<ACAData> _ACAList = new ArrayList<>();
    ACAData _selectedACA = null;
    String _acaCode = "";
    int _userGubun = 1;
    int _seq = 0;
    int _sfCode = 0;
    String searchWord = "";
    Menu _menu;
    private Handler _handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                //todo request
                case CMD_GET_REPORT_CARD:
                    requestReportCardList();
                    break;

            }
        }
    };
    //작성화면 resultLauncher
    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            LogMgr.w("result =" + result);
            if(result.getResultCode() != RESULT_CANCELED) {
                Intent intent = result.getData();
                if (intent != null) {
                    if (intent.hasExtra(IntentParams.PARAM_BOARD_ADDED)) {
                        boolean added = intent.getBooleanExtra(IntentParams.PARAM_BOARD_ADDED, false);
                        if (added) {
//                        LogMgr.e("acaCode = " + _selectedACA.acaCode);
                            requestReportCardList();
                        }

                    }
//                else if(intent.hasExtra(IntentParams.PARAM_BOARD_DELETED)) {
//                    boolean deleted = intent.getBooleanExtra(IntentParams.PARAM_BOARD_DELETED, false);
//                    int position = intent.getIntExtra(IntentParams.PARAM_BOARD_POSITION, -1);
//                    if(deleted && position >= 0) {
//                        _reportCardList.remove(position);
//                        _reportCardListAdapter.notifyItemRemoved(position);
//                        checkEmptyRecyclerView();
//                    }
//                }
                }
            }
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_report_card);
        mContext = this;
        _acaCode = PreferenceUtil.getAcaCode(this);
        _userGubun = PreferenceUtil.getUserGubun(this);
        _seq = PreferenceUtil.getUserSeq(this);
        _sfCode = PreferenceUtil.getUserSFCode(this);
        initIntentData();
        initView();
        initAppbar();
        initData();
        setAnimMove(Constants.MOVE_DOWN);
//        _handler.sendEmptyMessage(CMD_GET_REPORT_CARD);

    }
    private void initIntentData() {
        Intent intent = getIntent();
        if(intent != null){
            if(intent.hasExtra(IntentParams.PARAM_STU_ACACODE)) {
                _acaCode = intent.getStringExtra(IntentParams.PARAM_STU_ACACODE);
            }

        }
    }
    void initView() {
        tvEmptyList = findViewById(R.id.tv_empty_list);
        etSearch = findViewById(R.id.et_search);
        tvSearchBtn = findViewById(R.id.tv_search_btn);
        tvSearchBtn.setOnClickListener(this);
        //region spinner campus
        spinnerCampus = findViewById(R.id.spinner_campus);
        if(_userGubun >= Constants.USER_TYPE_TEACHER){  //강사
            spinnerCampus.setVisibility(View.GONE);
        }else {
            spinnerCampus.setVisibility(View.VISIBLE);
            if (_ACAList != null) _ACAList.clear();
            _ACAList.add(new ACAData("", getString(R.string.item_total), ""));
            _ACAList.addAll(DataManager.getInstance().getACAListMap().values());

            WrapContentSpinnerAdapter adapter = new WrapContentSpinnerAdapter(mContext, _ACAList.stream().map(t -> t.acaName).collect(Collectors.toList()), spinnerCampus);
            spinnerCampus.setSpinnerAdapter(adapter);

            spinnerCampus.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener<String>() {
                @Override
                public void onItemSelected(int oldIndex, @Nullable String oldItem, int newIndex, String newItem) {
                    LogMgr.e(newItem + " selected");
                    if (oldItem != null && oldItem.equals(newItem)) return;

                    ACAData selectedData = null;
                    Optional optional = _ACAList.stream().filter(t -> t.acaName == newItem).findFirst();
                    if (optional.isPresent()) {
                        selectedData = (ACAData) optional.get();
                    }
                    _selectedACA = selectedData;
                    LogMgr.w("selectedACA = " + _selectedACA.acaCode + " / " + _selectedACA.acaName);
                    _handler.sendEmptyMessage(CMD_GET_REPORT_CARD);
                }
            });
            spinnerCampus.setSpinnerOutsideTouchListener(new OnSpinnerOutsideTouchListener() {
                @Override
                public void onSpinnerOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                    spinnerCampus.dismiss();
                }
            });
            spinnerCampus.setSpinnerPopupHeight(ConstraintLayout.LayoutParams.WRAP_CONTENT);
            spinnerCampus.setLifecycleOwner(this);
        }
        //endregion
        //region recyclerview
        _recyclerViewReportCard = findViewById(R.id.recyclerview_report_card);
        _reportCardListAdapter = new ReportCardListAdapter(mContext, _reportCardList, new ReportCardListAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int position, ReportCardSummaryData item) {
                navigate2DetailActivity(item, position);
            }

            @Override
            public void onFilteringCompleted() {

            }
        });
        _recyclerViewReportCard.setAdapter(_reportCardListAdapter);
        _recyclerViewReportCard.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        _recyclerViewReportCard.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(((!_recyclerViewReportCard.canScrollVertically(1)) && _recyclerViewReportCard.canScrollVertically(-1))
                        && newState == RecyclerView.SCROLL_STATE_IDLE
                        && (_reportCardListAdapter != null && !_reportCardList.isEmpty()))
                {
                    int lastNoticeSeq = _reportCardList.get(_reportCardList.size() - 1).seq;
                    requestReportCardList(lastNoticeSeq);
                }
            }

        });
        //endregion
        _swipeRefreshLayout = findViewById(R.id.refresh_layout);
        _swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                _handler.sendEmptyMessage(CMD_GET_REPORT_CARD);
            }
        });
        checkEmptyRecyclerView();
    }

    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.title_report_card);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    void initData(){
        if(_userGubun >= Constants.USER_TYPE_TEACHER) {
            _selectedACA = DataManager.getInstance().getACAData(_acaCode);
            _handler.sendEmptyMessage(CMD_GET_REPORT_CARD);
        }else {
            Optional option = _ACAList.stream().filter(t -> t.acaCode.equals(_acaCode)).findFirst();
            if (option.isPresent()) {
                _selectedACA = (ACAData) option.get();
            }
            if (_selectedACA != null) {
                int selectedIndex = _ACAList.indexOf(_selectedACA);
                spinnerCampus.selectItemByIndex(selectedIndex);

            } else {
                if (!_ACAList.isEmpty()) {
                    spinnerCampus.selectItemByIndex(0);
                }
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_send_reportcard, menu);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_send:
                navigate2SelectReportCardRecipientActivity();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if (view.equals(tvSearchBtn)) {
            _handler.sendEmptyMessage(CMD_GET_REPORT_CARD);
        }
    }

    private void requestReportCardList(int... lastSeq){
        if(RetrofitClient.getInstance() != null) {
            int lastNoticeSeq = 0;
            if(lastSeq != null && lastSeq.length > 0) {
                LogMgr.e(TAG, "lastSeq[0] = " + lastSeq[0]);
                lastNoticeSeq = lastSeq[0];
            }
            String acaCode = "";
            if(_selectedACA != null) acaCode = _selectedACA.acaCode;
            String keyword = "";
            try{
                if(!TextUtils.isEmpty(etSearch.getText())) {
                    keyword = etSearch.getText().toString();
                }
            }catch(Exception ex){}
            final int finalLastNoticeSeq = lastNoticeSeq;
            RetrofitClient.getApiInterface().getReportCardSummaryList(_seq, _userGubun, keyword, acaCode, lastNoticeSeq).enqueue(new Callback<ReportCardSummaryListResponse>() {
                @Override
                public void onResponse(Call<ReportCardSummaryListResponse> call, Response<ReportCardSummaryListResponse> response) {
                    if(response.isSuccessful()) {
                        if(response.body() != null) {

                            List<ReportCardSummaryData> getData = response.body().data;
                            if (getData != null){
                                if(finalLastNoticeSeq == 0) {
                                    if (_reportCardList.size() > 0) _reportCardList.clear();
                                }
                                _reportCardList.addAll(getData);

                            }else LogMgr.e(TAG, "ListData is null");
                        }

                    } else {

                        if (response.code() == RetrofitApi.RESPONSE_CODE_BINDING_ERROR){
                            Toast.makeText(mContext, R.string.reportcard_binding_error, Toast.LENGTH_SHORT).show();

                        }else if (response.code() == RetrofitApi.RESPONSE_CODE_NOT_FOUND){
                            Toast.makeText(mContext, R.string.reportcard_not_found, Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(mContext, R.string.server_data_empty, Toast.LENGTH_SHORT).show();
                        }
                    }

                    if (_reportCardListAdapter != null) _reportCardListAdapter.notifyDataSetChanged();
                    checkEmptyRecyclerView();
                }

                @Override
                public void onFailure(Call<ReportCardSummaryListResponse> call, Throwable t) {
                    LogMgr.e(TAG, "request() onFailure >> " + t.getMessage());
                    Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                    if (_reportCardListAdapter != null) _reportCardListAdapter.notifyDataSetChanged();
                    checkEmptyRecyclerView();

                }
            });
        }

    }
    public void navigate2SelectReportCardRecipientActivity() {
        Intent editIntent = new Intent(mContext, SelectReportCardRecipientActivity.class);
        if(_selectedACA != null) {
            editIntent.putExtra(IntentParams.PARAM_STU_ACACODE, _selectedACA.acaCode);
        }
        resultLauncher.launch(editIntent);
//        startActivity(editIntent);
    }
    private void navigate2DetailActivity(ReportCardSummaryData item, int position) {
        Intent intent = new Intent(MenuReportCardActivity.this, DetailReportCardActivity.class);
        intent.putExtra(IntentParams.PARAM_BOARD_ITEM, item);
        intent.putExtra(IntentParams.PARAM_BOARD_POSITION, position);
        startActivity(intent);
        overridePendingTransition(R.anim.horizontal_enter, R.anim.horizontal_out);
    }

    private boolean checkEmptyRecyclerView() {
        if(_swipeRefreshLayout != null) _swipeRefreshLayout.setRefreshing(false);
        if (_reportCardListAdapter.getItemCount() > 0) {
//            _swipeRefreshLayout.setVisibility(View.VISIBLE);
            tvEmptyList.setVisibility(View.INVISIBLE);
            return false;
        } else {
//            _swipeRefreshLayout.setVisibility(View.INVISIBLE);
            tvEmptyList.setVisibility(View.VISIBLE);
            return true;
        }
    }
}