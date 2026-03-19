package hr.cizmic.seebanking.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import hr.cizmic.seebanking.databinding.ActivityTransferBinding;
import hr.cizmic.seebanking.sync.SyncScheduler;
import hr.cizmic.seebanking.util.DecimalDigitsInputFilter;
import hr.cizmic.seebanking.viewmodel.AppViewModelFactory;
import hr.cizmic.seebanking.viewmodel.TransferViewModel;

// Screen for sending money to another account
public class TransferActivity extends BaseActivity {

    private ActivityTransferBinding binding;
    private TransferViewModel vm;

    private long fromAccountId;

    // Set up transfer form and send button
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTransferBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get source account from intent - must be valid
        fromAccountId = getIntent().getLongExtra(AccountsActivity.EXTRA_ACCOUNT_ID, -1L);
        if (fromAccountId <= 0) {
            Toast.makeText(this, "Missing fromAccountId", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize view model
        vm = new ViewModelProvider(this, new AppViewModelFactory()).get(TransferViewModel.class);

        // Show which account we're sending from
        binding.tvFromAccount.setText("From account ID: " + fromAccountId);
        // Limit amount to 2 decimal places
        binding.etAmount.setFilters(new android.text.InputFilter[]{
                new DecimalDigitsInputFilter(2)
        });

        // Send button - collect all form data and call transfer
        binding.btnSend.setOnClickListener(v ->
                vm.transfer(
                        fromAccountId,
                        txt(binding.etToMobile),
                        txt(binding.etToIban),
                        txt(binding.etAmount),
                        txt(binding.etNote)
                )
        );

        // Show progress spinner and disable button while sending
        vm.loading().observe(this, l -> {
            binding.progress.setVisibility(Boolean.TRUE.equals(l) ? android.view.View.VISIBLE : android.view.View.GONE);
            binding.btnSend.setEnabled(l == null || !l);
        });

        // Show error or validation messages
        vm.message().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        // When transfer succeeds, sync data and close screen
        vm.sent().observe(this, ok -> {
            if (Boolean.TRUE.equals(ok)) {
                Toast.makeText(this, "Transfer sent", Toast.LENGTH_SHORT).show();
                SyncScheduler.enqueueOneTimeSync(this);
                finish();
            }
        });
    }

    // Get text from input field
    private static String txt(android.widget.EditText et) {
        return et.getText() == null ? null : et.getText().toString();
    }
}

