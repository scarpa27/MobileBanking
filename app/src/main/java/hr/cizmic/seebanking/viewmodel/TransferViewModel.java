package hr.cizmic.seebanking.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.math.BigDecimal;

import hr.cizmic.seebanking.data.repository.BankingRepository;
import hr.cizmic.seebanking.util.AppExecutors;
import hr.cizmic.seebanking.util.MoneyUtils;
import hr.cizmic.seebanking.util.SingleLiveEvent;
import hr.cizmic.seebanking.util.ValidationUtils;

// Handles money transfers between accounts with validation
public class TransferViewModel extends ViewModel {

    private final BankingRepository repo;
    private final AppExecutors executors;

    // Shows loading spinner
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    // Displays error messages to user
    private final SingleLiveEvent<String> message = new SingleLiveEvent<>();
    // Triggers success navigation
    private final SingleLiveEvent<Boolean> sent = new SingleLiveEvent<>();

    public TransferViewModel(BankingRepository repo, AppExecutors executors) {
        this.repo = repo;
        this.executors = executors;
    }

    public LiveData<Boolean> loading() { return loading; }
    public LiveData<String> message() { return message; }
    public LiveData<Boolean> sent() { return sent; }

    // Send money with validation
    public void transfer(Long fromAccountId,
                         String toMobile,
                         String toIban,
                         String amountText,
                         String note) {
        // Must pick source account
        if (fromAccountId == null || fromAccountId <= 0) {
            message.setValue("Pick a source account");
            return;
        }

        String mobileNorm = ValidationUtils.normalizePhone(toMobile);
        String ibanNorm = ValidationUtils.normalizeIban(toIban);
        // Need either mobile or IBAN, not both
        if (mobileNorm == null && ibanNorm == null) {
            message.setValue("Recipient mobile number or IBAN is required");
            return;
        }
        if (mobileNorm != null && ibanNorm != null) {
            message.setValue("Provide either recipient mobile or IBAN");
            return;
        }
        // Phone must be 6-20 digits
        if (mobileNorm != null && !ValidationUtils.isValidPhone(mobileNorm)) {
            message.setValue("Mobile number must be 6-20 digits (optionally +)");
            return;
        }

        // Amount must be positive with max 2 decimals
        BigDecimal amount = MoneyUtils.parseAmount(amountText);
        if (amount == null) {
            message.setValue("Amount must be a valid number with up to 2 decimals");
            return;
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            message.setValue("Amount must be > 0");
            return;
        }

        // Note max 200 characters
        String noteNorm = ValidationUtils.trimToNull(note);
        if (noteNorm != null && noteNorm.length() > 200) {
            message.setValue("Note must be at most 200 characters");
            return;
        }

        // Call API on background thread
        loading.setValue(true);
        repo.transfer(fromAccountId, mobileNorm, ibanNorm, amount, noteNorm, r -> executors.mainThread().execute(() -> {
            loading.setValue(false);
            if (r.success) {
                sent.setValue(true);
            } else {
                message.setValue(r.error);
            }
        }));
    }
}
