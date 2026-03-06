package ece.iit.edu.sendrestrequest.REST_API;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Standard error response model matching the JSON error format used by
 * the webapp and C++ server implementations.
 */
public class ApiErrorResponse {
    @Expose
    @SerializedName("error")
    private String error;

    @Expose
    @SerializedName("error_code")
    private int errorCode;

    @Expose
    @SerializedName("message")
    private String message;

    public String getError() { return error; }
    public int getErrorCode() { return errorCode; }
    public String getMessage() { return message; }

    public void setError(String error) { this.error = error; }
    public void setErrorCode(int errorCode) { this.errorCode = errorCode; }
    public void setMessage(String message) { this.message = message; }

    @Override
    public String toString() {
        return "ApiError{code=" + errorCode + ", error=" + error + ", message=" + message + "}";
    }
}
