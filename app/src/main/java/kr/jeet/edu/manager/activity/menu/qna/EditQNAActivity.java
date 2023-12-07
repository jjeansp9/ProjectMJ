package kr.jeet.edu.manager.activity.menu.qna;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;
import com.skydoves.powerspinner.OnSpinnerOutsideTouchListener;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.activity.BaseActivity;
import kr.jeet.edu.manager.adapter.RecipientChipListAdapter;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.DataManager;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.model.data.ACAData;
import kr.jeet.edu.manager.model.data.QnaDetailData;
import kr.jeet.edu.manager.model.data.RecipientData;
import kr.jeet.edu.manager.model.data.StudentGradeData;
import kr.jeet.edu.manager.model.request.BaseRequest;
import kr.jeet.edu.manager.model.request.QnaAddRequest;
import kr.jeet.edu.manager.model.request.QnaReplyRequest;
import kr.jeet.edu.manager.model.request.QnaUpdateRequest;
import kr.jeet.edu.manager.model.request.RecipientRequest;
import kr.jeet.edu.manager.model.response.BaseResponse;
import kr.jeet.edu.manager.model.response.StringResponse;
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

public class EditQNAActivity extends BaseActivity {
    private static final String TAG = "editQnA";
    private static final int CMD_GET_ACA_LIST = 0;
    private static final int CMD_GET_GRADE_LIST = 1;
    //views
    PowerSpinnerView spinnerCampus, spinnerGrade;
    NestedScrollView scrollView;
    EditText etSubject, etContent, etAnswer;
    LinearLayout layoutNotice, layoutPrivate;
    CheckBox cbIsNotice, cbIsPrivate;
    TextView tvAnswer;
//    RecyclerView _recyclerViewRecipient;
//    Button btnAppendRecipient;
//    TextView tvRecipient, tvRecipientEmpty;
    LinearLayoutCompat layoutBottom;

    RecipientChipListAdapter _recipientListAdapter;
    List<ACAData> _ACAList = new ArrayList<>();
    List<String> _ACANameList = new ArrayList<>();
    ACAData selectedACA = null;
    List<StudentGradeData> _GradeList = new ArrayList<>();
    List<String> _GradeNameList = new ArrayList<>();
    StudentGradeData selectedGrade = null;

    int _attachedCount = 0;
    QnaDetailData _currentData = new QnaDetailData();
    BaseRequest _currentRequest = null;
    List<RecipientData> _recipientList = new ArrayList<>();
    Set<RecipientData> _recipientSet = new HashSet<>();

    private RetrofitApi mRetrofitApi;
    String _acaCode = "";
    String _gubunCode = "";
    int _userGubun = 1;
    int _memberSeq = 0;
    String _memberName = "";
    int _sfCode = 0;
    Constants.BoardEditMode boardEditMode = Constants.BoardEditMode.New;
    boolean originalIsMain = false;

    private Handler _handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {

                case CMD_GET_ACA_LIST:
                    break;
                case CMD_GET_GRADE_LIST:
                    if (_GradeList != null) {
                        if(boardEditMode == Constants.BoardEditMode.New) {
                            if (spinnerGrade != null) spinnerGrade.setEnabled(true);
                        }
                        Utils.updateSpinnerList(spinnerGrade, _GradeList.stream().map(t -> t.gubunName).collect(Collectors.toList()));
                    }
                    if(boardEditMode == Constants.BoardEditMode.Edit){
                        LogMgr.w(TAG, "boardEditMode is Edit");
//                        if (selectedGrade == null) {
                        try {
                            LogMgr.w(TAG, "_currentData.acaGubunCode = " + _currentData.acaGubunName);
                            Optional option = _GradeList.stream().filter(t -> String.valueOf(t.gubunCode).equals(_currentData.acaGubunCode)).findFirst();
                            if (option.isPresent()) {
                                selectedGrade = (StudentGradeData) option.get();
                            }
                        }catch(Exception ex){
                            ex.printStackTrace();
                        }

                        if (selectedGrade != null) {
                            LogMgr.w(TAG, "selectedGrade is not null");
                            int selectedIndex = _GradeList.indexOf(selectedGrade);
                            LogMgr.w(TAG, "selectedGrade = " + selectedGrade.gubunName + " / index  = " + selectedIndex);
                            spinnerGrade.selectItemByIndex(selectedIndex);
                        }else{
                            LogMgr.w(TAG, "selectedGrade is null");
                        }

//                        }
                    }else{
                        if(!TextUtils.isEmpty(_gubunCode)) {
                            try {
                                LogMgr.w(TAG, "_currentData.acaGubunCode = " + _gubunCode);
                                Optional option = _GradeList.stream().filter(t -> String.valueOf(t.gubunCode).equals(_gubunCode)).findFirst();
                                if (option.isPresent()) {
                                    selectedGrade = (StudentGradeData) option.get();
                                }
                            }catch(Exception ex){
                                ex.printStackTrace();
                            }

                            if (selectedGrade != null) {
                                LogMgr.w(TAG, "selectedGrade is not null");
                                int selectedIndex = _GradeList.indexOf(selectedGrade);
                                LogMgr.w(TAG, "selectedGrade = " + selectedGrade.gubunName + " / index  = " + selectedIndex);
                                spinnerGrade.selectItemByIndex(selectedIndex);
                                _gubunCode = "";
                            }else{
                                LogMgr.w(TAG, "selectedGrade is null");
                            }
                        }
                    }
                    break;

            }
        }
    };

    //작성화면 resultLauncher
//    ActivityResultLauncher<Intent> recipientResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
//        @Override
//        public void onActivityResult(ActivityResult result) {
//            if(result.getResultCode() != RESULT_CANCELED) {
//                Intent intent = result.getData();
//                if(intent == null) return;
//                if(intent.hasExtra(IntentParams.PARAM_EXIST_RECIPIENT_INFO)) {
//                    if(intent.getBooleanExtra(IntentParams.PARAM_EXIST_RECIPIENT_INFO, false)) {
//                        List<RecipientData> list = DataManager.getInstance().getRecipientList();
//                        LogMgr.w(TAG, "list count = " + list.size());
//                        if(!list.isEmpty()) {
//                            _recipientSet.addAll(list);
//                        }
//                        initChipGroup();
//                    }
//                }
//            }
//        }
//    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_qna);
        mContext = this;
        _userGubun = PreferenceUtil.getUserGubun(this);
        _memberSeq = PreferenceUtil.getUserSeq(this);
        _memberName = PreferenceUtil.getUserName(this);
        _sfCode = PreferenceUtil.getUserSFCode(this);
//        _acaCode = PreferenceUtil.getAcaCode(this);

        initIntentData();
        initView();
        initAppbar();
        initData();
    }

    void initView() {

        layoutBottom = findViewById(R.id.layout_bottom);

        etSubject = findViewById(R.id.et_subject);
        etContent = findViewById(R.id.et_content);
        etAnswer = findViewById(R.id.et_answer);
        tvAnswer = findViewById(R.id.tv_answer);
        scrollView = findViewById(R.id.scrollview);
        spinnerCampus = findViewById(R.id.spinner_campus);
        spinnerGrade = findViewById(R.id.spinner_grade);
//        layoutSendNotify = findViewById(R.id.layout_allow_notify);
//        layoutSendNotify.setOnClickListener(this);
//        tvRecipient = findViewById(R.id.tv_recipient_count);
//        btnAppendRecipient = findViewById(R.id.btn_append_recipient);
//        btnAppendRecipient.setOnClickListener(this);
        layoutNotice = findViewById(R.id.layout_allow_notice);
        layoutPrivate = findViewById(R.id.layout_allow_private);
        if(_userGubun <= Constants.USER_TYPE_TEACHER) {
            layoutNotice.setVisibility(View.VISIBLE);
            layoutNotice.setOnClickListener(this);
            layoutPrivate.setVisibility(View.VISIBLE);
            layoutPrivate.setOnClickListener(this);
        }else{
            layoutNotice.setVisibility(View.GONE);
            layoutPrivate.setVisibility(View.VISIBLE);
            layoutPrivate.setOnClickListener(this);
        }
//        cbIsSendNotify = findViewById(R.id.check_notify);
        cbIsNotice = findViewById(R.id.check_notice);
        cbIsPrivate = findViewById(R.id.check_private);

        //ACA list for spinner
        //전체 추가 -> 전체 제거
//        _ACAList.add(new ACAData(getString(R.string.item_total), ""));
        _ACAList.addAll(DataManager.getInstance().getLocalACAListMap().values());
        if(_ACAList != null) _ACANameList = _ACAList.stream().map(t -> t.acaName).collect(Collectors.toList());
//        {
//            WrapContentSpinnerAdapter adapter = new WrapContentSpinnerAdapter(mContext, _ACANameList, spinnerCampus);
//            spinnerCampus.setSpinnerAdapter(adapter);
//        }

        Utils.updateSpinnerList(spinnerCampus, _ACANameList);
        spinnerCampus.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener<String>() {
            @Override
            public void onItemSelected(int oldIndex, @Nullable String oldItem, int newIndex, String newItem) {
                LogMgr.e(newItem + " selected");
                ACAData selectedData = null;
                Optional optional = _ACAList.stream().filter(t -> t.acaName == newItem).findFirst();
                if(optional.isPresent()) {
                    selectedData = (ACAData) optional.get();
                }
                selectedACA = selectedData;
                LogMgr.w("selectedACA = " + selectedACA.acaCode + " / " +selectedACA.acaName);
                if(boardEditMode == Constants.BoardEditMode.New) {
                    requestGradeList(selectedACA.acaCode);
                    if (selectedGrade != null) {
                        selectedGrade = null;
                    }
                    if (spinnerGrade != null) spinnerGrade.clearSelectedItem();
                }else{
                    requestGradeList(selectedACA.acaCode);
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

        spinnerGrade = findViewById(R.id.spinner_grade);
//        {
//            WrapContentSpinnerAdapter adapter = new WrapContentSpinnerAdapter(mContext, _GradeList.stream().map(t -> t.gubunName).collect(Collectors.toList()), spinnerGrade);
//            spinnerGrade.setSpinnerAdapter(adapter);
//        }

        spinnerGrade.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener<String>() {
            @Override
            public void onItemSelected(int oldIndex, @Nullable String oldItem, int newIndex, String newItem) {
                LogMgr.e(newItem + " selected");
                StudentGradeData selectedData = null;
                Optional optional = _GradeList.stream().filter(t -> t.gubunName == newItem).findFirst();
                if(optional.isPresent()) {
                    selectedData = (StudentGradeData) optional.get();
                }
                selectedGrade = selectedData;
            }
        });
        spinnerGrade.setSpinnerOutsideTouchListener(new OnSpinnerOutsideTouchListener() {
            @Override
            public void onSpinnerOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                spinnerGrade.dismiss();
            }
        });
        spinnerGrade.setLifecycleOwner(this);
        spinnerGrade.setEnabled(false);

        //region chip
//        recipientChipGroup = findViewById(R.id.chipgroup);
//        _recyclerViewRecipient = findViewById(R.id.recycler_chip);
//        _recipientListAdapter = new RecipientChipListAdapter(mContext, _recipientList, new RecipientChipListAdapter.onItemClickListener() {
//            @Override
//            public void onDeleteClick(int position, RecipientData item) {
//                _recipientList.remove(position);
//                _recipientListAdapter.notifyItemRemoved(position);
//                tvRecipient.setText(getString(R.string.content_recipient_counts, _recipientList.size()));
//                if(boardEditMode == Constants.BoardEditMode.New) {
//                    if (_recipientList.isEmpty()) {
//                        _recyclerViewRecipient.setVisibility(View.GONE);
//                        tvRecipientEmpty.setVisibility(View.VISIBLE);
//                    } else {
//                        _recyclerViewRecipient.setVisibility(View.VISIBLE);
//                        tvRecipientEmpty.setVisibility(View.GONE);
//                    }
//                }else{
//                    if (_recipientList.isEmpty()) {
//                        _recyclerViewRecipient.setVisibility(View.GONE);
//                    } else {
//                        _recyclerViewRecipient.setVisibility(View.VISIBLE);
//                    }
//                    tvRecipientEmpty.setVisibility(View.GONE);
//                }
//            }
//        });
//        _recipientListAdapter.setEditMode(boardEditMode);
//        FlexboxLayoutManager fblManager = new FlexboxLayoutManager(this);
//        fblManager.setFlexWrap(FlexWrap.WRAP);
//        fblManager.setFlexDirection(FlexDirection.ROW);
//        fblManager.setJustifyContent(JustifyContent.FLEX_START);
//        _recyclerViewRecipient.setLayoutManager(fblManager);
//        _recyclerViewRecipient.setAdapter(_recipientListAdapter);
//        tvRecipientEmpty = findViewById(R.id.tv_chipgroup_empty);
//        initChipGroup();
        //endregion
    }
//    private void initChipGroup() {
//        _recipientList.clear();
//        _recipientList.addAll(_recipientSet);
//        Collections.sort(_recipientList);
//        _recipientListAdapter.notifyDataSetChanged();
//        tvRecipient.setText(getString(R.string.content_recipient_counts, _recipientList.size()));
//        if(boardEditMode == Constants.BoardEditMode.New) {
//            if (_recipientList.isEmpty()) {
//                _recyclerViewRecipient.setVisibility(View.GONE);
//                tvRecipientEmpty.setVisibility(View.VISIBLE);
//            } else {
//                _recyclerViewRecipient.setVisibility(View.VISIBLE);
//                tvRecipientEmpty.setVisibility(View.GONE);
//            }
//        }else{
//            if (_recipientList.isEmpty()) {
//                _recyclerViewRecipient.setVisibility(View.GONE);
//            } else {
//                _recyclerViewRecipient.setVisibility(View.VISIBLE);
//            }
//            tvRecipientEmpty.setVisibility(View.GONE);
//        }
//    }
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
    void initIntentData() {
        Intent intent = getIntent();
        if(intent != null) {
            if(intent.hasExtra(IntentParams.PARAM_STU_ACACODE)) {
                _acaCode = intent.getStringExtra(IntentParams.PARAM_STU_ACACODE);
            }
            if(intent.hasExtra(IntentParams.PARAM_STU_GRADECODE)) {
                _gubunCode = intent.getStringExtra(IntentParams.PARAM_STU_GRADECODE);
            }
            if (intent.hasExtra(IntentParams.PARAM_BOARD_ITEM)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    _currentData = intent.getParcelableExtra(IntentParams.PARAM_BOARD_ITEM, QnaDetailData.class);
                } else {
                    _currentData = intent.getParcelableExtra(IntentParams.PARAM_BOARD_ITEM);
                }
                _acaCode = _currentData.acaCode;
                _gubunCode = _currentData.acaGubunCode;
                boardEditMode = Constants.BoardEditMode.Edit;
                if(intent.hasExtra(IntentParams.PARAM_IS_REPLY)) {
                    boardEditMode = Constants.BoardEditMode.Reply;
                }
            } else {
                boardEditMode = Constants.BoardEditMode.New;
            }

        }
        //LogMgr.w("currentData :" + _currentData.toString());

    }
    void initData() {
        if(boardEditMode == Constants.BoardEditMode.New){
            layoutBottom.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(_acaCode)) {

                Optional option = _ACAList.stream().filter(t -> t.acaCode.equals(_acaCode)).findFirst();
                if (option.isPresent()) {
                    selectedACA = (ACAData) option.get();
                }
                if (selectedACA != null) {
                    int selectedIndex = _ACAList.indexOf(selectedACA);
                    spinnerCampus.selectItemByIndex(selectedIndex);
                }

            }
            spinnerCampus.setEnabled(true);
        }else if(boardEditMode == Constants.BoardEditMode.Edit){
            layoutBottom.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(_currentData.acaCode)) {

                Optional option = _ACAList.stream().filter(t -> t.acaCode.equals(_currentData.acaCode)).findFirst();
                if (option.isPresent()) {
                    selectedACA = (ACAData) option.get();
                }

                if (selectedACA != null) {
                    int selectedIndex = _ACAList.indexOf(selectedACA);
                    spinnerCampus.selectItemByIndex(selectedIndex);
                }

            }
            if(!TextUtils.isEmpty(_currentData.title)){
                etSubject.setText(_currentData.title);
            }
            if(!TextUtils.isEmpty(_currentData.content)){
                etContent.setText(_currentData.content);
            }
            if(!TextUtils.isEmpty(_currentData.reply)){
                etAnswer.setText(_currentData.reply);
            }
            spinnerCampus.setEnabled(false);
            spinnerGrade.setEnabled(false);
            etSubject.setEnabled(true);
            etContent.setEnabled(true);
            cbIsNotice.setChecked("Y".equals(_currentData.isMain));
            originalIsMain = cbIsNotice.isChecked();
            cbIsPrivate.setChecked(!"Y".equals(_currentData.isOpen));
        }else if(boardEditMode == Constants.BoardEditMode.Reply){
            layoutBottom.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(_currentData.acaCode)) {

                Optional option = _ACAList.stream().filter(t -> t.acaCode.equals(_currentData.acaCode)).findFirst();
                if (option.isPresent()) {
                    selectedACA = (ACAData) option.get();
                }

                if (selectedACA != null) {
                    int selectedIndex = _ACAList.indexOf(selectedACA);
                    spinnerCampus.selectItemByIndex(selectedIndex);
                }

            }
            if(!TextUtils.isEmpty(_currentData.title)){
                etSubject.setText(_currentData.title);
                etSubject.setEnabled(false);
            }
            if(!TextUtils.isEmpty(_currentData.content)){
                etContent.setText(_currentData.content);
                etContent.setEnabled(false);
            }
            spinnerCampus.setEnabled(false);
            spinnerGrade.setEnabled(false);
            _handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showKeyboard(etAnswer);
                }
            }, 500);

        }

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
                if(checkForUpdate()){
                    if(boardEditMode == Constants.BoardEditMode.Reply) {
                        showMessageDialog(getString(R.string.dialog_title_alarm)
                                , getString(R.string.msg_confirm_answer)
                                , new View.OnClickListener() {  //OKClickListener
                                    @Override
                                    public void onClick(View view) {
                                        requestUpdateQna();
                                        hideMessageDialog();
                                    }
                                },
                                new View.OnClickListener() {    //cancelClickListener
                                    @Override
                                    public void onClick(View view) {
                                        hideMessageDialog();
                                    }
                                });
                    }else{
                        requestUpdateQna();
                    }
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
//            case R.id.btn_append_recipient: //수신인 추가
//                navigate2AppendRecipientActivity();
//                break;
            case R.id.layout_allow_notice:
                cbIsNotice.setChecked(!cbIsNotice.isChecked());
                break;
            case R.id.layout_allow_private:
                cbIsPrivate.setChecked(!cbIsPrivate.isChecked());
                break;
        }
    }
    private boolean checkForUpdate() {
        if(selectedACA == null) {   //캠퍼스 선택
            Toast.makeText(mContext, R.string.error_message_unselected_campus, Toast.LENGTH_SHORT).show();
            return false;
        }
        if(selectedGrade == null) {   //등급 선택
            Toast.makeText(mContext, R.string.msg_empty_school_grade, Toast.LENGTH_SHORT).show();
            return false;
        }
        if(Utils.isEmptyContainSpace(etSubject.getText())) {   //제목
            showKeyboard(etSubject);
            Toast.makeText(mContext, R.string.error_message_empty_subject, Toast.LENGTH_SHORT).show();
            return false;
        }
        if(Utils.isEmptyContainSpace(etContent.getText())) {   //내용
            showKeyboard(etContent);
            Toast.makeText(mContext, R.string.error_message_empty_content, Toast.LENGTH_SHORT).show();
            return false;
        }
        if(Utils.isEmptyContainSpace(etAnswer.getText())) {   //내용
            showKeyboard(etAnswer);
            Toast.makeText(mContext, R.string.error_message_empty_answer, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    private BaseRequest buildRequestData() {
        _currentData.title = etSubject.getText().toString();
        _currentData.content = etContent.getText().toString();
        _currentData.acaCode = selectedACA.acaCode;
        BaseRequest request = null;
        if(boardEditMode == Constants.BoardEditMode.New) {
            request = new QnaAddRequest(
                    _memberSeq,
                    _memberName,
                    _userGubun,
                    selectedACA.acaCode,
                    selectedACA.acaName,
                    selectedGrade.gubunCode,
                    selectedGrade.gubunName,
                    etSubject.getText().toString(),
                    etContent.getText().toString(),
                    cbIsPrivate.isChecked()? "N" : "Y",
                    cbIsNotice.isChecked()? "Y" : "N",
                    etAnswer.getText().toString(),
                    _memberSeq,
                    _memberName,
                    _sfCode
            );

        }else if(boardEditMode == Constants.BoardEditMode.Edit){
            request = new QnaUpdateRequest(
                    _currentData.seq,
                    _userGubun,
                    etSubject.getText().toString(),
                    etContent.getText().toString(),
                    cbIsPrivate.isChecked()? "N" : "Y",
                    cbIsNotice.isChecked()? "Y" : "N",
                    etAnswer.getText().toString()
            );
        }else if(boardEditMode == Constants.BoardEditMode.Reply) {
            request = new QnaReplyRequest(
                    _currentData.seq,
                    etAnswer.getText().toString(),
                    _memberSeq,
                    _memberName,
                    _sfCode
                    );
        }

        return request;
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
    private void requestUpdateQna() {

        showProgressDialog();
        _currentRequest = buildRequestData();
        if(boardEditMode == Constants.BoardEditMode.New) {

            RetrofitClient.getApiInterface().addQnaViaManager((QnaAddRequest) _currentRequest).enqueue(new Callback<StringResponse>(){

                @Override
                public void onResponse(Call<StringResponse> call, Response<StringResponse> response) {
                    hideProgressDialog();
                    if(response.isSuccessful()) {
                        Intent intent = getIntent();
                        intent.putExtra(IntentParams.PARAM_BOARD_ADDED, true);
                        setResult(RESULT_OK, intent);
                        finish();
                    }else{
                        Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<StringResponse> call, Throwable t) {
                    hideProgressDialog();
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }else if(boardEditMode == Constants.BoardEditMode.Edit) {
            mRetrofitApi.updateQna((QnaUpdateRequest) _currentRequest).enqueue(new Callback<BaseResponse>(){
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    hideProgressDialog();
                    if(response.isSuccessful()) {
                        Intent intent = getIntent();
                        intent.putExtra(IntentParams.PARAM_BOARD_EDITED, true);
                        intent.putExtra(IntentParams.PARAM_IS_REQUIRE_UPDATE, originalIsMain != (cbIsNotice.isChecked()));
                        setResult(RESULT_OK, intent);
                        finish();
//                        Toast.makeText(mContext, R.string.qna_update_success, Toast.LENGTH_SHORT).show();
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
        }else if(boardEditMode == Constants.BoardEditMode.Reply) {
            RetrofitClient.getApiInterface().updateReply((QnaReplyRequest) _currentRequest).enqueue(new Callback<BaseResponse>(){

                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    hideProgressDialog();
                    if(response.isSuccessful()) {
                        Intent intent = getIntent();
                        intent.putExtra(IntentParams.PARAM_BOARD_EDITED, true);
                        setResult(RESULT_OK, intent);
                        finish();
//                        Toast.makeText(mContext, R.string.qna_answer_success, Toast.LENGTH_SHORT).show();
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