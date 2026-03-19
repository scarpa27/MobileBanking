package hr.cizmic.seebanking.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import hr.cizmic.seebanking.data.repository.AuthRepository;
import hr.cizmic.seebanking.security.TokenStore;
import hr.cizmic.seebanking.util.AppExecutors;
import hr.cizmic.seebanking.util.Result;
import hr.cizmic.seebanking.util.SingleLiveEvent;
import hr.cizmic.seebanking.util.ValidationUtils;

// handles login, registration, and logout with validation
public class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final TokenStore tokenStore;
    private final AppExecutors executors;

    // shows spinner during login/register
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    // displays error messages to user (only fires once per event
    private final SingleLiveEvent<String> message = new SingleLiveEvent<>();

    // triggers navigation after successful login/register
    private final SingleLiveEvent<Boolean> authSuccess = new SingleLiveEvent<>();

    public AuthViewModel(AuthRepository authRepository, TokenStore tokenStore, AppExecutors executors) {
        this.authRepository = authRepository;
        this.tokenStore = tokenStore;
        this.executors = executors;
    }

    public LiveData<Boolean> loading() { return loading; }
    public LiveData<String> message() { return message; }
    public LiveData<Boolean> authSuccess() { return authSuccess; }

    // check if user has valid token
    public boolean isLoggedIn() {
        return authRepository.isLoggedIn();
    }

    // login user with phone and password
    public void login(String mobile, String pass) {
        String mobileNorm = ValidationUtils.normalizePhone(mobile);
        String passNorm = ValidationUtils.trimToNull(pass);

        // phone must be 6-20 digits
        if (mobileNorm == null) {
            message.setValue("Mobile number is required");
            return;
        }
        if (!ValidationUtils.isValidPhone(mobileNorm)) {
            message.setValue("Mobile number must be 6-20 digits (optionally +)");
            return;
        }
        // password must be 6-64 characters
        if (passNorm == null) {
            message.setValue("Password is required");
            return;
        }
        if (passNorm.length() < 6 || passNorm.length() > 64) {
            message.setValue("Password must be 6-64 characters");
            return;
        }

        // call api on bg thread
        loading.setValue(true);
        authRepository.login(mobileNorm, passNorm, r ->
                executors.mainThread().execute(() -> {
                    loading.setValue(false);
                    if (r.success) {
                        authSuccess.setValue(true);
                    } else {
                        message.setValue(r.error);
                    }
                })
        );
    }

    // register new user with validation
    public void register(String firstName, String lastName, String mobile, String pass, String pass2, String email) {
        String firstNameNorm = ValidationUtils.trimToNull(firstName);
        String lastNameNorm = ValidationUtils.trimToNull(lastName);
        String mobileNorm = ValidationUtils.normalizePhone(mobile);
        String passNorm = ValidationUtils.trimToNull(pass);
        String pass2Norm = ValidationUtils.trimToNull(pass2);
        String emailNorm = ValidationUtils.normalizeEmail(email);

        // names are required
        if (firstNameNorm == null) {
            message.setValue("First name is required");
            return;
        }
        if (lastNameNorm == null) {
            message.setValue("Last name is required");
            return;
        }
        // phone must be 6-20 digits
        if (mobileNorm == null) {
            message.setValue("Mobile number is required");
            return;
        }
        if (!ValidationUtils.isValidPhone(mobileNorm)) {
            message.setValue("Mobile number must be 6-20 digits (optionally +)");
            return;
        }
        // password must be 6-64 characters
        if (passNorm == null) {
            message.setValue("Password is required");
            return;
        }
        if (passNorm.length() < 6 || passNorm.length() > 64) {
            message.setValue("Password must be 6-64 characters");
            return;
        }
        // passwords must match
        if (pass2Norm == null || !passNorm.equals(pass2Norm)) {
            message.setValue("Passwords do not match");
            return;
        }
        // email max 120 characters
        if (emailNorm == null || !ValidationUtils.isValidEmail(emailNorm)) {
            message.setValue("Valid email is required");
            return;
        }
        // full name max 80 characters
        if ((firstNameNorm + " " + lastNameNorm).length() > 80) {
            message.setValue("Full name must be at most 80 characters");
            return;
        }

        // call api on bg thread
        loading.setValue(true);

        authRepository.register(firstNameNorm, lastNameNorm, mobileNorm, passNorm, emailNorm, r ->
                executors.mainThread().execute(() -> {
                    loading.setValue(false);
                    if (r.success) {
                        authSuccess.setValue(true);
                    } else {
                        message.setValue(r.error);
                    }
                })
        );
    }

    // clear tokens and log out
    public void logout() {
        authRepository.logout();
        authSuccess.setValue(false);
    }
}
