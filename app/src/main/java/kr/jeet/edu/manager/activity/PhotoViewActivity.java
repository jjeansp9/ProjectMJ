package kr.jeet.edu.manager.activity;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.viewpager.widget.ViewPager;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.adapter.PhotoViewPagerAdapter;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.model.data.FileData;
import kr.jeet.edu.manager.receiver.DownloadReceiver;
import kr.jeet.edu.manager.server.RetrofitApi;
import kr.jeet.edu.manager.utils.FileUtils;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.Utils;
import kr.jeet.edu.manager.view.CustomViewPager;

public class PhotoViewActivity extends BaseActivity {
    private String TAG = "PhotoViewActivity";

    private TextView tvPage;
    private RelativeLayout layoutHeader;

    private ArrayList<FileData> mImageList = new ArrayList<>();
    private ArrayList<String> _webImageList = new ArrayList<>();
    private int position = 0;
    private CustomViewPager mPager;
    private PhotoViewPagerAdapter mAdapter;
    private ImageView ivDownload;
    private DownloadReceiver _downloadReceiver = null;
    /**
     * 게시판 등에서 보여주는 이미지 구조인지, 차량에서 보여주는 웹 링크인지
     */
    boolean isFileData = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);
        mContext = this;
        initIntentData();
        initView();
    }

    private void initIntentData(){
        try {
            Intent intent = getIntent();
            if (intent == null) return;
            if (intent.hasExtra(IntentParams.PARAM_ANNOUNCEMENT_DETAIL_IMG) &&
                    intent.hasExtra(IntentParams.PARAM_ANNOUNCEMENT_DETAIL_IMG_POSITION)){
                isFileData = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    mImageList = intent.getParcelableArrayListExtra(IntentParams.PARAM_ANNOUNCEMENT_DETAIL_IMG, FileData.class);
                }else{
                    mImageList = intent.getParcelableArrayListExtra(IntentParams.PARAM_ANNOUNCEMENT_DETAIL_IMG);
                }
                position = intent.getIntExtra(IntentParams.PARAM_ANNOUNCEMENT_DETAIL_IMG_POSITION, position);
                if(mImageList.isEmpty()) finish();
                for (FileData file : mImageList) LogMgr.e(TAG+"ImgTest", RetrofitApi.FILE_SUFFIX_URL + file.path + file.saveName + " position : " + position);
            }else if(intent.hasExtra(IntentParams.PARAM_WEB_DETAIL_IMG) &&
                    intent.hasExtra(IntentParams.PARAM_WEB_DETAIL_IMG_POSITION)) {
                isFileData = false;
                _webImageList = intent.getStringArrayListExtra(IntentParams.PARAM_WEB_DETAIL_IMG);
                position = intent.getIntExtra(IntentParams.PARAM_WEB_DETAIL_IMG_POSITION, position);
                if(_webImageList.isEmpty()) finish();
            }

        }catch (Exception e){ LogMgr.e(TAG + " Exception : ", e.getMessage()); }

    }

    void initView() {
        tvPage = findViewById(R.id.tv_photoview_page);
        mPager = (CustomViewPager) findViewById(R.id.view_pager);
        if(isFileData) {
            if (mImageList != null && mImageList.size() > 0)
                tvPage.setText(position + 1 + " / " + mImageList.size());
            mAdapter = new PhotoViewPagerAdapter(mImageList, tvPage);
        }else{
            if (_webImageList != null && _webImageList.size() > 0)
                tvPage.setText(position + 1 + " / " + _webImageList.size());
            mAdapter = new PhotoViewPagerAdapter(tvPage, _webImageList);
        }

        mPager.setAdapter(mAdapter);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageSelected(int index) {
                LogMgr.w(TAG, "page selected " + index);
                if(isFileData) {
                    tvPage.setText(index + 1 + " / " + mImageList.size());
                    position = index;
                    FileData item = mImageList.get(position);
                    checkEnableForDownload(item);
                }else{
                    tvPage.setText(index + 1 + " / " + _webImageList.size());
                    position = index;
                    String item = _webImageList.get(position);
                    checkEnableForDownload(item);
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {}
        });
        mPager.setCurrentItem(position);
        ImageView ivClose = findViewById(R.id.img_close);
        ivClose.setColorFilter(getColor(R.color.white));
        ivClose.setOnClickListener(this);
        ivDownload = findViewById(R.id.img_photoview_download);
        ivDownload.setColorFilter(getColor(R.color.white));
        ivDownload.setOnClickListener(this);
        layoutHeader = findViewById(R.id.layout_header);

        if (Utils.isLandscapeMode(mContext)) { // 가로모드일때
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) layoutHeader.getLayoutParams();
            params.leftMargin = Utils.fromDpToPx(30); // 양쪽 마진 설정. 네비게이션바 겹쳐져서 다운로드 아이콘 클릭 잘 안됨
            params.rightMargin = Utils.fromDpToPx(30);
            layoutHeader.setLayoutParams(params);

        }else { // 세로모드일때
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) layoutHeader.getLayoutParams();
            params.topMargin = statusBarHeight(mContext); // 상단의 상태 바 size만큼 margin 값 주기
            layoutHeader.setLayoutParams(params);
        }
        setStatusBarTransparent();
//        if(position == 0) { //0일 때는 onPageSelected가 호출되지 않음.
//            if(isFileData) checkEnableForDownload(mImageList.get(0));
//            else checkEnableForDownload(_webImageList.get(0));
//        }

    }

    /**
     * 서버 이미지를 확인할 수 없는 경우 Download 비활성
     * @param item
     */
    private void checkEnableForDownload(FileData item) {
        String url = RetrofitApi.FILE_SUFFIX_URL + item.path + "/" + item.saveName;
        url = FileUtils.replaceMultipleSlashes(url);
        FileUtils.isImageUrlValid(mContext, url, new FileUtils.ImageValidationListener() {
            @Override
            public void onImageValidation(boolean isValid) {
                LogMgr.e(" image isValid =" + isValid);
                FileUtils.setImageViewEnabledWithColor(mContext, ivDownload, isValid, R.color.white, R.color.blackgray);
            }
        });
    }
    private void checkEnableForDownload(String item) {
        String url = item;
        url = FileUtils.replaceMultipleSlashes(url);
        FileUtils.isImageUrlValid(mContext, url, new FileUtils.ImageValidationListener() {
            @Override
            public void onImageValidation(boolean isValid) {
                LogMgr.e(" image isValid =" + isValid);
                FileUtils.setImageViewEnabledWithColor(mContext, ivDownload, isValid, R.color.white, R.color.blackgray);
            }
        });
    }
    public void setStatusBarTransparent() {
        Window window = getWindow();

        window.setFlags( // 바, 상태표시줄의 위치에 제한을 두지않고 레이아웃 확장
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(window, false); // false로 설정하면 바, 상태표시줄 확장
            final WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

            }

        }else{
            window = getWindow();
            View decorView = window.getDecorView();

            int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                flags += ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }

            decorView.setSystemUiVisibility(flags);
            WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, window.getDecorView());
            controller.setAppearanceLightStatusBars(false);
            controller.setAppearanceLightNavigationBars(false);
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(layoutParams);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        //window.setNavigationBarColor(Color.BLACK);
    }

    public int statusBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");

        if (resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        } else {
            return 0;
        }
    }

    public int navigationHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");

        if (resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        } else {
            return 0;
        }
    }
    void initAppbar() {}

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){
            case R.id.img_close:
                finish();
                break;
            case R.id.img_photoview_download:
                downloadImage();
                break;
        }
    }

    private void downloadImage(){
        String url, fileName = "";
        if(isFileData) {
            url = RetrofitApi.FILE_SUFFIX_URL + mImageList.get(position).path + "/" + mImageList.get(position).saveName;
            url = FileUtils.replaceMultipleSlashes(url);

            LogMgr.w("download file uri = " + url);

            fileName = mImageList.get(position).orgName;
        }else{
            url = _webImageList.get(position);
            fileName = url.contains("/")? url.substring(url.lastIndexOf("/")).replace("/", "") : url;
        }
        FileUtils.downloadFile(mContext, url, fileName);
        // BroadcastReceiver 등록
        _downloadReceiver = new DownloadReceiver(new DownloadReceiver.DownloadListener() {
            @Override
            public void onDownloadComplete(int position, FileData data, Uri downloadFileUri) {
                try {
                    mContext.unregisterReceiver(_downloadReceiver);
                } catch (IllegalArgumentException e) {
                }
            }

            @Override
            public void onShow(FileData data) {

            }
        });
        mContext.registerReceiver(_downloadReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    protected void onDestroy() {
        if(_downloadReceiver != null) {
            try {
                mContext.unregisterReceiver(_downloadReceiver);
            }catch(IllegalArgumentException e) {

            }
        }
        super.onDestroy();
    }
}