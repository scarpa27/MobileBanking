package hr.cizmic.seebanking.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import java.util.List;

import hr.cizmic.seebanking.data.local.entity.TransactionEntity;
import hr.cizmic.seebanking.data.repository.BankingRepository;
import hr.cizmic.seebanking.util.AppExecutors;
import hr.cizmic.seebanking.util.SingleLiveEvent;

// manages tx for acc with pagination (30 per page
public class TransactionsViewModel extends ViewModel {

    // load 30 tx per page
    private static final int PAGE_LIMIT = 30;

    private final BankingRepository repo;
    private final AppExecutors executors;

    // currently selected acc id
    private final MutableLiveData<Long> accountId = new MutableLiveData<>();

    // auto-updates when accountId changes via switchMap
    private final LiveData<List<TransactionEntity>> transactions;

    // shows spinner
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    // displays error messages to user
    private final SingleLiveEvent<String> message = new SingleLiveEvent<>();

    // whether more tx can be loaded
    private final MutableLiveData<Boolean> hasMore = new MutableLiveData<>(false);

    public TransactionsViewModel(BankingRepository repo, AppExecutors executors) {
        this.repo = repo;
        this.executors = executors;
        // auto-reload tx when acc changes
        this.transactions =  Transformations.switchMap(accountId, id -> repo.observeLatestTransactions(id, 200));
    }

    public LiveData<List<TransactionEntity>> transactions() { return transactions; }
    public LiveData<Boolean> loading() { return loading; }
    public LiveData<String> message() { return message; }
    public LiveData<Boolean> hasMore() { return hasMore; }

    // set which acc to show tx for
    public void setAccountId(long id) {
        accountId.setValue(id);
    }

    // fetch first page (30) from server
    public void refresh() {
        Long id = accountId.getValue();
        if (id == null) return;

        loading.setValue(true);
        repo.refreshTransactionsFirstPage(id, PAGE_LIMIT, r -> executors.mainThread().execute(() -> {
            loading.setValue(false);
            if (r.success) {
                hasMore.setValue(Boolean.TRUE.equals(r.data));
            } else {
                message.setValue(r.error);
            }
        }));
    }

    // load next page (30) from server for pagination
    public void loadMore() {
        Long id = accountId.getValue();
        if (id == null) return;

        loading.setValue(true);
        repo.loadMoreTransactions(id, PAGE_LIMIT, r -> executors.mainThread().execute(() -> {
            loading.setValue(false);
            if (r.success) {
                hasMore.setValue(Boolean.TRUE.equals(r.data));
            } else {
                message.setValue(r.error);
            }
        }));
    }
}
