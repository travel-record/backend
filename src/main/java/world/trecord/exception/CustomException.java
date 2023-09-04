package world.trecord.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private CustomExceptionError error;
    private String message;

    public CustomException(CustomExceptionError error) {
        this.error = error;
    }

    public CustomException(CustomExceptionError error, String message) {
        this.error = error;
        this.message = message;
    }

    @Override
    public String getMessage() {
        String errorMsg = error.getErrorMsg();
        return message == null ? errorMsg : String.format("%s. %s", errorMsg, message);
    }

    public String message() {
        return getMessage();
    }
}
