package hr.cizmic.seebanking.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import hr.cizmic.seebanking.data.local.entity.AccountEntity;
import hr.cizmic.seebanking.data.repository.BankingRepository;
import hr.cizmic.seebanking.security.TokenStore;
import hr.cizmic.seebanking.util.AppExecutors;
import hr.cizmic.seebanking.util.SingleLiveEvent;

// manages user's bank accounts list and refresh
public class AccountsViewModel extends ViewModel {

    private final BankingRepository repo;
    private final TokenStore tokenStore;
    private final AppExecutors executors;

    // List of user's bank accounts from database
    private final LiveData<List<AccountEntity>> accounts;

    // spinner
    private final MutableLiveData<Boolean> refreshing = new MutableLiveData<>(false);

    // display error messages to user
    private final SingleLiveEvent<String> message = new SingleLiveEvent<>();

    public AccountsViewModel(BankingRepository repo, TokenStore tokenStore, AppExecutors executors) {
        this.repo = repo;
        this.tokenStore = tokenStore;
        this.executors = executors;
        // observe db accounts
        this.accounts = repo.observeAccounts();
    }

    public LiveData<List<AccountEntity>> accounts() { return accounts; }
    public LiveData<Boolean> refreshing() { return refreshing; }
    public LiveData<String> message() { return message; }

    // Fetch latest acc data from server
    public void refresh() {
        refreshing.setValue(true);
        repo.refreshAccounts(r -> executors.mainThread().execute(() -> {
            refreshing.setValue(false);
            if (!r.success) message.setValue(r.error);
        }));
    }
}
