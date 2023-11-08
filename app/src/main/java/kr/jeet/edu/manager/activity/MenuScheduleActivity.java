package kr.jeet.edu.manager.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
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

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.skydoves.powerspinner.OnSpinnerOutsideTouchListener;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.adapter.ScheduleListAdapter;
import kr.jeet.edu.manager.adapter.WrapContentSpinnerAdapter;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.DataManager;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.db.JeetDatabase;
import kr.jeet.edu.manager.db.PushMessage;
import kr.jeet.edu.manager.fcm.FCMManager;
import kr.jeet.edu.manager.model.data.ACAData;
import kr.jeet.edu.manager.model.data.HolidayData;
import kr.jeet.edu.manager.model.data.ScheduleData;
import kr.jeet.edu.manager.model.data.StudentGradeData;
import kr.jeet.edu.manager.model.response.ScheduleListResponse;
import kr.jeet.edu.manager.model.response.StudentGradeListResponse;
import kr.jeet.edu.manager.server.RetrofitClient;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.PreferenceUtil;
import kr.jeet.edu.manager.utils.Utils;
import kr.jeet.edu.manager.view.CustomAppbarLayout;
import kr.jeet.edu.manager.view.calendar.decorator.EventDecorator;
import kr.jeet.edu.manager.view.calendar.decorator.HighlightSaturdayDecorator;
import kr.jeet.edu.manager.view.calendar.decorator.HighlightSundayDecorator;
import kr.jeet.edu.manager.view.calendar.decorator.HolidayDecorator;
import kr.jeet.edu.manager.view.calendar.decorator.OtherMonthDecorator;
import kr.jeet.edu.manager.view.calendar.decorator.OtherSaturdayDecorator;
import kr.jeet.edu.manager.view.calendar.decorator.OtherSundayDecorator;
import kr.jeet.edu.manager.view.calendar.decorator.SelBackgroundDecorator;
import kr.jeet.edu.manager.view.calendar.decorator.SelectionDecorator;
import kr.jeet.edu.manager.view.calendar.decorator.TodayBackgroundDecorator;
import kr.jeet.edu.manager.view.calendar.formatter.CustomTitleFormatter;
import kr.jeet.edu.manager.view.calendar.formatter.CustomWeekDayFormatter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuScheduleActivity extends BaseActivity {
    private static final String TAG = "schedule";

    private static final int CMD_GET_ACA_LIST = 0;
    private static final int CMD_GET_GRADE_LIST = 1;
    private static final int CMD_GET_SCHEDULES = 2;
    private static final int CMD_DATE_CHANGED = 3;
    private TextView _tvListEmpty, _tvHolidayDate, _tvHoliday;
    private PowerSpinnerView _spinnerCampus, _spinnerGrade;
    private MaterialCalendarView _calendarView;
    private RecyclerView _recyclerSchedule;
    private ScheduleListAdapter _listAdapter;

    private ArrayList<ScheduleData> scheduleList = new ArrayList<>();
//    private ArrayList<ScheduleData> mListDay = new ArrayList<>();
    private Set<CalendarDay> calendarDaySet = new HashSet<>();
    private ArrayList<HolidayData> calHolidayList = new ArrayList<>();

    private SimpleDateFormat _holidayFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_M_D_E, Locale.KOREA);
    SimpleDateFormat _dateTransferFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYY_MM_DD);
    private Set<CalendarDay> calHoliday = new HashSet<>();
    List<ACAData> _ACAList = new ArrayList<>();
    List<StudentGradeData> _GradeList = new ArrayList<>();
    ACAData _selectedACA = null;
    private StudentGradeData _selectedGrade = null;

    EventDecorator eventDecorator = null;
    HolidayDecorator holidayDec = null;
    SelectionDecorator selectionDec= null;
    OtherMonthDecorator otherDec = null;
    OtherSundayDecorator otherSundayDec = null;
    OtherSaturdayDecorator otherSaturdayDec = null;
    Date _selectedDate = new Date();

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
                        _handler.sendEmptyMessage(CMD_GET_SCHEDULES);
                    }
                    break;
                case CMD_GET_SCHEDULES:
                    //todo request logic
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(_selectedDate);
                    int year = cal.get(Calendar.YEAR);
                    int month = cal.get(Calendar.MONTH) + 1;
                    requestScheduleList(year, month);
                    break;
                case CMD_DATE_CHANGED:
//                    _tvCalendar.setText(_dateFormat.format(_selectedDate));
                    break;
            }
        }
    };
    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        LogMgr.w("result =" + result);
        if(result.getResultCode() != RESULT_CANCELED) {
            Intent intent = result.getData();
            if(intent.hasExtra(IntentParams.PARAM_BOARD_ADDED)) {
                boolean added = intent.getBooleanExtra(IntentParams.PARAM_BOARD_ADDED, false);
                if(added) {
                    _handler.sendEmptyMessage(CMD_GET_SCHEDULES);
                }
            }
            else if(intent.hasExtra(IntentParams.PARAM_BOARD_DELETED)) {
                boolean deleted = intent.getBooleanExtra(IntentParams.PARAM_BOARD_DELETED, false);
                if(deleted) {
                    _handler.sendEmptyMessage(CMD_GET_SCHEDULES);
                }
            }else if(intent.hasExtra(IntentParams.PARAM_BOARD_EDITED)) {
                boolean edited = intent.getBooleanExtra(IntentParams.PARAM_BOARD_EDITED, false);
                if(edited) {
                    _handler.sendEmptyMessage(CMD_GET_SCHEDULES);
                }
            }
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_schedule);
        mContext = this;
        _acaCode = PreferenceUtil.getAcaCode(mContext);
        _appAcaCode = PreferenceUtil.getAppAcaCode(mContext);
        _userGubun = PreferenceUtil.getUserGubun(this);
        _seq = PreferenceUtil.getUserSeq(this);
        _sfCode = PreferenceUtil.getUserSFCode(this);
        initView();
        initAppbar();
        changeMessageState2Read();
        setAnimMove(Constants.MOVE_DOWN);
    }
    void changeMessageState2Read() {
        new Thread(() -> {
            List<PushMessage> pushMessages = JeetDatabase.getInstance(getApplicationContext()).pushMessageDao().getMessageByReadFlagNType(false, FCMManager.MSG_TYPE_ACA_SCHEDULE);
            if(!pushMessages.isEmpty()) {
                for(PushMessage message : pushMessages) {
                    message.isRead = true;
                    JeetDatabase.getInstance(getApplicationContext()).pushMessageDao().update(message);
                }
            }
        }).start();
    }
    @Override
    void initView() {
        _spinnerCampus = findViewById(R.id.spinner_schedule_campus);
        _spinnerGrade = findViewById(R.id.spinner_schedule_grade);
        _calendarView = findViewById(R.id.cv_schedule);
        _recyclerSchedule = findViewById(R.id.recycler_schedule);
        _tvListEmpty = findViewById(R.id.tv_schedule_empty_list);
        _tvHoliday = findViewById(R.id.tv_holiday);
        _tvHolidayDate = findViewById(R.id.tv_holiday_date);
        setRecycler();
        setSpinner();
        setCalendar();
        setTvHolidayDate();
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.main_menu_campus_schedule);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
                navigate2EditScheduleActivity();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void navigate2EditScheduleActivity() {
        Intent editIntent = new Intent(mContext, EditScheduleActivity.class);
        String dateStr = _dateTransferFormat.format(_selectedDate);
        editIntent.putExtra(IntentParams.PARAM_SELECTED_DATE, dateStr);
        if(_selectedACA != null) {
            editIntent.putExtra(IntentParams.PARAM_STU_ACACODE, _selectedACA.acaCode);
        }
        if(_selectedGrade != null) {
            editIntent.putExtra(IntentParams.PARAM_STU_GRADECODE, _selectedGrade.gubunCode);
        }
        resultLauncher.launch(editIntent);
//        startActivity(editIntent);
    }
    private void setSpinner(){
        {
            WrapContentSpinnerAdapter adapter = new WrapContentSpinnerAdapter(mContext, _ACAList.stream().map(t -> t.acaName).collect(Collectors.toList()), _spinnerCampus);
            _spinnerCampus.setSpinnerAdapter(adapter);
        }

        _spinnerCampus.setIsFocusable(true);
        _ACAList.add(new ACAData("", "전체", ""));
        _ACAList.addAll(DataManager.getInstance().getLocalACAListMap().values());
        List<String> acaNames = new ArrayList<>();
        for (ACAData data : _ACAList) { acaNames.add(data.acaName); }

//        _spinnerCampus.setItems(acaNames);
        Utils.updateSpinnerList(_spinnerCampus, acaNames);
        _spinnerCampus.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
            if(oldItem != null && oldItem.equals(newItem)) return;
            ACAData selectedData = null;
            Optional optional =  _ACAList.stream().filter(t -> t.acaName == newItem).findFirst();
            if(optional.isPresent()) {
                _selectedACA = (ACAData) optional.get();
//                _listAdapter.setACACode(_selectedACA.acaCode);
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
                    _handler.sendEmptyMessage(CMD_GET_SCHEDULES);
                }
                LogMgr.w(TAG, "_spinnerGrade == null" + (_spinnerGrade == null));
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
            if(oldItem != null && oldItem.equals(newItem)) return;
            ACAData selectedData = null;
            Optional optional =  _GradeList.stream().filter(t -> t.gubunName == newItem).findFirst();
            if(optional.isPresent()) {
                _selectedGrade = (StudentGradeData) optional.get();
//                _listAdapter.setACACode(_selectedACA.acaCode);
                LogMgr.w(TAG,"selectedACA = " + _selectedGrade.gubunCode + " / " + _selectedGrade.gubunName);
            }
//            Calendar calendar = Calendar.getInstance();
//            calendar.setTime(_selectedDate);
            Message msg = _handler.obtainMessage(CMD_GET_SCHEDULES);//
//            msg.arg1 = calendar.get(Calendar.YEAR);
//            msg.arg2 = calendar.get(Calendar.MONTH);
            _handler.sendMessage(msg);

        });
        _spinnerGrade.setSpinnerOutsideTouchListener(new OnSpinnerOutsideTouchListener() {
            @Override
            public void onSpinnerOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                _spinnerGrade.dismiss();
            }
        });
        _spinnerGrade.setLifecycleOwner(this);

    }
    private void setCalendar(){
        final int MIN_MONTH = 0;
        final int MAX_MONTH = 11;
        final int MIN_DAY = 1;
        final int MAX_DAY = 31;

        TodayBackgroundDecorator todayDec = new TodayBackgroundDecorator(mContext);
        HighlightSaturdayDecorator saturdayDec = new HighlightSaturdayDecorator(mContext);
        HighlightSundayDecorator sundayDec = new HighlightSundayDecorator(mContext);
        SelBackgroundDecorator bgDec = new SelBackgroundDecorator(mContext);
        otherDec = new OtherMonthDecorator(mContext);
        otherSundayDec = new OtherSundayDecorator(mContext);
        otherSaturdayDec = new OtherSaturdayDecorator(mContext);
        holidayDec = new HolidayDecorator(mContext, new HashSet<CalendarDay>(Collections.<CalendarDay>emptyList()));
        selectionDec = new SelectionDecorator(mContext);
        eventDecorator = new EventDecorator(mContext, new HashSet<CalendarDay>(Collections.<CalendarDay>emptyList()));
        CalendarDay today = CalendarDay.from(_selectedDate);
        todayDec.setSelectedDay(today);
        bgDec.setSelectedDay(today);
        otherDec.setSelectedDay(today);
        otherSundayDec.setSelectedDay(today);
        otherSaturdayDec.setSelectedDay(today);
        holidayDec.setSelectedDay(today);

        _calendarView.setDynamicHeightEnabled(true);
        _calendarView.setSelected(false);
        _calendarView.setWeekDayFormatter(new CustomWeekDayFormatter(mContext));
        _calendarView.addDecorators(eventDecorator, todayDec, saturdayDec, sundayDec, bgDec, otherDec, otherSundayDec, otherSaturdayDec, holidayDec, selectionDec);
        _calendarView.setTitleFormatter(new CustomTitleFormatter(mContext));
        _calendarView.state().edit()
                .setMinimumDate(CalendarDay.from(Constants.PICKER_MIN_YEAR, MIN_MONTH, MIN_DAY))
                .setMaximumDate(CalendarDay.from(Constants.PICKER_MAX_YEAR, MAX_MONTH, MAX_DAY))
                .commit();

        _calendarView.setOnTitleClickListener(v -> {
            int currentYear = _calendarView.getCurrentDate().getYear();
            int currentMonth = _calendarView.getCurrentDate().getMonth();

            Utils.yearMonthPicker(mContext, (month, year) -> {
                LogMgr.e(TAG, year+"년 "+month+"월");
                CalendarDay newDate = CalendarDay.from(year, month, 1);
                runOnUiThread( () -> _calendarView.setCurrentDate(newDate) );
                _selectedDate = newDate.getDate();

                LogMgr.e(TAG, "selDate = " + _selectedDate);
                _handler.sendEmptyMessage(CMD_GET_SCHEDULES);
            }, currentYear, currentMonth);
        });

        _calendarView.setOnDateChangedListener((view, date, selected) -> {
            LogMgr.e(TAG, "calendarView onDateChanged");
            _selectedDate = date.getDate();
            setTvHolidayDate();
            setDeco(date);
            view.invalidateDecorators();

            LogMgr.i(TAG,"DateTest >> " + _selectedDate);

//            new Thread(() -> {
//                for (ScheduleData item : scheduleList) if (selDay == item.day) mListDay.add(item);
//
//                runOnUiThread(() -> {
//                    _listAdapter.notifyDataSetChanged();
//                    _tvListEmpty.setVisibility(mListDay.isEmpty() ? View.VISIBLE : View.GONE);
//                });
//            }).start();
            if(_listAdapter != null) {
                String trigger = String.valueOf(date.getDay());
                LogMgr.e(TAG, "filter with " + trigger);
                _listAdapter.getFilter().filter(trigger);
            }
        });

        _calendarView.setOnMonthChangedListener((view, date) -> {
            LogMgr.e(TAG, "calendarView onMonthChanged");
            Calendar calendar = date.getCalendar();
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            _selectedDate = calendar.getTime();

            setDeco(date);
            view.invalidateDecorators();

            otherDec.setSelectedDay(date);
            otherSundayDec.setSelectedDay(date);
            otherSaturdayDec.setSelectedDay(date);
            holidayDec.setSelectedDay(date);

            LogMgr.i(TAG, "DateTestMonth >> " + _selectedDate);
            _handler.sendEmptyMessage(CMD_GET_SCHEDULES);
        });
    }
    private void setTvHolidayDate(){
        _tvHoliday.setText("");
        _tvHoliday.setVisibility(View.GONE);

        _tvHolidayDate.setText(_holidayFormat.format(_selectedDate));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(_selectedDate);

        for (HolidayData item : calHolidayList){
            if ((Integer.parseInt(item.month) == calendar.get(Calendar.MONTH) + 1) && Integer.parseInt(item.day) == calendar.get(Calendar.DATE)){
                _tvHoliday.setText(TextUtils.isEmpty(item.name) ? "" : item.name);
                _tvHoliday.setVisibility(View.VISIBLE);
                break;
            }
        }
    }

    private void setDeco(CalendarDay day){

        if (calendarDaySet != null && calendarDaySet.size() > 0){
                eventDecorator.setDates(calendarDaySet);
        }else{
            eventDecorator.setDates(Collections.emptySet());
        }
    }
    private void setRecycler(){

        _listAdapter = new ScheduleListAdapter(mContext, scheduleList, new ScheduleListAdapter.ItemClickListener() {
            @Override
            public void onItemClick(ScheduleData item) {
                startDetailActivity(item);
            }

            @Override
            public void onFilteringCompleted() {
                _tvListEmpty.setVisibility(_listAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }
        });
        _recyclerSchedule.setAdapter(_listAdapter);
        _recyclerSchedule.addItemDecoration(Utils.setDivider(mContext));
        _listAdapter.notifyDataSetChanged();
    }
    private void startDetailActivity(ScheduleData item){
        if (item != null){
            Intent targetIntent = new Intent(mContext, MenuScheduleDetailActivity.class);
            targetIntent.putExtra(IntentParams.PARAM_BOARD_SEQ, item.seq);
            resultLauncher.launch(targetIntent);
            overridePendingTransition(R.anim.horizontal_enter, R.anim.horizontal_out);
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
                            LogMgr.e(TAG, "requestACAList() errBody : " + response.errorBody().string());
                        } catch (IOException e) {
                        }

                    }

                }

                @Override
                public void onFailure(Call<StudentGradeListResponse> call, Throwable t) {
                    LogMgr.e(TAG, "requestACAList() onFailure >> " + t.getMessage());
//                    _handler.sendEmptyMessage(CMD_GET_GRADE_LIST);
                }
            });
        }
    }
    private void requestScheduleList(int year, int month) {
        if (RetrofitClient.getInstance() != null) {
            String acaCode = "";
            String acaGubunCode = "";
            if(_selectedACA != null) {
                acaCode = _selectedACA.acaCode;
            }
            if(_selectedGrade != null) {
                try{
                    acaGubunCode = String.valueOf(_selectedGrade.gubunCode);
                }catch(NumberFormatException ex){}
            }

            RetrofitClient.getApiInterface().getScheduleList(acaCode, acaGubunCode, year, month).enqueue(new Callback<ScheduleListResponse>() {
                @Override
                public void onResponse(Call<ScheduleListResponse> call, Response<ScheduleListResponse> response) {
                    if (scheduleList != null && scheduleList.size() > 0) scheduleList.clear();
//                    if (mListDay != null && mListDay.size() > 0) mListDay.clear();
//                    if (calendarDayList != null && calendarDayList.size() > 0) calendarDayList.clear();
                    if (calendarDaySet != null && calendarDaySet.size() > 0) calendarDaySet.clear();
                    if (calHolidayList != null && calHolidayList.size() > 0) calHolidayList.clear();

                    try {
                        if (response.isSuccessful()) {
                            List<ScheduleData> getData = new ArrayList<>();
                            List<HolidayData> getHoliday = new ArrayList<>();
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(_selectedDate);
                            int selYear = calendar.get(Calendar.YEAR);
                            int selMonth = calendar.get(Calendar.MONTH) + 1;
                            int selDay = calendar.get(Calendar.DATE);
                            if (response.body().data.scheduleList != null) {
                                getData = response.body().data.scheduleList;

                                if (!getData.isEmpty()) {
                                    scheduleList.addAll(getData);
//                                    for (int i=0; i < getData.size(); i++) if (selDay == getData.get(i).day) mListDay.add(getData.get(i));
                                    for (ScheduleData item : getData) calendarDaySet.add(CalendarDay.from(item.year, item.month-1, item.day));


                                }
                            }

                            if (response.body().data.holidayList != null){
                                getHoliday = response.body().data.holidayList;

                                if (!getHoliday.isEmpty()){

                                    calHolidayList.addAll(getHoliday);

                                    for (HolidayData item : getHoliday) {
                                        calHoliday.add(CalendarDay.from(selYear, Integer.parseInt(item.month) - 1, Integer.parseInt(item.day)));
                                    }

                                    holidayDec.setDates(calHoliday);
                                }
                            }

                        } else {
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestScheduleList() Exception: ", e.getMessage());
                    }
                    updateCalView();
                }

                @Override
                public void onFailure(Call<ScheduleListResponse> call, Throwable t) {
                    if (_listAdapter != null) _listAdapter.notifyDataSetChanged();
                    try {
                        LogMgr.e(TAG, "requestScheduleList() onFailure >> " + t.getMessage());
                    } catch (Exception e) {
                    }
                    _calendarView.invalidateDecorators();
//                    _tvListEmpty.setVisibility(mListDay.isEmpty() ? View.VISIBLE : View.GONE);
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();

                    updateCalView();
                }
            });
        }
    }

    private void updateCalView(){
        LogMgr.e(TAG, "updateCalView");
        CalendarDay firstDay = CalendarDay.from(_selectedDate);
        _calendarView.setSelectedDate(firstDay);
        if(_listAdapter != null) {
            String trigger = String.valueOf(firstDay.getDay());
            LogMgr.e(TAG, "filter with " + trigger);
            _listAdapter.getFilter().filter(trigger);
        }
        setDeco(firstDay);
        setTvHolidayDate();
        _calendarView.invalidateDecorators();
    }
}