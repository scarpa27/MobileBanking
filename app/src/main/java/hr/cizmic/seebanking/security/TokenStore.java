package hr.cizmic.seebanking.security;

// interface for saving and loading jwt token and user id
public interface TokenStore {
    String getToken(); // get saved auth token
    void setToken(String token); // save auth token
    void clear(); // delete both token and user id on logout

    String getUserId(); // get saved user id
    void setUserId(String userId); // save user id
}
