package hr.cizmic.seebanking.sync;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

// schedules bg job to sync pending transfers when internet is available
public final class SyncScheduler {

    private static final String UNIQUE_NAME = "mobilebanking_sync";

    private SyncScheduler() {}

    // schedule sync worker to run when network is connected
    public static void enqueueOneTimeSync(Context context) {
        // only run when there's internet
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(constraints)
                .build();

        // keep means if it's already scheduled, don't add another one
        WorkManager.getInstance(context.getApplicationContext())
                .enqueueUniqueWork(UNIQUE_NAME, ExistingWorkPolicy.KEEP, work);
    }
}
