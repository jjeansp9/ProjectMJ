package kr.jeet.edu.manager.fcm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.util.Map;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.activity.IntroActivity;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.db.JeetDatabase;
import kr.jeet.edu.manager.db.PushMessage;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.PreferenceUtil;
import kr.jeet.edu.manager.utils.Utils;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private String TAG = "firebase";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        LogMgr.e(TAG, "onNewToken() : " + token);
        // 로그인 정보가 있으면 서버에 push token 전달
        int memberSeq = PreferenceUtil.getUserSeq(getApplicationContext());
        if(memberSeq != 0)
        {
            Utils.refreshPushToken(this, memberSeq, token);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        LogMgr.d(TAG, "onMessageReceived~");
        super.onMessageReceived(message);
        LogMgr.e(TAG, "Push From : " + message.getFrom());

//        if (message.getData().size() > 0) {
//            String jsonData = new JSONObject(message.getData()).toString();
//            LogMgr.d("FCM", "Received data: " + jsonData);
//        }else{
//            LogMgr.d("FCM", "Received data size 0");
//        }

//        // 알림 메시지가 있는 경우 알림 내용 출력
//        if (message.getNotification() != null) {
//            String notificationBody = message.getNotification().getBody();
//            LogMgr.d("FCM", "Notification body: " + notificationBody);
//        }
        if (message.getNotification() != null) {
            // notify message
            RemoteMessage.Notification notification = message.getNotification();
            LogMgr.e(TAG, "[FCM] noti  " + notification.getTag() + " / " +
                    notification.getChannelId() + " / " +
                    notification.getTag() + " / "
            );
        }
        if (message.getData().size() > 0) {
            // data message
            Map<String, String> map = message.getData();
            LogMgr.i(TAG, "[FCM] size : " + message.getData().size());
            LogMgr.i(TAG, "[FCM] payload : " + message.getData());
            for (String key : map.keySet()) {
                LogMgr.e(TAG, "key = " + key + " : value = " + map.get(key));
            }
            if(map != null){
                PushMessage pushMsg = PushMessage.buildFromMap(map);
                class InsertRunnable implements Runnable {
                    PushMessage[] pushMessages;
                    InsertRunnable(PushMessage... pushMessages) {
                        this.pushMessages = pushMessages;
                    }
                    @Override
                    public void run() {
                        JeetDatabase.getInstance(getApplicationContext()).pushMessageDao().insertAll(this.pushMessages);
                    }
                }
                InsertRunnable insertRunnable = new InsertRunnable(pushMsg);
                Thread t = new Thread(insertRunnable);
                t.start();
                FCMManager fcmManager = new FCMManager(this, pushMsg);
                fcmManager.handlePushMessage();
            }
        } else {
            // FCM 메시지가 알림 메시지도 아니고 데이터 메시지도 아닌 경우
            LogMgr.i(TAG, "[FCM] No notification or data payload.");
        }
//        sendNotification(message.getFrom(), message.getNotification().getBody());


        // https://moon8089.tistory.com/10
        // https://velog.io/@leeyjwinter/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-Firebase-FCM
    }

}
