package ece.iit.edu.sendrestrequest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import ece.iit.edu.sendrestrequest.REST_API.RestCallback;
import ece.iit.edu.sendrestrequest.REST_API.RestClient;
import ece.iit.edu.sendrestrequest.REST_API.UserCredentials;
import ece.iit.edu.sendrestrequest.REST_API.UserCredentialsResponse;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MAIN_ACTIVITY";
    private static final String BASE_URL = "https://www.google.com"; // TODO: replace with your server URL

    private RestClient client;
    private TextView user0ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client = new RestClient(BASE_URL, "11223344", BuildConfig.apikey);

        //Test user 0
        user0ID = findViewById(R.id.user0_userid);
        findViewById(R.id.testButton).setOnClickListener(this);

        //Get UserCredentials button
        findViewById(R.id.retrieveUserButton).setOnClickListener(this);

        //Login with JWT
        findViewById(R.id.loginJWTButton).setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        client.destroyDisposables();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.testButton:
                client.getUserDebug(0, new RestCallback<UserCredentials>() {
                    @Override
                    public void onSuccess(UserCredentials result) {
                        user0ID.setText(result.getUserID());
                    }

                    @Override
                    public void onError(Throwable error) {
                        Log.e(TAG, "getUserDebug failed: " + error);
                        user0ID.setText("Error: " + error.getMessage());
                    }
                });
                break;

            case R.id.retrieveUserButton:
                client.requestUserInServer(RestClient.TOKEN_ID, new RestCallback<UserCredentialsResponse>() {
                    @Override
                    public void onSuccess(UserCredentialsResponse result) {
                        if (result.getResponse()) {
                            UserCredentials user = result.getUserCredentials();
                            Log.d(TAG, "User found: " + user);
                        } else {
                            Log.d(TAG, "User not found on server");
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        Log.e(TAG, "requestUser failed: " + error);
                    }
                });
                break;

            case R.id.loginJWTButton:
                String jwtResult = client.testJWT();
                Log.d(TAG, "JWT test result: " + jwtResult);
                break;
        }
    }
}
