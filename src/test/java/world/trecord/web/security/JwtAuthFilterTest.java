package world.trecord.web.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// TODO Test
class JwtAuthFilterTest {

    String protectedPath = "/api/v1/feeds";

    @Test
    @DisplayName("Authorization 헤더에 유효한 토큰이 있으면 다음 필터로 넘어간다")
    void doFilterInternalWithValidTokenTest() throws Exception {
        //given

        //when

        //then
    }

    @Test
    @DisplayName("Authorization에 유효하지 않은 토큰이 있으면 다음 필터로 602 에러 응답 코드로 반환한다")
    void doFilterInternalWithInvalidTokenTest() throws Exception {

        //given

        //when

        //then
    }

}