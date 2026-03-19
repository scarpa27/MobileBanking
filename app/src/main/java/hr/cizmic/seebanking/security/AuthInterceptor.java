package hr.cizmic.seebanking.security;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

// intercepts every api request and adds the "Authorization: Bearer <token>" header automatically
public final class AuthInterceptor implements Interceptor {

    private final TokenStore tokenStore;

    public AuthInterceptor(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        String token = tokenStore.getToken();

        // if no token saved, just send request as-is (login/register don't need auth
        if (token == null || token.trim().isEmpty()) {
            return chain.proceed(original);
        }

        // add the bearer token to the authorization header
        Request authed = original.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();

        return chain.proceed(authed); // send the modified request
    }
}
