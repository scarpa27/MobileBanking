package hr.cizmic.seebanking.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import hr.cizmic.seebanking.data.local.entity.AccountEntity;
import hr.cizmic.seebanking.databinding.ActivityCreateTransactionBinding;
import hr.cizmic.seebanking.util.DecimalDigitsInputFilter;
import hr.cizmic.seebanking.viewmodel.AppViewModelFactory;
import hr.cizmic.seebanking.viewmodel.AccountsViewModel;
import hr.cizmic.seebanking.viewmodel.CreateTransactionViewModel;

// Screen for creating new transaction - internal or external
public class CreateTransactionActivity extends BaseActivity {

    public static final String EXTRA_ACCOUNT_ID = "account_id";
    public static final String EXTRA_ACCOUNT_NAME = "account_name";

    private ActivityCreateTransactionBinding binding;
    private CreateTransactionViewModel vm;
    private AccountsViewModel accountsVm;

    private long preselectedAccountId;
    private final List<AccountEntity> accounts = new ArrayList<>();

    // Set up transaction form with account selection
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCreateTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get preselected account if passed from previous screen
        preselectedAccountId = getIntent().getLongExtra(EXTRA_ACCOUNT_ID, -1L);

        // Initialize view models
        vm = new ViewModelProvider(this, new AppViewModelFactory()).get(CreateTransactionViewModel.class);
        accountsVm = new ViewModelProvider(this, new AppViewModelFactory()).get(AccountsViewModel.class);
        // Limit amount to 2 decimal places
        binding.etAmount.setFilters(new android.text.InputFilter[]{
                new DecimalDigitsInputFilter(2)
        });

        // load all accounts and populate spinner dropdowns
        accountsVm.accounts().observe(this, list -> {
            accounts.clear();
            if (list != null) accounts.addAll(list);
            updateAccountSpinners();
        });
        accountsVm.refresh();

        // Toggle internal and external transfer mode
        binding.switchInternal.setOnCheckedChangeListener((buttonView, isChecked) -> toggleInternal(isChecked));
        toggleInternal(binding.switchInternal.isChecked());

        // validate and send transaction
        binding.btnCreate.setOnClickListener(v -> {
            Long fromId = selectedFromAccountId();
            //  check that source and destination are different
            if (binding.switchInternal.isChecked()) {
                Long toId = selectedToAccountId();
                if (fromId != null && toId != null && fromId.equals(toId)) {
                    Toast.makeText(this, "Pick a different destination account", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            // send transaction with internal or external recipient details
            vm.create(
                    fromId,
                    txt(binding.etAmount),
                    binding.switchInternal.isChecked() ? null : txt(binding.etRecipientMobile),
                    binding.switchInternal.isChecked() ? selectedToAccountIban() : txt(binding.etRecipientIban),
                    binding.switchInternal.isChecked() ? null : txt(binding.etCounterpartyName),
                    txt(binding.etNote)
            );
        });

        // Back to accounts button
        binding.btnNavAccounts.setOnClickListener(v -> {
            finish();
        });

        // Show progress spinner and disable button while creating
        vm.loading().observe(this, l -> {
            binding.progress.setVisibility(Boolean.TRUE.equals(l) ? android.view.View.VISIBLE : android.view.View.GONE);
            binding.btnCreate.setEnabled(l == null || !l);
        });

        // Show error or validation messages
        vm.message().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        // When transaction created successfully  close screen
        vm.created().observe(this, ok -> {
            if (Boolean.TRUE.equals(ok)) {
                Toast.makeText(this, "Transaction created", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    // Build account list for spinner dropdowns with name, currency, and IBAN
    private void updateAccountSpinners() {
        List<String> labels = new ArrayList<>();
        for (AccountEntity a : accounts) {
            String name = (a.name == null || a.name.trim().isEmpty()) ? ("Account " + a.id) : a.name;
            String iban = a.iban == null ? "" : a.iban;
            String cur = a.currency == null ? "" : a.currency;
            labels.add(name + " (" + cur + ") " + iban);
        }

        // Set up both spinners with same account list
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                labels
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spFromAccount.setAdapter(adapter);
        binding.spToAccount.setAdapter(adapter);

        // Select preselected account in from spinner
        int preselect = indexOfAccount(preselectedAccountId);
        if (preselect >= 0) {
            binding.spFromAccount.setSelection(preselect);
        }
        // Auto-select different account in to spinner
        if (accounts.size() > 1) {
            int toIndex = preselect >= 0 ? ((preselect + 1) % accounts.size()) : 0;
            binding.spToAccount.setSelection(toIndex);
        }
    }

    // Find position of account in list by ID
    private int indexOfAccount(long accountId) {
        if (accountId <= 0) return -1;
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).id != null && accounts.get(i).id == accountId) {
                return i;
            }
        }
        return -1;
    }

    // Get ID of selected from account
    private Long selectedFromAccountId() {
        int idx = binding.spFromAccount.getSelectedItemPosition();
        if (idx < 0 || idx >= accounts.size()) return null;
        AccountEntity a = accounts.get(idx);
        return a.id;
    }

    // Get IBAN of selected to account
    private String selectedToAccountIban() {
        int idx = binding.spToAccount.getSelectedItemPosition();
        if (idx < 0 || idx >= accounts.size()) return null;
        AccountEntity a = accounts.get(idx);
        return a.iban;
    }

    // Get ID of selected to account
    private Long selectedToAccountId() {
        int idx = binding.spToAccount.getSelectedItemPosition();
        if (idx < 0 || idx >= accounts.size()) return null;
        AccountEntity a = accounts.get(idx);
        return a.id;
    }

    // Show/hide fields based on internal vs external transfer
    private void toggleInternal(boolean internal) {
        int internalVisibility = internal ? android.view.View.VISIBLE : android.view.View.GONE;
        int externalVisibility = internal ? android.view.View.GONE : android.view.View.VISIBLE;

        // Internal transfer - show account spinner
        binding.tvToAccountLabel.setVisibility(internalVisibility);
        binding.spToAccount.setVisibility(internalVisibility);

        // External transfer - show recipient fields
        binding.etRecipientMobile.setVisibility(externalVisibility);
        binding.etRecipientIban.setVisibility(externalVisibility);
        binding.etCounterpartyName.setVisibility(externalVisibility);
    }

    // Get text from input field
    private static String txt(android.widget.EditText et) {
        return et.getText() == null ? null : et.getText().toString();
    }
}

