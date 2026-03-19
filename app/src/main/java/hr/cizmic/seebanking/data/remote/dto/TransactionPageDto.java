package hr.cizmic.seebanking.data.remote.dto;

import java.util.List;

// paginated response for tx list (allows loading more in chunks
public class TransactionPageDto {
    public List<TransactionDto> items;  // current page of tx
    public String nextCursor;           // cursor for next page (null when no more pages left
}
