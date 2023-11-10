package kr.jeet.edu.manager.activity;

import static kr.jeet.edu.manager.common.Constants.DATE_FORMATTER_YYYY_M_D_E;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.chip.Chip;
import com.tomergoldst.tooltips.ToolTip;
import com.tomergoldst.tooltips.ToolTipsManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.adapter.RecipientChipListAdapter;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.model.data.RecipientData;
import kr.jeet.edu.manager.model.data.RecipientStudentData;
import kr.jeet.edu.manager.model.data.ScheduleData;
import kr.jeet.edu.manager.model.response.BaseResponse;
import kr.jeet.edu.manager.model.response.ScheduleDetailResponse;
import kr.jeet.edu.manager.server.RetrofitClient;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.PreferenceUtil;
import kr.jeet.edu.manager.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuScheduleDetailActivity extends BaseActivity implements ToolTipsManager.TipListener {

    private static final String TAG = "ScheduleDetailActivity";

    private TextView tvDate, tvCampus, tvTitle, tvTarget, tvContent;

    private ScheduleData _currentData = null;
    private TextView tvRecipient;
    private ImageView imgInfo;
    RecyclerView _recyclerViewRecipient;
    private LinearLayoutCompat layoutControlRecipient;
    RecipientChipListAdapter _recipientListAdapter;

    List<RecipientData> _recipientList = new ArrayList<>();
    int _userGubun = 1;
    int _seq = -1;
    Menu _menu;
    private int _currentSeq = -1;
    private boolean isEdited = false;

    private ToolTip.Builder builder;
    private ToolTipsManager toolTipsMgr;

    //작성화면 resultLauncher
    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() != RESULT_CANCELED) {
                Intent intent = result.getData();
                if(intent.hasExtra(IntentParams.PARAM_BOARD_EDITED)) {
                    isEdited = intent.getBooleanExtra(IntentParams.PARAM_BOARD_EDITED, false);
                    LogMgr.e( "_currentData seq = " + _currentData.seq);
                    if(isEdited) {
                        int boardSeq = _currentData.seq;
                        requestDetailSchedule(boardSeq);
                    }
                }
            }
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_schedule_detail);
        mContext = this;
        _userGubun = PreferenceUtil.getUserGubun(this);
        _seq = PreferenceUtil.getUserSeq(this);
        toolTipsMgr = new ToolTipsManager(this);
        initIntentData();
        initView();
        initAppbar();
        initData();
    }

    @Override
    public void onBackPressed() {
        LogMgr.d(TAG,"onBackPressed edit? " + isEdited);
        if(isEdited) {
            Intent intent = new Intent();
            intent.putExtra(IntentParams.PARAM_BOARD_EDITED, isEdited);
            setResult(RESULT_OK, intent);
            finish();
        }else {
            super.onBackPressed();
        }
        overridePendingTransition(R.anim.horizontal_in, R.anim.horizontal_exit);
    }

    private void initIntentData(){
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(IntentParams.PARAM_BOARD_SEQ)){
            _currentSeq = intent.getIntExtra(IntentParams.PARAM_BOARD_SEQ, -1);
        }
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(getString(R.string.title_detail));
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    void initView() {
        imgInfo = findViewById(R.id.img_info);
        tvDate = findViewById(R.id.tv_sc_detail_date);
        tvCampus = findViewById(R.id.tv_sc_detail_campus);
        tvTarget = findViewById(R.id.tv_sc_detail_target);
        tvTitle = findViewById(R.id.tv_sc_detail_title);
        tvContent = findViewById(R.id.tv_sc_detail_content);
        tvRecipient = findViewById(R.id.tv_recipient_count);
        layoutControlRecipient = findViewById(R.id.layout_recipient_control);
        layoutControlRecipient.setVisibility(View.VISIBLE);
        //region chip
        _recyclerViewRecipient = findViewById(R.id.recycler_chip);
        _recyclerViewRecipient.setVisibility(View.VISIBLE);
        _recipientListAdapter = new RecipientChipListAdapter(mContext, _recipientList, new RecipientChipListAdapter.onItemClickListener() {
            @Override
            public void onDeleteClick(int position, RecipientData item) {
                _recipientList.remove(position);
                _recipientListAdapter.notifyItemRemoved(position);
                tvRecipient.setText(getString(R.string.content_recipient_counts, _recipientList.size()));
            }
        });
        _recipientListAdapter = new RecipientChipListAdapter(mContext, _recipientList, new RecipientChipListAdapter.longClickListener() {
            @Override
            public void onLongClick(int position, RecipientData item, Chip chip, ConstraintLayout root) {
                navigate2DetailActivity(item);
            }
        });
        _recipientListAdapter.setEditMode(Constants.BoardEditMode.Show);
        FlexboxLayoutManager fblManager = new FlexboxLayoutManager(this);
        fblManager.setFlexWrap(FlexWrap.WRAP);
        fblManager.setFlexDirection(FlexDirection.ROW);
        fblManager.setJustifyContent(JustifyContent.FLEX_START);
        _recyclerViewRecipient.setLayoutManager(fblManager);
        _recyclerViewRecipient.setAdapter(_recipientListAdapter);
        //endregion
    }

    void initData() {
        if(_currentData != null) {
//            if(_currentData.writerSeq == _seq) {
                invalidateOptionsMenu();
//            }
            try {
                Calendar calendar = Calendar.getInstance();
                calendar.set(_currentData.year, _currentData.month - 1, _currentData.day);

                // 원하는 날짜 형식으로 포맷합니다.
                SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMATTER_YYYY_M_D_E, Locale.getDefault());
                Date date = calendar.getTime();

                tvDate.setText(sdf.format(date));

            } catch (Exception e) {
                e.printStackTrace();
            }

            String str = TextUtils.isEmpty(_currentData.acaName) ? "" : _currentData.acaName;
            tvCampus.setText(str);

            str = TextUtils.isEmpty(_currentData.title) ? "" : _currentData.title;
            tvTitle.setText(str);

            str = TextUtils.isEmpty(_currentData.acaGubunName) ? "" : "[" + _currentData.acaGubunName + "]";
            viewVisibility(tvTarget, str);

            str = TextUtils.isEmpty(_currentData.content) ? "" : _currentData.content;
            viewVisibility(tvContent, str);
            initChipGroup();
        }else{
            requestDetailSchedule(_currentSeq);
        }

    }

    private void initChipGroup() {
        if(_currentData != null) {
            _recipientList.clear();
            if(_currentData.receiverList != null && !_currentData.receiverList.isEmpty()) {
                _recipientList.addAll(_currentData.receiverList);
            }
        }
        if(_recipientList.size() > 0) {
            _recyclerViewRecipient.setVisibility(View.VISIBLE);
            imgInfo.setVisibility(View.VISIBLE);
            Collections.sort(_recipientList);
            _recipientListAdapter.notifyDataSetChanged();
        }else{
            _recyclerViewRecipient.setVisibility(View.GONE);
            imgInfo.setVisibility(View.GONE);
        }
        tvRecipient.setText(getString(R.string.content_recipient_counts, _recipientList.size()));

        imgInfo.setOnClickListener(v -> {

            builder = new ToolTip.Builder(mContext, imgInfo, findViewById(R.id.test_root), "수신인을 길게 클릭하면 원생정보를 자세히 알 수 있어요", ToolTip.POSITION_LEFT_TO);

            builder.setAlign(ToolTip.ALIGN_RIGHT);
            builder.setBackgroundColor(getColor(R.color.darkgray));

            toolTipsMgr.show(builder.build());

//            if (builder == null) {
//                builder = new ToolTip.Builder(mContext, imgInfo, findViewById(R.id.test_root), "수신인을 길게 클릭하면 원생정보를 자세히 알 수 있어요", ToolTip.POSITION_LEFT_TO);
//
//                builder.setAlign(ToolTip.ALIGN_RIGHT);
//                builder.setBackgroundColor(R.color.blackgray2);
//
//                toolTipsMgr.show(builder.build());
//            }

//            if (builder != null) {
//                builder = null;
//                new Handler().postDelayed(() -> {
//                    toolTipsMgr.dismissAll();
//                }, 3000);
//            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(_currentData != null) {
            if ((_userGubun == Constants.USER_TYPE_ADMIN && _currentData.writerSeq == _seq)
                    || _userGubun == Constants.USER_TYPE_SUPER_ADMIN) {
                getMenuInflater().inflate(R.menu.menu_edit, menu);
                this._menu = menu;
            }
        }
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_edit:
                navigate2EditScheduleActivity();
                return true;
            case R.id.action_delete:
                showMessageDialog(getString(R.string.dialog_title_alarm)
                        , getString(R.string.board_item_confirm_delete)
                        , new View.OnClickListener() {  //OKClickListener
                            @Override
                            public void onClick(View view) {
                                if (_currentData != null) {
                                    requestBoardDelete(_currentData.seq);
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

                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    private void viewVisibility(TextView tv, String str){
        if (!TextUtils.isEmpty(str)) tv.setText(str);
        else tv.setVisibility(View.GONE);
    }
    private void requestBoardDelete(int boardSeq) {
        if(RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().deleteSchedule(_currentData.seq).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    try{
                        if(response.isSuccessful()) {
                            Toast.makeText(mContext, R.string.board_item_deleted, Toast.LENGTH_SHORT).show();
                            Intent deleteIntent = new Intent();
                            deleteIntent.putExtra(IntentParams.PARAM_BOARD_DELETED, true);
                            setResult(RESULT_OK, deleteIntent);
                            finish();
                        }else{
                            Toast.makeText(mContext, R.string.board_item_deleted_fail, Toast.LENGTH_SHORT).show();
                        }
                    }catch(Exception ex) {

                    }
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    Toast.makeText(mContext, R.string.board_item_deleted_fail, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void requestDetailSchedule(int seq) {
        if (RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getScheduleDetail(seq).enqueue(new Callback<ScheduleDetailResponse>() {
                @Override
                public void onResponse(Call<ScheduleDetailResponse> call, Response<ScheduleDetailResponse> response) {
                    try {
                        if (response.isSuccessful()) {
                            if (response.body().data != null) {
                                _currentData = response.body().data;
                                initData();
                            }

                        } else {
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestScheduleDetail() Exception: ", e.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<ScheduleDetailResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "requestScheduleDetail() onFailure >> " + t.getMessage());
                    } catch (Exception e) {
                    }
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void navigate2EditScheduleActivity() {
        Intent editIntent = new Intent(mContext, EditScheduleActivity.class);
        editIntent.putExtra(IntentParams.PARAM_SCHEDULE_INFO, _currentData);
        resultLauncher.launch(editIntent);

    }

    @Override
    public void onTipDismissed(View view, int anchorViewId, boolean byUser) {}

    private void navigate2DetailActivity(RecipientData data) {
        Intent intent = new Intent(mContext, DetailStudentInfoActivity.class);
        //원생정보 seq, stCode
        intent.putExtra(IntentParams.PARAM_STU_SEQ, data.seq);
        intent.putExtra(IntentParams.PARAM_STU_STCODE, data.stCode);
        startActivity(intent);
        overridePendingTransition(R.anim.horizontal_enter, R.anim.horizontal_out);
    }
}