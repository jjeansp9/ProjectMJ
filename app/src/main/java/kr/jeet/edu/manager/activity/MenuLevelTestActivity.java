package kr.jeet.edu.manager.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;
import com.skydoves.powerspinner.OnSpinnerOutsideTouchListener;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import kr.jeet.edu.manager.R;
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
import kr.jeet.edu.manager.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuLevelTestActivity extends BaseActivity {
    private static final String TAG = "leveltest";
    private RecyclerView _recyclerView;
    private LevelTestListAdapter _listAdapter;

    private PowerSpinnerView _spinnerLTC;
    private SwipeRefreshLayout _swipeRefreshLayout;
    private TextView tvEmpty;
    private ArrayList<LevelTestData> _list = new ArrayList<>();

    int _userGubun = 1;
    int _seq = 0;
    int _sfCode = 0;

    private String _ltcCode = "";
    List<LTCData> _LTCList = new ArrayList<>();
    LTCData _selectedLTC = null;
    String _userType = "";
    Menu _menu;
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

    @Override
    void initView() {
        tvEmpty = findViewById(R.id.tv_empty_list);
        _swipeRefreshLayout = findViewById(R.id.refresh_layout);
        _recyclerView = findViewById(R.id.recyclerview_leveltest);
        _listAdapter = new LevelTestListAdapter(mContext, _list, new LevelTestListAdapter.ItemClickListener() {
            @Override
            public void onItemClick(LevelTestData item) {
                navigate2LevelTestDetailActivity(item);
            }
        });
        _recyclerView.setAdapter(_listAdapter);
        _recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));

        _spinnerLTC = findViewById(R.id.spinner_campus);
        _spinnerLTC.setIsFocusable(true);
        _LTCList.clear();
        _LTCList.add(new LTCData("0", "전체"));
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
                    int lastNoticeSeq = _list.get(_list.size() - 1).seq;
                    LogMgr.w(TAG, "scroll listener lastNoticeSeq= " + lastNoticeSeq);
                    requestBoardList(_selectedLTC.ltcCode, lastNoticeSeq);
                }
            }
        });
    }

    @Override
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
            RetrofitClient.getApiInterface().getLevelTestList(memberSeq, ltcCode, finalLastNoticeSeq).enqueue(new Callback<LevelTestListResponse>() {
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
                                    _list.sort(new Comparator<LevelTestData>() {
                                        @Override
                                        public int compare(LevelTestData t1, LevelTestData t2) {
                                            if (t1.seq > t2.seq) return -1;
                                            else if(t1.seq < t2.seq) return 1;
                                            else return 0;
                                        }
                                    });
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

    private void navigate2LevelTestDetailActivity(LevelTestData item) {
        Intent detailIntent = new Intent(mContext, MenuLevelTestDetailActivity.class);
        detailIntent.putExtra(IntentParams.PARAM_LEVELTEST_INFO, item);
//        resultLauncher.launch(detailIntent);
        startActivity(detailIntent);
        overridePendingTransition(R.anim.horizontal_enter, R.anim.horizontal_out);
    }

    private void navigate2LevelTestSettingActivity() {
        Intent settingIntent = new Intent(mContext, LevelTestSettingActivity.class);
        startActivity(settingIntent);
    }
}
