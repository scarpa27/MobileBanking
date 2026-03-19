package hr.cizmic.seebanking.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// stores user profile info (name, phone, email
@Entity(tableName = "users")
public class UserEntity {
    @PrimaryKey
    @NonNull
    public String id; // unique user id

    public String fullName; // combined first + last name
    public String mobileNumber; // phone number for login
    public String email; // user's email address

    public UserEntity(@NonNull String id, String firstName, String lastName, String mobileNumber, String email) {
        this.id = id;
        this.fullName = firstName + " " + lastName;
        this.mobileNumber = mobileNumber;
        this.email = email;
    }

    public UserEntity(@NonNull String id, String fullName, String mobileNumber, String email) {
        this.id = id;
        this.fullName = fullName;
        this.mobileNumber = mobileNumber;
        this.email = email;
    }
}
