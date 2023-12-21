package kr.jeet.edu.manager.activity.menu.announcement;

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

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

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
import kr.jeet.edu.manager.adapter.BoardDetailFileListAdapter;
import kr.jeet.edu.manager.adapter.BoardDetailImageListAdapter;
import kr.jeet.edu.manager.adapter.RecipientChipListAdapter;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.DataManager;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.db.PushMessage;
import kr.jeet.edu.manager.fcm.FCMManager;
import kr.jeet.edu.manager.model.data.AnnouncementData;
import kr.jeet.edu.manager.model.data.FileData;
import kr.jeet.edu.manager.model.data.RecipientData;
import kr.jeet.edu.manager.model.response.BaseResponse;
import kr.jeet.edu.manager.model.response.BoardDetailResponse;
import kr.jeet.edu.manager.receiver.DownloadReceiver;
import kr.jeet.edu.manager.server.RetrofitApi;
import kr.jeet.edu.manager.server.RetrofitClient;
import kr.jeet.edu.manager.utils.DBUtils;
import kr.jeet.edu.manager.utils.FileUtils;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.PreferenceUtil;
import kr.jeet.edu.manager.utils.Utils;
import kr.jeet.edu.manager.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuAnnouncementDetailActivity extends BaseActivity {

    private String TAG = MenuAnnouncementDetailActivity.class.getSimpleName();

    private TextView mTvTitle, mTvName, mTvDate, mTvContent, tvReadCount;
    private RecyclerView mRecyclerViewImages;
    private RecyclerView mRecyclerViewFiles;
    private BoardDetailImageListAdapter mImageAdapter;
    private BoardDetailFileListAdapter mFileAdapter;
    private RetrofitApi mRetrofitApi;

    private ArrayList<FileData> mImageList = new ArrayList<>();
    private ArrayList<FileData> mFileList = new ArrayList<>();
    private DownloadReceiver _downloadReceiver;
    AnnouncementData _currentData = null;
    int _currentDataPosition = 0;
    private TextView tvRecipient;
    RecyclerView _recyclerViewRecipient;
    private ConstraintLayout layoutControlRecipient;
    RecipientChipListAdapter _recipientListAdapter;

    List<RecipientData> _recipientList = new ArrayList<>();

    Menu _menu;
    int _memberSeq = -1;
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
        setContentView(R.layout.activity_menu_board_detail);
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
        _memberSeq = PreferenceUtil.getUserSeq(this);
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
            _currentData = Utils.getParcelableExtra(intent, IntentParams.PARAM_ANNOUNCEMENT_INFO, AnnouncementData.class);

        }
        Bundle bundle = intent.getExtras();
        if(bundle != null) {
            PushMessage message = Utils.getSerializableExtra(bundle, IntentParams.PARAM_PUSH_MESSAGE, PushMessage.class);
            if(message != null) _currentSeq = message.connSeq;
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

        mTvTitle = findViewById(R.id.tv_board_detail_title);
        mTvName = findViewById(R.id.tv_board_detail_name);
        mTvDate = findViewById(R.id.tv_board_detail_write_date);
        tvReadCount = findViewById(R.id.tv_rd_cnt);
        mTvContent = findViewById(R.id.tv_board_detail_content);
        //imageList
        mRecyclerViewImages = findViewById(R.id.recycler_board_img);
        mImageAdapter = new BoardDetailImageListAdapter(mContext, mImageList, new BoardDetailImageListAdapter.ItemClickListener() {
            @Override
            public void onItemClick(List<FileData> item, int position) {
                if (item != null) {
                    Intent intent = new Intent(mContext, PhotoViewActivity.class);
                    intent.putExtra(IntentParams.PARAM_ANNOUNCEMENT_DETAIL_IMG, mImageList);
                    intent.putExtra(IntentParams.PARAM_ANNOUNCEMENT_DETAIL_IMG_POSITION, position);
                    startActivity(intent);
                } else LogMgr.e("item is null");
            }
        });
        mRecyclerViewImages.setAdapter(mImageAdapter);
        mRecyclerViewFiles = findViewById(R.id.recycler_board_file);
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
            if(_currentData.memberResponseVO.seq == _memberSeq) {
                invalidateOptionsMenu();
            }
            mTvTitle.setText(_currentData.title); // 제목
            LogMgr.w(TAG,"memberResponseVO = " + _currentData.memberResponseVO);
            mTvName.setText(_currentData.memberResponseVO.name); // 작성자 이름
            mTvDate.setText(_currentData.insertDate); // 작성날짜
            mTvContent.setText(_currentData.content); // 내용
            tvReadCount.setText(Utils.decimalFormat(_currentData.rdcnt));    //조회수
            if(_currentData.fileList != null && _currentData.fileList.size() > 0) {
                for(FileData data : _currentData.fileList) {
                    String mimeType = FileUtils.getMimeTypeFromExtension(data.extension);
                    LogMgr.w(data.saveName + " / " + mimeType);
                    if(mimeType.startsWith("image")) {  //image
                        mImageList.add(data);
                    }else{
                        data.initTempFileName();
                        mFileList.add(data);
                    }
                }
            }
            if(mImageList.size() > 0) mImageAdapter.notifyDataSetChanged();
            if(mFileList.size() > 0)mFileAdapter.notifyDataSetChanged();
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
            if ((_userGubun == Constants.USER_TYPE_ADMIN && _currentData.memberResponseVO.seq == _memberSeq)
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
                navigate2EditAnnouncementActivity();
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
        mImageList.clear();
        mFileList.clear();
        if (RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.getBoardDetail(boardSeq).enqueue(new Callback<BoardDetailResponse>() {
                @Override
                public void onResponse(Call<BoardDetailResponse> call, Response<BoardDetailResponse> response) {
                    try {
                        if (response != null && response.isSuccessful()) {

                            if (response.body() != null) {

                                AnnouncementData getData = response.body().data;
                                if (getData != null) {
                                    _currentData = getData;
                                    _currentData.isRead = true;
                                    //insertDB(_currentData);
                                    DBUtils.insertReadDB(mContext, _currentData, _memberSeq, DataManager.BOARD_NOTICE);
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
    private void navigate2EditAnnouncementActivity() {
        Intent editIntent = new Intent(mContext, EditAnnouncementActivity.class);
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

                                downloadFile.setWritable(true);
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
