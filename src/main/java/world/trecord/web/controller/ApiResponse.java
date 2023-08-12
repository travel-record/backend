package world.trecord.web.controller;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

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
        return of(HttpStatus.OK.value(), HttpStatus.OK.name(), data);
    }
}
