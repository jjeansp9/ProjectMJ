package kr.jeet.edu.manager.activity.menu.briefing;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.demogorgorn.monthpicker.MonthPickerDialog;
import com.skydoves.powerspinner.OnSpinnerOutsideTouchListener;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.activity.BaseActivity;
import kr.jeet.edu.manager.adapter.BriefingListAdapter;
import kr.jeet.edu.manager.adapter.WrapContentSpinnerAdapter;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.DataManager;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.db.JeetDatabase;
import kr.jeet.edu.manager.db.PushMessage;
import kr.jeet.edu.manager.fcm.FCMManager;
import kr.jeet.edu.manager.model.data.ACAData;
import kr.jeet.edu.manager.model.data.BriefingData;
import kr.jeet.edu.manager.model.data.StudentGradeData;
import kr.jeet.edu.manager.model.response.BriefingResponse;
import kr.jeet.edu.manager.model.response.StudentGradeListResponse;
import kr.jeet.edu.manager.server.RetrofitApi;
import kr.jeet.edu.manager.server.RetrofitClient;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.PreferenceUtil;
import kr.jeet.edu.manager.utils.Utils;
import kr.jeet.edu.manager.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuBriefingActivity extends BaseActivity implements MonthPickerDialog.OnDateSetListener{
    private static final String TAG = "briefing";

    private static final int CMD_GET_ACA_LIST = 0;
    private static final int CMD_GET_GRADE_LIST = 1;
    private static final int CMD_GET_BRIEFINGS = 2;
    private static final int CMD_DATE_CHANGED = 3;
    private PowerSpinnerView _spinnerCampus, _spinnerGrade;
    private RecyclerView _recyclerView;
    private SwipeRefreshLayout _swipeRefreshLayout;
    private BriefingListAdapter _listAdapter;
    private TextView _tvCalendar, tvEmptyList;
    private Button _btnCalendarPrev, _btnCalendarNext;
    private List<BriefingData> _list = new ArrayList<>();
    private RetrofitApi mRetrofitApi;
    List<ACAData> _ACAList = new ArrayList<>();
    ACAData _selectedACA = null;
    List<StudentGradeData> _GradeList = new ArrayList<>();
    private StudentGradeData _selectedGrade = null;
    Date _selectedDate = new Date();
    SimpleDateFormat _dateFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYY_MM_KOR);
    int _userGubun = 1;
    int _seq = 0;
    int _sfCode = 0;

    private String _acaCode = "";
    private String _appAcaCode = "";
    String _userType = "";

    Menu _menu;
    private Handler _handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case CMD_GET_ACA_LIST:
                    break;
                case CMD_GET_GRADE_LIST:
                    if (_GradeList != null && !_GradeList.isEmpty()) {
//                        _spinnerGrade.setEnabled(true);
                        Utils.updateSpinnerList(_spinnerGrade, _GradeList.stream().map(t -> t.gubunName).collect(Collectors.toList()));
                        Optional optional = (_GradeList.stream().filter(t->TextUtils.isEmpty(t.gubunCode)).findFirst());
                        if(optional.isPresent()) {
                            int index = _GradeList.indexOf(optional.get());
                            _spinnerGrade.selectItemByIndex(index);
                        }else{
                            _spinnerGrade.selectItemByIndex(0);
                        }
                    }else {
                        _handler.sendEmptyMessage(CMD_GET_BRIEFINGS);
                    }
                    break;
                case CMD_GET_BRIEFINGS:
                    //todo request logic
//                    int year = msg.arg1;
//                    int month = msg.arg2 + 1;
                    requestBriefingList();
                    break;
                case CMD_DATE_CHANGED:
                    _tvCalendar.setText(_dateFormat.format(_selectedDate));
                    break;
            }
        }
    };
    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        LogMgr.w("result =" + result);
//        if(result.getResultCode() != RESULT_CANCELED) {
//            Calendar calendar = Calendar.getInstance();
//            calendar.setTime(_selectedDate);
//            Message msg = _handler.obtainMessage(CMD_GET_BRIEFINGS);
//            msg.arg1 = calendar.get(Calendar.YEAR);
//            msg.arg2 = calendar.get(Calendar.MONTH);
//            _handler.sendMessage(msg);
//        }
        if(result.getResultCode() != RESULT_CANCELED) {
            Intent intent = result.getData();
            if(intent == null) return;
            if(intent.hasExtra(IntentParams.PARAM_BOARD_ADDED)) {
                boolean added = intent.getBooleanExtra(IntentParams.PARAM_BOARD_ADDED, false);

                if(added) {
//                    Calendar calendar = Calendar.getInstance();
//                    calendar.setTime(_selectedDate);
                    Message msg = _handler.obtainMessage(CMD_GET_BRIEFINGS);
//                    msg.arg1 = calendar.get(Calendar.YEAR);
//                    msg.arg2 = calendar.get(Calendar.MONTH);
                    _handler.sendMessage(msg);
                }
            }
            else if(intent.hasExtra(IntentParams.PARAM_BOARD_DELETED)) {
                boolean deleted = intent.getBooleanExtra(IntentParams.PARAM_BOARD_DELETED, false);
                int position = intent.getIntExtra(IntentParams.PARAM_BOARD_POSITION, -1);
                if(deleted && position >= 0) {
                    _list.remove(position);
                    _listAdapter.notifyItemRemoved(position);
                }
            }else if(intent.hasExtra(IntentParams.PARAM_BOARD_EDITED)) {
                boolean edited = intent.getBooleanExtra(IntentParams.PARAM_BOARD_EDITED, false);
                BriefingData changedItem = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    changedItem = intent.getParcelableExtra(IntentParams.PARAM_BOARD_ITEM, BriefingData.class);
                }else{
                    changedItem = intent.getParcelableExtra(IntentParams.PARAM_BOARD_ITEM);
                }
                LogMgr.w("edited =" + changedItem);
                int position = intent.getIntExtra(IntentParams.PARAM_BOARD_POSITION, -1);
                LogMgr.w("position =" + position);
                if(edited && position >= 0 && changedItem != null) {
                    _list.set(position, changedItem);
                    _listAdapter.notifyItemChanged(position);
                }
            }
        }else{
            Intent intent = result.getData();
            if(intent == null) return;
            BriefingData changedItem = null;
            if(intent.hasExtra(IntentParams.PARAM_BOARD_ITEM)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    changedItem = intent.getParcelableExtra(IntentParams.PARAM_BOARD_ITEM, BriefingData.class);
                } else {
                    changedItem = intent.getParcelableExtra(IntentParams.PARAM_BOARD_ITEM);
                }
            }
            LogMgr.w("edited =" + changedItem);
            int position = intent.getIntExtra(IntentParams.PARAM_BOARD_POSITION, -1);
            LogMgr.w("position =" + position);
            if(position >= 0 && changedItem != null) {
                _list.set(position, changedItem);
                _listAdapter.notifyItemChanged(position);
            }
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_briefing);
        mContext = this;
        _acaCode = PreferenceUtil.getAcaCode(mContext);
        _appAcaCode = PreferenceUtil.getAppAcaCode(mContext);
        _userGubun = PreferenceUtil.getUserGubun(this);
        _seq = PreferenceUtil.getUserSeq(this);
        _sfCode = PreferenceUtil.getUserSFCode(this);
        LogMgr.e(_userGubun + "/" + _seq + "/" + _sfCode);
        //test

        initAppbar();
        initView();
        changeMessageState2Read();
        setAnimMove(Constants.MOVE_DOWN);
    }
    void changeMessageState2Read() {
        new Thread(() -> {
            List<PushMessage> pushMessages = JeetDatabase.getInstance(getApplicationContext()).pushMessageDao().getMessageByReadFlagNType(false, FCMManager.MSG_TYPE_PT);
            if(!pushMessages.isEmpty()) {
                for(PushMessage message : pushMessages) {
                    message.isRead = true;
                    JeetDatabase.getInstance(getApplicationContext()).pushMessageDao().update(message);
                }
            }
        }).start();
    }
    void initView() {
        _tvCalendar = findViewById(R.id.tv_calendar);
        _tvCalendar.setOnClickListener(this);
        _tvCalendar.setText(_dateFormat.format(_selectedDate));
        _btnCalendarPrev = findViewById(R.id.btn_calendar_previous);
        _btnCalendarPrev.setOnClickListener(this);
        _btnCalendarNext = findViewById(R.id.btn_calendar_next);
        _btnCalendarNext.setOnClickListener(this);
        tvEmptyList = findViewById(R.id.tv_brf_empty_list);
        _swipeRefreshLayout = findViewById(R.id.refresh_layout);
        _swipeRefreshLayout.setOnRefreshListener( () ->{
//            Calendar calendar = Calendar.getInstance();
//            calendar.setTime(_selectedDate);
            Message msg = _handler.obtainMessage(CMD_GET_BRIEFINGS);
//            msg.arg1 = calendar.get(Calendar.YEAR);
//            msg.arg2 = calendar.get(Calendar.MONTH);
            _handler.sendMessage(msg);

        });
        //region set recyclerview
        _recyclerView = findViewById(R.id.recycler_briefing);
        _listAdapter = new BriefingListAdapter(mContext, _list, new BriefingListAdapter.ItemClickListener() {
            @Override
            public void onItemClick(BriefingData item, int position) {
                navigate2DetailActivity(item, position);
            }
        });

        _recyclerView.setAdapter(_listAdapter);
        _recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        //endregion
        //region spinner

        _spinnerCampus = findViewById(R.id.spinner_brf_campus);
        _spinnerGrade = findViewById(R.id.spinner_brf_grade);
        _spinnerCampus.setIsFocusable(true);
        _ACAList.add(new ACAData("", "전체", ""));
        _ACAList.addAll(DataManager.getInstance().getLocalACAListMap().values());
        List<String> acaNames = new ArrayList<>();
        for (ACAData data : _ACAList) { acaNames.add(data.acaName); }
        {
            WrapContentSpinnerAdapter adapter = new WrapContentSpinnerAdapter(mContext, _ACAList.stream().map(t -> t.acaName).collect(Collectors.toList()), _spinnerCampus);
            _spinnerCampus.setSpinnerAdapter(adapter);
        }

//        _spinnerCampus.setText(acaNames.get(0));
        Utils.updateSpinnerList(_spinnerCampus, acaNames);
        _spinnerCampus.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
            if(oldItem != null && oldItem.equals(newItem)) return;
            ACAData selectedData = null;
            Optional optional =  _ACAList.stream().filter(t -> t.acaName == newItem).findFirst();
            if(optional.isPresent()) {
                _selectedACA = (ACAData) optional.get();
                LogMgr.w(TAG,"selectedACA = " + _selectedACA.acaCode + " / " + _selectedACA.acaName);
            }
            if(_selectedACA != null) {
                LogMgr.w("selectedACA = " + _selectedACA.acaCode + " / " + _selectedACA.acaName);
                if (_selectedGrade != null) {
                    _selectedGrade = null;
                }
                if (_spinnerGrade != null) _spinnerGrade.clearSelectedItem();
                if(!TextUtils.isEmpty(_selectedACA.acaCode)) {
                    requestGradeList(_selectedACA.acaCode);
                }else{
                    if(_GradeList != null) _GradeList.clear();
                    _handler.sendEmptyMessage(CMD_GET_BRIEFINGS);
                }
                if(TextUtils.isEmpty(_selectedACA.acaCode)){   //전체
                    if (_spinnerGrade != null) _spinnerGrade.setEnabled(false);
                }else{
                    if (_spinnerGrade != null) _spinnerGrade.setEnabled(true);
                }
            }

        });
        _spinnerCampus.setSpinnerOutsideTouchListener(new OnSpinnerOutsideTouchListener() {
            @Override
            public void onSpinnerOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                _spinnerCampus.dismiss();
            }
        });
        _spinnerCampus.setLifecycleOwner(this);
        if(!TextUtils.isEmpty(_appAcaCode) && _userGubun >= Constants.USER_TYPE_TEACHER){
            ACAData selectedACA = null;
            Optional option =  _ACAList.stream().filter(t -> t.acaCode.equals(_appAcaCode)).findFirst();
            if(option.isPresent()) {
                selectedACA = (ACAData) option.get();
            }

            try {
                if (selectedACA != null) {
                    int selectedIndex = _ACAList.indexOf(selectedACA);
                    _spinnerCampus.selectItemByIndex(selectedIndex); //전체

                } else {
                    _spinnerCampus.selectItemByIndex(0);
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }

        }else{
            _spinnerCampus.selectItemByIndex(0);
        }

        {
            WrapContentSpinnerAdapter adapter = new WrapContentSpinnerAdapter(mContext, _GradeList.stream().map(t -> t.gubunName).collect(Collectors.toList()), _spinnerGrade);
            _spinnerGrade.setSpinnerAdapter(adapter);
        }

        _spinnerGrade.setIsFocusable(true);
        _spinnerGrade.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
            LogMgr.e(newItem + " selected");
            if(oldItem != null && oldItem.equals(newItem)) return;
            StudentGradeData selectedData = null;
            Optional optional = _GradeList.stream().filter(t -> t.gubunName == newItem).findFirst();
            if(optional.isPresent()) {
                selectedData = (StudentGradeData) optional.get();
            }
            _selectedGrade = selectedData;
            if(_selectedGrade != null) {
                LogMgr.w("selectedGubun = " + _selectedGrade.gubunCode + " / " + _selectedGrade.gubunName);
            }
            requestBriefingList();
        });
        _spinnerGrade.setSpinnerOutsideTouchListener(new OnSpinnerOutsideTouchListener() {
            @Override
            public void onSpinnerOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                _spinnerGrade.dismiss();
            }
        });
        _spinnerGrade.setLifecycleOwner(this);
        //endregion
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.tv_calendar:
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(_selectedDate);
                Utils.yearMonthPicker(mContext, this::onDateSet, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
                break;
            case R.id.btn_calendar_previous:
                navigateMonth(-1);
                break;
            case R.id.btn_calendar_next:
                navigateMonth(1);
                break;
        }
    }

    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.title_briefing);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    private void navigateMonth(int addMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(_selectedDate);
        calendar.add(Calendar.MONTH, addMonth);
        onDateSet(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
    }
//    void showDatePicker() {
//        DatePickerFragment datePickerDialog = new DatePickerFragment(this);
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(_selectedDate);
//        datePickerDialog.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
//        datePickerDialog.show(getSupportFragmentManager(), "date");
//    }
    @Override
    public void onDateSet(int month, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(_selectedDate);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        _selectedDate = calendar.getTime();
        LogMgr.e(TAG, "selectedDate = " + _selectedDate);
        _handler.sendEmptyMessage(CMD_DATE_CHANGED);

        if(_spinnerCampus.getSelectedIndex() >= 0) {
            Message msg = _handler.obtainMessage(CMD_GET_BRIEFINGS);//
//            msg.arg1 = calendar.get(Calendar.YEAR);
//            msg.arg2 = calendar.get(Calendar.MONTH);
            _handler.sendMessage(msg);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(_userGubun == Constants.USER_TYPE_ADMIN || _userGubun == Constants.USER_TYPE_SUPER_ADMIN) {
            getMenuInflater().inflate(R.menu.menu_board, menu);
            this._menu = menu;
        }
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_add:
                navigate2EditBriefingActivity();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void navigate2EditBriefingActivity() {
        Intent editIntent = new Intent(mContext, EditBriefingActivity.class);
        if(_selectedACA != null) {
            editIntent.putExtra(IntentParams.PARAM_STU_ACACODE, _selectedACA.acaCode);
        }
        if(_selectedGrade != null) {
            editIntent.putExtra(IntentParams.PARAM_STU_GRADECODE, _selectedGrade.gubunCode);
        }
        resultLauncher.launch(editIntent);
//        startActivity(editIntent);
    }
    public void navigate2DetailActivity(BriefingData briefingData, int position) {
        Intent detailIntent = new Intent(mContext, MenuBriefingDetailActivity.class);
        detailIntent.putExtra(IntentParams.PARAM_BRIEFING_INFO, briefingData);
        detailIntent.putExtra(IntentParams.PARAM_BOARD_POSITION, position);
        resultLauncher.launch(detailIntent);
        overridePendingTransition(R.anim.horizontal_enter, R.anim.horizontal_out);

    }
    private void requestBriefingList(){
        if (RetrofitClient.getInstance() != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(_selectedDate);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            String acaCode = "";
            String gradeCode = "";
            if(_selectedACA != null) acaCode = _selectedACA.acaCode;
            if(_selectedGrade != null) gradeCode = String.valueOf(_selectedGrade.gubunCode);
            RetrofitClient.getApiInterface().getBriefingList(acaCode, gradeCode, year, month).enqueue(new Callback<BriefingResponse>() {
                @Override
                public void onResponse(Call<BriefingResponse> call, Response<BriefingResponse> response) {
                    _list.clear();

                    try {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {

                                List<BriefingData> list = response.body().data;
                                if (list != null && !list.isEmpty()) {
                                    _list.addAll(list);
                                }
                            }
                        } else {
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestTestReserveList() Exception : ", e.getMessage());
                    }

                    if(_listAdapter != null) {
                        LogMgr.d(TAG, "_selectedACACode = " + _selectedACA.acaCode);
//                        _listAdapter.setWholeCampusMode(TextUtils.isEmpty(_selectedACA.acaCode));
                        _listAdapter.notifyDataSetChanged();
                    }
                    if(_list.size() > 0 && _recyclerView != null) {
                        _handler.postDelayed(() -> _recyclerView.smoothScrollToPosition(0), scrollToTopDelay);
                    }
                    tvEmptyList.setVisibility(_list.isEmpty() ? View.VISIBLE : View.GONE);
                    _swipeRefreshLayout.setRefreshing(false);
                }

                @Override
                public void onFailure(Call<BriefingResponse> call, Throwable t) {
                    _list.clear();
                    if(_listAdapter != null) _listAdapter.notifyDataSetChanged();
                    tvEmptyList.setVisibility(_list.isEmpty() ? View.VISIBLE : View.GONE);

                    _swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void requestGradeList(String acaCode){
        if(RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.getStudentGradeList(acaCode).enqueue(new Callback<StudentGradeListResponse>() {
                @Override
                public void onResponse(Call<StudentGradeListResponse> call, Response<StudentGradeListResponse> response) {
                    if(response.isSuccessful()) {

                        if(response.body() != null) {
                            List<StudentGradeData> list = response.body().data;
                            if(_GradeList != null) _GradeList.clear();
                            _GradeList.add(new StudentGradeData("", "구분 전체"));
                            _GradeList.addAll(list);
                            Collections.sort(_GradeList, new Comparator<StudentGradeData>() {
                                @Override
                                public int compare(StudentGradeData schoolData, StudentGradeData t1) {
                                    return schoolData.gubunCode.compareTo(t1.gubunCode);
                                }
                            });
                            _handler.sendEmptyMessage(CMD_GET_GRADE_LIST);
                        }
                    } else {

                        try {
                            LogMgr.e(TAG, "requestGradeList() errBody : " + response.errorBody().string());
                        } catch (IOException e) {
                        }

                    }

                }

                @Override
                public void onFailure(Call<StudentGradeListResponse> call, Throwable t) {
                    LogMgr.e(TAG, "requestGradeList() onFailure >> " + t.getMessage());
//                    _handler.sendEmptyMessage(CMD_GET_GRADE_LIST);
                }
            });
        }
    }
}