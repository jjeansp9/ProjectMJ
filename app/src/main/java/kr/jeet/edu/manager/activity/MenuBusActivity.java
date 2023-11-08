package kr.jeet.edu.manager.activity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.skydoves.powerspinner.OnSpinnerOutsideTouchListener;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.adapter.BusesListAdapter;
import kr.jeet.edu.manager.adapter.WrapContentSpinnerAdapter;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.model.data.BCData;
import kr.jeet.edu.manager.model.data.BusInfoData;
import kr.jeet.edu.manager.model.response.BCListResponse;
import kr.jeet.edu.manager.model.response.BusInfoResponse;
import kr.jeet.edu.manager.server.RetrofitClient;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.Utils;
import kr.jeet.edu.manager.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuBusActivity extends BaseActivity {
    private static final String TAG = "bus";
    private static final int CMD_GET_CAMPUS = 0;
    private static final int CMD_GET_ROUTE = 1;
    private static final String BUS_TIMETABLE_BASE_URL = "http://m.jeet.kr/intro/table/index.jsp?route_type=1&campus_fk=";
    private String url = "";

    //region views///
    private PowerSpinnerView _spinnerCampus;//
    private RecyclerView _recyclerViewRoute;
    private BusesListAdapter _busesListAdapter;
    private SwipeRefreshLayout _swipeRefreshLayout;
    private TextView tvEmptyList;
    private RelativeLayout layoutTimeTable;
    //endregion views///

    //region variables///
    List<BCData> _campusList = new ArrayList<>();
//    List<BusInfoData> _busesList4Spinner = new ArrayList<>();
    List<BusInfoData> _busesList = new ArrayList<>();
    BCData _selectedBusCampus = null;
    BusInfoData _selectedBus = null;
    Menu _menu;
    //endregion variables///
    private Handler _handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                //todo request
                case CMD_GET_CAMPUS:
                    requestBusCampusList();
                    break;
                case CMD_GET_ROUTE:
                    if(_selectedBusCampus == null) {
                        return;
                    }
                    requestBusesList();

                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_bus);
        mContext = this;
        initView();
        initAppbar();
        _handler.sendEmptyMessage(CMD_GET_CAMPUS);
        setAnimMove(Constants.MOVE_DOWN);
    }

    @Override
    void initView() {
        tvEmptyList = findViewById(R.id.tv_empty_list);
        _swipeRefreshLayout = findViewById(R.id.refresh_layout);
        _swipeRefreshLayout.setOnRefreshListener( () ->{
            Message msg = _handler.obtainMessage(CMD_GET_ROUTE);
            _handler.sendMessage(msg);

        });
        //region set recyclerview
        _recyclerViewRoute = findViewById(R.id.recyclerview_route);
        _busesListAdapter = new BusesListAdapter(mContext, _busesList, new BusesListAdapter.ItemClickListener() {

            @Override
            public void onItemClick(BusInfoData item) {
                navigate2RouteActivity(item);
            }

            @Override
            public void onFilteringCompleted() {

            }
        });

        _recyclerViewRoute.setAdapter(_busesListAdapter);
        _recyclerViewRoute.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        //endregion

        _spinnerCampus = findViewById(R.id.spinner_campus);
        {
            WrapContentSpinnerAdapter adapter = new WrapContentSpinnerAdapter(mContext, _campusList.stream().map(t -> t.bcName).collect(Collectors.toList()), _spinnerCampus);
            _spinnerCampus.setSpinnerAdapter(adapter);
        }

        _spinnerCampus.setIsFocusable(true);
        _spinnerCampus.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
            if(oldItem != null && oldItem.equals(newItem)) return;
            Optional optional =  _campusList.stream().filter(t -> t.bcName == newItem).findFirst();
            if(optional.isPresent()) {
                _selectedBusCampus = (BCData) optional.get();
                LogMgr.w(TAG,"selectedACA = " + _selectedBusCampus.bcCode + " / " + _selectedBusCampus.bcName);
                _selectedBus = null;
//                _spinnerBus.clearSelectedItem();
            }
            Message msg = _handler.obtainMessage(CMD_GET_ROUTE);//
            _handler.sendMessage(msg);

        });
        _spinnerCampus.setSpinnerOutsideTouchListener(new OnSpinnerOutsideTouchListener() {
            @Override
            public void onSpinnerOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                _spinnerCampus.dismiss();
            }
        });
        layoutTimeTable = findViewById(R.id.layout_timetable);
        layoutTimeTable.setOnClickListener(this);
//        _spinnerBus = findViewById(R.id.spinner_bus);
//        _spinnerBus.setIsFocusable(true);
    }
    private void setCampusSpinnerItems() {
        //        _campusList.add(new BCData("전체", ""));
        List<String> acaNames = new ArrayList<>();
        for (BCData data : _campusList) { acaNames.add(data.bcName); }
        Utils.updateSpinnerList(_spinnerCampus, acaNames);

        _spinnerCampus.setLifecycleOwner(this);
        if(_selectedBusCampus == null){
            if(_campusList != null) {
                _spinnerCampus.selectItemByIndex(0); //전체
            }
        }
    }
//    private void setBusSpinner() {
//        List<String> busNames = new ArrayList<>();
//        for (BusInfoData data : _busesList4Spinner) { busNames.add(data.bcName); }
//        _spinnerBus.setItems(busNames);
//        _spinnerBus.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
//            if(oldItem != null && oldItem.equals(newItem)) return;
//            ACAData selectedData = null;
//            Optional optional =  _busesList4Spinner.stream().filter(t -> t.busName == newItem).findFirst();
//            if(optional.isPresent()) {
//                _selectedBus = (BusInfoData) optional.get();
//                LogMgr.w(TAG,"selectedBus = " + _selectedBus.busName + " / " + _selectedBus.busCode);
//            }
//            //todo filtering
//
//        });
//        _spinnerBus.setSpinnerOutsideTouchListener(new OnSpinnerOutsideTouchListener() {
//            @Override
//            public void onSpinnerOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
//                _spinnerBus.dismiss();
//            }
//        });
//        _spinnerBus.setLifecycleOwner(this);
//        if(_spinnerBus == null){
//            if(_busesList4Spinner != null) {
//                _spinnerBus.selectItemByIndex(0); //전체
//            }
//        }
//    }
    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.main_menu_bus_info);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    private void requestBusCampusList(){
        if (RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getBusCampusList().enqueue(new Callback<BCListResponse>() {
                @Override
                public void onResponse(Call<BCListResponse> call, Response<BCListResponse> response) {
                    _campusList.clear();

                    try {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {

                                List<BCData> list = response.body().data;
                                if (list != null && !list.isEmpty()) {
                                    _campusList.addAll(list);
                                }
                            }
                        } else {
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestTestReserveList() Exception : ", e.getMessage());
                    }finally{
                        setCampusSpinnerItems();
                    }

                }

                @Override
                public void onFailure(Call<BCListResponse> call, Throwable t) {
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                    setCampusSpinnerItems();
                }
            });
        }
    }
    private void requestBusesList(){
        if (RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getBusesInfo(_selectedBusCampus.bcName).enqueue(new Callback<BusInfoResponse>() {
                @Override
                public void onResponse(Call<BusInfoResponse> call, Response<BusInfoResponse> response) {
                    _busesList.clear();

                    try {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {

                                List<BusInfoData> list = response.body().data;
                                if (list != null && !list.isEmpty()) {
                                    Collections.sort(list, new Comparator<BusInfoData>(){

                                        @Override
                                        public int compare(BusInfoData data1, BusInfoData data2) {
                                            int isDriveComparison = data2.isDrive.compareTo(data1.isDrive);
                                            if (isDriveComparison != 0) {
                                                return isDriveComparison;
                                            }
                                            int result = 0;
                                            Pattern pattern = Pattern.compile("\\d+");
                                            Matcher matcher1 = pattern.matcher(data1.busName);
                                            Matcher matcher2 = pattern.matcher(data2.busName);
                                            while (matcher1.find() && matcher2.find()) {
                                                String match1 = matcher1.group();
                                                String match2 = matcher2.group();

                                                int numberComparison = Integer.compare(Integer.parseInt(match1), Integer.parseInt(match2));
                                                if (numberComparison != 0) {
                                                    result = numberComparison;
                                                    break; // 숫자가 다르면 비교 종료
                                                }

                                                // 숫자 이후 문자열로 비교
                                                int textComparison = data1.busName.compareTo(data2.busName);
                                                if (textComparison != 0) {
                                                    result = textComparison;
                                                    break;
                                                }
                                            }

                                            // 숫자가 없는 경우에는 이름으로 비교
                                            if (result == 0) {
                                                result = data1.busName.compareTo(data2.busName);
                                            }

                                            return result;
                                        }
                                    });
                                    _busesList.addAll(list);
                                }
                            }
                        } else {
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestTestReserveList() Exception : ", e.getMessage());
                    }

                    if(_busesListAdapter != null) {
//                        _listAdapter.setWholeCampusMode(TextUtils.isEmpty(_selectedACA.acaCode));
                        _busesListAdapter.notifyDataSetChanged();
                    }
                    tvEmptyList.setVisibility(_busesList.isEmpty() ? View.VISIBLE : View.GONE);
                    _swipeRefreshLayout.setRefreshing(false);
                }

                @Override
                public void onFailure(Call<BusInfoResponse> call, Throwable t) {
                    _busesList.clear();
                    if(_busesListAdapter != null) _busesListAdapter.notifyDataSetChanged();
                    tvEmptyList.setVisibility(_busesList.isEmpty() ? View.VISIBLE : View.GONE);

                    _swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.layout_timetable:
                url = BUS_TIMETABLE_BASE_URL + _selectedBusCampus.bcCode;
                navigate2WebViewActivity(getString(R.string.title_bus_timetable));
                break;
        }
    }

    void navigate2RouteActivity(BusInfoData item) {
        Intent targetIntent = new Intent(MenuBusActivity.this, BusRouteActivity.class);
        targetIntent.putExtra(IntentParams.PARAM_BOARD_ITEM, item);
        targetIntent.putExtra(IntentParams.PARAM_BCNAME, _selectedBusCampus.bcName);
        startActivity(targetIntent);
        overridePendingTransition(R.anim.horizontal_enter, R.anim.horizontal_out);
    }
    private void navigate2WebViewActivity(String title){
        Intent intent = new Intent(mContext, WebViewActivity.class);
        intent.putExtra(IntentParams.PARAM_APPBAR_TITLE, title);
        intent.putExtra(IntentParams.PARAM_WEB_VIEW_URL, url);
        startActivity(intent);
    }
}