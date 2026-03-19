package hr.cizmic.seebanking.data.remote;

import java.util.List;

import hr.cizmic.seebanking.data.remote.dto.AccountDto;
import hr.cizmic.seebanking.data.remote.dto.CreateTransactionRequestDto;
import hr.cizmic.seebanking.data.remote.dto.TransactionPageDto;
import hr.cizmic.seebanking.data.remote.dto.TransferRequestDto;
import hr.cizmic.seebanking.data.remote.dto.TransferResponseDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

// banking operations api endpoints
public interface BankingApi {

    // GET /api/accounts (fetch all user's bank accs
    @GET("api/accounts")
    Call<List<AccountDto>> getAccounts();

    // GET /api/accounts/{accountId}/transactions (paginated list of tx for acc
    // cursor is for pagination (null for first page), limit is how many to fetch
    @GET("api/accounts/{accountId}/transactions")
    Call<TransactionPageDto> getTransactions(
            @Path("accountId") long accountId,
            @Query("cursor") String cursor,
            @Query("limit") int limit
    );

    // POST /api/accounts/{accountId}/transactions (add new tx - deposit/withdrawal
    @POST("api/accounts/{accountId}/transactions")
    Call<hr.cizmic.seebanking.data.remote.dto.TransactionDto> createTransaction(
            @Path("accountId") long accountId,
            @Body CreateTransactionRequestDto req
    );

    // POST /api/transfers (transfer money between accs
    @POST("api/transfers")
    Call<TransferResponseDto> transfer(@Body TransferRequestDto req);
}
