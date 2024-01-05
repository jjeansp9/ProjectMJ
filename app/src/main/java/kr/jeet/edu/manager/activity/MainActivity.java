package kr.jeet.edu.manager.activity;

import static kr.jeet.edu.manager.fcm.FCMManager.MSG_TYPE_ATTEND;
import static kr.jeet.edu.manager.fcm.FCMManager.MSG_TYPE_COUNSEL;
import static kr.jeet.edu.manager.fcm.FCMManager.MSG_TYPE_NOTICE;
import static kr.jeet.edu.manager.fcm.FCMManager.MSG_TYPE_LEVEL_TEST;
import static kr.jeet.edu.manager.fcm.FCMManager.MSG_TYPE_QNA;
import static kr.jeet.edu.manager.fcm.FCMManager.MSG_TYPE_QNA_COMPLETE;
import static kr.jeet.edu.manager.fcm.FCMManager.MSG_TYPE_QNA_ING;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.activity.menu.announcement.MenuAnnouncementActivity;
import kr.jeet.edu.manager.activity.menu.announcement.MenuAnnouncementDetailActivity;
import kr.jeet.edu.manager.activity.menu.briefing.MenuBriefingActivity;
import kr.jeet.edu.manager.activity.menu.bus.MenuBusActivity;
import kr.jeet.edu.manager.activity.menu.leveltest.MenuLevelTestActivity;
import kr.jeet.edu.manager.activity.menu.leveltest.MenuLevelTestDetailActivity;
import kr.jeet.edu.manager.activity.menu.notice.MenuNoticeActivity;
import kr.jeet.edu.manager.activity.menu.qna.DetailQNAActivity;
import kr.jeet.edu.manager.activity.menu.qna.MenuQNAActivity;
import kr.jeet.edu.manager.activity.menu.reportcard.MenuReportCardActivity;
import kr.jeet.edu.manager.activity.menu.schedule.MenuScheduleActivity;
import kr.jeet.edu.manager.activity.menu.student.MenuManageStudentActivity;
import kr.jeet.edu.manager.activity.menu.student.MenuStudentInfoActivity;
import kr.jeet.edu.manager.activity.setting.SettingsActivity;
import kr.jeet.edu.manager.adapter.AnnouncementListAdapter;
import kr.jeet.edu.manager.adapter.MainMenuListAdapter;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.DataManager;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.db.JeetDatabase;
import kr.jeet.edu.manager.db.NewBoardDao;
import kr.jeet.edu.manager.db.NewBoardData;
import kr.jeet.edu.manager.db.PushMessage;
import kr.jeet.edu.manager.dialog.PushPopupDialog;
import kr.jeet.edu.manager.model.data.ACAData;
import kr.jeet.edu.manager.model.data.AnnouncementData;
import kr.jeet.edu.manager.model.data.BoardAttributeData;
import kr.jeet.edu.manager.model.data.LTCData;
import kr.jeet.edu.manager.model.data.MainMenuItemData;
import kr.jeet.edu.manager.model.data.ManagerInfo;
import kr.jeet.edu.manager.model.data.ReadData;
import kr.jeet.edu.manager.model.data.SchoolData;
import kr.jeet.edu.manager.model.data.SettingItemData;
import kr.jeet.edu.manager.model.response.AnnouncementListResponse;
import kr.jeet.edu.manager.model.response.BoardAttributeResponse;
import kr.jeet.edu.manager.model.response.BoardNewResponse;
import kr.jeet.edu.manager.model.response.GetACAListResponse;
import kr.jeet.edu.manager.model.response.GetManagerInfoResponse;
import kr.jeet.edu.manager.model.response.LTCListResponse;
import kr.jeet.edu.manager.model.response.SchoolListResponse;
import kr.jeet.edu.manager.model.response.SettingItemListResponse;
import kr.jeet.edu.manager.server.RetrofitApi;
import kr.jeet.edu.manager.server.RetrofitClient;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.PreferenceUtil;
import kr.jeet.edu.manager.utils.Utils;
import kr.jeet.edu.manager.view.CustomAppbarLayout;
import kr.jeet.edu.manager.view.decoration.LastIndexDeleteDecoration;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MainActivity extends BaseActivity {

    private static final String TAG = "mainActivity";

//    TextView tvProfileDate;
    TextView tvManagerName, tvManagerDesignation, tvCampusName, tvDepartmentName;
//    ConstraintLayout layoutCounsel;
//    ImageView ivNewCounsel;
    TextView tvNew;
    RecyclerView _recyclerViewAnnouncement;
    TextView tvEmptyList;
    AnnouncementListAdapter _announcementListAdapter;
    ConstraintLayout layoutTeacherProfile, layoutRequestConsulting, layoutAnnouncement;
    int _userGubun = 1;
    int _memberSeq = 0;
    int _sfCode = 0;
    private RetrofitApi mRetrofitApi;
    boolean doubleBackToExitPressedOnce = false;

    private final int CMD_GET_ACALIST = 1;  // 일반 캠퍼스정보 (ACA정보) 가져오기
    private final int CMD_GET_LOCAL_ACALIST = 2;  // 지역별 캠퍼스정보 가져오기
    private final int CMD_GET_SCHOOL_LIST = 3;       // 학교 목록 조회하기
    private final int CMD_GET_LTC_LIST = 4;     //테스트예약용 캠퍼스정보 가져오기
    private final int CMD_GET_MANAGER_INFO = 5;       // 강사정보 가져오기
    private final int CMD_GET_BOARD_INFO = 6;       // 게시판정보 가져오기
    private final int CMD_GET_LAST_ANNOUNCEMENT_BOARD = 7;       // 공지사항게시판정보 가져오기
    private final int CMD_GET_RECIPIENT_SETTING = 8;       // 설정조회 (수신인)
    private final int CMD_GET_BOARD_NEW_CONTENT = 11;  //공지사항 최신글 조회


//    private final int CMD_PUSH_MESSAGE_RECEIVED = 5;

    private RecyclerView mRecyclerView;
    private MainMenuListAdapter mListAdapter;
//    private TextView tvLastAnnouncementTitle;
    private List<MainMenuItemData> menuList = new ArrayList<>();
    private PushMessage _pushMessage;
    List<ReadData> announcementList = new ArrayList<>();
    ManagerInfo _managerInfo = null;
    NewBoardDao _newBoardDAO = null;

    private BroadcastReceiver pushNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent != null && Constants.ACTION_JEET_PUSH_MESSAGE_RECEIVED.equals(intent.getAction())){
                LogMgr.w(TAG, "broadcast onReceived ");
                if(intent.hasExtra(IntentParams.PARAM_NOTICE_TYPE)) {
                    String type = intent.getStringExtra(IntentParams.PARAM_NOTICE_TYPE);
                    if(type.equals(MSG_TYPE_COUNSEL)) {
                        new Thread(() -> {
                            List<PushMessage> pushMessages = JeetDatabase.getInstance(getApplicationContext()).pushMessageDao().getMessageByReadFlagNType(false, MSG_TYPE_COUNSEL);
                            if(pushMessages.isEmpty()) {
                                setNewCounselContent(false);
                            }else{
                                setNewCounselContent(true);
                            }
                        }).start();
                    }
                }
                mHandler.sendEmptyMessage(CMD_GET_LAST_ANNOUNCEMENT_BOARD);
            }
        }
    };
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case CMD_GET_ACALIST :
                    requestACAList();
                    break;
                case CMD_GET_LOCAL_ACALIST :
                    requestLocalACAList();
                    break;
                case CMD_GET_SCHOOL_LIST:
                    requestSchoolList();
                    break;
                case CMD_GET_LTC_LIST:
                    requestLTCList();
                    break;
                case CMD_GET_MANAGER_INFO:
                    requestManagerInfo();
                    break;

                case CMD_GET_BOARD_INFO:
                    requestBoardInfoList();
                    break;
                case CMD_GET_LAST_ANNOUNCEMENT_BOARD:
                    if(_userGubun >= Constants.USER_TYPE_TEACHER) {
                        requestAnnouncementBoardList(PreferenceUtil.getAppAcaCode(mContext), "");
                    }else {
                        requestAnnouncementBoardList("", "");
                    }
                    break;
                case CMD_GET_RECIPIENT_SETTING:
                    requestSettingItems();
                    break;
                case CMD_GET_BOARD_NEW_CONTENT:
                    requestBoardNew(DataManager.BOARD_NOTICE);
                    requestBoardNew(DataManager.BOARD_PT);
                    break;
//                case CMD_PUSH_MESSAGE_RECEIVED:
//                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        _newBoardDAO = JeetDatabase.getInstance(mContext).newBoardDao();
        _userGubun = PreferenceUtil.getUserGubun(this);
        _memberSeq = PreferenceUtil.getUserSeq(this);
        _sfCode = PreferenceUtil.getUserSFCode(this);
        LogMgr.e(_userGubun + "/" + _memberSeq + "/" + _sfCode);
        DataManager.getInstance().sevenDaysAgo = LocalDateTime.now().minusDays(Constants.IS_READ_DELETE_DAY); // 현재 날짜에서 7일을 뺀 날짜
        initView();
        initIntentData();
        removeOldDBData();
        mHandler.sendEmptyMessage(CMD_GET_ACALIST);
    }
    private void removeOldDBData() {
        new Thread(() -> {
            List<NewBoardData> getOldList = _newBoardDAO.getOldReadInfoList(DataManager.getInstance().sevenDaysAgo);
            LogMgr.e(TAG, "getOldList size = " + getOldList.size());
            getOldList.forEach(item -> _newBoardDAO.delete(item));
        }).start();
    }
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle("");
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(false);
        setSupportActionBar(customAppbar.getToolbar());
//        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            finishAffinity();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.msg_backbutton_to_exit, Toast.LENGTH_SHORT).show();

        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    void initView() {
//        tvLastAnnouncementTitle = findViewById(R.id.tv_notify_content);
//        tvProfileDate = findViewById(R.id.tv_date);
        tvManagerName = findViewById(R.id.tv_manager_name);
        tvManagerDesignation = findViewById(R.id.tv_teacher_designation);

        tvCampusName = findViewById(R.id.tv_campus);
        tvDepartmentName = findViewById(R.id.tv_department);

        if(_userGubun == Constants.USER_TYPE_ADMIN) {
            tvManagerDesignation.setText(R.string.main_admin_sub);
            tvCampusName.setVisibility(View.VISIBLE);
            tvDepartmentName.setVisibility(View.VISIBLE);
        }else if(_userGubun == Constants.USER_TYPE_TEACHER) {
            tvManagerDesignation.setText(R.string.main_teacher_sub);
            tvCampusName.setVisibility(View.VISIBLE);
            tvDepartmentName.setVisibility(View.VISIBLE);
        }else if(_userGubun == Constants.USER_TYPE_SUPER_ADMIN) {
            tvManagerDesignation.setText(R.string.main_superadmin_sub);
            tvCampusName.setVisibility(View.GONE);
            tvDepartmentName.setVisibility(View.GONE);
        }


        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_menu);

        FlexboxLayoutManager fblManager = new FlexboxLayoutManager(this);
        fblManager.setFlexWrap(FlexWrap.WRAP);
        fblManager.setFlexDirection(FlexDirection.ROW);
        fblManager.setJustifyContent(JustifyContent.FLEX_START);
        mRecyclerView.setLayoutManager(fblManager);
        mListAdapter = new MainMenuListAdapter(mContext, menuList, new MainMenuListAdapter.onItemClickListener() {
            @Override
            public void onItemClick(MainMenuItemData item) {
                if(item.getTargetClass() != null) {
                    Intent targetIntent = new Intent(mContext, item.getTargetClass());
                    startActivity(targetIntent);
                    overridePendingTransition(R.anim.vertical_enter, R.anim.none);
                }else{
                    Toast.makeText(mContext, "개발 중인 기능입니다.", Toast.LENGTH_SHORT).show();
                    LogMgr.d("targetIntent is null at " + getString(item.getTitleRes()));
                }
            }
        });
        mRecyclerView.setAdapter(mListAdapter);
//        layoutCounsel = findViewById(R.id.btn_request_consulting);
        layoutTeacherProfile = findViewById(R.id.btn_teacher_profile);
        layoutRequestConsulting = findViewById(R.id.btn_request_consulting);
        layoutAnnouncement = findViewById(R.id.btn_announcement_state);
        layoutAnnouncement.setVisibility(View.VISIBLE);
        if(_userGubun == Constants.USER_TYPE_SUPER_ADMIN) {
            layoutTeacherProfile.setVisibility(View.GONE);
            layoutRequestConsulting.setVisibility(View.GONE);

        }else{
            layoutTeacherProfile.setVisibility(View.VISIBLE);
            layoutRequestConsulting.setVisibility(View.VISIBLE);
            tvNew = findViewById(R.id.tv_new);
        }
        tvEmptyList = findViewById(R.id.tv_main_empty_list);
        _recyclerViewAnnouncement = findViewById(R.id.recycler_announcement);
        _announcementListAdapter = new AnnouncementListAdapter(mContext, announcementList, new AnnouncementListAdapter.onItemClickListener() {
            @Override
            public void onItemClick(AnnouncementData item, int position) {
                navigate2DetailAnnouncementActivity(item);
            }
        });
        _announcementListAdapter.setViewMode(AnnouncementListAdapter.ViewMode.SUMMARY);
        _recyclerViewAnnouncement.setAdapter(_announcementListAdapter);
        Drawable dividerDrawable = ContextCompat.getDrawable(mContext, R.drawable.bg_line);
        LastIndexDeleteDecoration dividerItemDecoration = new LastIndexDeleteDecoration(dividerDrawable);
        _recyclerViewAnnouncement.addItemDecoration(dividerItemDecoration);

//        _recyclerViewAnnouncement.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        initMenus();
        initAppbar();
    }
    private void initIntentData() {
        Intent intent = getIntent();
        if(intent != null) {
            Bundle bundle = intent.getExtras();
            if(bundle != null)
                _pushMessage = Utils.getSerializableExtra(bundle, IntentParams.PARAM_PUSH_MESSAGE, PushMessage.class);
        }

        if(_pushMessage != null) {
            switch(_pushMessage.pushType) {
                case MSG_TYPE_NOTICE:   //공지사항의 경우 공지사항 상세페이지로 이동
                {
                    Intent noticeIntent = new Intent(this, MenuAnnouncementDetailActivity.class);
                    noticeIntent.putExtras(intent);
                    startActivity(noticeIntent);
                }
                    break;
                case MSG_TYPE_ATTEND:
                {
                    PushPopupDialog pushPopupDialog = new PushPopupDialog(this, _pushMessage);
                    pushPopupDialog.setOnOkButtonClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(!TextUtils.isEmpty(_pushMessage.pushId)) {
                                List<String> list = new ArrayList<>();
                                list.add(_pushMessage.pushId);
                                pushPopupDialog.getFCMManager().requestPushConfirmToServer(list);
                                _pushMessage.isRead = true;
                                JeetDatabase.getInstance(getApplicationContext()).pushMessageDao().update(_pushMessage);
                            }
                            pushPopupDialog.dismiss();
                        }
                    });
                    pushPopupDialog.show();
                }
                    break;
                case MSG_TYPE_LEVEL_TEST:
                {
                    int seq = _pushMessage.connSeq;
                    navigate2LevelTestDetailActivity(seq);
                }
                break;
                case MSG_TYPE_COUNSEL:
                {
                    int seq = _pushMessage.connSeq;
                    navigate2CounselDetailActivity(seq);
                }
                    break;
                case MSG_TYPE_QNA:
                case MSG_TYPE_QNA_ING:
                case MSG_TYPE_QNA_COMPLETE:
                {
                    int seq = _pushMessage.connSeq;
                    navigate2QnaDetailActivity(seq);
                }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(Constants.ACTION_JEET_PUSH_MESSAGE_RECEIVED);
        registerReceiver(pushNotificationReceiver, intentFilter);

        new Thread(() -> {
//            List<PushMessage> pushMessages = JeetDatabase.getInstance(getApplicationContext()).pushMessageDao().getMessageByReadFlag(false);
//            pushMessages.forEach(t-> LogMgr.e(TAG, "unread = " + t.title + "/"+ t.pushType + "/" + t.connSeq));
//            if(pushMessages.stream().anyMatch(t->t.pushType.equals(MSG_TYPE_COUNSEL))) {
//                setNewCounselContent(true);
//            }else{
//                setNewCounselContent(false);
//            }
            List<PushMessage> pushMessages = JeetDatabase.getInstance(getApplicationContext()).pushMessageDao().getMessageByReadFlagNType(false, MSG_TYPE_COUNSEL);
            if(pushMessages.isEmpty()) {
                setNewCounselContent(false);
            }else{
                setNewCounselContent(true);
            }
        }).start();

        mHandler.sendEmptyMessage(CMD_GET_LAST_ANNOUNCEMENT_BOARD);

    }

    @Override
    protected void onPause() {
        unregisterReceiver(pushNotificationReceiver);
        super.onPause();
    }
    void setNewCounselContent(boolean isNew) {
        runOnUiThread(()->{
            if(tvNew != null) {
                if (isNew) {
//                    layoutCounsel.setBackground(getDrawable(R.drawable.selector_main_box_new));
                    tvNew.setVisibility(View.VISIBLE);
                } else {
//                    layoutCounsel.setBackground(getDrawable(R.drawable.selector_main_box));
                    tvNew.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_teacher_profile:
                navigate2ProfileActivity();
                break;
            case R.id.btn_request_consulting:
                navigate2MenuNoticeActivity();
                break;
        }
    }
//    private void showProfileDate() {
//        SimpleDateFormat sdf = new SimpleDateFormat("M월 d일\nEEEE", Locale.KOREAN);
//        Calendar cal = Calendar.getInstance();
//        Date date = cal.getTime();
//        String formatTime = sdf.format(date);
//        tvProfileDate.setText(formatTime);
//    }
    // 공지사항 or 설명회 메뉴아이콘에 new 표시
    private void updateMenusNew(Context context, int memberSeq, String type, List<Integer> newContentList) {
        if(menuList == null) return;
        new Thread(() -> {

            List<NewBoardData> getReadList = JeetDatabase.getInstance(context).newBoardDao().getReadInfoList(memberSeq, type, DataManager.getInstance().sevenDaysAgo);
            boolean hasAttention = false;
            if(getReadList.isEmpty()) {
                if(!newContentList.isEmpty()){
                    hasAttention = true;
                }
            }else {

                hasAttention = !(newContentList.stream().allMatch(seq -> getReadList.stream().anyMatch(data -> data.connSeq == seq)));
            }
            LogMgr.e(TAG, "hasAttention = " + hasAttention);
            OptionalInt optionalIndex = IntStream.range(0, menuList.size()).filter(i -> type.equals(menuList.get(i).getType())).findFirst();
            if(optionalIndex.isPresent()) {
                updateViewNew(optionalIndex.getAsInt(), hasAttention);
            }


        }).start();
    }

    private void updateViewNew(
            int position,
            boolean hasAttention
    ) {
        if (position >= 0 && menuList.size() > position && menuList.get(position) != null && menuList.get(position).getIsNew() != hasAttention) {
            menuList.get(position).setIsNew(hasAttention);
            runOnUiThread(() -> mListAdapter.notifyItemChanged(position));
        }
    }
    private void requestACAList(){
        if(RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.getACAList().enqueue(new Callback<GetACAListResponse>() {
                @Override
                public void onResponse(Call<GetACAListResponse> call, Response<GetACAListResponse> response) {
                    if(response.isSuccessful()) {
                        if(response.body() != null) {
                            List<ACAData> list = response.body().data;
                            DataManager.getInstance().initACAListMap(list);
                        }
                    } else {

                        try {
                            LogMgr.e(TAG, "requestACAList() errBody : " + response.errorBody().string());
                        } catch (IOException e) {
                        }

                    }
                    mHandler.sendEmptyMessage(CMD_GET_LOCAL_ACALIST);
                }

                @Override
                public void onFailure(Call<GetACAListResponse> call, Throwable t) {
                    LogMgr.e(TAG, "requestACAList() onFailure >> " + t.getMessage());
                    mHandler.sendEmptyMessage(CMD_GET_LOCAL_ACALIST);
                }
            });
        }
    }
    private void requestLocalACAList(){
        if(RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.getLocalACAList().enqueue(new Callback<GetACAListResponse>() {
                @Override
                public void onResponse(Call<GetACAListResponse> call, Response<GetACAListResponse> response) {
                    if(response.isSuccessful()) {
                        if(response.body() != null) {
                            List<ACAData> list = response.body().data;
                            DataManager.getInstance().initLocalACAListMap(list);
                        }
                    } else {

                        try {
                            LogMgr.e(TAG, "requestLocalACAList() errBody : " + response.errorBody().string());
                        } catch (IOException e) {
                        }

                    }
                    mHandler.sendEmptyMessage(CMD_GET_SCHOOL_LIST);
                }

                @Override
                public void onFailure(Call<GetACAListResponse> call, Throwable t) {
                    LogMgr.e(TAG, "requestLocalACAList() onFailure >> " + t.getMessage());
                    mHandler.sendEmptyMessage(CMD_GET_SCHOOL_LIST);
                }
            });
        }
    }
    // 학교 목록 조회
    private void requestSchoolList(){
        if(RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.getSchoolList().enqueue(new Callback<SchoolListResponse>() {
                @Override
                public void onResponse(Call<SchoolListResponse> call, Response<SchoolListResponse> response) {
                    try {
                        if(response.isSuccessful()) {
                            if(response.body() != null) {
                                List<SchoolData> list = response.body().data;
                                DataManager.getInstance().initSchoolListMap(list);
                            }
                        } else {
                            LogMgr.e(TAG, "requestSchoolList() errBody : " + response.errorBody().string());
                        }

                    }catch (Exception e) { LogMgr.e(TAG + "requestSchoolList() Exception : ", e.getMessage()); }

                    mHandler.sendEmptyMessage(CMD_GET_LTC_LIST);
                }

                @Override
                public void onFailure(Call<SchoolListResponse> call, Throwable t) {
                    try { LogMgr.e(TAG, "requestSchoolList() onFailure >> " + t.getMessage()); }
                    catch (Exception e) { LogMgr.e(TAG + "requestSchoolList() Exception : ", e.getMessage()); }

                    mHandler.sendEmptyMessage(CMD_GET_LTC_LIST);
                }
            });
        }
    }
    // 테스트예약 캠퍼스 목록 조회
    private void requestLTCList(){
        if(RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.getLTCList().enqueue(new Callback<LTCListResponse>() {
                @Override
                public void onResponse(Call<LTCListResponse> call, Response<LTCListResponse> response) {
                    try {
                        if(response.isSuccessful()) {
                            if(response.body() != null) {
                                List<LTCData> list = response.body().data;
                                DataManager.getInstance().initLTCListMap(list);
                            }
                        } else {
                            LogMgr.e(TAG, "requestLTCList() errBody : " + response.errorBody().string());
                        }

                    }catch (Exception e) { LogMgr.e(TAG + "requestLTCList() Exception : ", e.getMessage()); }
                    mHandler.sendEmptyMessage(CMD_GET_MANAGER_INFO);
                }

                @Override
                public void onFailure(Call<LTCListResponse> call, Throwable t) {
                    try { LogMgr.e(TAG, "requestLTCList() onFailure >> " + t.getMessage()); }
                    catch (Exception e) { LogMgr.e(TAG + "requestLTCList() Exception : ", e.getMessage()); }
                    mHandler.sendEmptyMessage(CMD_GET_MANAGER_INFO);

                }
            });
        }
    }
    private void requestManagerInfo() {

        if(RetrofitClient.getInstance() != null) {
//            showProgressDialog();
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.getManagerInfo(_memberSeq, _sfCode).enqueue(new Callback<GetManagerInfoResponse>() {
                @Override
                public void onResponse(Call<GetManagerInfoResponse> call, Response<GetManagerInfoResponse> response) {
//                    hideProgressDialog();
                    if(response.isSuccessful()) {
                        if(response.body() != null) {
                            ManagerInfo res = response.body().data;
                            _managerInfo = res;
                            PreferenceUtil.setUserName(mContext, res.name);
                            tvManagerName.setText(res.name);
                            //API 변경으로 찾을 필요 없어짐
//                            if(DataManager.getInstance().getACAList() != null) {
//                                if(Build.VERSION.SDK_INT >=24) {
//                                    ACAData selectedACAData = DataManager.getInstance().getACAList().stream().filter(t->t.acaCode.equalsIgnoreCase(res.avaCode)).findFirst().get();
//                                }
//                            }
//                            if(!TextUtils.isEmpty(res.acaCode)) {
                                PreferenceUtil.setAcaCode(mContext, res.acaCode);
//                            }
//                            if(!TextUtils.isEmpty(res.acaName)) {
                                PreferenceUtil.setAcaName(mContext, res.acaName);
                                tvCampusName.setText(res.acaName);
//                            }
//                            if(!TextUtils.isEmpty(res.appAcaCode)) {
                                PreferenceUtil.setAppAcaCode(mContext, res.appAcaCode);
//                            }
//                            if(!TextUtils.isEmpty(res.appAcaName)) {
                                PreferenceUtil.setAppAcaName(mContext, res.appAcaName);
//                            }
//                            if(!TextUtils.isEmpty(res.smsSender)) {
                                PreferenceUtil.setSMSSender(mContext, res.smsSender);
//                            }
//                            if(!TextUtils.isEmpty(res.sfGrade)) {
                                PreferenceUtil.setSFGrade(mContext, res.sfGrade);
//                            }
//                            if(!TextUtils.isEmpty(res.deptName)) {
                                PreferenceUtil.setDepartmentName(mContext, res.deptName);
//                            }
                            StringBuilder sb = new StringBuilder();
                            if(TextUtils.isEmpty(res.deptName)) {
                                if(TextUtils.isEmpty(res.sfGrade)){

                                }else{
                                    sb.append(res.sfGrade);
                                }
                            }else {
                                if(TextUtils.isEmpty(res.sfGrade)){
                                    sb.append(res.deptName);
                                }else{
                                    sb.append(res.deptName + " / " + res.sfGrade);
                                }
                            }
                            tvDepartmentName.setText(sb.toString());
//                            if(res.pushStatus != null){
//                                //공지사항
//                                PreferenceUtil.setNotificationAnnouncement(MainActivity.this, res.pushStatus.pushNotice.equals("Y"));
//                                //설명회
//                                PreferenceUtil.setNotificationSeminar(MainActivity.this, res.pushStatus.pushInformationSession.equals("Y"));
//                                //출석
//                                PreferenceUtil.setNotificationAttendance(MainActivity.this, res.pushStatus.pushAttendance.equals("Y"));
//                                //시스템알림
//                                PreferenceUtil.setNotificationSystem(MainActivity.this, res.pushStatus.pushSystem.equals("Y"));
//                            }else{
//                                //공지사항
//                                PreferenceUtil.setNotificationAnnouncement(MainActivity.this, true);
//                                //설명회
//                                PreferenceUtil.setNotificationSeminar(MainActivity.this, true);
//                                //출석
//                                PreferenceUtil.setNotificationAttendance(MainActivity.this, true);
//                                //시스템알림
//                                PreferenceUtil.setNotificationSystem(MainActivity.this, true);
//                            }
                        }
                        mHandler.sendEmptyMessage(CMD_GET_BOARD_INFO);
                    } else {

                        try {
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                            LogMgr.e(TAG, "requestManagerInfo() errBody : " + response.errorBody().string());
                        } catch (IOException e) {
                        }
                        mHandler.sendEmptyMessage(CMD_GET_BOARD_INFO);
                    }
                }

                @Override
                public void onFailure(Call<GetManagerInfoResponse> call, Throwable t) {
//                    hideProgressDialog();
                    LogMgr.e(TAG, "requestManagerInfo() onFailure >> " + t.getMessage());
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                    mHandler.sendEmptyMessage(CMD_GET_BOARD_INFO);
                }
            });
        }
    }
    private void requestBoardInfoList(){
        if(RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.getBoardAttribute().enqueue(new Callback<BoardAttributeResponse>() {
                @Override
                public void onResponse(Call<BoardAttributeResponse> call, Response<BoardAttributeResponse> response) {
                    if(response != null && response.isSuccessful()) {
                        if(response.body() != null) {
                            List<BoardAttributeData> list = response.body().data;
                            for(BoardAttributeData data : list){
                                DataManager.getInstance().setBoardInfo(data);
                            }
                        }
                        updateMenus();
                    } else {

                        try {
                            LogMgr.e(TAG, "requestBoardInfoList() errBody : " + response.errorBody().string());
                        } catch (IOException e) {
                        }

                    }

                }

                @Override
                public void onFailure(Call<BoardAttributeResponse> call, Throwable t) {
                    LogMgr.e(TAG, "requestBoardInfoList() onFailure >> " + t.getMessage());
                }
            });
        }
    }
    // 공지사항 일주일 이내 글 seq - 메뉴아이콘 new 표시 관련
    private void requestBoardNew(String type){
        if(RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            Call<BoardNewResponse> call = null;
            switch(type) {
                case DataManager.BOARD_NOTICE:
                    call = mRetrofitApi.getNoticeNewList();
                    break;
                case DataManager.BOARD_PT:
                    call = mRetrofitApi.getPtNewList();
                    break;
                default:
                    break;
            }
            if(call == null) return;
            final String finalType = type;
            call.enqueue(new Callback<BoardNewResponse>() {
                @Override
                public void onResponse(Call<BoardNewResponse> call, Response<BoardNewResponse> response) {
                    try {
                        if (response.isSuccessful()){
                            if (response.body() != null)  {
                                List<Integer> getData = response.body().data;
                                if (getData != null) {
                                    updateMenusNew(mContext, _memberSeq, finalType, getData);
                                }
                            }
                        }else{
                            LogMgr.e(TAG, "requestBoardAttribute() errBody : " + response.errorBody().string());

                        }

                    }catch (Exception e) { LogMgr.e(TAG + "requestBoardAttribute() Exception : ", e.getMessage()); }
                }
                @Override
                public void onFailure(Call<BoardNewResponse> call, Throwable t) {
                    try{
                        LogMgr.e(TAG, "requestBoardAttribute() onFailure >> " + t.getMessage());

                    }catch (Exception e){ LogMgr.e(TAG + "requestBoardAttribute() Exception : ", e.getMessage()); }
                    mListAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    //최근 공지사항 가져오기
    private void requestAnnouncementBoardList(String acaCode, String appgubunCode, int... lastSeq){
        int lastNoticeSeq = 0;
        if(lastSeq != null && lastSeq.length > 0) {
            LogMgr.e(TAG, "lastSeq[0] = " + lastSeq[0]);
            lastNoticeSeq = lastSeq[0];
        }

        if (RetrofitClient.getInstance() != null){
            mRetrofitApi = RetrofitClient.getApiInterface();
            final int finalLastNoticeSeq = lastNoticeSeq;
            mRetrofitApi.getAnnouncementList(lastNoticeSeq, acaCode, appgubunCode).enqueue(new Callback<AnnouncementListResponse>() {
                @Override
                public void onResponse(Call<AnnouncementListResponse> call, Response<AnnouncementListResponse> response) {
                    try {
                        if (response.isSuccessful()){
                            List<AnnouncementData> getData = null;

                            if (response.body() != null) {
                                if(announcementList != null) announcementList.clear();
                                getData = response.body().data;
                                if (getData != null && !getData.isEmpty()){
//                                    for(int i = 0 ; i < 3; i++){
//                                        AnnouncementData item = getData.get(i);
//                                        announcementList.add(item);
//                                    }
                                    announcementList.addAll(getData.stream().limit(3).collect(Collectors.toList()));

                                }else LogMgr.e(TAG, "ListData is null");
                            }
                        }else{
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e){
                        LogMgr.e(TAG + "requestBoardList() Exception : ", e.getMessage());
                    }finally {
                        setAnnouncementView();
                        mHandler.sendEmptyMessage(CMD_GET_RECIPIENT_SETTING);
                    }
                }

                @Override
                public void onFailure(Call<AnnouncementListResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "requestBoardList() onFailure >> " + t.getMessage());
                    }catch (Exception e){
                    }
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                    setAnnouncementView();
                    mHandler.sendEmptyMessage(CMD_GET_RECIPIENT_SETTING);
                }
            });
        }
    }
    //설정사항 가져오기
    private void requestSettingItems(){
        if (RetrofitClient.getInstance() != null){
            RetrofitClient.getApiInterface().getSettingItems().enqueue(new Callback<SettingItemListResponse>() {
                @Override
                public void onResponse(Call<SettingItemListResponse> call, Response<SettingItemListResponse> response) {
                    try {
                        if (response.isSuccessful()){
                            List<SettingItemData> getData = null;

                            if (response.body() != null) {
                                getData = response.body().data;
                                DataManager.getInstance().initSettingListMap(getData);
                            }
                        }else{
//                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e){
                        LogMgr.e(TAG + "requestBoardList() Exception : ", e.getMessage());
                    }finally {
                        mHandler.sendEmptyMessage(CMD_GET_BOARD_NEW_CONTENT);
                    }
                }

                @Override
                public void onFailure(Call<SettingItemListResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "requestBoardList() onFailure >> " + t.getMessage());
                    }catch (Exception e){
                    }
                    mHandler.sendEmptyMessage(CMD_GET_BOARD_NEW_CONTENT);
                }
            });
        }
    }
    private void initMenus() {
        if(menuList != null) {
            menuList.clear();
        }
        //원생정보
        //매니저는 원생관리
        if(_userGubun == Constants.USER_TYPE_ADMIN || _userGubun == Constants.USER_TYPE_SUPER_ADMIN) {
            menuList.add(new MainMenuItemData(DataManager.BOARD_STUDENT_INFO, R.drawable.icon_menu_student, R.string.main_menu_student_manage, false, MenuManageStudentActivity.class));
        }else {
            menuList.add(new MainMenuItemData(DataManager.BOARD_STUDENT_INFO, R.drawable.icon_menu_student, R.string.main_menu_student_info, false, MenuStudentInfoActivity.class));
        }

        //공지사항
        menuList.add(new MainMenuItemData(DataManager.BOARD_NOTICE, R.drawable.icon_menu_attention, R.string.main_menu_announcement, false, MenuAnnouncementActivity.class));
        //캠퍼스일정
        menuList.add(new MainMenuItemData(DataManager.BOARD_SCHEDULE, R.drawable.icon_menu_schedule, R.string.main_menu_campus_schedule, false,MenuScheduleActivity.class));
        //알림장
        menuList.add(new MainMenuItemData(DataManager.BOARD_SYSTEM_NOTICE, R.drawable.icon_menu_notice, R.string.main_menu_notice, false, MenuNoticeActivity.class));
        //테스트예약 -> 컨셉변경으로 제거 -> 테스트시간 설정이 필요하여 생성
        menuList.add(new MainMenuItemData(DataManager.BOARD_LEVELTEST, R.drawable.icon_menu_test_reserve, R.string.main_menu_test_reserve, false, MenuLevelTestActivity.class));
        //차량정보
        menuList.add(new MainMenuItemData(DataManager.BOARD_BUS, R.drawable.icon_menu_bus, R.string.main_menu_bus_info, false, MenuBusActivity.class));
        //설명회예약
        menuList.add(new MainMenuItemData(DataManager.BOARD_PT, R.drawable.icon_menu_briefing, R.string.main_menu_briefing_reserve, false, MenuBriefingActivity.class));
        //성적표
        menuList.add(new MainMenuItemData(DataManager.BOARD_REPORT, R.drawable.icon_menu_report, R.string.title_report_card, false, MenuReportCardActivity.class));
        //QnA
        menuList.add(new MainMenuItemData(DataManager.BOARD_QNA, R.drawable.icon_menu_question, R.string.main_menu_qna, false, MenuQNAActivity.class));
//        mListAdapter.notifyDataSetChanged();
    }
    private void updateMenus() {
        if(menuList == null) return;
        menuList.stream().forEach(menu -> {
            BoardAttributeData boardData = DataManager.getInstance().getBoardInfo(menu.getType());
           if(boardData != null) {
               if(!boardData.boardNm.equals(mContext.getString(menu.getTitleRes()))) {
                   menu.setTitle(boardData.boardNm);
               }
           }
        });
        mListAdapter.notifyDataSetChanged();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_setting:
                startActivity(new Intent(mContext, SettingsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void setAnnouncementView() {
        _announcementListAdapter.notifyDataSetChanged();
        if(announcementList == null || announcementList.isEmpty()) {
            tvEmptyList.setVisibility(View.VISIBLE);
        }else{
            tvEmptyList.setVisibility(View.GONE);
        }
    }
    //
    private void navigate2MenuNoticeActivity() {
        Intent targetIntent = new Intent(mContext, MenuNoticeActivity.class);
        targetIntent.putExtra(IntentParams.PARAM_NOTICE_TYPE, Constants.NoticeType.COUNSEL);
        startActivity(targetIntent);
    }
    private void navigate2DetailAnnouncementActivity(AnnouncementData data) {
        Intent targetIntent = new Intent(mContext, MenuAnnouncementDetailActivity.class);
        targetIntent.putExtra(IntentParams.PARAM_ANNOUNCEMENT_INFO, data);
        startActivity(targetIntent);
        overridePendingTransition(R.anim.horizontal_enter, R.anim.horizontal_out);
    }
    private void navigate2ProfileActivity() {
        Intent targetIntent = new Intent(mContext, ProfileActivity.class);
        if(_managerInfo != null) {
            targetIntent.putExtra(IntentParams.PARAM_MANAGER_INFO, _managerInfo);
        }
        startActivity(targetIntent);
    }
    private void navigate2CounselDetailActivity(int seq) {
        Intent targetIntent = new Intent(mContext, CounselDetailActivity.class);
        targetIntent.putExtra(IntentParams.PARAM_BOARD_SEQ, seq);
        startActivity(targetIntent);
    }
    private void navigate2QnaDetailActivity(int seq) {
        Intent targetIntent = new Intent(mContext, DetailQNAActivity.class);
        targetIntent.putExtra(IntentParams.PARAM_BOARD_SEQ, seq);
        startActivity(targetIntent);
    }
    private void navigate2LevelTestDetailActivity(int seq) {
        Intent targetIntent = new Intent(mContext, MenuLevelTestDetailActivity.class);
        targetIntent.putExtra(IntentParams.PARAM_BOARD_SEQ, seq);
        startActivity(targetIntent);
    }

}