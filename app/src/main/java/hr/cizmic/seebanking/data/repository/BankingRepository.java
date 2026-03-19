package hr.cizmic.seebanking.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.math.BigDecimal;
import java.util.List;

import hr.cizmic.seebanking.data.local.AppDatabase;
import hr.cizmic.seebanking.data.local.entity.AccountEntity;
import hr.cizmic.seebanking.data.local.entity.PendingTransferEntity;
import hr.cizmic.seebanking.data.local.entity.TransactionEntity;
import hr.cizmic.seebanking.data.local.entity.TransactionRemoteKeyEntity;
import hr.cizmic.seebanking.data.mapper.DtoMappers;
import hr.cizmic.seebanking.data.remote.ApiErrorUtils;
import hr.cizmic.seebanking.data.remote.BankingApi;
import hr.cizmic.seebanking.data.remote.dto.CreateTransactionRequestDto;
import hr.cizmic.seebanking.data.remote.dto.TransactionPageDto;
import hr.cizmic.seebanking.data.remote.dto.TransactionDto;
import hr.cizmic.seebanking.data.remote.dto.TransferRequestDto;
import hr.cizmic.seebanking.data.remote.dto.TransferResponseDto;
import hr.cizmic.seebanking.security.TokenStore;
import hr.cizmic.seebanking.util.AppExecutors;
import hr.cizmic.seebanking.util.Result;
import hr.cizmic.seebanking.util.ResultCallback;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Handles accounts, transactions, and transfers
// First read from database then update from API (:
public final class BankingRepository {

    private static final int DEFAULT_TX_LIMIT = 30;

    private final BankingApi api;
    private final AppDatabase db;
    private final TokenStore tokenStore;
    private final AppExecutors executors;

    public BankingRepository(BankingApi api, AppDatabase db, TokenStore tokenStore, AppExecutors executors) {
        this.api = api;
        this.db = db;
        this.tokenStore = tokenStore;
        this.executors = executors;
    }

    // Load accounts from database - updates automatically via LiveData
    // UI observes this to show accounts instantly (offline-first)
    public LiveData<List<AccountEntity>> observeAccounts() {
        String userId = tokenStore.getUserId();
        if (userId == null) {
            // A safer alternative is to expose a MediatorLiveData and update when user changes.
            return new androidx.lifecycle.MutableLiveData<>();
        }
        return db.accountDao().observeForUser(userId);
    }

    // Get accounts from API and save to local database
    // Database updates trigger LiveData so UI gets notified automatically
    public void refreshAccounts(ResultCallback<Void> cb) {
        String userId = tokenStore.getUserId();
        if (userId == null) {
            cb.onResult(Result.fail("Not logged in"));
            return;
        }

        api.getAccounts().enqueue(new Callback<List<hr.cizmic.seebanking.data.remote.dto.AccountDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<hr.cizmic.seebanking.data.remote.dto.AccountDto>> call,
                                   @NonNull Response<List<hr.cizmic.seebanking.data.remote.dto.AccountDto>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    cb.onResult(Result.fail(ApiErrorUtils.messageOrFallback(response, "Failed to load accounts")));
                    return;
                }

                // Save accounts to database on background thread
                executors.diskIO().execute(() -> {
                    db.accountDao().upsertAll(DtoMappers.toAccountEntities(response.body(), userId));
                    cb.onResult(Result.ok(null));
                });
            }

            @Override
            public void onFailure(@NonNull Call<List<hr.cizmic.seebanking.data.remote.dto.AccountDto>> call, @NonNull Throwable t) {
                cb.onResult(Result.fail("Network error: " + t.getMessage()));
            }
        });
    }


    // Blocking version of refreshAccounts - used by background workers only
    // Get accounts from API and save to database (waits for network)
    public boolean refreshAccountsBlocking() {
        String userId = tokenStore.getUserId();
        if (userId == null) return false;

        try {
            Response<List<hr.cizmic.seebanking.data.remote.dto.AccountDto>> resp = api.getAccounts().execute();
            if (!resp.isSuccessful() || resp.body() == null) return false;

            db.accountDao().upsertAll(DtoMappers.toAccountEntities(resp.body(), userId));
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    // Load transactions from database - updates automatically via LiveData
    // UI observes this to show transactions instantly (offline-first)
    public LiveData<List<TransactionEntity>> observeLatestTransactions(long accountId, int limit) {
        return db.transactionDao().observeLatestForAccount(accountId, limit <= 0 ? DEFAULT_TX_LIMIT : limit);
    }

    // Get first page of transactions from API and replace database cache
    // Save nextCursor to database for loading more later (pagination)
    public void refreshTransactionsFirstPage(long accountId, int limit, ResultCallback<Boolean> cb) {
        api.getTransactions(accountId, null, limit <= 0 ? DEFAULT_TX_LIMIT : limit)
                .enqueue(new Callback<TransactionPageDto>() {
                    @Override
                    public void onResponse(@NonNull Call<TransactionPageDto> call, @NonNull Response<TransactionPageDto> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            cb.onResult(Result.fail(ApiErrorUtils.messageOrFallback(response, "Failed to load transactions")));
                            return;
                        }
                        TransactionPageDto page = response.body();

                        // Save transactions to database on background thread
                        executors.diskIO().execute(() -> {
                            // Clear old transactions for this account
                            db.transactionDao().deleteForAccount(accountId);
                            db.transactionRemoteKeyDao().deleteForAccount(accountId);

                            // Save new transactions to database
                            db.transactionDao().upsertAll(DtoMappers.toTransactionEntities(page.items, accountId));
                            // Store cursor for pagination (to load more later)
                            db.transactionRemoteKeyDao().upsert(new TransactionRemoteKeyEntity(
                                    accountId,
                                    page.nextCursor,
                                    System.currentTimeMillis()
                            ));

                            cb.onResult(Result.ok(page.nextCursor != null));
                        });
                    }

                    @Override
                    public void onFailure(@NonNull Call<TransactionPageDto> call, @NonNull Throwable t) {
                        cb.onResult(Result.fail("Network error: " + t.getMessage()));
                    }
                });
    }

    // Get next cursor from database to load more transactions (pagination)
    // If cursor exists, fetch next page from API and append to database
    public void loadMoreTransactions(long accountId, int limit, ResultCallback<Boolean> cb) {
        // Read cursor from database on background thread
        executors.diskIO().execute(() -> {
            TransactionRemoteKeyEntity key = db.transactionRemoteKeyDao().getBlocking(accountId);
            String cursor = key == null ? null : key.nextCursor;

            if (cursor == null || cursor.trim().isEmpty()) {
                cb.onResult(Result.ok(false)); // no more
                return;
            }

            api.getTransactions(accountId, cursor, limit <= 0 ? DEFAULT_TX_LIMIT : limit)
                    .enqueue(new Callback<TransactionPageDto>() {
                        @Override
                        public void onResponse(@NonNull Call<TransactionPageDto> call, @NonNull Response<TransactionPageDto> response) {
                            if (!response.isSuccessful() || response.body() == null) {
                                cb.onResult(Result.fail(ApiErrorUtils.messageOrFallback(response, "Failed to load more")));
                                return;
                            }
                            TransactionPageDto page = response.body();

                            // Append new transactions to database on background thread
                            executors.diskIO().execute(() -> {
                                // Add new transactions (don't clear old ones)
                                db.transactionDao().upsertAll(DtoMappers.toTransactionEntities(page.items, accountId));
                                // Update cursor for next pagination
                                db.transactionRemoteKeyDao().upsert(new TransactionRemoteKeyEntity(
                                        accountId,
                                        page.nextCursor,
                                        System.currentTimeMillis()
                                ));
                                cb.onResult(Result.ok(page.nextCursor != null));
                            });
                        }

                        @Override
                        public void onFailure(@NonNull Call<TransactionPageDto> call, @NonNull Throwable t) {
                            cb.onResult(Result.fail("Network error: " + t.getMessage()));
                        }
                    });
        });
    }

    // Send transaction to API and save returned transaction to database
    // Also updates account balance in database
    public void createTransaction(Long accountId,
                                  BigDecimal amount,
                                  String counterpartyName,
                                  String counterpartyMobile,
                                  String recipientIban,
                                  String description,
                                  ResultCallback<Void> cb) {
        if (accountId == null) {
            cb.onResult(Result.fail("Pick an account"));
            return;
        }
        boolean hasMobile = counterpartyMobile != null && !counterpartyMobile.trim().isEmpty();
        boolean hasIban = recipientIban != null && !recipientIban.trim().isEmpty();
        if (!hasMobile && !hasIban) {
            cb.onResult(Result.fail("Recipient mobile number or IBAN is required"));
            return;
        }
        if (hasMobile && hasIban) {
            cb.onResult(Result.fail("Provide either recipient mobile or IBAN"));
            return;
        }
        String mobileNorm = hasMobile ? counterpartyMobile.trim() : null;
        String ibanNorm = hasIban ? recipientIban.trim() : null;
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            cb.onResult(Result.fail("Amount must be > 0"));
            return;
        }

        api.createTransaction(accountId, new CreateTransactionRequestDto(amount, description, mobileNorm, ibanNorm, counterpartyName))
                .enqueue(new Callback<TransactionDto>() {
                    @Override
                    public void onResponse(@NonNull Call<TransactionDto> call, @NonNull Response<TransactionDto> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            cb.onResult(Result.fail(ApiErrorUtils.messageOrFallback(response, "Create failed")));
                            return;
                        }
                        TransactionDto dto = response.body();

                        // Save transaction to database on background thread
                        executors.diskIO().execute(() -> {
                            TransactionEntity entity = DtoMappers.toTransactionEntity(dto, accountId);
                            if (entity != null) {
                                // Add transaction to database
                                db.transactionDao().upsertAll(java.util.Collections.singletonList(entity));
                                if (dto.balanceAfter != null) {
                                    // Update account balance in database
                                    db.accountDao().updateBalance(accountId, dto.balanceAfter);
                                }
                            }
                            cb.onResult(Result.ok(null));
                        });
                    }

                    @Override
                    public void onFailure(@NonNull Call<TransactionDto> call, @NonNull Throwable t) {
                        cb.onResult(Result.fail("Network error: " + t.getMessage()));
                    }
                });
    }

    // Try API first - if fails, save to pending transfers table (offline queue)
    // SyncWorker retries pending transfers later
    public void transfer(Long fromAccountId,
                         String recipientMobile,
                         String recipientIban,
                         BigDecimal amount,
                         String description,
                         ResultCallback<Void> cb) {
        if (fromAccountId == null) {
            cb.onResult(Result.fail("Pick a source account"));
            return;
        }
        boolean hasMobile = recipientMobile != null && !recipientMobile.trim().isEmpty();
        boolean hasIban = recipientIban != null && !recipientIban.trim().isEmpty();
        if (!hasMobile && !hasIban) {
            cb.onResult(Result.fail("Recipient mobile number or IBAN is required"));
            return;
        }
        if (hasMobile && hasIban) {
            cb.onResult(Result.fail("Provide either recipient mobile or IBAN"));
            return;
        }
        final String recipientMobileNorm = hasMobile ? recipientMobile.trim() : null;
        final String recipientIbanNorm = hasIban ? recipientIban.trim() : null;
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            cb.onResult(Result.fail("Amount must be > 0"));
            return;
        }

        api.transfer(new TransferRequestDto(fromAccountId, recipientMobileNorm, recipientIbanNorm, amount, description))
                .enqueue(new Callback<TransferResponseDto>() {
                    @Override
                    public void onResponse(@NonNull Call<TransferResponseDto> call, @NonNull Response<TransferResponseDto> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            cb.onResult(Result.fail(ApiErrorUtils.messageOrFallback(response, "Transfer failed")));
                            return;
                        }
                        cb.onResult(Result.ok(null));
                    }

                    @Override
                    public void onFailure(@NonNull Call<TransferResponseDto> call, @NonNull Throwable t) {
                        // Save failed transfer to offline queue for retry later
                        executors.diskIO().execute(() -> {
                            PendingTransferEntity pending = new PendingTransferEntity(
                                    fromAccountId,
                                    recipientMobileNorm,
                                    recipientIbanNorm,
                                    amount,
                                    description,
                                    System.currentTimeMillis(),
                                    "PENDING",
                                    t.getMessage()
                            );
                            // Add to pending transfers table in database
                            db.pendingTransferDao().insert(pending);
                        });

                        cb.onResult(Result.fail("Network error (queued): " + t.getMessage()));
                    }
                });
    }

    // Retry all pending transfers from database (blocking call for background workers)
    // Read pending transfers, try API for each, update status in database
    // Delete successfully sent transfers from pending table
    public int processPendingTransfersBlocking() {
        List<PendingTransferEntity> pending = db.pendingTransferDao().getPendingBlocking();
        int sent = 0;

        for (PendingTransferEntity p : pending) {
            try {
                // Try sending transfer to API
                Response<TransferResponseDto> r = api.transfer(
                        new TransferRequestDto(p.fromAccountId, p.recipientMobile, p.recipientIban, p.amount, p.description)
                ).execute();

                if (r.isSuccessful() && r.body() != null) {
                    // Mark as sent in database
                    db.pendingTransferDao().updateStatus(p.localId, "SENT", null);
                    sent++;
                } else {
                    // Mark as failed in database
                    db.pendingTransferDao().updateStatus(p.localId, "FAILED", "HTTP " + r.code());
                }
            } catch (Exception ex) {
                // Mark as failed in database
                db.pendingTransferDao().updateStatus(p.localId, "FAILED", ex.getMessage());
            }
        }

        // Remove sent transfers from database
        db.pendingTransferDao().deleteSent();
        return sent;
    }
}
