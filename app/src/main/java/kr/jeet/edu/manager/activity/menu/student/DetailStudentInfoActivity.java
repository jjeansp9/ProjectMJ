package kr.jeet.edu.manager.activity.menu.student;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.skydoves.powerspinner.OnSpinnerOutsideTouchListener;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.activity.BaseActivity;
import kr.jeet.edu.manager.adapter.MonthlyAttendanceListAdapter;
import kr.jeet.edu.manager.adapter.WrapContentSpinnerAdapter;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.DataManager;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.model.data.AttendanceData;
import kr.jeet.edu.manager.model.data.AttendanceSummaryData;
import kr.jeet.edu.manager.model.data.HolidayData;
import kr.jeet.edu.manager.model.data.ManagerInfo;
import kr.jeet.edu.manager.model.data.RecipientStudentData;
import kr.jeet.edu.manager.model.data.StudentInfo;
import kr.jeet.edu.manager.model.data.TeacherClsData;
import kr.jeet.edu.manager.model.response.GetAttendanceInfoResponse;
import kr.jeet.edu.manager.model.response.GetParentNotificationInfoResponse;
import kr.jeet.edu.manager.model.response.GetStudentInfoResponse;
import kr.jeet.edu.manager.model.response.TeacherClsResponse;
import kr.jeet.edu.manager.server.RetrofitApi;
import kr.jeet.edu.manager.server.RetrofitClient;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.PreferenceUtil;
import kr.jeet.edu.manager.utils.Utils;
import kr.jeet.edu.manager.view.CustomAppbarLayout;
import kr.jeet.edu.manager.view.calendar.decorator.AttendanceDecorator;
import kr.jeet.edu.manager.view.calendar.decorator.HighlightSaturdayDecorator;
import kr.jeet.edu.manager.view.calendar.decorator.HighlightSundayDecorator;
import kr.jeet.edu.manager.view.calendar.decorator.HolidayDecorator;
import kr.jeet.edu.manager.view.calendar.decorator.OtherMonthDecorator;
import kr.jeet.edu.manager.view.calendar.decorator.OtherSaturdayDecorator;
import kr.jeet.edu.manager.view.calendar.decorator.OtherSundayDecorator;
import kr.jeet.edu.manager.view.calendar.decorator.SelectionDecorator;
import kr.jeet.edu.manager.view.calendar.decorator.TodayBackgroundDecorator;
import kr.jeet.edu.manager.view.calendar.formatter.CustomTitleFormatter;
import kr.jeet.edu.manager.view.calendar.formatter.CustomWeekDayFormatter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailStudentInfoActivity extends BaseActivity {
    private static final String TAG = "detail_studentinfo";
    private static final int CMD_GET_STU_INFO = 1;
    private static final int CMD_GET_PARENT_NOTIFICATION_INFO = 2;
    private static final int CMD_GET_CLASS_INFO = 3;
    private static final int CMD_GET_ATTENDANCE_INFO = 4;
    SimpleDateFormat _dateTransferFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYY_MM_DD);
    SimpleDateFormat _apiDateFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYYMM);

    private TextView mTvStuName, mTvStuBirth, mTvStuCampus, mTvStuPhoneNum, mTvParentPhoneNum, mTvDeptName; //, mTvStGrade, mTvClstName;
    private ImageView mImgStuProfile;
    private TextView tvPushStatusAnnouncement, tvPushStatusSeminar, tvPushStatusAttendance, tvPushStatusSystemAlarm;
    private TextView tvParentPushStatusAnnouncement, tvParentPushStatusSeminar, tvParentPushStatusAttendance, tvParentPushStatusSystemAlarm;
    private TextView tvInstall, tvParentInstall;
    private ConstraintLayout layoutConsultRequest, layoutAttendanceArea, layoutPushArea, layoutPushItems, layoutParentPushItems;
    private RetrofitApi mRetrofitApi;
    private PowerSpinnerView mSpinnerCls;
    private MaterialCalendarView _calendarView;
    private ChipGroup chipGroupLegend;  //범례
    private RecyclerView recyclerViewMonthlyAttend;
    private MonthlyAttendanceListAdapter _attendanceListAdapter;
    private int _stuSeq = 0;
    private int _userGubun = 0;
    private int _stCode = 0;
    private int _clsCode = 0;
    private boolean isContainClassInfo = false;
    private final String MAN = "M";
    private final String WOMAN = "F";

    ArrayList<TeacherClsData> _classList = new ArrayList<>();
    ArrayList<AttendanceData> _attendanceList = new ArrayList<>();
    //calendar
    private Set<AttendanceSummaryData> calendarDaySet = new HashSet<>();
    private ArrayList<HolidayData> calHolidayList = new ArrayList<>();
    private Set<CalendarDay> calHoliday = new HashSet<>();
//    EventDecorator eventDecorator = null;
//관리자 로그인의 경우 선택한 원생이 퇴원생일 때 memberInfo를 가져올 수 없으므로 최소한의 정보표현을 위해 RecipientStudentData를 넘겨줌
    RecipientStudentData _selectedStudentData = null;

    AttendanceDecorator attendDecorator = null;
    AttendanceDecorator absenceDecorator = null;
    AttendanceDecorator earlyLeaveDecorator = null;
    AttendanceDecorator tardyDecorator = null;
    AttendanceDecorator makeupClassDecorator = null;
    AttendanceDecorator onlineLectureDecorator = null;
    HolidayDecorator holidayDec = null;
    SelectionDecorator selectionDec= null;
    OtherMonthDecorator otherDec = null;
    OtherSundayDecorator otherSundayDec = null;
    OtherSaturdayDecorator otherSaturdayDec = null;
    private Date _selectedDate = new Date();
    private TeacherClsData _selectedClass = null;

    private Handler _handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch(msg.what) {
                case CMD_GET_STU_INFO:
                    requestMemberInfo(_stuSeq, _stCode);
                    break;
                case CMD_GET_PARENT_NOTIFICATION_INFO:
                    if(msg.obj != null) {
                        String phoneNo = msg.obj.toString();
                        if(!TextUtils.isEmpty(phoneNo)) {
                            requestParentPushInfo(phoneNo, Constants.USER_TYPE_PARENTS);
                        }else {
                            layoutParentPushItems.setVisibility(View.GONE);
                            tvParentInstall.setVisibility(View.VISIBLE);
                        }
                    }
                    if(isContainClassInfo) {
                        _handler.sendEmptyMessage(CMD_GET_ATTENDANCE_INFO);
                    }else{
                        _handler.sendEmptyMessage(CMD_GET_CLASS_INFO);
                    }
                    break;
                case CMD_GET_CLASS_INFO:
                    requestCls();
                    break;
                case CMD_GET_ATTENDANCE_INFO:
                    requestGetAttendanceList();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_student_info);
        mContext = this;
        _userGubun = PreferenceUtil.getUserGubun(mContext);
        initData();
        initAppbar();
        initView();
        if(!isContainClassInfo) {
            requestCls();
        }
        long delayed = getResources().getInteger(R.integer.screen_in_time);
        _handler.postDelayed(() -> _handler.sendEmptyMessage(CMD_GET_STU_INFO), delayed);

        setAnimMove(Constants.MOVE_DETAIL_RIGHT);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.main_menu_student_info);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    void initView() {
        mTvStuName = findViewById(R.id.tv_stu_info_name);
        mTvStuBirth = findViewById(R.id.tv_stu_info_birth);
        mTvStuCampus = findViewById(R.id.tv_stu_info_campus);
        mTvDeptName = findViewById(R.id.tv_stu_info_dept_name);
//        mTvStGrade = findViewById(R.id.tv_stu_info_st_grade);
//        mTvClstName = findViewById(R.id.tv_stu_info_clst_name);
        mTvStuPhoneNum = findViewById(R.id.tv_stu_info_stu_phone_num);
        mTvParentPhoneNum = findViewById(R.id.tv_stu_info_parent_phone_num);

        layoutConsultRequest = findViewById(R.id.layout_consultation_request);
        layoutPushArea = findViewById(R.id.layout_push_status);
        layoutAttendanceArea = findViewById(R.id.layout_attendance);

        mImgStuProfile = findViewById(R.id.img_stu_info_profile);

        if(_userGubun == Constants.USER_TYPE_ADMIN || _userGubun == Constants.USER_TYPE_SUPER_ADMIN) {
            layoutPushArea.setVisibility(View.GONE);
        }else{
            layoutPushArea.setVisibility(View.VISIBLE);
        }
        layoutPushItems = findViewById(R.id.layout_push_status_student_items);
        layoutParentPushItems = findViewById(R.id.layout_push_status_parent_items);

        tvInstall = findViewById(R.id.tv_student_install);
        tvParentInstall = findViewById(R.id.tv_parent_install);

        tvPushStatusAnnouncement = findViewById(R.id.sw_set_announcement_state);
        tvPushStatusSeminar = findViewById(R.id.sw_set_information_session_state);
        tvPushStatusAttendance = findViewById(R.id.sw_set_attendance_state);
        tvPushStatusSystemAlarm = findViewById(R.id.sw_set_system_state);

        tvParentPushStatusAnnouncement = findViewById(R.id.sw_parent_set_announcement_state);
        tvParentPushStatusSeminar = findViewById(R.id.sw_parent_set_information_session_state);
        tvParentPushStatusAttendance = findViewById(R.id.sw_parent_set_attendance_state);
        tvParentPushStatusSystemAlarm = findViewById(R.id.sw_parent_set_system_state);
        setCalendar();
        recyclerViewMonthlyAttend = findViewById(R.id.recyclerview_attendance);
        _attendanceListAdapter = new MonthlyAttendanceListAdapter(mContext, _attendanceList);
        recyclerViewMonthlyAttend.setAdapter(_attendanceListAdapter);

        mSpinnerCls = findViewById(R.id.spinner_cls);

        mSpinnerCls.setSpinnerOutsideTouchListener(new OnSpinnerOutsideTouchListener() {
            @Override
            public void onSpinnerOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                mSpinnerCls.dismiss();
            }
        });
        {
            WrapContentSpinnerAdapter adapter = new WrapContentSpinnerAdapter(mContext, _classList.stream().map(t -> t.clsName).collect(Collectors.toList()), mSpinnerCls);
            mSpinnerCls.setSpinnerAdapter(adapter);
        }

        mSpinnerCls.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
            Optional optional = _classList.stream().filter(t->String.valueOf(newItem).equals(t.clsName)).findFirst();
            if(optional.isPresent()) {
                _selectedClass = (TeacherClsData) optional.get();
                _clsCode = _selectedClass.clsCode;
                _handler.sendEmptyMessage(CMD_GET_ATTENDANCE_INFO);
            }
        });
        mSpinnerCls.setLifecycleOwner(this);
        LogMgr.w(TAG, "isContainClassInfo = " + isContainClassInfo);
        if(isContainClassInfo) {
            mSpinnerCls.setVisibility(View.GONE);
        }else{
            mSpinnerCls.setVisibility(View.VISIBLE);
        }
        initChipGroup();
    }
    void setCalendar() {
        final int MIN_MONTH = 0;
        final int MAX_MONTH = 11;
        final int MIN_DAY = 1;
        final int MAX_DAY = 31;
        //calendar
        _calendarView = findViewById(R.id.cv_attendance);

        TodayBackgroundDecorator todayDec = new TodayBackgroundDecorator(mContext);
        HighlightSaturdayDecorator saturdayDec = new HighlightSaturdayDecorator(mContext);
        HighlightSundayDecorator sundayDec = new HighlightSundayDecorator(mContext);
//        SelBackgroundDecorator bgDec = new SelBackgroundDecorator(mContext);
        otherDec = new OtherMonthDecorator(mContext);
        otherSundayDec = new OtherSundayDecorator(mContext);
        otherSaturdayDec = new OtherSaturdayDecorator(mContext);
        holidayDec = new HolidayDecorator(mContext, new HashSet<CalendarDay>(Collections.<CalendarDay>emptyList()));
        selectionDec = new SelectionDecorator(mContext);
//        eventDecorator = new EventDecorator(mContext, new HashSet<CalendarDay>(Collections.<CalendarDay>emptyList()));
        attendDecorator = new AttendanceDecorator(mContext, new HashSet<AttendanceSummaryData>(Collections.<AttendanceSummaryData>emptyList()), Constants.AttendanceStatus.ATTENDANCE);
        absenceDecorator = new AttendanceDecorator(mContext, new HashSet<AttendanceSummaryData>(Collections.<AttendanceSummaryData>emptyList()), Constants.AttendanceStatus.ABSENCE);
        earlyLeaveDecorator = new AttendanceDecorator(mContext, new HashSet<AttendanceSummaryData>(Collections.<AttendanceSummaryData>emptyList()), Constants.AttendanceStatus.EARLY_LEAVE);
        tardyDecorator = new AttendanceDecorator(mContext, new HashSet<AttendanceSummaryData>(Collections.<AttendanceSummaryData>emptyList()), Constants.AttendanceStatus.TARDY);
        makeupClassDecorator = new AttendanceDecorator(mContext, new HashSet<AttendanceSummaryData>(Collections.<AttendanceSummaryData>emptyList()), Constants.AttendanceStatus.MAKEUP_CLASS);
        onlineLectureDecorator = new AttendanceDecorator(mContext, new HashSet<AttendanceSummaryData>(Collections.<AttendanceSummaryData>emptyList()), Constants.AttendanceStatus.ONLINE_LECTURE);
        CalendarDay today = CalendarDay.from(_selectedDate);
        todayDec.setSelectedDay(today);
//        bgDec.setSelectedDay(today);
        otherDec.setSelectedDay(today);
        otherSundayDec.setSelectedDay(today);
        otherSaturdayDec.setSelectedDay(today);
        holidayDec.setSelectedDay(today);

        _calendarView.setDynamicHeightEnabled(true);
        _calendarView.setSelected(false);
        _calendarView.setWeekDayFormatter(new CustomWeekDayFormatter(mContext));
        _calendarView.addDecorators(todayDec, saturdayDec, sundayDec, otherDec, otherSundayDec, otherSaturdayDec, holidayDec, selectionDec, attendDecorator, absenceDecorator, earlyLeaveDecorator, tardyDecorator, makeupClassDecorator, onlineLectureDecorator);
        _calendarView.setTitleFormatter(new CustomTitleFormatter(mContext));
        LogMgr.e(TAG, "calendar setCurrentDate = " + _selectedDate);

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
                CalendarDay selectedCalendarDay = CalendarDay.from(_selectedDate);
                if(!newDate.isAfter(selectedCalendarDay) && !newDate.isBefore(selectedCalendarDay)){
                    return;
                }
                runOnUiThread( () -> _calendarView.setCurrentDate(newDate) );
                _selectedDate = newDate.getDate();

                LogMgr.e(TAG, "selDate = " + _selectedDate);
                if(isContainClassInfo) {
                    _handler.sendEmptyMessage(CMD_GET_ATTENDANCE_INFO);
                }else{
                    _handler.sendEmptyMessage(CMD_GET_CLASS_INFO);
                }
            }, currentYear, currentMonth);
        });
        _calendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_NONE);
//        _calendarView.setOnDateChangedListener((view, date, selected) -> {
//            LogMgr.e(TAG, "calendarView onDateChanged");
//            _selectedDate = date.getDate();
////            setTvHolidayDate();
//            setDeco(date);
//            view.invalidateDecorators();
//
//            LogMgr.i(TAG,"DateTest >> " + _selectedDate);
//
////            new Thread(() -> {
////                for (ScheduleData item : scheduleList) if (selDay == item.day) mListDay.add(item);
////
////                runOnUiThread(() -> {
////                    _listAdapter.notifyDataSetChanged();
////                    _tvListEmpty.setVisibility(mListDay.isEmpty() ? View.VISIBLE : View.GONE);
////                });
////            }).start();
////            if(_listAdapter != null) {
////                String trigger = String.valueOf(date.getDay());
////                LogMgr.e(TAG, "filter with " + trigger);
////                _listAdapter.getFilter().filter(trigger);
////            }
//        });

        _calendarView.setOnMonthChangedListener((view, date) -> {
            LogMgr.e(TAG, "calendarView onMonthChanged");
            Calendar calendar = date.getCalendar();
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 12);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Date selectedTime = calendar.getTime();
            if(_selectedDate.compareTo(selectedTime) == 0) return;
            _selectedDate = calendar.getTime();

//            setTvHolidayDate();

            otherDec.setSelectedDay(date);
            otherSundayDec.setSelectedDay(date);
            otherSaturdayDec.setSelectedDay(date);
            holidayDec.setSelectedDay(date);
            setDeco();
            view.invalidateDecorators();
            LogMgr.i(TAG, "DateTestMonth >> " + _selectedDate);
            if(isContainClassInfo) {
                _handler.sendEmptyMessage(CMD_GET_ATTENDANCE_INFO);
            }else{
                _handler.sendEmptyMessage(CMD_GET_CLASS_INFO);
            }
        });
        _calendarView.setCurrentDate(today);
    }
    private void initChipGroup() {
        chipGroupLegend = findViewById(R.id.chipgroup_legend);
        chipGroupLegend.removeAllViews();
        int paddingDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
        chipGroupLegend.setPadding(paddingDp,paddingDp,paddingDp,paddingDp);
        for(int i = 0 ; i < 9; i ++ ){
            Constants.AttendanceStatus attendance = Constants.AttendanceStatus.getByCode(i);
            if(attendance == null) break;
            Chip chip = new Chip(DetailStudentInfoActivity.this);
            chip.setText(attendance.getName());
            chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10F);
            chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.bg_white)));
            chip.setChipStrokeWidth(0);
            chip.setCloseIconVisible(false);
            chip.setCheckable(false);
            chip.setCheckedIconVisible(true);
            chip.setClickable(false);
            chip.setFocusable(false);
            chip.setTextStartPadding(4f);
            chip.setTextEndPadding(4f);
            chip.setChipIcon(getDrawable(R.drawable.ic_vector_circle));
            chip.setChipIconTint(ColorStateList.valueOf(ContextCompat.getColor(mContext, attendance.getColorRes())));
//            chip.setEnsureMinTouchTargetSize(false);
            chip.setChipIconSize(getResources().getDimensionPixelSize(R.dimen.dot_sex_size));
            chipGroupLegend.addView(chip);
        }

    }

    private void setDeco(){
        //test
        ////
//        _calendarView.removeDecorator(attendDecorator);
//        _calendarView.removeDecorator(absenceDecorator);
//        _calendarView.removeDecorator(earlyLeaveDecorator);
//        _calendarView.removeDecorator(tardyDecorator);
//        _calendarView.removeDecorator(makeupClassDecorator);
//        _calendarView.removeDecorator(onlineLectureDecorator);
//        _calendarView.addDecorators(attendDecorator, absenceDecorator, earlyLeaveDecorator, tardyDecorator, makeupClassDecorator, onlineLectureDecorator);
        if (calendarDaySet != null && calendarDaySet.size() > 0){

            attendDecorator.setDates(calendarDaySet.stream().filter(t->t.attendGubun == Constants.AttendanceStatus.ATTENDANCE.getCode()).collect(Collectors.toSet()));
            absenceDecorator.setDates(calendarDaySet.stream().filter(t->t.attendGubun == Constants.AttendanceStatus.ABSENCE.getCode()).collect(Collectors.toSet()));
            earlyLeaveDecorator.setDates(calendarDaySet.stream().filter(t->t.attendGubun == Constants.AttendanceStatus.EARLY_LEAVE.getCode()).collect(Collectors.toSet()));
            tardyDecorator.setDates(calendarDaySet.stream().filter(t->t.attendGubun == Constants.AttendanceStatus.TARDY.getCode()).collect(Collectors.toSet()));
            makeupClassDecorator.setDates(calendarDaySet.stream().filter(t->t.attendGubun == Constants.AttendanceStatus.MAKEUP_CLASS.getCode()).collect(Collectors.toSet()));
            onlineLectureDecorator.setDates(calendarDaySet.stream().filter(t->t.attendGubun == Constants.AttendanceStatus.ONLINE_LECTURE.getCode()).collect(Collectors.toSet()));
        }else{
            attendDecorator.setDates(Collections.emptySet());
            absenceDecorator.setDates(Collections.emptySet());
            earlyLeaveDecorator.setDates(Collections.emptySet());
            tardyDecorator.setDates(Collections.emptySet());
            makeupClassDecorator.setDates(Collections.emptySet());
            onlineLectureDecorator.setDates(Collections.emptySet());
        }
    }
    void initData() {
        Intent intent = getIntent();
        if(intent != null) {
            if(intent.hasExtra(IntentParams.PARAM_STU_SEQ)){
                _stuSeq = intent.getIntExtra(IntentParams.PARAM_STU_SEQ, 0);
            }
            if(intent.hasExtra(IntentParams.PARAM_STU_STCODE)){
                _stCode = intent.getIntExtra(IntentParams.PARAM_STU_STCODE, -1);
            }
            if(intent.hasExtra(IntentParams.PARAM_SELECTED_DATE)) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                String dateStr = intent.getStringExtra(IntentParams.PARAM_SELECTED_DATE);
                try {
                    _selectedDate = _dateTransferFormat.parse(dateStr);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            if(intent.hasExtra(IntentParams.PARAM_STU_CLASSCODE)) {
                isContainClassInfo = true;
                _clsCode = intent.getIntExtra(IntentParams.PARAM_STU_CLASSCODE, 0);
            }else{
                isContainClassInfo = false;
            }
            if(intent.hasExtra(IntentParams.PARAM_BOARD_ITEM)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    _selectedStudentData = intent.getParcelableExtra(IntentParams.PARAM_BOARD_ITEM, RecipientStudentData.class);
                }else{
                    _selectedStudentData = intent.getParcelableExtra(IntentParams.PARAM_BOARD_ITEM);
                }
            }
            LogMgr.e(TAG, "selectedDate = " + _selectedDate);
            LogMgr.e(TAG, "isContainClassInfo = " + isContainClassInfo);
        }
    }

    // 부모 push 정보 조회
    private void requestParentPushInfo(String phoneNumber, int userGubun){
        if(RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.getParentNotificationInfo(phoneNumber, userGubun).enqueue(new Callback<GetParentNotificationInfoResponse>() {
                @Override
                public void onResponse(Call<GetParentNotificationInfoResponse> call, Response<GetParentNotificationInfoResponse> response) {
                    try {
                        if (response.isSuccessful()){
                            ManagerInfo.PushStatus getData = new ManagerInfo.PushStatus();
                            if (response.body() != null)  getData = response.body().data;

                            if (userGubun == Constants.USER_TYPE_STUDENT){
                                layoutPushItems.setVisibility(View.VISIBLE);
                                tvInstall.setVisibility(View.INVISIBLE);

                                setPushStatusView(getData, tvPushStatusAnnouncement, tvPushStatusSeminar, tvPushStatusAttendance,
                                        tvPushStatusSystemAlarm, layoutPushItems, tvInstall);
                            }
                            if (userGubun == Constants.USER_TYPE_PARENTS){
                                layoutParentPushItems.setVisibility(View.VISIBLE);
                                tvParentInstall.setVisibility(View.INVISIBLE);

                                setPushStatusView(getData, tvParentPushStatusAnnouncement, tvParentPushStatusSeminar, tvParentPushStatusAttendance,
                                        tvParentPushStatusSystemAlarm, layoutParentPushItems, tvParentInstall);
                            }

                        }else{
                            layoutParentPushItems.setVisibility(View.GONE);
                            tvParentInstall.setVisibility(View.VISIBLE);
                        }

                    }catch (Exception e){ LogMgr.e(TAG + "requestMemberInfo() Exception : ", e.getMessage()); }

                }

                @Override
                public void onFailure(Call<GetParentNotificationInfoResponse> call, Throwable t) {
                    try { LogMgr.e(TAG, "requestMemberInfo() onFailure >> " + t.getMessage()); }
                    catch (Exception e) { LogMgr.e(TAG + "requestMemberInfo() Exception : ", e.getMessage()); }

                    layoutParentPushItems.setVisibility(View.GONE);
                    tvParentInstall.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void setPushStatusView(
            ManagerInfo.PushStatus getData, TextView tvAnnouncement,
            TextView tvSeminar, TextView tvAttendance, TextView tvSystem,
            ConstraintLayout layoutPush, TextView tvInstall
    ){
        if (getData != null) {

            //공지사항
            if(getData.pushNotice.equals("Y")) {
                tvAnnouncement.setText(getString(R.string.value_on));
                tvAnnouncement.setTextColor(getColor(R.color.color_value_on));
            }else{
                tvAnnouncement.setText(getString(R.string.value_off));
                tvAnnouncement.setTextColor(getColor(R.color.color_value_off));
            }
            //설명회
            if(getData.pushInformationSession.equals("Y")) {
                tvSeminar.setText(getString(R.string.value_on));
                tvSeminar.setTextColor(getColor(R.color.color_value_on));
            }else{
                tvSeminar.setText(getString(R.string.value_off));
                tvSeminar.setTextColor(getColor(R.color.color_value_off));
            }
            //출석
            if(getData.pushAttendance.equals("Y")) {
                tvAttendance.setText(getString(R.string.value_on));
                tvAttendance.setTextColor(getColor(R.color.color_value_on));
            }else{
                tvAttendance.setText(getString(R.string.value_off));
                tvAttendance.setTextColor(getColor(R.color.color_value_off));
            }
            //시스템알림
            if(getData.pushSystem.equals("Y")) {
                tvSystem.setText(getString(R.string.value_on));
                tvSystem.setTextColor(getColor(R.color.color_value_on));
            }else{
                tvSystem.setText(getString(R.string.value_off));
                tvSystem.setTextColor(getColor(R.color.color_value_off));
            }
        }else{
            layoutPush.setVisibility(View.GONE);
            tvInstall.setVisibility(View.VISIBLE);
        }
    }

    private void setSpinnerClass(){
        LogMgr.e(TAG, "setSpinnerTeacher()");
        try {
            if(_classList != null && _classList.size() > 0) {
                if(mSpinnerCls.getVisibility() != View.VISIBLE) {
                    Toast.makeText(mContext, R.string.msg_changed_class, Toast.LENGTH_SHORT).show();
                    mSpinnerCls.setVisibility(View.VISIBLE);
                }
            }else{
                mSpinnerCls.setVisibility(View.GONE);
                mSpinnerCls.clearSelectedItem();
                if(_selectedClass != null) {
                    Toast.makeText(mContext, R.string.msg_empty_class, Toast.LENGTH_SHORT).show();
                    _selectedClass = null;
                    _clsCode = 0;
                }
                if(_attendanceList != null) {
                    _attendanceList.clear();
                    _attendanceListAdapter.notifyDataSetChanged();
                }

//                if(!isContainClassInfo) {   //클래스정보가 없는 경우 출석을 표시할 수 없으므로 gone 처리함
//                    layoutAttendanceArea.setVisibility(View.GONE);
//                }

                return;
            }
//            List<String> sfNames = new ArrayList<>();

//            for (TeacherClsData data : _classList) sfNames.add(data.clsName);
            Utils.updateSpinnerList(mSpinnerCls, _classList.stream().map(t->t.clsName).collect(Collectors.toList()));


            if(_selectedClass != null){
                Optional optional = _classList.stream().filter(t -> t.clsName.equals(_selectedClass.clsName)).findFirst();
                if (optional.isPresent()) {
                    _selectedClass = (TeacherClsData) optional.get();
                    try {
                        int index = _classList.indexOf(_selectedClass);
                        mSpinnerCls.selectItemByIndex(index);
                    }catch(Exception ex) {
                        Toast.makeText(mContext, R.string.msg_changed_class, Toast.LENGTH_SHORT).show();
                        mSpinnerCls.selectItemByIndex(0);
                    }
                } else {
                    Toast.makeText(mContext, R.string.msg_changed_class, Toast.LENGTH_SHORT).show();
                    mSpinnerCls.selectItemByIndex(0);
                }
            }else{
                mSpinnerCls.selectItemByIndex(0);
            }
        }catch (Exception e){}
    }
    // 원생 정보 조회
    private void requestMemberInfo(int stuSeq, int stCode){
        if(RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.getStudentInfo(stuSeq, stCode).enqueue(new Callback<GetStudentInfoResponse>() {
                @Override
                public void onResponse(Call<GetStudentInfoResponse> call, Response<GetStudentInfoResponse> response) {
                    String phoneNo = "";
                    try {
                        if (response.isSuccessful()){
                            StudentInfo getData = new StudentInfo();
                            if (response.body() != null)  getData= response.body().data;

                            if (getData != null) {
                                if (TextUtils.isEmpty(getData.name)){ // 이름이 없다면 자녀선택화면의 이름 사용
                                    if(_selectedStudentData != null) {
                                        mTvStuName.setText(_selectedStudentData.stName);
                                    }
                                }else {
                                    mTvStuName.setText(getData.name); // 원생 오리지널 이름
                                }
                                SimpleDateFormat inputDateFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYY_MM_DD);
                                SimpleDateFormat outputDateFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYMMDD);
                                try {
                                    Date date = inputDateFormat.parse(getData.birth);
                                    String output = outputDateFormat.format(date);
                                    mTvStuBirth.setText(output);
                                }catch(Exception ex){}
                                if (!TextUtils.isEmpty(getData.acaName)) { // 캠퍼스명
//                                    PreferenceUtil.setAcaName(mContext, getData.acaName);
                                    mTvStuCampus.setText(getData.acaName);
                                    mTvStuCampus.setVisibility(View.VISIBLE);
                                }else if(!TextUtils.isEmpty(getData.acaCode)){
                                    mTvStuCampus.setText(DataManager.getInstance().getACAData(getData.acaCode).acaName);
                                    mTvStuCampus.setVisibility(View.VISIBLE);
                                }else{
                                    if(_selectedStudentData != null) {
                                        mTvStuCampus.setText(DataManager.getInstance().getACAData(_selectedStudentData.acaCode).acaName);
                                        mTvStuCampus.setVisibility(View.VISIBLE);
                                    }
                                }
                                if(getData.gender != null) {
                                    if (getData.gender.equals(MAN))
                                        mImgStuProfile.setImageResource(R.drawable.img_profile_man);
                                    else if (getData.gender.equals(WOMAN))
                                        mImgStuProfile.setImageResource(R.drawable.img_profile_woman);
                                    else
                                        mImgStuProfile.setImageResource(R.drawable.img_student);

                                    }
                                else {
                                    mImgStuProfile.setImageResource(R.drawable.img_student);
                                }

//                                mTvDeptName.setText(getData.deptName); // 부서
//                                mTvStGrade.setText(getData.stGrade); // 학년
//                                mTvClstName.setText(getData.clstName); // 학년 동일내용이라 삭제
                                StringBuilder sb = new StringBuilder();
                                if(TextUtils.isEmpty(getData.deptName)) {
                                    if(TextUtils.isEmpty(getData.stGrade)){

                                    }else{
                                        sb.append(getData.stGrade);
                                    }
                                }else {
                                    if(TextUtils.isEmpty(getData.stGrade)){
                                        sb.append(getData.deptName);
                                    }else{
                                        sb.append(getData.deptName + " / " + getData.stGrade);
                                    }
                                }
                                mTvDeptName.setText(sb.toString()); // 부서
//                                if (getData.stGrade.equals("")) mTvStGrade.setVisibility(View.GONE);
//                                if (TextUtils.isEmpty(getData.clstName)) mTvClstName.setVisibility(View.GONE);
                                String formattedPhoneNumber = "";
                                if (TextUtils.isEmpty(getData.phoneNumber)){
                                    if(_selectedStudentData != null) {
                                        formattedPhoneNumber = Utils.formatPhoneNumber(_selectedStudentData.stPhoneNumber);
                                    }
                                }else {
                                    formattedPhoneNumber = Utils.formatPhoneNumber(getData.phoneNumber);
                                }
                                LogMgr.w("format phone number " + formattedPhoneNumber);
                                if(!TextUtils.isEmpty(formattedPhoneNumber)) {
                                    mTvStuPhoneNum.setText(formattedPhoneNumber);
                                }
                                formattedPhoneNumber = "";
                                if (!Utils.checkPhoneNumber(getData.parentPhoneNumber)){
                                    if(_selectedStudentData != null) {
                                        formattedPhoneNumber = Utils.formatPhoneNumber(_selectedStudentData.parentPhoneNumber);
                                    }
                                }else {
                                    formattedPhoneNumber = Utils.formatPhoneNumber(getData.parentPhoneNumber);
                                }
                                LogMgr.w("format phone number " + formattedPhoneNumber);
                                if(!TextUtils.isEmpty(formattedPhoneNumber)) {
                                    mTvParentPhoneNum.setText(formattedPhoneNumber);
                                    phoneNo = formattedPhoneNumber;
                                }
                                if(_userGubun == Constants.USER_TYPE_TEACHER) {

                                    if (getData.pushStatus != null) {
                                        LogMgr.w("getData.pushStatus is not null");
                                        if (getData.pushStatus.seq == 0) {
                                            LogMgr.w("getData.pushStatus is empty");
                                            layoutPushItems.setVisibility(View.GONE);
                                            tvInstall.setVisibility(View.VISIBLE);
                                        } else {
                                            layoutPushItems.setVisibility(View.VISIBLE);
                                            tvInstall.setVisibility(View.INVISIBLE);
                                            //공지사항
                                            if (getData.pushStatus.pushNotice.equals("Y")) {
                                                tvPushStatusAnnouncement.setText(getString(R.string.value_on));
                                                tvPushStatusAnnouncement.setTextColor(getColor(R.color.color_value_on));
                                            } else {
                                                tvPushStatusAnnouncement.setText(getString(R.string.value_off));
                                                tvPushStatusAnnouncement.setTextColor(getColor(R.color.color_value_off));
                                            }
                                            //설명회
                                            if (getData.pushStatus.pushInformationSession.equals("Y")) {
                                                tvPushStatusSeminar.setText(getString(R.string.value_on));
                                                tvPushStatusSeminar.setTextColor(getColor(R.color.color_value_on));
                                            } else {
                                                tvPushStatusSeminar.setText(getString(R.string.value_off));
                                                tvPushStatusSeminar.setTextColor(getColor(R.color.color_value_off));
                                            }
                                            //출석
                                            if (getData.pushStatus.pushAttendance.equals("Y")) {
                                                tvPushStatusAttendance.setText(getString(R.string.value_on));
                                                tvPushStatusAttendance.setTextColor(getColor(R.color.color_value_on));
                                            } else {
                                                tvPushStatusAttendance.setText(getString(R.string.value_off));
                                                tvPushStatusAttendance.setTextColor(getColor(R.color.color_value_off));
                                            }
                                            //시스템알림
                                            if (getData.pushStatus.pushSystem.equals("Y")) {
                                                tvPushStatusSystemAlarm.setText(getString(R.string.value_on));
                                                tvPushStatusSystemAlarm.setTextColor(getColor(R.color.color_value_on));
                                            } else {
                                                tvPushStatusSystemAlarm.setText(getString(R.string.value_off));
                                                tvPushStatusSystemAlarm.setTextColor(getColor(R.color.color_value_off));
                                            }
                                        }
                                    } else {
                                        LogMgr.w("getData.pushStatus is null");
                                        layoutPushItems.setVisibility(View.GONE);
                                        tvInstall.setVisibility(View.VISIBLE);
                                    }
                                }
                            }else{
                                LogMgr.w(TAG, "getdata is null");
                            }

                        }else{
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                            LogMgr.e(TAG, "requestMemberInfo() errBody : " + response.errorBody().string());
                        }

                    }catch (Exception e){
                        LogMgr.e(TAG + "requestMemberInfo() Exception : ", e.getMessage());
                    }finally{
                        String cleanedNumber = phoneNo.replaceAll("[^0-9]", "");
                        Message message = _handler.obtainMessage(CMD_GET_PARENT_NOTIFICATION_INFO);
                        message.obj = cleanedNumber;
                        _handler.sendMessage(message);
                    }

                    animateLayout(layoutConsultRequest);
                    Utils.animateLayoutMoveLeft(layoutConsultRequest, mContext);
                }

                @Override
                public void onFailure(Call<GetStudentInfoResponse> call, Throwable t) {
                    try { LogMgr.e(TAG, "requestMemberInfo() onFailure >> " + t.getMessage()); }
                    catch (Exception e) { LogMgr.e(TAG + "requestMemberInfo() Exception : ", e.getMessage()); }
                    finally {
                        Message message = _handler.obtainMessage(CMD_GET_PARENT_NOTIFICATION_INFO);
                        message.obj = "";
                        _handler.sendMessage(message);
                    }
                    animateLayout(layoutConsultRequest);
                    Utils.animateLayoutMoveLeft(layoutConsultRequest, mContext);
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void requestGetAttendanceList(){
        if(_clsCode == 0) return;
        String date = _apiDateFormat.format(_selectedDate);

        if (RetrofitClient.getInstance() != null){

            RetrofitClient.getApiInterface().getMonthlyAttendanceInfo(date, _clsCode, _stCode).enqueue(new Callback<GetAttendanceInfoResponse>() {
                @Override
                public void onResponse(Call<GetAttendanceInfoResponse> call, Response<GetAttendanceInfoResponse> response) {
                    try {
                        if(_attendanceList != null && _attendanceList.size() > 0) {
                            _attendanceList.clear();
                        }
                        if (response.isSuccessful()){
                            List<AttendanceData> getData = null;
                            if (response.body() != null) {

                                getData = response.body().data;
                                _attendanceList.addAll(getData.stream().filter(t->t.attendGubun!= Constants.AttendanceStatus.UNSET.getCode()).collect(Collectors.toList()));
                            }
                        }else{
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e){
                        LogMgr.e(TAG + "requestBoardList() Exception : ", e.getMessage());
                    }finally{
//                        hideProgressDialog();
                        _attendanceListAdapter.notifyDataSetChanged();
                        updateCalView();
                    }
                }

                @Override
                public void onFailure(Call<GetAttendanceInfoResponse> call, Throwable t) {
                    try {
                        if(_attendanceList != null && _attendanceList.size() > 0) {
                            _attendanceList.clear();
                        }
                        Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                        LogMgr.e(TAG, "requestBoardList() onFailure >> " + t.getMessage());
                    }catch (Exception e){
                    }finally{
//                        hideProgressDialog();
                        _attendanceListAdapter.notifyDataSetChanged();
                        updateCalView();
                    }

                }
            });
        }
    }
    // 원생 학급 정보 조회
    private void requestCls(){
        if(RetrofitClient.getInstance() != null) {
            String dateStr = _apiDateFormat.format(_selectedDate);
            RetrofitClient.getApiInterface().requestTeacherCls(_stCode, dateStr).enqueue(new Callback<TeacherClsResponse>() {
                @Override
                public void onResponse(Call<TeacherClsResponse> call, Response<TeacherClsResponse> response) {
                    try {
                        if (response.isSuccessful() && response.body() != null){
                            if(_classList != null) _classList.clear();
                            _classList.addAll(response.body().data);
                            setSpinnerClass();
                        }else{
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                            LogMgr.e(TAG, "requestCls() errBody : " + response.errorBody().string());
                        }

                    }catch (Exception e){ LogMgr.e(TAG + "requestCls() Exception : ", e.getMessage()); }

                }

                @Override
                public void onFailure(Call<TeacherClsResponse> call, Throwable t) {
                    try { LogMgr.e(TAG, "requestCls() onFailure >> " + t.getMessage()); }
                    catch (Exception e) { LogMgr.e(TAG + "requestCls() Exception : ", e.getMessage()); }

                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
//                    _handler.sendEmptyMessage(CMD_GET_ATTENDANCE_INFO);
                }
            });
        }
    }
    private void updateCalView(){
        LogMgr.e(TAG, "updateCalView");
        if(calendarDaySet != null) calendarDaySet.clear();
        for(AttendanceData item : _attendanceList) {
            AttendanceSummaryData summaryData = new AttendanceSummaryData();
            summaryData.attendGubun = item.attendGubun;
            try {
                Date attendDate = _dateTransferFormat.parse(item.attendDate);
                summaryData.attendDate = CalendarDay.from(attendDate);
                LogMgr.w(TAG, "date = " + summaryData + " / " + item.attendGubun);
                calendarDaySet.add(summaryData);
            }catch(Exception ex) {
                ex.printStackTrace();
                continue;
            }
        }
        setDeco();
        _calendarView.invalidateDecorators();
    }

    private void animateLayout(final View view) {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(Constants.LAYOUT_ANIM_DURATION);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());

        animator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            view.setAlpha(progress); // 애니메이션 중간값을 알파값으로 설정하여 서서히 보이도록 함
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // 애니메이션 종료 후 다음 레이아웃으로 전환
                if (view == layoutConsultRequest) {
                    if (layoutPushArea.getVisibility() == View.VISIBLE) {
                        animateLayout(layoutPushArea);
                        Utils.animateLayoutMoveLeft(layoutPushArea, mContext);
                    } else {
                        animateLayout(layoutAttendanceArea);
                        Utils.animateLayoutMoveLeft(layoutAttendanceArea, mContext);
                    }
                }
                if (view == layoutPushArea) {
                    animateLayout(layoutAttendanceArea);
                    Utils.animateLayoutMoveLeft(layoutAttendanceArea, mContext);
                }
            }
        });
        animator.start();
    }
}