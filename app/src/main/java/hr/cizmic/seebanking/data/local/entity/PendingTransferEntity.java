package hr.cizmic.seebanking.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.math.BigDecimal;

// Queue for transfers that failed - will retry later
@Entity(tableName = "pending_transfers")
public class PendingTransferEntity {

    @PrimaryKey(autoGenerate = true)
    public Long localId; // local database id

    public Long fromAccountId; // which account to send from
    public String recipientMobile; // recipient's phone number
    public String recipientIban; // recipient's bank account

    public BigDecimal amount; // how much to send
    public String description; // transfer note

    public Long createdAtEpochMs; // when transfer was created

    public String status; // "PENDING", "SENT", or "FAILED"
    public String lastError; // error message if failed

    public PendingTransferEntity(Long fromAccountId,
                                 String recipientMobile,
                                 String recipientIban,
                                 BigDecimal amount,
                                 String description,
                                 Long createdAtEpochMs,
                                 String status,
                                 String lastError) {
        this.fromAccountId = fromAccountId;
        this.recipientMobile = recipientMobile;
        this.recipientIban = recipientIban;
        this.amount = amount;
        this.description = description;
        this.createdAtEpochMs = createdAtEpochMs;
        this.status = status;
        this.lastError = lastError;
    }
}
