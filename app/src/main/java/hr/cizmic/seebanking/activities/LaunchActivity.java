package hr.cizmic.seebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import hr.cizmic.seebanking.ServiceLocator;

// first screen (checks if user logged in and redirects to login or accs)
public class LaunchActivity extends BaseActivity {

    // check login status and go to right screen
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check if user has saved login token
        boolean loggedIn = ServiceLocator.get().authRepository().isLoggedIn();

        if (loggedIn) {
            // already logged in - go to main screen
            startActivity(new Intent(this, AccountsActivity.class));
        } else {
            // not logged in - go to login screen
            startActivity(new Intent(this, LoginActivity.class));
        }
        // close this screen so back doesn't come back here
        finish();
    }
}

