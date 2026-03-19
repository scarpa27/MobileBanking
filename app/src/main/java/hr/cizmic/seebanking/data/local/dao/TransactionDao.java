package hr.cizmic.seebanking.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import hr.cizmic.seebanking.data.local.entity.TransactionEntity;

@Dao
public interface TransactionDao {

    // insert or update multiple tx at once
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<TransactionEntity> txs);

    // get latest tx for acc (newest first, updates automatically
    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY createdAtEpochMs DESC LIMIT :limit")
    LiveData<List<TransactionEntity>> observeLatestForAccount(long accountId, int limit);

    // delete all tx for acc
    @Query("DELETE FROM transactions WHERE accountId = :accountId")
    void deleteForAccount(long accountId);
}
