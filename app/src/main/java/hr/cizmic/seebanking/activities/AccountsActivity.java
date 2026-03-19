package hr.cizmic.seebanking.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;

import hr.cizmic.seebanking.ServiceLocator;
import hr.cizmic.seebanking.data.local.entity.AccountEntity;
import hr.cizmic.seebanking.databinding.ActivityAccountsBinding;
import hr.cizmic.seebanking.ui.adapter.AccountAdapter;
import hr.cizmic.seebanking.ui.adapter.TransactionAdapter;
import hr.cizmic.seebanking.viewmodel.AccountsViewModel;
import hr.cizmic.seebanking.viewmodel.AppViewModelFactory;
import hr.cizmic.seebanking.viewmodel.TransactionsViewModel;

// Main screen after login. Accounts view with swipe-cards and transactions
public class AccountsActivity extends BaseActivity {

    public static final String EXTRA_ACCOUNT_ID = "account_id";

    private ActivityAccountsBinding binding;
    private AccountsViewModel accountsVm;
    private TransactionsViewModel txVm;

    private AccountAdapter accountAdapter;
    private TransactionAdapter txAdapter;

    private Long selectedAccountId = null;
    private String selectedAccountName = null;
    private int currentPage = 0;

    // Setup UI with ViewPager for accounts and RecyclerView for transactions
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAccountsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // VMs: one for accounts one for transactions
        accountsVm = new ViewModelProvider(this, new AppViewModelFactory()).get(AccountsViewModel.class);
        txVm = new ViewModelProvider(this, new AppViewModelFactory()).get(TransactionsViewModel.class);

        // Setup ViewPager for swiping between account cards
        accountAdapter = new AccountAdapter(null);
        binding.pagerAccounts.setAdapter(accountAdapter);
        binding.pagerAccounts.setOffscreenPageLimit(1);

        // on acc swipe (change acc), load its' transactions
        binding.pagerAccounts.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                // prevent skipping multiple pages at once :)
                if (Math.abs(position - currentPage) > 1) {
                    int target = currentPage + (position > currentPage ? 1 : -1);
                    binding.pagerAccounts.setCurrentItem(target, false);
                    return;
                }
                currentPage = position;
                AccountEntity account = accountAdapter.getItem(position);
                selectAccount(account, true);
            }
        });

        // transaction list setup
        txAdapter = new TransactionAdapter();
        binding.recyclerTransactions.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerTransactions.setAdapter(txAdapter);

        // Refresh button - reload accounts and transactions from API
        binding.btnRefresh.setOnClickListener(v -> {
            accountsVm.refresh();
            if (selectedAccountId != null) txVm.refresh();
        });

        // Logout button - clear token and go to login
        binding.btnLogout.setOnClickListener(v -> {
            ServiceLocator.get().authRepository().logout();
            startActivity(new android.content.Intent(this, LoginActivity.class));
            finish();
        });

        // Create transaction button
        binding.btnNavCreate.setOnClickListener(v -> openCreateTransaction());

        // Update accounts list when data changes
        accountsVm.accounts().observe(this, accounts -> {
            accountAdapter.submit(accounts);
            ensureSelection(accounts);
        });

        // Update transactions list when data changes
        txVm.transactions().observe(this, txs -> txAdapter.submit(txs));

        // Show/hide progress spinner
        accountsVm.refreshing().observe(this, r -> {
            binding.btnRefresh.setEnabled(r == null || !r);
            updateProgress();
        });

        // Show error messages
        accountsVm.message().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        // Show/hide progress spinner for transactions
        txVm.loading().observe(this, l -> updateProgress());

        // Show/hide "Load More" button for pagination
        txVm.hasMore().observe(this, hasMore -> {
            boolean show = Boolean.TRUE.equals(hasMore);
            binding.btnLoadMore.setEnabled(show);
            binding.btnLoadMore.setAlpha(show ? 1f : 0.5f);
            binding.btnLoadMore.setVisibility(show ? android.view.View.VISIBLE : android.view.View.GONE);
        });

        // Show error messages for transactions
        txVm.message().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        // Load more button - get next page of transactions
        binding.btnLoadMore.setOnClickListener(v -> txVm.loadMore());

        // Load initial data
        accountsVm.refresh();
    }

    // Open create transaction screen with selected account
    private void openCreateTransaction() {
        if (selectedAccountId == null) {
            Toast.makeText(this, "Select an account first", Toast.LENGTH_SHORT).show();
            return;
        }
        android.content.Intent i = new android.content.Intent(this, CreateTransactionActivity.class);
        i.putExtra(CreateTransactionActivity.EXTRA_ACCOUNT_ID, selectedAccountId);
        i.putExtra(CreateTransactionActivity.EXTRA_ACCOUNT_NAME, selectedAccountName);
        startActivity(i);
    }

    // Make sure an account is selected when list changes
    private void ensureSelection(List<AccountEntity> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            selectedAccountId = null;
            selectedAccountName = null;
            txAdapter.submit(java.util.Collections.emptyList());
            return;
        }

        // Find previously selected account or use first one
        int index = 0;
        if (selectedAccountId != null) {
            for (int i = 0; i < accounts.size(); i++) {
                if (accounts.get(i).id.equals(selectedAccountId)) {
                    index = i;
                    break;
                }
            }
        }

        binding.pagerAccounts.setCurrentItem(index, false);
        currentPage = index;
        selectAccount(accounts.get(index), selectedAccountId == null);
    }

    // Set account as selected and load its transactions
    private void selectAccount(AccountEntity account, boolean forceRefresh) {
        if (account == null) return;
        boolean changed = selectedAccountId == null || !selectedAccountId.equals(account.id);
        selectedAccountId = account.id;
        selectedAccountName = account.name;
        txVm.setAccountId(account.id);
        if (forceRefresh || changed) {
            txVm.refresh();
        }
    }

    // Show progress bar if either accounts or transactions are loading
    private void updateProgress() {
        Boolean accRefreshing = accountsVm.refreshing().getValue();
        Boolean txLoading = txVm.loading().getValue();
        boolean show = Boolean.TRUE.equals(accRefreshing) || Boolean.TRUE.equals(txLoading);
        binding.progress.setVisibility(show ? android.view.View.VISIBLE : android.view.View.GONE);
    }
}

