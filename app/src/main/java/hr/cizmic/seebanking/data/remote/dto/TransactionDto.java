package hr.cizmic.seebanking.data.remote.dto;

import java.math.BigDecimal;

// single tx info (money in or out of acc
public class TransactionDto {
    public Long id;                      // tx id
    public String type;                  // direction: "CREDIT" means money in, "DEBIT" means money out
    public String currency;              // currency code, e.g. "EUR"
    public BigDecimal amount;            // tx amount
    public BigDecimal balanceAfter;      // acc balance after this tx
    public String createdAt;             // when it happened (ISO-8601 timestamp
    public String description;           // tx description/note

    // info about the other party (if it's a transfer
    public String transferReference;     // reference number for transfer
    public String counterpartyUserId;    // other user's id
    public String counterpartyMobile;    // other user's phone
    public String counterpartyName;      // other user's name
}
