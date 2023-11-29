package kr.jeet.edu.manager.activity.menu.notice;

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
import kr.jeet.edu.manager.adapter.BoardDetailFileListAdapter;
import kr.jeet.edu.manager.adapter.BoardDetailImageListAdapter;
import kr.jeet.edu.manager.adapter.RecipientChipListAdapter;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.db.PushMessage;
import kr.jeet.edu.manager.model.data.FileData;
import kr.jeet.edu.manager.model.data.NoticeData;
import kr.jeet.edu.manager.model.data.RecipientData;
import kr.jeet.edu.manager.model.response.SystemNoticeDetailResponse;
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

public class SystemNoticeDetailActivity extends BaseActivity {
    private static final String TAG = "noticeDetail";

    private TextView mTvTitle, mTvContent, mTvWriterName, mTvDate;
    private RecyclerView mRecyclerViewImages, mRecyclerViewFiles;
    private TextView tvRecipient;
//    private ChipGroup recipientChipGroup;
    RecyclerView _recyclerViewRecipient;
    private LinearLayoutCompat layoutControlRecipient;
    private BoardDetailImageListAdapter mImageAdapter;
    private BoardDetailFileListAdapter mFileAdapter;
    RecipientChipListAdapter _recipientListAdapter;
    private RetrofitApi mRetrofitApi;

    private ArrayList<FileData> mImageList = new ArrayList<>();
    private ArrayList<FileData> mFileList = new ArrayList<>();
    List<RecipientData> _recipientList = new ArrayList<>();
    private DownloadReceiver _downloadReceiver;
    NoticeData _currentData = null;
    int _currentDataPosition = 0;
    int _userGubun = 1;
    int _seq = 0;
    int _sfCode = 0;
    private int _currentSeq = -1;

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
        _seq = PreferenceUtil.getUserSeq(this);
        _sfCode = PreferenceUtil.getUserSFCode(this);
        initIntentData();
        initView();
        initAppbar();
        setAnimMove(Constants.MOVE_DETAIL_RIGHT);
    }

    private void initIntentData(){
        Intent intent = getIntent();
        if(intent != null) {

            if (intent.hasExtra(IntentParams.PARAM_BOARD_SEQ)){

                _currentSeq = intent.getIntExtra(IntentParams.PARAM_BOARD_SEQ, 0);
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
        if(_currentSeq == 0) {
            finish();
        }
    }

    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.settings_notice_system);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    void initView() {
        mTvTitle = findViewById(R.id.tv_board_detail_title);
        mTvContent = findViewById(R.id.tv_board_detail_content);
        mTvWriterName = findViewById(R.id.tv_board_detail_name);
        mTvDate = findViewById(R.id.tv_board_detail_write_date);
        mRecyclerViewImages = findViewById(R.id.recycler_board_img);
        mRecyclerViewFiles = findViewById(R.id.recycler_board_file);
        findViewById(R.id.img_rd_cnt).setVisibility(View.GONE);
        setImageRecycler();
        setFileRecycler();
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
//        initChipGroup();
        //endregion
        initData();
    }

    private void initData() {
        if (_currentData != null) {
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
            if(mImageAdapter != null && mImageList.size() > 0) mImageAdapter.notifyDataSetChanged();
            if(mFileAdapter != null && mFileList.size() > 0)mFileAdapter.notifyDataSetChanged();
            initChipGroup();
        }else if(_currentSeq != -1) {
            requestSystemNoticeDetail(_currentSeq);
        }
    }
    private void initChipGroup() {
        if(_currentData != null) {
            _recipientList.clear();
            _recipientList.addAll(_currentData.receiverList);
        }
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
        str = TextUtils.isEmpty(_currentData.content) ? "" : _currentData.content;
        mTvContent.setText(str);
        mTvWriterName.setText(_currentData.writerName);
        mTvDate.setText(Utils.formatDate(
                _currentData.insertDate,
                Constants.DATE_FORMATTER_YYYY_MM_DD_HH_mm_ss,
                Constants.DATE_FORMATTER_YYYY_MM_DD_HH_mm
        ));
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

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){

        }
    }

    private void requestSystemNoticeDetail(int ptSeq){
        if (RetrofitClient.getInstance() != null){
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.getSystemNoticeDetail(ptSeq).enqueue(new Callback<SystemNoticeDetailResponse>() {
                @Override
                public void onResponse(Call<SystemNoticeDetailResponse> call, Response<SystemNoticeDetailResponse> response) {
                    try {
                        if (response.isSuccessful()){
                            if (response.body() != null) {
                                NoticeData data = response.body().data;
                                if (data != null){
                                    LogMgr.e(TAG,"data != null");
                                    _currentData = data;
                                    initData();
                                }else LogMgr.e(TAG," DetailData is null");
                            }else{
                                LogMgr.e(TAG,"body.data is null");
                            }
                        }else{
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e){
                        LogMgr.e(TAG,"requestNoticeDetail() Exception : " + e.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<SystemNoticeDetailResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "requestBrfDetail() onFailure >> " + t.getMessage());
                    }catch (Exception e){
                    }
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void showFile(FileData data) {
        String url = data.tempFileName;
        url = FileUtils.replaceMultipleSlashes(url);
        LogMgr.w("view file uri = " + url);
        String mimeType = FileUtils.getMimeTypeFromExtension(data.extension);
        Intent intent = new Intent(Intent.ACTION_VIEW);
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