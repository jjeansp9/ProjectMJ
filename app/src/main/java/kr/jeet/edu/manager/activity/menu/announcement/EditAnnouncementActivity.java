package kr.jeet.edu.manager.activity.menu.announcement;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.io.File;
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
import kr.jeet.edu.manager.activity.AppendRecipientActivity;
import kr.jeet.edu.manager.activity.BaseActivity;
import kr.jeet.edu.manager.adapter.AttachFileListAdapter;
import kr.jeet.edu.manager.adapter.AttachImageListAdapter;
import kr.jeet.edu.manager.adapter.RecipientChipListAdapter;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.DataManager;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.model.data.ACAData;
import kr.jeet.edu.manager.model.data.AnnouncementData;
import kr.jeet.edu.manager.model.data.AttachFileData;
import kr.jeet.edu.manager.model.data.BoardAttributeData;
import kr.jeet.edu.manager.model.data.FileData;
import kr.jeet.edu.manager.model.data.RecipientData;
import kr.jeet.edu.manager.model.data.StudentGradeData;
import kr.jeet.edu.manager.model.request.AnnouncementRequest;
import kr.jeet.edu.manager.model.request.RecipientRequest;
import kr.jeet.edu.manager.model.response.BaseResponse;
import kr.jeet.edu.manager.model.response.BoardRegisterResponse;
import kr.jeet.edu.manager.model.response.StudentGradeListResponse;
import kr.jeet.edu.manager.server.RetrofitApi;
import kr.jeet.edu.manager.server.RetrofitClient;
import kr.jeet.edu.manager.utils.FileUtils;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.PreferenceUtil;
import kr.jeet.edu.manager.utils.Utils;
import kr.jeet.edu.manager.view.CustomAppbarLayout;
import kr.jeet.edu.manager.view.LimitableEditText;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditAnnouncementActivity extends BaseActivity {
    private static final String TAG = "editAnnouncement";
    private static final int CMD_GET_ACA_LIST = 0;
    private static final int CMD_GET_GRADE_LIST = 1;
    private int MaxFileAttachCount = 10;
    //views
    PowerSpinnerView spinnerCampus, spinnerGrade;
    NestedScrollView scrollView;
    ImageButton btnAttachPhoto, btnAttachFile;
    EditText etSubject;
    LimitableEditText etContent;
    RecyclerView _recyclerViewAttachedImage;
    RecyclerView _recyclerViewAttachedFile;
    LinearLayout layoutSendSMS; //layoutSendNotify,
    CheckBox cbIsSendSMS; //cbIsSendNotify
    TextView tvFileCount, tvMaxFileDescription;
    RecyclerView _recyclerViewRecipient;
    Button btnAppendRecipient;
    TextView tvRecipient, tvRecipientEmpty;
    LinearLayoutCompat layoutBottom;

    AttachImageListAdapter _attachImageListAdapter;
    AttachFileListAdapter _attachFileListAdapter;
    RecipientChipListAdapter _recipientListAdapter;
    List<ACAData> _ACAList = new ArrayList<>();
    List<String> _ACANameList = new ArrayList<>();
    ACAData selectedACA = null;
    List<StudentGradeData> _GradeList = new ArrayList<>();
    List<String> _GradeNameList = new ArrayList<>();
    StudentGradeData selectedGrade = null;
    List<AttachFileData> _selectedImages = new ArrayList<>();
    List<AttachFileData> _selectedFiles = new ArrayList<>();
    int _attachedCount = 0;
    AnnouncementData _currentData = new AnnouncementData();
    List<Integer> _deleteFileSeqList = new ArrayList<>();
    List<RecipientData> _recipientList = new ArrayList<>();
    Set<RecipientData> _recipientSet = new HashSet<>();

    private RetrofitApi mRetrofitApi;
    String _acaCode = "";
    String _gubunCode = "";
    int _userGubun = 1;
    int _seq = 0;
    int _sfCode = 0;
    Constants.BoardEditMode boardEditMode = Constants.BoardEditMode.New;

    private Handler _handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {

                case CMD_GET_ACA_LIST:
                    break;
                case CMD_GET_GRADE_LIST:
                    if (_GradeList != null) {
//                        if(boardEditMode == Constants.BoardEditMode.New) {
//                            if (spinnerGrade != null) spinnerGrade.setEnabled(true);
//                        }
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_announcement);
        mContext = this;
        _userGubun = PreferenceUtil.getUserGubun(this);
        _seq = PreferenceUtil.getUserSeq(this);
        _sfCode = PreferenceUtil.getUserSFCode(this);
//        _acaCode = PreferenceUtil.getAcaCode(this);
        BoardAttributeData boardData = DataManager.getInstance().getBoardInfo(DataManager.BOARD_NOTICE);
        if(boardData != null) {
            MaxFileAttachCount = boardData.atchPosblFileNum;
        }
        LogMgr.e(_userGubun + "/" + _seq + "/" + _sfCode + " / maxCount = " + MaxFileAttachCount);

        initIntentData();
        initView();
        initAppbar();
        initData();
    }

    void initView() {

        layoutBottom = findViewById(R.id.layout_bottom);

        etSubject = findViewById(R.id.et_subject);
        etContent = findViewById(R.id.et_content);
        btnAttachPhoto = findViewById(R.id.btn_attach_photo);
        btnAttachPhoto.setOnClickListener(this);
        btnAttachFile = findViewById(R.id.btn_attach_file);
        btnAttachFile.setOnClickListener(this);
        scrollView = findViewById(R.id.scrollview);
        spinnerCampus = findViewById(R.id.spinner_campus);
        spinnerGrade = findViewById(R.id.spinner_grade);
//        layoutSendNotify = findViewById(R.id.layout_allow_notify);
//        layoutSendNotify.setOnClickListener(this);
        tvRecipient = findViewById(R.id.tv_recipient_count);
        btnAppendRecipient = findViewById(R.id.btn_append_recipient);
        btnAppendRecipient.setOnClickListener(this);
        layoutSendSMS = findViewById(R.id.layout_send_sms);
        if(boardEditMode == Constants.BoardEditMode.New) {
            layoutSendSMS.setVisibility(View.VISIBLE);
            layoutSendSMS.setOnClickListener(this);
        }else{
            layoutSendSMS.setVisibility(View.GONE);
        }
//        cbIsSendNotify = findViewById(R.id.check_notify);
        cbIsSendSMS = findViewById(R.id.check_sms);
        tvFileCount = findViewById(R.id.tv_count);
        //ACA list for spinner
        //전체 추가 -> 전체 제거

        _ACAList.add(new ACAData(getString(R.string.item_total), getString(R.string.item_total), Constants.MAIN_CONTACT)); // 전체
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

                if (selectedACA.acaCode.equals(getString(R.string.item_total))) {
                    try {
                        selectedGrade = new StudentGradeData(String.valueOf(Constants.GRADE_TOTAL_CODE), getString(R.string.item_total));
                        LogMgr.w("selectedACA. selectedGrade = " + selectedGrade.gubunCode + " / " +selectedGrade.gubunName);
                        spinnerGrade.setText(""); // 이렇게 안하면 UI가 뭔가 어색함
                        spinnerGrade.setHint(selectedGrade.gubunName);
                        spinnerGrade.setEnabled(false);

                    }catch (Exception e) {}
                } else {
                    if(boardEditMode == Constants.BoardEditMode.New) {
                        if (selectedGrade != null) selectedGrade = null;
                        if (spinnerGrade != null) {
                            spinnerGrade.clearSelectedItem();
                            spinnerGrade.setHint(getString(R.string.msg_empty_school_grade));
                            spinnerGrade.setEnabled(true);
                        }
                    }
                }
                requestGradeList(selectedACA.acaCode);
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
        _recyclerViewAttachedImage = findViewById(R.id.recyclerview_images);
        _attachImageListAdapter = new AttachImageListAdapter(mContext, _selectedImages, new AttachImageListAdapter.onItemClickListener() {
            @Override
            public void onItemClick(AttachFileData attachFile) {
            }
            @Override
            public void onDeleteClick(int position) {
                AttachFileData data = _selectedImages.get(position);
                if(data != null && data.seq != -1) {
                    _deleteFileSeqList.add(data.seq);
                }
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
                if(data != null && data.seq != -1) {
                    _deleteFileSeqList.add(data.seq);
                }
                _selectedFiles.remove(position);
                _attachFileListAdapter.notifyItemRemoved(position);
                tvFileCount.setText(getString(R.string.hint_file_count, _selectedImages.size() + _selectedFiles.size(), MaxFileAttachCount));
            }
        });
        _recyclerViewAttachedFile.setAdapter(_attachFileListAdapter);

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
            if (intent.hasExtra(IntentParams.PARAM_ANNOUNCEMENT_INFO)) {
                boardEditMode = Constants.BoardEditMode.Edit;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    _currentData = intent.getParcelableExtra(IntentParams.PARAM_ANNOUNCEMENT_INFO, AnnouncementData.class);
                } else {
                    _currentData = intent.getParcelableExtra(IntentParams.PARAM_ANNOUNCEMENT_INFO);
                }
            } else {
                boardEditMode = Constants.BoardEditMode.New;
            }
            if(intent.hasExtra(IntentParams.PARAM_STU_ACACODE)) {
                _acaCode = intent.getStringExtra(IntentParams.PARAM_STU_ACACODE);
            }
            if(intent.hasExtra(IntentParams.PARAM_STU_GRADECODE)) {
                _gubunCode = intent.getStringExtra(IntentParams.PARAM_STU_GRADECODE);
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
        }else {
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

            } else {
                //전체선택
//            spinner.selectItemByIndex(0);
            }
        }
        if(boardEditMode == Constants.BoardEditMode.New) {
            spinnerCampus.setEnabled(true);
//            spinnerGrade.setEnabled(true);
            tvFileCount.setText(getString(R.string.hint_file_count, 0, MaxFileAttachCount));
            btnAppendRecipient.setVisibility(View.VISIBLE);
        }else if(boardEditMode == Constants.BoardEditMode.Edit){
            if(!TextUtils.isEmpty(_currentData.title)){
                etSubject.setText(_currentData.title);
            }
            if(!TextUtils.isEmpty(_currentData.content)){
                etContent.setText(_currentData.content);
            }
            spinnerCampus.setEnabled(false);
            spinnerGrade.setEnabled(false);
            for(FileData fileData : _currentData.fileList) {
                AttachFileData attachFileData = FileUtils.initAttachFileFromServer(mContext, fileData);
                if(attachFileData.mimeType.startsWith("image")) {
                    _selectedImages.add(attachFileData);
                }else{
                    _selectedFiles.add(attachFileData);
                }
            }
            tvFileCount.setText(getString(R.string.hint_file_count, _currentData.fileList.size(), MaxFileAttachCount));
            _attachImageListAdapter.notifyDataSetChanged();
            _attachFileListAdapter.notifyDataSetChanged();
            btnAppendRecipient.setVisibility(View.GONE);
            if(_currentData.receiverList != null) {
                _recipientSet = new HashSet<>(_currentData.receiverList);
            }else{
                _recipientSet = new HashSet<>();
            }
            initChipGroup();
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
                    requestUpdateAnnouncement();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
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
//            case R.id.layout_allow_notify:
//                cbIsSendNotify.setChecked(!cbIsSendNotify.isChecked());
//                break;
            case R.id.layout_send_sms:
                cbIsSendSMS.setChecked(!cbIsSendSMS.isChecked());
                break;
        }
    }
    private boolean checkForUpdate() {
        if(selectedACA == null) {   //캠퍼스 선택
            Toast.makeText(mContext, R.string.error_message_unselected_campus, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!selectedACA.acaName.equals(getString(R.string.item_total))) { // 전체선택이 아닌 경우
            if(selectedGrade == null) {   //등급 선택
                Toast.makeText(mContext, R.string.msg_empty_school_grade, Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        if(Utils.isEmptyContainSpace(etSubject.getText())) {   //제목
            showKeyboard(etSubject);
            Toast.makeText(mContext, R.string.error_message_empty_subject, Toast.LENGTH_SHORT).show();
            return false;
        }
        if(Utils.isEmptyContainSpace(etContent.getText())) {   //내용
            showKeyboard(etContent.getEditText());
            Toast.makeText(mContext, R.string.error_message_empty_content, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    private RequestBody buildRequestData() {
        _currentData.title = etSubject.getText().toString();
        _currentData.content = etContent.getText();
        _currentData.acaCode = selectedACA.acaCode;
        AnnouncementRequest request = null;
        if(boardEditMode == Constants.BoardEditMode.New) {
            String smsSender = "";
            if (selectedACA.acaCode.equals(getString(R.string.item_total))) {
                smsSender = Constants.MAIN_CONTACT;
            } else {
                smsSender = DataManager.getInstance().getLocalACAData(selectedACA.acaCode).acaTel.replaceAll("[^0-9]", "");
            }
            String acaName = "";
            if (selectedACA != null && selectedACA.acaName != null) {
                if (selectedACA.acaName.equals(getString(R.string.item_total))) {
                    acaName = getString(R.string.item_total_campus);
                } else {
                    acaName = selectedACA.acaName;
                }
            }
            if (selectedGrade != null) {
                // 캠퍼스 전체로 선택시 캠퍼스구분코드 0, 구분이름은 "전체"로
                request = AnnouncementRequest.initNewDataFromAnnouncementData(_currentData, selectedACA.acaCode, acaName, String.valueOf(selectedGrade.gubunCode), selectedGrade.gubunName, _seq, /*cbIsSendNotify.isChecked(),*/ cbIsSendSMS.isChecked(), smsSender);
            }

        }else if(boardEditMode == Constants.BoardEditMode.Edit){
            request = AnnouncementRequest.initUpdateDataFromAnnouncementData(_currentData, _deleteFileSeqList);
        }
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
                            try {
                                _GradeList.add(new StudentGradeData(String.valueOf(Constants.GRADE_TOTAL_CODE), getString(R.string.item_total)));
                            }catch (Exception e) {}

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
    private void requestUpdateAnnouncement() {
//        if(RetrofitClient.getInstance() != null) {
//            mRetrofitApi = RetrofitClient.getApiInterface();
//            showProgressDialog();
//            RequestBody reqBody = buildRequestData();
//            List<MultipartBody.Part> reqMultipartBodyList = buildRequestFiles();
//            if(boardEditMode == Constants.BoardEditMode.New) {
//                mRetrofitApi.insertAnnouncement(reqBody, reqMultipartBodyList).enqueue(new Callback<BaseResponse>(){
//
//                    @Override
//                    public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
//                        hideProgressDialog();
//                        if(response.isSuccessful()) {
//                            Intent intent = getIntent();
//                            intent.putExtra(IntentParams.PARAM_BOARD_ADDED, true);
//                            setResult(RESULT_OK, intent);
//                            finish();
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Call<BaseResponse> call, Throwable t) {
//                        hideProgressDialog();
//                    }
//                });
//            }else if(boardEditMode == Constants.BoardEditMode.Edit) {
//                mRetrofitApi.updateAnnouncement(reqBody, reqMultipartBodyList).enqueue(new Callback<BaseResponse>(){
//                    @Override
//                    public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
//                        hideProgressDialog();
//                        if(response.isSuccessful()) {
//                            Intent intent = getIntent();
//                            intent.putExtra(IntentParams.PARAM_BOARD_EDITED, true);
//                            setResult(RESULT_OK, intent);
//                            finish();
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Call<BaseResponse> call, Throwable t) {
//                        hideProgressDialog();
//                    }
//                });
//            }
//        }
        showProgressDialog();
        RequestBody reqBody = buildRequestData();
        List<MultipartBody.Part> reqMultipartBodyList = buildRequestFiles();
        if(boardEditMode == Constants.BoardEditMode.New) {

            RetrofitClient.getApiInterface().insertAnnouncement(reqBody, reqMultipartBodyList).enqueue(new Callback<BoardRegisterResponse>(){

                @Override
                public void onResponse(Call<BoardRegisterResponse> call, Response<BoardRegisterResponse> response) {
//                        hideProgressDialog();
                    if(response.isSuccessful()) {
                        int ptSeq = 0;
                        if(response.body() != null) {
                            ptSeq =  response.body().data;
                        }
                        if(ptSeq > 0) {
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
                public void onFailure(Call<BoardRegisterResponse> call, Throwable t) {
                    hideProgressDialog();
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }else if(boardEditMode == Constants.BoardEditMode.Edit) {
            mRetrofitApi.updateAnnouncement(reqBody, reqMultipartBodyList).enqueue(new Callback<BaseResponse>(){
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
    private void requestUpdateRecipient(int seq, int currentPage, int totalPage, boolean prevResult) {
        LogMgr.w(TAG, "requestUpdateRecipient  " + seq + " /" + currentPage + " / " +  prevResult);
        RecipientRequest request = new RecipientRequest();
        request.seq = seq;
        request.sfCode = _sfCode;
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
        request.smsSender = selectedACA.acaTel;

        if (RetrofitClient.getInstance() != null){
            int finalCurrentPage = currentPage;
            RetrofitClient.getLongTimeApiInterface().updateAnnounceRecipient(request).enqueue(new Callback<BaseResponse>() {
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
    public void navigate2AppendRecipientActivity() {
        Intent appendIntent = new Intent(mContext, AppendRecipientActivity.class);
        appendIntent.putExtra(IntentParams.PARAM_STU_ACACODE, PreferenceUtil.getAcaCode(mContext));
        appendIntent.putExtra(IntentParams.PARAM_RECIPIENT_FILTERTYPE, Constants.RecipientFilterType.TYPE_SCHOOL);
        appendIntent.putExtra(IntentParams.PARAM_IGNORE_RECIPIENT_TYPE, false); //Main에서 가져온 RecipientType 사용
        recipientResultLauncher.launch(appendIntent);
    }
}