package kr.jeet.edu.manager.activity;

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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.demogorgorn.monthpicker.MonthPickerDialog;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;
import com.skydoves.powerspinner.OnSpinnerOutsideTouchListener;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.text.Collator;
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
import kr.jeet.edu.manager.adapter.RecipientListAdapter;
import kr.jeet.edu.manager.adapter.SchoolListAdapter;
import kr.jeet.edu.manager.adapter.WrapContentSpinnerAdapter;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.DataManager;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.dialog.SchoolListBottomSheetDialog;
import kr.jeet.edu.manager.model.data.ACAData;
import kr.jeet.edu.manager.model.data.ClassData;
import kr.jeet.edu.manager.model.data.ClstData;
import kr.jeet.edu.manager.model.data.DepartmentData;
import kr.jeet.edu.manager.model.data.RecipientData;
import kr.jeet.edu.manager.model.data.RecipientStudentData;
import kr.jeet.edu.manager.model.data.SchoolData;
import kr.jeet.edu.manager.model.response.GetClassListResponse;
import kr.jeet.edu.manager.model.response.GetClstListResponse;
import kr.jeet.edu.manager.model.response.GetDeptListResponse;
import kr.jeet.edu.manager.model.response.GetRecipientStudentResponse;
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

public class AppendRecipientActivity extends BaseActivity implements MonthPickerDialog.OnDateSetListener {
    private static final String TAG = "recipient";
    private Constants.RecipientFilterType _filterType = Constants.RecipientFilterType.TYPE_CLASS;
    private static final int CMD_GET_ACA_LIST = 0;
    private static final int CMD_GET_DEPT_LIST = 1;
    private static final int CMD_GET_CLST_LIST = 2;
    private static final int CMD_GET_CLASS_LIST = 3;
    private static final int CMD_SEARCH = 4;
    private static final int CMD_NOTIFY_DATASET_CHANGED = 5;
    private static final int CMD_COMPLETE = 6;
    SimpleDateFormat outputDateFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYY_MM_KOR);
    SimpleDateFormat apiDateFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYYMM);
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
    Date _selectedDate = new Date();
    List<ACAData> _ACAList = new ArrayList<>();
    List<DepartmentData> _DeptList = new ArrayList<>();
    List<ClstData> _ClstList = new ArrayList<>();
    List<ClassData> _ClassList = new ArrayList<>();
//    List<String> _ACANameList = new ArrayList<>();
//    ArrayList<RecipientData> _recipientList = new ArrayList<>();
    List<RecipientStudentData> _recipientStudentList = new ArrayList<>();
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
                case CMD_GET_CLASS_LIST:
                    if (_ClassList != null && !_ClassList.isEmpty()) {
                        spinnerClass.setEnabled(true);
//                        spinnerClass.setItems(_ClassList.stream().map(t -> t.clsName).collect(Collectors.toList()));
                    }else{

                        spinnerClass.setEnabled(false);
                    }
                    Utils.updateSpinnerList(spinnerClass, _ClassList.stream().map(t -> t.clsName).collect(Collectors.toList()));
                    break;
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
                            String apiDate = apiDateFormat.format(_selectedDate);
                            if (_selectedDept != null) deptCode = _selectedDept.deptCode;
                            if (_selectedClst != null) clstCode = _selectedClst.clstCode;
                            if (_selectedClass != null) clsCode = _selectedClass.clsCode;
                            showProgressDialog();

                            requestRecipientList(_selectedACA.acaCode, deptCode, clstCode, clsCode, apiDate);
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
                    checkEmptyRecyclerView();
                    updateCount();
                    break;
            }
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
        initData();
        initView();
        initAppbar();
    }
    void initData() {
        Intent intent = getIntent();
        if(intent != null){
            if(intent.hasExtra(IntentParams.PARAM_STU_ACACODE)) {
                _acaCode = intent.getStringExtra(IntentParams.PARAM_STU_ACACODE);
            }
            if(intent.hasExtra(IntentParams.PARAM_RECIPIENT_FILTERTYPE)) {
                _filterType = (Constants.RecipientFilterType) intent.getSerializableExtra(IntentParams.PARAM_RECIPIENT_FILTERTYPE);
            }

        }
    }
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
        tvSelectCount = findViewById(R.id.tv_select_count);
        tvSelectCount.setVisibility(View.VISIBLE);
        Calendar cal = Calendar.getInstance();
        _selectedDate = cal.getTime();
        try {
            String output = outputDateFormat.format(_selectedDate);
            tvCalendar.setText(output);
        }catch(Exception ex){}
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
        spinnerCampus.setSpinnerPopupHeight(ConstraintLayout.LayoutParams.WRAP_CONTENT);
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
        spinnerDept.setSpinnerPopupHeight(ConstraintLayout.LayoutParams.WRAP_CONTENT);
        //endregion
        //region spinner clst
        spinnerClst = findViewById(R.id.spinner_clst);
        spinnerClst.setEnabled(false);
        {
            WrapContentSpinnerAdapter adapter = new WrapContentSpinnerAdapter(mContext, _ClstList.stream().map(t -> t.clstName).collect(Collectors.toList()), spinnerClst);
            spinnerClst.setSpinnerAdapter(adapter);
        }
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
                if(_filterType.equals(Constants.RecipientFilterType.TYPE_CLASS)) {

                    if (_selectedClass != null) {
                        _selectedClass = null;
                        if (spinnerClass != null) spinnerClass.clearSelectedItem();
                    }
                    if(newItem == getString(R.string.item_total)) { //전체 선택
                        if(spinnerClass != null) spinnerClass.setEnabled(false);
                    }else {
                        try {
                            String apiDate = apiDateFormat.format(_selectedDate);
                            requestClassList(_selectedACA.acaCode, _selectedDept.deptCode, _selectedClst.clstCode, apiDate);
                        } catch (Exception ex) {
                        }
                    }
                }

            }
        });
        spinnerClst.setSpinnerOutsideTouchListener(new OnSpinnerOutsideTouchListener() {
            @Override
            public void onSpinnerOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                spinnerClst.dismiss();
            }
        });
        spinnerClst.setLifecycleOwner(this);
        spinnerClst.setSpinnerPopupHeight(ConstraintLayout.LayoutParams.WRAP_CONTENT);
        //endregion
        //region spinner class
        spinnerClass = findViewById(R.id.spinner_cls);
        spinnerClass.setEnabled(false);
        {
            WrapContentSpinnerAdapter adapter = new WrapContentSpinnerAdapter(mContext, _ClassList.stream().map(t -> t.clsName).collect(Collectors.toList()), spinnerClass);
            spinnerClass.setSpinnerAdapter(adapter);
        }
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
        spinnerClass.setSpinnerPopupHeight(ConstraintLayout.LayoutParams.WRAP_CONTENT);
        //endregion
        //region spinner studentType
        spinnerStudentType = findViewById(R.id.spinner_student_type);
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
        spinnerStudentType.setSpinnerPopupHeight(ConstraintLayout.LayoutParams.WRAP_CONTENT);
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
        //_swipe 동작 제거 요청 0921
        _swipeRefreshLayout.setEnabled(false);
        _recyclerRecipient = findViewById(R.id.recyclerview_recipient);
        _adapterRecipient = new RecipientListAdapter(mContext, _recipientStudentList, new RecipientListAdapter.onItemClickListener() {
            @Override
            public void onItemClick(RecipientStudentData item) {
                //do nothing
            }

            @Override
            public void onCheckedParent(int position, boolean b) {
                if(!b) {
                    LogMgr.e(TAG, "onCheckedParent in adapter " + b);
                    cbParentTotal.setChecked(false);
                }else {
                    if (_adapterRecipient._filteredlist.stream().allMatch(t -> t.isCheckParent == true|| !Utils.checkPhoneNumber(t.parentPhoneNumber))) {
                        LogMgr.e(TAG, "onCheckedParent in allmatch true");
                        cbParentTotal.setChecked(true);
                    } else {
                        LogMgr.e(TAG, "onCheckedParent in  allmatch false");
                        cbParentTotal.setChecked(false);
                    }
                }
                updateCount();
                cbTotal.setChecked(cbStudentTotal.isChecked() && cbParentTotal.isChecked());
            }

            @Override
            public void onCheckedStudent(int position, boolean b) {
                if(!b) {
                    cbStudentTotal.setChecked(false);
                }else {
                    if (_adapterRecipient._filteredlist.stream().allMatch(t -> t.isCheckStudent == true || !Utils.checkPhoneNumber(t.stPhoneNumber))) {
                        cbStudentTotal.setChecked(true);
                    } else {
                        cbStudentTotal.setChecked(false);
                    }
                }
                updateCount();
                cbTotal.setChecked(cbStudentTotal.isChecked() && cbParentTotal.isChecked());
            }

            @Override
            public void onFilteringCompleted() {
                if(checkEmptyRecyclerView()){

                }else {
                    LogMgr.e(TAG, "itemcount > 0");
                    if (_adapterRecipient._filteredlist.stream().allMatch(t -> t.isCheckParent || !Utils.checkPhoneNumber(t.parentPhoneNumber))) {
                        LogMgr.e(TAG, "searchFilter in allmatch true");
                        cbParentTotal.setChecked(true);
                    } else {
                        LogMgr.e(TAG, "searchFilter in allmatch false");
                        cbParentTotal.setChecked(false);
                    }
                    if (_adapterRecipient._filteredlist.stream().allMatch(t -> t.isCheckStudent || !Utils.checkPhoneNumber(t.stPhoneNumber))) {
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
        checkEmptyRecyclerView();

    }
    private void updateCount() {
        if(_recipientStudentList == null || _recipientStudentList.isEmpty()) {
            tvSelectCount.setText("");
        }else {
            tvSelectCount.setVisibility(View.VISIBLE);
            int count = (int) (_recipientStudentList.stream().filter(t -> t.isCheckParent).count() + _recipientStudentList.stream().filter(t -> t.isCheckStudent).count());
            tvSelectCount.setText(getString(R.string.selected_count, count));
        }
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
                        isFilterTriggerChanged = true;
                        tvSchool.setText(item.scName);
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

    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.title_append_recipient);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.tv_content_date:
                LogMgr.e(TAG, "yearmonthpicker");
                Calendar cal = Calendar.getInstance();
                cal.setTime(_selectedDate);
                Utils.yearMonthPicker(mContext, this, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH));
                break;
            case R.id.tv_search_btn:
                if(checkValid()) {
                    _handler.sendEmptyMessage(CMD_SEARCH);
                }
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
                    if(Utils.checkPhoneNumber(data.stPhoneNumber)) {
                        data.isCheckStudent = toDo;
                    }
                    if(Utils.checkPhoneNumber(data.parentPhoneNumber)) {
                        data.isCheckParent = toDo;
                    }
                }
                _handler.sendEmptyMessage(CMD_NOTIFY_DATASET_CHANGED);
                break;
            case R.id.layout_student_checkbox: {
                cbStudentTotal.setChecked(!cbStudentTotal.isChecked());
                boolean todo = cbStudentTotal.isChecked();
                cbTotal.setChecked(todo && cbParentTotal.isChecked());
                _adapterRecipient._filteredlist.forEach(t -> t.isCheckStudent = todo && Utils.checkPhoneNumber(t.stPhoneNumber));
                _handler.sendEmptyMessage(CMD_NOTIFY_DATASET_CHANGED);
            }
                break;
            case R.id.layout_parent_checkbox: {
                LogMgr.e(TAG, "parent checkbox clicked");
                cbParentTotal.setChecked(!cbParentTotal.isChecked());
                boolean todo = cbParentTotal.isChecked();
                cbTotal.setChecked(todo && cbStudentTotal.isChecked());
                _adapterRecipient._filteredlist.forEach(t -> t.isCheckParent = todo && Utils.checkPhoneNumber(t.parentPhoneNumber));
                _handler.sendEmptyMessage(CMD_NOTIFY_DATASET_CHANGED);
            }
                break;

        }
    }
    private boolean checkValid() {
        //todo make check Valid
        return true;
    }
    @Override
    public void onDateSet(int month, int year) {
        LogMgr.e(TAG, year + "/" + month);
        //시간 비교
        Calendar cal = Calendar.getInstance();
        cal.setTime(_selectedDate);
        //설정되어 있던 시간과 다른 경우만 filterTriggerchanged 변경
        if(year == cal.get(Calendar.YEAR) && month == cal.get(Calendar.MONTH)){
            return;
        }
        
        isFilterTriggerChanged = true;
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        _selectedDate = cal.getTime();
        try {
            String output = outputDateFormat.format(_selectedDate);
            String apiDate = apiDateFormat.format(_selectedDate);
            tvCalendar.setText(output);
            if(spinnerClass != null && spinnerClass.isEnabled()) {
                requestClassList(_selectedACA.acaCode, _selectedDept.deptCode, _selectedClst.clstCode, apiDate);

                if (_selectedClass != null) {
                    _selectedClass = null;
                    spinnerClass.clearSelectedItem();
                }
            }
        }catch(Exception ex){}


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_confirm, menu);
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
            case R.id.action_ok:
                Intent intent = new Intent();
                showProgressDialog();
                new Thread(() -> {
                    try {

                        ArrayList<RecipientData> recipientList = DataManager.getInstance().getRecipientList();
                        if (recipientList != null) recipientList.clear();
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
                                recipientList.add(recipientData);
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
                                recipientList.add(recipientData);
                            }
                        });
//                    for (RecipientStudentData row : checkedList) {
//                        if (row.isCheckStudent) {
//                            RecipientData recipientData = new RecipientData();
//                            recipientData.stCode = row.stCode;
//                            recipientData.stName = row.stName;
//                            recipientData.phoneNumber = row.stPhoneNumber;
//                            recipientData.userGubun = Constants.USER_TYPE_STUDENT;
//                            recipientList.add(recipientData);
//                        }
//                        if (row.isCheckParent) {
//                            RecipientData recipientData = new RecipientData();
//                            recipientData.stCode = row.stCode;
//                            recipientData.stName = row.stName;
//                            recipientData.phoneNumber = row.parentPhoneNumber;
//                            recipientData.userGubun = Constants.USER_TYPE_PARENTS;
//                            recipientList.add(recipientData);
//                        }
//                    }
//                intent.putParcelableArrayListExtra(IntentParams.PARAM_RECIPIENT_INFO, _recipientList);
                        runOnUiThread(() -> {
                            intent.putExtra(IntentParams.PARAM_EXIST_RECIPIENT_INFO, true);
                            setResult(RESULT_OK, intent);
                            finish();
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        hideProgressDialog();
                    }
                }).start();
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    /**
     * 학급 목록 요청
     * @param acaCode 캠퍼스코드
     * @param deptCode 부서코드
     * @param clstCode 학년코드
     */
    private void requestClassList(String acaCode, int deptCode, int clstCode, String date) {

        if (RetrofitClient.getInstance() != null) {
            _retrofitApi = RetrofitClient.getApiInterface();

            _retrofitApi.getClassList2(acaCode, deptCode, clstCode, date, _sfCode).enqueue(new Callback<GetClassListResponse>() {
                @Override
                public void onResponse(Call<GetClassListResponse> call, Response<GetClassListResponse> response) {
                    try {
                        if (response.isSuccessful()) {

                            if (response.body() != null) {

                                List<ClassData> getData = response.body().data;
                                _ClassList.clear();
                                if (getData != null && !getData.isEmpty()) {
//                                    if (getData.size() != _ClassList.size()) Utils.updateSpinnerList(spinnerClass);

                                    _ClassList.add(new ClassData(getString(R.string.item_total), 0));
                                    _ClassList.addAll(getData);
                                    _handler.sendEmptyMessage(CMD_GET_CLASS_LIST);
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
                public void onFailure(Call<GetClassListResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "requestBoardDetail() onFailure >> " + t.getMessage());
                    } catch (Exception e) {
                    }
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void requestRecipientList(String acaCode, int deptCode, int clstCode, int clsCode, String date) {

        if (RetrofitClient.getInstance() != null) {
            _retrofitApi = RetrofitClient.getApiInterface();
            Call<GetRecipientStudentResponse> call = null;
            if(_filterType.equals(Constants.RecipientFilterType.TYPE_CLASS)) {
                call = _retrofitApi.getRecipientStudentList3(acaCode, deptCode, clstCode, clsCode, date, _sfCode);
            }else{
                int scCode = 0;
                if(_selectedSchoolData != null) scCode = _selectedSchoolData.scCode;
                int gubun = -1;
                if(_selectedStudentType != null) gubun = _selectedStudentType.getCode();
                call = _retrofitApi.getRecipientStudentList2(acaCode, deptCode, clstCode, scCode, gubun);
            }
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
                                        _handler.sendEmptyMessage(CMD_NOTIFY_DATASET_CHANGED);

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

}