package hr.cizmic.seebanking.data.remote.dto;

// login request (send phone number + password to get authenticated
public class LoginRequestDto {
    public String mobileNumber;  // user's phone number
    public String password;      // user's password

    public LoginRequestDto(String mobileNumber, String password) {
        this.mobileNumber = mobileNumber;
        this.password = password;
    }
}
