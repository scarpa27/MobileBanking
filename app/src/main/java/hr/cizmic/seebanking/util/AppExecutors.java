package hr.cizmic.seebanking.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

// provides thread pools for running tasks on different threads
public final class AppExecutors {

    private final Executor diskIO = Executors.newSingleThreadExecutor(); // one thread for db ops
    private final Executor networkIO = Executors.newFixedThreadPool(3); // 3 threads for api calls
    private final Executor mainThread = new MainThreadExecutor(); // runs stuff on ui thread

    public Executor diskIO() { return diskIO; }
    public Executor networkIO() { return networkIO; }
    public Executor mainThread() { return mainThread; }

    // custom executor that posts tasks to main thread (for updating ui
    private static class MainThreadExecutor implements Executor {
        private final Handler handler = new Handler(Looper.getMainLooper());
        @Override public void execute(Runnable command) { handler.post(command); }
    }
}
