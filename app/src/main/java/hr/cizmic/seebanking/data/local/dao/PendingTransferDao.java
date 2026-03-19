package hr.cizmic.seebanking.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import hr.cizmic.seebanking.data.local.entity.PendingTransferEntity;

@Dao
public interface PendingTransferDao {

    // add new transfer to queue (returns generated id)
    @Insert
    long insert(PendingTransferEntity pending);

    // get all pending transfers oldest first (for retry processing)
    @Query("SELECT * FROM pending_transfers WHERE status = 'PENDING' ORDER BY createdAtEpochMs ASC")
    List<PendingTransferEntity> getPendingBlocking();

    // update transfer status after send attempt
    @Query("UPDATE pending_transfers SET status = :status, lastError = :err WHERE localId = :localId")
    void updateStatus(long localId, String status, String err);

    // clean up successfully sent transfers
    @Query("DELETE FROM pending_transfers WHERE status = 'SENT'")
    void deleteSent();
}
