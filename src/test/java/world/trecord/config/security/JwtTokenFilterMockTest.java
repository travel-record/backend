package world.trecord.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import world.trecord.config.properties.JwtProperties;
import world.trecord.dto.users.UserContext;
import world.trecord.service.users.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class JwtTokenFilterMockTest {

    @Mock
    JwtTokenHandler jwtTokenHandler;

    @Mock
    UserService userService;

    @Mock
    HttpServletRequest req;

    @Mock
    HttpServletResponse res;

    @Mock
    FilterChain filterChain;

    @Spy
    ObjectMapper objectMapper;

    @Spy
    JwtProperties jwtProperties;

    JwtTokenFilter jwtTokenFilter;

    @Test
    @DisplayName("올바른 토큰을 가지고 요청하면 whitelist 상관없이 filterchain.doFilter이 호출된다")
    void doFilterInternalWithValidTokenTest() throws Exception {
        //given
        String validToken = "validToken";
        String whitelistPath = "/whitelist";

        jwtTokenFilter = new JwtTokenFilter(jwtProperties.getSecretKey(), jwtTokenHandler, userService, objectMapper, Map.of(whitelistPath, List.of(HttpMethod.GET)), new ArrayList<>());

        when(req.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(validToken);
        when(jwtTokenHandler.getUserIdFromToken(any(), any())).thenReturn(1L);
        when(userService.getUserContextOrException(any())).thenReturn(mock(UserContext.class));

        //when
        jwtTokenFilter.doFilterInternal(req, res, filterChain);

        //then
        verify(filterChain).doFilter(req, res);
    }

    @Test
    @DisplayName("토큰 없이 화이트리스트 URL 리소스에 대해서 요청을 하면 filterchain.doFilter이 호출된다")
    void doFilterInternalWithoutTokenToWhitelistUrlTest() throws Exception {
        //given
        String whiteListPath = "/whitelist";
        jwtTokenFilter = new JwtTokenFilter(jwtProperties.getSecretKey(), jwtTokenHandler, userService, objectMapper, Map.of(whiteListPath, List.of(HttpMethod.GET)), new ArrayList<>());

        when(req.getHeader("Authorization")).thenReturn(null);
        when(req.getServletPath()).thenReturn(whiteListPath);
        when(req.getMethod()).thenReturn(HttpMethod.GET.toString());

        //when
        jwtTokenFilter.doFilterInternal(req, res, filterChain);

        //then
        verify(filterChain).doFilter(req, res);
    }

//    TODO test 수정
//    @Test
//    @DisplayName("올바르지 않은 토큰으로 요청하면 인증 토큰 에러 응답 코드를 반환한다")
//    void doFilterInternalWithInvalidTokenTest() throws Exception {
//        //given
//        jwtTokenFilter = new JwtTokenFilter(jwtProperties.getSecretKey(), jwtTokenHandler, userService, objectMapper, Map.of("/whitelist", List.of(HttpMethod.GET)), new ArrayList<>());
//
//        String invalidToken = "invalidToken";
//        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";
//
//        ReflectionTestUtils.setField(jwtTokenFilter, "secretKey", secretKey);
//
//        when(req.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(invalidToken);
//        doThrow(new JwtException("invalid jwt exception")).when(jwtTokenHandler).verifyToken(secretKey, invalidToken);
//
//        PrintWriter mockPrintWriter = mock(PrintWriter.class);
//        when(res.getWriter()).thenReturn(mockPrintWriter);
//
//        ArgumentCaptor<String> responseContentCaptor = ArgumentCaptor.forClass(String.class);
//
//        //when
//        jwtTokenFilter.doFilterInternal(req, res, filterChain);
//
//        //then
//        verify(res).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//        verify(mockPrintWriter).write(responseContentCaptor.capture());
//
//        String responseBody = responseContentCaptor.getValue();
//        Assertions.assertThat(responseBody).contains(String.valueOf(INVALID_TOKEN.code()));
//    }
//
//    @Test
//    @DisplayName("토큰 없이 보안 URL 리소스에 대해서 요청을 하면 인증 토큰 에러 응답 코드를 반환한다")
//    void doFilterInternalWithoutTokenToSecuritylistUrlTest() throws Exception {
//        //given
//        jwtTokenFilter = new JwtTokenFilter(jwtProperties.getSecretKey(), jwtTokenHandler, userService, objectMapper, Map.of("/whitelist", List.of(HttpMethod.GET)), new ArrayList<>());
//
//        String requestUri = "/security";
//        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";
//
//        when(req.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
//        when(req.getServletPath()).thenReturn(requestUri);
//        doThrow(new JwtException("invalid jwt exception")).when(jwtTokenHandler).verifyToken(secretKey, null);
//
//        PrintWriter mockPrintWriter = mock(PrintWriter.class);
//        when(res.getWriter()).thenReturn(mockPrintWriter);
//
//        ArgumentCaptor<String> responseContentCaptor = ArgumentCaptor.forClass(String.class);
//
//        //when
//        jwtTokenFilter.doFilterInternal(req, res, filterChain);
//
//        //then
//        verify(res).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//        verify(mockPrintWriter).write(responseContentCaptor.capture());
//
//        String responseBody = responseContentCaptor.getValue();
//        Assertions.assertThat(responseBody).contains(String.valueOf(INVALID_TOKEN.code()));
//    }
//
//    @Test
//    @DisplayName("요청 URL 쿼리 파라미터에 올바른 토큰이 있으면 doFilterInternal이 실행된다")
//    void isTokenInRequestQueryParamTest() throws Exception {
//        //given
//        String validToken = "validToken";
//
//        String tokenInUrl = "/uri";
//        jwtTokenFilter = new JwtTokenFilter(jwtProperties.getSecretKey(), jwtTokenHandler, userService, objectMapper, new HashMap<>(), List.of(tokenInUrl));
//
//        when(req.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
//        when(req.getServletPath()).thenReturn(tokenInUrl + "?token=" + validToken);
//
//        when(jwtTokenHandler.getUserIdFromToken(any(), any())).thenReturn(1L);
//        when(userService.getUserContextOrException(any())).thenReturn(mock(UserContext.class));
//
//        //when
//        jwtTokenFilter.doFilterInternal(req, res, filterChain);
//
//        //then
//        verify(filterChain).doFilter(req, res);
//    }
//
//    @Test
//    @DisplayName("요청 URL 쿼리 파라미터에에 올바르지 않은 토큰이 있으면 인증 토큰 에러 응답 코드를 반환한다")
//    void isTokenInRequestQueryParamWhenInvalidTokenTest() throws Exception {
//        //given
//        String invalidToken = "invalidToken";
//        String tokenInUrl = "/uri";
//
//        jwtTokenFilter = new JwtTokenFilter(jwtProperties.getSecretKey(), jwtTokenHandler, userService, objectMapper, new HashMap<>(), List.of(tokenInUrl));
//
//        when(req.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
//        when(req.getServletPath()).thenReturn(tokenInUrl + "?token=" + invalidToken);
//
//        doThrow(new JwtException("invalid jwt exception")).when(jwtTokenHandler).verifyToken(any(), any());
//
//        PrintWriter mockPrintWriter = mock(PrintWriter.class);
//        when(res.getWriter()).thenReturn(mockPrintWriter);
//
//        ArgumentCaptor<String> responseContentCaptor = ArgumentCaptor.forClass(String.class);
//
//        //when
//        jwtTokenFilter.doFilterInternal(req, res, filterChain);
//
//        //then
//        verify(res).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//        verify(mockPrintWriter).write(responseContentCaptor.capture());
//
//        String responseBody = responseContentCaptor.getValue();
//        Assertions.assertThat(responseBody).contains(String.valueOf(INVALID_TOKEN.code()));
//    }
}