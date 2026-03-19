package hr.cizmic.seebanking.data.remote.dto;

import java.math.BigDecimal;

// Request to transfer money from one account to another
public class TransferRequestDto {

    public Long fromAccountId;       // Which account to send money from
    public String recipientMobile;   // Recipient's phone number
    public String recipientIban;     // Recipient's IBAN

    public BigDecimal amount;        // How much to transfer
    public String description;       // Transfer note/description

    public TransferRequestDto(Long fromAccountId, String recipientMobile, String recipientIban, BigDecimal amount, String description) {
        this.fromAccountId = fromAccountId;
        this.recipientMobile = recipientMobile;
        this.recipientIban = recipientIban;
        this.amount = amount;
        this.description = description;
    }
}
