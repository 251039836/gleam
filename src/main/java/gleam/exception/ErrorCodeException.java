package gleam.exception;

public class ErrorCodeException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 696200552935293547L;

    private int errorCode;

    public ErrorCodeException(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

}
