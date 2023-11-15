package kr.jeet.edu.manager.fcm;

import java.util.concurrent.atomic.AtomicInteger;

public class NotificationID {
    private final static AtomicInteger c = new AtomicInteger(FCMManager.NOTIFICATION_ID_NONE);
    public static int getID() {
        return c.incrementAndGet();
    }
}
