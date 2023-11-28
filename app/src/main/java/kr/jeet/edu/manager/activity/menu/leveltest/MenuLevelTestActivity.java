package kr.jeet.edu.manager.activity.menu.leveltest;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;
import com.skydoves.powerspinner.OnSpinnerOutsideTouchListener;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.util.ArrayList;
import java.util.List;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.activity.BaseActivity;
import kr.jeet.edu.manager.adapter.LevelTestListAdapter;
import kr.jeet.edu.manager.adapter.WrapContentSpinnerAdapter;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.DataManager;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.db.JeetDatabase;
import kr.jeet.edu.manager.db.PushMessage;
import kr.jeet.edu.manager.fcm.FCMManager;
import kr.jeet.edu.manager.model.data.LTCData;
import kr.jeet.edu.manager.model.data.LevelTestData;
import kr.jeet.edu.manager.model.response.LevelTestListResponse;
import kr.jeet.edu.manager.server.RetrofitClient;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.PreferenceUtil;
import kr.jeet.edu.manager.utils.Utils;
import kr.jeet.edu.manager.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuLevelTestActivity extends BaseActivity {
    private static final String TAG = "leveltest";
    private RecyclerView _recyclerView;
    private LevelTestListAdapter _listAdapter;

    private PowerSpinnerView _spinnerLTC, _spinnerSchoolLevel, _spinnerGrade;
    private SwipeRefreshLayout _swipeRefreshLayout;
    private TextView tvEmpty;
    private ArrayList<LevelTestData> _list = new ArrayList<>();
    private ArrayList<String> _schoolLevelList = new ArrayList<>();
    private ArrayList<String> _gradeList = new ArrayList<>();
    int _userGubun = 1;
    int _seq = 0;
    int _sfCode = 0;

    private String _ltcCode = "";
    List<LTCData> _LTCList = new ArrayList<>();
    LTCData _selectedLTC = null;
    String _selectedSchoolLevel = "";
    Constants.StudentGrade _selectedGrade = null;
    String _userType = "";
    Menu _menu;
    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        LogMgr.w("result =" + result);

        if(result.getResultCode() != RESULT_CANCELED) {
            Intent intent = result.getData();
            if(intent == null) return;
            if(intent.hasExtra(IntentParams.PARAM_BOARD_DELETED)) {
                boolean deleted = intent.getBooleanExtra(IntentParams.PARAM_BOARD_DELETED, false);
                int position = intent.getIntExtra(IntentParams.PARAM_BOARD_POSITION, -1);
                if(deleted && position >= 0) {
                    _list.remove(position);
                    _listAdapter.notifyItemRemoved(position);
                }
            }
        }

    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_level_test);
        mContext = this;
        _userGubun = PreferenceUtil.getUserGubun(this);
        _seq = PreferenceUtil.getUserSeq(this);
        _sfCode = PreferenceUtil.getUserSFCode(this);
        initAppbar();
        initView();
        changeMessageState2Read();
        setAnimMove(Constants.MOVE_DOWN);
    }

    void initView() {
        tvEmpty = findViewById(R.id.tv_empty_list);
        _swipeRefreshLayout = findViewById(R.id.refresh_layout);
        _recyclerView = findViewById(R.id.recyclerview_leveltest);
        _listAdapter = new LevelTestListAdapter(mContext, _list, new LevelTestListAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int position, LevelTestData item) {
                navigate2LevelTestDetailActivity(item, position);
            }
        });
        _recyclerView.setAdapter(_listAdapter);
        _recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        //region spinners
        _spinnerLTC = findViewById(R.id.spinner_campus);
        _spinnerSchoolLevel = findViewById(R.id.spinner_school_level);
        _spinnerGrade = findViewById(R.id.spinner_grade);

        _spinnerLTC.setIsFocusable(true);
        _LTCList.clear();
        _LTCList.add(new LTCData("0", getString(R.string.item_total)));
        _LTCList.addAll(DataManager.getInstance().getLTCListMap().values());
        List<String> ltcNames = new ArrayList<>();
        for(LTCData data : _LTCList) { ltcNames.add(data.ltcName);}
        _spinnerLTC.setItems(ltcNames);
        {
            WrapContentSpinnerAdapter adapter = new WrapContentSpinnerAdapter(mContext, ltcNames, _spinnerLTC);
            _spinnerLTC.setSpinnerAdapter(adapter);
        }

        _spinnerLTC.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener<String>() {
            @Override
            public void onItemSelected(int oldIndex, @Nullable String oldItem, int newIndex, String newItem) {
                _selectedLTC = _LTCList.get(newIndex);
                _spinnerSchoolLevel.clearSelectedItem();
                _selectedSchoolLevel = "";
                _spinnerGrade.clearSelectedItem();
                _selectedGrade = null;
                _spinnerGrade.setEnabled(false);

                if(_selectedLTC.ltcCode.equals("0")) {  //캠퍼스 전체
                    _spinnerSchoolLevel.setEnabled(false);
                }else{
                    _spinnerSchoolLevel.setEnabled(true);
                    _spinnerSchoolLevel.selectItemByIndex(0);
                }
                requestBoardList(_selectedLTC.ltcCode);
            }
        });
        _spinnerLTC.setSpinnerOutsideTouchListener(new OnSpinnerOutsideTouchListener() {
            @Override
            public void onSpinnerOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                _spinnerLTC.dismiss();
            }
        });
        _spinnerLTC.setLifecycleOwner(this);
        if(!ltcNames.isEmpty()) _spinnerLTC.selectItemByIndex(0);

        //region spinner dept

        _spinnerSchoolLevel.setEnabled(false);
        _schoolLevelList.clear();
        _schoolLevelList.add(getString(R.string.item_total));
        _schoolLevelList.addAll(Constants.SchoolGradeType.getNameList());
//        {
//            WrapContentSpinnerAdapter adapter = new WrapContentSpinnerAdapter(mContext, _schoolLevelList, _spinnerSchoolLevel);
//            _spinnerSchoolLevel.setSpinnerAdapter(adapter);
//        }
        Utils.updateSpinnerList(_spinnerSchoolLevel, _schoolLevelList);
        _spinnerSchoolLevel.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener<String>() {
            @Override
            public void onItemSelected(int oldIndex, @Nullable String oldItem, int newIndex, String newItem) {
                LogMgr.e(newItem + " selected");
                if(oldItem != null && oldItem.equals(newItem)) return;

                _selectedSchoolLevel = newItem;
                LogMgr.w("selectedSchoolLevel = " + newItem);

                if(_spinnerGrade != null) {
                    _spinnerGrade.clearSelectedItem();
                }
                if(newItem.equals(getString(R.string.item_total))) { //전체 선택
                    if(_spinnerGrade != null) _spinnerGrade.setEnabled(false);
                    _selectedGrade = null;
                    requestBoardList(_selectedLTC.ltcCode);
                }else {
                    if(_spinnerGrade != null) _spinnerGrade.setEnabled(true);
                    initGradeList();
                }
            }
        });
        _spinnerSchoolLevel.setSpinnerOutsideTouchListener(new OnSpinnerOutsideTouchListener() {
            @Override
            public void onSpinnerOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                _spinnerSchoolLevel.dismiss();
            }
        });
        _spinnerSchoolLevel.setLifecycleOwner(this);
        _spinnerSchoolLevel.setSpinnerPopupHeight(ConstraintLayout.LayoutParams.WRAP_CONTENT);
        //endregion
        //region spinner clst

        _spinnerGrade.setEnabled(false);
//        {
//            WrapContentSpinnerAdapter adapter = new WrapContentSpinnerAdapter(mContext, _gradeList, _spinnerGrade);
//            _spinnerGrade.setSpinnerAdapter(adapter);
//        }
        Utils.updateSpinnerList(_spinnerGrade, _gradeList);
        _spinnerGrade.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener<String>() {
            @Override
            public void onItemSelected(int oldIndex, @Nullable String oldItem, int newIndex, String newItem) {
                LogMgr.e("oldItem = " + oldItem + "/ newItem + " + newItem + " selected");
                if(oldItem != null && oldItem.equals(newItem)) return;
                _selectedGrade = Constants.StudentGrade.getByName(newItem);
                requestBoardList(_selectedLTC.ltcCode);

            }
        });

        _spinnerGrade.setSpinnerOutsideTouchListener(new OnSpinnerOutsideTouchListener() {
            @Override
            public void onSpinnerOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                _spinnerGrade.dismiss();
            }
        });
        _spinnerGrade.setLifecycleOwner(this);
        _spinnerGrade.setSpinnerPopupHeight(ConstraintLayout.LayoutParams.WRAP_CONTENT);
        //endregion
        _swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                LogMgr.w(TAG, "onRefresh");
                requestBoardList(_selectedLTC.ltcCode); // spinner에서 선택한 캠퍼스 코드로 갱신
            }
        });
        _recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(((!_recyclerView.canScrollVertically(1)) && _recyclerView.canScrollVertically(-1))
                        && newState == RecyclerView.SCROLL_STATE_IDLE
                        && (_list != null && !_list.isEmpty()))
                {
                    // 희망일 순 정렬된 리스트 데이터의 마지막 seq로 페이징 요청
                    // 컨셉 변경 -> 리스트 내에서 최소 seq 값을 lastNoticeSeq 로 변경
                    // 컨셉 변경 -> 서버에서 등록일 순으로 데이터를 정렬해서 주는 것으로 변경-> 앱에서는 rollback
                    int lastNoticeSeq = _list.get(_list.size() - 1).seq;
                    LogMgr.w(TAG, "scroll listener lastNoticeSeq= " + lastNoticeSeq);
                    requestBoardList(_selectedLTC.ltcCode, lastNoticeSeq);
//                    Optional optional = _list.stream().min(new Comparator<LevelTestData>() {
//                        @Override
//                        public int compare(LevelTestData t1, LevelTestData t2) {
//                            if (t1.seq > t2.seq) return 1;
//                            else if(t1.seq < t2.seq) return -1;
//                            else return 0;
//                        }
//                    });
//                    if(optional.isPresent()){
//                        int lastNoticeSeq = ((LevelTestData)optional.get()).seq;
//                        LogMgr.w(TAG, "scroll listener lastNoticeSeq= " + lastNoticeSeq);
//                        requestBoardList(_selectedLTC.ltcCode, lastNoticeSeq);
//                    }else{
//                        LogMgr.w(TAG, "optional is not present");
//                    }

                }
            }
        });

    }

    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.main_menu_test_reserve);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    void changeMessageState2Read() {
        new Thread(() -> {
            List<PushMessage> pushMessages = JeetDatabase.getInstance(getApplicationContext()).pushMessageDao().getMessageByReadFlagNType(false, FCMManager.MSG_TYPE_LEVEL_TEST);
            if(!pushMessages.isEmpty()) {
                for(PushMessage message : pushMessages) {
                    message.isRead = true;
                    JeetDatabase.getInstance(getApplicationContext()).pushMessageDao().update(message);
                }
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(_userGubun == Constants.USER_TYPE_ADMIN || _userGubun == Constants.USER_TYPE_SUPER_ADMIN) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
            this._menu = menu;
        }
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_setting:
                navigate2LevelTestSettingActivity();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void initGradeList() {
        LogMgr.w(TAG, "initGradeList");
        _gradeList.clear();
        if(!_selectedSchoolLevel.equals(getString(R.string.item_total))){
            boolean isElementary = (Constants.SchoolGradeType.ELEMENTARY.getNameKor()).contains(_selectedSchoolLevel);
            _gradeList.addAll(Constants.StudentGrade.getNameList(isElementary));
        }
        Utils.updateSpinnerList(_spinnerGrade, _gradeList);
        if(_spinnerGrade.isEnabled() && !_gradeList.isEmpty()) {
            _spinnerGrade.selectItemByIndex(0);
        }
    }
    private String extractGrade() {
        StringBuilder sb = new StringBuilder();
        if(_selectedSchoolLevel.equals(getString(R.string.item_total)) || TextUtils.isEmpty(_selectedSchoolLevel)) {
            return null;
        }else{
            if(!TextUtils.isEmpty(_selectedSchoolLevel)) {

                sb.append(_selectedSchoolLevel.substring(0, 1));
                if(_selectedGrade != null && _selectedGrade.getCode() != 0) {
                    sb.append(_selectedGrade.getCode());
                }
            }
        }
        LogMgr.e(TAG, "_selectedSchoolLevel = " + _selectedSchoolLevel + "sb = " + sb.toString());
        return sb.toString();
    }
    // 레벨테스트 글 목록 조회
    private void requestBoardList(String ltcCode, int... lastSeq){
        int lastNoticeSeq = 0;
        if(lastSeq != null && lastSeq.length > 0) {
            LogMgr.e(TAG, "lastSeq[0] = " + lastSeq[0]);
            lastNoticeSeq = lastSeq[0];
        }

        if (RetrofitClient.getInstance() != null){
            final int finalLastNoticeSeq = lastNoticeSeq;
            int memberSeq = 0;  // 매니저앱에서는 memberSeq 를 0으로
            String grade = extractGrade();
            RetrofitClient.getApiInterface().getLevelTestList(memberSeq, ltcCode, grade, finalLastNoticeSeq).enqueue(new Callback<LevelTestListResponse>() {
                @Override
                public void onResponse(Call<LevelTestListResponse> call, Response<LevelTestListResponse> response) {
                    try {
                        if (response.isSuccessful()){
                            List<LevelTestData> getData = null;

                            if (response.body() != null) {

                                getData = response.body().data;
                                if (getData != null){
                                    if(finalLastNoticeSeq == 0) {
                                        if (_list.size() > 0) _list.clear();
                                    }
                                    _list.addAll(getData);
//                                    _list.sort(new Comparator<LevelTestData>() {
//                                        @Override
//                                        public int compare(LevelTestData t1, LevelTestData t2) {
//                                            if (t1.seq > t2.seq) return -1;
//                                            else if(t1.seq < t2.seq) return 1;
//                                            else return 0;
//                                        }
//                                    });
//                                    LinkedHashSet<LevelTestData> set = new LinkedHashSet<>(_list);
//                                    set.addAll(getData);
//                                    for(LevelTestData item : set) {
//                                        LogMgr.e(TAG, item.seq + " / " + item.memberSeq);
//                                    }
//                                    _list.clear();
//                                    _list.addAll(set);
//                                    for(LevelTestData item : set) {
//                                        LogMgr.e(TAG, item.seq + " / " + item.memberSeq);
//                                    }
                                    refreshRecyclerView();

                                }else LogMgr.e(TAG, "ListData is null");
                            }
                        }else{
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e){
                        LogMgr.e(TAG + "requestBoardList() Exception : ", e.getMessage());
                    }finally {
                        _swipeRefreshLayout.setRefreshing(false);
                    }
                }

                @Override
                public void onFailure(Call<LevelTestListResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "requestBoardList() onFailure >> " + t.getMessage());
                    }catch (Exception e){
                    }
//                    hideProgressDialog();
                    _swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void refreshRecyclerView() {
        if(_listAdapter != null) {
            _listAdapter.notifyDataSetChanged();
            if(_list.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
            }else{
                tvEmpty.setVisibility(View.GONE);
            }
        }
    }

    private void navigate2LevelTestDetailActivity(LevelTestData item, int position) {
        Intent detailIntent = new Intent(mContext, MenuLevelTestDetailActivity.class);
        detailIntent.putExtra(IntentParams.PARAM_LEVELTEST_INFO, item);
        detailIntent.putExtra(IntentParams.PARAM_BOARD_POSITION, position);
        resultLauncher.launch(detailIntent);
//        startActivity(detailIntent);
        overridePendingTransition(R.anim.horizontal_enter, R.anim.horizontal_out);
    }

    private void navigate2LevelTestSettingActivity() {
        Intent settingIntent = new Intent(mContext, LevelTestSettingActivity.class);
        startActivity(settingIntent);
    }
}
