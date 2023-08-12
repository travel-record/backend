package world.trecord.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    CustomExceptionError error;

    public CustomException(CustomExceptionError error) {
        this.error = error;
    }
}
