package kr.jeet.edu.manager.activity.menu.reportcard;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.activity.BaseActivity;
import kr.jeet.edu.manager.adapter.AbstractReportCardShowAdapter;
import kr.jeet.edu.manager.adapter.ReportCardShowType0Adapter;
import kr.jeet.edu.manager.adapter.ReportCardShowType3Adapter;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.CustomGridLayoutMgr;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.model.data.ReportCardData;
import kr.jeet.edu.manager.model.data.ReportCardExamData;
import kr.jeet.edu.manager.model.data.ReportCardExamFooterData;
import kr.jeet.edu.manager.model.data.ReportCardExamHeaderData;
import kr.jeet.edu.manager.model.data.ReportCardShowData;
import kr.jeet.edu.manager.model.data.ReportCardShowListItemData;
import kr.jeet.edu.manager.model.response.ReportCardShowResponse;
import kr.jeet.edu.manager.server.RetrofitApi;
import kr.jeet.edu.manager.server.RetrofitClient;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.PreferenceUtil;
import kr.jeet.edu.manager.utils.Utils;
import kr.jeet.edu.manager.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShowReportCardActivity extends BaseActivity {

    private static final String TAG = "ReportCardShowActivity";
    public interface ExamListTypeItem extends Comparable<ExamListTypeItem> {
        int getEsGubun();
        int getType();
    }
    private TextView tvProcess, tvDate, tvSubject, tvName, tvSchool;
    private TextView tvEmptyList;
    private ConstraintLayout layoutInfo;
    private RecyclerView mRecycler;
    private AbstractReportCardShowAdapter mAdapter;
    public int _boardSeq = 0;
    public ReportCardData _currentData;
    private ArrayList<ShowReportCardActivity.ExamListTypeItem> mList = new ArrayList<>();

    private int _memberSeq = 0;
    private int _userGubun = 0;
    private String _acaCode = "";

    private int spanCount = Constants.REPORT_MATH_SPAN_COUNT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_report_card);
        _memberSeq = PreferenceUtil.getUserSeq(mContext);
        _userGubun = PreferenceUtil.getUserGubun(mContext);
        _acaCode = PreferenceUtil.getAcaCode(mContext);
        initIntentData();
        initView();
        initAppbar();
        initData();
        setAnimMove(Constants.MOVE_DETAIL_RIGHT);
    }

    private void initData() {
        requestReportCardShowList();
    }
    void initIntentData() {
        Intent intent = getIntent();
        if(intent != null) {
            if(intent.hasExtra(IntentParams.PARAM_BOARD_ITEM)) {
                LogMgr.w("param is recived");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    _currentData = intent.getParcelableExtra(IntentParams.PARAM_BOARD_ITEM, ReportCardData.class);
                }else{
                    _currentData = intent.getParcelableExtra(IntentParams.PARAM_BOARD_ITEM);
                }
            }
            if(intent.hasExtra(IntentParams.PARAM_BOARD_SEQ)) {
                _boardSeq = intent.getIntExtra(IntentParams.PARAM_BOARD_SEQ, -1);
            }
        }
        if(_currentData == null || _boardSeq == -1) {
            finish();
        }
    }
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(getString(R.string.title_report_card));
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    void initView() {
        layoutInfo = findViewById(R.id.layout_exam_info);
        tvProcess = findViewById(R.id.tv_process);
        tvDate = findViewById(R.id.tv_date);
        tvSubject = findViewById(R.id.tv_subject);
        tvName = findViewById(R.id.tv_name);
        tvSchool = findViewById(R.id.tv_school);
        tvEmptyList = findViewById(R.id.tv_empty_list);
        mRecycler = (RecyclerView) findViewById(R.id.recycler_exam);
        if(_currentData.etTitleGubun == Constants.ReportCardType.E_ELEMENTARY.getCode() || _currentData.etTitleGubun == Constants.ReportCardType.E_MIDDLE.getCode()) {
            LinearLayoutManager layoutMgr = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
            mRecycler.setLayoutManager(layoutMgr);
            mAdapter = new ReportCardShowType0Adapter(mContext, mList);
        }else if(_currentData.etTitleGubun == Constants.ReportCardType.KJ_E_MATH.getCode()) {
            // 악어수학용 Adapter
            CustomGridLayoutMgr layoutMgr = new CustomGridLayoutMgr(mContext, spanCount);
            mRecycler.setLayoutManager(layoutMgr);
            int padding = Utils.fromDpToPx(1);
            mRecycler.setPadding(padding, padding, padding, padding);
            mAdapter = new ReportCardShowType3Adapter(mContext, mList);
        }
        mRecycler.setAdapter(mAdapter);

        long delayed = getResources().getInteger(R.integer.screen_in_time);
        new Handler().postDelayed(() -> {
            animateLayout(layoutInfo);
            Utils.animateLayoutMoveLeft(layoutInfo, mContext);
        }, delayed);
    }
    private void requestReportCardShowList() {
        if (RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getReportCardShowList(_boardSeq, _currentData.seq).enqueue(new Callback<ReportCardShowResponse>() {
                @Override
                public void onResponse(Call<ReportCardShowResponse> call, Response<ReportCardShowResponse> response) {


                    try {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                mList.clear();
                                ReportCardShowData getData = response.body().data;
                                initUI(getData);
                                parsingData(getData);

                            }
                        } else {
                            int code = response.code();
                            if (code == RetrofitApi.RESPONSE_CODE_NOT_FOUND) {
                                showMessageDialog(getString(R.string.dialog_title_error)
                                        , getString(R.string.item_not_found)
                                        , new View.OnClickListener() {  //OKClickListener
                                            @Override
                                            public void onClick(View view) {
                                                finish();
                                                overridePendingTransition(R.anim.horizontal_in, R.anim.horizontal_exit);
                                                hideMessageDialog();
                                            }
                                        },
                                        null,
                                        false);
                            } else {
                                Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestReportShow() Exception : ", e.getMessage());
                    }

                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }
                    tvEmptyList.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);
                }

                @Override
                public void onFailure(Call<ReportCardShowResponse> call, Throwable t) {
                    mList.clear();
                    if (mAdapter != null) mAdapter.notifyDataSetChanged();
                    tvEmptyList.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);

                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }

    }
    void initUI(ReportCardShowData data) {
        tvProcess.setText(data.etGubun);
        tvDate.setText(data.regDate);
        tvSubject.setText(data.etName);
        tvName.setText(data.stName);
        String schoolName = TextUtils.isEmpty(data.scName)? "-" : data.scName;
        String gradeName = TextUtils.isEmpty(data.stGrade)? "-" : data.stGrade;
        tvSchool.setText(String.format("%s / %s", schoolName, gradeName));
    }
    void parsingData(ReportCardShowData data) {
        List<ReportCardShowListItemData> listItem = data.list;
        for(int i = 0; i < listItem.size(); i++) {
            ReportCardShowListItemData listItemData = listItem.get(i);
            List<ReportCardExamData> examListData = listItemData.dataList;
            for(int j = 0; j < examListData.size(); j++) {
                ReportCardExamData examItem = examListData.get(j);
                if(_currentData.etTitleGubun == Constants.ReportCardType.KJ_E_MATH.getCode()) { // 악어수학
                    if(j == examListData.size() - 1) { //dataList 마지막에 Footer 추가
                        mList.add(new ReportCardExamFooterData(listItemData.esGubun, listItemData.totalScore, listItemData.totalCount, listItemData.correctCount, listItemData.correctRate, examItem.esTitle, examItem.esNum));
                    }
                } else { // 악어초등, 중등
                    if(j ==0) { //dataList 첫번째에서 Header, Footer 추가
                        mList.add(new ReportCardExamHeaderData(examItem.esGubun, examItem.esTitle));
                        mList.add(new ReportCardExamFooterData(listItemData.esGubun, listItemData.totalScore, listItemData.totalCount, listItemData.correctCount, listItemData.correctRate, examItem.esTitle));
                    }
                }
                mList.add(new ReportCardExamData(examItem.esGubun, examItem.esNum, examItem.esName, examItem.esScore));
            }
        }

        if(_currentData.etTitleGubun == Constants.ReportCardType.KJ_E_MATH.getCode()) { // 악어수학인 경우 spanCount에 맞게 ui 채우기
            int currentSize = mList.size() - 1;
            int targetSize = 0;

            if (currentSize % spanCount != 0) {
                targetSize = (currentSize + spanCount) / spanCount * spanCount + 1;
                ReportCardExamData dummy = new ReportCardExamData();
                dummy.esNum = 0;
                while (mList.size() < targetSize) mList.add(dummy);
            }
        }
        Collections.sort(mList);
    }
    private void animateLayout(final View view) {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(Constants.LAYOUT_ANIM_DURATION);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());

        animator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            view.setAlpha(progress); // 애니메이션 중간값을 알파값으로 설정하여 서서히 보이도록 함
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // 애니메이션 종료 후 다음 레이아웃으로 전환
                if (view == layoutInfo) {
                    animateLayout(mRecycler);
                    Utils.animateLayoutMoveLeft(mRecycler, mContext);

                }

            }
        });
        animator.start();
    }
}