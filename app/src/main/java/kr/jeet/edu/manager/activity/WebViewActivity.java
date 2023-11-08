package kr.jeet.edu.manager.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.MyWebChromeClient;
import kr.jeet.edu.manager.utils.MyWebViewClient;
import kr.jeet.edu.manager.view.CustomAppbarLayout;

public class WebViewActivity extends BaseActivity {
    private final static String TAG = "terms";
    private String title = "";
    private String url = "";
    private WebView wv;

    private AppCompatActivity mActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        mContext = this;
        mActivity = this;
        initIntentData();
        initView();
        initAppbar();
    }
    private void initIntentData(){
        try {
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra(IntentParams.PARAM_APPBAR_TITLE)){
                title = intent.getStringExtra(IntentParams.PARAM_APPBAR_TITLE);
            }
            if (intent.hasExtra(IntentParams.PARAM_WEB_VIEW_URL)){
                url = intent.getStringExtra(IntentParams.PARAM_WEB_VIEW_URL);
            }
        }catch (Exception e){

        }
    }
    @Override
    void initView() {
        findViewById(R.id.btn_confirm).setOnClickListener(this);

        if (!TextUtils.isEmpty(url)){
//            mScrollView.setVisibility(View.GONE);
            wv = findViewById(R.id.webview);

            wv.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            wv.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            wv.getSettings().setSupportZoom(true);
            wv.getSettings().setBuiltInZoomControls(true);
            wv.getSettings().setDisplayZoomControls(false);

            WebSettings webSettings = wv.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setUseWideViewPort(true);
            webSettings.setSupportMultipleWindows(true);
            webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

            wv.setWebViewClient(new MyWebViewClient(mActivity, wv));
            wv.setWebChromeClient(new MyWebChromeClient(mActivity));

            wv.loadUrl(url);
        }else{
            LogMgr.e(TAG, "empty url~");
        }
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(title);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){
            case R.id.btn_confirm:
                finish();
                break;
        }
    }
}