package hr.cizmic.seebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import hr.cizmic.seebanking.databinding.ActivityLoginBinding;
import hr.cizmic.seebanking.viewmodel.AppViewModelFactory;
import hr.cizmic.seebanking.viewmodel.AuthViewModel;

// login screen (phone + password)
public class LoginActivity extends BaseActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel vm;

    // setup ui and connect to vm
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setup view binding
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // get vm for login logic
        vm = new ViewModelProvider(this, new AppViewModelFactory()).get(AuthViewModel.class);

        // login btn - send phone + password to vm for validation
        binding.btnLogin.setOnClickListener(v ->
                vm.login(
                        binding.etMobile.getText() != null ? binding.etMobile.getText().toString() : null,
                        binding.etPassword.getText() != null ? binding.etPassword.getText().toString() : null
                )
        );

        // register btn - go to register screen
        binding.btnGoRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });

        // show/hide spinner
        vm.loading().observe(this, isLoading -> {
            binding.btnLogin.setEnabled(isLoading == null || !isLoading);
            binding.progress.setVisibility(Boolean.TRUE.equals(isLoading) ? android.view.View.VISIBLE : android.view.View.GONE);
        });

        // show error or info messages
        vm.message().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        // go to main screen when login succeeds
        vm.authSuccess().observe(this, ok -> {
            if (Boolean.TRUE.equals(ok)) {
                startActivity(new Intent(this, AccountsActivity.class));
                finish();
            }
        });
    }
}

