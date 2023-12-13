package kr.jeet.edu.manager.activity.menu.notice;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.gson.Gson;
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;
import com.skydoves.powerspinner.OnSpinnerOutsideTouchListener;
import com.skydoves.powerspinner.PowerSpinnerView;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.activity.AppendRecipientActivity;
import kr.jeet.edu.manager.activity.BaseActivity;
import kr.jeet.edu.manager.adapter.AttachFileListAdapter;
import kr.jeet.edu.manager.adapter.AttachImageListAdapter;
import kr.jeet.edu.manager.adapter.RecipientChipListAdapter;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.DataManager;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.model.data.ACAData;
import kr.jeet.edu.manager.model.data.AttachFileData;
import kr.jeet.edu.manager.model.data.BoardAttributeData;
import kr.jeet.edu.manager.model.data.NoticeData;
import kr.jeet.edu.manager.model.data.RecipientData;
import kr.jeet.edu.manager.model.data.StudentGradeData;
import kr.jeet.edu.manager.model.request.NoticeRequest;
import kr.jeet.edu.manager.model.request.RecipientRequest;
import kr.jeet.edu.manager.model.response.BaseResponse;
import kr.jeet.edu.manager.model.response.BoardRegisterResponse;
import kr.jeet.edu.manager.model.response.StudentGradeListResponse;
import kr.jeet.edu.manager.server.RetrofitClient;
import kr.jeet.edu.manager.utils.FileUtils;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.PreferenceUtil;
import kr.jeet.edu.manager.utils.Utils;
import kr.jeet.edu.manager.view.CustomAppbarLayout;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditNoticeActivity extends BaseActivity {
    //region constants
    private static final String TAG = "edit_notice";
    private static final int CMD_GET_ACA_LIST = 0;
    private static final int CMD_GET_GRADE_LIST = 1;
    //endregion
    //region member views
    PowerSpinnerView spinnerCampus, spinnerGrade;
    ImageButton btnAttachPhoto, btnAttachFile;
    EditText etTitle, etContent;
    RecyclerView _recyclerViewAttachedImage;
    RecyclerView _recyclerViewAttachedFile;
    RecyclerView _recyclerViewRecipient;
    Button btnAppendRecipient;
    TextView tvFileCount, tvRecipient, tvRecipientEmpty;
//    ChipGroup recipientChipGroup;
    LinearLayout layoutCheckSMS;
    NestedScrollView scrollView;
    LinearLayoutCompat layoutBottom;
    CheckBox cbSendSMS;
    //endregion

    //region member variables
    AttachImageListAdapter _attachImageListAdapter;
    AttachFileListAdapter _attachFileListAdapter;
    RecipientChipListAdapter _recipientListAdapter;
    List<ACAData> _ACAList = new ArrayList<>();
    List<String> _ACANameList = new ArrayList<>();
    ACAData selectedACA = null;
    List<StudentGradeData> _GradeList = new ArrayList<>();
    StudentGradeData _selectedGrade = null;
    List<AttachFileData> _selectedImages = new ArrayList<>();
    List<AttachFileData> _selectedFiles = new ArrayList<>();
    int _attachedCount = 0;
    NoticeData _currentData = new NoticeData();
    List<RecipientData> _recipientList = new ArrayList<>();
    Set<RecipientData> _recipientSet = new HashSet<>();
//    private RetrofitApi mRetrofitApi;
    String _acaCode = "";
    String _gubunCode = "";
    int _userGubun = 1;
    int _seq = 0;
    int _sfCode = 0;
    //endregion
    private int MaxFileAttachCount = 10;
    ActivityResultLauncher imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() == RESULT_OK &&  result.getData() != null) {
                Intent intent = result.getData();
                ClipData clipData = intent.getClipData();
                boolean isOverFlow = false;
                boolean isDuplicate = false;
                if(clipData != null) {
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        _attachedCount = _selectedFiles.size() + _selectedImages.size();
                        if(_attachedCount >= MaxFileAttachCount) {
                            isOverFlow = true;
                            break;
                        }
                        Uri uri = clipData.getItemAt(i).getUri();

//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        isDuplicate = _selectedImages.stream().anyMatch(t -> t.uri.equals(uri));
//                        }else{
//                            for(AttachFileData data : _selectedImages) {
//                                if(data.uri.equals(uri)) {
//                                    isDuplicate = true;
//                                    break;
//                                }
//                            }
//                        }
                        if(isDuplicate) continue;
                        AttachFileData attachFileData = FileUtils.initAttachFileDataInstance(mContext, uri);
                        _selectedImages.add(attachFileData);
                    }
                    if(clipData.getItemCount() > 0) {
                        _attachImageListAdapter.notifyDataSetChanged();
                    }
                    if(isOverFlow) {
                        Toast.makeText(mContext, getString(R.string.board_update_overflow, MaxFileAttachCount), Toast.LENGTH_SHORT).show();
                    }else if(isDuplicate) {
                        Toast.makeText(mContext, R.string.board_update_file_duplicate_list, Toast.LENGTH_SHORT).show();
                    }
                }else if(intent.getData() != null){
                    Uri uri = intent.getData();
                    _attachedCount = _selectedFiles.size() + _selectedImages.size();
                    if(_attachedCount >= MaxFileAttachCount) {
                        isOverFlow = true;
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        isDuplicate = _selectedImages.stream().anyMatch(t -> t.uri.equals(uri));
                    }else{
                        for(AttachFileData data : _selectedImages) {
                            if(data.uri.equals(uri)) {
                                isDuplicate = true;
                                break;
                            }
                        }
                    }
                    if(isOverFlow) {
                        Toast.makeText(mContext, getString(R.string.board_update_overflow, MaxFileAttachCount), Toast.LENGTH_SHORT).show();
                    }else if(isDuplicate) {
                        Toast.makeText(mContext, R.string.board_update_file_duplicate, Toast.LENGTH_SHORT).show();
                    }else{
                        AttachFileData attachFileData = FileUtils.initAttachFileDataInstance(mContext, uri);
                        _selectedImages.add(attachFileData);
                        _attachImageListAdapter.notifyItemInserted(_selectedImages.size());
                    }
                }
            }else{
                LogMgr.w("result.getData() == null");
            }
            tvFileCount.setText(getString(R.string.hint_file_count, _selectedImages.size() + _selectedFiles.size(), MaxFileAttachCount));
        }
    });
    ActivityResultLauncher filePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() == RESULT_OK &&  result.getData() != null) {
                Intent intent = result.getData();
                ClipData clipData = intent.getClipData();
                boolean isOverFlow = false;
                boolean isDuplicate = false;
                if(clipData != null) {
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        _attachedCount = _selectedFiles.size() + _selectedImages.size();
                        if(_attachedCount >= MaxFileAttachCount) {
                            isOverFlow = true;
                            break;
                        }
                        Uri uri = clipData.getItemAt(i).getUri();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            isDuplicate = _selectedFiles.stream().anyMatch(t -> t.uri.equals(uri));
                        }else{
                            for(AttachFileData data : _selectedFiles) {
                                if(data.uri.equals(uri)) {
                                    isDuplicate = true;
                                    break;
                                }
                            }
                        }
                        if(isDuplicate) continue;
                        AttachFileData attachFileData = FileUtils.initAttachFileDataInstance(mContext, uri);
                        _selectedFiles.add(attachFileData);
                    }
                    if (clipData.getItemCount() > 0) {
                        _attachFileListAdapter.notifyDataSetChanged();
                    }
                    if(isOverFlow) {
                        Toast.makeText(mContext, getString(R.string.board_update_overflow, MaxFileAttachCount), Toast.LENGTH_SHORT).show();
                    }else if(isDuplicate) {
                        Toast.makeText(mContext, R.string.board_update_file_duplicate_list, Toast.LENGTH_SHORT).show();
                    }
                }else if(intent.getData() != null){
                    Uri uri = intent.getData();
                    _attachedCount = _selectedFiles.size() + _selectedImages.size();
                    if(_attachedCount >= MaxFileAttachCount) {
                        isOverFlow = true;
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        isDuplicate = _selectedFiles.stream().anyMatch(t -> t.uri.equals(uri));
                    }else{
                        for(AttachFileData data : _selectedFiles) {
                            if(data.uri.equals(uri)) {
                                isDuplicate = true;
                                break;
                            }
                        }
                    }
                    if(isOverFlow) {
                        Toast.makeText(mContext, getString(R.string.board_update_overflow, MaxFileAttachCount), Toast.LENGTH_SHORT).show();
                    }else if(isDuplicate) {
                        Toast.makeText(mContext, R.string.board_update_file_duplicate, Toast.LENGTH_SHORT).show();
                    }else{
                        AttachFileData attachFileData = FileUtils.initAttachFileDataInstance(mContext, uri);
                        _selectedFiles.add(attachFileData);
                        _attachFileListAdapter.notifyItemInserted(_selectedFiles.size());
                    }

                }
            }else{
                LogMgr.w("result.getData() == null");
            }
            tvFileCount.setText(getString(R.string.hint_file_count, _selectedImages.size() + _selectedFiles.size(), MaxFileAttachCount));
        }
    });
    //작성화면 resultLauncher
    ActivityResultLauncher<Intent> recipientResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() != RESULT_CANCELED) {
                Intent intent = result.getData();
                if(intent == null) return;
                if(intent.hasExtra(IntentParams.PARAM_EXIST_RECIPIENT_INFO)) {
                    if(intent.getBooleanExtra(IntentParams.PARAM_EXIST_RECIPIENT_INFO, false)) {
                        showProgressDialog();
                        List<RecipientData> list = DataManager.getInstance().getRecipientList();
                        LogMgr.w(TAG, "list count = " + list.size());

                        if (!list.isEmpty()) {
                        _recipientSet.addAll(list);
//                                _recipientList.addAll(list);
                            }

                        initChipGroup();
                        hideProgressDialog();
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
                    if (_GradeList != null) {
                        boolean isSelectNow = (boolean) msg.obj;

                        spinnerGrade.setEnabled(true);
//                        spinnerDept.setItems(_DeptList.stream().map(t -> t.deptName).collect(Collectors.toList()));
                        Utils.updateSpinnerList(spinnerGrade, _GradeList.stream().map(t -> t.gubunName).collect(Collectors.toList()));
                        //
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
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_notice);
        mContext = this;
        _userGubun = PreferenceUtil.getUserGubun(this);
        _seq = PreferenceUtil.getUserSeq(this);
        _sfCode = PreferenceUtil.getUserSFCode(this);
//        _acaCode = PreferenceUtil.getAcaCode(this);
        BoardAttributeData boardData = DataManager.getInstance().getBoardInfo(DataManager.BOARD_SYSTEM_NOTICE);
        if(boardData != null) {
            MaxFileAttachCount = boardData.atchPosblFileNum;
        }
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
        if(intent != null) {
            if(intent.hasExtra(IntentParams.PARAM_STU_ACACODE)) {
                _acaCode = intent.getStringExtra(IntentParams.PARAM_STU_ACACODE);
            }
            if(intent.hasExtra(IntentParams.PARAM_STU_GRADECODE)) {
                _gubunCode = intent.getStringExtra(IntentParams.PARAM_STU_GRADECODE);
            }
        }
    }
    void initView() {
        scrollView = ((NestedScrollView)findViewById(R.id.scrollview));
        layoutBottom = ((LinearLayoutCompat)findViewById(R.id.layout_bottom));
        spinnerCampus = findViewById(R.id.spinner_campus);
        spinnerGrade = findViewById(R.id.spinner_grade);
        etTitle = findViewById(R.id.et_content_title);
        etContent = findViewById(R.id.et_content_content);
        btnAttachPhoto = findViewById(R.id.btn_attach_photo);
        btnAttachPhoto.setOnClickListener(this);
        btnAttachFile = findViewById(R.id.btn_attach_file);
        btnAttachFile.setOnClickListener(this);
        tvFileCount = findViewById(R.id.tv_count);
        tvRecipient = findViewById(R.id.tv_recipient_count);
        btnAppendRecipient = findViewById(R.id.btn_append_recipient);
        btnAppendRecipient.setOnClickListener(this);
        layoutCheckSMS = findViewById(R.id.layout_send_sms);
        layoutCheckSMS.setOnClickListener(this);
        cbSendSMS = findViewById(R.id.check_sms);
        //region spinner
        //ACA list for spinner
        //전체 추가 -> 전체 제거
//        _ACAList.add(new ACAData(getString(R.string.item_total), ""));
        _ACAList.addAll(DataManager.getInstance().getLocalACAListMap().values());
        if(_ACAList != null) _ACANameList = _ACAList.stream().map(t -> t.acaName).collect(Collectors.toList());
//        {
//            WrapContentSpinnerAdapter adapter = new WrapContentSpinnerAdapter(mContext, _ACANameList, spinnerCampus);
//            spinnerCampus.setSpinnerAdapter(adapter);
//        }

        spinnerCampus.setItems(_ACANameList);
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
                requestGradeList(selectedACA.acaCode, false);
                if (_selectedGrade != null) {
                    _selectedGrade = null;
                }
                if (spinnerGrade != null) spinnerGrade.clearSelectedItem();
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
        //region recyclerview
        _recyclerViewAttachedImage = findViewById(R.id.recyclerview_images);
        _attachImageListAdapter = new AttachImageListAdapter(mContext, _selectedImages, new AttachImageListAdapter.onItemClickListener() {
            @Override
            public void onItemClick(AttachFileData attachFile) {
            }
            @Override
            public void onDeleteClick(int position) {
                AttachFileData data = _selectedImages.get(position);
//                if(data != null && data.seq != -1) {
//                    _deleteFileSeqList.add(data.seq);
//                }
                _selectedImages.remove(position);
                _attachImageListAdapter.notifyItemRemoved(position);
                tvFileCount.setText(getString(R.string.hint_file_count, _selectedImages.size() + _selectedFiles.size(), MaxFileAttachCount));
            }
        });
        _recyclerViewAttachedImage.setAdapter(_attachImageListAdapter);

        _recyclerViewAttachedFile = findViewById(R.id.recyclerview_files);
        _attachFileListAdapter = new AttachFileListAdapter(mContext, _selectedFiles, AttachFileListAdapter.Action.Delete, new AttachFileListAdapter.onItemClickListener() {
            @Override
            public void onItemClick(AttachFileData attachFile) {
            }

            @Override
            public void onActionBtnClick(int position, AttachFileListAdapter.Action action) {
                AttachFileData data = _selectedFiles.get(position);
//                if(data != null && data.seq != -1) {
//                    _deleteFileSeqList.add(data.seq);
//                }
                _selectedFiles.remove(position);
                _attachFileListAdapter.notifyItemRemoved(position);
                tvFileCount.setText(getString(R.string.hint_file_count, _selectedImages.size() + _selectedFiles.size(), MaxFileAttachCount));
            }
        });
        _recyclerViewAttachedFile.setAdapter(_attachFileListAdapter);
        //endregion
        //region chip
        _recyclerViewRecipient = findViewById(R.id.recycler_chip);
        _recipientListAdapter = new RecipientChipListAdapter(mContext, _recipientList, new RecipientChipListAdapter.onItemClickListener() {
            @Override
            public void onDeleteClick(int position, RecipientData item) {
                _recipientList.remove(position);
                _recipientListAdapter.notifyItemRemoved(position);
                tvRecipient.setText(getString(R.string.content_recipient_counts, _recipientList.size()));
//                if(boardEditMode == Constants.BoardEditMode.New) {
                if (_recipientList.isEmpty()) {
                    _recyclerViewRecipient.setVisibility(View.GONE);
                    tvRecipientEmpty.setVisibility(View.VISIBLE);
                } else {
                    _recyclerViewRecipient.setVisibility(View.VISIBLE);
                    tvRecipientEmpty.setVisibility(View.GONE);
                }
//                }
            }
        });
//        _recipientListAdapter.setEditMode(boardEditMode);
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
    //    if(boardEditMode == Constants.BoardEditMode.New) {

            if (_recipientList.isEmpty()) {
                _recyclerViewRecipient.setVisibility(View.GONE);
                tvRecipientEmpty.setVisibility(View.VISIBLE);
            } else {
                _recyclerViewRecipient.setVisibility(View.VISIBLE);
                tvRecipientEmpty.setVisibility(View.GONE);
            }
    //    }
    }
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.menu_item_add);
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
                    requestUpdateNotice();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void updateCurrentData() {
        if(_currentData == null) _currentData = new NoticeData();
        _currentData.writerSeq = _seq;
        _currentData.title = etTitle.getText().toString().trim();
        _currentData.content = etContent.getText().toString().trim();
        _currentData.acaCode = selectedACA.acaCode;
        _currentData.acaName = selectedACA.acaName;
        _currentData.acaGubunCode = String.valueOf(_selectedGrade.gubunCode);
        int recipientCount = _recipientSet != null?_recipientSet.size() : 0;
//        int recipientCount = _recipientList != null?_recipientList.size() : 0;
        _currentData.receiverCnt = recipientCount;
        _currentData.isSendSMS = cbSendSMS.isChecked()? "Y" : "N";

    }
    private void openImagePicker() {
        Intent intent = new Intent();
        if (Build.VERSION_CODES.Q >= Build.VERSION.SDK_INT)
            intent.setAction(Intent.ACTION_GET_CONTENT);
        else
            intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        imagePickerLauncher.launch(Intent.createChooser(intent, "gallery"));
    }
    private void openFilePicker() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/*, text/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        filePickerLauncher.launch(Intent.createChooser(intent, "file"));
    }
    void initData() {

        //LogMgr.w("currentData :" + _currentData.toString());
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
        tvFileCount.setText(getString(R.string.hint_file_count, 0, MaxFileAttachCount));

        btnAppendRecipient.setVisibility(View.VISIBLE);

    }
    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btn_attach_photo:
                openImagePicker();
                break;
            case R.id.btn_attach_file:
                openFilePicker();
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
        if(selectedACA == null) {   //캠퍼스 선택
            spinnerCampus.requestFocus();
            Toast.makeText(mContext, R.string.error_message_unselected_campus, Toast.LENGTH_SHORT).show();
            return false;
        }
        if(_selectedGrade == null) {
            spinnerGrade.requestFocus();
            Toast.makeText(mContext, R.string.msg_empty_school_grade, Toast.LENGTH_SHORT).show();
            return false;
        }
        if(Utils.isEmptyContainSpace(etTitle.getText())) {   //제목
            showKeyboard(etTitle);
            Toast.makeText(mContext, R.string.error_message_empty_subject, Toast.LENGTH_SHORT).show();
            return false;
        }
        if(Utils.isEmptyContainSpace(etContent.getText())) {   //내용
            showKeyboard(etContent);
            Toast.makeText(mContext, R.string.error_message_empty_content, Toast.LENGTH_SHORT).show();
            return false;
        }
        if(_recipientList.size() < 1) {   //수신인

            showBtnAnimation();

            Toast.makeText(mContext, R.string.error_message_empty_recipient, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    private void showBtnAnimation() {
        int[] location = new int[2];
        btnAppendRecipient.getLocationOnScreen(location);

        int scrollDuration = 500;
        int duration = getResources().getInteger(R.integer.btn_push_duration) + 100;

        int screenHeight = getResources().getDisplayMetrics().heightPixels; // 디바이스 화면의 높이
        int resultLocation = location[1] + layoutBottom.getHeight();

        if (resultLocation >= screenHeight) scrollView.smoothScrollTo(0 ,location[1], scrollDuration);

        _handler.postDelayed(() -> {
            btnAppendRecipient.setPressed(true);
            _handler.postDelayed(() -> btnAppendRecipient.setPressed(false), duration);
        }, scrollDuration);
    }
    private RequestBody buildRequestData() {
        _currentData.title = etTitle.getText().toString();
        _currentData.content = etContent.getText().toString();
        _currentData.acaCode = selectedACA.acaCode;
        NoticeRequest request = null;
        int recipientCnt = (_recipientSet != null)? _recipientSet.size() : 0;
//        int recipientCnt = (_recipientList != null)? _recipientList.size() : 0;
        request = NoticeRequest.initNewData(
                _seq,
                _currentData.title,
                _currentData.content,
                cbSendSMS.isChecked(),
                recipientCnt,
                _currentData.acaCode,
                DataManager.getInstance().getLocalACAData(_currentData.acaCode).acaName,
                _currentData.acaGubunCode,
                _selectedGrade.gubunName
        );

        String json = "";
        if(request != null) {
            json = new Gson().toJson(request);
        }
        LogMgr.e("item = " + json);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);
        return requestBody;
    }
    private List<MultipartBody.Part> buildRequestFiles() {
        List<MultipartBody.Part> multipartFilesList = new ArrayList<>();
        for(AttachFileData fileData : _selectedImages) {
            LogMgr.w(fileData.uri + " ----->");
            LogMgr.w("fileData.uri.getScheme() = " + fileData.uri.getScheme() + " / " + ContentResolver.SCHEME_CONTENT);
            if(!fileData.uri.getScheme().equalsIgnoreCase(ContentResolver.SCHEME_CONTENT)) {
                LogMgr.w("continue ~" + fileData.uri);
                continue;
            }
            AttachFileData tempFileData = FileUtils.copyTempFile(mContext, fileData);
            File tempFile = null;
            if(FileUtils.isTargetResize(tempFileData)) {
                LogMgr.e("isTargetResize");
                tempFile = FileUtils.resizeImageFile(mContext.getExternalFilesDir(null), tempFileData.tempFilePath, 1080, 1080);
            }else {
                LogMgr.e("isNotTargetResize");
                tempFile = new File(tempFileData.tempFilePath);
            }
            LogMgr.w("tempFile path =" + tempFileData.tempFilePath + " / mime=" + tempFileData.mimeType + "/" +  tempFile.getName());
            RequestBody requestBody = RequestBody.create(MediaType.parse(fileData.mimeType), tempFile);
            MultipartBody.Part part = MultipartBody.Part.createFormData("files", tempFile.getName(), requestBody);
            multipartFilesList.add(part);
        }
        for(AttachFileData fileData : _selectedFiles) {
            LogMgr.w(fileData.uri + " ----->");
            LogMgr.w("fileData.uri.getScheme() = " + fileData.uri.getScheme() + " / " + ContentResolver.SCHEME_CONTENT);
            if(!fileData.uri.getScheme().equalsIgnoreCase(ContentResolver.SCHEME_CONTENT)) {
                LogMgr.w("continue ~" + fileData.uri);
                continue;
            }
            LogMgr.e("fileData.fileName = " + fileData.fileName);
            AttachFileData tempFileData = FileUtils.copyTempFile(mContext, fileData);
            File tempFile = new File(tempFileData.tempFilePath);
            LogMgr.w("tempFile path =" + tempFileData.tempFilePath + " / mime=" + tempFileData.mimeType);
            RequestBody requestBody = RequestBody.create(MediaType.parse(fileData.mimeType), tempFile);
            MultipartBody.Part part = MultipartBody.Part.createFormData("files", tempFile.getName(), requestBody);
            multipartFilesList.add(part);
        }
        return multipartFilesList;
    }
    private void requestUpdateRecipient(int seq, int currentPage, int totalPage, boolean prevResult) {
        LogMgr.w(TAG, "requestUpdateRecipient  " + currentPage + " /" + totalPage + " / " +  prevResult);
        RecipientRequest request = new RecipientRequest();
        request.seq = seq;
        request.sfCode = _sfCode;
        request.smsSender = selectedACA.acaTel;
        if(totalPage > 0) {
            if(currentPage < totalPage) {
                request.receiverList = _recipientList.subList(currentPage * Constants.MAX_RECIPIENT_COUNT, (currentPage + 1) * Constants.MAX_RECIPIENT_COUNT);
            }else{
                request.receiverList = _recipientList.subList(currentPage * Constants.MAX_RECIPIENT_COUNT, _recipientList.size());
            }
        }else {
            request.receiverList = _recipientList;
        }

        if (RetrofitClient.getInstance() != null){
            int finalCurrentPage = currentPage;
            RetrofitClient.getLongTimeApiInterface().updateNoticeRecipient(request).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    try {
                        if(finalCurrentPage < totalPage) {
                            requestUpdateRecipient(seq, finalCurrentPage + 1, totalPage, prevResult && response.isSuccessful());
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
                        hideProgressDialog();
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
    private void requestUpdateNotice() {
        if(RetrofitClient.getInstance() != null) {
            showProgressDialog();
            RequestBody reqBody = buildRequestData();
            List<MultipartBody.Part> reqMultipartBodyList = buildRequestFiles();
            RetrofitClient.getApiInterface().insertNotice(reqBody, reqMultipartBodyList).enqueue(new Callback<BoardRegisterResponse>(){

                @Override
                public void onResponse(Call<BoardRegisterResponse> call, Response<BoardRegisterResponse> response) {
//                        hideProgressDialog();
                    if(response.isSuccessful()) {
                        int seq = 0;
                        if(response.body() != null) {
                            seq =  response.body().data;
                        }
                        if(seq > 0) {
                            int totalPage = (_recipientList.size() / Constants.MAX_RECIPIENT_COUNT);
                            int currentPage = 0;
                            requestUpdateRecipient(seq, currentPage, totalPage, true);
                        }else{
                            showMessageDialog(getString(R.string.dialog_title_error), getString(R.string.msg_fail_to_register_notice_recipient), new View.OnClickListener(){
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
                public void onFailure(Call<BoardRegisterResponse> call, Throwable t) {
                    hideProgressDialog();
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });

        }
    }
    public void navigate2AppendRecipientActivity() {
        Intent appendIntent = new Intent(mContext, AppendRecipientActivity.class);
        appendIntent.putExtra(IntentParams.PARAM_STU_ACACODE, PreferenceUtil.getAcaCode(mContext));
        appendIntent.putExtra(IntentParams.PARAM_RECIPIENT_FILTERTYPE, Constants.RecipientFilterType.TYPE_CLASS);
        appendIntent.putExtra(IntentParams.PARAM_IGNORE_RECIPIENT_TYPE, true); //Main에서 가져온 RecipientType 사용하지 않음.
        recipientResultLauncher.launch(appendIntent);
    }
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
//    private void requestBriefingRecipientData(int ptSeq){
//        if (RetrofitClient.getInstance() != null) {
//            mRetrofitApi = RetrofitClient.getApiInterface();
//            mRetrofitApi.getBriefingRecipientList(ptSeq).enqueue(new Callback<BriefingRecipientListResponse>() {
//                @Override
//                public void onResponse(Call<BriefingRecipientListResponse> call, Response<BriefingRecipientListResponse> response) {
//                    try {
//                        if (response.isSuccessful()) {
//                            List<RecipientData> getData;
//
//                            if (response.body() != null) {
//                                getData = response.body().data;
//                                if (getData != null && !getData.isEmpty()) {
//                                    _recipientList.addAll(getData);
//
//                                } else {
//                                    LogMgr.e(TAG, "ListData is null");
//                                }
//                            }
//                        } else {
//                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
//                        }
//                    } catch (Exception e) {
//                        LogMgr.e(TAG + "requestBrfReservedListData() Exception: ", e.getMessage());
//                    }
//                    initChipGroup();
//                    //mSwipeRefresh.setRefreshing(false);
//                }
//
//                @Override
//                public void onFailure(Call<BriefingRecipientListResponse> call, Throwable t) {
//                    initChipGroup();
//                    try {
//                        LogMgr.e(TAG, "requestBrfReservedListData() onFailure >> " + t.getMessage());
//                    } catch (Exception e) {
//                    }
//                    hideProgressDialog();
//                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
//                    //mSwipeRefresh.setRefreshing(false);
//                }
//            });
//        }
//    }
}