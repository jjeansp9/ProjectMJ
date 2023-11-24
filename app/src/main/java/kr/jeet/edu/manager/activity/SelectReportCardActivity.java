package kr.jeet.edu.manager.activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.adapter.SelectReportCardListAdapter;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.IntentParams;

import kr.jeet.edu.manager.model.data.RecipientStudentData;
import kr.jeet.edu.manager.model.data.ReportCardData;
import kr.jeet.edu.manager.model.response.ReportCardListResponse;
import kr.jeet.edu.manager.server.RetrofitClient;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.PreferenceUtil;
import kr.jeet.edu.manager.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectReportCardActivity extends BaseActivity {
    private static final String TAG = "selectReportCard";
    private static final int CMD_GET_REPORT_CARD = 1;
    private RecyclerView _recyclerView;
    private SwipeRefreshLayout _swipeRefreshLayout;
    private SelectReportCardListAdapter _listAdapter;
    private TextView tvEmptyList;

    private RecipientStudentData _currentStudent = null;
    private ArrayList<ReportCardData> _reportCardList = new ArrayList<ReportCardData>();

    int _userGubun = 1;
    int _seq = 0;
    int _sfCode = 0;
    int _stCode = 0;
    Menu _menu;

    private Handler _handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case CMD_GET_REPORT_CARD:
                    //todo request logic
                    requestReportCardList();
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
                        setResult(RESULT_OK, intent);
                        overridePendingTransition(R.anim.horizontal_in, R.anim.horizontal_exit);
                        finishAfterTransition();
                    }
                }
            }
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_report_card);
        mContext = this;

        _userGubun = PreferenceUtil.getUserGubun(this);
        _seq = PreferenceUtil.getUserSeq(this);
        _sfCode = PreferenceUtil.getUserSFCode(this);

        initIntentData();
        initAppbar();
        initView();
        setAnimMove(Constants.MOVE_DETAIL_RIGHT);
        _handler.sendEmptyMessage(CMD_GET_REPORT_CARD);
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        if(_reportCardList != null && !_reportCardList.isEmpty()) {
//            _reportCardList.stream().forEach(t-> t.isSelected = false);
//            if(_listAdapter != null) {
//                _listAdapter.notifyDataSetChanged();
//            }
//        }
//    }

    private void initIntentData() {
        Intent intent = getIntent();
        if(intent != null) {
            if (intent.hasExtra(IntentParams.PARAM_STUDENT_INFO)){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    _currentStudent = intent.getParcelableExtra(IntentParams.PARAM_STUDENT_INFO, RecipientStudentData.class);
                }else{
                    _currentStudent = intent.getParcelableExtra(IntentParams.PARAM_STUDENT_INFO);
                }
            }
        }
        if(_currentStudent == null) {
            finish();
        }

    }
    @Override
    void initView() {
        tvEmptyList = findViewById(R.id.tv_empty_list);
        _swipeRefreshLayout = findViewById(R.id.refresh_layout);
        _swipeRefreshLayout.setOnRefreshListener( () ->{
//            Calendar calendar = Calendar.getInstance();
//            calendar.setTime(_selectedDate);
            Message msg = _handler.obtainMessage(CMD_GET_REPORT_CARD);
//            msg.arg1 = calendar.get(Calendar.YEAR);
//            msg.arg2 = calendar.get(Calendar.MONTH);
            _handler.sendMessage(msg);

        });
        //region set recyclerview
        _recyclerView = findViewById(R.id.recycler_report_card);
        _listAdapter = new SelectReportCardListAdapter(mContext, _reportCardList, Constants.ReportCardListType.CHECK, new SelectReportCardListAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int position, ReportCardData item) {

            }

        });

        _recyclerView.setAdapter(_listAdapter);
        _recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.title_list_report_card);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_next, menu);
        int positionOfMenuItem = 0;
        try {
            MenuItem item = menu.getItem(positionOfMenuItem);
            SpannableString span = new SpannableString(item.getTitle());
            span.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.red)), 0, span.length(), 0);
            item.setTitle(span);
        }catch(Exception ex){}

        this._menu = menu;
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_next:
                ArrayList<ReportCardData> list = new ArrayList<>(_reportCardList.stream().filter(t->t.isSelected).collect(Collectors.toList()));
                if(list.isEmpty()) {
                    Toast.makeText(mContext, "전송할 성적표를 선택해 주세요.", Toast.LENGTH_SHORT).show();
                }else {
                    navigate2ConfirmReportCardActivity(list);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void requestReportCardList(){
        if (RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getReportCardList(_currentStudent.stCode).enqueue(new Callback<ReportCardListResponse>() {
                @Override
                public void onResponse(Call<ReportCardListResponse> call, Response<ReportCardListResponse> response) {


                    try {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                _reportCardList.clear();
                                List<ReportCardData> list = response.body().data;
                                if (list != null && !list.isEmpty()) {
                                    _reportCardList.addAll(list);
                                }
                            }
                        } else {
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestTestReserveList() Exception : ", e.getMessage());
                    }

                    if(_listAdapter != null) {
                        _listAdapter.notifyDataSetChanged();
                    }
                    tvEmptyList.setVisibility(_reportCardList.isEmpty() ? View.VISIBLE : View.GONE);
                    _swipeRefreshLayout.setRefreshing(false);
                }

                @Override
                public void onFailure(Call<ReportCardListResponse> call, Throwable t) {
                    _reportCardList.clear();
                    if(_listAdapter != null) _listAdapter.notifyDataSetChanged();
                    tvEmptyList.setVisibility(_reportCardList.isEmpty() ? View.VISIBLE : View.GONE);

                    _swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            _swipeRefreshLayout.setRefreshing(false);
        }
    }

    public void navigate2ConfirmReportCardActivity(ArrayList<ReportCardData> list) {
        Intent intent = new Intent(mContext, ConfirmReportCardActivity.class);
        intent.putParcelableArrayListExtra(IntentParams.PARAM_REPORT_CARD_LIST, list);
        intent.putExtra(IntentParams.PARAM_STUDENT_INFO, _currentStudent);
//        if(_selectedACA != null) {
//            editIntent.putExtra(IntentParams.PARAM_STU_ACACODE, _selectedACA.acaCode);
//        }
        resultLauncher.launch(intent);
        overridePendingTransition(R.anim.horizontal_enter, R.anim.horizontal_out);
//        startActivity(editIntent);
    }
}