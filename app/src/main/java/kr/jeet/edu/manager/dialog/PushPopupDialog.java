package kr.jeet.edu.manager.dialog;

import static kr.jeet.edu.manager.fcm.FCMManager.MSG_TYPE_ATTEND;
import static kr.jeet.edu.manager.fcm.FCMManager.MSG_TYPE_COUNSEL;
import static kr.jeet.edu.manager.fcm.FCMManager.MSG_TYPE_NOTICE;
import static kr.jeet.edu.manager.fcm.FCMManager.MSG_TYPE_LEVEL_TEST;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.db.PushMessage;
import kr.jeet.edu.manager.fcm.FCMManager;
import kr.jeet.edu.manager.utils.LogMgr;

public class PushPopupDialog extends Dialog {
    private static final String TAG = "pushPopup";
    private Context context;
    private TextView titleTv, contentTv, noteTv;
    private Button okBtn;
    private ViewGroup titleLayout;
    PushMessage _pushMessage;
    FCMManager _fcmManager;

    public PushPopupDialog(@NonNull Context context, PushMessage msg) {
        super(context);
        this.context = context;
        this._pushMessage = msg;
        _fcmManager = new FCMManager(context, msg);
        initView();
    }

    public PushPopupDialog(@NonNull Context context, int themeResId, PushMessage msg) {
        super(context, themeResId);
        this.context = context;
        initView();
    }

    public void setPushMessage(PushMessage message) {
        this._pushMessage = message;
    }
    private void initView() {
        this.setCanceledOnTouchOutside(false);
        this.setCancelable(false);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.layout_push_popup);

        // 버튼 테두리 라운드 적용
        findViewById(R.id.dialog_ly).setClipToOutline(true);

        titleLayout = findViewById(R.id.title_ly);
        titleTv = (TextView) findViewById(R.id.title);
        contentTv = (TextView) findViewById(R.id.content);
        noteTv = (TextView) findViewById(R.id.note);
        okBtn = (Button) findViewById(R.id.okBtn);
        if(_pushMessage != null) {
            String title = "";

            switch(_pushMessage.pushType) {
                case MSG_TYPE_NOTICE:
                    title = context.getString(R.string.push_type_announcement);
                    break;
                case MSG_TYPE_ATTEND:
                    title = context.getString(R.string.push_type_attendance);
                    break;
                case MSG_TYPE_LEVEL_TEST:
                    title = context.getString(R.string.push_type_leveltest);
                    break;
                case MSG_TYPE_COUNSEL:
                    title = context.getString(R.string.push_type_counsel);
                default :
                    title = "JEET알림";
                    break;
            }
            LogMgr.e(TAG, "set title = " + title);
            titleTv.setText(title);
            contentTv.setText(_pushMessage.title);
            noteTv.setText(_pushMessage.body);
        }
    }


    public void setOkButtonText(String str) {
        okBtn.setText(str);
    }

    public void setOnOkButtonClickListener(View.OnClickListener listener) {
        okBtn.setOnClickListener(listener);
    }

    public FCMManager getFCMManager() {
        return _fcmManager;
    }

}
