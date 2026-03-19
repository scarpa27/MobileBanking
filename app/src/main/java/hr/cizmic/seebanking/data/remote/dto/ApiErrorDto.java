package hr.cizmic.seebanking.data.remote.dto;

// error response structure from api when something goes wrong
public class ApiErrorDto {
    public String timestamp;  // when error happened
    public int status;        // http status code (e.g. 400, 401, 500
    public String error;      // short error type (e.g. "Bad Request"
    public String message;    // detailed error message
    public String path;       // which api endpoint was called
}
