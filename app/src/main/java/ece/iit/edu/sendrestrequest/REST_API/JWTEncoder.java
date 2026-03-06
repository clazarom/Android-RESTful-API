package ece.iit.edu.sendrestrequest.REST_API;

import android.util.Log;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Secure Rest API client which encrypts every message using JWT
 * https://jwt.io/
 * https://java.jsonwebtoken.io/
 *
 * Platform-independent: accepts the secret key value as a constructor parameter
 * so it can be used from Android (BuildConfig), webapp (env vars), or C++ (JNI).
 *
 * @author Caterina Lazaro
 * @version 2.0
 */
public class JWTEncoder {
    private static final String TAG = "JWT_CLIENT";
    private SecretKey secretKey;
    //Claims in the JWT messages - standard names for cross-platform compatibility
    public static final String ID_TOKEN = "idtoken";
    public static final String EMAIL = "email";
    public static final String DATA = "data";

    //Algorithm: HMAC256 - Jwt HS256, HMAC with SHA-256
    private static final String ALGORITHM_TYPE = "HmacSHA256";
    private static final SignatureAlgorithm ALGORITHM_HS = SignatureAlgorithm.HS256;

    /**
     * Create a JWTEncoder with the given Base64-encoded secret key.
     * @param secretKeyValue Base64-encoded secret key string
     */
    public JWTEncoder(String secretKeyValue) {
        Log.d(TAG, "Initializing JWTEncoder");
        byte[] decodedKey = Base64.getDecoder().decode(secretKeyValue);
        secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM_TYPE);
    }

    /**
     * Create a signed token with the class algorithm.
     * Uses standard JWT claims that can be verified by any JWT library
     * (webapp JS/TS, C++ jwt-cpp, Python PyJWT, etc.)
     *
     * @param idToken idToken
     * @param email email
     * @param data additionalData
     * @return signedToken
     */
    public String createASignedToken(String idToken, String email, String data) {
        String signedToken = "";
        try {
            signedToken = Jwts.builder()
                    .setHeaderParam("typ", "JWT")
                    .signWith(secretKey)
                    .claim(ID_TOKEN, idToken)
                    .claim(EMAIL, email)
                    .claim(DATA, data)
                    .compact();
        } catch (Exception exception) {
            //Invalid Signing configuration / Couldn't convert Claims.
            Log.d(TAG, "Exception when creating a sign token: " + exception);
        }
        return signedToken;
    }

    /**
     * Decode the JWT signed object
     * @param token encodedToken
     * @return decodedToken payload as string, or empty string on failure
     */
    public String verifyAToken(String token) {
        String decodedPayload = "";

        if (token != null && !token.isEmpty()) {
            try {
                Jws<Claims> decoded = Jwts.parser()
                        .setSigningKey(secretKey)
                        .parseClaimsJws(token);
                //Get id, email and data
                String id = decoded.getBody().get(ID_TOKEN).toString();
                String email = decoded.getBody().get(EMAIL).toString();
                String data = decoded.getBody().get(DATA).toString();
                decodedPayload = "id=" + id + ", email=" + email + ", data=" + data;
                Log.d(TAG, "Decoded successfully: " + decodedPayload);

            } catch (JwtException ex) {
                Log.d(TAG, "JWT verification failed: " + ex.getMessage());
            }
        }
        return decodedPayload;
    }
}
