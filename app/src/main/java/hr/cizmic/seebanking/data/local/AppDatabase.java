package hr.cizmic.seebanking.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import hr.cizmic.seebanking.data.local.dao.AccountDao;
import hr.cizmic.seebanking.data.local.dao.PendingTransferDao;
import hr.cizmic.seebanking.data.local.dao.TransactionDao;
import hr.cizmic.seebanking.data.local.dao.TransactionRemoteKeyDao;
import hr.cizmic.seebanking.data.local.dao.UserDao;
import hr.cizmic.seebanking.data.local.entity.AccountEntity;
import hr.cizmic.seebanking.data.local.entity.PendingTransferEntity;
import hr.cizmic.seebanking.data.local.entity.TransactionEntity;
import hr.cizmic.seebanking.data.local.entity.TransactionRemoteKeyEntity;
import hr.cizmic.seebanking.data.local.entity.UserEntity;

// Room database: stores users, accounts, transactions locally
@Database(
        entities = {
                UserEntity.class,
                AccountEntity.class,
                TransactionEntity.class,
                TransactionRemoteKeyEntity.class,
                PendingTransferEntity.class
        },
        version = 5,
        exportSchema = false
)
@TypeConverters(DbConverters.class)
public abstract class AppDatabase extends RoomDatabase {
    // Get DAO for user operations
    public abstract UserDao userDao();
    // Get DAO for account operations
    public abstract AccountDao accountDao();
    // Get DAO for transaction operations
    public abstract TransactionDao transactionDao();
    // Get DAO for pagination keys
    public abstract TransactionRemoteKeyDao transactionRemoteKeyDao();
    // Get DAO for pending transfer queue
    public abstract PendingTransferDao pendingTransferDao();
}
