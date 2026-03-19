package hr.cizmic.seebanking.data.remote.dto;

import java.math.BigDecimal;

// bank acc info from server
public class AccountDto {
    public Long id;             // internal acc id
    public String publicId;     // public facing acc id
    public String type;         // acc type: "CURRENT" or "GIRO"
    public String name;         // acc name (e.g. "Main Account"
    public String iban;         // international bank acc number
    public String currency;     // currency code, e.g. "EUR"
    public BigDecimal balance;  // current acc balance
}
