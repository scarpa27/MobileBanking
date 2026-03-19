package hr.cizmic.seebanking.util;

// callback interface for receiving async results
public interface ResultCallback<T> {
    void onResult(Result<T> result); // called when operation completes (success or failure)
}
