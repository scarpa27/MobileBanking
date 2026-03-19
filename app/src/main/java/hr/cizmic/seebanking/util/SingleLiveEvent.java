package hr.cizmic.seebanking.util;

import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.concurrent.atomic.AtomicBoolean;

// like livedata but only triggers once per event (no re-triggering on rotation
// used for one-time events like showing toasts or navigation
public class SingleLiveEvent<T> extends MutableLiveData<T> {

    private final AtomicBoolean pending = new AtomicBoolean(false); // flag to track if there's new event

    @MainThread
    @Override
    public void observe(LifecycleOwner owner, final Observer<? super T> observer) {
        super.observe(owner, t -> {
            // only notify observer if pending is true, then reset it to false
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(t);
            }
        });
    }

    @MainThread
    @Override
    public void setValue(@Nullable T t) {
        pending.set(true); // mark that there's new event
        super.setValue(t);
    }

    @Override
    public void postValue(T value) {
        pending.set(true); // mark that there's new event
        super.postValue(value);
    }
}
