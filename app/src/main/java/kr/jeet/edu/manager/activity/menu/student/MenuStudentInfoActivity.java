package kr.jeet.edu.manager.activity.menu.student;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.InputType;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;
import com.skydoves.powerspinner.OnSpinnerOutsideTouchListener;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.activity.BaseActivity;
import kr.jeet.edu.manager.adapter.ColoredSpinnerAdapter;
import kr.jeet.edu.manager.adapter.StudentInfoListAdapter;
import kr.jeet.edu.manager.adapter.WrapContentSpinnerAdapter;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.DataManager;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.dialog.DatePickerFragment;
import kr.jeet.edu.manager.dialog.PopupDialog;
import kr.jeet.edu.manager.model.data.AttendanceData;
import kr.jeet.edu.manager.model.data.ClassData;
import kr.jeet.edu.manager.model.request.AttendanceRequest;
import kr.jeet.edu.manager.model.response.BaseResponse;
import kr.jeet.edu.manager.model.response.GetAttendanceInfoResponse;
import kr.jeet.edu.manager.model.response.GetManagerClassInfoResponse;
import kr.jeet.edu.manager.server.RetrofitApi;
import kr.jeet.edu.manager.server.RetrofitClient;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.PreferenceUtil;
import kr.jeet.edu.manager.utils.Utils;
import kr.jeet.edu.manager.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuStudentInfoActivity extends BaseActivity implements DatePickerFragment.OnDateSetListener{
    private static final String TAG = "studentInfo";

    private static final int CMD_GET_CLASS_LIST = 0;
    private static final int CMD_GET_ATTENDANCE_LIST = 1;
    private static final int CMD_UPDATE_ATTENDANCE = 2;
    private static final int CMD_UPDATE_ATTENDANCE_RESULT = 3;
    private static final int CMD_INIT_LAYOUT = 9;
    SimpleDateFormat _dateFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYY_MM_DD_KOR);
    SimpleDateFormat _dateTransferFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYY_MM_DD);

    private TextView _tvCalendar;
    private TextView _tvDepartment, _tvClass;
    private View _viewDivider;
    private Button _btnAttendance;  //출석체크
    private Button _btnCalendarPrev, _btnCalendarNext;
    private RecyclerView _recyclerView;
    private StudentInfoListAdapter _adapter;
    private RetrofitApi _retrofitApi;
    private PowerSpinnerView _spinnerClass;
    private SwipeRefreshLayout _swipeRefresh;
    private ConstraintLayout _layoutCalendar;
    private ConstraintLayout _layoutControlAll;
    private MaterialCheckBox _checkBoxAll;
    private PowerSpinnerView _spinnerAttendanceMultiple;
    private TextView _tvCount, _tvTotalCount;
    private TextView tvEmptyList;

    ArrayList<AttendanceData> _originAttendanceList;
    ArrayList<AttendanceData> _attendanceList = new ArrayList<>();
    ArrayList<AttendanceData> _updateList = new ArrayList<>();
    List<ClassData> _classDataList = new ArrayList<>();
    List<String> _classNameList = new ArrayList<>();
    Date _selectedDate = new Date();
//    int _selectedClassCode = 0;
    ClassData _selectedClassData = null;
    Constants.AttendanceStatus _selectMultipleAttendanceStatus = Constants.AttendanceStatus.ATTENDANCE;

    int _userGubun = 1;
    int _seq = 0;
    int _sfCode = 0;
    private String _acaCode = "";
    private String _acaName = "";
    String _userType = "";

    Menu _menu;
    Constants.BoardEditMode _boardMode = Constants.BoardEditMode.Show;

    private Handler _handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch(msg.what) {
                case CMD_GET_CLASS_LIST: {
                    int sfCode = msg.arg1;
                    requestGetClassList(sfCode);
                }
                break;
                case CMD_GET_ATTENDANCE_LIST: {
//                    int clsCode = msg.arg1;
                    int clsCode = _selectedClassData.clsCode;
                    if (msg.obj != null) {
                        requestGetAttendanceList(clsCode, (String) msg.obj);
                    }else {
                        requestGetAttendanceList(clsCode, null);
                    }
                }
                break;
                case CMD_UPDATE_ATTENDANCE: {
                    if(msg != null) {
                        if(msg.arg1 < _updateList.size()) {
                            LogMgr.e(TAG, "msg.arg1 = " + msg.arg1);
                            boolean isPushUpdate = (boolean) msg.obj;
                            int clsCode = _selectedClassData.clsCode;
                            requestUpdateAttendanceList(msg.arg1, clsCode, isPushUpdate);
                        }
                    }else{
                        LogMgr.e(TAG, "msg obj is null");
                    }
                }
                    break;
                case CMD_UPDATE_ATTENDANCE_RESULT: {
                    hideProgressDialog();
                    ArrayList<AttendanceData> failList = (ArrayList<AttendanceData>) _updateList.stream().filter(t->t.updateResult != 200 && t.updateResult != 201).collect(Collectors.toList());

                    if(failList.size()  == _updateList.size()) {
                        showMessageDialog(getString(R.string.dialog_title_error), getString(R.string.error_msg_save_attendance), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        hideMessageDialog();
                                    }
                                },
                                null);
                    }else{
                        Message message = _handler.obtainMessage(CMD_INIT_LAYOUT);
                        message.arg1 = Constants.BoardEditMode.Show.ordinal();
                        _handler.sendMessage(message);

                        Message message2 = _handler.obtainMessage(CMD_GET_ATTENDANCE_LIST);
//                        message2.arg1 = _selectedClassCode;
                        _handler.sendMessage(message2);
                        showMessageDialog(getString(R.string.dialog_title_alarm), getString(R.string.msg_save_attendance_success, _updateList.size() - failList.size(), failList.size()), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        hideMessageDialog();
                                    }
                                },
                                null);
                    }
                }
                break;
                case CMD_INIT_LAYOUT:{
                    int arg = msg.arg1;
                    Constants.BoardEditMode mode = Constants.BoardEditMode.Show;
                    try{
                        mode = Constants.BoardEditMode.values()[arg];
                        _boardMode = mode;
                        initLayout(mode);
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }

                }
                break;

            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_student_info);
        mContext = this;
        _acaCode = PreferenceUtil.getAcaCode(mContext);
        _acaName = PreferenceUtil.getAcaName(mContext);
        _userGubun = PreferenceUtil.getUserGubun(this);
        _seq = PreferenceUtil.getUserSeq(this);
        _sfCode = PreferenceUtil.getUserSFCode(this);
        LogMgr.e(_userGubun + "/" + _seq + "/" + _sfCode);

        initAppbar();
        initView();

//        getData();
        Message msg = _handler.obtainMessage(CMD_GET_CLASS_LIST);
        msg.arg1 = _sfCode;
        _handler.sendMessage(msg);
        setAnimMove(Constants.MOVE_DOWN);
    }

    void initView() {
        _tvDepartment = findViewById(R.id.tv_department);
        _tvClass = findViewById(R.id.tv_class);
        _viewDivider = findViewById(R.id.view_divider);
        _tvCalendar = findViewById(R.id.tv_calendar);
        _tvCalendar.setOnClickListener(this);
        _tvCalendar.setText(_dateFormat.format(_selectedDate));
        tvEmptyList = findViewById(R.id.tv_empty_list);

        _btnCalendarPrev = findViewById(R.id.btn_calendar_previous);
        _btnCalendarPrev.setOnClickListener(this);
        _btnCalendarNext = findViewById(R.id.btn_calendar_next);
        _btnCalendarNext.setOnClickListener(this);

        _btnAttendance = findViewById(R.id.btn_attendance);
        _btnAttendance.setOnClickListener(this);
        _btnAttendance.setCompoundDrawableTintList(ColorStateList.valueOf(getColor(R.color.btn_stroke)));
        _swipeRefresh = findViewById(R.id.refresh_layout);
        _recyclerView = findViewById(R.id.recyclerview_student_attendance);
        _adapter = new StudentInfoListAdapter(mContext, _attendanceList, new StudentInfoListAdapter.onItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if(position != NO_POSITION) {
                    AttendanceData data = _attendanceList.get(position);
                    Intent intent = new Intent(MenuStudentInfoActivity.this, DetailStudentInfoActivity.class);
                    //원생정보 seq, stCode
                    intent.putExtra(IntentParams.PARAM_STU_SEQ, data.memberSeq);
                    intent.putExtra(IntentParams.PARAM_STU_STCODE, data.stCode);
                    //날짜
                    String dateStr = _dateTransferFormat.format(_selectedDate);
                    intent.putExtra(IntentParams.PARAM_SELECTED_DATE, dateStr);
                    //반
                    int clsCode = 0;
                    if(_selectedClassData != null) clsCode = _selectedClassData.clsCode;
                    intent.putExtra(IntentParams.PARAM_STU_CLASSCODE, clsCode);
                    startActivity(intent);
                    overridePendingTransition(R.anim.horizontal_enter, R.anim.horizontal_out);
                }
            }

            @Override
            public void onCheckBoxChanged(int position, boolean isChecked) {
                LogMgr.w(TAG, "position = " + position + " /isChecked = " + isChecked);
                if(isChecked) {
                    if(_attendanceList.stream().allMatch(t->t.isChecked)) {
                        _checkBoxAll.setChecked(true);
                    }
                }else{
                    if(_checkBoxAll.isChecked()) {
                        _checkBoxAll.setChecked(false);
                    }
                }
                int count = (int)_attendanceList.stream().filter(t->t.isChecked).count();
                _tvCount.setText(getString(R.string.selected_count, count));
            }

            @Override
            public void onEditNote(int position) {
                AttendanceData data = _attendanceList.get(position);
                hideMessageDialog();
                popupDialog = new PopupDialog(mContext);
                popupDialog.setTitle(getString(R.string.dialog_title_edit_note));
                popupDialog.setContent(data.stName);
                popupDialog.setEdit(true);
                popupDialog.setEditTextInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                popupDialog.setEditMinMaxLines(4, 4);
                popupDialog.setEditText(data.attendBigoWrite);
                popupDialog.setOnOkButtonClickListener(ok -> {
                    data.attendBigoWrite = popupDialog.getInputText();
                    data.isChecked = true;
                    _adapter.notifyItemChanged(position);
                    hideMessageDialog();
                });
                popupDialog.setOnCancelButtonClickListener(cancel -> { hideMessageDialog(); });
                popupDialog.show();
            }
        });
        _recyclerView.setAdapter(_adapter);
//        _recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));

        _spinnerClass = findViewById(R.id.spinner_class);
        {
            WrapContentSpinnerAdapter adapter = new WrapContentSpinnerAdapter(mContext, _classDataList.stream().map(t -> t.clsName).collect(Collectors.toList()), _spinnerClass);
            _spinnerClass.setSpinnerAdapter(adapter);
        }

        _spinnerClass.setIsFocusable(true);
        _spinnerClass.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {

            _selectedClassData = _classDataList.get(newIndex);
            _tvClass.setText(_classDataList.get(newIndex).clsName);
            Message msg = _handler.obtainMessage(CMD_GET_ATTENDANCE_LIST);// spinner에서 선택한 반 코드로 갱신
//            msg.arg1 = _selectedClassCode;
            _handler.sendMessage(msg);

        });
        _spinnerClass.setSpinnerOutsideTouchListener(new OnSpinnerOutsideTouchListener() {
            @Override
            public void onSpinnerOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                _spinnerClass.dismiss();
            }
        });
        _spinnerClass.setLifecycleOwner(this);
        _swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Message msg = _handler.obtainMessage(CMD_GET_ATTENDANCE_LIST);// spinner에서 선택한 반 코드로 갱신
//                msg.arg1 = _selectedClassCode;
                _handler.sendMessage(msg);
                _swipeRefresh.setRefreshing(false);
            }
        });
        _layoutCalendar = findViewById(R.id.layout_select_calendar);
        _layoutControlAll = findViewById(R.id.layout_control_all);
        _checkBoxAll = findViewById(R.id.checkbox_all);
        _checkBoxAll.setOnClickListener(this);
        _tvCount = findViewById(R.id.tv_count);
        _tvTotalCount = findViewById(R.id.tv_total_count);
        _spinnerAttendanceMultiple = findViewById(R.id.spinner_attendance_status_all);
        _spinnerAttendanceMultiple.setIsFocusable(true);
        _spinnerAttendanceMultiple.setItems(Constants.AttendanceStatus.getNameList());
        ColoredSpinnerAdapter adapter = new ColoredSpinnerAdapter(mContext, Constants.AttendanceStatus.getColoredNameList(mContext), _spinnerAttendanceMultiple);
        _spinnerAttendanceMultiple.setSpinnerAdapter(adapter);
        _spinnerAttendanceMultiple.setSpinnerPopupHeight(ConstraintLayout.LayoutParams.WRAP_CONTENT);
        _spinnerAttendanceMultiple.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener<SpannableString>() {
            @Override
            public void onItemSelected(int oldIndex, @Nullable SpannableString oldItem, int newIndex, SpannableString newItem) {
                Constants.AttendanceStatus selectedStatus = Constants.AttendanceStatus.ATTENDANCE;
//                selectedStatus = Constants.AttendanceStatus.getByName(newItem);
                selectedStatus = Constants.AttendanceStatus.values()[newIndex];
                _selectMultipleAttendanceStatus = selectedStatus;
                _attendanceList.stream().filter(t->t.isChecked).forEach(item->{
                        item.attendGubunWrite = _selectMultipleAttendanceStatus.getCode();
                });
                _adapter.notifyDataSetChanged();
                _spinnerAttendanceMultiple.clearSelectedItem();
            }
        });
        _spinnerAttendanceMultiple.setSpinnerOutsideTouchListener(new OnSpinnerOutsideTouchListener() {
            @Override
            public void onSpinnerOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                _spinnerAttendanceMultiple.dismiss();
            }
        });
        StringBuilder sb = new StringBuilder();
        String deptName = PreferenceUtil.getDepartmentName(mContext);
        String sfGrade = PreferenceUtil.getSFGrade(mContext);
        if(TextUtils.isEmpty(deptName)) {
            if(TextUtils.isEmpty(sfGrade)){

            }else{
                sb.append(sfGrade);
            }
        }else {
            if(TextUtils.isEmpty(sfGrade)){
                sb.append(deptName);
            }else{
                sb.append(deptName + " / " + sfGrade);
            }
        }
        _tvDepartment.setText(sb.toString());
        initLayout(_boardMode);
    }
    void initLayout(Constants.BoardEditMode mode) {
        LogMgr.d(TAG, "initLayout " + mode.name());
        switch(mode) {
            case Show:  //조회모드
                _adapter.setEditMode(false);
                _tvTotalCount.setVisibility(View.VISIBLE);
                if(_attendanceList != null) {
                    _tvTotalCount.setText(getString(R.string.content_recipient_counts, _attendanceList.size()));
                }
                if(_layoutControlAll != null) _layoutControlAll.setVisibility(View.GONE);
                if(_viewDivider != null) _viewDivider.setVisibility(View.INVISIBLE);
                if(_tvClass != null) _tvClass.setVisibility(View.INVISIBLE);
                if(_tvDepartment != null) _tvDepartment.setVisibility(View.VISIBLE);
                if(_spinnerClass != null) _spinnerClass.setVisibility(View.VISIBLE);
                if(_layoutCalendar != null) {
                    _layoutCalendar.setEnabled(true);
                    _layoutCalendar.setBackgroundColor(getColor(R.color.white));
                }
                if(_btnAttendance != null) _btnAttendance.setVisibility(View.VISIBLE);
                if(_btnCalendarPrev != null) _btnCalendarPrev.setVisibility(View.VISIBLE);
                if(_btnCalendarNext != null) _btnCalendarNext.setVisibility(View.VISIBLE);
                if(_tvCalendar != null) {
                    _tvCalendar.setEnabled(true);
                    _tvCalendar.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_vector_calendar,0);
                }
                if(_swipeRefresh != null) {
                    _swipeRefresh.setEnabled(true);
                }
                if(_adapter != null) {
                    _adapter.notifyDataSetChanged();
                }
                invalidateOptionsMenu();    //onCreateOptionsMenu 호출됨
                break;
            case Edit:
                _adapter.setEditMode(true);
                _tvTotalCount.setVisibility(View.GONE);
                if(_layoutControlAll != null) _layoutControlAll.setVisibility(View.VISIBLE);
//                _tvCount.setText(getString(R.string.format_count_per_all, 0, _attendanceList.size()));
                if(_viewDivider != null) _viewDivider.setVisibility(View.VISIBLE);
                if(_tvClass != null) _tvClass.setVisibility(View.VISIBLE);
                if(_tvDepartment != null) _tvDepartment.setVisibility(View.INVISIBLE);
                if(_spinnerClass != null) _spinnerClass.setVisibility(View.INVISIBLE);
                if(_layoutCalendar != null) {
                    _layoutCalendar.setEnabled(false);
                    _layoutCalendar.setBackgroundColor(getColor(R.color.bg_gray));
                }
                if(_btnAttendance != null) _btnAttendance.setVisibility(View.GONE);
                if(_btnCalendarPrev != null) _btnCalendarPrev.setVisibility(View.INVISIBLE);
                if(_btnCalendarNext != null) _btnCalendarNext.setVisibility(View.INVISIBLE);
                if(_tvCalendar != null) {
                    _tvCalendar.setEnabled(false);
                    _tvCalendar.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                }
                if(_checkBoxAll != null) _checkBoxAll.setChecked(false);
//                for(AttendanceData data : _attendanceList) {
//                    data.isChecked = false;
//                    data.attendCode = -1;
//                }
                _attendanceList.forEach(t->{
                    t.attendGubunWrite = -1;
                    t.attendBigoWrite = TextUtils.isEmpty(t.attendBigo)?"":t.attendBigo;
                    t.isChecked = false;
                    t.updateResult = 0;
                });
                if(_swipeRefresh != null) {
                    _swipeRefresh.setEnabled(false);
                }
                if(_adapter != null) {
                    _adapter.notifyDataSetChanged();
                }
                if(_spinnerAttendanceMultiple != null) {
                    _spinnerAttendanceMultiple.clearSelectedItem();
                }
                invalidateOptionsMenu();    //onCreateOptionsMenu 호출됨
                break;
            default:
                break;
        }
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

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btn_attendance:
                Message msg = _handler.obtainMessage(CMD_INIT_LAYOUT);
                msg.arg1 = Constants.BoardEditMode.Edit.ordinal();
                _handler.sendMessage(msg);
                _originAttendanceList = (ArrayList<AttendanceData>) _attendanceList.clone();
                break;
            case R.id.tv_calendar:
                showDatePicker();
                break;
            case R.id.btn_calendar_previous:
                navigateDate(-1);
                break;
            case R.id.btn_calendar_next:
                navigateDate(1);
                break;
            case R.id.checkbox_all:
                boolean tobeChecked = _checkBoxAll.isChecked();
                LogMgr.w(TAG, "tobeChecked = " + tobeChecked);
//                _checkBoxAll.setChecked(tobeChecked);
                if(_attendanceList != null) {
                    for(AttendanceData data : _attendanceList) {
                        data.isChecked = tobeChecked;
                    }
                }
                _adapter.notifyDataSetChanged();
                break;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(_boardMode == Constants.BoardEditMode.Show) {
            menu.clear();

        }else if(_boardMode == Constants.BoardEditMode.Edit) {
            getMenuInflater().inflate(R.menu.menu_save, menu);
            int positionOfMenuItem = 0;
            try {
                MenuItem item = menu.getItem(positionOfMenuItem);
                SpannableString span = new SpannableString(item.getTitle());
                span.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.red)), 0, span.length(), 0);
                item.setTitle(span);
            }catch(Exception ex){}
        }
        this._menu = menu;
        return (super.onCreateOptionsMenu(menu));
    }
    
    protected void showCustomMessageDialog(String title, String msg, View.OnClickListener okListener, View.OnClickListener cancelListener) {
        if(popupDialog != null && popupDialog.isShowing()) {
            popupDialog.dismiss();
        }
        popupDialog = new PopupDialog(mContext);
        popupDialog.setTitle(title);
        popupDialog.setContent(msg);
        boolean isToday = Utils.isToday(_selectedDate);
        LogMgr.e(TAG, _selectedDate.toString() + " / isToday =" + isToday);
        popupDialog.setCheckBox(getString(R.string.msg_allow_notify), isToday); //오늘만 Push 전송 켜고 나머지는 끄기
        popupDialog.setOnOkButtonClickListener(okListener);
        if(cancelListener != null) {
            popupDialog.setOnCancelButtonClickListener(cancelListener);
        }
        popupDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_save:
                if(_updateList == null) {
                    _updateList = new ArrayList<>();
                }
                else {
                    _updateList.clear();
                }
                int isCheckCount = (int)_attendanceList.stream().filter(t-> t.isChecked).count();
                if(isCheckCount == 0) {
                    super.showMessageDialog(getString(R.string.dialog_title_alarm), getString(R.string.msg_select_student_attendance), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    hideMessageDialog();
                                }
                            },
                            null);
                    return true;
                }else {
                    for (AttendanceData data : _attendanceList) {
                        if (data.isChecked) {
                            LogMgr.e("isChecked true");
                            LogMgr.e("gubun = " + data.attendGubun + " / set code = " + data.attendGubunWrite);
                            LogMgr.e("attendBigo = " + data.attendBigo + " / set bigo = " + data.attendBigoWrite);
                            if (data.attendGubun != data.attendGubunWrite || !data.attendBigo.equals(data.attendBigoWrite)) {
                                _updateList.add(data);
                            }
                        }
                    }
                }
                LogMgr.e("updatelist size = " + _updateList.size());
                if(_updateList.size() < 1) {
                    super.showMessageDialog(getString(R.string.dialog_title_error), getString(R.string.msg_edit_student_attendance), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    hideMessageDialog();
                                }
                            },
                            null);
                    return true;
                }
                showCustomMessageDialog(getString(R.string.dialog_title_alarm), getString(R.string.msg_save_attendance), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {    //ok
                                hideMessageDialog();

                                Message msg = _handler.obtainMessage(CMD_UPDATE_ATTENDANCE);
                                msg.arg1 = 0;
//                                msg.arg2 = _selectedClassCode;
                                msg.obj = popupDialog.getCheckBoxChecked();
                                _handler.sendMessage(msg);
                                showProgressDialog();
//                                finish();
                            }
                        },
                        new View.OnClickListener() {    //cancel
                            @Override
                            public void onClick(View view) {
                                hideMessageDialog();
                            }
                        });

                //test
//                _boardMode = Constants.BoardEditMode.Show;
//                initLayout(_boardMode);
                ////////
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void navigateDate(int addDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(_selectedDate);
        calendar.add(Calendar.DATE, addDate);
        onDateSet(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    }
    void showDatePicker() {
        DatePickerFragment datePickerDialog = new DatePickerFragment(this);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(_selectedDate);
        datePickerDialog.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show(getSupportFragmentManager(), "date");
    }

    private void requestGetClassList(int sfCode){
        if(_classNameList != null && _classNameList.size() > 0) {
            _classNameList.clear();
        }
        if(_classDataList != null && _classDataList.size() > 0) {
            _classDataList.clear();
        }
        if (RetrofitClient.getInstance() != null){
            _retrofitApi = RetrofitClient.getApiInterface();
            _retrofitApi.getManagerClassInfo(sfCode).enqueue(new Callback<GetManagerClassInfoResponse>() {
                @Override
                public void onResponse(Call<GetManagerClassInfoResponse> call, Response<GetManagerClassInfoResponse> response) {
                    try {
                        if (response.isSuccessful()){
                            List<ClassData> getData = null;
                            if (response.body() != null) {

                                getData = response.body().data;
                                _classDataList = getData;
                                if (getData != null){
                                    for (ClassData data : getData) {
                                        _classNameList.add(data.clsName);
                                    }
                                    Utils.updateSpinnerList(_spinnerClass, _classNameList);
                                    if(_classNameList.size() > 0) {
                                        _spinnerClass.selectItemByIndex(0);
                                    }else{
                                        showMessageDialog(getString(R.string.dialog_title_alarm),
                                                getString(R.string.error_message_empty_class),
                                                new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        finish();
                                                    }
                                                },
                                                null);
                                    }

                                }else {
                                    LogMgr.e(TAG, "ListData is null");
                                    showMessageDialog(getString(R.string.dialog_title_error),
                                            getString(R.string.error_message_empty_class),
                                            new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    finish();
                                                }
                                            },
                                            null);
                                }
                            }
                        }else{
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e){
                        LogMgr.e(TAG + "requestBoardList() Exception : ", e.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<GetManagerClassInfoResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "requestBoardList() onFailure >> " + t.getMessage());
                    }catch (Exception e){
                    }
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void requestGetAttendanceList(int clsCode, String changeDate){
        SimpleDateFormat mFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYYMMDD);
        String date = TextUtils.isEmpty(changeDate) ? mFormat.format(_selectedDate) : changeDate;

        if (RetrofitClient.getInstance() != null){
            _retrofitApi = RetrofitClient.getApiInterface();
            _retrofitApi.getClassAttendanceInfo(date, clsCode).enqueue(new Callback<GetAttendanceInfoResponse>() {
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
                                _attendanceList.addAll(getData);

                                _selectedDate = mFormat.parse(changeDate);
                                _tvCalendar.setText(_dateFormat.format(_selectedDate));
                            }
                        }else{
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e){
                        LogMgr.e(TAG + "requestBoardList() Exception : ", e.getMessage());
                    }finally{
//                        hideProgressDialog();
                        _adapter.notifyDataSetChanged();
                        if(_adapter.getItemCount() > 0) {
                            tvEmptyList.setVisibility(View.GONE);
                        }else{
                            tvEmptyList.setVisibility(View.VISIBLE);
                        }
                        _tvTotalCount.setText(getString(R.string.content_recipient_counts, _attendanceList.size()));
                        if(_attendanceList.size() > 0 && _recyclerView != null) {
                            _handler.postDelayed(() -> _recyclerView.smoothScrollToPosition(0), scrollToTopDelay);
                        }
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
                        _adapter.notifyDataSetChanged();
                        if(_adapter.getItemCount() > 0) {
                            tvEmptyList.setVisibility(View.GONE);
                        }else{
                            tvEmptyList.setVisibility(View.VISIBLE);
                        }
                    }

                }
            });
        }
    }
    private void requestUpdateAttendanceList(int index, int clsCode, boolean isPushUpdate){
        String dateString = "";
        SimpleDateFormat outputDateFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYYMMDD);
        dateString = outputDateFormat.format(_selectedDate);
        StringBuilder sbStCode = new StringBuilder();
        StringBuilder sbAttendanceGubun = new StringBuilder();
        String attendanceBigo = "";
//        for(int i = 0; i < list.size(); i++) {
//
            AttendanceData data = _updateList.get(index);
            if(!data.attendBigo.equals(data.attendBigoWrite)) {
                attendanceBigo = data.attendBigoWrite;
            }
            LogMgr.e(TAG, "stCode = " + data.stCode + " / attendCode =" + data.attendGubunWrite + "/ bigowrite =" + attendanceBigo);
            sbStCode.append(String.valueOf(data.stCode));
            sbAttendanceGubun.append(String.valueOf(data.attendGubunWrite));
//            if(i < list.size() - 1) {
//                sbStCode.append(",");
//                sbAttendanceGubun.append(",");
//            }
//        }
        AttendanceRequest request = new AttendanceRequest();
        request.memberSeq = _seq;
        request.attendanceDate = dateString;
        request.attendanceBigo = attendanceBigo;
        request.acaCode = _acaCode;
        request.acaName = _acaName;
        request.clsCode = clsCode;
        request.clsName = _selectedClassData.clsName;
        request.stCode = sbStCode.toString();
        request.sfCode = _sfCode;
        request.stName = data.stName;
        request.attendanceGubun = sbAttendanceGubun.toString();
        request.isPushSend = isPushUpdate? "Y" : "N";
        request.parentPhoneNumber = data.parentPhoneNumber.replaceAll("[^0-9]", "");
        //캠퍼스 대표번호로 변경
//        request.smsSender = PreferenceUtil.getSMSSender(mContext).replaceAll("[^0-9]", "");
        request.smsSender = DataManager.getInstance().getACAData(_acaCode).acaTel.replaceAll("[^0-9]", "");
        //test
//        request.parentPhoneNumber = "01046332026";
        if (RetrofitClient.getInstance() != null){
            _retrofitApi = RetrofitClient.getApiInterface();
            _retrofitApi.updateAttendance(request).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    try {
                        if (response.isSuccessful()){

                        }else{
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e){
                        LogMgr.e(TAG + "requestBoardList() Exception : ", e.getMessage());
                    }finally{
//                        hideProgressDialog();
                        _updateList.get(index).updateResult = response.code();
                        if(index >= _updateList.size() - 1) {   //종료
                            _handler.sendEmptyMessage(CMD_UPDATE_ATTENDANCE_RESULT);
                        }else{
                            int nextIndex = index + 1;
                            Message msg = _handler.obtainMessage(CMD_UPDATE_ATTENDANCE);
                            msg.arg1 = nextIndex;
//                            msg.arg2 = _selectedClassCode;
                            msg.obj = isPushUpdate;
                            _handler.sendMessage(msg);
                        }
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    try {
                        Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                        LogMgr.e(TAG, "requestBoardList() onFailure >> " + t.getMessage());
                    }catch (Exception e){
                    }finally{
//                        hideProgressDialog();
                        _updateList.get(index).updateResult = 500;
                        if(index >= _updateList.size() - 1) {
                            _handler.sendEmptyMessage(CMD_UPDATE_ATTENDANCE_RESULT);
                        }else{
                            int nextIndex = index + 1;
                            Message msg = _handler.obtainMessage(CMD_UPDATE_ATTENDANCE);
                            msg.arg1 = nextIndex;
//                            msg.arg2 = _selectedClassCode;
                            msg.obj = isPushUpdate;
                            _handler.sendMessage(msg);
                        }
                    }

                }
            });
        }
    }

    @Override
    public void onDateSet(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);

        if(_classDataList.size() > 0 && _spinnerClass.getSelectedIndex() >= 0) {
            Message msg = _handler.obtainMessage(CMD_GET_ATTENDANCE_LIST);// spinner에서 선택한 반 코드로 갱신
//            msg.arg1 = _selectedClassCode;
            SimpleDateFormat outputDateFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYYMMDD);
            msg.obj = outputDateFormat.format(calendar.getTime());
            _handler.sendMessage(msg);
        }
    }
    @Override
    public void onBackPressed() {
        LogMgr.d(TAG,"onBackPressed mode? " + _boardMode.name());
        if(_boardMode == Constants.BoardEditMode.Edit) {
            Message msg = _handler.obtainMessage(CMD_INIT_LAYOUT);
            msg.arg1 = Constants.BoardEditMode.Show.ordinal();
            _handler.sendMessage(msg);
        }else {
            super.onBackPressed();
            setAnimMove(Constants.MOVE_DOWN);
        }
    }

}