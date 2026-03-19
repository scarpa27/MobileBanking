package hr.cizmic.seebanking;

import android.app.Application;

import hr.cizmic.seebanking.sync.SyncScheduler;

// init dependencies and background sync
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // DI setup
        ServiceLocator.init(this);

        // Schedule background work to sync data when network is available (first transaction list load on app start)
        SyncScheduler.enqueueOneTimeSync(this);
    }
}
