package hr.cizmic.seebanking.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import hr.cizmic.seebanking.ServiceLocator;

// factory to create VMs with their dependencies injected
public class AppViewModelFactory implements ViewModelProvider.Factory {

    // creates right VM with dependencies from ServiceLocator
    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        ServiceLocator sl = ServiceLocator.get();

        // check which VM is requested and create it with proper deps
        if (modelClass.isAssignableFrom(AuthViewModel.class)) {
            return (T) new AuthViewModel(sl.authRepository(), sl.tokenStore(), sl.executors());
        }
        if (modelClass.isAssignableFrom(AccountsViewModel.class)) {
            return (T) new AccountsViewModel(sl.bankingRepository(), sl.tokenStore(), sl.executors());
        }
        if (modelClass.isAssignableFrom(TransactionsViewModel.class)) {
            return (T) new TransactionsViewModel(sl.bankingRepository(), sl.executors());
        }
        if (modelClass.isAssignableFrom(TransferViewModel.class)) {
            return (T) new TransferViewModel(sl.bankingRepository(), sl.executors());
        }
        if (modelClass.isAssignableFrom(CreateTransactionViewModel.class)) {
            return (T) new CreateTransactionViewModel(sl.bankingRepository(), sl.executors());
        }

        throw new IllegalArgumentException("Unknown VM class: " + modelClass.getName());
    }
}
