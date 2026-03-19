package hr.cizmic.seebanking.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.math.BigDecimal;

// stores bank accs (each user can have multiple accs)
@Entity(
        tableName = "accounts",
        indices = {
                @Index(value = {"userId"}), // fast lookup by user
                @Index(value = {"iban"}, unique = true) // ensure IBAN is unique
        }
)
public class AccountEntity {

    @PrimaryKey
    @NonNull
    public Long id; // unique acc id

    @NonNull
    public String userId; // which user owns this acc

    public String iban; // international bank acc number
    public String name; // acc nickname (eg "Current")
    public String currency; // e.g. "EUR", "USD"
    public BigDecimal balance; // current acc balance
    public Long updatedAtEpochMs; // when balance was last updated

    public AccountEntity(@NonNull Long id, @NonNull String userId, String iban, String name, String currency, BigDecimal balance, Long updatedAtEpochMs) {
        this.id = id;
        this.userId = userId;
        this.iban = iban;
        this.name = name;
        this.currency = currency;
        this.balance = balance;
        this.updatedAtEpochMs = updatedAtEpochMs;
    }
}
