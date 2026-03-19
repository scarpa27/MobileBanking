package hr.cizmic.seebanking.data.remote;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import hr.cizmic.seebanking.BuildConfig;
import hr.cizmic.seebanking.security.AuthInterceptor;
import hr.cizmic.seebanking.security.TokenStore;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// retrofit setup with auth interceptor and logging
public final class ApiClient {

    private ApiClient() {}

    // creates retrofit instance with auth headers and json converter
    public static Retrofit createRetrofit(TokenStore tokenStore) {
        // gson setup (makes sure nulls get sent in json
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .create();

        // okhttp client with auth interceptor to add token to requests
        OkHttpClient.Builder ok = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(tokenStore));

        // add logging in debug mode to see what's being sent/received
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor log = new HttpLoggingInterceptor();
            log.setLevel(HttpLoggingInterceptor.Level.BODY);
            ok.addInterceptor(log);
        }

        // build retrofit with base url and json converter
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .client(ok.build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }
}
