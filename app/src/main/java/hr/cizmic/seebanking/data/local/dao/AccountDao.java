package hr.cizmic.seebanking.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import hr.cizmic.seebanking.data.local.entity.AccountEntity;

import java.math.BigDecimal;

@Dao
public interface AccountDao {

    // insert or update multiple accs at once
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<AccountEntity> accounts);

    // get all accs for user (updates automatically)
    @Query("SELECT * FROM accounts WHERE userId = :userId ORDER BY id ASC")
    LiveData<List<AccountEntity>> observeForUser(String userId);

    // get single acc by id (updates automatically)
    @Query("SELECT * FROM accounts WHERE id = :accountId LIMIT 1")
    LiveData<AccountEntity> observeById(long accountId);

    // get single acc by id (return immediately, no updates)
    @Query("SELECT * FROM accounts WHERE id = :accountId LIMIT 1")
    AccountEntity getBlocking(long accountId);

    // update acc balance after tx
    @Query("UPDATE accounts SET balance = :balance WHERE id = :accountId")
    void updateBalance(long accountId, BigDecimal balance);

    // delete all accs for user
    @Query("DELETE FROM accounts WHERE userId = :userId")
    void deleteForUser(String userId);
}
