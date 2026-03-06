package ece.iit.edu.sendrestrequest.REST_API;

/**
 * Platform-independent callback for REST API results.
 * Implement this interface in Android Activities, web controllers, or C++ JNI bridges.
 *
 * @param <T> the response type
 */
public interface RestCallback<T> {
    void onSuccess(T result);
    void onError(Throwable error);
}
