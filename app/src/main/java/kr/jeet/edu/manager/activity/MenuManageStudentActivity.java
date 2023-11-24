package kr.jeet.edu.manager.activity;

import android.os.Bundle;

import kr.jeet.edu.manager.R;
import android.content.Intent;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;
import com.skydoves.powerspinner.OnSpinnerOutsideTouchListener;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import kr.jeet.edu.manager.adapter.RecipientListAdapter;
import kr.jeet.edu.manager.adapter.SchoolListAdapter;
import kr.jeet.edu.manager.adapter.WrapContentSpinnerAdapter;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.DataManager;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.dialog.SchoolListBottomSheetDialog;
import kr.jeet.edu.manager.dialog.SendSMSDialog;
import kr.jeet.edu.manager.model.data.ACAData;
import kr.jeet.edu.manager.model.data.ClassData;
import kr.jeet.edu.manager.model.data.ClstData;
import kr.jeet.edu.manager.model.data.DepartmentData;
import kr.jeet.edu.manager.model.data.RecipientData;
import kr.jeet.edu.manager.model.data.RecipientStudentData;
import kr.jeet.edu.manager.model.data.SchoolData;
import kr.jeet.edu.manager.model.request.SmsRequest;
import kr.jeet.edu.manager.model.response.BaseResponse;
import kr.jeet.edu.manager.model.response.GetClstListResponse;
import kr.jeet.edu.manager.model.response.GetDeptListResponse;
import kr.jeet.edu.manager.model.response.GetRecipientStudentResponse;
import kr.jeet.edu.manager.model.response.StringResponse;
import kr.jeet.edu.manager.server.RetrofitApi;
import kr.jeet.edu.manager.server.RetrofitClient;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.PreferenceUtil;
import kr.jeet.edu.manager.utils.Utils;
import kr.jeet.edu.manager.view.CustomAppbarLayout;
import kr.jeet.edu.manager.view.ClearableTextView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuManageStudentActivity extends BaseActivity {
    private static final String TAG = "manage";
    private Constants.RecipientFilterType _filterType = Constants.RecipientFilterType.TYPE_SCHOOL;
    private static final int CMD_GET_ACA_LIST = 0;
    private static final int CMD_GET_DEPT_LIST = 1;
    private static final int CMD_GET_CLST_LIST = 2;
    private static final int CMD_SEARCH = 4;
    private static final int CMD_NOTIFY_DATASET_CHANGED = 5;
    private static final int CMD_INIT_LAYOUT = 6;
    SimpleDateFormat _dateTransferFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYY_MM_DD);
    //region define views
    PowerSpinnerView spinnerCampus, spinnerDept, spinnerClst, spinnerClass, spinnerStudentType;
    TextView tvCalendar;
    ClearableTextView tvSchool;
    EditText etSearch;
    TextView tvSearchBtn, tvSelectCount, tvEmptyLayout;
    MaterialCheckBox cbTotal, cbParentTotal, cbStudentTotal;
    SwipeRefreshLayout _swipeRefreshLayout;
    RecyclerView _recyclerRecipient;
    RecipientListAdapter _adapterRecipient;
    LinearLayout layoutCheckBoxTotal, layoutCheckBoxStudentTotal, layoutCheckBoxParentTotal;
    //endregion
    //region define datas
    private RetrofitApi _retrofitApi;
//    Date _selectedDate = new Date();
    List<ACAData> _ACAList = new ArrayList<>();
    List<DepartmentData> _DeptList = new ArrayList<>();
    List<ClstData> _ClstList = new ArrayList<>();
    List<ClassData> _ClassList = new ArrayList<>();
    //    List<String> _ACANameList = new ArrayList<>();
//    ArrayList<RecipientData> _recipientList = new ArrayList<>();
    List<RecipientStudentData> _recipientStudentList = new ArrayList<>();
    ArrayList<RecipientData> _recipientList;
    ACAData _selectedACA = null;
    DepartmentData _selectedDept = null;
    ClstData _selectedClst = null;
    ClassData _selectedClass = null;
    Constants.StudentType _selectedStudentType = Constants.StudentType.ENROLLED;
    SchoolData _selectedSchoolData = null;
    //for bottomSheet
    SchoolListBottomSheetDialog _schoolListBottomSheetDialog;
    SchoolListAdapter _schoolListAdapter;
    List<SchoolData> _schoolList;
    //이름 검색 시 filter가 변한 경우에만 서버로 요청하기 위한 flag
    boolean isFilterTriggerChanged = false;
    String _acaCode = "";
    int _userGubun = 1;
    int _seq = 0;
    int _sfCode = 0;
    String searchWord = "";
    Menu _menu;
    //endregion
    Constants.BoardEditMode _boardMode = Constants.BoardEditMode.Show;
    SendSMSDialog _smsDialog;
    String _installSMSContent = "";
    private Handler _handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {

                case CMD_GET_ACA_LIST:
                    break;
                case CMD_GET_DEPT_LIST:
                    if (_DeptList != null && !_DeptList.isEmpty()) {
                        spinnerDept.setEnabled(true);
//                        spinnerDept.setItems(_DeptList.stream().map(t -> t.deptName).collect(Collectors.toList()));

                    }else{
                        spinnerDept.setEnabled(false);
                    }
                    Utils.updateSpinnerList(spinnerDept, _DeptList.stream().map(t -> t.deptName).collect(Collectors.toList()));
                    break;
                case CMD_GET_CLST_LIST:
                    if (_ClstList != null && !_ClstList.isEmpty()) {
                        spinnerClst.setEnabled(true);
//                        spinnerClst.setItems(_ClstList.stream().map(t -> t.clstName).collect(Collectors.toList()));

                    }else{
                        spinnerClst.setEnabled(false);
                    }
                    Utils.updateSpinnerList(spinnerClst, _ClstList.stream().map(t -> t.clstName).collect(Collectors.toList()));
                    break;
//                case CMD_GET_CLASS_LIST:
//                    if (_ClassList != null) {
//                        spinnerClass.setEnabled(true);
////                        spinnerClass.setItems(_ClassList.stream().map(t -> t.clsName).collect(Collectors.toList()));
//                        updateSpinnerList(spinnerClass, _ClassList.stream().map(t -> t.clsName).collect(Collectors.toList()));
//                    }
//                    break;
                case CMD_SEARCH:
                    if(isFilterTriggerChanged) {
                        LogMgr.e(TAG, "triggerChanged new request");
                        cbParentTotal.setChecked(false);
                        cbStudentTotal.setChecked(false);
                        cbTotal.setChecked(false);
                        int deptCode = 0;
                        int clstCode = 0;
                        int clsCode = 0;
                        try {
//                        String date = outputDateFormat.format(_selectedDate);
//                            String apiDate = apiDateFormat.format(_selectedDate);
                            if (_selectedDept != null) deptCode = _selectedDept.deptCode;
                            if (_selectedClst != null) clstCode = _selectedClst.clstCode;
                            if (_selectedClass != null) clsCode = _selectedClass.clsCode;
                            showProgressDialog();

                            requestRecipientList(_selectedACA.acaCode, deptCode, clstCode);
                        } catch (Exception ex) {
                        }
                    }else{
                        String searchStr = "";
                        if(etSearch != null && etSearch.getText() != null) {
                            searchStr = etSearch.getText().toString().trim();
                        }
                        searchFilter(searchStr);
                    }
                    break;
                case CMD_NOTIFY_DATASET_CHANGED:
                    if(_adapterRecipient != null) {
                        _adapterRecipient.notifyDataSetChanged();
                    }
                    break;
                case CMD_INIT_LAYOUT:
                    int arg = msg.arg1;
                    Constants.BoardEditMode mode = Constants.BoardEditMode.Show;
                    try{
                        mode = Constants.BoardEditMode.values()[arg];
                        _boardMode = mode;
                        initLayout(mode);
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }
                    break;
            }
        }
    };
    View.OnClickListener okListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            showProgressDialog();
            int totalPage = (_recipientList.size() / Constants.MAX_RECIPIENT_COUNT);
            int currentPage = 0;
            requestSendSMS(currentPage, totalPage, true);
        }
    };
    View.OnClickListener cancelListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            hideSendSMSDialog();
        }
    };
    SendSMSDialog.UpdateContentInterface updatedListener = new SendSMSDialog.UpdateContentInterface() {
        @Override
        public void onUpdatedContents(String content) {
            LogMgr.w(TAG, "updated  = " + content);
            _installSMSContent = content;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_append_recipient);
        mContext = this;
        _userGubun = PreferenceUtil.getUserGubun(this);
        _seq = PreferenceUtil.getUserSeq(this);
        _sfCode = PreferenceUtil.getUserSFCode(this);
        _acaCode = PreferenceUtil.getAcaCode(this);
//        initData();
        initView();
        initAppbar();
        requestAppInstallMsg();
        Message msg = _handler.obtainMessage(CMD_INIT_LAYOUT);
        msg.arg1 = Constants.BoardEditMode.Show.ordinal();
        _handler.sendMessage(msg);
        setAnimMove(Constants.MOVE_DOWN);
    }
//    void initData() {
//        Intent intent = getIntent();
//        if(intent != null){
//            if(intent.hasExtra(IntentParams.PARAM_STU_ACACODE)) {
//                _acaCode = intent.getStringExtra(IntentParams.PARAM_STU_ACACODE);
//            }
//            if(intent.hasExtra(IntentParams.PARAM_RECIPIENT_FILTERTYPE)) {
//                _filterType = (Constants.RecipientFilterType) intent.getSerializableExtra(IntentParams.PARAM_RECIPIENT_FILTERTYPE);
//            }
//
//        }
//    }
    void initLayout(Constants.BoardEditMode mode) {
        LogMgr.d(TAG, "initLayout " + mode.name());
        switch(mode) {
            case Show:  //조회모드
                _adapterRecipient.setEditMode(false);
                cbParentTotal.setChecked(false);
                cbStudentTotal.setChecked(false);
                cbTotal.setChecked(false);
                if(layoutCheckBoxTotal != null) layoutCheckBoxTotal.setVisibility(View.GONE);
                if(cbParentTotal != null) cbParentTotal.setVisibility(View.INVISIBLE);
                if(cbStudentTotal != null) cbStudentTotal.setVisibility(View.INVISIBLE);

                if(_swipeRefreshLayout != null) {
                    _swipeRefreshLayout.setEnabled(true);
                }
                if(_adapterRecipient != null) {
//                    _adapterRecipient.notifyDataSetChanged();
                    _adapterRecipient.notifyItemRangeChanged(0, _recipientStudentList.size());
                }
                invalidateOptionsMenu();    //onCreateOptionsMenu 호출됨
                break;
            case Edit:
                _adapterRecipient.setEditMode(true);
                _recipientStudentList.stream().forEach(t->{
                    t.isCheckStudent = false;
                    t.isCheckParent = false;
                });
                cbParentTotal.setChecked(false);
                cbStudentTotal.setChecked(false);
                cbTotal.setChecked(false);
                if(layoutCheckBoxTotal != null) layoutCheckBoxTotal.setVisibility(View.VISIBLE);
                if(cbParentTotal != null) cbParentTotal.setVisibility(View.VISIBLE);
                if(cbStudentTotal != null) cbStudentTotal.setVisibility(View.VISIBLE);

                if(_swipeRefreshLayout != null) {
                    _swipeRefreshLayout.setEnabled(false);
                }
                if(_adapterRecipient != null) {
//                    _adapterRecipient.notifyDataSetChanged();
                    _adapterRecipient.notifyItemRangeChanged(0, _recipientStudentList.size());
                }
                invalidateOptionsMenu();    //onCreateOptionsMenu 호출됨
                break;
            default:
                break;
        }
    }
    @Override
    void initView() {
        //region button
        tvSearchBtn = findViewById(R.id.tv_search_btn);
        tvSearchBtn.setOnClickListener(this);
        //endregion
        tvSchool = findViewById(R.id.tv_content_school);
//        tvSchool.setOnClickListener(this);
        tvSchool.setCloseClickListener(new ClearableTextView.onTextViewClickListener() {
            @Override
            public void onContentClick() {
                if(_schoolListBottomSheetDialog == null) {
                    _schoolListBottomSheetDialog = new SchoolListBottomSheetDialog(_schoolListAdapter);
                }
                _schoolListBottomSheetDialog.show(getSupportFragmentManager(), TAG);
            }

            @Override
            public void onDeleteClick() {
                LogMgr.e(TAG, "onDeleteClick");
                isFilterTriggerChanged = true;
                _selectedSchoolData = null;
                tvSchool.setText("");
            }
        });
        //region calendar
        tvCalendar = findViewById(R.id.tv_content_date);
        tvCalendar.setOnClickListener(this);
        etSearch = findViewById(R.id.et_search);
        tvEmptyLayout = findViewById(R.id.tv_empty_list);
//        Calendar cal = Calendar.getInstance();
//        _selectedDate = cal.getTime();
//        try {
//            String output = outputDateFormat.format(_selectedDate);
//            tvCalendar.setText(output);
//        }catch(Exception ex){}
        //endregion
        //region spinner campus
        spinnerCampus = findViewById(R.id.spinner_campus);
        if(_ACAList != null) _ACAList.clear();
        _ACAList.addAll(DataManager.getInstance().getACAListMap().values());
        if(_ACAList != null) {
            spinnerCampus.setItems(_ACAList.stream().map(t -> t.acaName).collect(Collectors.toList()));
        }
        {
            WrapContentSpinnerAdapter adapter = new WrapContentSpinnerAdapter(mContext, _ACAList.stream().map(t -> t.acaName).collect(Collectors.toList()), spinnerCampus);
            spinnerCampus.setSpinnerAdapter(adapter);
        }

        spinnerCampus.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener<String>() {
            @Override
            public void onItemSelected(int oldIndex, @Nullable String oldItem, int newIndex, String newItem) {
                LogMgr.e(newItem + " selected");
                if(oldItem != null && oldItem.equals(newItem)) return;
                isFilterTriggerChanged = true;
                ACAData selectedData = null;
                Optional optional = _ACAList.stream().filter(t -> t.acaName == newItem).findFirst();
                if(optional.isPresent()) {
                    selectedData = (ACAData) optional.get();
                }
                _selectedACA = selectedData;
                LogMgr.w("selectedACA = " + _selectedACA.acaCode + " / " + _selectedACA.acaName);
                requestDeptList(_selectedACA.acaCode);
                if(_selectedDept != null) {
                    _selectedDept = null;
                }
                if(spinnerDept != null) spinnerDept.clearSelectedItem();

                if(_selectedClst != null) {
                    _selectedClst = null;
                    spinnerClst.clearSelectedItem();
                }
                if(spinnerClst != null) spinnerClst.setEnabled(false);

                if(_selectedClass != null) {
                    _selectedClass = null;
                    spinnerClass.clearSelectedItem();
                }
                if(spinnerClass != null) spinnerClass.setEnabled(false);
            }
        });
        spinnerCampus.setSpinnerOutsideTouchListener(new OnSpinnerOutsideTouchListener() {
            @Override
            public void onSpinnerOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                spinnerCampus.dismiss();
            }
        });
        spinnerCampus.setLifecycleOwner(this);
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
        //endregion
        //region spinner dept
        spinnerDept = findViewById(R.id.spinner_dept);
        spinnerDept.setEnabled(false);
        {
            WrapContentSpinnerAdapter adapter = new WrapContentSpinnerAdapter(mContext, _DeptList.stream().map(t -> t.deptName).collect(Collectors.toList()), spinnerDept);
            spinnerDept.setSpinnerAdapter(adapter);
        }

        spinnerDept.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener<String>() {
            @Override
            public void onItemSelected(int oldIndex, @Nullable String oldItem, int newIndex, String newItem) {
                LogMgr.e(newItem + " selected");
                if(oldItem != null && oldItem.equals(newItem)) return;
                isFilterTriggerChanged = true;
                DepartmentData selectedData = null;
//                if(newItem == getString(R.string.item_total)) { //전체 선택
//                    selectedData = new ACAData(newItem, "");
//                }else {
                selectedData = _DeptList.stream().filter(t -> t.deptName.equals(newItem)).findFirst().get();
//                }
                _selectedDept = selectedData;
                LogMgr.w("selectedDept = " + _selectedDept.deptCode + " / " + _selectedDept.deptName);

                if(_selectedClst != null) {
                    _selectedClst = null;
                }
                if(spinnerClst != null) spinnerClst.clearSelectedItem();
                if(newItem == getString(R.string.item_total)) { //전체 선택
                    if(spinnerClst != null) spinnerClst.setEnabled(false);
                }else {
                    requestClstList(_selectedACA.acaCode, _selectedDept.deptCode);
                }
                if(_selectedClass != null) {
                    _selectedClass = null;
                    spinnerClass.clearSelectedItem();
                }
                if(spinnerClass != null) spinnerClass.setEnabled(false);


            }
        });
        spinnerDept.setSpinnerOutsideTouchListener(new OnSpinnerOutsideTouchListener() {
            @Override
            public void onSpinnerOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                spinnerDept.dismiss();
            }
        });
        spinnerDept.setLifecycleOwner(this);
        //endregion
        //region spinner clst
        spinnerClst = findViewById(R.id.spinner_clst);
        {
            WrapContentSpinnerAdapter adapter = new WrapContentSpinnerAdapter(mContext, _ClstList.stream().map(t -> t.clstName).collect(Collectors.toList()), spinnerClst);
            spinnerClst.setSpinnerAdapter(adapter);
        }

        spinnerClst.setEnabled(false);
        spinnerClst.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener<String>() {
            @Override
            public void onItemSelected(int oldIndex, @Nullable String oldItem, int newIndex, String newItem) {
                LogMgr.e(newItem + " selected");
                if(oldItem != null && oldItem.equals(newItem)) return;
                isFilterTriggerChanged = true;
                ClstData selectedData = null;
//                if(newItem == getString(R.string.item_total)) { //전체 선택
//                    selectedData = new ACAData(newItem, "");
//                }else {
                selectedData = _ClstList.stream().filter(t -> t.clstName.equals(newItem)).findFirst().get();
//                }
                _selectedClst = selectedData;
                LogMgr.w("selectedClst = " + _selectedClst.clstCode + " / " + _selectedClst.clstName);
//                if(_filterType.equals(Constants.RecipientFilterType.TYPE_CLASS)) {
//                    try {
//                        String apiDate = apiDateFormat.format(_selectedDate);
//                        requestClassList(_selectedACA.acaCode, _selectedDept.deptCode, _selectedClst.clstCode, apiDate);
//                    } catch (Exception ex) {
//                    }
//                    if (_selectedClass != null) {
//                        _selectedClass = null;
//                        if (spinnerClass != null) spinnerClass.clearSelectedItem();
//                    }
//                }

            }
        });
        spinnerClst.setSpinnerOutsideTouchListener(new OnSpinnerOutsideTouchListener() {
            @Override
            public void onSpinnerOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                spinnerClst.dismiss();
            }
        });
        spinnerClst.setLifecycleOwner(this);
        //endregion
        //region spinner class
        spinnerClass = findViewById(R.id.spinner_cls);
        {
            WrapContentSpinnerAdapter adapter = new WrapContentSpinnerAdapter(mContext, _ClassList.stream().map(t -> t.clsName).collect(Collectors.toList()), spinnerClass);
            spinnerClass.setSpinnerAdapter(adapter);
        }
        spinnerClass.setEnabled(false);
        spinnerClass.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener<String>() {
            @Override
            public void onItemSelected(int oldIndex, @Nullable String oldItem, int newIndex, String newItem) {
                LogMgr.e(newItem + " selected");
                if(oldItem != null && oldItem.equals(newItem)) return;
                isFilterTriggerChanged = true;
                ClassData selectedData = null;
//                if(newItem == getString(R.string.item_total)) { //전체 선택
//                    selectedData = new ACAData(newItem, "");
//                }else {
                selectedData = _ClassList.stream().filter(t -> t.clsName.equals(newItem)).findFirst().get();
//                }
                _selectedClass = selectedData;
                LogMgr.w("selectedClass = " + _selectedClass.clsCode + " / " + _selectedClass.clsName);
            }
        });
        spinnerClass.setSpinnerOutsideTouchListener(new OnSpinnerOutsideTouchListener() {
            @Override
            public void onSpinnerOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                spinnerClass.dismiss();
            }
        });
        spinnerClass.setLifecycleOwner(this);
        //endregion
        //region spinner studentType
        spinnerStudentType = findViewById(R.id.spinner_student_type);
        {
            WrapContentSpinnerAdapter adapter = new WrapContentSpinnerAdapter(mContext, Constants.StudentType.getNameList(), spinnerStudentType);
            spinnerStudentType.setSpinnerAdapter(adapter);
        }

        spinnerStudentType.setEnabled(true);
        List<String> studentTypeList = Constants.StudentType.getNameList();
        spinnerStudentType.setItems(studentTypeList);
        spinnerStudentType.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener<String>() {
            @Override
            public void onItemSelected(int oldIndex, @Nullable String oldItem, int newIndex, String newItem) {
                LogMgr.e(newItem + " selected");
                if(oldItem != null && oldItem.equals(newItem)) return;
                isFilterTriggerChanged = true;
                _selectedStudentType = Constants.StudentType.getByName(newItem);            }
        });
        spinnerStudentType.setSpinnerOutsideTouchListener(new OnSpinnerOutsideTouchListener() {
            @Override
            public void onSpinnerOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                spinnerStudentType.dismiss();
            }
        });
        spinnerStudentType.setLifecycleOwner(this);
        if (studentTypeList != null && studentTypeList.size() > 0) {    //default 재원생
            int index = studentTypeList.indexOf(Constants.StudentType.ENROLLED.getNameKor());
            if(index > 0) spinnerStudentType.selectItemByIndex(index);
        }
        //endregion
        //region recyclerview
        _swipeRefreshLayout = findViewById(R.id.refresh_layout);
        _swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isFilterTriggerChanged = true;
                _handler.sendEmptyMessage(CMD_SEARCH);
            }
        });

        _recyclerRecipient = findViewById(R.id.recyclerview_recipient);
        _adapterRecipient = new RecipientListAdapter(mContext, _recipientStudentList, new RecipientListAdapter.onItemClickListener() {
            @Override
            public void onItemClick(RecipientStudentData item) {
                //todo 상세화면 연결
                navigate2DetailActivity(item);
            }

            @Override
            public void onCheckedParent(int position, boolean b) {
                if(!b) {
                    LogMgr.e(TAG, "onCheckedParent in adapter " + b);
                    cbParentTotal.setChecked(false);
                }else {
                    if (_adapterRecipient._filteredlist.stream().allMatch(t -> t.isCheckParent == true|| TextUtils.isEmpty(t.parentPhoneNumber) || "Y".equals(t.parentInstall))) {
                        LogMgr.e(TAG, "onCheckedParent in allmatch true");
                        cbParentTotal.setChecked(true);
                    } else {
                        LogMgr.e(TAG, "onCheckedParent in  allmatch false");
                        cbParentTotal.setChecked(false);
                    }
                }
                cbTotal.setChecked(cbStudentTotal.isChecked() && cbParentTotal.isChecked());
            }

            @Override
            public void onCheckedStudent(int position, boolean b) {
                if(!b) {
                    cbStudentTotal.setChecked(false);
                }else {
                    if (_adapterRecipient._filteredlist.stream().allMatch(t -> t.isCheckStudent == true|| TextUtils.isEmpty(t.stPhoneNumber) || "Y".equals(t.stInstall))) {
                        cbStudentTotal.setChecked(true);
                    } else {
                        cbStudentTotal.setChecked(false);
                    }
                }
                cbTotal.setChecked(cbStudentTotal.isChecked() && cbParentTotal.isChecked());
            }

            @Override
            public void onFilteringCompleted() {
                if(checkEmptyRecyclerView()){

                }else {
                    LogMgr.e(TAG, "itemcount > 0");
                    if (_adapterRecipient._filteredlist.stream().allMatch(t -> t.isCheckParent|| TextUtils.isEmpty(t.parentPhoneNumber) || "Y".equals(t.parentInstall))) {
                        LogMgr.e(TAG, "searchFilter in allmatch true");
                        cbParentTotal.setChecked(true);
                    } else {
                        LogMgr.e(TAG, "searchFilter in allmatch false");
                        cbParentTotal.setChecked(false);
                    }
                    if (_adapterRecipient._filteredlist.stream().allMatch(t -> t.isCheckStudent|| TextUtils.isEmpty(t.stPhoneNumber) || "Y".equals(t.stInstall))) {
                        cbStudentTotal.setChecked(true);
                    } else {
                        cbStudentTotal.setChecked(false);
                    }
                    if(cbStudentTotal.isChecked() && cbParentTotal.isChecked()){
                        cbTotal.setChecked(true);
                    }else{
                        cbTotal.setChecked(false);
                    }
                }
            }
        });
        _adapterRecipient.setIgnoreInstalled(true); // 설치된 폰은 무시
        _recyclerRecipient.setAdapter(_adapterRecipient);
//        _recyclerRecipient.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        //endregion
        //region checkbox
        layoutCheckBoxTotal = findViewById(R.id.layout_total_checkbox);
        layoutCheckBoxTotal.setOnClickListener(this);
        layoutCheckBoxStudentTotal = findViewById(R.id.layout_student_checkbox);
        layoutCheckBoxStudentTotal.setOnClickListener(this);
        layoutCheckBoxParentTotal = findViewById(R.id.layout_parent_checkbox);
        layoutCheckBoxParentTotal.setOnClickListener(this);
        cbTotal = findViewById(R.id.checkbox_total);
//        cbTotal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                cbStudentTotal.setChecked(b);
//                cbParentTotal.setChecked(b);
//            }
//        });
        cbStudentTotal = findViewById(R.id.checkbox_student);
//        cbStudentTotal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                LogMgr.e(TAG, "student check changed " + b);
//
//            }
//        });
        cbParentTotal = findViewById(R.id.checkbox_parent);
//        cbParentTotal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                LogMgr.e(TAG, "parent check changed " + b);
//                cbTotal.setChecked(b && cbStudentTotal.isChecked());
//                _recipientStudentList.forEach(t->t.isCheckParent = b);
//                _handler.sendEmptyMessage(CMD_NOTIFY_DATASET_CHANGED);
//            }
//        });
        //endregion
        toggleFilterLayout();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        _handler.sendEmptyMessage(CMD_SEARCH);
    }

    private void toggleFilterLayout() {
        switch(_filterType) {
            case TYPE_CLASS:
                //visibility
                spinnerStudentType.setVisibility(View.GONE);
                tvSchool.setVisibility(View.GONE);
                break;
            case TYPE_SCHOOL:
                spinnerClass.setVisibility(View.GONE);
                tvCalendar.setVisibility(View.GONE);
                _schoolList = new ArrayList<>();
                _schoolList.addAll(DataManager.getInstance().getSchoolListMap().values());
                Collections.sort(_schoolList, new Comparator<SchoolData>() {
                    @Override
                    public int compare(SchoolData schoolData, SchoolData t1) {
                        return Collator.getInstance().compare(schoolData.scName, t1.scName);
                    }
                });
                _schoolListAdapter = new SchoolListAdapter(mContext, _schoolList, new SchoolListAdapter.ItemClickListener() {
                    @Override
                    public void onItemClick(SchoolData item) {
                        _selectedSchoolData = item;
                        tvSchool.setText(item.scName);
                        isFilterTriggerChanged = true;
                        if(_schoolListBottomSheetDialog != null) {
                            _schoolListBottomSheetDialog.dismiss();
                        }
                    }

                    @Override
                    public void onFilteringCompleted() {

                    }
                });
                break;
        }
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.main_menu_student_manage);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {

//            case R.id.tv_content_date:
//                LogMgr.e(TAG, "yearmonthpicker");
//                Calendar cal = Calendar.getInstance();
//                cal.setTime(_selectedDate);
//                Utils.yearMonthPicker(mContext, this, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH));
//                break;
            case R.id.tv_search_btn:
                _handler.sendEmptyMessage(CMD_SEARCH);

                break;
//            case R.id.tv_content_school:
//                if(_schoolListBottomSheetDialog == null) {
//                    _schoolListBottomSheetDialog = new SchoolListBottomSheetDialog(_schoolListAdapter);
//                }
//                _schoolListBottomSheetDialog.show(getSupportFragmentManager(), TAG);
//                break;
            case R.id.layout_total_checkbox:
                boolean toDo = !cbTotal.isChecked();
                cbTotal.setChecked(toDo);
                cbStudentTotal.setChecked(toDo);
                LogMgr.e(TAG, "total checkbox clicked");
                cbParentTotal.setChecked(toDo);
                for(RecipientStudentData data : _adapterRecipient._filteredlist){
                    if(!TextUtils.isEmpty(data.stPhoneNumber) && ("N".equals(data.stInstall))) {
                        data.isCheckStudent = toDo;
                    }
                    if(!TextUtils.isEmpty(data.parentPhoneNumber) && ("N".equals(data.parentInstall))) {
                        data.isCheckParent = toDo;
                    }
                }
                _handler.sendEmptyMessage(CMD_NOTIFY_DATASET_CHANGED);
                break;
            case R.id.layout_student_checkbox: {
                cbStudentTotal.setChecked(!cbStudentTotal.isChecked());
                boolean todo = cbStudentTotal.isChecked();
                cbTotal.setChecked(todo && cbParentTotal.isChecked());
                _adapterRecipient._filteredlist.forEach(t -> t.isCheckStudent = todo && !TextUtils.isEmpty(t.stPhoneNumber) && ("N".equals(t.stInstall)));
                _handler.sendEmptyMessage(CMD_NOTIFY_DATASET_CHANGED);
            }
            break;
            case R.id.layout_parent_checkbox: {
                LogMgr.e(TAG, "parent checkbox clicked");
                cbParentTotal.setChecked(!cbParentTotal.isChecked());
                boolean todo = cbParentTotal.isChecked();
                cbTotal.setChecked(todo && cbStudentTotal.isChecked());
                _adapterRecipient._filteredlist.forEach(t -> t.isCheckParent = todo && !TextUtils.isEmpty(t.parentPhoneNumber) && ("N".equals(t.parentInstall)));
                _handler.sendEmptyMessage(CMD_NOTIFY_DATASET_CHANGED);
            }
            break;

        }
    }
    private boolean checkValid() {
        //todo make check Valid
        if(_recipientStudentList.stream().filter(t->t.isCheckStudent || t.isCheckParent).count() == 0){
            Toast.makeText(mContext, getString(R.string.msg_check_recipient_install_sms), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
//    @Override
//    public void onDateSet(int month, int year) {
//        LogMgr.e(TAG, year + "/" + month);
//        //시간 비교
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(_selectedDate);
//        //설정되어 있던 시간과 다른 경우만 filterTriggerchanged 변경
//        if(year == cal.get(Calendar.YEAR) && month == cal.get(Calendar.MONTH)){
//            return;
//        }
//
//        isFilterTriggerChanged = true;
//        cal.set(Calendar.YEAR, year);
//        cal.set(Calendar.MONTH, month);
//        cal.set(Calendar.DAY_OF_MONTH, 1);
//        _selectedDate = cal.getTime();
//        try {
//            String output = outputDateFormat.format(_selectedDate);
//            String apiDate = apiDateFormat.format(_selectedDate);
//            tvCalendar.setText(output);
//            if(spinnerClass != null && spinnerClass.isEnabled()) {
//                requestClassList(_selectedACA.acaCode, _selectedDept.deptCode, _selectedClst.clstCode, apiDate);
//
//                if (_selectedClass != null) {
//                    _selectedClass = null;
//                    spinnerClass.clearSelectedItem();
//                }
//            }
//        }catch(Exception ex){}
//
//
//    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(_boardMode == Constants.BoardEditMode.Show) {
            getMenuInflater().inflate(R.menu.menu_manage_student, menu);
            int positionOfMenuItem = 0;
            try {
                MenuItem item = menu.getItem(positionOfMenuItem);
                SpannableString span = new SpannableString(item.getTitle());
                span.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.red)), 0, span.length(), 0);
                item.setTitle(span);
            }catch(Exception ex){}
        }else if(_boardMode == Constants.BoardEditMode.Edit) {
            getMenuInflater().inflate(R.menu.menu_next, menu);
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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_install_sms:
                Message msg = _handler.obtainMessage(CMD_INIT_LAYOUT);
                msg.arg1 = Constants.BoardEditMode.Edit.ordinal();
                _handler.sendMessage(msg);
                break;
            case R.id.action_next:
                if(checkValid()) {
                    showSendSMSDialog();
                    new Thread(() -> {
                        try {

                            _recipientList = DataManager.getInstance().getRecipientList();
                            if (_recipientList != null) _recipientList.clear();
//                    List<RecipientStudentData> checkedList = _recipientStudentList.stream().filter(t -> t.isCheckStudent || t.isCheckParent).collect(Collectors.toList());
                            _recipientStudentList.stream().filter(t -> t.isCheckStudent || t.isCheckParent).forEach(row -> {
                                if (row.isCheckStudent) {
                                    RecipientData recipientData = new RecipientData();
                                    recipientData.stCode = row.stCode;
                                    recipientData.stName = row.stName;
                                    recipientData.phoneNumber = row.stPhoneNumber;
                                    recipientData.userGubun = Constants.USER_TYPE_STUDENT;
                                    recipientData.acaCode = row.acaCode;
                                    recipientData.acaName = DataManager.getInstance().getACAData(row.acaCode).acaName;
                                    recipientData.isApp = row.stInstall;
                                    _recipientList.add(recipientData);
                                }
                                if (row.isCheckParent) {
                                    RecipientData recipientData = new RecipientData();
                                    recipientData.stCode = row.stCode;
                                    recipientData.stName = row.stName;
                                    recipientData.phoneNumber = row.parentPhoneNumber;
                                    recipientData.userGubun = Constants.USER_TYPE_PARENTS;
                                    recipientData.acaCode = row.acaCode;
                                    recipientData.acaName = DataManager.getInstance().getACAData(row.acaCode).acaName;
                                    recipientData.isApp = row.parentInstall;
                                    _recipientList.add(recipientData);
                                }
                            });
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(_smsDialog != null)
                                        _smsDialog.setRecipientCount(_recipientList.size());
                                }
                            });
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        } finally {
//                    hideProgressDialog();
                        }
                    }).start();

                }
//                Intent intent = new Intent();
//                showProgressDialog();
//                new Thread(() -> {
//                    try {
//
//                        ArrayList<RecipientData> recipientList = DataManager.getInstance().getRecipientList();
//                        if (recipientList != null) recipientList.clear();
////                    List<RecipientStudentData> checkedList = _recipientStudentList.stream().filter(t -> t.isCheckStudent || t.isCheckParent).collect(Collectors.toList());
//                        _recipientStudentList.stream().filter(t -> t.isCheckStudent || t.isCheckParent).forEach(row -> {
//                            if (row.isCheckStudent) {
//                                RecipientData recipientData = new RecipientData();
//                                recipientData.stCode = row.stCode;
//                                recipientData.stName = row.stName;
//                                recipientData.phoneNumber = row.stPhoneNumber;
//                                recipientData.userGubun = Constants.USER_TYPE_STUDENT;
//                                recipientData.acaCode = row.acaCode;
//                                recipientData.acaName = DataManager.getInstance().getACAData(row.acaCode).acaName;
//                                recipientData.isApp = row.stInstall;
//                                recipientList.add(recipientData);
//                            }
//                            if (row.isCheckParent) {
//                                RecipientData recipientData = new RecipientData();
//                                recipientData.stCode = row.stCode;
//                                recipientData.stName = row.stName;
//                                recipientData.phoneNumber = row.parentPhoneNumber;
//                                recipientData.userGubun = Constants.USER_TYPE_PARENTS;
//                                recipientData.acaCode = row.acaCode;
//                                recipientData.acaName = DataManager.getInstance().getACAData(row.acaCode).acaName;
//                                recipientData.isApp = row.parentInstall;
//                                recipientList.add(recipientData);
//                            }
//                        });
////                    for (RecipientStudentData row : checkedList) {
////                        if (row.isCheckStudent) {
////                            RecipientData recipientData = new RecipientData();
////                            recipientData.stCode = row.stCode;
////                            recipientData.stName = row.stName;
////                            recipientData.phoneNumber = row.stPhoneNumber;
////                            recipientData.userGubun = Constants.USER_TYPE_STUDENT;
////                            recipientList.add(recipientData);
////                        }
////                        if (row.isCheckParent) {
////                            RecipientData recipientData = new RecipientData();
////                            recipientData.stCode = row.stCode;
////                            recipientData.stName = row.stName;
////                            recipientData.phoneNumber = row.parentPhoneNumber;
////                            recipientData.userGubun = Constants.USER_TYPE_PARENTS;
////                            recipientList.add(recipientData);
////                        }
////                    }
////                intent.putParcelableArrayListExtra(IntentParams.PARAM_RECIPIENT_INFO, _recipientList);
//                        runOnUiThread(() -> {
//                            intent.putExtra(IntentParams.PARAM_EXIST_RECIPIENT_INFO, true);
//                            setResult(RESULT_OK, intent);
//                            finish();
//                        });
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    } finally {
//                        hideProgressDialog();
//                    }
//                }).start();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //수신인 리스트 비움
        DataManager.getInstance().getRecipientList().clear();
    }

    private void showSendSMSDialog() {
        if(_smsDialog != null && _smsDialog.isShowing()) {
            _smsDialog.dismiss();
        }
        _smsDialog = new SendSMSDialog(mContext);
        _smsDialog.setTitle(getString(R.string.msg_send_install_sms));
        _smsDialog.setEditText(_installSMSContent);
        _smsDialog.setRecipientCount((int)(_recipientStudentList.stream().filter(t->t.isCheckStudent || t.isCheckParent).count()));
        _smsDialog.setOnOkButtonClickListener(okListener);
        _smsDialog.setOnCancelButtonClickListener(cancelListener);
        _smsDialog.setUpdatedContentListener(updatedListener);
        _smsDialog.show();
    }
    private void hideSendSMSDialog() {
        if(_smsDialog != null) _smsDialog.dismiss();
    }
    /**
     * 부서 목록 요청
     * @param acaCode 캠퍼스 코드
     */
    private void requestDeptList(String acaCode) {

        if (RetrofitClient.getInstance() != null) {
            _retrofitApi = RetrofitClient.getApiInterface();
            _retrofitApi.getDeptList(acaCode).enqueue(new Callback<GetDeptListResponse>() {
                @Override
                public void onResponse(Call<GetDeptListResponse> call, Response<GetDeptListResponse> response) {
                    try {
                        if (response.isSuccessful()) {

                            if (response.body() != null) {

                                List<DepartmentData> getData = response.body().data;
                                _DeptList.clear();
                                if (getData != null && !getData.isEmpty()) {
//                                    if (getData.size() != _DeptList.size()) Utils.updateSpinnerList(spinnerDept);

                                    _DeptList.add(new DepartmentData(getString(R.string.item_total), 0));
                                    _DeptList.addAll(getData);

                                } else LogMgr.e(TAG + " DetailData is null");
                                _handler.sendEmptyMessage(CMD_GET_DEPT_LIST);
                            }
                        } else {
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestBoardDetail() Exception : ", e.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<GetDeptListResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "requestBoardDetail() onFailure >> " + t.getMessage());
                    } catch (Exception e) {
                    }
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * 학년 목록
     * @param acaCode 캠퍼스코드
     * @param deptCode 부서코드
     */
    private void requestClstList(String acaCode, int deptCode) {

        if (RetrofitClient.getInstance() != null) {
            _retrofitApi = RetrofitClient.getApiInterface();
            _retrofitApi.getClstList(acaCode, deptCode).enqueue(new Callback<GetClstListResponse>() {
                @Override
                public void onResponse(Call<GetClstListResponse> call, Response<GetClstListResponse> response) {
                    try {
                        if (response.isSuccessful()) {

                            if (response.body() != null) {

                                List<ClstData> getData = response.body().data;
                                _ClstList.clear();
                                if (getData != null && !getData.isEmpty()) {
//                                    if (getData.size() != _ClstList.size()) Utils.updateSpinnerList(spinnerClst);

                                    _ClstList.add(new ClstData(getString(R.string.item_total), 0));
                                    _ClstList.addAll(getData);

                                } else LogMgr.e(TAG + " DetailData is null");
                                _handler.sendEmptyMessage(CMD_GET_CLST_LIST);
                            }
                        } else {
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestBoardDetail() Exception : ", e.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<GetClstListResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "requestBoardDetail() onFailure >> " + t.getMessage());
                    } catch (Exception e) {
                    }
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void requestAppInstallMsg() {

        if (RetrofitClient.getInstance() != null) {
            _retrofitApi = RetrofitClient.getApiInterface();
            _retrofitApi.getAppInstallMsg().enqueue(new Callback<StringResponse>() {
                @Override
                public void onResponse(Call<StringResponse> call, Response<StringResponse> response) {
                    try {
                        if (response.isSuccessful()) {

                            if (response.body() != null) {

                                _installSMSContent = response.body().data;
                            }
                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestBoardDetail() Exception : ", e.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<StringResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "requestBoardDetail() onFailure >> " + t.getMessage());
                    } catch (Exception e) {
                    }

                }
            });
        }
    }
    //region unused
//    /**
//     * 학급 목록 요청
//     * @param acaCode 캠퍼스코드
//     * @param deptCode 부서코드
//     * @param clstCode 학년코드
//     */
//    private void requestClassList(String acaCode, int deptCode, int clstCode, String date) {
//
//        if (RetrofitClient.getInstance() != null) {
//            _retrofitApi = RetrofitClient.getApiInterface();
//
//            _retrofitApi.getClassList(acaCode, deptCode, clstCode, date).enqueue(new Callback<GetClassListResponse>() {
//                @Override
//                public void onResponse(Call<GetClassListResponse> call, Response<GetClassListResponse> response) {
//                    try {
//                        if (response.isSuccessful()) {
//
//                            if (response.body() != null) {
//
//                                List<ClassData> getData = response.body().data;
//                                if (getData != null) {
//                                    _ClassList = getData;
//                                    _handler.sendEmptyMessage(CMD_GET_CLASS_LIST);
//                                } else LogMgr.e(TAG + " DetailData is null");
//                            }
//                        } else {
//                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
//                        }
//                    } catch (Exception e) {
//                        LogMgr.e(TAG + "requestBoardDetail() Exception : ", e.getMessage());
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<GetClassListResponse> call, Throwable t) {
//                    try {
//                        LogMgr.e(TAG, "requestBoardDetail() onFailure >> " + t.getMessage());
//                    } catch (Exception e) {
//                    }
//                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//    }
    //endregion
    private void requestRecipientList(String acaCode, int deptCode, int clstCode) {

        if (RetrofitClient.getInstance() != null) {
            _retrofitApi = RetrofitClient.getApiInterface();
            Call<GetRecipientStudentResponse> call = null;

            int scCode = 0;
            if(_selectedSchoolData != null) scCode = _selectedSchoolData.scCode;
            int gubun = -1;
            if(_selectedStudentType != null) gubun = _selectedStudentType.getCode();
            call = _retrofitApi.getRecipientStudentList2(acaCode, deptCode, clstCode, scCode, gubun);

            call.enqueue(new Callback<GetRecipientStudentResponse>() {
                @Override
                public void onResponse(Call<GetRecipientStudentResponse> call, Response<GetRecipientStudentResponse> response) {
                    try {
                        if (response.isSuccessful()) {
                            isFilterTriggerChanged = false;
                            if (response.body() != null) {

                                List<RecipientStudentData> getData = response.body().data;
                                if (_recipientStudentList != null) _recipientStudentList.clear();
                                if (getData != null) {
                                    _recipientStudentList.addAll(getData.stream().peek(t-> t.acaCode = _selectedACA.acaCode).collect(Collectors.toList()));
                                    String searchStr = "";
                                    if(etSearch != null && etSearch.getText() != null) {
                                        searchStr = etSearch.getText().toString().trim();
                                    }

                                    if(!TextUtils.isEmpty(searchStr)) {
                                        searchFilter(searchStr);
                                    }else{
                                        _adapterRecipient.notifyDataSetChanged();
                                        checkEmptyRecyclerView();
                                    }
                                } else LogMgr.e(TAG + " DetailData is null");
                            }
                        } else {
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestBoardDetail() Exception : ", e.getMessage());
                    }finally{
                        hideProgressDialog();
                        _swipeRefreshLayout.setRefreshing(false);
                    }
                }

                @Override
                public void onFailure(Call<GetRecipientStudentResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "requestBoardDetail() onFailure >> " + t.getMessage());
                    } catch (Exception e) {
                    }
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                    hideProgressDialog();
                    _swipeRefreshLayout.setRefreshing(false);
                }
            });
        }
    }
    private void requestSendSMS(int currentPage, int totalPage, boolean prevResult) {

        SmsRequest request = new SmsRequest();
        request.msg = _installSMSContent;
        request.senderCode = String.valueOf(_sfCode);
        List<RecipientData> recipientList = DataManager.getInstance().getRecipientList();
        List<String> phoneList = new ArrayList<>();
        List<String> receiverList = new ArrayList<>();
        if(totalPage > 0) {
            if(currentPage < totalPage) {
                phoneList = recipientList.subList(currentPage * Constants.MAX_RECIPIENT_COUNT, (currentPage + 1) * Constants.MAX_RECIPIENT_COUNT).stream().map(t->t.phoneNumber).collect(Collectors.toList());
                receiverList = recipientList.subList(currentPage * Constants.MAX_RECIPIENT_COUNT, (currentPage + 1) * Constants.MAX_RECIPIENT_COUNT).stream().map(t->String.valueOf(t.stCode)).collect(Collectors.toList());
            }else{
                phoneList = recipientList.subList(currentPage * Constants.MAX_RECIPIENT_COUNT, recipientList.size()).stream().map(t->t.phoneNumber).collect(Collectors.toList());
                receiverList = recipientList.subList(currentPage * Constants.MAX_RECIPIENT_COUNT, recipientList.size()).stream().map(t->String.valueOf(t.stCode)).collect(Collectors.toList());
            }
        }else {
            phoneList = recipientList.stream().map(t->t.phoneNumber).collect(Collectors.toList());
            receiverList = recipientList.stream().map(t->String.valueOf(t.stCode)).collect(Collectors.toList());
        }

        request.receiver = String.join(",", phoneList);
        request.receiverCode = String.join(",", receiverList);
        request.sender = _selectedACA.acaTel;   //대표번호

        if(RetrofitClient.getInstance() != null) {
            int finalCurrentPage = currentPage;
            RetrofitClient.getApiInterface().sendSms(request).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    try {
                        if(finalCurrentPage < totalPage) {
                            requestSendSMS(finalCurrentPage + 1, totalPage, prevResult && response.isSuccessful());
                        }else {
                            hideProgressDialog();
                            hideSendSMSDialog();
                            if(!prevResult) {
                                showMessageDialog(getString(R.string.dialog_title_error), getString(R.string.msg_fail_to_send_install_SMS), new View.OnClickListener(){

                                    @Override
                                    public void onClick(View view) {
                                        hideMessageDialog();
                                    }
                                }, null);
                            }else {
                                if (response.isSuccessful()) {
                                    Toast.makeText(mContext, R.string.msg_success_to_send_install_SMS, Toast.LENGTH_SHORT).show();
                                    initLayout(Constants.BoardEditMode.Show);
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
                    hideProgressDialog();
                    hideSendSMSDialog();
                    LogMgr.e(TAG, "sendSms() onFailure >> " + t.getMessage());
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void searchFilter(String searchStr){
        if(_adapterRecipient != null) {
            LogMgr.e(TAG, "filter with " + searchStr);
            _adapterRecipient.getFilter().filter(searchStr);
        }
    }
    private boolean checkEmptyRecyclerView() {
        if (_adapterRecipient.getItemCount() > 0) {
            _swipeRefreshLayout.setVisibility(View.VISIBLE);
            tvEmptyLayout.setVisibility(View.INVISIBLE);
            return false;
        } else {
            _swipeRefreshLayout.setVisibility(View.INVISIBLE);
            tvEmptyLayout.setVisibility(View.VISIBLE);
            return true;
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
    private void navigate2DetailActivity(RecipientStudentData data) {
        Intent intent = new Intent(MenuManageStudentActivity.this, DetailStudentInfoActivity.class);
        //원생정보 seq, stCode
//        intent.putExtra(IntentParams.PARAM_STU_SEQ, data.seq);
        intent.putExtra(IntentParams.PARAM_STU_STCODE, data.stCode);
        //날짜
        String dateStr = _dateTransferFormat.format(new Date());
        intent.putExtra(IntentParams.PARAM_SELECTED_DATE, dateStr);
        intent.putExtra(IntentParams.PARAM_BOARD_ITEM, data);
        //반
//        int clsCode = 0;
//        if(_selectedClassData != null) clsCode = _selectedClassData.clsCode;
//        intent.putExtra(IntentParams.PARAM_STU_CLASSCODE, clsCode);
        startActivity(intent);
        overridePendingTransition(R.anim.horizontal_enter, R.anim.horizontal_out);
    }
}