package hr.cizmic.seebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import hr.cizmic.seebanking.databinding.ActivityRegisterBinding;
import hr.cizmic.seebanking.viewmodel.AppViewModelFactory;
import hr.cizmic.seebanking.viewmodel.AuthViewModel;

// registration screen (create new acc)
public class RegisterActivity extends BaseActivity {

    private ActivityRegisterBinding binding;
    private AuthViewModel vm;

    // setup ui and connect to vm
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setup view binding
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // get vm for registration logic
        vm = new ViewModelProvider(this, new AppViewModelFactory()).get(AuthViewModel.class);

        // register btn - send all form fields to vm for validation
        binding.btnRegister.setOnClickListener(v -> vm.register(
                txt(binding.etFirstName),
                txt(binding.etLastName),
                txt(binding.etMobile),
                txt(binding.etPassword),
                txt(binding.etPassword2),
                txt(binding.etEmail)
        ));

        // login btn - go to login screen
        binding.btnGoLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        // show/hide spinner
        vm.loading().observe(this, isLoading -> {
            binding.btnRegister.setEnabled(isLoading == null || !isLoading);
            binding.progress.setVisibility(Boolean.TRUE.equals(isLoading) ? android.view.View.VISIBLE : android.view.View.GONE);
        });

        // show validation errors or registration errors
        vm.message().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        // go to main screen when registration succeeds
        vm.authSuccess().observe(this, ok -> {
            if (Boolean.TRUE.equals(ok)) {
                startActivity(new Intent(this, AccountsActivity.class));
                finish();
            }
        });
    }

    // helper to get text from EditText
    private static String txt(android.widget.EditText et) {
        return et.getText() == null ? null : et.getText().toString();
    }
}

