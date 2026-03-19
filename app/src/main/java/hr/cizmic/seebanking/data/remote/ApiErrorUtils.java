package hr.cizmic.seebanking.data.remote;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import hr.cizmic.seebanking.data.remote.dto.ApiErrorDto;
import okhttp3.ResponseBody;
import retrofit2.Response;

// helper to extract error messages from api responses
public final class ApiErrorUtils {

    private static final Gson GSON = new GsonBuilder().create();

    private ApiErrorUtils() {}

    // gets error message from response, or returns fallback with status code
    public static String messageOrFallback(Response<?> response, String fallback) {
        String msg = messageFrom(response);
        if (msg != null) return msg;
        if (response != null) return fallback + " (" + response.code() + ")";
        return fallback;
    }

    // tries to parse error message from response body (checks ApiErrorDto first, then raw string
    public static String messageFrom(Response<?> response) {
        if (response == null) return null;

        ResponseBody body = response.errorBody();
        if (body == null) return null;

        // read error body as string
        String raw;
        try {
            raw = body.string();
        } catch (IOException ex) {
            return null;
        }

        if (raw == null) return null;
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) return null;

        // try to parse as ApiErrorDto to get message field
        try {
            ApiErrorDto dto = GSON.fromJson(trimmed, ApiErrorDto.class);
            if (dto != null) {
                if (dto.message != null && !dto.message.trim().isEmpty()) {
                    return dto.message.trim();
                }
                if (dto.error != null && !dto.error.trim().isEmpty()) {
                    return dto.error.trim();
                }
            }
        } catch (JsonSyntaxException ignored) {
            // fall through to raw string
        }

        // if parsing fails, just return the raw error text
        return trimmed;
    }
}
