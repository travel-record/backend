package world.trecord.web.exception;

import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import world.trecord.web.controller.ApiResponse;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.*;
import static world.trecord.web.exception.CustomExceptionError.*;

@Slf4j
@RestControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse> customException(CustomException exception) {
        ApiResponse apiResponse = ApiResponse.of(exception.getError().getErrorCode(), exception.getError().getErrorMsg(), null);
        return ResponseEntity.status(BAD_REQUEST).body(apiResponse);
    }

    // TODO response에 exception cause 추가
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse> bindException(BindException exception) {
        ApiResponse apiResponse = ApiResponse.of(INVALID_ARGUMENT.getErrorCode(), INVALID_ARGUMENT.getErrorMsg(), null);
        return ResponseEntity.status(BAD_REQUEST).body(apiResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> httpMessageNotReadableException(HttpMessageNotReadableException exception) {
        ApiResponse apiResponse = ApiResponse.of(INVALID_ARGUMENT.getErrorCode(), INVALID_ARGUMENT.getErrorMsg(), null);
        return ResponseEntity.status(BAD_REQUEST).body(apiResponse);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse> noHandlerFoundException(NoHandlerFoundException exception) {
        CustomExceptionError error = CustomExceptionError.NOT_FOUND;
        ApiResponse apiResponse = ApiResponse.of(error.getErrorCode(), error.getErrorMsg(), null);
        return ResponseEntity.status(NOT_FOUND).body(apiResponse);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse> httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        CustomExceptionError error = INVALID_REQUEST_METHOD;
        ApiResponse apiResponse = ApiResponse.of(error.getErrorCode(), error.getErrorMsg(), null);
        return ResponseEntity.status(BAD_REQUEST).body(apiResponse);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponse> jwtException(JwtException exception) {
        CustomExceptionError error = INVALID_TOKEN;
        ApiResponse apiResponse = ApiResponse.of(error.getErrorCode(), error.getErrorMsg(), null);
        return ResponseEntity.status(BAD_REQUEST).body(apiResponse);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse> runtimeException(RuntimeException exception) {
        log.error("[Runtime Error] == [{}]", exception);
        CustomExceptionError error = CustomExceptionError.INTERNAL_SERVER_ERROR;
        ApiResponse apiResponse = ApiResponse.of(error.getErrorCode(), error.getErrorMsg(), null);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(apiResponse);
    }

}
