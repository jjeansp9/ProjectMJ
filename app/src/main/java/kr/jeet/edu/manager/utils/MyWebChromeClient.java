package kr.jeet.edu.manager.utils;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Message;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MyWebChromeClient extends WebChromeClient {

    private static final String TAG = "MyWebChromeClient";

    private Dialog dialog;
    private WebView newWebView;
    private AppCompatActivity activity;


    public MyWebChromeClient(AppCompatActivity mActivity) {
        this.activity = mActivity;
    }

    @Override
    public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
        LogMgr.e("WebView", "Event!! create");
        newWebView = new WebView(view.getContext());
        WebSettings webSettings = newWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        newWebView.setWebViewClient(new MyWebViewClient(activity, newWebView));
        newWebView.setWebChromeClient(new MyWebChromeClient(activity) {
            @Override
            public void onCloseWindow(WebView window) {
                LogMgr.e("WebView", "Event!! close2");

                if (dialog != null) dialog.dismiss();
                if (newWebView != null) newWebView.destroy();
                if (window != null) window.destroy();

                super.onCloseWindow(window);
            }
        });
        activity.setContentView(newWebView);
        //dialog = new Dialog(view.getContext());

//            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
//            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
//            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
//
//            dialog.getWindow().setAttributes(params);
//            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                @Override
//                public void onDismiss(DialogInterface dialogInterface) {
//                    if (newWebView != null) newWebView.destroy();
//                }
//            });
//            dialog.show();

        WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
        transport.setWebView(newWebView);
        resultMsg.sendToTarget();

        return true;
    }

//        @Override
//        public void onCloseWindow(WebView window) {
//            LogMgr.e("testWebView", "Event!! close");
//
//            if (dialog != null) dialog.dismiss();
//            if (newWebView != null) newWebView.destroy();
//            if (window != null) window.destroy();
//        }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
        LogMgr.e(TAG, "onJsAlert() url ["+url+"], msg ="+message);
        new AlertDialog.Builder(view.getContext())
                .setMessage(message)
                .setPositiveButton(android.R.string.ok,
                        new AlertDialog.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm();
                            }
                        })
                .setCancelable(false)
                .create()
                .show();

        return true;
    }
}
