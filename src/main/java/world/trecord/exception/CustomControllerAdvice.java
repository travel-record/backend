package world.trecord.exception;

import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import world.trecord.controller.ApiResponse;

import java.util.List;

import static world.trecord.exception.CustomExceptionError.*;

@Slf4j
@RestControllerAdvice
public class CustomControllerAdvice {

    private void logException(Exception e, String description) {
        log.error("Error in [{}]: [{}] Cause: [{}]", e.getStackTrace()[0], description, e.getMessage());
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handle(CustomException e) {
        logException(e, "CustomException while processing request.");
        return ResponseEntity.status(e.getError().status()).body(ApiResponse.of(e.getError().code(), e.message(), null));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<ValidationErrorDTO>> handle(BindException e) {
        logException(e, "BindException while binding request parameters.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.of(INVALID_ARGUMENT.code(), INVALID_ARGUMENT.message(), buildFieldErrors(e)));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handle(IllegalArgumentException e) {
        logException(e, "IllegalArgumentException while processing arguments.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.of(INVALID_ARGUMENT.code(), INVALID_ARGUMENT.message(), null));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handle(HttpMessageNotReadableException e) {
        logException(e, "HttpMessageNotReadableException while reading the HTTP message.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.of(INVALID_ARGUMENT.code(), INVALID_ARGUMENT.message(), null));
    }

    @ExceptionHandler(HttpMessageNotWritableException.class)
    public ResponseEntity<ApiResponse<Void>> handle(HttpMessageNotWritableException e) {
        logException(e, "HttpMessageNotWritableException for the requested method.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.of(INVALID_ARGUMENT.code(), INVALID_ARGUMENT.message(), null));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handle(MethodArgumentTypeMismatchException e) {
        logException(e, "MethodArgumentTypeMismatchException while matching argument types.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.of(INVALID_ARGUMENT.code(), INVALID_ARGUMENT.message(), null));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handle(NoHandlerFoundException e) {
        logException(e, "NoHandlerFoundException for the requested route.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.of(NOT_FOUND.code(), NOT_FOUND.message(), null));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handle(HttpRequestMethodNotSupportedException e) {
        logException(e, "HttpRequestMethodNotSupportedException for the requested method.");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(ApiResponse.of(METHOD_NOT_ALLOWED.code(), METHOD_NOT_ALLOWED.message(), null));
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponse<Void>> handle(JwtException e) {
        logException(e, "JwtException while validating JWT.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.of(INVALID_TOKEN.code(), INVALID_TOKEN.message(), null));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handle(RuntimeException e) {
        log.error("Error in [{}] Cause: [{}]", e.getStackTrace()[0], e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.of(INTERNAL_SERVER_ERROR.code(), INTERNAL_SERVER_ERROR.message(), null));
    }

    private ValidationErrorDTO buildFieldErrors(BindException e) {
        List<ValidationErrorDTO.FieldError> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> ValidationErrorDTO.FieldError.builder()
                        .field(fieldError.getField())
                        .message(fieldError.getDefaultMessage())
                        .build())
                .toList();

        return ValidationErrorDTO.builder()
                .fieldErrors(fieldErrors)
                .build();
    }
}
