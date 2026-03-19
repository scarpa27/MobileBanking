package hr.cizmic.seebanking.sync;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import hr.cizmic.seebanking.ServiceLocator;

// bg worker that retries failed transfers from offline queue
public class SyncWorker extends Worker {

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            ServiceLocator sl = ServiceLocator.get();
            if (sl.tokenStore().getToken() == null) return Result.success(); // not logged in, nothing to do

            // 1) try to send any queued transfers that failed earlier (offline mode
            sl.bankingRepository().processPendingTransfersBlocking();

            // 2) refresh accs so balances are up to date
            sl.bankingRepository().refreshAccountsBlocking();

            return Result.success(); // all good
        } catch (Exception ex) {
            return Result.retry(); // something went wrong, try again later
        }
    }
}
