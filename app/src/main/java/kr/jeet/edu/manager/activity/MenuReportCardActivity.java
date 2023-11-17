package kr.jeet.edu.manager.activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;
import com.skydoves.powerspinner.OnSpinnerOutsideTouchListener;
import com.skydoves.powerspinner.PowerSpinnerView;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.adapter.ReportCardListAdapter;
import kr.jeet.edu.manager.adapter.WrapContentSpinnerAdapter;
import kr.jeet.edu.manager.common.Constants;

import kr.jeet.edu.manager.common.DataManager;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.model.data.ACAData;
import kr.jeet.edu.manager.model.data.ReportCardSummaryData;
import kr.jeet.edu.manager.server.RetrofitClient;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.PreferenceUtil;
import kr.jeet.edu.manager.view.CustomAppbarLayout;


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
                if(intent.hasExtra(IntentParams.PARAM_BOARD_ADDED)) {
                    boolean added = intent.getBooleanExtra(IntentParams.PARAM_BOARD_ADDED, false);

                    if(added) {
//                        LogMgr.e("acaCode = " + _selectedACA.acaCode);
                        requestReportCardList();
                    }
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
    @Override
    void initView() {
        tvEmptyList = findViewById(R.id.tv_empty_list);
        etSearch = findViewById(R.id.et_search);
        tvSearchBtn = findViewById(R.id.tv_search_btn);
        tvSearchBtn.setOnClickListener(this);
        //region spinner campus
        spinnerCampus = findViewById(R.id.spinner_campus);
        if(_ACAList != null) _ACAList.clear();
        _ACAList.addAll(DataManager.getInstance().getACAListMap().values());

        WrapContentSpinnerAdapter adapter = new WrapContentSpinnerAdapter(mContext, _ACAList.stream().map(t -> t.acaName).collect(Collectors.toList()), spinnerCampus);
        spinnerCampus.setSpinnerAdapter(adapter);

        spinnerCampus.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener<String>() {
            @Override
            public void onItemSelected(int oldIndex, @Nullable String oldItem, int newIndex, String newItem) {
                LogMgr.e(newItem + " selected");
                if(oldItem != null && oldItem.equals(newItem)) return;

                ACAData selectedData = null;
                Optional optional = _ACAList.stream().filter(t -> t.acaName == newItem).findFirst();
                if(optional.isPresent()) {
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
        //endregion
        //region recyclerview
        _recyclerViewReportCard = findViewById(R.id.recyclerview_report_card);
        _reportCardListAdapter = new ReportCardListAdapter(mContext, _reportCardList, new ReportCardListAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int position, ReportCardSummaryData item) {

            }

            @Override
            public void onFilteringCompleted() {

            }
        });
        _recyclerViewReportCard.setAdapter(_reportCardListAdapter);
        _recyclerViewReportCard.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
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

    @Override
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
        Optional option = _ACAList.stream().filter(t -> t.acaCode.equals(_acaCode)).findFirst();
        if (option.isPresent()) {
            _selectedACA = (ACAData) option.get();
        }
        if (_selectedACA != null) {
            int selectedIndex = _ACAList.indexOf(_selectedACA);
            spinnerCampus.selectItemByIndex(selectedIndex);

        }else{
            if(!_ACAList.isEmpty()) {
                spinnerCampus.selectItemByIndex(0);
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_send, menu);
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

    private void requestReportCardList(){
        if(RetrofitClient.getInstance() != null) {
//            RetrofitClient.getApiInterface().getBusRoute(bcName, _currentData.busCode).enqueue(new Callback<BusRouteResponse>() {
//                @Override
//                public void onResponse(Call<BusRouteResponse> call, Response<BusRouteResponse> response) {
//                    boolean isScrollY = false;
//                    if(response.isSuccessful()) {
//                        if(response.body() != null) {
//                            if (mList!=null && mList.size() > 0) mList.clear();
//
//                            List<BusRouteData> getData = response.body().data;
//                            if (getData != null){
//                                //운행중?
//                                if(_currentData.isDrive.equals("Y")) {
//                                    Optional optional = getData.stream().filter(t->"Y".equals(t.isArrive)).reduce((first, second) -> second);
//                                    if(optional.isPresent()) {
//                                        BusRouteData lastArrived = (BusRouteData) optional.get();
//                                        lastArrived.isAtThisStop = true;
//                                        int position = getData.indexOf(lastArrived);
//                                        if(_positionBus != position) {
//                                            _positionBus = position;
//                                            isScrollY = true;
//                                        }
//                                    }
//                                }else{
//
//                                }
//                                if (mList != null) mList.addAll(getData);
//
//                            }
//                        }
//
//                    } else {
//
//                        if (response.code() == RetrofitApi.RESPONSE_CODE_BINDING_ERROR){
//                            Toast.makeText(mContext, R.string.bus_route_binding_error, Toast.LENGTH_SHORT).show();
//
//                        }else if (response.code() == RetrofitApi.RESPONSE_CODE_NOT_FOUND){
//                            Toast.makeText(mContext, R.string.bus_route_not_found, Toast.LENGTH_SHORT).show();
//                        }else{
//                            Toast.makeText(mContext, R.string.server_data_empty, Toast.LENGTH_SHORT).show();
//                        }
//                    }
//
//                    if (mAdapter != null) mAdapter.notifyDataSetChanged();
//                    mRecyclerRoute.smoothScrollToPosition(_positionBus);
//                    if (mList != null) mTvListEmpty.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);
//                    setProgressWithAnimation();
//                    _handler.sendEmptyMessageDelayed(CMD_REFRESH, REFRESH_SEC * 1000);
//                }
//
//                @Override
//                public void onFailure(Call<BusRouteResponse> call, Throwable t) {
//                    LogMgr.e(TAG, "request() onFailure >> " + t.getMessage());
//                    Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
//                    if (mAdapter != null) mAdapter.notifyDataSetChanged();
//                    mTvListEmpty.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);
//                    setProgressWithAnimation();
//                    _handler.sendEmptyMessageDelayed(CMD_REFRESH, REFRESH_SEC * 1000);
//
//                }
//            });
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
    private boolean checkEmptyRecyclerView() {
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