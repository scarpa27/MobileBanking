package hr.cizmic.seebanking.data.mapper;

import java.util.ArrayList;
import java.util.List;

import hr.cizmic.seebanking.data.local.entity.AccountEntity;
import hr.cizmic.seebanking.data.local.entity.TransactionEntity;
import hr.cizmic.seebanking.data.local.entity.UserEntity;
import hr.cizmic.seebanking.data.remote.dto.AccountDto;
import hr.cizmic.seebanking.data.remote.dto.TransactionDto;

// converts api data (dtos) into db entities so we can save them locally
public final class DtoMappers {

    private DtoMappers() {}

    // turns user data from api into UserEntity for db
    public static UserEntity toEntity(String id, String fullName, String email, String mobileNumber) {
        if (id == null) return null;
        return new UserEntity(id, fullName, email, mobileNumber);
    }

    // converts list of acc dtos from api to entities for db
    public static List<AccountEntity> toAccountEntities(List<AccountDto> dtos, String userId) {
        List<AccountEntity> out = new ArrayList<>();
        if (dtos == null) return out;
        for (AccountDto dto : dtos) {
            if (dto == null || dto.id == null) continue; // skip broken data
            out.add(new AccountEntity(
                    dto.id,
                    userId,
                    dto.iban,
                    dto.name,
                    dto.currency,
                    dto.balance,
                    null // no timestamp needed here
            ));
        }
        return out;
    }

    // converts list of tx dtos from api to entities for db
    public static List<TransactionEntity> toTransactionEntities(List<TransactionDto> dtos, long accountId) {
        List<TransactionEntity> out = new ArrayList<>();
        if (dtos == null) return out;
        for (TransactionDto dto : dtos) {
            if (dto == null || dto.id == null) continue; // skip broken data
            String direction = null;
            // api says "CREDIT" or "DEBIT"-> convert to "IN" or "OUT" for simpler display
            if ("CREDIT".equalsIgnoreCase(dto.type)) {
                direction = "IN";
            } else if ("DEBIT".equalsIgnoreCase(dto.type)) {
                direction = "OUT";
            }
            out.add(new TransactionEntity(
                    dto.id,
                    accountId,
                    direction,
                    dto.amount,
                    dto.currency,
                    dto.balanceAfter,
                    dto.counterpartyUserId,
                    dto.counterpartyMobile,
                    dto.counterpartyName,
                    dto.description,
                    parseInstantToEpochMs(dto.createdAt) // convert date string to timestamp
            ));
        }
        return out;
    }

    // same as above but for single tx
    public static TransactionEntity toTransactionEntity(TransactionDto dto, long accountId) {
        if (dto == null || dto.id == null) return null;
        String direction = null;
        if ("CREDIT".equalsIgnoreCase(dto.type)) {
            direction = "IN";
        } else if ("DEBIT".equalsIgnoreCase(dto.type)) {
            direction = "OUT";
        }
        return new TransactionEntity(
                dto.id,
                accountId,
                direction,
                dto.amount,
                dto.currency,
                dto.balanceAfter,
                dto.counterpartyUserId,
                dto.counterpartyMobile,
                dto.counterpartyName,
                dto.description,
                parseInstantToEpochMs(dto.createdAt)
        );
    }

    // api sends dates as strings like "2023-01-15T10:30:00Z", we need milliseconds for db
    private static Long parseInstantToEpochMs(String instant) {
        if (instant == null || instant.trim().isEmpty()) return null;
        try {
            return java.time.Instant.parse(instant.trim()).toEpochMilli();
        } catch (Exception ignored) {
            return null; // if parsing fails, just ignore it
        }
    }
}
