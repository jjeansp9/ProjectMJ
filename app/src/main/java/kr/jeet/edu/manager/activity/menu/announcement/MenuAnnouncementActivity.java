package kr.jeet.edu.manager.activity.menu.announcement;

import android.content.Intent;
import android.os.Build;
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

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.skydoves.powerspinner.OnSpinnerOutsideTouchListener;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.activity.BaseActivity;
import kr.jeet.edu.manager.adapter.AnnouncementListAdapter;
import kr.jeet.edu.manager.adapter.WrapContentSpinnerAdapter;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.DataManager;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.db.JeetDatabase;
import kr.jeet.edu.manager.db.PushMessage;
import kr.jeet.edu.manager.fcm.FCMManager;
import kr.jeet.edu.manager.model.data.ACAData;
import kr.jeet.edu.manager.model.data.AnnouncementData;
import kr.jeet.edu.manager.model.data.ReadData;
import kr.jeet.edu.manager.model.data.StudentGradeData;
import kr.jeet.edu.manager.model.response.AnnouncementListResponse;
import kr.jeet.edu.manager.model.response.StudentGradeListResponse;
import kr.jeet.edu.manager.server.RetrofitApi;
import kr.jeet.edu.manager.server.RetrofitClient;
import kr.jeet.edu.manager.utils.DBUtils;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.PreferenceUtil;
import kr.jeet.edu.manager.utils.Utils;
import kr.jeet.edu.manager.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuAnnouncementActivity extends BaseActivity {

    private static final String TAG = "notice"; //boardType 함께 사용
    private static final int CMD_GET_ACA_LIST = 0;
    private static final int CMD_GET_GRADE_LIST = 1;
//    private Context mContext;
    private TextView tvEmptyList;
    private RecyclerView mRecyclerView;
    private AnnouncementListAdapter mAdapter;
    private RetrofitApi mRetrofitApi;
    private PowerSpinnerView mSpinnerCampus, mSpinnerGrade;
    private SwipeRefreshLayout mSwipeRefresh;
    List<ACAData> _ACAList = new ArrayList<>();
    List<String> _ACANameList = new ArrayList<>();
    List<StudentGradeData> _GradeList = new ArrayList<>();
    private ArrayList<ReadData> mList = new ArrayList<>();
    int _userGubun = 1;
    int _memberSeq = 0;
    int _sfCode = 0;

    private String _acaCode = "";
    private String _appAcaCode = "";
//    private String _selectedAcaCode = "";
    private ACAData _selectedLocalACA = null;
    private StudentGradeData _selectedGrade = null;
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
//                        mSpinnerGrade.setEnabled(true);
                        Utils.updateSpinnerList(mSpinnerGrade, _GradeList.stream().map(t -> t.gubunName).collect(Collectors.toList()));
                        Optional optional = (_GradeList.stream().filter(t->TextUtils.isEmpty(t.gubunCode)).findFirst());
                        if(optional.isPresent()) {
                            int index = _GradeList.indexOf(optional.get());
                            mSpinnerGrade.selectItemByIndex(index);
                        }else{
                            mSpinnerGrade.selectItemByIndex(0);
                        }
                    }else {
                        requestBoardList();
                    }
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
                if(intent == null) return;
                if(intent.hasExtra(IntentParams.PARAM_BOARD_ADDED)) {
                    boolean added = intent.getBooleanExtra(IntentParams.PARAM_BOARD_ADDED, false);
                    if(added) {
                        if(_selectedLocalACA != null)
                            LogMgr.e("acaCode = " + _selectedLocalACA.acaCode);
                        requestBoardList();
                    }
                }
                else if(intent.hasExtra(IntentParams.PARAM_BOARD_DELETED)) {
                    boolean deleted = intent.getBooleanExtra(IntentParams.PARAM_BOARD_DELETED, false);
                    int position = intent.getIntExtra(IntentParams.PARAM_BOARD_POSITION, -1);
                    if(deleted && position >= 0) {
                        mList.remove(position);
                        mAdapter.notifyItemRemoved(position);
                        checkEmptyRecyclerView();
                    }
                }else if(intent.hasExtra(IntentParams.PARAM_BOARD_EDITED)) {
                    boolean edited = intent.getBooleanExtra(IntentParams.PARAM_BOARD_EDITED, false);
                    AnnouncementData changedItem = null;
                    if(intent.hasExtra(IntentParams.PARAM_BOARD_ITEM)) {
                        changedItem = Utils.getParcelableExtra(intent, IntentParams.PARAM_BOARD_ITEM, AnnouncementData.class);
                    }
                    LogMgr.w("edited =" + changedItem);
                    int position = intent.getIntExtra(IntentParams.PARAM_BOARD_POSITION, -1);
                    LogMgr.w("position =" + position);
                    if(edited && position >= 0 && changedItem != null) {
                        mList.set(position, changedItem);
                        mAdapter.notifyItemChanged(position);
                        checkEmptyRecyclerView();
                    }
                }
            }else{
                Intent intent = result.getData();
                if(intent == null) return;
                AnnouncementData changedItem = null;
                if(intent.hasExtra(IntentParams.PARAM_BOARD_ITEM)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        changedItem = intent.getParcelableExtra(IntentParams.PARAM_BOARD_ITEM, AnnouncementData.class);
                    } else {
                        changedItem = intent.getParcelableExtra(IntentParams.PARAM_BOARD_ITEM);
                    }
                }
                LogMgr.w("showed =" + changedItem);
                int position = intent.getIntExtra(IntentParams.PARAM_BOARD_POSITION, -1);
                LogMgr.w("position =" + position);
                if(position >= 0 && changedItem != null) {
                    mList.set(position, changedItem);
                    mAdapter.notifyItemChanged(position);
                }
            }
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_announcement);
        mContext = this;
        _acaCode = PreferenceUtil.getAcaCode(mContext);
        _appAcaCode = PreferenceUtil.getAppAcaCode(mContext);
        _userGubun = PreferenceUtil.getUserGubun(this);
        _memberSeq = PreferenceUtil.getUserSeq(this);
        _sfCode = PreferenceUtil.getUserSFCode(this);
        LogMgr.e(_userGubun + "/" + _memberSeq + "/" + _sfCode);
        initAppbar();
        initView();
        getData();
        changeMessageState2Read();
        setAnimMove(Constants.MOVE_DOWN);
    }
    void changeMessageState2Read() {
        new Thread(() -> {
            List<PushMessage> pushMessages = JeetDatabase.getInstance(getApplicationContext()).pushMessageDao().getMessageByReadFlagNType(false, FCMManager.MSG_TYPE_NOTICE);
            if(!pushMessages.isEmpty()) {
                for(PushMessage message : pushMessages) {
                    message.isRead = true;
                    JeetDatabase.getInstance(getApplicationContext()).pushMessageDao().update(message);
                }
            }
        }).start();
    }
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.main_menu_announcement);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    void initView(){
        tvEmptyList = findViewById(R.id.tv_empty_list);
        mSwipeRefresh = findViewById(R.id.refresh_layout);
        mRecyclerView = findViewById(R.id.recyclerview_announcement);
        mAdapter = new AnnouncementListAdapter(mContext, mList, new AnnouncementListAdapter.onItemClickListener() {
            @Override
            public void onItemClick(AnnouncementData item, int position) {
                if (item != null){
                    Intent targetIntent = new Intent(mContext, MenuAnnouncementDetailActivity.class);
//                    targetIntent.putExtra(IntentParams.PARAM_ANNOUNCEMENT_SEQ, item.seq);
                    targetIntent.putExtra(IntentParams.PARAM_ANNOUNCEMENT_INFO, item);
                    targetIntent.putExtra(IntentParams.PARAM_BOARD_POSITION, position);
                    resultLauncher.launch(targetIntent);
                    overridePendingTransition(R.anim.horizontal_enter, R.anim.horizontal_out);

                }else LogMgr.e("item is null ");
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));

        mSpinnerCampus = findViewById(R.id.spinner_campus);
        mSpinnerCampus.setIsFocusable(true);
        _ACAList.clear();
        _ACAList.add(new ACAData("", "전체", ""));
        _ACAList.addAll(DataManager.getInstance().getLocalACAListMap().values());
        if(_ACAList != null) _ACANameList = _ACAList.stream().map(t -> t.acaName).collect(Collectors.toList());
        Utils.updateSpinnerList(mSpinnerCampus, _ACANameList);
        {
            WrapContentSpinnerAdapter adapter = new WrapContentSpinnerAdapter(mContext, _ACANameList, mSpinnerCampus);
            mSpinnerCampus.setSpinnerAdapter(adapter);
        }

        mSpinnerCampus.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
            LogMgr.e(newItem + " selected");
            if(oldItem != null && oldItem.equals(newItem)) return;
            ACAData selectedData = null;
            Optional optional = _ACAList.stream().filter(t -> t.acaName == newItem).findFirst();
            if(optional.isPresent()) {
                selectedData = (ACAData) optional.get();
            }
            _selectedLocalACA = selectedData;
            if(_selectedLocalACA != null) {
                LogMgr.w("selectedACA = " + _selectedLocalACA.acaCode + " / " + _selectedLocalACA.acaName);
                if (_selectedGrade != null) {
                    _selectedGrade = null;
                }
                if (mSpinnerGrade != null) mSpinnerGrade.clearSelectedItem();
                if(!TextUtils.isEmpty(_selectedLocalACA.acaCode)) {
                    requestGradeList(_selectedLocalACA.acaCode);
                }else{
                    if(_GradeList != null) _GradeList.clear();
                    _handler.sendEmptyMessage(CMD_GET_GRADE_LIST);
                }
                if(TextUtils.isEmpty(_selectedLocalACA.acaCode)){   //전체
                    if (mSpinnerGrade != null) mSpinnerGrade.setEnabled(false);
                }else{
                    if (mSpinnerGrade != null) mSpinnerGrade.setEnabled(true);
                }

            }

        });
        mSpinnerCampus.setSpinnerOutsideTouchListener(new OnSpinnerOutsideTouchListener() {
            @Override
            public void onSpinnerOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                mSpinnerCampus.dismiss();
            }
        });
        mSpinnerCampus.setLifecycleOwner(this);

        mSpinnerGrade = findViewById(R.id.spinner_grade);
        {
            WrapContentSpinnerAdapter adapter = new WrapContentSpinnerAdapter(mContext, _GradeList.stream().map(t -> t.gubunName).collect(Collectors.toList()), mSpinnerGrade);
            mSpinnerGrade.setSpinnerAdapter(adapter);
        }

        mSpinnerGrade.setIsFocusable(true);
        mSpinnerGrade.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
            LogMgr.e(newItem + " selected");
            if(oldItem != null && oldItem.equals(newItem)) return;
            StudentGradeData selectedData = null;
            Optional optional = _GradeList.stream().filter(t -> t.gubunName == newItem).findFirst();
            if(optional.isPresent()) {
                selectedData = (StudentGradeData) optional.get();
            }
            _selectedGrade = selectedData;
            if(_selectedGrade != null) {
                LogMgr.w("selectedGubun = " + _selectedGrade.gubunCode + " / " + _selectedGrade.gubunName);
            }
            requestBoardList();
        });
        mSpinnerGrade.setSpinnerOutsideTouchListener(new OnSpinnerOutsideTouchListener() {
            @Override
            public void onSpinnerOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                mSpinnerGrade.dismiss();
            }
        });
        mSpinnerGrade.setLifecycleOwner(this);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestBoardList(); // spinner에서 선택한 캠퍼스 코드로 갱신
//                mSwipeRefresh.setRefreshing(false);
            }
        });

//        mRecyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
//            @Override
//            public void onScrollChange(View view, int i, int i1, int i2, int i3) {
//
//            }
//        });
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(((!mRecyclerView.canScrollVertically(1)) && mRecyclerView.canScrollVertically(-1))
                        && newState == RecyclerView.SCROLL_STATE_IDLE
                        && (mList != null && !mList.isEmpty()))
                {
                    int lastNoticeSeq = mList.get(mList.size() - 1).getSeq();
                    requestBoardList(lastNoticeSeq);
                }
            }

        });
    }
    // 공시사항 글 목록 조회
    private void requestBoardList(int... lastSeq){
        int lastNoticeSeq = 0;
        if(lastSeq != null && lastSeq.length > 0) {
            LogMgr.e(TAG, "lastSeq[0] = " + lastSeq[0]);
            lastNoticeSeq = lastSeq[0];
        }
        String acaCode = "";
        String gradeCode = "";
        if(_selectedLocalACA != null) acaCode = _selectedLocalACA.acaCode;
        if(_selectedGrade != null) gradeCode = String.valueOf(_selectedGrade.gubunCode);
        if (RetrofitClient.getInstance() != null){
            mRetrofitApi = RetrofitClient.getApiInterface();
            final int finalLastNoticeSeq = lastNoticeSeq;
            mRetrofitApi.getAnnouncementList(lastNoticeSeq, acaCode, gradeCode).enqueue(new Callback<AnnouncementListResponse>() {
                @Override
                public void onResponse(Call<AnnouncementListResponse> call, Response<AnnouncementListResponse> response) {
                    try {
                        if (response.isSuccessful()){
                            List<AnnouncementData> getData = null;

                            if (response.body() != null) {

                                getData = response.body().data;
                                if (getData != null){
                                    if(finalLastNoticeSeq == 0) {
                                        if (mList.size() > 0) mList.clear();
                                    }
                                    mList.addAll(getData);

                                    DBUtils.setReadDB(mContext, mList, _memberSeq, DataManager.BOARD_NOTICE, new DBUtils.onQueryCompletedListener() {
                                        @Override
                                        public void onComplete() {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mAdapter.notifyDataSetChanged();
                                                }
                                            });

                                        }
                                    });
                                    checkEmptyRecyclerView();
                                    if(finalLastNoticeSeq == 0 && mList.size() > 0 && mRecyclerView != null) {
                                        _handler.postDelayed(() -> mRecyclerView.smoothScrollToPosition(0), scrollToTopDelay);
                                    }

                                }else LogMgr.e(TAG, "ListData is null");
                            }
                        }else{
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e){
                        LogMgr.e(TAG + "requestBoardList() Exception : ", e.getMessage());
                    }finally {
                        mSwipeRefresh.setRefreshing(false);
                    }
                }

                @Override
                public void onFailure(Call<AnnouncementListResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "requestBoardList() onFailure >> " + t.getMessage());
                    }catch (Exception e){
                    }
//                    hideProgressDialog();
                    mSwipeRefresh.setRefreshing(false);
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
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

    private void getData(){
        if(!TextUtils.isEmpty(_appAcaCode) && _userGubun >= Constants.USER_TYPE_TEACHER){
            ACAData selectedACA = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Optional option =  _ACAList.stream().filter(t -> t.acaCode.equals(_appAcaCode)).findFirst();
                if(option.isPresent()) {
                    selectedACA = (ACAData) option.get();
                }
            } else {
                for (ACAData data : _ACAList) {
                    if (data.acaCode == _appAcaCode) {
                        selectedACA = data;
                        break;
                    }
                }
            }
            try {
                if (selectedACA != null) {
                    int selectedIndex = _ACAList.indexOf(selectedACA);
                    if(selectedIndex >= 0 && selectedIndex <= _ACAList.size()) {
                        mSpinnerCampus.selectItemByIndex(selectedIndex); //전체
                    }
                } else {
                    mSpinnerCampus.selectItemByIndex(0);
                }
            }catch(Exception ex){

            }

        }else{
            mSpinnerCampus.selectItemByIndex(0);
        }
//        requestBoardList(_acaCode);
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
                navigate2EditAnnouncementActivity();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void navigate2EditAnnouncementActivity() {
        Intent editIntent = new Intent(mContext, EditAnnouncementActivity.class);
        if(_selectedLocalACA != null) {
            editIntent.putExtra(IntentParams.PARAM_STU_ACACODE, _selectedLocalACA.acaCode);
        }
        if(_selectedGrade != null) {
            editIntent.putExtra(IntentParams.PARAM_STU_GRADECODE, _selectedGrade.gubunCode);
        }
        resultLauncher.launch(editIntent);
    }
    private boolean checkEmptyRecyclerView() {
        if (mAdapter.getItemCount() > 0) {
            mSwipeRefresh.setVisibility(View.VISIBLE);
            tvEmptyList.setVisibility(View.INVISIBLE);
            return false;
        } else {
            mSwipeRefresh.setVisibility(View.INVISIBLE);
            tvEmptyList.setVisibility(View.VISIBLE);
            return true;
        }
    }
}