package hr.cizmic.seebanking.data.remote;

import hr.cizmic.seebanking.data.remote.dto.AuthResponseDto;
import hr.cizmic.seebanking.data.remote.dto.LoginRequestDto;
import hr.cizmic.seebanking.data.remote.dto.RegisterRequestDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

// auth api endpoints
public interface AuthApi {

    // POST /api/auth/login (send phone + password, get back token and user info
    @POST("api/auth/login")
    Call<AuthResponseDto> login(@Body LoginRequestDto req);

    // POST /api/auth/register (create new acc with user details
    @POST("api/auth/register")
    Call<AuthResponseDto> register(@Body RegisterRequestDto req);
}
