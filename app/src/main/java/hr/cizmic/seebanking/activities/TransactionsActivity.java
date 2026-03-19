package hr.cizmic.seebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import hr.cizmic.seebanking.databinding.ActivityTransactionsBinding;
import hr.cizmic.seebanking.ui.adapter.TransactionAdapter;
import hr.cizmic.seebanking.viewmodel.AppViewModelFactory;
import hr.cizmic.seebanking.viewmodel.TransactionsViewModel;

// shows transaction list for single acc
public class TransactionsActivity extends BaseActivity {

    private ActivityTransactionsBinding binding;
    private TransactionsViewModel vm;

    private TransactionAdapter adapter;

    private long accountId;

    // setup screen and load tx list
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTransactionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // get acc id from intent
        accountId = getIntent().getLongExtra(AccountsActivity.EXTRA_ACCOUNT_ID, -1L);
        String name = getIntent().getStringExtra("account_name");
        if (name != null) setTitle(name);

        // setup recycler view with tx adapter
        adapter = new TransactionAdapter();
        binding.recyclerTransactions.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerTransactions.setAdapter(adapter);

        // init vm
        vm = new ViewModelProvider(this, new AppViewModelFactory()).get(TransactionsViewModel.class);
        vm.setAccountId(accountId);

        // btn clicks (refresh, load more, send money
        binding.btnRefresh.setOnClickListener(v -> vm.refresh());
        binding.btnLoadMore.setOnClickListener(v -> vm.loadMore());
        binding.btnSendMoney.setOnClickListener(v -> {
            Intent i = new Intent(this, TransferActivity.class);
            i.putExtra(AccountsActivity.EXTRA_ACCOUNT_ID, accountId);
            startActivity(i);
        });

        // update list when tx change
        vm.transactions().observe(this, txs -> adapter.submit(txs));

        // show/hide spinner and disable buttons while loading
        vm.loading().observe(this, l -> {
            binding.progress.setVisibility(Boolean.TRUE.equals(l) ? android.view.View.VISIBLE : android.view.View.GONE);
            binding.btnRefresh.setEnabled(l == null || !l);
            binding.btnLoadMore.setEnabled(l == null || !l);
        });

        // show/hide load more btn if there are more tx
        vm.hasMore().observe(this, hasMore -> {
            boolean show = Boolean.TRUE.equals(hasMore);
            binding.btnLoadMore.setEnabled(show);
            binding.btnLoadMore.setAlpha(show ? 1f : 0.5f);
            binding.btnLoadMore.setVisibility(show ? android.view.View.VISIBLE : android.view.View.GONE);
        });

        // show error or success messages
        vm.message().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        // load initial data
        vm.refresh();
    }
}

