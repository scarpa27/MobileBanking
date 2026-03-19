package hr.cizmic.seebanking;

import android.content.Context;

import hr.cizmic.seebanking.data.local.AppDatabase;
import hr.cizmic.seebanking.data.local.DatabaseProvider;
import hr.cizmic.seebanking.data.remote.ApiClient;
import hr.cizmic.seebanking.data.remote.AuthApi;
import hr.cizmic.seebanking.data.remote.BankingApi;
import hr.cizmic.seebanking.data.repository.AuthRepository;
import hr.cizmic.seebanking.data.repository.BankingRepository;
import hr.cizmic.seebanking.security.SharedPrefsTokenStore;
import hr.cizmic.seebanking.security.TokenStore;
import hr.cizmic.seebanking.util.AppExecutors;
import retrofit2.Retrofit;

// app dependenies provider singleton
public final class ServiceLocator {

    private static volatile ServiceLocator INSTANCE;

    private final Context appContext;
    private final AppExecutors executors;
    private final TokenStore tokenStore;

    private final AppDatabase db;
    private final Retrofit retrofit;
    private final AuthApi authApi;
    private final BankingApi bankingApi;

    private final AuthRepository authRepository;
    private final BankingRepository bankingRepository;

    // create all deps
    private ServiceLocator(Context appContext) {
        this.appContext = appContext.getApplicationContext();
        this.executors = new AppExecutors();
        this.tokenStore = new SharedPrefsTokenStore(this.appContext);

        this.db = DatabaseProvider.get(this.appContext);

        this.retrofit = ApiClient.createRetrofit(tokenStore);
        this.authApi = retrofit.create(AuthApi.class);
        this.bankingApi = retrofit.create(BankingApi.class);

        this.authRepository = new AuthRepository(authApi, db, tokenStore, executors);
        this.bankingRepository = new BankingRepository(bankingApi, db, tokenStore, executors);
    }

    public static void init(Context context) {
        if (INSTANCE == null) {
            synchronized (ServiceLocator.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ServiceLocator(context);
                }
            }
        }
    }

    public static ServiceLocator get() {
        if (INSTANCE == null) {
            throw new IllegalStateException("ServiceLocator not initialized. Call ServiceLocator.init(context) in Application.");
        }
        return INSTANCE;
    }

    // for bg work
    public AppExecutors executors() { return executors; }

    // shared prefs
    public TokenStore tokenStore() { return tokenStore; }

    // db instance (actually used through repository instances)
    public AppDatabase db() { return db; }

    // Auth repository (handles login/register  db wrapper)
    public AuthRepository authRepository() { return authRepository; }

    // banking repository (handles accounts/transactions/transfers - db wrapper
    public BankingRepository bankingRepository() { return bankingRepository; }
}
