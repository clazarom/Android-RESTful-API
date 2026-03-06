package ece.iit.edu.sendrestrequest.REST_API;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Platform-independent RESTful API client.
 *
 * Decoupled from Android Activity/UI so it can be used by:
 * - Android app (via RestCallback in Activity)
 * - Webapp (Java/Kotlin server, via RestCallback)
 * - C++ app (via JNI bridge, via RestCallback)
 *
 * All results are delivered through RestCallback interfaces.
 */
public class RestClient {
    private static final String TAG = "REST_CLIENT";

    //Retrofit fields
    private Retrofit retrofit;
    private ApiInterface restAPI;

    private final String baseUrl;
    private final String apiKey;
    public static final String TOKEN_ID = "aadsfaewubfuadiubfasdufbaudsfgbadusfybudfgbv";

    //Manage disposables
    private CompositeDisposable compositeDisposable;

    //Encrypt: JWT
    private JWTEncoder jwtEncoder;

    //HTTPS
    private HttpsClientManager httpsClientManager;

    /**
     * Create a RestClient with configurable base URL, API key, and JWT secret.
     * @param baseUrl the server base URL (e.g. "https://api.example.com")
     * @param apiKey the API key for authenticating requests
     * @param jwtSecret Base64-encoded JWT secret key for token signing/verification
     */
    public RestClient(String baseUrl, String apiKey, String jwtSecret) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;

        //HTTPS
        httpsClientManager = new HttpsClientManager();

        //Initialize retrofit with GSON for JSON compatibility across platforms
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(httpsClientManager.getOkHttpClient())
                .build();

        //API Interface
        restAPI = retrofit.create(ApiInterface.class);
        compositeDisposable = new CompositeDisposable();

        //JWT
        jwtEncoder = new JWTEncoder(jwtSecret);
    }

    /**
     * Fetch user debug info by user number.
     * Results delivered via the callback — no Android UI dependency.
     */
    public void getUserDebug(int user, final RestCallback<UserCredentials> callback) {
        Single<UserCredentials> userReq = restAPI.getUserDebug(user, apiKey);
        userReq.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<UserCredentials>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(UserCredentials user) {
                        Log.d(TAG, "Request success: " + user.getUserID());
                        callback.onSuccess(user);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "Request error: " + e);
                        callback.onError(e);
                    }
                });
    }

    /**
     * Check if user exists on server by token ID.
     * Results delivered via the callback.
     */
    public void requestUserInServer(String tokenID, final RestCallback<UserCredentialsResponse> callback) {
        UserCredentialsRequest request = new UserCredentialsRequest(tokenID);
        Single<UserCredentialsResponse> userCredentials = restAPI.requestUser(apiKey, request);

        userCredentials.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<UserCredentialsResponse>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(UserCredentialsResponse response) {
                        Log.d(TAG, "Request success: " + response.getResponse());
                        callback.onSuccess(response);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "Request error: " + e);
                        callback.onError(e);
                    }
                });
    }

    /**
     * Create and verify a JWT token. Returns the decoded payload string.
     */
    public String testJWT() {
        String encoded = jwtEncoder.createASignedToken("1234", "user@emai.com", "{data1:22, data3:3}");
        return jwtEncoder.verifyAToken(encoded);
    }

    public JWTEncoder getJwtEncoder() {
        return jwtEncoder;
    }

    public void destroyDisposables() {
        if (!compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
    }
}
