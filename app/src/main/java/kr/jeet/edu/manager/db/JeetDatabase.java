package kr.jeet.edu.manager.db;

import android.content.Context;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import kr.jeet.edu.manager.utils.Converters;

@Database(entities = {PushMessage.class, NewBoardData.class},
        version = 2,
        exportSchema = true,
        autoMigrations = {@AutoMigration(from = 1, to = 2)})
@TypeConverters(Converters.class)
public abstract class JeetDatabase extends RoomDatabase {
    private static JeetDatabase JeetDBInstance = null;
    public abstract PushMessageDao pushMessageDao();
    public abstract NewBoardDao newBoardDao();

    public static JeetDatabase getInstance(Context context) {
        if(JeetDBInstance == null) {
            JeetDBInstance = Room.databaseBuilder(context.getApplicationContext(),
                    JeetDatabase.class,
                    "jeetmanager.db")
                        .fallbackToDestructiveMigration()
                        .build();
        }
        return JeetDBInstance;
    }

    public static void destroyInstance() {
        JeetDBInstance = null;
    }
}
