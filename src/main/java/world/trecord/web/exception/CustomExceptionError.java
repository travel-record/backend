package world.trecord.web.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CustomExceptionError {

    FORBIDDEN(403, "권한이 없습니다"),
    NOT_FOUND(404, "존재하지 않는 리소스에 대한 요청입니다"),
    INVALID_REQUEST_METHOD(405, "올바르지 않은 요청 메소드입니다"),
    INTERNAL_SERVER_ERROR(500, "서버 내부 오류"),
    INVALID_GOOGLE_AUTHORIZATION_CODE(600, "유효하지 않은 인가 코드입니다"),
    INVALID_TOKEN(601, "유효하지 않은 토큰입니다"),
    INVALID_ARGUMENT(602, "파라미터가 올바르지 않습니다"),
    EXISTING_NICKNAME(700, "이미 존재하는 닉네임입니다"),
    NOT_EXISTING_USER(701, "존재하지 않는 사용자입니다"),
    NOT_EXISTING_FEED(702, "존재하지 않는 피드입니다"),
    NOT_EXISTING_RECORD(703, "존재하지 않는 기록입니다"),
    NOT_EXISTING_COMMENT(704, "존재하지 않는 댓글입니다");

    private final int errorCode;
    private final String errorMsg;

    public int code() {
        return errorCode;
    }
}
