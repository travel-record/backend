package world.trecord.web.controller;

import lombok.Getter;
import lombok.Setter;

import static org.springframework.http.HttpStatus.OK;

@Setter
@Getter
public class ApiResponse<T> {

    private int code;
    private String message;
    private T data;

    private ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> of(int code, String message, T data) {
        return new ApiResponse<>(code, message, data);
    }

    public static <T> ApiResponse<T> ok(T data) {
        return of(OK.value(), OK.name(), data);
    }

    public static <T> ApiResponse<T> ok() {
        return ok(null);
    }
}
