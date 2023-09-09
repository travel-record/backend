package world.trecord.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// TODO errorCode reset
@RequiredArgsConstructor
public enum CustomExceptionError {

    FORBIDDEN(HttpStatus.FORBIDDEN, 403, "권한이 없습니다"),
    NOT_FOUND(HttpStatus.NOT_FOUND, 404, "존재하지 않는 리소스에 대한 요청입니다"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, 405, "올바르지 않은 요청 메소드입니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 500, "서버 내부 오류"),
    INVALID_GOOGLE_AUTHORIZATION_CODE(HttpStatus.UNAUTHORIZED, 600, "유효하지 않은 인가 코드입니다"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, 601, "유효하지 않은 토큰입니다"),
    INVALID_ARGUMENT(HttpStatus.BAD_REQUEST, 602, "파라미터가 올바르지 않습니다"),
    USER_ALREADY_INVITED(HttpStatus.CONFLICT, 603, "이미 초대된 사용자입니다"),
    SELF_INVITATION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, 604, "자신을 초대할 수 없습니다"),
    NICKNAME_DUPLICATED(HttpStatus.CONFLICT, 700, "이미 존재하는 닉네임입니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, 701, "존재하지 않는 사용자입니다"),
    FEED_NOT_FOUND(HttpStatus.NOT_FOUND, 702, "존재하지 않는 피드입니다"),
    RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, 703, "존재하지 않는 기록입니다"),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, 704, "존재하지 않는 댓글입니다"),
    NOTIFICATION_CONNECT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 800, "알림 연결에 실패하였습니다"),
    MAX_CONNECTIONS_EXCEEDED_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 801, "알림 연결이 최대로 설정되었습니다");

    private final HttpStatus httpStatus;
    private final int errorCode;
    private final String errorMsg;

    public int code() {
        return this.errorCode;
    }

    public String message() {
        return this.errorMsg;
    }

    public HttpStatus status() {
        return this.httpStatus;
    }
}
