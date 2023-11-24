package kr.jeet.edu.manager.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.adapter.BriefingReservedListAdapter;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.model.data.BriefingReservedListData;
import kr.jeet.edu.manager.model.response.BriefingReservedListResponse;
import kr.jeet.edu.manager.model.response.StringResponse;
import kr.jeet.edu.manager.server.RetrofitApi;
import kr.jeet.edu.manager.server.RetrofitClient;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.PreferenceUtil;
import kr.jeet.edu.manager.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuBriefingReservedListActivity extends BaseActivity {

    private final static String TAG = "BriefingReservedList";

    private TextView mTvEmptyList;
    private RecyclerView mRecyclerBrf;
    private SwipeRefreshLayout mSwipeRefresh;

    private BriefingReservedListAdapter mAdapter;

    private RetrofitApi mRetrofitApi;

    private ArrayList<BriefingReservedListData> mList = new ArrayList<>();
    int _userGubun = 1;
    int _seq = 0;
    int _sfCode = 0;

    private int ptSeq = 0; // 글 seq
    private int ptCnt = 0; // 참가인원
    private int rvCnt = 0; // 현재예약 수
    Constants.BriefingType briefingType = Constants.BriefingType.TYPE_OPEN;
    boolean isEdited = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_briefing_reserved_list);
        mContext = this;
        _userGubun = PreferenceUtil.getUserGubun(this);
        _seq = PreferenceUtil.getUserSeq(this);
        _sfCode = PreferenceUtil.getUserSFCode(this);
        initData();
        initView();
        initAppbar();
    }

    private void initData(){
        Intent intent= getIntent();
        if(intent == null) return;
        if (
                intent.hasExtra(IntentParams.PARAM_BRIEFING_PT_SEQ) &&
                        intent.hasExtra(IntentParams.PARAM_BRIEFING_PARTICIPANTS_CNT) &&
                        intent.hasExtra(IntentParams.PARAM_BRIEFING_RESERVATION_CNT)){

            ptSeq = intent.getIntExtra(IntentParams.PARAM_BRIEFING_PT_SEQ, ptSeq);
            ptCnt = intent.getIntExtra(IntentParams.PARAM_BRIEFING_PARTICIPANTS_CNT, ptCnt);
            rvCnt = intent.getIntExtra(IntentParams.PARAM_BRIEFING_RESERVATION_CNT, rvCnt);
        }
        if(intent.hasExtra(IntentParams.PARAM_BRIEFING_TYPE)) {
            briefingType = (Constants.BriefingType) intent.getSerializableExtra(IntentParams.PARAM_BRIEFING_TYPE);
        }

        requestBrfReservedListData();
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.briefing_reserved_list);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    void initView() {
        mTvEmptyList = findViewById(R.id.tv_empty_list);
//        mTvReservedCnt = findViewById(R.id.tv_reserved_cnt);
        mRecyclerBrf = findViewById(R.id.recycler_brf_reserve);
        //mSwipeRefresh = findViewById(R.id.refresh_layout);

        mAdapter = new BriefingReservedListAdapter(mContext, mList, new BriefingReservedListAdapter.ItemClickListener() {
            @Override
            public void onDeleteClick(BriefingReservedListData item) {
                showMessageDialog(getString(R.string.dialog_title_alarm)
                        , getString(R.string.reservation_item_confirm_delete)
                        , new View.OnClickListener() {  //OKClickListener
                            @Override
                            public void onClick(View view) {
                                if (item != null) {
                                    LogMgr.w(TAG, "seq = " + item.seq + " / name = " + item.name);
                                    requestDeleteBrfReservation(item);
                                }
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
        });
        mAdapter.setUserGubun(_userGubun);
        mAdapter.setReservable(briefingType.equals(Constants.BriefingType.TYPE_OPEN));
        mRecyclerBrf.setAdapter(mAdapter);
        mRecyclerBrf.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));

        mRecyclerBrf.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(((!mRecyclerBrf.canScrollVertically(1)) && mRecyclerBrf.canScrollVertically(-1))
                        && newState == RecyclerView.SCROLL_STATE_IDLE
                        && (mList != null && !mList.isEmpty()))
                {

                }
            }
        });
        mAdapter.notifyDataSetChanged();

//        mSwipeRefresh.setOnRefreshListener( () -> {
//            mSwipeRefresh.setRefreshing(false);
//        } );

//        String str = getString(R.string.briefing_write_personnel_cnt, rvCnt, ptCnt);
//        mTvReservedCnt.setText(str);
    }

    private void requestBrfReservedListData(){
        if (RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.getBrfReservedList(ptSeq, 0).enqueue(new Callback<BriefingReservedListResponse>() {
                @Override
                public void onResponse(Call<BriefingReservedListResponse> call, Response<BriefingReservedListResponse> response) {
                    try {
                        if (response.isSuccessful()) {
                            List<BriefingReservedListData> getData;
                            if(mList != null) mList.clear();
                            if (response.body() != null) {
                                getData = response.body().data;
                                if (getData != null && !getData.isEmpty()) {
                                    mList.addAll(getData);

                                } else {
                                    LogMgr.e(TAG, "ListData is null");
                                }
                            }
                        } else {
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestBrfReservedListData() Exception: ", e.getMessage());
                    }
                    mTvEmptyList.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);
                    if (mAdapter != null) mAdapter.notifyDataSetChanged();
                    //mSwipeRefresh.setRefreshing(false);
                }

                @Override
                public void onFailure(Call<BriefingReservedListResponse> call, Throwable t) {
                    mTvEmptyList.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);
                    try {
                        LogMgr.e(TAG, "requestBrfReservedListData() onFailure >> " + t.getMessage());
                    } catch (Exception e) {
                    }
                    hideProgressDialog();
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                    //mSwipeRefresh.setRefreshing(false);
                }
            });
        }
    }
    private void requestDeleteBrfReservation(BriefingReservedListData data){
        if (RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.deleteBriefingReservation(data.seq, _seq, _userGubun).enqueue(new Callback<StringResponse>() {
                @Override
                public void onResponse(Call<StringResponse> call, Response<StringResponse> response) {
                    try {
                        if (response.isSuccessful()) {
                            isEdited = true;
                            requestBrfReservedListData();
                        } else {
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestBrfReservedListData() Exception: ", e.getMessage());
                    }

                }

                @Override
                public void onFailure(Call<StringResponse> call, Throwable t) {
                    mTvEmptyList.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);
                    try {
                        LogMgr.e(TAG, "requestBrfReservedListData() onFailure >> " + t.getMessage());
                    } catch (Exception e) {
                    }
                    hideProgressDialog();
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                    //mSwipeRefresh.setRefreshing(false);
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if(isEdited) {
            Intent intent = new Intent();
            intent.putExtra(IntentParams.PARAM_BOARD_EDITED, isEdited);
            setResult(RESULT_OK, intent);
            finish();
        }else {
            super.onBackPressed();
        }
    }
}