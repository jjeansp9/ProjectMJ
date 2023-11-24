package kr.jeet.edu.manager.activity;

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
import android.util.Log;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.adapter.BoardDetailFileListAdapter;
import kr.jeet.edu.manager.adapter.BoardDetailImageListAdapter;
import kr.jeet.edu.manager.adapter.RecipientChipListAdapter;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.db.PushMessage;
import kr.jeet.edu.manager.model.data.AnnouncementData;
import kr.jeet.edu.manager.model.data.BriefingData;
import kr.jeet.edu.manager.model.data.FileData;
import kr.jeet.edu.manager.model.data.RecipientData;
import kr.jeet.edu.manager.model.response.BaseResponse;
import kr.jeet.edu.manager.model.response.BriefingDetailResponse;
import kr.jeet.edu.manager.model.response.BriefingRecipientListResponse;
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

public class MenuBriefingDetailActivity extends BaseActivity {
    private static final String TAG = "briefingDetail";

    private TextView mTvTitle, mTvDate, mTvTime, mTvLoc, mTvPersonnel, mTvContent, _tvReservedCnt, _tvReadCount;
    private RecyclerView mRecyclerViewImages, mRecyclerViewFiles;
    private BoardDetailImageListAdapter mImageAdapter;
    private BoardDetailFileListAdapter mFileAdapter;
    private RetrofitApi mRetrofitApi;

    private ArrayList<FileData> mImageList = new ArrayList<>();
    private ArrayList<FileData> mFileList = new ArrayList<>();
    private DownloadReceiver _downloadReceiver;
    BriefingData _currentData = null;
    //Recipient
    private TextView tvRecipient;
    RecyclerView _recyclerViewRecipient;
    private LinearLayoutCompat layoutControlRecipient;
    RecipientChipListAdapter _recipientListAdapter;

    List<RecipientData> _recipientList = new ArrayList<>();
    int _currentDataPosition = 0;
    Menu _menu;
    int _userGubun = 1;
    int _seq = 0;
    int _sfCode = 0;
    private boolean isEdited = false;
    private int _currentSeq = -1;
    //작성화면 resultLauncher
    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() != RESULT_CANCELED) {
                Intent intent = result.getData();
                if(intent == null) return;
                if(intent.hasExtra(IntentParams.PARAM_BOARD_EDITED)) {
                    isEdited = intent.getBooleanExtra(IntentParams.PARAM_BOARD_EDITED, false);

//                    if(edited && _currentDataPosition > 0) {
//                        intent.putExtra(IntentParams.PARAM_BOARD_POSITION, _currentDataPosition);
//                        setResult(result.getResultCode(), intent);
//                    }
                    if(isEdited) {
                        int boardSeq = _currentData.seq;
                        requestBriefingDetail(boardSeq);
                    }
                }
            }
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_briefing_detail);
        _downloadReceiver = new DownloadReceiver(new DownloadReceiver.DownloadListener() {
            @Override
            public void onDownloadComplete(int position, FileData fileData, Uri downloadFileUri) {
                hideProgressDialog();
                try {
                    mContext.unregisterReceiver(_downloadReceiver);
                }catch(IllegalArgumentException e){}
//                File destFile = new File(downloadPath);
//                Uri uri = Uri.fromFile(destFile);
                if(downloadFileUri == null) return;
                FileData tempFileData = FileUtils.copyBoardTempFile(mContext, downloadFileUri, fileData);
                LogMgr.e(TAG, "tempFileData = " + tempFileData.tempFileName);
                mFileAdapter.notifyItemChanged(position);
            }

            @Override
            public void onShow(FileData data) {
                showFile(data);
            }
        });
        mContext = this;
        _userGubun = PreferenceUtil.getUserGubun(this);
        _seq = PreferenceUtil.getUserSeq(this);
        _sfCode = PreferenceUtil.getUserSFCode(this);
        initIntentData();
        initView();
        initAppbar();
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(_currentData != null) {
            if ((_userGubun == Constants.USER_TYPE_ADMIN && _currentData.writerSeq == _seq)
                || _userGubun == Constants.USER_TYPE_SUPER_ADMIN){
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
                navigate2EditBriefingActivity();
                return true;
            case R.id.action_delete:
                showMessageDialog(getString(R.string.dialog_title_alarm)
                        , getString(R.string.board_item_confirm_delete)
                        , new View.OnClickListener() {  //OKClickListener
                            @Override
                            public void onClick(View view) {
                                if (_currentData != null) {
                                    requestBriefingDelete(_currentData.seq);
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
    private void initIntentData(){
        Intent intent = getIntent();
        if(intent != null) {

            if (intent.hasExtra(IntentParams.PARAM_BRIEFING_INFO)){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    _currentData = intent.getParcelableExtra(IntentParams.PARAM_BRIEFING_INFO, BriefingData.class);
                }else{
                    _currentData = intent.getParcelableExtra(IntentParams.PARAM_BRIEFING_INFO);
                }

                _currentSeq = _currentData.seq;
            }else if(intent.hasExtra(IntentParams.PARAM_PUSH_MESSAGE)) {
                PushMessage message = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    message = intent.getParcelableExtra(IntentParams.PARAM_PUSH_MESSAGE, PushMessage.class);
                }else{
                    message = intent.getParcelableExtra(IntentParams.PARAM_PUSH_MESSAGE);
                }
                _currentSeq = message.connSeq;
            }
            _currentDataPosition = intent.getIntExtra(IntentParams.PARAM_BOARD_POSITION, -1);
        }
        if(_currentData == null) {
            finish();
        }
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.title_detail);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    void initView() {
        findViewById(R.id.layout_reserver_list).setOnClickListener(this);
        _tvReservedCnt = findViewById(R.id.tv_reserved_cnt);
        mTvTitle = findViewById(R.id.tv_brf_detail_title);
        mTvDate = findViewById(R.id.tv_brf_detail_date);
//        mTvTime = findViewById(R.id.tv_brf_detail_time);
        mTvLoc = findViewById(R.id.tv_brf_detail_loc);
        mTvPersonnel = findViewById(R.id.tv_brf_detail_personnel);
        _tvReadCount = findViewById(R.id.tv_rd_cnt);
        mTvContent = findViewById(R.id.tv_brf_detail_content);

        tvRecipient = findViewById(R.id.tv_recipient_count);
        layoutControlRecipient = findViewById(R.id.layout_recipient_control);
        layoutControlRecipient.setVisibility(View.VISIBLE);

        mRecyclerViewImages = findViewById(R.id.recycler_brf_img);
        mRecyclerViewFiles = findViewById(R.id.recycler_brf_file);
        _recyclerViewRecipient = findViewById(R.id.recycler_chip);

        setImageRecycler();
        setFileRecycler();
        setRecipientRecycler();
//        initData();
        if (_currentData != null) {
            requestBriefingDetail(_currentData.seq);   //조회수 때문에 detail 을 호출해야만 함...
        }else if(_currentSeq != -1) {
            requestBriefingDetail(_currentSeq);
        }
    }

    private void initData() {
        if (_currentData != null) {
            if(_currentData.writerSeq == _seq) {
                invalidateOptionsMenu();
            }
            mImageList.clear();
            mFileList.clear();
            setView();
            if(_currentData.fileList != null && _currentData.fileList.size() > 0) {

                for(FileData data : _currentData.fileList) {
                    String mimeType = FileUtils.getMimeTypeFromExtension(data.extension);
                    LogMgr.w(data.saveName + " / " + mimeType);

                    // mimeType is checked for null here.
                    if (mimeType != null && mimeType.startsWith("image")) mImageList.add(data);
                    else {
                        data.initTempFileName();
                        mFileList.add(data);
                    }

                }
            }
            if(_currentData.seq != 0) {
                requestBriefingRecipientData(_currentData.seq);
            }
            if(mImageAdapter != null && mImageList.size() > 0) mImageAdapter.notifyDataSetChanged();
            if(mFileAdapter != null && mFileList.size() > 0)mFileAdapter.notifyDataSetChanged();
        }else if(_currentSeq != -1) {
            requestBriefingDetail(_currentSeq);
        }
    }
    private void initChipGroup() {

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
    private void setView(){
        String str = TextUtils.isEmpty(_currentData.title) ? "" : _currentData.title;
        mTvTitle.setText(str);

        str = TextUtils.isEmpty(_currentData.date) || TextUtils.isEmpty(_currentData.ptTime) ? "" : Utils.formatDate(_currentData.date, _currentData.ptTime, true);
        mTvDate.setText(str);

        str = TextUtils.isEmpty(_currentData.place) ? "" : _currentData.place;
        mTvLoc.setText(str);

//        str = TextUtils.isEmpty(_currentData.ptTime) ? "" : "시간: "+_currentData.ptTime;
//        mTvTime.setText(str);

        str = _currentData.participantsCnt+"명";
        //str = "참가인원: "+mInfo.reservationCnt+"명";
        mTvPersonnel.setText(str);
        _tvReadCount.setText(Utils.decimalFormat(_currentData.rdcnt));
        str = TextUtils.isEmpty(_currentData.content) ? "" : _currentData.content;
        mTvContent.setText(str);

        _tvReservedCnt.setText(String.format("(%d)", _currentData.reservationCnt));
    }

    private void setImageRecycler(){
        mImageAdapter = new BoardDetailImageListAdapter(mContext, mImageList, new BoardDetailImageListAdapter.ItemClickListener() {
            @Override
            public void onItemClick(List<FileData> item, int position) {
                startPhotoViewActivity(item, position);
            }
        });
        mRecyclerViewImages.setAdapter(mImageAdapter);
    }

    private void startPhotoViewActivity(List<FileData> clickImg, int position){
        if (clickImg != null) {
            Intent intent = new Intent(mContext, PhotoViewActivity.class);
            intent.putExtra(IntentParams.PARAM_ANNOUNCEMENT_DETAIL_IMG, mImageList);
            intent.putExtra(IntentParams.PARAM_ANNOUNCEMENT_DETAIL_IMG_POSITION, position);
            startActivity(intent);
        } else LogMgr.e("item is null");
    }

    private void setFileRecycler(){
        mFileAdapter = new BoardDetailFileListAdapter(mContext, mFileList, BoardDetailFileListAdapter.Action.Download, new BoardDetailFileListAdapter.onItemClickListener() {
            @Override
            public void onItemClick(int position, FileData data) {
                String type = data.path.replaceAll("/", "");
                File file = new File(mContext.getExternalFilesDir(type).getPath() + "/" + data.tempFileName);
                if(file.exists()) {
                    showFile(data);
                }else{
                    _downloadReceiver.setRequireRun(true);
                    _downloadReceiver.setCurrentPosition(position);
                    downloadFile(data);
                }
            }

            @Override
            public void onActionBtnClick(int position, FileData data, BoardDetailFileListAdapter.Action action) {
                if(action == BoardDetailFileListAdapter.Action.Download) {
                    _downloadReceiver.setRequireRun(false);
                    _downloadReceiver.setCurrentPosition(position);
                    downloadFile(data);
                }
            }
        });
        mRecyclerViewFiles.setAdapter(mFileAdapter);
    }
    private void setRecipientRecycler(){
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
    }
    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){
            case R.id.layout_reserver_list:
                navigate2ReservedListActivity();
                break;
        }
    }
    private void requestBriefingDelete(int boardSeq) {
        if(RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.deleteBriefing(_currentData.seq).enqueue(new Callback<BaseResponse>() {
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

    private void requestBriefingDetail(int ptSeq){
        if (RetrofitClient.getInstance() != null){
            showProgressDialog();
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.getBriefingDetail(ptSeq).enqueue(new Callback<BriefingDetailResponse>() {
                @Override
                public void onResponse(Call<BriefingDetailResponse> call, Response<BriefingDetailResponse> response) {
                    try {
                        if (response.isSuccessful()){

                            if (response.body() != null) {

                                BriefingData data = response.body().data;
                                if (data != null){
                                    _currentData = data;
                                    initData();
                                }else LogMgr.e(TAG+" DetailData is null");
                            }
                        }else{
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e){
                        LogMgr.e(TAG + "requestBrfDetail() Exception : ", e.getMessage());
                    }

                    hideProgressDialog();
                }

                @Override
                public void onFailure(Call<BriefingDetailResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "requestBrfDetail() onFailure >> " + t.getMessage());
                    }catch (Exception e){
                    }
                    hideProgressDialog();
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void requestBriefingRecipientData(int ptSeq){
        if (RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getLongTimeApiInterface();
            mRetrofitApi.getBriefingRecipientList(ptSeq).enqueue(new Callback<BriefingRecipientListResponse>() {
                @Override
                public void onResponse(Call<BriefingRecipientListResponse> call, Response<BriefingRecipientListResponse> response) {
                    try {
                        if (response.isSuccessful()) {
                            List<RecipientData> getData;
                            _recipientList.clear();
                            if (response.body() != null) {
                                getData = response.body().data;
                                if (getData != null && !getData.isEmpty()) {
                                    _recipientList.addAll(getData);

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
                    initChipGroup();
                    //mSwipeRefresh.setRefreshing(false);
                }

                @Override
                public void onFailure(Call<BriefingRecipientListResponse> call, Throwable t) {
                    initChipGroup();
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
    private void navigate2ReservedListActivity(){
        String dateStr = _currentData.date + _currentData.ptTime;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddHH:mm", Locale.KOREA);
        Date date = null;
        try {
            date = sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calBrf = Calendar.getInstance();
        if (date != null) calBrf.setTime(date);
        Calendar calCurrent = Calendar.getInstance();

        Constants.BriefingType type = Constants.BriefingType.TYPE_OPEN;
        if (calCurrent.after(calBrf) || calCurrent.equals(calBrf)) {
            type = Constants.BriefingType.TYPE_CLOSE;
        }
        else if (_currentData.reservationCnt >= _currentData.participantsCnt) {
            type = Constants.BriefingType.TYPE_FULL;
        }
        else {
            type = Constants.BriefingType.TYPE_OPEN;
        }
        Intent intent = new Intent(mContext, MenuBriefingReservedListActivity.class);
        intent.putExtra(IntentParams.PARAM_BRIEFING_PT_SEQ, _currentData.seq);
        intent.putExtra(IntentParams.PARAM_BRIEFING_PARTICIPANTS_CNT, _currentData.participantsCnt);
        intent.putExtra(IntentParams.PARAM_BRIEFING_RESERVATION_CNT, _currentData.reservationCnt);
        intent.putExtra(IntentParams.PARAM_BRIEFING_TYPE, type);
        resultLauncher.launch(intent);

    }
    private void navigate2EditBriefingActivity(){
        Intent editIntent = new Intent(mContext, EditBriefingActivity.class);
        editIntent.putExtra(IntentParams.PARAM_BRIEFING_INFO, _currentData);
        editIntent.putExtra(IntentParams.PARAM_BOARD_POSITION, _currentDataPosition);
        resultLauncher.launch(editIntent);
    }
    private void showFile(FileData data) {
        String url = data.tempFileName;
        url = FileUtils.replaceMultipleSlashes(url);
        LogMgr.w("view file uri = " + url);
        String mimeType = FileUtils.getMimeTypeFromExtension(data.extension);
        Intent intent = new Intent(Intent.ACTION_VIEW);
//                new ShareCompat.IntentBuilder(mContext)
//                        .setStream(Uri.parse(url))
//                        .setType(mimeType)
//                                .startChooser();
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        String type = data.path.replaceAll("/", "");
        File file = new File(mContext.getExternalFilesDir(type).getPath() + "/" + data.tempFileName);
        if(file.exists()) {
            Uri uri = FileProvider.getUriForFile(mContext, getApplicationContext().getPackageName() + ".provider", file);
            intent.setDataAndType(uri, mimeType);
            startActivity(Intent.createChooser(intent, getString(R.string.open_with)));
            if (intent.resolveActivity(getPackageManager()) == null) {
                Toast.makeText(mContext, R.string.msg_empty_open_with, Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void downloadFile(FileData data) {
        String url = RetrofitApi.FILE_SUFFIX_URL + data.path + "/" + data.saveName;
        url = FileUtils.replaceMultipleSlashes(url);
        LogMgr.w("download file uri = " + url);
        String fileName = data.orgName;
        String destinationPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + fileName;
//
        File downloadFile = new File(destinationPath);
        if(downloadFile.exists()) {
            final String finalUrl = url;
            showMessageDialog(getString(R.string.dialog_title_alarm)
                    , getString(R.string.board_item_confirm_download)
                    , new View.OnClickListener() {  //OKClickListener
                        @Override
                        public void onClick(View view) {
                            try {
                                downloadFile.delete();
                            }catch(Exception ex) {
                                ex.printStackTrace();
                            }

                            _downloadReceiver.setCurrentFileData(data);
//                            _downloadReceiver.setOriginalDownloadPath(destinationPath);
                            showProgressDialog();
                            FileUtils.downloadFile(mContext, finalUrl, fileName);
                            // BroadcastReceiver 등록

                            mContext.registerReceiver(_downloadReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                            hideMessageDialog();
                        }
                    },
                    new View.OnClickListener() {    //cancelClickListener
                        @Override
                        public void onClick(View view) {
                            hideMessageDialog();
                        }
                    });
        }else {
            _downloadReceiver.setCurrentFileData(data);
//            _downloadReceiver.setOriginalDownloadPath(destinationPath);
            showProgressDialog();
            FileUtils.downloadFile(mContext, url, fileName);
            // BroadcastReceiver 등록

            mContext.registerReceiver(_downloadReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        }
    }
}