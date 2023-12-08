package kr.jeet.edu.manager.activity.menu.qna;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.activity.BaseActivity;
import kr.jeet.edu.manager.adapter.AnswererListAdapter;
import kr.jeet.edu.manager.common.DataManager;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.model.data.AnswererData;
import kr.jeet.edu.manager.model.request.UpdateAnswererRequest;
import kr.jeet.edu.manager.model.request.UpdateReportCardRequest;
import kr.jeet.edu.manager.model.response.BaseResponse;
import kr.jeet.edu.manager.model.response.GetAnswererListResponse;
import kr.jeet.edu.manager.server.RetrofitClient;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.PreferenceUtil;
import kr.jeet.edu.manager.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectAnswererActivity extends BaseActivity {
    private static final String TAG = "selectAnswerer";
    private static final int CMD_GET_ANSWERER_LIST = 0;
    EditText etSearch;
    TextView tvSelectCount, tvEmptyList;
    RecyclerView recyclerViewAnswerer;
    AnswererListAdapter listAdapter;

    List<AnswererData> _answererList = new ArrayList<>();
    int _currentSeq = -1;
    Menu _menu;
    private Handler _handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {

                case CMD_GET_ANSWERER_LIST:
                    requestAnswererList();
                    break;

            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_answerer);
        mContext = this;
        initIntentData();
        initView();
        initAppbar();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        _handler.sendEmptyMessage(CMD_GET_ANSWERER_LIST);
    }
    void initIntentData(){
        Intent intent = getIntent();
        if(intent == null) return;
        if(intent.hasExtra(IntentParams.PARAM_BOARD_SEQ)) {
            _currentSeq = intent.getIntExtra(IntentParams.PARAM_BOARD_SEQ, -1);
        }
    }
    void initView() {
        tvSelectCount = findViewById(R.id.tv_manager_count);
        tvEmptyList = findViewById(R.id.tv_empty_list);
        etSearch = findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s != null) {
                    String trigger = s.toString();
                    searchFilter(trigger);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        recyclerViewAnswerer = findViewById(R.id.recyclerview_answerer);
        listAdapter = new AnswererListAdapter(mContext, _answererList, new AnswererListAdapter.ItemClickListener() {
            @Override
            public void onCheckChanged(int position, AnswererData item, boolean isChecked) {
                int count = (int)_answererList.stream().filter(t->t.isSelected).count();
                tvSelectCount.setText(getString(R.string.selected_count, count));
            }

            @Override
            public void onFilteringCompleted() {

            }
        });
        recyclerViewAnswerer.setAdapter(listAdapter);
        recyclerViewAnswerer.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
    }
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.title_select_manager);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
                if(checkForUpdate()){
                    showMessageDialog(getString(R.string.dialog_title_alarm)
                            , getString(R.string.qna_answerer_confirm_select,(int)_answererList.stream().filter(t->t.isSelected).count())
                            , new View.OnClickListener() {  //OKClickListener
                                @Override
                                public void onClick(View view) {
                                    requestUpdateAnswerer();
                                    hideMessageDialog();
                                }
                            },
                            new View.OnClickListener() {    //cancelClickListener
                                @Override
                                public void onClick(View view) {
                                    hideMessageDialog();
                                }
                            });

                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private boolean checkForUpdate() {
        if(!_answererList.stream().anyMatch(t->t.isSelected)) {
            Toast.makeText(mContext, R.string.error_msg_no_selected_manager, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    private void requestUpdateAnswerer() {
        if (RetrofitClient.getInstance() != null) {
            UpdateAnswererRequest request = new UpdateAnswererRequest();
            request.initRequest(
                    _currentSeq,
                    _answererList.stream().filter(t->t.isSelected).collect(Collectors.toList())
            );
            RetrofitClient.getApiInterface().updateAnswererList(request).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {

                    try {
                        if (response.isSuccessful()) {
                            Intent intent = new Intent();
                            intent.putExtra(IntentParams.PARAM_BOARD_ADDED, true);
                            setResult(RESULT_OK, intent);
                            overridePendingTransition(R.anim.horizontal_in, R.anim.horizontal_exit);
                            finishAfterTransition();
                            Toast.makeText(mContext, R.string.qna_specify_answerer_success, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestTestReserveList() Exception : ", e.getMessage());
                    }

                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    _answererList.clear();
                    if(listAdapter != null) listAdapter.notifyDataSetChanged();
//                    tvEmptyList.setVisibility(_reportCardList.isEmpty() ? View.VISIBLE : View.GONE);

                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });

        }
    }
    private void requestAnswererList() {

        if (RetrofitClient.getInstance() != null) {
            Call<GetAnswererListResponse> call = RetrofitClient.getApiInterface().getAnswererList();

            call.enqueue(new Callback<GetAnswererListResponse>() {
                @Override
                public void onResponse(Call<GetAnswererListResponse> call, Response<GetAnswererListResponse> response) {
                    try {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {

                                List<AnswererData> getData = response.body().data;
                                if (_answererList != null) _answererList.clear();
                                if (getData != null) {
                                    _answererList.addAll(getData);
                                    String searchStr = "";
                                    if(etSearch != null && etSearch.getText() != null) {
                                        searchStr = etSearch.getText().toString().trim();
                                    }

                                    if(!TextUtils.isEmpty(searchStr)) {
                                        searchFilter(searchStr);
                                    }else{
                                        listAdapter.notifyDataSetChanged();
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

                    }
                }

                @Override
                public void onFailure(Call<GetAnswererListResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "requestBoardDetail() onFailure >> " + t.getMessage());
                    } catch (Exception e) {
                    }
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void searchFilter(String searchStr){
        if(listAdapter != null) {
            LogMgr.e(TAG, "filter with " + searchStr);
            listAdapter.getFilter().filter(searchStr);
        }
    }
    private boolean checkEmptyRecyclerView() {
        if (listAdapter.getItemCount() > 0) {
            tvEmptyList.setVisibility(View.INVISIBLE);
            return false;
        } else {
            tvEmptyList.setVisibility(View.VISIBLE);
            return true;
        }
    }

}