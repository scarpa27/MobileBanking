package hr.cizmic.seebanking.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// stores pagination cursor so we know where to load next page from
@Entity(tableName = "transaction_remote_keys")
public class TransactionRemoteKeyEntity {

    @PrimaryKey
    @NonNull
    public Long accountId; // which acc this cursor is for

    public String nextCursor; // server-provided token for next page
    public Long lastSyncedEpochMs; // when we last fetched data

    public TransactionRemoteKeyEntity(@NonNull Long accountId, String nextCursor, Long lastSyncedEpochMs) {
        this.accountId = accountId;
        this.nextCursor = nextCursor;
        this.lastSyncedEpochMs = lastSyncedEpochMs;
    }
}
