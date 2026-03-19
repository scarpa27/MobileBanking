package hr.cizmic.seebanking.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import hr.cizmic.seebanking.data.local.entity.TransactionRemoteKeyEntity;

@Dao
public interface TransactionRemoteKeyDao {

    // save pagination cursor so we know where to load next page from
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(TransactionRemoteKeyEntity key);

    // get pagination cursor for acc (returns immediately)
    @Query("SELECT * FROM transaction_remote_keys WHERE accountId = :accountId LIMIT 1")
    TransactionRemoteKeyEntity getBlocking(long accountId);

    // clear pagination cursor for acc
    @Query("DELETE FROM transaction_remote_keys WHERE accountId = :accountId")
    void deleteForAccount(long accountId);
}
