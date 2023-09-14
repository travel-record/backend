package world.trecord.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum CustomExceptionError {

    // 400-499: 기본 HTTP 에러
    INVALID_ARGUMENT(HttpStatus.BAD_REQUEST, 400, "파라미터가 올바르지 않습니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, 403, "권한이 없습니다"),
    NOT_FOUND(HttpStatus.NOT_FOUND, 404, "존재하지 않는 리소스에 대한 요청입니다"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, 405, "올바르지 않은 요청 메소드입니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 500, "서버 내부 오류"),

    // 1000-1099: 인증과 관련된 에러
    INVALID_GOOGLE_AUTHORIZATION_CODE(HttpStatus.UNAUTHORIZED, 1000, "유효하지 않은 인가 코드입니다"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, 1001, "유효하지 않은 토큰입니다"),

    // 1100-1199: 사용자와 관련된 에러
    NICKNAME_DUPLICATED(HttpStatus.CONFLICT, 1101, "이미 존재하는 닉네임입니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, 1102, "존재하지 않는 사용자입니다"),

    // 1200-1299: 피드와 관련된 에러
    USER_ALREADY_INVITED(HttpStatus.CONFLICT, 1200, "이미 초대된 사용자입니다"),
    SELF_INVITATION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, 1201, "자신을 초대할 수 없습니다"),
    SELF_EXPELLING_NOT_ALLOWED(HttpStatus.BAD_REQUEST, 1202, "자신을 내보낼 수 없습니다"),
    USER_NOT_INVITED(HttpStatus.BAD_REQUEST, 1203, "초대되지 않은 사용자입니다"),
    FEED_OWNER_LEAVING_NOT_ALLOWED(HttpStatus.BAD_REQUEST, 1204, "피드 주인은 피드에서 나갈 수 없습니다"),
    FEED_NOT_FOUND(HttpStatus.NOT_FOUND, 1205, "존재하지 않는 피드입니다"),

    // 1300-1399: 기록과 관련된 에러
    RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, 1300, "존재하지 않는 기록입니다"),

    // 1400-1499: 댓글과 관련된 에러
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, 1400, "존재하지 않는 댓글입니다"),

    // 1500-1599: 알림과 관련된 에러
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, 1500, "존재하지 않는 알림입니다"),
    NOTIFICATION_CONNECT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 1501, "알림 연결에 실패하였습니다"),
    MAX_CONNECTIONS_EXCEEDED_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 1502, "알림 연결이 최대로 설정되었습니다");

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
