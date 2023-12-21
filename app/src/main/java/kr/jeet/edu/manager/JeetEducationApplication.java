package kr.jeet.edu.manager;

import android.app.Application;

import com.jakewharton.threetenabp.AndroidThreeTen;
import com.kakao.sdk.common.util.Utility;

import kr.jeet.edu.manager.db.JeetDatabase;
import kr.jeet.edu.manager.utils.LogMgr;

public class JeetEducationApplication extends Application {
    private final String TAG = "JeetApplication";
    @Override
    public void onCreate() {

        super.onCreate();
        String keyhash = Utility.INSTANCE.getKeyHash(this);
        LogMgr.d("keyhash : " + keyhash);
        JeetDatabase.getInstance(this);
        AndroidThreeTen.init(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
