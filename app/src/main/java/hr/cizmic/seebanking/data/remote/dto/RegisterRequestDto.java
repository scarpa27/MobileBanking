package hr.cizmic.seebanking.data.remote.dto;

// registration request (all info needed to create new user acc
public class RegisterRequestDto {
    public String fullName;      // combined first + last name
    public String mobileNumber;  // user's phone number (used for login
    public String password;      // user's password

    public String email;         // user's email (optional

    // constructor combines firstName and lastName into fullName
    public RegisterRequestDto(String firstName, String lastName, String mobileNumber, String password, String email) {
        this.fullName = firstName + " " + lastName;
        this.mobileNumber = mobileNumber;
        this.password = password;
        this.email = email;
    }
}
