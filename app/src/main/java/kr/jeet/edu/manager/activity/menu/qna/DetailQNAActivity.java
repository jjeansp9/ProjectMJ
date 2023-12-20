package kr.jeet.edu.manager.activity.menu.qna;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import java.util.ArrayList;
import java.util.List;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.activity.BaseActivity;
import kr.jeet.edu.manager.activity.menu.announcement.MenuAnnouncementDetailActivity;
import kr.jeet.edu.manager.adapter.AnswererChipListAdapter;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.db.PushMessage;
import kr.jeet.edu.manager.model.data.AnswererData;
import kr.jeet.edu.manager.model.data.QnaDetailData;
import kr.jeet.edu.manager.model.request.QnaAddRequest;
import kr.jeet.edu.manager.model.request.QnaReplyRequest;
import kr.jeet.edu.manager.model.request.QnaUpdateRequest;
import kr.jeet.edu.manager.model.response.BaseResponse;
import kr.jeet.edu.manager.model.response.GetAnswererListResponse;
import kr.jeet.edu.manager.model.response.GetRecipientStudentResponse;
import kr.jeet.edu.manager.model.response.QnaDetailResponse;
import kr.jeet.edu.manager.model.response.StringResponse;
import kr.jeet.edu.manager.server.RetrofitApi;
import kr.jeet.edu.manager.server.RetrofitClient;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.PreferenceUtil;
import kr.jeet.edu.manager.utils.Utils;
import kr.jeet.edu.manager.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailQNAActivity extends BaseActivity {
    private String TAG = MenuAnnouncementDetailActivity.class.getSimpleName();

    private TextView mTvTitle, mTvName, mTvDate, mTvQuestion, mTvAnswer, tvReadCount;
    private RetrofitApi mRetrofitApi;
    private TextView tvAnswererCount;
    private TextView tvAnswererName;
    RecyclerView _recyclerViewAnswerer;
    private LinearLayoutCompat layoutStatus, layoutAnswer;
    private ConstraintLayout layoutControlManager;
    AnswererChipListAdapter _answererListAdapter;
    RelativeLayout layoutBottom;
    TextView tvBottomTitle;
    ImageView ivArrow;

    List<AnswererData> _answererList = new ArrayList<>();
    QnaDetailData _currentData = null;
    int _currentDataPosition = 0;
    Menu _menu;
    int _seq = -1;
    int _sfCode = -1;
    int _userGubun = 1;
    private boolean isAdded = false;
    private boolean isEdited = false;
    private boolean isRequireUpdate = false;
    private int _currentSeq = -1;

    //작성화면 resultLauncher
    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() != RESULT_CANCELED) {
                Intent intent = result.getData();
                if(intent != null) {

                    if (intent.hasExtra(IntentParams.PARAM_BOARD_EDITED)) {
                        isEdited = intent.getBooleanExtra(IntentParams.PARAM_BOARD_EDITED, false);
                        LogMgr.e("position = " + _currentDataPosition + "_currentData seq = " + _currentData.seq);
//                    if(edited && _currentDataPosition > 0) {
//                        intent.putExtra(IntentParams.PARAM_BOARD_POSITION, _currentDataPosition);
//                        setResult(result.getResultCode(), intent);
//                    }
                        if (isEdited) {
                            int boardSeq = _currentData.seq;
                            requestBoardDetail(boardSeq);
                        }
                        if(intent.hasExtra(IntentParams.PARAM_IS_REQUIRE_UPDATE)) {
                            isRequireUpdate = intent.getBooleanExtra(IntentParams.PARAM_IS_REQUIRE_UPDATE, false);
                        }
                    }else if(intent.hasExtra(IntentParams.PARAM_BOARD_ADDED)) {
                        isEdited = intent.getBooleanExtra(IntentParams.PARAM_BOARD_ADDED, false);
                        LogMgr.e("position = " + _currentDataPosition + "_currentData seq = " + _currentData.seq);

                        if (isEdited) {
                            int boardSeq = _currentData.seq;
                            requestBoardDetail(boardSeq);
                        }
                    }
                }
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_qna);

        mContext = this;
        _userGubun = PreferenceUtil.getUserGubun(this);
        _sfCode = PreferenceUtil.getUserSFCode(this);
        _seq = PreferenceUtil.getUserSeq(this);
        initIntentData();
        initAppbar();
        initView();

    }
    private void initIntentData(){
        Intent intent = getIntent();
        if(intent == null) return;
        if(intent.hasExtra(IntentParams.PARAM_BOARD_POSITION)) {
            _currentDataPosition = intent.getIntExtra(IntentParams.PARAM_BOARD_POSITION, -1);
        }

        if(intent.hasExtra(IntentParams.PARAM_BOARD_SEQ)) {
            LogMgr.w("param is recived");
            _currentSeq = intent.getIntExtra(IntentParams.PARAM_BOARD_SEQ, -1);

        }else if(intent.hasExtra(IntentParams.PARAM_PUSH_MESSAGE)) {
            PushMessage message = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                message = intent.getParcelableExtra(IntentParams.PARAM_PUSH_MESSAGE, PushMessage.class);
            }else{
                message = intent.getParcelableExtra(IntentParams.PARAM_PUSH_MESSAGE);
            }
            _currentSeq = message.connSeq;
        }
    }
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.title_detail);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    void initView() {
        layoutAnswer = findViewById(R.id.layout_answer);
        layoutStatus = findViewById(R.id.layout_tag);
        mTvTitle = findViewById(R.id.tv_qna_title);
        mTvName = findViewById(R.id.tv_writer_name);
        mTvDate = findViewById(R.id.tv_board_detail_write_date);
        tvReadCount = findViewById(R.id.tv_rd_cnt);
        mTvQuestion = findViewById(R.id.tv_question);
        mTvAnswer = findViewById(R.id.tv_answer);
        tvAnswererName = findViewById(R.id.tv_answerer_name);
        tvAnswererCount = findViewById(R.id.tv_manager_count);
        layoutControlManager = findViewById(R.id.layout_manager_control);
//        layoutControlRecipient.setVisibility(View.VISIBLE);
        //region chip
        _recyclerViewAnswerer = findViewById(R.id.recycler_chip);
//        _recyclerViewRecipient.setVisibility(View.VISIBLE);
        _answererListAdapter = new AnswererChipListAdapter(mContext, _answererList);
        FlexboxLayoutManager fblManager = new FlexboxLayoutManager(this);
        fblManager.setFlexWrap(FlexWrap.WRAP);
        fblManager.setFlexDirection(FlexDirection.ROW);
        fblManager.setJustifyContent(JustifyContent.FLEX_START);
        _recyclerViewAnswerer.setLayoutManager(fblManager);
        _recyclerViewAnswerer.setAdapter(_answererListAdapter);

        layoutBottom = findViewById(R.id.layout_bottom);
        layoutBottom.setOnClickListener(this);
        tvBottomTitle = findViewById(R.id.title_bottom_item);
        ivArrow = findViewById(R.id.iv_arrow);
        if(_currentSeq != -1) {
            requestBoardDetail(_currentSeq);
        }
    }

    private void initData() {
        if (_currentData != null) {
            //작성자만 삭제 및 수정 가능
//            if(_currentData.writerSeq == _seq) {
                invalidateOptionsMenu();
//            }
            mTvTitle.setText(_currentData.title); // 제목
            if(_currentData.userGubun >= Constants.USER_TYPE_STUDENT) {
                mTvName.setVisibility(View.VISIBLE);
                mTvName.setText(_currentData.writerNm); // 작성자 이름
            }else{
                mTvName.setVisibility(View.GONE);
            }
            mTvDate.setText(_currentData.insertDate); // 작성날짜
            mTvQuestion.setText(_currentData.content); // 내용
            if(_currentData.state.equals(Constants.QNAState.SUBSCRIPTION.getCodeStr())){    //신청
                layoutAnswer.setVisibility(View.GONE);
                layoutControlManager.setVisibility(View.GONE);
                _recyclerViewAnswerer.setVisibility(View.GONE);
            }else if(_currentData.state.equals(Constants.QNAState.RECEPTION.getCodeStr())){ //접수
                layoutAnswer.setVisibility(View.GONE);
                if(_currentData.userGubun <= Constants.USER_TYPE_TEACHER) { //강사/관리자가 작성한 글
                    layoutControlManager.setVisibility(View.GONE);
                    _recyclerViewAnswerer.setVisibility(View.GONE);
                }else { //학부모가 작성한 글
                    if(_userGubun == Constants.USER_TYPE_TEACHER) { //강사 로그인
                        layoutControlManager.setVisibility(View.GONE);
                        _recyclerViewAnswerer.setVisibility(View.GONE);

                    }else { //관리자 이상 로그인
                        layoutControlManager.setVisibility(View.VISIBLE);
                        _recyclerViewAnswerer.setVisibility(View.VISIBLE);
                    }
                    requestSpecifiedAnswererList(_currentSeq);
                }
                requestSpecifiedAnswererList(_currentSeq);
            }else if(_currentData.state.equals(Constants.QNAState.COMPLETE.getCodeStr())) { //완료
                layoutAnswer.setVisibility(View.VISIBLE);
                mTvAnswer.setText(_currentData.reply);
                if(_currentData.userGubun <= Constants.USER_TYPE_TEACHER) { //강사/관리자가 작성한 글
                    layoutControlManager.setVisibility(View.GONE);
                    _recyclerViewAnswerer.setVisibility(View.GONE);
                }else { //학부모가 작성한 글
                    if(_userGubun == Constants.USER_TYPE_TEACHER) { //강사 로그인
                        layoutControlManager.setVisibility(View.GONE);
                        _recyclerViewAnswerer.setVisibility(View.GONE);

                    }else { //관리자 이상 로그인
                        layoutControlManager.setVisibility(View.VISIBLE);
                        _recyclerViewAnswerer.setVisibility(View.VISIBLE);
                    }
                    if(!TextUtils.isEmpty(_currentData.replyMberNm)) {
                        tvAnswererName.setText(_currentData.replyMberNm);
                    }
                    requestSpecifiedAnswererList(_currentSeq);
                }

            }
            tvReadCount.setText(Utils.decimalFormat(_currentData.rdcnt));    //조회수
            //region TAG
            layoutStatus.removeAllViews();

            if (!TextUtils.isEmpty(_currentData.isMain)) {
                if (_currentData.isMain.equals("Y")) { // 공지 글
                    addTag(R.color.color_notice, "공지");

                    if (_currentData.userGubun <= Constants.USER_TYPE_TEACHER) { // 강사, 관리자의 경우
                        if (!_currentData.isOpen.equals("Y")) {
                            addTag(R.color.color_private, "비공개");
                        }
                        if (_currentData.writerSeq == PreferenceUtil.getUserSeq(mContext)) {
                            addTag(R.color.color_me, "본인");
                        }
                    }
                } else {
                    if (_currentData.userGubun >= Constants.USER_TYPE_STUDENT) {
                        if (!TextUtils.isEmpty(_currentData.state)) {
                            if (_currentData.state.equals(Constants.QNAState.SUBSCRIPTION.getCodeStr())) {
                                addTag(R.color.color_subscription, "신청");

                            } else if(_currentData.state.equals(Constants.QNAState.RECEPTION.getCodeStr())) {
                                addTag(R.color.color_receiption, "접수");

                            } else if(_currentData.state.equals(Constants.QNAState.COMPLETE.getCodeStr())) {
                                addTag(R.color.color_complete, "완료");

                            }
                        }
                    }
                    if (!TextUtils.isEmpty(_currentData.isOpen)) {
                        if (!_currentData.isOpen.equals("Y")) {
                            addTag(R.color.color_private, "비공개");
                        }
                    }
                    if (_currentData.writerSeq == PreferenceUtil.getUserSeq(mContext)) {
                        addTag(R.color.color_me, "본인");
                    }
                }
            }

            //endregion TAG
            //region Bottom
            //중간관리자 이고 신청단계에서 담당자 지정
            if("Y".equals(_currentData.isSubAdmin) && _currentData.state.equals(Constants.QNAState.SUBSCRIPTION.getCodeStr())) {
//                ViewCompat.setBackgroundTintList(layoutBottom, ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.blackgray)));
                layoutBottom.setVisibility(View.VISIBLE);
                tvBottomTitle.setText(R.string.title_select_manager);
            }else if("Y".equals(_currentData.isReplyAdmin) && _currentData.state.equals(Constants.QNAState.RECEPTION.getCodeStr())) {
//                ViewCompat.setBackgroundTintList(layoutBottom, ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.red)));
                layoutBottom.setVisibility(View.VISIBLE);
                tvBottomTitle.setText(R.string.title_do_answer);
            }else{
                layoutBottom.setVisibility(View.GONE);
            }
            //endregion
//            initChipGroup();
        }else if(_currentSeq != -1) {
            requestBoardDetail(_currentSeq);
        }
    }
    private void initAnswererList() {
        if(_userGubun < Constants.USER_TYPE_TEACHER) {
            if (_answererList != null && !_answererList.isEmpty()) {
                _recyclerViewAnswerer.setVisibility(View.VISIBLE);
                if (_currentData.state.equals(Constants.QNAState.COMPLETE.getCodeStr())) {
                    _answererList.stream().filter(t -> t.sfCode == _currentData.sfCode).forEach(item -> item.isWrited = true);
                }
                tvAnswererCount.setText(getString(R.string.content_recipient_counts, _answererListAdapter.getItemCount()));
                _answererListAdapter.notifyDataSetChanged();
            } else {
                _recyclerViewAnswerer.setVisibility(View.GONE);
            }
        }

    }
    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.layout_bottom:
                if("Y".equals(_currentData.isSubAdmin) && _currentData.state.equals(Constants.QNAState.SUBSCRIPTION.getCodeStr())) {    //담당자 지정
                    navigate2SelectAnswererActivity();
                }else if("Y".equals(_currentData.isReplyAdmin) && _currentData.state.equals(Constants.QNAState.RECEPTION.getCodeStr())) {   //답변하기
                    navigate2EditQNAActivity(true);
                }
                break;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(_currentData != null) {
            if ((_userGubun == Constants.USER_TYPE_ADMIN )
                    || _userGubun == Constants.USER_TYPE_SUPER_ADMIN) {
                getMenuInflater().inflate(R.menu.menu_qna_detail, menu);
                this._menu = menu;
            }
        }
        return (super.onCreateOptionsMenu(menu));
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(_currentData == null) return false;
        if(_userGubun == Constants.USER_TYPE_ADMIN) {
            if(_currentData.userGubun >= Constants.USER_TYPE_STUDENT) {
                menu.findItem(R.id.action_edit).setVisible(false);
                menu.findItem(R.id.action_delete).setVisible(false);
                menu.findItem(R.id.action_set_main).setVisible(false);
                menu.findItem(R.id.action_unset_main).setVisible(false);
                if (_currentData.isOpen.equals("Y")) {
                    menu.findItem(R.id.action_public).setVisible(false);
                    menu.findItem(R.id.action_private).setVisible(true);
                } else {
                    menu.findItem(R.id.action_public).setVisible(true);
                    menu.findItem(R.id.action_private).setVisible(false);
                }
            }else {
                if (_currentData.writerSeq == _seq) {
                    menu.findItem(R.id.action_edit).setVisible(true);
                    menu.findItem(R.id.action_delete).setVisible(true);
                    if (_currentData.isMain.equals("Y")) {
                        menu.findItem(R.id.action_set_main).setVisible(false);
                        menu.findItem(R.id.action_unset_main).setVisible(true);
                    } else {
                        menu.findItem(R.id.action_set_main).setVisible(true);
                        menu.findItem(R.id.action_unset_main).setVisible(false);
                    }
                    if (_currentData.isOpen.equals("Y")) {
                        menu.findItem(R.id.action_public).setVisible(false);
                        menu.findItem(R.id.action_private).setVisible(true);
                    } else {
                        menu.findItem(R.id.action_public).setVisible(true);
                        menu.findItem(R.id.action_private).setVisible(false);
                    }
                } else {
                    menu.findItem(R.id.action_edit).setVisible(false);
                    menu.findItem(R.id.action_delete).setVisible(false);
                    menu.findItem(R.id.action_public).setVisible(false);
                    menu.findItem(R.id.action_private).setVisible(false);
                    menu.findItem(R.id.action_set_main).setVisible(false);
                    menu.findItem(R.id.action_unset_main).setVisible(false);
                }
            }
        }else if(_userGubun == Constants.USER_TYPE_SUPER_ADMIN){
            if(_currentData.userGubun >= Constants.USER_TYPE_STUDENT) {
                menu.findItem(R.id.action_edit).setVisible(false);
                menu.findItem(R.id.action_delete).setVisible(true);
                menu.findItem(R.id.action_set_main).setVisible(false);
                menu.findItem(R.id.action_unset_main).setVisible(false);
                if (_currentData.isOpen.equals("Y")) {
                    menu.findItem(R.id.action_public).setVisible(false);
                    menu.findItem(R.id.action_private).setVisible(true);
                } else {
                    menu.findItem(R.id.action_public).setVisible(true);
                    menu.findItem(R.id.action_private).setVisible(false);
                }
            }else {
                if (_currentData.writerSeq == _seq) {
                    menu.findItem(R.id.action_edit).setVisible(true);
                    menu.findItem(R.id.action_delete).setVisible(true);
                    if (_currentData.isMain.equals("Y")) {
                        menu.findItem(R.id.action_set_main).setVisible(false);
                        menu.findItem(R.id.action_unset_main).setVisible(true);
                    } else {
                        menu.findItem(R.id.action_set_main).setVisible(true);
                        menu.findItem(R.id.action_unset_main).setVisible(false);
                    }
                    if (_currentData.isOpen.equals("Y")) {
                        menu.findItem(R.id.action_public).setVisible(false);
                        menu.findItem(R.id.action_private).setVisible(true);
                    } else {
                        menu.findItem(R.id.action_public).setVisible(true);
                        menu.findItem(R.id.action_private).setVisible(false);
                    }
                } else {
                    menu.findItem(R.id.action_edit).setVisible(false);
                    menu.findItem(R.id.action_delete).setVisible(true);
                    if (_currentData.isMain.equals("Y")) {
                        menu.findItem(R.id.action_set_main).setVisible(false);
                        menu.findItem(R.id.action_unset_main).setVisible(true);
                    } else {
                        menu.findItem(R.id.action_set_main).setVisible(true);
                        menu.findItem(R.id.action_unset_main).setVisible(false);
                    }
                    if (_currentData.isOpen.equals("Y")) {
                        menu.findItem(R.id.action_public).setVisible(false);
                        menu.findItem(R.id.action_private).setVisible(true);
                    } else {
                        menu.findItem(R.id.action_public).setVisible(true);
                        menu.findItem(R.id.action_private).setVisible(false);
                    }

                }
            }

        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_edit:
                navigate2EditQNAActivity(false);
                return true;
            case R.id.action_delete:
                showMessageDialog(getString(R.string.dialog_title_alarm)
                        , getString(R.string.board_item_confirm_delete)
                        , new View.OnClickListener() {  //OKClickListener
                            @Override
                            public void onClick(View view) {
                                if (_currentData != null) {
                                    requestQnaDelete(_currentData.seq);
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
            case R.id.action_public:
                requestUpdateQnaProperty(true, null);
                return true;
            case R.id.action_private:
                requestUpdateQnaProperty(false, null);
                return true;
            case R.id.action_set_main:
                requestUpdateQnaProperty(null, true);
                return true;
            case R.id.action_unset_main:
                requestUpdateQnaProperty(null, false);
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    //글 삭제
    private void requestQnaDelete(int boardSeq) {
        if(RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.deleteQna(boardSeq).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    try{
                        if(response.isSuccessful()) {
                            Toast.makeText(mContext, R.string.board_item_deleted, Toast.LENGTH_SHORT).show();
                            Intent deleteIntent = new Intent();
                            deleteIntent.putExtra(IntentParams.PARAM_BOARD_DELETED, true);
                            deleteIntent.putExtra(IntentParams.PARAM_BOARD_POSITION, _currentDataPosition);
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
    private void requestUpdateQnaProperty(Boolean isOpen, Boolean isMain) {
        Call<BaseResponse> call = null;
        String isOpenStr = null;
        String isMainStr = null;
        if(isOpen != null){
            isOpenStr = (isOpen)? "Y" : "N";
        }
        if(isMain != null) {
            isMainStr = (isMain)? "Y" : "N";
        }

        call = RetrofitClient.getApiInterface().updateQnaProperty(
                _currentSeq,
                _userGubun,
                isOpenStr,
                isMainStr
        );
        call.enqueue(new Callback<BaseResponse>(){

            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                hideProgressDialog();
                if(response.isSuccessful()) {
//                    String message = "";
//                    if(isOpen != null){
//                        message = (isOpen)? getString(R.string.qna_public_success) : getString(R.string.qna_private_success);
//                    }
//                    if(isMain != null) {
//                        message = (isMain)? getString(R.string.qna_set_main_success) : getString(R.string.qna_unset_main_success);
//                    }
//                    if(!TextUtils.isEmpty(message)) {
//                        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
//                    }
                    if(isMain != null) {
                        isRequireUpdate = true;
                    }
                    requestBoardDetail(_currentSeq);
                }else{
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                hideProgressDialog();
                Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
            }
        });

    }
    // 글 상세정보 조회
    private void requestBoardDetail(int boardSeq) {

        if (RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.getQnaDetail(boardSeq, _userGubun, _sfCode).enqueue(new Callback<QnaDetailResponse>() {
                @Override
                public void onResponse(Call<QnaDetailResponse> call, Response<QnaDetailResponse> response) {
                    try {
                        if (response.isSuccessful()) {

                            if (response.body() != null) {

                                QnaDetailData getData = response.body().data;
                                if (getData != null) {
                                    _currentData = getData;
                                    initData();

                                } else LogMgr.e(TAG + " DetailData is null");
                            }
                        } else {
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestQnaDetail() Exception : ", e.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<QnaDetailResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "requestQnaDetail() onFailure >> " + t.getMessage());
                    } catch (Exception e) {
                    }
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    // 지정된 담당자 조회
    private void requestSpecifiedAnswererList(int boardSeq) {

        if (RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.getSpecifiedAnswererList(boardSeq).enqueue(new Callback<GetAnswererListResponse>() {
                @Override
                public void onResponse(Call<GetAnswererListResponse> call, Response<GetAnswererListResponse> response) {
                    try {
                        if (response.isSuccessful()) {
                            if(_answererList != null) _answererList.clear();
                            if (response.body() != null) {
                                List<AnswererData> getData = response.body().data;
                                if (getData != null) {
                                   _answererList.addAll(getData);
                                    initAnswererList();

                                } else LogMgr.e(TAG + " DetailData is null");
                            }
                        } else {
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestQnaDetail() Exception : ", e.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<GetAnswererListResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "requestQnaDetail() onFailure >> " + t.getMessage());
                    } catch (Exception e) {
                    }
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void navigate2EditQNAActivity(boolean isReply) {
        Intent editIntent = new Intent(mContext, EditQNAActivity.class);
        editIntent.putExtra(IntentParams.PARAM_BOARD_ITEM, _currentData);
        editIntent.putExtra(IntentParams.PARAM_BOARD_POSITION, _currentDataPosition);
        if(isReply)
            editIntent.putExtra(IntentParams.PARAM_IS_REPLY, true);
        resultLauncher.launch(editIntent);

    }
    private void navigate2SelectAnswererActivity() {
        Intent selectIntent = new Intent(mContext, SelectAnswererActivity.class);
        selectIntent.putExtra(IntentParams.PARAM_BOARD_SEQ, _currentData.seq);
        resultLauncher.launch(selectIntent);

    }

    @Override
    public void onBackPressed() {
        LogMgr.d(TAG,"onBackPressed edit? " + isEdited);
        if(isEdited) {
            Intent intent = new Intent();
            intent.putExtra(IntentParams.PARAM_BOARD_EDITED, isEdited);
            intent.putExtra(IntentParams.PARAM_BOARD_ITEM, _currentData);
            intent.putExtra(IntentParams.PARAM_BOARD_POSITION, _currentDataPosition);
            intent.putExtra(IntentParams.PARAM_IS_REQUIRE_UPDATE, isRequireUpdate);
            setResult(RESULT_OK, intent);
            finish();
        }else {
            Intent intent = new Intent();
            intent.putExtra(IntentParams.PARAM_BOARD_ITEM, _currentData);
            intent.putExtra(IntentParams.PARAM_BOARD_POSITION, _currentDataPosition);
            intent.putExtra(IntentParams.PARAM_IS_REQUIRE_UPDATE, isRequireUpdate);
            setResult(RESULT_CANCELED, intent);
            finish();
        }
        overridePendingTransition(R.anim.horizontal_in, R.anim.horizontal_exit);
    }
    private void viewVisibility(TextView tv, String str){
        if (!TextUtils.isEmpty(str)) tv.setText(str);
        else tv.setVisibility(View.GONE);
    }
    private void addTag(int bgColor, String state) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        int marginEnd = Utils.fromDpToPx(4);
        int paddingTop = Utils.fromDpToPx(1);
        int paddingBottom = Utils.fromDpToPx(2);
        int paddingHorizontal = Utils.fromDpToPx(12);

        TextView tvStatus = new TextView(mContext);
        tvStatus.setText(state);
        //tvStatus.setHint("비공개");
        tvStatus.setBackground(ContextCompat.getDrawable(mContext, R.drawable.bg_badge_default));
        ViewCompat.setBackgroundTintList(tvStatus, ColorStateList.valueOf(ContextCompat.getColor(mContext, bgColor)));
        tvStatus.setTextAppearance(R.style.QnaDetailTagTextAppearance);
        //tvStatus.setTextSize(R.dimen.font_size);
        tvStatus.setGravity(Gravity.CENTER);

        tvStatus.setPadding(paddingHorizontal, paddingTop, paddingHorizontal, paddingBottom);

        layoutParams.rightMargin = marginEnd;
        tvStatus.setLayoutParams(layoutParams);

        layoutStatus.addView(tvStatus);

    }
    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

}