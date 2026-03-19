package hr.cizmic.seebanking.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import hr.cizmic.seebanking.data.local.AppDatabase;
import hr.cizmic.seebanking.data.mapper.DtoMappers;
import hr.cizmic.seebanking.data.remote.ApiErrorUtils;
import hr.cizmic.seebanking.data.remote.AuthApi;
import hr.cizmic.seebanking.data.remote.dto.AuthResponseDto;
import hr.cizmic.seebanking.data.remote.dto.LoginRequestDto;
import hr.cizmic.seebanking.data.remote.dto.RegisterRequestDto;
import hr.cizmic.seebanking.security.TokenStore;
import hr.cizmic.seebanking.util.AppExecutors;
import hr.cizmic.seebanking.util.Result;
import hr.cizmic.seebanking.util.ResultCallback;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// handles login, registration, and logout
// talks to api and saves user info + tokens to db
public final class AuthRepository {

    private final AuthApi authApi;
    private final AppDatabase db;
    private final TokenStore tokenStore;
    private final AppExecutors executors;

    public AuthRepository(AuthApi authApi, AppDatabase db, TokenStore tokenStore, AppExecutors executors) {
        this.authApi = authApi;
        this.db = db;
        this.tokenStore = tokenStore;
        this.executors = executors;
    }

    // check if user is logged in by looking for token in storage
    public boolean isLoggedIn() {
        String t = tokenStore.getToken();
        return t != null && !t.trim().isEmpty();
    }

    // call login api and save user + token to db
    // runs on bg thread for both network and db operations
    public void login(String mobileNumber, String password, ResultCallback<Void> cb) {
        authApi.login(new LoginRequestDto(mobileNumber, password))
                .enqueue(new Callback<AuthResponseDto>() {
                    @Override
                    public void onResponse(@NonNull Call<AuthResponseDto> call, @NonNull Response<AuthResponseDto> response) {
                        if (!response.isSuccessful() || response.body() == null || response.body().accessToken == null) {
                            cb.onResult(Result.fail(ApiErrorUtils.messageOrFallback(response, "Login failed")));
                            Log.e("login", "onResponse: %d".formatted(response.code()));
                            return;
                        }

                        AuthResponseDto body = response.body();
                        // save token to TokenStore
                        tokenStore.setToken(body.accessToken);
                        if (body.userId != null) {
                            tokenStore.setUserId(body.userId);
                        }

                        // save user data to db on bg thread
                        executors.diskIO().execute(() -> {
                            if (body.userId != null) {
                                db.userDao().upsert(DtoMappers.toEntity(body.userId, body.fullName, body.email, body.mobileNumber));
                            }
                            cb.onResult(Result.ok(null));
                        });
                    }

                    @Override
                    public void onFailure(@NonNull Call<AuthResponseDto> call, @NonNull Throwable t) {
                        cb.onResult(Result.fail("Network error: " + t.getMessage()));
                    }
                });
    }

    // call register api and save user + token to db
    // same as login but sends different data to api
    public void register(String firstName, String lastName, String mobileNumber, String password, String email, ResultCallback<Void> cb) {
        authApi.register(new RegisterRequestDto(firstName, lastName, mobileNumber, password, email))
                .enqueue(new Callback<AuthResponseDto>() {
                    @Override
                    public void onResponse(@NonNull Call<AuthResponseDto> call, @NonNull Response<AuthResponseDto> response) {
                        if (!response.isSuccessful() || response.body() == null || response.body().accessToken == null) {
                            cb.onResult(Result.fail(ApiErrorUtils.messageOrFallback(response, "Register failed")));
                            return;
                        }

                        AuthResponseDto body = response.body();
                        // save token to TokenStore
                        tokenStore.setToken(body.accessToken);
                        if (body.userId != null) {
                            tokenStore.setUserId(body.userId);
                        }

                        // save user data to db on bg thread
                        executors.diskIO().execute(() -> {
                            if (body.userId != null) {
                                db.userDao().upsert(DtoMappers.toEntity(body.userId, body.fullName, body.email, body.mobileNumber));
                            }
                            cb.onResult(Result.ok(null));
                        });
                    }

                    @Override
                    public void onFailure(@NonNull Call<AuthResponseDto> call, @NonNull Throwable t) {
                        cb.onResult(Result.fail("Network error: " + t.getMessage()));
                    }
                });
    }

    // clear user data from db and remove token
    // runs on bg thread to avoid blocking ui
    public void logout() {
        executors.diskIO().execute(() -> {
            String userId = tokenStore.getUserId();
            if (userId != null) {
                // delete all accs from db
                db.accountDao().deleteForUser(userId);
                // delete all tx, keys, etc. if needed
            }
            // remove user from db
            db.userDao().deleteAll();
            // clear token from storage
            tokenStore.clear();
        });
    }
}
