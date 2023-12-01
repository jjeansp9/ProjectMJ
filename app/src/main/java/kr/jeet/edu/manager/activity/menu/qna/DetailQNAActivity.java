package kr.jeet.edu.manager.activity.menu.qna;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DownloadManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.activity.BaseActivity;
import kr.jeet.edu.manager.activity.PhotoViewActivity;
import kr.jeet.edu.manager.activity.menu.announcement.EditAnnouncementActivity;
import kr.jeet.edu.manager.activity.menu.announcement.MenuAnnouncementDetailActivity;
import kr.jeet.edu.manager.adapter.BoardDetailFileListAdapter;
import kr.jeet.edu.manager.adapter.BoardDetailImageListAdapter;
import kr.jeet.edu.manager.adapter.RecipientChipListAdapter;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.db.PushMessage;
import kr.jeet.edu.manager.model.data.AnnouncementData;
import kr.jeet.edu.manager.model.data.FileData;
import kr.jeet.edu.manager.model.data.RecipientData;
import kr.jeet.edu.manager.model.response.BaseResponse;
import kr.jeet.edu.manager.model.response.BoardDetailResponse;
import kr.jeet.edu.manager.receiver.DownloadReceiver;
import kr.jeet.edu.manager.server.RetrofitApi;
import kr.jeet.edu.manager.server.RetrofitClient;
import kr.jeet.edu.manager.utils.FileUtils;
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

    AnnouncementData _currentData = null;
    int _currentDataPosition = 0;
    private TextView tvRecipient;
    RecyclerView _recyclerViewRecipient;
    private LinearLayoutCompat layoutControlRecipient;
    RecipientChipListAdapter _recipientListAdapter;

    List<RecipientData> _recipientList = new ArrayList<>();

    Menu _menu;
    int _seq = -1;
    int _userGubun = 1;
    private boolean isEdited = false;
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

        if(intent.hasExtra(IntentParams.PARAM_ANNOUNCEMENT_INFO)) {
            LogMgr.w("param is recived");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                _currentData = intent.getParcelableExtra(IntentParams.PARAM_ANNOUNCEMENT_INFO, AnnouncementData.class);
            }else{
                _currentData = intent.getParcelableExtra(IntentParams.PARAM_ANNOUNCEMENT_INFO);
            }

        }else if(intent.hasExtra(IntentParams.PARAM_PUSH_MESSAGE)) {
            PushMessage message = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                message = intent.getParcelableExtra(IntentParams.PARAM_PUSH_MESSAGE, PushMessage.class);
            }else{
                message = intent.getParcelableExtra(IntentParams.PARAM_PUSH_MESSAGE);
            }
            _currentSeq = message.connSeq;
        }

        LogMgr.w("currentData = " + _currentData);
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

        mTvTitle = findViewById(R.id.tv_qna_title);
        mTvName = findViewById(R.id.tv_writer_name);
        mTvDate = findViewById(R.id.tv_board_detail_write_date);
        tvReadCount = findViewById(R.id.tv_rd_cnt);
        mTvQuestion = findViewById(R.id.tv_question);
        mTvAnswer = findViewById(R.id.tv_answer);

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
        _recipientListAdapter.setEditMode(Constants.BoardEditMode.Edit);
        FlexboxLayoutManager fblManager = new FlexboxLayoutManager(this);
        fblManager.setFlexWrap(FlexWrap.WRAP);
        fblManager.setFlexDirection(FlexDirection.ROW);
        fblManager.setJustifyContent(JustifyContent.FLEX_START);
        _recyclerViewRecipient.setLayoutManager(fblManager);
        _recyclerViewRecipient.setAdapter(_recipientListAdapter);

//        initData();
        if (_currentData != null) {
            requestBoardDetail(_currentData.seq);   //조회수 때문에 detail 을 호출해야만 함...
        }else if(_currentSeq != -1) {
            requestBoardDetail(_currentSeq);
        }
    }

    private void initData() {
        if (_currentData != null) {
            //작성자만 삭제 및 수정 가능
            if(_currentData.memberResponseVO.seq == _seq) {
                invalidateOptionsMenu();
            }
            mTvTitle.setText(_currentData.title); // 제목
            LogMgr.w(TAG,"memberResponseVO = " + _currentData.memberResponseVO);
            mTvName.setText(_currentData.memberResponseVO.name); // 작성자 이름
            mTvDate.setText(_currentData.insertDate); // 작성날짜
            mTvQuestion.setText(_currentData.content); // 내용
            tvReadCount.setText(Utils.decimalFormat(_currentData.rdcnt));    //조회수

            initChipGroup();
        }else if(_currentSeq != -1) {
            requestBoardDetail(_currentSeq);
        }
    }
    private void initChipGroup() {
        if(_currentData != null) {
            _recipientList.clear();
            if(_currentData.receiverList != null && !_currentData.receiverList.isEmpty()) {
                _recipientList.addAll(_currentData.receiverList);
            }
        }
        LogMgr.w(TAG, "added recipient size = " + _recipientList);
        if(_recipientList.size() > 0) {
            _recyclerViewRecipient.setVisibility(View.VISIBLE);
            Collections.sort(_recipientList);
            _recipientListAdapter.notifyDataSetChanged();
        }else{
            _recyclerViewRecipient.setVisibility(View.GONE);
        }
        tvRecipient.setText(getString(R.string.content_recipient_counts, _recipientList.size()));
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(_currentData != null) {
            if ((_userGubun == Constants.USER_TYPE_ADMIN && _currentData.memberResponseVO.seq == _seq)
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
                navigate2EditQNAActivity();
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
    private void requestBoardDelete(int boardSeq) {
        if(RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.deleteAnnouncement(_currentData.seq).enqueue(new Callback<BaseResponse>() {
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

    // 글 상세정보 조회
    private void requestBoardDetail(int boardSeq) {

        if (RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.getBoardDetail(boardSeq).enqueue(new Callback<BoardDetailResponse>() {
                @Override
                public void onResponse(Call<BoardDetailResponse> call, Response<BoardDetailResponse> response) {
                    try {
                        if (response.isSuccessful()) {

                            if (response.body() != null) {

                                AnnouncementData getData = response.body().data;
                                if (getData != null) {
                                    _currentData = getData;
                                    initData();

                                } else LogMgr.e(TAG + " DetailData is null");
                            }
                        } else {
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestBoardDetail() Exception : ", e.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<BoardDetailResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "requestBoardDetail() onFailure >> " + t.getMessage());
                    } catch (Exception e) {
                    }
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void navigate2EditQNAActivity() {
        Intent editIntent = new Intent(mContext, EditQNAActivity.class);
        editIntent.putExtra(IntentParams.PARAM_ANNOUNCEMENT_INFO, _currentData);
        editIntent.putExtra(IntentParams.PARAM_BOARD_POSITION, _currentDataPosition);
        resultLauncher.launch(editIntent);

    }

    @Override
    public void onBackPressed() {
        LogMgr.d(TAG,"onBackPressed edit? " + isEdited);
        if(isEdited) {
            Intent intent = new Intent();
            intent.putExtra(IntentParams.PARAM_BOARD_EDITED, isEdited);
            intent.putExtra(IntentParams.PARAM_BOARD_ITEM, _currentData);
            intent.putExtra(IntentParams.PARAM_BOARD_POSITION, _currentDataPosition);
            setResult(RESULT_OK, intent);
            finish();
        }else {
            Intent intent = new Intent();
            intent.putExtra(IntentParams.PARAM_BOARD_ITEM, _currentData);
            intent.putExtra(IntentParams.PARAM_BOARD_POSITION, _currentDataPosition);
            setResult(RESULT_CANCELED, intent);
            finish();
        }
        overridePendingTransition(R.anim.horizontal_in, R.anim.horizontal_exit);
    }
    private void viewVisibility(TextView tv, String str){
        if (!TextUtils.isEmpty(str)) tv.setText(str);
        else tv.setVisibility(View.GONE);
    }
    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

}