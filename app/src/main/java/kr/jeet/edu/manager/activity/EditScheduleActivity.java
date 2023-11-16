package kr.jeet.edu.manager.activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;
import com.skydoves.powerspinner.OnSpinnerOutsideTouchListener;
import com.skydoves.powerspinner.PowerSpinnerView;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.text.ParseException;
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
import kr.jeet.edu.manager.adapter.RecipientChipListAdapter;
import kr.jeet.edu.manager.adapter.WrapContentSpinnerAdapter;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.DataManager;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.dialog.DatePickerFragment;
import kr.jeet.edu.manager.model.data.ACAData;
import kr.jeet.edu.manager.model.data.DepartmentData;
import kr.jeet.edu.manager.model.data.RecipientData;
import kr.jeet.edu.manager.model.data.ScheduleData;
import kr.jeet.edu.manager.model.data.StudentGradeData;
import kr.jeet.edu.manager.model.request.RecipientRequest;
import kr.jeet.edu.manager.model.response.BaseResponse;
import kr.jeet.edu.manager.model.response.BoardRegisterResponse;
import kr.jeet.edu.manager.model.response.GetDeptListResponse;
import kr.jeet.edu.manager.model.response.ScheduleRegisterResponse;
import kr.jeet.edu.manager.model.response.StudentGradeListResponse;
import kr.jeet.edu.manager.server.RetrofitClient;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.PreferenceUtil;
import kr.jeet.edu.manager.utils.Utils;
import kr.jeet.edu.manager.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditScheduleActivity extends BaseActivity implements DatePickerFragment.OnDateSetListener{
    private static final String TAG = "editSchedule";
    private static final int CMD_GET_ACA_LIST = 0;
    private static final int CMD_GET_GRADE_LIST = 1;
    SimpleDateFormat _dateTransferFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYY_MM_DD);
    //region views
    PowerSpinnerView spinnerCampus, spinnerGrade;
    EditText etTitle, etContent;
    TextView tvDate, tvDateStart, tvDateEnd;
    RecyclerView _recyclerViewRecipient;
    Button btnAppendRecipient;
    TextView tvRecipient, tvRecipientEmpty;
    LinearLayout layoutCheckSMS;
    CheckBox cbSendSMS;
    LinearLayoutCompat layoutBottom, layoutDateStart, layoutDateEnd;
    //endregion
    //region variables
    RecipientChipListAdapter _recipientListAdapter;
    List<ACAData> _ACAList = new ArrayList<>();
    List<String> _ACANameList = new ArrayList<>();
    List<StudentGradeData> _GradeList = new ArrayList<>();
    ACAData _selectedACA = null;
    StudentGradeData _selectedGrade = null;
    ScheduleData _currentData = null;
    int _currentSeq = 0;
    Date _selectedDate = new Date();
    Date _selectedDateStart = new Date();
    Date _selectedDateEnd = new Date();
    List<RecipientData> _recipientList = new ArrayList<>();
    Set<RecipientData> _recipientSet = new HashSet<>();
    String _acaCode = "";
    String _gubunCode = "";
    int _userGubun = 1;
    int _seq = 0;
    int _sfCode = 0;

    Constants.BoardEditMode boardEditMode = Constants.BoardEditMode.New;
    //endregion
    //작성화면 resultLauncher
    ActivityResultLauncher<Intent> recipientResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() != RESULT_CANCELED) {
                Intent intent = result.getData();
                if(intent.hasExtra(IntentParams.PARAM_EXIST_RECIPIENT_INFO)) {
                    if(intent.getBooleanExtra(IntentParams.PARAM_EXIST_RECIPIENT_INFO, false)) {
                        List<RecipientData> list = DataManager.getInstance().getRecipientList();
                        LogMgr.w(TAG, "list count = " + list.size());
                        if(!list.isEmpty()) {
                            _recipientSet.addAll(list);
                        }
                        initChipGroup();
                    }
                }
            }
        }
    });
    private Handler _handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {

                case CMD_GET_ACA_LIST:
                    break;
                case CMD_GET_GRADE_LIST:
                    if (_GradeList != null && !_GradeList.isEmpty()) {
                        boolean isSelectNow = (boolean) msg.obj;
                        if(boardEditMode.equals(Constants.BoardEditMode.New)) {
                            spinnerGrade.setEnabled(true);
                        }
//                        spinnerDept.setItems(_DeptList.stream().map(t -> t.deptName).collect(Collectors.toList()));
                        Utils.updateSpinnerList(spinnerGrade, _GradeList.stream().map(t -> t.gubunName).collect(Collectors.toList()));
                        //
                        //todo 수정으로 들어온 경우 선택해야 함.
                        if(isSelectNow){
                            for(int i = 0; i < _GradeList.size(); i++) {
                                StudentGradeData data = _GradeList.get(i);
                                if(String.valueOf(data.gubunCode).equals(_currentData.acaGubunCode)) {
                                    LogMgr.w(TAG, "edit mode gubunName = " + _currentData.acaGubunName);
                                    spinnerGrade.selectItemByIndex(i);
                                    break;
                                }
                            }
                        }else{
                            if(!TextUtils.isEmpty(_gubunCode)) {
                                try {
                                    LogMgr.w(TAG, "_currentData.acaGubunCode = " + _gubunCode);
                                    Optional option = _GradeList.stream().filter(t -> String.valueOf(t.gubunCode).equals(_gubunCode)).findFirst();
                                    if (option.isPresent()) {
                                        _selectedGrade = (StudentGradeData) option.get();
                                    }
                                }catch(Exception ex){
                                    ex.printStackTrace();
                                }

                                if (_selectedGrade != null) {
                                    LogMgr.w(TAG, "selectedGrade is not null");
                                    int selectedIndex = _GradeList.indexOf(_selectedGrade);
                                    LogMgr.w(TAG, "selectedGrade = " + _selectedGrade.gubunName + " / index  = " + selectedIndex);
                                    spinnerGrade.selectItemByIndex(selectedIndex);
                                    _gubunCode = "";
                                }else{
                                    LogMgr.w(TAG, "selectedGrade is null");
                                }
                            }
                        }
                    }
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_schedule);
        mContext = this;
        _userGubun = PreferenceUtil.getUserGubun(this);
        _seq = PreferenceUtil.getUserSeq(this);
        _sfCode = PreferenceUtil.getUserSFCode(this);
//        _acaCode = PreferenceUtil.getAcaCode(this);

        initIntentData();
        initView();
        initAppbar();
        initData();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //수신인 리스트 비움
        DataManager.getInstance().getRecipientList().clear();
    }
    void initIntentData() {
        Intent intent = getIntent();
        if(intent != null){
            if(intent.hasExtra(IntentParams.PARAM_SCHEDULE_INFO)) {
                boardEditMode = Constants.BoardEditMode.Edit;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    _currentData = intent.getParcelableExtra(IntentParams.PARAM_SCHEDULE_INFO, ScheduleData.class);
                }else{
                    _currentData = intent.getParcelableExtra(IntentParams.PARAM_SCHEDULE_INFO);
                }
            }else{
                boardEditMode = Constants.BoardEditMode.New;
                if(intent.hasExtra(IntentParams.PARAM_SELECTED_DATE)) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    String dateStr = intent.getStringExtra(IntentParams.PARAM_SELECTED_DATE);
                    try {
                        _selectedDate = _dateTransferFormat.parse(dateStr);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                if(intent.hasExtra(IntentParams.PARAM_STU_ACACODE)) {
                    _acaCode = intent.getStringExtra(IntentParams.PARAM_STU_ACACODE);
                }
                if(intent.hasExtra(IntentParams.PARAM_STU_GRADECODE)) {
                    _gubunCode = intent.getStringExtra(IntentParams.PARAM_STU_GRADECODE);
                }
            }
        }
    }
    void initData() {

        //LogMgr.w("currentData :" + _currentData.toString());
        if(boardEditMode == Constants.BoardEditMode.New){
            layoutBottom.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(_acaCode)) {
                Optional option = _ACAList.stream().filter(t -> t.acaCode.equals(_acaCode)).findFirst();
                if (option.isPresent()) {
                    _selectedACA = (ACAData) option.get();
                }
                if (_selectedACA != null) {
                    int selectedIndex = _ACAList.indexOf(_selectedACA);
                    spinnerCampus.selectItemByIndex(selectedIndex);
                }

            }
            spinnerCampus.setEnabled(true);

            try {
                String outputDate = _dateTransferFormat.format(_selectedDate);
                //tvDate.setText(outputDate);

                try {
                    Date dateSel = _dateTransferFormat.parse(outputDate);
                    Calendar calSel = Calendar.getInstance();
                    if (dateSel != null) calSel.setTime(dateSel);

                    Calendar calCurrent = Calendar.getInstance();

                    if (calSel.before(calCurrent)) {
                        tvDateStart.setText(_dateTransferFormat.format(_selectedDate));
                        tvDateEnd.setText(_dateTransferFormat.format(_selectedDate));

                        _selectedDateStart = _selectedDate;
                        _selectedDateEnd = _selectedDate;

                    } else {
                        tvDateStart.setText(outputDate);
                        tvDateEnd.setText(outputDate);

                        _selectedDateStart = _selectedDate;
                        _selectedDateEnd = _selectedDate;
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }catch(Exception ex){}

            btnAppendRecipient.setVisibility(View.VISIBLE);
        }else {
            layoutBottom.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(_currentData.acaCode)) {

                Optional option = _ACAList.stream().filter(t -> t.acaCode.equals(_currentData.acaCode)).findFirst();
                if (option.isPresent()) {
                    _selectedACA = (ACAData) option.get();
                }
                if (_selectedACA != null) {
                    int selectedIndex = _ACAList.indexOf(_selectedACA);
                    spinnerCampus.selectItemByIndex(selectedIndex);
                }

            } else {
                //전체선택
//            spinner.selectItemByIndex(0);
            }
            if(!TextUtils.isEmpty(_currentData.title)){
                etTitle.setText(_currentData.title);
            }
            if(!TextUtils.isEmpty(_currentData.content)){
                etContent.setText(_currentData.content);
            }

            spinnerCampus.setEnabled(false);
//            etTarget.setText(_currentData.target);
//            if (!TextUtils.isEmpty(_currentData.target)) {
//
//                Optional option = _ACAList.stream().filter(t -> t.acaCode.equals(_currentData.acaCode)).findFirst();
//                if (option.isPresent()) {
//                    _selectedACA = (ACAData) option.get();
//                }
//                if (_selectedACA != null) {
//                    int selectedIndex = _ACAList.indexOf(_selectedACA);
//                    spinnerCampus.selectItemByIndex(selectedIndex);
//                }
//
//            } else {
//                //전체선택
////            spinner.selectItemByIndex(0);
//            }
            try {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, _currentData.year);
                calendar.set(Calendar.MONTH, _currentData.month - 1);
                calendar.set(Calendar.DAY_OF_MONTH, _currentData.day);
                Date date = calendar.getTime();
                _selectedDate = date;
                tvDate.setText(_dateTransferFormat.format(date));

            }catch(Exception ex){}
            btnAppendRecipient.setVisibility(View.GONE);
            _recipientSet = new HashSet<>(_currentData.receiverList);
            initChipGroup();
        }


    }
    @Override
    void initView() {
        findViewById(R.id.layout_root).setOnClickListener(this);
        layoutBottom = findViewById(R.id.layout_bottom);
        layoutDateStart = findViewById(R.id.layout_date_start);
        layoutDateEnd = findViewById(R.id.layout_date_end);
        spinnerCampus = findViewById(R.id.spinner_campus);
        tvDate = findViewById(R.id.tv_content_date);
        tvDateStart = findViewById(R.id.tv_content_date_start);
        tvDateEnd = findViewById(R.id.tv_content_date_end);
        etTitle = findViewById(R.id.et_content_title);
        etContent = findViewById(R.id.et_content_content);
//        etTarget = findViewById(R.id.et_content_target);
        tvRecipient = findViewById(R.id.tv_recipient_count);
        btnAppendRecipient = findViewById(R.id.btn_append_recipient);
        btnAppendRecipient.setOnClickListener(this);
        layoutCheckSMS = findViewById(R.id.layout_send_sms);
        if(boardEditMode == Constants.BoardEditMode.New) {
            layoutCheckSMS.setVisibility(View.VISIBLE);
            layoutCheckSMS.setOnClickListener(this);
            tvDateStart.setOnClickListener(this);
            layoutDateStart.setVisibility(View.VISIBLE);
            tvDateEnd.setOnClickListener(this);
            layoutDateEnd.setVisibility(View.VISIBLE);
            tvDate.setVisibility(View.GONE);
        }else{
            layoutCheckSMS.setVisibility(View.GONE);
            layoutDateStart.setVisibility(View.GONE);
            layoutDateEnd.setVisibility(View.GONE);
            tvDate.setVisibility(View.VISIBLE);
            tvDate.setOnClickListener(this);
        }
        cbSendSMS = findViewById(R.id.check_sms);
        //region spinner campus
        //ACA list for spinner
        _ACAList.addAll(DataManager.getInstance().getLocalACAListMap().values());
        if(_ACAList != null) _ACANameList = _ACAList.stream().map(t -> t.acaName).collect(Collectors.toList());
//        {
//            WrapContentSpinnerAdapter adapter = new WrapContentSpinnerAdapter(mContext, _ACANameList, spinnerCampus);
//            spinnerCampus.setSpinnerAdapter(adapter);
//        }

        spinnerCampus.setItems(_ACANameList);
        spinnerCampus.setSpinnerPopupHeight(ConstraintLayout.LayoutParams.WRAP_CONTENT);
        spinnerCampus.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener<String>() {
            @Override
            public void onItemSelected(int oldIndex, @Nullable String oldItem, int newIndex, String newItem) {
                LogMgr.e(newItem + " selected");
                ACAData selectedData = null;

                Optional optional = _ACAList.stream().filter(t -> t.acaName == newItem).findFirst();
                if(optional.isPresent()) {
                    selectedData = (ACAData) optional.get();
                }
                _selectedACA = selectedData;
                LogMgr.w("selectedACA = " + _selectedACA.acaCode + " / " + _selectedACA.acaName);

                if(boardEditMode == Constants.BoardEditMode.New) {
                    requestGradeList(_selectedACA.acaCode, false);
                    if (_selectedGrade != null) {
                        _selectedGrade = null;
                    }
                    if (spinnerGrade != null) spinnerGrade.clearSelectedItem();
                }else{
                    requestGradeList(_selectedACA.acaCode, true);
                }
            }
        });
        spinnerCampus.setSpinnerOutsideTouchListener(new OnSpinnerOutsideTouchListener() {
            @Override
            public void onSpinnerOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                spinnerCampus.dismiss();
            }
        });
        spinnerCampus.setLifecycleOwner(this);
        //endregion
        //region spinner dept
        spinnerGrade = findViewById(R.id.spinner_grade);
//        {
//            WrapContentSpinnerAdapter adapter = new WrapContentSpinnerAdapter(mContext, _GradeList.stream().map(t -> t.gubunName).collect(Collectors.toList()), spinnerGrade);
//            spinnerGrade.setSpinnerAdapter(adapter);
//        }

        spinnerGrade.setEnabled(false);
        spinnerGrade.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener<String>() {
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

            }
        });
        spinnerGrade.setSpinnerOutsideTouchListener(new OnSpinnerOutsideTouchListener() {
            @Override
            public void onSpinnerOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                spinnerGrade.dismiss();
            }
        });
        spinnerGrade.setLifecycleOwner(this);
        //endregion
        //region chip
//        recipientChipGroup = findViewById(R.id.chipgroup);
        _recyclerViewRecipient = findViewById(R.id.recycler_chip);
        _recipientListAdapter = new RecipientChipListAdapter(mContext, _recipientList, new RecipientChipListAdapter.onItemClickListener() {
            @Override
            public void onDeleteClick(int position, RecipientData item) {
                _recipientList.remove(position);
                _recipientListAdapter.notifyItemRemoved(position);
                tvRecipient.setText(getString(R.string.content_recipient_counts, _recipientList.size()));
                if(boardEditMode == Constants.BoardEditMode.New) {
                    if (_recipientList.isEmpty()) {
                        _recyclerViewRecipient.setVisibility(View.GONE);
                        tvRecipientEmpty.setVisibility(View.VISIBLE);
                    } else {
                        _recyclerViewRecipient.setVisibility(View.VISIBLE);
                        tvRecipientEmpty.setVisibility(View.GONE);
                    }
                }else{
                    if (_recipientList.isEmpty()) {
                        _recyclerViewRecipient.setVisibility(View.GONE);
                    } else {
                        _recyclerViewRecipient.setVisibility(View.VISIBLE);
                    }
                    tvRecipientEmpty.setVisibility(View.GONE);
                }
            }
        });
        _recipientListAdapter.setEditMode(boardEditMode);
        FlexboxLayoutManager fblManager = new FlexboxLayoutManager(this);
        fblManager.setFlexWrap(FlexWrap.WRAP);
        fblManager.setFlexDirection(FlexDirection.ROW);
        fblManager.setJustifyContent(JustifyContent.FLEX_START);
        _recyclerViewRecipient.setLayoutManager(fblManager);
        _recyclerViewRecipient.setAdapter(_recipientListAdapter);
        tvRecipientEmpty = findViewById(R.id.tv_chipgroup_empty);
        initChipGroup();
        //endregion
    }
    private void initChipGroup() {
        _recipientList.clear();
        _recipientList.addAll(_recipientSet);
        Collections.sort(_recipientList);
        _recipientListAdapter.notifyDataSetChanged();
        tvRecipient.setText(getString(R.string.content_recipient_counts, _recipientList.size()));
        if(boardEditMode == Constants.BoardEditMode.New) {
            if (_recipientList.isEmpty()) {
                _recyclerViewRecipient.setVisibility(View.GONE);
                tvRecipientEmpty.setVisibility(View.VISIBLE);
            } else {
                _recyclerViewRecipient.setVisibility(View.VISIBLE);
                tvRecipientEmpty.setVisibility(View.GONE);
            }
        }else{
            if (_recipientList.isEmpty()) {
                _recyclerViewRecipient.setVisibility(View.GONE);
            } else {
                _recyclerViewRecipient.setVisibility(View.VISIBLE);
            }
            tvRecipientEmpty.setVisibility(View.GONE);
        }
    }
    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        if(boardEditMode == Constants.BoardEditMode.New) {
            customAppbar.setTitle(R.string.menu_item_add);
        }else {
            customAppbar.setTitle(R.string.menu_item_edit);
        }
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);
        int positionOfMenuItem = 0;
        try {
            MenuItem item = menu.getItem(positionOfMenuItem);
            SpannableString span = new SpannableString(item.getTitle());
            span.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.red)), 0, span.length(), 0);
            item.setTitle(span);
        }catch(Exception ex){}
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_complete:
                if(checkValid()){
                    updateCurrentData();
                    requestUpdateSchedule();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void updateCurrentData() {
        if(_currentData == null) _currentData = new ScheduleData();
        _currentData.writerSeq = _seq;
        _currentData.title = etTitle.getText().toString().trim();
        _currentData.content = etContent.getText().toString().trim();
        _currentData.acaCode = _selectedACA.acaCode;
        _currentData.acaName = _selectedACA.acaName;
        String gubunName = "";
        String gubunCode = "";
        if(_selectedGrade != null) {
            gubunCode = String.valueOf(_selectedGrade.gubunCode);
            gubunName = _selectedGrade.gubunName;
        }
        _currentData.acaGubunCode = gubunCode;
        _currentData.acaGubunName = gubunName;
        Calendar calendar = Calendar.getInstance();
        if(boardEditMode == Constants.BoardEditMode.New) {
            calendar.setTime(_selectedDateStart);
            _currentData.duration = calculateDuration(_selectedDateStart, _selectedDateEnd);
        } else {
            calendar.setTime(_selectedDate);
        }
        _currentData.year = calendar.get(Calendar.YEAR);
        _currentData.month = calendar.get(Calendar.MONTH) + 1;
        _currentData.day = calendar.get(Calendar.DAY_OF_MONTH);
        if(_selectedGrade != null) {
            _currentData.acaGubunCode = String.valueOf(_selectedGrade.gubunCode);
            _currentData.acaGubunName = _selectedGrade.gubunName;
        }
        _currentData.isSendSMS = cbSendSMS.isChecked()? "Y" : "N";
    }
    void showDatePicker() {
        DatePickerFragment datePickerDialog = null;
        Calendar calendar = Calendar.getInstance();

        if (clickType == Constants.CLICK_START_DATE) {

            datePickerDialog = new DatePickerFragment(this);
            calendar.setTime(_selectedDateStart);

        } else if (clickType == Constants.CLICK_END_DATE){

            calendar.setTime(_selectedDateEnd);
            try {
                if (!TextUtils.isEmpty(tvDateStart.getText().toString())) {
                    Date dateStart = _dateTransferFormat.parse(tvDateStart.getText().toString());
                    Calendar calStart = Calendar.getInstance();
                    if (dateStart != null) calStart.setTime(dateStart);

                    datePickerDialog = new DatePickerFragment(this, dateStart, true);
                } else {
                    datePickerDialog = new DatePickerFragment(this);
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            datePickerDialog = new DatePickerFragment(this);
            calendar.setTime(_selectedDate);
        }

        LogMgr.d("selected date =" + _selectedDate.toString());

        datePickerDialog.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show(getSupportFragmentManager(), "date");
    }

    private Date setSelDate(Date selDate, int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selDate);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        selDate = calendar.getTime();
        return selDate;
    }

    @Override
    public void onDateSet(int year, int month, int day) {

        if (clickType == Constants.CLICK_START_DATE) {
            _selectedDateStart = setSelDate(_selectedDateStart, year, month, day);
            tvDateStart.setText(_dateTransferFormat.format(_selectedDateStart));
            try {
                Date dateStart = _dateTransferFormat.parse(tvDateStart.getText().toString());
                Calendar calStart = Calendar.getInstance();
                if (dateStart != null) calStart.setTime(dateStart);

                Date dateEnd = _dateTransferFormat.parse(tvDateEnd.getText().toString());
                Calendar calEnd = Calendar.getInstance();
                if (dateEnd != null) calEnd.setTime(dateEnd);

                if (calStart.after(calEnd)) {
                    tvDateEnd.setText(_dateTransferFormat.format(_selectedDateStart));
                    _selectedDateEnd = _selectedDateStart;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

        } else if (clickType == Constants.CLICK_END_DATE) {
            _selectedDateEnd = setSelDate(_selectedDateEnd, year, month, day);
            tvDateEnd.setText(_dateTransferFormat.format(_selectedDateEnd));

        } else {
            _selectedDate = setSelDate(_selectedDate, year, month, day);
            tvDate.setText(_dateTransferFormat.format(_selectedDate));
        }
    }

    private int clickType = -1;

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.layout_root:
                Utils.hideKeyboard(mContext, etTitle, etContent);
                break;
            case R.id.tv_content_date:
                clickType = Constants.CLICK_DEFAULT_DATE;
                showDatePicker();
                break;
            case R.id.tv_content_date_start:
                clickType = Constants.CLICK_START_DATE;
                showDatePicker();
                break;
            case R.id.tv_content_date_end:
                clickType = Constants.CLICK_END_DATE;
                showDatePicker();
                break;
            case R.id.btn_append_recipient: //수신인 추가
                navigate2AppendRecipientActivity();
                break;
            case R.id.layout_send_sms:  //알림 미허용 시 SMS 전달
                cbSendSMS.setChecked(!cbSendSMS.isChecked());
                break;
        }
    }
    private boolean checkValid() {
        if(_selectedACA == null) {   //캠퍼스 선택
            Toast.makeText(mContext, R.string.error_message_unselected_campus, Toast.LENGTH_SHORT).show();
            return false;
        }
        if(_selectedGrade == null) {   //대상
            Toast.makeText(mContext, R.string.msg_empty_school_grade, Toast.LENGTH_SHORT).show();
            return false;
        }
        if(TextUtils.isEmpty(etTitle.getText())) {   //제목
            showKeyboard(mContext, etTitle);
            Toast.makeText(mContext, R.string.error_message_empty_subject, Toast.LENGTH_SHORT).show();
            return false;
        }
//        if(TextUtils.isEmpty(etContent.getText())) {   //내용 (선택)
//            Toast.makeText(mContext, R.string.error_message_empty_content, Toast.LENGTH_SHORT).show();
//            return false;
//        }

        if (boardEditMode == Constants.BoardEditMode.New) {
            if(TextUtils.isEmpty(tvDateStart.getText())) {   //날짜
                Toast.makeText(mContext, R.string.error_message_empty_date_start, Toast.LENGTH_SHORT).show();
                return false;
            } else if(TextUtils.isEmpty(tvDateEnd.getText())) {   //날짜
                Toast.makeText(mContext, R.string.error_message_empty_date_end, Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            if(TextUtils.isEmpty(tvDate.getText())) {   //날짜
                Toast.makeText(mContext, R.string.error_message_empty_date, Toast.LENGTH_SHORT).show();
                return false;
            }
        }


        return true;
    }
    private void requestUpdateRecipient(ArrayList<Integer> seqs, int currentPage, int totalPage, boolean prevResult) {
        LogMgr.w(TAG, "requestUpdateRecipient  " + seqs + " /" + currentPage + " / " +  prevResult);
        RecipientRequest request = new RecipientRequest();
        request.seqs = seqs;
        if(totalPage > 0) {
            if(currentPage < totalPage) {
                request.receiverList = _recipientList.subList(currentPage * Constants.MAX_RECIPIENT_COUNT, (currentPage + 1) * Constants.MAX_RECIPIENT_COUNT);
            }else{
                request.receiverList = _recipientList.subList(currentPage * Constants.MAX_RECIPIENT_COUNT, _recipientList.size());
            }
        }else {
            request.receiverList = _recipientList;
        }
        //smsSender
        request.smsSender = _selectedACA.acaTel;

        if (RetrofitClient.getInstance() != null){
            int finalCurrentPage = currentPage;
            RetrofitClient.getLongTimeApiInterface().updateScheduleRecipient(request).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    try {
                        if(finalCurrentPage < totalPage) {
                            requestUpdateRecipient(seqs, finalCurrentPage + 1, totalPage, prevResult && response.isSuccessful());
                        }else {
                            hideProgressDialog();
                            if(!prevResult) {
                                showMessageDialog(getString(R.string.dialog_title_error), getString(R.string.msg_fail_to_register_recipient), new View.OnClickListener(){

                                    @Override
                                    public void onClick(View view) {
                                        hideMessageDialog();
                                    }
                                }, null);
                            }else {
                                if (response.isSuccessful()) {
                                    Intent intent = getIntent();
                                    intent.putExtra(IntentParams.PARAM_BOARD_ADDED, true);
                                    setResult(RESULT_OK, intent);
                                    finish();
                                } else {
                                    Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }catch (Exception e){
                        LogMgr.e(TAG + "requestBoardList() Exception : ", e.getMessage());
                    }finally{

                    }
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    try {
                        Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                        LogMgr.e(TAG, "requestBoardList() onFailure >> " + t.getMessage());
                    }catch (Exception e){
                    }finally{
                        hideProgressDialog();
                    }

                }
            });
        }
    }
    private void requestUpdateSchedule() {
        if(RetrofitClient.getInstance() != null) {

            showProgressDialog();
            if(boardEditMode == Constants.BoardEditMode.New) {
                RetrofitClient.getApiInterface().insertSchedule(_currentData).enqueue(new Callback<ScheduleRegisterResponse>(){

                    @Override
                    public void onResponse(Call<ScheduleRegisterResponse> call, Response<ScheduleRegisterResponse> response) {
//                        hideProgressDialog();
                        if(response.isSuccessful()) {
                            ArrayList<Integer> ptSeq = new ArrayList<>();
                            if(response.body() != null) {
                                ptSeq =  response.body().data;
                            }
                            if(ptSeq != null && ptSeq.size() > 0) {
                                int totalPage = (_recipientList.size() / Constants.MAX_RECIPIENT_COUNT);
                                int currentPage = 0;
                                requestUpdateRecipient(ptSeq, currentPage, totalPage, true);
                            }else{
                                showMessageDialog(getString(R.string.dialog_title_error), getString(R.string.msg_fail_to_register_recipient), new View.OnClickListener(){
                                            @Override
                                            public void onClick(View view) {
                                                hideMessageDialog();
                                                Intent intent = getIntent();
                                                intent.putExtra(IntentParams.PARAM_BOARD_ADDED, true);
                                                setResult(RESULT_OK, intent);
                                                finish();
                                            }
                                        }
                                        , null);
                            }
                        }else{
                            hideProgressDialog();
                            Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ScheduleRegisterResponse> call, Throwable t) {
                        hideProgressDialog();
                        Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                    }
                });
            }else if(boardEditMode == Constants.BoardEditMode.Edit) {
                RetrofitClient.getApiInterface().updateSchedule(_currentData).enqueue(new Callback<BaseResponse>(){
                    @Override
                    public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                        hideProgressDialog();
                        if(response.isSuccessful()) {
                            Intent intent = getIntent();
                            intent.putExtra(IntentParams.PARAM_BOARD_EDITED, true);
                            setResult(RESULT_OK, intent);
                            finish();
                        }else{
                            Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponse> call, Throwable t) {
                        hideProgressDialog();
                        Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

//    private void requestGradeList(String acaCode){
//        if(RetrofitClient.getInstance() != null) {
//            RetrofitClient.getApiInterface().getStudentGradeList(acaCode).enqueue(new Callback<StudentGradeListResponse>() {
//                @Override
//                public void onResponse(Call<StudentGradeListResponse> call, Response<StudentGradeListResponse> response) {
//                    if(response.isSuccessful()) {
//
//                        if(response.body() != null) {
//                            List<StudentGradeData> list = response.body().data;
//                            if(_GradeList != null) _GradeList.clear();
//                            _GradeList.addAll(list);
//                            Collections.sort(_GradeList, new Comparator<StudentGradeData>() {
//                                @Override
//                                public int compare(StudentGradeData schoolData, StudentGradeData t1) {
//                                    return Integer.compare(schoolData.gubunCode, t1.gubunCode);
//                                }
//                            });
//                            _handler.sendEmptyMessage(CMD_GET_GRADE_LIST);
//                        }
//                    } else {
//
//                        try {
//                            LogMgr.e(TAG, "requestACAList() errBody : " + response.errorBody().string());
//                        } catch (IOException e) {
//                        }
//
//                    }
//
//                }
//
//                @Override
//                public void onFailure(Call<StudentGradeListResponse> call, Throwable t) {
//                    LogMgr.e(TAG, "requestACAList() onFailure >> " + t.getMessage());
////                    _handler.sendEmptyMessage(CMD_GET_GRADE_LIST);
//                }
//            });
//        }
//    }
    /**
     * 부서 목록 요청
     * @param acaCode 캠퍼스 코드
     */
    private void requestGradeList(String acaCode, boolean selectable) {

        if (RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getStudentGradeList(acaCode).enqueue(new Callback<StudentGradeListResponse>() {
                @Override
                public void onResponse(Call<StudentGradeListResponse> call, Response<StudentGradeListResponse> response) {
                    try {
                        if (response.isSuccessful()) {

                            if (response.body() != null) {

                                List<StudentGradeData> getData = response.body().data;
                                if (getData != null) {
                                    _GradeList = getData;
                                    Message msg = _handler.obtainMessage(CMD_GET_GRADE_LIST);
                                    msg.obj = selectable;
                                    _handler.sendMessage(msg);
                                } else LogMgr.e(TAG + " DetailData is null");
                            }
                        } else {
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestBoardDetail() Exception : ", e.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<StudentGradeListResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "requestBoardDetail() onFailure >> " + t.getMessage());
                    } catch (Exception e) {
                    }
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
//    private void updateSpinnerList(PowerSpinnerView powerSpinner, List<String> newList) {
//        powerSpinner.setItems(newList);
//        PopupWindow popupWindow = powerSpinner.getSpinnerWindow();
//        if (popupWindow != null) {
//            int itemCount = newList.size();
//            int maxHeight = calculatePopupMaxHeight(itemCount);
//            popupWindow.setHeight(maxHeight);
//        }
//    }
//
//    private int calculatePopupMaxHeight(int itemCount) {
//        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
//        int screenHeight = displayMetrics.heightPixels;
//        int maxItemHeight = getResources().getDimensionPixelSize(R.dimen.spinner_item_height); // spinner에서 설정한 item 의 높이
//        int maxHeight = Math.min(screenHeight * 4 / 5, maxItemHeight * itemCount);
//
//        return maxHeight;
//    }

    public static int calculateDuration(Date startDate, Date endDate) {
        //return (int) endDate.getTime() - (int) startDate.getTime() / (24 * 60 * 60 * 1000) + 1;
        return (int) Math.abs(endDate.getTime() - startDate.getTime()) / (24 * 60 * 60 * 1000) + 1;
    }
    public void navigate2AppendRecipientActivity() {
        Intent appendIntent = new Intent(mContext, AppendRecipientActivity.class);
        appendIntent.putExtra(IntentParams.PARAM_STU_ACACODE, PreferenceUtil.getAcaCode(mContext));
        appendIntent.putExtra(IntentParams.PARAM_RECIPIENT_FILTERTYPE, Constants.RecipientFilterType.TYPE_SCHOOL);
        recipientResultLauncher.launch(appendIntent);
    }
}