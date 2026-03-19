package hr.cizmic.seebanking.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.math.BigDecimal;

// stores tx history (money in and out of accs
@Entity(
        tableName = "transactions",
        indices = {
                @Index(value = {"accountId"}), // fast lookup by acc
                @Index(value = {"accountId", "createdAtEpochMs"}) // fast sorted queries
        }
)
public class TransactionEntity {

    @PrimaryKey
    @NonNull
    public Long id; // unique tx id

    @NonNull
    public Long accountId; // which acc this tx belongs to

    public String direction; // "IN" for received money, "OUT" for sent money
    public BigDecimal amount; // how much money moved
    public String currency; // e.g. "EUR", "USD"
    public BigDecimal balanceAfter; // acc balance after this tx

    public String counterpartyUserId; // who we sent to / received from
    public String counterpartyMobileNumber; // their phone number
    public String counterpartyName; // their display name

    public String note; // optional tx description
    public Long createdAtEpochMs; // when tx happened

    public TransactionEntity(@NonNull Long id,
                             @NonNull Long accountId,
                             String direction,
                             BigDecimal amount,
                             String currency,
                             BigDecimal balanceAfter,
                             String counterpartyUserId,
                             String counterpartyMobileNumber,
                             String counterpartyName,
                             String note,
                             Long createdAtEpochMs) {
        this.id = id;
        this.accountId = accountId;
        this.direction = direction;
        this.amount = amount;
        this.currency = currency;
        this.balanceAfter = balanceAfter;
        this.counterpartyUserId = counterpartyUserId;
        this.counterpartyMobileNumber = counterpartyMobileNumber;
        this.counterpartyName = counterpartyName;
        this.note = note;
        this.createdAtEpochMs = createdAtEpochMs;
    }
}
