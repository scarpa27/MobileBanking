package hr.cizmic.seebanking.util;

import androidx.annotation.Nullable;

// wrapper for async operations that can succeed or fail
public class Result<T> {
    public final boolean success; // true if operation succeeded
    @Nullable public final T data; // result if successful
    @Nullable public final String error; // error message if failed

    private Result(boolean success, @Nullable T data, @Nullable String error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    // creates successful result with data
    public static <T> Result<T> ok(T data) {
        return new Result<>(true, data, null);
    }

    // creates failed result with error message
    public static <T> Result<T> fail(String error) {
        return new Result<>(false, null, error);
    }
}
