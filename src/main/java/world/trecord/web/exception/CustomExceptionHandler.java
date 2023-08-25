package world.trecord.web.exception;

import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import world.trecord.web.controller.ApiResponse;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static world.trecord.web.exception.CustomExceptionError.*;

@Slf4j
@RestControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Object>> customException(CustomException exception) {
        ApiResponse<Object> apiResponse = ApiResponse.of(exception.getError().getErrorCode(), exception.getError().getErrorMsg(), null);
        return ResponseEntity.status(BAD_REQUEST).body(apiResponse);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<ValidationErrorDTO>> bindException(BindException exception) {
        ValidationErrorDTO validationErrorDTO = getFieldErrorDTO(exception);
        ApiResponse<ValidationErrorDTO> apiResponse = ApiResponse.of(INVALID_ARGUMENT.getErrorCode(), INVALID_ARGUMENT.getErrorMsg(), validationErrorDTO);
        return ResponseEntity.status(BAD_REQUEST).body(apiResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> illegalArgumentException(IllegalArgumentException exception) {
        ApiResponse<Object> apiResponse = ApiResponse.of(INVALID_ARGUMENT.getErrorCode(), INVALID_ARGUMENT.getErrorMsg(), null);
        return ResponseEntity.status(BAD_REQUEST).body(apiResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> httpMessageNotReadableException(HttpMessageNotReadableException exception) {
        ApiResponse<Object> apiResponse = ApiResponse.of(INVALID_ARGUMENT.getErrorCode(), INVALID_ARGUMENT.getErrorMsg(), null);
        return ResponseEntity.status(BAD_REQUEST).body(apiResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        CustomExceptionError error = INVALID_ARGUMENT;
        ApiResponse<Object> apiResponse = ApiResponse.of(error.getErrorCode(), error.getErrorMsg(), null);
        return ResponseEntity.status(BAD_REQUEST).body(apiResponse);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Object>> noHandlerFoundException(NoHandlerFoundException exception) {
        CustomExceptionError error = CustomExceptionError.NOT_FOUND;
        ApiResponse<Object> apiResponse = ApiResponse.of(error.getErrorCode(), error.getErrorMsg(), null);
        return ResponseEntity.status(BAD_REQUEST).body(apiResponse);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        CustomExceptionError error = INVALID_REQUEST_METHOD;
        ApiResponse<Object> apiResponse = ApiResponse.of(error.getErrorCode(), error.getErrorMsg(), null);
        return ResponseEntity.status(BAD_REQUEST).body(apiResponse);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponse<Object>> jwtException(JwtException exception) {
        CustomExceptionError error = INVALID_TOKEN;
        ApiResponse<Object> apiResponse = ApiResponse.of(error.getErrorCode(), error.getErrorMsg(), null);
        return ResponseEntity.status(BAD_REQUEST).body(apiResponse);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> runtimeException(RuntimeException exception) {
        log.error("[RuntimeException in customExceptionHandler]", exception);
        CustomExceptionError error = CustomExceptionError.INTERNAL_SERVER_ERROR;
        ApiResponse<Object> apiResponse = ApiResponse.of(error.getErrorCode(), error.getErrorMsg(), null);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(apiResponse);
    }

    private ValidationErrorDTO getFieldErrorDTO(BindException exception) {
        List<ValidationErrorDTO.FieldError> fieldErrors = exception.getBindingResult().getFieldErrors().stream()
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
