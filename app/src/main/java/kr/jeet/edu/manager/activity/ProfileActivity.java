package kr.jeet.edu.manager.activity;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.adapter.ClassListAdapter;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.model.data.ClassData;
import kr.jeet.edu.manager.model.data.ManagerInfo;
import kr.jeet.edu.manager.model.response.GetManagerClassInfoResponse;
import kr.jeet.edu.manager.server.RetrofitClient;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.PreferenceUtil;
import kr.jeet.edu.manager.utils.Utils;
import kr.jeet.edu.manager.view.CustomAppbarLayout;
import kr.jeet.edu.manager.view.decoration.LastIndexDeleteDecoration;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends BaseActivity {
    private final String TAG = "profile";
    private SimpleDateFormat birthdayFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYY_MM_DD_KOR);
    private SimpleDateFormat YMDFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYY_MM_DD);
    //region Views--
    TextView tvName, tvCampus, tvBirthday, tvPhoneNo, tvDept, tvEmpty;
    ImageView ivProfile;
    RecyclerView _recyclerViewClass;
    ConstraintLayout layoutFirst, layoutSecond;
    //endregion Views--
    //region Variables--
    private ArrayList<String> _classList = new ArrayList<>();
    private ManagerInfo _currentManagerData;
    ClassListAdapter _listAdapter;
    int _userGubun = 1;
    int _seq = 0;
    int _sfCode = 0;

    private String _acaCode = "";
    //endregion Variables--
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mContext = this;
        _acaCode = PreferenceUtil.getAcaCode(mContext);
        _userGubun = PreferenceUtil.getUserGubun(this);
        _seq = PreferenceUtil.getUserSeq(this);
        _sfCode = PreferenceUtil.getUserSFCode(this);
        initIntentData();
        initView();
        initAppbar();
        initData();
        setAnimMove(Constants.MOVE_DOWN);
    }
    void initIntentData() {
        Intent intent = getIntent();
        if(intent == null) return;
        if(intent.hasExtra(IntentParams.PARAM_MANAGER_INFO)) {
            LogMgr.w("param is recived");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                _currentManagerData = intent.getParcelableExtra(IntentParams.PARAM_MANAGER_INFO, ManagerInfo.class);
            }else{
                _currentManagerData = intent.getParcelableExtra(IntentParams.PARAM_MANAGER_INFO);
            }
        }
    }
    void initView() {
        tvName = findViewById(R.id.tv_stu_info_name);
        tvCampus = findViewById(R.id.tv_stu_info_campus);
        tvDept = findViewById(R.id.tv_stu_info_dept_name);
//        tvClst = findViewById(R.id.tv_stu_info_st_grade);
        tvBirthday = findViewById(R.id.tv_stu_info_birthday);
        tvPhoneNo = findViewById(R.id.tv_stu_info_phone_num);
        tvEmpty = findViewById(R.id.tv_class_empty);
        ivProfile = findViewById(R.id.img_stu_info_profile);
        _recyclerViewClass = findViewById(R.id.recycler_class);
        layoutFirst = findViewById(R.id.layout_consultation_request);
        layoutSecond = findViewById(R.id.layout_class);
        _listAdapter = new ClassListAdapter(mContext, _classList, new ClassListAdapter.ItemClickListener() {
            @Override
            public void onItemClick(String item) {

            }

        });
        _recyclerViewClass.setAdapter(_listAdapter);
//        _recyclerViewClass.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        Drawable dividerDrawable = ContextCompat.getDrawable(mContext, R.drawable.bg_line);
        LastIndexDeleteDecoration dividerItemDecoration = new LastIndexDeleteDecoration(dividerDrawable);
        _recyclerViewClass.addItemDecoration(dividerItemDecoration);
//        _recyclerViewClass.addItemDecoration(Utils.setDivider(mContext));
    }

    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.title_main_teacher_profile);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    void initData() {
        if(_currentManagerData != null) {
            tvName.setText(_currentManagerData.name);
            tvCampus.setText(_currentManagerData.acaName);
            StringBuilder sb = new StringBuilder();
            if(TextUtils.isEmpty(_currentManagerData.deptName)) {
                if(TextUtils.isEmpty(_currentManagerData.sfGrade)){

                }else{
                    sb.append(_currentManagerData.sfGrade);
                }
            }else {
                if(TextUtils.isEmpty(_currentManagerData.sfGrade)){
                    sb.append(_currentManagerData.deptName);
                }else{
                    sb.append(_currentManagerData.deptName + " / " + _currentManagerData.sfGrade);
                }
            }
            tvDept.setText(sb.toString());
            try {
                Date birthDayDate = YMDFormat.parse(_currentManagerData.birth);
                String birthDayStr = birthdayFormat.format(birthDayDate);
                tvBirthday.setText(birthDayStr);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            tvPhoneNo.setText(Utils.formatPhoneNumber(_currentManagerData.phoneNumber));
            if(_currentManagerData.gender.equals("M")) {
                ivProfile.setBackground(getDrawable(R.drawable.img_profile_teacher_man));
            }else{
                ivProfile.setBackground(getDrawable(R.drawable.img_profile_teacher_woman));
            }
            requestGetClassList(_sfCode);

        }else{
            finish();
        }
    }

    private void requestGetClassList(int sfCode){

        if (RetrofitClient.getInstance() != null){
            RetrofitClient.getApiInterface().getManagerClassInfo(sfCode).enqueue(new Callback<GetManagerClassInfoResponse>() {
                @Override
                public void onResponse(Call<GetManagerClassInfoResponse> call, Response<GetManagerClassInfoResponse> response) {
                    try {
                        _classList.clear();
                        if (response.isSuccessful()){
                            List<ClassData> getData = null;
                            if (response.body() != null) {

                                getData = response.body().data;
                                if (getData != null){
                                    for (ClassData data : getData) {
                                        _classList.add(data.clsName);
                                    }

                                }else {

                                }
                            }
                        }else{
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e){
                        LogMgr.e(TAG + "requestBoardList() Exception : ", e.getMessage());
                    }finally{
                        _listAdapter.notifyDataSetChanged();
                        if(_classList.size() == 0) {
                            tvEmpty.setVisibility(View.VISIBLE);
                        }
                    }
                    animateLayout(layoutFirst);
                    Utils.animateLayoutMoveLeft(layoutFirst, mContext);
                }
                @Override
                public void onFailure(Call<GetManagerClassInfoResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "requestBoardList() onFailure >> " + t.getMessage());
                    }catch (Exception e){
                    }
                    animateLayout(layoutFirst);
                    Utils.animateLayoutMoveLeft(layoutFirst, mContext);
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
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
                if (view == layoutFirst) {
                    animateLayout(layoutSecond);
                    Utils.animateLayoutMoveLeft(layoutSecond, mContext);
                }
            }
        });
        animator.start();
    }
}