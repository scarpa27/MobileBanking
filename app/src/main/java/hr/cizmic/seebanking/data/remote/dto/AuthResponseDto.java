package hr.cizmic.seebanking.data.remote.dto;

// data from server when login/register succeeds (includes token and user info
public class AuthResponseDto {
    public String tokenType;        // usually "Bearer"
    public String accessToken;      // jwt token for auth
    public long expiresInSeconds;   // how long token is valid
    public String userId;           // user's unique id
    public String fullName;         // user's full name
    public String email;            // user's email
    public String mobileNumber;     // user's phone number
}
