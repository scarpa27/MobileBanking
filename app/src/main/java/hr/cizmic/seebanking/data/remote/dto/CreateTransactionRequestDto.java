package hr.cizmic.seebanking.data.remote.dto;

import java.math.BigDecimal;

// Request to create a new transaction (deposit or withdrawal)
public class CreateTransactionRequestDto {
    public BigDecimal amount;           // How much money
    public String description;          // Transaction note/description
    public String counterpartyMobile;   // Other party's phone number
    public String recipientIban;        // Recipient's IBAN
    public String counterpartyName;     // Other party's name

    public CreateTransactionRequestDto(BigDecimal amount,
                                       String description,
                                       String counterpartyMobile,
                                       String recipientIban,
                                       String counterpartyName) {
        this.amount = amount;
        this.description = description;
        this.counterpartyMobile = counterpartyMobile;
        this.recipientIban = recipientIban;
        this.counterpartyName = counterpartyName;
    }
}
