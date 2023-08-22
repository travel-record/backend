package world.trecord.web.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.UserDetails;
import world.trecord.IntegrationTestSupport;
import world.trecord.web.service.users.UserService;

import java.io.PrintWriter;
import java.util.List;

import static org.mockito.Mockito.*;
import static world.trecord.web.exception.CustomExceptionError.INVALID_TOKEN;

@IntegrationTestSupport
class JwtAuthFilterTest {

    @Mock
    private JwtResolver jwtResolver;

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthFilter jwtAuthFilter;

    @Test
    @DisplayName("올바른 토큰을 가지고 요청하면 filterchain.doFilter이 호출된다")
    void doFilterInternalWithValidTokenTest() throws Exception {
        //given
        jwtAuthFilter = new JwtAuthFilter(jwtResolver, userService, List.of("/whitelist"));

        when(request.getHeader("Authorization")).thenReturn("validToken");
        when(jwtResolver.extractUserIdFrom("validToken")).thenReturn("1");
        when(userService.loadUserByUsername("1")).thenReturn(mock(UserDetails.class));

        //when
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        //then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("올바르지 않은 토큰을 요청하면 601 에러 응답 코드를 반환한다")
    void doFilterInternalWithInvalidTokenTest() throws Exception {
        //given
        jwtAuthFilter = new JwtAuthFilter(jwtResolver, userService, List.of("/whitelist"));

        when(request.getHeader("Authorization")).thenReturn("invalidToken");

        doThrow(new JwtException("invalid jwt exception")).when(jwtResolver).validate("invalidToken");

        PrintWriter mockPrintWriter = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(mockPrintWriter);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class); // Capture the printed value

        //when
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        //then
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(mockPrintWriter).print(captor.capture()); // Capture

        String responseBody = captor.getValue();
        Assertions.assertThat(responseBody).contains(String.valueOf(INVALID_TOKEN.getErrorCode()));
    }

    @Test
    @DisplayName("토큰 없이 화이트리스트 URL 리소스에 대해서 요청을 하면 filterchain.doFilter이")
    void doFilterInternalWithoutTokenToWhitelistUrlTest() throws Exception {
        //given
        String whiteListPath = "/whitelist";
        jwtAuthFilter = new JwtAuthFilter(jwtResolver, userService, List.of(whiteListPath));

        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getMethod()).thenReturn("GET");
        when(request.getServletPath()).thenReturn(whiteListPath);
        when(request.getContextPath()).thenReturn("");

        //when
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        //then
        verify(filterChain).doFilter(request, response);
    }

}