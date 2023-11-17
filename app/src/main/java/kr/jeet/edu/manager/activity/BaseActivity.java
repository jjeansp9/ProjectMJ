package kr.jeet.edu.manager.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsControllerCompat;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.dialog.PopupDialog;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.Utils;

public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener {

    public Context mContext;
    private AlertDialog mProgressDialog = null;
    //메세지 팝업
    protected PopupDialog popupDialog = null;
    private boolean setBar = false;
    private int move = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setStatusAndNavigatinBar(setBar);
    }
    abstract void initView();
    abstract void initAppbar();

    @Override
    public void onClick(View view) {

    }

    protected void showProgressDialog()
    {
        showProgressDialog(getString(R.string.requesting), null);
    }

    protected void showProgressDialog(String msg)
    {
        showProgressDialog(msg, null);
    }

    protected void showProgressDialog(DialogInterface.OnCancelListener listener)
    {
        showProgressDialog(getString(R.string.requesting), listener);
    }

    protected void showProgressDialog(String msg, DialogInterface.OnCancelListener listener) {
        runOnUiThread(() -> {
            if (mProgressDialog == null){
                View view = getLayoutInflater().inflate(R.layout.dialog_progressbar, null, false);
                TextView txt = view.findViewById(R.id.text);
                txt.setText(msg);

                mProgressDialog = new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setView(view)
                        .create();
                mProgressDialog.show();
            }
        });

/*
        Window window = mProgressDialog.getWindow();
        if(window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(mProgressDialog.getWindow().getAttributes());
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            mProgressDialog.getWindow().setAttributes(layoutParams);
        }
        */
    }

    protected void hideProgressDialog() {
        runOnUiThread(() -> {
            try {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                }
            } catch (Exception e) {
                LogMgr.e("hideProgressDialog()", e.getMessage());
            }
        });
    }
//    @Override
//    public boolean dispatchTouchEvent(MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//            View view = getCurrentFocus();
//            if (view instanceof EditText) {
//                Rect outRect = new Rect();
//                view.getGlobalVisibleRect(outRect);
//                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
//                    view.clearFocus();
//                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
//                }
//            }
//        }
//        return super.dispatchTouchEvent(event);
//    }
    protected void showMessageDialog(String title, String msg, View.OnClickListener okListener, View.OnClickListener cancelListener) {
        if(popupDialog != null && popupDialog.isShowing()) {
            popupDialog.dismiss();
        }
        popupDialog = new PopupDialog(mContext);
        popupDialog.setTitle(title);
        popupDialog.setContent(msg);
        popupDialog.setOnOkButtonClickListener(okListener);
        if(cancelListener != null) {
            popupDialog.setOnCancelButtonClickListener(cancelListener);
        }
        popupDialog.show();
    }
    protected void hideMessageDialog() {
        if(popupDialog != null && popupDialog.isShowing()) {
            popupDialog.dismiss();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // 이전 화면으로 이동
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        switch (move){
            case Constants.MOVE_DEFAULT:
                LogMgr.e("EVENT_DETAIL");
                break;
            case Constants.MOVE_LEFT:
                overridePendingTransition(R.anim.none, R.anim.horizontal_out);
                LogMgr.e("EVENT_LEFT");
                break;
            case Constants.MOVE_RIGHT:
                overridePendingTransition(R.anim.none, R.anim.horizontal_exit);
                LogMgr.e("EVENT_RIGHT");
                break;
            case Constants.MOVE_UP:
                overridePendingTransition(R.anim.none, R.anim.vertical_out);
                LogMgr.e("EVENT_UP");
                break;
            case Constants.MOVE_DOWN:
                overridePendingTransition(R.anim.none, R.anim.vertical_exit);
                LogMgr.e("EVENT_DOWN");
                break;
            case Constants.MOVE_DETAIL_RIGHT:
                overridePendingTransition(R.anim.horizontal_in, R.anim.horizontal_exit);
                LogMgr.e("EVENT_DETAIL_RIGHT");
                break;
        }
    }
    protected void setAnimMove(int setMove){
        move = setMove;
    }

    protected void setStatusAndNavigatinBar(boolean isNotSet){
        Window window = getWindow();
        int resColor = Color.BLACK;
        boolean lightStatusBar = false;
        boolean lightNavigationBar = false;

        if (!isNotSet){
            resColor = Color.WHITE;
            lightStatusBar = true; // 상태표시줄 [ true - black, false - white ]
            lightNavigationBar = true; // 네비게이션 [ true - black, false - white ]
        }
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, window.getDecorView());

        window.setStatusBarColor(resColor);
        window.setNavigationBarColor(resColor);

        controller.setAppearanceLightStatusBars(lightStatusBar); // status bar
        controller.setAppearanceLightNavigationBars(lightNavigationBar); // navigation bar
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }
    protected void showKeyboard(View view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }
}