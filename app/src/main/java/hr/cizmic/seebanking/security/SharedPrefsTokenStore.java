package hr.cizmic.seebanking.security;

import android.content.Context;
import android.content.SharedPreferences;

// saves auth token and user id in shared prefs so they survive app restarts
public final class SharedPrefsTokenStore implements TokenStore {

    private static final String PREF = "auth"; // name of prefs file
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";

    private final SharedPreferences prefs;

    public SharedPrefsTokenStore(Context context) {
        // use app context to avoid memory leaks
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    @Override
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null); // null if not set
    }

    @Override
    public void setToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply(); // async save
    }

    @Override
    public void clear() {
        prefs.edit().remove(KEY_TOKEN).remove(KEY_USER_ID).apply(); // logout = remove both
    }

    @Override
    public String getUserId() {
        if (!prefs.contains(KEY_USER_ID)) return null; // check if key exists first
        return prefs.getString(KEY_USER_ID, null);
    }

    @Override
    public void setUserId(String userId) {
        if (userId == null) {
            prefs.edit().remove(KEY_USER_ID).apply(); // null = remove
        } else {
            prefs.edit().putString(KEY_USER_ID, userId).apply(); // save it
        }
    }
}
