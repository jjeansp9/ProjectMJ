package kr.jeet.edu.manager.sns;

import android.content.Context;
import android.os.Handler;


public class SNSLoginManager {
    private static final String TAG = SNSLoginManager.class.getSimpleName();
    private Context mContext;

    public Boolean mIsJoinStatus = false;
    public Handler mHandler = null;

    public SNSLoginManager(Context context) {
        this.mContext = context;
    }

    public void LoginProcess() {
        //
    }

    public void LogoutProcess() {
        //
    }

    public void DeleteAccountProcess(){

    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }
}

