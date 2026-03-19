package hr.cizmic.seebanking.data.local;

import android.content.Context;

import androidx.room.Room;

// singleton provider for db instance
public final class DatabaseProvider {

    private static volatile AppDatabase INSTANCE;

    private DatabaseProvider() {}

    // get db instance. Create if doesn't exist
    public static AppDatabase get(Context context) {
        if (INSTANCE == null) {
            synchronized (DatabaseProvider.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "mobilebanking.db"
                            )
                            .fallbackToDestructiveMigration() // wipe data on schema change
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
