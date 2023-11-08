package kr.jeet.edu.manager.activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.demogorgorn.monthpicker.MonthPickerDialog;
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;
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
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.adapter.NoticeListAdapter;
import kr.jeet.edu.manager.adapter.WrapContentSpinnerAdapter;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.DataManager;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.db.JeetDatabase;
import kr.jeet.edu.manager.db.PushMessage;
import kr.jeet.edu.manager.fcm.FCMManager;
import kr.jeet.edu.manager.model.data.ACAData;
import kr.jeet.edu.manager.model.data.NoticeData;
import kr.jeet.edu.manager.model.data.StudentGradeData;
import kr.jeet.edu.manager.model.response.NoticeListResponse;
import kr.jeet.edu.manager.model.response.StudentGradeListResponse;
import kr.jeet.edu.manager.server.RetrofitClient;
import kr.jeet.edu.manager.utils.Converters;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.PreferenceUtil;
import kr.jeet.edu.manager.utils.Utils;
import kr.jeet.edu.manager.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuNoticeActivity extends BaseActivity implements MonthPickerDialog.OnDateSetListener{
    private static final String TAG = "notice";
    private static final int CMD_GET_ACA_LIST = 0;
    private static final int CMD_GET_GRADE_LIST = 1;
    private static final int CMD_GET_DATAS = 2;
    private static final int CMD_DATE_CHANGED = 3;

    //region views
    private PowerSpinnerView _spinnerType, _spinnerCampus, _spinnerGrade;
    private RecyclerView _recyclerView;
    private SwipeRefreshLayout _swipeRefreshLayout;
    private NoticeListAdapter _listAdapter;
    private TextView tvEmpty, tvCalendar;
    //endregion

    //region member variables

    int _userGubun = 1;
    int _seq = 0;
    int _sfCode = 0;
    private String _acaCode = "";
    private String _appAcaCode = "";
    String _userType = "";
    Menu _menu;
    private ArrayList<NoticeData> _noticeList = new ArrayList<NoticeData>();
    List<ACAData> _ACAList = new ArrayList<>();
    ACAData _selectedACA = null;    //상담요청
    ACAData _selectedLocalACA = null;   //시스템
    List<StudentGradeData> _GradeList = new ArrayList<>();
    private StudentGradeData _selectedGrade = null;

    private Constants.NoticeType _selectedNotice = Constants.NoticeType.SYSTEM;
    Date _selectedDate = new Date();

    SimpleDateFormat _dateFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYY_MM_KOR, Locale.KOREA);
    SimpleDateFormat _monthFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYY_MM_KOR);
    SimpleDateFormat apiDateFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYYMM);
    //endregion
    private Handler _handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case CMD_GET_ACA_LIST:
                    if(_selectedNotice != null) {
                        if(_ACAList != null) _ACAList.clear();
                        switch(_selectedNotice) {
                            case SYSTEM:
                                _ACAList.add(new ACAData("", "전체", ""));
                                _ACAList.addAll(DataManager.getInstance().getLocalACAListMap().values());
                                break;
                            case COUNSEL:
                                _ACAList.add(new ACAData("", "전체", ""));
                                _ACAList.addAll(DataManager.getInstance().getACAListMap().values());
                                break;
                        }

                    }
                    List<String> acaNames = new ArrayList<>();
                    for (ACAData data : _ACAList) { acaNames.add(data.acaName); }
//        _spinnerCampus.setText(acaNames.get(0));
                    Utils.updateSpinnerList(_spinnerCampus, acaNames);
                    String selAcaCode = "";
                    switch(_selectedNotice) {
                        case SYSTEM:
                            if(_selectedLocalACA != null) {
                                selAcaCode = _selectedLocalACA.acaCode;
                            }else if(_userGubun >= Constants.USER_TYPE_TEACHER) {
                                selAcaCode = _appAcaCode;
                            }
                            break;
                        case COUNSEL:

                            if(_selectedACA == null) {
                                selAcaCode = _acaCode;
                            }else{
                                selAcaCode = _selectedACA.acaCode;
                            }

                            break;
                    }
                    if(!TextUtils.isEmpty(selAcaCode)){
                        ACAData selectedACA = null;
                        String finalSelAcaCode = selAcaCode;
                        Optional option =  _ACAList.stream().filter(t -> t.acaCode.equals(finalSelAcaCode)).findFirst();
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
                    _handler.sendEmptyMessage(CMD_GET_DATAS);
                    break;
                case CMD_GET_GRADE_LIST:
                    if (_GradeList != null && !_GradeList.isEmpty()) {
                        _spinnerGrade.setEnabled(true);
                        Utils.updateSpinnerList(_spinnerGrade, _GradeList.stream().map(t -> t.gubunName).collect(Collectors.toList()));
                        Optional optional = (_GradeList.stream().filter(t->TextUtils.isEmpty(t.gubunCode)).findFirst());
                        if(optional.isPresent()) {
                            int index = _GradeList.indexOf(optional.get());
                            _spinnerGrade.selectItemByIndex(index);
                        }else{
                            _spinnerGrade.selectItemByIndex(0);
                        }
                    }
//                    _handler.sendEmptyMessage(CMD_GET_DATAS);
                    break;
                case CMD_GET_DATAS:
                    //todo request logic
                    if (_noticeList.size() > 0) _noticeList.clear();
                    requestNoticeList();
                    break;
                case CMD_DATE_CHANGED:
                    tvCalendar.setText(_dateFormat.format(_selectedDate));
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
                        requestNoticeList();
                    }
                }
            }
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_notice);
//        noticeType = getResources().getStringArray(R.array.notice_type);
        _acaCode = PreferenceUtil.getAcaCode(mContext);
        _appAcaCode = PreferenceUtil.getAppAcaCode(mContext);
        _userGubun = PreferenceUtil.getUserGubun(this);
        _seq = PreferenceUtil.getUserSeq(this);
        _sfCode = PreferenceUtil.getUserSFCode(this);
        initIntentData();
        initView();
        initAppbar();
        setAnimMove(Constants.MOVE_DOWN);
    }
    void initIntentData(){
        Intent intent = getIntent();
        if(intent == null) return;
        if(intent.hasExtra(IntentParams.PARAM_NOTICE_TYPE)) {
            _selectedNotice = (Constants.NoticeType) intent.getSerializableExtra(IntentParams.PARAM_NOTICE_TYPE);
        }
    }
    @Override
    void initView() {
        findViewById(R.id.btn_calendar_previous).setOnClickListener(this);
        findViewById(R.id.btn_calendar_next).setOnClickListener(this);

        tvCalendar = findViewById(R.id.tv_calendar);
        tvEmpty = (TextView) findViewById(R.id.tv_empty_list);
        _swipeRefreshLayout = findViewById(R.id.refresh_layout);
        _recyclerView = (RecyclerView) findViewById(R.id.recycler_notice);


        tvCalendar.setOnClickListener(this);
        tvCalendar.setText(_dateFormat.format(_selectedDate));

        setSpinner();
        setRecycler();
        _swipeRefreshLayout.setOnRefreshListener(() -> requestNoticeList());
    }
    private void setSpinner(){
        //region spinner notice type
        _spinnerType = findViewById(R.id.spinner_notice_type);
        {
            WrapContentSpinnerAdapter adapter = new WrapContentSpinnerAdapter(mContext, Constants.NoticeType.getNameList(), _spinnerType);
            _spinnerType.setSpinnerAdapter(adapter);
        }

        _spinnerType.setIsFocusable(true);
        List<String> noticeTypeNames = Constants.NoticeType.getNameList();
        _spinnerType.setItems(noticeTypeNames);
        _spinnerType.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
            if(oldItem != null && oldItem.equals(newItem)) return;
            _selectedNotice = Constants.NoticeType.getByName(newItem.toString());
            changeView(_selectedNotice);
            changeMessageState2Read(_selectedNotice);
            _handler.sendEmptyMessage(CMD_GET_ACA_LIST);
        });

        _spinnerType.setSpinnerOutsideTouchListener((view, motionEvent) -> _spinnerType.dismiss());
        _spinnerType.setLifecycleOwner(this);


        //endregion
        //region spinner campus
        _spinnerCampus = findViewById(R.id.spinner_campus);
        {
            WrapContentSpinnerAdapter adapter = new WrapContentSpinnerAdapter(mContext, _ACAList.stream().map(t -> t.acaName).collect(Collectors.toList()), _spinnerCampus);
            _spinnerCampus.setSpinnerAdapter(adapter);
        }

        _spinnerCampus.setIsFocusable(true);

        _spinnerCampus.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
            if(oldItem != null && oldItem.equals(newItem)) return;
            Optional optional =  _ACAList.stream().filter(t -> t.acaName == newItem).findFirst();
            if(optional.isPresent()) {
                if(_selectedNotice != null) {
                    switch(_selectedNotice){
                        case SYSTEM:
                            _selectedLocalACA = (ACAData) optional.get();
                            LogMgr.w(TAG, "_selectedLocalACA = " + _selectedLocalACA.acaCode + " / " + _selectedLocalACA.acaName);
                            if(!TextUtils.isEmpty(_selectedLocalACA.acaCode)) {
                                requestGradeList(_selectedLocalACA.acaCode);
                            }else{
                                _handler.sendEmptyMessage(CMD_GET_DATAS);
                            }
                            if (_selectedGrade != null) {
                                _selectedGrade = null;
                            }
                            if (_spinnerGrade != null) _spinnerGrade.clearSelectedItem();
                            if(TextUtils.isEmpty(_selectedLocalACA.acaCode)){   //전체
                                if (_spinnerGrade != null) _spinnerGrade.setEnabled(false);
                            }else{
                                if (_spinnerGrade != null) _spinnerGrade.setEnabled(true);
                            }
                            break;
                        case COUNSEL:
                            _selectedACA = (ACAData) optional.get();
                            LogMgr.w(TAG, "selectedACA = " + _selectedACA.acaCode + " / " + _selectedACA.acaName);
                            Message msg = _handler.obtainMessage(CMD_GET_DATAS);//
                            _handler.sendMessage(msg);
                            break;
                    }
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

        _spinnerGrade = findViewById(R.id.spinner_grade);
        {
            WrapContentSpinnerAdapter adapter = new WrapContentSpinnerAdapter(mContext, _GradeList.stream().map(t -> t.gubunName).collect(Collectors.toList()), _spinnerGrade);
            _spinnerGrade.setSpinnerAdapter(adapter);
        }

        _spinnerGrade.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener<String>() {
            @Override
            public void onItemSelected(int oldIndex, @androidx.annotation.Nullable String oldItem, int newIndex, String newItem) {
                LogMgr.e(newItem + " selected");
                if(oldItem != null && oldItem.equals(newItem)) return;
                StudentGradeData selectedData = null;
                try{
                    selectedData = _GradeList.stream().filter(t -> t.gubunName.equals(newItem)).findFirst().get();
                    LogMgr.w("selectedGrade = " + selectedData.gubunCode + " / " + selectedData.gubunName);
                }catch(Exception ex){}
                _selectedGrade = selectedData;
                _handler.sendEmptyMessage(CMD_GET_DATAS);
            }
        });
        _spinnerGrade.setSpinnerOutsideTouchListener(new OnSpinnerOutsideTouchListener() {
            @Override
            public void onSpinnerOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                _spinnerGrade.dismiss();
            }
        });
        _spinnerGrade.setLifecycleOwner(this);
        //endregion
        switch(_selectedNotice){
            case SYSTEM:
                if(noticeTypeNames.size() > 0) _spinnerType.selectItemByIndex(0);
                break;
            case COUNSEL:
                if(noticeTypeNames.size() > 1)_spinnerType.selectItemByIndex(1);
                break;
//            case LEVEL_TEST:
//                if(noticeTypeNames.size() > 2)_spinnerType.selectItemByIndex(2);
//                break;
        }
    }
    void changeMessageState2Read(Constants.NoticeType type) {
        new Thread(() -> {
            List<PushMessage> pushMessages = JeetDatabase.getInstance(getApplicationContext()).pushMessageDao().getMessageByReadFlagNType(false, type.name());
            if(!pushMessages.isEmpty()) {
                for(PushMessage message : pushMessages) {
                    message.isRead = true;
                    JeetDatabase.getInstance(getApplicationContext()).pushMessageDao().update(message);
                }
            }
        }).start();
    }
    void changeView(Constants.NoticeType type) {
        switch(type) {
            case SYSTEM:
                if(_spinnerGrade != null) _spinnerGrade.setVisibility(View.VISIBLE);
                break;
            case COUNSEL:
                if(_spinnerGrade != null) _spinnerGrade.setVisibility(View.GONE);
                break;
        }
    }

    private void setRecycler(){
        _listAdapter = new NoticeListAdapter(mContext, _noticeList, this::startActivity);
        _recyclerView.setAdapter(_listAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL);
        Drawable dividerColor = new ColorDrawable(ContextCompat.getColor(this, R.color.line_2));

        dividerItemDecoration.setDrawable(dividerColor);
        _recyclerView.addItemDecoration(dividerItemDecoration);

        _recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
//                if(_selectedNotice.equals(Constants.NoticeType.LEVEL_TEST)) return; //레벨테스트는 페이징 없음
                if(((!_recyclerView.canScrollVertically(1)) && _recyclerView.canScrollVertically(-1))
                        && newState == RecyclerView.SCROLL_STATE_IDLE
                        && (_noticeList != null && !_noticeList.isEmpty()))
                {
                    int lastNoticeSeq = _noticeList.get(_noticeList.size() - 1).seq;
                    requestNoticeList(lastNoticeSeq);
                }
            }
        });
    }
    private void updateRecyclerView() {
        runOnUiThread(() -> {
            if(_listAdapter != null) _listAdapter.notifyDataSetChanged();
            if (tvEmpty != null) tvEmpty.setVisibility(_noticeList.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }
    private void startActivity(NoticeData item){
        if (item != null){
            Intent intent = null;
            switch(item.searchType){
                case FCMManager.MSG_TYPE_SYSTEM:
                    intent = new Intent(MenuNoticeActivity.this, SystemNoticeDetailActivity.class);
                    break;
                case FCMManager.MSG_TYPE_COUNSEL:
                    intent = new Intent(MenuNoticeActivity.this, CounselDetailActivity.class);
                    break;
                default:
                    return;
            }

            intent.putExtra(IntentParams.PARAM_BOARD_SEQ, item.seq);
            startActivity(intent);
            overridePendingTransition(R.anim.horizontal_enter, R.anim.horizontal_out);
        }else LogMgr.e("item is null ");
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
    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.main_menu_notice);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_board, menu);
        this._menu = menu;

        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_add:
                navigate2EditNoticeActivity();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void navigate2EditNoticeActivity() {
        Intent editIntent = new Intent(mContext, EditNoticeActivity.class);
        if(_selectedLocalACA != null) {
            editIntent.putExtra(IntentParams.PARAM_STU_ACACODE, _selectedLocalACA.acaCode);
        }
        if(_selectedGrade != null) {
            editIntent.putExtra(IntentParams.PARAM_STU_GRADECODE, _selectedGrade.gubunCode);
        }
        resultLauncher.launch(editIntent);
//        startActivity(editIntent);
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
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);
        _selectedDate = calendar.getTime();
        LogMgr.e(TAG, "selectedDate = " + _selectedDate);
        _handler.sendEmptyMessage(CMD_DATE_CHANGED);

        if(_spinnerCampus.getSelectedIndex() >= 0) {
            Message msg = _handler.obtainMessage(CMD_GET_DATAS);//
//            msg.arg1 = calendar.get(Calendar.YEAR);
//            msg.arg2 = calendar.get(Calendar.MONTH);
            _handler.sendMessage(msg);
        }
    }

    // 알림장 글 목록 조회
    private void requestNoticeList(int... lastSeq){
        boolean isFromServer = true;
//        switch(_selectedNotice){
//            case SYSTEM:
//            case COUNSEL:
//                isFromServer = true;
//                break;
//            case LEVEL_TEST:
//                isFromServer = false;
//                break;
//        }
        if(isFromServer) {
            int lastNoticeSeq = 0;
            if (lastSeq != null && lastSeq.length > 0) {
                LogMgr.e(TAG, "lastSeq[0] = " + lastSeq[0]);
                lastNoticeSeq = lastSeq[0];
            }

            if (RetrofitClient.getInstance() != null) {
                String searchType = _selectedNotice.name();
                String apiDate = apiDateFormat.format(_selectedDate);
                final int finalLastNoticeSeq = lastNoticeSeq;
                String acaCode = "";
                String gradeCode = "";
                switch(_selectedNotice){
                    case SYSTEM:
                        if(_selectedLocalACA != null) acaCode = _selectedLocalACA.acaCode;
                        if(_selectedGrade != null) gradeCode = String.valueOf(_selectedGrade.gubunCode);
                        break;
                    case COUNSEL:
                        if(_selectedACA != null) acaCode = _selectedACA.acaCode;
                        break;
                }


                RetrofitClient.getApiInterface().getNoticeList(searchType, apiDate, _sfCode, _seq, _userGubun, lastNoticeSeq, acaCode, gradeCode).enqueue(new Callback<NoticeListResponse>() {
                    @Override
                    public void onResponse(Call<NoticeListResponse> call, Response<NoticeListResponse> response) {
                        try {
                            if (response.isSuccessful()) {
                                List<NoticeData> getData = null;

                                if (response.body() != null) {
                                    getData = response.body().data;
                                    if (getData != null) {
                                        if(finalLastNoticeSeq == 0) {
                                            if (_noticeList.size() > 0) _noticeList.clear();
                                        }
                                        //for (AnnouncementData data : getData) mList.add(data);
                                        _noticeList.addAll(getData);
                                        updateRecyclerView();

                                    } else LogMgr.e(TAG, "ListData is null");
                                }
                            } else {
                                Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            LogMgr.e(TAG + "requestBoardList() Exception : ", e.getMessage());
                        } finally {
                            _swipeRefreshLayout.setRefreshing(false);
                        }
                    }

                    @Override
                    public void onFailure(Call<NoticeListResponse> call, Throwable t) {
                        try {
                            LogMgr.e(TAG, "requestBoardList() onFailure >> " + t.getMessage());
                        } catch (Exception e) {
                        }
//                    hideProgressDialog();
                        _swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }else{  //로컬 DB에서 가져오기

            new Thread(() -> {
                //test//
//                List<PushMessage> allItems = JeetDatabase.getInstance(mContext).pushMessageDao().getAllMessage();
//                for(PushMessage message : allItems) {
//                    LogMgr.e(TAG, String.format(message.id + " / " + message.pushType + " / " + message.date + " / " + message.title));
//                }

                String searchType = _selectedNotice.name();
//                searchType = "NOTICE";//test
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(_selectedDate);
                String year = String.valueOf(calendar.get(Calendar.YEAR));
                String month = String.format("%02d", calendar.get(Calendar.MONTH) + 1);
                List<PushMessage> items = JeetDatabase.getInstance(mContext).pushMessageDao().getMessageTypeAtTime(searchType, year ,month);
                LogMgr.d(TAG, String.format("%s %s/%s : db Count = %d", searchType, year, month, items.size()));
                if (_noticeList.size() > 0) _noticeList.clear();
                for(PushMessage message : items) {
                    NoticeData data = new NoticeData();
                    data.searchType = message.pushType;
                    if(message.date != null) {
                        data.insertDate = message.date.format(Converters.formatter);
                    }
                    data.seq = message.connSeq;
                    data.title = message.title;
                    data.acaCode = message.acaCode;
                    _noticeList.add(data);
                }
                updateRecyclerView();
            }).start();
        }
    }
    private void requestGradeList(String acaCode){
        if(RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getStudentGradeList(acaCode).enqueue(new Callback<StudentGradeListResponse>() {
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