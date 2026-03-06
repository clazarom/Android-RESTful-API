package ece.iit.edu.sendrestrequest.REST_API;

import android.util.Log;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Class to manage HTTPS/SSL connections and provide a configured OkHttpClient.
 * Adds standard Content-Type and Accept headers for cross-platform compatibility
 * (webapp, C++ clients, Android).
 */
public class HttpsClientManager {

    private static final String TAG = "HTTPS_CLIENT";
    private OkHttpClient okHttpClient;
    private SSLContext sslContext;
    private SSLSocketFactory sslSocketFactory;
    private static final String SSL_ALGORITHM = "TLS";

    public HttpsClientManager(){
        if (initializeTrustManager())
            initializeHttpsClient();

    }
    /**
     * Initialize the SSL Context and SSL Socket factory, to establish the trus manager of the class
     * @return success
     */
    private boolean initializeTrustManager(){
        try {
            // Install the trust manager we defined in the class
            sslContext = SSLContext.getInstance(SSL_ALGORITHM);
            sslContext.init(null, new TrustManager[]{allowAllTrustCertificate},
                    new java.security.SecureRandom());
            // Create an ssl socket factory
            sslSocketFactory = sslContext.getSocketFactory();
            return true;
        }catch (NoSuchAlgorithmException  e) {
            Log.e(TAG, "Error initializing SSL trust manager: "+e);
        }catch (KeyManagementException e) {
            Log.e(TAG, "Error initializing SSL trust manager: "+e);
        }
        return false;
    }

    private void initializeHttpsClient(){
        okHttpClient = new OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, allowAllTrustCertificate)
                .hostnameVerifier(hostnameVerifier)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("Content-Type", "application/json")
                            .header("Accept", "application/json")
                            .method(original.method(), original.body())
                            .build();
                    return chain.proceed(request);
                })
                .build();
    }

    public OkHttpClient getOkHttpClient(){
        return okHttpClient;
    }

    //Host name verifier
    private final HostnameVerifier hostnameVerifier = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            Log.d(TAG, "Host name verfier: "+ hostname +" - "+session.getProtocol());
            boolean value = true;
            //TODO:Some logic to verify your host and set value
            return value;
        }
    };

    //Trust Manager
    private final X509TrustManager  allowAllTrustCertificate = new X509TrustManager() {
        @Override
        public void checkClientTrusted(
                java.security.cert.X509Certificate[] chain,
                String authType) throws CertificateException {
            Log.d(TAG, "Check client trusted ("+authType+"): "+ chain.length);
        }

        @Override
        public void checkServerTrusted(
                java.security.cert.X509Certificate[] chain,
                String authType) throws CertificateException {
            Log.d(TAG, "Check server trusted ("+authType+"): "+ chain.length);
        }

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[0];
        }
    };
}
